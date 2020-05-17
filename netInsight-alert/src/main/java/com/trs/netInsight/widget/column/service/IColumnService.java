/*
 * Project: netInsight
 * 
 * File Created at 2017年11月27日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.column.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.trs.netInsight.widget.user.entity.User;
import org.springframework.data.domain.Sort;

import com.alibaba.fastjson.JSONArray;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.widget.column.entity.Columns;
import com.trs.netInsight.widget.column.entity.IndexTab;

/**
 * @Type IColumnService.java
 * @Desc 栏目服务接口
 * @author yan.changjiang
 * @date 2017年11月27日 下午2:33:43
 * @version
 *  @author 北京拓尔思信息技术股份有限公司
 */
public interface IColumnService {

	/**
	 * 修改一级栏目
	 * 
	 * @param userId
	 *            用户
	 * @param name
	 *            栏目名称
	 * @param oneId
	 *            一级栏目id
	 * @return
	 * @throws OperationException
	 */
	public String updateOne(User user, String name, String oneId) throws OperationException;

	/**
	 * 修改二级栏目
	 * 
	 * @param userId
	 *            用户id
	 * @param name
	 *            栏目名称
	 * @param twoId
	 *            二级栏目id
	 * @return
	 * @throws OperationException
	 */
	public Object updateTwo(String userId, String name, String twoId) throws OperationException;

	/**
	 * 删除二级栏目以及级联的三级栏目
	 * 
	 * @param twoId
	 *            二级栏目id
	 * @return
	 * @throws OperationException
	 */
	public String deleteTwo(String twoId) throws OperationException;

	/**
	 * 删除一级栏目以及级联的二级三级
	 * 
	 * @param oneId
	 *            一级栏目id
	 * @return
	 * @throws OperationException
	 */
	public Object deleteOne(String oneId) throws OperationException;

	/**
	 * 根据用户id查询所有栏目
	 * 
	 * @param user
	 *            用户id
	 * @return
	 * @throws OperationException
	 */
	public List<Map<String, Object>> selectColumn(User user,String typeId) throws OperationException;

	/**
	 * 根据条件返回相关检索数据
	 * 
	 * @param findOne
	 *            三级栏目对象
	 * @param timeArray
	 *            时间范围
	 * @param grouName
	 *            数据类型(传统/微博/微信等)
	 * @param entityType
	 *            分类统计字段
	 * @return
	 * @throws SearchException
	 * @throws TRSException
	 */
//	public Object selectChart(IndexTab findOne, String[] timeArray, String grouName, String entityType,boolean mix,String emotion,int sumExport,String timeRange) throws SearchException, TRSException;

	/**
	 * 保存
	 * @date Created at 2017年12月4日  下午6:42:53
	 * @Author 谷泽昊
	 * @param column
	 */
	public void save(Columns column);

	/**
	 * 根据用户id查
	 * @date Created at 2017年12月4日  下午6:43:09
	 * @Author 谷泽昊
	 * @param uid
	 * @param sort
	 * @return
	 */
	public List<Columns> findByUserId(String uid, Sort sort);

	/**
	 * 根据机构id查询
	 * @date Created at 2017年12月28日  下午2:11:03
	 * @Author 谷泽昊
	 * @param sort
	 * @return
	 */
	public List<Columns> findByOrganizationId(Sort sort);
	
	/**
	 * 根据机构id查询
	 * @date Created at 2017年12月28日  下午2:52:32
	 * @Author 谷泽昊
	 * @param organizationId
	 * @return
	 * @throws OperationException 
	 */
	public Object selectColumnByOrganizationId(String organizationId) throws OperationException;
	
	public Object list(IndexTab indexTab,QueryBuilder queryBuilder,QueryBuilder countBuiler,int pagesize,int pageno,String fenlei,String sort,String key,String area) throws TRSException;
	
	public Object arealist(QueryBuilder indexBuilder,QueryBuilder countBuiler,String sort,String area,String source,String timeRange,String keywords) throws TRSException;

	public Object hotKeywordList(QueryBuilder indexBuilder,String sort,String area,String source,String timeRange,String hotKeywords,String keywords) throws OperationException,TRSException;
	
	/**
	 * 分类对比图简单模式分页列表查询
	 * 
	 * @param indexTab
	 * @param key
	 * @return
	 * @throws OperationException
	 */
//	public InfoListResult<IDocument> listContrast(IndexTab indexTab, String key, String source, String[] timeArray, String fuzzyValue, String sort, int pageNo, int pageSize) throws SearchException,TRSException;

	/**
	 * 折线图点击进详情
	 * @date Created at 2018年4月3日  下午2:51:17
	 * @Author 谷泽昊
	 * @param indexTab
	 * @param key
	 * @param source 
	 * @param timeArray
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws TRSException 
	 */
//	public Object listLine(IndexTab indexTab, String key, String source, String sort,String dateTime,String[] timeArray,String fuzzyValue, int pageNo, int pageSize) throws SearchException, TRSException;

	//public void selectDocumentChaos();
	/**
	 * 列表
	 * @param indexTab 三级栏目实体
	 * @return
	 */
	public Object selectList(String indexMapperId,int pageNo,int pageSize,String source,String emotion,String entityType,
			String dateTime,String key,String sort,String area,String irKeyword,String invitationCard,String keywords,String fuzzyValueScope,
			String forwarPrimary,boolean isExport);
	
	/**
	 * 日常监测饼图和柱状图数据导出
	 * @param array
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream exportData(JSONArray array) throws IOException;
	
	/**
	 * 日常监测折线图数据导出
	 * @param array
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream exportChartLine(JSONArray array) throws IOException;

	/**
	 * 日常监测折线图数据导出
	 * @param dataType
	 * @param array
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream exportWordCloud(String dataType,JSONArray array) throws IOException;
	
	/**
	 * 地域图数据导出
	 * @param array
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream exportMap(JSONArray array) throws IOException;
}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月27日 Administrator creat
 */