package com.trs.netInsight.widget.column.repository;

import com.trs.netInsight.widget.column.entity.IndexTab;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 三级栏目（图）实体Repository
 *
 * Create by yan.changjiang on 2017年11月27日
 */
@Repository
public interface IndexTabRepository
		extends PagingAndSortingRepository<IndexTab, String>, JpaSpecificationExecutor<IndexTab>,JpaRepository<IndexTab, String> {

	/**
	 * 通过一级栏目id找下一级栏目
	 * 
	 * @param parentId
	 *            一级栏目id
	 * @return
	 */
	public List<IndexTab> findByParentId(String parentId);

	/**
	 * 根据父级id检索列表
	 * 
	 * @param userId
	 * @return
	 */
	public List<IndexTab> findByUserId(String userId);
	public List<IndexTab> findByUserIdAndSubGroupIdIsNull(String userId);
	/**
	 * 根据机构id查询
	 * @date Created at 2017年12月28日  下午2:38:00
	 * @Author 谷泽昊
	 * @param organizationId
	 * @return
	 */
	public List<IndexTab> findByOrganizationId(String organizationId);
	
	/**
	 * 根据pageId以及资源拥有者id检索tab列表
	 * @since changjiang @ 2018年7月17日
	 * @param parentId
	 * @param userId
	 * @param sort
	 * @return
	 * @Return : List<IndexTab>
	 */
	public List<IndexTab> findByParentIdAndUserId(String parentId, String userId, Sort sort);
	public List<IndexTab> findByParentIdAndSubGroupId(String parentId, String subGroupId, Sort sort);
	
	/**
	 * 根据id以及资源拥有者id检索
	 * @since changjiang @ 2018年7月17日
	 * @param id
	 * @param userId
	 * @return
	 * @Return : IndexTab
	 */
	public IndexTab findByIdAndUserId(String id, String userId);
	public IndexTab findByIdAndSubGroupId(String id, String subGroupId);

	/**
	 * 查找并排序
	 * @date Created at 2018年12月20日  上午3:52:56
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param id
	 * @param sort
	 * @return
	 */
	public List<IndexTab> findByParentId(String id, Sort sort);

	/**
	 * 根据用户分组id查询
	 * @param subGroupId
	 * @return
	 */
	public List<IndexTab> findBySubGroupId(String subGroupId);

	public List<IndexTab> findByTypeAndGroupName(String type, String groupName);
	public List<IndexTab> findByKeyWordIsNotNullAndKeyWordIndexIsNotNullAndTrslIsNull();

	public List<IndexTab> findByKeyWordNotLikeAndKeyWordIsNotNull(String keyWord);


}
