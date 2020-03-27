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
 * 子菜单项 :没有子菜单的菜单项，有可能是二级菜单项，也有可能是不含二级菜单的一级菜单。
 * 
 * @Type CommonButton.java
 * @author 谷泽昊
 * @date 2018年1月25日 上午10:08:02
 * @version
 */
@Getter
@Setter
@ToString
public class CommonButton extends Button {
	private String type;
	private String key;
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