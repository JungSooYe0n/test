package com.trs.netInsight.widget.report.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Criterion.MatchMode;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.ExcelConst;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.cache.TimingCachePool;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.*;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.report.excel.DataRow;
import com.trs.netInsight.support.report.excel.ExcelData;
import com.trs.netInsight.support.report.excel.ExcelFactory;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.alert.service.IAlertService;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogService;
import com.trs.netInsight.widget.report.entity.Favourites;
import com.trs.netInsight.widget.report.entity.enums.ExportListType;
import com.trs.netInsight.widget.report.service.IExcelService;
import com.trs.netInsight.widget.report.service.IMaterialLibraryNewService;
import com.trs.netInsight.widget.report.service.IReportService;
import com.trs.netInsight.widget.report.util.ReportUtil;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.special.service.IInfoListService;
import com.trs.netInsight.widget.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 导出数据到Excel
 *
 * Created by xiaoying on 2017/12/13.
 */
@Service
@Slf4j
public class ExcelServiceImpl implements IExcelService {
	@Autowired
	private ISingleMicroblogService singleMicroblogService;
    @Autowired
	private FullTextSearch hybase8SearchService;

    @Autowired
	private IReportService reportService;

    @Autowired
	private IAlertService alertService;


    @Autowired
	private IMaterialLibraryNewService materialLibraryNewService;

	@Autowired
	private IInfoListService infoListService;

    @Value("${http.client}")
	private boolean httpClient;

    @Value("${http.alert.netinsight.url}")
	private String alertNetinsightUrl;

    /**
     * 按照选中id导出
     * @param ids ;隔开的sid
     * @param sort 排序方式
     * @param builder
     * @param head 表格头部 ;隔开
     * @return
     * @throws Exception
     * created by xiaoying
     */
    @Override
    public ByteArrayOutputStream export(String trslk,String ids, String sort,QueryBuilder builder,String head,String headEnglish,boolean handleAbstract,String sheetName) throws Exception {
    	builder.setPageSize(-1);
		builder.setDatabase(Const.HYBASE_NI_INDEX);
		builder.filterByTRSL(trslk);
        builder.filterField(FtsFieldConst.FIELD_SID, ids.split(";"), Operator.Equal);
        log.info(builder.asTRSL()+"热搜列表导出");
        List<FtsDocument> pagedList=hybase8SearchService.ftsQuery(builder, FtsDocument.class,false,false,false,null);
        pagedList=sortByIds(ids, pagedList);
        ObjectUtil.assertNull(pagedList, "传统导出Excel查询数据");
    	String headArray[] = head.split(";");
    	String englishArray[] = headEnglish.split(";");
        //按照前端传过来的id排序
        ExcelData data = new ExcelData();
        //data.setHead(headArray);
		data.putHeadMap(sheetName,Arrays.asList(headArray));
        for (int i = 0; i < pagedList.size(); i++) {
        	FtsDocument document = pagedList.get(i);
        	if (handleAbstract){
				String toAbstract = ReportUtil.contentToAbstract(document.getContent());
				String anAbstract = "";
				String anAbstractNew = "";
				if (StringUtil.isEmpty(toAbstract)){
					anAbstract = document.getUrlTitle().replaceAll("<font color=red>","").replaceAll("</font>","");
					anAbstractNew = anAbstract +"。";
				}else {
					anAbstractNew = toAbstract;
				}

				if (StringUtil.isNotEmpty(anAbstractNew)){
					document.setHit(StringUtil.replaceImg(StringUtil.replaceFont(anAbstractNew)));
				}

            }
        	//putData(englishArray, document, data, i);
			putDataSheet(englishArray, document, data, i,sheetName);
        }
        return ExcelFactory.getInstance().exportOfManySheet(data);
    }

    public List<FtsDocument> sortByIds(String ids,List<FtsDocument> pagedList){
    	String[] split = ids.split(";");
    	List<FtsDocument> fts = new ArrayList<FtsDocument>();
    	//为了让他俩长度一样
    	for(String id : split){
    		fts.add(new FtsDocument());
    	}
    	for(int i=0;i<split.length;i++){
    		for(FtsDocument ftsDocument : pagedList){
    			String sid1 = split[i];
    			log.info(sid1+"split");
    			String sid2 = ftsDocument.getSid();
    			log.info(sid2+"document");
    			log.info(i+"");
    			if(sid1.equals(sid2)){
    				fts.set(i, ftsDocument);
    			}
    		}
    	}
		return fts;
    }

    /**
     * 按照数量导出 传统
     * @param trslk 表达式
     * @param num 数量
     * @param sort 排序方式
     * @param sim 是否排重
     * @param server 是否转换为server
     * @param head 导出头
     * @param headEnglish 导出头字段
     * @return
     * @throws Exception
     * created by xiaoying
     */
    public ByteArrayOutputStream exportNum(String trslk,int num, String sort,boolean sim,boolean server,String head,String headEnglish,boolean handleAbstract,String sheetName,boolean weight) throws Exception {
    	QueryBuilder  builder=new QueryBuilder();
		builder.setDatabase(Const.MIX_DATABASE);
		builder.filterByTRSL(trslk);
		builder.page(0, num);
		builder.setServer(server);
		List<FtsDocumentCommonVO> pagedList=new ArrayList<>();
		if(num>10000){
			pagedList=hybase8SearchService.ftsQueryForExport(builder, FtsDocumentCommonVO.class,sim,false,false,null);
		}else{
			switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				GroupResult md5TAG = hybase8SearchService.categoryQuery(builder, true, false,false,"MD5TAG", Const.HYBASE_NI_INDEX);
	    		for (GroupInfo info : md5TAG) {
					QueryBuilder builder1 = new QueryBuilder();
					builder1.filterByTRSL(builder.asTRSL());
					builder.page(0, 1);
					builder1.filterField("MD5TAG", info.getFieldValue(), Operator.Equal);
					//List<FtsDocumentCommonVO> md5List = hybase8SearchService.ftsQuery(builder1, FtsDocumentCommonVO.class, false,false,false);
					//pagedList.add(md5List.get(0));
					PagedList<FtsDocumentCommonVO> pagedList_new = hybase8SearchService.ftsPageList(builder, FtsDocumentCommonVO.class,sim,false,false,null);
					pagedList.add(pagedList_new.getPageItems().get(0));
				}
	    		break;
			case "relevance":// 相关性排序
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			case "weight":
				builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
				break;
			default:
				if (weight) {
					builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
				} else {
					builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
				}
				break;
			}
			if(ObjectUtil.isEmpty(pagedList) || !"hot".equals(sort)){
				//pagedList = hybase8SearchService.ftsQuery(builder, FtsDocumentCommonVO.class, false,false,false);
				PagedList<FtsDocumentCommonVO> pagedList_new = hybase8SearchService.ftsPageList(builder, FtsDocumentCommonVO.class,sim,false,false,null);
				pagedList = pagedList_new.getPageItems();
			}
		}

