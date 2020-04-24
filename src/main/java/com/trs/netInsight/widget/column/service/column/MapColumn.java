package com.trs.netInsight.widget.column.service.column;

import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.user.entity.User;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地域热力图
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年4月8日
 */
public class MapColumn extends AbstractColumn {

	@Override
	public Object getColumnData(String timeRange) throws TRSSearchException {
		//QueryBuilder builder = this.createQueryBuilder();
		QueryCommonBuilder builder = this.createQueryCommonBuilder();
		IndexTab indexTab = super.config.getIndexTab();
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		String groupNames = super.config.getIndexTab().getGroupName();//本身存储的来源;
		if (ColumnConst.CONTRAST_TYPE_SITE.equals(indexTab.getContrast())) {
			//站点对比
			String[] tradition = indexTab.getTradition();
			groupNames = StringUtil.join(tradition, ";");
		}
		GroupResult categoryInfos = null;
		List<Map<String, Object>> list = new ArrayList<>();
		builder.setPageSize(Integer.MAX_VALUE);
		try {
			list = (List<Map<String, Object>>) commonChartService.getMapColumnData(builder, sim, irSimflag, irSimflagAll, groupNames, FtsFieldConst.FIELD_CATALOG_AREA, "column");
		} catch (TRSException | TRSSearchException e) {
			throw new TRSSearchException(e);
		}

		/*
		try {
			categoryInfos = hybase8SearchService.categoryQuery(builder.isServer(), builder.asTRSL(), sim, irSimflag,irSimflagAll,
					FtsFieldConst.FIELD_CATALOG_AREA, Integer.MAX_VALUE, "column",builder.getDatabase());
		} catch (TRSSearchException e) {
			throw new TRSSearchException(e);
		}
		// 获取区域map
		Map<String, List<String>> areaMap = districtInfoService.allAreas();
		for (Map.Entry<String, List<String>> entry : areaMap.entrySet()) {
			Map<String, Object> reMap = new HashMap<String, Object>();
			int num = 0;
			// 查询结果之间相互对比 所以把城市放开也不耽误查询速度
				for (GroupInfo classEntry : groupResult) {
					String area = classEntry.getFieldValue();
					if(area.contains(";")){
						continue;
					}
					//因为该查询字段形式类似数组，文章命中访问的是这个字段中的每个值的个数，例如一条数据的这个字段的值为：中国\北京市\朝阳区;中国\北京市\海淀区
					//按注释方法算 - 这样同一条数据北京市被计算2次，因为朝阳与海淀都是北京下属地域，2019-12该字段修改为在上面基础上增加当前条所属市，为：中国\北京市\朝阳区;中国\北京市\海淀区;中国\北京市
					//如果继续计算下属市则北京被计算3次，所以只计算到省，则需要数据库中改字段的值定义不变，为：中国\北京市
					String[] areaArr = area.split("\\\\");
					if (areaArr.length == 2) {
						if (areaArr[1].contains(entry.getKey())) {
							num += classEntry.getCount();
						}
					}
				}

			reMap.put("areaName", entry.getKey());
			reMap.put("areaCount", num);
			//reMap.put("citys", citys);
			list.add(reMap);
		}*/
		return list;
	}

	@Override
	public Object getColumnCount() throws TRSSearchException {
		return null;
	}

