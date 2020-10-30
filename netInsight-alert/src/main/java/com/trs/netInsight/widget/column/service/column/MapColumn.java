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
import com.trs.netInsight.widget.analysis.entity.ChartResultField;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.user.entity.User;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 地域热力图
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年4月8日
 */
public class MapColumn extends AbstractColumn {

	@Override
	public Object getColumnData(String timeRange) throws TRSSearchException {
		//用queryCommonBuilder和QueryBuilder 是一样的的
		QueryCommonBuilder builder = super.config.getCommonBuilder();
		IndexTab indexTab = super.config.getIndexTab();
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		String groupNames = super.config.getIndexTab().getGroupName();//本身存储的来源;
		List<Map<String, Object>> list = new ArrayList<>();
		builder.setPageSize(Integer.MAX_VALUE);
        ChartResultField resultField = new ChartResultField("name", "value");
        String contrastField = FtsFieldConst.FIELD_CATALOG_AREA;
        String contrast = indexTab.getContrast();
        if(StringUtil.isNotEmpty(contrast) && contrast.equals(ColumnConst.CONTRAST_TYPE_MEDIA_AREA)){
			contrastField = FtsFieldConst.FIELD_MEDIA_AREA;
		}else{
			contrast = ColumnConst.CONTRAST_TYPE_HIT_ARTICLE;
		}
		try {
        	String type = "column";
        	if(config.getMapto()!=null && !config.getMapto().equals("")) type = Const.mapto+config.getMapto();
			list = (List<Map<String, Object>>) commonChartService.getMapColumnData(builder, sim, irSimflag, irSimflagAll, groupNames, contrastField, type,resultField);
			if(list == null){
				return null;
			}
			for(Map<String,Object> oneMap : list){
				oneMap.put("contrast",contrast);
			}
			if(list != null && list.size() >0){
				Collections.sort(list, (o1, o2) -> {
					Integer seq1 = (Integer) o1.get("value");
					Integer seq2 = (Integer) o2.get("value");
					return seq2.compareTo(seq1);
				});
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
		//用queryCommonBuilder和QueryBuilder 是一样的的
		QueryCommonBuilder commonBuilder = super.config.getCommonBuilder();
		IndexTab indexTab = super.config.getIndexTab();
		boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean irSimflagAll = indexTab.isIrSimflagAll();
		String area = this.config.getKey();
		// 取地域名
		if (!"ALL".equals(area)) { // 地域
			String contrastField = FtsFieldConst.FIELD_CATALOG_AREA;
			String contrast = indexTab.getContrast();
			if(StringUtil.isNotEmpty(contrast) && contrast.equals(ColumnConst.CONTRAST_TYPE_MEDIA_AREA)){
				contrastField = FtsFieldConst.FIELD_MEDIA_AREA;
			}
			Map<String,String> areaMap = null;
			if(FtsFieldConst.FIELD_MEDIA_AREA.equals(contrastField)){
				areaMap = Const.MEDIA_PROVINCE_NAME;
			}else if(FtsFieldConst.FIELD_CATALOG_AREA.equals(contrastField)){
				areaMap = Const.CONTTENT_PROVINCE_NAME;
			}
			commonBuilder.filterByTRSL(contrastField + ":(" + areaMap.get(area) +")");
		}
		// 取userId
		User loginUser = UserUtils.getUser();
		String checkGroupName = this.config.getGroupName();

		try {
			if ("ALL".equals(checkGroupName)) {
				checkGroupName = indexTab.getGroupName();
			}
			//处理数据源
			checkGroupName = StringUtils.join(CommonListChartUtil.formatGroupName(checkGroupName), ";");

			if("hot".equals(this.config.getOrderBy())){
				return commonListService.queryPageListForHot(commonBuilder,checkGroupName,loginUser,"column",true);
			}else{
				return commonListService.queryPageList(commonBuilder,sim,irSimflag,irSimflagAll,checkGroupName,"column",loginUser,true);
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
		return null;
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
		/*String orderBy = builder.getOrderBy();

		String checkGroupName = super.config.getGroupName();//当前列表选中的数据源
		// 统一来源

		//当前栏目对应的数据源  地图的数据源就是groupname
		String groupNames = super.config.getIndexTab().getGroupName();//本身存储的来源;
		List<String> sourceList = CommonListChartUtil.formatGroupName(groupNames);
		if (!"ALL".equals(checkGroupName) && StringUtil.isNotEmpty(checkGroupName)) {
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
				//String exMNForOther = newAsTrsl.replace(" AND "+"("+"IR_NRESERVED1:(0))","").replace(" AND (IR_NRESERVED1:(1))","").replace(" AND (IR_RETWEETED_MID:(0 OR \"\"))", "").replace(" NOT IR_RETWEETED_MID:(0 OR \"\")", "");
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
		}*/
		int pageNo = super.config.getPageNo();
		int pageSize = super.config.getPageSize();
		builder.setPageNo(pageNo);
		if( pageSize != 0){
			builder.setPageSize(pageSize);
		}
		return builder;
	}
}
