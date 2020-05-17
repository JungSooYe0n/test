package com.trs.netInsight.widget.special.entity.repository;

import com.trs.netInsight.widget.special.entity.SpecialSubject;
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
 * Created by xiaoying on 2017/7/18. 专题
 */
@Repository
public interface SpecialSubjectRepository
		extends PagingAndSortingRepository<SpecialSubject, String>, JpaSpecificationExecutor<SpecialSubject>,JpaRepository<SpecialSubject,String> {

	/**
	 * 根据用户id查询，并排序
	 * 
	 * @date Created at 2017年11月24日 下午3:52:22
	 * @Author 谷泽昊
	 * @param userId
	 * @param sort
	 * @return
	 */
	List<SpecialSubject> findByUserId(String userId, Sort sort);

	/**
	 * 只为历史数据迁移
	 * @param userId
	 * @return
	 */
	List<SpecialSubject> findByUserId(String userId);
	/**
	 * 根据用户id查询并分页
	 * 
	 * @date Created at 2017年11月24日 下午3:52:25
	 * @Author 谷泽昊
	 * @param userId
	 * @param pageable
	 * @return
	 */
	List<SpecialSubject> findByUserId(String userId, Pageable pageable);

	/**
	 * 根据主题id查询
	 * 
	 * @date Created at 2017年11月24日 下午3:52:29
	 * @Author 谷泽昊
	 * @param subjectId
	 * @return
	 */
	List<SpecialSubject> findBySubjectId(String subjectId);

	/**
	 * 根据名字查询
	 * 
	 * @date Created at 2018年1月11日 下午4:01:17
	 * @Author 谷泽昊
	 * @param name
	 * @return
	 */
	List<SpecialSubject> findByName(String name);

	/**
	 * 根据用户id-名字-和标记检索
	 * 
	 * @date Created at 2018年1月16日 下午2:58:06
	 * @Author 谷泽昊
	 * @param userId
	 * @param name
	 * @param flag
	 * @param sort
	 * @return
	 */
	List<SpecialSubject> findByUserIdAndNameLikeAndFlag(String userId, String name, int flag, Sort sort);

	/**
	 * 根据用户id-名字-和标记并且id不为。。。检索
	 * 
	 * @date Created at 2018年1月16日 下午2:58:06
	 * @Author 谷泽昊
	 * @param userId
	 * @param name
	 * @param flag
	 * @param sort
	 * @return
	 */
	List<SpecialSubject> findByUserIdAndNameLikeAndFlagAndIdNotIn(String userId, String name, int flag,
                                                                  Collection<String> collection, Sort sort);

	/**
	 * 查询所有
	 *
	 * @date Created at 2018年1月16日 下午3:00:23
	 * @Author 谷泽昊
	 * @param userId
	 * @param groupId
	 * @param flag
	 * @param sort
	 * @return
	 */
	List<SpecialSubject> findByUserIdAndSubjectIdAndFlag(String userId, String groupId, int flag, Sort sort);

	/**
	 * 查询所有
	 * @param subGroupId 用户分组id
	 * @param groupId
	 * @param flag
	 * @param sort
	 * @return
	 */
	List<SpecialSubject> findBySubGroupIdAndSubjectIdAndFlag(String subGroupId, String groupId, int flag, Sort sort);
	/**
	 * 查询所有 id不为
	 *
	 * @date Created at 2018年1月16日 下午3:00:23
	 * @Author 谷泽昊
	 * @param userId
	 * @param groupId
	 * @param flag
	 * @param sort
	 * @return
	 */
	List<SpecialSubject> findByUserIdAndSubjectIdAndFlagAndIdNotIn(String userId, String groupId, int flag,
                                                                   Collection<String> collection, Sort sort);

	List<SpecialSubject> findByUserIdAndNameContainingAndFlagAndIdNotIn(String userId, String name, int i,
                                                                        Set<String> set, Sort sort);
	List<SpecialSubject> findBySubGroupIdAndNameContainingAndFlagAndIdNotIn(String subGroupId, String name, int i,
                                                                            Set<String> set, Sort sort);

	List<SpecialSubject> findByUserIdAndNameContainingAndFlag(String userId, String name, int i, Sort sort);
	List<SpecialSubject> findBySubGroupIdAndNameContainingAndFlag(String subGroupId, String name, int i, Sort sort);
	/**
	 * 根据id批量查询
	 * @param ids
	 * @return
	 */
	List<SpecialSubject> findByIdIn(Collection<String> ids);
}
