package com.trs.netInsight.widget.report.controller;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.entity.StatusUser;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.handler.Log;
import com.trs.netInsight.support.report.excel.DataRow;
import com.trs.netInsight.support.report.excel.ExcelData;
import com.trs.netInsight.support.report.excel.ExcelFactory;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.report.entity.ExportField;
import com.trs.netInsight.widget.report.entity.ExportParam;
import com.trs.netInsight.widget.report.entity.enums.ExportListType;
import com.trs.netInsight.widget.report.service.IExcelService;
import com.trs.netInsight.widget.report.util.ReportUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * 数据导出Controller
 *
 * Created by xiaoying on 2017/12/13.
 */
@RestController
@RequestMapping("/export")
@Slf4j
public class ExportController {

    @Autowired
    private IExcelService excelService;
	@Autowired
	private FullTextSearch hybase8SearchService;
    
    @GetMapping("/excel")
    @ApiOperation("导出到excel")
    public void export(HttpServletResponse response, @ApiParam("sid或mid或hkey") @RequestParam(value="ids",required=false) String ids,
    				@ApiParam("文章的urltime 选中导出时需要") @RequestParam(value = "urltime", required = false) String urltime,
    				@ApiParam("要生成的excel文件的名字") @RequestParam(value="name",required=false) String name,
    				@ApiParam("来源 查不同的库") @RequestParam("groupName") String groupName,
    				@ApiParam("检索条件") @RequestParam(value="trslk",required=false) String trslk,
    				@ApiParam("导出条数") @RequestParam(value="num",defaultValue="0") int num,
    				@ApiParam("在结果中搜索 (针对我的收藏按条数导出)") @RequestParam(value = "keywords", required=false) String keywords,
    				@ApiParam("导出条数（针对我的收藏和素材库按条数导出）") @RequestParam(value="dearNum",defaultValue="0") int dearNum,
					@ApiParam("论坛主贴 0 /回帖 1 ") @RequestParam(value="invitationCard",required = false) String invitationCard,
					@ApiParam("微博 原发 primary / 转发 forward ") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary,
    				@ApiParam("排序方式") @RequestParam(value="sort",defaultValue="default") String sort,
    				@ApiParam("时间(对于预警和素材库  素材库导出前多少条时传)") @RequestParam(value = "time",required=false) String time,
    				@ApiParam("对于预警 站内SMS 还是已发送SEND") @RequestParam(value="way",required=false) String way,
    				@ApiParam("导出头 ;分隔") @RequestParam(value="head",required=false) String head,
					//@ApiParam("是否处理摘要") @RequestParam(value="handleAbstract",required=false) boolean handleAbstract,
    				@ApiParam("是不是预警导出") @RequestParam(value="alert",defaultValue="false") boolean alert,
					@ApiParam("是不是用新字段导出") @RequestParam(value="isNew",defaultValue="false") boolean isNew,
					@ApiParam("是否以命中标题优先") @RequestParam(value="weight",defaultValue="false",required = false) boolean weight,
					@ApiParam("从哪个模块调用的导出方法") @RequestParam(value="moudleType",required = false) String moudleType,
					@ApiParam("素材库id（针对素材库导出前多少条）") @RequestParam(value="libraryId",required = false) String libraryId ) throws TRSException {
    	//如果是我的收藏导出  那就得在hybase搜索结果中判断是否超过5000
    	//限制导出文章数在5000以内
		String sheetName = groupName;
		if(groupName.contains(";")){
			String[] groupNameArray = groupName.split(";");
			Set<String> grouSet = new HashSet<>();
			for(String group : groupNameArray){
				grouSet.add(group);
			}
			if(grouSet.size() == 1){
				sheetName = groupNameArray[0];
			}
		}
    	String[] split = null;
    	QueryBuilder builder = new QueryBuilder();
    	if(StringUtil.isNotEmpty(ids)){
    		split = ids.split(";");
    		if(split.length>5000){
        		throw new OperationException("超过5000！");
        	}
    		builder = DateUtil.timeBuilder(urltime);
    	}
    	if(num>20000){
    		throw new OperationException("超过20000！");
    	}
//    	head = "标题;正文;发布时间;链接";
    	//导出头  前边加上序号
    	if(StringUtil.isNotEmpty(head)){
//    		if(head.contains("链接;")){//此时链接可能不在最后一个  挪到最后一个  datarow添加时好判断
//    			head = head.replace("链接;", "")+";链接";
//    		}
    		head = "序号;"+head;
    	}else{
    		head = "序号";
    	}
        boolean handleAbstract = false;
    	if (head.contains("命中词")){
    	    //head = head.replace(";命中词","");
    	    handleAbstract = true;
        }

    	boolean sim = false;
    	boolean server = false;
    	if(StringUtil.isNotEmpty(trslk)){
            trslk=RedisUtil.getString(trslk);
    		//权重
			if(StringUtil.isNotEmpty(trslk)){
				if(trslk.contains(FtsFieldConst.WEIGHT)){
					sort = "weight";
				}
				//server
				if(trslk.contains(RedisUtil.SUFFIX_KEY)){
					server = true;
				}
				if("国内新闻".equals(groupName)){
					int n = trslk.indexOf("IR_GROUPNAME");
					if(n != -1){
						String group1 = trslk.substring(n+14,n+18);
						String group2 = trslk.substring(n+13,n+17);
						if("国内博客".equals(group1)){
							sheetName = group1;
						}else if ("国内论坛".equals(group2)){
							sheetName = group2;
						}
					}
				}
			}
    	}
        try {
        	response.resetBuffer();
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.addHeader("Content-Disposition", "attachment;filename="
                    + new String((name + ".xlsx").getBytes(), "iso-8859-1"));
            ServletOutputStream outputStream = response.getOutputStream();
            //我的收藏
            if(dearNum > 0){
				head = head .replace(";命中词","");
				String headEnglish = head.replace("标题", "title").replace("正文", "content").replace("链接", "urlName")
						.replace("媒体类型", "groupName").replace("媒体名称", "siteName").replace("发布时间", "urlTime").replace("摘要","abstracts");
//				if(Const.MEDIA_TYPE_WEIBO.contains(groupName)){
//					headEnglish = headEnglish.replace("博主粉丝数","followersCount");
//				}
				excelService.exportDear(groupName, dearNum, keywords, invitationCard, forwarPrimary, head, headEnglish, moudleType, libraryId, time).writeTo(outputStream);
            }
            //预警导出 前几条的话查mysql
            if(alert){
            	ids = excelService.getIds(time, groupName, keywords, num, way)[0];
            }

			if (Const.MEDIA_TYPE_TF.contains(groupName)) {
				String headEnglish = null;
				if (isNew) {
					head = head .replace(";命中词","");
					headEnglish = head.replace("作者", "screenName").replace("发布时间", "createdAt")
							.replace("转发数", "rttCount").replace("评论数", "commtCount")
							.replace("点赞数", "approveCount").replace("正文", "content");
				} else {
					head = head .replace(";命中词","");
					headEnglish = head.replace("标题", "statusContent").replace("正文", "content").replace("链接", "urlName")
							.replace("媒体类型", "groupName").replace("媒体名称", "siteName").replace("发布时间", "urlTime")
							.replace("摘要", "abstracts");
				}
				//海外
//					headEnglish = headEnglish.replace("序号", "");
				if ("FaceBook".equals(groupName)) {
					sheetName = "FaceBook";
				} else if ("Twitter".equals(groupName)) {
					sheetName = "Twitter";
				}
				if (StringUtil.isNotEmpty(ids) && num == 0) {
					excelService.exportOversea(ids, sort, builder, head, headEnglish, sheetName).writeTo(outputStream);
				} else if (num > 0) {
					excelService.exportNumOversea(trslk, num, sort, sim, server, head, headEnglish, sheetName).writeTo(outputStream);
				}
			} else if (Const.MEDIA_TYPE_WEIBO.contains(groupName)) {
				//微博
				String headEnglish = null;
				if (isNew) {
					head = head .replace(";命中词","");
					headEnglish = head.replace("博主名称", "screenName").replace("原发博主", "retweetedScreenName")
							.replace("发布时间", "createdAt").replace("链接", "urlName")
							.replace("转发数", "rttCount").replace("评论数", "commtCount")
							.replace("点赞数", "approveCount").replace("正文", "statusContent").replace("博主粉丝数","followersCount");
				} else {
					head = head .replace(";命中词","");
					headEnglish = head.replace("标题", "title").replace("正文", "content").replace("链接", "urlName")
							.replace("媒体类型", "groupName").replace("媒体名称", "siteName").replace("发布时间", "urlTime").replace("摘要", "abstracts");
					headEnglish = headEnglish.replace("title", "statusContent").replace("content", "statusContent").replace("abstracts", "").replace("博主粉丝数","followersCount");
				}
				//微博
				sheetName = "微博";
				if (StringUtil.isNotEmpty(ids) && num == 0) {
					excelService.exportWeiBo(ids, sort, builder, head, headEnglish, sheetName).writeTo(outputStream);
				} else if (num > 0) {
					excelService.exportNumWeiBo(trslk, num, sort, sim, server, head, headEnglish, sheetName).writeTo(outputStream);
				}
			} else if (Const.MEDIA_TYPE_WEIXIN.contains(groupName)) {
				String headEnglish = null;
				if (isNew) {
					headEnglish = head.replace("标题", "urlTitle").replace("公众号名称", "siteName")
							.replace("发文时间", "urlTime").replace("链接", "urlName")
							.replace("点赞数", "prcount").replace("阅读数", "rdcount")
							.replace("正文", "content").replace("命中词", "hit");
				} else {
					headEnglish = head.replace("标题", "title").replace("正文", "content").replace("链接", "urlName")
							.replace("媒体类型", "groupName").replace("媒体名称", "siteName").replace("发布时间", "urlTime")
							.replace("摘要", "abstracts").replace("命中词", "hit");
					headEnglish = headEnglish.replace("title", "urlTitle");
				}
				sheetName = "微信";
				//微信
				if (StringUtil.isNotEmpty(ids) && num == 0) {
					excelService.exportWeiXin(trslk, ids, sort, builder, head, headEnglish, handleAbstract, sheetName).writeTo(outputStream);
				} else if (num > 0) {
					excelService.exportNumWeiXin(trslk, num, sort, sim, server, head, headEnglish, handleAbstract, sheetName).writeTo(outputStream);
				}
			} else {
				String headEnglish = null;
				if ("国内博客".equals(sheetName)) {
					sheetName = "博客";
					if (isNew) {

						headEnglish = head.replace("标题", "title").replace("作者", "authors")
								.replace("发文媒体", "siteName").replace("发文时间", "urlTime")
								.replace("链接", "urlName").replace("正文", "content")
								.replace("命中词", "hit");
					} else {
						headEnglish = head.replace("标题", "title").replace("正文", "content").replace("链接", "urlName")
								.replace("媒体类型", "groupName").replace("媒体名称", "siteName").replace("发布时间", "urlTime")
								.replace("摘要", "abstracts").replace("命中词", "hit");
					}
				} else if ("国内论坛".equals(sheetName)) {
					sheetName = "论坛";
					if (isNew) {

						headEnglish = head.replace("标题", "title")
								.replace("楼主", "authors").replace("频道", "channel")
								.replace("发文媒体", "siteName").replace("发文时间", "urlTime")
								.replace("链接", "urlName").replace("正文", "content")
								.replace("命中词", "hit");
					} else {
						headEnglish = head.replace("标题", "title").replace("正文", "content").replace("链接", "urlName")
								.replace("媒体类型", "groupName").replace("媒体名称", "siteName").replace("发布时间", "urlTime")
								.replace("摘要", "abstracts").replace("命中词", "hit");
					}
//					} else if (sheetName.contains("电子报")){
//                        sheetName = "电子报";
//                        if (isNew) {
//                            head = head+",版文";
//                            headEnglish = head.replace("标题", "title").replace("媒体名称", "siteName")
//                                    .replace("原始出处", "srcName").replace("频道", "channel")
//                                    .replace("作者", "authors").replace("发布时间", "urlTime")
//                                    .replace("链接", "urlName").replace("摘要", "abstracts")
//                                    .replace("正文", "content").replace("命中词", "hit").replace("版文","vreserved1");
//                        } else {
//                            headEnglish = head.replace("标题", "title").replace("正文", "content").replace("链接", "urlName")
//                                    .replace("媒体类型", "groupName").replace("媒体名称", "siteName").replace("发布时间", "urlTime").replace("摘要", "abstracts")
//                                    .replace("命中词", "hit");
//                        }
				}else {
					sheetName = "新闻类";
					if (isNew) {

						headEnglish = head.replace("标题", "title").replace("媒体名称", "siteName")
								.replace("原始出处", "srcName").replace("频道", "channel")
								.replace("作者", "authors").replace("发布时间", "urlTime")
								.replace("链接", "urlName").replace("摘要", "abstracts")
								.replace("正文", "content").replace("命中词", "hit");
					} else {
						headEnglish = head.replace("标题", "title").replace("正文", "content").replace("链接", "urlName")
								.replace("媒体类型", "groupName").replace("媒体名称", "siteName").replace("发布时间", "urlTime").replace("摘要", "abstracts")
								.replace("命中词", "hit");
					}
				}
				//混合的
				if (StringUtil.isNotEmpty(ids) && num == 0) {
					excelService.export(trslk, ids, sort, builder, head, headEnglish, handleAbstract, sheetName).writeTo(outputStream);
				} else if (num > 0) {
					excelService.exportNum(trslk, num, sort, sim, server, head, headEnglish, handleAbstract, sheetName,weight).writeTo(outputStream);
				}
			}
		} catch (Exception e) {
			log.error("导出excel出错,错误信息:{}", e);
		}
    }

