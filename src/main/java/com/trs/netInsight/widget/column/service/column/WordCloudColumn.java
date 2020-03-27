package com.trs.netInsight.widget.column.service.column;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.DateUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.widget.user.entity.User;
import org.apache.commons.lang3.StringUtils;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.model.result.GroupWordInfo;
import com.trs.netInsight.support.fts.model.result.GroupWordResult;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.factory.AbstractColumn;

/**
 * 词云列表
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年4月8日
 */
public class WordCloudColumn extends AbstractColumn {

	@Override
	public Object getColumnData(String timeRaneg) throws TRSSearchException {

		IndexTab indexTab = super.config.getIndexTab();
		//url排重
      	boolean irSimflag = indexTab.isIrSimflag();
		boolean sim = indexTab.isSimilar();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		Object wordCloud = null;
		try {
			String[] timeArray = DateUtil.formatTimeRange(timeRaneg);
			String time0 = timeArray[0];
			if (!DateUtil.isExpire("2019-10-01 00:00:00",time0)){
				QueryCommonBuilder queryBuilder = this.createQueryCommonBuilder();
				queryBuilder.page(0,50);
				String metas = indexTab.getGroupName();
				String[] data = null;
				String[] split = metas.split(";");
				data = TrslUtil.chooseDatabases(split);

				try {
					wordCloud = chartAnalyzeService.getWordCloud(indexTab.isServer(),queryBuilder.asTRSL(), sim,irSimflag,irSimflagAll, config.getEntityType(), queryBuilder.getPageSize(),"column", data);
				} catch (TRSSearchException e) {
					throw new TRSSearchException(e);
				}
			}else {
				QueryBuilder queryBuilder = this.createQueryBuilder();
				try {
					wordCloud = chartAnalyzeService.getWordCloud(indexTab.isServer(),queryBuilder.asTRSL(), sim,irSimflag,irSimflagAll, config.getEntityType(), queryBuilder.getPageSize(),"column", queryBuilder.getDatabase());
				} catch (TRSSearchException e) {
					throw new TRSSearchException(e);
				}
			}
		} catch (OperationException e) {
			e.printStackTrace();
		}

		return wordCloud;
	}

	@Override
	public Object getColumnCount() throws TRSSearchException {
		return null;
	}

	@Override
	public Object getSectionList() throws TRSSearchException {
		IndexTab indexTab = super.config.getIndexTab();
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		QueryBuilder queryBuilder = this.config.getQueryBuilder();
		queryBuilder.setPageNo(config.getPageNo());
		queryBuilder.setPageSize(config.getPageSize());
		String irKeyword = this.config.getIrKeyword();
		if (StringUtil.isNotEmpty(irKeyword)){
			String entityType = this.config.getEntityType();
			if ("location".equals(entityType) && !"省".equals(irKeyword.substring(irKeyword.length() - 1)) && !irKeyword.contains("自治区")){
				String irKeywordNew = "";
				 if ("市".equals(irKeyword.substring(irKeyword.length() - 1))){
					irKeywordNew = irKeyword.replace("市", "");
				}else {
					irKeywordNew = irKeyword;
				}
				if (!irKeywordNew.contains("\"")) {
					irKeywordNew = "\"" + irKeywordNew + "\"";
				}
				String trsl = FtsFieldConst.FIELD_URLTITLE+":("+irKeywordNew +") OR "+ FtsFieldConst.FIELD_CONTENT +":(" +irKeywordNew + ")";
				queryBuilder.filterByTRSL(trsl);
			}else {
				queryBuilder.filterField(Const.PARAM_MAPPING.get(entityType), irKeyword, Operator.Equal);
			}
		}
		String emotion = super.config.getEmotion();
		if(StringUtil.isNotEmpty(emotion) && !"ALL".equals(emotion)){
			queryBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
		}
		String area = super.config.getArea();
		// 取地域名
		if (!"ALL".equals(area)) { // 地域

			String[] areaSplit = area.split(";");
			String contentArea = "";
			for (int i = 0; i < areaSplit.length; i++) {
				areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
				if (i != areaSplit.length - 1) {
					areaSplit[i] += " OR ";
				}
				contentArea += areaSplit[i];
			}
			queryBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
		}
		//分组
		if (!StringUtils.equals("ALL", super.config.getGroupName())
				&& StringUtils.isNotBlank(super.config.getGroupName())) {
			String groupName=super.config.getGroupName();
			if("微信".equals(groupName)){
				groupName="国内微信";
			}
			queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupName, Operator.Equal);
		}
		
		// 取userId
		User loginUser = UserUtils.getUser();

		String groupName = this.config.getGroupName();
		
