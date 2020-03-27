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
 * 整个菜单对象的封装
 * @Type Menu.java
 * @author 谷泽昊
 * @date 2018年1月25日 上午10:09:25
 * @version
 */
@Getter
@Setter
@ToString
public class Menu {
	private Button[] button;

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