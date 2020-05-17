package com.trs.netInsight.widget.report.entity;

import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 素材库（表中的每一条数据可以理解为一种检索表达式） Created by xiaoying on 2017年5月3日
 */
@SuppressWarnings("serial")
@Getter
@Setter
@Entity
@Table(name = "report_material_library")
public class MaterialLibrary extends BaseEntity {

	/**
	 * 素材库名称
	 */
	@Column(name = "library_name")
	private String libraryName;

	/**
	 * 关联的专项监测ID
	 */
	@Column(name = "special_id")
	private String specialId;

	/**
	 * 素材库来源（0:专项监测，1:舆情报告）
	 */
	@Column(name = "source")
	private int source;

	/**
	 * 搜索来源 微博微信国内新闻
	 */
	private String groupName;
	/**
	 * 素材库状态（0：尚未选择素材，1： 已生选择素材） 作用是：在专项修改时 如果1状态 则不进行修改
	 */
	@Column(name = "status")
	private int status;

	/**
	 * 表达式选择模式（0：普通模式，1：专家模式）
	 */
	@Column(name = "mode")
	private int mode;

	/**
	 * 全部关键词
	 */
	@Column(name = "all_keyword", columnDefinition = "TEXT")
	private String allKeyword;

	/**
	 * 任意关键词
	 */
	@Column(name = "any_keyword", columnDefinition = "TEXT")
	private String anyKeyword;

	/**
	 * 排除词
	 */
	@Column(name = "exclude_keyword", columnDefinition = "TEXT")
	private String excludeKeyword;

	/**
	 * 关键词位置
	 */
	@Column(name = "keywords_location")
	private SearchScope keywordsLocation;

	/**
	 * 检索开始时间
	 */
	@Column(name = "search_begin_tTime")
	private Date searchBeginTime;

	/**
	 * 检索结束时间
	 */
	@Column(name = "search_end_time")
	private Date searchEndTime;

	/**
	 * 专家模式检索表达式
	 */
	@Column(name = "expression", columnDefinition = "TEXT")
	private String expression;

	/**
	 * 微博检索表达式
	 */
	@Column(columnDefinition = "TEXT")
	private String weiboExpression;
	
	/**
	 * 微信检索表达式
	 */
	@Column(columnDefinition = "TEXT")
	private String weixinExpression;
	/**
	 * 经过builder的表达式
	 */
	@Column(name = "real_trsl", columnDefinition = "TEXT")
	private String realTrsl;

	/**
	 * 素材库保留剩余时间天数
	 */
	@Column(name = "remain_days")
	private int remainDays;

	public MaterialLibrary() {
		this.remainDays = 7;// 默认保存7天
		this.status = 1;// 默认没有选择素材
	}

	public MaterialLibrary(String libraryName, int source,String groupName, int mode, String allKeyword, String anyKeyword,
			String excludeKeyword, SearchScope keywordsLocation, Date searchBeginTime, Date searchEndTime,
			String expression,String weiboExpression,String weixinExpression) {
		this.remainDays = 7;// 默认保存7天
		this.status = 1;// 默认没有选择素材
		this.libraryName = libraryName;
		this.source = source;
		this.groupName=groupName;
		this.mode = mode;
		this.allKeyword = allKeyword;
		this.anyKeyword = anyKeyword;
		this.excludeKeyword = excludeKeyword;
		this.keywordsLocation = keywordsLocation;
		this.searchBeginTime = searchBeginTime;
		this.searchEndTime = searchEndTime;
		this.expression = expression;
		this.weiboExpression=weiboExpression;
		this.weixinExpression=weixinExpression;
	}

	/**
	 * 构建查询builder
	 *
	 * @return ITRSSearchBuilder
	 */
	public QueryBuilder toBuilder(int pageNo, int pageSize) {
		return toSearchBuilder(pageNo, pageSize, true);
	}

	/**
	 * 构造不带分页的builder
	 *
	 * @return ITRSSearchBuilder
	 */
	public QueryBuilder toNoPagedBuilder() {
		return toSearchBuilder(-1, -1, true);
	}

