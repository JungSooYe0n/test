package com.trs.netInsight.widget.column.service.column;

import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.analysis.entity.CategoryBean;
import com.trs.netInsight.widget.analysis.entity.ChartResultField;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.emuns.ChartPageInfo;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.user.entity.User;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 饼状图图
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年4月8日
 */
public class PieColumn extends AbstractColumn {

	@Override
	public Object getColumnData(String timeRange) throws TRSSearchException {
		
		List<Map<String, Object>> list = new ArrayList<>();
		IndexTab indexTab = super.config.getIndexTab();
		try {
			//用queryCommonBuilder和QueryBuilder 是一样的的
			QueryCommonBuilder builder = super.config.getCommonBuilder();
			boolean sim = indexTab.isSimilar();
			// url排重
			boolean irSimflag = indexTab.isIrSimflag();
			boolean irSimflagAll = indexTab.isIrSimflagAll();
			String groupName = indexTab.getGroupName();
			builder.setPageSize(8);
			ChartResultField resultField = new ChartResultField("name", "value");
			if (StringUtils.isNotBlank(indexTab.getContrast())
					|| ChartPageInfo.StatisticalChart.equals(super.config.getChartPage())) {// 对比类别不为空,断言简单模式
				String contrastField = FtsFieldConst.FIELD_GROUPNAME;
				String type = indexTab.getType();
				if(ChartPageInfo.StatisticalChart.equals(super.config.getChartPage())){
					if(ColumnConst.CHART_PIE.equals(type)){
						contrastField = FtsFieldConst.FIELD_GROUPNAME;
						builder.setPageSize(20);

					}else if(ColumnConst.CHART_PIE_EMOTION.equals(type)){
						contrastField = FtsFieldConst.FIELD_APPRAISE;
						builder.setPageSize(20);

					}
				} else{
					// 来源分类对比图
					if (indexTab.getContrast().equals(ColumnConst.CONTRAST_TYPE_GROUP)) {
						builder.setPageSize(20);
					}
					// 站点对比图
					if (indexTab.getContrast().equals(ColumnConst.CONTRAST_TYPE_SITE)) {
						contrastField = FtsFieldConst.FIELD_SITENAME;
					}
					//微信公众号对比
					if (indexTab.getContrast().equals(ColumnConst.CONTRAST_TYPE_WECHAT)){
						contrastField = FtsFieldConst.FIELD_SITENAME;
					}
				}
				list = (List<Map<String, Object>>)commonChartService.getPieColumnData(builder,sim,irSimflag,irSimflagAll,groupName,null,contrastField,"column",resultField);
			} else {// 专家模式
				list = (List<Map<String, Object>>)commonChartService.getBarColumnData(builder,sim,irSimflag,irSimflagAll,groupName, indexTab.getXyTrsl(),null,"column",resultField);
			}
			return list;
		} catch (TRSException | TRSSearchException e) {
			throw new TRSSearchException(e);
		}

	}

	@Override
	public Object getColumnCount() throws TRSSearchException {
		return null;
	}

