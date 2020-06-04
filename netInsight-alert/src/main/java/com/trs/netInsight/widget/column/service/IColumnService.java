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

import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTabType;
import com.trs.netInsight.widget.column.entity.emuns.ColumnFlag;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
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
	 * 获取层级下最的排序值
	 * @param parentPageId 要获取排序值的对象的上级分组id ，如果没有上级分组，则为空
	 * @param navigationId 对应的模块id，默认日常监测模块为""，
	 * @param user 当前用户信息
	 * @return
	 */
	Integer getMaxSequenceForColumn(String parentPageId,String navigationId,User user);

	/**
	 * 对mapper和page进行排序
	 * @param mapperList
	 * @param indexPageList
	 * @param sortAll 是否包括对子层级排序
	 * @return
	 */
	List<Object> sortColumn(List<IndexTabMapper> mapperList, List<IndexPage> indexPageList, Boolean sortAll,Boolean sortPage);

	/**
	 * 获取日常监测第一层级的栏目和分组
	 * @param typeId
	 * @param loginUser
	 * @return
	 */
	Map<String,Object> getOneLevelColumnForMap(String typeId,User loginUser);
	/**
	 * 获取日常监测第一层级的栏目和分组
	 * @param typeId
	 * @param user
	 * @return
	 */
	Object getOneLevelColumn(String typeId,User user);

	/**
	 * 重新排序column，在删除一个分组或者栏目时，去掉原栏目的排序
	 * @param moveId  被删除的对象的id
	 * @param flag 标识是栏目还是分组  分组为 0  栏目为1
	 * @param user
	 * @return
	 */
	Object moveSequenceForColumn(String moveId, ColumnFlag flag, User user) throws OperationException;

	/**
	 * 移动日常监测中的栏目或者分组信息
	 * @param data 排序后的数据 包括类型和id数据
	 * @param moveData 排序后的数据 包括类型和id数据
	 * @param parentId  父分组id
	 * @return
	 */
	Object moveIndexSequence(String data,String moveData,String parentId,User user)throws OperationException;

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
	public List<Object> selectColumn(User user,String typeId) throws OperationException;

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
	 * 列表
	 * @param
	 * @return
	 */
	public Object selectList(IndexTab indexTab,int pageNo,int pageSize,String source,String emotion,String entityType,
							 String dateTime,String key,String sort,String invitationCard,
							 String forwarPrimary,String keywords,String fuzzyValueScope,String read,String mediaLevel,String mediaIndustry,String contentIndustry,String filterInfo,
							 String contentArea,String mediaArea,String preciseFilter);
	/**
	 * 日常监测 图数据导出
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public ByteArrayOutputStream exportChartData(String data, IndexTabType indexTabType) throws IOException;
}

/**
 * Revision history
 * -------------------------------------------------------------------------
 *
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月27日 Administrator creat
 */