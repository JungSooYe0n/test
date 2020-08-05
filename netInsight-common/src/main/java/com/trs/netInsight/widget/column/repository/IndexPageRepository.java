package com.trs.netInsight.widget.column.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.trs.netInsight.widget.column.entity.IndexPage;

/**
 * 一二级栏目实体Repository
 *
 * Create by yan.changjiang on 2017年11月27日
 */
@Repository
public interface IndexPageRepository extends JpaRepository<IndexPage, String>,
		PagingAndSortingRepository<IndexPage, String>, JpaSpecificationExecutor<IndexPage> {
	// 通过二级id查找记录
	// List<OneAndTwo> findBySonId(String sonId);

	/**
	 * 根据id获取
	 *
	 * @param id
	 * @return
	 */
	public List<IndexPage> findById(String id);

	/**
	 * 根据父id检索列表
	 *
	 * @param parentId
	 * @return
	 */
	List<IndexPage> findByParentId(String parentId);

	/**
	 * 根据用户id检索列表
	 *
	 * @param userId
	 * @param sort
	 * @return
	 */
	List<IndexPage> findByUserId(String userId, Sort sort);

	/**
	 * 只为迁移历史数据
	 * @param userId
	 * @return
	 */
	List<IndexPage> findByUserIdAndSubGroupIdIsNull(String userId);
	List<IndexPage> findByUserId(String userId);
	/**
	 * 根据用户分组id检索列表
	 * @param subGroupId
	 * @param sort
	 * @return
	 */
	List<IndexPage> findBySubGroupId(String subGroupId, Sort sort);
	/**
	 * 根据机构id查询
	 *
	 * @date Created at 2017年12月28日 下午2:18:34
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param sort
	 * @return
	 */
	List<IndexPage> findByOrganizationId(String organizationId, Sort sort);
	/**
	 * 根据机构id查询
	 *
	 * @date Created at 2017年12月28日 下午2:18:34
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param
	 * @return
	 */
	List<IndexPage> findByOrganizationId(String organizationId);
	/**
	 * 通过自定义栏目id查询以及栏目
	 *
	 * @param typeId
	 * @return
	 */
	List<IndexPage> findByTypeId(String typeId);

	/**
	 * 检索属于默认导航分组下栏目组（超管、机构管理员、运维）
	 *
	 * @since changjiang @ 2018年10月11日
	 * @param userId
	 * @return
	 * @Return : List<IndexPage>
	 */
	List<IndexPage> findByUserIdAndTypeId(String userId, String typeId);

	/**
	 * 检索属于默认导航分组下栏目组（普通用户）
	 * @param subGroupId
	 * @param typeId
	 * @return
	 */
	List<IndexPage> findBySubGroupIdAndTypeId(String subGroupId, String typeId);
	/**
	 * @since duhq @ 2019/05/08
	 * @Return : List<IndexPage>
	 */
	List<IndexPage> findByUserIdAndTypeIdOrderBySequence(String userId, String typeId);
	List<IndexPage> findBySubGroupIdAndTypeIdOrderBySequence(String subGroupId, String typeId);
	/**
	 * 根据id及用户id检索
	 *
	 * @since changjiang @ 2018年7月17日
	 * @param id
	 * @param userId
	 * @return
	 * @Return : IndexPage
	 */
	public IndexPage findByIdAndUserId(String id, String userId);
	public IndexPage findByIdAndSubGroupId(String id, String subGroupId);

	/**
	 * 根据用户id及共享状态检索分组
	 *
	 * @since changjiang @ 2018年9月20日
	 * @param userId
	 *            用户id
	 * @param share
	 *            分享状态
	 * @return
	 * @Return : List<IndexPage>
	 */
	public List<IndexPage> findByUserIdAndShare(String userId, boolean share);

	/**
	 * 根据id包含查询
	 * @param ids
	 * @param sort
	 * @return
	 */
	public List<IndexPage> findByIdIn(Collection<String> ids,Sort sort);
	public List<IndexPage> findByIdIn(Collection<String> ids);
}
