
package com.trs.netInsight.widget.special.service.impl;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.ESFieldConst;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.cache.RedisFactory;
import com.trs.netInsight.support.cache.TimingCachePool;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.annotation.parser.FtsParser;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.*;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.model.result.IDocument;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.support.template.GUIDGenerator;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.entity.enums.Store;
import com.trs.netInsight.widget.alert.service.IAlertService;
import com.trs.netInsight.widget.analysis.entity.ClassInfo;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.report.entity.Favourites;
import com.trs.netInsight.widget.report.service.IFavouritesService;
import com.trs.netInsight.widget.special.entity.*;
import com.trs.netInsight.widget.special.entity.enums.SearchPage;
import com.trs.netInsight.widget.special.service.IInfoListService;
import com.trs.netInsight.widget.special.service.ISpecialProjectService;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 信息列表
 * <p>
 * Created by ChangXiaoyang on 2017/5/4.
 */
@Service
@Slf4j
@Transactional
@SuppressWarnings({ "unchecked", "rawtypes" })
public class InfoListServiceImpl implements IInfoListService {

	@Autowired
	private IFavouritesService favouritesService;

	@Autowired
	private FullTextSearch hybase8SearchService;

	@Autowired
	private ISpecialProjectService specialProjectService;

	@Autowired
	private IAlertService alertService;

	@Autowired
	private OrganizationRepository organizationService;

	@Autowired
	private ICommonListService commonListService;

	/**
	 * 是否走独立预警服务
	 */
	@Value("${http.client}")
	private boolean httpClient;
	/**
	 * 独立预警服务地址
	 */
	@Value("${http.alert.netinsight.url}")
	private String alertNetinsightUrl;

