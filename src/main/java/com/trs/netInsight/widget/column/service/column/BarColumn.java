package com.trs.netInsight.widget.column.service.column;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.user.entity.User;
import org.apache.commons.lang3.StringUtils;

import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.MapUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.analysis.entity.CategoryBean;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.special.entity.InfoListResult;

/**
 * 柱状图
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年4月8日
 */
public class BarColumn extends AbstractColumn {

	@Override
	public Object getColumnData(String timeRange) throws TRSSearchException {

		List<Map<String, Object>> list = new ArrayList<>();
		IndexTab indexTab = super.config.getIndexTab();
		try {
			QueryCommonBuilder builder = super.config.getCommonBuilder();
			boolean sim = indexTab.isSimilar();
			// url排重
			boolean irSimflag = indexTab.isIrSimflag();
			boolean irSimflagAll = indexTab.isIrSimflagAll();
			String groupName = indexTab.getGroupName();
			builder.setPageSize(this.config.getPageSize());
			if (StringUtils.isNotBlank(indexTab.getContrast())) {// 对比类别不为空,断言简单模式
				String contrastField = FtsFieldConst.FIELD_GROUPNAME;
				// 来源分类对比图
				if (indexTab.getContrast().equals(ColumnConst.CONTRAST_TYPE_GROUP)) {
					builder.setPageSize(Integer.MAX_VALUE);
				}

				// 站点对比图
				if (indexTab.getContrast().equals(ColumnConst.CONTRAST_TYPE_SITE)) {
					contrastField = FtsFieldConst.FIELD_SITENAME;
					groupName = StringUtil.join(indexTab.getTradition(),";");
				}

				//微信公众号对比
				if (indexTab.getContrast().equals(ColumnConst.CONTRAST_TYPE_WECHAT)){
					contrastField = FtsFieldConst.FIELD_SITENAME;
					groupName = Const.GROUPNAME_WEIXIN;
				}
				list = (List<Map<String, Object>>)commonChartService.getBarColumnData(builder,sim,irSimflag,irSimflagAll,groupName, null,contrastField,"column");
			} else {// 专家模式
				//list = getDataBarCommon();
				list = (List<Map<String, Object>>)commonChartService.getBarColumnData(builder,sim,irSimflag,irSimflagAll,groupName, indexTab.getXyTrsl(),null,"column");
			}
		} catch (TRSException | TRSSearchException e) {
			throw new TRSSearchException(e);
		}
		return list;
	}

	@Override
	public Object getColumnCount() throws TRSSearchException {
		return null;
	}

	@Override
	public Object getSectionList() throws TRSSearchException {
		User loginUser = UserUtils.getUser();
		IndexTab indexTab = super.config.getIndexTab();
		QueryBuilder builder = this.createQueryBuilder();
		String groupName2 = super.config.getGroupName();

		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		// 来源对比图
		if (Const.MEDIA_TYPE_WEIBO.contains(config.getGroupName())) {
			builder.setDatabase(Const.WEIBO);
		} else if (Const.MEDIA_TYPE_WEIXIN.contains(config.getGroupName())) {
			if (groupName2.contains("微信") && !groupName2.contains("国内微信")){
				groupName2 = super.config.getGroupName().replaceAll("微信", "国内微信");
			}
			builder.setDatabase(Const.WECHAT);
		} else if (Const.MEDIA_TYPE_TF.contains(config.getGroupName())) {
			builder.setDatabase(Const.HYBASE_OVERSEAS);
		}
		// 来源对比
		if (ColumnConst.CONTRAST_TYPE_GROUP.equals(indexTab.getContrast())) {
			QueryCommonBuilder sourceCommonBuilder = new QueryCommonBuilder();
			if ("ALL".equals(groupName2)){
				sourceCommonBuilder = this.createQueryCommonBuilder();
			}
			if (StringUtils.isNotBlank(groupName2) ) {
				groupName2 = groupName2.replaceAll("境外媒体", "国外新闻");
				if (groupName2.endsWith(";|；")){
					groupName2 = groupName2.substring(0,groupName2.length()-1);
				}
				String[] split = groupName2.split(";");
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, split, Operator.Equal);
			}
			String[] data = TrslUtil.chooseDatabases(groupName2.split(";"));
			try {
				if ("ALL".equals(this.config.getGroupName())){
					if ("hot".equals(this.config.getOrderBy())){
						QueryBuilder hotBuilder = new QueryBuilder();
						hotBuilder.filterByTRSL(sourceCommonBuilder.asTRSL());
						hotBuilder.page(sourceCommonBuilder.getPageNo(),sourceCommonBuilder.getPageSize());

						String[] database = sourceCommonBuilder.getDatabase();
						if (ObjectUtil.isNotEmpty(database)){
							hotBuilder.setDatabase(StringUtil.join(database,";"));
						}else {
							hotBuilder.setDatabase(Const.HYBASE_NI_INDEX);
						}
						return infoListService.getHotList(builder,builder,loginUser,"column");
					}
					return this.infoListService.getDocListContrast(sourceCommonBuilder,
							loginUser, sim, irSimflag,irSimflagAll,"column");
				}else {
					if ("hot".equals(this.config.getOrderBy())){
						builder.setDatabase(StringUtil.join(data,";"));
						return infoListService.getHotList(builder,builder,loginUser,"column");
					}
					return infoListService.getDocListContrast(builder, loginUser, sim, irSimflag,irSimflagAll,"column");
				}
			} catch (TRSException e) {
				throw new TRSSearchException(e);
			}
		}

