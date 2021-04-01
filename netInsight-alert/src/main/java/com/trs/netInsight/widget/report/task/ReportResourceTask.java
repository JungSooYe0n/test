package com.trs.netInsight.widget.report.task;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.util.CollectionsUtil;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.report.constant.Chapter;
import com.trs.netInsight.widget.report.constant.ReportConst;
import com.trs.netInsight.widget.report.entity.ReportNew;
import com.trs.netInsight.widget.report.util.ReportUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.impl.Hybase8SearchImpl;
import com.trs.netInsight.support.template.ObjectContainer;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.report.entity.ReportResource;
import com.trs.netInsight.widget.report.entity.repository.ReportResourceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static com.trs.netInsight.widget.report.constant.ReportConst.*;

/**
 *  日报、周报、月报
 * @author 北京拓尔思信息技术股份有限公司
 * Created by shao.guangze on 2018年6月21日 下午7:34:02
 */
@Slf4j
@NoArgsConstructor
public class ReportResourceTask implements Runnable{
	private ICommonListService commonListService = (ICommonListService) ObjectContainer.getBean(ICommonListService.class);

	private ReportResourceRepository reportResourceRepository = (ReportResourceRepository) ObjectContainer.getBean(ReportResourceRepository.class);
	
	private List<ReportResource> reportResourcesList;
	private String trslk;
	
	public ReportResourceTask(List<ReportResource> reportResourcesList){
		this.reportResourcesList = reportResourcesList;
	}
	public ReportResourceTask(List<ReportResource> reportResourcesList,String trslk){
		this.reportResourcesList = reportResourcesList;
		this.trslk = trslk;
	}
	@Override
	public void run() {
		List<ReportResource> list ;
		try {
			list = reportResourceHandle(reportResourcesList,trslk);
			reportResourceRepository.save(list);
			log.info(String.format(REPORTRESOURCELOG,"成功保存至数据库，size："+list.size()));
		} catch (Exception e) {
			log.error("添加报告资源失败", e);
			e.printStackTrace();
		}
	}
	/**
	 *  sid -> hybase查数据	,对象合并
	 *  图片处理	，添加comment
	 * @author shao.guangze
	 * @param reportResourcesList
	 * @return
	 * @throws Exception
	 */
	public List<ReportResource> reportResourceHandle(
			List<ReportResource> reportResourcesList,String trslk) throws Exception {
		if (reportResourcesList == null || reportResourcesList.size() == 0) {
			log.info(String.format(REPORTRESOURCELOG,"资源重复"));
			return null;
		} else {
			if (reportResourcesList.size() == 1) {
				// 可能是单1条列表类型数据，或图片
				ReportResource reportResource = reportResourcesList.get(0);//s.isNullOrEmpty(reportResource.getImg_data())
				
				if (StringUtil.isEmpty(reportResource.getImg_data()) && (!OVERVIEWOFDATA.equals(reportResource.getChapter())&& !Chapter.Statistics_Summarize.toString().equals(reportResource.getChapter())) ) {
					log.info(String.format(REPORTRESOURCELOG,"单条报告列表资源！"));
					// 列表类型数据
					if (reportResource.getGroupName().equals(Const.GROUPNAME_WEIXIN)) {
						ArrayList<ReportResource> reportResourcePool = reportResourcePool(null,new String[] { reportResource.getSid() },trslk);
						log.info(String.format(REPORTRESOURCELOG,"根据SID查询报告资源完成！ "));
						// 计算转载数
						//produceRttCount(reportResourcePool);
						log.info(String.format(REPORTRESOURCELOG,"转载数计算完成！"));
						setReportResourceData(reportResource,reportResourcePool.get(0));
					} else {
						ArrayList<String> arrayList = new ArrayList<>();
						arrayList.add(reportResource.getSid());
						ArrayList<ReportResource> reportResourcePool = reportResourcePool(arrayList, null,trslk);
						setReportResourceData(reportResource,reportResourcePool.get(0));
					}
				} else {
					log.info(String.format(REPORTRESOURCELOG,"单条报告图片资源！"));
					// 图片
					// 如果是数据统计概述章节的话
					if (OVERVIEWOFDATA.equals(reportResource.getChapter()) ||Chapter.Statistics_Summarize.toString().equals(reportResource.getChapter())) {
						if (StringUtil.isNotEmpty(reportResource.getImg_data())){
							reportResource.setImgComment(ReportUtil.getOverviewOfData(reportResource.getImg_data().replace("\\n", "")));
						}
					} else {
						// 图片添加comment
						reportResource.setImgComment(ReportUtil.getImgComment(reportResource.getImg_data(),reportResource.getImgType(),Chapter.valueOf(reportResource.getChapter()).getValue()));
					}
					reportResource.setDocPosition(0);

					//查找当前模块当前位置有几条数据 - 图表，当前位置只允许一个图，如果之前加了图，就删除他们
					removeSameModules(reportResource.getTemplateId(),reportResource.getChapter(),reportResource.getChapterPosition(),reportResource.getResourceStatus());

				}
				reportResourcesList.remove(0);
				reportResourcesList.add(reportResource);
				return reportResourcesList;
			} else {
				log.info(String.format(REPORTRESOURCELOG,"多条报告列表资源！"));
				// 只可能是多条列表类型数据
				// 需要把微信单独拿出来 因为微信主键使用的是hkey
				Map<String, List<ReportResource>> collectResults = reportResourcesList.stream().collect(Collectors.groupingBy(ReportResource::getGroupName));
				Set<String> keySet = collectResults.keySet();
				List<String> sidList = new ArrayList<>();
				for (String key : keySet) {
					if (!Const.GROUPNAME_WEIXIN.equals(key)) {
						collectResults.get(key).stream().forEach(e -> sidList.add(e.getSid()));
					}
				}
				String[] sidsWeChat = null;
				if(collectResults.get(Const.GROUPNAME_WEIXIN)!=null){
					sidsWeChat = collectResults.get(Const.GROUPNAME_WEIXIN).stream().map(ReportResource::getSid).toArray(String[]::new);
				}
				// 一次性全部查出来
				ArrayList<ReportResource> reportResourcePool = reportResourcePool(sidList, sidsWeChat,trslk);

				//信息合并
				for (int i = 0; i < reportResourcesList.size(); i++) {
					ReportResource reportResource = reportResourcesList.get(i);
					for (ReportResource reportResourceInner : reportResourcePool) {
						if (reportResource.getSid().equals(reportResourceInner.getSid())) {
							// 匹配成功,重新赋值
							setReportResourceData(reportResource, reportResourceInner);
							reportResourcesList.remove(i);
							reportResourcesList.add(i, reportResource);
							break;
						}
					}
				}
			}
			return reportResourcesList;
		}
		}