	@Override
	public Object getSectionList() throws TRSSearchException {
		QueryBuilder queryBuilder = this.createQueryBuilder();
		QueryCommonBuilder commonBuilder = this.createQueryCommonBuilder();
		String emotion = this.config.getEmotion();
		IndexTab indexTab = super.config.getIndexTab();
		boolean server = indexTab.isServer();
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		if (StringUtil.isNotEmpty(emotion) && !emotion.equals("ALL")) {
			queryBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			commonBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
		}
		String area = this.config.getArea();
		// 取地域名
		if (!"ALL".equals(area)) { // 地域

			String[] areaSplit = area.split(";");
			String contentArea = "";
			for (int i = 0; i < areaSplit.length; i++) {
				/*
				2019-12-30改
				//原有规则是查询这个字段包含这个省，但是统计方法改变，只统计到省，存在历史数据问题（没有单独追加省，造成很多数据没有到省数据），所以查询时不能模糊查询
				if (server) {
					areaSplit[i] = "中国\\\\" + areaSplit[i] + "%";
				} else {
					areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
				}*/
				areaSplit[i] = Const.PROVINCE_FULL_NAME.get(areaSplit[i]);
				areaSplit[i] = "中国\\\\" + areaSplit[i];
				if (i != areaSplit.length - 1) {
					areaSplit[i] += " OR ";
				}
				contentArea += areaSplit[i];
			}
			queryBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":(" + contentArea +")");
			commonBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":(" + contentArea +")");
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
					return infoListService.getHotList(queryBuilder,queryBuilder,loginUser,"column");
				}
			} catch (TRSException e) {
				throw new TRSSearchException(e);
			}
			//地图加 来源 2019-12-05
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
				queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
				try {
					return infoListService.getDocList(queryBuilder, loginUser, sim,irSimflag,irSimflagAll,false,"column");
				} catch (TRSException e) {
					throw new TRSSearchException(e);
				}
			}
		}else{
			try {

				try {
					if ("hot".equals(this.config.getOrderBy())){
						QueryBuilder hotBuilder = new QueryBuilder();
						hotBuilder.filterByTRSL(commonBuilder.asTRSL());
						hotBuilder.page(commonBuilder.getPageNo(),queryBuilder.getPageSize());

						String[] database = commonBuilder.getDatabase();
						if (ObjectUtil.isNotEmpty(database)){
							hotBuilder.setDatabase(StringUtil.join(database,";"));
						}else {
							hotBuilder.setDatabase(Const.HYBASE_NI_INDEX);
						}
						return infoListService.getHotList(hotBuilder,hotBuilder,loginUser,"column");
					}
				} catch (TRSException e) {
					throw new TRSSearchException(e);
				}
				return infoListService.getDocListContrast(commonBuilder, loginUser, sim,irSimflag,irSimflagAll,"column");
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
		// 分组
		if (!StringUtils.equals("ALL", super.config.getGroupName())
				&& StringUtils.isNotBlank(super.config.getGroupName())) {
			String groupName = super.config.getGroupName();
			if ("微信".equals(groupName)) {
				groupName = "国内微信";
			}
			builder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupName, Operator.Equal);
		}
		return builder;

	}

	@Override
	public QueryCommonBuilder createQueryCommonBuilder() {
		QueryCommonBuilder builder = super.config.getCommonBuilder();
		String orderBy = builder.getOrderBy();
		IndexTab indexTab = super.config.getIndexTab();

		// 选择需要查询的group
		// 统一来源
		String groupNames = super.config.getIndexTab().getGroupName();//本身存储的来源;
		if (ColumnConst.CONTRAST_TYPE_SITE.equals(indexTab.getContrast())) {
			//站点对比
			String[] tradition = indexTab.getTradition();
			groupNames = StringUtil.join(tradition,";");
		}
		// 选择查询库
		String[] databases = TrslUtil.chooseDatabases(groupNames.split(";"));
		builder.setDatabase(databases);
		groupNames = groupNames.replaceAll(";"," OR ");
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
					String zongTrsl = "("+weiboTrsl + ") OR (" + luntanTrsl + ") OR (" + otherTrsl+")";
					builder.filterByTRSL(zongTrsl);
				}else if (asTrsl.contains(FtsFieldConst.FIELD_RETWEETED_MID)){
					String twoTrsl = "("+weiboTrsl + ") OR (" + otherTrsl+")";
					builder.filterByTRSL(twoTrsl);
				}else if (asTrsl.contains(FtsFieldConst.FIELD_NRESERVED1)){
					String twoTrsl1 = "("+luntanTrsl + ") OR (" + otherTrsl+")";
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

	private GroupResult splitArea(GroupResult groupInfos){
		GroupResult groupInfos1 = new GroupResult();
		for (GroupInfo classEntry : groupInfos) {
			String areas = classEntry.getFieldValue();
			Long count = classEntry.getCount();
			if(areas.contains(";")){

				String[] areaArr = areas.split(";");
				for(String area : areaArr){
					groupInfos1.addGroup(area,count);
				}
			}else{
				groupInfos1.addGroup(areas,count);
			}
		}
		return groupInfos;
	}

}