	/**
	 * 线程池跑任务
	 */
	private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);

	/**
	 * 从缓存中获取下一页的信息
	 *
	 * @param pageId
	 *            下一页的ID
	 * @return InfoListResult
	 */
	@Override
	public InfoListResult getNextList(String pageId) throws TRSException, InterruptedException {
		int times = 0;
		String valueFromRedis = "";
		while (++times < 20) {
			InfoListResult result = TimingCachePool.get(pageId);
			if (ObjectUtil.isNotEmpty(result)) {
				return result;
			}
			valueFromRedis = RedisFactory.getValueFromRedis(pageId);
			if (StringUtil.isNotEmpty(valueFromRedis)) {
				break;
			}
			Thread.sleep(500);
		}
		if (times == 20) {
			throw new OperationException("获取信息失败");
		}

		return ObjectUtil.toObject(valueFromRedis, InfoListResult.class);
	}

	/**
	 * 获取异步数据
	 *
	 * @param pageId
	 *            页码Id
	 * @return List
	 */
	@Override
	public List getAsyncList(String pageId) throws TRSException, InterruptedException {
		int times = 0;
		String valueFromRedis = "";
		while (++times < 40) {
			List<AsyncDocument> list = TimingCachePool.get("async:" + pageId);
			if (ObjectUtil.isNotEmpty(list)) {
				return list;
			}
			valueFromRedis = RedisFactory.getValueFromRedis("async:" + pageId);
			if (StringUtil.isNotEmpty(valueFromRedis)) {
				break;
			}
			Thread.sleep(4000);
		}
		if (times == 40) {
			throw new OperationException("获取信息失败：相似文章数");
		}
		// 关闭线程
		// fixedThreadPool.shutdown();
		return ObjectUtil.toObject(valueFromRedis, List.class);
	}

	/**
	 * 获取异步数据
	 * <p>
	 * 页码Id
	 *
	 * @return List
	 * @throws InterruptedException
	 */
	@Override
	public Object getOneAsy(String sid) throws TRSException, InterruptedException {
		int times = 0;
		String valueFromRedis = "";
		while (++times < 20) {
			Object object = TimingCachePool.get("async:" + sid);
			if (ObjectUtil.isNotEmpty(object)) {
				return object;
			}
			valueFromRedis = RedisFactory.getValueFromRedis("async:" + sid);
			if (StringUtil.isNotEmpty(valueFromRedis)) {
				break;
			}
			Thread.sleep(1000);
		}
		if (times == 20) {
			throw new OperationException("获取信息失败");
		}
		// 关闭线程
		// fixedThreadPool.shutdown();
		if (StringUtil.isEmpty(valueFromRedis)) {
			return null;
		}
		return ObjectUtil.toObject(valueFromRedis, List.class);
	}

	/**
	 * @param result
	 * @param md5List
	 * @return
	 */
	@Override
	public List<FtsDocument> resultByMd5(List<FtsDocument> result, List<String> md5List, GroupResult md5TAG) {
		// 如果用同一个的话 会覆盖
		List<FtsDocument> resultByMd5 = new ArrayList<>();
		for (FtsDocument chartAnalyzeEntity : result) {
			resultByMd5.add(chartAnalyzeEntity);
		}
		for (FtsDocument ftsDocument : result) {
			int ind = md5List.indexOf(ftsDocument.getMd5Tag());
			if (ind >= 0 && ind < result.size()) {
				resultByMd5.set(ind, ftsDocument);
			}
		}
		return resultByMd5;
	}

	/**
	 * 获取异步数据 - 相似文章数对应的发文网站信息
	 *
	 * @param pageId 页码Id
	 * @return List
	 */
	@Override
	public List getAsySiteNameList(String pageId) throws TRSException, InterruptedException {
		int times = 0;
		String valueFromRedis = "";
		while (++times < 40) {
			List<AsySiteNameDocument> list = TimingCachePool.get("asySiteName:" + pageId);
			if (ObjectUtil.isNotEmpty(list)) {
				return list;
			}
			valueFromRedis = RedisFactory.getValueFromRedis("asySiteName:" + pageId);
			if (StringUtil.isNotEmpty(valueFromRedis)) {
				break;
			}
			Thread.sleep(4000);
		}
		if (times == 40) {
			throw new OperationException("获取信息失败：相似文章对应的发文网站");
		}
		// 关闭线程
		// fixedThreadPool.shutdown();
		return ObjectUtil.toObject(valueFromRedis, List.class);
	}

	/**
	 * 在相同文章的基础上求发表相似文章的对应媒体网站
	 *
	 * @param pageId    页面id
	 * @param trslk     表达式key
	 * @param server    权重
	 * @param type      类型
	 * @param asyncInfoList 相似文章数数据
	 */
	private void querySiteNameForSimMd5(String pageId, String trslk, boolean server, String type, List<AsyncInfo> asyncInfoList) {
		//五大商业媒体 ：IR_INDUSTRY    =1 时
		//网信办白名单 ： IR_WXB_LIST =0
		try {
			log.info("相似文章的发文网站计算：" + "asySiteName:" + pageId + "开始");
			String trsl = RedisUtil.getString(trslk);
			if (StringUtil.isNotEmpty(trsl)) {
				trsl = removeSimflag(trsl);
			}
		/*
			infoList中的字段：
			id：sid --用处不大
			md5：MD5
			database：查询数据库
			simNum：相似文章数
		 */
			//因为是求相似文章数的发文网站，所以遵守相似文章计算的排重规则
			Boolean sim = false;
			Boolean irSimflag = true;
			Boolean irSimflagAll = false;
			List<AsySiteNameDocument> resultList = new ArrayList<>();
			for (AsyncInfo asyncInfo : asyncInfoList) {
				AsyncDocument asy = asyncInfo.getAsyncDocument();
				String id = asy.getId();
				String md5 = asyncInfo.getMd5();
				Long simNum = asy.getSimNum();
				String database = asyncInfo.getDatabase();
				String groupName = asyncInfo.getGroupName();
				AsySiteNameDocument asySiteNameDocument = new AsySiteNameDocument();
				asySiteNameDocument.setId(id);
				asySiteNameDocument.setMd5(md5);
				asySiteNameDocument.setSimNum(simNum);
				if (simNum > 0) {
					//需要计算发文网站信息
					//如果最多展示3个。网站类型分3类，网信办白名单、五大商业媒体、其他普通
					if (StringUtil.isNotEmpty(md5)) {
						List<Object> siteNames = new ArrayList<>();

						QueryBuilder searchBuilder = new QueryBuilder();

						searchBuilder.filterField(FtsFieldConst.FIELD_MD5TAG, md5, Operator.Equal);
						StringBuffer idBuffer = new StringBuffer();
						if(Const.MEDIA_TYPE_WEIBO.contains(groupName)){
							idBuffer.append(" NOT ").append(FtsFieldConst.FIELD_MID).append(":(").append(id).append(")");
						}else if(Const.MEDIA_TYPE_WEIXIN.contains(groupName)){
							idBuffer.append(" NOT ").append(FtsFieldConst.FIELD_HKEY).append(":(").append(id).append(")");
						}else{
							idBuffer.append(" NOT ").append(FtsFieldConst.FIELD_SID).append(":(").append(id).append(")");
						}
						String trslFilter = trsl + idBuffer.toString();
						searchBuilder.filterByTRSL(trslFilter);
						//网信办白名单
						QueryBuilder searchBuilder_wxb = new QueryBuilder();
						searchBuilder_wxb.filterByTRSL("(" + FtsFieldConst.FIELD_WXB_LIST + ":(0))");
						searchBuilder_wxb.filterByTRSL(searchBuilder.asTRSL());
						List<GroupInfo> list = new ArrayList<>();
						GroupResult categoryInfos = null;
						try {
							//计算网信办白名单的
							categoryInfos = hybase8SearchService.categoryQuery(server, searchBuilder_wxb.asTRSL(), sim, irSimflag, irSimflagAll,
									FtsFieldConst.FIELD_SITENAME, 3, type, database);
						} catch (TRSSearchException e) {
							throw new TRSSearchException(e);
						}
						if (categoryInfos != null && categoryInfos.getGroupList() != null && categoryInfos.getGroupList().size() > 0) {
							list.addAll(categoryInfos.getGroupList());
						}
						if (list.size() < 3) {
							//五大商业媒体
							QueryBuilder searchBuilder_industry = new QueryBuilder();
							searchBuilder_industry.filterByTRSL("(" + FtsFieldConst.FIELD_INDUSTRY + ":(1))");
							searchBuilder_industry.filterByTRSL(searchBuilder.asTRSL());
							searchBuilder_industry.filterByTRSL_NOT(FtsFieldConst.FIELD_WXB_LIST + ":(0)");
							try {
								//计算五大商业媒体
								categoryInfos = hybase8SearchService.categoryQuery(server, searchBuilder_industry.asTRSL(), sim, irSimflag, irSimflagAll,
										FtsFieldConst.FIELD_SITENAME, 3, type, database);
							} catch (TRSSearchException e) {
								throw new TRSSearchException(e);
							}
							if (categoryInfos != null && categoryInfos.getGroupList() != null && categoryInfos.getGroupList().size() > 0) {
								list.addAll(categoryInfos.getGroupList());
							}
						}
						if (list.size() < 3) {
							searchBuilder.filterByTRSL_NOT(FtsFieldConst.FIELD_WXB_LIST + ":(0)");
							searchBuilder.filterByTRSL_NOT(FtsFieldConst.FIELD_INDUSTRY + ":(1)");
							try {
								//计算普通媒体的
								categoryInfos = hybase8SearchService.categoryQuery(server, searchBuilder.asTRSL(), sim, irSimflag, irSimflagAll,
										FtsFieldConst.FIELD_SITENAME, 3, type, database);
							} catch (TRSSearchException e) {
								throw new TRSSearchException(e);
							}
							if (categoryInfos != null && categoryInfos.getGroupList() != null && categoryInfos.getGroupList().size() > 0) {
								list.addAll(categoryInfos.getGroupList());
							}
						}
						if (list.size() > 0) {
							int size = 3;
							if (list.size() < 3) {
								size = list.size();
							}
							for (int i = 0; i < size; i++) {
								GroupInfo groupInfo = list.get(i);
								String siteName = groupInfo.getFieldValue();
								Long num = groupInfo.getCount();
								Map<String, Object> map = new HashMap<>();
								map.put("sitename", siteName);
								map.put("num", num);
								siteNames.add(map);
							}
							asySiteNameDocument.setSitenames(siteNames);
						} else {
							asySiteNameDocument.setSitenames("");
						}
					} else {
						asySiteNameDocument.setSitenames("");
					}
				} else {
					asySiteNameDocument.setSitenames("");
				}
				resultList.add(asySiteNameDocument);
			}
			log.info("相似文章的发文网站计算：" + "asySiteName:" + pageId + "完成，数据为：" + resultList.size());
			TimingCachePool.put("asySiteName:" + pageId, resultList);
			RedisFactory.setValueToRedis("asySiteName:" + pageId, resultList);
		} catch (Exception e) {
			log.error("相似文章的发文网站计算信息失败", e);
		}
	}

	private String removeSimflag(String trslk) {
		if(trslk.indexOf("AND SIMFLAG:(1000 OR \"\")") != -1){
			trslk = trslk.replace(" AND SIMFLAG:(1000 OR \"\")","");
		}else if (trslk.indexOf("AND (IR_SIMFLAGALL:(\"0\" OR \"\"))") != -1){
			trslk = trslk.replace(" AND (IR_SIMFLAGALL:(\"0\" OR \"\"))","");
		}
		trslk = trslk.replaceAll("AND \\(IR_SIMFLAGALL:\\(\"0\" OR \"\"\\)\\)"," ");
		return trslk;
	}


	@Override
	public InfoListResult getHotList(QueryBuilder builder, QueryBuilder countBuilder, User user,String type)
			throws TRSException{
		String searchPage = SearchPage.COMMON_SEARCH.toString();
		return getHotList(builder, countBuilder, user, type,searchPage);
	}
	/**
	 * 热度排序
	 *
	 * @param builder
	 *            QueryBuilder
	 * @return InfoListResult
	 */
	@Override
	public InfoListResult getHotList(QueryBuilder builder, QueryBuilder countBuilder, User user,String type,String searchPage)
			throws TRSException {
		//  2018-12-18热点信息列表新增支持时间排序，默认是热度排序,,,变更，热点按时间排序无意义
		try {
			int pageSize = builder.getPageSize();
			if (pageSize > 50) {
				pageSize = 50;
				builder.setPageSize(50);
			} else if (pageSize <= 0) {
				pageSize = 10;
				builder.setPageSize(10);
			}
			String database = builder.getDatabase();
			if (database == null) {
				builder.setDatabase(Const.HYBASE_NI_INDEX);
				countBuilder.setDatabase(Const.HYBASE_NI_INDEX);
			}

			// 返回给前端总页数
			int pageListNo = 0;
			// 返回给前端总条数
			int pageListSize = 0;
			String pageId = GUIDGenerator.generate(InfoListServiceImpl.class);
			String nextPageId = GUIDGenerator.generate(InfoListServiceImpl.class);
			List<FtsDocumentCommonVO> ftsDocumentCommonVOS = new ArrayList<>();
			// hybase不能直接分页 每次都统计出50条 然后再取
			long pageNo = builder.getPageNo();
			// 从上一页到这一页 pageSize*pageNo到pageSize*pageNo+pageSize-1
			List<GroupInfo> groupList = new ArrayList();
			// 在这存个空的 在hybase里边填完整的
			String trslk = pageId + "trsl";
			RedisUtil.setString(trslk, builder.asTRSL());
			builder.setKeyRedis(trslk);
			// 这个给异步计算时候用
			String trsHot = pageId + "hot";
			RedisUtil.setString(trsHot, builder.asTRSL());
			String key = CachekeyUtil.getToolKey(user,builder.asTRSL(),"hotchuagntong");
			List<GroupInfo> list = TimingCachePool.get(key);
			if (ObjectUtil.isNotEmpty(list)) {
				groupList = list;
			} else {
				builder.page(0, 50);
				//单独算法,已修改
				GroupResult md5TAG = hybase8SearchService.categoryQuery(builder, false, true,
						false, "MD5TAG",type,
						builder.getDatabase());
				groupList = md5TAG.getGroupList();
				TimingCachePool.put(key, groupList);
			}
			int size = 0;
			String keyCount = CachekeyUtil.getToolKey(user, builder.asTRSL(), "hotchuagntongcount");
			if (ObjectUtil.isNotEmpty(TimingCachePool.get(keyCount))) {
				size = TimingCachePool.get(keyCount);
			} else {
				countBuilder.page(0, 50);
				GroupResult md5TAGCount = hybase8SearchService.categoryQuery(countBuilder, false, true,false, "MD5TAG",
						type,countBuilder.getDatabase());
				size = md5TAGCount.getGroupList().size();
				if (size > 50) {
					size = 50;
				}
				TimingCachePool.put(keyCount, size);
			}
			int start = (int) (pageSize * pageNo);
			int end = (int) (pageSize * pageNo + pageSize - 1);
			if (start >= groupList.size()) {
				return null;
			}
			if (groupList.size() <= end) {
				end = groupList.size() - 1;
			}
			// 返回前端总页数
			pageListNo = groupList.size() % pageSize == 0 ? groupList.size() / pageSize
					: groupList.size() / pageSize + 1;
			pageListSize = groupList.size();
			for (int i = start; i <= end; i++) {
				GroupInfo info = groupList.get(i);
				QueryBuilder builder1 = new QueryBuilder();
				builder1.filterByTRSL(builder.asTRSL());
				builder1.page(0, 1);
				builder1.filterField("MD5TAG", info.getFieldValue(), Operator.Equal);
				builder1.orderBy(FtsFieldConst.FIELD_URLTIME, true);
				List<FtsDocumentCommonVO> pagedList = hybase8SearchService.ftsQuery(builder1, FtsDocumentCommonVO.class,
						false, false,false,type);

				if (ObjectUtil.isNotEmpty(pagedList)) {
					FtsDocumentCommonVO ftsDocument = pagedList.get(0);
					String groupName = ftsDocument.getGroupName();
					if(StringUtils.equals("国内微信", groupName)){
						ftsDocument.setGroupName("微信");
						ftsDocument.setSid(ftsDocument.getHkey());
					}
					// ftsDocument.setTrslk(uid);
					ftsDocument.setTrslk(trslk);
					// 开始进行截取操作， 工具方法暂时不完整
					// ftsDocument.setContent(StringUtil.replaceImg(ftsDocument.getContent()));
					//修改原因 前端取得statusContent
					ftsDocument.setStatusContent(StringUtil.cutContent(StringUtil.replaceImg(ftsDocument.getContent()), 160));
					ftsDocument.setContent(StringUtil.cutContent(StringUtil.replaceImg(ftsDocument.getContent()), 160));
					// 控制标题长度
					if (StringUtil.isNotEmpty(ftsDocument.getTitle())){
						ftsDocument.setTitle( StringUtil.replaceAnnotation(ftsDocument.getTitle()).replace("&amp;nbsp;", ""));
					}
					//ftsDocument.setTitle(StringUtil.cutContent(ftsDocument.getTitle(), 160));
					ftsDocument.setAbstracts(StringUtil.replaceImg(ftsDocument.getAbstracts()));
					ftsDocumentCommonVOS.add(ftsDocument);
				}
			}
			if (0 == ftsDocumentCommonVOS.size()) {
				return null;
			}
			// 查预警
			List<String> idList = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			for (FtsDocumentCommonVO weChat : ftsDocumentCommonVOS) {
				idList.add(weChat.getSid());
				sb.append(weChat.getSid()).append(",");

			}
			//不查预警了
			/*List<String> sidAlert = new ArrayList<>();
			List<AlertEntity> alertList = null;
			if (httpClient){
				String sids = "";
				if (sb.length() > 0){
					sids = sb.substring(0,sb.length()-1);
				}
				alertList = AlertUtil.getAlerts(userId,sids,alertNetinsightUrl);
			}else {
				alertList = alertRepository.findByUserIdAndSidIn(userId, idList);
			}
			if (null != alertList && alertList.size() > 0){
				for (AlertEntity alert : alertList) {
					sidAlert.add(alert.getSid());
				}
			}*/


			List<String> sidFavourites = new ArrayList<>();

			List<Favourites> favouritesList = favouritesService.findAll(user);
			for (Favourites favourites : favouritesList) {
				sidFavourites.add(favourites.getSid());
			}
			for (FtsDocumentCommonVO weChat : ftsDocumentCommonVOS) {

				String id = weChat.getSid();
				// 预警数据
				//不查预警了
				/*int index = sidAlert.indexOf(id);
				if (index < 0) {
					weChat.setSend(false);
				} else {
					weChat.setSend(true);
				}*/
				int indexFavourites = sidFavourites.indexOf(id);
				if (indexFavourites < 0) {
					weChat.setFavourite(false);
				} else {
					weChat.setFavourite(true);
				}
			}

			// 热点信息列表原来是按相似文章数排，现支持时间正序倒序排，但是相似文章数得前端循环匹配了
//			if("-IR_URLTIME".equals(order)){
//				ftsDocumentCommonVOSS = ftsDocumentCommonVOS.stream().sorted((o1, o2) -> o2.getUrlTime().compareTo(o1.getUrlTime())).collect(Collectors.toList());
////				ftsDocumentCommonVOSS = ftsDocumentCommonVOS.stream().sorted(Comparator.comparing(FtsDocumentCommonVO::getUrlTime)).collect(Collectors.toList());
//			}else if("+IR_URLTIME".equals(order)){
//				ftsDocumentCommonVOSS = ftsDocumentCommonVOS.stream().sorted((o1, o2) -> o1.getUrlTime().compareTo(o2.getUrlTime())).collect(Collectors.toList());
//			}else{
//				ftsDocumentCommonVOSS = ftsDocumentCommonVOS;
//			}

			PagedList<FtsDocumentCommonVO> pagedList = new PagedList<FtsDocumentCommonVO>(pageListNo,
					(int) (pageSize < 0 ? 15 : pageSize), pageListSize, ftsDocumentCommonVOS, 1);
			RedisFactory.setValueToRedis(pageId, new InfoListResult<>(pageId, pagedList, nextPageId, size, trslk));
			// final String trsSim = trslk;
			fixedThreadPool.execute(
					() -> calculateSimNumCommon(builder.getDatabase(), pageId, ftsDocumentCommonVOS, user, trsHot, builder.isServer(),type,searchPage));
			return new InfoListResult<>(pageId, pagedList, nextPageId, size, trslk);
			// 把表达式放缓存里边 把key值返回给前端

		} catch (Exception e) {
			throw new OperationException("listByHot error:" + e);
		}
	}

	@Override
	public InfoListResult getDocList(QueryBuilder builder, User user, boolean sim, boolean irSimflag,boolean irSimflagAll,boolean isExport,String type)
			throws TRSException{
		String searchPage = SearchPage.COMMON_SEARCH.toString();
		return getDocList(builder, user, sim, irSimflag, irSimflagAll, isExport, type,searchPage);
	}
	/**
	 * 获取信息列表数据
	 *
	 * @param builder
	 *            QueryBuilder
	 * @return InfoListResult
	 */
	@Override
	public InfoListResult getDocList(QueryBuilder builder, User user, boolean sim, boolean irSimflag,boolean irSimflagAll,boolean isExport,String type,String searchPage)
			throws TRSException {
		// 暂时不用
		List<FtsDocument> ftsList = new ArrayList<>();
		List<String> md5List = new ArrayList<>();
		final String pageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		final String nextPageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		try {
			String asTrsl = builder.asTRSL();
			log.info("正式：" + asTrsl);
			// 如果是相似文章列表调用 sim就是true 其他就是false
			// 这里传的sim参数 将原来前面的 ! 符号删除 与参数值保持一致
			// if(!sim){
			// sim=true;
			// }
			// boolean s = !sim; //为保证栏目列表相似文章数 和 相似文章列表文章数 一致 sim参数应与传进来的保持一致

			// 把表达式放缓存里边 把key值返回给前端
			// long threadId = Thread.currentThread().getId();
			// String trslk = RedisUtil.PER_KEY + threadId;
			// //如果需要转换成server key就以server结尾 在导出exel时用
			// if(builder.isServer()){
			// trslk = trslk+RedisUtil.SUFFIX_KEY;
			// }
			// if(asTrsl.contains(FtsFieldConst.WEIGHT)){
			// trslk = trslk+FtsFieldConst.WEIGHT;
			// }
			// RedisUtil.setString(trslk, asTrsl);
			// String trslk = RedisUtil.saveKey(builder);
			// 在这存个空的 在hybase里边填完整的
			String trslk = pageId + "trsl";
			RedisUtil.setString(trslk, builder.asTRSL());
			builder.setKeyRedis(trslk);
			// 这个给异步计算时候用
			String trsHot = pageId + "hot";
			RedisUtil.setString(trsHot, builder.asTRSL());
			long currentTimeStart = System.currentTimeMillis();
			PagedList<FtsDocument> pagedList = hybase8SearchService.ftsPageList(builder, FtsDocument.class, sim,
					irSimflag,irSimflagAll,type);
			long currentTimeEnd = System.currentTimeMillis();
			log.info("专项列表查hybase用" + (currentTimeEnd - currentTimeStart));
			List<FtsDocument> list = pagedList.getPageItems();
			// 推荐列表排除自己
			label: {
				while (list.size() > 0) {
					// int index = 0;
					// 检验是否预警
					StringBuilder sb = new StringBuilder();
					List<String> sids = new ArrayList<>();
					for (FtsDocument document : list) {
						sb.append(document.getSid()).append(",");
						sids.add(document.getSid());
					}

					List<Favourites> favouritesList = favouritesService.findAll(user);
					//List<Favourites> library = favouritesRepository.findByUserIdAndSidIn(userId, sids);
					// 把已经预警的装里边
					/*List<String> sidAlert = new ArrayList<>();
					long alertBefore = System.currentTimeMillis();
					List<AlertEntity> alertList = null;
					//不查预警
					if (httpClient){
						String aSids = "";
						if (sb.length() > 0){
							aSids = sb.substring(0,sb.length()-1);
						}
						alertList = AlertUtil.getAlerts(userId,aSids,alertNetinsightUrl);
					}else {
						alertList = alertRepository.findByUserIdAndSidIn(userId, sids);
					}

					long alertAfter = System.currentTimeMillis();
					log.info("预警表查询用了" + (alertAfter - alertBefore));
					if (null != alertList && alertList.size()>0){
						for (AlertEntity alert : alertList) {
							sidAlert.add(alert.getSid());
						}
					}

					log.info("预警表查询用了" + (alertAfter - alertBefore));*/

					for (FtsDocument document : list) {
						String id = document.getSid();
						// 预警数据
						/*int indexOf = sidAlert.indexOf(id);
						if (indexOf < 0) {
							document.setSend(false);
						} else {
							document.setSend(true);
						}*/
						for (Favourites favourites : favouritesList) {
							if (favourites.getSid().toString().equals(id)) {// 该文章收藏了
								document.setFavourite(true);
								break;
							} else {
								document.setFavourite(false);
							}
						}
						if (ObjectUtils.isEmpty(favouritesList)) {// 因为当收藏列表为空时，程序不进以上的遍历，上面也不用写非空了
							document.setFavourite(false);
						}

						// String content = document.getAbstracts();
						// if (StringUtil.isEmpty(content)) {
						// String content = document.getContent();
						// }
						if (StringUtil.isNotEmpty(document.getContent())) {
							String content = document.getContent();
							List<String> imgSrcList = StringUtil.getImgStr(content);
							if (imgSrcList != null && imgSrcList.size() > 0) {
								if (imgSrcList.size() > 1) {
									document.setImgSrc(imgSrcList.get(1));
								} else {
									document.setImgSrc(imgSrcList.get(0));
								}
							}
							//document.setContent(StringUtil.substringRed(content, Const.CONTENT_LENGTH));

							document.setContent(StringUtil.cutContentPro(StringUtil.replaceImg(document.getContent()), Const.CONTENT_LENGTH));
						}
						document.setTrslk(trslk);
						if (StringUtil.isNotEmpty(document.getTitle())){
							document.setTitle( StringUtil.replaceAnnotation(document.getTitle()).replace("&amp;nbsp;", ""));
						}
						md5List.add(document.getMd5Tag());
						ftsList.add(document);

					}

					// 不排重的情况下
					TimingCachePool.put(pageId, new InfoListResult<>(pageId, pagedList, nextPageId, 0, trslk));
					// int finalIndex = index;
					// final String trsSim = trslk;
					if(builder.getPageSize()<10000 && !isExport){//导出就不算相似文章数
						fixedThreadPool.execute(() -> calculateSimNum(pageId, ftsList, user, trsHot, builder.isServer(),sim,irSimflag,type,searchPage));
					}
					break label;
				}
			}
		} catch (Exception e) {
			throw new OperationException("检索异常："+e, e);
		}
		long currentTimeMillis = System.currentTimeMillis();
		InfoListResult object = (InfoListResult) TimingCachePool.get(pageId);
		long currentTimeMillis2 = System.currentTimeMillis();
		long redisGet = currentTimeMillis2 - currentTimeMillis;
		log.info("从redis取数据用" + redisGet);
		return object;
	}

	/**
	 * 获取热点微信相似文章列表数据
	 *
	 * @param builder
	 *            QueryBuilder
	 * @return InfoListResult
	 */
	@Override
	public InfoListResult getHotWechatSimList(QueryBuilder builder, User user, boolean sim, boolean irSimflag,boolean irSimflagAll,String type)
			throws TRSException {
		// 暂时不用
		List<FtsDocumentWeChat> ftsList = new ArrayList<>();
		List<String> md5List = new ArrayList<>();
		final String pageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		final String nextPageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		try {
			String asTrsl = builder.asTRSL();
			log.info("正式：" + asTrsl);
			// 在这存个空的 在hybase里边填完整的
			String trslk = pageId + "trsl";
			RedisUtil.setString(trslk, "");
			builder.setKeyRedis(trslk);
			// 这个给异步计算时候用
			String trsHot = pageId + "hot";
			RedisUtil.setString(trsHot, builder.asTRSL());
			long currentTimeStart = System.currentTimeMillis();
			PagedList<FtsDocumentWeChat> pagedList = hybase8SearchService.ftsPageList(builder, FtsDocumentWeChat.class,
					sim, irSimflag,irSimflagAll,null);
			long currentTimeEnd = System.currentTimeMillis();
			log.info("专项列表查hybase用" + (currentTimeEnd - currentTimeStart));
			List<FtsDocumentWeChat> list = pagedList.getPageItems();
			// 推荐列表排除自己
			label: {
				while (list.size() > 0) {
					// int index = 0;
					// 检验是否预警
					List<String> sids = new ArrayList<>();
					StringBuilder sb = new StringBuilder();
					for (FtsDocumentWeChat document : list) {
						sids.add(document.getHkey());
						sb.append(document.getHkey()).append(",");
					}

					List<Favourites> favouritesList = favouritesService.findAll(user);
					//	List<Favourites> library = favouritesRepository.findByUserIdAndSidIn(userId, sids);
					// 把已经预警的装里边
					/*List<String> sidAlert = new ArrayList<>();
					long alertBefore = System.currentTimeMillis();
					List<AlertEntity> alertList = null;
					if (httpClient){
						String aSids = "";
						if (sb.length() > 1){
							aSids = sb.substring(0,sb.length()-1);
						}
						alertList = AlertUtil.getAlerts(userId,aSids,alertNetinsightUrl);
					}else {
						alertList = alertRepository.findByUserIdAndSidIn(userId, sids);
					}

					long alertAfter = System.currentTimeMillis();
					log.info("预警表查询用了" + (alertAfter - alertBefore));

					 * String userName = UserUtils.getUser().getUserName();
					 * if(userName.equals("xiaoying")){
					 * log.info("xiaoying预警表查询用了"+(alertAfter-alertBefore)); }

					for (AlertEntity alert : alertList) {
						sidAlert.add(alert.getSid());
					}*/
					for (FtsDocumentWeChat document : list) {
						String id = document.getHkey();
						// 预警数据
						/*int indexOf = sidAlert.indexOf(id);
						if (indexOf < 0) {
							document.setSend(false);
						} else {
							document.setSend(true);
						}*/
						for (Favourites favourites : favouritesList) {
							if (favourites.getSid().toString().equals(id)) {// 该文章收藏了
								document.setFavourite(true);
								break;
							} else {
								document.setFavourite(false);
							}
						}
						if (ObjectUtils.isEmpty(favouritesList)) {// 因为当收藏列表为空时，程序不进以上的遍历，上面也不用写非空了
							document.setFavourite(false);
						}

						String content = StringUtil.replaceImg(document.getContent());
						//document.setContent(StringUtil.substringRed(content, Const.CONTENT_LENGTH));
						document.setContent(StringUtil.cutContent(StringUtil.replaceImg(document.getContent()), 160));
						document.setTrslk(trslk);
						md5List.add(document.getMd5Tag());
						ftsList.add(document);

					}

					// 不排重的情况下
					TimingCachePool.put(pageId, new InfoListResult<>(pageId, pagedList, nextPageId, 0, trslk));
					// int finalIndex = index;
					// final String trsSim = trslk;
					fixedThreadPool
							.execute(() -> calculateSimNumWeChat(pageId, ftsList, user, trsHot, builder.isServer(),type));
					break label;
				}
			}
		} catch (Exception e) {
			throw new OperationException("检索异常：", e);
		}
		long currentTimeMillis = System.currentTimeMillis();
		InfoListResult object = (InfoListResult) TimingCachePool.get(pageId);
		long currentTimeMillis2 = System.currentTimeMillis();
		long redisGet = currentTimeMillis2 - currentTimeMillis;
		log.info("从redis取数据用" + redisGet);
		return object;
	}

	@Override
	public InfoListResult getDocTFList(QueryBuilder builder, User user, boolean sim,boolean irSimflag,boolean irSimflagAll,String type) throws TRSException{
		String searchPage = SearchPage.COMMON_SEARCH.toString();
		return getDocTFList(builder, user, sim, irSimflag, irSimflagAll, type,searchPage);
	}

	@Override
	public InfoListResult getDocTFList(QueryBuilder builder, User user, boolean sim,boolean irSimflag,boolean irSimflagAll,String type,String searchPage) throws TRSException {
		// 暂时不用
		List<FtsDocumentTF> ftsList = new ArrayList<>();
		List<String> md5List = new ArrayList<>();
		final String pageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		final String nextPageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		try {
			// String trslk = RedisUtil.saveKey(builder);
			// 在这存个空的 在hybase里边填完整的
			String trslk = pageId + "trslk";
			RedisUtil.setString(trslk, "");
			builder.setKeyRedis(trslk);
			// 把表达式放缓存里边 把key值返回给前端
			// long threadId = Thread.currentThread().getId();
			// String trslk = RedisUtil.PER_KEY + threadId;
			// //如果需要转换成server key就以server结尾 在导出exel时用
			// if(builder.isServer()){
			// trslk = trslk+RedisUtil.SUFFIX_KEY;
			// }
			// RedisUtil.setString(trslk, builder.asTRSL());
			log.info("正式：" + builder.asTRSL());
			String trslkHot = pageId + "hot";
			RedisUtil.setString(trslkHot, builder.asTRSL());
			PagedList<FtsDocumentTF> pagedList = hybase8SearchService.ftsPageList(builder, FtsDocumentTF.class, sim,
					irSimflag,irSimflagAll,type);
			List<FtsDocumentTF> list = pagedList.getPageItems();
			// 推荐列表排除自己
			label: {

				while (list.size() > 0) {
					// int index = 0;
					// 检验是否预警
					List<String> sids = new ArrayList<>();
					StringBuilder sb = new StringBuilder();
					for (FtsDocumentTF document : list) {
						sids.add(document.getSid());
						sb.append(document.getSid()).append(",");
					}

					List<Favourites> favouritesList = favouritesService.findAll(user);
					//List<Favourites> library = favouritesRepository.findByUserIdAndSidIn(userId, sids);
					// 把已经预警的装里边
					//不查预警
					/*List<String> sidAlert = new ArrayList<>();
					long alertBefore = System.currentTimeMillis();
					List<AlertEntity> alertList = null;
					if (httpClient){
						String aSids = "";
						if (sb.length() > 0){
							aSids = sb.substring(0,sb.length()-1);
						}
						alertList = AlertUtil.getAlerts(userId,aSids,alertNetinsightUrl);
					}else {
						alertList = alertRepository.findByUserIdAndSidIn(userId, sids);
					}
					long alertAfter = System.currentTimeMillis();
					log.info("预警表查询用了" + (alertAfter - alertBefore));
					if (null != alertList && alertList.size()>0){
						for (AlertEntity alert : alertList) {
							sidAlert.add(alert.getSid());
						}
					}

					log.info("预警表查询用了" + (alertAfter - alertBefore));
					String userName = UserUtils.getUser().getUserName();
					if (userName != null && userName.equals("xiaoying")) {
						log.info("xiaoying预警表查询用了" + (alertAfter - alertBefore));
					}*/

					for (FtsDocumentTF document : list) {
						String id = document.getSid();
						// 预警数据
						//不查预警
						/*int indexOf = sidAlert.indexOf(id);
						if (indexOf < 0) {
							document.setSend(false);
						} else {
							document.setSend(true);
						}*/
						for (Favourites favourites : favouritesList) {
							if (favourites.getSid().toString().equals(id)) {// 该文章收藏了
								document.setFavourite(true);
								break;
							} else {
								document.setFavourite(false);
							}
						}
						if (ObjectUtils.isEmpty(favouritesList)) {// 因为当收藏列表为空时，程序不进以上的遍历，上面也不用写非空了
							document.setFavourite(false);
						}

						// String content = document.getStatusContent();
						if (StringUtil.isNotEmpty(document.getContent())) {
							String content = StringUtil.replaceImg(document.getStatusContent());
							//	document.setContent(StringUtil.substringRed(content, Const.CONTENT_LENGTH));
							document.setStatusContent(StringUtil.cutContentPro(StringUtil.replaceImg(content), Const.CONTENT_LENGTH));
							document.setContent(StringUtil.cutContentPro(StringUtil.replaceImg(content), Const.CONTENT_LENGTH));
						}
						document.setTrslk(trslk);
						md5List.add(document.getMd5Tag());
						ftsList.add(document);

					}

					// 不排重的情况下
					TimingCachePool.put(pageId, new InfoListResult<>(pageId, pagedList, nextPageId, 0, trslk));
					// int finalIndex = index;
					// final String trsSim = trslk;
					fixedThreadPool
							.execute(() -> calculateSimNumTF(pageId, ftsList, user, trslkHot, builder.isServer(),type,searchPage));

					break label;
				}
			}
		} catch (Exception e) {
			throw new OperationException("检索异常：" + e.getMessage(), e);
		}
		InfoListResult object = (InfoListResult) TimingCachePool.get(pageId);
		return object;
	}

	@Override
	public InfoListResult getDocListContrast(QueryBuilder builder, User user, boolean sim, boolean irSimflag,boolean irSimflagAll,String type)
			throws TRSException {
		// 暂时不用
		List<FtsDocument> ftsList = new ArrayList<>();
		List<FtsDocumentWeChat> ftsListWeChat = new ArrayList<>();
		List<FtsDocumentStatus> ftsListStatus = new ArrayList<>();
		List<FtsDocumentTF> ftsListTF = new ArrayList<>();

		List<String> md5List = new ArrayList<>();
		final String pageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		final String nextPageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		// 在这存个空的 在hybase里边填完整的
		String trslk = pageId + "trslk";
		RedisUtil.setString(trslk, "");
		builder.setKeyRedis(trslk);
		// 异步计算
		String trslkHot = pageId + "hot";
		RedisUtil.setString(trslkHot, builder.asTRSL());
		// String trslk = RedisUtil.saveKey(builder);
		// 根据builder选择相应实体等信息
		if (Const.WEIBO.equals(builder.getDatabase())) {
			try {

				// 把表达式放缓存里边 把key值返回给前端
				// long threadId = Thread.currentThread().getId();
				// String trslk = RedisUtil.PER_KEY + threadId;
				// //如果需要转换成server key就以server结尾 在导出exel时用
				// if(builder.isServer()){
				// trslk = trslk+RedisUtil.SUFFIX_KEY;
				// }
				// RedisUtil.setString(trslk, builder.asTRSL());
				log.info("正式：" + builder.asTRSL());
				PagedList<FtsDocumentStatus> pagedList = hybase8SearchService.ftsPageList(builder,
						FtsDocumentStatus.class, sim, irSimflag,irSimflagAll,type);
				List<FtsDocumentStatus> list = pagedList.getPageItems();

				// 推荐列表排除自己
				label: {

					while (list.size() > 0) {
						// int index = 0;
						// 检验是否预警
						List<String> sids = new ArrayList<>();
						StringBuilder sb = new StringBuilder();
						for (FtsDocumentStatus document : list) {
							sids.add(document.getMid());
							sb.append(document.getMid()).append(",");
						}

						List<Favourites> favouritesList = favouritesService.findAll(user);
						// 把已经预警的装里边
						//不查预警了
						/*List<String> sidAlert = new ArrayList<>();
						long alertBefore = System.currentTimeMillis();
						List<AlertEntity> alertList = null;
						if (httpClient){
							String aSids = "";
							if (sb.length() > 0){
								aSids = sb.substring(0,sb.length()-1);
							}
							alertList = AlertUtil.getAlerts(userId,aSids,alertNetinsightUrl);
						}else {
							alertList = alertRepository.findByUserIdAndSidIn(userId, sids);
						}
						long alertAfter = System.currentTimeMillis();
						log.info("预警表查询用了" + (alertAfter - alertBefore));
						if (null != alertList && alertList.size()>0){
							for (AlertEntity alert : alertList) {
								sidAlert.add(alert.getSid());
							}
						}*/


						for (FtsDocumentStatus document : list) {

							String id = document.getMid();
							// 预警数据
							//不查预警了
							/*int indexOf = sidAlert.indexOf(id);
							if (indexOf < 0) {
								document.setSend(false);
							} else {
								document.setSend(true);
							}*/
							for (Favourites favourites : favouritesList) {
								if (favourites.getSid().toString().equals(id)) {// 该文章收藏了
									document.setFavourite(true);
									break;
								} else {
									document.setFavourite(false);
								}
							}

							if (ObjectUtils.isEmpty(favouritesList)) {// 因为当收藏列表为空时，程序不进以上的遍历，上面也不用写非空了
								document.setFavourite(false);
							}

							String content = document.getStatusContent();
							content = StringUtil.replaceImg(content);
							document.setStatusContent(content);
							document.setTrslk(trslk);
							md5List.add(document.getMd5Tag());
							ftsListStatus.add(document);

						}

						// 不排重的情况下
						TimingCachePool.put(pageId, new InfoListResult<>(pageId, pagedList, nextPageId, 0, trslk));
						// int finalIndex = index;
						// final String trsSim = trslk;
						fixedThreadPool.execute(() -> calculateSimNumStatus(pageId, ftsListStatus, user, trslkHot,type));

						break label;
					}
				}
			} catch (Exception e) {
				throw new OperationException("检索异常：", e);
			}
		} else if (Const.WECHAT.equals(builder.getDatabase())) {
			try {
				// 把表达式放缓存里边 把key值返回给前端
				// long threadId = Thread.currentThread().getId();
				// String trslk = RedisUtil.PER_KEY + threadId;
				// //如果需要转换成server key就以server结尾 在导出exel时用
				// if(builder.isServer()){
				// trslk = trslk+RedisUtil.SUFFIX_KEY;
				// }
				// RedisUtil.setString(trslk, builder.asTRSL());
				log.info("正式：" + builder.asTRSL());
				PagedList<FtsDocumentWeChat> pagedList = hybase8SearchService.ftsPageList(builder,
						FtsDocumentWeChat.class, sim, irSimflag,irSimflagAll,type);
				List<FtsDocumentWeChat> list = pagedList.getPageItems();
				// 推荐列表排除自己
				label: {

					while (list.size() > 0) {
						// int index = 0;
						// 检验是否预警
						List<String> sids = new ArrayList<>();
						StringBuilder sb = new StringBuilder();
						for (FtsDocumentWeChat document : list) {
							sids.add(document.getHkey());
							sb.append(document.getHkey()).append(",");
						}

						List<Favourites> favouritesList = favouritesService.findAll(user);
						// 把已经预警的装里边
						/*List<String> sidAlert = new ArrayList<>();
						long alertBefore = System.currentTimeMillis();
						List<AlertEntity> alertList = null;
						if (httpClient){
							String aSids = "";
							if (sb.length() > 0){
								aSids = sb.substring(0,sb.length()-1);
							}
							alertList = AlertUtil.getAlerts(userId,aSids,alertNetinsightUrl);
						}else {
							alertList = alertRepository.findByUserIdAndSidIn(userId, sids);
						}
						long alertAfter = System.currentTimeMillis();
						log.info("预警表查询用了" + (alertAfter - alertBefore));
						if (null != alertList && alertList.size()>0){
							for (AlertEntity alert : alertList) {
								sidAlert.add(alert.getSid());
							}
						}*/

						for (FtsDocumentWeChat document : list) {

							String id = document.getHkey();
							// 预警数据
							/*int indexOf = sidAlert.indexOf(id);
							if (indexOf < 0) {
								document.setSend(false);
							} else {
								document.setSend(true);
							}*/
							for (Favourites favourites : favouritesList) {
								if (favourites.getSid().toString().equals(id)) {// 该文章收藏了
									document.setFavourite(true);
									break;
								} else {
									document.setFavourite(false);
								}
							}

							String content = document.getContent();
							content = StringUtil.replaceImg(content);
							document.setContent(content);
							document.setTrslk(trslk);
							document.setGroupName("微信");
							document.setUrlTitle(document.getUrlTitle().replace("&amp;nbsp;", ""));
							md5List.add(document.getMd5Tag());
							ftsListWeChat.add(document);

						}

						// 不排重的情况下
						TimingCachePool.put(pageId, new InfoListResult<>(pageId, pagedList, nextPageId, 0, trslk));
						// int finalIndex = index;
						// final String trsSim = trslk;
						fixedThreadPool.execute(() -> calculateSimNumWeChat(pageId, ftsListWeChat, user, trslkHot,
								builder.isServer(),type));

						break label;
					}
				}
			} catch (Exception e) {
				throw new OperationException("检索异常：", e);
			}
		} else if (Const.HYBASE_OVERSEAS.equals(builder.getDatabase())) {
			try {
				// 把表达式放缓存里边 把key值返回给前端
				// long threadId = Thread.currentThread().getId();
				// String trslk = RedisUtil.PER_KEY + threadId;
				// //如果需要转换成server key就以server结尾 在导出exel时用
				// if(builder.isServer()){
				// trslk = trslk+RedisUtil.SUFFIX_KEY;
				// }
				// RedisUtil.setString(trslk, builder.asTRSL());
				log.info("正式：" + builder.asTRSL());
				PagedList<FtsDocumentTF> pagedList = hybase8SearchService.ftsPageList(builder, FtsDocumentTF.class, sim,
						irSimflag,irSimflagAll,type);
				List<FtsDocumentTF> list = pagedList.getPageItems();
				// 推荐列表排除自己
				label: {

					while (list.size() > 0) {
						// int index = 0;
						// 检验是否预警
						List<String> sids = new ArrayList<>();
						StringBuilder sb = new StringBuilder();
						for (FtsDocumentTF document : list) {
							sids.add(document.getSid());
							sb.append(document.getSid()).append(",");
						}

						List<Favourites> favouritesList = favouritesService.findAll(user);
						// 把已经预警的装里边
						/*List<String> sidAlert = new ArrayList<>();
						long alertBefore = System.currentTimeMillis();
						List<AlertEntity> alertList = null;
						if (httpClient){
							String aSids = "";
							if (sb.length() > 0){
								aSids = sb.substring(0,sb.length()-1);
							}
							alertList = AlertUtil.getAlerts(userId,aSids,alertNetinsightUrl);
						}else {
							alertList = alertRepository.findByUserIdAndSidIn(userId, sids);
						}
						long alertAfter = System.currentTimeMillis();
						log.info("预警表查询用了" + (alertAfter - alertBefore));
						if (null != alertList && alertList.size()>0){
							for (AlertEntity alert : alertList) {
								sidAlert.add(alert.getSid());
							}
						}*/

						for (FtsDocumentTF document : list) {

							String id = document.getSid();
							// 预警数据
							/*int indexOf = sidAlert.indexOf(id);
							if (indexOf < 0) {
								document.setSend(false);
							} else {
								document.setSend(true);
							}*/
							for (Favourites favourites : favouritesList) {
								if (favourites.getSid().toString().equals(id)) {// 该文章收藏了
									document.setFavourite(true);
									break;
								} else {
									document.setFavourite(false);
								}
							}

							String content = document.getStatusContent();
							content = StringUtil.replaceImg(content);
							document.setStatusContent(content);
							document.setTrslk(trslk);
							md5List.add(document.getMd5Tag());
							ftsListTF.add(document);
						}

						// 不排重的情况下
						TimingCachePool.put(pageId, new InfoListResult<>(pageId, pagedList, nextPageId, 0, trslk));
						// int finalIndex = index;
						// final String trsSim = trslk;
						fixedThreadPool.execute(
								() -> calculateSimNumTF(pageId, ftsListTF, user, trslkHot, builder.isServer(),type));

						break label;
					}
				}
			} catch (Exception e) {
				throw new OperationException("检索异常：", e);
			}
		} else {
			try {
				// 把表达式放缓存里边 把key值返回给前端
				// long threadId = Thread.currentThread().getId();
				// String trslk = RedisUtil.PER_KEY + threadId;
				// //如果需要转换成server key就以server结尾 在导出exel时用
				// if(builder.isServer()){
				// trslk = trslk+RedisUtil.SUFFIX_KEY;
				// }
				// RedisUtil.setString(trslk, builder.asTRSL());
				log.info("正式：" + builder.asTRSL());
				PagedList<FtsDocument> pagedList = hybase8SearchService.ftsPageList(builder, FtsDocument.class, sim,
						irSimflag,irSimflagAll,type);
				List<FtsDocument> list = pagedList.getPageItems();
				// 推荐列表排除自己
				label: {

					while (list.size() > 0) {
						// int index = 0;
						// 检验是否预警
						List<String> sids = new ArrayList<>();
						StringBuilder sb = new StringBuilder();
						for (FtsDocument document : list) {
							sids.add(document.getSid());
							sb.append(document.getSid()).append(",");
						}

						List<Favourites> favouritesList = favouritesService.findAll(user);
						// 把已经预警的装里边
						/*List<String> sidAlert = new ArrayList<>();
						long alertBefore = System.currentTimeMillis();
						List<AlertEntity> alertList = null;
						if (httpClient){
							String aSids = "";
							if (sb.length() > 0){
								aSids = sb.substring(0,sb.length()-1);
							}
							alertList = AlertUtil.getAlerts(userId,aSids,alertNetinsightUrl);
						}else {
							alertList = alertRepository.findByUserIdAndSidIn(userId, sids);
						}
						long alertAfter = System.currentTimeMillis();
						log.info("预警表查询用了" + (alertAfter - alertBefore));
						if (null != alertList && alertList.size()>0){
							for (AlertEntity alert : alertList) {
								sidAlert.add(alert.getSid());
							}
						}*/


						for (FtsDocument document : list) {


							String id = document.getSid();
							// 预警数据
							/*int indexOf = sidAlert.indexOf(id);
							if (indexOf < 0) {
								document.setSend(false);
							} else {
								document.setSend(true);
							}*/
							for (Favourites favourites : favouritesList) {
								if (favourites.getSid().toString().equals(id)) {// 该文章收藏了
									document.setFavourite(true);
									break;
								} else {
									document.setFavourite(false);
								}
							}

							String content = document.getAbstracts();
							if (StringUtil.isEmpty(content)) {
								content = document.getContent();
							}
							content = StringUtil.replaceImg(content);
							document.setContent(content);
							document.setTrslk(trslk);
							document.setTitle(document.getTitle().replace("&amp;nbsp;", ""));
							md5List.add(document.getMd5Tag());
							ftsList.add(document);

						}

						// 不排重的情况下
						TimingCachePool.put(pageId, new InfoListResult<>(pageId, pagedList, nextPageId, 0, trslk));
						// int finalIndex = index;
						// final String trsSim = trslk;
						fixedThreadPool
								.execute(() -> calculateSimNum(pageId, ftsList, user, trslkHot, builder.isServer(),type));

						break label;
					}
				}
			} catch (Exception e) {
				throw new OperationException("检索异常：", e);
			}
		}

		InfoListResult object = (InfoListResult) TimingCachePool.get(pageId);
		return object;
	}

	@Override
	public Object getHotListStatus(QueryBuilder builder, QueryBuilder countBuilder, User user,String type) throws TRSException{
		String searchPage = SearchPage.COMMON_SEARCH.toString();
		return getHotListStatus(builder, countBuilder, user, type,searchPage);
	}
	@Override
	public Object getHotListStatus(QueryBuilder builder, QueryBuilder countBuilder, User user,String type,String searchPage) throws TRSException {
		try {
			int pageSize = builder.getPageSize();
			if (pageSize > 50) {
				pageSize = 50;
				builder.setPageSize(50);
			} else if (pageSize <= 0) {
				pageSize = 10;
				builder.setPageSize(10);
			}
			// 返回给前端总页数
			int pageListNo = 0;
			// 返回给前端总条数
			int pageListSize = 0;
			String pageId = GUIDGenerator.generate(InfoListServiceImpl.class);
			String nextPageId = GUIDGenerator.generate(InfoListServiceImpl.class);
			List<FtsDocumentStatus> FtsDocuments = new ArrayList<>();
			String indices = FtsParser.getDatabases(FtsDocumentStatus.class);

			// hybase不能直接分页 每次都统计出50条 然后再取
			long pageNo = builder.getPageNo();
			// 从上一页到这一页 pageSize*pageNo到pageSize*pageNo+pageSize-1
			List<GroupInfo> groupList = new ArrayList();
			String keyList = CachekeyUtil.getToolKey(user, builder.asTRSL(), "hotweibo");
			log.info(keyList);
			List<GroupInfo> list = TimingCachePool.get(keyList);
			// String trslk = RedisUtil.saveKey(builder);
			// 在这存个空的 在hybase里边填完整的
			String trslk = pageId + "trslk";
			RedisUtil.setString(trslk, builder.asTRSL());
			builder.setKeyRedis(trslk);
			// 异步结算用
			String trslkHot = pageId + "hot";
			RedisUtil.setString(trslkHot, builder.asTRSL());
			// 把表达式放缓存里边 把key值返回给前端
			// long threadId = Thread.currentThread().getId();
			// String trslk = RedisUtil.PER_KEY + threadId;
			// //如果需要转换成server key就以server结尾 在导出exel时用
			// if(builder.isServer()){
			// trslk = trslk+RedisUtil.SUFFIX_KEY;
			// }
			// RedisUtil.setString(trslk, builder.asTRSL());
			if (ObjectUtil.isNotEmpty(list)) {
				groupList = list;
			} else {
				builder.page(0, 50);
				GroupResult md5TAG = hybase8SearchService.categoryQuery(builder, false, true, false,"MD5TAG",type, indices);
				groupList = md5TAG.getGroupList();
				TimingCachePool.put(keyList, groupList);
			}
			int size = 0;
			String countKey =CachekeyUtil.getToolKey(user, builder.asTRSL(), "hotweibocount");
			log.info(countKey);
			if (ObjectUtil.isNotEmpty(TimingCachePool.get(countKey))) {
				size = TimingCachePool.get(countKey);
			} else {
				countBuilder.page(0, 50);
				GroupResult md5TAGCount = hybase8SearchService.categoryQuery(countBuilder, false, true,false, "MD5TAG",
						type,indices);
				size = md5TAGCount.getGroupList().size();
				if (size > 50) {
					size = 50;
				}
				TimingCachePool.put(countKey, size);
			}

			int start = (int) (pageSize * pageNo);
			int end = (int) (pageSize * pageNo + pageSize - 1);
			if (start >= groupList.size()) {
				return null;
			}
			if (groupList.size() <= end) {
				end = groupList.size() - 1;
			}
			// 返回前端总页数
			pageListNo = groupList.size() % pageSize == 0 ? groupList.size() / pageSize
					: groupList.size() / pageSize + 1;
			pageListSize = groupList.size();
			for (int i = start; i <= end; i++) {
				GroupInfo info = groupList.get(i);
				QueryBuilder builder1 = new QueryBuilder();
				builder1.filterByTRSL(builder.asTRSL());
				builder1.page(0, 1);
				builder1.filterField("MD5TAG", info.getFieldValue(), Operator.Equal);
				List<FtsDocumentStatus> pagedList = hybase8SearchService.ftsQuery(builder1, FtsDocumentStatus.class,
						false, false,false,type);
				if (ObjectUtil.isNotEmpty(pagedList)) {
					FtsDocumentStatus ftsDocument = pagedList.get(0);
					ftsDocument.setTrslk(trslk);
					ftsDocument.setStatusContent(
							StringUtil.cutContent(StringUtil.replaceImg(ftsDocument.getStatusContent()), 160));
					FtsDocuments.add(ftsDocument);
				}
			}
			if (0 == FtsDocuments.size()) {
				return null;
			}
			List<String> idList = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			for (FtsDocumentStatus status : FtsDocuments) {
				idList.add(status.getMid());
				sb.append(status.getMid()).append(",");
			}

			List<Favourites> favouritesList = favouritesService.findAll(user);

			/*List<String> sidAlert = new ArrayList<>();
			List<AlertEntity> alertList = null;
			if (httpClient){
				String aSids = "";
				if (sb.length() > 1){
					aSids = sb.substring(0,sb.length()-1);
				}
				alertList = AlertUtil.getAlerts(userId,aSids,alertNetinsightUrl);
			}else {
				alertList = alertRepository.findByUserIdAndSidIn(userId, idList);
			}
			if (ObjectUtil.isNotEmpty(alertList)){
				for (AlertEntity alert : alertList) {
					sidAlert.add(alert.getSid());
				}
			}
*/
			for (FtsDocumentStatus status : FtsDocuments) {
				String id = status.getMid();
				// 预警数据
			/*	int indexOf = sidAlert.indexOf(id);
				if (indexOf < 0) {
					status.setSend(false);
				} else {
					status.setSend(true);
				}*/

				for (Favourites favourites : favouritesList) {
					if (favourites.getSid().toString().equals(id)) {// 该文章收藏了
						status.setFavourite(true);
						break;
					} else {
						status.setFavourite(false);
					}
				}
				if (ObjectUtils.isEmpty(favouritesList)) {// 因为当收藏列表为空时，程序不进以上的遍历，上面也不用写非空了
					status.setFavourite(false);
				}

			}
			// PagedList<FtsDocument> pagedList = new
			// PagedList<FtsDocument>(builder.getPageNo() < 0 ? 0 : (int)
			// builder.getPageNo(),
			// (int) (pageSize < 0 ? 15 : pageSize),
			// size, FtsDocuments,
			// 1);
			PagedList<FtsDocumentStatus> pagedList = new PagedList<FtsDocumentStatus>(pageListNo,
					(int) (pageSize < 0 ? 15 : pageSize), pageListSize, FtsDocuments, 1);
			RedisFactory.setValueToRedis(pageId, new InfoListResult<>(pageId, pagedList, nextPageId, size, trslk));
			// final String trsSim = trslk;
			fixedThreadPool.execute(() -> calculateSimNumStatus(pageId, FtsDocuments, user, trslkHot,type,searchPage));
			return new InfoListResult<>(pageId, pagedList, nextPageId, size, trslk);
			// 把表达式放缓存里边 把key值返回给前端

		} catch (Exception e) {
			throw new OperationException("listByHot error:" + e);
		}
	}
	@Override
	public Object getHotListWeChat(QueryBuilder builder, QueryBuilder countBuilder, User user,String type) throws TRSException{
		String searchPage = SearchPage.COMMON_SEARCH.toString();
		return getHotListWeChat(builder, countBuilder, user, type,searchPage);
	}

	@Override
	public Object getHotListWeChat(QueryBuilder builder, QueryBuilder countBuilder, User user,String type,String searchPage) throws TRSException {
		try {
			int pageSize = builder.getPageSize();
			if (pageSize > 50) {
				pageSize = 50;
				builder.setPageSize(50);
			} else if (pageSize <= 0) {
				pageSize = 10;
				builder.setPageSize(10);
			}
			if (builder.getDatabase() == null) {
				builder.setDatabase(Const.WECHAT_COMMON);
				countBuilder.setDatabase(Const.WECHAT_COMMON);
			}
			// 返回给前端总页数
			int pageListNo = 0;
			// 返回给前端总条数
			int pageListSize = 0;
			String pageId = GUIDGenerator.generate(InfoListServiceImpl.class);
			String nextPageId = GUIDGenerator.generate(InfoListServiceImpl.class);
			List<FtsDocumentWeChat> FtsDocuments = new ArrayList<>();
			String indices = FtsParser.getDatabases(FtsDocumentWeChat.class);
			// hybase不能直接分页 每次都统计出50条 然后再取
			long pageNo = builder.getPageNo();
			// 从上一页到这一页 pageSize*pageNo到pageSize*pageNo+pageSize-1
			List<GroupInfo> groupList = new ArrayList();
			List<GroupInfo> list = TimingCachePool.get(CachekeyUtil.getToolKey(user,builder.asTRSL(),"hotweixin"));
			// String trslk = RedisUtil.saveKey(builder);
			// 在这存个空的 在hybase里边填完整的
			String trslk = pageId + "trslk";
			RedisUtil.setString(trslk, builder.asTRSL());
			builder.setKeyRedis(trslk);
			// 异步计算
			String trslkHot = pageId + "hot";
			RedisUtil.setString(trslkHot, builder.asTRSL());
			// 把表达式放缓存里边 把key值返回给前端
			// long threadId = Thread.currentThread().getId();
			// String trslk = RedisUtil.PER_KEY + threadId;
			// //如果需要转换成server key就以server结尾 在导出exel时用
			// if(builder.isServer()){
			// trslk = trslk+RedisUtil.SUFFIX_KEY;
			// }
			// RedisUtil.setString(trslk, builder.asTRSL());
			if (ObjectUtil.isNotEmpty(list)) {
				groupList = list;
			} else {
				builder.page(0, 50);
				GroupResult md5TAG = hybase8SearchService.categoryQuery(builder, false, true, false,"MD5TAG",type, indices);
				groupList = md5TAG.getGroupList();
				TimingCachePool.put(CachekeyUtil.getToolKey(user,builder.asTRSL(),"hotweixin"), groupList);
			}
			int size = 0;
			String hotweixincount = CachekeyUtil.getToolKey(user, builder.asTRSL(), "hotweixincount");
			if (ObjectUtil.isNotEmpty(TimingCachePool.get(hotweixincount))) {
				size = TimingCachePool.get(hotweixincount);
			} else {
				countBuilder.page(0, 50);
				GroupResult md5TAGCount = hybase8SearchService.categoryQuery(countBuilder, false, true, false,"MD5TAG",type,
						indices);
				size = md5TAGCount.getGroupList().size();
				if (size > 50) {
					size = 50;
				}
				TimingCachePool.put(CachekeyUtil.getToolKey(user, builder.asTRSL(), "hotchuagntongcount"), size);
			}

			int start = (int) (pageSize * pageNo);
			int end = (int) (pageSize * pageNo + pageSize - 1);
			if (start >= groupList.size()) {
				return null;
			}
			if (groupList.size() <= end) {
				end = groupList.size() - 1;
			}
			// 返回前端总页数
			pageListNo = groupList.size() % pageSize == 0 ? groupList.size() / pageSize
					: groupList.size() / pageSize + 1;
			pageListSize = groupList.size();
			for (int i = start; i <= end; i++) {
				GroupInfo info = groupList.get(i);
				QueryBuilder builder1 = new QueryBuilder();
				builder1.filterByTRSL(builder.asTRSL());
				builder1.page(0, 1);
				builder1.filterField("MD5TAG", info.getFieldValue(), Operator.Equal);
				List<FtsDocumentWeChat> pagedList = hybase8SearchService.ftsQuery(builder1, FtsDocumentWeChat.class,
						false, false,false,type);
				if (ObjectUtil.isNotEmpty(pagedList)) {
					FtsDocumentWeChat ftsDocument = pagedList.get(0);
					ftsDocument.setTrslk(trslk);
					ftsDocument.setContent(StringUtil.cutContent(StringUtil.replaceImg(ftsDocument.getContent()), 160));
					if (StringUtil.isNotEmpty(ftsDocument.getUrlTitle())){
						ftsDocument.setUrlTitle( StringUtil.replaceAnnotation(ftsDocument.getUrlTitle()).replace("&amp;nbsp;", ""));
					}
					//微信库主键是hkey  sid没啥用
					ftsDocument.setSid(ftsDocument.getHkey());
					FtsDocuments.add(ftsDocument);
				}
			}
			if (0 == FtsDocuments.size()) {
				return null;
			}
			List<String> idList = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			for (FtsDocumentWeChat weChat : FtsDocuments) {
				idList.add(weChat.getHkey());
				sb.append(weChat.getHkey()).append(",");
			}

			List<Favourites> favouritesList = favouritesService.findAll(user);
			/*List<String> sidAlert = new ArrayList<>();
			List<AlertEntity> alertList = null;
			if (httpClient){
				String aSids = "";
				if (sb.length() > 1){
					aSids = sb.substring(0,sb.length()-1);
				}
				alertList = AlertUtil.getAlerts(userId,aSids,alertNetinsightUrl);
			}else {
				alertList = alertRepository.findByUserIdAndSidIn(userId, idList);
			}
			 if (ObjectUtil.isNotEmpty(alertList)){
				 for (AlertEntity alert : alertList) {
					 sidAlert.add(alert.getSid());
				 }
			 }*/

			for (FtsDocumentWeChat weChat : FtsDocuments) {
				String id = weChat.getHkey();
				if("国内微信".equals(weChat.getGroupName())){
					weChat.setGroupName("微信");
				}
				// 预警数据
				/*int indexOf = sidAlert.indexOf(id);
				if (indexOf < 0) {
					weChat.setSend(false);
				} else {
					weChat.setSend(true);
				}*/
				for (Favourites favourites : favouritesList) {
					if (favourites.getSid().toString().equals(id)) {// 该文章收藏了
						weChat.setFavourite(true);
						break;
					} else {
						weChat.setFavourite(false);
					}
				}
				if (ObjectUtils.isEmpty(favouritesList)) {// 因为当收藏列表为空时，程序不进以上的遍历，上面也不用写非空了
					weChat.setFavourite(false);
				}

			}
			PagedList<FtsDocumentWeChat> pagedList = new PagedList<FtsDocumentWeChat>(pageListNo,
					(int) (pageSize < 0 ? 15 : pageSize), pageListSize, FtsDocuments, 1);
			RedisFactory.setValueToRedis(pageId, new InfoListResult<>(pageId, pagedList, nextPageId, size, trslk));
			// final String trsSim = trslk;
			fixedThreadPool
					.execute(() -> calculateSimNumWeChat(pageId, FtsDocuments, user, trslkHot, builder.isServer(),type,searchPage));
			return new InfoListResult<>(pageId, pagedList, nextPageId, size, trslk);
			// 把表达式放缓存里边 把key值返回给前端

		} catch (Exception e) {
			throw new OperationException("listByHot error:" + e);
		}
	}


	@Override
	public InfoListResult<FtsDocumentStatus> getStatusList(QueryBuilder builder, User user, boolean sim,
														   boolean irSimflag,boolean irSimflagAll,boolean isExport,String type) throws TRSException{
		String searchPage = SearchPage.COMMON_SEARCH.toString();
		return getStatusList(builder, user, sim, irSimflag, irSimflagAll, isExport, type,searchPage);
	}
	@Override
	public InfoListResult<FtsDocumentStatus> getStatusList(QueryBuilder builder, User user, boolean sim,
														   boolean irSimflag,boolean irSimflagAll,boolean isExport,String type,String searchPage) throws TRSException {
		// 暂时不用了
		List<FtsDocumentStatus> ftsList = new ArrayList<>();
		// List<String> md5List = new ArrayList<>();
		final String pageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		final String nextPageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		try {
			// 把表达式放缓存里边 把key值返回给前端
			// long threadId = Thread.currentThread().getId();
			// //把线程Id作为key一部分 在hybaseimpl拼写完整表达式时更新
			// String trslk = RedisUtil.PER_KEY + threadId;
			// //如果需要转换成server key就以server结尾 在导出exel时用
			// if(builder.isServer()){
			// trslk = trslk+RedisUtil.SUFFIX_KEY;
			// }
			// RedisUtil.setString(trslk, builder.asTRSL());
			// String trslk = RedisUtil.saveKey(builder);
			// 在这存个空的 在hybase里边填完整的
			String trslk = pageId + "trslk";
			RedisUtil.setString(trslk, builder.asTRSL());
			builder.setKeyRedis(trslk);
			// 异步计算
			String trslkHot = pageId + "hot";
			RedisUtil.setString(trslkHot, builder.asTRSL());
			log.info(builder.asTRSL());
			PagedList<FtsDocumentStatus> pagedList = hybase8SearchService.ftsPageList(builder, FtsDocumentStatus.class,
					sim, irSimflag,irSimflagAll,type);
			List<FtsDocumentStatus> list = pagedList.getPageItems();

			label: {
				while (list.size() > 0) {
					// int index = 0;
					// 检验是否预警
					List<String> sids = new ArrayList<>();
					StringBuilder sb = new StringBuilder();
					for (FtsDocumentStatus document : list) {
						sids.add(document.getMid());
						sb.append(document.getMid()).append(",");
					}
					// 把已经预警的装里边
					//不查预警
					/*List<String> sidAlert = new ArrayList<>();
					long alertBefore = System.currentTimeMillis();
					List<AlertEntity> alertList = null;
					if (httpClient){
						String aSids = "";
						if (sb.length() > 0){
							aSids = sb.substring(0,sb.length()-1);
						}
						alertList = AlertUtil.getAlerts(userId,aSids,alertNetinsightUrl);
					}else {
						alertList = alertRepository.findByUserIdAndSidIn(userId, sids);
					}

					long alertAfter = System.currentTimeMillis();
					log.info("预警表查询用了" + (alertAfter - alertBefore));
					if (null != alertList && alertList.size() > 0){
						for (AlertEntity alert : alertList) {
							sidAlert.add(alert.getSid());
						}
					}

					log.info("预警表查询用了" + (alertAfter - alertBefore));
					String userName = UserUtils.getUser().getUserName();
					if (userName != null && userName.equals("xiaoying")) {
						log.info("xiaoying预警表查询用了" + (alertAfter - alertBefore));
					}*/


					List<Favourites> favouritesList = favouritesService.findAll(user);
					//List<Favourites> library = favouritesRepository.findByUserIdAndSidIn(userId, sids);
					for (FtsDocumentStatus ftsDocumentStatus : list) {
						// 检验是否收藏
						String id = ftsDocumentStatus.getMid();
						// 预警数据
						//不查预警
						/*int indexOf = sidAlert.indexOf(id);
						if (indexOf < 0) {
							ftsDocumentStatus.setSend(false);
						} else {
							ftsDocumentStatus.setSend(true);
						}*/
						// ftsDocumentStatus.isFavourites(library.stream().anyMatch(sid
						// -> sid.getSid()));
						for (Favourites favourites : favouritesList) {
							if (favourites.getSid().toString().equals(id)) {// 该文章收藏了
								ftsDocumentStatus.setFavourite(true);
								break;
							} else {
								ftsDocumentStatus.setFavourite(false);
							}
						}
						if (ObjectUtils.isEmpty(favouritesList)) {// 因为当收藏列表为空时，程序不进以上的遍历，上面也不用写非空了
							ftsDocumentStatus.setFavourite(false);
						}
						if (StringUtil.isNotEmpty(ftsDocumentStatus.getContent())) {
							String content = ftsDocumentStatus.getStatusContent();
							// 此处对文章具体内容按第一个命中的词的前后截取共150个字
							//Matcher matcher = Pattern.compile("font").matcher(content);
							ftsDocumentStatus.setStatusContent(StringUtil.cutContentPro(StringUtil.replaceImg(content), Const.CONTENT_LENGTH));
						}
						/*if (content.length() > 150 && matcher.find()) {
							int targetIndex = matcher.start();
							if (75 > targetIndex) {
								String str1 = content.substring(0, 150);
								content = str1 + "...";
							} else if (75 < targetIndex && content.length() - targetIndex >= 75) {
								String str1 = content.substring(targetIndex - 75, targetIndex);
								String str2 = content.substring(targetIndex, targetIndex + 75);
								content = "..." + str1 + str2 + "...";
							} else if (75 < targetIndex && content.length() - targetIndex < 75) {
								String str1 = content.substring(content.length() - 150, content.length());
								content = "..." + str1;
							}
						}*/
						ftsDocumentStatus.setTrslk(trslk);

						// md5List.add(ftsDocumentStatus.getMd5Tag());
						ftsList.add(ftsDocumentStatus);
					}
					// 不排重的情况下
					TimingCachePool.put(pageId, new InfoListResult<>(pageId, pagedList, nextPageId, 0, trslk));
					// 计算相似文章
					// final String trsSim = trslk;
					if(!isExport){//导出就不算相似文章数
						fixedThreadPool.execute(() -> calculateSimNumStatus(pageId, ftsList, user, trslkHot,type,searchPage));
					}
					break label;
				}
			}
		} catch (Exception e) {
			throw new OperationException("检索异常：" + e.getMessage(),e);
		}
		return TimingCachePool.get(pageId);
	}

	@Override
	public InfoListResult<FtsDocumentTF> getTFList(QueryBuilder builder, User user, boolean sim,String type)
			throws TRSException {
		// 暂时不用了
		List<FtsDocumentTF> ftsList = new ArrayList<>();
		// List<String> md5List = new ArrayList<>();
		final String pageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		final String nextPageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		try {
			// String trslk = RedisUtil.saveKey(builder);
			// 在这存个空的 在hybase里边填完整的
			String trslk = pageId + "trslk";
			RedisUtil.setString(trslk, "");
			builder.setKeyRedis(trslk);
			// 异步计算
			String trslkHot = pageId + "hot";
			RedisUtil.setString(trslkHot, builder.asTRSL());
			// 把表达式放缓存里边 把key值返回给前端
			// long threadId = Thread.currentThread().getId();
			// String trslk = RedisUtil.PER_KEY + threadId;
			// //如果需要转换成server key就以server结尾 在导出exel时用
			// if(builder.isServer()){
			// trslk = trslk+RedisUtil.SUFFIX_KEY;
			// }
			// RedisUtil.setString(trslk, builder.asTRSL());
			log.info(builder.asTRSL());
			PagedList<FtsDocumentTF> pagedList = hybase8SearchService.ftsPageList(builder, FtsDocumentTF.class, sim,
					false,false,type);
			List<FtsDocumentTF> list = pagedList.getPageItems();

			label: {
				while (list.size() > 0) {
					// int index = 0;
					// 检验是否预警
					List<String> sids = new ArrayList<>();
					StringBuilder sb = new StringBuilder();
					for (FtsDocumentTF document : list) {
						sids.add(document.getSid());
						sb.append(document.getSid()).append(",");
					}
					// 把已经预警的装里边
					//不查预警了
				/*	List<String> sidAlert = new ArrayList<>();
					long alertBefore = System.currentTimeMillis();
					List<AlertEntity> alertList = null;
					if (httpClient){
						String aSids = "";
						if (sb.length() > 0){
							aSids = sb.substring(0,sb.length() - 1 );
						}
						alertList = AlertUtil.getAlerts(userId,aSids,alertNetinsightUrl);
					}else {
						alertList = alertRepository.findByUserIdAndSidIn(userId, sids);
					}

					long alertAfter = System.currentTimeMillis();
					log.info("预警表查询用了" + (alertAfter - alertBefore));
					String userName = UserUtils.getUser().getUserName();
					if (userName != null && userName.equals("xiaoying")) {
						log.info("xiaoying预警表查询用了" + (alertAfter - alertBefore));
					}
					if (ObjectUtil.isNotEmpty(alertList)){
						for (AlertEntity alert : alertList) {
							sidAlert.add(alert.getSid());
						}
					}*/

					List<Favourites> favouritesList = favouritesService.findAll(user);
					//List<Favourites> library = favouritesRepository.findByUserIdAndSidIn(userId, sids);
					for (FtsDocumentTF document : list) {
						// 检验是否收藏
						String id = document.getSid();
						document.setSiteName(document.getGroupName());// siteName
						// 预警数据
						//不查预警了
						/*int indexOf = sidAlert.indexOf(id);
						if (indexOf < 0) {
							document.setSend(false);
						} else {
							document.setSend(true);
						}*/
						// ftsDocumentStatus.isFavourites(library.stream().anyMatch(sid
						// -> sid.getSid()));
						for (Favourites favourites : favouritesList) {
							if (favourites.getSid().toString().equals(id)) {// 该文章收藏了
								document.setFavourite(true);
								break;
							} else {
								document.setFavourite(false);
							}
						}
						if (ObjectUtils.isEmpty(favouritesList)) {// 因为当收藏列表为空时，程序不进以上的遍历，上面也不用写非空了
							document.setFavourite(false);
						}
						String content = document.getStatusContent();
						content = StringUtil.replaceImg(content);
						// 此处对文章具体内容按第一个命中的词的前后截取共150个字
						Matcher matcher = Pattern.compile("font").matcher(content);
						if (content.length() > 150 && matcher.find()) {
							int targetIndex = matcher.start();
							if (75 > targetIndex) {
								String str1 = content.substring(0, 150);
								content = str1 + "...";
							} else if (75 < targetIndex && content.length() - targetIndex >= 75) {
								String str1 = content.substring(targetIndex - 75, targetIndex);
								String str2 = content.substring(targetIndex, targetIndex + 75);
								content = "..." + str1 + str2 + "...";
							} else if (75 < targetIndex && content.length() - targetIndex < 75) {
								String str1 = content.substring(content.length() - 150, content.length());
								content = "..." + str1;
							}
						}
						document.setStatusContent(content);
						document.setTrslk(trslk);

						// md5List.add(ftsDocumentStatus.getMd5Tag());
						ftsList.add(document);
					}
					// 不排重的情况下
					TimingCachePool.put(pageId, new InfoListResult<>(pageId, pagedList, nextPageId, 0, trslk));
					// 计算相似文章
					// final String trsSim = trslk;
					fixedThreadPool
							.execute(() -> calculateSimNumTF(pageId, ftsList, user, trslkHot, builder.isServer(),type));

					break label;
				}
			}
		} catch (Exception e) {
			throw new OperationException("检索异常：", e);
		}
		return TimingCachePool.get(pageId);
	}
	@Override
	public InfoListResult<FtsDocumentWeChat> getWeChatList(QueryBuilder builder, User user, boolean sim,
														   boolean irSimflag,boolean irSimflagAll,boolean isExport,String type) throws TRSException{
		String searchPage = SearchPage.COMMON_SEARCH.toString();
		return getWeChatList(builder,user,sim,irSimflag,irSimflagAll,isExport,type,searchPage);
	}

	@Override
	public InfoListResult<FtsDocumentWeChat> getWeChatList(QueryBuilder builder, User user, boolean sim,
														   boolean irSimflag,boolean irSimflagAll,boolean isExport,String type,String searchPage) throws TRSException {
		// 暂时不用
		List<FtsDocumentWeChat> ftsList = new ArrayList<>();
		final String pageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		final String nextPageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		try {
			// String trslk = RedisUtil.saveKey(builder);
			// 在这存个空的 在hybase里边填完整的
			String trslk = pageId + "trslk";
			RedisUtil.setString(trslk, builder.asTRSL());
			builder.setKeyRedis(trslk);
			// 异步计算
			String trslkHot = pageId + "hot";
			RedisUtil.setString(trslkHot, builder.asTRSL());
			// 把表达式放缓存里边 把key值返回给前端
			// long threadId = Thread.currentThread().getId();
			// String trslk = RedisUtil.PER_KEY + threadId;
			// //如果需要转换成server key就以server结尾 在导出exel时用
			// if(builder.isServer()){
			// trslk = trslk+RedisUtil.SUFFIX_KEY;
			// }
			// RedisUtil.setString(trslk, builder.asTRSL());
			log.info(builder.asTRSL());
			PagedList<FtsDocumentWeChat> pageList = hybase8SearchService.ftsPageList(builder, FtsDocumentWeChat.class,
					sim, irSimflag,irSimflagAll,type);
			List<FtsDocumentWeChat> list = pageList.getPageItems();
			for (FtsDocumentWeChat ftsDocumentWeChat : list) {
				ftsDocumentWeChat.setGroupName("微信");
			}

			label: {
				while (list.size() > 0) {
					// int index = 0;
					// 检验是否预警
					List<String> sids = new ArrayList<>();
					StringBuilder sb = new StringBuilder();
					for (FtsDocumentWeChat document : list) {
						sids.add(document.getHkey());
						sb.append(document.getHkey()).append(",");
					}
					// 把已经预警的装里边
					//暂时不查预警了
					/*List<String> sidAlert = new ArrayList<>();
					long alertBefore = System.currentTimeMillis();
					List<AlertEntity> alertList = null;
					if (httpClient){
						String aSids = "";
						if (sb.length() > 0){
							aSids = sb.substring(0,sb.length()-1);
						}
						alertList = AlertUtil.getAlerts(userId,aSids,alertNetinsightUrl);
					}else {
						alertList = alertRepository.findByUserIdAndSidIn(userId, sids);
					}
					long alertAfter = System.currentTimeMillis();
					log.info("预警表查询用了" + (alertAfter - alertBefore));
					if (null != alertList && alertList.size()>0){
						for (AlertEntity alert : alertList) {
							sidAlert.add(alert.getSid());
						}
					}*/

					List<Favourites> favouritesList = favouritesService.findAll(user);
					//List<Favourites> library = favouritesRepository.findByUserIdAndSidIn(userId, sids);
					for (FtsDocumentWeChat ftsDocumentWeChat : list) {
						String id = ftsDocumentWeChat.getHkey();
						// 预警数据
						//暂时不查预警了
						/*int indexOf = sidAlert.indexOf(id);
						if (indexOf < 0) {
							ftsDocumentWeChat.setSend(false);
						} else {
							ftsDocumentWeChat.setSend(true);
						}*/
						for (Favourites favourites : favouritesList) {
							if (favourites.getSid().toString().equals(id)) {// 该文章收藏了
								ftsDocumentWeChat.setFavourite(true);
								break;
							} else {
								ftsDocumentWeChat.setFavourite(false);
							}
						}
						if (ObjectUtils.isEmpty(favouritesList)) {// 因为当收藏列表为空时，程序不进以上的遍历，上面也不用写非空了
							ftsDocumentWeChat.setFavourite(false);
						}
						if (StringUtil.isNotEmpty(ftsDocumentWeChat.getContent())) {
							//ftsDocumentWeChat.setContent(StringUtil.substringRed(content, Const.CONTENT_LENGTH));
							ftsDocumentWeChat.setContent(StringUtil.cutContentPro(StringUtil.replaceImg(ftsDocumentWeChat.getContent()),  Const.CONTENT_LENGTH));
						}
						ftsDocumentWeChat.setTrslk(trslk);
						if (StringUtil.isNotEmpty(ftsDocumentWeChat.getUrlTitle())){
							ftsDocumentWeChat.setUrlTitle( StringUtil.replaceAnnotation(ftsDocumentWeChat.getUrlTitle()).replace("&amp;nbsp;", ""));
						}
						// ftsDocumentWeChat.setSid(ftsDocumentWeChat.getHkey());
						// md5List.add(ftsDocumentStatus.getMd5Tag());
						//微信库主键是hkey  sid没啥用
						ftsDocumentWeChat.setSid(ftsDocumentWeChat.getHkey());
						ftsList.add(ftsDocumentWeChat);
					}
					// if (newList.size() == 0) {
					// return null;
					// }
					// 不排重的情况下
					// 把表达式放缓存里边 把key值返回给前端
					TimingCachePool.put(pageId, new InfoListResult<>(pageId, pageList, nextPageId, 0, trslk));
					// 计算相似文章
					// final String trsSim = trslk;
					if(!isExport){
						fixedThreadPool.execute(
								() -> calculateSimNumWeChat(pageId, ftsList, user, trslkHot, builder.isServer(),type,searchPage));
					}
					break label;
				}
			}
		} catch (Exception e) {
			throw new OperationException("检索异常：" + e);
		}
		Object object = TimingCachePool.get(pageId);
		return (InfoListResult<FtsDocumentWeChat>) object;
	}

	/**
	 * 查询下一页 舆情报告展示收藏
	 *
	 * @param specialId
	 *            专项id
	 * @param builder
	 *            QueryBuilder
	 * @param index
	 *            当前位置
	 * @param pageId
	 *            当前页
	 * @param md5List
	 *            MD5List
	 * @param ftsList
	 *            ftsList
	 */
	@SuppressWarnings("unused")

	private void nextListForReport(String specialId, QueryBuilder builder, int index, String pageId,
								   List<String> md5List, List<FtsDocument> ftsList, User user) {
		try {
			List<FtsDocument> childFtsList = new ArrayList<>();
			if (ftsList == null) {
				return;
			}
			// 只算5次
			int j = 0;
			while (index <= ftsList.size() && ftsList.size() > 0 && j++ < 5) {
				for (int i = index; i < ftsList.size(); i++) {
					FtsDocument document = ftsList.get(i);
					if (md5List == null) {
						return;
					}
					if (!md5List.contains(document.getMd5Tag())) {
						List<Favourites> favourite = favouritesService.findByUserAndSid(user,document.getSid());
						if (ObjectUtil.isNotEmpty(favourite)) {
							document.setStore(Store.hasStore);
						} else {
							document.setStore(Store.noStore);
						}
						md5List.add(document.getMd5Tag());
						childFtsList.add(document);
					}
					if (childFtsList.size() == builder.getPageSize()) {

					}
					// pageId = guidNext(childFtsList, builder, pageId,
					// specialId);
				}
				if (ftsList.size() < builder.getPageSize()) {

				}
				// pageId = guidNext(childFtsList, builder, pageId, specialId);
				builder.setPageNo(builder.getPageNo() + 1);
				ftsList = hybase8SearchService.ftsQuery(builder, FtsDocument.class, true, false,false,null);
				index = 0;
			}
		} catch (Exception e) {
			log.error("searchNext error", e);
		}
	}

	/**
	 * 查相似文章信息
	 *
	 * @param md5
	 * @return
	 * @throws TRSSearchException
	 * @throws TRSException
	 */
	@Override
	public void simCount(String sid, String md5,String type) {
		if (StringUtil.isNotEmpty(md5)) {
			String trsl = "MD5TAG:(" + md5 + ")";
			QueryBuilder query = new QueryBuilder();
			query.filterByTRSL(trsl);
			query.setDatabase(Const.HYBASE_NI_INDEX);
			// 相似文章列表
			long ftsCount = hybase8SearchService.ftsCount(query, false, false,false,type);
			// 查找详情的相似文章列表
			QueryBuilder querySim = new QueryBuilder();
			querySim.filterByTRSL(trsl);
			querySim.setDatabase(Const.HYBASE_NI_INDEX);
			querySim.orderBy(ESFieldConst.IR_URLTIME, true);
			// querySim.page(0, 20);
			querySim.page(0, 5);
			// String startSim = new SimpleDateFormat("yyyyMMddHHmmssSSS")
			// .format(new Date() );
			List<FtsDocument> ftsQuerySim = null;
			try {
				ftsQuerySim = hybase8SearchService.ftsQuery(querySim, FtsDocument.class, false, false,false,type);
			} catch (TRSSearchException e) {
				e.printStackTrace();
			} catch (TRSException e) {
				e.printStackTrace();
			}
			List<Map<String, String>> listSim = new ArrayList<>();
			if(ftsQuerySim!=null){
				for (FtsDocument simDocument : ftsQuerySim) {
					Map<String, String> mapSim = new HashMap<>();
					mapSim.put("simId", simDocument.getSid());
					mapSim.put("simTitle", simDocument.getTitle());
					mapSim.put("webCount", simDocument.getSiteName());
					long nowTime = new Date().getTime();
					long lastTime = simDocument.getUrlTime().getTime();
					long result = nowTime - lastTime;
					// 计算天数差
					int days = (int) (result / (1000 * 60 * 60 * 24));
					// 计算小时查
					int hours = (int) (result / (1000 * 60 * 60));
					// 计算分钟差
					int minutes = (int) (result / (1000 * 60));
					if (0 != minutes) {
						mapSim.put("timeAgo", minutes + "分钟前");
					}
					if (0 != hours) {
						mapSim.put("timeAgo", hours + "小时前");
					}
					if (0 != days) {
						mapSim.put("timeAgo", days + "天前");
					}
					listSim.add(mapSim);

				}
			}
			Map<String, Object> map = new HashMap<>();
			map.put("simuList", listSim);
			// 计算相似文章时 如果值为1 置 0
			if (ftsCount == 1L) {
				ftsCount = 0L;
			}
			map.put("simcount", ftsCount);
			// TimingCachePool.put("async:" + pageId, asyncList);
			// RedisFactory.setValueToRedis("async:" + pageId, asyncList);
			TimingCachePool.put("async:" + sid, map);
			RedisFactory.setValueToRedis("async:" + sid, map);
		}

		// all.add(map);
		// return ftsCount;
	}

	@Override
	public void simCountStatus(String mid, String md5,String type) {
		if (StringUtil.isNotEmpty(md5)) {
			String trsl = "MD5TAG:(" + md5 + ")";
			QueryBuilder query = new QueryBuilder();
			query.filterByTRSL(trsl);
			// 相似文章列表
			long ftsCount = hybase8SearchService.ftsCount(query, FtsDocumentStatus.class, true, false,false,type);
			// 查找详情的相似文章列表
			QueryBuilder querySim = new QueryBuilder();
			querySim.filterByTRSL(trsl);
			// querySim.orderBy(ESFieldConst.IR_URLTIME, true);
			// querySim.page(0, 20);
			querySim.page(0, 5);
			// String startSim = new SimpleDateFormat("yyyyMMddHHmmssSSS")
			// .format(new Date() );
			List<FtsDocumentStatus> ftsQuerySim = null;
			try {
				ftsQuerySim = hybase8SearchService.ftsQuery(querySim, FtsDocumentStatus.class, true, false,false,type);
			} catch (TRSSearchException e) {
				e.printStackTrace();
			} catch (TRSException e) {
				e.printStackTrace();
			}
			List<Map<String, String>> listSim = new ArrayList<>();
			for (FtsDocumentStatus simDocument : ftsQuerySim) {
				Map<String, String> mapSim = new HashMap<>();
				mapSim.put("simId", simDocument.getMid());
				mapSim.put("simTitle", simDocument.getStatusContent());
				mapSim.put("webCount", simDocument.getSiteName());
				long nowTime = new Date().getTime();
				long lastTime = simDocument.getCreatedAt().getTime();
				long result = nowTime - lastTime;
				// 计算天数差
				int days = (int) (result / (1000 * 60 * 60 * 24));
				// 计算小时查
				int hours = (int) (result / (1000 * 60 * 60));
				// 计算分钟差
				int minutes = (int) (result / (1000 * 60));
				if (0 != minutes) {
					mapSim.put("timeAgo", minutes + "分钟前");
				}
				if (0 != hours) {
					mapSim.put("timeAgo", hours + "小时前");
				}
				if (0 != days) {
					mapSim.put("timeAgo", days + "天前");
				}
				listSim.add(mapSim);

			}
			Map<String, Object> map = new HashMap<>();
			map.put("simuList", listSim);
			// 计算相似文章时 如果值为1 置 0
			if (ftsCount == 1L) {
				ftsCount = 0L;
			}
			map.put("simcount", ftsCount);
			// TimingCachePool.put("async:" + pageId, asyncList);
			// RedisFactory.setValueToRedis("async:" + pageId, asyncList);
			TimingCachePool.put("async:" + mid, map);
			RedisFactory.setValueToRedis("async:" + mid, map);
		}

		// all.add(map);
		// return ftsCount;
	}

	@Override
	public void simCountWeChat(String hkey, String md5,String type) {
		if (StringUtil.isNotEmpty(md5)) {
			String trsl = "MD5TAG:(" + md5 + ")";
			QueryBuilder query = new QueryBuilder();
			query.filterByTRSL(trsl);
			// 相似文章列表
			long ftsCount = hybase8SearchService.ftsCount(query, FtsDocumentWeChat.class, true, false,false,type);
			// 查找详情的相似文章列表
			QueryBuilder querySim = new QueryBuilder();
			querySim.filterByTRSL(trsl);
			// querySim.orderBy(ESFieldConst.IR_URLTIME, true);
			// querySim.page(0, 20);
			querySim.page(0, 5);
			// String startSim = new SimpleDateFormat("yyyyMMddHHmmssSSS")
			// .format(new Date() );
			List<FtsDocumentWeChat> ftsQuerySim = null;
			try {
				ftsQuerySim = hybase8SearchService.ftsQuery(querySim, FtsDocumentWeChat.class, true, false,false,type);
			} catch (TRSSearchException e) {
				e.printStackTrace();
			} catch (TRSException e) {
				e.printStackTrace();
			}
			List<Map<String, String>> listSim = new ArrayList<>();
			for (FtsDocumentWeChat simDocument : ftsQuerySim) {
				Map<String, String> mapSim = new HashMap<>();
				mapSim.put("simId", simDocument.getHkey());
				mapSim.put("simTitle", simDocument.getUrlTitle());
				long nowTime = new Date().getTime();
				long lastTime = simDocument.getUrlTime().getTime();
				long result = nowTime - lastTime;
				// 计算天数差
				int days = (int) (result / (1000 * 60 * 60 * 24));
				// 计算小时查
				int hours = (int) (result / (1000 * 60 * 60));
				// 计算分钟差
				int minutes = (int) (result / (1000 * 60));
				if (0 != minutes) {
					mapSim.put("timeAgo", minutes + "分钟前");
				}
				if (0 != hours) {
					mapSim.put("timeAgo", hours + "小时前");
				}
				if (0 != days) {
					mapSim.put("timeAgo", days + "天前");
				}
				listSim.add(mapSim);

			}
			Map<String, Object> map = new HashMap<>();
			map.put("simuList", listSim);
			// 计算相似文章时 如果值为1 置 0
			if (ftsCount == 1L) {
				ftsCount = 0L;
			}
			map.put("simcount", ftsCount);
			// TimingCachePool.put("async:" + pageId, asyncList);
			// RedisFactory.setValueToRedis("async:" + pageId, asyncList);
			TimingCachePool.put("async:" + hkey, map);
			RedisFactory.setValueToRedis("async:" + hkey, map);
		}

		// all.add(map);
		// return ftsCount;
	}

	/**
	 * 计算相似文章数和头像url
	 *
	 * @param pageId
	 *            数据缓存Id
	 * @param trslk
	 */
	private void calculateSimNum(String pageId, final List<FtsDocument> documentList, User user, String trslk,
								 boolean server,String type) {
		try {
			ObjectUtil.assertNull(documentList, "FtsDocumentList ");
			String trsl = RedisUtil.getString(trslk);
			List<AsyncDocument> asyncList = new ArrayList<>();

			// 检验是否预警
			List<String> sids = new ArrayList<>();
			for (FtsDocument document : documentList) {
				sids.add(document.getSid());
			}

			List<Favourites> favouritesList = favouritesService.findAll(user);
			//List<Favourites> library = favouritesRepository.findByUserIdAndSidIn(userId, sids);
			// 把已经预警的装里边
			List<String> sidAlert = new ArrayList<>();
			long alertBefore = System.currentTimeMillis();
			List<AlertEntity> alertList = alertService.findByUser(user, sids);
			long alertAfter = System.currentTimeMillis();
			log.info("预警表查询用了" + (alertAfter - alertBefore));
			/*
			 * String userName = UserUtils.getUser().getUserName();
			 * if(userName.equals("xiaoying")){
			 * log.info("xiaoying预警表查询用了"+(alertAfter-alertBefore)); }
			 */
			for (AlertEntity alert : alertList) {
				sidAlert.add(alert.getSid());
			}
			for (FtsDocument document : documentList) {
				String id = document.getSid();
				String md5Tag = document.getMd5Tag();
				AsyncDocument asyncDocument = new AsyncDocument();
				asyncDocument.setId(id);
				// 预警数据
				int indexOf = sidAlert.indexOf(id);
				if (indexOf < 0) {
					asyncDocument.setSend(false);
				} else {
					asyncDocument.setSend(true);
				}
				if (StringUtil.isNotEmpty(md5Tag)) { // 获取相似文章数
					QueryBuilder searchBuilder = new QueryBuilder();
					searchBuilder.filterByTRSL(trsl);
					searchBuilder.filterField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);
					// builder中加入groupName
					String indices = FtsParser.getDatabases(FtsDocument.class);
					searchBuilder.setDatabase(indices);
					// 算相似文章数时 如果值为1 置 0
					searchBuilder.setServer(server);
					//逻辑修改:热度值及相似文章数计算之前先进行站内排重  2019-12-3
					long ftsCount = hybase8SearchService.ftsCount(searchBuilder, false, true,false,type);

					if (ftsCount == 1L) {
						asyncDocument.setSimNum(0L);
					} else {
						asyncDocument.setSimNum(ftsCount);
					}
					// } else {
					// asyncDocument.setSimNum(0L);
				} else {
					asyncDocument.setSimNum(0L);
				}

				// 检验收藏
				asyncDocument.setFavourite(favouritesList.stream().anyMatch(sid -> sid.getSid().equals(id)));
				asyncList.add(asyncDocument);
			}
			TimingCachePool.put("async:" + pageId, asyncList);
			RedisFactory.setValueToRedis("async:" + pageId, asyncList);
		} catch (Exception e) {
			log.error("setSimNumAndPicUrl error", e);
		}
	}
	private void calculateSimNum(String pageId, final List<FtsDocument> documentList, User user, String trslk,
								 boolean server, boolean sim, boolean irSimflag,String type){
		String searchPage = SearchPage.COMMON_SEARCH.toString();
		calculateSimNum(pageId, documentList, user, trslk, server, sim, irSimflag, type,searchPage);
	}
	private void calculateSimNum(String pageId, final List<FtsDocument> documentList, User user, String trslk,
								 boolean server, boolean sim, boolean irSimflag,String type,String searchPage) {
		try {
			log.info("相似文章数计算-传统媒体："+"async:" + pageId+"开始");
			ObjectUtil.assertNull(documentList, "FtsDocumentList ");
			String trsl = RedisUtil.getString(trslk);
			if (StringUtil.isNotEmpty(trsl)) {
				trsl = trsl.replaceAll("AND \\(IR_SIMFLAGALL:\\(\"0\" OR \"\"\\)\\)"," ");
			}
			List<AsyncDocument> asyncList = new ArrayList<>();
			List<AsyncInfo> asyncInfoList = new ArrayList<>();

			// 检验是否预警
			List<String> sids = new ArrayList<>();
			for (FtsDocument document : documentList) {
				sids.add(document.getSid());
			}

			List<Favourites> favouritesList = favouritesService.findAll(user);

			/*
			注释为：查询数据是否被预警 20191223注释
			预警信息存储在json文件中，如果查询应该去查询json文件，而且现在因为预警信息过多，查询会造成速度很慢，所以不查
			List<String> sidAlert = new ArrayList<>();
			long alertBefore = System.currentTimeMillis();
			List<AlertEntity> alertList = alertService.findByUser(user, sids);
			long alertAfter = System.currentTimeMillis();
			log.info("预警表查询用了" + (alertAfter - alertBefore));

			for (AlertEntity alert : alertList) {
				sidAlert.add(alert.getSid());
			}*/
			for (FtsDocument document : documentList) {
				String indices = FtsParser.getDatabases(FtsDocument.class);
				String id = document.getSid();
				String md5Tag = document.getMd5Tag();
				AsyncDocument asyncDocument = new AsyncDocument();
				asyncDocument.setId(id);
				// 预警数据
				/*int indexOf = sidAlert.indexOf(id);
				if (indexOf < 0) {
					asyncDocument.setSend(false);
				} else {
					asyncDocument.setSend(true);
				}*/
				if (StringUtil.isNotEmpty(md5Tag)) { // 获取相似文章数
					QueryBuilder searchBuilder = new QueryBuilder();
					searchBuilder.filterByTRSL(trsl);
					searchBuilder.filterField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);
					// builder中加入groupName
					if(StringUtil.isNotEmpty(id) && searchPage.equals(SearchPage.ORDINARY_SEARCH.toString())){
						//id不为空，则去掉当前文章
						StringBuffer idBuffer = new StringBuffer();
						if(Const.MEDIA_TYPE_WEIBO.contains(document.getGroupName())){
							idBuffer.append(FtsFieldConst.FIELD_MID).append(":(").append(id).append(")");
						}else if(Const.MEDIA_TYPE_WEIXIN.contains(document.getGroupName())){
							idBuffer.append(FtsFieldConst.FIELD_HKEY).append(":(").append(id).append(")");
						}else{
							idBuffer.append(FtsFieldConst.FIELD_SID).append(":(").append(id).append(")");
						}
						searchBuilder.filterByTRSL_NOT(idBuffer.toString());
					}
					searchBuilder.setDatabase(indices);
					// 算相似文章数时 如果值为1 置 0
					searchBuilder.setServer(server);
					//逻辑修改:热度值及相似文章数计算之前先进行站内排重  2019-12-3
					long ftsCount = hybase8SearchService.ftsCount(searchBuilder, false, true,false,type);

					if (ftsCount == 1L) {
						if(SearchPage.ORDINARY_SEARCH.toString().equals(searchPage)){
							asyncDocument.setSimNum(ftsCount);
						}else{
							asyncDocument.setSimNum(0L);
						}
					} else {
						asyncDocument.setSimNum(ftsCount);
					}
				} else {
					asyncDocument.setSimNum(0L);
				}

				// 检验收藏
				asyncDocument.setFavourite(favouritesList.stream().anyMatch(sid -> sid.getSid().equals(id)));
				asyncList.add(asyncDocument);
				AsyncInfo asyncInfo = new AsyncInfo();
				asyncInfo.setAsyncDocument(asyncDocument);
				asyncInfo.setMd5(md5Tag);
				asyncInfo.setDatabase(indices);
				asyncInfo.setGroupName(document.getGroupName());
				asyncInfoList.add(asyncInfo);
			}
			log.info("相似文章数计算-传统媒体："+"async:" + pageId+"完成，数据为："+asyncList.size());
			TimingCachePool.put("async:" + pageId, asyncList);
			RedisFactory.setValueToRedis("async:" + pageId, asyncList);
			if(SearchPage.ORDINARY_SEARCH.toString().equals(searchPage)){
				fixedThreadPool.execute(
						() -> querySiteNameForSimMd5( pageId, trslk, server, type,asyncInfoList));
			}

		} catch (Exception e) {
			log.error("setSimNumAndPicUrl error", e);
		}
	}

	private void calculateSimNumStatus(String pageId, final List<FtsDocumentStatus> documentList, User user,
									   String trslk,String type){
		String searchPage = SearchPage.COMMON_SEARCH.toString();
		calculateSimNumStatus(pageId, documentList, user, trslk, type,searchPage);
	}
	/**
	 * 计算相似文章数和头像url
	 *
	 * @param pageId
	 *            数据缓存Id
	 * @param trslk
	 */
	private void calculateSimNumStatus(String pageId, final List<FtsDocumentStatus> documentList, User user,
									   String trslk,String type,String searchPage) {
		try {
			log.info("相似文章数计算-微博："+"async:" + pageId+"开始");
			ObjectUtil.assertNull(documentList, "FtsDocumentStatus ");
			String trsl = RedisUtil.getString(trslk);
			if (StringUtil.isNotEmpty(trsl)) {
				trsl = trsl.replaceAll("AND \\(IR_SIMFLAGALL:\\(\"0\" OR \"\"\\)\\)"," ");
			}
			List<AsyncDocument> asyncList = new ArrayList<>();
			List<AsyncInfo> asyncInfoList = new ArrayList<>();
			// 检验是否预警
			List<String> sids = new ArrayList<>();
			for (FtsDocumentStatus document : documentList) {
				sids.add(document.getMid());
			}

			List<Favourites> favouritesList = favouritesService.findAll(user);
			//List<Favourites> library = favouritesRepository.findByUserIdAndSidIn(userId, sids);
			// 把已经预警的装里边
			/*
			注释为：查询数据是否被预警 20191223注释
			预警信息存储在json文件中，如果查询应该去查询json文件，而且现在因为预警信息过多，查询会造成速度很慢，所以不查
			List<String> sidAlert = new ArrayList<>();
			long alertBefore = System.currentTimeMillis();
			List<AlertEntity> alertList = alertService.findByUser(user, sids);
			long alertAfter = System.currentTimeMillis();
			log.info("预警表查询用了" + (alertAfter - alertBefore));
			String userName = UserUtils.getUser().getUserName();
			if (userName != null && userName.equals("xiaoying")) {
				log.info("xiaoying预警表查询用了" + (alertAfter - alertBefore));
			}
			for (AlertEntity alert : alertList) {
				sidAlert.add(alert.getSid());
			}*/
			for (FtsDocumentStatus document : documentList) {
				String indices = FtsParser.getDatabases(FtsDocumentStatus.class);
				String id = document.getMid();
				String md5Tag = document.getMd5Tag();
				AsyncDocument asyncDocument = new AsyncDocument();
				asyncDocument.setId(id);
				// 预警数据
				/*int indexOf = sidAlert.indexOf(id);
				if (indexOf < 0) {
					asyncDocument.setSend(false);
				} else {
					asyncDocument.setSend(true);
				}*/
				if (StringUtil.isNotEmpty(md5Tag)) { // 获取相似文章数
					QueryBuilder searchBuilder = new QueryBuilder();
					searchBuilder.filterByTRSL(trsl);
					searchBuilder.filterField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);
					// searchBuilder.filterField(FtsFieldConst.FIELD_MID, id,
					// Operator.NotEqual);
					if(StringUtil.isNotEmpty(id) && searchPage.equals(SearchPage.ORDINARY_SEARCH.toString())){
						//id不为空，则去掉当前文章
						StringBuffer idBuffer = new StringBuffer();
						if(Const.MEDIA_TYPE_WEIBO.contains(document.getGroupName())){
							idBuffer.append(FtsFieldConst.FIELD_MID).append(":(").append(id).append(")");
						}else if(Const.MEDIA_TYPE_WEIXIN.contains(document.getGroupName())){
							idBuffer.append(FtsFieldConst.FIELD_HKEY).append(":(").append(id).append(")");
						}else{
							idBuffer.append(FtsFieldConst.FIELD_SID).append(":(").append(id).append(")");
						}
						searchBuilder.filterByTRSL_NOT(idBuffer.toString());
					}
					searchBuilder.setDatabase(indices);
					// 算相似文章数时 如果值为1 置 0
					//逻辑修改:热度值及相似文章数计算之前先进行站内排重  2019-12-3
					long ftsCount = hybase8SearchService.ftsCount(searchBuilder, false, true,false,type);
					if (ftsCount == 1L) {
						if(SearchPage.ORDINARY_SEARCH.toString().equals(searchPage)){
							asyncDocument.setSimNum(ftsCount);
						}else{
							asyncDocument.setSimNum(0L);
						}
					} else {
						asyncDocument.setSimNum(ftsCount);
					}
				} else {
					asyncDocument.setSimNum(0L);
				}

				// 检验收藏
				asyncDocument.setFavourite(favouritesList.stream().anyMatch(sid -> sid.getSid().equals(id)));

				asyncList.add(asyncDocument);

				AsyncInfo asyncInfo = new AsyncInfo();
				asyncInfo.setAsyncDocument(asyncDocument);
				asyncInfo.setMd5(md5Tag);
				asyncInfo.setDatabase(indices);
				asyncInfo.setGroupName(document.getGroupName());
				asyncInfoList.add(asyncInfo);
			}
			log.info("相似文章数计算-微博："+"async:" + pageId+"完成，数据为："+asyncList.size());
			TimingCachePool.put("async:" + pageId, asyncList);
			RedisFactory.setValueToRedis("async:" + pageId, asyncList);
			if(SearchPage.ORDINARY_SEARCH.toString().equals(searchPage)){
				fixedThreadPool.execute(
						() -> querySiteNameForSimMd5( pageId, trslk, false, type,asyncInfoList));
			}

		} catch (Exception e) {
			log.error("setSimNumAndPicUrl error", e);
		}
	}

	private void calculateSimNumTF(String pageId, final List<FtsDocumentTF> documentList, User user, String trslk,
								   boolean server,String type){
		String searchPage = SearchPage.COMMON_SEARCH.toString();
		calculateSimNumTF(pageId, documentList, user, trslk, server, type,searchPage);
	}
	/**
	 * 计算相似文章数和头像url
	 *
	 * @param pageId
	 *            数据缓存Id
	 * @param trslk
	 */
	private void calculateSimNumTF(String pageId, final List<FtsDocumentTF> documentList, User user, String trslk,
								   boolean server,String type,String searchPage) {
		try {
			log.info("相似文章数计算-TF数据："+"async:" + pageId+"开始");
			ObjectUtil.assertNull(documentList, "FtsDocumentTF ");
			String trsl = RedisUtil.getString(trslk);
			if (StringUtil.isNotEmpty(trsl)) {
				trsl = trsl.replaceAll("AND \\(IR_SIMFLAGALL:\\(\"0\" OR \"\"\\)\\)"," ");
			}
			List<AsyncDocument> asyncList = new ArrayList<>();
			List<AsyncInfo> asyncInfoList = new ArrayList<>();
			// 检验是否预警
			List<String> sids = new ArrayList<>();
			for (FtsDocumentTF document : documentList) {
				sids.add(document.getSid());
			}

			List<Favourites> favouritesList = favouritesService.findAll(user);
			//List<Favourites> library = favouritesRepository.findByUserIdAndSidIn(userId, sids);
			// 把已经预警的装里边
			/*
			注释为：查询数据是否被预警 20191223注释
			预警信息存储在json文件中，如果查询应该去查询json文件，而且现在因为预警信息过多，查询会造成速度很慢，所以不查
			List<String> sidAlert = new ArrayList<>();
			long alertBefore = System.currentTimeMillis();
			List<AlertEntity> alertList = alertService.findByUser(user, sids);
			long alertAfter = System.currentTimeMillis();
			log.info("预警表查询用了" + (alertAfter - alertBefore));
			String userName = UserUtils.getUser().getUserName();
			if (userName != null && userName.equals("xiaoying")) {
				log.info("xiaoying预警表查询用了" + (alertAfter - alertBefore));
			}
			for (AlertEntity alert : alertList) {
				sidAlert.add(alert.getSid());
			}*/
			for (FtsDocumentTF document : documentList) {
				String indices = FtsParser.getDatabases(FtsDocumentStatus.class);
				String id = document.getSid();
				String md5Tag = document.getMd5Tag();
				AsyncDocument asyncDocument = new AsyncDocument();
				asyncDocument.setId(id);
				// 预警数据
				/*int indexOf = sidAlert.indexOf(id);
				if (indexOf < 0) {
					asyncDocument.setSend(false);
				} else {
					asyncDocument.setSend(true);
				}*/
				if (StringUtil.isNotEmpty(md5Tag)) { // 获取相似文章数
					QueryBuilder searchBuilder = new QueryBuilder();
					searchBuilder.filterByTRSL(trsl);
					searchBuilder.filterField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);
					// searchBuilder.filterField(FtsFieldConst.FIELD_MID, id,
					// Operator.NotEqual);
					if(StringUtil.isNotEmpty(id) && searchPage.equals(SearchPage.ORDINARY_SEARCH.toString())){
						//id不为空，则去掉当前文章
						StringBuffer idBuffer = new StringBuffer();
						if(Const.MEDIA_TYPE_WEIBO.contains(document.getGroupName())){
							idBuffer.append(FtsFieldConst.FIELD_MID).append(":(").append(id).append(")");
						}else if(Const.MEDIA_TYPE_WEIXIN.contains(document.getGroupName())){
							idBuffer.append(FtsFieldConst.FIELD_HKEY).append(":(").append(id).append(")");
						}else{
							idBuffer.append(FtsFieldConst.FIELD_SID).append(":(").append(id).append(")");
						}
						searchBuilder.filterByTRSL_NOT(idBuffer.toString());
					}
					searchBuilder.setDatabase(indices);
					searchBuilder.setServer(server);
					// 算相似文章数时 如果值为1 置 0
					//逻辑修改:热度值及相似文章数计算之前先进行站内排重  2019-12-3
					long ftsCount = hybase8SearchService.ftsCount(searchBuilder, false, true,false,type);
					if (ftsCount == 1L) {
						if(SearchPage.ORDINARY_SEARCH.toString().equals(searchPage)){
							asyncDocument.setSimNum(ftsCount);
						}else{
							asyncDocument.setSimNum(0L);
						}
					} else {
						asyncDocument.setSimNum(ftsCount);
					}
				} else {
					asyncDocument.setSimNum(0L);
				}

				// 检验收藏
				asyncDocument.setFavourite(favouritesList.stream().anyMatch(sid -> sid.getSid().equals(id)));

				asyncList.add(asyncDocument);

				AsyncInfo asyncInfo = new AsyncInfo();
				asyncInfo.setAsyncDocument(asyncDocument);
				asyncInfo.setMd5(md5Tag);
				asyncInfo.setDatabase(indices);
				asyncInfo.setGroupName(document.getGroupName());
				asyncInfoList.add(asyncInfo);
			}
			log.info("相似文章数计算-TF数据："+"async:" + pageId+"完成，数据为："+asyncList.size());
			TimingCachePool.put("async:" + pageId, asyncList);
			RedisFactory.setValueToRedis("async:" + pageId, asyncList);
			if(SearchPage.ORDINARY_SEARCH.toString().equals(searchPage)){
				fixedThreadPool.execute(
						() -> querySiteNameForSimMd5(pageId, trslk, server, type,asyncInfoList));
			}

		} catch (Exception e) {
			log.error("setSimNumAndPicUrl error", e);
		}
	}

	private void calculateSimNumWeChat(String pageId, final List<FtsDocumentWeChat> documentList, User user,
									   String trslk, boolean server,String type){
		String searchPage = SearchPage.COMMON_SEARCH.toString();
		calculateSimNumWeChat(pageId,documentList,user,trslk,server,type,searchPage);
	}
	/**
	 * 计算相似文章数和头像url
	 *
	 * @param pageId
	 *            数据缓存Id
	 * @param trslk
	 */
	private void calculateSimNumWeChat(String pageId, final List<FtsDocumentWeChat> documentList, User user,
									   String trslk, boolean server,String type,String searchPage) {
		try {
			log.info("相似文章数计算-微信："+"async:" + pageId+"开始");
			ObjectUtil.assertNull(documentList, "FtsDocumentWeChat ");
			String trsl = RedisUtil.getString(trslk);
			if (StringUtil.isNotEmpty(trsl)) {
				trsl = trsl.replaceAll("AND \\(IR_SIMFLAGALL:\\(\"0\" OR \"\"\\)\\)"," ");
			}
			List<AsyncDocument> asyncList = new ArrayList<>();
			List<AsyncInfo> asyncInfoList = new ArrayList<>();
			// 检验是否预警
			List<String> sids = new ArrayList<>();
			for (FtsDocumentWeChat document : documentList) {
				sids.add(document.getHkey());
			}

			List<Favourites> favouritesList = favouritesService.findAll(user);
			//List<Favourites> library = favouritesRepository.findByUserIdAndSidIn(userId, sids);
			// 把已经预警的装里边
			/*
			注释为：查询数据是否被预警 20191223注释
			预警信息存储在json文件中，如果查询应该去查询json文件，而且现在因为预警信息过多，查询会造成速度很慢，所以不查
			List<String> sidAlert = new ArrayList<>();
			long alertBefore = System.currentTimeMillis();
			List<AlertEntity> alertList = alertService.findByUser(user, sids);
			long alertAfter = System.currentTimeMillis();
			log.info("预警表查询用了" + (alertAfter - alertBefore));
			String userName = UserUtils.getUser().getUserName();
			for (AlertEntity alert : alertList) {
				sidAlert.add(alert.getSid());
			}*/
			for (FtsDocumentWeChat document : documentList) {
				String indices = FtsParser.getDatabases(FtsDocumentWeChat.class);
				String id = document.getHkey();
				String md5Tag = document.getMd5Tag();
				AsyncDocument asyncDocument = new AsyncDocument();
				asyncDocument.setId(id);
				// 预警数据
				/*int indexOf = sidAlert.indexOf(id);
				if (indexOf < 0) {
					asyncDocument.setSend(false);
				} else {
					asyncDocument.setSend(true);
				}*/
				if (StringUtil.isNotEmpty(md5Tag)) { // 获取相似文章数
					QueryBuilder searchBuilder = new QueryBuilder();
					searchBuilder.filterByTRSL(trsl);
					searchBuilder.filterField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);
					// searchBuilder.filterField(FtsFieldConst.FIELD_HKEY, id,
					// Operator.NotEqual);
					if(StringUtil.isNotEmpty(id) && searchPage.equals(SearchPage.ORDINARY_SEARCH.toString())){
						//id不为空，则去掉当前文章
						StringBuffer idBuffer = new StringBuffer();
						if(Const.MEDIA_TYPE_WEIBO.contains(document.getGroupName())){
							idBuffer.append(FtsFieldConst.FIELD_MID).append(":(").append(id).append(")");
						}else if(Const.MEDIA_TYPE_WEIXIN.contains(document.getGroupName())){
							idBuffer.append(FtsFieldConst.FIELD_HKEY).append(":(").append(id).append(")");
						}else{
							idBuffer.append(FtsFieldConst.FIELD_SID).append(":(").append(id).append(")");
						}
						searchBuilder.filterByTRSL_NOT(idBuffer.toString());
					}
					searchBuilder.setDatabase(indices);
					searchBuilder.setServer(server);
					// 算相似文章数时 如果值为1 置 0
					//逻辑修改:热度值及相似文章数计算之前先进行站内排重  2019-12-3
					long ftsCount = hybase8SearchService.ftsCount(searchBuilder, false, true,false,type);
					if (ftsCount == 1L) {
						if(SearchPage.ORDINARY_SEARCH.toString().equals(searchPage)){
							asyncDocument.setSimNum(ftsCount);
						}else{
							asyncDocument.setSimNum(0L);
						}
					} else {
						asyncDocument.setSimNum(ftsCount);
					}
				} else {
					asyncDocument.setSimNum(0L);
				}

				// 检验收藏
				asyncDocument.setFavourite(favouritesList.stream().anyMatch(sid -> sid.getSid().equals(id)));

				asyncList.add(asyncDocument);
				AsyncInfo asyncInfo = new AsyncInfo();
				asyncInfo.setAsyncDocument(asyncDocument);
				asyncInfo.setMd5(md5Tag);
				asyncInfo.setDatabase(indices);
				asyncInfo.setGroupName(document.getGroupName());
				asyncInfoList.add(asyncInfo);
			}
			log.info("相似文章数计算-微信："+"async:" + pageId+"完成，数据为："+asyncList.size());
			TimingCachePool.put("async:" + pageId, asyncList);
			RedisFactory.setValueToRedis("async:" + pageId, asyncList);
			if(SearchPage.ORDINARY_SEARCH.toString().equals(searchPage)){
				fixedThreadPool.execute(
						() -> querySiteNameForSimMd5(pageId, trslk, server, type,asyncInfoList));
			}

		} catch (Exception e) {
			log.error("setSimNumAndPicUrl error", e);
		}
	}

	/**
	 * 查询微信列表
	 *
	 * @throws TRSException
	 */
	@Override
	public Object weChatSearch(String specialId, int pageNo, int pageSize, String source, String time, String area,
							   String industry, String emotion, String sort, String keywords,String fuzzyValueScope, String notKeyWords, String keyWordIndex,String type)
			throws TRSException {
		log.warn("专项检测信息列表，微信，  开始调用接口");
		try {
			QueryBuilder builder = new QueryBuilder();
			QueryBuilder countBuilder = new QueryBuilder();// 展示总数的
			builder.setPageNo(pageNo);
			builder.setPageSize(pageSize);
			countBuilder.setDatabase(Const.WECHAT);
			User loginUser = UserUtils.getUser();
			boolean sim = true;
			boolean irSimflagAll = false;
			boolean irSimflag = false;
			boolean weight = false;

			if (StringUtil.isNotEmpty(specialId)) {
				SpecialProject specialProject = specialProjectService.findOne(specialId);
				irSimflag = specialProject.isIrSimflag();
				irSimflagAll = specialProject.isIrSimflagAll();
				builder = specialProject.toNoTimeBuilderWeiXin(pageNo, pageSize);
				countBuilder = specialProject.toNoPagedAndTimeBuilderWeiXin();
				sim = specialProject.isSimilar();
				weight = specialProject.isWeight();
			}
			// 时间
			builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
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
				builder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
			}
			if (!"ALL".equals(emotion)) { // 情感
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			}
			// 结果中搜索
			if (StringUtil.isNotEmpty(keywords) && StringUtil.isNotEmpty(fuzzyValueScope)) {
				String[] split = keywords.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				keywords = splitNode.substring(0, splitNode.length() - 1);
				if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
						|| keywords.endsWith("，")) {
					keywords = keywords.substring(0, keywords.length() - 1);

				}
				StringBuilder fuzzyBuilder  = new StringBuilder();
				String hybaseField = "fullText";
				switch (fuzzyValueScope){
					case "title":
						hybaseField = FtsFieldConst.FIELD_URLTITLE;
						break;
					case "source":
						hybaseField = FtsFieldConst.FIELD_SITENAME;
						break;
					case "author":
						hybaseField = FtsFieldConst.FIELD_AUTHORS;
						break;
				}
				if("fullText".equals(hybaseField)){
					fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}else {
					fuzzyBuilder.append(hybaseField).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}
				builder.filterByTRSL(fuzzyBuilder.toString());
				countBuilder.filterByTRSL(fuzzyBuilder.toString());
				log.info(builder.asTRSL());
			}
			//拼接排除词
			if (StringUtil.isNotEmpty(notKeyWords) ) {
				if("positioCon".equals(keyWordIndex)){
					StringBuilder exbuilder = new StringBuilder();
					exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
							.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
					builder.filterByTRSL(exbuilder.toString());
					countBuilder.filterByTRSL(exbuilder.toString());
					StringBuilder exbuilder2 = new StringBuilder();
					exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
							.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
					builder.filterByTRSL(exbuilder2.toString());
					countBuilder.filterByTRSL(exbuilder2.toString());
				}else {
					StringBuilder exbuilder2 = new StringBuilder();
					exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
							.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
					builder.filterByTRSL(exbuilder2.toString());
					countBuilder.filterByTRSL(exbuilder2.toString());
				}

			}
			log.info(builder.asTRSL());
			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "hot":
					return commonListService.queryPageListForHot(builder,Const.GROUPNAME_WEIXIN,loginUser,type,true);
