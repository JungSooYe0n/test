package com.trs.netInsight.widget.report.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.trs.netInsight.widget.report.util.ReportUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.impl.Hybase8SearchImpl;
import com.trs.netInsight.support.template.ObjectContainer;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.report.entity.ReportResource;
import com.trs.netInsight.widget.report.entity.repository.ReportResourceRepository;
import static com.trs.netInsight.widget.report.constant.ReportConst.*;

/**
 *  日报、周报、月报
 * @author 北京拓尔思信息技术股份有限公司
 * Created by shao.guangze on 2018年6月21日 下午7:34:02
 */
@Slf4j
@NoArgsConstructor
public class ReportResourceTask implements Runnable{

	private FullTextSearch hybase8SearchService = (Hybase8SearchImpl) ObjectContainer
												.getBean(Hybase8SearchImpl.class);

	private ReportResourceRepository reportResourceRepository = (ReportResourceRepository) ObjectContainer.getBean(ReportResourceRepository.class);
	
	private List<ReportResource> reportResourcesList;
	
	public ReportResourceTask(List<ReportResource> reportResourcesList){
		this.reportResourcesList = reportResourcesList;
	}
	@Override
	public void run() {
		List<ReportResource> list ;
		try {
			list = reportResourceHandle(reportResourcesList);
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
			List<ReportResource> reportResourcesList) throws Exception {
		if (reportResourcesList == null || reportResourcesList.size() == 0) {
			log.info(String.format(REPORTRESOURCELOG,"资源重复"));
			return null;
		} else {
			if (reportResourcesList.size() == 1) {
				// 可能是单1条列表类型数据，或图片
				ReportResource reportResource = reportResourcesList.get(0);//s.isNullOrEmpty(reportResource.getImg_data())
				
				if (StringUtil.isEmpty(reportResource.getImg_data()) && !OVERVIEWOFDATA.equals(reportResource.getChapter())) {
					log.info(String.format(REPORTRESOURCELOG,"单条报告列表资源！"));
					// 列表类型数据
					if (reportResource.getGroupName().equals("国内微信")) {
						ArrayList<ReportResource> reportResourcePool = reportResourcePool(null,new String[] { reportResource.getSid() });
						log.info(String.format(REPORTRESOURCELOG,"根据SID查询报告资源完成！ "));
						// 计算转载数
						//produceRttCount(reportResourcePool);
						log.info(String.format(REPORTRESOURCELOG,"转载数计算完成！"));
						setReportResourceData(reportResource,reportResourcePool.get(0));
					} else {
						ArrayList<String> arrayList = new ArrayList<>();
						arrayList.add(reportResource.getSid());
						ArrayList<ReportResource> reportResourcePool = reportResourcePool(arrayList, null);
						setReportResourceData(reportResource,reportResourcePool.get(0));
					}
				} else {
					log.info(String.format(REPORTRESOURCELOG,"单条报告图片资源！"));
					// 图片
					// 如果是数据统计概述章节的话
					if (OVERVIEWOFDATA.equals(reportResource.getChapter())) {
						if (StringUtil.isNotEmpty(reportResource.getImg_data())){
							reportResource.setImgComment(ReportUtil.getOverviewOfData(reportResource.getImg_data().replace("\\n", "")));
						}
					} else {
						// 图片添加comment
						reportResource.setImgComment(ReportUtil.getImgComment(reportResource.getImg_data(),reportResource.getImgType(),reportResource.getChapter()));
					}
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
					if (!"国内微信".equals(key)) {
						collectResults.get(key).stream().forEach(e -> sidList.add(e.getSid()));
					}
				}
				String[] sidsWeChat = null;
				if(collectResults.get("国内微信")!=null){
					sidsWeChat = collectResults.get("国内微信").stream().map(ReportResource::getSid).toArray(String[]::new);
				}
				// 一次性全部查出来
				ArrayList<ReportResource> reportResourcePool = reportResourcePool(sidList, sidsWeChat);
				// 计算转载数
				//produceRttCount(reportResourcePool);
				//信息合并
					for (int i = 0; i < reportResourcesList.size(); i++) {
						ReportResource reportResource = reportResourcesList.get(i);
						for (ReportResource reportResourceInner : reportResourcePool) {
							if (reportResource.getSid().equals(reportResourceInner.getSid())) {
								// 匹配成功,重新赋值
								setReportResourceData(reportResource,reportResourceInner);
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
	 * 计算转载数
	 * @author shao.guangze
	 * @param reportResourcePool
	 */
//	private void produceRttCount(ArrayList<ReportResource> reportResourcePool) {
//		for (int i = 0; i < reportResourcePool.size(); i++) {
//			ReportResource reportResource = reportResourcePool.get(i);
//			if (reportResource.getMd5Tag() == null) {
//				reportResource.setRttCount(new Long(0));
//				reportResourcePool.remove(i);
//				reportResourcePool.add(i, reportResource);
//				continue;
//			}
//			String md5queryTRSL = FtsFieldConst.FIELD_MD5TAG + ":"+ reportResource.getMd5Tag();
//			QueryCommonBuilder queryBuilder = new QueryCommonBuilder();
//			queryBuilder.setDatabase(Const.HYBASE_NI_INDEX.split(";"));
//			queryBuilder.filterByTRSL(md5queryTRSL);
//			// 昨天0点到当前时间
//			queryBuilder.filterByTRSL(FtsFieldConst.FIELD_URLTIME + ":"
//					+ getFormatDateTRSL());
//			long ftsCount = hybase8SearchService.ftsCountCommon(
//					queryBuilder, false,false);
//			reportResource.setRttCount(ftsCount);
//			reportResourcePool.remove(i);
//			reportResourcePool.add(i, reportResource);
//		}
//	}
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
			String[] sidsWeChat) throws TRSException {
		String sidsTRSL = ReportUtil.buildSql(sidList);
		String weChatTRSL = null;
		if (sidsWeChat != null) {
			List<String> hkeysWeChat = Arrays.asList(sidsWeChat);
			weChatTRSL = ReportUtil.buildSqlWeiXin(hkeysWeChat);
		}
		QueryCommonBuilder queryBuilder = new QueryCommonBuilder();
		if (!StringUtils.isEmpty(sidsTRSL) && !StringUtils.isEmpty(weChatTRSL)) {
			queryBuilder.filterByTRSL("(" + sidsTRSL + ") OR (" + weChatTRSL
					+ ")");
		} else if (!StringUtils.isEmpty(sidsTRSL)) {
			queryBuilder.filterByTRSL(sidsTRSL);
		} else if (!StringUtils.isEmpty(weChatTRSL)) {
			queryBuilder.filterByTRSL(weChatTRSL);
		} else {
			return null;
		}
		String[] split = Const.MIX_DATABASE.split(";");
		queryBuilder.setDatabase(split);
		queryBuilder.setPageSize(200);
		// Operator.Equal);
		PagedList<FtsDocumentCommonVO> ftsQueryResult = hybase8SearchService
				.pageListCommon(queryBuilder, false,false,false,"detail");
		ArrayList<ReportResource> reportResources = new ArrayList<>();
		if (ftsQueryResult != null && ftsQueryResult.size() > 0) {
			ReportResource reportResourceTemp = null;
			for (FtsDocumentCommonVO ftsDocumentCommonVO : ftsQueryResult
					.getPageItems()) {
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
					// 传统媒体
					reportResourceTemp.setTitle(ReportUtil.replaceHtml(StringUtil.removeFourChar(StringUtil.replaceImg(ftsDocumentCommonVO.getTitle()))));
					reportResourceTemp.setSrcName(ftsDocumentCommonVO
							.getSrcName());
					reportResourceTemp.setSiteName(ftsDocumentCommonVO
							.getSiteName());
					reportResourceTemp.setSid(ftsDocumentCommonVO.getSid());
					reportResourceTemp.setContent(ReportUtil.replaceHtml(StringUtil.removeFourChar(StringUtil.replaceImg(ftsDocumentCommonVO
							.getContent()))));
					reportResourceTemp.setNewsAbstract(ReportUtil.replaceHtml(StringUtil.removeFourChar(StringUtil.replaceImg(ftsDocumentCommonVO.getAbstracts()))));
				}

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
		reportResource.setMd5Tag(hybaseReportResource.getMd5Tag());
		reportResource.setTitle(ReportUtil.replaceHtml(StringUtil.removeFourChar(hybaseReportResource.getTitle())));
		reportResource.setContent(ReportUtil.replaceHtml(StringUtil.removeFourChar(hybaseReportResource.getContent())));
		reportResource.setSiteName(hybaseReportResource.getSiteName());
		reportResource.setSrcName(hybaseReportResource.getSrcName());
		reportResource.setNewsAbstract(ReportUtil.replaceHtml(StringUtil.removeFourChar(hybaseReportResource.getNewsAbstract())));
		reportResource.setUrlName(hybaseReportResource.getUrlName());
	}

}
