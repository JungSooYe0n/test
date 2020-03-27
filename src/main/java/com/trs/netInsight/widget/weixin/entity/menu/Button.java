/*
 * Project: netInsight
 * 
 * File Created at 2018年1月25日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.weixin.entity.menu;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 菜单基类
 * 
 * @Type Button.java
 * @author 谷泽昊
 * @date 2018年1月25日 上午10:07:05
 * @version
 */
@Getter
@Setter
@ToString
public class Button {
	private String name;// 所有一级菜单、二级菜单都共有一个相同的属性，那就是name
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年1月25日 谷泽昊 creat
 */