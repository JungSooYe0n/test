package com.trs.netInsight.widget.column.service.column;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;


import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeBase;
import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeClassify;
import com.trs.netInsight.support.knowledgeBase.service.IKnowledgeBaseService;
import com.trs.netInsight.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.annotation.FtsClient;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.user.entity.User;

/**
 * 热点栏目列表
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年4月8日
 */
@Slf4j
public class HostListColumn extends AbstractColumn {

	@Override
	public Object getColumnData(String timeRange) throws TRSSearchException {
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> map = null;
		QueryBuilder builder = this.createQueryBuilder();
		// 选择数据库
		selectDatabase(builder);

		// 根据MD5分类统计
		try {

			list = (List<Map<String, Object>>) commonChartService.getHotListColumnData(builder,config.getMaxSize(),"column");

			/*String uid = UUID.randomUUID().toString();
			RedisUtil.setString(uid, builder.asTRSL());
			//逻辑修改:热度值及相似文章数计算之前先进行站内排重  2019-12-4
			GroupResult categoryQuery = hybase8SearchService.categoryQuery(builder.isServer(), builder.asTRSL(), false,true
					,false, FtsFieldConst.FIELD_MD5TAG, config.getMaxSize(),"column", builder.getDatabase());
			for (GroupInfo groupInfo : categoryQuery) {
				String md5 = groupInfo.getFieldValue();
				// 相似文章数
				long count = groupInfo.getCount();
				QueryBuilder query = new QueryBuilder();
				query.filterByTRSL(builder.asTRSL());
				query.filterField(FtsFieldConst.FIELD_MD5TAG, md5, Operator.Equal);
				query.page(0, 1);
				query.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				List<FtsDocumentCommonVO> docs = hybase8SearchService.ftsQuery(query, FtsDocumentCommonVO.class, false,false,
						false,"column");
				if (null != docs && docs.size() > 0) {
					FtsDocumentCommonVO doc = docs.get(0);
					map = new HashMap<>();
					String groupName = doc.getGroupName();
					map.put("sid", doc.getSid());
					map.put("title", StringUtil.replacePartOfHtml(doc.getTitle()));
					if ("微博".equals(groupName)) {
						map.put("siteName", doc.getScreenName());
					} else {
						map.put("siteName", doc.getSiteName());
					}
					map.put("urlName", doc.getUrlName());
					map.put("hkey", doc.getHkey());
					map.put("simCount", String.valueOf(count));
					map.put("trslk", uid);
					map.put("groupName", groupName);
					map.put("nreserved1", doc.getNreserved1());
					map.put("catalogArea",doc.getCatalogArea());
					map.put("location",doc.getLocation());
					// 获得时间差
					Map<String, String> timeDifference = DateUtil.timeDifference(doc);
					boolean isNew = false;
					if (ObjectUtil.isNotEmpty(timeDifference.get("timeAgo"))) {
						isNew = true;
						map.put("timeAgo", timeDifference.get("timeAgo"));
					} else {
						map.put("timeAgo", timeDifference.get("urlTime"));
					}
					map.put("isNew", isNew);
					map.put("md5Tag", doc.getMd5Tag());
					map.put("urlTime", doc.getUrlTime());
					list.add(map);
				}
			}*/
		} catch (TRSSearchException e) {
			throw new TRSSearchException(e);
		} catch (TRSException e) {
			throw new TRSSearchException(e);
		}
		return list;
	}

	@Override
	public Object getColumnCount() throws TRSSearchException {
		QueryCommonBuilder builder = this.createQueryCommonBuilder();
		// 选择数据库
		//selectDatabase(builder);
		String uid = UUID.randomUUID().toString();
		RedisUtil.setString(uid, builder.asTRSL());
		QueryBuilder queryBuilder = new QueryBuilder();
		selectDatabase(queryBuilder);
		String[] split = queryBuilder.getDatabase().split(";");
		builder.setDatabase(split);
		long countCommon = hybase8SearchService.ftsCountCommon(builder, false, false,false,"column");
		return countCommon;
	}


	@Override
	public Object getSectionList() throws TRSSearchException {
		User user = UserUtils.getUser();
		QueryBuilder builder = this.createQueryBuilder();
		// 选择数据库
		selectDatabase(builder);
		try {
			return super.infoListService.getHotList(builder, builder, user,"column");
		} catch (TRSException e) {
			throw new TRSSearchException(e);
		}
	}

	@Override
	public Object getAppSectionList(User user) throws TRSSearchException {
		return null;
	}

