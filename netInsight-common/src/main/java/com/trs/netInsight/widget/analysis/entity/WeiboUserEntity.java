package com.trs.netInsight.widget.analysis.entity;

import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.annotation.FtsField;
import lombok.Data;

/**
 * 微博用户实体
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Data
@FtsClient(indices = "dc_sina_users0711")
public class WeiboUserEntity {

	@FtsField("IR_UID")
	private String id;

	/**
	 * 微博名称
	 */
	@FtsField("IR_SCREEN_NAME")
	private String userName;

	/**
	 * 认证信息
	 */
	@FtsField("IR_VERIFIED")
	private String verified;

	/**
	 * 粉丝数量
	 */
	@FtsField("IR_FOLLOWERS_COUNT")
	private String floowersCount;

	/**
	 * 描述信息
	 */

	@FtsField("IR_DESCRIPTION")
	private String description;

	/**
	 * 微博ID
	 */
	@FtsField("IR_UID")
	private String uid;
}