		if("国外新闻".equals(groupName)){
			groupName = "境外媒体";
		}
		if(StringUtil.isNotEmpty(groupName) && !"ALL".equals(groupName)){
			//来源
			if(StringUtil.isNotEmpty(groupName) && !"ALL".equals(groupName)){
				if ("国内新闻".equals(groupName)) {
					String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 NOT ")
							.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
					queryBuilder.filterByTRSL(trsl);
				} else if ("国内论坛".equals(groupName)) {
					StringBuffer sb2 = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 OR ")
							.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧");
					queryBuilder.filterByTRSL(sb2.toString());
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
			if ("微博".equals(groupName)) {
				try {
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
			try {
				QueryCommonBuilder builder = this.createQueryCommonBuilder();
				builder.setPageNo(queryBuilder.getPageNo());
				builder.setPageSize(queryBuilder.getPageSize());

				if ("hot".equals(this.config.getOrderBy())){
					QueryBuilder hotBuilder = new QueryBuilder();
					hotBuilder.filterByTRSL(builder.asTRSL());
					hotBuilder.page(builder.getPageNo(),queryBuilder.getPageSize());

					String[] database = builder.getDatabase();
					if (ObjectUtil.isNotEmpty(database)){
						hotBuilder.setDatabase(StringUtil.join(database,";"));
					}else {
						hotBuilder.setDatabase(Const.HYBASE_NI_INDEX);
					}
					return infoListService.getHotList(hotBuilder,hotBuilder,loginUser,"column");
				}
				return this.infoListService.getDocListContrast(builder, loginUser, sim,irSimflag,irSimflagAll,"column");
			} catch (TRSException e) {
				throw new TRSSearchException(e);
			}
		}
	}

	@Override
	public Object getAppSectionList(User user) throws TRSSearchException {
		return null;
	}

	@Override
	public QueryBuilder createQueryBuilder() {
		QueryBuilder builder = super.config.getQueryBuilder();
		builder.setDatabase(Const.HYBASE_NI_INDEX);
		builder.setPageSize(50);
		return builder;
		
	}

	@Override
	public QueryCommonBuilder createQueryCommonBuilder() {
		QueryCommonBuilder builder = super.config.getCommonBuilder();
		String orderBy = builder.getOrderBy();

		IndexTab indexTab = super.config.getIndexTab();
		// 选择需要查询的group
		// 统一来源
		String groupName = indexTab.getGroupName();
		String groupNames = "";

		if (StringUtil.isNotEmpty(groupName)){
			String[] tradition = groupName.split(";");

			if (tradition != null && tradition.length > 0) {
				// 选择查询库
				String timeRange = this.config.getIndexTab().getTimeRange();
				String time0 = null;
				try {
					String[] timeArray = DateUtil.formatTimeRange(timeRange);
					time0 = timeArray[0];
					if (!DateUtil.isExpire("2019-10-01 00:00:00",time0)){
						String[] databases = TrslUtil.chooseDatabases(tradition);
						builder.setDatabase(databases);
					}else {
						builder.setDatabase(new String[]{Const.HYBASE_NI_INDEX});
					}
				} catch (OperationException e) {
					e.printStackTrace();
				}

				for (int i = 0; i < tradition.length; i++) {
					String meta = tradition[i];
					groupNames += meta + " OR ";
				}
			}
			if (groupNames.endsWith(" OR ")) {
				groupNames = groupNames.substring(0, groupNames.length() -4);
			}
			groupNames = groupNames.replace("境外媒体", "国外新闻").replace("微信","国内微信");
		}

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
				String exMNForOther = newAsTrsl.replace(" AND "+"("+"IR_NRESERVED1:(0 OR \"\"))","").replace(" AND (IR_NRESERVED1:(1))","").replace("(IR_NRESERVED1:(0 OR \"\")) AND ","").replace("(IR_NRESERVED1:(1)) AND ","").replace(" AND (IR_RETWEETED_MID:(0 OR \"\"))", "").replace(" NOT IR_RETWEETED_MID:(0 OR \"\")", "");

				String otherTrsl = FtsFieldConst.FIELD_GROUPNAME + ":("+exGForOther+") AND " + exMNForOther;
				String[] databases = builder.getDatabase();
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
		String irKeyword = this.config.getIrKeyword();
		if (StringUtil.isNotEmpty(irKeyword)){
			String entityType = this.config.getEntityType();
			if ("location".equals(entityType) && !"省".equals(irKeyword.substring(irKeyword.length() - 1)) && !irKeyword.contains("自治区")){
				String irKeywordNew = "";
				 if ("市".equals(irKeyword.substring(irKeyword.length() - 1))){
					irKeywordNew = irKeyword.replace("市", "");
				}else {
					irKeywordNew = irKeyword;
				}
				if (!irKeywordNew.contains("\"")) {
					irKeywordNew = "\"" + irKeywordNew + "\"";
				}
				String trsl = FtsFieldConst.FIELD_URLTITLE+":("+irKeywordNew +") OR "+ FtsFieldConst.FIELD_CONTENT +":(" +irKeywordNew + ")";
				builder.filterByTRSL(trsl);
				//builder.filterField(FtsFieldConst.FIELD_CONTENT, irKeywordNew, Operator.Equal);
				//builder.filterField(FtsFieldConst.FIELD_URLTITLE, irKeywordNew, Operator.Equal);
			}else {
				builder.filterField(Const.PARAM_MAPPING.get(entityType), irKeyword, Operator.Equal);
			}
		}
		return builder;
	}

}
