package com.trs.netInsight.widget.tool.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.MapUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.tool.service.ISensitiveService;

/**
 * 文章敏感模态分析服务接口实现
 *
 * @author changjiang
 * @date created at ${Date} - 12:04
 * @since 北京拓尔思信息技术股份有限公司
 */
@Service
public class SensitiveServiceImpl implements ISensitiveService {

	@Autowired
	private FullTextSearch hybase8SearchService;

	// /**
	// * wxb白名单热度系数
	// */
	// private final float wxbListNum = 0.5f;
	// /**
	// * 非wxb白名单热度系数
	// */
	// private final float notWxbListNum = 0.2f;

	@Override
	public Object getSensitiveDegree(String trsl) throws TRSException {
		return null;
	}

	@Override
	public Object getTendencyTendency(String trsl, Object sensitiveDegree, Object getHotDegree) throws TRSException {
		return null;
	}

	@Override
	public float getHotDegree(String md5tag, Date date) throws TRSException {
		if (date == null) {
			date = new Date();
		}

		QueryBuilder builder = getBuilder(getTimeInterval(date, DateUtil.yyyyMMddHHmmss, 15), md5tag);
		// 总数
		long allCount = hybase8SearchService.ftsCount(builder, false, true,false,null);
		if (allCount == 0) {
			return 0;
		}
		// wxb白名单热度媒体数量
		builder.filterField(FtsFieldConst.FIELD_WXB_LIST, "0", Operator.Equal);
		long wxbListCount = hybase8SearchService.ftsCount(builder, false, true,false,null);

		// long notWxbListCount = allCount - wxbListCount;
		float wxbSite = (float) wxbListCount;
		float all = (float) allCount;

		return Float.valueOf(String.format("%.2f", (wxbSite / all) * 100f));
	}

	@Override
	public Object getSensitiveType(String trsl) throws TRSException {
		QueryBuilder builder = new QueryBuilder();
		builder.setDatabase(Const.HYBASE_NI_INDEX);
		builder.filterByTRSL(trsl);
		String sensitiveType = "";
		try {
			List<FtsDocument> ftsDocuments = hybase8SearchService.ftsQuery(builder, FtsDocument.class, false, true,false,null);
			if (ftsDocuments.size() > 0) {
				sensitiveType = ftsDocuments.get(0).getSensitiveType();
				// sensitiveType = convertType(sensitiveType);
			}
		} catch (TRSSearchException e) {
			throw new TRSException(e);
		}
		return sensitiveType;
	}

	/**
	 * 计算敏感相关数据
	 *
	 * @param sensitiveType
	 * @return
	 */
	private Map<String, Object> compute(String sensitiveType) {
		Map<String, Object> result = new HashMap<>();
		float ratio = 0.0f;
		if (StringUtils.isNotBlank(sensitiveType) && sensitiveType.contains("重点舆情")) {
			float count = 0.0f;
			String[] types = null;
			if (sensitiveType.contains(";")) {
				types = sensitiveType.split(";");
				boolean isFirst = false;
				for (int i = 0; i < types.length; i++) {
					if (types[i].contains("重点舆情")) {
						if (!isFirst) {
							sensitiveType = types[i];// 选取第一个重点舆情
							isFirst = true;
						}
						count = count + 1.0f;
					}
				}

			} else {
				ratio = 10f;
			}
			sensitiveType = sensitiveType.substring(sensitiveType.lastIndexOf("\\") + 1, sensitiveType.length());
			sensitiveType = "重点舆情-" + sensitiveType;
			if (count > 0.0f && types != null && types.length > 0) {
				ratio = count / (float) types.length * 100;

			}
		} else {
			sensitiveType = "一般舆情";
			ratio = 10f;
		}
		result.put("sensitiveType", sensitiveType);
		result.put("ratio", Float.valueOf(String.format("%.2f", ratio)));
		return result;
	}

	@Override
	public Object computeSensitive(String trsl) throws TRSException {
		QueryBuilder builder = new QueryBuilder();
		builder.setDatabase(Const.HYBASE_NI_INDEX);
		builder.filterByTRSL(trsl);
		String sensitiveType = "";
		Map<String, Object> result = new HashMap<>();
		try {
			List<FtsDocument> ftsDocuments = hybase8SearchService.ftsQuery(builder, FtsDocument.class, false, false,false,null);
			if (ftsDocuments.size() > 0) {
				FtsDocument ftsDocument = ftsDocuments.get(0);
				sensitiveType = ftsDocuments.get(0).getSensitiveType();
				result = compute(sensitiveType);
				result.put("hotDegree", getHotDegree(ftsDocument.getMd5Tag(), ftsDocument.getUrlTime()));
				result.put("dangersTendency", "75");
			}
			weighting(result);
		} catch (TRSSearchException e) {
			throw new TRSException(e);
		}
		return result;
	}

