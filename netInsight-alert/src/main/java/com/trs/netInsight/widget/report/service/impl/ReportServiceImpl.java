package com.trs.netInsight.widget.report.service.impl;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Criterion.MatchMode;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.appApi.entity.AppApiAccessToken;
import com.trs.netInsight.support.appApi.entity.repository.IAppAccessTokenRepository;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.*;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.report.word.WordFactory;
import com.trs.netInsight.support.report.word.WordUtil;
import com.trs.netInsight.support.report.word.model.WordData;
import com.trs.netInsight.util.*;
import com.trs.netInsight.util.favourites.FavouritesUtil;
import com.trs.netInsight.util.report.PhantomjsFactory;
import com.trs.netInsight.widget.alert.entity.repository.AlertRepository;
import com.trs.netInsight.widget.analysis.entity.ChartAnalyzeEntity;
import com.trs.netInsight.widget.common.service.ICommonListService;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.report.entity.*;
import com.trs.netInsight.widget.report.entity.repository.*;
import com.trs.netInsight.widget.report.service.IReportService;
import com.trs.netInsight.widget.report.task.BuildReportTask;
import com.trs.netInsight.widget.report.util.ReportUtil;
import com.trs.netInsight.widget.special.entity.InfoListResult;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.UserRepository;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ReportServiceImpl implements IReportService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private FavouritesRepository favouritesRepository;
	@Autowired
	private MaterialLibraryRepository materialLibraryRepository;
	@Autowired
	private ReportRepository reportRepository;
	@Autowired
	private TemplateRepository templateRepository;
	@Autowired
	private ReportMaterialRepository reportMaterialRepository;
	@Autowired
	private ReportDataRepository reportDataRepository;
	@Autowired
	private ReportVeidooRepository reportVeidooRepository;
	@Autowired
	private AlertRepository alertRepository;
	@Autowired
	private IAppAccessTokenRepository iAppAccessTokenRepository;
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

	private static final String SEMICOLON = ";";
	private static final int BUILDED = 1;
	private static final String BIAS = "/";
	private static final String DOCX = ".docx";
	private static final int CODE = 1024;

	/**
	 * 单位换算 大概word中1厘米等于28.3像素
	 */
	private static final int height = 270;
	private static final int width = 405;
	public static final String nbsp = "&nbsp;";
	public static final String img = "(?=&lt;IMAGE&nbsp;SRC=).+?(?=&nbsp;&gt;)";
	public static final String gt = "&gt;";

	private static final String TIT = "#TIT#"; // title
	private static final String POS = "#POS#"; // position

	private static final String AREA = "地域分布";
	private static final String SOURCE = "来源分析";
	private static final String ACTIVE = "媒体活跃";
	private static final String EMOTION = "情感分析";
	private static final String VOLUME = "声量趋势";
	// private static final String DIFFUSE = "七、媒体扩散分析";
	// private static final String BOOM = "五、声量趋势图";
	// private static final String HOTORGAN = "六、情感分析"; 热点机构
	// private static final String HOTPLACE = "六、情感分析"; 热点地名
	// private static final String HOTWORD = "六、情感分析"; 热词分布
	// private static final String HOTNAME = "六、情感分析"; 热点人名

	/**
	 * 报告状态为分析中
	 */
	private static final int ANALYSIS = 0;

	/**
	 * 维度模板路径
	 */
//	@Value("${report.littleTemplate.path}")
//	private String littleTemp;

	/**
	 * 根据维度生成的doc文件路径
	 */
	/*@Value("${report.reportVeidoo.path}")
	private String reportVeidooPath;
*/
	/**
	 * 所有维度和成的报告文件路径
	 */
	@Value("${report.produceReport.path}")
	private String produceReport;

	/**
	 * 线程池跑任务
	 */
	private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
	/**
	 * 来源为专项监测
	 */
	private static final int specSourec = 0;

	@Autowired
	private FullTextSearch hybase8SearchService;

	/**
	 * 查询某个用户的所有收藏
	 *
	 * @throws TRSException
	 *
	 */
	@Override
	public Object getAllFavourites(User user, int pageNo, int pageSize,
								   String groupName, String keywords, String invitationCard,
								   String forwarPrimary,Boolean isExport) throws TRSException {

		List<String> sidList = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		String[] split = groupName.split(";");
		for (int i = 0; i < split.length; i++) {
			if (split[i].equals("国内新闻_手机客户端") || split[i].equals("国内新闻_电子报")) {
				split[i] = split[i].substring(split[i].length() - 3,
						split[i].length());
			}else if("境外媒体".equals(split[i])){
				split[i] = "国外新闻";
			}
		}
//		groupName = StringUtils.join(split, ";");
		String groupNameNew = StringUtils.join(split, ";");

		//原生sql
		Specification<Favourites> criteria = new Specification<Favourites>() {
			@Override
			public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

				List<Predicate> predicates = new ArrayList<>();
				if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
					predicates.add(cb.equal(root.get("userId"),user.getId()));
				}else {
					predicates.add(cb.equal(root.get("subGroupId"),user.getSubGroupId()));
				}
				if (StringUtil.isNotEmpty(keywords)){
					predicates.add(cb.or(cb.like(root.get("content"),"%"+keywords+"%"),cb.like(root.get("title"),"%"+keywords+"%")));
				}
				predicates.add(cb.isNull(root.get("libraryId")));
				if(!"ALL".equals(groupNameNew)){//查所有
					predicates.add(cb.equal(root.get("groupName"), groupNameNew));
				}
				Predicate[] pre = new Predicate[predicates.size()];
				return query.where(predicates.toArray(pre)).getRestriction();
			}
		};

		List<Favourites> findAll = favouritesRepository.findAll(criteria,new Sort(
				Sort.Direction.DESC,"urltime"));
//		String timeStart = null;
//		String timeEnd = null;
//		if (findAll.size() > 0) {
//			timeStart = findAll.get(findAll.size() - 1).getUrltime();
//			timeEnd = findAll.get(0).getUrltime();
//		}
		// 总条数
		int totalItemCount = findAll.size();
		// 总页数
		int totalList = totalItemCount % pageSize > 0 ? totalItemCount / pageSize + 1 : totalItemCount / pageSize;
		if (StringUtil.isEmpty(keywords)) {
			Page<Favourites> favouritesPage = favouritesRepository.findAll(criteria,
					new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, "createdTime")));
			if (ObjectUtil.isNotEmpty(favouritesPage)) {
				favouritesPage.forEach(item -> {
					if (item.getGroupName().equals("客户端")){
						item.setGroupName("国内新闻_手机客户端");
					}
					if (item.getGroupName().equals("电子报")){
						item.setGroupName("国内新闻_电子报");
					}
					if (item.getGroupName().equals("Twitter") || item.getGroupName().equals("FaceBook") || item.getGroupName().equals("微信")){
						item.setScreenName(item.getAuthors());
					}
					sidList.add(item.getSid());
					sb.append(item.getSid()).append(",");
//					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//				String dateString = formatter.format(item.getUrlTime());
					if(StringUtil.isEmpty(item.getUrltime()) && item.getUrlTime() != null) item.setUrltime(DateUtil.getDataToTime(item.getUrlTime()));//前端需要Urltime
					item.setUrlTitle(StringUtil.cutContentPro(StringUtil.replaceImg(subString(item.getUrlTitle())),Const.CONTENT_LENGTH));
					item.setStatusContent(StringUtil.cutContentPro(StringUtil.replaceImg(subString(item.getStatusContent())),Const.CONTENT_LENGTH));
					item.setContent(StringUtil.cutContentPro(StringUtil.replaceImg(subString(item.getContent())),Const.CONTENT_LENGTH));
					item.setTitle(StringUtil.cutContentPro(StringUtil.replaceImg(subString(item.getTitle())),Const.CONTENT_LENGTH));

				});
			}
			if(isExport){
				String sids = "";
				if (sb.length() > 0){
					sids = sb.substring(0, sb.length() - 1);
				}
				InfoListResult sectionList = (InfoListResult)favouriteMix(sids, sidList, pageNo, pageSize, totalItemCount, totalList, keywords);
				List<FtsDocumentCommonVO> listVo = (List<FtsDocumentCommonVO>)sectionList.getContent();
				RedisUtil.setMix("MyCollection", listVo);
			}
			return new InfoListResult<>(favouritesPage.getContent(), totalItemCount, totalList);
		} else {
			List<Favourites> favouriteList = favouritesRepository.findAll(criteria,
					new Sort(Sort.Direction.DESC, "createdTime"));
			if (ObjectUtil.isNotEmpty(favouriteList)) {
				favouriteList.forEach(item -> {
					sidList.add(item.getSid());
					sb.append(item.getSid()).append(",");
					//2019/10/25  目前数据库存的是客户端 前端需要国内新闻_手机客户端
					if (item.getGroupName().equals("客户端")){
						item.setGroupName("国内新闻_手机客户端");
					}
					if (item.getGroupName().equals("电子报")){
						item.setGroupName("国内新闻_电子报");
					}
					if (item.getGroupName().equals("Twitter") || item.getGroupName().equals("FaceBook") || item.getGroupName().equals("微信")){
						item.setScreenName(item.getAuthors());
					}
					if(StringUtil.isEmpty(item.getUrltime()) && item.getUrlTime() != null) item.setUrltime(DateUtil.getDataToTime(item.getUrlTime()));
					item.setUrlTitle(StringUtil.cutContentPro(StringUtil.replaceImg(subString(item.getUrlTitle())),Const.CONTENT_LENGTH));
					item.setStatusContent(StringUtil.cutContentPro(StringUtil.replaceImg(subString(item.getStatusContent())),Const.CONTENT_LENGTH));
					item.setContent(StringUtil.cutContentPro(StringUtil.replaceImg(subString(item.getContent())),Const.CONTENT_LENGTH).replace(keywords,"<font color='red'>"+keywords+"</font>"));
					item.setTitle(StringUtil.cutContentPro(StringUtil.replaceImg(subString(item.getTitle())),Const.CONTENT_LENGTH).replace(keywords,"<font color='red'>"+keywords+"</font>"));
				});
			}
			if(isExport){
				String sids = "";
				if (sb.length() > 0){
					sids = sb.substring(0, sb.length() - 1);
				}
				InfoListResult sectionList = (InfoListResult)favouriteMix(sids, sidList, pageNo, pageSize, totalItemCount, totalList, keywords);
				List<FtsDocumentCommonVO> listVo = (List<FtsDocumentCommonVO>)sectionList.getContent();
				RedisUtil.setMix("MyCollection", listVo);
			}
			return new InfoListResult<>(favouriteList, totalItemCount, totalList);
		}

		/*String sids = "";
		if (sb.length() > 0){
			sids = sb.substring(0, sb.length() - 1);
		}
		// 传统库
		if (Const.MEDIA_TYPE_NEWS.contains(groupName)) {
			return favourite(sids ,sidList, pageNo, pageSize, totalItemCount, totalList, keywords, invitationCard,
					groupName, timeStart, timeEnd);
		} else if (Const.MEDIA_TYPE_WEIBO.contains(groupName)) {
			// 微博库
			return favouriteWeiBo( sids,sidList, pageNo, pageSize, totalItemCount, totalList, keywords, forwarPrimary,
					timeStart, timeEnd);
		} else if (Const.MEDIA_TYPE_WEIXIN.contains(groupName)) {
			// 微信库
			return favouriteWeiXin(sids,sidList, pageNo, pageSize, totalItemCount, totalList, keywords, timeStart,
					timeEnd);
		} else if (Const.MEDIA_TYPE_TF.contains(groupName)) {
			// fackbook twiter
			return favouriteTF(sids, sidList, pageNo, pageSize, totalItemCount, totalList, keywords, timeStart, timeEnd);
		} else {
			//混合
			return favouriteMix(sids, sidList, pageNo, pageSize, totalItemCount, totalList, keywords);
		}*/
	}
	/**
	 * 检索用户收藏 传统库
	 *
	 * @param sidList
	 *            主键列表
	 * @param pageNo
	 *            第几页
	 * @param pageSize
	 *            一页几条
	 *
	 * @return
	 * @throws TRSException
	 */
	public Object favouriteMix( String sids,List<String> sidList, int pageNo,
							int pageSize, int totalItemCount, int totalList, String keywords) throws TRSException {
		//String TRSL = FavouritesUtil.buildSql(sidList);
		User loginUser = UserUtils.getUser();
		String TRSL = "";
		for(String s:sidList){
			List<String> list = new ArrayList<>();
			Favourites weixinFavourites = new Favourites();
			Specification<Favourites> criteria = new Specification<Favourites>() {
				@Override
				public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

					List<Predicate> predicates = new ArrayList<>();
					if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
						predicates.add(cb.equal(root.get("userId"),loginUser.getId()));
					}else {
						predicates.add(cb.equal(root.get("subGroupId"),loginUser.getSubGroupId()));
					}
					predicates.add(cb.equal(root.get("sid"),s));
					predicates.add(cb.isNull(root.get("libraryId")));
					Predicate[] pre = new Predicate[predicates.size()];
					return query.where(predicates.toArray(pre)).getRestriction();
				}
			};
			weixinFavourites =favouritesRepository.findOne(criteria);
			/*
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				 weixinFavourites =favouritesRepository.findByUserIdAndSid(loginUser.getId(),s);
			}else {
				weixinFavourites =favouritesRepository.findBySubGroupIdAndSid(loginUser.getSubGroupId(),s);
			}*/

			if ("微信".equals(weixinFavourites.getGroupName())){//如果是微信的话
				list.add(s);
				TRSL += FavouritesUtil.buildSqlWeiXin(list)+" OR ";
			}else {
				list.add(s);
				TRSL += FavouritesUtil.buildSql(list)+" OR  ";
			}
		}
		if(TRSL.trim().length()>2){
			TRSL = TRSL.substring(0,TRSL.trim().length()-2);
		}
		if (TRSL == null) {
			return null;
		}
		QueryBuilder searchBuilder = new QueryBuilder();
		TRSL = toDealWithTime(TRSL,searchBuilder);

		searchBuilder.filterByTRSL(TRSL);
		//页码  固定
		searchBuilder.page(0,pageSize);
		if (StringUtil.isNotEmpty(keywords)) {
			String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE)
					.append(":").append(keywords).append(" OR ")
					.append(FtsFieldConst.FIELD_ABSTRACTS).append(":")
					.append(keywords).append(" OR ")
					.append(FtsFieldConst.FIELD_CONTENT).append(":")
					.append(keywords).toString();
			searchBuilder.filterByTRSL(trsl);
			log.info(searchBuilder.asTRSL());
		}
		try {
			log.info(searchBuilder.asTRSL() + "我的收藏列表");
//			List<ChartAnalyzeEntity> ftsQuery = hybase8SearchService.ftsQuery(searchBuilder, ChartAnalyzeEntity.class,
//					false,false);
			//少时间
			List<FtsDocumentCommonVO> ftsQuery = hybase8SearchService.ftsQuery(searchBuilder, FtsDocumentCommonVO.class, false,false,false,null);
			if (StringUtil.isNotEmpty(keywords)) {
				QueryBuilder countBuilder = new QueryBuilder();
				countBuilder.filterByTRSL(searchBuilder.asTRSL());
				countBuilder.page(0,totalItemCount);
				countBuilder.setDatabase(Const.MIX_DATABASE);
				long ftsCount = hybase8SearchService.ftsCount(countBuilder, false, false,false,null);
				totalItemCount = new Long(ftsCount).intValue();
				totalList = totalItemCount % pageSize > 0 ? totalItemCount / pageSize + 1 : totalItemCount / pageSize;
			}
			List<FtsDocumentCommonVO> result = new ArrayList<>();
			for (FtsDocumentCommonVO chartAnalyze : ftsQuery) {
				chartAnalyze.setFavourite(true);
				// 去掉img标签
				String content = chartAnalyze.getContent();
				if (StringUtil.isNotEmpty(content)) {
					content = StringUtil.replaceImg(content);
				}
				chartAnalyze.setContent(content);
				result.add(chartAnalyze);
			}
			return new InfoListResult<>(mixByIds(sids,result), totalItemCount, totalList);

		} catch (Exception e) {
			throw new OperationException("检索异常：message:" + e);
		}
	}
	/**
	 * 混合列表按id顺序排序
	 * @param ids
	 * @param pagedList
	 * @return
	 */
	public List<FtsDocumentCommonVO> mixByIds(String ids,List<FtsDocumentCommonVO> pagedList){
		String[] split = ids.split(",");
		List<FtsDocumentCommonVO> fts = new ArrayList<>();
		//为了让他俩长度一样
		for(String id : split){
			fts.add(new FtsDocumentCommonVO());
		}
		for(int i=0;i<split.length;i++){
			for(FtsDocumentCommonVO ftsDocument : pagedList){
				if(Const.MEDIA_TYPE_WEIXIN.contains(ftsDocument.getGroupName())){
					if(split[i].equals(ftsDocument.getHkey())){
						fts.set(i, ftsDocument);
					}
				}else{
					if(split[i].equals(ftsDocument.getSid())){
						fts.set(i, ftsDocument);
					}
				}
			}
		}
		return fts;
	}

	 /* 检索用户收藏 传统库
	 *
	 * @param sidList
	 *            主键列表
	 * @param pageNo
	 *            第几页
	 * @param pageSize
	 *            一页几条
	 * @param timeStart
	 * 			  urltime开始时间
	 * @param timeEnd
	 * 			  urltime结束时间
	 * @return
	 * @throws TRSException
	 */
	public Object favourite(String sids,List<String> sidList, int pageNo,
							int pageSize, int totalItemCount, int totalList, String keywords,
							String invitationCard, String groupName,String timeStart,String timeEnd) throws TRSException {
		String TRSL = FavouritesUtil.buildSql(sidList);
		// 把已经预警的装里边
		//不查预警
		/*List<String> sidAlert = new ArrayList<>();
		List<AlertEntity> alertList = null;
		if (httpClient){
			alertList = AlertUtil.getAlerts(userId,sids,alertNetinsightUrl);
		}else {
			alertList = alertRepository.findByUserIdAndSidIn(userId, sidList);
		}
		if (null != alertList && alertList.size() > 0){
			for (AlertEntity alert : alertList) {
				sidAlert.add(alert.getSid());
			}
		}*/

		// 收藏
//		List<String> sidFavourite = new ArrayList<>();
//		List<Favourites> favouritesList = favouritesRepository.findByUserIdAndSidIn(userId, sidList);
//		if (ObjectUtil.isNotEmpty(favouritesList)) {
//			for (Favourites faSidList : favouritesList) {
//				if (StringUtil.isEmpty(faSidList.getLibraryId())){
//					// sidFavourite装载了所有已收藏的文章sid
//					sidFavourite.add(faSidList.getSid());
//				}
//			}
//		}

		if (TRSL == null) {
			return null;
		}
		QueryBuilder searchBuilder = new QueryBuilder();
		//去掉时间范围，删掉这里没用。会自动添加上。自动添加的代码不能动，所以这里给个范围
		TRSL = toDealWithTime(TRSL,searchBuilder);
		searchBuilder.filterByTRSL(TRSL);
		//页码  固定
		searchBuilder.page(0,pageSize);
		if (StringUtil.isNotEmpty(keywords)) {
			String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE)
					.append(":").append(keywords).append(" OR ")
					.append(FtsFieldConst.FIELD_ABSTRACTS).append(":")
					.append(keywords).append(" OR ")
					.append(FtsFieldConst.FIELD_CONTENT).append(":")
					.append(keywords).append(" OR ")
					.append(FtsFieldConst.FIELD_KEYWORDS).append(":")
					.append(keywords).toString();

			searchBuilder.filterByTRSL(trsl);
			log.info(searchBuilder.asTRSL());
		}
		try {
			log.info(searchBuilder.asTRSL() + "我的收藏列表");
			if ("国内论坛".equals(groupName)) {
				StringBuffer sb = new StringBuffer();
				if ("0".equals(invitationCard)) {// 主贴
					sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":0");
				} else if ("1".equals(invitationCard)) {// 回帖
					sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
				}
				searchBuilder.filterByTRSL(sb.toString());
			}