        ObjectUtil.assertNull(pagedList, "传统导出Excel查询数据");
        String headArray[] = head.split(";");
    	String englishArray[] = headEnglish.split(";");
        //按照前端传过来的id排序
        ExcelData data = new ExcelData();
        //data.setHead(headArray);
		data.putHeadMap(sheetName,Arrays.asList(headArray));
        for (int i = 0; i < pagedList.size(); i++) {
			FtsDocumentCommonVO document = pagedList.get(i);
            if (handleAbstract){
				String toAbstract = ReportUtil.contentToAbstract(document.getContent());
				String anAbstract = "";
				String anAbstractNew = "";
				if (StringUtil.isEmpty(toAbstract)){
					anAbstract = document.getUrlTitle().replaceAll("<font color=red>","").replaceAll("</font>","");
					anAbstractNew = anAbstract +"。";
				}else {
					anAbstractNew = toAbstract;
				}
				if (StringUtil.isNotEmpty(anAbstractNew)){
					document.setHit(StringUtil.replaceImg(StringUtil.replaceFont(anAbstractNew)));
				}
            }
        	//putData(englishArray, document, data, i);
			putDataSheet(englishArray, document, data, i,sheetName);
        }
        return ExcelFactory.getInstance().exportOfManySheet(data);
    }

    /**
     * 导出两万条测试
     * @param trslk
     * @param num
     * @param sort
     * @param sim
     * @param server
     * @return
     * @throws Exception
     */
    @Override
    public ByteArrayOutputStream exportNumTest(String trslk,int num, String sort,boolean sim,boolean server) throws Exception {
    	QueryBuilder  builder=new QueryBuilder();
		builder.setDatabase(Const.HYBASE_NI_INDEX);
		builder.filterByTRSL(trslk);
		builder.page(1, 10000);
		builder.setServer(server);
		List<FtsDocument> pagedList=hybase8SearchService.ftsQueryForExport(builder, FtsDocument.class,sim,false,false,null);
        ObjectUtil.assertNull(pagedList, "传统导出Excel查询数据");
        ExcelData data = new ExcelData();
        data.setHead(ExcelConst.HEAD_VALUE);
        for (int i = 0; i < pagedList.size(); i++) {
        	FtsDocument document = pagedList.get(i);
			String anAbstract = ReportUtil.contentToAbstract(document.getContent());
			if (StringUtil.isNotEmpty(anAbstract)){
				document.setAbstracts(anAbstract);
			}

        	String title=StringUtil.replaceImg(StringUtil.replaceFont(document.getTitle()));
            data.addRow(i+1, title, new DataRow(document.getUrlName(),document.getUrlName()), document.getSiteName(), document.getUrlTime());
        }
        return ExcelFactory.getInstance().export(data);
    }

    /**
     * 检索mid，将数据导出到excel  微博
     *
     * @param mid 文章mid
     * @return ByteArrayOutputStream
     * @throws IOException IOException
     * CreatedBy xiaoying
     */
	@Override
	public ByteArrayOutputStream exportWeiBo(String mid, String sort,QueryBuilder builder,String head,String headEnglish,String sheetName) throws Exception {
		builder.setPageSize(-1);
		builder.setDatabase(Const.WEIBO);
        builder.filterField(FtsFieldConst.FIELD_MID, mid.split(";"), Operator.Equal);
        List<FtsDocumentStatus> pagedList=hybase8SearchService.ftsQuery(builder, FtsDocumentStatus.class,false,false,false,null);
        ObjectUtil.assertNull(pagedList, "微博导出Excel查询数据");
        pagedList=weiboByIds(mid,pagedList);
        String headArray[] = head.split(";");
    	String englishArray[] = headEnglish.split(";");
    	ExcelData data = new ExcelData();
        //data.setHead(headArray);
		data.putHeadMap(sheetName,Arrays.asList(headArray));
        for (int i = 0; i < pagedList.size(); i++) {
			FtsDocumentStatus document = pagedList.get(i);
			if (headEnglish.contains("followersCount")) {
                QueryBuilder queryBuilder = new QueryBuilder();
                queryBuilder.filterField(FtsFieldConst.FIELD_URLNAME, "\"" + document.getUrlName() + "\"", Operator.Equal);
                queryBuilder.setDatabase(Const.WEIBO);
                if (ObjectUtil.isEmpty(document.getScreenName()) && ObjectUtil.isEmpty(document.getScreenName())) {
                    document.setFollowersCount(0);
                } else {
                    StatusUser statusUser = queryStatusUser(document.getScreenName(), document.getUid());
                    if (ObjectUtil.isNotEmpty(statusUser)) {
                        document.setFollowersCount(statusUser.getFollowersCount());
                    } else {
                        document.setFollowersCount(0);
                    }
                }
            }
        	//putData(englishArray, document, data, i);
			putDataSheet(englishArray, document, data, i,sheetName);
        }
        return ExcelFactory.getInstance().exportOfManySheet(data);
	}

	   public List<FtsDocumentStatus> weiboByIds(String ids,List<FtsDocumentStatus> pagedList){
	    	String[] split = ids.split(";");
	    	List<FtsDocumentStatus> fts = new ArrayList<FtsDocumentStatus>();
	    	for(String id : split){
	    		fts.add(new FtsDocumentStatus());
	    	}
	    	for(int i=0;i<split.length;i++){
	    		for(FtsDocumentStatus ftsDocument : pagedList){
	    			if(split[i].equals(ftsDocument.getMid())){
	    				fts.set(i, ftsDocument);
	    			}
	    		}
	    	}
			return fts;
	    }
	@Override
	public ByteArrayOutputStream exportNumWeiBo(String trslk,int num, String sort,boolean sim,boolean server,String head,String headEnglish,String sheetName) throws Exception {
		QueryBuilder  builder=new QueryBuilder();
		builder.setDatabase(Const.WEIBO);
		builder.filterByTRSL(trslk);
		builder.page(0, num);
		builder.setServer(server);
		List<FtsDocumentStatus> pagedList=new ArrayList<>();
		if(num>10000){
			pagedList=hybase8SearchService.ftsQueryForExport(builder, FtsDocumentStatus.class,sim,false,false,null);
		}else{
			switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				GroupResult md5TAG = hybase8SearchService.categoryQuery(builder, true, false,false,"MD5TAG", Const.WEIBO);
				for (GroupInfo info : md5TAG) {
					QueryBuilder builder1 = new QueryBuilder();
					builder1.filterByTRSL(builder.asTRSL());
					builder.page(0, 1);
					builder1.filterField("MD5TAG", info.getFieldValue(), Operator.Equal);
					List<FtsDocumentStatus> md5List = hybase8SearchService.ftsQuery(builder1, FtsDocumentStatus.class, false,false,false,null);
				    pagedList.add(md5List.get(0));
				}
	    		break;
			case "relevance":// 相关性排序
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			case "weight":
				builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
				break;
			default:
				builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
				break;
			}
			if(ObjectUtil.isEmpty(pagedList) || !"hot".equals(sort)){
				pagedList=hybase8SearchService.ftsQuery(builder, FtsDocumentStatus.class,sim,false,false,null);
			}
		}

        ObjectUtil.assertNull(pagedList, "传统导出Excel查询数据");
        String headArray[] = head.split(";");
    	String englishArray[] = headEnglish.split(";");
        ExcelData data = new ExcelData();
        //data.setHead(headArray);--设置sheet
		data.putHeadMap(sheetName,Arrays.asList(headArray));
        for (int i = 0; i < pagedList.size(); i++) {
        	FtsDocumentStatus document = pagedList.get(i);
        	//putData(englishArray, document, data, i);
			if (headEnglish.contains("followersCount")) {
                QueryBuilder queryBuilder = new QueryBuilder();
                queryBuilder.filterField(FtsFieldConst.FIELD_URLNAME,"\""+document.getUrlName()+"\"",Operator.Equal);
                queryBuilder.setDatabase(Const.WEIBO);
                //放入对应 微博用户信息
                if (ObjectUtil.isEmpty(document.getScreenName()) && ObjectUtil.isEmpty(document.getScreenName())){
                    document.setFollowersCount(0);
                }else {
                    StatusUser statusUser = queryStatusUser(document.getScreenName(), document.getUid());
                    if (ObjectUtil.isNotEmpty(statusUser)) {
                        document.setFollowersCount(statusUser.getFollowersCount());
                    }else {
                        document.setFollowersCount(0);
                    }
                }
			}

			putDataSheet(englishArray, document, data, i,sheetName);
        }
        return ExcelFactory.getInstance().exportOfManySheet(data);
    }
	/**
     * 检索hkey，将数据导出到excel
     *
     * @param hkey 文章hkey
     * @return ByteArrayOutputStream
     * @throws IOException IOException
     * CreatedBy xiaoying
     */
	@Override
	public ByteArrayOutputStream exportWeiXin(String trslk,String hkey, String sort,QueryBuilder builder,String head,String headEnglish,boolean handleAbstract,String sheetName) throws Exception {
		builder.setPageSize(-1);
		builder.setDatabase(Const.WECHAT);
        builder.filterByTRSL(trslk);
        builder.filterField(FtsFieldConst.FIELD_HKEY, hkey.split(";"), Operator.Equal);
        List<FtsDocumentWeChat> pagedList=hybase8SearchService.ftsQuery(builder, FtsDocumentWeChat.class,false,false,false,null);
        ObjectUtil.assertNull(pagedList, "微信导出Excel查询数据");
        pagedList=weixiByIds(hkey,pagedList);
        String headArray[] = head.split(";");
    	String englishArray[] = headEnglish.split(";");
        //按照前端传过来的id排序
        ExcelData data = new ExcelData();
        //data.setHead(headArray);
		data.putHeadMap(sheetName,Arrays.asList(headArray));
        for (int i = 0; i < pagedList.size(); i++) {
        	FtsDocumentWeChat document = pagedList.get(i);
        	if (handleAbstract){
				String toAbstract = ReportUtil.contentToAbstract(document.getContent());
				String anAbstract = "";
				String anAbstractNew = "";
				if (StringUtil.isEmpty(toAbstract)){
					anAbstract = document.getUrlTitle().replaceAll("<font color=red>","").replaceAll("</font>","");
					anAbstractNew = anAbstract +"。";
				}else {
					anAbstractNew = toAbstract;
				}
				if (StringUtil.isNotEmpty(anAbstractNew)){
					document.setHit(StringUtil.replaceImg(StringUtil.replaceFont(anAbstractNew)));
				}
            }
        	//putData(englishArray, document, data, i);
			putDataSheet(englishArray, document, data, i,sheetName);
        }
        return ExcelFactory.getInstance().exportOfManySheet(data);
	}

	public List<FtsDocumentWeChat> weixiByIds(String ids,List<FtsDocumentWeChat> pagedList){
    	String[] split = ids.split(";");
    	List<FtsDocumentWeChat> fts = new ArrayList<FtsDocumentWeChat>();
    	for(String id : split){
    		fts.add(new FtsDocumentWeChat());
    	}
    	for(int i=0;i<split.length;i++){
    		for(FtsDocumentWeChat ftsDocument : pagedList){
    			if(split[i].equals(ftsDocument.getHkey())){
    				fts.set(i, ftsDocument);
    			}
    		}
    	}
		return fts;
    }

	public List<FtsDocumentTF> overseaByIds(String ids,List<FtsDocumentTF> pagedList){
    	String[] split = ids.split(";");
    	List<FtsDocumentTF> fts = new ArrayList<FtsDocumentTF>();
    	for(String id : split){
    		fts.add(new FtsDocumentTF());
    	}
    	for(int i=0;i<split.length;i++){
    		for(FtsDocumentTF ftsDocument : pagedList){
    			if(split[i].equals(ftsDocument.getSid())){
    				fts.set(i, ftsDocument);
    			}
    		}
    	}
		return fts;
    }

	/**
	 * 混合列表按id顺序排序
	 * @param ids
	 * @param pagedList
	 * @return
	 */
	public List<FtsDocumentCommonVO> mixByIds(String ids,List<FtsDocumentCommonVO> pagedList){
    	String[] split = ids.split(";");
    	List<FtsDocumentCommonVO> fts = new ArrayList<>();
    	//为了让他俩长度一样
    	for(String id : split){
    		fts.add(new FtsDocumentCommonVO());
    	}
    	for(int i=0;i<split.length;i++){
    		for(FtsDocumentCommonVO ftsDocument : pagedList){
    			if(Const.MEDIA_TYPE_WEIXIN.contains(ftsDocument.getGroupName())){
    				if(split[i].equals(ftsDocument.getHkey())){
        				fts.set(i, ftsDocument);
        			}
    			}else{
    				if(split[i].equals(ftsDocument.getSid())){
        				fts.set(i, ftsDocument);
        			}
    			}

    		}
    	}
		return fts;
    }
	@Override
	public ByteArrayOutputStream exportNumWeiXin(String trslk,int num, String sort,boolean sim,boolean server,String head,String headEnglish,boolean handleAbstract,String sheetName) throws Exception {
		QueryBuilder  builder=new QueryBuilder();
		builder.setDatabase(Const.WECHAT);
		builder.filterByTRSL(trslk);
		builder.page(0, num);
		builder.setServer(server);
		List<FtsDocumentWeChat> pagedList=new ArrayList<>();
		if(num>10000){
			pagedList=hybase8SearchService.ftsQueryForExport(builder, FtsDocumentWeChat.class,sim,false,false,null);
		}else{
			switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				GroupResult md5TAG = hybase8SearchService.categoryQuery(builder, true, false,false,"MD5TAG", Const.WECHAT);
				for (GroupInfo info : md5TAG) {
					QueryBuilder builder1 = new QueryBuilder();
					builder1.filterByTRSL(builder.asTRSL());
					builder.page(0, 1);
					builder1.filterField("MD5TAG", info.getFieldValue(), Operator.Equal);
					List<FtsDocumentWeChat> md5List = hybase8SearchService.ftsQuery(builder1, FtsDocumentWeChat.class, false,false,false,null);
				    pagedList.add(md5List.get(0));
				}
	    		break;
			case "relevance":// 相关性排序
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			case "weight":
				builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
				break;
			default:
				builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
				break;
			}
			if(ObjectUtil.isEmpty(pagedList) || !"hot".equals(sort)){
				pagedList=hybase8SearchService.ftsQuery(builder, FtsDocumentWeChat.class,sim,false,false,null);
			}
		}

        ObjectUtil.assertNull(pagedList, "传统导出Excel查询数据");
        String headArray[] = head.split(";");
    	String englishArray[] = headEnglish.split(";");
        //按照前端传过来的id排序
        ExcelData data = new ExcelData();
        //data.setHead(headArray);
		data.putHeadMap(sheetName,Arrays.asList(headArray));
        for (int i = 0; i < pagedList.size(); i++) {
        	FtsDocumentWeChat document = pagedList.get(i);
        	if (handleAbstract){
				String toAbstract = ReportUtil.contentToAbstract(document.getContent());
				String anAbstract = "";
				String anAbstractNew = "";
				if (StringUtil.isEmpty(toAbstract)){
					anAbstract = document.getUrlTitle().replaceAll("<font color=red>","").replaceAll("</font>","");
					anAbstractNew = anAbstract +"。";
				}else {
					anAbstractNew = toAbstract;
				}
				if (StringUtil.isNotEmpty(anAbstractNew)){
					document.setHit(StringUtil.replaceImg(StringUtil.replaceFont(anAbstractNew)));
				}
            }
        	//putData(englishArray, document, data, i);
			putDataSheet(englishArray, document, data, i,sheetName);
//        	String title=StringUtil.replaceFont(document.getUrlTitle());
//            data.addRow(i+1, title, new DataRow(document.getUrlName(),document.getUrlName()),document.getSiteName(), document.getUrlTime());
//            data.addRow(i+1, title, new DataRow(document.getUrlName(),document.getUrlName()),document.getAuthors(), document.getUrlTime());
        }
        return ExcelFactory.getInstance().exportOfManySheet(data);
    }

	/**
	 * 我的收藏按照条数导出
	 * @throws TRSException
	 * @throws IOException
	 * xiaoying
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ByteArrayOutputStream exportDear(String groupName, int dearNum, String keywords,String invitationCard,
			String forwarPrimary,String head,String headEnglish,String type,String libraryId,String time) throws Exception {
		User loginUser = UserUtils.getUser();
		InfoListResult allFavourites = null;
		if(libraryId == null || "".equals(libraryId)){
			allFavourites = (InfoListResult) reportService.getAllFavourites(loginUser, 0, dearNum,groupName,keywords,invitationCard,forwarPrimary,false);
		}else{
			allFavourites = (InfoListResult)materialLibraryNewService.findMaterialResource(libraryId,0,dearNum,groupName,keywords,invitationCard,forwarPrimary,time);
		}
//		ExcelData data = new ExcelData();
//		data.setHead(ExcelConst.HEAD_VALUE);
		String headArray[] = head.split(";");
    	String englishArray[] = headEnglish.split(";");
        //按照前端传过来的id排序
        ExcelData data = new ExcelData();
        data.setHead(headArray);
		//根据来源不同 取出数据格式不同
		//传统
		if(Const.MEDIA_TYPE_NEWS.contains(groupName)){
			List<Favourites> list=(List<Favourites>) allFavourites.getContent();
	        for (int i = 0; i < list.size(); i++) {
	        	putData(englishArray, list.get(i), data, i);
//	        	String title=StringUtil.replaceFont(document.getTitle());
//	            data.addRow(i+1, title, new DataRow(document.getUrl(),document.getUrl()), document.getSiteName(), document.getUrltime());
	        }
		}else if(Const.MEDIA_TYPE_WEIBO.contains(groupName) || Const.MEDIA_TYPE_TF.contains(groupName)){//微博
			//微博没有标题
        	headEnglish = headEnglish.replace("title", "statusContent").replace("content", "statusContent") + ";rttCount";
			englishArray = headEnglish.split(";");

        	headEnglish = headEnglish.replace("title", "statusContent").replace("content", "statusContent");
			englishArray = headEnglish.split(";");
			List<Favourites> list=(List<Favourites>) allFavourites.getContent();
			for (int i = 0; i < list.size(); i++) {
	        	putData(englishArray, list.get(i), data, i);
//	            data.addRow(i+1,  new DataRow(document.getUrlName(),document.getUrlName()),  document.getSiteName(), document.getCreatedAt());
	        }
		}else if(Const.MEDIA_TYPE_WEIXIN.contains(groupName)){//微信
			headEnglish = headEnglish.replace("title", "urlTitle");
			List<Favourites> list=(List<Favourites>) allFavourites.getContent();
			englishArray = headEnglish.split(";");
			for (int i = 0; i < list.size(); i++) {
	        	putData(englishArray, list.get(i), data, i);
//	        	String title=StringUtil.replaceFont(document.getUrlTitle());
//	            data.addRow(i+1, title, new DataRow(document.getUrlName(),document.getUrlName()),document.getAuthors(),  document.getUrlTime());
	        }
		}
		return ExcelFactory.getInstance().export(data);
	}

	@Override
	public String exportMix(String id,String mixid, String groupName,QueryBuilder builderTime)
			throws TRSException, IOException, TRSSearchException {
		//最后按照他传的混合Id排序
		QueryCommonBuilder builder = new QueryCommonBuilder();
		if(null!=builderTime){
			Date start = builderTime.getStartTime();
			Date end = builderTime.getEndTime();
			builder.setStartTime(start);
			builder.setEndTime(end);
			SimpleDateFormat format = new SimpleDateFormat(DateUtil.yyyyMMdd2);
			String startString = format.format(start);
			String endString = format.format(end);
			builder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{startString+"000000",endString+"235959"}, Operator.Between);
		}
		String[] split = mixid.split(";");
		List<String> mixidList = new ArrayList<>();
		for(String sp : split){
			mixidList.add(sp);
		}
		List<String> groupList = Arrays.asList(groupName.split(";"));
		List<String> weixinList = new ArrayList<>();
		for(int i=0;i<mixidList.size();i++){
			if("微信".equals(groupList.get(i)) || "国内微信".equals(groupList.get(i))){
				weixinList.add(mixidList.get(i));
			}
		}
		if(weixinList.size()>0){
			mixidList.removeAll(weixinList);
			String weixinids = StringUtils.join(weixinList, ";");
			weixinids = weixinids.replace(";", " OR ");
			builder.filterChildField(FtsFieldConst.FIELD_HKEY, weixinids, Operator.Equal);
		}
		String asTRSL = builder.asTRSL();
		String mixids = StringUtils.join(mixidList, ";");
		mixids = mixids.replace(";", " OR ");
		builder.filterChildField(FtsFieldConst.FIELD_SID, mixids, Operator.Equal);
		builder.setDatabase(Const.MIX_DATABASE.split(";"));
		//四个库联查
		String asTRSL2 = builder.asTRSL();
		PagedList<FtsDocumentCommonVO> pagedList = hybase8SearchService.pageListCommon(builder, false,false,false,null);
		List<FtsDocumentCommonVO> list = pagedList.getPageItems();
        //最后按照idmix排序
		list = mixByIds(mixid,list);
//		String uuid = UUID.randomUUID().toString();
//		RedisUtil.setMix(uuid, list);
//		if(ObjectUtil.isNotEmpty(ftsQuery)){
//			list.add((FtsDocumentCommonVO) ftsQuery);
//		}
		RedisUtil.setMix(id, list);
        return id;
	}

	@Override
	public String exportAllMix(String id,String mixid, String groupName,QueryBuilder builderTime)
			throws TRSException, IOException, TRSSearchException {
		QueryBuilder builder_time = new QueryBuilder();
		String startString = null;
		String endString = null;
		if(null!=builderTime){
			Date start = builderTime.getStartTime();
			Date end = builderTime.getEndTime();
			builder_time.setStartTime(start);
			builder_time.setEndTime(end);
			SimpleDateFormat format = new SimpleDateFormat(DateUtil.yyyyMMdd2);
			startString = format.format(start);
			endString = format.format(end);
		}
		List<FtsDocumentCommonVO> listAll = new ArrayList<>();
		String[] groupArray = groupName.split(";");
		String[] idArray = mixid.split(";");
		for(int i =0;i<groupArray.length;i++){
			String group = groupArray[i];
			QueryBuilder builder = new QueryBuilder();
			builder.setStartTime(builder_time.getStartTime());
			builder.setEndTime(builder_time.getEndTime());
			builder.setPageSize(-1);
			builder.setDatabase(Const.HYBASE_OVERSEAS);
			if(null!=builderTime) {
				builder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{startString + "000000", endString + "235959"}, Operator.Between);
				builder.filterByTRSL(builderTime.getAppendTRSL().toString());
			}
			if("FaceBook".equals(group) || "Twitter".equals(group)){
				builder.filterField(FtsFieldConst.FIELD_SID, idArray[i], Operator.Equal);
				List<FtsDocumentTF> pagedList=hybase8SearchService.ftsQuery(builder, FtsDocumentTF.class,false,false,false,null);

				for(FtsDocumentTF tf :pagedList){
					FtsDocumentCommonVO oneVo = new FtsDocumentCommonVO();
					oneVo.setSid(tf.getSid());
					oneVo.setHkey(tf.getHkey());
					oneVo.setAuthors(tf.getScreenName());
					oneVo.setCreatedAt(tf.getCreatedAt());
					oneVo.setRttCount(tf.getRttCount());
					oneVo.setCommtCount(tf.getCommtCount());
					oneVo.setApproveCount(tf.getApproveCount());
					oneVo.setContent(tf.getContent());
					oneVo.setGroupName(tf.getGroupName());
					listAll.add(oneVo);
				}
			}else if("微博".equals(group)){
				/*QueryBuilder builder = new QueryBuilder();
				builder.setStartTime(builder_time.getStartTime());
				builder.setEndTime(builder_time.getEndTime());
				builder.setPageSize(-1);
				builder.setDatabase(Const.WEIBO);
				if(null!=builderTime) {
					builder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{startString + "000000", endString + "235959"}, Operator.Between);
				}*/
				builder.filterField(FtsFieldConst.FIELD_MID, idArray[i], Operator.Equal);
				List<FtsDocumentStatus> pagedList=hybase8SearchService.ftsQuery(builder, FtsDocumentStatus.class,false,false,false,null);
				for(FtsDocumentStatus status :pagedList){
					FtsDocumentCommonVO oneVo = new FtsDocumentCommonVO();
					oneVo.setSid(status.getSid());
					oneVo.setHkey(status.getHkey());
					oneVo.setScreenName(status.getScreenName());
					oneVo.setRetweetedScreenName(status.getRetweetedScreenName());
					oneVo.setCreatedAt(status.getCreatedAt());
					oneVo.setUrlName(status.getUrlName());
					oneVo.setRttCount(status.getRttCount());
					oneVo.setCommtCount(status.getCommtCount());
					oneVo.setApproveCount(status.getApproveCount());
					oneVo.setStatusContent(status.getStatusContent());
					oneVo.setContent(status.getStatusContent());
					oneVo.setGroupName(status.getGroupName());
//					QueryBuilder queryBuilder = new QueryBuilder();
//					queryBuilder.filterField(FtsFieldConst.FIELD_URLNAME,"\""+status.getUrlName()+"\"",Operator.Equal);
//					queryBuilder.setDatabase(Const.WEIBO);
//					if (ObjectUtil.isEmpty(status.getScreenName()) && ObjectUtil.isEmpty(status.getScreenName())){
//						oneVo.setFollowersCount(0);
//					}else {
//						StatusUser statusUser = queryStatusUser(status.getScreenName(), status.getUid());
//						if (ObjectUtil.isNotEmpty(statusUser)) {
//							oneVo.setFollowersCount(statusUser.getFollowersCount());
//						}else {
//							oneVo.setFollowersCount(0);
//						}
//					}
					listAll.add(oneVo);
				}
			}else if("微信".equals(group) || "国内微信".equals(group)){
				/*QueryBuilder builder = new QueryBuilder();
				builder.setStartTime(builder_time.getStartTime());
				builder.setEndTime(builder_time.getEndTime());
				builder.setPageSize(-1);
				builder.setDatabase(Const.WECHAT);
				//builder.filterByTRSL(trslk);
				if(null!=builderTime) {
					builder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{startString + "000000", endString + "235959"}, Operator.Between);
				}*/
				builder.filterField(FtsFieldConst.FIELD_HKEY, idArray[i], Operator.Equal);
				List<FtsDocumentWeChat> pagedList=hybase8SearchService.ftsQuery(builder, FtsDocumentWeChat.class,false,false,false,null);

				for(FtsDocumentWeChat wc :pagedList){
					FtsDocumentCommonVO oneVo = new FtsDocumentCommonVO();
					oneVo.setSid(wc.getSid());
					oneVo.setHkey(wc.getHkey());
					oneVo.setUrlTitle(wc.getUrlTitle());
					oneVo.setSiteName(wc.getSiteName());
					oneVo.setUrlTime(wc.getUrlTime());
					oneVo.setUrlName(wc.getUrlName());
					oneVo.setRdcount(wc.getRdcount());
					oneVo.setPrcount(wc.getPrcount());
					oneVo.setContent(wc.getContent());
					oneVo.setGroupName(wc.getGroupName());
					listAll.add(oneVo);
				}

			}else{
				/*QueryBuilder builder = new QueryBuilder();
				builder.setStartTime(builder_time.getStartTime());
				builder.setEndTime(builder_time.getEndTime());
				builder.setPageSize(-1);
				builder.setDatabase(Const.HYBASE_NI_INDEX);
				///builder.filterByTRSL(trslk);
				if(null!=builderTime) {
					builder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{startString + "000000", endString + "235959"}, Operator.Between);
				}*/
				builder.filterField(FtsFieldConst.FIELD_SID, idArray[i], Operator.Equal);
				List<FtsDocument> pagedList=hybase8SearchService.ftsQuery(builder, FtsDocument.class,false,false,false,null);

				for(FtsDocument dt :pagedList){
					FtsDocumentCommonVO oneVo = new FtsDocumentCommonVO();
					oneVo.setSid(dt.getSid());
					oneVo.setHkey(dt.getHKey());
					oneVo.setTitle(dt.getTitle());
					oneVo.setSiteName(dt.getSiteName());
					oneVo.setSrcName(dt.getSrcName());
					oneVo.setChannel(dt.getChannel());
					oneVo.setAuthors(dt.getAuthors());
					oneVo.setUrlTime(dt.getUrlTime());
					oneVo.setUrlName(dt.getUrlName());
					oneVo.setAbstracts(StringUtil.removeFourChar(StringUtil.replaceImg(dt.getAbstracts())));
					oneVo.setContent(StringUtil.replaceImg(dt.getContent()));
					oneVo.setGroupName(dt.getGroupName());
					listAll.add(oneVo);
				}
			}
		}
		//最后按照他传的混合Id排序
		listAll = mixByIds(mixid,listAll);
		RedisUtil.setMix(id, listAll);
		return id;
	}

	@Override
	public ByteArrayOutputStream exportNumMix(String trslk, String mtrslk,String htrslk,int num) throws Exception {
		List<FtsDocumentChaos> listMix = new ArrayList<>();
		//三个库各查50条 按时间排序取出他想要的条数
		if(StringUtil.isNotEmpty(trslk)){
			QueryBuilder  builderChuanTong=new QueryBuilder();
	    	builderChuanTong.setDatabase(Const.HYBASE_NI_INDEX);
	    	builderChuanTong.orderBy(FtsFieldConst.FIELD_URLTIME, true);
	    	builderChuanTong.page(0, 50);
	    	builderChuanTong.filterByTRSL(trslk);
	    	List<FtsDocument> listChuantong=hybase8SearchService.ftsQuery(builderChuanTong, FtsDocument.class,true,false,false,null);
	    	//转换为同一的
	    	for(FtsDocument document : listChuantong){
	    		FtsDocumentChaos documentChaos = new FtsDocumentChaos();
	    		documentChaos.setSid(document.getSid());
	    		documentChaos.setUrlTime(document.getUrlTime());
	    		documentChaos.setUrlTitle(document.getTitle());
	    		documentChaos.setUrlName(document.getUrlName());
	    		documentChaos.setSiteName(document.getSiteName());
	    		listMix.add(documentChaos);
	    	}
		}
    	if(StringUtil.isNotEmpty(mtrslk)){
    		QueryBuilder  builderWeiBo=new QueryBuilder();
        	builderWeiBo.setDatabase(Const.WEIBO);
        	builderWeiBo.page(0, 50);
        	builderWeiBo.orderBy(FtsFieldConst.FIELD_CREATED_AT, true);
        	builderWeiBo.filterByTRSL(mtrslk);
        	List<FtsDocumentStatus> listWeiBo=hybase8SearchService.ftsQuery(builderWeiBo, FtsDocumentStatus.class,false,false,false,null);
        	//转换为统一的
        	for(FtsDocumentStatus status : listWeiBo){
        		FtsDocumentChaos documentChaos = new FtsDocumentChaos();
        		documentChaos.setSid(status.getMid());
        		documentChaos.setUrlTime(status.getCreatedAt());
        		documentChaos.setUrlTitle(status.getStatusContent());
        		documentChaos.setUrlName(status.getUrlName());
        		documentChaos.setSiteName(status.getSiteName());
        		listMix.add(documentChaos);
        	}
    	}
    	if(StringUtil.isNotEmpty(htrslk)){
    		QueryBuilder  builderWeChat=new QueryBuilder();
        	builderWeChat.setDatabase(Const.WECHAT);
        	builderWeChat.page(0, 50);
        	builderWeChat.orderBy(FtsFieldConst.FIELD_URLTIME, true);
        	builderWeChat.filterByTRSL(htrslk);
        	List<FtsDocumentWeChat> listWeChat=hybase8SearchService.ftsQuery(builderWeChat, FtsDocumentWeChat.class,false,false,false,null);
        	//转换为统一的
        	for(FtsDocumentWeChat wechat : listWeChat){
        		FtsDocumentChaos documentChaos = new FtsDocumentChaos();
        		documentChaos.setSid(wechat.getHkey());
        		documentChaos.setUrlTime(wechat.getUrlTime());
        		documentChaos.setUrlTitle(wechat.getUrlTitle());
        		documentChaos.setUrlName(wechat.getUrlName());
        		documentChaos.setSiteName(wechat.getAuthors());
        		listMix.add(documentChaos);
        	}
    	}
    	SortMix sort = new SortMix();
		Collections.sort(listMix, sort);
    	//导出
    	 ExcelData data = new ExcelData();
         data.setHead(ExcelConst.HEAD_VALUE);
         for (int i = 0; i < listMix.size(); i++) {
         	FtsDocumentChaos document = listMix.get(i);
         	String title=StringUtil.replaceFont(document.getUrlTitle());
             data.addRow(i+1, title, new DataRow(document.getUrlName(),document.getUrlName()), document.getAuthors(), document.getUrlTime());
         }
         return ExcelFactory.getInstance().export(data);
	}

	@Override
	public ByteArrayOutputStream exportNumForAlert(String time,String groupName,String keywords, int num,String way,String head,String headEnglish) throws Exception {
		String userId = UserUtils.getUser().getId();
		String userName = UserUtils.getUser().getUserName();
		String[] formatTimeRange = null;
		try {
			formatTimeRange = DateUtil.formatTimeRange(time);
		} catch (OperationException e1) {
			e1.printStackTrace();
		}
//		return alertService.list(formatTimeRange,pageNo,pageSize,source,receivers);
		String start = formatTimeRange[0];
		String end = formatTimeRange[1];
		SimpleDateFormat sdf =   new SimpleDateFormat( "yyyyMMddHHmmss" );
		Criteria<AlertEntity> criteria=new Criteria<>();
		criteria.add(Restrictions.eq("groupName", groupName));
		Date parseStart = null;
		Date parseEnd = null;
		try {
			 parseStart = sdf.parse(start);
			 parseEnd = sdf.parse(end);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		criteria.add(Restrictions.between("createdTime", parseStart, parseEnd));
		if(StringUtil.isNotEmpty(keywords)){
			criteria.add(Restrictions.or(Restrictions.like("title", keywords,MatchMode.ANYWHERE),
					Restrictions.like("content", keywords,MatchMode.ANYWHERE)));
		}
		if("SMS".equals(way) || "站内预警".equals(way)){
			// 站内预警 别人发给我
			criteria.add(Restrictions.eq("receiver", userName));
			criteria.add(Restrictions.eq("sendWay", SendWay.SMS));
			way = "SMS";
		}else{
			//已发预警 我发给谁
			criteria.add(Restrictions.eq("userId", userId));
		}
		List<AlertEntity> list = new ArrayList<>();
		if(httpClient){
			String url = alertNetinsightUrl+"/alert/list?pageNo="+0+"&pageSize="+num+"&way="+way+"&source="+groupName+"&time="
					+time+"&keywords="+keywords+"&userId="+userId+"&userName="+userName;
	        String doGet = HttpUtil.doGet(url, null);
	        if(StringUtil.isEmpty(doGet)){
				return null;
			}else if(doGet.contains("\"code\":500")){
				Map<String,String> map = (Map<String,String>)JSON.parse(doGet);
				String message = map.get("message");
				throw new OperationException("预警导出失败,message:"+message ,new Exception());
			}else{
				Map<String,Object> map = (Map<String,Object>)JSON.parse(doGet);
				Map<String,Object> object = (Map<String,Object>)map.get("list");
				list = JSONObject.parseArray(((JSON) object.get("content")).toJSONString(), AlertEntity.class);
			}
		}else{
			Page<AlertEntity> findAll = alertService.findAll(criteria,0,num);
			list = findAll.getContent();
		}

		//导出
		String headArray[] = head.split(";");
    	String englishArray[] = headEnglish.split(";");
        //按照前端传过来的id排序
        ExcelData data = new ExcelData();
        data.setHead(headArray);
        for (int i = 0; i < list.size(); i++) {
        	AlertEntity document =list.get(i);
        	putData(englishArray, document, data, i);
        }
        return ExcelFactory.getInstance().export(data);
	}

	@Override
	public String[] getIds(String time, String groupName, String keywords, int num, String way) throws Exception {
		String userId = UserUtils.getUser().getId();
		String userName = UserUtils.getUser().getUserName();
		String[] formatTimeRange = null;
		try {
			formatTimeRange = DateUtil.formatTimeRange(time);
		} catch (OperationException e1) {
			e1.printStackTrace();
		}
		String start = formatTimeRange[0];
		String end = formatTimeRange[1];
		SimpleDateFormat sdf =   new SimpleDateFormat( "yyyyMMddHHmmss" );
		Criteria<AlertEntity> criteria=new Criteria<>();
		criteria.add(Restrictions.eq("groupName", groupName));
		Date parseStart = null;
		Date parseEnd = null;
		try {
			parseStart = sdf.parse(start);
			parseEnd = sdf.parse(end);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		criteria.add(Restrictions.between("createdTime", parseStart, parseEnd));
		if(StringUtil.isNotEmpty(keywords)){
			criteria.add(Restrictions.or(Restrictions.like("title", keywords,MatchMode.ANYWHERE),
					Restrictions.like("content", keywords,MatchMode.ANYWHERE)));
		}
		if("SMS".equals(way) || "站内预警".equals(way)){
			// 站内预警 别人发给我
			criteria.add(Restrictions.eq("receiver", userName));
			criteria.add(Restrictions.eq("sendWay", SendWay.SMS));
			way = "SMS";
		}else{
			//已发预警 我发给谁
			criteria.add(Restrictions.eq("userId", userId));
		}
		List<AlertEntity> list = new ArrayList<>();
		if(httpClient){
			String url = alertNetinsightUrl+"/alert/list?pageNo="+0+"&pageSize="+num+"&way="+way+"&source="+groupName+"&time="
					+time+"&keywords="+keywords+"&userId="+userId+"&userName="+userName;
			String doGet = HttpUtil.doGet(url, null);
			if(StringUtil.isEmpty(doGet)){
				return null;
			}else if(doGet.contains("\"code\":500")){
				Map<String,String> map = (Map<String,String>)JSON.parse(doGet);
				String message = map.get("message");
				throw new OperationException("预警导出失败,message:"+message ,new Exception());
			}else{
				Map<String,Object> map = (Map<String,Object>)JSON.parse(doGet);
				Map<String,Object> object = (Map<String,Object>)map.get("list");
				list = JSONObject.parseArray(((JSON) object.get("content")).toJSONString(), AlertEntity.class);
			}
		}else{
			Page<AlertEntity> findAll = alertService.findAll(criteria,0,num);
			list = findAll.getContent();
		}
		String ids = "";
		String groups = "";
		String times = "";
		SimpleDateFormat sdf1 =   new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		if (list.size()>0){
			for (AlertEntity obj:list) {
				ids = ids + obj.getSid() + ";";
				groups = groups + obj.getGroupName() + ";";
				times = times + sdf1.format(obj.getTime()) + ";";
			}
		}
		if (StringUtil.isNotEmpty(ids)){
			ids = ids.substring(0,ids.length() - 1);
			groups = groups.substring(0,groups.length() - 1);
			times = times.substring(0,times.length() - 1);
		}
		String[] idAndGroAndTime = {ids,groups,times};
		return idAndGroAndTime;
	}

	@Override
	public ByteArrayOutputStream exportOversea(String sid, String sort,QueryBuilder builder,String head,String headEnglish,String sheetName) throws Exception {
		builder.setPageSize(-1);
		builder.setDatabase(Const.HYBASE_OVERSEAS);
        builder.filterField(FtsFieldConst.FIELD_SID, sid.split(";"), Operator.Equal);
        List<FtsDocumentTF> pagedList=hybase8SearchService.ftsQuery(builder, FtsDocumentTF.class,false,false,false,null);
        pagedList=overseaByIds(sid,pagedList);
        ObjectUtil.assertNull(pagedList, "推特facebook导出Excel查询数据");
        String headArray[] = head.split(";");
    	String englishArray[] = headEnglish.split(";");
        //按照前端传过来的id排序
        ExcelData data = new ExcelData();
        //data.setHead(headArray);
		data.putHeadMap(sheetName,Arrays.asList(headArray));
        for (int i = 0; i < pagedList.size(); i++) {
        	FtsDocumentTF document = pagedList.get(i);
        	//putData(englishArray, document, data, i);
			putDataSheet(englishArray, document, data, i,sheetName);
        }
        return ExcelFactory.getInstance().exportOfManySheet(data);
	}

	@Override
	public ByteArrayOutputStream exportNumOversea(String trslk, int num, String sort,boolean sim,boolean server,String head,String headEnglish,String sheetName) throws Exception {
		QueryBuilder  builder=new QueryBuilder();
		builder.setDatabase(Const.HYBASE_OVERSEAS);
		builder.filterByTRSL(trslk);
		builder.page(0, num);
		builder.setServer(server);
		List<FtsDocumentTF> pagedList=new ArrayList<>();
		if(num>10000){
			pagedList=hybase8SearchService.ftsQueryForExport(builder, FtsDocumentTF.class,sim,false,false,null);
		}else{
			switch(sort){
			case "commtCount"://评论
				builder.orderBy(FtsFieldConst.FIELD_COMMTCOUNT, true);
				break;
			case "rttCount"://评论
				builder.orderBy(FtsFieldConst.FIELD_RTTCOUNT, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				builder.page(0, 50);
				GroupResult md5TAG = hybase8SearchService.categoryQuery(builder, false,false, false,"MD5TAG", Const.HYBASE_OVERSEAS);
				for (GroupInfo info : md5TAG) {
					QueryBuilder builder1 = new QueryBuilder();
					builder1.filterByTRSL(builder.asTRSL());
					builder.page(0, 1);
					builder1.filterField("MD5TAG", info.getFieldValue(), Operator.Equal);
					List<FtsDocumentTF> md5List = hybase8SearchService.ftsQuery(builder1, FtsDocumentTF.class, false,false,false,null);
				    pagedList.add(md5List.get(0));
				}
			default:
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
	    	}
			if(ObjectUtil.isEmpty(pagedList) || !"hot".equals(sort)){
				pagedList=hybase8SearchService.ftsQuery(builder, FtsDocumentTF.class,sim,false,false,null);
			}
		}
        ObjectUtil.assertNull(pagedList, "传统导出Excel查询数据");
        String headArray[] = head.split(";");
    	String englishArray[] = headEnglish.split(";");
        //按照前端传过来的id排序
        ExcelData data = new ExcelData();
        //data.setHead(headArray);
		data.putHeadMap(sheetName,Arrays.asList(headArray));
        for (int i = 0; i < pagedList.size(); i++) {
        	FtsDocumentTF document = pagedList.get(i);
        	//putData(englishArray, document, data, i);
			putDataSheet(englishArray, document, data, i,sheetName);
        }
        return ExcelFactory.getInstance().exportOfManySheet(data);
	}
	@Override
	public  void putData(String[] englishArray,Object document,ExcelData data,int i) throws TRSException{
		List<DataRow> list = new ArrayList<>();
		list.add(new DataRow(i+1));
    	for(String s:englishArray){
			if(!"序号".equals(s)){
				Field declaredField = null;
				try {
					declaredField = document.getClass().getDeclaredField(s);
				} catch (NoSuchFieldException | SecurityException e) {
					throw new TRSException(e);
				}
            	declaredField.setAccessible(true);
            	Object object = null;
				try {
					object = declaredField.get(document);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new TRSException(e);
				}
            	if("urlName".equals(s)){
            		list.add(new DataRow((String)object,(String)object));//两个入参是链接
            	}else if("title".equals(s) || "content".equals(s) || "statusContent".equals(s) || "urlTitle".equals(s)){//标题或正文过滤font标签img标签
            		list.add(new DataRow(StringUtil.replaceImg(StringUtil.replaceFont((String)object))));//一个入参是其他
            	}else{
            		list.add(new DataRow(object));
            	}
			}
		}
    	data.addRow(list);
	}
	@Override
	public  void putDataSheet(String[] englishArray,Object document,ExcelData data,int i,String key) throws TRSException{
		List<DataRow> list = new ArrayList<>();
		list.add(new DataRow(i+1));
		for(String s:englishArray){
			if(!"序号".equals(s)){
				Field declaredField = null;
				try {
					declaredField = document.getClass().getDeclaredField(s);
				} catch (NoSuchFieldException | SecurityException e) {
					throw new TRSException(e);
				}
				declaredField.setAccessible(true);
				Object object = null;
				try {
					object = declaredField.get(document);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new TRSException(e);
				}
				if("urlName".equals(s)){
					list.add(new DataRow((String)object,(String)object));//两个入参是链接
				}else if("title".equals(s) || "content".equals(s) || "exportContent".equals(s) || "statusContent".equals(s) || "urlTitle".equals(s)){//标题或正文过滤font标签img标签
					list.add(new DataRow(StringUtil.replaceImg(StringUtil.replaceFont((String)object))));//一个入参是其他
				}else{
					list.add(new DataRow(object));
				}
			}
		}
		data.putSheet(key,list);
	}
	/**
	 * 当前文章对应的用户信息
	 * @param screenName,uid
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

	@Override
	public Object queryByIds(String ids, String groupNames, QueryBuilder queryBuilder) throws TRSException {
		//微信数据查询 如果所选数据包含微信数据，需要单独查询，因为微信用hkey做主键，但是微信的hkey可能与传统媒体的hkey相同
		QueryBuilder builderWeiXin = new QueryBuilder();//微信的表达式
		QueryBuilder builder = new QueryBuilder();//其他数据源的表达式
		if(null!=queryBuilder){
			//querybuilder主要是存储时间和列表查询表达式的
			builderWeiXin.filterByTRSL(queryBuilder.asTRSL());
			builder.filterByTRSL(queryBuilder.asTRSL());
		}
		String[] groupNameArray = groupNames.split(";");
		String[] idArray = ids.split(";");
		List<String> idList = new ArrayList<>();
		List<String> weixinList = new ArrayList<>();
		for(int i = 0 ;i < idArray.length;i++){
			String groupName = groupNameArray[i];
			if(Const.EXPORT_WEIXIN_SOURCE.contains(groupName)){
				weixinList.add(idArray[i]);
			}else{
				idList.add(idArray[i]);
			}
		}
		List<FtsDocumentCommonVO> result = new ArrayList<>();
		if(idList.size()>0) {
			builder.filterField(FtsFieldConst.FIELD_SID, StringUtils.join(idList, " OR "), Operator.Equal);
			builder.setDatabase(Const.MIX_DATABASE);
			builder.page(0,idList.size() *2);
			log.info("选中导出查询数据表达式 - 全部：" + builder.asTRSL());
			PagedList<FtsDocumentCommonVO> pagedList = hybase8SearchService.ftsPageList(builder, FtsDocumentCommonVO.class, false, false, false, null);
			if(pagedList.getPageItems() != null && pagedList.getPageItems().size() > 0){
				result.addAll(pagedList.getPageItems());
			}
		}
		if(weixinList.size()>0){
			String weixinids = StringUtils.join(weixinList, " OR ");
			builderWeiXin.filterField(FtsFieldConst.FIELD_HKEY, weixinids, Operator.Equal);
			builderWeiXin.filterField(FtsFieldConst.FIELD_GROUPNAME, "国内微信", Operator.Equal);
			builderWeiXin.setDatabase(Const.WECHAT);
			builderWeiXin.page(0,weixinList.size()*2);
			log.info("选中导出查询数据表达式 - 微信：" + builderWeiXin.asTRSL());
			PagedList<FtsDocumentCommonVO> pagedListWeiXin = hybase8SearchService.ftsPageList(builderWeiXin,FtsDocumentCommonVO.class,false,false,false,null);
			if(pagedListWeiXin.getPageItems() != null && pagedListWeiXin.getPageItems().size() > 0){
				result.addAll(pagedListWeiXin.getPageItems());
			}
		}
		String key = UUID.randomUUID().toString();
		result = mixByIds(ids,result);
		RedisUtil.setMix(key, result);
		return key;
	}

	@Override
	public Object queryByNum(ExportListType exportListType,String trslk,String sort,Integer num,String[] source,Boolean weight) throws TRSException {

		if(trslk.contains(FtsFieldConst.WEIGHT)){
			weight = true;
		}

		String type = null;
		if(ExportListType.ALERT.equals(exportListType)){
			type = "alert";
		}

		QueryCommonBuilder queryBuilder = new QueryCommonBuilder();
		queryBuilder.page(0,num);
		queryBuilder.filterByTRSL(trslk);
		queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,StringUtils.join(source," OR "),Operator.Equal);
		queryBuilder = setDatabase(queryBuilder,source);

		List<FtsDocumentCommonVO> result = new ArrayList<>();

		switch (sort) { // 排序
			case "asc":
				queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "relevance":// 相关性排序
				queryBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			case "desc":
				queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);

				break;
			case "hot":

				QueryBuilder hotBuilder = new QueryBuilder();
				hotBuilder.filterByTRSL(queryBuilder.asTRSL());
				hotBuilder.page(queryBuilder.getPageNo(),queryBuilder.getPageSize());
				String database = StringUtil.join(queryBuilder.getDatabase(),";");
				if (ObjectUtil.isNotEmpty(database)){
					hotBuilder.setDatabase(database);
				}
				QueryBuilder hotCountBuilder = new QueryBuilder();
				hotCountBuilder.filterByTRSL(queryBuilder.asTRSL());
				hotCountBuilder.page(queryBuilder.getPageNo(),queryBuilder.getPageSize());
				if (ObjectUtil.isNotEmpty(database)){
					hotCountBuilder.setDatabase(database);
				}
				log.info("导出前N条数据查询数据表达式 - 全部  按照热度值：" + hotBuilder.asTRSL());
				PagedList<FtsDocumentCommonVO> content = getHotList(hotBuilder, hotCountBuilder, UserUtils.getUser(),type);
				if(content.getPageItems() != null && content.getPageItems().size() > 0){
					result.addAll(content.getPageItems());
				}

			default:
				if (ExportListType.SIM.equals(exportListType) || ExportListType.CHART2LIST.equals(exportListType)) {
					queryBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				} else {
					if (ExportListType.ORDINARYSEARCH.equals(exportListType)) {
						queryBuilder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						if (weight) {
							queryBuilder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
						} else {
							queryBuilder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
						}
					}
				}
				break;
		}
		if(!"hot".equals(sort)){
			log.info("导出前N条数据查询数据表达式 - 全部：" + queryBuilder.asTRSL());
			PagedList<FtsDocumentCommonVO> pagedList = hybase8SearchService.pageListCommon(queryBuilder,false,false,false,type);
			if(pagedList.getPageItems() != null && pagedList.getPageItems().size() > 0){
				result.addAll(pagedList.getPageItems());
			}
		}

		String key = UUID.randomUUID().toString();
		RedisUtil.setMix(key, result);
		return key;
	}

	@Override
	public Object queryByNumOfOther(ExportListType exportListType, Integer num, String[] source,
									String time, String libraryId, String invitationCard, String forwarPrimary, String keywords,String fuzzyValueScope,String receivers) throws TRSException {

		List<FtsDocumentCommonVO> result = new ArrayList<>();
		User user = UserUtils.getUser();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String groupName = source[0];
		if (source.length > 1) {
			groupName = "ALL";
		}
		Integer resultNum = 0;
		StringBuffer ids = new StringBuffer();
		StringBuffer groupNames = new StringBuffer();
		StringBuffer urlTimes = new StringBuffer();

		if (ExportListType.COLLECT.equals(exportListType) || ExportListType.LIBRARY.equals(exportListType)) {
			InfoListResult allFavourites = null;
			List<Favourites> list = new ArrayList<>();
			if (ExportListType.COLLECT.equals(exportListType)) {
				allFavourites = (InfoListResult)reportService.getFavouritesByCondition(user, 0, num, Arrays.asList(source), keywords,fuzzyValueScope, invitationCard, forwarPrimary);

			} else if (ExportListType.LIBRARY.equals(exportListType)) {
				allFavourites = (InfoListResult)materialLibraryNewService.findMaterialSourceByCondition(libraryId,0,num,Arrays.asList(source),keywords,fuzzyValueScope,invitationCard,forwarPrimary,time);
			}

			if (allFavourites != null) {
				list = (List<Favourites>) allFavourites.getContent();
			}
			if(list.size() > 0 ){
				resultNum = list.size();
				for (Favourites favourite : list) {
					if (ids.length() == 0) {
						ids.append(favourite.getSid());
						groupNames.append(favourite.getGroupName());
						urlTimes.append(favourite.getUrlTime());
					} else {
						ids.append(";").append(favourite.getSid());
						groupNames.append(";").append(favourite.getGroupName());
						urlTimes.append(";").append(favourite.getUrlTime());
					}
				}
			}

		} else if (ExportListType.SMSALERT.equals(exportListType) || ExportListType.SENDALERT.equals(exportListType)) {
			String way = "";
			if (ExportListType.SMSALERT.equals(exportListType)) {
				way = "SMS";
			}
			List<AlertEntity> list = new ArrayList<>();
			if (httpClient) {
				String url = alertNetinsightUrl+"/alert/list?pageNo="+0+"&pageSize="+num+"&way="+way+"&source="+groupName+"&time="
						+time+"&receivers="+receivers+"&invitationCard="+invitationCard+"&forwarPrimary="+forwarPrimary+"&keywords="
						+keywords+"&userId="+user.getId()+"&userName="+user.getUserName();
				String doGet = HttpUtil.doGet(url, null);
				if (StringUtil.isEmpty(doGet)) {
					return null;
				} else if (doGet.contains("\"code\":500")) {
					Map<String, String> map = (Map<String, String>) JSON.parse(doGet);
					String message = map.get("message");
					throw new OperationException("预警导出失败,message:" + message, new Exception());
				} else {
					Map<String, Object> map = (Map<String, Object>) JSON.parse(doGet);
					Map<String, Object> object = (Map<String, Object>) map.get("list");
					list = JSONObject.parseArray(((JSON) object.get("content")).toJSONString(), AlertEntity.class);
				}
			}
			if(list.size() > 0){
				resultNum = list.size();
				for (AlertEntity ae : list) {
					if (ids.length() == 0) {
						ids.append(ae.getSid());
						groupNames.append(ae.getGroupName());
						urlTimes.append(formatter.format(ae.getTime()));
					} else {
						ids.append(";").append(ae.getSid());
						groupNames.append(";").append(ae.getGroupName());
						urlTimes.append(";").append(formatter.format(ae.getTime()));
					}
				}
			}
		}
		String key = UUID.randomUUID().toString();
		if(resultNum > 0){
			QueryBuilder builder = DateUtil.timeBuilder(urlTimes.toString());
			key = (String)queryByIds(ids.toString(), groupNames.toString(), builder);
		}else{
			RedisUtil.setMix(key, result);
		}


		return key;
	}

	public QueryCommonBuilder setDatabase(QueryCommonBuilder builder,String[] source){
		Set<String> database = new HashSet<>();
		for( String str : source){
			//public static final String MIX_DATABASE = HYBASE_NI_INDEX+";"+WECHAT_COMMON+";"+WEIBO+";"+HYBASE_OVERSEAS;
			if(Const.GROUPNAME_WEIBO.equals(str)){
				database.add(Const.WEIBO);
			}else if(Const.GROUPNAME_WEIXIN.equals(str)){
				database.add(Const.WECHAT_COMMON);
			}else if(Const.GROUPNAME_FACEBOOK.equals(str) || Const.GROUPNAME_TWITTER.equals(str)){
				database.add(Const.HYBASE_OVERSEAS);
			}else if(Const.GROUPNAME_XINWEN.equals(str) || Const.GROUPNAME_LUNTAN.equals(str) || Const.GROUPNAME_DIANZIBAO.equals(str) || Const.GROUPNAME_BOKE.equals(str)
					|| Const.GROUPNAME_KEHUDUAN.equals(str) || Const.GROUPNAME_GUOWAIXINWEN.equals(str)){
				database.add(Const.HYBASE_NI_INDEX);
			}
		}
		builder.setDatabase(database.toArray(new String[database.size()]));
		return  builder;
	}

	public PagedList<FtsDocumentCommonVO> getHotList(QueryBuilder builder, QueryBuilder countBuilder, User user,String type)
			throws TRSException {
		try {
			int pageSize = builder.getPageSize();
			if (pageSize > 50) {
				pageSize = 50;
				builder.setPageSize(50);
			} else if (pageSize <= 0) {
				pageSize = 10;
				builder.setPageSize(10);
			}
			String database = builder.getDatabase();
			if (database == null) {
				builder.setDatabase(Const.HYBASE_NI_INDEX);
				countBuilder.setDatabase(Const.HYBASE_NI_INDEX);
			}

			// 返回给前端总页数
			int pageListNo = 0;
			// 返回给前端总条数
			int pageListSize = 0;

			List<FtsDocumentCommonVO> ftsDocumentCommonVOS = new ArrayList<>();
			// hybase不能直接分页 每次都统计出50条 然后再取
			long pageNo = builder.getPageNo();
			// 从上一页到这一页 pageSize*pageNo到pageSize*pageNo+pageSize-1
			List<GroupInfo> groupList = new ArrayList();
			String key = CachekeyUtil.getToolKey(user,builder.asTRSL(),"hotchuagntong");
			List<GroupInfo> list = TimingCachePool.get(key);
			if (ObjectUtil.isNotEmpty(list)) {
				groupList = list;
			} else {
				builder.page(0, 50);
				//单独算法,已修改
				GroupResult md5TAG = hybase8SearchService.categoryQuery(builder, false, true,
						false, "MD5TAG",type,
						builder.getDatabase());
				groupList = md5TAG.getGroupList();
				TimingCachePool.put(key, groupList);
			}
			int size = 0;
			String keyCount = CachekeyUtil.getToolKey(user, builder.asTRSL(), "hotchuagntongcount");
			if (ObjectUtil.isNotEmpty(TimingCachePool.get(keyCount))) {
				size = TimingCachePool.get(keyCount);
			} else {
				countBuilder.page(0, 50);
				GroupResult md5TAGCount = hybase8SearchService.categoryQuery(countBuilder, false, true,false, "MD5TAG",
						type,countBuilder.getDatabase());
				size = md5TAGCount.getGroupList().size();
				if (size > 50) {
					size = 50;
				}
				TimingCachePool.put(keyCount, size);
			}
			int start = (int) (pageSize * pageNo);
			int end = (int) (pageSize * pageNo + pageSize - 1);
			if (start >= groupList.size()) {
				return null;
			}
			if (groupList.size() <= end) {
				end = groupList.size() - 1;
			}
			// 返回前端总页数
			pageListNo = groupList.size() % pageSize == 0 ? groupList.size() / pageSize
					: groupList.size() / pageSize + 1;
			pageListSize = groupList.size();
			for (int i = start; i <= end; i++) {
				GroupInfo info = groupList.get(i);
				QueryBuilder builder1 = new QueryBuilder();
				builder1.filterByTRSL(builder.asTRSL());
				builder1.page(0, 1);
				builder1.filterField("MD5TAG", info.getFieldValue(), Operator.Equal);
				builder1.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				List<FtsDocumentCommonVO> pagedList = hybase8SearchService.ftsQuery(builder1, FtsDocumentCommonVO.class,
						false, false,false,type);

				if(ObjectUtil.isNotEmpty(pagedList) && pagedList.size() >0){
					ftsDocumentCommonVOS.addAll(pagedList);
				}
			}
			if (0 == ftsDocumentCommonVOS.size()) {
				return null;
			}

			PagedList<FtsDocumentCommonVO> pagedList = new PagedList<FtsDocumentCommonVO>(pageListNo,
					(int) (pageSize < 0 ? 15 : pageSize), pageListSize, ftsDocumentCommonVOS, 1);

			return pagedList;

		} catch (Exception e) {
			throw new OperationException("listByHot error:" + e);
		}
	}

}