	/**
	 * 选中导出的时候id和groupname一一对应  分号隔开
	 * @param groupName
	 * @param mixId
	 * @param urltime
	 * @param id
	 * @param num
	 * @param keywords
	 * @return
	 * @throws TRSException
	 */
    @PostMapping("/excelMix")
    @ApiOperation("混合列表导出到excel")
    @FormatResult
    public Object exportMix(
    				@ApiParam("来源") @RequestParam(value="groupName",required=false) String groupName,
    				@ApiParam("各种主键") @RequestParam(value="mixId",required=false) String mixId,
    				@ApiParam("文章的urltime 选中导出时需要") @RequestParam(value = "urltime", required = false) String urltime,
    				@ApiParam("按条数导出 三级栏目id或者专项Id") @RequestParam(value="id",required=false) String id,
    				@ApiParam("导出条数") @RequestParam(value="num",defaultValue="0") int num,
                    @ApiParam("检索条件") @RequestParam(value="trslk",required=false) String trslk,
					//@ApiParam("是否处理摘要") @RequestParam(value="handleAbstract",required=false) boolean handleAbstract,
    				@ApiParam("在结果中搜索 ") @RequestParam(value = "keywords", required=false) String keywords) throws TRSException {
    	//限制导出文章数在5000以内
    	QueryBuilder timeBuilder = new QueryBuilder();
    	String[] split = null;
    	if(StringUtil.isNotEmpty(mixId)){
    		split = mixId.split(";");
    		if(split.length>5000){
        		throw new OperationException("混合列表导出条数不能超过5000");
        	}
    		if(StringUtil.isNotEmpty(urltime)){
    			timeBuilder = DateUtil.timeBuilder(urltime);
    		}else{
    			timeBuilder = null;
    		}
    	}
    	if(num>5000){
    		throw new OperationException("混合列表导出条数不能超过5000");
    	}
      //  boolean server = false;
        if(StringUtil.isNotEmpty(trslk)){
            trslk=RedisUtil.getString(trslk);
            //权重
//            if(trslk.contains(FtsFieldConst.WEIGHT)){
//                sort = "weight";
//            }
            //server
//            if(trslk.contains(RedisUtil.SUFFIX_KEY)){
//                server = true;
//            }
            if (null == timeBuilder){
                timeBuilder = new QueryBuilder();
            }
            timeBuilder.filterByTRSL(trslk);
        }
        try {
            if(StringUtil.isNotEmpty(mixId)){
            	String exportMix = excelService.exportMix(id,mixId, groupName,timeBuilder);
            	return exportMix;
            }
        } catch (Exception e) {
            log.error("导出excel出错,错误信息:{}", e);
        }
		return "";
    }