//					return getHotListWeChat(builder, countBuilder, loginUser,type);
				case "relevance":// 相关性排序
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
				default:
					if (weight) {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
					}
					break;
			}
			log.warn(builder.asTRSL());
			//因微信库出现groupName字段值为空情况  导致专题分析数据统计与信息数据量不一致，故 builder加groupname
			builder.filterField(FtsFieldConst.FIELD_GROUPNAME,"国内微信",Operator.Equal);
			InfoListResult list = commonListService.queryPageList(builder,sim,irSimflag,irSimflagAll,source,type,loginUser,true);
			return list;
//			return getWeChatList(builder, loginUser, sim, irSimflag,irSimflagAll,false,type);
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e);
		}
	}

	@Override
	public Object statusSearch(String specialId, int pageNo, int pageSize, String source, String time, String area,
							   String industry, String emotion, String sort, String keywords,String fuzzyValueScope, String notKeyWords, String keyWordIndex,
							   String forwarPrimary,String type) throws TRSException {
		log.warn("专项检测信息列表，微博，  开始调用接口");
		try {
			QueryBuilder builder = new QueryBuilder();
			builder.setPageNo(pageNo);
			builder.setPageSize(pageSize);
			QueryBuilder countBuilder = new QueryBuilder();
			User loginUser = UserUtils.getUser();
			boolean sim = true;
			boolean irSimflagAll = false;
			boolean irSimflag = false;
			boolean weight = false;
			if (StringUtil.isNotEmpty(specialId)) {
				SpecialProject specialProject = specialProjectService.findOne(specialId);
				irSimflag = specialProject.isIrSimflag();
				irSimflagAll = specialProject.isIrSimflagAll();
				builder = specialProject.toNoTimeBuilderWeiBo(pageNo, pageSize);
				countBuilder = specialProject.toNoPagedAndTimeBuilderWeiBo();
				sim = specialProject.isSimilar();
				weight = specialProject.isWeight();
			}

			// 时间
			builder.filterField(FtsFieldConst.FIELD_CREATED_AT, DateUtil.formatTimeRange(time), Operator.Between);
			countBuilder.filterField(FtsFieldConst.FIELD_CREATED_AT, DateUtil.formatTimeRange(time), Operator.Between);
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
				builder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
			}
			if (!"ALL".equals(emotion)) { // 情感
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			}
			// 转发 / 原发
			String builderTRSL = builder.asTRSL();
			String builderDatabase = builder.getDatabase();
			int builderPageSize = builder.getPageSize();
			long builderPageNo = builder.getPageNo();
			String countTRSL = countBuilder.asTRSL();
			String countBuilerDatabase = countBuilder.getDatabase();
			int countBuilerPageSize = countBuilder.getPageSize();
			long countBuilerPageNo = countBuilder.getPageNo();
			StringBuilder builderTrsl = new StringBuilder(builderTRSL);
			StringBuilder countBuilderTrsl = new StringBuilder(countTRSL);
			if ("primary".equals(forwarPrimary)) {
				// 原发
				builder.filterByTRSL(Const.PRIMARY_WEIBO);
				countBuilder.filterByTRSL(Const.PRIMARY_WEIBO);
			} else if ("forward".equals(forwarPrimary)) {
				// 转发
				builder = new QueryBuilder();
				countBuilder = new QueryBuilder();

				builderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO);
				builder.filterByTRSL(builderTrsl.toString());

				builder.setDatabase(builderDatabase);
				builder.setPageSize(builderPageSize);
				builder.setPageNo(builderPageNo);

				countBuilderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO);
				countBuilder.filterByTRSL(countBuilderTrsl.toString());
				countBuilder.setDatabase(countBuilerDatabase);
				countBuilder.setPageSize(countBuilerPageSize);
				countBuilder.setPageNo(countBuilerPageNo);
			}

			// 结果中搜索
			if (StringUtil.isNotEmpty(keywords) && StringUtil.isNotEmpty(fuzzyValueScope)) {
				String[] split = keywords.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				keywords = splitNode.substring(0, splitNode.length() - 1);
				if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
						|| keywords.endsWith("，")) {
					keywords = keywords.substring(0, keywords.length() - 1);

				}
				StringBuilder fuzzyBuilder  = new StringBuilder();
				String hybaseField = "fullText";
				switch (fuzzyValueScope){
					case "title":
						hybaseField = FtsFieldConst.FIELD_URLTITLE;
						break;
					case "source":
						hybaseField = FtsFieldConst.FIELD_SITENAME;
						break;
					case "author":
						hybaseField = FtsFieldConst.FIELD_AUTHORS;
						break;
				}
				if("fullText".equals(hybaseField)){
					fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}else {
					fuzzyBuilder.append(hybaseField).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}
				builder.filterByTRSL(fuzzyBuilder.toString());
				countBuilder.filterByTRSL(fuzzyBuilder.toString());
				log.info(builder.asTRSL());
			}
			//拼接排除词
			if (StringUtil.isNotEmpty(notKeyWords) ) {
				if("positioCon".equals(keyWordIndex)){
					StringBuilder exbuilder = new StringBuilder();
					exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
							.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
					builder.filterByTRSL(exbuilder.toString());
					countBuilder.filterByTRSL(exbuilder.toString());
					StringBuilder exbuilder2 = new StringBuilder();
					exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
							.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
					builder.filterByTRSL(exbuilder2.toString());
					countBuilder.filterByTRSL(exbuilder2.toString());
				}else {
					StringBuilder exbuilder2 = new StringBuilder();
					exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
							.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
					builder.filterByTRSL(exbuilder2.toString());
					countBuilder.filterByTRSL(exbuilder2.toString());
				}

			}

			log.info(builder.asTRSL());
			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_CREATED_AT, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_CREATED_AT, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
					countBuilder.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
					break;
				case "hot":
					return commonListService.queryPageListForHot(builder,Const.GROUPNAME_WEIBO,loginUser,type,true);
