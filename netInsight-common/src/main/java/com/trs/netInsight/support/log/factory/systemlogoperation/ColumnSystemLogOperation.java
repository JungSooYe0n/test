/*
 * Project: netInsight
 * 
 * File Created at 2018年11月7日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.log.factory.systemlogoperation;

import com.trs.netInsight.support.log.entity.enums.SystemLogOperation;
import com.trs.netInsight.support.log.factory.AbstractSystemLogOperation;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.NavigationConfig;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 日常监测日志实现类
 * 
 * @Type ColumnSystemLogOperation.java
 * @author 谷泽昊
 * @date 2018年11月7日 下午5:53:40
 * @version
 */
public class ColumnSystemLogOperation extends AbstractSystemLogOperation {

	@Override
	public String getOperationPosition(Map<String, String[]> parameterMap, String operationPosition,
			SystemLogOperation systemLogOperation) {
		// 获取id的值
		String id = getValueByParamAndKey(operationPosition, parameterMap);
		// 获取key
		String oldStr = getKey(operationPosition);
		String newStr = null;
		StringBuffer buffer = new StringBuffer();
		switch (systemLogOperation) {
		/**
		 * 添加自定义导航栏
		 */
		case COLUMN_ADD_NAVIGATION:
			/**
			 * 删除自定义导航栏
			 */
		case COLUMN_DELETE_NAVIGATION:
			/**
			 * 拖拽自定义导航栏
			 */
		case COLUMN_MOVE_NAVIGATION:
			/**
			 * 修改自定义导航栏
			 */
		case COLUMN_UPDATE_NAVIGATION:
			/**
			 * 隐藏显示自定义导航栏
			 */
		case COLUMN_HIDE_OR_SHOW_NAVI:
			if (StringUtils.isNotEmpty(id)) {
				NavigationConfig navigationConfig = navigationService.findOne(id);
				if (navigationConfig != null) {
					buffer.insert(0, navigationConfig.getName());
				}
			}
			break;
		/**
		 * 添加一级栏目
		 */
		case COLUMN_ADD_INDEX_PAGE:
			if (StringUtils.isNotBlank(id)) {
				NavigationConfig navigationConfig = navigationService.findOne(id);
				if (navigationConfig != null) {
					buffer.insert(0, navigationConfig.getName());
				}
			}
			break;
		/**
		 * 修改一级栏目
		 */
		case COLUMN_UPDATE_INDEX_PAGE:
			/**
			 * 删除一级栏目
			 */
		case COLUMN_DELETE_INDEX_PAGE:
			/**
			 * 隐藏一级栏目
			 */
		case COLUMN_HIDE_INDEX_PAGE:
			/**
			 * 拖拽一级栏目
			 */
		case COLUMN_MOVE_ONE:
			if (StringUtils.isNotBlank(id)) {
				IndexPage indexPage = indexPageService.findOne(id);
				if (indexPage != null) {
					buffer.insert(0, indexPage.getName());
					String typeId = indexPage.getTypeId();
					if (StringUtils.isNotBlank(typeId)) {
						NavigationConfig navigationConfig = navigationService.findOne(typeId);
						if (navigationConfig != null) {
							buffer.insert(0, "/");
							buffer.insert(0, navigationConfig.getName());
						}
					}
				}
			}
			break;
		/**
		 * 添加二级栏目（图表）
		 */
		case COLUMN_ADD_INDEX_TAB:
			if (StringUtils.isNotBlank(id)) {
				IndexPage indexPage = indexPageService.findOne(id);
				if (indexPage != null) {
					buffer.insert(0, indexPage.getName());
					String typeId = indexPage.getTypeId();
					if (StringUtils.isNotBlank(typeId)) {
						NavigationConfig navigationConfig = navigationService.findOne(typeId);
						if (navigationConfig != null) {
							buffer.insert(0, "/");
							buffer.insert(0, navigationConfig.getName());
						}
					}
				}
			}
			break;
		/**
		 * 拖拽二级栏目
		 */
		case COLUMN_MOVE_TWO:
			/**
			 * 修改二级栏目（图表）
			 */
		case COLUMN_UPDATE_INDEX_TAB:
			/**
			 * 删除二级栏目（图表）
			 */
		case COLUMN_DELETE_INDEX_TAB:
			/**
			 * 隐藏二级栏目（图表）
			 */
		case COLUMN_HIDE_INDEX_TAB:
			/**
			 * 查看二级栏目（图表）数据
			 */
		case COLUMN_SELECT_INDEX_TAB_DATA:
			if (StringUtils.isNotBlank(id)) {
				IndexTabMapper one = indexTabMapperService.findOne(id);
				if (one != null) {
					buffer.insert(0, one.getIndexTab().getName());
					String indexPageId = one.getIndexTab().getParentId();
					if (StringUtils.isNotBlank(indexPageId)) {
						IndexPage indexPage = indexPageService.findOne(indexPageId);
						if (indexPage != null) {
							buffer.insert(0, "/");
							buffer.insert(0, indexPage.getName());
							String typeId = indexPage.getTypeId();
							if (StringUtils.isNotBlank(typeId)) {
								NavigationConfig navigationConfig = navigationService.findOne(typeId);
								if (navigationConfig != null) {
									buffer.insert(0, "/");
									buffer.insert(0, navigationConfig.getName());
								}
							}
						}
					}
				}
			}
			break;
			/**
			 * 查询栏目的信息列表数据
			 */
			case COLUMN_SELECT_INDEX_TAB_INFO:

			/**
			 * 添加自定义图表
			 */
			case COLUMN_ADD_CUSTOM_CHART:
			/**
			 * 修改自定义图表
			 */
			case COLUMN_UPDATE_CUSTOM_CHART:
			/**
			 * 删除自定义图表
			 */
			case COLUMN_DELETE_CUSTOM_CHART:

			/**
			 * 查询栏目下对应的统计分析 + 自定义图表
			 */
			case COLUMN_SELECT_TAB_CHART:
			/**
			 * 查询分类下的栏目和被置顶的统计分析 + 自定义图表
			 */
			case COLUMN_SELECT_PAGE_TOP_CHART:
			/**
			 * 置顶图表
			 */
			case COLUMN_TOP_CHART:

			default:
				break;
		}

		newStr = buffer.toString();
		String replace = StringUtils.replace(operationPosition, oldStr, newStr);
		return StringUtil.getKeyNameFromParam(replace, parameterMap, "@");
	}

}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年11月7日 谷泽昊 creat
 */