package com.trs.netInsight.widget.column.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.analysis.entity.ClassInfo;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;

/**
 * 栏目映射关系实体持久层接口
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年9月17日
 *
 */
@Repository
public interface IndexTabMapperRepository extends JpaRepository<IndexTabMapper, String>, JpaSpecificationExecutor<IndexTabMapper> {

	/**
	 * 根据机用户id、共享关系检索映射
	 *
	 * @since changjiang @ 2018年9月18日
	 * @param userId
	 *            用户id
	 * @param share
	 *            是否共享
	 * @param sort
	 *            排序
	 * @return
	 * @Return : List<IndexTabMapper>
	 */
	public List<IndexTabMapper> findByUserIdAndShareAndIsMeAndOrganizationId(String userId, boolean share, boolean isMe,String orgId, Sort sort);

	/**
	 * 根据栏目组id判断之下是否存在共享栏目
	 *
	 * @since changjiang @ 2018年9月19日
	 * @return
	 * @Return : boolean
	 */
	public boolean existsByIndexPageAndShareAndIsMe(IndexPage indexPage, boolean share, boolean isMe);

	/**
	 * 根据id集合删除
	 *
	 * @since changjiang @ 2018年9月19日
	 * @param mapperIds
	 * @Return : void
	 */
	@Modifying
	@Query(value = "DELETE IndexTabMapper i WHERE i.id IN (:mapperIds)")
	public void delete(List<String> mapperIds);

	/**
	 * 批量共享\取消共享
	 *
	 * @since changjiang @ 2018年9月19日
	 * @param mapperIds
	 * @Return : void
	 */
	@Modifying
	@Query(value = "UPDATE IndexTabMapper i SET i.share = (:share) WHERE i.id IN (:mapperIds)")
	public void updateShareStausByIds(List<String> mapperIds, boolean share);

	/**
	 * 批量修改隐藏状态
	 *
	 * @since changjiang @ 2018年10月10日
	 * @param mapperIds
	 * @param hide
	 * @Return : void
	 */
	@Modifying
	@Query(value = "UPDATE IndexTabMapper i SET i.hide = (:hide) WHERE i.id IN (:mapperIds)")
	public void updateHideByIds(List<String> mapperIds, boolean hide);

	/**
	 * 分类统计机构下各用户分别共享栏目数量
	 *
	 * @since changjiang @ 2018年9月18日
	 * @param orgId
	 *            机构id
	 * @return
	 * @Return : Map<String,Integer>
	 */
	@Query(value = "SELECT  new com.trs.netInsight.widget.analysis.entity.ClassInfo(i.userId, COUNT(*)) FROM IndexTabMapper i WHERE i.organizationId = (:orgId) AND i.share=TRUE AND i.isMe=TRUE GROUP BY i.userId")
	public List<ClassInfo> computeCountByOrgIdGroupByUserId(@Param(value = "orgId") String orgId);

	/**
	 * 分类统计用户分组下各用户分别共享栏目数量
	 * @param orgId
	 * @return
	 */
	@Query(value = "SELECT  new com.trs.netInsight.widget.analysis.entity.ClassInfo(i.userId, COUNT(*)) FROM IndexTabMapper i WHERE i.subGroupId = (:subGroupId) AND i.share=TRUE AND i.isMe=TRUE GROUP BY i.userId")
	public List<ClassInfo> computeCountBysubGroupIdGroupByUserId(@Param(value = "subGroupId") String orgId);

	/**
	 * 根据机构id检索共享栏目总量
	 * @since changjiang @ 2018年10月11日
	 * @param orgId
	 * @return
	 * @Return : long
	 */
	@Query(value = "SELECT COUNT(*) FROM IndexTabMapper i WHERE i.organizationId = (:orgId) AND i.share=TRUE AND i.isMe=TRUE")
	public long computeShareTotalNumByOrg(String orgId);

	/**
	 * 根据机构id检索共享栏目总量
	 *
	 * @since changjiang @ 2018年10月15日
	 * @param organizationId
	 * @param isMe
	 * @param share
	 * @return
	 * @Return : long
	 */
	public long countByOrganizationIdAndIsMeAndShare(String organizationId, boolean isMe, boolean share);

	/**
	 * 根据机构id检索共享栏目数量，并且除去自身
	 *
	 * @since changjiang @ 2018年10月17日
	 * @param organizationId
	 * @param isMe
	 * @param share
	 * @param userId
	 * @return
	 * @Return : long
	 */
	public long countByOrganizationIdAndIsMeAndShareAndUserIdNot(String organizationId, boolean isMe, boolean share,
																 String userId);

	/**
	 * 统计指定分组下栏目数量
	 *
	 * @since changjiang @ 2018年9月20日
	 * @param indexPage
	 * @return
	 * @Return : long
	 */
	public long countByIndexPage(IndexPage indexPage);

	/**
	 * 根据栏目组加载栏目映射关系
	 *
	 * @since changjiang @ 2018年10月9日
	 * @param indexPage
	 * @return
	 * @Return : List<IndexTabMapper>
	 */
	public List<IndexTabMapper> findByIndexPage(IndexPage indexPage);

	/**
	 * 根据栏目组加载栏目映射关系
	 *
	 * @since duhq 2019年4月16日
	 * @param indexPageId
	 * @return
	 * @Return : List<IndexTabMapper>
	 */
	public List<IndexTabMapper> findByIndexPageId(String indexPageId, Sort sort);


	/**
	 * 根据栏目实体加载
	 * @param indexTab
	 * @return
	 */
	public List<IndexTabMapper> findByIndexTab(IndexTab indexTab);

	/**
	 * 检索栏目组下自身所有的共享栏目（非引用）
	 * @param indexPage
	 * @param share
	 * @return
	 */
	public List<IndexTabMapper> findByIndexPageAndShareAndIsMe(IndexPage indexPage, boolean share, boolean isMe);

	/**
	 * 根据mapperid和拥有者id查询
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
	public List<IndexTabMapper> findByUserIdAndSubGroupIdIsNull(String userId);

	/**
	 * 根据模块id查找对应栏目
	 * @param typeId
	 * @return
	 */
	List<IndexTabMapper> findByTypeId(String typeId);
}