	/**
	 * 选中导出的时候id和groupname一一对应  分号隔开
	 * @param groupName
	 * @param num
	 * @param keywords
	 * @return
	 * @throws TRSException
	 */
	@PostMapping("/exportAlertMixNew")
	@ApiOperation("预警混合列表导出")
	@FormatResult
	public Object exportAlertMixNew(
			@ApiParam("来源") @RequestParam(value="groupName",required=false) String groupName,
			@ApiParam("时间(对于预警和素材库  素材库导出前多少条时传)") @RequestParam(value = "time",required=false) String time,
			@ApiParam("对于预警 站内SMS 还是已发送SEND") @RequestParam(value="way",required=false) String way,
			@ApiParam("导出条数") @RequestParam(value="num",defaultValue="0") int num,
			@ApiParam("在结果中搜索 ") @RequestParam(value = "keywords", required=false) String keywords) throws TRSException {
		//打印请求参数
		SpringUtil.getParameterMap();
		String[] idAndGroAndTime = null;
		if (num > 0){
			try {
				idAndGroAndTime = excelService.getIds(time, groupName, keywords, num, way);
			}catch (Exception e){
				log.error("预警导出错误",e);
			}
		}
		//限制导出文章数在5000以内
		QueryBuilder timeBuilder = new QueryBuilder();
		String[] split = null;
		timeBuilder = DateUtil.timeBuilder(idAndGroAndTime[2]);
		if(num>5000){
			throw new OperationException("混合列表导出条数不能超过5000");
		}

		try {
			String uuid = UUID.randomUUID().toString().replaceAll("-","");
			String exportMix = excelService.exportAllMix(uuid,idAndGroAndTime[0], idAndGroAndTime[1],timeBuilder);
			return exportMix;

		} catch (Exception e) {
			log.error("导出excel出错,错误信息:{}", e);
		}
		return "";
	}