//					return getHotListStatus(builder, countBuilder, loginUser,type);
				case "relevance":// 相关性排序
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
				default:
					if (weight) {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
					}
					break;
			}
			// countBuiler.setDatabase(Const.WEIBO);
			//因微信库出现groupName字段值为空情况  导致专题分析数据统计与信息数据量不一致，防止微博出现上述情况 builder加groupname
			builder.filterField(FtsFieldConst.FIELD_GROUPNAME,"微博",Operator.Equal);
			InfoListResult list = commonListService.queryPageList(builder,sim,irSimflag,irSimflagAll,source,type,loginUser,true);
//			return getStatusList(builder, loginUser, sim,irSimflag,irSimflagAll,false,type);
			return list;

		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e);
		}
	}

	@Override
	public Object documentSearch(String specialId, int pageNo, int pageSize, String source, String time, String area,
								 String industry, String emotion, String sort, String invitationCard, String keywords,String fuzzyValueScope, String notKeyWords,
								 String keyWordIndex, String foreign,String type) throws TRSException {
		log.warn("专项检测信息列表  开始调用接口");
		try {
			QueryBuilder builder = new QueryBuilder();
			builder.setPageNo(pageNo);
			builder.setPageSize(pageSize);
			QueryBuilder countBuilder = new QueryBuilder();
			User loginUser = UserUtils.getUser();
			boolean sim = true;
			boolean irSimflag = false;
			boolean irSimflagAll = false;
			boolean weight = false;

			if (StringUtil.isNotEmpty(specialId)) {
				SpecialProject specialProject = specialProjectService.findOne(specialId);
				irSimflag = specialProject.isIrSimflag();
				irSimflagAll = specialProject.isIrSimflagAll();
				builder = specialProject.toNoTimeBuilder(pageNo, pageSize);
				countBuilder = specialProject.toNoPagedAndTimeBuilder();
				sim = specialProject.isSimilar();
				weight = specialProject.isWeight();
			}
			// 时间
			builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			// 来源
			if (!"ALL".equals(source)) {
				// 单选状态
				if ("国内新闻".equals(source)) {

					String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻").toString();

					builder.filterByTRSL(trsl);
					countBuilder.filterByTRSL(trsl);
				} else if ("国内论坛".equals(source)) {
					StringBuffer sb = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 ");
					if ("0".equals(invitationCard)) {// 主贴
						sb.append(" AND ").append(Const.NRESERVED1_LUNTAN);
					} else if ("1".equals(invitationCard)) {// 回帖
						sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
					}
					builder.filterByTRSL(sb.toString());
					countBuilder.filterByTRSL(sb.toString());
				} else {
					builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
					countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
				}
			}

			if (StringUtils.isNotBlank(area) && !"ALL".equals(area)) { // 地域
				String[] areaSplit = area.split(";");
				String contentArea = "";
				for (int i = 0; i < areaSplit.length; i++) {
					areaSplit[i] = "(中国\\\\" + areaSplit[i] + "*)";
					if (i != areaSplit.length - 1) {
						areaSplit[i] += " OR ";
					}
					contentArea += areaSplit[i];
				}
				builder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
			}

			if (null != foreign && !"".equals(foreign) && !"ALL".equals(foreign)) {
				// 放境外区域
				setForeignData(foreign, builder, countBuilder, null, null);
			}
			if (!"ALL".equals(industry)) { // 行业
				builder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
			}
			// ALL = ((NOT 正面) && (NOT 负面)) + 正面 + 负面
			if (!"ALL".equals(emotion)) { // 情感
				if("中性".equals(emotion)){
					//builder.filterByTRSL("(NOT IR_APPRAISE:(\"正面\")) AND (NOT IR_APPRAISE:(\"负面\"))");
					//builder.setAppendTRSL(new StringBuilder().append("NOT INR_APPRAISE:(\"正面\" OR \"负面\")"));
					countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
					builder.filterField(FtsFieldConst.FIELD_APPRAISE, "(\"正面\" OR \"负面\")", Operator.NotEqual);
				}else {
					builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
					countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
				}
			}

			// 结果中搜索
			if (StringUtil.isNotEmpty(keywords) && StringUtil.isNotEmpty(fuzzyValueScope)) {
				String[] split = keywords.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				keywords = splitNode.substring(0, splitNode.length() - 1);
				if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
						|| keywords.endsWith("，")) {
					keywords = keywords.substring(0, keywords.length() - 1);

				}
				StringBuilder fuzzyBuilder  = new StringBuilder();
				String hybaseField = "fullText";
				switch (fuzzyValueScope){
					case "title":
						hybaseField = FtsFieldConst.FIELD_URLTITLE;
						break;
					case "source":
						hybaseField = FtsFieldConst.FIELD_SITENAME;
						break;
					case "author":
						hybaseField = FtsFieldConst.FIELD_AUTHORS;
						break;
				}
				if("fullText".equals(hybaseField)){
					fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}else {
					fuzzyBuilder.append(hybaseField).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}
				builder.filterByTRSL(fuzzyBuilder.toString());
				countBuilder.filterByTRSL(fuzzyBuilder.toString());
				log.info(builder.asTRSL());
			}
			//拼接排除词
			if (StringUtil.isNotEmpty(notKeyWords) ) {
				if("positioCon".equals(keyWordIndex)){
					StringBuilder exbuilder = new StringBuilder();
					exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
							.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
					builder.filterByTRSL(exbuilder.toString());
					countBuilder.filterByTRSL(exbuilder.toString());
					StringBuilder exbuilder2 = new StringBuilder();
					exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
							.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
					builder.filterByTRSL(exbuilder2.toString());
					countBuilder.filterByTRSL(exbuilder2.toString());
				}else {
					StringBuilder exbuilder2 = new StringBuilder();
					exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
							.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
					builder.filterByTRSL(exbuilder2.toString());
					countBuilder.filterByTRSL(exbuilder2.toString());
				}

			}

			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "hot":
					return commonListService.queryPageListForHot(builder,Const.TYPE_NEWS,loginUser,type,true);
//					return getHotList(builder, countBuilder, loginUser,type);
				case "relevance":// 相关性排序
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
				default:
					if (weight) {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
					}
					break;
			}
			builder.setDatabase(Const.HYBASE_NI_INDEX);
			InfoListResult list = commonListService.queryPageList(builder,sim,irSimflag,irSimflagAll,source,type,loginUser,true);
			return list;
