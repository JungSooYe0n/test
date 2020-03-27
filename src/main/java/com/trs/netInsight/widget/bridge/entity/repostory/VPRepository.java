package com.trs.netInsight.widget.bridge.entity.repostory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.trs.netInsight.widget.bridge.entity.VirtualProps;
import com.trs.netInsight.widget.bridge.entity.enums.PropsType;

/**
 * VPRepository虚拟道具库
 *
 * Created by ChangXiaoyang on 2017/9/14.
 */
public interface VPRepository extends PagingAndSortingRepository<VirtualProps, String>,JpaSpecificationExecutor<VirtualProps> {

	/**
	 * 根据用户Id查询
	 * @date Created at 2017年12月28日  下午2:04:24
	 * @Author 谷泽昊
	 * @param userId
	 * @return
	 */
    List<VirtualProps> findByUserId(String userId);
    
    /**
     * 根据机构id查询
     * @date Created at 2017年12月28日  下午2:04:53
     * @Author 谷泽昊
     * @param organizationId
     * @return
     */
    List<VirtualProps> findByOrganizationId(String organizationId);

    /**
     * 根据机构id
     * @date Created at 2017年12月28日  下午2:05:11
     * @Author 谷泽昊
     * @param organizationId
     * @param propsType
     * @return
     */
    Optional<VirtualProps> findByOrganizationIdAndPropsType(String organizationId, PropsType propsType);
    
    /**
     * 根据用户id
     * @date Created at 2017年12月28日  下午2:05:33
     * @Author 谷泽昊
     * @param userId
     * @param propsType
     * @return
     */
    Optional<VirtualProps> findByUserIdAndPropsType(String userId, PropsType propsType);

}