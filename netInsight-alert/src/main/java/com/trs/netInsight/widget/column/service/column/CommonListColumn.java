package com.trs.netInsight.widget.column.service.column;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.trs.netInsight.support.fts.annotation.FtsField;
import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeBase;
import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeClassify;
import com.trs.netInsight.support.knowledgeBase.service.IKnowledgeBaseService;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.analysis.entity.ChartResultField;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.report.util.ReportUtil;
import com.trs.netInsight.widget.user.entity.User;
import javafx.animation.FadeTransition;
import org.apache.commons.lang3.StringUtils;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.builder.condition.SearchCondition;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * 普通新闻列表栏目
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年4月8日
 */
public class CommonListColumn extends AbstractColumn {

	@Override
	public Object getColumnData(String timeRange) throws TRSSearchException {
		long start = new Date().getTime();
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		//用queryCommonBuilder和QueryBuilder 是一样的的
		QueryCommonBuilder builder = super.config.getCommonBuilder();

		IndexTab indexTab = super.config.getIndexTab();
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		List<Map<String, Object>> list = new ArrayList<>();
		try {
			//微博content字段已经有title别名  不需要再为微博写个builder

			//普通列表按照时间倒序来排，热点列表还是按相似文章数来排
			String uid = UUID.randomUUID().toString();
			RedisUtil.setString(uid, builder.asTRSL());
			builder.page(super.config.getPageNo(), super.config.getPageSize());
			String source = indexTab.getGroupName();
			PagedList<FtsDocumentCommonVO> listCommon = commonListService.queryPageListNoFormat(builder,sim,irSimflag,irSimflagAll,"column",source);
			//PagedList<FtsDocumentCommonVO> listCommon = hybase8SearchService.pageListCommon(builder, sim,irSimflag,irSimflagAll,"column");
			if (listCommon == null || listCommon.getPageItems() == null || listCommon.getPageItems().size() == 0) {
				return null;
			}
			for (FtsDocumentCommonVO vo : listCommon.getPageItems()) {
				Map<String, Object> map = new HashMap<>();
				map.put("trslk", uid);
				String groupName = CommonListChartUtil.formatPageShowGroupName(vo.getGroupName());
				map.put("id", vo.getSid());
				if (Const.PAGE_SHOW_WEIXIN.equals(groupName)) {
					map.put("id", vo.getHkey());
				}
				map.put("groupName", groupName);
				map.put("time", vo.getUrlTime());
				map.put("md5", vo.getMd5Tag());
				String title= vo.getTitle();
				if(StringUtil.isNotEmpty(title)){
					title = StringUtil.replacePartOfHtml(StringUtil.cutContentPro(StringUtil.replaceImg(title), Const.CONTENT_LENGTH));
				}
				map.put("title", title);
				String content = "";
				if (StringUtil.isNotEmpty(vo.getContent())) {
					content = StringUtil.cutContentPro(StringUtil.replaceImg(vo.getContent()), Const.CONTENT_LENGTH);
				}
				if (StringUtil.isNotEmpty(vo.getAbstracts())) {
					vo.setAbstracts(StringUtil.cutContentPro(StringUtil.replaceImg(vo.getAbstracts()), Const.CONTENT_LENGTH));
				}
				//摘要
				map.put("abstracts", vo.getAbstracts());

				map.put("nreserved1", null);
				map.put("hkey", null);
				if (Const.PAGE_SHOW_LUNTAN.equals(groupName)) {
					map.put("nreserved1", vo.getNreserved1());
					map.put("hkey", vo.getHkey());
				}
				map.put("urlName", vo.getUrlName());
				//微博、Facebook、Twitter、短视频等没有标题，应该用正文当标题
				if (Const.PAGE_SHOW_WEIBO.equals(groupName)) {
					map.put("title", content);
					map.put("abstracts", content);

					map.put("siteName", vo.getScreenName());
				} else if (Const.PAGE_SHOW_FACEBOOK.equals(groupName) || Const.PAGE_SHOW_TWITTER.equals(groupName)) {
					map.put("title", content);
					map.put("abstracts", content);
					map.put("siteName", vo.getAuthors());
				} else if(Const.PAGE_SHOW_DUANSHIPIN.equals(groupName) || Const.PAGE_SHOW_CHANGSHIPIN.equals(groupName)){
					map.put("title", content);
					map.put("abstracts", content);
				}else {
					map.put("siteName", vo.getSiteName());
				}
				map.put("commtCount", vo.getCommtCount());
				map.put("rttCount", vo.getRttCount());
				map.put("simNum", 0);
// 获得时间差,三天内显示时间差,剩下消失urltime
				Map<String, String> timeDifference = DateUtil.timeDifference(vo);
				boolean isNew = false;
				if (ObjectUtil.isNotEmpty(timeDifference.get("timeAgo"))) {
					isNew = true;
					map.put("timeAgo", timeDifference.get("timeAgo"));
				} else {
					map.put("timeAgo", timeDifference.get("urlTime"));
				}
				map.put("isNew", isNew);
				list.add(map);
			}
			return list;
		} catch (TRSException e) {
			throw new TRSSearchException(e);
		}finally {
			LogPrintUtil logReids = RedisUtil.getLog(id);
			long end = new Date().getTime();
			int timeApi = (int) (end - start);
			logReids.setComeBak(start);
			logReids.setFinishBak(end);
			logReids.setFullBak(timeApi);
			if(logReids.getFullHybase()>FtsFieldConst.OVER_TIME){
				logReids.printTime(LogPrintUtil.Column_LIST);
			}
		}
	}