	@Override
	public Map<String, Object> getTrendMap(String md5, Date date) throws TRSException {
		String[] timeInterval = getTimeInterval(date, DateUtil.yyyyMMddHHmmss, 3);
		try {
			GroupResult groupResult = hybase8SearchService.categoryQuery(getBuilder(timeInterval, md5), false, false,false,
					FtsFieldConst.FIELD_URLDATE,null, Const.HYBASE_NI_INDEX);

			List<String> dateList = DateUtil.getBetweenDateString(timeInterval[0], timeInterval[1],
					DateUtil.yyyyMMddHHmmss, DateUtil.yyyyMMdd3);
			Map<String, Object> result = new HashMap<>();
			List<Long> list = MapUtil.sortAndChangeList(groupResult, dateList, DateUtil.yyyyMMdd3, true);
			result.put("date", dateList);
			result.put("list", list);
			return result;
		} catch (TRSSearchException e) {
			throw new TRSException(e);
		}

	}

	@Override
	public Map<String, Long> getEmotion(String md5, Date date) throws TRSException {
		String[] timeInterval = getTimeInterval(date, DateUtil.yyyyMMddHHmmss, 15);
		QueryBuilder builderAll = getBuilder(timeInterval, md5);
		QueryBuilder builderPositive = getBuilder(timeInterval, md5);
		builderPositive.filterField(FtsFieldConst.FIELD_APPRAISE, "正面", Operator.Equal);
		QueryBuilder builderNegative = getBuilder(timeInterval, md5);
		builderNegative.filterField(FtsFieldConst.FIELD_APPRAISE, "负面", Operator.Equal);
		// 总数
		long allCount = hybase8SearchService.ftsCount(builderAll, false, false,false,null);
		if (allCount == 0) {
			return null;
		}
		// 正面
		long positiveCount = hybase8SearchService.ftsCount(builderPositive, false, false,false,null);
		// 负面
		long negativeCount = hybase8SearchService.ftsCount(builderNegative, false, false,false,null);
		// 中性
		long neutralCount = allCount - positiveCount - negativeCount;
		Map<String, Long> result = new HashMap<>();
		result.put("positive", positiveCount);
		result.put("negative", negativeCount);
		result.put("neutral", neutralCount);
		return result;
	}

	/**
	 * 个性加权
	 *
	 * @param result
	 * @return
	 */
	private void weighting(Map<String, Object> result) {
		if (result != null && result.containsKey("hotDetree") && result.containsKey("ratio")) {
			float ratio = (float) result.get("ratio");
			float hotDetree = (float) result.get("hotDetree");
			if (hotDetree > 100f && ratio <= 0f) {
				ratio = 20.0f;
				result.put("ratio", ratio);
			}
		}
	}

	/**
	 * 根据文章时间获取搜索时间
	 * 
	 * @date Created at 2018年8月16日 下午3:37:39
	 * @Author 谷泽昊
	 * @param date
	 * @param format
	 * @param day
	 *            时间区间
	 * @return
	 */
	private String[] getTimeInterval(Date date, String format, int day) {
		String beginTime = null;
		String endTime = null;
		int now = DateUtil.rangBetweenNow(date);
		if (now == 0) {
			beginTime = DateUtil.formatDateAfter(date, format, -day * 2);
			endTime = DateUtil.formatCurrentTime(format);
		} else {
			beginTime = DateUtil.formatDateAfter(date, format, -day * 2 - now);
			endTime = DateUtil.formatDateAfter(date, format, -now);
		}
		return new String[] { beginTime, endTime };
	}

	/**
	 * 根据MD5查询时，通用创建QueryBuilder
	 * 
	 * @date Created at 2018年8月16日 下午3:55:14
	 * @Author 谷泽昊
	 * @param date
	 * @param md5tag
	 * @return
	 */
	private QueryBuilder getBuilder(String[] timeInterval, String md5tag) {
		QueryBuilder builder = new QueryBuilder();
		builder.setDatabase(Const.HYBASE_NI_INDEX);
		builder.setPageSize(Integer.MAX_VALUE);
		builder.filterField(FtsFieldConst.FIELD_URLTIME, timeInterval, Operator.Between);
		builder.filterField(FtsFieldConst.FIELD_MD5TAG, md5tag, Operator.Equal);
		return builder;
	}