	/**
	 * 获取昨天0点到当前时间的 时间数组， 24h+
	 * 
	 * @author shao.guangze
	 * @return
	 */
	private String getFormatDateTRSL() {
		Calendar calendar = Calendar.getInstance();
		String currentDate = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(calendar.getTime());
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		String yesterdayDate = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(calendar.getTime());
		// 获取到昨天
		yesterdayDate = yesterdayDate.substring(0, 8) + "000000";
		return "[" + yesterdayDate + " TO " + currentDate + "]";
	}
	private ArrayList<ReportResource> reportResourcePool(List<String> sidList,
			String[] sidsWeChat,String trslk) throws TRSException {
		if(!CollectionsUtil.isNotEmpty(sidList) && (sidsWeChat==null || sidsWeChat.length==0)){
			return null;
		}
		String type = "detail";
		String sidsTRSL = ReportUtil.buildSql(sidList);
		String weChatTRSL = null;
		if (sidsWeChat != null) {
			List<String> hkeysWeChat = Arrays.asList(sidsWeChat);
			weChatTRSL = ReportUtil.buildSqlWeiXin(hkeysWeChat);
		}
		QueryBuilder builderWeiXin = new QueryBuilder();//微信的表达式
		QueryBuilder builder = new QueryBuilder();//其他数据源的表达式
		String trsl = "";
		if(StringUtil.isNotEmpty(trslk)){
			 trsl= RedisUtil.getString(trslk);
			trsl = removeSimflag(trsl);
			builder.filterByTRSL(trsl);
			builderWeiXin.filterByTRSL(trsl);
		}

		List<FtsDocumentCommonVO> result = new ArrayList<>();
		if(CollectionsUtil.isNotEmpty(sidList)){
			builder.filterByTRSL(sidsTRSL);
			builder.setDatabase(Const.MIX_DATABASE);
			builder.page(0,200);
			PagedList<FtsDocumentCommonVO> pagedList = commonListService.queryPageListNoFormat(builder, false, false, false, type,null);

			if (pagedList != null && pagedList.getPageItems() != null && pagedList.getPageItems().size() > 0) {
				result.addAll(pagedList.getPageItems());
			}
		}
		if(sidsWeChat != null &&sidsWeChat.length>0){
			builderWeiXin.filterByTRSL(weChatTRSL);
			builderWeiXin.setDatabase(Const.WECHAT);
			builderWeiXin.page(0,200);
			PagedList<FtsDocumentCommonVO> pagedList = commonListService.queryPageListNoFormat(builderWeiXin, false, false, false, type, Const.GROUPNAME_WEIXIN);
			if (pagedList != null && pagedList.getPageItems() != null && pagedList.getPageItems().size() > 0) {
				result.addAll(pagedList.getPageItems());
			}
		}


		ArrayList<ReportResource> reportResources = new ArrayList<>();
		if (result.size() > 0) {
			ReportResource reportResourceTemp = null;
			for (FtsDocumentCommonVO ftsDocumentCommonVO : result) {
				reportResourceTemp = new ReportResource();
				reportResourceTemp.setUrlDate(ftsDocumentCommonVO.getUrlTime());
				reportResourceTemp.setGroupName(ftsDocumentCommonVO
						.getGroupName());
				reportResourceTemp.setMd5Tag(ftsDocumentCommonVO.getMd5Tag());
				reportResourceTemp.setUrlName(ftsDocumentCommonVO.getUrlName());

				/*
				 * 获取 传统媒体、微信的标题, 微博获取正文 来源，微信微博都是获取authoers
				 * 放到sitename里，传统媒体是获取srcname放到srcname里。
				 * 微信唯一标识使用的hkey,但再ReportResource里不做区分，一律使用sid 微信微博获取正文，传统媒体获取摘要
				 */
				if ("微博".equals(ftsDocumentCommonVO.getGroupName())) {
					reportResourceTemp.setTitle(ReportUtil.replaceHtml(StringUtil.removeFourChar(StringUtil.replaceImg(ftsDocumentCommonVO
							.getContent()))));
					reportResourceTemp.setSiteName(ftsDocumentCommonVO
							.getAuthors());
					reportResourceTemp.setSid(ftsDocumentCommonVO.getSid());
					reportResourceTemp.setContent(ReportUtil.replaceHtml(StringUtil.removeFourChar(StringUtil.replaceImg(ftsDocumentCommonVO
							.getContent()))));
				} else if ("国内微信".equals(ftsDocumentCommonVO.getGroupName())) {
					reportResourceTemp.setTitle(ReportUtil.replaceHtml(StringUtil.removeFourChar(StringUtil.replaceImg(ftsDocumentCommonVO.getTitle()))));
					reportResourceTemp.setSiteName(ftsDocumentCommonVO
							.getSiteName());
					reportResourceTemp.setSid(ftsDocumentCommonVO.getHkey());
					reportResourceTemp.setContent(ReportUtil.replaceHtml(StringUtil.removeFourChar(StringUtil.replaceImg(ftsDocumentCommonVO
							.getContent()))));
				} else {
					if (Const.MEDIA_TYPE_ZIMEITI_BOKE.contains(ftsDocumentCommonVO.getGroupName()) || Const.MEDIA_TYPE_VIDEO.contains(ftsDocumentCommonVO.getGroupName())){
						//自媒体号、博客、视频、短视频
						reportResourceTemp.setSiteName(ftsDocumentCommonVO.getSiteName() + (StringUtil.isNotEmpty(ftsDocumentCommonVO.getAuthors()) ?  "-" + ftsDocumentCommonVO.getAuthors() : ""));
					} else {
						//传统媒体
						reportResourceTemp.setSiteName(ftsDocumentCommonVO.getSiteName());
					}
					reportResourceTemp.setTitle(ReportUtil.replaceHtml(StringUtil.removeFourChar(StringUtil.replaceImg(ftsDocumentCommonVO.getTitle()))));
					reportResourceTemp.setSrcName(ftsDocumentCommonVO.getSrcName());
					reportResourceTemp.setSid(ftsDocumentCommonVO.getSid());
					reportResourceTemp.setContent(ReportUtil.replaceHtml(StringUtil.removeFourChar(StringUtil.replaceImg(ftsDocumentCommonVO.getContent()))));
					reportResourceTemp.setNewsAbstract(ReportUtil.replaceHtml(StringUtil.removeFourChar(StringUtil.replaceImg(ftsDocumentCommonVO.getAbstracts()))));
				}

				Integer simnum = calculateMd5Hot(ftsDocumentCommonVO,trsl,type);
				reportResourceTemp.setSimNum(simnum.toString());
				reportResourceTemp.setSimCount(simnum.toString());
				reportResources.add(reportResourceTemp);
			}
		}
		return reportResources;
	}
	/**
	 * 合并数据，将hybase查询出来的结果(title、content、srcname等)与原有对象(templateId、reportType等)进行合并
	 * @author shao.guangze
	 * @param reportResource
	 * @param hybaseReportResource
	 */
	private void setReportResourceData(ReportResource reportResource,
			ReportResource hybaseReportResource) {
		
		reportResource.setUrlDate(hybaseReportResource.getUrlDate());
		reportResource.setRttCount(hybaseReportResource.getRttCount());
		reportResource.setSimCount(hybaseReportResource.getSimCount());
		reportResource.setSimNum(hybaseReportResource.getSimNum());
		reportResource.setMd5Tag(hybaseReportResource.getMd5Tag());
		reportResource.setTitle(ReportUtil.replaceHtml(StringUtil.removeFourChar(hybaseReportResource.getTitle())));
		reportResource.setContent(ReportUtil.replaceHtml(StringUtil.removeFourChar(hybaseReportResource.getContent())));
		reportResource.setSiteName(hybaseReportResource.getSiteName());
		reportResource.setSrcName(hybaseReportResource.getSrcName());
		reportResource.setNewsAbstract(ReportUtil.replaceHtml(StringUtil.removeFourChar(hybaseReportResource.getNewsAbstract())));
		reportResource.setUrlName(hybaseReportResource.getUrlName());
	}