//			List<ChartAnalyzeEntity> ftsQuery = hybase8SearchService.ftsQuery(searchBuilder, ChartAnalyzeEntity.class,
//					false,false);
			List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(searchBuilder, FtsDocument.class, false,false,false,null);
			if (StringUtil.isNotEmpty(keywords)) {
				QueryBuilder countBuilder = new QueryBuilder();
				countBuilder.filterByTRSL(searchBuilder.asTRSL());
				countBuilder.page(0,totalItemCount);
				countBuilder.setDatabase(Const.HYBASE_NI_INDEX);
				long ftsCount = hybase8SearchService.ftsCount(countBuilder, false, false,false,null);
				totalItemCount = new Long(ftsCount).intValue();
				totalList = totalItemCount % pageSize > 0 ? totalItemCount / pageSize + 1 : totalItemCount / pageSize;
			}
			List<FtsDocument> result = new ArrayList<>();
			for (FtsDocument chartAnalyze : ftsQuery) {
				String id = chartAnalyze.getSid();
				// 预警数据
				//不查预警
				/*int indexOf = sidAlert.indexOf(id);
				if (indexOf < 0) {
					chartAnalyze.setSend(false);
				} else {
					chartAnalyze.setSend(true);
				}*/

				//是否收藏信息
				int indexOfFa = sidList.indexOf(id);
				if (indexOfFa < 0) {
					chartAnalyze.setFavourite(false);
				} else {
					chartAnalyze.setFavourite(true);
				}

				// 我的收藏不算相似文章数
				// 去掉img标签
				String content = chartAnalyze.getContent();
				if (StringUtil.isNotEmpty(content)) {
					content = StringUtil.replaceImg(content);
				}
				chartAnalyze.setContent(content);
				result.add(chartAnalyze);
			}