	@Override
	public Object getColumnCount() throws TRSSearchException {
		long id = Thread.currentThread().getId();
		LogPrintUtil loginpool = new LogPrintUtil();
		RedisUtil.setLog(id, loginpool);
		QueryCommonBuilder builder = super.config.getCommonBuilder();
		IndexTab indexTab = super.config.getIndexTab();
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		String source = indexTab.getGroupName();
		long countCommon = 0L;
		try {
			commonListService.ftsCount(builder, sim, irSimflag,irSimflagAll,"column",source);
		}catch (TRSException e){
			throw new TRSSearchException(e);
		}
		return countCommon;
	}

	@Override
	public Object getSectionList() throws TRSSearchException {
		//source不为空就是正常列表的 source为空就是混合列表的
		String groupName = this.config.getGroupName();

		User loginUser = UserUtils.getUser();
		IndexTab indexTab = super.config.getIndexTab();
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		String metas = indexTab.getGroupName();
		if("ALL".equals(groupName)){
			groupName = metas;
		}
		//用queryCommonBuilder和QueryBuilder 是一样的的
		QueryCommonBuilder queryBuilder = super.config.getCommonBuilder();

		try {
			if ("hot".equals(this.config.getOrderBy())) {
				//暂时先改成false，提升查询速度
				return commonListService.queryPageListForHot(queryBuilder, groupName, loginUser, "column", true);
			} else {
				return commonListService.queryPageList(queryBuilder, sim, irSimflag, irSimflagAll, groupName, "column", loginUser, true);
			}
		} catch (TRSException e) {
			throw new TRSSearchException(e);
		}
	}

	@Override
	public Object getAppSectionList(User user) throws TRSSearchException {
		QueryCommonBuilder queryBuilder = super.config.getCommonBuilder();
		IndexTab indexTab = super.config.getIndexTab();
		queryBuilder.page(super.config.getPageNo(), super.config.getPageSize());
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		try {
			String groupName = this.config.getGroupName();
			if("hot".equals(this.config.getOrderBy())){
				return commonListService.queryPageListForHot(queryBuilder,groupName,user,"column",true);
			}else {
				return commonListService.queryPageList(queryBuilder, sim, irSimflag, irSimflagAll, groupName, "column", user, true);
			}
		} catch (TRSException e) {
			throw new TRSSearchException(e);
		}
	}
	/**
	 * 信息列表统计 - 但是页面上的信息列表统计不受栏目类型影响，所以只需要用普通列表的这个方法即可
	 * 对应为信息列表的数据源条数统计
	 * @return
	 * @throws TRSSearchException
	 */
	@Override
	public Object getListStattotal() throws TRSSearchException {
		QueryCommonBuilder queryBuilder = super.config.getCommonBuilder();
		IndexTab indexTab = super.config.getIndexTab();
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		String groupName = indexTab.getGroupName();
		try {
			ChartResultField resultField = new ChartResultField("name", "value");
			List<Map<String, Object>> cateqoryQuery = (List<Map<String, Object>>)commonListService.queryListGroupNameStattotal(queryBuilder, sim, irSimflag, irSimflagAll, groupName, "column", resultField);
			Long count = 0L;
			for(Map<String, Object> map :cateqoryQuery){
				count += (Long)map.get(resultField.getCountField());
			}
			Map<String, Object> total = new HashMap<>();
			total.put(resultField.getContrastField(),"全部");
			total.put(resultField.getCountField(),count);
			cateqoryQuery.add(0,total);
			return cateqoryQuery;
		} catch (TRSException e) {
			throw new TRSSearchException(e);
		}
	}

	@Override
	public QueryBuilder createQueryBuilder() {
		return null;
	}

	@Override
	public QueryCommonBuilder createQueryCommonBuilder() {
		QueryCommonBuilder builder = super.config.getCommonBuilder();
		int pageNo = super.config.getPageNo();
		int pageSize = super.config.getPageSize();
		builder.setPageNo(pageNo);
		if( pageSize != 0){
			builder.setPageSize(pageSize);
		}
		return builder;
	}
}