	/**
	 * 选中导出的时候id和groupname一一对应  分号隔开
	 * @param groupName
	 * @param mixId
	 * @param urltime
	 * @param id
	 * @param num
	 * @param keywords
	 * @return
	 * @throws TRSException
	 */
	@PostMapping("/exportMixNew")
	@ApiOperation("混合列表导出到excel")
	@FormatResult
	public Object exportMixNew(
			@ApiParam("来源") @RequestParam(value="groupName",required=false) String groupName,
			@ApiParam("各种主键") @RequestParam(value="mixId",required=false) String mixId,
			@ApiParam("文章的urltime 选中导出时需要") @RequestParam(value = "urltime", required = false) String urltime,
			@ApiParam("按条数导出 三级栏目id或者专项Id") @RequestParam(value="id",required=false) String id,
			@ApiParam("导出条数") @RequestParam(value="num",defaultValue="0") int num,
			@ApiParam("检索条件") @RequestParam(value="trslk",required=false) String trslk,
			@ApiParam("在结果中搜索 ") @RequestParam(value = "keywords", required=false) String keywords) throws TRSException {
		//打印请求参数
		SpringUtil.getParameterMap();
		//限制导出文章数在5000以内
		QueryBuilder timeBuilder = new QueryBuilder();
		String[] split = null;
		if(StringUtil.isNotEmpty(mixId)){
			split = mixId.split(";");
			if(split.length>5000){
				throw new OperationException("混合列表导出条数不能超过5000");
			}
			if(StringUtil.isNotEmpty(urltime)){
				timeBuilder = DateUtil.timeBuilder(urltime);
			}else{
				timeBuilder = null;
			}
		}
		if(num>5000){
			throw new OperationException("混合列表导出条数不能超过5000");
		}
		//  boolean server = false;
		if(StringUtil.isNotEmpty(trslk)){
			trslk=RedisUtil.getString(trslk);
			if (null == timeBuilder){
				timeBuilder = new QueryBuilder();
			}
			timeBuilder.filterByTRSL(trslk);
		}
		try {
			if(StringUtil.isNotEmpty(mixId)){
				String exportMix = excelService.exportAllMix(id,mixId, groupName,timeBuilder);
				return exportMix;
			}
		} catch (Exception e) {
			log.error("导出excel出错,错误信息:{}", e);
		}
		return "";
	}
    
