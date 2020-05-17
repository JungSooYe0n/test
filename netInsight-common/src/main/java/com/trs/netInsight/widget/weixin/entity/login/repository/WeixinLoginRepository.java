package com.trs.netInsight.widget.weixin.entity.login.repository;

import com.trs.netInsight.widget.weixin.entity.login.Weixinlogin;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 微信登录绑定
 * @author xiaoying
 *
 */
@Repository   
public interface WeixinLoginRepository extends PagingAndSortingRepository<Weixinlogin, String> {

	List<Weixinlogin> findByOpenId(String openId);
	
	List<Weixinlogin> findByUserAccount(String userAccount);
}
