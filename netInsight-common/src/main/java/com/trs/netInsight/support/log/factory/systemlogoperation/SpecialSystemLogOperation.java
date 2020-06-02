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
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.SpecialSubject;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 专题日志操作实现类
 * 
 * @Type SpecialSystemLogOperation.java
 * @author 谷泽昊
 * @date 2018年11月7日 下午5:53:40
 * @version
 */
public class SpecialSystemLogOperation extends AbstractSystemLogOperation {

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
		 * 添加主题
		 */
		case SPECIAL_ADD_SUBJECT:
			/**
			 * 添加专题
			 */
		case SPECIAL_ADD_ZHUAN:
			/**
			 * 专题重命名
			 */
		case SPECIAL_RENAME_SPECIAL:
			/**
			 * 主题重命名
			 */
		case SPECIAL_RENAME_SUBJECT:
			/**
			 * 删除主题
			 */
		case SPECIAL_DELETE_SUBJECT:
			/**
			 * 删除专题
			 */
		case SPECIAL_DELETE_ZHUANTI:
			if (StringUtils.isNotBlank(id)) {
				SpecialSubject specialSubject = specialSubjectService.findOne(id);
				if (specialSubject != null) {
					buffer.insert(0, specialSubject.getName());
					String subjectId = specialSubject.getSubjectId();
					if (StringUtils.isNotBlank(subjectId)) {
						SpecialSubject specialSubject2 = specialSubjectService.findOne(subjectId);
						if (specialSubject2 != null) {
							buffer.insert(0, "/");
							buffer.insert(0, specialSubject2.getName());
						}
					}
				}
			}
			break;

		/**
		 * 添加专项
		 */
		case SPECIAL_ADD:
			if (StringUtils.isNotBlank(id)) {
				SpecialSubject specialSubject = specialSubjectService.findOne(id);
				if (specialSubject != null) {
					buffer.insert(0, specialSubject.getName());
					String subjectId = specialSubject.getSubjectId();
					if (StringUtils.isNotBlank(subjectId)) {
						SpecialSubject specialSubject2 = specialSubjectService.findOne(subjectId);
						if (specialSubject2 != null) {
							buffer.insert(0, "/");
							buffer.insert(0, specialSubject2.getName());
						}
					}
				}
			}
			break;
		/**
		 * 专项置顶
		 */
		case SPECIAL_TOP_FLAG:
			/**
			 * 取消专项置顶
			 */
		case SPECIAL_NO_TOP_FLAG:

			/**
			 * 修改专项
			 */
		case SPECIAL_UPDATE:
			/**
			 * 删除专项
			 */
		case SPECIAL_DELETE:
			/**
			 * 查询专题数据列表
			 */
		case SPECIAL_SELECT_ZHUANTI_LIST:
			/**
			 * 查询专题分析 / 内容统计 / 来源类型统计
			 */
		case SPECIAL_SELECT_ZHUANTI_WEBCOUNT:
			/**
			 * 查询专题分析 / 内容统计 / 微博top5
			 */
		case SPECIAL_SELECT_ZHUANTI_TOP5:
			/**
			 * 查询专题分析 / 内容统计 / 地域分布
			 */
		case SPECIAL_SELECT_ZHUANTI_AREA:
			/**
			 * 查询专题分析 / 内容统计 / 媒体活跃等级
			 */
		case SPECIAL_SELECT_ZHUANTI_ACTIVE_LEVEL:
			/**
			 * 查询专题分析 / 内容统计 / 微博情感分析
			 */
		case SPECIAL_SELECT_ZHUANTI_EMOTIONOPTION:
			/**
			 * 查询专题分析 / 事件趋势 / 事件溯源
			 */
		case SPECIAL_SELECT_ZHUANTI_TRENDTIME:
			/**
			 * 查询专题分析 / 事件趋势 / 信息走势图 / 信息走势
			 */
		case SPECIAL_SELECT_ZHUANTI_TRENDMESSAGE:
			/**
			 * 查询专题分析 / 事件趋势 / 信息走势图 / 网民参与趋势
			 */
		case SPECIAL_SELECT_ZHUANTI_NETTENDENCY:
			/**
			 * 查询专题分析 / 事件趋势 / 信息走势图 / 媒体参与趋势
			 */
		case SPECIAL_SELECT_ZHUANTI_METATENDENCY:
			/**
			 * 查询专题分析 / 事件趋势 / 新闻传播分析
			 */
		case SPECIAL_SELECT_ZHUANTI_NEWSSITEANALYSIS:
			/**
			 * 查询专题分析 / 事件趋势 / 引爆点
			 */
		case SPECIAL_SELECT_ZHUANTI_TIPPINGPOINT:
			/**
			 * 查询专题分析 / 事件趋势 / 情感走势
			 */
		case SPECIAL_SELECT_ZHUANTI_VOLUME:
			/**
			 * 查询专题分析 / 事件趋势 / 事件溯源2
			 */
		case SPECIAL_SELECT_ZHUANTI_TRENDMD5:
			/**
			 * 查询专题分析 / 事件分析 / 热词探索
			 */
		case SPECIAL_SELECT_ZHUANTI_TOPICEVOEXPLOR:
			/**
			 * 查询专题分析 / 事件分析 / 网友观点
			 */
		case SPECIAL_SELECT_ZHUANTI_USERVIEWS:
			/**
			 * 查询专题分析 / 事件分析 / 词云
			 */
		case SPECIAL_SELECT_ZHUANTI_WORDCLOUD:

			if (StringUtils.isNotBlank(id)) {
				SpecialProject specialProject = specialProjectService.findOne(id);
				if (specialProject != null) {
					// 添加
					buffer.insert(0, specialProject.getSpecialName());

					String groupId = specialProject.getGroupId();
					if (StringUtils.isNotBlank(groupId)) {
						SpecialSubject specialSubject = specialSubjectService.findOne(groupId);
						if (specialSubject != null) {
							buffer.insert(0, "/");
							buffer.insert(0, specialSubject.getName());
							String subjectId = specialSubject.getSubjectId();
							if (StringUtils.isNotBlank(subjectId)) {
								SpecialSubject specialSubject2 = specialSubjectService.findOne(subjectId);
								if (specialSubject2 != null) {
									buffer.insert(0, "/");
									buffer.insert(0, specialSubject2.getName());
								}
							}
						}
					}
				}
			}

			// 词云专用
			String[] entityTypes = parameterMap.get("entityType");
			if (entityTypes != null && entityTypes.length > 0) {
				String entityType = entityTypes[0];
				entityType = entityTypeMap.get(entityType);
				if (StringUtils.isNotBlank(entityType)) {
					Map<String, String[]> entityTypeMap = new HashMap<>();
					entityTypeMap.putAll(parameterMap);
					entityTypeMap.put("entityType", new String[] { entityType });
					parameterMap=entityTypeMap;
				}
			}

			break;
		default:
			break;
		}

		newStr = buffer.toString();
		String replace = StringUtils.replace(operationPosition, oldStr, newStr);

		return StringUtil.getKeyNameFromParam(replace, parameterMap, "@");
	}

	/**
	 * 词云专用map
	 */
	private Map<String, String> entityTypeMap = new HashMap<String, String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put("location", "地域词云");
			put("agency", "机构词云");
			put("people", "人物词云");
			put("keywords", "通用词云");
		}
	};
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