	/**
	 * 按siteName分类统计，
	 * @param sid
	 * @param md5
	 * @param date
	 * @param trslk
	 * @return
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	@Override
	public List<GroupInfo> getPie(String sid, String md5, String trsl) throws TRSException, TRSSearchException {
		//最后返结果的map
		List<Map<String,Object>> arrayList = new ArrayList<Map<String,Object>>();
		
//		Map<String,Map<String,Object>> map1 = new HashMap<String, Map<String,Object>>();
		
		QueryBuilder builder = new QueryBuilder();
		List<GroupInfo> list = null;
		GroupResult result = null;
		builder.setPageNo(0);
		builder.setPageSize(10);
//      builder.filterField(FtsFieldConst.FIELD_SID, sid, Operator.Equal);
        builder.filterByTRSL(trsl);
        builder.filterField(FtsFieldConst.FIELD_MD5TAG, md5, Operator.Equal);
	    // wxb白名单
	    builder.filterField(FtsFieldConst.FIELD_WXB_LIST, "0", Operator.Equal);
	    //前三天+后三天
	    //builder.filterField(FtsFieldConst.FIELD_URLTIME, timeInterval, Operator.Between);
	    String asTRSL = builder.asTRSL();
	    result = hybase8SearchService.categoryQuery(builder, false, false,false, FtsFieldConst.FIELD_SITENAME, null,Const.HYBASE_NI_INDEX);
	    list = result.getGroupList();
	    //优先显示白名单的图，如果点击该文中不是白名单中网点，则取非白名单做分类统计进而出饼状图
	    if(ObjectUtil.isEmpty(list)){
	    	 QueryBuilder builder2 = new QueryBuilder();
	    	 builder2.setPageNo(0);
	    	 builder2.setPageSize(10);
	    	 builder2.filterByTRSL(trsl);
	         builder.filterField(FtsFieldConst.FIELD_MD5TAG, md5, Operator.Equal);
	 	     //白名单
	         builder2.filterField(FtsFieldConst.FIELD_WXB_LIST, "0", Operator.NotEqual);
	 	     String as = builder2.asTRSL();
	 	     result = hybase8SearchService.categoryQuery(builder2, false, false,false, FtsFieldConst.FIELD_SITENAME, null,Const.HYBASE_NI_INDEX);
	 	     list = result.getGroupList();
	     }
	     //先取前8看效果
	     if(list.size()>8){
	    	 list = list.subList(0, 8);
	     }
//	     for (GroupInfo groupInfo : list) {
//	    	 Map<String,Object> map2 = new HashMap<String, Object>();
//	    	 long count = groupInfo.getCount();
//	    	 String siteName = groupInfo.getFieldValue();
//	    	 map2.put("name", siteName);
//	    	 map2.put("value", count);
//	    	 arrayList.add(map2);
//		 }
		return list;
	}

	@Override
	public List<GroupInfo> getPieNoCache(Date date, String md5) throws TRSException, TRSSearchException {
		List<GroupInfo> list = null;
		GroupResult result = null;
		String[] timeInterval = DateUtil.getTimeInterval(date, DateUtil.yyyyMMddHHmmss, 15);
		if(StringUtil.isNotEmpty(md5)){
			QueryBuilder builder = getBuilder(timeInterval, md5);
			if (timeInterval != null && timeInterval.length == 2){
				builder.setPageNo(0);
				builder.setPageSize(10);
				builder.setStartTime(DateUtil.stringToDate(timeInterval[0],DateUtil.yyyyMMddHHmmss));
				builder.setEndTime(DateUtil.stringToDate(timeInterval[1],DateUtil.yyyyMMddHHmmss));
				builder.filterField(FtsFieldConst.FIELD_WXB_LIST, "0", Operator.Equal);
		 	    String as = builder.asTRSL();
		 	    result = hybase8SearchService.categoryQuery(builder, false, false,false, FtsFieldConst.FIELD_SITENAME,null, Const.HYBASE_NI_INDEX);
		 	    list = result.getGroupList();
			}
			//优先显示白名单的图，如果点击该文中不是白名单中网点，则取非白名单做分类统计进而出饼状图
		    if(ObjectUtil.isEmpty(list)){
		    	QueryBuilder builder2 = getBuilder(timeInterval, md5);
		    	builder2.setPageNo(0);
		    	builder2.setPageSize(10);
		    	builder2.setStartTime(DateUtil.stringToDate(timeInterval[0],DateUtil.yyyyMMddHHmmss));
		    	builder2.setEndTime(DateUtil.stringToDate(timeInterval[1],DateUtil.yyyyMMddHHmmss));
		    	builder2.filterField(FtsFieldConst.FIELD_WXB_LIST, "0", Operator.NotEqual);
		 	    String as = builder2.asTRSL();
		 	    result = hybase8SearchService.categoryQuery(builder2, false, false,false, FtsFieldConst.FIELD_SITENAME,null, Const.HYBASE_NI_INDEX);
		 	    list = result.getGroupList();
			}
		    if(list.size()>8){
		    	list = list.subList(0, 8);
		    }
		    return list;
		}
		return null;
	}
}