	@Override
	public Object getSectionList() throws TRSSearchException {
		User loginUser = UserUtils.getUser();
		IndexTab indexTab = super.config.getIndexTab();
		//用queryCommonBuilder和QueryBuilder 是一样的的
		QueryCommonBuilder commonBuilder = super.config.getCommonBuilder();
		//当前列表选中的数据源
		String checkGroupName = super.config.getGroupName();
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		try {
			String type = indexTab.getType();
			if ("ALL".equals(checkGroupName)) {
				checkGroupName = indexTab.getGroupName();
				//微信公众号对比
				if (ColumnConst.CONTRAST_TYPE_WECHAT.equals(indexTab.getContrast())) {
					checkGroupName = Const.GROUPNAME_WEIXIN;
				}
				if(StringUtil.isNotEmpty(indexTab.getXyTrsl())){
					if(StringUtil.isEmpty(checkGroupName)){
						checkGroupName = "ALL";
					}
				}
			}
			//处理数据源
			checkGroupName = StringUtils.join(CommonListChartUtil.formatGroupName(checkGroupName), ";");
			//专家模式
			if (StringUtil.isNotEmpty(indexTab.getXyTrsl()) && !ChartPageInfo.StatisticalChart.equals(super.config.getChartPage())) {
				List<CategoryBean> mediaType = CommonListChartUtil.getMediaType(indexTab.getXyTrsl());
				for (CategoryBean categoryBean : mediaType) {

					if (config.getKey().equals(categoryBean.getKey())) {
						if (StringUtil.isNotEmpty(categoryBean.getValue())) {
							String value = categoryBean.getValue().toLowerCase().trim();
							if (value.startsWith("not")) {
								commonBuilder.filterByTRSL_NOT(categoryBean.getValue().substring(3, categoryBean.getValue().length()));
							} else {
								commonBuilder.filterByTRSL(categoryBean.getValue());
							}
						}
						break;
					}
				}
			}else{
				//站点对比
				if (ColumnConst.CONTRAST_TYPE_SITE.equals(indexTab.getContrast())) {
					commonBuilder.filterField(FtsFieldConst.FIELD_SITENAME, super.config.getKey(), Operator.Equal);
				}else if (ColumnConst.CONTRAST_TYPE_WECHAT.equals(indexTab.getContrast())) {//微信公众号对比
					commonBuilder.filterField(FtsFieldConst.FIELD_SITENAME, super.config.getKey(), Operator.Equal);
				}else if(ColumnConst.CHART_PIE_EMOTION.equals(type)){
					commonBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, super.config.getKey(), Operator.Equal);

				}else if(!ColumnConst.CONTRAST_TYPE_GROUP.equals(indexTab.getContrast())){
					//除去专家模式，柱状图只有三种模式，如果不是这三种，则无对比模式
					throw new TRSSearchException("未获取到检索条件");
				}
			}
			if("hot".equals(this.config.getOrderBy())){
				return commonListService.queryPageListForHot(commonBuilder,checkGroupName,loginUser,"column",true);
			}else{
				return commonListService.queryPageList(commonBuilder,sim,irSimflag,irSimflagAll,checkGroupName,"column",loginUser,true);
			}
		}catch (TRSException e){
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
		return null;
	}

	@Override
	public Object getAppSectionList(User user) throws TRSSearchException {
		return null;
	}

	//没用了
	@Override
	public QueryBuilder createQueryBuilder() {
		QueryBuilder builder = super.config.getQueryBuilder();
		return builder;

	}

	@Override
	public QueryCommonBuilder createQueryCommonBuilder() {
		QueryCommonBuilder builder = super.config.getCommonBuilder();
		/*String orderBy = builder.getOrderBy();
		IndexTab indexTab = super.config.getIndexTab();

		// 统一来源
		String checkGroupName = super.config.getGroupName();//当前列表选中的数据源
		//当前栏目对应的数据源
		String groupNames = super.config.getIndexTab().getGroupName();//本身存储的来源;
		//站点对比
		if (ColumnConst.CONTRAST_TYPE_SITE.equals(indexTab.getContrast())) {
			groupNames = StringUtil.join(indexTab.getTradition(), ";");
		}
		//微信公众号对比
		if (ColumnConst.CONTRAST_TYPE_WECHAT.equals(indexTab.getContrast())) {
			groupNames = Const.GROUPNAME_WEIXIN;
		}

		List<String> sourceList = CommonListChartUtil.formatGroupName(groupNames);
		if (!"ALL".equals(checkGroupName)) {
			List<String> checkSourceList = CommonListChartUtil.formatGroupName(checkGroupName);
			Boolean isSearch = false;
			for (String group : checkSourceList) {
				if (sourceList.contains(group)) {
					isSearch = true;
				}
			}
			if (!isSearch) {
				throw new TRSSearchException("当前栏目无法查询该数据源");
			}
		}
		groupNames = StringUtils.join(sourceList," OR ");
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
				String exMNForOther = newAsTrsl.replace(" AND "+"("+"IR_NRESERVED1:(0 OR \"\"))","").replace(" AND (IR_NRESERVED1:(1))","").replace("(IR_NRESERVED1:(0 OR \"\")) AND ","").replace("(IR_NRESERVED1:(1)) AND ","").replace(" AND (IR_RETWEETED_MID:(0 OR \"\"))", "").replace(" NOT IR_RETWEETED_MID:(0 OR \"\")", "");

				String otherTrsl = FtsFieldConst.FIELD_GROUPNAME + ":("+exGForOther+") AND " + exMNForOther;

				builder = new QueryCommonBuilder();
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
*/
		int pageNo = super.config.getPageNo();
		int pageSize = super.config.getPageSize();
		builder.setPageNo(pageNo);
		if( pageSize != 0){
			builder.setPageSize(pageSize);
		}
		return builder;
	}
}
