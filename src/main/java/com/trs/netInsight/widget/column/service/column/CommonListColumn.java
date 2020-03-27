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
		QueryCommonBuilder builder = this.createQueryCommonBuilder();

		IndexTab indexTab = super.config.getIndexTab();
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		List<Map<String, Object>> list = new ArrayList<>();
		try {
			//微博content字段已经有title别名  不需要再为微博写个builder

			//普通列表按照时间倒序来排，热点列表还是按相似文章数来排
			//2018/08/10 changed,普通列表排序方式是什么，就是什么。
			//builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME);
			builder.page(super.config.getPageNo(), super.config.getPageSize());
			PagedList<FtsDocumentCommonVO> listCommon = hybase8SearchService.pageListCommon(builder, sim,irSimflag,irSimflagAll,"column");
			Map<String, Object> map = null;
			String uid = UUID.randomUUID().toString();
			RedisUtil.setString(uid, builder.asTRSL());
			for (FtsDocumentCommonVO document : listCommon.getPageItems()) {
				map = new HashMap<>();
				// 处理微信id
				if (document.getGroupName().equals("国内微信")) {
					map.put("sid", document.getHkey());
				}else {
					map.put("sid", document.getSid());
				}
				if (document.getGroupName().equals("微博")) {
					map.put("siteName", document.getScreenName());
					map.put("commtCount", document.getCommtCount());
					map.put("rttCount", document.getRttCount());
				}else if (document.getGroupName().equals("FaceBook") || document.getGroupName().equals("Twitter") || document.getGroupName().equals("Facebook")){
					map.put("siteName", document.getAuthors());
					map.put("commtCount", document.getCommtCount());
					map.put("rttCount", document.getRttCount());
				}else {
					map.put("siteName", document.getSiteName());
				}
				map.put("trslk", uid);
				if (document.getGroupName().equals("Twitter")){//去掉Twitter数据标题开头有两个类似空格的符号
					String title = document.getTitle();
					title = title.substring(2);
					map.put("title", StringUtil.replacePartOfHtml(title));
				}else {
					map.put("title", StringUtil.replacePartOfHtml(document.getTitle()));

				}
				map.put("catalogArea",document.getCatalogArea());
				map.put("location",document.getLocation());

				map.put("groupName", document.getGroupName().equals("国内微信")?"微信":document.getGroupName());
				map.put("urlTime", document.getUrlTime());
				map.put("appraise", document.getAppraise());
				map.put("screenName", document.getScreenName());
				map.put("urlName", document.getUrlName());
				map.put("content", document.getContent());
				map.put("nreserved1", document.getNreserved1());
				// 获得时间差,三天内显示时间差,剩下消失urltime
				Map<String, String> timeDifference = DateUtil.timeDifference(document);
				boolean isNew = false;
				if (ObjectUtil.isNotEmpty(timeDifference.get("timeAgo"))) {
					isNew = true;
					map.put("timeAgo", timeDifference.get("timeAgo"));
				} else {
					map.put("timeAgo", timeDifference.get("urlTime"));
				}
				map.put("isNew", isNew);
				map.put("md5Tag", document.getMd5Tag());
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
//			String link = loginpool.getLink();
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
		QueryCommonBuilder builder = this.createQueryCommonBuilder();
		IndexTab indexTab = super.config.getIndexTab();
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		/*if (irSimflagAll){
			builder.filterField(FtsFieldConst.FIELD_IR_SIMFLAGALL,"0 OR \"\"",Operator.Equal);
		}*/
		long countCommon = hybase8SearchService.ftsCountCommon(builder, sim, irSimflag,irSimflagAll,"column");

		return countCommon;
	}

	@Override
	public Object getSectionList() throws TRSSearchException {
		//source不为空就是正常列表的 source为空就是混合列表的
		String groupName = this.config.getGroupName();
		if("国外新闻".equals(groupName)){
			groupName = "境外媒体";
		}
		User loginUser = UserUtils.getUser();
		IndexTab indexTab = super.config.getIndexTab();
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		List<String> types = Arrays.asList(indexTab.getType());
		List<String> metas = new ArrayList<>();
		for (String type : types) {
			if (type.equals(ColumnConst.LIST_WECHAT_COMMON)) {
				metas.add("微信");
				metas.add("国内微信");
			}else if (type.equals(ColumnConst.LIST_STATUS_COMMON)) {
				metas.add("微博");
			}else if (type.equals(ColumnConst.LIST_TWITTER)) {
				metas.add("Twitter");
			}else if (type.equals(ColumnConst.LIST_FaceBook)) {
				metas.add("FaceBook");
			}
		}
		String[] tradition = indexTab.getTradition();
		if (tradition != null && tradition.length > 0) {
			List<String> traditions = Arrays.asList(tradition);
			metas.addAll(traditions);
		}
		if (!metas.contains(groupName) && !"ALL".equals(groupName)) {
			return null;
		}
		if(StringUtil.isNotEmpty(groupName) && !"ALL".equals(groupName)){
			QueryBuilder queryBuilder = this.config.getQueryBuilder();
			//来源
			if(StringUtil.isNotEmpty(groupName) && !"ALL".equals(groupName)){
				if ("国内新闻".equals(groupName)) {
					String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 NOT ")
							.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
					queryBuilder.filterByTRSL(trsl);
				} else if ("国内论坛".equals(groupName)) {
					StringBuffer sb = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 OR ")
							.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧");
					queryBuilder.filterByTRSL(sb.toString());
				}else if ("境外媒体".equals(groupName)){
					groupName = "国外新闻";
					queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupName, Operator.Equal);
				}else{
					 if("微信".equals(groupName)){
						 groupName = "国内微信";
					 }
					 queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupName, Operator.Equal);
				}
			}
			try {
				if ("hot".equals(this.config.getOrderBy())){
					String[] data = TrslUtil.chooseDatabases(groupName.split(";"));
					if (ObjectUtil.isNotEmpty(data)){
						queryBuilder.setDatabase(StringUtil.join(data,";"));
					}
					return infoListService.getHotList(queryBuilder,queryBuilder,loginUser,"column");
				}
			} catch (TRSException e) {
				throw new TRSSearchException(e);
			}

			if ("微博".equals(groupName) || "FaceBook".equals(groupName) || "Twitter".equals(groupName)) {
				try {
					if ("FaceBook".equals(groupName) || "Twitter".equals(groupName)) {
						return infoListService.getTFList(queryBuilder, loginUser,sim,"column");
					}
					return infoListService.getStatusList(queryBuilder, loginUser,sim,irSimflag,irSimflagAll,false,"column");
				} catch (TRSException e) {
					throw new TRSSearchException(e);
				}
			} else if ("微信".equals(groupName) || "国内微信".equals(groupName)) {
				try {
					return infoListService.getWeChatList(queryBuilder, loginUser,sim,irSimflag,irSimflagAll,false,"column");
				} catch (TRSException e) {
					throw new TRSSearchException(e);
				}
			} else if ("FaceBook".equals(groupName) || ("Twitter".equals(groupName))) {//FaceBook 列表查询
				try {
					return infoListService.getTFList(queryBuilder, loginUser,sim,"column");
				} catch (TRSException e) {
					throw new TRSSearchException(e);
				}
			} else {// 传统
				queryBuilder.setDatabase(Const.HYBASE_NI);
				try {
					return infoListService.getDocList(queryBuilder, loginUser, sim,irSimflag,irSimflagAll,false,"column");
				} catch (TRSException e) {
					throw new TRSSearchException(e);
				}
			}
		}else{
			return mixList();
		}