	/**
	 * 构造不带时间的builder
	 *
	 * @return ITRSSearchBuilder
	 */
	public QueryBuilder toNoTimeBuilder(int pageNo, int pageSize) {
		return toSearchBuilder(pageNo, pageSize, false);
	}

	/**
	 * 构造不带分页和时间的builder
	 *
	 * @return ITRSSearchBuilder
	 */
	public QueryBuilder toNoPagedAndTimeBuilder() {
		return toSearchBuilder(-1, -1, false);
	}

	private QueryBuilder toSearchBuilder(int pageNo, int pageSize, boolean withTime) {
		String startTime = new SimpleDateFormat("yyyyMMddHHmmss").format(this.searchBeginTime);
		String endTime = new SimpleDateFormat("yyyyMMddHHmmss").format(this.searchEndTime);
		QueryBuilder searchBuilder = new QueryBuilder();
		if (pageNo == -1) {

		} else {
			searchBuilder.page(pageNo, pageSize);
		}
		if (withTime) {
			searchBuilder.filterField("IR_URLTIME", new String[] { startTime, endTime }, Operator.Between);
		}
		switch (this.mode) {
		case 0:
			for (String field : this.keywordsLocation.getField()) {
				StringBuilder childBuilder = new StringBuilder();
				if (StringUtil.isNotEmpty(this.allKeyword)) {
					// 防止全部关键词结尾为;报错
					String replaceAllKey = "";
					if (this.allKeyword.endsWith(";")) {
						replaceAllKey = this.allKeyword.substring(0, this.allKeyword.length() - 1);
						childBuilder.append("(").append(replaceAllKey.replaceAll(";", " AND ")).append(")");
					} else {
						childBuilder.append("(").append(this.allKeyword.replaceAll(";", " AND ")).append(")");
					}
				}
				if (StringUtil.isNotEmpty(this.anyKeyword)) {
					if (childBuilder.length() > 0) {
						childBuilder.append(" AND ");
					}

					// 拦截专项检测加号空格 start
					String[] split = this.anyKeyword.split(",");
					String splitNode = "";
					for (int i = 0; i < split.length; i++) {
						if (StringUtil.isNotEmpty(split[i])) {
							splitNode += split[i] + ",";
						}
					}
					this.anyKeyword = splitNode.substring(0, splitNode.length() - 1);
					// childBuilder.append("((").append(this.anyKeywords.replaceAll(",",
					// " AND ").replaceAll(";", ") OR (")).append("))");
					// 防止全部关键词结尾为;报错
					String replaceAnyKey = "";
					if (this.anyKeyword.endsWith(";")) {
						replaceAnyKey = this.anyKeyword.substring(0, this.anyKeyword.length() - 1);
						childBuilder.append("(").append(replaceAnyKey.replaceAll(";", " AND ")).append(")");
					} else {
						/*childBuilder.append("((")
								.append(this.anyKeyword.replaceAll(",", " AND ").replaceAll(";", ") OR ("))
								.append("))");*/
						childBuilder.append("((\"")
						.append(this.anyKeyword.replaceAll(",", "\" ) AND ( \"").replaceAll(";", "\" OR \""))
						.append("\"))");
					}

				}
				if (StringUtil.isNotEmpty(this.excludeKeyword)) {
					childBuilder.append(" NOT (").append(this.excludeKeyword.replaceAll(";", " AND ")).append(")");
				}
				searchBuilder.filterChildField(field, childBuilder.toString(), Operator.Equal);
			}
			break;
		case 1:
			searchBuilder.filterByTRSL(this.expression);
			break;
		default:
			break;
		}
		// todo :拼接地域 行业
		return searchBuilder;
	}
	/**
	 * 构建查询builder 针对微博
	 *
	 * @return ITRSSearchBuilder
	 */
	public QueryBuilder toBuilderWeiBo(int pageNo, int pageSize) {
		return toSearchBuilderWeiBo(pageNo, pageSize, true);
	}

	/**
	 * 构造不带分页的builder 针对微博 CreateBy xiaoying
	 */
	public QueryBuilder toNoPagedBuilderWeiBo() {
		return toSearchBuilderWeiBo(-1, -1, true);
	}