//			if (StringUtil.isNotEmpty(keywords)) {
//				totalItemCount = ftsQuery.size();
//				totalList = totalItemCount % pageSize > 0 ? totalItemCount
//						/ pageSize + 1 : totalItemCount / pageSize;
//			}
			List<FtsDocument> resultByTime = FavouritesUtil.resultByTimeTrandition(result,
					sidList);
			if(resultByTime==null || resultByTime.size()==0){
				return null;
			}
			return new InfoListResult<>(resultByTime, totalItemCount, totalList);

		} catch (Exception e) {
			throw new OperationException("检索异常：message:" + e);
		}
	}

	public Object favouriteApp(List<String> sidList, int pageNo,
							   int pageSize, int totalItemCount, int totalList, String keywords,
							   String invitationCard, String groupName) throws OperationException {
		String TRSL = FavouritesUtil.buildSql(sidList);
		if (TRSL == null) {
			return null;
		}
		QueryBuilder searchBuilder = new QueryBuilder();
		searchBuilder.filterByTRSL(TRSL);
		searchBuilder.setPageSize(pageSize);
		if (StringUtil.isNotEmpty(keywords)) {
			String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE)
					.append(":").append(keywords).append(" OR ")
					.append(FtsFieldConst.FIELD_ABSTRACTS).append(":")
					.append(keywords).append(" OR ")
					.append(FtsFieldConst.FIELD_CONTENT).append(":")
					.append(keywords).append(" OR ")
					.append(FtsFieldConst.FIELD_KEYWORDS).append(":")
					.append(keywords).toString();
			searchBuilder.filterByTRSL(trsl);
			log.info(searchBuilder.asTRSL());
		}
		searchBuilder.page(0, pageSize);
		try {
			log.info(searchBuilder.asTRSL() + "我的收藏列表");
			if ("国内论坛".equals(groupName)) {
				StringBuffer sb = new StringBuffer();
				if ("0".equals(invitationCard)) {// 主贴
					sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":0");
				} else if ("1".equals(invitationCard)) {// 回帖
					sb.append(" AND ").append(FtsFieldConst.FIELD_NRESERVED1).append(":1");
				}
				searchBuilder.filterByTRSL(sb.toString());
			}
			List<ChartAnalyzeEntity> ftsQuery = hybase8SearchService.ftsQuery(searchBuilder, ChartAnalyzeEntity.class,
					false, false,false,null);
			List<ChartAnalyzeEntity> result = new ArrayList<>();
			for (ChartAnalyzeEntity chartAnalyze : ftsQuery) {
				// 我的收藏不算相似文章数
				// 去掉img标签
				String content = chartAnalyze.getAbstracts();
				if (StringUtil.isNotEmpty(content)) {
					content = StringUtil.replaceImg(content);
				} else if (StringUtil.isNotEmpty(chartAnalyze.getContent())) {
					content = StringUtil.replaceImg(chartAnalyze.getContent());
					// chartAnalyze.setContent(content);
				}
				chartAnalyze.setContent(content);
				result.add(chartAnalyze);
			}
			// 关键词为空 页数条数根据mysql 关键词不为空 页数条数根据hybase
			if (StringUtil.isNotEmpty(keywords)) {
				totalItemCount = ftsQuery.size();
				totalList = totalItemCount % pageSize > 0 ? totalItemCount / pageSize + 1 : totalItemCount / pageSize;
			}
			List<ChartAnalyzeEntity> resultByTime = resultByTime(result, sidList);
			return new InfoListResult<>(resultByTime, totalItemCount, totalList);
			// return resultByTime;
		} catch (Exception e) {
			throw new OperationException("检索异常：message:" + e);
		}
	}

	/**
	 * 检索用户收藏 微博库
	 *
	 * @param sidList
	 *            主键列表
	 * @param pageNo
	 *            第几页
	 * @param pageSize
	 *            一页几条
	 * @return
	 * @throws TRSException
	 */
	public Object favouriteWeiBo(String sids, List<String> sidList, int pageNo, int pageSize, int totalItemCount,
								 int totalList, String keywords, String forwarPrimary, String timeStart, String timeEnd)
			throws TRSException {
		// 把已经预警的装里边
		//不查预警
		/*List<String> sidAlert = new ArrayList<>();
		List<AlertEntity> alertList = null;
		if (httpClient){
			alertList = AlertUtil.getAlerts(userId,sids,alertNetinsightUrl);
		}else {
			alertList = alertRepository.findByUserIdAndSidIn(userId, sidList);
		}
		if (null != alertList && alertList.size() > 0){
			for (AlertEntity alert : alertList) {
				sidAlert.add(alert.getSid());
			}
		}*/

		String TRSL = FavouritesUtil.buildSqlWeiBo(sidList);

		//收藏
//		List<String> sidFavourite = new ArrayList<>();
//		List<Favourites> favouritesList = favouritesRepository.findByUserIdAndSidIn(userId, sidList);
//		if (ObjectUtil.isNotEmpty(favouritesList)) {
//			for (Favourites faSidList : favouritesList) {
//				if (StringUtil.isEmpty(faSidList.getLibraryId())){
//					// sidFavourite装载了所有已收藏的文章sid
//					sidFavourite.add(faSidList.getSid());
//				}
//			}
//		}

		if (TRSL == null) {
			return null;
		}
		QueryBuilder searchBuilder = new QueryBuilder();

		TRSL = toDealWithTime(TRSL,searchBuilder);
		searchBuilder.filterByTRSL(TRSL);
		if (StringUtil.isNotEmpty(keywords)) {
			String trsl = new StringBuffer().append(FtsFieldConst.FIELD_STATUS_CONTENT).append(":\"").append(keywords)
					.append("\"").toString();
			searchBuilder.filterByTRSL(trsl);
			log.info(searchBuilder.asTRSL());
		}

		// 转发 / 原发
		String builderTRSL = searchBuilder.asTRSL();
		String builderDatabase = searchBuilder.getDatabase();
		StringBuilder builderTrsl = new StringBuilder(builderTRSL);
		if ("primary".equals(forwarPrimary)) {
			// 原发
			searchBuilder.filterByTRSL(Const.PRIMARY_WEIBO);
		} else if ("forward".equals(forwarPrimary)) {
			// 转发
			searchBuilder = new QueryBuilder();

			builderTrsl.append(" NOT ").append(Const.PRIMARY_WEIBO);
			searchBuilder.filterByTRSL(builderTrsl.toString());

			searchBuilder.setDatabase(builderDatabase);
		}
		/*// 转发 / 原发
		if ("primary".equals(forwarPrimary)) {
			// 原发
			searchBuilder.filterField(FtsFieldConst.IR_RETWEETED_MID, "0",
					Operator.Equal);
		} else if ("forward".equals(forwarPrimary)) {
			// 转发
			searchBuilder.filterField(FtsFieldConst.IR_RETWEETED_MID, "0",
					Operator.NotEqual);
		}*/
		//页码 固定
		searchBuilder.page(0, pageSize);
		try {
			List<FtsDocumentStatus> ftsQuery = hybase8SearchService.ftsQuery(searchBuilder, FtsDocumentStatus.class,
					false, false, false,null);
			List<FtsDocumentStatus> result = new ArrayList<>();
			for (FtsDocumentStatus chartAnalyze : ftsQuery) {
				String id = chartAnalyze.getMid();
				// 预警数据
				//不查预警
				/*int indexOf = sidAlert.indexOf(id);
				if (indexOf < 0) {
					chartAnalyze.setSend(false);
				} else {
					chartAnalyze.setSend(true);
				}*/

				// 是否收藏信息
				int indexOfFa = sidList.indexOf(id);
				if (indexOfFa < 0) {
					chartAnalyze.setFavourite(false);
				} else {
					chartAnalyze.setFavourite(true);
				}

				// 我的收藏不算相似文章数
				// 去掉img标签
				String content = chartAnalyze.getStatusContent();
				if (StringUtil.isNotEmpty(content)) {
					content = StringUtil.replaceImg(content);
				}
				chartAnalyze.setStatusContent(content);
				result.add(chartAnalyze);
			}
			if (StringUtil.isNotEmpty(keywords)) {
				QueryBuilder countBuilder = new QueryBuilder();
				countBuilder.filterByTRSL(searchBuilder.asTRSL());
				countBuilder.page(0,totalItemCount);
				countBuilder.setDatabase(Const.WEIBO);
				long ftsCount = hybase8SearchService.ftsCount(countBuilder, false, false,false,null);
				totalItemCount = new Long(ftsCount).intValue();
				totalList = totalItemCount % pageSize > 0 ? totalItemCount / pageSize + 1 : totalItemCount / pageSize;
			}
			List<FtsDocumentStatus> resultByTime = FavouritesUtil.resultByTimeWeiBo(result,
					sidList);
			return new InfoListResult<>(resultByTime, totalItemCount, totalList);
			// return resultByTime;
		} catch (Exception e) {
			throw new OperationException("检索异常：message:" + e);
		}
	}

	/**
	 * 检索用户收藏 微信库
	 *
	 * @param sidList
	 *            主键列表
	 * @param pageNo
	 *            第几页
	 * @param pageSize
	 *            一页几条
	 * @return
	 * @throws TRSException
	 */
	public Object favouriteWeiXin(String sids, List<String> sidList, int pageNo, int pageSize, int totalItemCount,
								  int totalList, String keywords, String timeStart, String timeEnd) throws TRSException {
		// 把已经预警的装里边
		//不查预警
		/*List<String> sidAlert = new ArrayList<>();
		List<AlertEntity> alertList = null;
		if (httpClient){
			alertList = AlertUtil.getAlerts(userId,sids,alertNetinsightUrl);
		}else {
			alertList = alertRepository.findByUserIdAndSidIn(userId, sidList);
		}
		if (null != alertList && alertList.size() > 0){
			for (AlertEntity alert : alertList) {
				sidAlert.add(alert.getSid());
			}
		}*/

		String TRSL = FavouritesUtil.buildSqlWeiXin(sidList);

		//收藏
//		List<String> sidFavourite = new ArrayList<>();
//		List<Favourites> favouritesList = favouritesRepository.findByUserIdAndSidIn(userId, sidList);
//		if (ObjectUtil.isNotEmpty(favouritesList)) {
//			for (Favourites faSidList : favouritesList) {
//				if (StringUtil.isEmpty(faSidList.getLibraryId())){//排除素材库数据
//					// sidFavourite装载了所有已收藏的文章sid
//					sidFavourite.add(faSidList.getSid());
//				}
//			}
//		}

		if (TRSL == null) {
			return null;
		}
		QueryBuilder searchBuilder = new QueryBuilder();
		/*if (StringUtil.isNotEmpty(timeStart) && StringUtil.isNotEmpty(timeEnd)) {
			searchBuilder = DateUtil.timeBuilder(timeStart + ";" + timeEnd);
		}*/
		TRSL = toDealWithTime(TRSL,searchBuilder);
		searchBuilder.filterByTRSL(TRSL);
		if (StringUtil.isNotEmpty(keywords)) {
			String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE)
					.append(":\"").append(keywords).append("\" OR ")
					.append(FtsFieldConst.FIELD_CONTENT).append(":\"")
					.append(keywords).append("\"").toString();
			searchBuilder.filterByTRSL(trsl);
		}
		//页码  固定
		searchBuilder.page(0, pageSize);
		try {
			List<FtsDocumentWeChat> ftsQuery = hybase8SearchService.ftsQuery(searchBuilder, FtsDocumentWeChat.class,
					false, false, false,null);
			List<FtsDocumentWeChat> result = new ArrayList<>();
			for (FtsDocumentWeChat chartAnalyze : ftsQuery) {
				String id = chartAnalyze.getHkey();
				// 预警数据
				//不查预警
				/*int indexOf = sidAlert.indexOf(id);
				if (indexOf < 0) {
					chartAnalyze.setSend(false);
				} else {
					chartAnalyze.setSend(true);
				}*/
				// 是否收藏信息
				int indexOfFa = sidList.indexOf(id);
				if (indexOfFa < 0) {
					chartAnalyze.setFavourite(false);
				} else {
					chartAnalyze.setFavourite(true);
				}

				// 我的收藏里边不用显示相似文章数了
				// 去掉img标签
				String content = chartAnalyze.getContent();
				if (StringUtil.isNotEmpty(content)) {
					content = StringUtil.replaceImg(content).replace("　　", "");// 过滤空格
					// img标签
				}
				// 解决收藏列表中微信摘要太长的问题，现取两行
				if (content.length() > 160) {
					content = content.substring(0, 160) + "...";
				}
				chartAnalyze.setContent(content);
				result.add(chartAnalyze);
			}
			if (StringUtil.isNotEmpty(keywords)) {
				QueryBuilder countBuilder = new QueryBuilder();
				countBuilder.filterByTRSL(searchBuilder.asTRSL());
				countBuilder.page(0,totalItemCount);
				countBuilder.setDatabase(Const.WECHAT);
				long ftsCount = hybase8SearchService.ftsCount(countBuilder, false, false,false,null);
				totalItemCount = new Long(ftsCount).intValue();
				totalList = totalItemCount % pageSize > 0 ? totalItemCount / pageSize + 1 : totalItemCount / pageSize;
			}
			List<FtsDocumentWeChat> resultByTime = FavouritesUtil.resultByTimeWeiXin(result,
					sidList);
			return new InfoListResult<>(resultByTime, totalItemCount, totalList);
		} catch (Exception e) {
			throw new OperationException("检索异常：message:" + e.getMessage(), e);
		}
	}

	/**
	 * 检索用户收藏 TF
	 *
	 * @param sidList
	 *            主键列表
	 * @param pageNo
	 *            第几页
	 * @param pageSize
	 *            一页几条
	 * @return
	 * @throws TRSException
	 */
	public Object favouriteTF( String sids,List<String> sidList, int pageNo, int pageSize, int totalItemCount,
							  int totalList, String keywords, String timeStart, String timeEnd) throws TRSException {
		// 收藏
//		List<String> sidFavourite = new ArrayList<>();
//		List<Favourites> favouritesList = favouritesRepository.findByUserIdAndSidIn(userId, sidList);
//		if (ObjectUtil.isNotEmpty(favouritesList)) {
//			for (Favourites faSidList : favouritesList) {
//				if (StringUtil.isEmpty(faSidList.getLibraryId())){//排除素材库里的数据
//					// sidFavourite装载了所有已收藏的文章sid
//					sidFavourite.add(faSidList.getSid());
//				}
//			}
//		}
		// 把已经预警的装里边
		//不查预警
		/*List<String> sidAlert = new ArrayList<>();
		List<AlertEntity> alertList = null;
		if (httpClient){
			alertList = AlertUtil.getAlerts(userId,sids,alertNetinsightUrl);
		}else {
			alertList = alertRepository.findByUserIdAndSidIn(userId, sidList);
		}
		 if (ObjectUtil.isNotEmpty(alertList)){
			 for (AlertEntity alert : alertList) {
				 sidAlert.add(alert.getSid());
			 }
		 }*/

		String TRSL = FavouritesUtil.buildSqlTF(sidList);
		if (TRSL == null) {
			return null;
		}
		QueryBuilder searchBuilder = new QueryBuilder();
		/*if (timeStart != null && timeEnd != null) {
			searchBuilder = DateUtil.timeBuilder(timeStart + ";" + timeEnd);
		}*/
		TRSL = toDealWithTime(TRSL,searchBuilder);
		searchBuilder.filterByTRSL(TRSL);
		if (StringUtil.isNotEmpty(keywords)) {
			String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":\"").append(keywords)
					.append("\" OR ").append(FtsFieldConst.FIELD_CONTENT).append(":\"").append(keywords).append("\"")
					.toString();
			searchBuilder.filterByTRSL(trsl);
		}
		//页码 固定
		searchBuilder.page(0, pageSize);
		try {
			List<FtsDocumentTF> ftsQuery = hybase8SearchService.ftsQuery(searchBuilder, FtsDocumentTF.class, false,
					false, false,null);
			List<FtsDocumentTF> result = new ArrayList<>();
			for (FtsDocumentTF chartAnalyze : ftsQuery) {
				String id = chartAnalyze.getHkey();
				// 预警数据
				//不查预警
				/*int indexOf = sidAlert.indexOf(id);
				if (indexOf < 0) {
					chartAnalyze.setSend(false);
				} else {
					chartAnalyze.setSend(true);
				}*/

				// 是否收藏信息
				int indexOfFa = sidList.indexOf(id);
				if (indexOfFa < 0) {
					chartAnalyze.setFavourite(false);
				} else {
					chartAnalyze.setFavourite(true);
				}

				// 我的收藏里边不用显示相似文章数了
				// 去掉img标签
				String content = chartAnalyze.getContent();
				if (StringUtil.isNotEmpty(content)) {
					content = StringUtil.replaceImg(content).replace("　　", "");// 过滤空格
					// img标签
				}
				chartAnalyze.setContent(content);
				result.add(chartAnalyze);
			}
			if (StringUtil.isNotEmpty(keywords)) {
				QueryBuilder countBuilder = new QueryBuilder();
				countBuilder.filterByTRSL(searchBuilder.asTRSL());
				countBuilder.page(0,totalItemCount);
				countBuilder.setDatabase(Const.HYBASE_OVERSEAS);
				long ftsCount = hybase8SearchService.ftsCount(countBuilder, false, false,false,null);
				totalItemCount = new Long(ftsCount).intValue();
				totalList = totalItemCount % pageSize > 0 ? totalItemCount / pageSize + 1 : totalItemCount / pageSize;
			}
			List<FtsDocumentTF> resultByTime = FavouritesUtil.resultByTimeTF(result, sidList);
			return new InfoListResult<>(resultByTime, totalItemCount, totalList);
		} catch (Exception e) {
			throw new OperationException("检索异常：message:" + e.getMessage(), e);
		}
	}

	/**
	 * 由于hybase搜索结果不按照查询条件的sid排列 而从mysql取出的市按照时间排列的 为了让结果按照时间排列
	 */
	@Override
	public List<ChartAnalyzeEntity> resultByTime(List<ChartAnalyzeEntity> result, List<String> sidList) {
		// 如果用同一个的话 会覆盖
		List<ChartAnalyzeEntity> resultByTime = new ArrayList<>();
		// 先把他撑开
		for (ChartAnalyzeEntity chartAnalyzeEntity : result) {
			resultByTime.add(chartAnalyzeEntity);
		}
		for (ChartAnalyzeEntity chartAnalyzeEntity : result) {
			int ind = sidList.indexOf(chartAnalyzeEntity.getSid());
			if (ind >= 0 && ind < result.size()) {
				resultByTime.set(ind, chartAnalyzeEntity);
			}
		}
		return resultByTime;
	}




	/**
	 * 加入收藏
	 *
	 * @param sids
	 *            需要收藏的文章ID
	 * @param userId
	 *            用户ID
	 * @return
	 */
	@Override
	@Transactional
	public String saveFavourites(String sids, String userId, String subGroupId,String md5, String groupName, String urltime) {
		if(RedisUtil.getString(UserUtils.getUser().getId()+"_"+sids)!=null) return "已经添加过了";
		//redis防止多次添加
		RedisUtil.setString(UserUtils.getUser().getId()+"_"+sids,sids,20,TimeUnit.SECONDS);
		String[] sidArry = sids.split(SEMICOLON);
		String[] groupNameArray = groupName.split(SEMICOLON);
		String[] timeArray = urltime.split(SEMICOLON);
		if (groupNameArray.length != sidArry.length) {
			return "fail";
		}
		User user = userRepository.findOne(userId);
		Favourites newAdd = null;
		List<Favourites> favouritesList = new ArrayList<Favourites>();
		QueryBuilder builder = null;
		try {
			builder= DateUtil.timeBuilder(urltime);
		}catch (Exception e) {
			e.printStackTrace();
		}
		String timeTrsl = builder.asTRSL();
		SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMdd);
		for (int i = 0; i < sidArry.length; i++) {
			//原生sql
			String sid = sidArry[i];
			Specification<Favourites> criteria = new Specification<Favourites>() {

				@Override
				public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					List<Object> predicates = new ArrayList<>();
					predicates.add(cb.equal(root.get("userId"), userId));
					predicates.add(cb.isNull(root.get("libraryId")));
					predicates.add(cb.equal(root.get("sid"), sid));
					Predicate[] pre = new Predicate[predicates.size()];

					return query.where(predicates.toArray(pre)).getRestriction();
				}
			};

			Favourites favourites = favouritesRepository.findOne(criteria);
			if (ObjectUtil.isEmpty(favourites)) {
				try {
					FtsDocumentCommonVO fav = null;
					QueryBuilder queryBuilder = new QueryBuilder();
					queryBuilder.filterByTRSL(timeTrsl);
					queryBuilder.page(0, 1);
					String equals = CommonListChartUtil.changeGroupName(groupNameArray[i]);
					if (Const.GROUPNAME_WEIXIN.equals(equals)) {
						queryBuilder.filterField(FtsFieldConst.FIELD_HKEY, sidArry[i], Operator.Equal);
					} else if (Const.GROUPNAME_WEIBO.equals(equals)) {
						queryBuilder.filterField(FtsFieldConst.FIELD_MID, sidArry[i], Operator.Equal);
					} else {
						queryBuilder.filterField(FtsFieldConst.FIELD_SID, sidArry[i], Operator.Equal);
					}
					InfoListResult infoListResult = commonListService.queryPageList(queryBuilder, false, false, false, groupNameArray[i], null, user, false);
					if (infoListResult.getContent() != null) {
						PagedList<FtsDocumentCommonVO> pagedList = (PagedList<FtsDocumentCommonVO>) infoListResult.getContent();
						if (pagedList.getPageItems() != null && pagedList.getPageItems().size() > 0) {
							fav = pagedList.getPageItems().get(0);
						}
					}
					if (fav != null) {
						newAdd = new Favourites(fav.getSid(),user.getId(),user.getSubGroupId());
						//查到的数据已经是处理过后的 - 对摘要、正文、sid
						String oneGroupName = CommonListChartUtil.changeGroupName(fav.getGroupName());
						newAdd.setGroupName(oneGroupName);
						// 去掉img标签
						String content = fav.getContent();
						if (StringUtil.isNotEmpty(content)) {
							content = StringUtil.filterEmoji(StringUtil.replaceImg(content));
						}
						newAdd.setContent(content);
						newAdd.setStatusContent(content);
						String title = fav.getTitle();
						if (StringUtil.isNotEmpty(title)) {
							title = StringUtil.cutContent(StringUtil.filterEmoji(StringUtil.replaceImg(title)),100);

						}
						newAdd.setTitle(title);
						newAdd.setUrlTitle(title);
						String abstracts = fav.getAbstracts();
						if (StringUtil.isNotEmpty(abstracts)) {
							abstracts = StringUtil.filterEmoji(StringUtil.replaceImg(abstracts));
						}
						newAdd.setAbstracts(abstracts);
						String fullContent = fav.getExportContent();
						if (StringUtil.isNotEmpty(fullContent)) {
							fullContent = StringUtil.filterEmoji(StringUtil.replaceImg(fullContent)).replaceAll("[\ud800\udc00-\udbff\udfff\ud800-\udfff]", "");
						}
						newAdd.setFullContent(fullContent);
						newAdd.setHkey(null);
						if (Const.GROUPNAME_LUNTAN.equals(oneGroupName)){
							newAdd.setHkey(fav.getHkey());
						}
						newAdd.setMdsTag(fav.getMd5Tag());
						newAdd.setUrlName(fav.getUrlName());
						newAdd.setUrlTime(fav.getUrlTime());
						newAdd.setUrltime(sdf.format(fav.getUrlTime()));
						newAdd.setUrlDate(fav.getUrlDate());
						newAdd.setSiteName(fav.getSiteName());
						newAdd.setNreserved1(fav.getNreserved1());
						newAdd.setRetweetedMid(fav.getRetweetedMid());
						newAdd.setCommtCount(fav.getCommtCount());
						newAdd.setRttCount(fav.getRttCount());
						newAdd.setCreatedAt(fav.getCreatedAt());
						newAdd.setAppraise(StringUtil.isEmpty(fav.getAppraise()) ? "中性":fav.getAppraise());
						newAdd.setKeywords(fav.getKeywords());
						newAdd.setChannel(fav.getChannel());
						if(Const.PAGE_SHOW_WEIXIN.equals(groupName)){
							newAdd.setImgSrc(null);
						}else{
							newAdd.setImgSrc(fav.getImgSrc());
						}

						String authors = fav.getAuthors();
						newAdd.setAuthors(StringUtil.filterEmoji(StringUtil.replaceImg(authors)));
						newAdd.setAuthor(StringUtil.filterEmoji(StringUtil.replaceImg(authors)));
						String siteName = fav.getSiteName();
						newAdd.setSiteName(StringUtil.filterEmoji(StringUtil.replaceImg(siteName)));
						String screenName = fav.getScreenName();
						String srcName = fav.getSrcName();

						if (Const.GROUPNAME_WEIBO.equals(oneGroupName)) {
							newAdd.setTitle(content);
							newAdd.setUrlTitle(content);
							srcName = fav.getRetweetedScreenName();
							newAdd.setAbstracts(content);
						}else if (Const.GROUPNAME_FACEBOOK.equals(oneGroupName) || Const.GROUPNAME_TWITTER.equals(oneGroupName)) {
							newAdd.setTitle(content);
							newAdd.setUrlTitle(content);
							screenName = fav.getAuthors();
							srcName = fav.getRetweetedScreenName();
							newAdd.setAbstracts(content);
						}else if(Const.GROUPNAME_DUANSHIPIN.equals(oneGroupName) || Const.GROUPNAME_CHANGSHIPIN.equals(oneGroupName)){
							if(StringUtil.isEmpty(title)){
								newAdd.setTitle(content);
								newAdd.setUrlTitle(content);
							}
							newAdd.setAbstracts(content);
							newAdd.setImgSrc(fav.getImgUrl());
						}
						screenName =  StringUtil.filterEmoji(StringUtil.replaceImg(screenName));
						newAdd.setScreenName(screenName);
						srcName =  StringUtil.filterEmoji(StringUtil.replaceImg(srcName));
						newAdd.setSrcName(srcName);
						favouritesList.add(newAdd);
					}

				} catch (TRSException e) {
					e.printStackTrace();
				}
			}
		}
		if (favouritesList.size() > 0) {
			favouritesRepository.save(favouritesList);
		}
		return Const.SUCCESS;
	}
	public String subString(String str){
		if (StringUtil.isNotEmpty(str) && str.length() > 200) {
				str = str.substring(0, 200);
		}
		if (StringUtil.isEmpty(str)) str="";
		return str;
	}
	/**
	 * 检索hybase收藏信息
	 *
	 * @param sidList
	 *            主键列表
	 * @param pageNo
	 *            第几页
	 * @param pageSize
	 *            一页几条
	 *
	 * @return
	 * @throws TRSException
	 */
	public Object favouriteHybase( String sids,List<String> sidList, String groupName,int pageNo,
								int pageSize) throws TRSException {
		String TRSL = "";
		for(String s:sidList){
			List<String> list = new ArrayList<>();
			if ("国内微信".equals(groupName)){//如果是微信的话
				list.add(s);
				TRSL += FavouritesUtil.buildSqlWeiXin(list)+" OR ";
			}else {
				list.add(s);
				TRSL += FavouritesUtil.buildSql(list)+" OR  ";
			}
		}

		if(TRSL.trim().length()>2){
			TRSL = TRSL.substring(0,TRSL.trim().length()-2);
		}
		if (TRSL == null) {
			return null;
		}
		QueryBuilder searchBuilder = new QueryBuilder();
		TRSL = toDealWithTime(TRSL,searchBuilder);

		searchBuilder.filterByTRSL(TRSL);
		//页码  固定
		searchBuilder.page(0,pageSize);
		try {
			System.err.println("历史数据表达式："+searchBuilder.asTRSL());
//			List<ChartAnalyzeEntity> ftsQuery = hybase8SearchService.ftsQuery(searchBuilder, ChartAnalyzeEntity.class,
//					false,false);
			//少时间
			List<FtsDocumentCommonVO> ftsQuery = hybase8SearchService.ftsQuery(searchBuilder, FtsDocumentCommonVO.class, false,false,false,null);
			List<FtsDocumentCommonVO> result = new ArrayList<>();
			for (FtsDocumentCommonVO chartAnalyze : ftsQuery) {
				chartAnalyze.setFavourite(true);
				// 去掉img标签
				String content = chartAnalyze.getContent();
				if (StringUtil.isNotEmpty(content)) {
					content = StringUtil.replaceImg(content);
				}
				chartAnalyze.setContent(content);
				result.add(chartAnalyze);
			}
			return result;

		} catch (Exception e) {
			throw new OperationException("检索异常：message:" + e);
		}
	}
	/**
	 * 取消收藏
	 *
	 * @param sids
	 *            需要删除的id
	 * @param userId
	 *            用户id
	 * @return
	 */
	@Override
	public String delFavourites(String sids, String userId) {
		String[] sidArry = sids.split(SEMICOLON);
//		Criteria<Favourites> criteria = new Criteria<Favourites>();
//		criteria.add(Restrictions.in("sid", Arrays.asList(sidArry)));
//		criteria.add(Restrictions.eq("userId", userId));
//		criteria.add(Restrictions.eq("libraryId",""));
//		favouritesRepository.delete(favouritesRepository.findAll(criteria));
		User loginUser = UserUtils.getUser();

		//原生sql
		Specification<Favourites> criteria = new Specification<Favourites>() {
			@Override
			public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

				List<Predicate> predicates = new ArrayList<>();
				if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
					predicates.add(cb.equal(root.get("userId"),loginUser.getId()));
				}else {
					predicates.add(cb.equal(root.get("subGroupId"),loginUser.getSubGroupId()));
				}
				predicates.add(cb.isNull(root.get("libraryId")));

				CriteriaBuilder.In<Object> sid = cb.in(root.get("sid"));
				List<String> sids = Arrays.asList(sidArry);
				if (ObjectUtil.isNotEmpty(sids)){
					for (String str : sids) {
						if (StringUtil.isNotEmpty(str)){
							sid.value(str);
						}
					}
					predicates.add(sid);
				}

				Predicate[] pre = new Predicate[predicates.size()];
				return query.where(predicates.toArray(pre)).getRestriction();
			}
		};