//			return getDocList(builder, loginUser, sim, irSimflag,irSimflagAll,false,type);
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("查询出错："+e, e);
		}
	}

	@Override
	public void simForAlert(String pageId, Page<AlertEntity> alertList, User user, String groupName) {
		String indices = Const.HYBASE_NI_INDEX;
		if (Const.MEDIA_TYPE_WEIXIN.contains(groupName)) {
			indices = Const.WECHAT;
		} else if (Const.MEDIA_TYPE_WEIBO.contains(groupName)) {
			indices = Const.WEIBO;
		}
		List<AsyncDocument> asyncList = new ArrayList<>();

		List<Favourites> favouritesList = favouritesService.findAll(user);
//		List<Favourites> library = favouritesRepository.findByUserId(userId,
//				new Sort(Sort.Direction.DESC, "lastModifiedTime"));
		for (AlertEntity alert : alertList) {
			String id = alert.getSid();
			String md5Tag = alert.getMd5tag();
			AsyncDocument asyncDocument = new AsyncDocument();
			asyncDocument.setId(id);
			if (StringUtil.isNotEmpty(md5Tag)) { // 获取相似文章数
				QueryBuilder searchBuilder = new QueryBuilder();
				searchBuilder.filterField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);
				searchBuilder.setDatabase(indices);
				// 算相似文章数时 如果值为1 置 0
				long ftsCount = hybase8SearchService.ftsCount(searchBuilder, false, true,false,null);
				if (ftsCount == 1L) {
					asyncDocument.setSimNum(0L);
				} else {
					asyncDocument.setSimNum(ftsCount);
				}
			} else {
				asyncDocument.setSimNum(0L);
			}
			// 检验收藏
			asyncDocument.setFavourite(favouritesList.stream().anyMatch(sid -> sid.getSid().equals(id)));
			asyncList.add(asyncDocument);
		}
		TimingCachePool.put("async:" + pageId, asyncList);
		RedisFactory.setValueToRedis("async:" + pageId, asyncList);
	}

	public InfoListResult<IDocument> getDocListContrast(QueryCommonBuilder builder, User user, boolean sim,
														boolean irSimflag, boolean irSimflagAll, String type) throws TRSException{
		String searchPage = SearchPage.COMMON_SEARCH.toString();
		return getDocListContrast(builder, user, sim, irSimflag, irSimflagAll, type,searchPage);
	}

	@Override
	public InfoListResult<IDocument> getDocListContrast(QueryCommonBuilder builder, User user, boolean sim,
														boolean irSimflag, boolean irSimflagAll, String type,String searchPage) throws TRSException {
		// 暂时不用
		List<FtsDocumentCommonVO> ftsList = new ArrayList<>();

		List<String> md5List = new ArrayList<>();
		final String pageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		final String nextPageId = GUIDGenerator.generate(InfoListServiceImpl.class);

		// 根据builder选择相应实体等信息
		try {
			// 把表达式放缓存里边 把key值返回给前端
			// long threadId = Thread.currentThread().getId();
			// String trslk = RedisUtil.PER_KEY + threadId;
			// //如果需要转换成server key就以server结尾 在导出exel时用
			// if(builder.isServer()){
			// trslk = trslk+RedisUtil.SUFFIX_KEY;
			// }
			// RedisUtil.setString(trslk, builder.asTRSL());
			// 在这存个空的 在hybase里边填完整的
			String trslk = pageId + "trslk";
			RedisUtil.setString(trslk, builder.asTRSL());
			builder.setKeyRedis(trslk);
			log.info("正式：" + builder.asTRSL());
			String trslkHot = pageId + "hot";
			RedisUtil.setString(trslkHot, builder.asTRSL());
			long startTime = System.currentTimeMillis();
			PagedList<FtsDocumentCommonVO> pagedList = hybase8SearchService.pageListCommon(builder, sim, irSimflag,irSimflagAll,type);
			long endTime = System.currentTimeMillis();
			log.error("间隔HY时间："+(endTime - startTime));
			List<FtsDocumentCommonVO> list = pagedList.getPageItems();

			// 检查是否预警
			List<String> sids = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			for (FtsDocumentCommonVO ftsDocumentCommonVO : list) {
				if ("微信".equals(ftsDocumentCommonVO.getGroupName()) || "国内微信".equals(ftsDocumentCommonVO.getGroupName())){
					sids.add(ftsDocumentCommonVO.getHkey());

					sb.append(ftsDocumentCommonVO.getHkey()).append(",");
				}else {
					sids.add(ftsDocumentCommonVO.getSid());

					sb.append(ftsDocumentCommonVO.getSid()).append(",");
				}

			}
			// 检验收藏

			List<Favourites> favouritesList = favouritesService.findAll(user);
			//List<Favourites> library = favouritesRepository.findByUserIdAndSidIn(userId, sids);
			// 推荐列表排除自己
			label: {

				while (list.size() > 0) {
					// int index = 0;
					for (FtsDocumentCommonVO document : list) {
						// String content = document.getContent();
						String content= "";
						if (StringUtil.isNotEmpty(document.getContent())) {
							content = StringUtil.replaceImg(document.getContent());
							document.setExportContent(content);
							//content = StringUtil.substringRed(content, Const.CONTENT_LENGTH);
							//document.setContent(content);
							document.setContent(StringUtil.cutContentPro(StringUtil.replaceImg(document.getContent()), Const.CONTENT_LENGTH));
							document.setStatusContent(StringUtil.cutContentPro(StringUtil.replaceImg(document.getContent()), Const.CONTENT_LENGTH));
						}
						// 控制标题长度
						document.setTrslk(trslk);
						if (StringUtil.isNotEmpty(document.getTitle())){
							document.setTitle( StringUtil.replaceAnnotation(document.getTitle()).replace("&amp;nbsp;", ""));
						}
						// 检验预警发送
						//不查预警
						/*int index = 0;
						if ("微信".equals(document.getGroupName()) || "国内微信".equals(document.getGroupName())){
							index = sidAlert.indexOf(document.getHkey());
						}else {
							index = sidAlert.indexOf(document.getSid());
						}
						if (index < 0) {
							document.setSend(false);
						} else {
							document.setSend(true);
						}*/

						if ("国内微信".equals(document.getGroupName())) {
							document.setGroupName("微信");
							document.setSid(document.getHkey());
							// 检验收藏
							document.setFavourite(favouritesList.stream().anyMatch(sid -> sid.getSid().equals(document.getHkey())));
						}else {
							// 检验收藏
							document.setFavourite(favouritesList.stream().anyMatch(sid -> sid.getSid().equals(document.getSid())));
						}
						if ("Twitter".equals(document.getGroupName()) || "FaceBook".equals(document.getGroupName()) || "Facebook".equals(document.getGroupName())) {
							document.setSiteName(document.getGroupName());
							document.setTitle(content);
						}
						md5List.add(document.getMd5Tag());
						ftsList.add(document);

					}

					// 不排重的情况下
					TimingCachePool.put(pageId, new InfoListResult<>(pageId, pagedList, nextPageId, 0, trslk));
					// int finalIndex = index;
					final String trsSim = trslkHot;
					String[] database = builder.getDatabase();
					if(builder.getPageSize()<10000){//大于一万就是导出  不算相似文章数
						fixedThreadPool.execute(() -> calculateSimNumCommon(String.join(";", database), pageId, ftsList,
								user, trsSim, builder.isServer(),type,searchPage));
					}
					break label;
				}
			}
		} catch (Exception e) {
			throw new OperationException("检索异常：", e);
		}
		InfoListResult object = (InfoListResult) TimingCachePool.get(pageId);
		return object;
	}

	private void calculateSimNumCommon(String database, String pageId, final List<FtsDocumentCommonVO> documentList,
									   User user, String trslk, boolean server,String type){
		String searchPage = SearchPage.COMMON_SEARCH.toString();
		calculateSimNumCommon(database, pageId, documentList, user, trslk, server, type,searchPage);
	}
	private void calculateSimNumCommon(String database, String pageId, final List<FtsDocumentCommonVO> documentList,
									   User user, String trslk, boolean server,String type,String searchPage) {
		try {
			log.info("相似文章数计算-全部数据源："+"async:" + pageId+"开始");
			String trsl = RedisUtil.getString(trslk);
			List<AsyncDocument> asyncList = new ArrayList<>();
			List<AsyncInfo> asyncInfoList = new ArrayList<>();
			for (FtsDocumentCommonVO document : documentList) {
				String id = document.getSid();
				String md5Tag = document.getMd5Tag();
				AsyncDocument asyncDocument = new AsyncDocument();
				asyncDocument.setId(id);

				if (StringUtil.isNotEmpty(md5Tag)) { // 获取相似文章数
					QueryBuilder searchBuilder = new QueryBuilder();
					searchBuilder.filterField(FtsFieldConst.FIELD_MD5TAG, md5Tag, Operator.Equal);
					String sourceGro = document.getGroupName();
					if ("微信".equals(sourceGro)){
						sourceGro = "国内微信";
					}else if ("电子报".equals(sourceGro)) {
						sourceGro = "国内新闻_电子报";
					} else if ("论坛".equals(sourceGro)) {
						sourceGro = "国内论坛";
					} else if ("新闻".equals(sourceGro)) {
						sourceGro = "国内新闻";
					} else if ("博客".equals(sourceGro)) {
						sourceGro = "国内博客";
					}
					String trslpro = trsl.replaceAll("AND \\(IR_SIMFLAGALL:\\(\"0\" OR \"\"\\)\\)"," ");
					searchBuilder.filterByTRSL(trslpro);
					// 根据文章的groupName选择对应库检索
					if (StringUtil.isEmpty(database)) {
						searchBuilder.setDatabase(TrslUtil.chooseDatabases(document.getGroupName()));
					} else {
						searchBuilder.setDatabase(database);
					}
					if(StringUtil.isNotEmpty(id) && searchPage.equals(SearchPage.ORDINARY_SEARCH.toString())){
						//id不为空，则去掉当前文章
						StringBuffer idBuffer = new StringBuffer();
						if(Const.MEDIA_TYPE_WEIBO.contains(document.getGroupName())){
							idBuffer.append(FtsFieldConst.FIELD_MID).append(":(").append(id).append(")");
						}else if(Const.MEDIA_TYPE_WEIXIN.contains(document.getGroupName())){
							idBuffer.append(FtsFieldConst.FIELD_HKEY).append(":(").append(id).append(")");
						}else{
							idBuffer.append(FtsFieldConst.FIELD_SID).append(":(").append(id).append(")");
						}
						searchBuilder.filterByTRSL_NOT(idBuffer.toString());
					}
					searchBuilder.setServer(server);
					searchBuilder.setPageSize(1);
					// 算相似文章数时 如果值为1 置 0

					long ftsCount;
					//逻辑修改:热度值及相似文章数计算之前先进行站内排重  2019-12-2
					if (searchBuilder.getDatabase()
							.equals(FtsParser.getDatabases(FtsDocument.class))) {
						ftsCount = hybase8SearchService.ftsCountNoRtt(searchBuilder, FtsDocument.class, false,true, false,type);
					} else if (searchBuilder.getDatabase()
							.equals(FtsParser.getDatabases(FtsDocumentStatus.class))) {
						ftsCount = hybase8SearchService.ftsCountNoRtt(searchBuilder, FtsDocumentStatus.class, false,true,
								false,type);
					} else if (searchBuilder.getDatabase()
							.equals(FtsParser.getDatabases(FtsDocumentWeChat.class))) {
						ftsCount = hybase8SearchService.ftsCountNoRtt(searchBuilder, FtsDocumentWeChat.class, false,true,
								false,type);
					}else if (searchBuilder.getDatabase()
							.equals(FtsParser.getDatabases(FtsDocumentTF.class))) {
						ftsCount = hybase8SearchService.ftsCountNoRtt(searchBuilder, FtsDocumentTF.class, false,true,
								false,type);
					} else {
						ftsCount = hybase8SearchService.ftsCountNoRtt(searchBuilder, FtsDocumentCommonVO.class, false,true,
								false,type);
					}
					RedisUtil.setString(searchBuilder.getKeyRedis(), trsl, 30, TimeUnit.MINUTES);
					if (ftsCount == 1L) {
						if(SearchPage.ORDINARY_SEARCH.toString().equals(searchPage)){
							asyncDocument.setSimNum(ftsCount);
						}else{
							asyncDocument.setSimNum(0L);
						}
					} else {
						asyncDocument.setSimNum(ftsCount);
					}
				} else {
					asyncDocument.setSimNum(0L);
				}
				// 检验收藏
				// asyncDocument.setFavourite(library.stream().anyMatch(sid ->
				// sid.getSid().equals(id)));
				asyncList.add(asyncDocument);
				AsyncInfo asyncInfo = new AsyncInfo();
				asyncInfo.setAsyncDocument(asyncDocument);
				asyncInfo.setMd5(md5Tag);
				asyncInfo.setDatabase(database);
				asyncInfo.setGroupName(document.getGroupName());
				asyncInfoList.add(asyncInfo);
			}
			log.info("相似文章数计算-全部数据源："+"async:" + pageId+"完成，数据为："+asyncList.size());
			TimingCachePool.put("async:" + pageId, asyncList);
			RedisFactory.setValueToRedis("async:" + pageId, asyncList);
			//现在只针对普通搜索页面做了相似文章发文网站的检索
			if(SearchPage.ORDINARY_SEARCH.toString().equals(searchPage)){
				fixedThreadPool.execute(
						() -> querySiteNameForSimMd5(pageId, trslk, server, type,asyncInfoList));
			}

		} catch (Exception e) {
			log.error("setSimNumAndPicUrl error", e);
		}
	}

	@Override
	public Object documentTFSearch(String specialId, int pageNo, int pageSize, String source, String time, String area,
								   String industry, String emotion, String sort, String invitationCard, String keywords, String fuzzyValueScope,String notKeyWords,
								   String keyWordIndex,String type) throws TRSException {
		try {
			QueryBuilder builder = new QueryBuilder();
			builder.setPageNo(pageNo);
			builder.setPageSize(pageSize);
			builder.setDatabase(Const.HYBASE_OVERSEAS);
			QueryBuilder countBuilder = new QueryBuilder();
			User loginUser = UserUtils.getUser();
			boolean sim = false;
			boolean irSimflag = false;
			boolean irSimflagAll = false;
			boolean weight = false;

			if (StringUtil.isNotEmpty(specialId)) {
				SpecialProject specialProject = specialProjectService.findOne(specialId);
				irSimflag = specialProject.isIrSimflag();
				irSimflagAll = specialProject.isIrSimflagAll();
				builder = specialProject.toNoTimeBuilder(pageNo, pageSize);
				countBuilder = specialProject.toNoPagedAndTimeBuilder();
				sim = specialProject.isSimilar();
				weight = specialProject.isWeight();
			}
			// 时间
			builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			// 来源
			if (!"ALL".equals(source)) {
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
			}
			// 无地域、行业、情感数据,忽略
			// 结果中搜索
			if (StringUtil.isNotEmpty(keywords) && StringUtil.isNotEmpty(fuzzyValueScope)) {
				String[] split = keywords.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				keywords = splitNode.substring(0, splitNode.length() - 1);
				if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
						|| keywords.endsWith("，")) {
					keywords = keywords.substring(0, keywords.length() - 1);

				}
				StringBuilder fuzzyBuilder  = new StringBuilder();
				String hybaseField = "fullText";
				switch (fuzzyValueScope){
					case "title":
						hybaseField = FtsFieldConst.FIELD_URLTITLE;
						break;
					case "source":
						hybaseField = FtsFieldConst.FIELD_SITENAME;
						break;
					case "author":
						hybaseField = FtsFieldConst.FIELD_AUTHORS;
						break;
				}
				if("fullText".equals(hybaseField)){
					fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}else {
					fuzzyBuilder.append(hybaseField).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}
				builder.filterByTRSL(fuzzyBuilder.toString());
				countBuilder.filterByTRSL(fuzzyBuilder.toString());
				log.info(builder.asTRSL());
			}
			//拼接排除词
			if (StringUtil.isNotEmpty(notKeyWords) ) {
				if("positioCon".equals(keyWordIndex)){
					StringBuilder exbuilder = new StringBuilder();
					exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
							.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
					builder.filterByTRSL(exbuilder.toString());
					countBuilder.filterByTRSL(exbuilder.toString());
					StringBuilder exbuilder2 = new StringBuilder();
					exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
							.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
					builder.filterByTRSL(exbuilder2.toString());
					countBuilder.filterByTRSL(exbuilder2.toString());
				}else {
					StringBuilder exbuilder2 = new StringBuilder();
					exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
							.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
					builder.filterByTRSL(exbuilder2.toString());
					countBuilder.filterByTRSL(exbuilder2.toString());
				}

			}
			switch (sort) { // 排序
				case "commtCount":// 评论
					builder.orderBy(FtsFieldConst.FIELD_COMMTCOUNT, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_COMMTCOUNT, true);
					break;
				case "rttCount":// 评论
					builder.orderBy(FtsFieldConst.FIELD_RTTCOUNT, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_RTTCOUNT, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "hot":
					return commonListService.queryPageListForHot(builder,Const.GROUPNAME_TWITTER,loginUser,type,true);
//					return getHotList(builder, countBuilder, loginUser,type);
				case "relevance":// 相关性排序
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
				default:
					if (weight) {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
					}
					break;
			}
			// countBuilder.setDatabase(Const.HYBASE_OVERSEAS);
			InfoListResult list = commonListService.queryPageList(builder,sim,irSimflag,irSimflagAll,source,type,loginUser,true);
			return list;
//			return getDocTFList(builder, loginUser, sim,irSimflag,irSimflagAll,type);

		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e.getMessage(), e);
		}
	}

	@Override
	public Object weChatForSearchList(boolean sim, boolean irSimflag,boolean irSimflagAll, int pageNo, int pageSize, String source,String checkedSource,
									  String time, String area, String industry, String emotion, String sort, String keywords, String notKeyWords,
									  String keyWordIndex, boolean weight, String fuzzyValue,String fuzzyValueScope,String fromWebSite,String excludeWeb,String type,String searchPage,String searchType) throws TRSException {
		log.warn("专项检测信息列表，微信，  开始调用接口");
		try {
			QueryBuilder builder = new QueryBuilder();
			QueryBuilder countBuilder = new QueryBuilder();// 展示总数的
			builder.setPageNo(pageNo);
			builder.setPageSize(pageSize);
			countBuilder.setDatabase(Const.WECHAT);
			countBuilder.setPageNo(pageNo);
			countBuilder.setPageSize(pageSize);
			User loginUser = UserUtils.getUser();

			Boolean isFuzzySearch = false;
			if("fuzzy".equals(searchType)){
				//是否是模糊搜索  是
				isFuzzySearch = true;
			}
			String originKeyword = "";
			if ("ordinarySearch".equals(searchPage)) {
				originKeyword = keywords;
				keywords = keywords.replaceAll("\\s+", ",");
			}
			StringBuilder childBuilder = new StringBuilder();
			// 搜索关键词
			if (StringUtil.isNotEmpty(keywords)) {
				String[] split = keywords.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				keywords = splitNode.substring(0, splitNode.length() - 1);
				// 防止全部关键词结尾为;报错
				String replaceAnyKey = "";
				if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
						|| keywords.endsWith("，")) {
					replaceAnyKey = keywords.substring(0, keywords.length() - 1);
					childBuilder.append("((\"").append(
							replaceAnyKey.replaceAll("[,|，]+", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				} else {
					childBuilder.append("((\"")
							.append(keywords.replaceAll("[,|，]+", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				}
			}
			if (childBuilder.length() > 0) {

				if (keyWordIndex.equals("positioCon")) {// 标题 + 正文
					String filterTrsl = FuzzySearchUtil.filterFuzzyTrsl(originKeyword,childBuilder,keywords,keyWordIndex,weight,isFuzzySearch);
					builder.filterByTRSL(filterTrsl);
					countBuilder.filterByTRSL(filterTrsl);
					/*builder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					countBuilder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					if (weight) {
						QueryBuilder weightBuilder = new QueryBuilder();
						QueryBuilder weightCountBuilder = new QueryBuilder();
						weightBuilder.page(pageNo, pageSize);
						weightCountBuilder.page(pageNo, pageSize);
						// weightBuilder.filterByTRSL(builder.asTRSL()+FtsFieldConst.WEIGHT);
						// weightCountBuilder.filterByTRSL(countBuilder.asTRSL()+FtsFieldConst.WEIGHT);
						weightBuilder.filterByTRSL(builder.asTRSL() + FtsFieldConst.WEIGHT + " OR "
								+ "("+FtsFieldConst.FIELD_CONTENT + ":" + childBuilder.toString()+")");
						weightCountBuilder.filterByTRSL(countBuilder.asTRSL() + FtsFieldConst.WEIGHT + " OR "
								+ "("+FtsFieldConst.FIELD_CONTENT + ":" + childBuilder.toString()+")");
						builder = weightBuilder;
						countBuilder = weightCountBuilder;
					} else {
						builder.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(), Operator.Equal);
						countBuilder.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(),
								Operator.Equal);
					}*/
					//拼接排除词
					if (StringUtil.isNotEmpty(notKeyWords)) {
						StringBuilder exbuilder = new StringBuilder();
						exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
						builder.filterByTRSL(exbuilder.toString());
						countBuilder.filterByTRSL(exbuilder.toString());
						StringBuilder exbuilder2 = new StringBuilder();
						exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
						builder.filterByTRSL(exbuilder2.toString());
						countBuilder.filterByTRSL(exbuilder.toString());
					}
				} else if (keyWordIndex.equals("positionKey")) {
					String filterTrsl = FuzzySearchUtil.filterFuzzyTrsl(originKeyword,childBuilder,keywords,keyWordIndex,weight,isFuzzySearch);
					builder.filterByTRSL(filterTrsl);
					countBuilder.filterByTRSL(filterTrsl);
					//拼接排除词
					if (StringUtil.isNotEmpty(notKeyWords)) {
						StringBuilder exbuilder = new StringBuilder();
						exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
						builder.filterByTRSL(exbuilder.toString());
						countBuilder.filterByTRSL(exbuilder.toString());
					}
					// 仅标题
					/*
					builder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					countBuilder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					*/
				}

				// 时间  这个位置不要在加权重之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);

			}else if (StringUtil.isNotEmpty(notKeyWords)){
				// 时间  只有排除词时  时间要在排除词之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				//查询条件只有排除词和时间
				childBuilder.append("(\"").append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				String builderAs = builder.asTRSL();
				String countAs = countBuilder.asTRSL();
				StringBuilder not_builderkeyWord = new StringBuilder();
				StringBuilder not_countkeyWord = new StringBuilder();
				builder = new QueryBuilder();
				builder.page(pageNo,pageSize);
				countBuilder = new QueryBuilder();
				countBuilder.page(pageNo,pageSize);
				if (keyWordIndex.equals("positioCon")) {// 标题 + 正文
					not_builderkeyWord = not_builderkeyWord.append(builderAs).append(" NOT (").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString()).append(") NOT (").append(FtsFieldConst.FIELD_CONTENT).append(":").append(childBuilder.toString()).append(")");
					not_countkeyWord = not_countkeyWord.append(countAs).append(" NOT (").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString()).append(") NOT (").append(FtsFieldConst.FIELD_CONTENT).append(":").append(childBuilder.toString()).append(")");
				}else if (keyWordIndex.equals("positionKey")){
					// 仅标题
					not_builderkeyWord = new StringBuilder(builderAs).append(" NOT ").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString());
					not_countkeyWord = new StringBuilder(countAs).append(" NOT ").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString());
				}
				builder.filterByTRSL(not_builderkeyWord.toString());
				countBuilder.filterByTRSL(not_countkeyWord.toString());
			}else {
				// 时间 只有排除词的情况要加在排除词之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			}

			if(StringUtil.isNotEmpty(source)){
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, Const.GROUPNAME_WEIXIN, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, Const.GROUPNAME_WEIXIN, Operator.Equal);
				if(StringUtil.isNotEmpty(checkedSource) && !checkedSource.equals("ALL")){
					checkedSource = checkedSource .replaceAll("境外媒体",Const.GROUPNAME_GUOWAIXINWEN).replaceAll("境外网站",Const.GROUPNAME_GUOWAIXINWEN);
					if(checkedSource.contains("微信")&& !checkedSource.contains(Const.GROUPNAME_WEIXIN)){
						checkedSource = checkedSource .replaceAll("微信",Const.GROUPNAME_WEIXIN);
					}
					checkedSource = checkedSource.replaceAll(";"," OR ");
					builder.filterField(FtsFieldConst.FIELD_GROUPNAME, checkedSource, Operator.Equal);
					countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, checkedSource, Operator.Equal);
				}
			}
			// 结果中搜索
			if (StringUtil.isNotEmpty(fuzzyValue) && StringUtil.isNotEmpty(fuzzyValueScope)) {//在结果中搜索,范围为全文的时候
				String[] split = fuzzyValue.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				fuzzyValue = splitNode.substring(0, splitNode.length() - 1);
				if (fuzzyValue.endsWith(";") || fuzzyValue.endsWith(",") || fuzzyValue.endsWith("；")
						|| fuzzyValue.endsWith("，")) {
					fuzzyValue = fuzzyValue.substring(0, fuzzyValue.length() - 1);

				}
				StringBuilder fuzzyBuilder  = new StringBuilder();
				String hybaseField = "fullText";
				switch (fuzzyValueScope){
					case "title":
						hybaseField = FtsFieldConst.FIELD_URLTITLE;
						break;
					case "source":
						hybaseField = FtsFieldConst.FIELD_SITENAME;
						break;
					case "author":
						hybaseField = FtsFieldConst.FIELD_AUTHORS;
						break;
				}
				if("fullText".equals(hybaseField)){
					fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}else {
					fuzzyBuilder.append(hybaseField).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}
				builder.filterByTRSL(fuzzyBuilder.toString());
				countBuilder.filterByTRSL(fuzzyBuilder.toString());
			}

			//来源网站
			if (null != fromWebSite && !fromWebSite.isEmpty()){
				addFieldValue(fromWebSite,builder,countBuilder," OR ",FtsFieldConst.FIELD_SITENAME,null,null);
			}
			//排除网站
			if (null != excludeWeb && !excludeWeb.isEmpty()){
				String builderTrsl = builder.asTRSL();
				String countTrsl = countBuilder.asTRSL();
				excludeWeb = excludeWeb.replaceAll("[;|；]"," OR ");

				if (excludeWeb.endsWith(" OR ")) {
					excludeWeb = excludeWeb.substring(0, excludeWeb.length() - 4);
				}

				if (!"".equals(builderTrsl)){
					builderTrsl += new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(excludeWeb)
							.append(")").toString();
					builder = new QueryBuilder();
					builder.page(pageNo,pageSize);
					builder.filterByTRSL(builderTrsl);
				}
				if (!"".equals(countTrsl)){
					countTrsl +=  new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(excludeWeb)
							.append(")").toString();
					countBuilder = new QueryBuilder();
					countBuilder.page(pageNo,pageSize);
					countBuilder.filterByTRSL(countTrsl);
				}
				//addExcloudSite(excludeWeb,builder,countBuilder,null,null);
			}
			//排除网站
            /*if (null != excludeWeb){
                addExcloudSite(excludeWeb,builder,countBuilder,null,null);
            }*/

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
				builder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
			}
			if (!"ALL".equals(emotion)) { // 情感
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			}
			log.info("微信："+builder.asTRSL());
			if("ordinarySearch".equals(searchPage)){
				searchPage = SearchPage.ORDINARY_SEARCH.toString();
			}else{
				searchPage = SearchPage.COMMON_SEARCH.toString();
			}
			builder.filterField(FtsFieldConst.FIELD_GROUPNAME,"国内微信",Operator.Equal);
			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "hot":
					return getHotListWeChat(builder, countBuilder, loginUser,type,searchPage);
				case "relevance":// 相关性排序
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
				default:
					if (isFuzzySearch) {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						if (weight) {
							builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
						} else {
							builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
						}
					}
					break;
			}
			log.warn(builder.asTRSL());
			return getWeChatList(builder, loginUser, sim, irSimflag,irSimflagAll,false,type,searchPage);
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e, e);
		}
	}

	@Override
	public Object statusForSearchList(boolean sim, boolean irSimflag,boolean irSimflagAll, int pageNo, int pageSize, String source,String checkedSource,
									  String time, String area, String industry, String emotion, String sort, String keywords, String notKeyWords,
									  String keyWordIndex, String forwardPrimary,String forwardPrimary1, boolean weight, String fuzzyValue,String fuzzyValueScope,String fromWebSite,String excludeWeb,String type,String searchPage,String searchType) throws TRSException {
		log.warn("专项检测信息列表，微博，  开始调用接口");
		try {
			QueryBuilder builder = new QueryBuilder();
			builder.setPageNo(pageNo);
			builder.setPageSize(pageSize);
			// 是否按照url排重
			// if(irSimflag){
			// builder.filterByTRSL(Const.IR_SIMFLAG_TRSL);
			//// builder.filterField(FtsFieldConst.FIELD_IR_SIMFLAG,"0",Operator.Equal);
			// }
			QueryBuilder countBuiler = new QueryBuilder();
			countBuiler.setPageNo(pageNo);
			countBuiler.setPageSize(pageSize);
			User loginUser = UserUtils.getUser();
			Boolean isFuzzySearch = false;
			if("fuzzy".equals(searchType)){
				//是否是模糊搜索  是
				isFuzzySearch = true;
			}
			String originKeyword = "";
			if ("ordinarySearch".equals(searchPage)) {
				originKeyword = keywords;
				keywords = keywords.replaceAll("\\s+", ",");
			}
			StringBuilder childBuilder = new StringBuilder();
			// 关键词搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String[] split = keywords.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				keywords = splitNode.substring(0, splitNode.length() - 1);
				// 防止全部关键词结尾为;报错
				String replaceAnyKey = "";
				if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
						|| keywords.endsWith("，")) {
					replaceAnyKey = keywords.substring(0, keywords.length() - 1);
					childBuilder.append("((\"").append(
							replaceAnyKey.replaceAll("[,|，]+", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				} else {
					childBuilder.append("((\"")
							.append(keywords.replaceAll("[,|，]+", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				}
			}
			if (childBuilder.length() > 0) {
//				//排除词
//				if (StringUtil.isNotEmpty(notKeyWords)) {
//					childBuilder.append(" NOT (\"").append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
//				}
				if (keyWordIndex.equals("positioCon")) {// 标题 + 正文
					/*builder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					countBuiler.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					if (weight) {
						QueryBuilder weightBuilder = new QueryBuilder();
						QueryBuilder weightCountBuilder = new QueryBuilder();
						weightBuilder.page(pageNo, pageSize);
						weightCountBuilder.page(pageNo, pageSize);
						// weightBuilder.filterByTRSL(builder.asTRSL()+FtsFieldConst.WEIGHT);
						// weightCountBuilder.filterByTRSL(countBuiler.asTRSL()+FtsFieldConst.WEIGHT);
						String asTRSL = builder.asTRSL();
						weightBuilder.filterByTRSL(builder.asTRSL() + FtsFieldConst.WEIGHT + " OR "
								+ "("+FtsFieldConst.FIELD_CONTENT + ":" + childBuilder.toString()+")");
						weightCountBuilder.filterByTRSL(countBuiler.asTRSL() + FtsFieldConst.WEIGHT + " OR "
								+ "("+FtsFieldConst.FIELD_CONTENT + ":" + childBuilder.toString()+")");
						builder = weightBuilder;
						countBuiler = weightCountBuilder;
					} else {
						builder.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(), Operator.Equal);
						countBuiler.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(),
								Operator.Equal);
					}*/
					String filterTrsl = FuzzySearchUtil.filterFuzzyTrsl(originKeyword,childBuilder,keywords,keyWordIndex,weight,isFuzzySearch);
					builder.filterByTRSL(filterTrsl);
					countBuiler.filterByTRSL(filterTrsl);
					//拼接排除词
					if (StringUtil.isNotEmpty(notKeyWords)) {
						StringBuilder exbuilder = new StringBuilder();
						exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
						builder.filterByTRSL(exbuilder.toString());
						countBuiler.filterByTRSL(exbuilder.toString());
						StringBuilder exbuilder2 = new StringBuilder();
						exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
						builder.filterByTRSL(exbuilder2.toString());
						countBuiler.filterByTRSL(exbuilder.toString());
					}
				} else if (keyWordIndex.equals("positionKey")) {
					String filterTrsl = FuzzySearchUtil.filterFuzzyTrsl(originKeyword,childBuilder,keywords,keyWordIndex,weight,isFuzzySearch);
					builder.filterByTRSL(filterTrsl);
					countBuiler.filterByTRSL(filterTrsl);
					//拼接排除词
					if (StringUtil.isNotEmpty(notKeyWords)) {
						StringBuilder exbuilder = new StringBuilder();
						exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
						builder.filterByTRSL(exbuilder.toString());
						countBuiler.filterByTRSL(exbuilder.toString());
					}
					// 仅标题
					/*
					builder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					countBuiler.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					*/
				}


				// 时间  这个位置不要在加权重之前
				builder.filterField(FtsFieldConst.FIELD_CREATED_AT, DateUtil.formatTimeRange(time), Operator.Between);
				countBuiler.filterField(FtsFieldConst.FIELD_CREATED_AT, DateUtil.formatTimeRange(time), Operator.Between);

			}else if (StringUtil.isNotEmpty(notKeyWords)){
				// 时间  只有排除词的时候  要在排除词之前
				builder.filterField(FtsFieldConst.FIELD_CREATED_AT, DateUtil.formatTimeRange(time), Operator.Between);
				countBuiler.filterField(FtsFieldConst.FIELD_CREATED_AT, DateUtil.formatTimeRange(time), Operator.Between);

				//查询条件只有时间和排除词
				childBuilder.append("(\"").append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				String builderAs = builder.asTRSL();
				String countAs = countBuiler.asTRSL();
				StringBuilder not_builderkeyWord = new StringBuilder();
				StringBuilder not_countkeyWord = new StringBuilder();
				builder = new QueryBuilder();
				builder.page(pageNo,pageSize);
				countBuiler = new QueryBuilder();
				countBuiler.page(pageNo,pageSize);
				if (keyWordIndex.equals("positioCon")) {// 标题 + 正文
					not_builderkeyWord = not_builderkeyWord.append(builderAs).append(" NOT (").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString()).append(") NOT (").append(FtsFieldConst.FIELD_CONTENT).append(":").append(childBuilder.toString()).append(")");
					not_countkeyWord = not_countkeyWord.append(countAs).append(" NOT (").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString()).append(") NOT (").append(FtsFieldConst.FIELD_CONTENT).append(":").append(childBuilder.toString()).append(")");
				}else if (keyWordIndex.equals("positionKey")){
					// 仅标题
					not_builderkeyWord = new StringBuilder(builderAs).append(" NOT ").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString());
					not_countkeyWord = new StringBuilder(countAs).append(" NOT ").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString());
				}
				builder.filterByTRSL(not_builderkeyWord.toString());
				countBuiler.filterByTRSL(not_countkeyWord.toString());
			}else {
				// 时间 只有排除词的情况要加在排除词之前
				builder.filterField(FtsFieldConst.FIELD_CREATED_AT, DateUtil.formatTimeRange(time), Operator.Between);
				countBuiler.filterField(FtsFieldConst.FIELD_CREATED_AT, DateUtil.formatTimeRange(time), Operator.Between);
			}

			if(StringUtil.isNotEmpty(source)){
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, Const.GROUPNAME_WEIBO, Operator.Equal);
				countBuiler.filterField(FtsFieldConst.FIELD_GROUPNAME, Const.GROUPNAME_WEIBO, Operator.Equal);
				if(StringUtil.isNotEmpty(checkedSource) && !checkedSource.equals("ALL")){
					checkedSource = checkedSource .replaceAll("境外媒体",Const.GROUPNAME_GUOWAIXINWEN).replaceAll("境外网站",Const.GROUPNAME_GUOWAIXINWEN);
					if(checkedSource.contains("微信")&& !checkedSource.contains(Const.GROUPNAME_WEIXIN)){
						checkedSource = checkedSource .replaceAll("微信",Const.GROUPNAME_WEIXIN);
					}
					checkedSource = checkedSource.replaceAll(";"," OR ");
					builder.filterField(FtsFieldConst.FIELD_GROUPNAME, checkedSource, Operator.Equal);
					countBuiler.filterField(FtsFieldConst.FIELD_GROUPNAME, checkedSource, Operator.Equal);
				}
			}
			// 结果中搜索
			if (StringUtil.isNotEmpty(fuzzyValue) && StringUtil.isNotEmpty(fuzzyValueScope)) {//在结果中搜索,范围为全文的时候
				String[] split = fuzzyValue.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				fuzzyValue = splitNode.substring(0, splitNode.length() - 1);
				if (fuzzyValue.endsWith(";") || fuzzyValue.endsWith(",") || fuzzyValue.endsWith("；")
						|| fuzzyValue.endsWith("，")) {
					fuzzyValue = fuzzyValue.substring(0, fuzzyValue.length() - 1);

				}
				StringBuilder fuzzyBuilder  = new StringBuilder();
				String hybaseField = "fullText";
				switch (fuzzyValueScope){
					case "title":
						hybaseField = FtsFieldConst.FIELD_URLTITLE;
						break;
					case "source":
						hybaseField = FtsFieldConst.FIELD_SITENAME;
						break;
					case "author":
						hybaseField = FtsFieldConst.FIELD_AUTHORS;
						break;
				}
				if("fullText".equals(hybaseField)){
					fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(") OR ("+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND \"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}else {
					fuzzyBuilder.append(hybaseField).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}
				builder.filterByTRSL(fuzzyBuilder.toString());
				countBuiler.filterByTRSL(fuzzyBuilder.toString());
			}
			//来源网站
			if (null != fromWebSite && !fromWebSite.isEmpty()){
				addFieldValue(fromWebSite,builder,countBuiler," OR ",FtsFieldConst.FIELD_SITENAME,null,null);
			}
			//排除网站
			if (null != excludeWeb && !excludeWeb.isEmpty()){
				String builderTrsl = builder.asTRSL();
				String countTrsl = countBuiler.asTRSL();
				excludeWeb = excludeWeb.replaceAll("[;|；]"," OR ");

				if (excludeWeb.endsWith(" OR ")) {
					excludeWeb = excludeWeb.substring(0, excludeWeb.length() - 4);
				}

				if (!"".equals(builderTrsl)){
					builderTrsl += new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(excludeWeb)
							.append(")").toString();
					builder = new QueryBuilder();
					builder.page(pageNo,pageSize);
					builder.filterByTRSL(builderTrsl);
				}
				if (!"".equals(countTrsl)){
					countTrsl +=  new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(excludeWeb)
							.append(")").toString();
					countBuiler = new QueryBuilder();
					countBuiler.page(pageNo,pageSize);
					countBuiler.filterByTRSL(countTrsl);
				}
				//addExcloudSite(excludeWeb,builder,countBuilder,null,null);
			}
			//排除网站
           /* if (null != excludeWeb){
                addExcloudSite(excludeWeb,builder,countBuiler,null,null);
            }*/

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
				builder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
				countBuiler.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
				/*
				 * builder.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA + ":" +
				 * contentArea);
				 * countBuiler.filterByTRSL(FtsFieldConst.FIELD_CATALOG_AREA +
				 * ":" + contentArea);
				 */
			}
			if (!"ALL".equals(emotion)) { // 情感
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
				countBuiler.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			}
			// 转发 / 原发
			String builderTRSL = builder.asTRSL();
			String builderDatabase = builder.getDatabase();
			int builderPageSize = builder.getPageSize();
			long builderPageNo = builder.getPageNo();
			String countTRSL = countBuiler.asTRSL();
			String countBuilerDatabase = countBuiler.getDatabase();
			int countBuilerPageSize = countBuiler.getPageSize();
			long countBuilerPageNo = countBuiler.getPageNo();
			StringBuilder builderTrsl = new StringBuilder(builderTRSL);
			StringBuilder countBuilderTrsl = new StringBuilder(countTRSL);
			if ("primary".equals(forwardPrimary)) {
				// 原发
				//二级筛选
				if("primary".equals(forwardPrimary1)){
					builder.filterByTRSL(Const.PRIMARY_WEIBO+" AND "+Const.PRIMARY_WEIBO);
					countBuiler.filterByTRSL(Const.PRIMARY_WEIBO+" AND "+Const.PRIMARY_WEIBO);
				}else if("forward".equals(forwardPrimary1)){
					builder.filterByTRSL(Const.PRIMARY_WEIBO+" NOT "+Const.PRIMARY_WEIBO);
					countBuiler.filterByTRSL(Const.PRIMARY_WEIBO+" NOT "+Const.PRIMARY_WEIBO);
				}else {
					builder.filterByTRSL(Const.PRIMARY_WEIBO);
					countBuiler.filterByTRSL(Const.PRIMARY_WEIBO);
				}
			} else if ("forward".equals(forwardPrimary)) {
				// 转发
				builder = new QueryBuilder();
				countBuiler = new QueryBuilder();
				//二级筛选
				if("primary".equals(forwardPrimary1)){
					builderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO).append(" AND ").append(Const.PRIMARY_WEIBO);
					countBuilderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO).append(" AND ").append(Const.PRIMARY_WEIBO);
				}else if("forward".equals(forwardPrimary1)){
					builderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO).append(" NOT ").append(Const.PRIMARY_WEIBO);
					countBuilderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO).append(" NOT").append(Const.PRIMARY_WEIBO);
				}else {
					builderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO);
					countBuilderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO);
				}

				builder.filterByTRSL(builderTrsl.toString());

				builder.setDatabase(builderDatabase);
				builder.setPageSize(builderPageSize);
				builder.setPageNo(builderPageNo);


				countBuiler.filterByTRSL(countBuilderTrsl.toString());
				countBuiler.setDatabase(countBuilerDatabase);
				countBuiler.setPageSize(countBuilerPageSize);
				countBuiler.setPageNo(countBuilerPageNo);
			}else {
				if("primary".equals(forwardPrimary1)){
					builder.filterByTRSL(Const.PRIMARY_WEIBO);
					countBuiler.filterByTRSL(Const.PRIMARY_WEIBO);
				}else if("forward".equals(forwardPrimary1)){
					// 转发
					builder = new QueryBuilder();
					countBuiler = new QueryBuilder();
					builderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO);
					countBuilderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO);

					builder.filterByTRSL(builderTrsl.toString());

					builder.setDatabase(builderDatabase);
					builder.setPageSize(builderPageSize);
					builder.setPageNo(builderPageNo);


					countBuiler.filterByTRSL(countBuilderTrsl.toString());
					countBuiler.setDatabase(countBuilerDatabase);
					countBuiler.setPageSize(countBuilerPageSize);
					countBuiler.setPageNo(countBuilerPageNo);
				}
			}
			log.info("微博："+builder.asTRSL());
			if("ordinarySearch".equals(searchPage)){
				searchPage = SearchPage.ORDINARY_SEARCH.toString();
			}else{
				searchPage = SearchPage.COMMON_SEARCH.toString();
			}
			builder.filterField(FtsFieldConst.FIELD_GROUPNAME,"微博",Operator.Equal);
			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_CREATED_AT, true);
					countBuiler.orderBy(FtsFieldConst.FIELD_CREATED_AT, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
					countBuiler.orderBy(FtsFieldConst.FIELD_CREATED_AT, false);
					break;
				case "hot":
					return getHotListStatus(builder, countBuiler, loginUser,type,searchPage);
				case "relevance":// 相关性排序
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
				default:
					if (isFuzzySearch) {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						if (weight) {
							builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
						} else {
							builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
						}
					}
					break;
			}
			return getStatusList(builder, loginUser, sim, irSimflag,irSimflagAll,false,type,searchPage);
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e, e);
		}
	}

	@Override
	public Object documentForSearchList(boolean sim, boolean irSimflag,boolean irSimflagAll, int pageNo, int pageSize, String source,String checkedSource,
										String time, String area, String mediaIndustry, String emotion, String sort, String invitationCard,String invitationCard1,
										String keywords, String notKeyWords, String keyWordIndex, boolean weight, String fuzzyValue,String fuzzyValueScope,
										String fromWebSite,String excludeWeb,String newsInformation,String reprintPortal,String siteType,String type,String searchPage,String searchType)
			throws TRSException {
		log.warn("专项检测信息列表  开始调用接口");
		try {
			QueryBuilder builder = new QueryBuilder();
			builder.setPageNo(pageNo);
			builder.setPageSize(pageSize);
			QueryBuilder countBuilder = new QueryBuilder();
			countBuilder.setPageNo(pageNo);
			countBuilder.setPageSize(pageSize);
			User loginUser = UserUtils.getUser();

			Boolean isFuzzySearch = false;
			if("fuzzy".equals(searchType)){
				//是否是模糊搜索  是
				isFuzzySearch = true;
			}
			String originKeyword = "";
			if ("ordinarySearch".equals(searchPage)) {
				originKeyword = keywords;
				keywords = keywords.replaceAll("\\s+", ",");
			}
			StringBuilder childBuilder = new StringBuilder();
			// 关键词中搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String[] split = keywords.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				keywords = splitNode.substring(0, splitNode.length() - 1);
				// 防止全部关键词结尾为;报错
				String replaceAnyKey = "";
				if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
						|| keywords.endsWith("，")) {
					replaceAnyKey = keywords.substring(0, keywords.length() - 1);
					childBuilder.append("((\"").append(
							replaceAnyKey.replaceAll("[,|，]+", "\")  AND  (\"")
									.replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				} else {
					childBuilder.append("((\"")
							.append(keywords.replaceAll("[,|，]+", "\")  AND  (\"")
									.replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				}
			}
			if (childBuilder.length() > 0) {
				//排除词
//				if (StringUtil.isNotEmpty(notKeyWords)) {
//					childBuilder.append(" NOT (\"").append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
//				}
				if (keyWordIndex.equals("positioCon")) {// 标题 + 正文
					/*builder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					countBuilder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					if (weight) {
						QueryBuilder weightBuilder = new QueryBuilder();
						weightBuilder.page(pageNo, pageSize);
						QueryBuilder weightCountBuilder = new QueryBuilder();
						weightCountBuilder.page(pageNo, pageSize);
						// weightBuilder.filterByTRSL(builder.asTRSL()+FtsFieldConst.WEIGHT);
						// weightCountBuilder.filterByTRSL(countBuilder.asTRSL()+FtsFieldConst.WEIGHT);
						weightBuilder.filterByTRSL(builder.asTRSL() + FtsFieldConst.WEIGHT + " OR "
								+"("+ FtsFieldConst.FIELD_CONTENT + ":" + childBuilder.toString()+")");
						weightCountBuilder.filterByTRSL(countBuilder.asTRSL() + FtsFieldConst.WEIGHT + " OR "
								+ "("+FtsFieldConst.FIELD_CONTENT + ":" + childBuilder.toString()+")");
						builder = weightBuilder;
						countBuilder = weightCountBuilder;
					} else {
						builder.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(), Operator.Equal);
						countBuilder.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(),
								Operator.Equal);
					}*/
					String filterTrsl = FuzzySearchUtil.filterFuzzyTrsl(originKeyword,childBuilder,keywords,keyWordIndex,weight,isFuzzySearch);
					builder.filterByTRSL(filterTrsl);
					countBuilder.filterByTRSL(filterTrsl);
					//拼接排除词
					if (StringUtil.isNotEmpty(notKeyWords)) {
						StringBuilder exbuilder = new StringBuilder();
						exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
						builder.filterByTRSL(exbuilder.toString());
						countBuilder.filterByTRSL(exbuilder.toString());
						StringBuilder exbuilder2 = new StringBuilder();
						exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
						builder.filterByTRSL(exbuilder2.toString());
						countBuilder.filterByTRSL(exbuilder.toString());
					}
				} else if (keyWordIndex.equals("positionKey")) {
					String filterTrsl = FuzzySearchUtil.filterFuzzyTrsl(originKeyword,childBuilder,keywords,keyWordIndex,weight,isFuzzySearch);
					builder.filterByTRSL(filterTrsl);
					countBuilder.filterByTRSL(filterTrsl);
					//拼接排除词
					if (StringUtil.isNotEmpty(notKeyWords)) {
						StringBuilder exbuilder = new StringBuilder();
						exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
						builder.filterByTRSL(exbuilder.toString());
						countBuilder.filterByTRSL(exbuilder.toString());
					}
					// 仅标题
					/*
					builder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					countBuilder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					*/
				}

				// 时间  这个位置不要在加权重之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			}else if (StringUtil.isNotEmpty(notKeyWords)){

				// 时间  只有排除词的时候 时间要在排除词之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);

				//查询条件只有时间和排除词
				childBuilder.append("(\"").append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				String builderAs = builder.asTRSL();
				String countAs = countBuilder.asTRSL();
				StringBuilder not_builderkeyWord = new StringBuilder();
				StringBuilder not_countkeyWord = new StringBuilder();
				builder = new QueryBuilder();
				builder.page(pageNo,pageSize);
				countBuilder = new QueryBuilder();
				countBuilder.page(pageNo,pageSize);
				if (keyWordIndex.equals("positioCon")) {// 标题 + 正文
					not_builderkeyWord = not_builderkeyWord.append(builderAs).append(" NOT (").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString()).append(") NOT (").append(FtsFieldConst.FIELD_CONTENT).append(":").append(childBuilder.toString()).append(")");
					not_countkeyWord = not_countkeyWord.append(countAs).append(" NOT (").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString()).append(") NOT (").append(FtsFieldConst.FIELD_CONTENT).append(":").append(childBuilder.toString()).append(")");
				}else if (keyWordIndex.equals("positionKey")){
					// 仅标题
					not_builderkeyWord = new StringBuilder(builderAs).append(" NOT ").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString());
					not_countkeyWord = new StringBuilder(countAs).append(" NOT ").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString());
				}
				builder.filterByTRSL(not_builderkeyWord.toString());
				countBuilder.filterByTRSL(not_countkeyWord.toString());
			}else {
				// 时间 只有排除词的情况要加在排除词之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			}


			// 来源
			if (StringUtils.isNotBlank(source)) {
				source = source.equals("境外媒体") ? "国外新闻" : source;
				source = source.equals("境外网站") ? "国外新闻" : source;
			}
			if (!"ALL".equals(source)) {

				if ("国内新闻".equals(source)) {
					String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻").toString();
					builder.filterByTRSL(trsl);
					countBuilder.filterByTRSL(trsl);
				} else if (source.contains("国内论坛")) {
					StringBuffer sb = new StringBuffer("("+FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 ");
					if ("0".equals(invitationCard)) {// 主贴
						if ("0".equals(invitationCard1)) {// 主贴
							sb.append(" AND ").append(Const.NRESERVED1_LUNTAN).append(" AND ").append(Const.NRESERVED1_LUNTAN);
						} else if ("1".equals(invitationCard1)) {// 回帖
							sb.append(" AND ").append(Const.NRESERVED1_LUNTAN).append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
						}else {
							sb.append(" AND ").append(Const.NRESERVED1_LUNTAN);
						}
					} else if ("1".equals(invitationCard)) {// 回帖
						if ("0".equals(invitationCard1)) {// 主贴
							sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1").append(" AND ").append(Const.NRESERVED1_LUNTAN);
						} else if ("1".equals(invitationCard1)) {// 回帖
							sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1").append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
						}else {
							sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
						}
					}else {
						if ("0".equals(invitationCard1)) {// 主贴
							sb.append(" AND ").append(Const.NRESERVED1_LUNTAN);
						} else if ("1".equals(invitationCard1)) {// 回帖
							sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
						}
					}
					sb.append(")");
					builder.filterByTRSL(sb.toString());
					countBuilder.filterByTRSL(sb.toString());
				} else {
					builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
					countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
				}
				if(StringUtil.isNotEmpty(checkedSource) && !checkedSource.equals("ALL")){
					checkedSource = checkedSource .replaceAll("境外媒体",Const.GROUPNAME_GUOWAIXINWEN).replaceAll("境外网站",Const.GROUPNAME_GUOWAIXINWEN);
					if(checkedSource.contains("微信")&& !checkedSource.contains(Const.GROUPNAME_WEIXIN)){
						checkedSource = checkedSource .replaceAll("微信",Const.GROUPNAME_WEIXIN);
					}
					checkedSource = checkedSource.replaceAll(";"," OR ");
					builder.filterField(FtsFieldConst.FIELD_GROUPNAME, checkedSource, Operator.Equal);
					countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, checkedSource, Operator.Equal);
				}
			}
			// 结果中搜索
			if (StringUtil.isNotEmpty(fuzzyValue) && StringUtil.isNotEmpty(fuzzyValueScope)) {//在结果中搜索,范围为全文的时候
				String[] split = fuzzyValue.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				fuzzyValue = splitNode.substring(0, splitNode.length() - 1);
				if (fuzzyValue.endsWith(";") || fuzzyValue.endsWith(",") || fuzzyValue.endsWith("；")
						|| fuzzyValue.endsWith("，")) {
					fuzzyValue = fuzzyValue.substring(0, fuzzyValue.length() - 1);

				}
				StringBuilder fuzzyBuilder  = new StringBuilder();
				String hybaseField = "fullText";
				switch (fuzzyValueScope){
					case "title":
						hybaseField = FtsFieldConst.FIELD_URLTITLE;
						break;
					case "source":
						hybaseField = FtsFieldConst.FIELD_SITENAME;
						break;
					case "author":
						hybaseField = FtsFieldConst.FIELD_AUTHORS;
						break;
				}
				if("fullText".equals(hybaseField)){
					fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND \"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}else {
					fuzzyBuilder.append(hybaseField).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}
				builder.filterByTRSL(fuzzyBuilder.toString());
				countBuilder.filterByTRSL(fuzzyBuilder.toString());
			}
			//来源网站
			if (null != fromWebSite && !fromWebSite.isEmpty()){
				addFieldValue(fromWebSite,builder,countBuilder," OR ",FtsFieldConst.FIELD_SITENAME,null,null);
			}
			//排除网站
			if (null != excludeWeb && !excludeWeb.isEmpty()){
				String builderTrsl = builder.asTRSL();
				String countTrsl = countBuilder.asTRSL();
				excludeWeb = excludeWeb.replaceAll("[;|；]"," OR ");

				if (excludeWeb.endsWith(" OR ")) {
					excludeWeb = excludeWeb.substring(0, excludeWeb.length() - 4);
				}

				if (!"".equals(builderTrsl)){
					builderTrsl += new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(excludeWeb)
							.append(")").toString();
					builder = new QueryBuilder();
					builder.page(pageNo,pageSize);
					builder.filterByTRSL(builderTrsl);
				}
				if (!"".equals(countTrsl)){
					countTrsl +=  new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(excludeWeb)
							.append(")").toString();
					countBuilder = new QueryBuilder();
					countBuilder.page(pageNo,pageSize);
					countBuilder.filterByTRSL(countTrsl);
				}
				//addExcloudSite(excludeWeb,builder,countBuilder,null,null);
			}
			//新闻信息资质
			if (!"ALL".equals(newsInformation)){
				addFieldValue(newsInformation,builder,countBuilder," OR ",FtsFieldConst.FIELD_WXB_GRADE,null,null);
			}

			//网站类型
			if (!"ALL".equals(siteType) ){
				addFieldValue(siteType,builder,countBuilder," OR ",FtsFieldConst.FIELD_SITE_PROPERTY,null,null);
			}

			//新闻可供转载网站/门户类型
			if (!"ALL".equals(reprintPortal)){
				addFieldValue(reprintPortal,builder,countBuilder," OR ",FtsFieldConst.FIELD_SITE_APTUTIDE,null,null);
			}
			// 媒体行业
			if (!"ALL".equals(mediaIndustry)) {

				addFieldValue(mediaIndustry,builder,countBuilder,"* OR ",FtsFieldConst.FIELD_CHANNEL_INDUSTRY,null,null);
			}


			if (!"ALL".equals(area)) { // 地域
				String[] areaSplit = area.split(";");
				String contentArea = "";
				for (int i = 0; i < areaSplit.length; i++) {
					areaSplit[i] = "(中国\\\\" + areaSplit[i] + "*)";
					if (i != areaSplit.length - 1) {
						areaSplit[i] += " OR ";
					}
					contentArea += areaSplit[i];
				}
				builder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
			}

			if (!"ALL".equals(emotion)) { // 情感
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			}

			log.info(builder.asTRSL());
			if("ordinarySearch".equals(searchPage)){
				searchPage = SearchPage.ORDINARY_SEARCH.toString();
			}else{
				searchPage = SearchPage.COMMON_SEARCH.toString();
			}
			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "hot":
					return getHotList(builder, countBuilder, loginUser,type,searchPage);
				case "relevance":// 相关性排序
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
				default:
					if (isFuzzySearch) {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						if (weight) {
							builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
						} else {
							builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
						}
					}
					break;
			}
			log.info("传统："+builder.asTRSL());
			return getDocList(builder, loginUser, sim, irSimflag,irSimflagAll,false,type,searchPage);
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e, e);
		}
	}

	@Override
	public Object documentTFForSearchList(boolean sim, boolean irSimflag,boolean irSimflagAll, int pageNo, int pageSize, String source,String checkedSource,
										  String time, String area, String mediaIndustry, String emotion, String sort,
										  String keywords, String notKeyWords, String keyWordIndex, boolean weight, String fuzzyValue,String fuzzyValueScope,
										  String fromWebSite,String excludeWeb,String newsInformation,String reprintPortal,String siteType,String type,String searchPage,String searchType)
			throws TRSException {
		log.warn("专项检测信息列表  开始调用接口");
		try {
			QueryBuilder builder = new QueryBuilder();
			builder.setPageNo(pageNo);
			builder.setPageSize(pageSize);
			builder.setDatabase(Const.HYBASE_OVERSEAS);
			QueryBuilder countBuilder = new QueryBuilder();
			countBuilder.setPageNo(pageNo);
			countBuilder.setPageSize(pageSize);
			User loginUser = UserUtils.getUser();

			Boolean isFuzzySearch = false;
			if("fuzzy".equals(searchType)){
				//是否是模糊搜索  是
				isFuzzySearch = true;
			}
			String originKeyword = "";
			if ("ordinarySearch".equals(searchPage)) {
				originKeyword = keywords;
				keywords = keywords.replaceAll("\\s+", ",");
			}
			StringBuilder childBuilder = new StringBuilder();
			// 关键词中搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String[] split = keywords.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				keywords = splitNode.substring(0, splitNode.length() - 1);
				// 防止全部关键词结尾为;报错
				String replaceAnyKey = "";
				if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
						|| keywords.endsWith("，")) {
					replaceAnyKey = keywords.substring(0, keywords.length() - 1);
					childBuilder.append("((\"").append(
							replaceAnyKey.replaceAll("[,|，]+", "\")  AND  (\"").replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				} else {
					childBuilder.append("((\"")
							.append(keywords.replaceAll("[,|，]+", "\")  AND  (\"").replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				}
			}
			if (childBuilder.length() > 0) {

				if (keyWordIndex.equals("positioCon")) {// 标题 + 正文
					/*builder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					countBuilder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					if (weight) {
						QueryBuilder weightBuilder = new QueryBuilder();
						weightBuilder.page(pageNo, pageSize);
						QueryBuilder weightCountBuilder = new QueryBuilder();
						weightCountBuilder.page(pageNo, pageSize);
						weightBuilder.filterByTRSL(builder.asTRSL() + FtsFieldConst.WEIGHT + " OR "
								+"("+ FtsFieldConst.FIELD_CONTENT + ":" + childBuilder.toString()+")");
						weightCountBuilder.filterByTRSL(countBuilder.asTRSL() + FtsFieldConst.WEIGHT + " OR "
								+ "("+FtsFieldConst.FIELD_CONTENT + ":" + childBuilder.toString()+")");
						builder = weightBuilder;
						countBuilder = weightCountBuilder;
					} else {
						builder.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(), Operator.Equal);
						countBuilder.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(),
								Operator.Equal);
					}*/
					String filterTrsl = FuzzySearchUtil.filterFuzzyTrsl(originKeyword,childBuilder,keywords,keyWordIndex,weight,isFuzzySearch);
					builder.filterByTRSL(filterTrsl);
					countBuilder.filterByTRSL(filterTrsl);
					//拼接排除词
					if (StringUtil.isNotEmpty(notKeyWords)) {
						StringBuilder exbuilder = new StringBuilder();
						exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
						builder.filterByTRSL(exbuilder.toString());
						countBuilder.filterByTRSL(exbuilder.toString());
						StringBuilder exbuilder2 = new StringBuilder();
						exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
						builder.filterByTRSL(exbuilder2.toString());
						countBuilder.filterByTRSL(exbuilder.toString());
					}
				} else if (keyWordIndex.equals("positionKey")) {
					String filterTrsl = FuzzySearchUtil.filterFuzzyTrsl(originKeyword,childBuilder,keywords,keyWordIndex,weight,isFuzzySearch);
					builder.filterByTRSL(filterTrsl);
					countBuilder.filterByTRSL(filterTrsl);
					//拼接排除词
					if (StringUtil.isNotEmpty(notKeyWords)) {
						StringBuilder exbuilder = new StringBuilder();
						exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
						builder.filterByTRSL(exbuilder.toString());
						countBuilder.filterByTRSL(exbuilder.toString());
					}
					// 仅标题
					/*
					builder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					countBuilder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					*/
				}

				// 时间  这个位置不要在加权重之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			}else if (StringUtil.isNotEmpty(notKeyWords)){

				// 时间  只有排除词的时候 时间要在排除词之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);

				//查询条件只有时间和排除词
				childBuilder.append("(\"").append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				String builderAs = builder.asTRSL();
				String countAs = countBuilder.asTRSL();
				StringBuilder not_builderkeyWord = new StringBuilder();
				StringBuilder not_countkeyWord = new StringBuilder();
				builder = new QueryBuilder();
				builder.page(pageNo,pageSize);
				countBuilder = new QueryBuilder();
				countBuilder.page(pageNo,pageSize);
				if (keyWordIndex.equals("positioCon")) {// 标题 + 正文
					not_builderkeyWord = not_builderkeyWord.append(builderAs).append(" NOT (").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString()).append(") NOT (").append(FtsFieldConst.FIELD_CONTENT).append(":").append(childBuilder.toString()).append(")");
					not_countkeyWord = not_countkeyWord.append(countAs).append(" NOT (").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString()).append(") NOT (").append(FtsFieldConst.FIELD_CONTENT).append(":").append(childBuilder.toString()).append(")");
				}else if (keyWordIndex.equals("positionKey")){
					// 仅标题
					not_builderkeyWord = new StringBuilder(builderAs).append(" NOT ").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString());
					not_countkeyWord = new StringBuilder(countAs).append(" NOT ").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString());
				}
				builder.filterByTRSL(not_builderkeyWord.toString());
				countBuilder.filterByTRSL(not_countkeyWord.toString());
			}else {
				// 时间 只有排除词的情况要加在排除词之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			}


			// 来源
			if (StringUtils.isNotBlank(source)) {
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
				if(StringUtil.isNotEmpty(checkedSource) && !checkedSource.equals("ALL")){


					checkedSource = checkedSource .replaceAll("境外媒体","国外新闻").replaceAll("境外网站","国外新闻");
					if(checkedSource.contains("微信")&& !checkedSource.contains("国内微信")){
						checkedSource = checkedSource .replaceAll("微信","国内微信");
					}
					checkedSource = checkedSource.replaceAll(";"," OR ");
					builder.filterField(FtsFieldConst.FIELD_GROUPNAME, checkedSource, Operator.Equal);
					countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, checkedSource, Operator.Equal);
				}
			}
			// 结果中搜索
			if (StringUtil.isNotEmpty(fuzzyValue) && StringUtil.isNotEmpty(fuzzyValueScope)) {//在结果中搜索,范围为全文的时候
				String[] split = fuzzyValue.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				fuzzyValue = splitNode.substring(0, splitNode.length() - 1);
				if (fuzzyValue.endsWith(";") || fuzzyValue.endsWith(",") || fuzzyValue.endsWith("；")
						|| fuzzyValue.endsWith("，")) {
					fuzzyValue = fuzzyValue.substring(0, fuzzyValue.length() - 1);

				}
				StringBuilder fuzzyBuilder  = new StringBuilder();
				String hybaseField = "fullText";
				switch (fuzzyValueScope){
					case "title":
						hybaseField = FtsFieldConst.FIELD_URLTITLE;
						break;
					case "source":
						hybaseField = FtsFieldConst.FIELD_SITENAME;
						break;
					case "author":
						hybaseField = FtsFieldConst.FIELD_AUTHORS;
						break;
				}
				if("fullText".equals(hybaseField)){
					fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND \"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}else {
					fuzzyBuilder.append(hybaseField).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}
				builder.filterByTRSL(fuzzyBuilder.toString());
				countBuilder.filterByTRSL(fuzzyBuilder.toString());
			}
			//来源网站
			if (null != fromWebSite && !fromWebSite.isEmpty()){
				addFieldValue(fromWebSite,builder,countBuilder," OR ",FtsFieldConst.FIELD_SITENAME,null,null);
			}
			//排除网站
			if (null != excludeWeb && !excludeWeb.isEmpty()){
				String builderTrsl = builder.asTRSL();
				String countTrsl = countBuilder.asTRSL();
				excludeWeb = excludeWeb.replaceAll("[;|；]"," OR ");

				if (excludeWeb.endsWith(" OR ")) {
					excludeWeb = excludeWeb.substring(0, excludeWeb.length() - 4);
				}

				if (!"".equals(builderTrsl)){
					builderTrsl += new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(excludeWeb)
							.append(")").toString();
					builder = new QueryBuilder();
					builder.page(pageNo,pageSize);
					builder.filterByTRSL(builderTrsl);
				}
				if (!"".equals(countTrsl)){
					countTrsl +=  new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(excludeWeb)
							.append(")").toString();
					countBuilder = new QueryBuilder();
					countBuilder.page(pageNo,pageSize);
					countBuilder.filterByTRSL(countTrsl);
				}
				//addExcloudSite(excludeWeb,builder,countBuilder,null,null);
			}
			//新闻信息资质
			if (!"ALL".equals(newsInformation)){
				addFieldValue(newsInformation,builder,countBuilder," OR ",FtsFieldConst.FIELD_WXB_GRADE,null,null);
			}

			//网站类型
			if (!"ALL".equals(siteType) ){
				addFieldValue(siteType,builder,countBuilder," OR ",FtsFieldConst.FIELD_SITE_PROPERTY,null,null);
			}

			//新闻可供转载网站/门户类型
			if (!"ALL".equals(reprintPortal)){
				addFieldValue(reprintPortal,builder,countBuilder," OR ",FtsFieldConst.FIELD_SITE_APTUTIDE,null,null);
			}
			// 媒体行业
			if (!"ALL".equals(mediaIndustry)) {

				addFieldValue(mediaIndustry,builder,countBuilder,"* OR ",FtsFieldConst.FIELD_CHANNEL_INDUSTRY,null,null);
			}


			if (!"ALL".equals(area)) { // 地域
				String[] areaSplit = area.split(";");
				String contentArea = "";
				for (int i = 0; i < areaSplit.length; i++) {
					areaSplit[i] = "(中国\\\\" + areaSplit[i] + "*)";
					if (i != areaSplit.length - 1) {
						areaSplit[i] += " OR ";
					}
					contentArea += areaSplit[i];
				}
				builder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
			}

			if (!"ALL".equals(emotion)) { // 情感
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			}

			log.info(builder.asTRSL());
			if("ordinarySearch".equals(searchPage)){
				searchPage = SearchPage.ORDINARY_SEARCH.toString();
			}else{
				searchPage = SearchPage.COMMON_SEARCH.toString();
			}
			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "hot":
					builder.setDatabase(Const.HYBASE_OVERSEAS);
					return getHotList(builder, countBuilder, loginUser,type,searchPage);
				case "relevance":// 相关性排序
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
				default:
					if (isFuzzySearch) {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						if (weight) {
							builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
						} else {
							builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
						}
					}
					break;
			}
			log.info("TF："+builder.asTRSL());
			return getDocTFList(builder, loginUser, sim,irSimflag,irSimflagAll,type,searchPage);
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e, e);
		}
	}

	@Override
	public Object documentCommonVOForSearchList(boolean sim, boolean irSimflag,boolean irSimflagAll, int pageNo, int pageSize, String source, String time, String area, String mediaIndustry,
												String emotion, String sort, String invitationCard,String invitationCard1, String forwarPrimary,String forwarPrimary1,String keywords, String notKeyWords, String keyWordIndex, boolean weight, String fuzzyValue,String fuzzyValueScope, String fromWebSite,
												String excludeWeb, String newsInformation, String reprintPortal, String siteType,boolean isExport,String type,String keyName,String searchPage,String searchType) throws TRSException {

		try {
			QueryCommonBuilder builder = new QueryCommonBuilder();
			builder.setDatabase(Const.MIX_DATABASE.split(";"));
			builder.setPageNo(pageNo);
			builder.setPageSize(pageSize);
			QueryCommonBuilder countBuilder = new QueryCommonBuilder();
			countBuilder.setDatabase(Const.MIX_DATABASE.split(";"));
			countBuilder.setPageNo(pageNo);
			countBuilder.setPageSize(pageSize);
			User loginUser = UserUtils.getUser();

			StringBuilder childBuilder = new StringBuilder();
			// 关键词中搜索
			Boolean isFuzzySearch = false;
			if("fuzzy".equals(searchType)){
				//是否是模糊搜索  是
				isFuzzySearch = true;
			}
			String originKeyword = "";
			if ("ordinarySearch".equals(searchPage)) {
				originKeyword = keywords;
				keywords = keywords.replaceAll("\\s+", ",");
			}
			if (StringUtil.isNotEmpty(keywords)) {
				String[] split = keywords.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				keywords = splitNode.substring(0, splitNode.length() - 1);
				// 防止全部关键词结尾为;报错
				String replaceAnyKey = "";
				if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
						|| keywords.endsWith("，")) {
					replaceAnyKey = keywords.substring(0, keywords.length() - 1);
					childBuilder.append("((\"").append(
							replaceAnyKey.replaceAll("[,|，]+", "\" ) AND ( \"")
									.replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				} else {
					childBuilder.append("((\"")
							.append(keywords.replaceAll("[,|，]+", "\" ) AND ( \"")
									.replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				}
			}
			if (childBuilder.length() > 0) {
//				if (StringUtil.isNotEmpty(notKeyWords)){//排除词
//					childBuilder.append(" NOT (\"").append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
//				}
				if (keyWordIndex.equals("positioCon")) {// 标题 + 正文
					String filterTrsl = FuzzySearchUtil.filterFuzzyTrsl(originKeyword,childBuilder,keywords,keyWordIndex,weight,isFuzzySearch);
					builder.filterByTRSL(filterTrsl);
					countBuilder.filterByTRSL(filterTrsl);
					/*builder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					countBuilder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					if (weight) {
						QueryCommonBuilder weightBuilder = new QueryCommonBuilder();
						weightBuilder.page(pageNo, pageSize);
						QueryCommonBuilder weightCountBuilder = new QueryCommonBuilder();
						weightCountBuilder.page(pageNo, pageSize);
						weightBuilder.filterByTRSL(builder.asTRSL() + FtsFieldConst.WEIGHT + " OR "
								+ FtsFieldConst.FIELD_CONTENT + ":" + childBuilder.toString());
						weightCountBuilder.filterByTRSL(countBuilder.asTRSL() + FtsFieldConst.WEIGHT + " OR "
								+ FtsFieldConst.FIELD_CONTENT + ":" + childBuilder.toString());
						builder = weightBuilder;
						builder.setDatabase(Const.MIX_DATABASE.split(";"));
						countBuilder = weightCountBuilder;
						countBuilder.setDatabase(Const.MIX_DATABASE.split(";"));
					} else {
						builder.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(), Operator.Equal);
						countBuilder.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(),
								Operator.Equal);
					}
					*/
					//拼接排除词
					if (StringUtil.isNotEmpty(notKeyWords)) {
						StringBuilder exbuilder = new StringBuilder();
						exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
						builder.filterByTRSL(exbuilder.toString());
						countBuilder.filterByTRSL(exbuilder.toString());
						StringBuilder exbuilder2 = new StringBuilder();
						exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
						builder.filterByTRSL(exbuilder2.toString());
						countBuilder.filterByTRSL(exbuilder.toString());
					}
				} else if (keyWordIndex.equals("positionKey")) {
					String filterTrsl = FuzzySearchUtil.filterFuzzyTrsl(originKeyword,childBuilder,keywords,keyWordIndex,weight,isFuzzySearch);
					// 仅标题
					//builder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					//countBuilder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					builder.filterByTRSL(filterTrsl);
					countBuilder.filterByTRSL(filterTrsl);
					//拼接排除词
					if (StringUtil.isNotEmpty(notKeyWords)) {
						StringBuilder exbuilder = new StringBuilder();
						exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
						builder.filterByTRSL(exbuilder.toString());
						countBuilder.filterByTRSL(exbuilder.toString());
					}
				}

				// 时间  这个位置不要在加权重之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			}else if (StringUtil.isNotEmpty(notKeyWords)){
				// 时间 只有排除词的情况要加在排除词之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				//查询条件只有时间和排除词
				childBuilder.append("(\"").append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				String builderAs = builder.asTRSL();
				String countAs = countBuilder.asTRSL();
				StringBuilder not_builderkeyWord = new StringBuilder();
				StringBuilder not_countkeyWord = new StringBuilder();
				builder = new QueryCommonBuilder();
				builder.page(pageNo,pageSize);
				builder.setDatabase(Const.MIX_DATABASE.split(";"));
				countBuilder = new QueryCommonBuilder();
				countBuilder.page(pageNo,pageSize);
				countBuilder.setDatabase(Const.MIX_DATABASE.split(";"));
				if (keyWordIndex.equals("positioCon")) {// 标题 + 正文
					not_builderkeyWord = not_builderkeyWord.append(builderAs).append(" NOT (").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString()).append(") NOT (").append(FtsFieldConst.FIELD_CONTENT).append(":").append(childBuilder.toString()).append(")");
					not_countkeyWord = not_countkeyWord.append(countAs).append(" NOT (").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString()).append(") NOT (").append(FtsFieldConst.FIELD_CONTENT).append(":").append(childBuilder.toString()).append(")");
				}else if (keyWordIndex.equals("positionKey")){
					// 仅标题
					not_builderkeyWord = new StringBuilder(builderAs).append(" NOT ").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString());
					not_countkeyWord = new StringBuilder(countAs).append(" NOT ").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString());
				}
				builder.filterByTRSL(not_builderkeyWord.toString());
				countBuilder.filterByTRSL(not_countkeyWord.toString());
			}else {
				// 时间 只有排除词的情况要加在排除词之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			}

			if("ALL".equals(source)){
				source = Const.STATTOTAL_GROUP;
			}
			if (source.contains("微信") && !source.contains("国内微信")){
				source = source.replaceAll("微信","国内微信");
			}
			if(source.contains("国内论坛") || source.contains("微博")){
				//只有包含微博和论坛的情况下才会出现主贴和回帖，其他时候无意义
				//这段代码要写在添加groupName之前，因为主回帖和原转发都是特性，主要把grouname的论坛和微博拿出来，单独用OR拼接，否则回帖时其他类型数据查不到
				StringBuffer sb = new StringBuffer();
				if(source.contains("微博") && (StringUtil.isNotEmpty(forwarPrimary) || StringUtil.isNotEmpty(forwarPrimary1))){
					sb.append("(").append(FtsFieldConst.FIELD_GROUPNAME + ":(\"微博\")");
					if ("primary".equals(forwarPrimary)) {
						// 原发
						sb.append(" AND ").append(Const.PRIMARY_WEIBO);
					}else  if ("forward".equals(forwarPrimary)){
						//转发
						sb.append(" NOT ").append(Const.PRIMARY_WEIBO);
					}
					if ("primary".equals(forwarPrimary1)) {
						// 原发
						sb.append(" AND ").append(Const.PRIMARY_WEIBO);
					}else  if ("forward".equals(forwarPrimary1)){
						//转发
						sb.append(" NOT ").append(Const.PRIMARY_WEIBO);
					}
					sb.append(")");
					source = source.replaceAll(";微博","").replaceAll("微博;","");
				}
				if(source.contains("国内论坛") && (StringUtil.isNotEmpty(invitationCard) || StringUtil.isNotEmpty(invitationCard1))){
					if(sb.length() >0){
						sb.append(" OR ");
					}
					sb.append("(").append(FtsFieldConst.FIELD_GROUPNAME + ":(\"国内论坛\")");
					if ("0".equals(invitationCard)) {// 主贴
						sb.append(" AND ").append(Const.NRESERVED1_LUNTAN);
					} else if ("1".equals(invitationCard)) {// 回帖
						sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
					}
					if ("0".equals(invitationCard1)) {// 主贴
						sb.append(" AND ").append(Const.NRESERVED1_LUNTAN);
					} else if ("1".equals(invitationCard1)) {// 回帖
						sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
					}
					sb.append(")");
					source = source.replaceAll(";国内论坛","").replaceAll("国内论坛;","");
				}
				source = source.replaceAll(";", " OR ").replace("境外媒体", "国外新闻");
				if (source.endsWith("OR ")) {
					source = source.substring(0, source.lastIndexOf("OR"));
				}
				if(sb.length() > 0){
					sb.append(" OR ");
				}
				sb.append("(").append(FtsFieldConst.FIELD_GROUPNAME + ":("+source+")").append(")");

				builder.filterByTRSL(sb.toString());
				countBuilder.filterByTRSL(sb.toString());
			}else{
				// 增加具体来源
				if (StringUtils.isNotBlank(source) && !"ALL".equals(source)) {
					source = source.replaceAll(";", " OR ");
					if (source.endsWith("OR ")) {
						source = source.substring(0, source.lastIndexOf("OR"));
					}
					builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
					countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
				}else if("ALL".equals(source)){
					builder.filterField(FtsFieldConst.FIELD_GROUPNAME,
							Const.STATTOTAL_GROUP.replace(";", " OR ").replace("境外媒体", "国外新闻"), Operator.Equal);
					countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, Const.STATTOTAL_GROUP.replace(";", " OR ").replace("境外媒体", "国外新闻"), Operator.Equal);
				}
			}
			// 结果中搜索
			if (StringUtil.isNotEmpty(fuzzyValue) && StringUtil.isNotEmpty(fuzzyValueScope)) {//在结果中搜索,范围为全文的时候
				String[] split = fuzzyValue.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				fuzzyValue = splitNode.substring(0, splitNode.length() - 1);
				if (fuzzyValue.endsWith(";") || fuzzyValue.endsWith(",") || fuzzyValue.endsWith("；")
						|| fuzzyValue.endsWith("，")) {
					fuzzyValue = fuzzyValue.substring(0, fuzzyValue.length() - 1);

				}
				StringBuilder fuzzyBuilder  = new StringBuilder();
				String hybaseField = "fullText";
				switch (fuzzyValueScope){
					case "title":
						hybaseField = FtsFieldConst.FIELD_URLTITLE;
						break;
					case "source":
						hybaseField = FtsFieldConst.FIELD_SITENAME;
						break;
					case "author":
						hybaseField = FtsFieldConst.FIELD_AUTHORS;
						break;
				}
				if("fullText".equals(hybaseField)){
					fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND \"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}else {
					fuzzyBuilder.append(hybaseField).append(":((\"").append(fuzzyValue.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}
				builder.filterByTRSL(fuzzyBuilder.toString());
				countBuilder.filterByTRSL(fuzzyBuilder.toString());
			}
			//来源网站
			if (null != fromWebSite && !fromWebSite.isEmpty()){
				addFieldValue(fromWebSite,null,null," OR ",FtsFieldConst.FIELD_SITENAME,builder,countBuilder);
			}

			//排除网站
			if (null != excludeWeb && !excludeWeb.isEmpty()){
				String builderTrsl = builder.asTRSL();
				String countTrsl = countBuilder.asTRSL();
				excludeWeb = excludeWeb.replaceAll("[;|；]"," OR ");

				if (excludeWeb.endsWith(" OR ")) {
					excludeWeb = excludeWeb.substring(0, excludeWeb.length() - 4);
				}

				if (!"".equals(builderTrsl)){
					builderTrsl += new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(excludeWeb)
							.append(")").toString();
					builder = new QueryCommonBuilder();
					builder.filterByTRSL(builderTrsl);
					builder.page(pageNo,pageSize);
					builder.setDatabase(Const.MIX_DATABASE.split(";"));
				}
				if (!"".equals(countTrsl)){
					countTrsl +=  new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(excludeWeb)
							.append(")").toString();
					countBuilder = new QueryCommonBuilder();
					countBuilder.filterByTRSL(countTrsl);
					countBuilder.page(pageNo,pageSize);
					countBuilder.setDatabase(Const.MIX_DATABASE.split(";"));
				}

			}


			//新闻信息资质
			if (!"ALL".equals(newsInformation)){
				addFieldValue(newsInformation,null,null," OR ",FtsFieldConst.FIELD_WXB_GRADE,builder,countBuilder);
			}

			//网站类型
			if (!"ALL".equals(siteType) ){
				addFieldValue(siteType,null,null," OR ",FtsFieldConst.FIELD_SITE_PROPERTY,builder,countBuilder);
			}

			//新闻可供转载网站/门户类型
			if (!"ALL".equals(reprintPortal)){
				addFieldValue(reprintPortal,null,null," OR ",FtsFieldConst.FIELD_SITE_APTUTIDE,builder,countBuilder);
			}
			// 媒体行业
			if (!"ALL".equals(mediaIndustry)) {

				addFieldValue(mediaIndustry,null,null,"* OR ",FtsFieldConst.FIELD_CHANNEL_INDUSTRY,builder,countBuilder);
			}


			if (!"ALL".equals(area)) { // 地域
				String[] areaSplit = area.split(";");
				String contentArea = "";
				for (int i = 0; i < areaSplit.length; i++) {
					areaSplit[i] = "(中国\\\\" + areaSplit[i] + "*)";
					if (i != areaSplit.length - 1) {
						areaSplit[i] += " OR ";
					}
					contentArea += areaSplit[i];
				}
				builder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
			}

			if (!"ALL".equals(emotion)) { // 情感
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			}

			log.info(builder.asTRSL());

			if("ordinarySearch".equals(searchPage)){
				searchPage = SearchPage.ORDINARY_SEARCH.toString();
			}else{
				searchPage = SearchPage.COMMON_SEARCH.toString();
			}
			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "relevance":// 相关性排序
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
				case "hot":
					//20191125  复制专题分析全部列表页的代码，进行的展示，如果有问题，高级搜索、专题分析、这里都会有问题
					QueryBuilder hotBuilder = new QueryBuilder();
					hotBuilder.filterByTRSL(builder.asTRSL());
					hotBuilder.page(builder.getPageNo(),builder.getPageSize());
					String[] database = builder.getDatabase();
					if (ObjectUtil.isNotEmpty(database)){
						hotBuilder.setDatabase(StringUtil.join(database,";"));
					}
					InfoListResult list = commonListService.queryPageListForHot(hotBuilder,source,loginUser,type,true);
					/*QueryBuilder hotCountBuilder = new QueryBuilder();
					hotCountBuilder.filterByTRSL(countBuilder.asTRSL());
					hotCountBuilder.page(countBuilder.getPageNo(),countBuilder.getPageSize());
					if (ObjectUtil.isNotEmpty(database)){
						hotCountBuilder.setDatabase(StringUtil.join(database,";"));
					}
					InfoListResult list = getHotList(hotBuilder, hotCountBuilder, loginUser,type,searchPage);
					*/
					if (isExport) {
						PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>) list.getContent();
						List<FtsDocumentCommonVO> listVo = content.getPageItems();
						RedisUtil.setMix(keyName, listVo);
					}
					return list;
				default:
					if(isFuzzySearch){
						builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					}else{
						if (weight) {
							builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
						} else {
							builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
						}
					}

					break;
			}
			log.info("综合："+builder.asTRSL());
			InfoListResult sectionList = commonListService.queryPageList(builder,sim,irSimflag,irSimflagAll,source,type,loginUser,true);
			// (InfoListResult) getDocListContrast(builder, loginUser, sim, irSimflag,irSimflagAll,type,searchPage);
			if(isExport){
				//存入缓存  以便混合列表导出时使用
				PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>)sectionList.getContent();
				List<FtsDocumentCommonVO> listVo = content.getPageItems();
				RedisUtil.setMix(keyName, listVo);
			}
			return sectionList;
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e, e);
		}
	}

	@Override
	public Object documentForOrdinarySearch(boolean sim, boolean irSimflag, int pageNo, int pageSize, String source, String time, String emotion, String sort, String invitationCard, String keywords, String keyWordIndex, boolean weight, String fuzzyValue,String type) throws TRSException {
		try {
			QueryBuilder builder = new QueryBuilder();
			builder.setPageNo(pageNo);
			builder.setPageSize(pageSize);
			QueryBuilder countBuilder = new QueryBuilder();
			User loginUser = UserUtils.getUser();

			StringBuilder childBuilder = new StringBuilder();
			// 关键词中搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String[] split = keywords.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				keywords = splitNode.substring(0, splitNode.length() - 1);
				// 防止全部关键词结尾为;报错
				String replaceAnyKey = "";
				if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
						|| keywords.endsWith("，")) {
					replaceAnyKey = keywords.substring(0, keywords.length() - 1);
					childBuilder.append("((\"").append(
							replaceAnyKey.replaceAll("[,|，]+", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				} else {
					childBuilder.append("((\"")
							.append(keywords.replaceAll("[,|，]+", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				}
			}
			// 结果中搜索
			if (StringUtil.isNotEmpty(fuzzyValue)) {
				String[] split = fuzzyValue.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				fuzzyValue = splitNode.substring(0, splitNode.length() - 1);
				String replaceAnyKey = "";
				if (fuzzyValue.endsWith(";") || fuzzyValue.endsWith(",") || fuzzyValue.endsWith("；")
						|| fuzzyValue.endsWith("，")) {
					replaceAnyKey = fuzzyValue.substring(0, fuzzyValue.length() - 1);
					childBuilder.append(" AND (\"").append(
							replaceAnyKey.replaceAll("[,|，]+", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
							.append("\")");
				} else {
					childBuilder.append(" AND (\"")
							.append(fuzzyValue.replaceAll("[,|，]+", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
							.append("\")");
				}
			}
			if (childBuilder.length() > 0) {
				if (keyWordIndex.equals("positioCon")) {// 标题 + 正文
					builder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					countBuilder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					if (weight) {
						QueryBuilder weightBuilder = new QueryBuilder();
						weightBuilder.page(pageNo, pageSize);
						QueryBuilder weightCountBuilder = new QueryBuilder();
						weightCountBuilder.page(pageNo, pageSize);
						weightBuilder.filterByTRSL(builder.asTRSL() + FtsFieldConst.WEIGHT + " OR "
								+ FtsFieldConst.FIELD_CONTENT + ":" + childBuilder.toString());
						weightCountBuilder.filterByTRSL(countBuilder.asTRSL() + FtsFieldConst.WEIGHT + " OR "
								+ FtsFieldConst.FIELD_CONTENT + ":" + childBuilder.toString());
						builder = weightBuilder;
						countBuilder = weightCountBuilder;
					} else {
						builder.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(), Operator.Equal);
						countBuilder.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(),
								Operator.Equal);
					}
				} else if (keyWordIndex.equals("positionKey")) {
					// 仅标题
					builder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					countBuilder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
				}
			}

			// 时间
			builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			// 来源
			if (StringUtils.isNotBlank(source)) {
				source = source.equals("境外媒体") ? "国外新闻" : source;
			}
			if (!"ALL".equals(source)) {
				// 单选状态
				if ("国内新闻".equals(source)) {
					String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻").toString();
					builder.filterByTRSL(trsl);
					countBuilder.filterByTRSL(trsl);
				} else if ("国内论坛".equals(source)) {
					StringBuffer sb = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 OR ")
							.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧");
					if ("0".equals(invitationCard)) {// 主贴
						sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":0");
					} else if ("1".equals(invitationCard)) {// 回帖
						sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
					}
					builder.filterByTRSL(sb.toString());
					countBuilder.filterByTRSL(sb.toString());
				} else {
					builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
					countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
				}
			}

			if (!"ALL".equals(emotion)) { // 情感
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			}

			log.info(builder.asTRSL());
			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "hot":
					return getHotList(builder, countBuilder, loginUser,type);
				case "relevance":// 相关性排序
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
				default:
					if (weight) {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
					}
					break;
			}
			log.info(builder.asTRSL());
			return getDocList(builder, loginUser, sim, irSimflag,false,false,type);
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e, e);
		}
	}

	@Override
	public Object documentCommonSearch(String specialId, int pageNo, int pageSize, String source, String time,
									   String area, String industry, String emotion, String sort, String invitationCard, String forwarPrimary, String keywords,
									   String fuzzyValueScope,String notKeyWords, String keyWordIndex, String foreign, boolean isExport,String type) throws TRSException {
		try {
			SpecialProject specialProject = specialProjectService.findOne(specialId);
			User loginUser = UserUtils.getUser();
			QueryCommonBuilder builder = specialProject.toCommonBuilder(pageNo, pageSize, false);
			QueryCommonBuilder countBuilder = specialProject.toCommonBuilder(pageNo, pageSize, false);
			boolean sim = specialProject.isSimilar();
			boolean irSimflag = specialProject.isIrSimflag();
			boolean irSimflagAll = specialProject.isIrSimflagAll();
			boolean weight = specialProject.isWeight();
			String sources = specialProject.getSource();
			// 选择来源库
			if (StringUtils.isNotBlank(sources) && !sources.equals("ALL")) {
				String[] split = sources.split(";");
				String[] databases = TrslUtil.chooseDatabases(split);
				builder.setDatabase(databases);
				countBuilder.setDatabase(databases);
			} else {// 数据源全选的时候
				//全选的时候应该根据所属机构的dataSources来，若机构dataSources为ALL，sources = Const.ALL_GROUP;
				String organizationId = specialProject.getOrganizationId();
				if (StringUtil.isNotEmpty(organizationId) && !"platformId".equals(organizationId)){
					Organization organization = organizationService.findOne(organizationId);
					if (ObjectUtil.isNotEmpty(organization)){
						sources = organization.getDataSources();
					}
				}
				if (StringUtil.isNotEmpty(sources) || "ALL".equals(sources)){
					sources = Const.ALL_GROUP;
				}
				builder.setDatabase(Const.MIX_DATABASE.split(";"));
				countBuilder.setDatabase(Const.MIX_DATABASE.split(";"));
			}
			// 时间
			builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);

			//1、原发/转发
			String weiboTrsl = FtsFieldConst.FIELD_GROUPNAME + ":(微博)";
			String weiboCountTrsl = FtsFieldConst.FIELD_GROUPNAME + ":(微博)";
			if ("primary".equals(forwarPrimary)) {
				// 原发
				weiboTrsl = weiboTrsl + " AND "+ Const.PRIMARY_WEIBO;
				weiboCountTrsl = weiboCountTrsl + " AND "+ Const.PRIMARY_WEIBO;
			}else  if ("forward".equals(forwarPrimary)){
				//转发
				weiboTrsl = weiboTrsl + " NOT "+ Const.PRIMARY_WEIBO;
				weiboCountTrsl = weiboCountTrsl + " NOT "+ Const.PRIMARY_WEIBO;
			}
			//2、主贴/回帖
			String lunTanTrsl = FtsFieldConst.FIELD_GROUPNAME + ":(国内论坛)";
			String lunTanCountTrsl = FtsFieldConst.FIELD_GROUPNAME + ":(国内论坛)";
			if ("0".equals(invitationCard)){
				//主贴
				lunTanTrsl = lunTanTrsl + " AND " + FtsFieldConst.FIELD_NRESERVED1 + ":(0 OR \"\")";
				lunTanCountTrsl = lunTanCountTrsl + " AND " + FtsFieldConst.FIELD_NRESERVED1 + ":(0 OR \"\")";
			}else if ("1".equals(invitationCard)){
				lunTanTrsl = lunTanTrsl + " AND " + FtsFieldConst.FIELD_NRESERVED1 + ":" +invitationCard;
				lunTanCountTrsl = lunTanCountTrsl + " AND " + FtsFieldConst.FIELD_NRESERVED1 + ":" +invitationCard;
			}

			//3、除去论坛 和 微博
			if (StringUtil.isNotEmpty(forwarPrimary) && StringUtil.isNotEmpty(invitationCard)){
				sources = sources.replaceAll("微博;","").replaceAll(";微博","").replaceAll("微博","").replaceAll("国内论坛;","").replaceAll(";国内论坛","").replaceAll("国内论坛","");
			}else if (StringUtil.isNotEmpty(forwarPrimary)){
				sources = sources.replaceAll("微博;","").replaceAll(";微博","").replaceAll("微博","");
			}else if (StringUtil.isNotEmpty(invitationCard)){
				sources = sources.replaceAll("国内论坛;","").replaceAll(";国内论坛","").replaceAll("国内论坛","");
			}
			// 增加具体来源
			if (StringUtils.isNotBlank(source) && "ALL".equals(source)) {
				if (StringUtils.isNotBlank(sources)) {
					sources = sources.replaceAll(";", " OR ");
					if (sources.endsWith("OR ")) {
						sources = sources.substring(0, sources.lastIndexOf("OR"));
					}
				}
			}
			sources = sources.replace("微信", "国内微信").replace("境外媒体", "国外新闻");
			String exGForOther = FtsFieldConst.FIELD_GROUPNAME + ":("+sources+")";
			if (StringUtil.isNotEmpty(forwarPrimary) && StringUtil.isNotEmpty(invitationCard)){
				String zongTrsl = "("+weiboTrsl + ") OR (" + lunTanTrsl + ")";
				String zongCountTrsl = "("+weiboCountTrsl+ ") OR (" + lunTanCountTrsl + ")";

				if (StringUtil.isNotEmpty(sources)){
					zongTrsl += " OR (" + exGForOther+")";
					zongCountTrsl += " OR (" + exGForOther+")";
				}

				builder.filterByTRSL(zongTrsl);
				countBuilder.filterByTRSL(zongCountTrsl);
			}else if (StringUtil.isNotEmpty(forwarPrimary)){
				String twoTrsl = "("+weiboTrsl + ")";
				String twoCountTrsl = "("+weiboCountTrsl + ")";

				if (StringUtil.isNotEmpty(sources)){
					twoTrsl += " OR (" + exGForOther+")";
					twoCountTrsl += " OR (" + exGForOther+")";
				}

				builder.filterByTRSL(twoTrsl);
				countBuilder.filterByTRSL(twoCountTrsl);
			}else if (StringUtil.isNotEmpty(invitationCard)){
				String twoTrsl1 = "("+lunTanTrsl + ")";
				String twoCountTrsl1 = "("+lunTanCountTrsl + ")";

				if (StringUtil.isNotEmpty(sources)){
					twoTrsl1 += " OR (" + exGForOther+")";
					twoCountTrsl1 += " OR (" + exGForOther+")";
				}

				builder.filterByTRSL(twoTrsl1);
				countBuilder.filterByTRSL(twoCountTrsl1);
			}else if (StringUtil.isNotEmpty(sources)){
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, sources, Operator.Equal);
			}

			if (!"ALL".equals(area)) { // 地域
				String[] areaSplit = area.split(";");
				String contentArea = "";
				for (int i = 0; i < areaSplit.length; i++) {
					areaSplit[i] = "(中国\\\\" + areaSplit[i] + "*)";
					if (i != areaSplit.length - 1) {
						areaSplit[i] += " OR ";
					}
					contentArea += areaSplit[i];
				}
				builder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
			}
			if (null != foreign && !"".equals(foreign) && !"ALL".equals(foreign)) {
				// 放境外区域
				setForeignData(foreign, null, null, builder, countBuilder);
			}
			if (!"ALL".equals(industry)) { // 行业
				builder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_INDUSTRY, industry.split(";"), Operator.Equal);
			}
			if (!"ALL".equals(emotion)) { // 情感
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
				countBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			}

			// 结果中搜索
			if (StringUtil.isNotEmpty(keywords) && StringUtil.isNotEmpty(fuzzyValueScope)) {
				String[] split = keywords.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				keywords = splitNode.substring(0, splitNode.length() - 1);
				if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
						|| keywords.endsWith("，")) {
					keywords = keywords.substring(0, keywords.length() - 1);

				}
				StringBuilder fuzzyBuilder  = new StringBuilder();
				String hybaseField = "fullText";
				switch (fuzzyValueScope){
					case "title":
						hybaseField = FtsFieldConst.FIELD_URLTITLE;
						break;
					case "source":
						hybaseField = FtsFieldConst.FIELD_SITENAME;
						break;
					case "author":
						hybaseField = FtsFieldConst.FIELD_AUTHORS;
						break;
				}
				if("fullText".equals(hybaseField)){
					fuzzyBuilder.append(FtsFieldConst.FIELD_TITLE).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))").append(" OR "+FtsFieldConst.FIELD_CONTENT).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}else {
					fuzzyBuilder.append(hybaseField).append(":((\"").append(keywords.replaceAll("[,|，]+","\") AND (\"")
							.replaceAll("[;|；]+","\" OR \"")).append("\"))");
				}
				builder.filterByTRSL(fuzzyBuilder.toString());
				countBuilder.filterByTRSL(fuzzyBuilder.toString());
				log.info(builder.asTRSL());
			}
			//拼接排除词
			if (StringUtil.isNotEmpty(notKeyWords) ) {
				if("positioCon".equals(keyWordIndex)){
					StringBuilder exbuilder = new StringBuilder();
					exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
							.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
					builder.filterByTRSL(exbuilder.toString());
					countBuilder.filterByTRSL(exbuilder.toString());
					StringBuilder exbuilder2 = new StringBuilder();
					exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
							.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
					builder.filterByTRSL(exbuilder2.toString());
					countBuilder.filterByTRSL(exbuilder2.toString());
				}else {
					StringBuilder exbuilder2 = new StringBuilder();
					exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
							.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
					builder.filterByTRSL(exbuilder2.toString());
					countBuilder.filterByTRSL(exbuilder2.toString());
				}

			}
			switch (sort) { // 排序
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				case "relevance":// 相关性排序
					builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
					break;
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "hot":
					//20191031  修复JIRA1605专题分析信息列表 全部 加上热点排序（因前端没时间，页面上暂未加上相似文章数显示）
					QueryBuilder hotBuilder = new QueryBuilder();
					hotBuilder.filterByTRSL(builder.asTRSL());
					hotBuilder.page(builder.getPageNo(),builder.getPageSize());
					String[] database = builder.getDatabase();
					if (ObjectUtil.isNotEmpty(database)){
						hotBuilder.setDatabase(StringUtil.join(database,";"));
					}
					InfoListResult list = commonListService.queryPageListForHot(hotBuilder,specialProject.getSource(),loginUser,type,true);
					/*QueryBuilder hotCountBuilder = new QueryBuilder();
					hotCountBuilder.filterByTRSL(countBuilder.asTRSL());
					hotCountBuilder.page(countBuilder.getPageNo(),countBuilder.getPageSize());
					if (ObjectUtil.isNotEmpty(database)){
						hotCountBuilder.setDatabase(StringUtil.join(database,";"));
					}

					InfoListResult list = getHotList(hotBuilder, hotCountBuilder, loginUser,type);*/
					if (isExport) {
						PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>) list.getContent();
						List<FtsDocumentCommonVO> listVo = content.getPageItems();
						RedisUtil.setMix(specialId, listVo);
					}
					return list;
				default:
					if (weight) {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
					}
					break;
			}
			String groupName = "ALL".equals(source) ? specialProject.getSource() : source;
			InfoListResult list = commonListService.queryPageList(builder,sim,irSimflag,irSimflagAll,groupName,type,loginUser,true);
			// getDocListContrast(builder, loginUser, sim, irSimflag,irSimflagAll,type);
			if (isExport) {
				PagedList<FtsDocumentCommonVO> content = (PagedList<FtsDocumentCommonVO>) list.getContent();
				List<FtsDocumentCommonVO> listVo = content.getPageItems();
				RedisUtil.setMix(specialId, listVo);
			}
			return list;
		} catch (TRSException e) {
			throw e;
		} catch (Exception e) {
			throw new OperationException("查询出错：" + e.getMessage(), e);
		}
	}

	@Override
	public void setForeignData(String foreign, QueryBuilder builder, QueryBuilder countBuilder,
							   QueryCommonBuilder builderCom, QueryCommonBuilder countBuilderCom) {
		if (foreign != null) {
			String[] split = foreign.split(";");
			StringBuffer sb = new StringBuffer();
			for (String str : split) {
				if (str.equals("美国")) {
					if (sb.length() > 0) {
						sb.append(" OR ").append("美国之音").append(" OR ").append("美国纽约时报").append(" OR ")
								.append("美国华尔街日报");
					} else {
						sb.append("美国之音").append(" OR ").append("美国纽约时报").append(" OR ").append("美国华尔街日报");
					}
				} else if (str.equals("英国")) {
					if (sb.length() > 0) {
						sb.append(" OR ").append("BBC中文网").append(" OR ").append("英国金融时报").append(" OR ")
								.append("英国路透社");
					} else {
						sb.append("BBC中文网").append(" OR ").append("英国金融时报").append(" OR ").append("英国路透社");
					}
				} else if (str.equals("日本")) {
					if (sb.length() > 0) {
						sb.append(" OR ").append("日经中文网").append(" OR ").append("日本共同社").append(" OR ").append("日本新闻网");
					} else {
						sb.append("日经中文网").append(" OR ").append("日本共同社").append(" OR ").append("日本新闻网");
					}
				} else if (str.equals("亚洲")) {
					if (sb.length() > 0) {
						sb.append(" OR ").append("台湾中央社").append(" OR ").append("菲律宾商报").append(" OR ").append("朝鲜日报")
								.append(" OR ").append("韩国中央日报").append(" OR ").append("韩联社").append(" OR ")
								.append("新加坡联合早报");
					} else {
						sb.append("台湾中央社").append(" OR ").append("菲律宾商报").append(" OR ").append("朝鲜日报").append(" OR ")
								.append("韩国中央日报").append(" OR ").append("韩联社").append(" OR ").append("新加坡联合早报");

					}
				} else if (str.equals("欧洲")) {
					if (sb.length() > 0) {
						sb.append(" OR ").append("俄罗斯卫星通讯社").append(" OR ").append("法国国际广播电台中文网").append(" OR ")
								.append("德国之声中文网").append(" OR ").append("联合国新闻网");
					} else {
						sb.append("俄罗斯卫星通讯社").append(" OR ").append("法国国际广播电台中文网").append(" OR ").append("德国之声中文网")
								.append("联合国新闻网");
					}
				} else if ("ALL".equals(foreign)) {
					sb.append("美国之音").append(" OR ").append("美国纽约时报").append(" OR ").append("美国华尔街日报").append(" OR ")
							.append("BBC中文网").append(" OR ").append("英国金融时报").append(" OR ").append("英国路透社")
							.append(" OR ").append("日经中文网").append(" OR ").append("日本共同社").append(" OR ")
							.append("日本新闻网").append(" OR ").append("台湾中央社").append(" OR ").append("菲律宾商报")
							.append(" OR ").append("朝鲜日报").append(" OR ").append("韩国中央日报").append(" OR ").append("韩联社")
							.append(" OR ").append("新加坡联合早报").append(" OR ").append("俄罗斯卫星通讯社").append(" OR ")
							.append("法国国际广播电台中文网").append(" OR ").append("德国之声中文网").append(" OR ").append("联合国新闻网");
				}
			}
			// String trsl = builder.asTRSL();
			// String countTrsl = countBuilder.asTRSL();

			if (sb.length() > 0) {
				if (builder != null) {
					builder.filterField(FtsFieldConst.FIELD_SITENAME, sb.toString(), Operator.Equal);

				}
				if (countBuilder != null) {
					countBuilder.filterField(FtsFieldConst.FIELD_SITENAME, sb.toString(), Operator.Equal);
				}
				if (builderCom != null) {
					builderCom.filterField(FtsFieldConst.FIELD_SITENAME, sb.toString(), Operator.Equal);

				}
				if (countBuilderCom != null) {
					countBuilderCom.filterField(FtsFieldConst.FIELD_SITENAME, sb.toString(), Operator.Equal);
				}

				/*
				 * trsl = " OR " + sb.toString(); countTrsl = " OR " +
				 * sb.toString(); builder = new QueryBuilder(); countBuilder =
				 * new QueryBuilder(); builder.filterByTRSL(trsl);
				 * countBuilder.filterByTRSL(countTrsl);
				 */
			}
		}
	}

	/**
	 * 获取热点微信相似文章详情
	 *
	 * @param builder
	 *            QueryBuilder
	 * @return InfoListResult
	 */
	@Override
	public InfoListResult getHotWechatSimListDetail(QueryBuilder builder, User user, boolean sim, boolean irSimflag,boolean irSimflagAll,String type)
			throws TRSException {
		List<FtsDocumentWeChat> ftsList = new ArrayList<>();
		List<String> md5List = new ArrayList<>();
		final String pageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		final String nextPageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		try {
			String asTrsl = builder.asTRSL();
			log.info("正式：" + asTrsl);
			// 在这存个空的 在hybase里边填完整的
			String trslk = pageId + "trsl";
			RedisUtil.setString(trslk, builder.asTRSL());
			builder.setKeyRedis(trslk);
			// 这个给异步计算时候用
			String trsHot = pageId + "hot";
			RedisUtil.setString(trsHot, builder.asTRSL());
			PagedList<FtsDocumentWeChat> pagedList = hybase8SearchService.ftsPageList(builder, FtsDocumentWeChat.class, sim,
					irSimflag,irSimflagAll,type);
			List<FtsDocumentWeChat> list = pagedList.getPageItems();
			// 推荐列表排除自己
			label: {
				while (list.size() > 0) {
					// int index = 0;
					// 检验是否预警
					List<String> sids = new ArrayList<>();
					StringBuilder sb = new StringBuilder();

					for (FtsDocumentWeChat document : list) {
						sids.add(document.getHkey());
						sb.append(document.getHkey()).append(",");
					}

					List<Favourites> favouritesList = favouritesService.findAll(user);
					//List<Favourites> library = favouritesRepository.findByUserIdAndSidIn(userId, sids);
					long alertBefore = System.currentTimeMillis();
					// 把已经预警的装里边
					//不查预警
					/*List<String> sidAlert = new ArrayList<>();
					List<AlertEntity> alertList = null;
					if (httpClient){
						String aSids = "";
						if (sb.length() > 0){
							aSids = sb.substring(0,sb.length()-1);
						}
						alertList = AlertUtil.getAlerts(userId,aSids,alertNetinsightUrl);
					}else {
						alertList = alertRepository.findByUserIdAndSidIn(userId, sids);

					}
					long alertAfter = System.currentTimeMillis();
					log.info("预警表查询用了" + (alertAfter - alertBefore));
					if (null != alertList && alertList.size() > 0){
						for (AlertEntity alert : alertList) {
							sidAlert.add(alert.getSid());
						}
					}*/

					for (FtsDocumentWeChat document : list) {
						String id = document.getHkey();
						// 预警数据
						//不查预警
						/*int indexOf = sidAlert.indexOf(id);
						if (indexOf < 0) {
							document.setSend(false);
						} else {
							document.setSend(true);
						}*/
						for (Favourites favourites : favouritesList) {
							if (favourites.getSid().toString().equals(id)) {// 该文章收藏了
								document.setFavourite(true);
								break;
							} else {
								document.setFavourite(false);
							}
						}
						if (ObjectUtils.isEmpty(favouritesList)) {
							document.setFavourite(false);
						}

						document.setContent(StringUtil.cutContent(StringUtil.replaceImg(document.getContent()), 160));
						if (StringUtil.isNotEmpty(document.getUrlTitle())){
							document.setUrlTitle( StringUtil.replaceAnnotation(document.getUrlTitle()).replace("&amp;nbsp;", ""));
						}
						//微信库主键是hkey  sid没啥用
						document.setSid(document.getHkey());
						document.setTrslk(trslk);
						md5List.add(document.getMd5Tag());
						ftsList.add(document);

					}

					// 不排重的情况下
					TimingCachePool.put(pageId, new InfoListResult<>(pageId, pagedList, nextPageId, 0, trslk));
					// int finalIndex = index;
					// final String trsSim = trslk;
					// 点击相似文章 进去的详细列表页，不再需要计算相似文章
					// fixedThreadPool.execute(() ->
					// calculateSimNumWeChat(pageId, ftsList, userId,
					// trsHot,builder.isServer()));
					break label;
				}
			}
		} catch (Exception e) {
			throw new OperationException("检索异常：", e);
		}
		long currentTimeMillis = System.currentTimeMillis();
		InfoListResult object = (InfoListResult) TimingCachePool.get(pageId);
		long currentTimeMillis2 = System.currentTimeMillis();
		long redisGet = currentTimeMillis2 - currentTimeMillis;
		log.info("从redis取数据用" + redisGet);
		return object;
	}


	/**
	 * 获取热点微信相似文章详情
	 *
	 * @param builder
	 *            QueryBuilder
	 * @return InfoListResult
	 */
	@Override
	public InfoListResult getHotStatusSimListDetail(QueryBuilder builder, User user, boolean sim, boolean irSimflag,boolean irSimflagAll,String type)
			throws TRSException {
		List<FtsDocumentStatus> ftsList = new ArrayList<>();
		List<String> md5List = new ArrayList<>();
		final String pageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		final String nextPageId = GUIDGenerator.generate(InfoListServiceImpl.class);
		try {
			String asTrsl = builder.asTRSL();
			log.info("正式：" + asTrsl);
			//在这存个空的  在hybase里边填完整的
			String trslk = pageId+"trsl";
			RedisUtil.setString(trslk, builder.asTRSL());
			builder.setKeyRedis(trslk);
			//这个给异步计算时候用
			String trsHot = pageId + "hot";
			RedisUtil.setString(trsHot, builder.asTRSL());
			PagedList<FtsDocumentStatus> pagedList = hybase8SearchService.ftsPageList(builder, FtsDocumentStatus.class, sim,
					irSimflag,irSimflagAll,type);
			List<FtsDocumentStatus> list = pagedList.getPageItems();
			// 推荐列表排除自己
			label: {
				while (list.size() > 0) {
					// int index = 0;

					List<Favourites> favouritesList = favouritesService.findAll(user);
//					List<Favourites> library = favouritesRepository.findByUserId(userId,
//							new Sort(Sort.Direction.DESC, "lastModifiedTime"));
					// 检验是否预警
					List<String> sids = new ArrayList<>();
					StringBuilder sb = new StringBuilder();
					for (FtsDocumentStatus document : list) {
						sids.add(document.getMid());
						sb.append(document.getMid()).append(",");
					}
					// 把已经预警的装里边
					/*List<String> sidAlert = new ArrayList<>();
					long alertBefore = System.currentTimeMillis();
					List<AlertEntity> alertList = null;
					if (httpClient){
						String aSids = "";
						if (sb.length() > 0){
							aSids = sb.substring(0,sb.length()-1);
						}
						alertList = AlertUtil.getAlerts(userId,aSids,alertNetinsightUrl);
					}else {
						alertList = alertRepository.findByUserIdAndSidIn(userId, sids);
					}

					long alertAfter = System.currentTimeMillis();
					log.info("预警表查询用了" + (alertAfter - alertBefore));
					if (null != alertList && alertList.size() > 0){
						for (AlertEntity alert : alertList) {
							sidAlert.add(alert.getSid());
						}
					}*/

					for (FtsDocumentStatus document : list) {
						String id = document.getMid();
						// 预警数据
						/*int indexOf = sidAlert.indexOf(id);
						if (indexOf < 0) {
							document.setSend(false);
						} else {
							document.setSend(true);
						}*/
						for (Favourites favourites : favouritesList) {
							if (favourites.getSid().toString().equals(id)) {// 该文章收藏了
								document.setFavourite(true);
								break;
							} else {
								document.setFavourite(false);
							}
						}
						if (ObjectUtils.isEmpty(favouritesList)) {
							document.setFavourite(false);
						}

						String content = StringUtil.replaceImg(document.getStatusContent());
						//document.setStatusContent(StringUtil.substringRed(content, Const.CONTENT_LENGTH));
						document.setStatusContent(StringUtil.cutContent(StringUtil.replaceImg(document.getStatusContent()), 160));
						document.setTrslk(trslk);
						md5List.add(document.getMd5Tag());
						ftsList.add(document);

					}

					// 不排重的情况下
					TimingCachePool.put(pageId, new InfoListResult<>(pageId, pagedList, nextPageId, 0, trslk));
					// int finalIndex = index;
//					final String trsSim = trslk;
					//点击相似文章 进去的详细列表页，不再需要计算相似文章
//					fixedThreadPool.execute(() -> calculateSimNumWeChat(pageId, ftsList, userId, trsHot,builder.isServer()));
					break label;
				}
			}
		} catch (Exception e) {
			throw new OperationException("检索异常：", e);
		}
		long currentTimeMillis = System.currentTimeMillis();
		InfoListResult object = (InfoListResult) TimingCachePool.get(pageId);
		long currentTimeMillis2 = System.currentTimeMillis();
		long redisGet = currentTimeMillis2 - currentTimeMillis;
		log.info("从redis取数据用" + redisGet);
		return object;
	}

	/**
	 * 文章详情页的推荐文章列表
	 * @param sid 文章主键
	 * @param source 来源
	 * @return
	 * @throws TRSSearchException
	 * @throws TRSException
	 */
	@Override
	public Object simlist(String sid,String source) throws TRSSearchException, TRSException{
		QueryBuilder builderOne = new QueryBuilder();
		builderOne.page(0, 1);
		Map<String, Object> map = new HashMap<String, Object>();
		QueryBuilder channelBuilder = new QueryBuilder();
		channelBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
		channelBuilder.page(0, 5);
		// 先判断来源 查库(因为前端传参格式不固定，会传‘手机客户端’这样的，所以for循环遍历，拿到每一个再contains)
		for(String groupName : Const.MEDIA_TYPE_NEWS){
			if(groupName.contains(source)){
				//传统  IR_CHANNEL_INDUSTRY  ;分组  AND关系查找
				builderOne.setDatabase(Const.HYBASE_NI_INDEX);
				String idTrsl = new StringBuffer().append(FtsFieldConst.FIELD_SID).append(":").append(sid).toString();
				builderOne.filterByTRSL(idTrsl);
				List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(builderOne, FtsDocument.class, false, false,false,null);
				String channelIndustry = ftsQuery.get(0).getChannelIndustry();
				String siteName = ftsQuery.get(0).getSiteName();
				String groupNameInResult = ftsQuery.get(0).getGroupName();
				if (StringUtil.isNotEmpty(channelIndustry)) {//优先查channelIndustry
					String[] split = channelIndustry.split(";");
					StringBuilder stringBuilder = new StringBuilder();
					for(String s:split){
						String[] splitInner = s.split("\\\\");
						if(null!=splitInner && splitInner.length>0){
							stringBuilder.append(splitInner[0]).append("*").append(" AND ");
						}else{
							stringBuilder.append(s).append(" AND ");
						}
					}
					String channelString = stringBuilder.toString();
					if(StringUtil.isNotEmpty(channelString)){
						if(channelString.endsWith(" AND ")){
							channelString = channelString.substring(0, channelString.length()-5);
						}
						channelBuilder.filterField(FtsFieldConst.FIELD_CHANNEL_INDUSTRY, channelString, Operator.Equal);
					}
					//在channel的基础上再加上groupName
					channelBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupNameInResult, Operator.Equal);
				}else if(StringUtil.isNotEmpty(siteName)){//channelIndustry为空时查同sitename下最新五篇
					channelBuilder.filterField(FtsFieldConst.FIELD_SITENAME, siteName, Operator.Equal);
				}
				//排除自己
				channelBuilder.filterField(FtsFieldConst.FIELD_SID, sid, Operator.NotEqual);
				List<FtsDocument> channelList = hybase8SearchService.ftsQuery(channelBuilder, FtsDocument.class, false, true,false,null);
				for (FtsDocument document : channelList) {
					String content = document.getContent();
					List<String> imgSrcList = StringUtil.getImgStr(content);
					if(imgSrcList!=null && imgSrcList.size()>0){
						if(imgSrcList.size()>1){
							document.setImgSrc(imgSrcList.get(1));
						}else{
							document.setImgSrc(imgSrcList.get(0));
						}
					}
					content = StringUtil.replaceImg(document.getContent());
					//document.setContent(StringUtil.substringRed(content, Const.CONTENT_LENGTH));
					document.setContent(StringUtil.cutContent(StringUtil.replaceImg(document.getContent()), 160));
					document.setTitle(document.getTitle().replace("&amp;nbsp;", ""));
				}
				map.put("simCount", 5);
				map.put("simuList", channelList);
				return map;
			}
		}
		if (Const.MEDIA_TYPE_WEIBO.contains(source)) {
			//微博微信  作者5篇最新文章
			String hostTrsl = new StringBuffer().append(FtsFieldConst.FIELD_MID).append(":").append(sid).toString();
			builderOne.filterByTRSL(hostTrsl);
			// 一个sid对应拿到这篇文章详情，再拿screenName--博主，最后拿到他所有文章，前5篇，排序
			List<FtsDocumentStatus> ftsQuery = hybase8SearchService.ftsQuery(builderOne, FtsDocumentStatus.class,
					false, false,false,null);
			String screenName = ftsQuery.get(0).getScreenName();
			// 拼接搜索表达式
			String findArticleTrsl = new StringBuffer().append(FtsFieldConst.FIELD_SCREEN_NAME).append(":\"")
					.append(screenName + "\"").toString();
			channelBuilder.filterByTRSL(findArticleTrsl);
			channelBuilder.filterField(FtsFieldConst.FIELD_MID, sid, Operator.NotEqual);
			channelBuilder.setStartTime(DateUtil.getDate(-15));//15天之内
			channelBuilder.setEndTime(new Date());
			SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMddHHmmss);
			channelBuilder.filterField(FtsFieldConst.FIELD_SID, sid, Operator.NotEqual);
			channelBuilder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{sdf.format(DateUtil.getDate(-15)),sdf.format(new Date())}, Operator.Between);
			List<FtsDocumentStatus> everyArticle = hybase8SearchService.ftsQuery(channelBuilder,
					FtsDocumentStatus.class, false, true,false,null);

			map.put("simCount", 5);
			map.put("simuList", everyArticle);
		} else if (Const.MEDIA_TYPE_TF.contains(source)) {//海外的没出方案
			builderOne.setDatabase(Const.HYBASE_OVERSEAS);
			String hostTrsl = new StringBuffer().append(FtsFieldConst.FIELD_SID).append(":").append(sid).toString();
			builderOne.filterByTRSL(hostTrsl);
			List<FtsDocumentTF> ftsQuery = hybase8SearchService.ftsQuery(builderOne, FtsDocumentTF.class, false,
					false,false,null);
			String screenName = "";
			for (FtsDocumentTF ftsDocument : ftsQuery) {
				screenName = ftsDocument.getScreenName();
			}
			// 拼接搜索表达式
			String findArticleTrsl = new StringBuffer().append(FtsFieldConst.FIELD_SCREEN_NAME).append(":\"")
					.append(screenName + "\"").toString();
			channelBuilder.filterByTRSL(findArticleTrsl);
			List<FtsDocumentTF> everyArticle = hybase8SearchService.ftsQuery(channelBuilder,
					FtsDocumentTF.class, false, true,false,null);

			map.put("simCount", 5);
			map.put("simuList", everyArticle);

		} else if (Const.MEDIA_TYPE_WEIXIN.contains(source)) {
			String hostTrsl = new StringBuffer().append(FtsFieldConst.FIELD_HKEY).append(":").append(sid).toString();
			builderOne.filterByTRSL(hostTrsl);
			List<FtsDocumentWeChat> ftsQuery = hybase8SearchService.ftsQuery(builderOne, FtsDocumentWeChat.class,
					false, false,false,null);
			String siteName = ftsQuery.get(0).getSiteName();//公众号
			// 拼接搜索表达式
			String findArticleTrsl = new StringBuffer().append(FtsFieldConst.FIELD_SITENAME).append(":\"")
					.append(siteName + "\"").toString();
			channelBuilder.filterByTRSL(findArticleTrsl);
			channelBuilder.filterField(FtsFieldConst.FIELD_HKEY, sid, Operator.NotEqual);
			channelBuilder.setStartTime(DateUtil.getDate(-15));//15天之内
			channelBuilder.setEndTime(new Date());
			SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMddHHmmss);
			channelBuilder.filterField(FtsFieldConst.FIELD_URLTIME, new String[]{sdf.format(DateUtil.getDate(-15)),sdf.format(new Date())}, Operator.Between);
			List<FtsDocumentWeChat> wechatList = hybase8SearchService.ftsQuery(channelBuilder,
					FtsDocumentWeChat.class, false, true,false,null);
			map.put("simCount", 5);
			map.put("simuList", wechatList);
		}
		return map;
	}


	/**
	 * 增加排除站点
	 *
	 * @Return : void
	 */
	private void addExcloudSite(String excludeWeb,QueryBuilder queryBuilder,QueryBuilder countBuilder,QueryCommonBuilder queryCommonBuilder,QueryCommonBuilder countCommonBuilder) {
		String asTRSL = "";
		if (null != queryBuilder){
			asTRSL = queryBuilder.asTRSL();
		}
		String asCountTRSL = "";
		if (null != countBuilder){
			asCountTRSL = countBuilder.asTRSL();
		}
		String asCommonTRSL = "";
		if (null != queryCommonBuilder){
			asCommonTRSL = queryCommonBuilder.asTRSL();
		}
		String asCountCommonTRSL = "";
		if (null != countCommonBuilder){
			asCountCommonTRSL = countCommonBuilder.asTRSL();
		}
		excludeWeb = excludeWeb.replaceAll("[;|；]"," OR ");
		/*String notSite = "";
		for (String site : excludeWeb) {
			notSite += site + " OR ";
		}*/
		if (excludeWeb.endsWith(" OR ")) {
			excludeWeb = excludeWeb.substring(0, excludeWeb.length() - 4);
		}
		if (!"".equals(asTRSL)){
			asTRSL += new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(excludeWeb)
					.append(")").toString();
			queryBuilder = new QueryBuilder();
			queryBuilder.filterByTRSL(asTRSL);
		}

		if (!"".equals(asCountTRSL)){
			asCountTRSL +=  new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(excludeWeb)
					.append(")").toString();
			countBuilder = new QueryBuilder();
			countBuilder.filterByTRSL(asCountTRSL);
		}

		if (!"".equals(asCommonTRSL)){
			asCommonTRSL +=  new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(excludeWeb)
					.append(")").toString();
			queryCommonBuilder = new QueryCommonBuilder();
			queryCommonBuilder.filterByTRSL(asCommonTRSL);
		}
		if (!"".equals(asCountCommonTRSL)){
			asCountCommonTRSL +=  new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(excludeWeb)
					.append(")").toString();
			countCommonBuilder = new QueryCommonBuilder();
			countCommonBuilder.filterByTRSL(asCountCommonTRSL);
		}
	}

	/**
	 * 增加 字段值
	 * @Return : void
	 */
	private void addFieldValue(String value,QueryBuilder queryBuilder,QueryBuilder countBuilder,String replacePoiont,String fieldValue,QueryCommonBuilder queryCommonBuilder,QueryCommonBuilder countCommonBuilder) {

		if (null != value){

			String[] split = value.split("[;|；]");
			if (split.length==1 && "* OR ".equals(replacePoiont)){
				value = value+"*";
			}else {
				value = value.replaceAll("[;|；]",replacePoiont);
			}
			if (value.endsWith(replacePoiont)) {
				value = value.substring(0, value.length() - replacePoiont.length());
			}
			if (null != queryBuilder){
				queryBuilder.filterField(fieldValue,value,Operator.Equal);
			}
			if (null != countBuilder){
				countBuilder.filterField(fieldValue,value,Operator.Equal);
			}
			if (null != queryCommonBuilder){
				queryCommonBuilder.filterField(fieldValue,value,Operator.Equal);
			}
			if (null != countCommonBuilder){
				countCommonBuilder.filterField(fieldValue,value,Operator.Equal);
			}

		}

	}
	@Override
	public List<Object> getOriginalData(String trsl,String statusTrsl,String weChatTrsl,String requestTime,Integer period,
										boolean isSimilar, boolean irSimflag,boolean irSimflagAll)throws TRSException, TRSSearchException{
		List<Object> listData = new ArrayList<>();
		String[] timeRange = DateUtil.setTimeForSomeMinute(requestTime,period);
		QueryCommonBuilder searchBuilder = getSearchBuilder("",timeRange,trsl,weChatTrsl,statusTrsl);
		log.info("平安金服预警接口 ----- 查询表达式："+searchBuilder.asTRSL());
		try{
			if(searchBuilder != null){
				PagedList<FtsDocumentCommonVO> pagedList = hybase8SearchService.pageListCommon(searchBuilder, isSimilar, irSimflag,irSimflagAll,null);
				if(pagedList != null && pagedList.getPageItems() != null  && pagedList.getPageItems().size()>0){
					listData.addAll(pagedList.getPageItems());
				}
			}
		}catch(Exception e){
			log.info(e.getMessage());
			throw new TRSException(e.getMessage());
		}
		return listData;
	}

	@Override
	public List<ClassInfo> searchstattotal(boolean sim, boolean irSimflag,boolean irSimflagAll, int pageNo, int pageSize, String source, String time, String area,
										   String mediaIndustry, String emotion, String sort, String invitationCard,String forwardPrimary, String keywords,
										   String notKeyWords, String keyWordIndex, boolean weight, String fuzzyValue, String fromWebSite, String excludeWeb,
										   String newsInformation, String reprintPortal, String siteType,boolean isExport,String type) throws TRSException {
		List<ClassInfo> classInfo = new ArrayList<>();
		try {
			QueryCommonBuilder builder = new QueryCommonBuilder();
			builder.setDatabase(Const.MIX_DATABASE.split(";"));
			builder.setPageNo(pageNo);
			builder.setPageSize(pageSize);
			QueryCommonBuilder countBuilder = new QueryCommonBuilder();
			countBuilder.setDatabase(Const.MIX_DATABASE.split(";"));

			User loginUser = UserUtils.getUser();

			StringBuilder childBuilder = new StringBuilder();
			// 关键词中搜索
			if (StringUtil.isNotEmpty(keywords)) {
				String[] split = keywords.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				keywords = splitNode.substring(0, splitNode.length() - 1);
				// 防止全部关键词结尾为;报错
				String replaceAnyKey = "";
				if (keywords.endsWith(";") || keywords.endsWith(",") || keywords.endsWith("；")
						|| keywords.endsWith("，")) {
					replaceAnyKey = keywords.substring(0, keywords.length() - 1);
					childBuilder.append("((\"").append(
							replaceAnyKey.replaceAll("[,|，]+", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				} else {
					childBuilder.append("((\"")
							.append(keywords.replaceAll("[,|，]+", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
							.append("\"))");
				}
			}
			// 结果中搜索
			if (StringUtil.isNotEmpty(fuzzyValue)) {
				String[] split = fuzzyValue.split(",");
				String splitNode = "";
				for (int i = 0; i < split.length; i++) {
					if (StringUtil.isNotEmpty(split[i])) {
						splitNode += split[i] + ",";
					}
				}
				fuzzyValue = splitNode.substring(0, splitNode.length() - 1);
				if (fuzzyValue.endsWith(";") || fuzzyValue.endsWith(",") || fuzzyValue.endsWith("；")
						|| fuzzyValue.endsWith("，")) {
					fuzzyValue = fuzzyValue.substring(0, fuzzyValue.length() - 1);

				}
				if (childBuilder.length()>0){
					childBuilder.append(" AND (\"")
							.append(fuzzyValue.replaceAll("[,|，]+", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
							.append("\")");
				}else {
					childBuilder.append("(\"").append(fuzzyValue.replaceAll("[,|，]+", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \""))
							.append("\")");
				}
			}

			if (childBuilder.length() > 0) {
				if (keyWordIndex.equals("positioCon")) {// 标题 + 正文
					builder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					countBuilder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					if (weight) {
						QueryCommonBuilder weightBuilder = new QueryCommonBuilder();
						weightBuilder.page(pageNo, pageSize);
						QueryCommonBuilder weightCountBuilder = new QueryCommonBuilder();
						weightCountBuilder.page(pageNo, pageSize);
						weightBuilder.filterByTRSL(builder.asTRSL() + FtsFieldConst.WEIGHT + " OR "
								+ FtsFieldConst.FIELD_CONTENT + ":" + childBuilder.toString());
						weightCountBuilder.filterByTRSL(countBuilder.asTRSL() + FtsFieldConst.WEIGHT + " OR "
								+ FtsFieldConst.FIELD_CONTENT + ":" + childBuilder.toString());
						builder = weightBuilder;
						builder.setDatabase(Const.MIX_DATABASE.split(";"));
						countBuilder = weightCountBuilder;
						countBuilder.setDatabase(Const.MIX_DATABASE.split(";"));
					} else {
						builder.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(), Operator.Equal);
						countBuilder.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(),
								Operator.Equal);
					}
					//拼接排除词
					if (StringUtil.isNotEmpty(notKeyWords)) {
						StringBuilder exbuilder = new StringBuilder();
						exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
						builder.filterByTRSL(exbuilder.toString());
						countBuilder.filterByTRSL(exbuilder.toString());
						StringBuilder exbuilder2 = new StringBuilder();
						exbuilder2.append("*:* -").append(FtsFieldConst.FIELD_CONTENT).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");;
						builder.filterByTRSL(exbuilder2.toString());
						countBuilder.filterByTRSL(exbuilder.toString());
					}
				} else if (keyWordIndex.equals("positionKey")) {
					//拼接排除词
					if (StringUtil.isNotEmpty(notKeyWords)) {
						StringBuilder exbuilder = new StringBuilder();
						exbuilder.append("*:* -").append(FtsFieldConst.FIELD_URLTITLE).append(":(\"")
								.append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
						builder.filterByTRSL(exbuilder.toString());
						countBuilder.filterByTRSL(exbuilder.toString());
					}
					// 仅标题
					builder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
					countBuilder.filterChildField(FtsFieldConst.FIELD_TITLE, childBuilder.toString(), Operator.Equal);
				}

				// 时间  这个位置不要在加权重之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			}else if (StringUtil.isNotEmpty(notKeyWords)){
				// 时间 只有排除词的情况要加在排除词之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				//查询条件只有时间和排除词
				childBuilder.append("(\"").append(notKeyWords.replaceAll("[;|；]+", "\" OR \"")).append("\")");
				String builderAs = builder.asTRSL();
				String countAs = countBuilder.asTRSL();
				StringBuilder not_builderkeyWord = new StringBuilder();
				StringBuilder not_countkeyWord = new StringBuilder();
				builder = new QueryCommonBuilder();
				builder.page(pageNo,pageSize);
				builder.setDatabase(Const.MIX_DATABASE.split(";"));
				countBuilder = new QueryCommonBuilder();
				countBuilder.page(pageNo,pageSize);
				countBuilder.setDatabase(Const.MIX_DATABASE.split(";"));
				if (keyWordIndex.equals("positioCon")) {// 标题 + 正文
					not_builderkeyWord = not_builderkeyWord.append(builderAs).append(" NOT (").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString()).append(") NOT (").append(FtsFieldConst.FIELD_CONTENT).append(":").append(childBuilder.toString()).append(")");
					not_countkeyWord = not_countkeyWord.append(countAs).append(" NOT (").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString()).append(") NOT (").append(FtsFieldConst.FIELD_CONTENT).append(":").append(childBuilder.toString()).append(")");
				}else if (keyWordIndex.equals("positionKey")){
					// 仅标题
					not_builderkeyWord = new StringBuilder(builderAs).append(" NOT ").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString());
					not_countkeyWord = new StringBuilder(countAs).append(" NOT ").append(FtsFieldConst.FIELD_TITLE).append(":").append(childBuilder.toString());
				}
				builder.filterByTRSL(not_builderkeyWord.toString());
				countBuilder.filterByTRSL(not_countkeyWord.toString());
			}else {
				// 时间 只有排除词的情况要加在排除词之前
				builder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
				countBuilder.filterField(FtsFieldConst.FIELD_URLTIME, DateUtil.formatTimeRange(time), Operator.Between);
			}
			if("ALL".equals(source)){
				source = Const.STATTOTAL_GROUP;
			}
			if (source.contains("微信") && !source.contains("国内微信")){
				source = source.replaceAll("微信","国内微信");
			}
			if(source.contains("国内论坛") || source.contains("微博")){
				//只有包含微博和论坛的情况下才会出现主贴和回帖，其他时候无意义
				//这段代码要写在添加groupName之前，因为主回帖和原转发都是特性，主要把grouname的论坛和微博拿出来，单独用OR拼接，否则回帖时其他类型数据查不到
				//String invitationCard,String invitationCard1, String forwarPrimary,String forwarPrimary1,
				StringBuffer sb = new StringBuffer();
				if(source.contains("微博") && StringUtil.isNotEmpty(forwardPrimary)){
					sb.append("(").append(FtsFieldConst.FIELD_GROUPNAME + ":(\"微博\")");
					if ("primary".equals(forwardPrimary)) {
						// 原发
						sb.append(" AND ").append(Const.PRIMARY_WEIBO);
					}else  if ("forward".equals(forwardPrimary)){
						//转发
						sb.append(" NOT ").append(Const.PRIMARY_WEIBO);
					}
					sb.append(")");
					source = source.replaceAll(";微博","").replaceAll("微博;","");
				}
				if(source.contains("国内论坛") && StringUtil.isNotEmpty(invitationCard)){
					if(sb.length() >0){
						sb.append(" OR ");
					}
					sb.append("(").append(FtsFieldConst.FIELD_GROUPNAME + ":(\"国内论坛\")");
					if ("0".equals(invitationCard)) {// 主贴
						sb.append(" AND ").append(Const.NRESERVED1_LUNTAN);
					} else if ("1".equals(invitationCard)) {// 回帖
						sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
					}
					sb.append(")");
					source = source.replaceAll(";国内论坛","").replaceAll("国内论坛;","");
				}
				source = source.replaceAll(";", " OR ").replace("境外媒体", "国外新闻");
				if (source.endsWith("OR ")) {
					source = source.substring(0, source.lastIndexOf("OR"));
				}
				if(sb.length() > 0){
					sb.append(" OR ");
				}
				sb.append("(").append(FtsFieldConst.FIELD_GROUPNAME + ":("+source+")").append(")");

				builder.filterByTRSL(sb.toString());
				countBuilder.filterByTRSL(sb.toString());
			}else{
				// 增加具体来源
				if (StringUtils.isNotBlank(source) && !"ALL".equals(source)) {
					source = source.replaceAll(";", " OR ");
					if (source.endsWith("OR ")) {
						source = source.substring(0, source.lastIndexOf("OR"));
					}
					builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
					countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
				}else if("ALL".equals(source)){
					builder.filterField(FtsFieldConst.FIELD_GROUPNAME,
							Const.STATTOTAL_GROUP.replace(";", " OR ").replace("境外媒体", "国外新闻"), Operator.Equal);
					countBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME, Const.STATTOTAL_GROUP.replace(";", " OR ").replace("境外媒体", "国外新闻"), Operator.Equal);
				}
			}
			//来源网站
			if (null != fromWebSite && !fromWebSite.isEmpty()){
				addFieldValue(fromWebSite,null,null," OR ",FtsFieldConst.FIELD_SITENAME,builder,countBuilder);
			}

			//排除网站
			if (null != excludeWeb && !excludeWeb.isEmpty()){
				String builderTrsl = builder.asTRSL();
				String countTrsl = countBuilder.asTRSL();
				excludeWeb = excludeWeb.replaceAll("[;|；]"," OR ");

				if (excludeWeb.endsWith(" OR ")) {
					excludeWeb = excludeWeb.substring(0, excludeWeb.length() - 4);
				}

				if (!"".equals(builderTrsl)){
					builderTrsl += new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(excludeWeb)
							.append(")").toString();
					builder = new QueryCommonBuilder();
					builder.filterByTRSL(builderTrsl);
					builder.page(pageNo,pageSize);
					builder.setDatabase(Const.MIX_DATABASE.split(";"));
				}
				if (!"".equals(countTrsl)){
					countTrsl +=  new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(excludeWeb)
							.append(")").toString();
					countBuilder = new QueryCommonBuilder();
					countBuilder.filterByTRSL(countTrsl);
					countBuilder.page(pageNo,pageSize);
					countBuilder.setDatabase(Const.MIX_DATABASE.split(";"));
				}
			}

			//新闻信息资质
			if (!"ALL".equals(newsInformation)){
				addFieldValue(newsInformation,null,null," OR ",FtsFieldConst.FIELD_WXB_GRADE,builder,countBuilder);
			}

			//网站类型
			if (!"ALL".equals(siteType) ){
				addFieldValue(siteType,null,null," OR ",FtsFieldConst.FIELD_SITE_PROPERTY,builder,countBuilder);
			}

			//新闻可供转载网站/门户类型
			if (!"ALL".equals(reprintPortal)){
				addFieldValue(reprintPortal,null,null," OR ",FtsFieldConst.FIELD_SITE_APTUTIDE,builder,countBuilder);
			}
			// 媒体行业
			if (!"ALL".equals(mediaIndustry)) {

				addFieldValue(mediaIndustry,null,null,"* OR ",FtsFieldConst.FIELD_CHANNEL_INDUSTRY,builder,countBuilder);
			}


			if (!"ALL".equals(area)) { // 地域
				String[] areaSplit = area.split(";");
				String contentArea = "";
				for (int i = 0; i < areaSplit.length; i++) {
					areaSplit[i] = "(中国\\\\" + areaSplit[i] + "*)";
					if (i != areaSplit.length - 1) {
						areaSplit[i] += " OR ";
					}
					contentArea += areaSplit[i];
				}
				builder.filterField(FtsFieldConst.FIELD_CATALOG_AREA, contentArea, Operator.Equal);
			}

			if (!"ALL".equals(emotion)) { // 情感
				builder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
			}

			log.info(builder.asTRSL());
			switch (sort) { // 排序
				case "desc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
					break;
				case "asc":
					builder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					countBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
					break;
				default:
					if (weight) {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_RELEVANCE + ";-" + FtsFieldConst.FIELD_URLTIME);
					} else {
						builder.setOrderBy("-" + FtsFieldConst.FIELD_URLTIME + ";-" + FtsFieldConst.FIELD_RELEVANCE);
					}
					break;
			}

			log.warn("searchstattal接口：" + builder.asTRSL());
			GroupResult categoryQuery = hybase8SearchService.categoryQuery(builder.isServer(), builder.asTRSL(), sim, irSimflag,irSimflagAll, FtsFieldConst.FIELD_GROUPNAME,
					builder.getPageSize(), "special",Const.MIX_DATABASE);
			List<GroupInfo> groupList = categoryQuery.getGroupList();
			Map<String, Long> map = new LinkedHashMap<>();
			map.put("国内新闻", 0L);
			map.put("微博", 0L);
			map.put("微信", 0L);
			map.put("国内新闻_手机客户端", 0L);
			map.put("国内论坛", 0L);
			map.put("国内博客", 0L);
			map.put("国内新闻_电子报", 0L);
			map.put("国外新闻", 0L);
			map.put("Twitter", 0L);
			map.put("FaceBook", 0L);
			map.put("国内视频", 0L);
			for (GroupInfo groupInfo : groupList) {
				String fieldValue = groupInfo.getFieldValue();
				if (!"国内论坛_元搜索".equals(fieldValue) && !"国内分组".equals(fieldValue) && !"国内博客_站内搜索".equals(fieldValue)) {
					if ("国内微信".equals(fieldValue)) {
						fieldValue = "微信";
					}
					if ("Facebook".equals(fieldValue)) {
						fieldValue = "FaceBook";
					}
					if ("手机客户端".equals(fieldValue)) {
						fieldValue = "客户端";
					}
					if ("境外新闻".equals(fieldValue)) {
						fieldValue = "国外新闻";
					}
					map.put(fieldValue, groupInfo.getCount());
				}
			}
			Iterator<Map.Entry<String, Long>> iterMap = map.entrySet().iterator();
			while (iterMap.hasNext()) {
				Map.Entry<String, Long> entry = iterMap.next();
				String key = entry.getKey();
				classInfo.add(new ClassInfo(key, entry.getValue().intValue()));
			}

		} catch (Exception e) {
			log.error("statByClassification error ", e);
			throw new OperationException("statByClassification error: " + e, e);
		}
		return classInfo;
	}

	public QueryCommonBuilder getSearchBuilder(String type,String[] timeRange,String ctTrsl,String wxTrsl,String wbTrsl) throws TRSException {
		QueryCommonBuilder searchBuilder = new QueryCommonBuilder();
		searchBuilder.page(0,-1);
		String trsl = "";
		List<String> database = new ArrayList<>();
		if(ctTrsl != null && !"".equals(ctTrsl)){
			ctTrsl = FtsFieldConst.FIELD_GROUPNAME + ":("+Const.TYPE_NEWS.replaceAll("境外媒体","国外新闻").replaceAll(";"," OR ")+") AND (" + ctTrsl +")";
			trsl = "("+ctTrsl+ ")";
			database.add(Const.HYBASE_NI_INDEX);
		}
		if(wbTrsl != null && !"".equals(wbTrsl)){
			wbTrsl = FtsFieldConst.FIELD_GROUPNAME +  ":(\"微博\") AND (" + wbTrsl+")";
			trsl = "".equals(trsl) ? "("+wbTrsl+ ")" : trsl +" OR (" + wbTrsl + ")";
			database.add(Const.WEIBO);
		}
		if(wxTrsl != null && !"".equals(wxTrsl)){
			wxTrsl = FtsFieldConst.FIELD_GROUPNAME +  ":(\"国内微信\" OR \"微信\" ) AND (" + wxTrsl+")";
			trsl = "".equals(trsl) ? "("+wxTrsl+ ")" : trsl +" OR (" + wxTrsl + ")";
			database.add(Const.WECHAT_COMMON);
		}
		if (StringUtils.isBlank(trsl)) {
			throw new TRSException("无查询表达式");
		}
		String[] arrays = new String[database.size()];
		searchBuilder.setDatabase(database.toArray(arrays));
		searchBuilder.setOrderBy("IR_URLTIME");
		searchBuilder.filterByTRSL(trsl);
		searchBuilder.filterField(FtsFieldConst.FIELD_HYLOAD_TIME, new String[] { timeRange[0], timeRange[1] }, Operator.Between);
		return searchBuilder;
	}
}
