package com.trs.netInsight.widget.column.service.column;

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
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.ObjectUtil;
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

import java.util.*;
/**
 * 折线图
 *
 *  @author 北京拓尔思信息技术股份有限公司
 */
public class ChartLineColumn extends AbstractColumn{

	@Override
	public Object getColumnData(String timeRange) throws TRSSearchException {
		String showType = super.config.getShowType();
		IndexTab indexTab = super.config.getIndexTab();
		boolean sim = indexTab.isSimilar();
		//url排重
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		String tabWidth = indexTab.getTabWidth();
		String contrast = indexTab.getContrast();
		//用queryCommonBuilder和QueryBuilder 是一样的的
		String trsl = super.config.getCommonBuilder().asTRSL();
		return queryChartLineData(sim,irSimflag,irSimflagAll,timeRange,showType,tabWidth,trsl,contrast,indexTab.getXyTrsl(),indexTab);
	}

	private Object queryChartLineData(Boolean sim, Boolean irSimflag, Boolean irSimflagAll, String timeRange, String showType, String tabWidth, String trsl, String contrast, String xyTrsl, IndexTab indexTab) {
		Map<String, Object> result = new HashMap<>();
		Map<String, Object> singleMap = new HashMap<>();
		Map<String, Object> doubleMap = new HashMap<>();

		List<String> dateList = new ArrayList<>();
		List<Object> contrastList = new ArrayList<>();
		List<List<Long>> countList = new ArrayList<>();
		List<Object> doubleContrastList = new ArrayList<>();
		List<List<Long>> doubleCountList = new ArrayList<>();
		List<Object> totalList = new ArrayList<>();
		Map<String, Object> maxSourceMap = new HashMap<>();
		List<Long> maxSourceList = new ArrayList<>();

		List<String> contrastData = new ArrayList<>();
		String source = indexTab.getGroupName();
		String contrastField = FtsFieldConst.FIELD_GROUPNAME;
		if (StringUtils.equals(contrast, ColumnConst.CONTRAST_TYPE_GROUP)) {
			List<String> sourceList = CommonListChartUtil.formatGroupName(source);
			List<String> allList = Const.ALL_GROUPNAME_SORT;
			for(String oneGroupName : allList){
				//只显示选择的数据源
				if(sourceList.contains(oneGroupName)){
					contrastData.add(oneGroupName);
				}
			}
		}else if (StringUtils.equals(contrast, ColumnConst.CONTRAST_TYPE_SITE)) {
			contrastField = FtsFieldConst.FIELD_SITENAME;
		}else if (StringUtils.equals(contrast, ColumnConst.CONTRAST_TYPE_WECHAT)) {
			contrastField = FtsFieldConst.FIELD_SITENAME;
		} else if (StringUtil.isNotEmpty(xyTrsl)) {
			contrastField = null;
		}

		/*
		时间格式展示判断
				按小时展示时，最多显示7天的
		小于24小时 - 小时
		 大于24小时且小于48小时
		 		通栏 - 小时
		 		半栏 -天
		 其他
		 	天
		 */
		String[] timeArray = null;
		try {
			timeArray = DateUtil.formatTimeRangeMinus1(timeRange);//修改时间格式 时间戳
		} catch (OperationException e) {
			throw new TRSSearchException(e);
		}

		List<String[]> list_time = new ArrayList<>();
		String groupBy = FtsFieldConst.FIELD_URLTIME;
		//格式化时间，一个String[] 数据对应的是一次查询对应的时间范围  如果按天查，则list_time只有一个值，如果为按小时查询，则有多少天，list_time就有多长，一个元素为当天的起止时间
		//最后list_time 会进行裁剪，因为按小时查询最多查询7天，时间太长了页面无法显示
		if ("hour".equals(showType)) {
			groupBy = FtsFieldConst.FIELD_URLTIME_HOUR;
			list_time = DateUtil.getBetweenTimeOfStartToEnd(timeArray[0], timeArray[1]);
		} else if ("day".equals(showType)) {
			list_time.add(timeArray);
		} else {
			if (DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], tabWidth) <= 1) {
				showType = "hour";
				groupBy = FtsFieldConst.FIELD_URLTIME_HOUR;
				list_time = DateUtil.getBetweenTimeOfStartToEnd(timeArray[0], timeArray[1]);
			} else {
				showType = "day";
				list_time.add(timeArray);
			}
		}
		try {
			if (list_time.size() > 8) {
				list_time = list_time.subList(list_time.size() - 8, list_time.size());
			}
			ChartResultField resultField = new ChartResultField("groupName", "count", "date");
			for (String[] times : list_time) {
				// 获取开始时间和结束时间之间的小时
				//date = DateUtil.getHourOfHH(arrays[0], arrays[1]);
				//这个是按小时分一天之内的时间（开始结束时间都在一天之内），返回结果是带有当天日期的
				//DateUtil.getStartToEndOfHour(arrays[0], arrays[1]);//将时间分组，按天分，早晚
				//获取两个时间之间的所有时间字符串，第一个时间格式为传入的时间格式，第二个为返回的时间格式
				// DateUtil.getBetweenDateString(TimeArray()[0], TimeArray()[1], DateUtil.yyyyMMddHHmmss,DateUtil.yyyyMMdd4);
				String queryTrsl = trsl;
				List<String> groupDate = new ArrayList<>(); // 对比hybase查询结果的key值得时间格式
				List<String> showDate = new ArrayList<>();// 页面展示的时间格式

				//如果按小时展示，需要对表达式中的时间进行替换
				if ("hour".equals(showType)) {
					int n = queryTrsl.indexOf("URLTIME");
					if (n != -1) {
						String timeTrsl = queryTrsl.substring(n + 9, n + 41);//替换查询条件
						queryTrsl = queryTrsl.replace(timeTrsl, times[0] + " TO " + times[1]);
					}

					groupDate = DateUtil.getHourOfHH(times[0], times[1]);
					showDate = DateUtil.getStartToEndOfHour(times[0], times[1]);

				} else {
					groupDate = DateUtil.getBetweenDateString(times[0], times[1], DateUtil.yyyyMMddHHmmss, DateUtil.yyyyMMdd4);
					showDate = groupDate;
				}
				//查询结果的返回时间是与hybase的查询结果一致的时间格式，不是前端页面展示的时间格式
				dateList.addAll(showDate);

				QueryBuilder builder = new QueryBuilder();
				builder.filterByTRSL(queryTrsl);
				Map<String, List<Object>> oneTimeResult = (Map<String, List<Object>>) commonChartService.getChartLineColumnData(builder, sim, irSimflag, irSimflagAll, source, "column", xyTrsl, contrastField, contrastData, groupBy, groupDate, resultField);
				if(contrastList.size() ==0){
					List<Object> oneTimeContrast = oneTimeResult.get(resultField.getContrastField());
					contrastData.clear();
					oneTimeContrast.stream().forEach(oneContrast -> contrastData.add((String)oneContrast));
					contrastList.addAll(contrastData);
				}
				List<Object> oneTimeTotal = oneTimeResult.get("total");
				oneTimeTotal.stream().forEach(onetotal-> totalList.add(onetotal));
				List<Object> oneTimeCount = oneTimeResult.get(resultField.getCountField());
				//将查到的结果放入要展示的结果中
				if(countList.size() == 0){
					for(Object  countOne:oneTimeCount){
						countList.add((List<Long>)countOne);
					}
				}else{
					for(int i=0;i<countList.size();i++){
						List<Long>  countOne= (List<Long>)oneTimeCount.get(i);
						countList.get(i).addAll(countOne);
					}
				}
			}

			Long countTotal = 0L;
			for(Object num : totalList){
				countTotal = countTotal+ (Long)num;
			}
			//判断图上有没有点，如果没有点，则直接返回null，不画图
			if(countTotal == 0L){
				return null;
			}

			int maxIndex = 0;
			Long maxSourceTotal = 0L;
			String maxSourceName = "";
			for(int i =0; i<countList.size();i++){
				Long oneTotal = 0L;
				List<Long> oneCount = countList.get(i);
				for(Long one :oneCount){
					oneTotal += one;
				}
				if(oneTotal > maxSourceTotal){
					maxSourceTotal = oneTotal;
					maxIndex = i;
				}
			}
			maxSourceName = (String)contrastList.get(maxIndex);
			maxSourceList = countList.get(maxIndex);
			for(int i = 0;i<contrastList.size();i++){
				if(maxIndex != i){
					doubleContrastList.add(contrastList.get(i));
					doubleCountList.add(countList.get(i));
				}
			}
			//网察折线图统一的返回结果
			doubleMap.put("legendData",doubleContrastList);
			doubleMap.put("lineXdata",dateList);
			doubleMap.put("lineYdata",doubleCountList);
			doubleMap.put("total",totalList);
			maxSourceMap.put("name",maxSourceName);
			maxSourceMap.put("info",maxSourceList);
			doubleMap.put("maxSource",maxSourceMap);
			result.put("double",doubleMap);

			contrastList.add(0,"总量");
			List<Long> totalLong = new ArrayList<>();
			for(Object one :totalList){
				totalLong.add((Long)one);
			}
			countList.add(0,totalLong);
			//网察折线图统一的返回结果
			singleMap.put("legendData",contrastList);
			singleMap.put("lineXdata",dateList);
			singleMap.put("lineYdata",countList);
			result.put("single",singleMap);

			return result;
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
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();

		//当前列表选中的数据源
		String checkGroupName = super.config.getGroupName();
		//用queryCommonBuilder和QueryBuilder 是一样的的
		QueryCommonBuilder commonBuilder = super.config.getCommonBuilder();
		try {
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
			if (StringUtil.isNotEmpty(indexTab.getXyTrsl())) {
				List<CategoryBean> mediaType = CommonListChartUtil.getMediaType(indexTab.getXyTrsl());
				mediaType.add(new CategoryBean("总量", null));
				if ("总量".equals(config.getKey())) {
					StringBuilder sb = new StringBuilder();
					for (CategoryBean categoryBean : mediaType) {
						if (null != categoryBean.getValue()) {//排除掉 总量对应的value
							sb.append(categoryBean.getValue() + " OR ");
						}
					}
					String str = sb.toString();
					if (str.endsWith(" OR ")) {
						str = str.substring(0, str.length() - 4);
					}
					commonBuilder.filterByTRSL(str);
				} else {
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
				}
			}else{
				if (ColumnConst.CONTRAST_TYPE_GROUP.equals(indexTab.getContrast())) {// 舆论来源对比
                    String key = StringUtils.join(CommonListChartUtil.formatGroupName(super.config.getKey()), ";");
                    commonBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, key, Operator.Equal);
				}else if (ColumnConst.CONTRAST_TYPE_SITE.equals(indexTab.getContrast()) ||ColumnConst.CONTRAST_TYPE_WECHAT.equals(indexTab.getContrast())) {
					//站点对比 + 微信公众号对比
					String sitename = super.config.getKey().replaceAll(";"," OR ");
					if (sitename.endsWith(" OR ")){
						sitename = sitename.substring(0,sitename.length()-4);
					}
					commonBuilder.filterField(FtsFieldConst.FIELD_SITENAME, sitename, Operator.Equal);
				}else{
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

	@Override
	public Object getAppSectionList(User user) throws TRSSearchException {
		return null;
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
	public QueryBuilder createQueryBuilder() {
		// TODO Auto-generated method stub
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