	private Integer removeSameModules(String templateId,String chapter,Integer chapterPosition,Integer resourceStatus){
		 List<ReportResource> list =  reportResourceRepository.findAll((Root<ReportResource> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->{
			ArrayList<Predicate> allPredicates = new ArrayList<>();

			allPredicates.add(cb.equal(root.get("templateId").as(String.class), templateId));
			allPredicates.add(cb.equal(root.get("chapter").as(String.class), chapter));
			allPredicates.add(cb.equal(root.get("chapterPosition").as(String.class), chapterPosition));
			allPredicates.add(cb.equal(root.get("resourceStatus").as(String.class), resourceStatus));
			return cb.and(allPredicates.toArray(new Predicate[allPredicates.size()]));
		});
		 int size = 0;
		if(list != null && list.size()>0){
			size = list.size();
			reportResourceRepository.delete(list);
		}
		return size;
	}

	private String removeSimflag(String trslk) {
		if(trslk.indexOf("AND SIMFLAG:(1000 OR \"\")") != -1){
			trslk = trslk.replace(" AND SIMFLAG:(1000 OR \"\")","");
		}else if (trslk.indexOf("AND (IR_SIMFLAGALL:(\"0\" OR \"\"))") != -1){
			trslk = trslk.replace(" AND (IR_SIMFLAGALL:(\"0\" OR \"\"))","");
		}
		trslk = trslk.replaceAll("AND \\(IR_SIMFLAGALL:\\(\"0\" OR \"\"\\)\\)"," ");
		trslk = trslk.replaceAll("AND (IR_SIMFLAG:(0 OR \"\"))","");
		return trslk;
	}

	private Integer calculateMd5Hot(FtsDocumentCommonVO document,String trsl,String type){
		QueryBuilder searchBuilder = new QueryBuilder();
		if(StringUtil.isNotEmpty(trsl)){
			trsl = removeSimflag(trsl);
			searchBuilder.filterByTRSL(trsl);
		}
		String md5Tag = document.getMd5Tag();
		String id = document.getSid();
		if(document.getGroupName().equals(Const.GROUPNAME_WEIXIN)){
			id= document.getHkey();
		}
		if (StringUtil.isNotEmpty(md5Tag)) {
			searchBuilder.filterField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);
			if (StringUtil.isNotEmpty(id)) {
				//id不为空，则去掉当前文章
				StringBuffer idBuffer = new StringBuffer();
				if (Const.MEDIA_TYPE_WEIBO.contains(document.getGroupName())) {
					idBuffer.append(FtsFieldConst.FIELD_MID).append(":(").append(id).append(")");
				} else if (Const.MEDIA_TYPE_WEIXIN.contains(document.getGroupName())) {
					idBuffer.append(FtsFieldConst.FIELD_HKEY).append(":(").append(id).append(")");
				} else {
					idBuffer.append(FtsFieldConst.FIELD_SID).append(":(").append(id).append(")");
				}
				searchBuilder.filterByTRSL_NOT(idBuffer.toString());
			}
			searchBuilder.setPageSize(1);
			searchBuilder.setDatabase(Const.MIX_DATABASE);
			Integer simNum = 0;
			try {
				Long ftsCount = commonListService.ftsCount(searchBuilder, false, true, false, type);
				if(ftsCount != null && ftsCount >0){
					simNum = Integer.valueOf(ftsCount.toString());
				}
			} catch (TRSException e) {
				e.printStackTrace();
			}
			return simNum;

		}else{
			return 0;
		}
	}




}
