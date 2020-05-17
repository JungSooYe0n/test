package com.trs.netInsight.widget.weixin.entity.login;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 微信登录绑定实体
 * @author xiaoying
 *
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "weixin_login")
public class Weixinlogin extends BaseEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 微信openId
	 */
	@Column(name="`OPENID`")
	private String openId;
	
	/**
	 * 微信昵称
	 */
	@Column(name="`NICKNAME`")
	private String nickname;
	
	public Weixinlogin(String openId,String userId,String userName){
		this.openId = openId;
		super.setUserId(userId);
		super.setUserAccount(userName);
	}

}
