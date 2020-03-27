package com.trs.netInsight.widget.column.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.ESFieldConst;
import com.trs.netInsight.config.constant.ExcelConst;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.*;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.report.excel.ExcelData;
import com.trs.netInsight.support.report.excel.ExcelFactory;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.repository.AlertRepository;
import com.trs.netInsight.widget.analysis.entity.CategoryBean;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;
import com.trs.netInsight.widget.analysis.service.impl.ChartAnalyzeService;
import com.trs.netInsight.widget.column.entity.Columns;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.factory.AbstractColumn;
import com.trs.netInsight.widget.column.factory.ColumnConfig;
import com.trs.netInsight.widget.column.factory.ColumnFactory;
import com.trs.netInsight.widget.column.repository.ColumnRepository;
import com.trs.netInsight.widget.column.repository.IndexPageRepository;
import com.trs.netInsight.widget.column.repository.IndexTabMapperRepository;
import com.trs.netInsight.widget.column.repository.IndexTabRepository;
import com.trs.netInsight.widget.column.service.IColumnService;
import com.trs.netInsight.widget.column.service.IIndexTabMapperService;
import com.trs.netInsight.widget.column.service.IIndexTabService;
import com.trs.netInsight.widget.microblog.entity.SpreadObject;
import com.trs.netInsight.widget.report.entity.repository.FavouritesRepository;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.special.service.IInfoListService;
import com.trs.netInsight.widget.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 栏目相关接口服务实现类
 * <p>
 *      @author 北京拓尔思信息技术股份有限公司
 * Create by yan.changjiang on 2017年11月27日
 */
@Service
@Slf4j
public class ColumnServiceImpl implements IColumnService {

	@Autowired
	private IndexTabRepository indexTabRepository;

	@Autowired
	private IndexPageRepository indexPageRepository;

	@Autowired
	private FullTextSearch hybase8SearchService;

	@Autowired
	private ChartAnalyzeService chartAnalyzeService;

	@Autowired
	private IDistrictInfoService districtInfoService;

	@Autowired
	private ColumnRepository columnRepository;

	@Autowired
	private IInfoListService infoListService;

	@Autowired
	private FavouritesRepository favouritesRepository;

	@Autowired
	private AlertRepository alertRepository;
	
	@Autowired
	private IIndexTabService indexTabService;
	
	@Autowired
	private IIndexTabMapperService indexTabMapperService;
	
	@Autowired
	private IndexTabMapperRepository tabMapperRepository;

	@Override
	public String updateOne(User user, String name, String oneId) throws OperationException {
		try {
			// 修改一级二级表中所有关于一级栏目的名字字段 不管他的二级栏目是否为空
			Criteria<IndexPage> criteria = new Criteria<>();
			if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
				criteria.add(Restrictions.eq("userId", user.getId()));
			}else {
				criteria.add(Restrictions.eq("subGroupId", user.getSubGroupId()));
			}
			criteria.add(Restrictions.eq("id", oneId));
			List<IndexPage> findAll = indexPageRepository.findAll(criteria);
			for (IndexPage oneAndTwo : findAll) {
				oneAndTwo.setParentName(name);
				indexPageRepository.save(oneAndTwo);
			}
			return "success";
		} catch (Exception e) {
			throw new OperationException("修改一级栏目出错");
		}
	}

	@Override
	public Object updateTwo(String userId, String name, String twoId) throws OperationException {
		try {
			// 修改一级二级表中所有关于一级栏目的名字字段 不管他的二级栏目是否为空
			Criteria<IndexPage> criteria = new Criteria<>();
			criteria.add(Restrictions.eq("userId", userId));
			criteria.add(Restrictions.eq("sonId", twoId));
			List<IndexPage> findAll = indexPageRepository.findAll(criteria);
			for (IndexPage oneAndTwo : findAll) {
				// oneAndTwo.setSonName(name);
				indexPageRepository.save(oneAndTwo);
			}
			return "success";
		} catch (Exception e) {
			throw new OperationException("修改一级栏目出错");
		}
	}

	@Override
	public String deleteTwo(String twoId) throws OperationException {
		try {
			// 删除二级栏目下级联的三级栏目
			List<IndexTab> findByParentId = indexTabRepository.findByParentId(twoId);
			for (IndexTab three : findByParentId) {
				indexTabRepository.delete(three.getId());
			}
		} catch (Exception e) {
			throw new OperationException("删除二级栏目时出错");
		}
		return "success";
	}

	@Override
	@Transactional
	public Object deleteOne(String indexPageId) throws OperationException {
		int i=0;
		try {
			// 删除栏目组及下级子栏目
			try {
				List<IndexTabMapper> mappers = indexTabMapperService.findByIndexPageId(indexPageId);
				if (CollectionsUtil.isNotEmpty(mappers)) {
					for (IndexTabMapper mapper : mappers) {
						if(mapper.getShare()){//表示为共享
							i++;
						}
						// 删除栏目映射关系，isMe为true的栏目关系须级联删除栏目实体
						List<IndexTabMapper> findByIndexTab = indexTabMapperService.findByIndexTab(mapper.getIndexTab());
						//删除所有与indexTab关联的  否则剩余关联则删除indexTab时失败
						tabMapperRepository.delete(findByIndexTab);
						if (mapper.isMe()) {
							indexTabRepository.delete(mapper.getIndexTab());
						}
					}
				}
			} catch (Exception e) {
				throw new OperationException("子栏目为空！",e);
			}
			// 删除栏目组
			indexPageRepository.delete(indexPageId);
		} catch (Exception e) {
			throw new OperationException("删除一级级栏目时出错",e);
		}
		Map<String,Integer> data = new HashMap<String,Integer>();
		data.put("delCount",i);
		return data;
	}

	@Override
	public List<Map<String, Object>> selectColumn(User user,String typeId) throws OperationException {
		String userId = user.getId();
		String subGroupId = user.getSubGroupId();
		// 从一级开始找
		// 把sonId为空的找出来 这是一级的
		Criteria<IndexPage> criteria = new Criteria<>();
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			criteria.add(Restrictions.eq("userId", userId));
		}else {
			criteria.add(Restrictions.eq("subGroupId", subGroupId));
		}
		criteria.orderByASC("sequence");
		criteria.add(Restrictions.eq("typeId", typeId));
		List<IndexPage> list1 = indexPageRepository.findAll(criteria);
		List<IndexPage> oneList = new ArrayList<>();
		oneList = list1;
		// 通过一级找三级
		List<Map<String, Object>> addTwo = new ArrayList<>();