	/**
	 * 构造不带时间的builder 针对微博
	 *
	 * CreateBy xiaoying
	 */
	public QueryBuilder toNoTimeBuilderWeiBo(int pageNo, int pageSize) {
		return toSearchBuilderWeiBo(pageNo, pageSize, false);
	}

	/**
	 * 构造不带分页和时间的builder 针对微博
	 *
	 * CreateBy xiaoying
	 */
	public QueryBuilder toNoPagedAndTimeBuilderWeiBo() {
		return toSearchBuilderWeiBo(-1, -1, false);
	}
	private QueryBuilder toSearchBuilderWeiBo(int pageNo, int pageSize, boolean withTime) {
		String startTime = new SimpleDateFormat("yyyyMMddHHmmss").format(this.searchBeginTime);
		String endTime = new SimpleDateFormat("yyyyMMddHHmmss").format(this.searchEndTime);
		QueryBuilder searchBuilder = new QueryBuilder();
		if (pageNo == -1) {

		} else {
			searchBuilder.page(pageNo, pageSize);
		}
		if (withTime) {
			searchBuilder.filterField("IR_URLTIME", new String[] { startTime, endTime }, Operator.Between);
		}
		switch (this.mode) {
		case 0:
			String field = FtsFieldConst.FIELD_CONTENT;
//			for (String field : this.keywordsLocation.getField()) {
				StringBuilder childBuilder = new StringBuilder();
				if (StringUtil.isNotEmpty(this.allKeyword)) {
					// 防止全部关键词结尾为;报错
					String replaceAllKey = "";
					if (this.allKeyword.endsWith(";")) {
						replaceAllKey = this.allKeyword.substring(0, this.allKeyword.length() - 1);
						childBuilder.append("(").append(replaceAllKey.replaceAll(";", " AND ")).append(")");
					} else {
						childBuilder.append("(").append(this.allKeyword.replaceAll(";", " AND ")).append(")");
					}
				}
				if (StringUtil.isNotEmpty(this.anyKeyword)) {
					if (childBuilder.length() > 0) {
						childBuilder.append(" AND ");
					}

					// 拦截专项检测加号空格 start
					String[] split = this.anyKeyword.split(",");
					String splitNode = "";
					for (int i = 0; i < split.length; i++) {
						if (StringUtil.isNotEmpty(split[i])) {
							splitNode += split[i] + ",";
						}
					}
					this.anyKeyword = splitNode.substring(0, splitNode.length() - 1);
					// childBuilder.append("((").append(this.anyKeywords.replaceAll(",",
					// " AND ").replaceAll(";", ") OR (")).append("))");
					// 防止全部关键词结尾为;报错
					String replaceAnyKey = "";
					if (this.anyKeyword.endsWith(";")) {
						replaceAnyKey = this.anyKeyword.substring(0, this.anyKeyword.length() - 1);
						childBuilder.append("(").append(replaceAnyKey.replaceAll(";", " AND ")).append(")");
					} else {
						/*childBuilder.append("((")
								.append(this.anyKeyword.replaceAll(",", " AND ").replaceAll(";", ") OR ("))
								.append("))");*/
						childBuilder.append("((\"")
						.append(this.anyKeyword.replaceAll(",", "\" ) AND ( \"").replaceAll(";", "\" OR \""))
						.append("\"))");
					}

				}
				if (StringUtil.isNotEmpty(this.excludeKeyword)) {
					childBuilder.append(" NOT (").append(this.excludeKeyword.replaceAll(";", " AND ")).append(")");
				}
				searchBuilder.filterChildField(field, childBuilder.toString(), Operator.Equal);
//			}//for field
			break;
		case 1:
			searchBuilder.filterByTRSL(this.weiboExpression);
			break;
		default:
			break;
		}
		// todo :拼接地域 行业
		return searchBuilder;
	}

	/**
	 * 构建查询builder 针对微信
	 *
	 * @return ITRSSearchBuilder
	 */
	public QueryBuilder toBuilderWeiXin(int pageNo, int pageSize) {
		return toSearchBuilderWeiXin(pageNo, pageSize, true);
	}

