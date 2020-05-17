package com.trs.netInsight.widget.column.service;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;

import java.util.List;
import java.util.Map;

/**
 * 日常监测栏目映射关系实体操作相关接口
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年9月18日
 *
 */
public interface IIndexTabMapperService {

	/**
	 * 根据id检索
	 * 
	 * @since changjiang @ 2018年9月20日
	 * @param indexTabMapperId
	 * @return
	 * @Return : IndexTabMapper
	 */
	public IndexTabMapper findOne(String indexTabMapperId);

	/**
	 * 根据用户id、共享状态检索列表
	 * 
	 * @since changjiang @ 2018年9月18日
	 * @param userId
	 * @param share
	 * @return
	 * @Return : List<IndexTabMapper>
	 */
	public List<IndexTabMapper> findByUserIdAndShare(String userId, boolean share);

	/**
	 * 根据栏目组id检索栏目映射关系集合
	 * 
	 * @since changjiang @ 2018年10月9日
	 * @param indexPageId
	 * @return
	 * @Return : List<IndexTabMapper>
	 */
	public List<IndexTabMapper> findByIndexPageId(String indexPageId);

	/**
	 * 根据栏目组实体检索栏目映射关系集合
	 * 
	 * @since changjiang @ 2018年10月9日
	 * @param indexPage
	 * @return
	 * @Return : List<IndexTabMapper>
	 */
	public List<IndexTabMapper> findByIndexPage(IndexPage indexPage);

	/**
	 * 根据栏目组实体检索栏目映射关系集合
	 *
	 * @since duhq @ 2019年4月16日
	 * @param indexPage
	 * @return
	 * @Return : List<IndexTabMapper>
	 */
	public List<IndexTabMapper> findByIndexPageOfApp(IndexPage indexPage);

	/**
	 * 统计机构下个用户分别共享栏目数量
	 * 
	 * @since changjiang @ 2018年9月18日
	 * @return
	 * @Return : Map<String,Integer>
	 */
	public List<Map<String, Object>> computeShareMapperNumber();

	/**
	 * 统计机构下共享栏目数量总数
	 * 
	 * @since changjiang @ 2018年10月11日
	 * @return
	 * @Return : long
	 */
	public long computeShareByOrg();

	/**
	 * 保存
	 * 
	 * @since changjiang @ 2018年9月18日
	 * @param mapper
	 * @return
	 * @Return : IndexTabMapper
	 */
	public IndexTabMapper save(IndexTabMapper mapper);

	/**
	 * 修改基础属性
	 * 
	 * @since changjiang @ 2018年9月18日
	 * @param mapper
	 * @return
	 * @Return : IndexTabMapper
	 */
	public IndexTabMapper update(IndexTabMapper mapper);

	/**
	 * 删除关联关系
	 * 
	 * @since changjiang @ 2018年9月18日
	 * @param mapperId
	 * @Return : void
	 */
	public void delete(String mapperId);
	public void deleteByUserId(String userId) throws TRSException;
	/**
	 * 批量删除关联关系
	 * 
	 * @since changjiang @ 2018年9月18日
	 * @param mapperIds
	 * @Return : void
	 */
	public void batchDelete(List<String> mapperIds);

	/**
	 * 批量取消分享
	 * 
	 * @since changjiang @ 2018年9月19日
	 * @param mapperIds
	 * @return
	 * @Return : IndexTabMapper
	 */
	public void unShare(String[] mapperIds) throws TRSException;

	/**
	 * 批量共享
	 * 
	 * @since changjiang @ 2018年9月19日
	 * @param mapperIds
	 * @Return : void
	 */
	public void share(String[] mapperIds) throws TRSException;

	/**
	 * 检索当前机构管理员所有共享栏目组
	 * 
	 * @since changjiang @ 2018年9月20日
	 * @return
	 * @Return : List<IndexPage>
	 */
	public List<IndexPage> searchOrgAdminSharePages();

	/**
	 * 检索指定indexPage下共有多少栏目映射关系
	 * 
	 * @since changjiang @ 2018年9月20日
	 * @param indexPage
	 * @return
	 * @Return : long
	 */
	public long countByIndexPage(IndexPage indexPage);

	/**
	 * 根据栏目映射关系删除栏目
	 * 
	 * @since changjiang @ 2018年10月10日
	 * @param indexMapperId
	 * @Return : void
	 */
	public void deleteMapper(String indexMapperId) throws TRSException;
	
	/**
	 * 修改三级栏目的半栏通栏属性  id和tabWidth都用;分割 顺序一一对应
	 * @param indexMapperId
	 * @param tabWidth 50为半栏，100为通栏
	 */
	public void changeTabWidth(String indexMapperId, String tabWidth);

	/**
	 * 栏目隐藏
	 * 
	 * @since changjiang @ 2018年10月10日
	 * @param indexMapperId
	 * @throws TRSException
	 * @Return : void
	 */
	public void hide(String indexMapperId, String hide) throws TRSException;
	
	
	/**
	 * 删除栏目映射关系
	 * @since changjiang @ 2018年10月17日
	 * @param mapper
	 * @Return : void
	 */
	public void delete(IndexTabMapper mapper);
	
	/**
	 * 根据indexTab检索mapper列表
	 * @param indexTab
	 * @return
	 */
	public List<IndexTabMapper> findByIndexTab(IndexTab indexTab);

	/**
	 * 根据 mapperId和userid查询
	 * @param id
	 * @param userId
	 * @return
	 */
	public IndexTabMapper findByIdAndUserId(String id, String userId);

	/**
	 * 只为迁移历史数据
	 * @param userId
	 * @return
	 */
	public List<IndexTabMapper> findByUserId(String userId);

	/**
	 * 修改历史数据 - 栏目 给indextab类添加导航栏id
	 * @return
	 */
	Object updateHistortIndexTab();
}