		// 站点对比图
		if (ColumnConst.CONTRAST_TYPE_SITE.equals(indexTab.getContrast())) {
			if ("ALL".equals(groupName2)) {
				//站点对比选择tradition来源
				String[] tradition = indexTab.getTradition();
				if (ObjectUtil.isNotEmpty(tradition)){
					groupName2 = StringUtil.join(tradition,";");
				}
			}
			if (StringUtil.isNotEmpty(groupName2)){
				groupName2 = groupName2.replaceAll("境外媒体", "国外新闻");
				String[] split = groupName2.split(";");
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, split, Operator.Equal);
			}

			builder.filterField(FtsFieldConst.FIELD_SITENAME, super.config.getKey(), Operator.Equal);
			try {
				//站点分类列表页无条件筛选项 20191031
//				if ("hot".equals(this.config.getOrderBy())){
//					return infoListService.getHotList(builder,builder,loginUser,"column");
//				}
				return infoListService.getDocListContrast(builder, loginUser, sim, irSimflag,irSimflagAll,"column");
			} catch (TRSException e) {
				throw new TRSSearchException(e);
			}
		}
		//微信公众号
		if (ColumnConst.CONTRAST_TYPE_WECHAT.equals(indexTab.getContrast())) {
			builder.filterField(FtsFieldConst.FIELD_SITENAME, super.config.getKey(), Operator.Equal);
			builder.setDatabase(Const.WECHAT);
			try {
				if ("hot".equals(this.config.getOrderBy())){
					return infoListService.getHotList(builder,builder,loginUser,"column");
				}
				return infoListService.getDocListContrast(builder, loginUser, sim, irSimflag,irSimflagAll,"column");
			} catch (TRSException e) {
				throw new TRSSearchException(e);
			}
		}
		// 专家模式-跳转混合列表
		if (StringUtil.isNotEmpty(indexTab.getXyTrsl())) {
			String groupName = "";
			QueryCommonBuilder sourceCommonBuilder = new QueryCommonBuilder();
			if (null != super.config.getGroupName() && !"ALL".equals(super.config.getGroupName())) {
				String groupName3 = indexTab.getGroupName();
				groupName3 = groupName3.replaceAll("境外媒体", "国外新闻");
				groupName3 = groupName3.replaceAll("微信", "国内微信");
				String[] split = groupName3.split(";");
				List<String> asList = Arrays.asList(split);
				
				// 前端传的是微信的话super.config.getGroupName()还是“微信”，柱状图跳列表时，if (!asList.contains(groupName))为true
				groupName = super.config.getGroupName().replaceAll("微信", "国内微信").replaceAll("境外媒体", "国外新闻");
				String[] databases = TrslUtil.chooseDatabases(groupName.split(";"));
				builder.setDatabase(StringUtils.join(databases,";"));
				if (!asList.contains(groupName)) {
					return null;
				}
			} else {
				sourceCommonBuilder = this.createQueryCommonBuilder();
				groupName = super.config.getIndexTab().getGroupName();//本身存储的来源;
				if (ColumnConst.CONTRAST_TYPE_SITE.equals(indexTab.getContrast())) {
					//站点对比
					String[] tradition = indexTab.getTradition();
					groupName = StringUtil.join(tradition,";");
				}
				if(StringUtil.isEmpty(groupName)){
					groupName = "国内新闻;微信;微博;国内新闻_手机客户端;国内论坛;国内博客;国内新闻_电子报;国外新闻;Twitter;FaceBook";
				}
				// 选择查询库
				String[] databases = TrslUtil.chooseDatabases(groupName.split(";"));
				builder.setDatabase(StringUtils.join(databases,";"));
				groupName = groupName.replaceAll(";"," OR ");
				if (groupName.endsWith(" OR ")) {
					groupName = groupName.substring(0, groupName.length() -4);
				}
				groupName = groupName.replace("境外媒体", "国外新闻").replace("微信","国内微信");
			}
			groupName = groupName.trim();
			if (groupName.endsWith(";")) {
				groupName = groupName.substring(0, groupName.length() - 2);
			}
			groupName = "(" + groupName.replaceAll(";", " OR ") + ")";
			builder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupName, Operator.Equal);

			List<CategoryBean> mediaType = chartAnalyzeService.getMediaType(indexTab.getXyTrsl());
			for (CategoryBean categoryBean : mediaType) {

				if (config.getKey().equals(categoryBean.getKey())) {
					if(StringUtil.isNotEmpty(categoryBean.getValue())){
						String value = categoryBean.getValue().toLowerCase().trim();
						if(value.startsWith("not")){
							sourceCommonBuilder.filterByTRSL_NOT(categoryBean.getValue().substring(3,categoryBean.getValue().length()));
							builder.filterByTRSL_NOT(categoryBean.getValue().substring(3,categoryBean.getValue().length()));
						}else {
							sourceCommonBuilder.filterByTRSL(categoryBean.getValue());
							builder.filterByTRSL(categoryBean.getValue());
						}
					}
					try {
						//柱状图专家模式跳列表 无条件筛选项 20191031
						if ("hot".equals(this.config.getOrderBy())){
							return infoListService.getHotList(builder,builder,loginUser,"column");
						}

						if ("asc".equals(config.getOrderBy())) {
							sourceCommonBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
						} else {
							sourceCommonBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
						}
						if ("ALL".equals(config.getGroupName())) {
							InfoListResult<IDocument> result = this.infoListService.getDocListContrast(sourceCommonBuilder,
									loginUser, sim, irSimflag,irSimflagAll,"column");
							return result;
						} else {
							return infoListService.getDocListContrast(builder, loginUser, sim, irSimflag,irSimflagAll,"column");
						}
					} catch (TRSException e) {
						throw new TRSSearchException(e);
					}
				}
			}
		}

		throw new TRSSearchException("未获取到检索条件");
	}

	@Override
	public Object getAppSectionList(User user) throws TRSSearchException {
		return null;
	}

	@Override
	public QueryBuilder createQueryBuilder() {
		QueryBuilder builder = super.config.getQueryBuilder();
		builder.setDatabase(Const.HYBASE_NI_INDEX);
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

	/**
	 * 以媒体类型为x轴,检索柱状图栏目数据
	 *
	 * @return
	 * @throws OperationException
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> getDataBarCommon() throws OperationException, TRSSearchException {
		List<Map<String, Object>> list = new ArrayList<>();
		QueryCommonBuilder builder = super.config.getCommonBuilder();
		IndexTab indexTab = super.config.getIndexTab();
		boolean sim = indexTab.isSimilar();
		// url排重
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();

		String metas = indexTab.getGroupName();
		String[] data = null;
		List<String> chooseMetas = null;
		if (StringUtil.isNotEmpty(metas)) {

			String[] split = metas.split(";");
			data = TrslUtil.chooseDatabases(split);

			metas = metas.replaceAll("境外媒体", "国外新闻");
			metas = metas.replaceAll("微信", "国内微信");
			String[] dataSimple = metas.split(";");
			chooseMetas = Arrays.asList(dataSimple);

			// 统一来源
			metas = metas.trim();
			if (metas.endsWith(";")) {
				metas = metas.substring(0, metas.length() - 2);
			}
			metas = "(" + metas.replaceAll(";", " OR ") + ")";
			builder.filterField(FtsFieldConst.FIELD_GROUPNAME, metas, Operator.Equal);

			// 增加排除网站
			String trslAll = builder.asTRSL();

			// 辨别普通模式与专家模式
			if (StringUtil.isNotEmpty(indexTab.getXyTrsl())) {
				List<CategoryBean> mediaType = chartAnalyzeService.getMediaType(indexTab.getXyTrsl());
				for (CategoryBean categoryBean : mediaType) {

					builder = new QueryCommonBuilder();
					builder.filterByTRSL(trslAll);
					if(StringUtil.isNotEmpty(categoryBean.getValue())){
						String value = categoryBean.getValue().toLowerCase().trim();
						if(value.startsWith("not")){
							builder.filterByTRSL_NOT(categoryBean.getValue().substring(3,categoryBean.getValue().length()));
						}else {
							builder.filterByTRSL(categoryBean.getValue());
						}
					}
					builder.setDatabase(data);
					builder.setServer(indexTab.isServer());
					long ftsCount = hybase8SearchService.ftsCountCommon(builder, sim, irSimflag,irSimflagAll,"column");
					if (ftsCount > 0L){
						Map<String, Object> putValue = MapUtil.putValue(new String[] { "groupName", "group", "num" },
								categoryBean.getKey(), categoryBean.getKey(), String.valueOf(ftsCount));
						list.add(putValue);
					}
				}
			} else {

				// 简单模式直接进行联合分类统计即可
				GroupResult groupResult = hybase8SearchService.categoryQuery(indexTab.isServer(), trslAll, sim,
						irSimflag,irSimflagAll, FtsFieldConst.FIELD_GROUPNAME, Integer.MAX_VALUE,"column", data);
				if (groupResult != null && groupResult.getGroupList().size() > 0) {
					List<GroupInfo> groupList = groupResult.getGroupList();
					for (String chooseMeta : chooseMetas) {

						Map<String, Object> putValue = new HashMap<>();
						for (GroupInfo groupInfo : groupList) {
							if (chooseMeta.equals(groupInfo.getFieldValue())) {
								putValue.put("num", groupInfo.getCount());
								break;
							} else {
								putValue.put("num", 0);
							}
						}
						if (chooseMeta.equals("国内微信")) {
							chooseMeta = "微信";
						} else if (chooseMeta.equals("国外新闻")) {
							//chooseMeta = "境外媒体";
							//页面展示 国外新闻 变为 境外网站  2019-12-10
							chooseMeta = "境外网站";
						}
						putValue.put("groupName", chooseMeta);
						putValue.put("group", chooseMeta);
						list.add(putValue);
					}
				}

			}
			return list;
		}

		return list;
	}

	/**
	 * 站点对比图
	 *
	 * @return
	 * @throws OperationException
	 */
	private List<Map<String, Object>> getContrastChartBySiteData() throws OperationException, TRSSearchException {
		List<Map<String, Object>> list = new ArrayList<>();
		QueryBuilder queryBuilder = this.createQueryBuilder();
		IndexTab indexTab = super.config.getIndexTab();
		boolean sim = indexTab.isSimilar();
		// urlname排重
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		String[] metas = indexTab.getTradition();
		if (metas != null && metas.length > 0) {
			String groupNames = "(";
			// 筛选tradition
			for (int i = 0; i < metas.length; i++) {
				if ("境外媒体".equals(metas[i])) {
					metas[i] = "国外新闻";
				}
				if (i == metas.length - 1) {
					groupNames += metas[i] + ")";
				} else {
					groupNames += metas[i] + " OR ";
				}
			}
			int pageSize = this.config.getPageSize();
			queryBuilder.filterByTRSL(FtsFieldConst.FIELD_GROUPNAME + ":" + groupNames);
			/*if (irSimflagAll){
				queryBuilder.filterField(FtsFieldConst.FIELD_IR_SIMFLAGALL,"0 OR \"\"",Operator.Equal);
			}*/
			GroupResult groupResult = hybase8SearchService.categoryQuery(indexTab.isServer(), queryBuilder.asTRSL(),
					sim, irSimflag,irSimflagAll, FtsFieldConst.FIELD_SITENAME, pageSize,"column", queryBuilder.getDatabase());
			if (groupResult != null && groupResult.getGroupList().size() > 0) {
				List<GroupInfo> groupList = groupResult.getGroupList();
				for (GroupInfo groupInfo : groupList) {
					Map<String, Object> putValue = MapUtil.putValue(new String[] { "groupName", "group", "num" },
							groupInfo.getFieldValue(), groupInfo.getFieldValue(), String.valueOf(groupInfo.getCount()));
					list.add(putValue);
				}
			}
		}
		return list;
	}

	/**
	 * 微信公众号对比
	 * @return
	 * @throws OperationException
	 */
	private List<Map<String,Object>> getContrastChartByWeChat() throws OperationException, TRSSearchException{
		List<Map<String, Object>> list = new ArrayList<>();
		QueryBuilder queryBuilder = super.config.getQueryBuilder();
		queryBuilder.setDatabase(Const.WECHAT);
		IndexTab indexTab = super.config.getIndexTab();
		int pageSize = this.config.getPageSize();
		boolean sim = indexTab.isSimilar();
		// urlname排重
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();

		/*if (irSimflagAll){
			queryBuilder.filterField(FtsFieldConst.FIELD_IR_SIMFLAGALL,"0 OR \"\"",Operator.Equal);
		}*/
		GroupResult groupResult = hybase8SearchService.categoryQuery(indexTab.isServer(), queryBuilder.asTRSL(),
				sim, irSimflag,irSimflagAll, FtsFieldConst.FIELD_SITENAME, pageSize,"column", queryBuilder.getDatabase());
		if (groupResult != null && groupResult.getGroupList().size() > 0) {
			List<GroupInfo> groupList = groupResult.getGroupList();
			for (GroupInfo groupInfo : groupList) {
				Map<String, Object> putValue = MapUtil.putValue(new String[] { "groupName", "group", "num" },
						groupInfo.getFieldValue(), groupInfo.getFieldValue(), String.valueOf(groupInfo.getCount()));
				list.add(putValue);
			}
		}

		return list;
	}

}