    @GetMapping("/exportFromRedis")
    @ApiOperation("按照id导出从redis取值")
    public void exportFromRedis(HttpServletResponse response,
    		@ApiParam("是否按照条数导出 是：true") @RequestParam(value="isNum") boolean isNum,
    		@ApiParam("返回的key") @RequestParam(value="key") String key,
    		@ApiParam("导出头 ;分隔") @RequestParam(value="head",required=false) String head,
    		@ApiParam("要生成文件的名字") @RequestParam(value="name") String name,
			@ApiParam("导出头 ;分隔") @RequestParam(value="groupName",required=false) String groupName) throws TRSException{
    	response.resetBuffer();
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
		//打印请求参数
		SpringUtil.getParameterMap();
        try {
			response.addHeader("Content-Disposition", "attachment;filename="
			        + new String((name + ".xlsx").getBytes(), "iso-8859-1"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
//    	head = "标题;正文;链接;发布时间";
    	//导出头  前边加上序号
    	if(StringUtil.isNotEmpty(head)){
//    		if(head.contains("链接;")){//此时链接可能不在最后一个  挪到最后一个  datarow添加时好判断
//    			head = head.replace("链接;", "")+";链接";
//    		}
    		head = "序号;"+head;
    	}else{
    		head = "序号";
    	}
        boolean handleAbstract = false;
        if (head.contains("命中词")){
            //head = head.replace(";命中词","");
            handleAbstract = true;
        }
    	String headEnglish = head.replace("标题", "title").replace("正文", "content").replace("链接", "urlName")
    	    	.replace("媒体类型", "groupName").replace("媒体名称", "siteName").replace("发布时间", "urlTime").replace("摘要","abstracts").replace("命中词", "hit");
    	String headArray[] = head.split(";");
    	String englishArray[] = headEnglish.split(";");
        //按照前端传过来的id排序
        ExcelData data = new ExcelData();
        data.setHead(headArray);
        try {
        	 ServletOutputStream outputStream = response.getOutputStream();
        	 List<FtsDocumentCommonVO> mix = RedisUtil.getMix(key);
//        	if(isNum){
//        		data.setHead(ExcelConst.HEAD_VALUE_MIX);
//        	}else{
//        		data.setHead(ExcelConst.HEAD_VALUE);
//        	}
        	for(int i=0;i<mix.size();i++){
        		FtsDocumentCommonVO vo = mix.get(i);
                if (handleAbstract && (Const.MEDIA_TYPE_NEWS.contains(vo.getGroupName()) || Const.MEDIA_TYPE_WEIXIN.contains(vo.getGroupName()))){
					String toAbstract = ReportUtil.contentToAbstract(vo.getContent());
					String anAbstract = "";
					String anAbstractNew = "";
					if (StringUtil.isEmpty(toAbstract)){
						anAbstract = vo.getUrlTitle().replaceAll("<font color=red>","").replaceAll("</font>","");
						anAbstractNew = anAbstract +"。"+ toAbstract;
					}else {
						anAbstractNew = toAbstract;
					}
					if (StringUtil.isNotEmpty(anAbstractNew)){
						vo.setHit(StringUtil.replaceImg(StringUtil.replaceFont(anAbstractNew)));
					}
                	//vo.setAbstracts(ReportUtil.contentToAbstract(vo.getContent()));
                }
        		excelService.putData(englishArray, vo, data, i);
    		}
        	 ByteArrayOutputStream export = ExcelFactory.getInstance().export(data);
			 export.writeTo(outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	@GetMapping("/exportFromRedisNew")
	@ApiOperation("按照id导出从redis取值")
	public void exportFromRedisNew(HttpServletResponse response,
								   @ApiParam("是否按照条数导出 是：true") @RequestParam(value = "isNum") boolean isNum,
								   @ApiParam("返回的key") @RequestParam(value = "key") String key,
								   @ApiParam("要生成文件的名字") @RequestParam(value = "name") String name,
								   @ApiParam("导出数据的数据源") @RequestParam(value = "groupName", required = false) String groupName,
								   @ApiParam("不同数据源的导出头") @RequestParam(value = "heads", required = false) String heads) throws TRSException {
		response.resetBuffer();
		response.setContentType("application/vnd.ms-excel;charset=utf-8");
		try {
			response.addHeader("Content-Disposition", "attachment;filename="
					+ new String((name + ".xlsx").getBytes(), "iso-8859-1"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (groupName.length() == 0 || groupName == null) {
			throw new TRSException("混合导出必须选择数据源");
		} else if(heads.split(":").length == 0 || heads.split(":") == null){
			throw new TRSException("混合导出必须有一项选择数据源");
		} else {
			ExcelData data = new ExcelData();
			Map<String, List<FtsDocumentCommonVO>> groupMap = new LinkedHashMap<>();
			String[] groupArray = groupName.split(";");
			Map<String, Boolean> handleAbstractMap = new HashMap<>();
			Map<String, String[]> headEnglishMap = new HashMap<>();
			Map<String, String[]> headMap = new HashMap<>();
			String[] head = heads.split(":");
			String content = "content";
			if(isNum){
				content = "exportContent";
			}else{
				content = "content";
			}
			for (int i = 0; i < groupArray.length; i++) {
				groupMap.put(groupArray[i], new ArrayList<FtsDocumentCommonVO>());
				handleAbstractMap.put(groupArray[i], false);
				if (StringUtil.isNotEmpty(head[i])) {
					head[i] = "序号;" + head[i];
				} else {
					head[i] = "序号";
				}
				if (head[i].contains("命中词")) {
					//head[i] = head[i].replace(";命中词", "");
					handleAbstractMap.put(groupArray[i], true);
				}
				String headEnglish = "";
				if ("新闻类".equals(groupArray[i])) {
					headEnglish = head[i].replace("标题", "title").replace("媒体名称", "siteName")
							.replace("原始出处", "srcName").replace("频道", "channel")
							.replace("作者", "authors").replace("发布时间", "urlTime")
							.replace("链接", "urlName").replace("摘要", "abstracts")
							.replace("正文", content).replace("命中词", "hit");

				} else if ("微博".equals(groupArray[i])) {
					head[i] = head[i].replace(";命中词", "");
					headEnglish = head[i].replace("博主名称", "screenName").replace("原发博主", "retweetedScreenName")
							.replace("发布时间", "createdAt").replace("链接", "urlName")
							.replace("转发数", "rttCount").replace("评论数", "commtCount")
							.replace("点赞数", "approveCount").replace("正文", content).replace("博主粉丝数","followersCount");

				} else if ("微信".equals(groupArray[i])) {
					headEnglish = head[i].replace("标题", "urlTitle").replace("公众号名称", "siteName")
							.replace("发文时间", "urlTime").replace("链接", "urlName")
							.replace("点赞数", "prcount").replace("阅读数", "rdcount")
							.replace("正文", content).replace("命中词", "hit");

				} else if ("论坛".equals(groupArray[i])) {
					headEnglish = head[i].replace("标题", "title")
							.replace("楼主", "authors").replace("频道", "channel")
							.replace("发文媒体", "siteName").replace("发文时间", "urlTime")
							.replace("链接", "urlName").replace("正文", content)
							.replace("命中词", "hit");

				} else if ("博客".equals(groupArray[i])) {
					headEnglish = head[i].replace("标题", "title").replace("作者", "authors")
							.replace("发文媒体", "siteName").replace("发文时间", "urlTime")
							.replace("链接", "urlName").replace("正文", content)
							.replace("命中词", "hit");

				} else if ("FaceBook".equals(groupArray[i]) || "Twitter".equals(groupArray[i])) {
					head[i] = head[i].replace(";命中词", "");
					headEnglish = head[i].replace("作者", "authors").replace("发布时间", "createdAt")
							.replace("转发数", "rttCount").replace("评论数", "commtCount")
							.replace("点赞数", "approveCount").replace("正文", content);
				}
				String[] headArray = head[i].split(";");
				String[] englishArray = headEnglish.split(";");
				if (headArray.length >2 && "正文".equals(headArray[headArray.length - 2]) && "命中词".equals(headArray[headArray.length - 1])) {
					String field = headArray[headArray.length - 1];
					headArray[headArray.length - 1] = headArray[headArray.length - 2];
					headArray[headArray.length - 2] = field;
					String engLishField = englishArray[englishArray.length - 1];
					englishArray[englishArray.length - 1] = englishArray[englishArray.length - 2];
					englishArray[englishArray.length - 2] = engLishField;
				}
				headMap.put(groupArray[i], headArray);
				data.putHeadMap(groupArray[i], Arrays.asList(headArray));
				headEnglishMap.put(groupArray[i], englishArray);
			}
			try {
				ServletOutputStream outputStream = response.getOutputStream();
				List<FtsDocumentCommonVO> mix = RedisUtil.getMix(key);
				for (FtsDocumentCommonVO vo : mix) {
					if ("国内新闻".equals(vo.getGroupName()) || "国内新闻_手机客户端".equals(vo.getGroupName()) || "国内新闻_电子报".equals(vo.getGroupName()) || "国外新闻".equals(vo.getGroupName())) {
						List<FtsDocumentCommonVO> list = groupMap.get("新闻类");
						list.add(vo);
						groupMap.put("新闻类", list);
					} else if ("微博".equals(vo.getGroupName())) {
						List<FtsDocumentCommonVO> list = groupMap.get("微博");
						if (heads.contains("博主粉丝数")) {
							QueryBuilder queryBuilder = new QueryBuilder();
							queryBuilder.filterField(FtsFieldConst.FIELD_URLNAME, "\"" + vo.getUrlName() + "\"", Operator.Equal);
							queryBuilder.setDatabase(Const.WEIBO);
							//放入对应 微博用户信息
							if (ObjectUtil.isEmpty(vo.getScreenName()) && ObjectUtil.isEmpty(vo.getScreenName())){
								vo.setFollowersCount(0);
							}else {
								StatusUser statusUser = queryStatusUser(vo.getScreenName(), vo.getUid());
								if (ObjectUtil.isNotEmpty(statusUser)) {
									vo.setFollowersCount(statusUser.getFollowersCount());
								}else {
									vo.setFollowersCount(0);
								}
							}
						}
						list.add(vo);
						groupMap.put("微博", list);
					} else if ("国内微信".equals(vo.getGroupName()) || "微信".equals(vo.getGroupName())) {
						List<FtsDocumentCommonVO> list = groupMap.get("微信");
						list.add(vo);
						groupMap.put("微信", list);
					} else if ("FaceBook".equals(vo.getGroupName()) || "Facebook".equals(vo.getGroupName())) {
						List<FtsDocumentCommonVO> list = groupMap.get("FaceBook");
						list.add(vo);
						groupMap.put("FaceBook", list);
					} else if ("Twitter".equals(vo.getGroupName())) {
						List<FtsDocumentCommonVO> list = groupMap.get("Twitter");
						list.add(vo);
						groupMap.put("Twitter", list);
					} else if ("国内论坛".equals(vo.getGroupName())) {
						List<FtsDocumentCommonVO> list = groupMap.get("论坛");
						list.add(vo);
						groupMap.put("论坛", list);
					} else if ("国内博客".equals(vo.getGroupName())) {
						List<FtsDocumentCommonVO> list = groupMap.get("博客");
						list.add(vo);
						groupMap.put("博客", list);
					}
				}
				for (Map.Entry<String, List<FtsDocumentCommonVO>> entry : groupMap.entrySet()) {
					List<FtsDocumentCommonVO> voList = entry.getValue();
					if (voList.size() == 0) {
						data.putSheet(entry.getKey(), new ArrayList<DataRow>());
					} else {
						for (int i = 0; i < voList.size(); i++) {
							FtsDocumentCommonVO vo = voList.get(i);
							if (handleAbstractMap.get(entry.getKey()) && (Const.MEDIA_TYPE_NEWS.contains(vo.getGroupName()) || Const.MEDIA_TYPE_WEIXIN.contains(vo.getGroupName()))) {
								String toAbstract = "";
								if(isNum){
									toAbstract = ReportUtil.contentToAbstract(vo.getExportContent());
								}else{
									toAbstract = ReportUtil.contentToAbstract(vo.getContent());
								}
								String anAbstract = "";
								String anAbstractNew = "";
								if (StringUtil.isEmpty(toAbstract)) {
									anAbstract = vo.getUrlTitle().replaceAll("<font color=red>", "").replaceAll("</font>", "");
									anAbstractNew = anAbstract + "。" + toAbstract;
								} else {
									anAbstractNew = toAbstract;
								}
								if (StringUtil.isNotEmpty(anAbstractNew)) {
									vo.setHit(StringUtil.replaceImg(StringUtil.replaceFont(anAbstractNew)));
								}
								//vo.setAbstracts(ReportUtil.contentToAbstract(vo.getContent()));
							}
							excelService.putDataSheet(headEnglishMap.get(entry.getKey()), vo, data, i, entry.getKey());
						}
					}
				}
				ByteArrayOutputStream export = ExcelFactory.getInstance().exportOfManySheet(data);
				export.writeTo(outputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 当前文章对应的用户信息
	 * @throws TRSException
	 */
	private StatusUser queryStatusUser(String screenName,String uid) throws TRSException{
		QueryBuilder queryStatusUser = new QueryBuilder();
		queryStatusUser.filterField(FtsFieldConst.FIELD_SCREEN_NAME,"\""+screenName+"\"",Operator.Equal);
		queryStatusUser.setDatabase(Const.SINAUSERS);
		//查询微博用户信息
		List<StatusUser> statusUsers = hybase8SearchService.ftsQuery(queryStatusUser, StatusUser.class, false, false,false,null);
		if (ObjectUtil.isEmpty(statusUsers)){
			QueryBuilder queryStatusUser1 = new QueryBuilder();
			queryStatusUser1.filterField(FtsFieldConst.FIELD_UID,"\""+uid+"\"",Operator.Equal);
			queryStatusUser1.setDatabase(Const.SINAUSERS);
			//查询微博用户信息
			statusUsers = hybase8SearchService.ftsQuery(queryStatusUser1, StatusUser.class, false, false,false,null);
		}
		if (ObjectUtil.isNotEmpty(statusUsers)){
			//放入该条微博对应的 发布人信息
			return statusUsers.get(0);
		}
		return null;

	}

	/**
	 * 选中导出的时候id和groupname一一对应  分号隔开
	 *
	 * @param ids
	 * @param groupNames
	 * @param urltimes
	 * @param trslk
	 * @param num
	 * @return
	 * @throws TRSException
	 */
	@Log(systemLogOperation = SystemLogOperation.EXPORT_EXCEL_SELECT_DATA, systemLogType = SystemLogType.EXPORT_EXCEL,systemLogOperationPosition="日常监测",methodDescription = "导出选择excel")
	@PostMapping("/exportForCheck")
	@ApiOperation("将选中的数据导出到excel")
	@FormatResult
	public Object exportForCheck(HttpServletResponse response, @ApiParam("sid或mid或hkey 被选中数据的主键 用;分割") @RequestParam(value = "ids") String ids,
								 @ApiParam("groupNames 数据来源，与id、urltime一一对应，数量一致 用;分割") @RequestParam(value = "groupNames") String groupNames,
								 @ApiParam("urltime 被选中数据的时间 与id、groupName一一对应，数量一致 用;分割") @RequestParam(value = "urltimes") String urltimes,
								 @ApiParam("检索表达式，普通列表页都有，收藏、素材库、已发预警没有") @RequestParam(value = "trslk", required = false) String trslk,
								 @ApiParam("导出条数") @RequestParam(value = "num", defaultValue = "0") Integer num) throws TRSException {

		if (StringUtil.isEmpty(ids)) {
			throw new OperationException("所选id为空，不能导出");
		}
		if (StringUtil.isEmpty(groupNames)) {
			throw new OperationException("所选数据的来源为空，不能导出");
		}
		if (StringUtil.isEmpty(urltimes)) {
			throw new OperationException("所选数据的urltime为空，不能导出");
		}
		if (!(ids.split(";").length == groupNames.split(";").length && ids.split(";").length == urltimes.split(";").length)) {
			throw new OperationException("所选数据的id、groupname、urltime的数量不相符");
		}
		if (num < 1) {
			throw new OperationException("所选数据数量小于1");
		}
		String[] timeArray = urltimes.split(";");
		for (String time : timeArray) {
			if (!DateUtil.isTimeFormatter(time)) {
				throw new OperationException("所选数据的urltime 格式错误，不能导出，为:" + time + "不能导出");
			}
		}
		if (ids.split(";").length > 5000 || num > 5000) {
			throw new OperationException("选中导出时，导出条数不能超过5000");
		}

		QueryBuilder builder = DateUtil.timeBuilder(urltimes);
		if (StringUtil.isNotEmpty(trslk)) {
			trslk = RedisUtil.getString(trslk);
			if (null == builder) {
				builder = new QueryBuilder();
			}
			builder.filterByTRSL(trslk);
		}
		try {
			Object key = excelService.queryByIds(ids, groupNames, builder);
			return key;
		} catch (Exception e) {
			log.error("导出excel出错,错误信息:{}", e);
		}
		return null;
	}

	/**
	 * 选中导出的时候id和groupname一一对应  分号隔开
	 *
	 * @param groupName
	 * @param trslk
	 * @param num
	 * @param type
	 * @param time
	 * @param sort
	 * @param libraryId
	 * @param invitationCard
	 * @param forwarPrimary
	 * @return
	 * @throws TRSException
	 */
	@Log(systemLogOperation = SystemLogOperation.EXPORT_EXCEL_SELECT_DATA, systemLogType = SystemLogType.EXPORT_EXCEL,systemLogOperationPosition="---",methodDescription = "导出当前列表的前N条数据到excel")
	@PostMapping("/exportForNum")
	@ApiOperation("导出当前列表的前N条数据到excel")
	@FormatResult
	public Object exportForNum(HttpServletResponse response,
							   @ApiParam("groupName 数据来源 与导出excel的sheet页名字相同，用;分割") @RequestParam(value = "groupName") String groupName,
							   @ApiParam("检索表达式，普通列表页都有，收藏、素材库、已发预警没有") @RequestParam(value = "trslk", required = false) String trslk,
							   @ApiParam("导出条数") @RequestParam(value = "num", defaultValue = "0") Integer num,
							   @ApiParam("类型，区分所查询列表  普通列表页、普通搜索列表页、高级搜索列表页、预警、相似文章、已发预警、站内预警、收藏、素材库") @RequestParam(value = "type", required = false) String type,
							   @ApiParam("排序方式") @RequestParam(value = "sort", defaultValue = "default") String sort,
							   @ApiParam("标题权重 - 影响排序方式") @RequestParam(value = "weight", defaultValue = "false", required = false) boolean weight,
							   @ApiParam("素材库id 只有素材库导出时需要") @RequestParam(value = "libraryId", required = false) String libraryId,
							   @ApiParam("time 检索时间 针对已发预警和素材库 这两个都是在页面上选择时间") @RequestParam(value = "time", defaultValue = "0d") String time,
							   @ApiParam("在结果中搜索") @RequestParam(value = "fuzzyValue", required = false) String fuzzyValue,
							   @ApiParam("在结果中搜索de范围") @RequestParam(value = "fuzzyValueScope", defaultValue = "fullText",required = false) String fuzzyValueScope,
							   @ApiParam("接收者 已发预警和站内预警使用，针对用账号筛选") @RequestParam(value = "receivers", defaultValue = "ALL") String receivers,
							   @ApiParam("论坛主贴回帖 针对收藏、已发预警和素材库") @RequestParam(value = "invitationCard", required = false) String invitationCard,
							   @ApiParam("微博转发原发 针对收藏、已发预警和素材库") @RequestParam(value = "forwarPrimary", required = false) String forwarPrimary) throws TRSException {
		if (num < 1) {
			throw new OperationException("导出数据数量小于1");
		}
		if (num > 20000) {
			throw new OperationException("导出数据最多是20000条数据，现在是：" + num);
		}
		if (StringUtil.isEmpty(groupName)) {
			throw new OperationException("数据来源为空");
		}
		ExportListType exportListType = ExportListType.valueOf(type);
		String trs = "";
		if (ExportListType.COMMON.equals(exportListType)|| ExportListType.ORDINARYSEARCH.equals(exportListType) || ExportListType.ALERT.equals(exportListType) || ExportListType.SIM.equals(exportListType)|| ExportListType.CHART2LIST.equals(exportListType)) {
			if (StringUtil.isEmpty(trslk)) {
				throw new OperationException("普通数据列表，trslk为空，无法准确获取数据列表，请刷新");
			}
			trs = RedisUtil.getString(trslk);
			if (StringUtil.isEmpty(trs)) {
				throw new OperationException("普通数据列表，通过trslk没有获取到查询表达式，无法准确获取数据列表，请刷新");
			}
		}
		if (StringUtil.isEmpty(libraryId) && ExportListType.LIBRARY.equals(exportListType)) {
			throw new OperationException("素材库导出，没有对应素材库id，无法导出");
		}
		String[] source = groupName.split(";");
		for (int i = 0; i < source.length; i++) {
			if (Const.EXPORT_SHEET_SOURCE.containsKey(source[i])) {
				source[i] = Const.EXPORT_SHEET_SOURCE.get(source[i]);
			} else {
				throw new OperationException("所传数据类型与要生成的excel工作名不相符");
			}
		}
		Object key = null;
		if (ExportListType.COMMON.equals(exportListType) || ExportListType.ORDINARYSEARCH.equals(exportListType) ||ExportListType.ADVANCEDSEARCH.equals(exportListType) || ExportListType.ALERT.equals(exportListType) || ExportListType.SIM.equals(exportListType) || ExportListType.CHART2LIST.equals(exportListType)) {
			//都是直接从hybase拿取数据
			//预警列表查询时间用hybase_loadtime，相似文章和专题分析图表 则是默认排序方式不同
			key = excelService.queryByNum(exportListType, trs, sort, num, source,weight);
		} else {
			key = excelService.queryByNumOfOther(exportListType,num,source,time,libraryId,invitationCard,forwarPrimary,fuzzyValue,fuzzyValueScope,receivers);
		}
		return key;
	}

	@PostMapping("/exportExcelData")
	@ApiOperation("把前一个接口的数据从redis中导出到excel中")
	public void exportExcelData(HttpServletResponse response,
								@ApiParam("要生成文件的名字") @RequestBody ExportParam exportParam
								/*@ApiParam("返回的key") @RequestParam(value = "key") String key,
								@ApiParam("要生成文件的名字") @RequestParam(value = "name") String name,
                                @ApiParam("导出数据的数据源") @RequestParam(value = "groupName", required = false) String groupName,
                                @ApiParam("不同数据源的导出头") @RequestParam(value = "heads", required = false) String heads*/) throws TRSException {
		String name = exportParam.getName();
		String key = exportParam.getKey();
		List<ExportField> exportField = exportParam.getExportField();
		if (StringUtil.isEmpty(name)) {
			throw new OperationException("导出文件名为空");
		}
		if (StringUtil.isEmpty(key)) {
			throw new OperationException("从redis获取数据的key值为空");
		}
		if (exportField == null || exportField.size() == 0) {
			throw new OperationException("excel导出字段为空");
		}
		List<FtsDocumentCommonVO> volist = RedisUtil.getMix(key);
		if (volist == null) {
			throw new OperationException("从redis中没有获取到数据");
		}
		//缓存用过之后从redis清除
		boolean isSuc = RedisUtil.deleteKey(key);
		log.info("导出过后,清除redis中数据----->"+isSuc);

		response.resetBuffer();
		response.setContentType("application/vnd.ms-excel;charset=utf-8");
		try {
			response.addHeader("Content-Disposition", "attachment;filename="
					+ new String((name + ".xlsx").getBytes(), "iso-8859-1"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		ExcelData data = new ExcelData();//字段值

		Map<String, Map<String, Object>> map = new LinkedHashMap<>();
		Map<String, Object> childMap = null;
		String headKey = "head";
		String englishHeadKey = "englishHead";
		String hitWordKey = "hitWord";
		String hitKey = "hit";
		String followersCountKey = "followersCount";
		String vreservedKey = "vreserved";
		for (ExportField ef : exportField) {
			String groupName = ef.getGroupName();
			String heads = ef.getHead();
			if (StringUtil.isNotEmpty(heads)) {
				heads = "序号;" + heads;
			} else {
				heads = "序号";
			}
			String[] headArray = heads.split(";");
			String[] englishHeadArray = new String[headArray.length];
			Boolean isHitWord = false;
			Boolean isHit = false;
			Boolean isStatusUser = false;
			Boolean isVreserved = false;
			Map<String, String> fieldMap = Const.EXPORT_EXCEL_SHEET_FIELD.get(groupName);
			for (int i = 0; i < headArray.length; i++) {

				if (fieldMap.containsKey(headArray[i])) {
					englishHeadArray[i] = fieldMap.get(headArray[i]);
					if ("hitWord".equals(englishHeadArray[i])) {
						isHitWord = true;
					}
					if ("hit".equals(englishHeadArray[i])) {
						isHit = true;
					}
					if ("followersCount".equals(englishHeadArray[i])) {
						isStatusUser = true;
					}
					if("vreserved".equals(englishHeadArray[i])){
						isVreserved = true;
					}
				} else {
					throw new OperationException("需要导出的excel字段值错误，为：" + headArray[i]);
				}
			}
			childMap = new HashMap<>();
			//childMap.put(headKey, headArray);
			childMap.put(englishHeadKey, englishHeadArray);
			childMap.put(hitWordKey, isHitWord);
			childMap.put(hitKey, isHit);
			childMap.put(followersCountKey, isStatusUser);
			childMap.put(vreservedKey,isVreserved );
			map.put(groupName, childMap);

			data.putHeadMap(groupName, Arrays.asList(headArray));
			data.putSheets(groupName, null);
		}
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			if (volist.size() > 0) {
				for (FtsDocumentCommonVO vo : volist) {
					String sheetName = Const.EXPORT_SOURCE_EXCEL_SHEET.get(vo.getGroupName());
					if (StringUtil.isEmpty(sheetName)) {
						continue;
						//因为导出文件会报错，太明显了，直接跳过
						// throw new OperationException("错误的数据类型：" + vo.getGroupName());
					} else {
						childMap = map.get(sheetName);
						if (!(childMap == null || childMap.size() == 0)) {
							//如果该条数据不在应导出的数据类型中，则不导出
							String[] englishHeadArray = (String[]) childMap.get(englishHeadKey);
							Boolean isHitWord = (Boolean) childMap.get(hitWordKey);
							Boolean isHit = (Boolean) childMap.get(hitKey);
							Boolean isStatusUser = (Boolean) childMap.get(followersCountKey);
							Boolean isVreserved = (Boolean) childMap.get(vreservedKey);
							vo.setHit(ReportUtil.calcuHit(vo.getTitle(), vo.getExportContent(), isHit));
							vo.setHitWord(ReportUtil.calcuRedWord(isHitWord, vo.getTitle(), vo.getExportContent()));
							if (isStatusUser) {
								vo.setFollowersCount(getFollowersCount(isStatusUser, vo.getUrlName(), vo.getScreenName(), vo.getUid()));
							}
							if(isVreserved){
								vo.setVreserved(vo.getVreserved());
							}

							LinkedHashMap<Integer, List<DataRow>> sheet = data.getOneSheet(sheetName);
							int size = 0;
							if (sheet != null) {
								size = sheet.size();
							}
							excelService.putDataSheet(englishHeadArray, vo, data, size, sheetName);
						}

					}
				}
			}
			ByteArrayOutputStream export = ExcelFactory.getInstance().exportOfManySheet(data);
			export.writeTo(outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private Long getFollowersCount(Boolean isFollowersCount, String urlName, String screenName, String uid) throws TRSException {
		Long followersCount = 0L;
		if (isFollowersCount) {
			QueryBuilder queryBuilder = new QueryBuilder();
			queryBuilder.filterField(FtsFieldConst.FIELD_URLNAME, "\"" + urlName + "\"", Operator.Equal);
			queryBuilder.setDatabase(Const.WEIBO);
			//放入对应 微博用户信息
			if (ObjectUtil.isEmpty(screenName) && ObjectUtil.isEmpty(screenName)) {
				followersCount = 0L;
			} else {
				StatusUser statusUser = queryStatusUser(screenName, uid);
				if (ObjectUtil.isNotEmpty(statusUser)) {
					followersCount = statusUser.getFollowersCount();
				} else {
					followersCount = 0L;
				}
			}
		}
		return followersCount;
	}

}