//		}
	}

	@Override
	public Object getAppSectionList(User user) throws TRSSearchException {
		QueryCommonBuilder queryBuilder = this.createQueryCommonBuilder();
		IndexTab indexTab = super.config.getIndexTab();
		queryBuilder.page(super.config.getPageNo(), super.config.getPageSize());
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		try {

			return this.infoListService.getDocListContrast(queryBuilder, user, sim,irSimflag,irSimflagAll,"column");
		} catch (TRSException e) {
			throw new TRSSearchException(e);
		}
	}

	public void builderStatus(QueryBuilder querybuilder,QueryCommonBuilder commonBuilder){
		if(ObjectUtil.isNotEmpty(querybuilder)){
			QueryBuilder queryBuilder = new QueryBuilder();
			querybuilder.filterByTRSL(querybuilder.asTRSL().replace(FtsFieldConst.FIELD_TITLE, FtsFieldConst.FIELD_CONTENT));
			querybuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			if("asc".equals(this.config.getOrderBy())){
				querybuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
			}
			querybuilder.setDatabase(queryBuilder.getDatabase());
			querybuilder.page(queryBuilder.getPageNo(), queryBuilder.getPageSize());
			queryBuilder = querybuilder;
		}
		if(ObjectUtil.isNotEmpty(commonBuilder)){
			QueryCommonBuilder queryBuilder = new QueryCommonBuilder();
			queryBuilder.filterByTRSL(commonBuilder.asTRSL().replace(FtsFieldConst.FIELD_TITLE, FtsFieldConst.FIELD_CONTENT));
			queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			queryBuilder.page(commonBuilder.getPageNo(), commonBuilder.getPageSize());
			queryBuilder.setDatabase(commonBuilder.getDatabase());
			commonBuilder = queryBuilder;
	}
}

	public Object mixList(){
		QueryCommonBuilder queryBuilder = this.createQueryCommonBuilder();
		User loginUser = UserUtils.getUser();
		IndexTab indexTab = super.config.getIndexTab();
		queryBuilder.page(super.config.getPageNo(), super.config.getPageSize());
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();

		// 保证混合栏目进列表 数据顺序和栏目数据顺序一致
		String traUid = UUID.randomUUID().toString();
		RedisUtil.setString(traUid, queryBuilder.asTRSL());
		if(this.listOnlyStatus){
			QueryBuilder querybuilder = new QueryBuilder();
			querybuilder.filterByTRSL(queryBuilder.asTRSL().replace(FtsFieldConst.FIELD_TITLE, FtsFieldConst.FIELD_CONTENT));
			switch (super.config.getOrderBy()) { // 排序
			case "commtCount"://评论
				queryBuilder.orderBy(FtsFieldConst.FIELD_COMMTCOUNT, true);
				break;
			case "rttCount"://转发
				queryBuilder.orderBy(FtsFieldConst.FIELD_RTTCOUNT, true);
				break;
			case "asc":
				queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "desc":
				queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "relevance"://相关性排序
				queryBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			default:
				if (config.isWeight()) {
					queryBuilder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
				} else {
					queryBuilder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
				}
				break;
			}
			querybuilder.setDatabase(StringUtils.join(queryBuilder.getDatabase(), ";"));
			querybuilder.page(queryBuilder.getPageNo(), queryBuilder.getPageSize());
			querybuilder.setServer(indexTab.isServer());


			try {
				return infoListService.getStatusList(querybuilder, loginUser,sim,irSimflag,irSimflagAll,false,"column");
			} catch (TRSException e) {
				throw new TRSSearchException(e);
			}
		}

		try {
			if ("hot".equals(this.config.getOrderBy())){
				QueryBuilder hotBuilder = new QueryBuilder();
				hotBuilder.filterByTRSL(queryBuilder.asTRSL());
				hotBuilder.page(queryBuilder.getPageNo(),queryBuilder.getPageSize());

				String[] database = queryBuilder.getDatabase();
				if (ObjectUtil.isNotEmpty(database)){
					hotBuilder.setDatabase(StringUtil.join(database,";"));
				}else {
					hotBuilder.setDatabase(Const.HYBASE_NI_INDEX);
				}
				return infoListService.getHotList(hotBuilder,hotBuilder,loginUser,"column");
			}
			return this.infoListService.getDocListContrast(queryBuilder, loginUser, sim,irSimflag,irSimflagAll,"column");
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

		String orderBy = builder.getOrderBy();
		IndexTab indexTab = super.config.getIndexTab();
		// 选择查询库
		String[] databases = TrslUtil.chooseDatabaseByIndexType(indexTab.getType());
		builder.setDatabase(databases);

		// 选择需要查询的group
		// 统一来源
		String[] tradition = indexTab.getTradition();
		String groupNames = "";
		if (tradition != null && tradition.length > 0) {
			for (int i = 0; i < tradition.length; i++) {
				String meta = tradition[i];
				groupNames += meta + " OR ";
			}
		}
		
		if (groupNames.endsWith(" OR ")) {
			groupNames = groupNames.substring(0, groupNames.length() -4);
		}
		groupNames = groupNames.replace("境外媒体", "国外新闻").replace("微信","国内微信");

		//下面将表达式处理为 （微博 AND FIELD_RETWEETED_MID） OR (微博以外来源 AND TRSL(不包含FIELD_RETWEETED_MID字段)) 的形式
		String asTrsl = builder.asTRSL();
		if (StringUtil.isNotEmpty(asTrsl)){
			if ((asTrsl.contains(FtsFieldConst.FIELD_RETWEETED_MID) || asTrsl.contains(FtsFieldConst.FIELD_NRESERVED1)) && groupNames.length() > 1){
				String newAsTrsl = asTrsl;
				//1、
				//String exForWeibo = newAsTrsl.replace(" AND (IR_NRESERVED1:(0))","").replace(" AND (IR_NRESERVED1:(1))","");
				String exForWeibo = newAsTrsl.replace(" AND (IR_NRESERVED1:(0 OR \"\"))","").replace(" AND (IR_NRESERVED1:(1))","").replace("(IR_NRESERVED1:(0 OR \"\")) AND ","").replace("(IR_NRESERVED1:(1)) AND ","");
				String weiboTrsl = FtsFieldConst.FIELD_GROUPNAME + ":(微博) AND " + exForWeibo;

				//2、
				String exForLuntan = newAsTrsl.replace(" AND (IR_RETWEETED_MID:(0 OR \"\"))", "").replace(" NOT IR_RETWEETED_MID:(0 OR \"\")", "");
				String luntanTrsl = FtsFieldConst.FIELD_GROUPNAME + ":(国内论坛) AND " + exForLuntan;

				//3、
				String exGForOther = "";
				if (asTrsl.contains(FtsFieldConst.FIELD_RETWEETED_MID) && asTrsl.contains(FtsFieldConst.FIELD_NRESERVED1)){
					exGForOther = groupNames.replaceAll("国内论坛 OR ", "").replaceAll(" OR 国内论坛", "").replaceAll("国内论坛", "").replaceAll("微博 OR ", "").replaceAll(" OR 微博", "").replaceAll("微博", "");
				}else if (asTrsl.contains(FtsFieldConst.FIELD_RETWEETED_MID)){
					exGForOther = groupNames.replaceAll("微博 OR ", "").replaceAll(" OR 微博", "").replaceAll("微博", "");
				}else if (asTrsl.contains(FtsFieldConst.FIELD_NRESERVED1)){
					exGForOther = groupNames.replaceAll("国内论坛 OR ", "").replaceAll(" OR 国内论坛", "").replaceAll("国内论坛", "");
				}
				//String exMNForOther = newAsTrsl.replace(" AND "+"("+"IR_NRESERVED1:(0))","").replace(" AND (IR_NRESERVED1:(1))","").replace(" AND (IR_RETWEETED_MID:(0 OR \"\"))", "").replace(" NOT IR_RETWEETED_MID:(0 OR \"\")", "");
				String exMNForOther = newAsTrsl.replace(" AND "+"("+"IR_NRESERVED1:(0 OR \"\"))","").replace(" AND (IR_NRESERVED1:(1))","").replace("(IR_NRESERVED1:(0 OR \"\")) AND ","").replace("(IR_NRESERVED1:(1)) AND ","").replace(" AND (IR_RETWEETED_MID:(0 OR \"\"))", "").replace(" NOT IR_RETWEETED_MID:(0 OR \"\")", "");

				String otherTrsl = FtsFieldConst.FIELD_GROUPNAME + ":("+exGForOther+") AND " + exMNForOther;

				builder = new QueryCommonBuilder();
				builder.setDatabase(databases);
				builder.setOrderBy(orderBy);
				if (asTrsl.contains(FtsFieldConst.FIELD_RETWEETED_MID) && asTrsl.contains(FtsFieldConst.FIELD_NRESERVED1)){
					String zongTrsl = "("+weiboTrsl + ") OR (" + luntanTrsl + ")";
					if (StringUtil.isNotEmpty(exGForOther)){
						zongTrsl += " OR (" + otherTrsl+")";
					}
					builder.filterByTRSL(zongTrsl);
				}else if (asTrsl.contains(FtsFieldConst.FIELD_RETWEETED_MID)){
					String twoTrsl = "("+weiboTrsl + ")";
					if (StringUtil.isNotEmpty(exGForOther)){
						twoTrsl += " OR (" + otherTrsl+")";
					}
					builder.filterByTRSL(twoTrsl);
				}else if (asTrsl.contains(FtsFieldConst.FIELD_NRESERVED1)){
					String twoTrsl1 = "("+luntanTrsl + ")";
					if (StringUtil.isNotEmpty(exGForOther)){
						twoTrsl1 += " OR (" + otherTrsl+")";
					}
					builder.filterByTRSL(twoTrsl1);
				}
			}else {
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupNames, Operator.Equal);
			}
		}else {
			builder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupNames, Operator.Equal);
		}

		int pageNo = super.config.getPageNo();
		int pageSize = super.config.getPageSize();
		builder.setPageNo(pageNo);
		if( pageSize != 0){
			builder.setPageSize(pageSize);
		}
		return builder;
	}
}
