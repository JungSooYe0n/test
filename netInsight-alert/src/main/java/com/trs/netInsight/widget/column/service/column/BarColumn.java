package com.trs.netInsight.widget.column.service.column;

import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.util.SearchTimeLongUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 柱状图
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年4月8日
 */
public class BarColumn extends AbstractColumn {

	@Override
	public Object getColumnData(String timeRange) throws TRSSearchException {

		IndexTab indexTab = super.config.getIndexTab();
		try {
			QueryCommonBuilder builder = super.config.getCommonBuilder();
			boolean sim = indexTab.isSimilar();
			// url排重
			boolean irSimflag = indexTab.isIrSimflag();
			boolean irSimflagAll = indexTab.isIrSimflagAll();
			String groupName = indexTab.getGroupName();
			//记录searchTimeLongLog日志
			SearchTimeLongUtil.execute(indexTab.getName(), indexTab.getTimeRange());
			builder.setPageSize(this.config.getPageSize());

			//判断类型，如果是横向柱状图或微博热点话题，需要
			ChartResultField resultField = new ChartResultField("name", "value");
			if(ChartPageInfo.StatisticalChart.equals(super.config.getChartPage())){
				String type = indexTab.getType();
				builder.setPageSize(10);
				List<String> sourceList = CommonListChartUtil.formatGroupName(groupName);
				if(ColumnConst.HOT_TOPIC_SORT.equals(type)){
					//不受栏目选择数据源限制
					String contrastField = FtsFieldConst.FIELD_TAG;
					String currentGroupName = Const.GROUPNAME_WEIBO;
					return commonChartService.getBarColumnData(builder,sim,irSimflag,irSimflagAll,currentGroupName,null,contrastField,"column",resultField);

				}else if(ColumnConst.CHART_BAR_CROSS.equals(type)){

					List<String> allList = Const.ALL_GROUPNAME_SORT;
					List<Object> result = new ArrayList<>();
					Boolean resultFlag = true;
					Integer active = 0;
					for (String oneGroupName : allList) {
						//只显示选择的数据源
						if (sourceList.contains(oneGroupName)) {
							String contrastField = FtsFieldConst.FIELD_SITENAME;
							if (Const.GROUPNAME_WEIBO.equals(oneGroupName)) {
								contrastField = FtsFieldConst.FIELD_SCREEN_NAME;
							} else if (Const.MEDIA_TYPE_TF.contains(oneGroupName) || Const.MEDIA_TYPE_VIDEO.contains(oneGroupName) || Const.MEDIA_TYPE_ZIMEITI_LUNTAN_BOKE.contains(oneGroupName)) {
								//FaceBook、Twitter、视频、短视频、自媒体号、论坛、博客
								contrastField = FtsFieldConst.FIELD_AUTHORS;
							}
							QueryBuilder queryBuilder = new QueryBuilder();
							queryBuilder.filterByTRSL(builder.asTRSL());
							if (Const.GROUPNAME_XINWEN.equals(oneGroupName)){
								//去掉新闻里的这类号
								queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME,Const.REMOVEMEDIAS,Operator.NotEqual);
							}
							Map<String,Object> oneInfo = new HashMap<>();
							Object list = commonChartService.getBarColumnData(queryBuilder,sim,irSimflag,irSimflagAll,oneGroupName,null,contrastField,"column",resultField);
                            List<Map<String, Object>> changeList = new ArrayList<>();
							if (list != null) {
								changeList = (List<Map<String, Object>>) list;
								if (changeList.size() > 0) {
									resultFlag = false;
									active++;
								}
							}
							oneInfo.put("name", Const.PAGE_SHOW_GROUPNAME_CONTRAST.get(oneGroupName));
							oneInfo.put("info", list);
							oneInfo.put("active", active == 1 ? true : false);
							result.add(oneInfo);
						}
					}
					if (resultFlag) {
						return null;
					}
					return result;
				}
				return null;
			}else if (StringUtils.isNotBlank(indexTab.getContrast())) {// 对比类别不为空,断言简单模式
				String contrastField = FtsFieldConst.FIELD_GROUPNAME;
				// 来源分类对比图
				if (ColumnConst.CONTRAST_TYPE_GROUP.equals(indexTab.getContrast())) {
					builder.setPageSize(20);
				}

				// 站点对比图
				if (ColumnConst.CONTRAST_TYPE_SITE.equals(indexTab.getContrast())) {
					contrastField = FtsFieldConst.FIELD_SITENAME;
				}

				//微信公众号对比
				if (ColumnConst.CONTRAST_TYPE_WECHAT.equals(indexTab.getContrast())) {
					contrastField = FtsFieldConst.FIELD_SITENAME;
				}
				return commonChartService.getBarColumnData(builder,sim,irSimflag,irSimflagAll,groupName, null,contrastField,"column",resultField);
			} else {// 专家模式
				return commonChartService.getBarColumnData(builder,sim,irSimflag,irSimflagAll,groupName, indexTab.getXyTrsl(),null,"column",resultField);
			}
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
		QueryCommonBuilder commonBuilder = super.config.getCommonBuilder();

		//当前列表选中的数据源
		String checkGroupName = super.config.getGroupName();
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		try {
			String type = indexTab.getType();
			if(ColumnConst.HOT_TOPIC_SORT.equals(type)){
				checkGroupName = Const.GROUPNAME_WEIBO;
			}else{
				if ("ALL".equals(checkGroupName)) {
					checkGroupName = indexTab.getGroupName();
					//微信公众号对比
					if (ColumnConst.CONTRAST_TYPE_WECHAT.equals(indexTab.getContrast())) {
						checkGroupName = Const.GROUPNAME_WEIXIN;
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
			} else{
				String key = super.config.getKey();
				String contrastField = FtsFieldConst.FIELD_SITENAME;
				if (ColumnConst.HOT_TOPIC_SORT.equals(type)) {//微博热点话题
					contrastField = FtsFieldConst.FIELD_TAG;
				}else if (ColumnConst.CHART_BAR_CROSS.equals(type)) {//活跃账号对比
					contrastField = FtsFieldConst.FIELD_SITENAME;
					if(Const.GROUPNAME_WEIBO.equals(checkGroupName)){
						contrastField = FtsFieldConst.FIELD_SCREEN_NAME;
					}else if(Const.MEDIA_TYPE_TF.contains(checkGroupName) || Const.MEDIA_TYPE_VIDEO.contains(checkGroupName) || Const.MEDIA_TYPE_ZIMEITI_LUNTAN_BOKE.contains(checkGroupName)){
						//FaceBook、Twitter、视频、短视频、自媒体号、论坛、博客
						contrastField = FtsFieldConst.FIELD_AUTHORS;
					}
				}else{
					if (ColumnConst.CONTRAST_TYPE_GROUP.equals(indexTab.getContrast())) {// 舆论来源对比
						key = StringUtils.join(CommonListChartUtil.formatGroupName(key), ";");
						contrastField = FtsFieldConst.FIELD_GROUPNAME;
					}else if (ColumnConst.CONTRAST_TYPE_SITE.equals(indexTab.getContrast())) {//站点对比
						contrastField = FtsFieldConst.FIELD_SITENAME;
					}else if (ColumnConst.CONTRAST_TYPE_WECHAT.equals(indexTab.getContrast())) {//微信公众号对比
						contrastField = FtsFieldConst.FIELD_SITENAME;
					} else {
						//除去专家模式，柱状图只有三种模式，+两种特殊的图，如果不是这5种，则无对比模式
						throw new TRSSearchException("未获取到检索条件");
					}
				}
				commonBuilder.filterField(contrastField, "\""+key+"\"", Operator.Equal);
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
		int pageNo = super.config.getPageNo();
		int pageSize = super.config.getPageSize();
		builder.setPageNo(pageNo);
		if (pageSize != 0) {
			builder.setPageSize(pageSize);
		}
		return builder;
	}

}