//		List<Favourites> findAll = favouritesRepository.findAll(criteria,new Sort(
//				Sort.Direction.DESC, "urltime"));
		List<Favourites> findAll = favouritesRepository.findAll(criteria);
		favouritesRepository.delete(findAll);
		return Const.SUCCESS;
	}

	@Override
	public String changeToken() {
		List<AppApiAccessToken> list = iAppAccessTokenRepository.findAll();
		for (AppApiAccessToken token : list){
			if (StringUtil.isEmpty(token.getSubGroupId())){
				token.setSubGroupId(userRepository.findOne(token.getGrantSourceOwnerId()).getSubGroupId());
				iAppAccessTokenRepository.save(token);
			}
		}
		return "success";
	}

	/**
	 * 用来修改历史收藏 ，只执行这一次
	 * @return
	 */
	@Override
	public String changeHistoryFavourites() {
		Specification<Favourites> criteria = new Specification<Favourites>() {

			@Override
			public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Object> predicates = new ArrayList<>();
				predicates.add(cb.isNull(root.get("libraryId")));
				Predicate[] pre = new Predicate[predicates.size()];

				return query.where(predicates.toArray(pre)).getRestriction();
			}
		};

		List<Favourites> favouritesList = favouritesRepository.findAll(criteria);
		for(Favourites favourites : favouritesList){
			if (ObjectUtil.isNotEmpty(favourites)) {
				List<String> sidList = new ArrayList<>();
				sidList.add(favourites.getSid());
				List<FtsDocumentCommonVO> listF = new ArrayList<>();

				try {
					listF = (List<FtsDocumentCommonVO>) favouriteHybase(favourites.getSid(), sidList, favourites.getGroupName(), 0, 1);
					if (listF.size() > 0) {
						favourites.setTitle(subString(listF.get(0).getTitle()));
						favourites.setContent(subString(listF.get(0).getContent()));
						favourites.setUrlName(listF.get(0).getUrlName());
						favourites.setUrlDate(listF.get(0).getUrlDate());
						favourites.setAuthors(StringUtil.removeFourChar(listF.get(0).getAuthors()));
						favourites.setSiteName(listF.get(0).getSiteName());
						favourites.setNreserved1(listF.get(0).getNreserved1());
						favourites.setAbstracts(subString(listF.get(0).getAbstracts()));
						favourites.setCommtCount(listF.get(0).getCommtCount());
						favourites.setSrcName(listF.get(0).getSrcName());
						favourites.setScreenName(listF.get(0).getScreenName());
						favourites.setRttCount(listF.get(0).getRttCount());
						favourites.setCreatedAt(listF.get(0).getCreatedAt());
						favourites.setStatusContent(subString(listF.get(0).getStatusContent()));
						favourites.setUrlTitle(subString(listF.get(0).getUrlTitle()));
						favourites.setRetweetedMid(listF.get(0).getRetweetedMid());
						favourites.setMdsTag(listF.get(0).getMd5Tag());
						favourites.setUrlTime(listF.get(0).getUrlTime());
						favouritesRepository.save(favourites);
					} else {
						favouritesRepository.delete(favourites);
					}

				} catch (TRSException e) {
					e.printStackTrace();
				}

			}
		}