	@Override
	public QueryBuilder createQueryBuilder() {
		QueryBuilder builder = super.config.getQueryBuilder();
		IndexTab indexTab = super.config.getIndexTab();
		String[] tradition = null;
		String groupName = super.config.getGroupName() ;
		if (StringUtils.equals("ALL", groupName)
				|| StringUtils.isBlank(groupName)) {
			tradition = indexTab.getTradition();

			for (int i = 0; i < tradition.length; i++) {
				if ("微信".equals(tradition[i])) {
					tradition[i] = "国内微信";
				}else if ("境外媒体".equals(tradition[i])){
					tradition[i] = "国外新闻";
				}
			}
		} else {

			if ("微信".equals(groupName)) {
				groupName = "国内微信";
			}
			tradition = new String[] { groupName };
		}

		builder.filterField(FtsFieldConst.FIELD_GROUPNAME, tradition, Operator.Equal);
		if (indexTab.isServer()) {
			builder.setServer(true);
		}
		return builder;

	}
	/**
	 * 查询数据库
	 *
	 * @param builder
	 *            表达式builder
	 */
	public void selectDatabase(QueryBuilder builder) {
		// 选择查询数据源
		IndexTab indexTab = super.config.getIndexTab();
		String[] tradition = indexTab.getTradition();
		int weibo = 0;
		int weixin = 0;
		int chuantong = 0;
		int oversea = 0;
		for (String dataBase : tradition) {
			if (Const.MEDIA_TYPE_WEIBO.contains(dataBase)) {
				weibo++;
			} else if (Const.MEDIA_TYPE_WEIXIN.contains(dataBase)) {
				weixin++;
			} else if (Const.MEDIA_TYPE_FINAL_NEWS.contains(dataBase)) {
				chuantong++;
			} else if (Const.MEDIA_TYPE_TF.contains(dataBase)) {
				oversea++;
			}
		}
		if (weibo > 0) {
			if (StringUtil.isNotEmpty(builder.getDatabase())) {
				builder.setDatabase(builder.getDatabase() + ";" + Const.WEIBO);
			} else {
				builder.setDatabase(Const.WEIBO);
			}
		}
		if (weixin > 0) {
			if (StringUtil.isNotEmpty(builder.getDatabase())) {
				builder.setDatabase(builder.getDatabase() + ";" + Const.WECHAT);
			} else {
				builder.setDatabase(Const.WECHAT);
			}
		}
		if (chuantong > 0) {
			if (StringUtil.isNotEmpty(builder.getDatabase())) {
				builder.setDatabase(builder.getDatabase() + ";" + Const.HYBASE_NI_INDEX);
			} else {
				builder.setDatabase(Const.HYBASE_NI_INDEX);
			}
		}
		if (oversea > 0) {
			if (StringUtil.isNotEmpty(builder.getDatabase())) {
				builder.setDatabase(builder.getDatabase() + ";" + Const.HYBASE_OVERSEAS);
			} else {
				builder.setDatabase(Const.HYBASE_OVERSEAS);
			}
		}
	}
	@Override
	public QueryCommonBuilder createQueryCommonBuilder() {
        QueryCommonBuilder builder = super.config.getCommonBuilder();
        IndexTab indexTab = super.config.getIndexTab();
        // 选择查询库
        String[] databases = TrslUtil.chooseDatabaseByIndexType(indexTab.getType());
        builder.setDatabase(databases);
        List<String> type = Arrays.asList(indexTab.getType());

        // 选择需要查询的group
        // 统一来源
        String[] tradition = indexTab.getTradition();
        String groupNames = "";
        List<String> dataList = Arrays.asList(databases);
        if (dataList.contains(Const.WEIBO)) {
            groupNames += "微博 OR ";
        }
        if (dataList.contains(Const.WECHAT_COMMON)) {
            groupNames += "国内微信 OR ";
        }
        if (type.contains(ColumnConst.LIST_TWITTER)) {
            groupNames += "\"Twitter\" OR ";
        }
        if (type.contains(ColumnConst.LIST_FaceBook)) {
            groupNames += "\"FaceBook\" OR ";
        }
        if (tradition != null && tradition.length > 0) {
            for (int i = 0; i < tradition.length; i++) {
                String meta = tradition[i];
                groupNames += meta + " OR ";
            }
        }

        if (groupNames.endsWith(" OR ")) {
            groupNames = groupNames.substring(0, groupNames.length() -4);
        }
        groupNames = groupNames.replace("境外媒体", "国外新闻");
        builder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupNames, Operator.Equal);

        Integer maxSize = super.config.getMaxSize();
        if(maxSize != null && maxSize != 0){
            builder.setPageSize(maxSize);
        }
        return builder;
	}

}