//		for (IndexPage one : oneList) {
//			String id = one.getId();
//			Criteria<IndexTab> criteriaThree = new Criteria<>();
//			criteriaThree.add(Restrictions.eq("userId", userId));
//			if (StringUtil.isNotEmpty(id)) {
//				criteriaThree.add(Restrictions.eq("parentId", id));
//			}
//			criteriaThree.orderByASC("sequence");
//			List<IndexTab> threeList = indexTabRepository.findAll(criteriaThree);
//			List<IndexTab> addThree = new ArrayList<>();
//			if (ObjectUtil.isNotEmpty(threeList)) {
//				for (IndexTab three : threeList) {
//					String[] tradition = three.getTradition();
//					if (tradition != null && tradition.length > 0) {
//						if (tradition[0].equals("境外媒体")) {
//							three.setGroupName("境外媒体");
//						}
//					}
//					addThree.add(three);
//				}
//			}
//			Map<String, Object> putValue = MapUtil.putValue(new String[] { "threeList", "oneName", "oneId", "hide" },
//					addThree, one.getParentName(), one.getId(), one.isHide());
//			addTwo.add(putValue);
//		}
		for (IndexPage indexPage : oneList) {
			List<IndexTabMapper> mappers = this.indexTabMapperService.findByIndexPage(indexPage);
			if (CollectionsUtil.isNotEmpty(mappers)) {
				int min = mappers.get(0).getSequence();
				for (IndexTabMapper indexTabMapper : mappers) {
					if(min > indexTabMapper.getSequence()){
						min = indexTabMapper.getSequence();
					}
				}
				//前端首先加载四个栏目，如果顺序大于4时，会导致前端加载不出来
				//在每次返回给前端时判断顺序，避免前端出现问题
				if(min >1){
					for (IndexTabMapper indexTabMapper : mappers) {
						indexTabMapper.setSequence(indexTabMapper.getSequence()- min +1);
					}
					tabMapperRepository.save(mappers);
				}
			}
			// 考虑加锁 一个一个存
//			if (CollectionsUtil.isNotEmpty(mappers)) {
//				IndexTab indexTab = null;
//				for (IndexTabMapper mapper : mappers) {
//					indexTab = mapper.getIndexTab();
//					if (indexTab != null) {
//						String[] tradition = indexTab.getTradition();
//						if (tradition != null && tradition.length > 0 ) {
//							if (tradition[0].equals("境外媒体")) {
//								indexTab.setGroupName("境外媒体");
//							}
//						}
//					}
//				}
//			}
			//不做3个月限制，具体查询范围，只以机构查询范围为准
//			for(IndexTabMapper mapper : mappers){
//				IndexTab indexTab = mapper.getIndexTab();
//				String timeRange = indexTab.getTimeRange();
//				timeRange = com.trs.netInsight.util.DateUtil.getStartToThreeMonth(timeRange);
//				indexTab.setTimeRange(timeRange);
//				mapper.setIndexTab(indexTab);
//			}
			Map<String, Object> putValue = MapUtil.putValue(new String[] { "threeList", "oneName", "oneId", "hide" },
					mappers, indexPage.getParentName(), indexPage.getId(), indexPage.isHide());
			addTwo.add(putValue);
		}
		return addTwo;
	}


	/**
	 * TODO 获取无相似文章列表数据,传统媒体类型
	 *
	 * @param queryBuilder
	 * @return
	 * @throws TRSException
	 * @throws TRSSearchException
	 */
	private List<Map<String, Object>> getDataNoSim(QueryBuilder queryBuilder) throws TRSSearchException, TRSException {
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> map = null;
		log.info(queryBuilder.asTRSL());
		List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocument.class, true,false,false,"column");
		String uid = UUID.randomUUID().toString();
		RedisUtil.setString(uid, queryBuilder.asTRSL());
		for (FtsDocument document : ftsQuery) {
			map = new HashMap<>();
			map.put("sid", document.getSid());
			map.put("title", document.getTitle());
			map.put("siteName", document.getSiteName());
			map.put("trslk", uid);
			map.put("groupName", document.getGroupName());
			map.put("nreserved1", document.getNreserved1());
			map.put("urlTime", document.getUrlTime());
			map.put("appraise", document.getAppraise());
			map.put("screenName", document.getSiteName());
			map.put("urlName", document.getUrlName());
			map.put("nreserved1", document.getNreserved1());
			map.put("content", document.getContent());
			map.put("abstracts",document.getAbstracts());
			/*map.put("content", StringUtil.removeFourChar(StringUtil.replaceImg(document.getContent())));
			map.put("abstracts",StringUtil.removeFourChar(StringUtil.replaceImg(document.getAbstracts())));*/
			// 获得时间差,三天内显示时间差,剩下消失urltime
			Map<String, String> timeDifference = DateUtil.timeDifference(document);
			boolean isNew = false;
			if (ObjectUtil.isNotEmpty(timeDifference.get("timeAgo"))) {
				isNew = true;
				map.put("timeAgo", timeDifference.get("timeAgo"));
			} else {
				map.put("timeAgo", timeDifference.get("urlTime"));
			}
			map.put("isNew", isNew);
			map.put("md5Tag", document.getMd5Tag());
			list.add(map);
		}
		return list;
	}

	/**
	 * 根据关键词以及关键词位置生成trsl表达式
	 *
	 * @param indexTab
	 *            关键词,多值,以';'隔开
	 * @param queryBuilder
	 *            关键词位置,多值,以';'隔开
	 * @return
	 */
	private void appendTrsl(IndexTab indexTab, QueryBuilder queryBuilder, QueryBuilder queryBuilderStatus,
			QueryBuilder queryBuilderWeChat) {
		String keyWords = indexTab.getKeyWord();
		String keyWordindex = indexTab.getKeyWordIndex();
		String excludeWords = indexTab.getExcludeWords();
		StringBuilder childTrsl = new StringBuilder();
		// 判断关键词位置是否为空
		if (StringUtil.isNotEmpty(keyWordindex) && StringUtil.isNotEmpty(keyWords)) {
			// 切割关键词位置
			String[] sources = keyWordindex.split(";");
			for (String source : sources) {
				// 关键词是否为空
				// if (StringUtil.isNotEmpty(keyWords)) {
				// 判断是否为";"结尾
				// if (keyWords.endsWith(";")) {
				// keyWords = keyWords.substring(0, keyWords.length() - 1);
				// keyWords = keyWords.replaceAll("[;|；]+", "\" AND \"");
				// childTrsl.append("(\"" + keyWords + "\")");
				// } else {
				// keyWords = keyWords.replaceAll("[;|；]+", "\" AND \"");
				// childTrsl.append("(\"" + keyWords + "\")");
				// }
				String replaceAnyKey = "";
				if (keyWords.endsWith(";")) {
					replaceAnyKey = keyWords.substring(0, keyWords.length() - 1);
					childTrsl.append("((\"")
							.append(replaceAnyKey.replaceAll(",", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				} else {
					childTrsl.append("((\"")
							.append(keyWords.replaceAll(",", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				}
				// }
				if (StringUtil.isNotEmpty(excludeWords)) {
					childTrsl.append(" NOT (\"").append(excludeWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				}
				switch (source.trim()) {
				// 仅标题
				case "0":
					queryBuilder.filterChildField(FtsFieldConst.FIELD_URLTITLE, childTrsl.toString(), Operator.Equal);
					queryBuilderWeChat.filterChildField(FtsFieldConst.FIELD_URLTITLE, childTrsl.toString(),
							Operator.Equal);
					queryBuilderStatus.filterChildField(FtsFieldConst.FIELD_STATUS_CONTENT, childTrsl.toString(),
							Operator.Equal);
					queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);// 传统
					queryBuilderWeChat.setDatabase(Const.WECHAT);// 微信
					queryBuilderStatus.setDatabase(Const.WEIBO);// 微博
					queryBuilder.orderBy(ESFieldConst.IR_URLTIME, true);
					queryBuilderWeChat.orderBy(ESFieldConst.IR_URLTIME, true);
					queryBuilderStatus.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					continue;
				case "1":// 标题 + 正文
					queryBuilder.filterChildField(FtsFieldConst.FIELD_URLTITLE, childTrsl.toString(), Operator.Equal);
					queryBuilder.filterChildField(FtsFieldConst.FIELD_CONTENT, childTrsl.toString(), Operator.Equal);
					queryBuilderWeChat.filterChildField(FtsFieldConst.FIELD_URLTITLE, childTrsl.toString(),
							Operator.Equal);
					queryBuilderWeChat.filterChildField(FtsFieldConst.FIELD_CONTENT, childTrsl.toString(),
							Operator.Equal);
					queryBuilderStatus.filterChildField(FtsFieldConst.FIELD_STATUS_CONTENT, childTrsl.toString(),
							Operator.Equal);
					queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);// 传统
					queryBuilderWeChat.setDatabase(Const.WECHAT);// 微信
					queryBuilderStatus.setDatabase(Const.WEIBO);// 微博
					queryBuilder.orderBy(ESFieldConst.IR_URLTIME, true);
					queryBuilderWeChat.orderBy(ESFieldConst.IR_URLTIME, true);
					queryBuilderStatus.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					continue;
				default:// 仅标题
					queryBuilder.filterChildField(FtsFieldConst.FIELD_URLTITLE, childTrsl.toString(), Operator.Equal);
					queryBuilderWeChat.filterChildField(FtsFieldConst.FIELD_URLTITLE, childTrsl.toString(),
							Operator.Equal);
					queryBuilderStatus.filterChildField(FtsFieldConst.FIELD_STATUS_CONTENT, childTrsl.toString(),
							Operator.Equal);
					queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);// 传统
					queryBuilderWeChat.setDatabase(Const.WECHAT);// 微信
					queryBuilderStatus.setDatabase(Const.WEIBO);// 微博
					queryBuilder.orderBy(ESFieldConst.IR_URLTIME, true);
					queryBuilderWeChat.orderBy(ESFieldConst.IR_URLTIME, true);
					queryBuilderStatus.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					continue;
				}
			}
		} else {// 时间倒序
			queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);// 传统
			queryBuilderWeChat.setDatabase(Const.WECHAT);// 微信
			queryBuilderStatus.setDatabase(Const.WEIBO);// 微博
			queryBuilder.orderBy(ESFieldConst.IR_URLTIME, true);
			queryBuilderWeChat.orderBy(ESFieldConst.IR_URLTIME, true);
			queryBuilderStatus.orderBy(FtsFieldConst.FIELD_URLTIME, true);
		}
	}

	@Override
	public void save(Columns column) {
		this.columnRepository.save(column);

	}

	@Override
	public List<Columns> findByUserId(String uid, Sort sort) {
		return this.columnRepository.findByUserId(uid, sort);
	}

	@Override
	public List<Columns> findByOrganizationId(Sort sort) {

		String userId = UserUtils.getUser().getId();
		return this.columnRepository.findByUserId(userId, sort);
	}

	@Override
	public Object selectColumnByOrganizationId(String organizationId) throws OperationException {
		// 从一级开始找
		// 把sonId为空的找出来 这是一级的
		Criteria<IndexPage> criteria = new Criteria<>();
		criteria.add(Restrictions.eq("organizationId", organizationId));
		// criteria.add(Restrictions.eq("sonId", ""));
		// criteria.orderByDESC("lastModifiedTime");
		criteria.orderByASC("sequence");
		List<IndexPage> list1 = indexPageRepository.findAll(criteria);
		List<IndexPage> oneList = new ArrayList<>();
		oneList = list1;
		// 通过一级找三级
		List<Map<String, Object>> addTwo = new ArrayList<>();
		for (IndexPage one : oneList) {
			String id = one.getId();
			Criteria<IndexTab> criteriaThree = new Criteria<>();
			criteriaThree.add(Restrictions.eq("organizationId", organizationId));
			if (StringUtil.isNotEmpty(id)) {
				criteriaThree.add(Restrictions.eq("parentId", id));
			}
			criteriaThree.orderByASC("sequence");
			List<IndexTab> threeList = indexTabRepository.findAll(criteriaThree);
			List<IndexTab> addThree = new ArrayList<>();
			if (ObjectUtil.isNotEmpty(threeList)) {
				for (IndexTab three : threeList) {
					addThree.add(three);
				}
			}
			Map<String, Object> putValue = MapUtil.putValue(new String[] { "threeList", "oneName", "oneId", "hide" },
					addThree, one.getParentName(), one.getId(), one.isHide());
			addTwo.add(putValue);
		}
		return addTwo;
	}

	private void getDataBarByList(QueryBuilder queryBuilder, IndexTab indexTab, String key) throws OperationException {
		List<CategoryBean> mediaType = chartAnalyzeService.getMediaType(indexTab.getXyTrsl());
		for (CategoryBean categoryBean : mediaType) {
			if (categoryBean.getKey().equals(key)) {
				// 取表达式
				String value = categoryBean.getValue();
				queryBuilder.filterByTRSL(value);
				// countBuiler.filterByTRSL(value);
			}
		}
	}

	@Override
	public Object list(IndexTab indexTab, QueryBuilder queryBuilder, QueryBuilder countBuiler, int pagesize, int pageno,
			String fenlei, String sort, String key, String area) throws TRSException {
		//从实体里取是否排重
		//boolean sim = indexTab.isSimilar();
		boolean irSimflag = indexTab.isIrSimflag();
		boolean sim = indexTab.isIrSimflagAll();
		// 取地域名
		if (!"ALL".equals(area)) { // 地域

			String[] areaSplit = area.split(";");
			String contentArea = "";
			for (int i = 0; i < areaSplit.length; i++) {
				areaSplit[i] = "中国\\\\" + areaSplit[i] + "*";
				if (i != areaSplit.length - 1) {
					areaSplit[i] += " OR ";
				}
				contentArea += areaSplit[i];
			}
			queryBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
			countBuiler.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + contentArea);
		}
		// 词云进去
		// 取userId
		User loginUser = UserUtils.getUser();
		// 为了调用人家写好的方法
		// QueryBuilder queryBuilderWeChat = queryBuilder;
		// QueryBuilder queryBuilderStatus = queryBuilder;
		QueryBuilder queryBuilderWeChat = new QueryBuilder();
		QueryBuilder queryBuilderStatus = new QueryBuilder();
		queryBuilderWeChat.filterByTRSL(queryBuilder.asTRSL());
		queryBuilderWeChat.page(pageno, pagesize);
		queryBuilderStatus.filterByTRSL(queryBuilder.asTRSL());
		queryBuilderStatus.page(pageno, pagesize);
		// queryBuilderStatus = queryBuilder;
		String trsl = indexTab.getTrsl();
		String statusTrsl = indexTab.getStatusTrsl();
		String weChatTrsl = indexTab.getWeChatTrsl();
		// 根据trsl字段,判别普通模式与专家模式
		if (StringUtil.isEmpty(trsl)) {
			// 将关键词与关键词位置,转换为trsl表达式,并保存到builder中
			appendTrsl(indexTab, queryBuilder, queryBuilderStatus, queryBuilderWeChat);
			if ("微博".equals(fenlei)) {
				queryBuilderStatus.filterByTRSL(statusTrsl);
				queryBuilder = queryBuilderStatus;
			} else if ("微信".equals(fenlei)) {
				queryBuilderWeChat.filterByTRSL(weChatTrsl);
				queryBuilder = queryBuilderWeChat;
			}
		} else {
			if ("微博".equals(fenlei)) {// 防止trsl不对
				String replace = statusTrsl.replace(FtsFieldConst.FIELD_TITLE, FtsFieldConst.FIELD_STATUS_CONTENT);
				replace = replace.replaceAll(FtsFieldConst.FIELD_CONTENT, FtsFieldConst.FIELD_STATUS_CONTENT);
				replace = replace.replaceAll(FtsFieldConst.FIELD_URLTIME, FtsFieldConst.FIELD_URLTIME);
				queryBuilder.filterByTRSL(replace);
				countBuiler.filterByTRSL(replace);
			} else if ("微信".equals(fenlei)) {
				queryBuilderWeChat.filterByTRSL(weChatTrsl);
				queryBuilder = queryBuilderWeChat;
				countBuiler = queryBuilderWeChat;
			} else {
				queryBuilder.filterByTRSL(indexTab.getTrsl());
				countBuiler.filterByTRSL(indexTab.getTrsl());
			}
		}
		// 如果有key值 就是分类对比图
		if (StringUtil.isNotEmpty(key)) {
			getDataBarByList(queryBuilder, indexTab, key);
		}
		// 分用哪个方法
		if ("微博".equals(fenlei)) {
			// 然后还得分是否是按照热度排序
			switch (sort) { // 排序
			case "desc":
				queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				countBuiler.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				countBuiler.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				return infoListService.getHotListStatus(queryBuilder, countBuiler, loginUser,"column");
			case "commtCount": // 微博按照 评论数 排序
				queryBuilder.orderBy(FtsFieldConst.FIELD_COMMTCOUNT, true);
				countBuiler.orderBy(FtsFieldConst.FIELD_COMMTCOUNT, true);
				break;
			case "rttCount": // 微博按照评论数 转发数 排序
				queryBuilder.orderBy(FtsFieldConst.FIELD_RTTCOUNT, true);
				countBuiler.orderBy(FtsFieldConst.FIELD_RTTCOUNT, true);
				break;
			default:
				queryBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				countBuiler.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}
			return infoListService.getStatusList(queryBuilder, loginUser,sim,irSimflag,false,false,"column");
		} else if ("微信".equals(fenlei)) {
			switch (sort) { // 排序
			case "desc":
				queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				return infoListService.getHotListWeChat(queryBuilder, countBuiler, loginUser,"column");
			default:
				queryBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}
			countBuiler.setDatabase(Const.WECHAT);
			String groupName = indexTab.getGroupName();
			return infoListService.getWeChatList(queryBuilder, loginUser,sim,irSimflag,false,false,"column");
		} else {// 传统
			queryBuilder.setDatabase(Const.HYBASE_NI);
			switch (sort) { // 排序
			case "desc":
				queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				countBuiler.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				countBuiler.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				return infoListService.getHotList(queryBuilder, countBuiler, loginUser,"column");
			default:
				queryBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				countBuiler.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}
			countBuiler.setDatabase(Const.HYBASE_NI);
			return infoListService.getDocList(queryBuilder, loginUser, true,false,false,false,"column");
		}
	}

	/**
	 * 首页地域
	 */
	@Override
	public Object arealist(QueryBuilder indexBuilder, QueryBuilder countBuiler, String sort, String area, String source,
			String timeRange, String keywords) throws TRSException {
		// 取地域名
		if (StringUtil.isNotEmpty(area)) {
			String province = districtInfoService.province(area);
			// 地域只传一个 所以不用分割了
			area = "中国\\\\" + province + "*";
			indexBuilder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + area);
			countBuiler.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + area);
		}
		// loginUser
		User loginUser = UserUtils.getUser();
		// 分用哪个方法
		if ("微博".equals(source)) {
			if (StringUtil.isNotEmpty(keywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_STATUS_CONTENT).append(":").append(keywords)
						.toString();
				indexBuilder.filterByTRSL(trsl);
				countBuiler.filterByTRSL(trsl);
			}
			indexBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange),
					Operator.Between);
			countBuiler.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange),
					Operator.Between);
			// 然后还得分是否是按照热度排序
			switch (sort) { // 排序
			case "desc":
				indexBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				countBuiler.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				indexBuilder.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
				countBuiler.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
				break;
			case "hot":
				return infoListService.getHotListStatus(indexBuilder, countBuiler, loginUser,"column");
			default:
				indexBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				countBuiler.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}
			return infoListService.getStatusList(indexBuilder, loginUser,true,false,false,false,"column");
		} else if ("微信".equals(source)) {
			indexBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange),
					Operator.Between);
			countBuiler.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange), Operator.Between);
			// 现在在结果中搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":").append(keywords)
						.append(" OR ").append(FtsFieldConst.FIELD_CONTENT).append(":").append(keywords).toString();
				indexBuilder.filterByTRSL(trsl);
				countBuiler.filterByTRSL(trsl);
			}
			switch (sort) { // 排序
			case "desc":
				indexBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				indexBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				return infoListService.getHotListWeChat(indexBuilder, countBuiler, loginUser,"column");
			default:
				indexBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}
			countBuiler.setDatabase(Const.WECHAT);
			log.info(indexBuilder.asTRSL());
			return infoListService.getWeChatList(indexBuilder, loginUser,true,false,false,false,"column");
		} else {// 传统
			indexBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange),
					Operator.Between);
			countBuiler.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange), Operator.Between);
			if (StringUtil.isNotEmpty(keywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":").append(keywords)
						.append(" OR ").append(FtsFieldConst.FIELD_ABSTRACTS).append(":").append(keywords)
						.append(" OR ").append(FtsFieldConst.FIELD_CONTENT).append(":").append(keywords).append(" OR ")
						.append(FtsFieldConst.FIELD_KEYWORDS).append(":").append(keywords).toString();
				indexBuilder.filterByTRSL(trsl);
				countBuiler.filterByTRSL(trsl);
			}
			if (!"ALL".equals(source)) {
				// 单选状态
				if ("国内新闻".equals(source)) {
					String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 NOT ")
							.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
					indexBuilder.filterByTRSL(trsl);
					countBuiler.filterByTRSL(trsl);
				} else if ("国内论坛".equals(source)) {
					String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 OR ")
							.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
					indexBuilder.filterByTRSL(trsl);
					countBuiler.filterByTRSL(trsl);
				} else {
					indexBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
					countBuiler.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
				}
			}
			switch (sort) { // 排序
			case "desc":
				indexBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				countBuiler.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				indexBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				countBuiler.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				return infoListService.getHotList(indexBuilder, countBuiler, loginUser,"column");
			default:
				indexBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				countBuiler.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}
			countBuiler.setDatabase(Const.HYBASE_NI);
			indexBuilder.setDatabase(Const.HYBASE_NI);
			return infoListService.getDocList(indexBuilder, loginUser, false,false,false,false,"column");
		}
	}

	@Override
	public Object hotKeywordList(QueryBuilder builder, String sort, String area, String source, String timeRange,
			String hotKeywords, String keywords) throws TRSException {
		QueryBuilder countBuiler = new QueryBuilder();
		// 取地域名
		if (!"ALL".equals(area)) {
			String province = districtInfoService.province(area);
			// 地域只传一个 所以不用分割了
			area = "中国\\\\" + province + "*";
			builder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" + area);
		}
		// 取loginUser
		User loginUser = UserUtils.getUser();
		// 分用哪个方法
		if ("微博".equals(source)) {
			if (StringUtil.isNotEmpty(hotKeywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_STATUS_CONTENT).append(":")
						.append(hotKeywords).toString();
				builder.filterByTRSL(trsl);
			}
			// 结果中搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_STATUS_CONTENT).append(":").append(keywords)
						.toString();
				builder.filterByTRSL(trsl);
			}
			builder.filterField(FtsFieldConst.FIELD_CREATED_AT, DateUtil.formatTimeRange(timeRange), Operator.Between);
			// 然后还得分是否是按照热度排序
			switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_CREATED_AT, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
				break;
			case "hot":
				return infoListService.getHotListStatus(builder, countBuiler, loginUser,"column");
			default:
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}
			return infoListService.getStatusList(builder, loginUser,true,false,false,false,"column");
		} else if ("微信".equals(source)) {
			builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange), Operator.Between);
			if (StringUtil.isNotEmpty(hotKeywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":").append(hotKeywords)
						.append(" OR ").append(FtsFieldConst.FIELD_CONTENT).append(":").append(hotKeywords).toString();
				builder.filterByTRSL(trsl);
			}
			// 结果中搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":").append(keywords)
						.append(" OR ").append(FtsFieldConst.FIELD_CONTENT).append(":").append(keywords).toString();
				builder.filterByTRSL(trsl);
			}
			switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				return infoListService.getHotListWeChat(builder, countBuiler, loginUser,"column");
			default:
				builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				break;
			}
			log.info(builder.asTRSL());
			return infoListService.getWeChatList(builder, loginUser,true,false,false,false,"column");
		} else {// 传统
			if (StringUtil.isNotEmpty(timeRange)) {
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(timeRange), Operator.Between);
			}
			if (StringUtil.isNotEmpty(hotKeywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":").append(hotKeywords)
						.append(" OR ").append(FtsFieldConst.FIELD_ABSTRACTS).append(":").append(hotKeywords)
						.append(" OR ").append(FtsFieldConst.FIELD_CONTENT).append(":").append(hotKeywords)
						.append(" OR ").append(FtsFieldConst.FIELD_KEYWORDS).append(":").append(hotKeywords).toString();
				builder.filterByTRSL(trsl);
			} // 结果中搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":").append(keywords)
						.append(" OR ").append(FtsFieldConst.FIELD_ABSTRACTS).append(":").append(keywords)
						.append(" OR ").append(FtsFieldConst.FIELD_CONTENT).append(":").append(keywords).append(" OR ")
						.append(FtsFieldConst.FIELD_KEYWORDS).append(":").append(keywords).toString();
				builder.filterByTRSL(trsl);
			}
			// if (!"ALL".equals(source)) {
			// // 单选状态
			// if ("国内新闻".equals(source)) {
			// String trsl = new
			// StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 NOT ")
			// .append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
			// builder.filterByTRSL(trsl);
			// } else if ("国内论坛".equals(source)) {
			// String trsl = new
			// StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 OR ")
			// .append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
			// builder.filterByTRSL(trsl);
			// } else {
			// builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source,
			// Operator.Equal);
			// }
			// }
			switch (sort) { // 排序
			case "desc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			case "asc":
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
				break;
			case "hot":
				return infoListService.getHotList(builder, countBuiler, loginUser,"column");
			default:
				// builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
				builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				break;
			}
			builder.setDatabase(Const.HYBASE_NI);
			log.info("热搜列表" + builder.asTRSL());
			return infoListService.getDocList(builder, loginUser, false,false,false,false,"column");
		}
	}

	/**
	 * 转换 实体类型
	 *
	 * @param document
	 *            需转换类型 FtsDocument FtsDocumentWeChat FtsDocumentStatus
	 * @return 最终所需类型 FtsDocumentChaos
	 */
	private FtsDocumentChaos changeClass(IDocument document) {
		FtsDocumentChaos ftsDocumentChaos = new FtsDocumentChaos();

		Field[] declaredFields = document.getClass().getDeclaredFields();
		for (int i = 0; i < declaredFields.length; i++) {
			String name = declaredFields[i].getName();
			Class<?> type = declaredFields[i].getType();
			// 通过反射拼类中的get方法
			String getMehtodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
			if (boolean.class.equals(type)) {
				getMehtodName = "is" + name.substring(0, 1).toUpperCase() + name.substring(1);
			}
			// 获取当前参数 类名
			String simpleName = document.getClass().getSimpleName();
			Method getMethod = null;
			try {
				if (simpleName.equals("FtsDocument")) {
					// 返回方法对象 参数一：方法的名字 参数二：方法的参数类型
					// 形参类型
					getMethod = FtsDocument.class.getDeclaredMethod(getMehtodName, new Class[] {});
				} else if (simpleName.equals("FtsDocumentWeChat")) {
					getMethod = FtsDocumentWeChat.class.getDeclaredMethod(getMehtodName, new Class[] {});
				} else if (simpleName.equals("FtsDocumentStatus")) {
					getMethod = FtsDocumentStatus.class.getDeclaredMethod(getMehtodName, new Class[] {});
				}

				// 执行方法 参数一：执行那个对象中的方法 参数二：该方法的参数
				Object invoke = getMethod.invoke(document, new Object[] {});

				// 实体类 字段匹配 获取相应值
				if ("sid".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setSid(invoke.toString());
					}
				} else if ("title".equals(name) || "urlTitle".equals(name) || "statusContent".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setUrlTitle(invoke.toString());
					}
				} else if ("md5Tag".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setMd5Tag(invoke.toString());
					}
				} else if ("appraise".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setAppraise(invoke.toString());
					}
				} else if ("screenName".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setScreenName(invoke.toString());
					}
				} else if ("rttCount".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setRttCount(Long.valueOf(invoke.toString()));
					}
				} else if ("commtCount".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setCommtCount(Long.valueOf(invoke.toString()));
					}
				} else if ("hkey".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setHkey(invoke.toString());
					}
				} else if ("mid".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setMid(invoke.toString());
					}
				} else if ("siteName".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setSiteName(invoke.toString());
					}
				} else if ("authors".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setAuthors(invoke.toString());
					}
				} else if ("urlName".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setUrlName(invoke.toString());
					}
				} else if ("nreserved1".equals(name)) {
					if (null != invoke) {
						ftsDocumentChaos.setNreserved1(invoke.toString());
					}
				} else if ("urlTime".equals(name) || "createdAt".equals(name)) {
					if (null != invoke) {
						// 日期类型
						// SimpleDateFormat sdf = new
						// SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						// String format = sdf.format(invoke);
						ftsDocumentChaos.setUrlTime((Date) invoke);
						// ftsDocumentChaos.setUrlTime(sdf.parse(format));
					}
				}

				if (simpleName.equals("FtsDocument")) {
					if ("groupName".equals(name)) {
						ftsDocumentChaos.setGroupName(invoke.toString());
					}
					Map<String, String> timeDifference = DateUtil.timeDifference((FtsDocument) document);
					boolean isNew = false;
					if (ObjectUtil.isNotEmpty(timeDifference.get("timeAgo"))) {
						isNew = true;
						ftsDocumentChaos.setTimeAgo(timeDifference.get("timeAgo"));
						ftsDocumentChaos.setNew(isNew);
					} else {
						ftsDocumentChaos.setTimeAgo(timeDifference.get("urlTime"));
						ftsDocumentChaos.setNew(isNew);
					}
				} else if (simpleName.equals("FtsDocumentWeChat")) {
					if ("groupName".equals(name)) {
						ftsDocumentChaos.setGroupName("微信");
					}
					Map<String, String> timeDifference = DateUtil.timeDifference((FtsDocumentWeChat) document);
					boolean isNew = false;
					if (ObjectUtil.isNotEmpty(timeDifference.get("timeAgo"))) {
						isNew = true;
						ftsDocumentChaos.setTimeAgo(timeDifference.get("timeAgo"));
						ftsDocumentChaos.setNew(isNew);
					} else {
						ftsDocumentChaos.setTimeAgo(timeDifference.get("urlTime"));
						ftsDocumentChaos.setNew(isNew);
					}
				} else if (simpleName.equals("FtsDocumentStatus")) {
					if ("groupName".equals(name)) {
						ftsDocumentChaos.setGroupName("微博");
					}
					Map<String, String> timeDifference = DateUtil.timeDifference((FtsDocumentStatus) document);
					boolean isNew = false;
					if (ObjectUtil.isNotEmpty(timeDifference.get("timeAgo"))) {
						isNew = true;
						ftsDocumentChaos.setTimeAgo(timeDifference.get("timeAgo"));
						ftsDocumentChaos.setNew(isNew);
					} else {
						ftsDocumentChaos.setTimeAgo(timeDifference.get("createdAt"));
						ftsDocumentChaos.setNew(isNew);
					}
				}

			} catch (NoSuchMethodException e) {
				log.error("ColumnServiceImpl.java  changeClass（） fail: NoSuchMethodException" + e.getMessage());
			} catch (IllegalAccessException e) {
				log.error("ColumnServiceImpl.java  changeClass（） fail: IllegalAccessException" + e.getMessage());
			} catch (InvocationTargetException e) {
				log.error("ColumnServiceImpl.java  changeClass（） fail: InvocationTargetException" + e.getMessage());
			}
		}
		return ftsDocumentChaos;
	}

	private Map<String, String> timeDifference(FtsDocumentChaos doc) {
		Map<String, String> map = new HashMap<>();
		long nowTime = new Date().getTime();
		long lastTime = doc.getUrlTime().getTime();
		long result = nowTime - lastTime;
		int days = (int) (result / (1000 * 60 * 60 * 24));
		if (days < 1) {
			// 计算小时查
			int hours = (int) (result / (1000 * 60 * 60));
			// 计算分钟差
			int minutes = (int) (result / (1000 * 60));
			if (0 == minutes) {
				map.put("timeAgo", "刚刚");
			} else if (0 != minutes) {
				map.put("timeAgo", minutes + "分钟前");
			}
			if (0 != hours) {
				map.put("timeAgo", hours + "小时前");
			}
			// if (0 != days) {
			// map.put("timeAgo", days + "天前");
			// }
		} else {
			map.put("urlTime", DateUtil.date2String(doc.getUrlTime(), DateUtil.FMT_TRS_yMdhms));
		}

		return map;
	}

	/**
	 * 查询列表
	 */
	@Override
	public Object selectList(String indexMapperId,int pageNo,int pageSize,String source,String emotion,String entityType,
			String dateTime,String key,String sort,String area,String irKeyword,String invitationCard,String keywords,String fuzzyValueScope,
			String forwarPrimary,boolean isExport) {
		String userName = UserUtils.getUser().getUserName();
		long start = new Date().getTime();
		if ("电子报".equals(source)) {
			source = "国内新闻_电子报";
		} else if ("论坛".equals(source)) {
			source = "国内论坛";
		} else if ("新闻".equals(source)) {
			source = "国内新闻";
		} else if ("博客".equals(source)) {
			source = "国内博客";
		} else if ("境外媒体".equals(source)) {
			source = "国外新闻";
		} else if ("国内新闻_客户端".equals(source) || "客户端".equals(source)) {
			source = "国内新闻_手机客户端";
		}/*else if("微信".equals(source)){
			source = "国内微信";
		}*/
		IndexTabMapper mapper = indexTabMapperService.findOne(indexMapperId);
		IndexTab indexTab = mapper.getIndexTab();
		if (indexTab != null) {
			String timerange = indexTab.getTimeRange();
		try{
			AbstractColumn column = ColumnFactory.createColumn(indexTab.getType());
			ColumnConfig config = new ColumnConfig();
			config.initSection(indexTab, timerange, pageNo, pageSize ,source,emotion, entityType,dateTime,key,sort,area,irKeyword, invitationCard,keywords,fuzzyValueScope,forwarPrimary);
			column.setHybase8SearchService(hybase8SearchService);
			column.setChartAnalyzeService(chartAnalyzeService);
			column.setInfoListService(infoListService);
			column.setAlertRepository(alertRepository);
			column.setFavouritesRepository(favouritesRepository);
			column.setConfig(config);
			Object list = column.getSectionList();
			if(isExport){
				//存入缓存  以便混合列表导出时使用
				InfoListResult sectionList = (InfoListResult) list;
				PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>)sectionList.getContent();
				List<FtsDocumentCommonVO> listVo = content.getPageItems();
				RedisUtil.setMix(indexMapperId, listVo);
			}
			return list;
		}catch(Exception e){
			log.info(e.toString());
		}finally{
			long end = new Date().getTime();
			long timeApi = end - start;
			if(userName!=null && userName.equals("xiaoying")){
				log.info("xiaoying调用接口用了" + timeApi + "ms");
			}
		}
	}
		return null;
	}
	
	/**
	 * 日常监测饼图和柱状图数据的导出
	 */
	@Override
	public ByteArrayOutputStream exportData(JSONArray array) throws IOException {
		ExcelData content = new ExcelData();
		content.setHead(ExcelConst.HEAD_PIE_BAR);  // { "媒体来源", "数值"}
		for (Object object : array) {
			JSONObject parseObject = JSONObject.parseObject(String.valueOf(object));
			
			String groupNameValue = parseObject.get("groupName").toString();
			String numValue = parseObject.get("num").toString();
			
			if("国内新闻".equals(groupNameValue)){
				groupNameValue = "新闻";
			}else if("国内新闻_电子报".equals(groupNameValue)){
				groupNameValue = "电子报";
			}else if("国内新闻_手机客户端".equals(groupNameValue)){
				groupNameValue = "客户端";
			}else if("国内论坛".equals(groupNameValue)){
				groupNameValue = "论坛";
			}else if("国内微信".equals(groupNameValue)){
				groupNameValue = "微信";
			}else if("国内博客".equals(groupNameValue)){
				groupNameValue = "博客";
			}
			content.addRow(groupNameValue, numValue);
		}
		return ExcelFactory.getInstance().export(content);
	}
	
	/**
	 * 折线图数据导出
	 */