//		if (favouritesList.size() > 0) {
//			favouritesRepository.save(favouritesList);
//		}
		return Const.SUCCESS;
	}

	@Override
	public String changeHistoryFavouritesGroupName() {
		Specification<Favourites> criteria = new Specification<Favourites>() {

			@Override
			public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Object> predicates = new ArrayList<>();
				predicates.add(cb.isNull(root.get("libraryId")));
				Predicate[] pre = new Predicate[predicates.size()];
				return query.where(predicates.toArray(pre)).getRestriction();
			}
		};

		List<Favourites> favouritesList = favouritesRepository.findAll(criteria);

		for(Favourites fav : favouritesList){
			if(Const.SOURCE_GROUPNAME_CONTRAST.containsKey(fav.getGroupName())){
				String groupName = Const.SOURCE_GROUPNAME_CONTRAST.get(fav.getGroupName());
				fav.setGroupName(groupName);
			}
		}
		favouritesRepository.save(favouritesList);
		return Const.SUCCESS;
	}

	/**
	 * 新增素材库
	 *
	 * @param materialLibrary
	 * @return
	 */
	@Override
	public Object saveMaterialLibrary(MaterialLibrary materialLibrary) {
		QueryBuilder searchBuilder = materialLibrary.toBuilder(0, 20);
		materialLibrary.setRealTrsl(searchBuilder.asTRSL());
		return materialLibraryRepository.save(materialLibrary);
	}

	@Override
	public void saveBatchList(List<MaterialLibrary> libraryList) {
		if (ObjectUtil.isNotEmpty(libraryList)) {
			materialLibraryRepository.save(libraryList);
		}
	}

	@Override
	public void delBatchList(List<MaterialLibrary> libraryList) {
		if (ObjectUtil.isNotEmpty(libraryList)) {
			// 可以删除整个list 不必要id循环
			materialLibraryRepository.delete(libraryList);
		}
	}

	/**
	 * 对专项监测过来的数据，新增或者修改
	 *
	 * @param specialProject
	 */
	@Override
	public void saveMaterialLibrary(SpecialProject specialProject) {
		log.info("接收到专项监测的信息,ID为:" + specialProject.getId());
		String specId = specialProject.getId();
		List<MaterialLibrary> libraryList;
		List<MaterialLibrary> speLibraryList;
		Criteria<MaterialLibrary> criteria = new Criteria<MaterialLibrary>();
		criteria.add(Restrictions.eq("specialId", specId));
		// 查询已指定素材库的
		criteria.add(Restrictions.eq("status", BUILDED));
		// 查询该专项检查是否加载素材
		libraryList = materialLibraryRepository.findAll(criteria);
		speLibraryList = materialLibraryRepository.findBySpecialId(specId);
		MaterialLibrary materialLibrary;
		String message = "";
		// 如果没有指定素材库 就进行添加或修改操作 否则不进行
		if (ObjectUtil.isEmpty(libraryList)) {
			if (ObjectUtil.isEmpty(speLibraryList)) {
				materialLibrary = new MaterialLibrary();
				message = "从专项检查新增至素材库";
			} else {
				materialLibrary = speLibraryList.get(0);
				message = "从专项检查修改并同步至素材库";
			}
			materialLibrary.setSpecialId(specId);
			materialLibrary.setLibraryName(specialProject.getSpecialName());
			materialLibrary.setAllKeyword(specialProject.getAllKeywords());
			materialLibrary.setAnyKeyword(specialProject.getAnyKeywords());
			materialLibrary.setExcludeKeyword(specialProject.getExcludeWords());
			materialLibrary.setKeywordsLocation(specialProject.getSearchScope());
			materialLibrary.setMode(specialProject.getSpecialType().ordinal());
			materialLibrary.setExpression(specialProject.getTrsl());
			materialLibrary.setWeiboExpression(specialProject.getStatusTrsl());
			materialLibrary.setWeixinExpression(specialProject.getWeChatTrsl());
			materialLibrary.setSource(specSourec);
			materialLibrary.setGroupName(specialProject.getSource());
			materialLibrary.setUserId(specialProject.getUserId());
			materialLibrary.setSearchBeginTime(specialProject.getStartTime());
			materialLibrary.setSearchEndTime(specialProject.getEndTime());
			this.saveMaterialLibrary(materialLibrary);
			log.info(message + "成功！");
		}
	}

	/**
	 * 获取当前用户所有的素材库
	 *
	 * @param userId
	 *            用户id
	 * @param pageNo
	 *            页码
	 * @param pageSize
	 *            每页大小
	 * @return
	 * @throws TRSException
	 */
	@Override
	public Object getUserLibrary(String userId, int pageNo, int pageSize) throws TRSException {
		log.info("获取当前用户素材库");
		Page<MaterialLibrary> libraryList;
		libraryList = materialLibraryRepository.findByUserId(userId,
				new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, "lastModifiedTime")));
		ObjectUtil.assertNull(libraryList, "素材库列表");

		return libraryList;
	}

	@Override
	public Object preview(MaterialLibrary materialLibrary, String groupame) throws TRSException {
		if (materialLibrary.getSearchBeginTime().after(materialLibrary.getSearchEndTime())) {
			return null;
		}
		QueryBuilder builder = null;
		if (Const.MEDIA_TYPE_WEIXIN.contains(groupame)) {
			builder = materialLibrary.toBuilderWeiXin(0, 15);
			builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			log.info("trsl:{}", builder.asTRSL());
			try {
				List<FtsDocumentWeChat> ftsQuery = hybase8SearchService.ftsQuery(builder, FtsDocumentWeChat.class,
						false, false,false,null);
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();
				for (FtsDocumentWeChat ftsDocumentWeChat : ftsQuery) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("title", ftsDocumentWeChat.getUrlTitle());
					list.add(map);
				}
				return list;
			} catch (Exception e) {
				throw new OperationException("检索异常：message:" + e);
			}
		} else if (Const.MEDIA_TYPE_WEIBO.contains(groupame)) {
			builder = materialLibrary.toBuilderWeiBo(0, 15);
			builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			log.info("trsl:{}", builder.asTRSL());
			try {
				List<FtsDocumentStatus> ftsQuery = hybase8SearchService.ftsQuery(builder, FtsDocumentStatus.class,
						false, false,false,null);
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();
				for (FtsDocumentStatus ftsDocumentWeChat : ftsQuery) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("title", ftsDocumentWeChat.getStatusContent());
					list.add(map);
				}
				return list;
			} catch (Exception e) {
				throw new OperationException("检索异常：message:" + e);
			}
		} else if (Const.MEDIA_TYPE_NEWS.contains(groupame)) {
			builder = materialLibrary.toBuilder(0, 15);
			// 单选状态
			if ("国内新闻".equals(groupame)) {
				// String trsl = new
				// StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻
				// ").toString();
				String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 NOT ")
						.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
				builder.filterByTRSL(trsl);
			} else if ("国内论坛".equals(groupame)) {
				String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 OR ")
						.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
				builder.filterByTRSL(trsl);
			} else {
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupame, Operator.Equal);
			}
			builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
			// builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, false);
			log.info("trsl:{}", builder.asTRSL());
			try {
				List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(builder, FtsDocument.class, true, false,false,null);
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();
				for (FtsDocument ftsDocumentWeChat : ftsQuery) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("title", ftsDocumentWeChat.getTitle());
					list.add(map);
				}
				return list;
			} catch (Exception e) {
				throw new OperationException("检索异常：message:" + e);
			}
		}
		return null;
	}

	/**
	 * 获取所有的素材库
	 *
	 * @return
	 */
	@Override
	public List<MaterialLibrary> getAllLibrary() {
		log.info("获取素材库列表");
		return materialLibraryRepository.findAll();
	}

	/**
	 * 获取单个素材库
	 *
	 * @param libraryId
	 * @return
	 */
	@Override
	public MaterialLibrary getOneLibrary(String libraryId) {
		return materialLibraryRepository.findOne(libraryId);
	}

	/**
	 * 获取该用户的所有报告
	 *
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@Override
	public List<Report> getAllReport(User user, int pageNo, int pageSize) {
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			List<Report> findByUserId = reportRepository.findByUserId(user.getId(),
					new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, "lastModifiedTime")));
			return findByUserId;
		}else {
			List<Report> findBySubGroupId = reportRepository.findByUserId(user.getSubGroupId(),
					new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, "lastModifiedTime")));
			return findBySubGroupId;
		}

	}

	/**
	 * 获取单个报告
	 *
	 * @return
	 */
	@Override
	public Report getOneReport(String reportId) {
		return reportRepository.findOne(reportId);
	}

	/**
	 * 模糊查询报告
	 *
	 * @param reportName
	 * @return
	 */
	@Override
	public List<Report> seachReport(User user, String reportName) {
		Criteria<Report> criteria = new Criteria<Report>();
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			criteria.add(Restrictions.eq("userId", user.getId()));
		}else {
			criteria.add(Restrictions.eq("subGroupId", user.getSubGroupId()));
		}
		if (StringUtil.isNotEmpty(reportName)) {
			criteria.add(Restrictions.like("reportName", reportName, MatchMode.ANYWHERE));
		}
		return reportRepository.findAll(criteria);
	}

	@Override
	public List<MaterialLibrary> seachLib(User user, String libName) {
		Criteria<MaterialLibrary> criteria = new Criteria<MaterialLibrary>();
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			criteria.add(Restrictions.eq("userId", user.getId()));
		}else {
			criteria.add(Restrictions.eq("subGroupId", user.getSubGroupId()));
		}
		if (StringUtil.isNotEmpty(libName)) {
			criteria.add(Restrictions.like("libraryName", libName, MatchMode.ANYWHERE));
		}
		return materialLibraryRepository.findAll(criteria);
	}

	@Override
	public void saveReport(Report report) {
		reportRepository.save(report);
	}

	/**
	 * 删除报告
	 *
	 * @param reportId
	 * @return
	 */
	@Override
	public Object deleteReport(String reportId) {
		reportRepository.delete(reportId);
		return Const.SUCCESS;
	}

	@Override
	public Template saveOrupdateTep(Template templat) {
		Template template = templateRepository.save(templat);
		return template;
	}

	@Override
	public Template getOneTemplate(String templateId, String templateList) throws Exception {
		Template template = templateRepository.findOne(templateId);
		template.setTemplateList(templateList);
		return this.saveOrupdateTep(template);
	}

	@Override
	public Object getTemplateList(String userId, String libraryId, int pageNo, int pageSize) throws Exception {
		getReportMaterialSql(libraryId);
		List<Template> templateList = templateRepository.findByUserId(userId,
				new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, "lastModifiedTime")));
		return templateList;
	}

	@Override
	public Object getTemplateNoLib(String userId, int pageNo, int pageSize) throws Exception {
		return templateRepository.findByUserId(userId,
				new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, "lastModifiedTime")));
	}

	@Override
	public Object saveFavMaterial(String sid, String libraryId, String operate, String groupName) throws Exception {
		String[] sidArry = sid.split(SEMICOLON);
		ReportMaterial reportMaterial;
		List<ReportMaterial> ReportMaterialList = new ArrayList<ReportMaterial>();
		for (String item : sidArry) {
			reportMaterial = new ReportMaterial();
			reportMaterial.setSid(item);
			reportMaterial.setLibraryId(libraryId);
			reportMaterial.setStatus(operate);
			reportMaterial.setGroupName(groupName);
			ReportMaterialList.add(reportMaterial);
		}
		if (ObjectUtil.isNotEmpty(ReportMaterialList)) {
			reportMaterialRepository.save(ReportMaterialList);
		}
		return Const.SUCCESS;
	}

	/**
	 * 构建出报告模板
	 *
	 * @param eleList
	 * @param libraryId
	 * @param template
	 * @return
	 * @throws OperationException
	 */
	@Override
	public String analyTemplateList(List<TElement> eleList, String libraryId, Template template, String reportName)
			throws OperationException {
		// 允许自定义相同的维度
		List<Map<String, TElement>> dataList = new ArrayList<Map<String, TElement>>();
		Map<String, TElement> dataMap;
		for (TElement action : eleList) {
			dataMap = new HashMap<String, TElement>();
			dataMap.put(action.getId(), action);
			dataList.add(dataMap);
		}
		Report report = new Report();
		report.setUserId(template.getUserId());
		report.setLibraryId(libraryId);
		report.setReportName(reportName);
		report.setTemplateId(template.getId());
		report.setStatus(ANALYSIS);
		report.setCreatedTime(new Date());
		reportRepository.save(report);
		MaterialLibrary materialLibrary = materialLibraryRepository.findOne(libraryId);
		// 单拿出一个线程算数据
		fixedThreadPool.execute(new BuildReportTask(materialLibrary, dataList, report));
		return Const.SUCCESS;
	}

	@Override
	public void saveReportData(ReportData reportData) {
		reportDataRepository.save(reportData);
	}

	/**
	 * 得到生成报告所使用的sql 拼接来着：素材库real_sql , 报告素材库中的sid ， 如果为收藏则加，删除这去除。
	 *
	 * @author songbinbin 2017年5月11日 String
	 * @param libraryId
	 * @return
	 */
	@Override
	public String getReportMaterialSql(String libraryId) {
		List<ReportMaterial> reportMaterials;
		MaterialLibrary materialLibrary;
		materialLibrary = materialLibraryRepository.findOne(libraryId);
		String materialSql = materialLibrary.getRealTrsl();
		// 根据libraryId 和Status 得到要增加的sid
		Criteria<ReportMaterial> addCriteria = new Criteria<ReportMaterial>();
		addCriteria.add(Restrictions.eq("libraryId", libraryId));
		addCriteria.add(Restrictions.eq("status", "1"));
		reportMaterials = reportMaterialRepository.findAll(addCriteria);
		List<String> sidList = new ArrayList<>();
		reportMaterials.forEach(sid -> {
			sidList.add(sid.getSid());
		});
		String addSidsql = FavouritesUtil.buildSql(sidList);
		// 根据libraryId 和Status 得到要减少的sid
		Criteria<ReportMaterial> delCriteria = new Criteria<ReportMaterial>();
		delCriteria.add(Restrictions.eq("libraryId", libraryId));
		delCriteria.add(Restrictions.eq("status", "0"));
		reportMaterials = reportMaterialRepository.findAll(delCriteria);
		List<String> delList = new ArrayList<>();
		reportMaterials.forEach(sid -> {
			delList.add(sid.getSid());
		});
		String delSidsql = FavouritesUtil.buildSql(delList);
		// 整合sql
		String reportSql = materialSql + " OR " + addSidsql + " NOT "
				+ delSidsql;
		materialLibrary.setRealTrsl(reportSql);
		materialLibrary.setStatus(BUILDED);
		materialLibraryRepository.save(materialLibrary);
		return reportSql;
	}

	@Override
	public boolean mergeReport(String reportId) {
		log.error("报告ID：" + reportId);
		Report report = reportRepository.findOne(reportId);
		List<ReportVeidoo> list = reportVeidooRepository.findByReportId(reportId, new Sort(Sort.Direction.ASC, "sort"));
		List<String> subReportPath = new ArrayList<String>();
		for (ReportVeidoo item : list) {
			subReportPath.add(item.getVeidooPath());
		}
		File newfile = new File(produceReport);
		if (!newfile.exists()) {
			newfile.mkdirs();
		}
		String reportPath = produceReport + BIAS + report.getReportName() + "_" + System.currentTimeMillis() + DOCX;
		if (WordUtil.getInstance().mergeDocx(subReportPath, reportPath)) {
			report.setDocFilePath(reportPath);
			File file = new File(reportPath);
			report.setDocFileSize(GetFileSize(file));
			report.setBulidRat(100.0);
			reportRepository.save(report);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mergeReport(Report report) {
		List<ReportVeidoo> list = reportVeidooRepository.findByReportId(report.getId(),
				new Sort(Sort.Direction.ASC, "sort"));
		List<String> subReportPath = new ArrayList<String>();
		for (ReportVeidoo item : list) {
			subReportPath.add(item.getVeidooPath());
		}
		File newfile = new File(produceReport);
		if (!newfile.exists()) {
			newfile.mkdirs();
		}
		String reportPath = produceReport + BIAS + report.getReportName() + "_" + System.currentTimeMillis() + DOCX;
		if (WordUtil.getInstance().mergeDocx(subReportPath, reportPath)) {
			report.setDocFilePath(reportPath);
			File file = new File(reportPath);
			report.setDocFileSize(GetFileSize(file));
			report.setBulidRat(100.0);
			reportRepository.save(report);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取文件大小
	 *
	 * @param file
	 * @return
	 */
	public static String GetFileSize(File file) {
		String size = "";
		if (file.exists() && file.isFile()) {
			long fileS = file.length();
			DecimalFormat df = new DecimalFormat("#.00");
			if (fileS < CODE) {
				size = df.format((double) fileS) + "BT";
			} else if (fileS < CODE * CODE) {
				size = df.format((double) fileS / CODE) + "KB";
			} else if (fileS < CODE * CODE * CODE) {
				size = df.format((double) fileS / CODE * CODE) + "MB";
			} else {
				size = df.format((double) fileS / CODE * CODE * CODE) + "GB";
			}
		} else if (file.exists() && file.isDirectory()) {
			size = "";
		} else {
			size = "0BT";
		}
		return size;
	}

	@Override
	public void bulidReportDoc(Report report, ReportData reportData2) throws Exception {
		// 得到所有的数据，分析有哪些类型图表, 报告简介和监测概述文字描述还有需求不确定，先不考虑
		// 先生成不需要前端画图的维度，标好序号，存入数据库，比如报顶部
		ReportData reportData = reportDataRepository.findOne(report.getDataId());
		String reportId = report.getId();
		deleteOldViedoo(reportId);
		String reportTopData = reportData.getReportTopData();// 报告标题
		String reportIntroData = reportData.getReportIntro();// 报告简介
		String monitorSumData = reportData.getMonitorSummarize();// 监测概述
		String hottestData = reportData.getHottestData();// 最热新闻列表
		String newestData = reportData.getNewestData();// 最新新闻列表
		if (StringUtil.isNotEmpty(reportTopData)) {
			produceTop(reportTopData, reportId);
		}
		if (StringUtil.isNotEmpty(reportIntroData)) {
			produceIntro(reportIntroData, reportId);
		}
		if (StringUtil.isNotEmpty(monitorSumData)) {
			produceMonitor(monitorSumData, reportId);
		}
		if (StringUtil.isNotEmpty(hottestData)) {
			produceHottest(hottestData, reportId);
		}
		if (StringUtil.isNotEmpty(newestData)) {
			produceNewest(newestData, reportId);
		}
		// 再生成需要画图的数据以及路径给前端画图,热点数据暂时不做处理
		String allImageData = "";
		ArrayList<String> prePhantomjsData = new ArrayList<String>();
		String areaImageData = reportData.getArealDistributionData();
		if (StringUtil.isNotEmpty(areaImageData)) {
			prePhantomjsData.add(areaImageData);
		}
		String sourceTypeData = reportData.getSourceTypeData();
		if (StringUtil.isNotEmpty(sourceTypeData)) {
			prePhantomjsData.add(sourceTypeData);
		}
		String mediaActiveData = reportData.getMediaActivityData();
		if (StringUtil.isNotEmpty(mediaActiveData)) {
			prePhantomjsData.add(mediaActiveData);
		}
		String mediaDiffuseData = reportData.getMediaDiffuseData();
		if (StringUtil.isNotEmpty(mediaDiffuseData)) {
			prePhantomjsData.add(mediaDiffuseData);
		}
		String emotionAnalData = reportData.getEmotionAnalysis();
		if (StringUtil.isNotEmpty(emotionAnalData)) {
			prePhantomjsData.add(emotionAnalData);
		}
		String hotwordData = reportData.getHotWordData();
		if (StringUtil.isNotEmpty(hotwordData)) {
			prePhantomjsData.add(hotwordData);
		}
		String hotplaceData = reportData.getHotPlaceData();
		if (StringUtil.isNotEmpty(hotplaceData)) {
			prePhantomjsData.add(hotplaceData);
		}
		String hotorganData = reportData.getHotOrganData();
		if (StringUtil.isNotEmpty(hotorganData)) {
			prePhantomjsData.add(hotorganData);
		}
		String hotnameData = reportData.getHotNameData();
		if (StringUtil.isNotEmpty(hotnameData)) {
			prePhantomjsData.add(hotnameData);
		}
		String volumeTrendData = reportData.getVolumeData();
		if (StringUtil.isNotEmpty(volumeTrendData)) {
			prePhantomjsData.add(volumeTrendData);
		}
		String boomPointData = reportData.getBoomData();
		if (StringUtil.isNotEmpty(boomPointData)) {
			prePhantomjsData.add(boomPointData);
		}
		String exponentData = reportData.getExponentData();
		if (StringUtil.isNotEmpty(exponentData)) {
			prePhantomjsData.add(exponentData);
		}
		// 调用phantomjs 生产图片。
		String[] produceImage = PhantomjsFactory.getInstance().produceImage(prePhantomjsData);
		// 生成图片
		preDrawImage(produceImage, report.getId());
		// 合并报告
		mergeReport(report);
	}

	@Override
	public Object bulidReportDoc(Report report) throws Exception {
		// 得到所有的数据，分析有哪些类型图表, 报告简介和监测概述文字描述还有需求不确定，先不考虑
		// 先生成不需要前端画图的维度，标好序号，存入数据库，比如报顶部
		String reportId = report.getId();
		deleteOldViedoo(reportId);
		ReportData reportData = reportDataRepository.findOne(report.getDataId());
		String reportTopData = reportData.getReportTopData();// 报告标题
		String reportIntroData = reportData.getReportIntro();// 报告简介
		String monitorSumData = reportData.getMonitorSummarize();// 监测概述
		String hottestData = reportData.getHottestData();// 最热新闻列表
		String newestData = reportData.getNewestData();// 最新新闻列表
		if (StringUtil.isNotEmpty(reportTopData)) {
			produceTop(reportTopData, reportId);
		}
		if (StringUtil.isNotEmpty(reportIntroData)) {
			produceIntro(reportIntroData, reportId);
		}
		if (StringUtil.isNotEmpty(monitorSumData)) {
			produceMonitor(monitorSumData, reportId);
		}
		if (StringUtil.isNotEmpty(hottestData)) {
			produceHottest(hottestData, reportId);
		}
		if (StringUtil.isNotEmpty(newestData)) {
			produceNewest(newestData, reportId);
		}
		// 再生成需要画图的数据以及路径给前端画图,热点数据暂时不做处理
		String allImageData = "";
		String areaImageData = reportData.getArealDistributionData();
		if (StringUtil.isNotEmpty(areaImageData)) {
			allImageData += Const.TRS_SEPARATOR + areaImageData;
		}
		String sourceTypeData = reportData.getSourceTypeData();
		if (StringUtil.isNotEmpty(sourceTypeData)) {
			allImageData += Const.TRS_SEPARATOR + sourceTypeData;
		}
		String mediaActiveData = reportData.getMediaActivityData();
		if (StringUtil.isNotEmpty(mediaActiveData)) {
			allImageData += Const.TRS_SEPARATOR + mediaActiveData;
		}
		String mediaDiffuseData = reportData.getMediaDiffuseData();
		if (StringUtil.isNotEmpty(mediaDiffuseData)) {
			allImageData += Const.TRS_SEPARATOR + mediaDiffuseData;
		}
		String emotionAnalData = reportData.getEmotionAnalysis();
		if (StringUtil.isNotEmpty(emotionAnalData)) {
			allImageData += Const.TRS_SEPARATOR + emotionAnalData;
		}
		String hotwordData = reportData.getHotWordData();
		if (StringUtil.isNotEmpty(hotwordData)) {
			allImageData += Const.TRS_SEPARATOR + hotwordData;
		}
		String hotplaceData = reportData.getHotPlaceData();
		if (StringUtil.isNotEmpty(hotplaceData)) {
			allImageData += Const.TRS_SEPARATOR + hotplaceData;
		}
		String hotorganData = reportData.getHotOrganData();
		if (StringUtil.isNotEmpty(hotorganData)) {
			allImageData += Const.TRS_SEPARATOR + hotorganData;
		}
		String hotnameData = reportData.getHotNameData();
		if (StringUtil.isNotEmpty(hotnameData)) {
			allImageData += Const.TRS_SEPARATOR + hotnameData;
		}
		String volumeTrendData = reportData.getVolumeData();
		if (StringUtil.isNotEmpty(volumeTrendData)) {
			allImageData += Const.TRS_SEPARATOR + volumeTrendData;
		}
		String boomPointData = reportData.getBoomData();
		if (StringUtil.isNotEmpty(boomPointData)) {
			allImageData += Const.TRS_SEPARATOR + boomPointData;
		}
		String exponentData = reportData.getExponentData();
		if (StringUtil.isNotEmpty(exponentData)) {
			allImageData += Const.TRS_SEPARATOR + exponentData;
		}
		if (StringUtil.isNotEmpty(allImageData)) {
			return produceAllImage(allImageData);
		} else {
			log.info("暂无图片数据处理！");
			return null;
		}
	}

	/**
	 * 用来清除重复创建的报告维度
	 *
	 * @param reportId
	 */
	private void deleteOldViedoo(String reportId) {
		List<ReportVeidoo> list = reportVeidooRepository.findByReportId(reportId, new Sort(Sort.Direction.ASC, "sort"));
		List<String> subReportPath = new ArrayList<String>();
		for (ReportVeidoo item : list) {
			subReportPath.add(item.getVeidooPath());
		}
		if (subReportPath.size() > 0) {
			if (WordUtil.getInstance().deleteFiles(subReportPath)) {
				reportVeidooRepository.delete(list);
			}
			;
		}
	}

	/**
	 * 处理最新新闻数据，生成文档
	 *
	 * @param newestData
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void produceNewest(String newestData, String reportId) {
		try {
			String[] newsArry = newestData.split(Const.TRS_SEPARATOR);
			Map<String, Object> datas;
			Map<String, Object> newsMap;
			ReportVeidoo veidoo;
			WordData data;
			WordData innerData;
			List<WordData> dataCList;
			for (String newsData : newsArry) {
				datas = ObjectUtil.toObject(newsData, Map.class);
				newsMap = ObjectUtil.toObject(ObjectUtil.toJson(datas.get("data")), Map.class);
				String newsTitle = String.valueOf(newsMap.get("newsTitle"));
				List<String> titleList = (List<String>) newsMap.get("titleList");
				List<String> timeList = (List<String>) newsMap.get("timeList");
				int position = (Integer) datas.get("position");
				String templteName = Const.pathMap.get(Const.NEWEST_15);
				//String templtePath = littleTemp + templteName;
				String templtePath =  templteName;
				//File file = new File(reportVeidooPath);
				File file = new File("");
				if (!file.exists()) {
					file.mkdirs();
				}
//				String outPath = reportVeidooPath + BIAS + "NEWSSET_" + System.currentTimeMillis()
//						+ (int) (Math.random() * 10000) + DOCX;
				String outPath = "";
						data = new WordData();
				dataCList = new ArrayList<WordData>();
				data.addTextField(Const.tempKeyMap.get(Const.NEWEST_15)[0], newsTitle);
				for (int i = 0; i < titleList.size(); i++) {
					innerData = new WordData();
					innerData.addTextField(Const.tempKeyMap.get(Const.NEWEST_15)[2], String.valueOf(i + 1));
					innerData.addTextField(Const.tempKeyMap.get(Const.NEWEST_15)[3], titleList.get(i));
					innerData.addTextField(Const.tempKeyMap.get(Const.NEWEST_15)[4], timeList.get(i));
					dataCList.add(innerData);
				}
				data.addTable(Const.tempKeyMap.get(Const.NEWEST_15)[1], dataCList);
				if (WordFactory.getInstance().reportByTemplate(templtePath, outPath, data)) {
					veidoo = new ReportVeidoo();
					veidoo.setReportId(reportId);
					veidoo.setSort(position);
					veidoo.setVeidooPath(outPath);
					reportVeidooRepository.save(veidoo);
				} else {
					throw new OperationException("最新新闻文档失败！  reportId=" + reportId);
				}
			}
		} catch (Exception e) {
			log.error("和成最新新闻列表模板数据失败!", e);
		}

	}

	/**
	 * 处理报告顶端数据，生成文档
	 *
	 * @param reportTopData
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void produceTop(String reportTopData, String reportId) throws Exception {
		String[] topArry = reportTopData.split(Const.TRS_SEPARATOR);
		Map<String, Object> datas;
		Map<String, String> topMap;
		ReportVeidoo veidoo;
		WordData data;
		for (String topData : topArry) {
			datas = ObjectUtil.toObject(topData, Map.class);
			topMap = ObjectUtil.toObject(ObjectUtil.toJson(datas.get("data")), Map.class);
			String organName = topMap.get("organName");
			String createYear = topMap.get("createYear");
			String createDate = topMap.get("createDate");
			int position = (Integer) datas.get("position");
			String templteName = Const.pathMap.get(Const.TOP_1);
			//String templtePath = littleTemp + templteName;
			String templtePath =  templteName;

			//File file = new File(reportVeidooPath);
			File file = new File("");
			if (!file.exists()) {
				file.mkdirs();
			}
			/*String outPath = reportVeidooPath + BIAS + "TOP_" + System.currentTimeMillis()
					+ (int) (Math.random() * 10000) + DOCX;*/
			String outPath = "";
			data = new WordData();
			data.addTextField(Const.tempKeyMap.get(Const.TOP_1)[0], organName);
			data.addTextField(Const.tempKeyMap.get(Const.TOP_1)[1], createYear);
			data.addTextField(Const.tempKeyMap.get(Const.TOP_1)[2], createDate);
			if (WordFactory.getInstance().reportByTemplate(templtePath, outPath, data)) {
				veidoo = new ReportVeidoo();
				veidoo.setReportId(reportId);
				veidoo.setSort(position);
				veidoo.setVeidooPath(outPath);
				reportVeidooRepository.save(veidoo);
			} else {
				throw new OperationException("生成报告顶部文档失败！  reportId=" + reportId);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void produceIntro(String reportIntro, String reportId) throws OperationException {
		String[] topArry = reportIntro.split(Const.TRS_SEPARATOR);
		Map<String, Object> datas;
		Map<String, String> introMap;
		ReportVeidoo veidoo;
		WordData data;
		for (String introData : topArry) {
			datas = ObjectUtil.toObject(introData, Map.class);
			introMap = ObjectUtil.toObject(ObjectUtil.toJson(datas.get("data")), Map.class);
			String libraryName = introMap.get("libraryName");
			String keyWords = introMap.get("keyWords");
			String time = introMap.get("time");
			String count = String.valueOf(introMap.get("count"));
			String siteName = introMap.get("siteName");
			String introTitle = introMap.get("introTitle");
			int position = (Integer) datas.get("position");
			String templteName = Const.pathMap.get(Const.INTRO_16);
		//	String templtePath = littleTemp + templteName;
			String templtePath =  templteName;

			//	File file = new File(reportVeidooPath);
			File file = new File("");
			if (!file.exists()) {
				file.mkdirs();
			}
			/*String outPath = reportVeidooPath + BIAS + "INTRO_" + System.currentTimeMillis()
					+ (int) (Math.random() * 10000) + DOCX;*/
			String outPath = "";
			data = new WordData();
			data.addTextField(Const.tempKeyMap.get(Const.INTRO_16)[0], libraryName);
			data.addTextField(Const.tempKeyMap.get(Const.INTRO_16)[1], keyWords);
			data.addTextField(Const.tempKeyMap.get(Const.INTRO_16)[2], time);
			data.addTextField(Const.tempKeyMap.get(Const.INTRO_16)[3], count);
			data.addTextField(Const.tempKeyMap.get(Const.INTRO_16)[4], siteName);
			data.addTextField(Const.tempKeyMap.get(Const.INTRO_16)[5], introTitle);
			if (WordFactory.getInstance().reportByTemplate(templtePath, outPath, data)) {
				veidoo = new ReportVeidoo();
				veidoo.setReportId(reportId);
				veidoo.setSort(position);
				veidoo.setVeidooPath(outPath);
				reportVeidooRepository.save(veidoo);
			} else {
				throw new OperationException("生成报告简介文档失败！  reportId=" + reportId);
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void produceMonitor(String monitorSum, String reportId) throws Exception {
		String[] topArry = monitorSum.split(Const.TRS_SEPARATOR);
		Map<String, Object> datas;
		Map<String, String> SumMap;
		ReportVeidoo veidoo;
		WordData data;
		for (String SumData : topArry) {
			datas = ObjectUtil.toObject(SumData, Map.class);
			SumMap = ObjectUtil.toObject(ObjectUtil.toJson(datas.get("data")), Map.class);
			String totalNum = String.valueOf(SumMap.get("count"));
			String posNum = String.valueOf(SumMap.get("posNum"));
			String neNum = String.valueOf(SumMap.get("neNum"));
			String posRat = SumMap.get("posRat");
			String neRat = SumMap.get("neRat");
			String sumTitle = SumMap.get("sumTitle");
			int position = (Integer) datas.get("position");
			String templteName = Const.pathMap.get(Const.SUMMARIZE_17);
		//	String templtePath = littleTemp + templteName;
			String templtePath =  templteName;

//			File file = new File(reportVeidooPath);
//			if (!file.exists()) {
//				file.mkdirs();
//			}
//			String outPath = reportVeidooPath + BIAS + "SUMMARIZE_" + System.currentTimeMillis()
//					+ (int) (Math.random() * 10000) + DOCX;
			File file = new File("");
			if (!file.exists()) {
				file.mkdirs();
			}

			String outPath = "";
			data = new WordData();
			data.addTextField(Const.tempKeyMap.get(Const.SUMMARIZE_17)[0], totalNum);
			data.addTextField(Const.tempKeyMap.get(Const.SUMMARIZE_17)[1], posNum);
			data.addTextField(Const.tempKeyMap.get(Const.SUMMARIZE_17)[2], posRat);
			data.addTextField(Const.tempKeyMap.get(Const.SUMMARIZE_17)[3], neNum);
			data.addTextField(Const.tempKeyMap.get(Const.SUMMARIZE_17)[4], neRat);
			data.addTextField(Const.tempKeyMap.get(Const.SUMMARIZE_17)[5], sumTitle);
			if (WordFactory.getInstance().reportByTemplate(templtePath, outPath, data)) {
				veidoo = new ReportVeidoo();
				veidoo.setReportId(reportId);
				veidoo.setSort(position);
				veidoo.setVeidooPath(outPath);
				reportVeidooRepository.save(veidoo);
			} else {
				throw new OperationException("生成报告概述文档失败！  reportId=" + reportId);
			}
		}

	}

	private void produceHottest(String hottestData, String reportId) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> produceAllImage(String allImageData) throws Exception {
		allImageData = allImageData.replaceFirst(Const.TRS_SEPARATOR, "");
		String[] allImageArry = allImageData.split(Const.TRS_SEPARATOR);
		Map<String, Object> datas;
		List<Map<String, Object>> allData = new ArrayList<Map<String, Object>>();
		for (String oneData : allImageArry) {
			datas = ObjectUtil.toObject(oneData, Map.class);
			allData.add(datas);
		}
		return allData;
	}

	@Synchronized
	@Override
	public Object drawImage(String reportId, String imageDate, String title, int position, String key, String imageName)
			throws Exception {
		Base64 basedec = new Base64();
		byte[] imgarry = basedec.decode(imageDate);
		InputStream inputs = new ByteArrayInputStream(imgarry);
		WordData data = new WordData();
		ReportVeidoo veidoo;
		data.addTextField(Const.tempKeyMap.get(key)[0], title);
		data.addImageField(Const.tempKeyMap.get(key)[1], inputs, width, height);
		String templteName = Const.pathMap.get(key);
	//	String templtePath = littleTemp + templteName;
		String templtePath =  templteName;

		/*File file = new File(reportVeidooPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		String outPath = reportVeidooPath + BIAS + imageName + "_" + System.currentTimeMillis()
				+ (int) (Math.random() * 10000) + DOCX;*/
		File file = new File("");
		if (!file.exists()) {
			file.mkdirs();
		}

		String outPath = "";
		if (WordFactory.getInstance().reportByTemplate(templtePath, outPath, data)) {
			veidoo = new ReportVeidoo();
			veidoo.setReportId(reportId);
			veidoo.setSort(position);
			veidoo.setVeidooPath(outPath);
			reportVeidooRepository.save(veidoo);
			return Const.SUCCESS;
		} else {
			throw new OperationException("生成报告" + imageName + "文档失败！  reportId=" + reportId);
		}
	}






	@Override
	public List<Report> getReportByOrganizationId(String organizationId, int pageNo, int pageSize) {
		List<Report> findByUserId = reportRepository.findByOrganizationId(organizationId,
				new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, "lastModifiedTime")));
		return findByUserId;
	}

	@Override
	public List<Report> seachReportByOrganizationId(String organizationId, String reportName) {
		Criteria<Report> criteria = new Criteria<Report>();
		criteria.add(Restrictions.eq("organizationId", organizationId));
		if (StringUtil.isNotEmpty(reportName)) {
			criteria.add(Restrictions.like("reportName", reportName, MatchMode.ANYWHERE));
		}
		return reportRepository.findAll(criteria);
	}

	@Override
	public List<MaterialLibrary> seachLibByOrganizationId(String organizationId, String libName) {
		Criteria<MaterialLibrary> criteria = new Criteria<MaterialLibrary>();
		criteria.add(Restrictions.eq("organizationId", organizationId));
		if (StringUtil.isNotEmpty(libName)) {
			criteria.add(Restrictions.like("libraryName", libName, MatchMode.ANYWHERE));
		}
		return materialLibraryRepository.findAll(criteria);
	}

	@Override
	public Object getOrganizationIdLibrary(String organizationId, int pageNo, int pageSize) throws TRSException {
		log.info("获取当前用户素材库");
		Page<MaterialLibrary> libraryList;
		libraryList = materialLibraryRepository.findByOrganizationId(organizationId,
				new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, "lastModifiedTime")));
		ObjectUtil.assertNull(libraryList, "素材库列表");

		return libraryList;
	}

	@Override
	public Object getTemplateListByOrganizationId(String organizationId, String libraryId, int pageNo, int pageSize)
			throws Exception {
		getReportMaterialSql(libraryId);
		List<Template> templateList = templateRepository.findByOrganizationId(organizationId,
				new PageRequest(pageNo, pageSize, new Sort(Sort.Direction.DESC, "lastModifiedTime")));
		return templateList;
	}

	private boolean preDrawImage(String[] strs, String reportId) throws Exception {
		String title = null;
		Integer position = null;
		String base64str = null;
		String key = null;
		String imageName = null;
		for (int i = 0; i < strs.length; i++) {
			// strs[i] ==> title#TIT#position#POS#base64str
			String[] str = strs[i].split(TIT);
			title = str[0];
			String[] str2 = str[1].split(POS);
			position = Integer.parseInt(str2[0]);
			base64str = str2[1];
			String[] matchByTitle = matchByTitle(title);
			key = matchByTitle[0];
			if (key == null)
				continue;
			imageName = matchByTitle[1];
			drawImage(reportId, base64str, title, position, key, imageName);
		}
		return false;
	}

	private String[] matchByTitle(String title) {
		String key = null;
		String imageName = null;
		if (title.contains(AREA)) {
			key = "2";
			imageName = "AREA";
		} else if (title.contains(SOURCE)) {
			key = "3";
			imageName = "SOURCE";
		} else if (title.contains(ACTIVE)) {
			key = "4";
			imageName = "ACTIVE";
		} else if (title.contains(EMOTION)) {
			key = "6";
			imageName = "EMOTION";
		} else if (title.contains(VOLUME)) {
			key = "11";
			imageName = "VOLUME";
		}
		String[] strArr = new String[] { key, imageName };
		return strArr;
	}
	/*
	*@Description: 达到取消时间限制的目的
	*@Param: [TRSL, searchBuilder]
	*@return: java.lang.String
	*@Author: Maguocai
	*@create time: 2019/6/17 14:10
	*/
	public String toDealWithTime(String TRSL,QueryBuilder searchBuilder){
		TRSL = "("+TRSL+")";
		String end = DateUtil.format2String(new Date(),DateUtil.yyyyMMddHHmmssSSS);//结束时间用当前时间
		String start = "19000101121212000";//开始时间为1900年
		TRSL += " AND "+FtsFieldConst.FIELD_URLTIME + ":[" + start + " TO " + end + "]";
		searchBuilder.setDataDateAndDataSources("maguocai");
		return TRSL;
	}
	/**
	 * 查询某个用户收藏  根据条件
	 *
	 * @throws TRSException
	 */
	public Object getFavouritesByCondition(User user, int pageNo, int pageSize, List<String> groupNameList, String keyword, String fuzzyValueScope, String invitationCard, String forwarPrimary, Boolean isExport) throws TRSException {
		//获取用户可查询的数据源
		String groupNames = org.apache.commons.lang3.StringUtils.join(groupNameList, ";");
		List<String> groupName = SourceUtil.getGroupNameList(groupNames);
		if (groupName == null || groupName.size() == 0) {
			return null;
		}
		String source = StringUtils.join(groupName, ";");
		Sort sort = new Sort(Sort.Direction.DESC, "createdTime");
		PageRequest pageable = new PageRequest(pageNo, pageSize, sort);
		Page<Favourites> list = null;
		Specification<Favourites> criteria = new Specification<Favourites>() {
			@Override
			public Predicate toPredicate(Root<Favourites> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicate = new ArrayList<>();

				if (UserUtils.ROLE_LIST.contains(user.getCheckRole())) {
					predicate.add(cb.equal(root.get("userId"), user.getId()));
				} else {
					predicate.add(cb.equal(root.get("subGroupId"), user.getSubGroupId()));
				}
				predicate.add(cb.isNull(root.get("libraryId")));

				if (StringUtil.isNotEmpty(keyword)) {
					String[] split = keyword.split("\\s+|,");
					String splitNode = "";
					for (int i = 0; i < split.length; i++) {
						if (StringUtil.isNotEmpty(split[i])) {
							splitNode += split[i] + ",";
						}
					}
					String tkeyword = splitNode.substring(0, splitNode.length() - 1);
					List<Predicate> predicateKeyWord = new ArrayList<>();
					switch (fuzzyValueScope) {
						case "title":
							predicateKeyWord.add(cb.like(root.get("title"), "%" + keyword + "%"));
							break;
						case "source":
							predicateKeyWord.add(cb.like(root.get("siteName"), "%" + keyword + "%"));
							break;
						case "author":
							predicateKeyWord.add(cb.like(root.get("authors"), "%" + keyword + "%"));
							break;
						case "fullText":
							predicateKeyWord.add(cb.like(root.get("title"), "%" + keyword + "%"));
							predicateKeyWord.add(cb.like(root.get("content"), "%" + keyword + "%"));
							break;
					}
					predicate.add(cb.or(predicateKeyWord.toArray(new Predicate[predicateKeyWord.size()])));
				}
				if ((StringUtil.isNotEmpty(forwarPrimary) && source.contains("微博")) ||
						StringUtil.isNotEmpty(invitationCard) && source.contains("国内论坛")) {
					List<Predicate> predicateGroupName = new ArrayList<>();

					if (StringUtil.isNotEmpty(forwarPrimary) && source.contains("微博")) {
						List<Predicate> predicateWeibo = new ArrayList<>();
						predicateWeibo.add(cb.equal(root.get("groupName"), "微博"));
						if ("primary".equals(forwarPrimary)) {
							// 原发
							predicateWeibo.add(cb.isNull(root.get("retweetedMid")));
						} else if ("forward".equals(forwarPrimary)) {
							//转发
							predicateWeibo.add(cb.isNotNull(root.get("retweetedMid")));
						}
						for (int i = 0; i < groupName.size(); i++) {
							if ("微博".equals(groupName.get(i))) {
								groupName.remove(i);
							}
						}
						predicateGroupName.add(cb.and(predicateWeibo.toArray(new Predicate[predicateWeibo.size()])));
					}

					if (StringUtil.isNotEmpty(invitationCard) && source.contains("国内论坛")) {
						List<Predicate> predicateLuntan = new ArrayList<>();
						predicateLuntan.add(cb.equal(root.get("groupName"), "国内论坛"));
						if ("0".equals(invitationCard)) {
							// 主贴
							List<Predicate> predicateLuntan_zhutie = new ArrayList<>();
							predicateLuntan_zhutie.add(cb.isNull(root.get("nreserved1")));
							predicateLuntan_zhutie.add(cb.equal(root.get("nreserved1"), "0"));
							predicateLuntan_zhutie.add(cb.equal(root.get("nreserved1"), ""));
							predicateLuntan.add(cb.or(predicateLuntan_zhutie.toArray(new Predicate[predicateLuntan_zhutie.size()])));
						} else if ("1".equals(invitationCard)) {
							//回帖
							predicateLuntan.add(cb.equal(root.get("nreserved1"), "1"));
						}
						for (int i = 0; i < groupName.size(); i++) {
							if ("国内论坛".equals(groupName.get(i))) {
								groupName.remove(i);
							}
						}
						predicateGroupName.add(cb.and(predicateLuntan.toArray(new Predicate[predicateLuntan.size()])));
					}

					if (groupName.size() > 0) {
						//如果还有其他数据源，应该是or的关系，可以是符合数据源，可以是符合筛选
						List<Predicate> predicatOtherGroupName = new ArrayList<>();
						CriteriaBuilder.In<String> in = cb.in(root.get("groupName").as(String.class));
						for (int i = 0; i < groupName.size(); i++) {
							in.value(groupName.get(i));
						}
						predicatOtherGroupName.add(in);
						predicateGroupName.add(cb.or(predicatOtherGroupName.toArray(new Predicate[predicatOtherGroupName.size()])));
					}
					predicate.add(cb.or(predicateGroupName.toArray(new Predicate[predicateGroupName.size()])));

				} else {
					//In<String> in = cb.in(root.get("groupName").as(String.class));
					CriteriaBuilder.In<String> in = cb.in(root.get("groupName").as(String.class));
					for (int i = 0; i < groupName.size(); i++) {
						in.value(groupName.get(i));
					}
					predicate.add(in);
				}
				Predicate[] pre = new Predicate[predicate.size()];
				return query.where(predicate.toArray(pre)).getRestriction();
			}
		};

		list = favouritesRepository.findAll(criteria, pageable);
		if (isExport) {
			if (ObjectUtil.isNotEmpty(list)) {
				list.forEach(item -> {
					if (item.getGroupName().equals("Twitter") || item.getGroupName().equals("Facebook") || item.getGroupName().equals("国内微信")) {
						item.setScreenName(item.getAuthors());
					}
					if (StringUtil.isEmpty(item.getUrltime()) && item.getUrlTime() != null)
						item.setUrltime(DateUtil.getDataToTime(item.getUrlTime()));//前端需要Urltime
					item.setUrlTitle(StringUtil.cutContentPro(StringUtil.replaceImg(subString(item.getUrlTitle())), Const.CONTENT_LENGTH));
					item.setStatusContent(StringUtil.cutContentPro(StringUtil.replaceImg(subString(item.getStatusContent())), Const.CONTENT_LENGTH));
					item.setContent(StringUtil.cutContentPro(StringUtil.replaceImg(subString(item.getContent())), Const.CONTENT_LENGTH));
					item.setTitle(StringUtil.cutContentPro(StringUtil.replaceImg(subString(item.getTitle())), Const.CONTENT_LENGTH));
				});
			}

			return new InfoListResult<>(list.getContent(), (int) list.getTotalElements(), list.getTotalPages());
		} else {
			if (ObjectUtil.isNotEmpty(list)) {
				List<Object> resultList = new ArrayList<>();
				list.forEach(item -> {
					Map<String, Object> map = new HashMap<>();

					String oneGroup = CommonListChartUtil.formatPageShowGroupName(item.getGroupName());
					map.put("id", item.getSid());
					map.put("groupName", oneGroup);
					map.put("urlTime", item.getUrlTime());
					map.put("md5", item.getMdsTag());

					if (StringUtil.isEmpty(item.getUrltime()) && item.getUrlTime() != null)
						item.setUrltime(DateUtil.getDataToTime(item.getUrlTime()));//前端需要Urltime
					item.setUrlTitle(StringUtil.cutContentByFont(StringUtil.replaceImg(subString(item.getUrlTitle())), Const.CONTENT_LENGTH));
					item.setContent(StringUtil.cutContentByFont(StringUtil.replaceImg(subString(item.getContent())), Const.CONTENT_LENGTH));
					item.setTitle(StringUtil.cutContentByFont(StringUtil.replaceImg(subString(item.getTitle())), Const.CONTENT_LENGTH));

					String title = item.getTitle();
					map.put("title", title);
					if (StringUtil.isNotEmpty(title)) {
						title = title.replaceAll("<font color=red>", "").replaceAll("</font>", "");
					}
					map.put("copyTitle", title); //前端复制功能需要用到
					//摘要
					map.put("abstracts", item.getContent());
					if (item.getKeywords() != null && item.getKeywords().size() > 3) {
						map.put("keyWordes", item.getKeywords().subList(0, 3));
					} else {
						map.put("keyWordes", item.getKeywords());
					}
					String voEmotion = item.getAppraise();
					if (StringUtil.isNotEmpty(voEmotion)) {
						map.put("emotion", voEmotion);
					} else {
						map.put("emotion", "中性");
						map.put("isEmotion", null);
					}
					map.put("nreserved1", null);
					map.put("hkey", item.getHkey());
					if (Const.PAGE_SHOW_LUNTAN.equals(oneGroup)) {
						map.put("nreserved1", item.getNreserved1());
					}
					map.put("urlName", item.getUrlName());
					map.put("favourite", item.isFavourite());
					String fullContent = item.getFullContent();
					if (StringUtil.isNotEmpty(fullContent)) {
						if (fullContent.indexOf("<font color=red>") != -1) {
							fullContent = ReportUtil.calcuHit("", fullContent, true);
						}
						fullContent = StringUtil.replaceImg(StringUtil.replaceFont(fullContent));
					}
					map.put("siteName", item.getSiteName());
					map.put("authors", item.getAuthors());
					map.put("srcName", item.getSrcName());
					//微博、Facebook、Twitter、短视频等没有标题，应该用正文当标题
					if (Const.PAGE_SHOW_WEIBO.equals(oneGroup)) {
						map.put("title", item.getContent());
						map.put("abstracts", item.getContent());
						map.put("copyTitle", fullContent); //前端复制功能需要用到

						map.put("authors", item.getScreenName());
					} else if (Const.PAGE_SHOW_FACEBOOK.equals(oneGroup) || Const.PAGE_SHOW_TWITTER.equals(oneGroup)) {
						map.put("title", item.getContent());
						map.put("abstracts", item.getContent());
						map.put("copyTitle", fullContent); //前端复制功能需要用到
						map.put("authors", item.getAuthors());
					} else if (Const.PAGE_SHOW_DUANSHIPIN.equals(oneGroup) || Const.PAGE_SHOW_CHANGSHIPIN.equals(oneGroup)) {
						if(StringUtil.isEmpty(title)){
							map.put("title", item.getContent());
						}
						map.put("abstracts", item.getContent());
						map.put("authors", item.getAuthors());
						if(StringUtil.isEmpty(title)){
							map.put("copyTitle", fullContent); //前端复制功能需要用到
						}
					}
					map.put("channel", item.getChannel());
					//前端页面显示需要，与后端无关
					map.put("img", item.getImgSrc());
					//前端页面显示需要，与后端无关
					map.put("isImg", StringUtil.isEmpty(item.getImgSrc())? false:true);
					map.put("simNum", 0);
					resultList.add(map);
				});
				return new InfoListResult<>(resultList, (int) list.getTotalElements(), list.getTotalPages());
			}
		}
		return null;
	}

}
