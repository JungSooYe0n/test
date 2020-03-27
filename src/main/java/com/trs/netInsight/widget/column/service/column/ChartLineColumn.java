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
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
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
		Map<String,String> mapNotNull = new HashMap<>();
		String showType = super.config.getShowType();
		IndexTab indexTab = super.config.getIndexTab();
		boolean sim = indexTab.isSimilar();
		//url排重
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		String tabWidth = indexTab.getTabWidth();
		String contrast = indexTab.getContrast();
		String trsl = super.config.getQueryBuilder().asTRSL();

		if (StringUtils.equals(contrast, ColumnConst.CONTRAST_TYPE_GROUP)) {//按来源分类对比
			try {
				return getDataLineByShowType(trsl, indexTab.getGroupName(),timeRange,sim,irSimflag,irSimflagAll,showType,tabWidth);
			} catch (TRSSearchException e) {
				throw new TRSSearchException(e);
			}
		} else if (StringUtils.equals(contrast, ColumnConst.CONTRAST_TYPE_SITE)) {
			String join = String.join(" OR ",indexTab.getTradition()).replace("境外媒体", "国外新闻");
			String trslCategoryQuery=null;
			if(StringUtils.isNotBlank(trsl)){
				trslCategoryQuery=trsl+FtsFieldConst.FIELD_GROUPNAME+":("+join+")";
			}else{
				trslCategoryQuery=FtsFieldConst.FIELD_GROUPNAME+":("+join+")";
			}
			GroupResult result = null;
			try {
				result = hybase8SearchService.categoryQuery(indexTab.isServer(),trslCategoryQuery, sim,irSimflag,irSimflagAll,
						FtsFieldConst.FIELD_SITENAME, 8,"column", Const.HYBASE_NI);
			} catch (TRSSearchException e) {
				throw new TRSSearchException(e);
			}
			if (result != null) {
				List<GroupInfo> groupList = result.getGroupList();
				if (groupList != null && groupList.size() > 0) {
					StringBuffer buffer = new StringBuffer();
					for (GroupInfo groupInfo : groupList) {
						buffer.append(groupInfo.getFieldValue()).append(";");
					}
					String groupName = buffer.toString();
					try {
						return getDataLineByShowType(trsl, groupName,indexTab.getTimeRange(),sim,irSimflag,irSimflagAll,showType,tabWidth);
					} catch (TRSSearchException e) {
						throw new TRSSearchException(e);
					}
				}
			}
		}else{
			//进入折线图的专家模式
			//判断需要查询的hybase库,为trsl添加来源进行处理
			String metas = indexTab.getGroupName();
			metas = metas.replaceAll("境外媒体", "国外新闻");
			metas = metas.replaceAll("微信", "国内微信");
			String[] data = null;
			if(StringUtil.isNotEmpty(metas)){
				String[] split = metas.split(";");
				data = TrslUtil.chooseDatabases(split);
			}
			metas = metas.trim();
			if (metas.endsWith(";")) {
				metas = metas.substring(0, metas.length() - 2);
			}
			metas = "(" + metas.replaceAll(";", " OR ") + ")";
			//处理分类检索表达式
			List<CategoryBean> mediaType = chartAnalyzeService.getMediaType(indexTab.getXyTrsl());
			//需要修改部分
			//处理折线图日期
			String groupBy = FtsFieldConst.FIELD_URLTIME;
			List<String> list4timeArray = null;
			List<String[]> list_time = new ArrayList<>();
			String[] timeArray = null;
			Boolean timeListEmpty = false;
			try {
				timeArray = DateUtil.formatTimeRange(timeRange);
			} catch (OperationException e1) {
				e1.printStackTrace();
			}
			if (StringUtils.equals(timeRange, "24h")) {
				groupBy = FtsFieldConst.FIELD_URLTIME_HOUR;
				list_time.add(timeArray);
				list4timeArray = DateUtil.getNowDateHourString(timeArray[0], DateUtil.yyyyMMddHHmmss);
			}else if (StringUtils.equals(timeRange, "0d")){
				groupBy = FtsFieldConst.FIELD_URLTIME_HOUR;
				list_time.add(timeArray);
				list4timeArray = DateUtil.getCurrentDateHourString(timeArray[0],timeArray[1], DateUtil.yyyyMMddHHmmss);
			} else if(DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], tabWidth) == 0){
				groupBy = FtsFieldConst.FIELD_URLTIME_HOUR;
				list_time.add(timeArray);
				list4timeArray = DateUtil.getHHLess24(timeArray[0], timeArray[1]);
				int n = trsl.indexOf("URLTIME");
				if(n != -1){
					String str1 = trsl.substring(n + 9, n + 41);//替换查询条件
					trsl = trsl.replace(str1, timeArray[0] + " TO " + timeArray[1]);
				}
			} else {
				if("hour".equals(showType)){//已经将今天和小于24小时的部分去除，其他部分查小时都需要分组
					groupBy = FtsFieldConst.FIELD_URLTIME_HOUR;
					timeListEmpty = true;
					timeArray = DateUtil.getTimeToSevenDay(timeArray[0],timeArray[1]);
					list_time = DateUtil.getBetweenTimeOfStartToEnd(timeArray[0], timeArray[1]);
				}else if("day".equals(showType) && DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], tabWidth) > 1){
					groupBy = FtsFieldConst.FIELD_URLTIME;
					list_time.add(timeArray);
					list4timeArray = DateUtil.getBetweenDateString(super.config.getTimeArray()[0], super.config.getTimeArray()[1], DateUtil.yyyyMMddHHmmss,
							DateUtil.yyyyMMdd4);
				}else{
					if (DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], tabWidth) == 1 && "".equals(showType)) {
						groupBy = FtsFieldConst.FIELD_URLTIME_HOUR;
						timeListEmpty = true;
						list_time = DateUtil.getBetweenTimeOfStartToEnd(timeArray[0], timeArray[1]);
					} else {
						groupBy = FtsFieldConst.FIELD_URLTIME;
						list_time.add(timeArray);
						list4timeArray = DateUtil.getBetweenDateString(super.config.getTimeArray()[0], super.config.getTimeArray()[1], DateUtil.yyyyMMddHHmmss,
								DateUtil.yyyyMMdd4);
					}
				}
			}
			List<Map<String, Object>> listResult = new ArrayList<>();//最终结果
			for (String[] arrays : list_time) {
				List<Map<String, Object>> listMap = new ArrayList<>();//川数据用
				String day_time = null;
				if(timeListEmpty){
					list4timeArray = DateUtil.getStartToEndOfHour(arrays[0], arrays[1]);
					 day_time = list4timeArray.get(0).substring(0,11);
					int n = trsl.indexOf("URLTIME");
					if(n != -1){
						String str1 = trsl.substring(n + 9, n + 41);//替换查询条件
						trsl = trsl.replace(str1, arrays[0] + " TO " + arrays[1]);
					}
				}
				Map<String, Long> mapAll = new LinkedHashMap<>();
				Map<String, Long> mapTotal = new LinkedHashMap<>();
				for (String date : list4timeArray) {
					mapAll.put(date, 0L);
					mapTotal.put(date, 0L);
				}
				int pageSize = 100;
				if(FtsFieldConst.FIELD_URLTIME.equals(groupBy)){
					if(list4timeArray.size()+1 > pageSize){
						pageSize = list4timeArray.size()+1;
					}
				}
				Map<String, Object> mapObjectTotal = new LinkedHashMap<>();
				for (CategoryBean categoryBean : mediaType) {
					QueryBuilder querybuilder = new QueryBuilder();
					//页面上的检索表达式+时间
					querybuilder.filterByTRSL(trsl);
					if(StringUtil.isNotEmpty(categoryBean.getValue())){
						String value = categoryBean.getValue().toLowerCase().trim();
						if(value.startsWith("not")){
							querybuilder.filterByTRSL_NOT(categoryBean.getValue().substring(3,categoryBean.getValue().length()));
						}else {
							querybuilder.filterByTRSL(categoryBean.getValue());
						}
					}

					//来源
					querybuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, metas, Operator.Equal);
					querybuilder.setPageSize(pageSize);
					querybuilder.setServer(indexTab.isServer());
					Map<String, Long> map = new LinkedHashMap<>();
					Map<String, Object> mapObject = new HashMap<>();
					mapObject.put("groupName", categoryBean.getKey());
					map.putAll(mapAll);
					GroupResult result = null;
					try {
						result = hybase8SearchService.categoryQuery(querybuilder, sim, irSimflag, irSimflagAll, groupBy,
								"column",data);
					} catch (TRSSearchException e) {
						throw new TRSSearchException(e);
					}
					if (result != null) {
						List<GroupInfo> groupList = result.getGroupList();
						if (result.getGroupList() != null && result.getGroupList().size() > 0) {
							for (GroupInfo groupInfo : groupList) {
								if(timeListEmpty){
									String key = "";
									if(groupInfo.getFieldValue().length() ==2){
										key = day_time + groupInfo.getFieldValue() + ":00";
									}else if(groupInfo.getFieldValue().length() ==19){
										key = day_time + groupInfo.getFieldValue().substring(11,13) + ":00";
									}
									map.put(key, groupInfo.getCount());
									mapTotal.put(key, groupInfo.getCount() +
											(mapTotal.get(key) == null ? 0l : mapTotal.get(key)));
									mapNotNull.put(key, groupInfo.getFieldValue());
								}else{
									map.put(groupInfo.getFieldValue(), groupInfo.getCount());
									// 计算总量
									mapTotal.put(groupInfo.getFieldValue(), groupInfo.getCount() +
											(mapTotal.get(groupInfo.getFieldValue()) == null ? 0l : mapTotal.get(groupInfo.getFieldValue())));
									mapNotNull.put(groupInfo.getFieldValue(), groupInfo.getFieldValue());
								}
							}
							GroupResult resultMap = new GroupResult();
							resultMap.addAll(map);
							mapObject.put("data", resultMap.getGroupList());
							listMap.add(mapObject);
						} else {
							mapObject.put("data", null);
							listMap.add(mapObject);
						}
					}
				}
				// 总量计算开始
				GroupResult resultMapToTal = new GroupResult();
				resultMapToTal.addAll(mapTotal);
				mapObjectTotal.put("groupName", "总量");
				mapObjectTotal.put("data", resultMapToTal.getGroupList());
				listMap.add(mapObjectTotal);
				// 总量计算结束
				for (Map<String, Object> map : listMap) {
					if (null == map.get("data")) {

						GroupResult resultMap = new GroupResult();
						resultMap.addAll(mapAll);
						map.put("data", resultMap.getGroupList());
					}
				}
				if (listResult.size() == 0 || listResult == null) {
					for (Map<String, Object> map : listMap
					) {
						listResult.add(map);
					}
				} else {
					for (int j = 0; j < listResult.size(); j++) {
						Map<String, Object> map_day = listMap.get(j);
						Map<String, Object> map_all = listResult.get(j);
						List<Object> data_day = (List<Object>) map_day.get("data");
						List<Object> data_all = (List<Object>) map_all.get("data");
						data_all.addAll(data_day);
						map_all.put("data", data_all);
						listResult.set(j, map_all);
					}
				}
			}

			return listResult;
		}
		return null;
	}

	@Override
	public Object getColumnCount() throws TRSSearchException {
		return null;
	}

	/**
	 * 折线图 根据展示形式查询  按小时查询时需要按天数分组
	 *
	 * @return
	 * @throws TRSSearchException
	 */
	private List<Map<String, Object>> getDataLineByShowType(String trsl, String groupName, String timeRange, boolean isSimilar, boolean irSimflag, boolean irSimflagAll, String showType, String tabWidth) throws TRSSearchException {
		String[] timeArray = null;
		try {
			timeArray = DateUtil.formatTimeRangeMinus1(timeRange);//修改时间格式 时间戳
		} catch (OperationException e) {
			throw new TRSSearchException(e);
		}
		String groupBy = FtsFieldConst.FIELD_URLTIME;//正常页面不传展示类型，
		if (StringUtils.equals(timeRange, "24h")) {//默认用小时展示，不分组
			groupBy = FtsFieldConst.FIELD_URLTIME_HOUR;
			Boolean timeType = false;
			List<String> list = new ArrayList<>();
			list = DateUtil.getNowDateHourString(timeArray[0], DateUtil.yyyyMMddHHmmss);//24h
			return getDataLine(trsl, groupName, timeArray, isSimilar, irSimflag, irSimflagAll, groupBy, list, timeType);
		} else if (StringUtils.equals(timeRange, "0d")) {
			groupBy = FtsFieldConst.FIELD_URLTIME_HOUR;
			Boolean timeType = false;
			List<String> list = new ArrayList<>();
			list = DateUtil.getCurrentDateHourString(timeArray[0], timeArray[1], DateUtil.yyyyMMddHHmmss);
			return getDataLine(trsl, groupName, timeArray, isSimilar, irSimflag, irSimflagAll, groupBy, list, timeType);
		} else if (DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], tabWidth) == 0) {//判断通半栏以及时间数  针对指定时间  24 48 为限 按小时  默认
			groupBy = FtsFieldConst.FIELD_URLTIME_HOUR;
			Boolean timeType = false;
			List<String> list = new ArrayList<>();
			list = DateUtil.getHHLess24(timeArray[0], timeArray[1]);
			int n = trsl.indexOf("URLTIME");
			if(n != -1){
				String str1 = trsl.substring(n + 9, n + 41);//替换查询条件
				trsl = trsl.replace(str1, timeArray[0] + " TO " + timeArray[1]);
			}
			return getDataLine(trsl, groupName, timeArray, isSimilar, irSimflag, irSimflagAll, groupBy, list, timeType);
		}  else {
			if (StringUtils.equals(showType, "hour")) {//按小时展示  需要将时间分组
				groupBy = FtsFieldConst.FIELD_URLTIME_HOUR;
				//时间分组
				List<Map<String, Object>> list_all = new ArrayList<>();
				timeArray = DateUtil.getTimeToSevenDay(timeArray[0],timeArray[1]);
				List<String[]> list_time = DateUtil.getBetweenTimeOfStartToEnd(timeArray[0], timeArray[1]);//按天分
				Boolean timeType = true;
				for (String[] arrays : list_time) {
					List<String> list = new ArrayList<>();
					list = DateUtil.getStartToEndOfHour(arrays[0], arrays[1]);//将时间分组，按天分，早晚
					int n = trsl.indexOf("URLTIME");
					if(n != -1){
						String str1 = trsl.substring(n + 9, n + 41);//替换查询条件
						trsl = trsl.replace(str1, arrays[0] + " TO " + arrays[1]);
					}
					List<Map<String, Object>> list_day = getDataLine(trsl, groupName, arrays, isSimilar, irSimflag, irSimflagAll, groupBy, list, timeType);
					if (list_all.size() == 0 || list_all == null) {
						for (Map<String, Object> map : list_day
						) {
							list_all.add(map);
						}
					} else {
						for (int j = 0; j < list_all.size(); j++) {
							Map<String, Object> map_day = list_day.get(j);
							Map<String, Object> map_all = list_all.get(j);
							List<Object> data_day = (List<Object>) map_day.get("data");
							List<Object> data_all = (List<Object>) map_all.get("data");
							data_all.addAll(data_day);
							map_all.put("data", data_all);
							list_all.set(j, map_all);
						}
					}
				}
				return list_all;
			} else if (StringUtils.equals(showType, "day") && DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], tabWidth) > 1) {//按天展示  默认
				groupBy = FtsFieldConst.FIELD_URLTIME;
				Boolean timeType = false;
				List<String> list = new ArrayList<>();
				list = DateUtil.getBetweenDateString(super.config.getTimeArray()[0], super.config.getTimeArray()[1], DateUtil.yyyyMMddHHmmss,
						DateUtil.yyyyMMdd4);
				return getDataLine(trsl, groupName, timeArray, isSimilar, irSimflag, irSimflagAll, groupBy, list, timeType);
			} else {//不穿展示类型，根据栏目属性值进行数据展示
				if (DateUtil.judgeTime24Or48(timeArray[0], timeArray[1], tabWidth) == 1) {//判断通半栏以及时间数  针对指定时间  24 48 为限 需要分组
					groupBy = FtsFieldConst.FIELD_URLTIME_HOUR;
					//时间分组
					List<Map<String, Object>> list_all = new ArrayList<>();
					List<String[]> list_time = DateUtil.getBetweenTimeOfStartToEnd(timeArray[0], timeArray[1]);//按天分
					Boolean timeType = true;
					for (String[] arrays : list_time) {
						List<String> list = new ArrayList<>();
						list = DateUtil.getStartToEndOfHour(arrays[0], arrays[1]);//一天之内的小时
						int n = trsl.indexOf("URLTIME");
						if(n != -1){
							String str1 = trsl.substring(n + 9, n + 41);//替换查询条件
							trsl = trsl.replace(str1, arrays[0] + " TO " + arrays[1]);
						}
						List<Map<String, Object>> list_day = getDataLine(trsl, groupName, arrays, isSimilar, irSimflag, irSimflagAll, groupBy, list, timeType);
						if (list_all.size() == 0 || list_all == null) {
							for (Map<String, Object> map : list_day
							) {
								list_all.add(map);
							}
						} else {
							for (int j = 0; j < list_all.size(); j++) {
								Map<String, Object> map_day = list_day.get(j);
								Map<String, Object> map_all = list_all.get(j);
								List<Object> data_day = (List<Object>) map_day.get("data");
								List<Object> data_all = (List<Object>) map_all.get("data");
								data_all.addAll(data_day);
								map_all.put("data", data_all);
								list_all.set(j, map_all);
							}
						}

					}
					return list_all;
				} else {//用天展示
					groupBy = FtsFieldConst.FIELD_URLTIME;
					List<String> list = new ArrayList<>();
					list = DateUtil.getBetweenDateString(timeArray[0], timeArray[1], DateUtil.yyyyMMddHHmmss,
							DateUtil.yyyyMMdd4);
					return getDataLine(trsl, groupName, timeArray, isSimilar, irSimflag, irSimflagAll, groupBy, list, false);
				}
			}
		}
	}

		/**
         * 折线图 TODO
         *
         * @date Created at 2018年3月30日 上午9:56:31
         * @Author 谷泽昊
         * @param trsl
         * @param groupName
         * @param timeArray
         * @return
         * @throws TRSSearchException
         */
	private List<Map<String, Object>> getDataLine(String trsl, String groupName,String[] timeArray,boolean isSimilar,boolean irSimflag,boolean irSimflagAll,String groupBy,List<String> list,Boolean timeType) throws TRSSearchException {
		Map<String,String> mapNotNull = new TreeMap<>();
		if (StringUtils.isBlank(groupName)) {
			return null;
		}

		List<Map<String, Object>> listMap = new ArrayList<>();
		String[] groupNames = groupName.split(";");

		String day_time = null;
		if(timeType){
			day_time = list.get(0).substring(0,11);
		}
		Map<String, Long> mapAll = new LinkedHashMap<>();
		Map<String, Long> mapTotal = new LinkedHashMap<>();
		for (String date : list) {
			mapAll.put(date, 0L);
			mapTotal.put(date, 0L);
		}
		int pageSize = 100;
		if(FtsFieldConst.FIELD_URLTIME.equals(groupBy)){
			if(list.size()+1 > pageSize){
				pageSize = list.size()+1;
			}
		}
		Map<String, Object> mapObjectTotal = new LinkedHashMap<>();
		for (String name : groupNames) {
			QueryBuilder queryBuilder = new QueryBuilder();
			if (Const.MEDIA_TYPE_WEIXIN.contains(name)) {

				queryBuilder.filterByTRSL(trsl);
				queryBuilder.setDatabase(Const.WECHAT);

			} else if (Const.MEDIA_TYPE_WEIBO.contains(name)) {

				queryBuilder.setDatabase(Const.WEIBO);
				queryBuilder.filterByTRSL(trsl);
			} else if (Const.MEDIA_TYPE_TF.contains(name)) {

				queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, name, Operator.Equal);
				queryBuilder.filterByTRSL(trsl);
				queryBuilder.setDatabase(Const.HYBASE_OVERSEAS);

			} else if (Const.MEDIA_TYPE_FINAL_NEWS.contains(name)) {

				if ("境外媒体".equals(name)) {
					name = "国外新闻";
				}
				queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, name, Operator.Equal);
				queryBuilder.filterByTRSL(trsl);
				queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);

			} else {

				//(IR_SITENAME:(新浪[台湾]))  hybase会报错，要改为(IR_SITENAME:(“新浪[台湾]”)) 20191022
				queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME, "\""+name+"\"", Operator.Equal);
				queryBuilder.filterByTRSL(trsl);
				queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
			}
			queryBuilder.setPageSize(pageSize);
			Map<String, Long> map = new LinkedHashMap<>();
			Map<String, Object> mapObject = new LinkedHashMap<>();
			//页面展示 国外新闻 变为 境外网站  2019-12-10
			//mapObject.put("groupName", name.equals("国外新闻")?"境外媒体":name);
			mapObject.put("groupName", name.equals("国外新闻")?"境外网站":name);
			map.putAll(mapAll);
			GroupResult result = hybase8SearchService.categoryQuery(queryBuilder, isSimilar,irSimflag,irSimflagAll, groupBy,"column",
					queryBuilder.getDatabase());
			if (result != null) {
				List<GroupInfo> groupList = result.getGroupList();
				if (result.getGroupList() != null && result.getGroupList().size() > 0) {
					for (GroupInfo groupInfo : groupList) {
						if (timeType) {//时间格式转化
							String key = "";
							if(groupInfo.getFieldValue().length() ==2){
								key = day_time + groupInfo.getFieldValue() + ":00";
							}else if(groupInfo.getFieldValue().length() ==19){
								key = day_time + groupInfo.getFieldValue().substring(11,13) + ":00";
							}
							map.put(key, groupInfo.getCount());
							mapTotal.put(key, groupInfo.getCount() +
									(mapTotal.get(key) == null ? 0l : mapTotal.get(key)));
							mapNotNull.put(key, groupInfo.getFieldValue());
						} else {
							map.put(groupInfo.getFieldValue(), groupInfo.getCount());
							// 计算总量
							mapTotal.put(groupInfo.getFieldValue(), groupInfo.getCount() +
									(mapTotal.get(groupInfo.getFieldValue()) == null ? 0l : mapTotal.get(groupInfo.getFieldValue())));
							mapNotNull.put(groupInfo.getFieldValue(), groupInfo.getFieldValue());
						}
					}
					GroupResult resultMap = new GroupResult();
					resultMap.addAll(map);
					mapObject.put("data", resultMap.getGroupList());
					listMap.add(mapObject);
				}else{
					mapObject.put("data", null);
					listMap.add(mapObject);
				}
			}
		}
		// 总量计算开始
		GroupResult resultMapToTal = new GroupResult();
		resultMapToTal.addAll(mapTotal);
		mapObjectTotal.put("groupName", "总量");
		mapObjectTotal.put("data", resultMapToTal.getGroupList());
		listMap.add(mapObjectTotal);
		// 总量计算结束
		for(Map<String,Object> map : listMap){
			if(null == map.get("data")){
				Map<String,Long> dataMap = new LinkedHashMap<>();
				//只取不为空的key  折线图点数会比期望的少
				for(String keySet : mapAll.keySet()){
					dataMap.put(keySet, 0L);
				}
				GroupResult resultMap = new GroupResult();
				resultMap.addAll(dataMap);
				map.put("data", resultMap.getGroupList());
			}
		}
		return listMap;
	}
	@Override
	public Object getSectionList() throws TRSSearchException {
		User loginUser = UserUtils.getUser();
		IndexTab indexTab = super.config.getIndexTab();
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();

		String source = super.config.getGroupName();//前台传来的来源
		String key = super.config.getKey();
		QueryBuilder builder = this.config.getQueryBuilder();

		if (StringUtils.isNotEmpty(indexTab.getXyTrsl())){//专家模式
			String groupName = super.config.getIndexTab().getGroupName();//本身存储的来源
			groupName = groupName.trim().replaceAll("境外媒体","国外新闻").replaceAll("微信", "国内微信");
			if (groupName.endsWith(";")) {
				groupName = groupName.substring(0, groupName.length() - 2);
			}
			String[] data = TrslUtil.chooseDatabases(groupName.split(";"));
			groupName = "(" + groupName.replaceAll(";", " OR ") + ")";
			builder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupName, Operator.Equal);
			List<CategoryBean> mediaType = chartAnalyzeService.getMediaType(indexTab.getXyTrsl());
			//新增总量
			mediaType.add(new CategoryBean("总量",null));
			for (CategoryBean categoryBean : mediaType) {
				QueryCommonBuilder builderCommon = this.config.getCommonBuilder();
				builderCommon.setPageNo(builder.getPageNo());
				builderCommon.setPageSize(builder.getPageSize());
				builderCommon.setServer(indexTab.isServer());
				QueryCommonBuilder sourceCommonBuilder = new QueryCommonBuilder();
				if ("ALL".equals(source)){
					//主要是groupName
					sourceCommonBuilder = this.createQueryCommonBuilder();
				}else {
					if (StringUtil.isNotEmpty(source) && !"ALL".equals(super.config.getGroupName())){
						source = source.replace("境外媒体", "国外新闻").replace("微信", "国内微信");
						if (!groupName.contains(source)) {
							return null;
						}
						builderCommon.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
					}
					if (Const.MEDIA_TYPE_WEIBO.contains(source)){
						builderCommon.setDatabase(Const.WEIBO.split(";"));
					}else if (Const.MEDIA_TYPE_WEIXIN.contains(source)){
						builderCommon.setDatabase(Const.WECHAT.split(";"));
					}else if(Const.MEDIA_TYPE_TF.contains(source)){
						builderCommon.setDatabase(Const.HYBASE_OVERSEAS.split(";"));
					}else{
						builderCommon.setDatabase(Const.HYBASE_NI_INDEX.split(";"));
					}
				}
				if (key.equals(categoryBean.getKey())) {

					if ("总量".equals(key)){
						StringBuilder sb = new StringBuilder();
						for (CategoryBean bean : mediaType) {
							if (null != bean.getValue()){//排除掉 总量对应的value
								sb.append(bean.getValue()+" OR ");
							}
						}
						String str = sb.toString();
						if (str.endsWith(" OR ")){
							str = str.substring(0, str.length() - 4);
						}
						builderCommon.filterByTRSL(str);
						sourceCommonBuilder.filterByTRSL(str);
					}else {
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
					}

					try{

						//折线图专家模式列表页无 条件筛选 项 20191031
						if ("hot".equals(this.config.getOrderBy())){
							builder.setDatabase(StringUtil.join(data,";"));
							return infoListService.getHotList(builder,builder,loginUser,"column");
						}
						if ("ALL".equals(source)){
							return this.infoListService.getDocListContrast(sourceCommonBuilder, loginUser, sim,irSimflag,irSimflagAll,"column");
						}else {
							return this.infoListService.getDocListContrast(builderCommon, loginUser, sim,irSimflag,irSimflagAll,"column");
						}
					}catch(TRSException e){
						throw new TRSSearchException(e);
					}
					
				}
			}
			throw new TRSSearchException("未获取到检索条件");
		}else {
            if ("客户端".equals(key)){
                key = "国内新闻_手机客户端";
            }
	
			String contrast = indexTab.getContrast();
			//全部来源
			if(StringUtils.equals(source, "ALL")){
				QueryCommonBuilder builderCommon = this.createQueryCommonBuilder();
				//如果是 站点统计
				if (StringUtils.equals(contrast, ColumnConst.CONTRAST_TYPE_SITE) && StringUtil.isNotEmpty(key)){
					key = key.replaceAll(";"," OR ");
					if (key.endsWith(" OR ")){
						key = key.substring(0,key.length()-4);
					}
					builderCommon.filterField(FtsFieldConst.FIELD_SITENAME, key, Operator.Equal);
				}
				try {
					if ("hot".equals(this.config.getOrderBy())){
						QueryBuilder hotBuilder = new QueryBuilder();
						hotBuilder.filterByTRSL(builderCommon.asTRSL());
						hotBuilder.page(builderCommon.getPageNo(),builderCommon.getPageSize());

						String[] database = builderCommon.getDatabase();
						if (ObjectUtil.isNotEmpty(database)){
							hotBuilder.setDatabase(StringUtil.join(database,";"));
						}else {
							hotBuilder.setDatabase(Const.HYBASE_NI_INDEX);
						}
						return infoListService.getHotList(builder,builder,loginUser,"column");
					}
					return infoListService.getDocListContrast(builderCommon, loginUser, sim,irSimflag,irSimflagAll,"column");
				} catch (TRSException e) {
					throw new TRSSearchException(e);
				}
			}else if (Const.MEDIA_TYPE_WEIBO.contains(source)) {
				builder.setDatabase(Const.WEIBO);
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
	
			} else if (Const.MEDIA_TYPE_WEIXIN.contains(source)) {
				if ("微信".equals(source)){
					source = "国内微信";
				}
				builder.setDatabase(Const.WECHAT_COMMON);
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
	
			} else if (Const.MEDIA_TYPE_FINAL_NEWS.contains(source)) {
				builder.setDatabase(Const.HYBASE_NI);
				if (source.equals("境外媒体")) {
					source = "国外新闻";
				}
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
	
			} else if(Const.MEDIA_TYPE_TF.contains(source)){
				builder.setDatabase(Const.HYBASE_OVERSEAS);
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
	
			} else {
				builder.setDatabase(Const.HYBASE_NI);
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
				//如果是 站点统计
				if (StringUtils.equals(contrast, ColumnConst.CONTRAST_TYPE_SITE) && StringUtil.isNotEmpty(key)){
					key = key.replaceAll(";"," OR ");
					if (key.endsWith(" OR ")){
						key = key.substring(0,key.length()-4);
					}
					builder.filterField(FtsFieldConst.FIELD_SITENAME, key, Operator.Equal);
				}
			}
			try {
				if ("hot".equals(this.config.getOrderBy())){
					return infoListService.getHotList(builder,builder,loginUser,"column");
				}
				return infoListService.getDocListContrast(builder, loginUser, sim,irSimflag,irSimflagAll,"column");
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
		// TODO Auto-generated method stub
		return null;
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
	
}