//	@Override
//	public ByteArrayOutputStream exportChartLine(JSONArray array) throws IOException {
//		ExcelData data = new ExcelData();
//		//单独循环设置表头
//		String[] header = null;
//		for (Object object : array) {
//
//			JSONObject parseObject = JSONObject.parseObject(String.valueOf(object));
//
//			String timeAndCount = parseObject.get("data").toString();
//			JSONArray timeAndCountArray = JSONObject.parseArray(timeAndCount);
//			String headArray = "媒体来源";
//			//用于动态设置表头数据
////			List<String> list = new ArrayList<String>();
//			boolean hasHeader = false;
//			if(!hasHeader){
//				for (Object object2 : timeAndCountArray) {
//					JSONObject parseObject2 = JSONObject.parseObject(String.valueOf(object2));
//					String fieldValue = parseObject2.get("fieldValue").toString();
//					if(StringUtil.isNotEmpty(fieldValue)){
//						headArray += ", " + fieldValue;
//					}
//					continue;
//				}
//				header = headArray.split(",");
//			}
//			break;
//		}
//		data.setHead(header);
//		for (Object object : array) {
//
//			JSONObject parseObject = JSONObject.parseObject(String.valueOf(object));
//
//			String timeAndCount = parseObject.get("data").toString();
//			JSONArray timeAndCountArray = JSONObject.parseArray(timeAndCount);
//
//			String groupName = parseObject.get("groupName").toString();
//			if("国内新闻".equals(groupName)){
//				groupName = "新闻";
//			}else if("国内新闻_电子报".equals(groupName)){
//				groupName = "电子报";
//			}else if("国内新闻_手机客户端".equals(groupName)){
//				groupName = "客户端";
//			}else if("国内论坛".equals(groupName)){
//				groupName = "论坛";
//			}else if("国内微信".equals(groupName)){
//				groupName = "微信";
//			}else if("国内博客".equals(groupName)){
//				groupName = "博客";
//			}
//			String rowOne = groupName;
//			for (Object object2 : timeAndCountArray) {
//				JSONObject parseObject2 = JSONObject.parseObject(String.valueOf(object2));
//				String count = parseObject2.get("count").toString();
//				if(StringUtil.isNotEmpty(count)){
//					rowOne += "," + count;
//				}
//			}
//			data.addRow(rowOne.split(","));
//		}
//		return ExcelFactory.getInstance().export(data);
//	}
//
    @Override
    public ByteArrayOutputStream exportChartLine(JSONArray array) throws IOException {
        ExcelData data = new ExcelData();
        //单独循环设置表头
        String[] header = null;
        String headArray = "媒体来源";
        for (Object object : array) {

            JSONObject parseObject = JSONObject.parseObject(String.valueOf(object));

            String groupName = parseObject.get("groupName").toString();
            if("国内新闻".equals(groupName)){
                groupName = "新闻";
            }else if("国内新闻_电子报".equals(groupName)){
                groupName = "电子报";
            }else if("国内新闻_手机客户端".equals(groupName)){
                groupName = "客户端";
            }else if("国内论坛".equals(groupName)){
                groupName = "论坛";
            }else if("国内微信".equals(groupName)){
                groupName = "微信";
            }else if("国内博客".equals(groupName)){
                groupName = "博客";
            }

            if(StringUtil.isNotEmpty(groupName)){
                headArray += ", " + groupName;
            }
        }
        header = headArray.split(",");

        data.setHead(header);


        List<String[]> arrayList = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {

            JSONObject parseObject = JSONObject.parseObject(String.valueOf(array.get(i)));

            String timeAndCount = parseObject.get("data").toString();
            JSONArray timeAndCountArray = JSONObject.parseArray(timeAndCount);
            for (int j = 0; j < timeAndCountArray.size(); j++) {
                String[] arr = null;
                if (0 == i){
                    arr = new String[array.size()+1];
                }else {
                    arr = arrayList.get(j);
                }
                Object o = timeAndCountArray.get(j);
                JSONObject parseObject2 = JSONObject.parseObject(String.valueOf(o));
                if (0==i){
                    arr[i] = parseObject2.get("fieldValue").toString();
                    arr[i+1] = parseObject2.get("count").toString();
                    arrayList.add(arr);
                }else {
                    arr[i+1] = parseObject2.get("count").toString();
                }
            }
        }
        for (String[] strings : arrayList) {
            data.addRow(strings);
        }
        return ExcelFactory.getInstance().export(data);
    }

    /**
	 * 词云数据的导出
	 */
	@Override
	public ByteArrayOutputStream exportWordCloud(String dataType,JSONArray array) throws IOException {
		ExcelData content = new ExcelData();
		content.setHead(ExcelConst.HEAD_WORDCLOUD); // {"词语", "所属分组", "信息数量"}
		for (Object object : array) {
			JSONObject parseObject = JSONObject.parseObject(String.valueOf(object));
			
			String word = parseObject.get("fieldValue").toString();
			String count = parseObject.get("count").toString();
			String group = "";
			if ("通用".equals(dataType)){
				group = parseObject.get("entityType").toString();
			}

			if("地域".equals(dataType) || "location".equals(group)){
				group = "地域";
			}else if("机构".equals(dataType) || "agency".equals(group)){
				group = "机构";
			}else{
				group = "人物";
			}

			content.addRow(word, group, count);
			
//			String groupList = parseObject.get("groupList").toString();
//			JSONArray dataArray = JSONObject.parseArray(groupList);
//			for (Object data : dataArray) {
//				JSONObject jSONObject = JSONObject.parseObject(String.valueOf(data));
//				String word = jSONObject.get("fieldValue").toString();
//				String count = jSONObject.get("count").toString();
//				String group = jSONObject.get("entityType").toString();
//				content.addRow(word, group, count);
//			}
			
			
		}
		return ExcelFactory.getInstance().export(content);
	}
	
	/**
	 * 地域图数据的导出
	 */
	@Override
	public ByteArrayOutputStream exportMap(JSONArray array) throws IOException {
		ExcelData content = new ExcelData();
		content.setHead(ExcelConst.HEAD_MAP); // { "地域", "信息数量"};
		array.sort(Comparator.comparing(obj -> {
			JSONObject parseObject = JSONObject.parseObject(String.valueOf(obj));
			Long value = parseObject.getLongValue("areaCount");
			return value;
		}).reversed());
		for (Object object : array) {
			JSONObject parseObject = JSONObject.parseObject(String.valueOf(object));
			
			String areaName = parseObject.get("areaName").toString();
			String areaCount = parseObject.get("areaCount").toString();
			content.addRow(areaName, areaCount);
		}
		return ExcelFactory.getInstance().export(content);
	}
}