	/**
	 * 构造不带分页的builder 针对微信 CreateBy xiaoying
	 */
	public QueryBuilder toNoPagedBuilderWeiXin() {
		return toSearchBuilderWeiXin(-1, -1, true);
	}

	/**
	 * 构造不带时间的builder 针对微信
	 *
	 * CreateBy xiaoying
	 */
	public QueryBuilder toNoTimeBuilderWeiXin(int pageNo, int pageSize) {
		return toSearchBuilderWeiXin(pageNo, pageSize, false);
	}

	/**
	 * 构造不带分页和时间的builder 针对微信
	 *
	 * CreateBy xiaoying
	 */
	public QueryBuilder toNoPagedAndTimeBuilderWeiXin() {
		return toSearchBuilderWeiXin(-1, -1, false);
	}

	/**
	 * 查微信拼写表达式
	 * 
	 * @param pageNo
	 *            页数（从0开始）
	 * @param pageSize
	 *            一页几条
	 * @param withTime
	 *            是否带时间查询
	 * @return CreateBy xiaoying
	 */
	private QueryBuilder toSearchBuilderWeiXin(int pageNo, int pageSize, boolean withTime) {
		String startTime = new SimpleDateFormat("yyyyMMddHHmmss").format(this.searchBeginTime);
		String endTime = new SimpleDateFormat("yyyyMMddHHmmss").format(this.searchEndTime);
		QueryBuilder searchBuilder = new QueryBuilder();
		if (pageNo == -1) {

		} else {
			searchBuilder.page(pageNo, pageSize);
		}
		if (withTime) {
			searchBuilder.filterField("IR_CREATED_DATE", new String[] { startTime, endTime }, Operator.Between);
		}
		switch (this.mode) {
		case 0:
			for (String field : this.keywordsLocation.getField()) {
				StringBuilder childBuilder = new StringBuilder();
				if (StringUtil.isNotEmpty(this.allKeyword)) {
					// 防止全部关键词结尾为;报错
					String replaceAllKey = "";
					if (this.allKeyword.endsWith(";")) {
						replaceAllKey = this.allKeyword.substring(0, this.allKeyword.length() - 1);
						childBuilder.append("(").append(replaceAllKey.replaceAll(";", " AND ")).append(")");
					} else {
						childBuilder.append("(").append(this.allKeyword.replaceAll(";", " AND ")).append(")");
					}
				}
				if (StringUtil.isNotEmpty(this.anyKeyword)) {
					if (childBuilder.length() > 0) {
						childBuilder.append(" AND ");
					}

					// 拦截专项检测加号空格 start
					String[] split = this.anyKeyword.split(",");
					String splitNode = "";
					for (int i = 0; i < split.length; i++) {
						if (StringUtil.isNotEmpty(split[i])) {
							splitNode += split[i] + ",";
						}
					}
					this.anyKeyword = splitNode.substring(0, splitNode.length() - 1);
					// 防止全部关键词结尾为;报错
					String replaceAnyKey = "";
					if (this.anyKeyword.endsWith(";")) {
						replaceAnyKey = this.anyKeyword.substring(0, this.anyKeyword.length() - 1);
						childBuilder.append("(").append(replaceAnyKey.replaceAll(";", " AND ")).append(")");
					} else {
						/*childBuilder.append("((")
								.append(this.anyKeyword.replaceAll(",", " AND ").replaceAll(";", ") OR ("))
								.append("))");*/
						childBuilder.append("((\"")
						.append(this.anyKeyword.replaceAll(",", "\" ) AND ( \"").replaceAll(";", "\" OR \""))
						.append("\"))");
					}

				}
				if (StringUtil.isNotEmpty(this.excludeKeyword)) {
					childBuilder.append(" NOT (").append(this.excludeKeyword.replaceAll(";", " AND ")).append(")");
				}
				searchBuilder.filterChildField(field, childBuilder.toString(), Operator.Equal);
			}
			break;
		case 1:
			searchBuilder.filterByTRSL(this.weixinExpression);
			break;
		default:
			break;
		}
		// todo :拼接地域 行业
		return searchBuilder;
	}
}
