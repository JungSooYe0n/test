package com.trs.netInsight.widget.special.entity.repository;

import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * 专项数据访问
 *
 * Created by ChangXiaoyang on 2017/2/15.
 */
@Repository   
public interface SpecialProjectRepository extends PagingAndSortingRepository<SpecialProject, String>,JpaSpecificationExecutor<SpecialProject> ,JpaRepository<SpecialProject,String> {
	List<SpecialProject> findByUserId(String userId);
    List<SpecialProject> findByUserId(String userId, Sort sort);
    List<SpecialProject> findAllByUserId(String userId, Sort sort);
	List<SpecialProject> findAllBySubGroupId(String subGroupId, Sort sort);
    List<SpecialProject> findByUserId(String userId, Pageable pageable);

    List<SpecialProject> findBySpecialNameContains(String name);
    
    List<SpecialProject> findByGroupId(String id);

    /**
     * 根据机构id查询
     * @date Created at 2017年12月28日  下午2:15:19
     * @Author 谷泽昊
     * @param organizationId
     * @param pageable
     * @return
     */
	List<SpecialProject> findByOrganizationId(String organizationId, Pageable pageable);

	/**
	 * 根据机构id查询
	 * @date Created at 2017年12月28日  下午2:32:18
	 * @Author 谷泽昊
	 * @param organizationId
	 * @param sort
	 * @return
	 */
	List<SpecialProject> findByOrganizationId(String organizationId, Sort sort);


	/**
	 * 根据用户id和
	 * @date Created at 2018年1月16日  下午3:01:47
	 * @Author 谷泽昊
	 * @param userId
	 * @param groupId
	 * @param sort
	 * @return
	 */
	List<SpecialProject> findByUserIdAndGroupId(String userId, String groupId, Sort sort);

	/**
	 * 根据用户分组id和
	 * @param subGroupId 用户分组id
	 * @param groupId
	 * @param sort
	 * @return
	 */
	List<SpecialProject> findBySubGroupIdAndGroupId(String subGroupId, String groupId, Sort sort);
	/**
	 * 根据用户id和名字模糊检索
	 * @date Created at 2018年1月16日  下午3:05:54
	 * @Author 谷泽昊
	 * @param userId
	 * @param name
	 * @param sort
	 * @return
	 */
	List<SpecialProject> findByUserIdAndSpecialNameLike(String userId, String name, Sort sort);

	/**
	 * id不为。。的检索
	 * @date Created at 2018年1月16日  下午3:27:29
	 * @Author 谷泽昊
	 * @param userId
	 * @param name
	 * @param list
	 * @param sort
	 * @return
	 */
	List<SpecialProject> findByUserIdAndSpecialNameLikeAndIdNotIn(String userId, String name, Collection<String> list,
                                                                  Sort sort);

	/**
	 * 查询
	 * @date Created at 2018年1月18日  下午2:04:47
	 * @Author 谷泽昊
	 * @param userId
	 * @param set
	 * @param topFlag
	 * @param sort
	 * @return
	 */
	List<SpecialProject> findByUserIdAndSpecialNameLikeAndIdNotInAndTopFlagNot(String userId, String name,
                                                                               Collection<String> set, String topFlag, Sort sort);

	/**
	 * 查询
	 * @date Created at 2018年1月18日  下午2:04:57
	 * @Author 谷泽昊
	 * @param userId
	 * @param topFlag
	 * @param sort
	 * @return
	 */
	List<SpecialProject> findByUserIdAndSpecialNameLikeAndTopFlagNot(String userId, String name, String topFlag,
                                                                     Sort sort);

	/**
	 * 查询
	 * @date Created at 2018年1月18日  下午2:10:58
	 * @Author 谷泽昊
	 * @param userId
	 * @param id
	 * @param topFlag
	 * @param sort
	 * @return
	 */
	List<SpecialProject> findByUserIdAndGroupIdAndTopFlagNot(String userId, String id, String topFlag, Sort sort);

	List<SpecialProject> findByUserIdAndSpecialNameContainingAndIdNotIn(String userId, String name, Set<String> set,
                                                                        Sort sort);
	List<SpecialProject> findBySubGroupIdAndSpecialNameContainingAndIdNotIn(String subGroupId, String name, Set<String> set,
                                                                            Sort sort);

	List<SpecialProject> findByUserIdAndSpecialNameContaining(String userId, String string, Sort sort);
	List<SpecialProject> findBySubGroupIdAndSpecialNameContaining(String subGroupId, String string, Sort sort);
	List<SpecialProject> findByIdIn(Collection<String> ids);

	List<SpecialProject> findBySubGroupId(String subGroupId);

	List<SpecialProject> findBySpecialType(SpecialType specialType);
}
