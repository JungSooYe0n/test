package com.trs.netInsight.widget.special.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.cache.PerpetualPool;
import com.trs.netInsight.support.cache.RedisFactory;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocument;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.entity.FtsDocumentWeChat;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.column.entity.emuns.SpecialFlag;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.home.service.IHomeService;
import com.trs.netInsight.widget.notice.service.INoticeSendService;
import com.trs.netInsight.widget.report.service.IReportService;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.SpecialSubject;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.special.entity.repository.SpecialProjectRepository;
import com.trs.netInsight.widget.special.entity.repository.SpecialSubjectRepository;
import com.trs.netInsight.widget.special.service.ISpecialProjectService;
import com.trs.netInsight.widget.special.service.ISpecialService;
import com.trs.netInsight.widget.special.service.ISpecialSubjectService;
import com.trs.netInsight.widget.user.entity.DataSyncSpecial;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 专项监测服务层
 *
 * Created by ChangXiaoyang on 2017/4/7.
 */
@Service
@Slf4j
@Transactional
public class SpecialServiceImpl implements ISpecialService {

	@Autowired
	private SpecialProjectRepository specialProjectRepository;

	@Autowired
	private IReportService reportService;

	@Autowired
	private FullTextSearch hybase8SearchService;

	@Autowired
	private INoticeSendService noticeSendService;

	@Value("${email.services.receivers}")
	private String receivers;

	@Autowired
	private IHomeService homeService;

	@Autowired
	private SpecialSubjectRepository specialSubjectRepository;

	@Autowired
	private ISpecialProjectService specialProjectService;

	@Autowired
	private ISpecialSubjectService specialSubjectService;

	/**
	 * 发送邮件
	 * 
	 * @throws OperationException
	 */
	@Override
	public Object sendEmail(SpecialProject specialProject) throws OperationException {
		// 为了轮播展示图片 创建完专项之后发送提醒邮件
		Map<String, String> mapTitle = new HashMap<>();
		mapTitle.put("title", specialProject.getSpecialName());
		mapTitle.put("url", specialProject.toNoPagedAndTimeBuilder().asTRSL());
		List<Map<String, String>> list = new ArrayList<>();
		list.add(mapTitle);
		Map<String, Object> listMap = new HashMap<>();
		listMap.put("listMap", list);
		// 模板名
		noticeSendService.sendAll(SendWay.EMAIL, "specialMailmess.ftl", "专项添加邮件", listMap, receivers, null,null);
		return "send email success";
	}

	/**
	 * 存储imgurl
	 * 
	 * @throws TRSException
	 * @throws com.trs.netInsight.handler.exception.TRSException
	 */
	@Override
	public String imgUrl(SpecialProject specialProject) throws TRSSearchException, TRSException {
		// 通过id把它查出来 然后再把imgurl存进去
		// 通过内容截取图片
		QueryBuilder queryBuilder = specialProject.toNoTimeBuilder(0, 1);
		// 过滤
		queryBuilder.filterField("IR_GROUPNAME", "国内微信", Operator.NotEqual);
		queryBuilder.orderBy("IR_IMAGEFLAG", true);
		queryBuilder.setDatabase(Const.HYBASE_NI_INDEX);
		List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(queryBuilder, FtsDocument.class, true, false,false,"special");
		if (ftsQuery != null && ftsQuery.size() > 0) {
			FtsDocument document = ftsQuery.get(0);
			String content = document.getContent();
			if (content.contains("IMAGE")) {
				String[] split1 = content.split("IMAGE");
				int mid = split1.length / 2;
				String imageContent1 = split1[mid];
				// 过滤掉git
				String group = homeService.noGif(mid + 1, imageContent1, split1, 203, "");
				if (StringUtil.isNotEmpty(group) && !group.contains("gif")) {
					return group;
				} else {
					imageContent1 = split1[0];
					String noGif = homeService.noGif(0, imageContent1, split1, 203, "");
					return noGif;
				}
			} else {
				return "";
			}
		} else {
			return "";
		}
	}

	/**
	 * 获取检测方案列表
	 *
	 * @return List<SpecialTopic>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List getMonitorList(String organizationId, int size) throws TRSException {
		List<SpecialProject> topicList;
		if (size <= 0) {
			topicList = specialProjectRepository.findByOrganizationId(organizationId,
					new Sort(Sort.Direction.DESC, "lastModifiedTime"));
		} else {
			topicList = specialProjectRepository.findByOrganizationId(organizationId,
					new PageRequest(0, size, new Sort(Sort.Direction.DESC, "lastModifiedTime")));
		}
		ObjectUtil.assertNull(topicList, "监测方案列表");
		return ObjectUtil.writeWithView(topicList, SpecialProject.DisplayView.class);
	}

	/**
	 * 创建新专项
	 */
	@Override
	public void createSpecial(SpecialProject specialProject) throws TRSException {
		specialProjectRepository.save(specialProject);
	}

	/**
	 * 更新专项
	 */
	@Override
	public SpecialProject updateSpecial(String specialId, SpecialType type, String specialName,
			String anyKeywords, String excludeWords, String trsl,
			SearchScope scope, Date startTime, Date endTime, String source, String timerange, boolean similar,
			boolean weight, boolean irSimflag, boolean server,boolean irSimflagAll,String excludeWeb,String monitorSite,String mediaLevel,
										String mediaIndustry,String contentIndustry,String filterInfo,String contentArea,String mediaArea) throws Exception {
		SpecialProject specialProject = specialProjectRepository.findOne(specialId);
		// 修改专项
		specialProject.setSpecialType(type);
		specialProject.setSpecialName(specialName);
		specialProject.setSearchScope(scope);
		specialProject.setAnyKeywords(anyKeywords);
		specialProject.setExcludeWords(excludeWords);
		specialProject.setTrsl(trsl);
		specialProject.setStartTime(startTime);
		specialProject.setStart(new SimpleDateFormat("yyyyMMddHHmmss").format(startTime));
		specialProject.setTimeRange(timerange);
		specialProject.setEndTime(endTime);
		specialProject.setEnd(new SimpleDateFormat("yyyyMMddHHmmss").format(endTime));
		specialProject.setSimilar(similar);
		specialProject.setSource(source);
		specialProject.setIrSimflag(irSimflag);
		specialProject.setLastModifiedTime(new Date());
		specialProject.setWeight(weight);
		specialProject.setServer(server);
		specialProject.setIrSimflagAll(irSimflagAll);
		specialProject.setExcludeWeb(excludeWeb);
		specialProject.setMonitorSite(monitorSite);
		specialProject.setMediaLevel(mediaLevel);
		specialProject.setMediaIndustry(mediaIndustry);
		specialProject.setContentIndustry(contentIndustry);
		specialProject.setFilterInfo(filterInfo);
		specialProject.setMediaArea(mediaArea);
		specialProject.setContentArea(contentArea);
		// if (legal(specialProject)) {
		specialProjectRepository.save(specialProject);
		reportService.saveMaterialLibrary(specialProject);
		PerpetualPool.put(specialId, DateUtil.formatCurrentTime("yyyyMMddHHmmss"));
		RedisFactory.deleteAllKey(specialId);
		// RedisFactory.batchClearRedis(specialId);
		return specialProject;
		// } else {
		// throw new OperationException("修改失败");
		// }
	}
	@Override
	public Integer getMaxSequenceForSpecial(String parentPageId, User user) {
		Integer seq = 0;
		List<SpecialSubject> indexPage = new ArrayList<>();
		List<SpecialProject> indexTabMapper = new ArrayList<>();
		if(StringUtil.isEmpty(parentPageId)){
			Map<String,Object> oneColumn = this.getOneLevelColumnForMap(user);
			if (oneColumn.containsKey("page")) {
				indexPage = (List<SpecialSubject>) oneColumn.get("page");
			}
			if (oneColumn.containsKey("tab")) {
				indexTabMapper = (List<SpecialProject>) oneColumn.get("tab");
			}
		}else{
			SpecialSubject parentPage =specialSubjectRepository.findOne(parentPageId);
			indexPage = parentPage.getChildrenPage();
			indexTabMapper = parentPage.getIndexTabMappers();
		}
		List<Object> sortColumn = this.sortColumn(new ArrayList<>(),indexTabMapper,indexPage,false,false);
		if(sortColumn != null && sortColumn.size() > 0){
			Object column = sortColumn.get(sortColumn.size()-1);
			if (column instanceof SpecialProject) {
				seq = ((SpecialProject)column).getSequence();
			} else if (column instanceof SpecialSubject) {
				seq = ((SpecialSubject)column).getSequence();
			}
		}

		return seq;
	}

	/**
	 * 检验专项是否合法
	 *
	 * @param specialProject
	 *            SpecialProject
	 * @return true or false
	 */
	private boolean legal(SpecialProject specialProject) {
		if (specialProject.getStartTime().after(specialProject.getEndTime())) {
			return false;
		}
		QueryBuilder searchBuilder = specialProject.toNoPagedBuilder();
		log.info("trsl:{}", searchBuilder.asTRSL());
		try {
			long count = hybase8SearchService.ftsCount(searchBuilder, true, false,false,"special");
			log.info("count:{}", count);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 预览表达式数据
	 *
	 * @param specialProject
	 *            SpecialProject
	 * @return PagedList
	 * @throws TRSException
	 *             TRSException
	 */
	@Override
	public Object preview(SpecialProject specialProject, String source) throws TRSException {
		if (specialProject.getStartTime().after(specialProject.getEndTime())) {
			return null;
		}
		QueryBuilder builder = null;
		if (Const.MEDIA_TYPE_WEIXIN.contains(source)) {
			builder = specialProject.toBuilderWeiXin(0, 15);
			// builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, false);
			builder.orderBy(FtsFieldConst.FIELD_CREATED_AT, true);// 呼应列表 时间降序
			log.info("trsl:{}", builder.asTRSL());
			try {
				List<FtsDocumentWeChat> ftsQuery = hybase8SearchService.ftsQuery(builder, FtsDocumentWeChat.class,
						specialProject.isSimilar(), false,false,"special");
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();
				for (FtsDocumentWeChat ftsDocumentWeChat : ftsQuery) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("title", ftsDocumentWeChat.getUrlTitle());
					list.add(map);
				}
				return list;
				// return ftsQuery;
			} catch (Exception e) {
				throw new OperationException("检索异常：message:" + e);
			}
		} else if (Const.MEDIA_TYPE_WEIBO.contains(source)) {
			builder = specialProject.toBuilderWeiBo(0, 15);
			// builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, false);
			builder.orderBy(FtsFieldConst.FIELD_CREATED_AT, true);// 呼应列表 时间降序
			log.info("trsl:{}", builder.asTRSL());
			try {
				List<FtsDocumentStatus> ftsQuery = hybase8SearchService.ftsQuery(builder, FtsDocumentStatus.class,
						specialProject.isSimilar(), false,false,"special");
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();
				for (FtsDocumentStatus ftsDocumentWeChat : ftsQuery) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("title", ftsDocumentWeChat.getStatusContent());
					list.add(map);
				}
				return list;
				// return ftsQuery;
			} catch (Exception e) {
				throw new OperationException("检索异常：message:" + e);
			}
		} else if (Const.MEDIA_TYPE_NEWS.contains(source)) {
			builder = specialProject.toBuilder(0, 15);
			// 单选状态
			if ("国内新闻".equals(source)) {
				String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内新闻 NOT ")
						.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
				builder.filterByTRSL(trsl);
			} else if ("国内论坛".equals(source)) {
				String trsl = new StringBuffer(FtsFieldConst.FIELD_GROUPNAME).append(":国内论坛 OR ")
						.append(FtsFieldConst.FIELD_SITENAME).append(":百度贴吧").toString();
				builder.filterByTRSL(trsl);
			} else if (!"传统媒体".equals(source)) {
				builder.filterField(FtsFieldConst.FIELD_GROUPNAME, source, Operator.Equal);
			}
			// builder.orderBy(FtsFieldConst.FIELD_RELEVANCE, false);
			builder.orderBy(FtsFieldConst.FIELD_URLTIME, true);// 呼应列表 时间降序
			log.info("trsl:{}", builder.asTRSL());
			try {
				List<FtsDocument> ftsQuery = hybase8SearchService.ftsQuery(builder, FtsDocument.class,
						specialProject.isSimilar(), false,false,"special");
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();
				for (FtsDocument ftsDocumentWeChat : ftsQuery) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("title", ftsDocumentWeChat.getTitle());
					list.add(map);
				}
				return list;
				// return ftsQuery;
			} catch (Exception e) {
				throw new OperationException("检索异常：message:" + e);
			}
		}
		return null;
	}

	@Override
	public Object selectSpecial(User user) throws OperationException {
		String subGroupId = user.getSubGroupId();
		String userId = user.getId();
		// 重构表时出现的bug 三级都有时 第三集没删除 这段代码为了清除以前数据
		Iterable<SpecialProject> findAll = specialProjectRepository.findAll();
		for (SpecialProject s : findAll) {
			if (StringUtil.isNotEmpty(s.getGroupId())) {
				SpecialSubject findOne = specialSubjectRepository.findOne(s.getGroupId());
				if (ObjectUtil.isEmpty(findOne)) {
					specialProjectRepository.delete(s);
				}
			}
		}
		boolean seqBoolean = false;// 为false
									// 主题和专题的混合列表subjectAndProject就按照sequence排
									// 否则按照lastModifiedTime排
		// 搞一个大的list装置顶的
		Criteria<SpecialProject> criteriaProject = new Criteria<>();
		criteriaProject.add(Restrictions.eq("topFlag", "top"));
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			criteriaProject.add(Restrictions.eq("userId", userId));
		}else {
			criteriaProject.add(Restrictions.eq("subGroupId", subGroupId));
		}

		criteriaProject.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
		List<SpecialProject> topList = specialProjectService.findAll(criteriaProject);
		topList = this.removeSomeProjects(topList,user);
		List listBig = new ArrayList();
		Criteria<SpecialSubject> criteria = new Criteria<>();
		criteria.add(Restrictions.eq("flag", 0));
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			criteria.add(Restrictions.eq("userId", userId));
		}else {
			criteria.add(Restrictions.eq("subGroupId", subGroupId));
		}
		criteria.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
		// 把一级的栏目和专项都装起来按照lastModifiedTime排序
		List<Object> oneSpecialOrSubject = new ArrayList<>();
		// 查询主题（一级分类）
		// 主题是一级 只按照拖拽顺序排 不按照修改时间排
		List<SpecialSubject> listSubject = specialSubjectRepository.findAll(criteria);
		//
		//listSubject = this.removeSomeSubjects(listSubject,user);
		oneSpecialOrSubject.addAll(listSubject);
		// 查方案（专题）
		Criteria<SpecialProject> criteriaProject2 = new Criteria<>();
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			criteriaProject2.add(Restrictions.eq("userId", userId));
		}else {
			criteriaProject2.add(Restrictions.eq("subGroupId", subGroupId));
		}
		criteriaProject2.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
		List<SpecialProject> listSpecialProject = specialProjectService.findAll(criteriaProject2);
		listSpecialProject = this.removeSomeProjects(listSpecialProject,user);
		List<SpecialProject> noGroupAndTop = new ArrayList<>();
		for (SpecialProject special : listSpecialProject) {
			if (StringUtil.isEmpty(special.getGroupId()) && !"top".equals(special.getTopFlag())) {
				noGroupAndTop.add(special);
			}
		}
		oneSpecialOrSubject.addAll(noGroupAndTop);
		// 只按照拖拽顺序排 不按照修改时间排
		ObjectUtil.sort(oneSpecialOrSubject, "sequence", "asc");
		for (int n = 0; n < oneSpecialOrSubject.size(); n++) {
			Object one = oneSpecialOrSubject.get(n);
			if (one instanceof SpecialSubject) {// 专题
				List listMiddle = new ArrayList();
				SpecialSubject subject = (SpecialSubject) one;
				String name = subject.getName();// 主题名字放到一级
				// 根据主题id查专题
				String id = subject.getId();
				Criteria<SpecialSubject> criteriaSubject = new Criteria<>();
				criteriaSubject.add(Restrictions.eq("subjectId", id));
				criteriaSubject.add(Restrictions.eq("flag", 1));//二级分类
				criteriaSubject.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
				List<SpecialSubject> list = specialSubjectService.findAll(criteriaSubject);
				List<Object> subjectAndProject = new ArrayList<>();
				subjectAndProject.addAll(list);
				// 根据主题id查方案
				Criteria<SpecialProject> criteria3 = new Criteria<>();
				criteria3.add(Restrictions.eq("groupId", id));
				if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
					criteria3.add(Restrictions.eq("userId", userId));
				}else {
					criteria3.add(Restrictions.eq("subGroupId", subGroupId));
				}
				criteria3.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
				List<SpecialProject> listSmall = specialProjectService.findAll(criteria3);// 放到第二级
				listSmall = this.removeSomeProjects(listSmall,user);
				subjectAndProject.addAll(listSmall);
				// 只按照拖拽顺序排 不按照修改时间排
				ObjectUtil.sort(subjectAndProject, "sequence", "asc");
				// 反射识别是专题还是专项进行查找下一级
				for (int i = 0; i < subjectAndProject.size(); i++) {
					Object o = subjectAndProject.get(i);
					if (o instanceof SpecialSubject) {// 专题
						SpecialSubject specialSubject = (SpecialSubject) o;
						String specialSubjectName = specialSubject.getName();// 专题名放在二级
						String specialSubjectId = specialSubject.getId();
						// 根据专题id查方案
						Criteria<SpecialProject> criteria2 = new Criteria<>();
						criteria2.add(Restrictions.eq("groupId", specialSubjectId));
						criteria2.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
						List<SpecialProject> list3 = new ArrayList<>();
						List<SpecialProject> list2 = specialProjectService.findAll(criteria2);// 放到第三级
						// 之前参数写错
						// 重复了
						for (SpecialProject special : list2) {
							if (!"top".equals(special.getTopFlag())) {
								// 把不置顶的放进去 左侧竖着显示
								list3.add(special);
							}
						}
						Map<String, Object> putValue = null;
						// if(ObjectUtil.isNotEmpty(list2)){//不管专题下是否有方案也要装进去
						putValue = MapUtil.putValue(
								new String[] { "specialName", "flag", "zhuantiDetail", "currentTheme", "id"},
								specialSubjectName, specialSubject.getFlag(), list3, specialSubject.isCurrentTheme(),
								specialSubject.getId());
						listMiddle.add(putValue);
						// }
					} else if (o instanceof SpecialProject) {// 专项
						SpecialProject specialProject = (SpecialProject) o;
						if (ObjectUtil.isNotEmpty(specialProject) && !"top".equals(specialProject.getTopFlag())) {
							// 之前某个主题下没有直属的方案
							// 所以会返回一个空数组
							listMiddle.add(specialProject);
						}
					}
				}
				Map<String, Object> putValue = MapUtil.putValue(
						new String[] { "specialName", "flag", "zhutiDetail", "currentTheme", "id" }, name,
						subject.getFlag(), listMiddle, subject.isCurrentTheme(),subject.getId());
				listBig.add(putValue);

			} else if (one instanceof SpecialProject) {// 专项
				SpecialProject specialProject = (SpecialProject) one;
				listBig.add(specialProject);
			}
		}
		Map<String, Object> mapAll = new HashMap<>();
		mapAll.put("list", listBig);
		mapAll.put("topList", topList);
		if (ObjectUtil.isNotEmpty(listSpecialProject)) {
			SpecialProject specialProject = listSpecialProject.get(0);
			String groupId = specialProject.getGroupId();
			if (StringUtils.isNotBlank(groupId)) {
				SpecialSubject subject = specialSubjectRepository.findOne(groupId);
				if (subject != null) {
					if (subject.getFlag() == 1) {//二级分类
						SpecialSubject subjectFu = specialSubjectRepository.findOne(subject.getSubjectId());//二级分类对应的一级分类
						mapAll.put("zhutiOne", subjectFu);
						mapAll.put("zhutiTwo", subject);
					} else {
						mapAll.put("zhutiOne", subject);
					}
				}
			}
			// 有置顶的就定位到置顶的第一个 没置顶的就定位到最后修改的那一个
			if (topList != null && topList.size() > 0) {
				mapAll.put("firstId", topList.get(0));
			} else {

				//董晶晶 JIRA1048 提：专题分析列表中默认选中非分组下的第一个专题。
				Criteria<SpecialProject> criteriaLast = new Criteria<>();
				if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
					criteriaLast.add(Restrictions.eq("userId", userId));
				}else {
					criteriaLast.add(Restrictions.eq("subGroupId", subGroupId));
				}
				criteriaLast.add(Restrictions.eq("groupId",""));//非分组下
				criteriaLast.orderByASC("sequence");// 只按照拖拽顺序排
				List<SpecialProject> last = specialProjectService.findAll(criteriaLast);
				last = this.removeSomeProjects(last,user);
				if (last != null && last.size() > 0) {
					mapAll.put("firstId", last.get(0));
				}else {
					//如果没有置顶，且非分组下无专题，则选中按修改时间最近的一个专题
					Criteria<SpecialProject> criteria1 = new Criteria<>();
					if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
						criteria1.add(Restrictions.eq("userId", userId));
					}else {
						criteria1.add(Restrictions.eq("subGroupId", subGroupId));
					}
					criteria1.orderByASC("lastModifiedTime");// 按照修改时间排序
					List<SpecialProject> last1 = specialProjectService.findAll(criteria1);
					last1 = this.removeSomeProjects(last1,user);
					if (last1 != null && last1.size() > 0) {
						mapAll.put("firstId", last1.get(0));
					}
				}
			}
		}
		return mapAll;
	}
	private List<Object> formatResultSpecial(List<Object> list,Integer level,List<Boolean> isGetOne,Boolean parentHide) {
		//前端需要字段
		/*
		id
		name
		flag
		flagSort
		show
		children
		 */
		List<Object> result = new ArrayList<>();
		Map<String, Object> map = null;
		if (list != null && list.size() > 0) {
			for (Object obj : list) {
				map = new HashMap<>();
				if (obj instanceof SpecialProject) {
					SpecialProject tab = (SpecialProject) obj;
					map.put("id",  tab.getId());
					map.put("name", tab.getSpecialName());
					map.put("flag", SpecialFlag.SpecialProjectFlag.ordinal());
					map.put("flagSort", level);
					map.put("show", false);//前端需要，与后端无关
					map.put("type", "");
					map.put("contrast", "");
					map.put("groupName", CommonListChartUtil.formatPageShowGroupName(tab.getSource()));
					map.put("keyWord", tab.getAnyKeywords());
					map.put("keyWordIndex", tab.getSearchScope());
					map.put("excludeWordsIndex",tab.getExcludeWordIndex());
					map.put("weight", tab.isWeight());
					map.put("excludeWords", tab.getExcludeWords());
					map.put("excludeWeb", tab.getExcludeWeb());
					map.put("monitorSite", tab.getMonitorSite());
					map.put("subjectId",tab.getGroupId());
					map.put("specialType",tab.getSpecialType());
					//排重方式 不排 no，单一媒体排重 netRemove,站内排重 urlRemove,全网排重 sourceRemove
					if (tab.isSimilar()) {
						map.put("simflag", "netRemove");
					} else if (tab.isIrSimflag()) {
						map.put("simflag", "urlRemove");
					} else if (tab.isIrSimflagAll()) {
						map.put("simflag", "sourceRemove");
					} else {
						map.put("simflag", "no");
					}
					map.put("timeRange", tab.getTimeRange());
					map.put("trsl", tab.getTrsl());
					map.put("xyTrsl", "");
					map.put("active", false);
					//获取词距信息
					String keywordJson = tab.getAnyKeywords();
					map.put("updateWordForm", false);
					map.put("wordFromNum", 0);
					map.put("wordFromSort",false);
					if(StringUtil.isNotEmpty(keywordJson)){
						JSONArray jsonArray = JSONArray.parseArray(keywordJson);
						//现在词距修改情况为：只有一个关键词组时，可以修改词距等，多个时不允许
						if(jsonArray!= null && jsonArray.size() ==1 ){
							Object o = jsonArray.get(0);
							JSONObject jsonObject = JSONObject.parseObject(String.valueOf(o));
							String key = jsonObject.getString("keyWords");
							if(StringUtil.isNotEmpty(key) && key.contains(",")){
								String wordFromNum = jsonObject.getString("wordSpace");
								Boolean wordFromSort = jsonObject.getBoolean("wordOrder");
								map.put("updateWordForm", true);
								map.put("wordFromNum", wordFromNum);
								map.put("wordFromSort",wordFromSort);
							}
						}
					}

					map.put("mediaLevel", tab.getMediaLevel());
					map.put("mediaIndustry", tab.getMediaIndustry());
					map.put("contentIndustry", tab.getContentIndustry());
					map.put("filterInfo", tab.getFilterInfo());
					map.put("contentArea", tab.getContentArea());
					map.put("mediaArea", tab.getMediaArea());

					if(!isGetOne.get(0) ){//之前还没找到一个要显示的 栏目数据
						//要显示的栏目不可以是被隐藏的栏目 且它的父级不可以被隐藏
							map.put("active", true);
							isGetOne.set(0,true);
					}
					if("top".equals(tab.getTopFlag())){
						map.put("topFlag", true);
					}else{
						map.put("topFlag", false);
					}

				} else if (obj instanceof SpecialSubject) {
					SpecialSubject page = (SpecialSubject) obj;
					map.put("id", page.getId());
					map.put("name", page.getName());
					map.put("flag", SpecialFlag.SpecialSubjectFlag.ordinal());
					map.put("flagSort", level);
					map.put("show", false);//前端需要，与后端无关
					map.put("active", false);
//					map.put("parentId",page.getParentId());
					map.put("parentId",page.getSubjectId());
					List<Object> childColumn = page.getColumnList();
					List<Object> child = new ArrayList<>();
					//如果父级被隐藏，这一级也会被隐藏，直接用父级的隐藏值
					//如果父级没被隐藏，当前级被隐藏，则用当前级的隐藏值
					//如果父级没隐藏，当前级没隐藏，用没隐藏，父级则可
					if(!parentHide){
							parentHide = true;
					}
					//如果分组被隐藏了，前端不会显示，所以这里不查询了
					if (childColumn != null && childColumn.size() > 0) {
						child = this.formatResultSpecial(childColumn,level+1,isGetOne,parentHide);
					}
					map.put("children", child);
					map.put("topFlag", false);
				}
				result.add(map);
			}
		}
		return result;
	}
	public List<Object> sortColumn(List<Object> result,List<SpecialProject> mapperList,List<SpecialSubject> indexPageList,Boolean sortAll,Boolean onlySortPage){
		List<Map<String,Object>> sortList = new ArrayList<>();
		if(!onlySortPage){
			if(mapperList != null && mapperList.size() >0){
				for(int i =0;i<mapperList.size();i++){
					Map<String,Object> map = new HashMap<>();
					map.put("id",mapperList.get(i).getId());
					map.put("sequence",mapperList.get(i).getSequence());
					map.put("index",i);
					//栏目类型为1
					map.put("flag",SpecialFlag.SpecialProjectFlag);
					sortList.add(map);
				}
			}
		}
		if(indexPageList != null && indexPageList.size() > 0){
			for(int i =0;i<indexPageList.size();i++){
				Map<String,Object> map = new HashMap<>();
				map.put("id",indexPageList.get(i).getId());
				map.put("sequence",indexPageList.get(i).getSequence());
				map.put("index",i);
				//分组类型为0
				map.put("flag", SpecialFlag.SpecialSubjectFlag);
				sortList.add(map);
			}
		}
		if(sortList.size() >0){
			Collections.sort(sortList, (o1, o2) -> {
				Integer seq1 = (Integer) o1.get("sequence");
				Integer seq2 = (Integer) o2.get("sequence");
				return seq1.compareTo(seq2);
			});
			//sortList 排序过后的数据
			//只排序当前层，排序过后的数据，按顺序取出并返回
			for(Map<String,Object> map : sortList){
				SpecialFlag flag = (SpecialFlag) map.get("flag");
				Integer index = (Integer) map.get("index");
				if(flag.equals(SpecialFlag.SpecialSubjectFlag)){
					SpecialSubject indexPage = indexPageList.get(index);
					if(sortAll){
						//获取子类的数据进行排序
						List<SpecialSubject> child_page = indexPage.getChildrenPage();
						List<SpecialProject> child_mapper = null;
						if(!onlySortPage){
							child_mapper = indexPage.getIndexTabMappers();
						}
						if( (child_mapper != null && child_mapper.size()>0) || (child_page != null && child_page.size() >0) ){
							indexPage.setColumnList(sortColumn(new ArrayList<Object>(),child_mapper,child_page,sortAll,onlySortPage));
						}
					}
					result.add(indexPage);
				}else if(flag.equals(SpecialFlag.SpecialProjectFlag)){
					SpecialProject mapper = mapperList.get(index);
					//栏目只有一层，直接添加就行
					result.add(mapper);
				}
			}
		}

		return result;
	}
	@Override
	public Object selectSpecialReNew(User user) {
		List<SpecialSubject> oneIndexPage = null;
		List<SpecialProject> oneIndexTab = null;
		List<SpecialProject> top = null;
		Map<String,Object> oneSpecial = this.getOneLevelColumnForMap(user);
		if (oneSpecial.containsKey("page")) {
			oneIndexPage = (List<SpecialSubject>) oneSpecial.get("page");
		}
		if (oneSpecial.containsKey("tab")) {
			oneIndexTab = (List<SpecialProject>) oneSpecial.get("tab");
		}
		if (oneSpecial.containsKey("top")) {
			top = (List<SpecialProject>) oneSpecial.get("top");
		}
		List<Object> result = new ArrayList<>();
		if(top != null && top.size()>0){
			result.addAll(top);
		}
		//获取到了第一层的栏目和分组信息，现在对信息进行排序
		result =  sortColumn(result,oneIndexTab,oneIndexPage,true,false);
		List<Boolean> isGetOne = new ArrayList<>();
		isGetOne.add(false);
		return formatResultSpecial(result,0,isGetOne,false);
	}

	@Override
	public Object selectSpecialNew(User user) throws OperationException {
		List<Object> result = new ArrayList<>();
		String subGroupId = user.getSubGroupId();
		String userId = user.getId();
		//置顶
		Criteria<SpecialProject> criteriaProject = new Criteria<>();
		criteriaProject.add(Restrictions.eq("topFlag", "top"));
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole()) || StringUtil.isEmpty(subGroupId)){
			criteriaProject.add(Restrictions.eq("userId", userId));
		}else {
			criteriaProject.add(Restrictions.eq("subGroupId", subGroupId));
		}

		criteriaProject.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
		List<SpecialProject> topList = specialProjectService.findAll(criteriaProject);
		topList = this.removeSomeProjects(topList,user);
		Map<String, Object> map = new HashMap<>();

		map.put("id", topList.get(0).getId());
		map.put("name", topList.get(0).getSpecialName());
		map.put("flag", SpecialFlag.SpecialProjectFlag.ordinal());
		map.put("flagSort", 0);
		map.put("show", false);//前端需要，与后端无关
		map.put("hide", false);
		map.put("isMe", true);
		map.put("share", false);

		map.put("type", "");
		map.put("contrast", "");
		map.put("groupName", CommonListChartUtil.formatPageShowGroupName(topList.get(0).getGroupName()));
		map.put("keyWord", topList.get(0).getAnyKeywords());
		map.put("keyWordIndex", topList.get(0).getSearchScope());
		map.put("weight", topList.get(0).isWeight());
		map.put("excludeWords", topList.get(0).getExcludeWords());
		map.put("excludeWeb", topList.get(0).getExcludeWeb());
		//排重方式 不排 no，单一媒体排重 netRemove,站内排重 urlRemove,全网排重 sourceRemove
		if (topList.get(0).isSimilar()) {
			map.put("simflag", "netRemove");
		} else if (topList.get(0).isIrSimflag()) {
			map.put("simflag", "urlRemove");
		} else if (topList.get(0).isIrSimflagAll()) {
			map.put("simflag", "sourceRemove");
		} else {
			map.put("simflag", "no");
		}
		map.put("tabWidth",0);
		map.put("timeRange", topList.get(0).getTimeRange());
		map.put("trsl", topList.get(0).getTrsl());
		map.put("xyTrsl", "");
		map.put("active", true);
		result.add(map);
		return result;
	}

	@Override
	public Object moveSequenceForSpecial(String moveId, SpecialFlag flag, User user)throws OperationException{
		try {
			SpecialSubject movePage = null;
			SpecialProject moveMapper = null;
			String parentId = "";
			if (flag.equals(SpecialFlag.SpecialProjectFlag)) {
				moveMapper = specialProjectRepository.findOne(moveId);
				if(ObjectUtil.isNotEmpty(moveMapper.getSpecialSubject())){
					parentId = moveMapper.getSpecialSubject().getId();
				}
			} else if (flag.equals(SpecialFlag.SpecialSubjectFlag)) {
				movePage = specialSubjectRepository.findOne(moveId);
				parentId = movePage.getSubjectId();
			}

			List<SpecialProject> mapperList = null;
			List<SpecialSubject> pageList = null;
			if (StringUtil.isEmpty(parentId)) {
				//无父分组，则为一级，直接获取一级的内容
				Map<String, Object> oneColumn = getOneLevelColumnForMap(user);
				if (oneColumn.containsKey("page")) {
					pageList = (List<SpecialSubject>) oneColumn.get("page");
				}
				if (oneColumn.containsKey("tab")) {
					mapperList = (List<SpecialProject>) oneColumn.get("tab");
				}
			} else {
				SpecialSubject parentPage = specialSubjectRepository.findOne(parentId);
				pageList = parentPage.getChildrenPage();
				mapperList = parentPage.getIndexTabMappers();
			}
			List<Object> sortColumn = this.sortColumn(new ArrayList<>(),mapperList, pageList, false,false);
			int seq = 1;
			for (Object o : sortColumn) {
				if (o instanceof SpecialProject) {
					SpecialProject seqMapper = (SpecialProject) o;
					if ((flag.equals(SpecialFlag.SpecialProjectFlag) && !moveMapper.getId().equals(seqMapper.getId()))
							|| flag.equals(SpecialFlag.SpecialSubjectFlag)) {
						seqMapper.setSequence(seq);
						seq++;
						specialProjectRepository.saveAndFlush(seqMapper);
					}
				} else if (o instanceof SpecialSubject) {
					SpecialSubject seqPage = (SpecialSubject) o;
					if ((flag.equals(SpecialFlag.SpecialSubjectFlag) && !movePage.getId().equals(seqPage.getId()))
							|| flag.equals(SpecialFlag.SpecialProjectFlag)) {
						seqPage.setSequence(seq);
						seq++;
						specialSubjectRepository.saveAndFlush(seqPage);
					}
				}
			}
			return "success";
		} catch (Exception e) {
			throw new OperationException("重新对分组和专题排序失败："+e.getMessage());
		}
	}

	public Map<String,Object> getOneLevelColumnForMap(User loginUser){
		Sort sort = new Sort(Sort.Direction.ASC,"sequence");
		Specification<SpecialSubject> criteria_page = new Specification<SpecialSubject>(){

			@Override
			public Predicate toPredicate(Root<SpecialSubject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicate = new ArrayList<>();
				if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
					predicate.add(cb.equal(root.get("userId"),loginUser.getId()));
				}else {
					predicate.add(cb.equal(root.get("subGroupId"),loginUser.getSubGroupId()));
				}
				List<Predicate> predicateParent = new ArrayList<>();
				predicateParent.add(cb.isNull(root.get("subjectId")));
				predicateParent.add(cb.equal(root.get("subjectId"),""));

				predicate.add(cb.or(predicateParent.toArray(new Predicate[predicateParent.size()])));
				Predicate[] pre = new Predicate[predicate.size()];
				return query.where(predicate.toArray(pre)).getRestriction();
			}
		};
		Specification<SpecialProject> criteria_tab_mapper = new Specification<SpecialProject>(){

			@Override
			public Predicate toPredicate(Root<SpecialProject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> predicate = new ArrayList<>();
				if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
					predicate.add(cb.equal(root.get("userId"),loginUser.getId()));
				}else {
					predicate.add(cb.equal(root.get("subGroupId"),loginUser.getSubGroupId()));
				}
				predicate.add(cb.isNull(root.get("groupId")));
				Predicate[] pre = new Predicate[predicate.size()];
				return query.where(predicate.toArray(pre)).getRestriction();
			}
		};

		Map<String,Object> result = new HashMap<>();
		//获取当前用户一层分组内的所有内容
		List<SpecialSubject> oneIndexPage = null;
		List<SpecialProject> oneIndexTab = null;

		oneIndexPage = specialSubjectRepository.findAll(criteria_page,sort);
		oneIndexTab = specialProjectRepository.findAll(criteria_tab_mapper,sort);
		if(oneIndexPage != null && oneIndexPage.size() >0){
			result.put("page",oneIndexPage);
		}
		if(oneIndexTab != null && oneIndexTab.size() >0){
			List<SpecialProject> topList = new ArrayList<>();
			List<SpecialProject> otherProjectList = new ArrayList<>();
			for(SpecialProject specialProject :oneIndexTab){
				if("top".equals(specialProject.getTopFlag())){
					topList.add(specialProject);
				}else{
					otherProjectList.add(specialProject);
				}
			}
			if(topList != null && topList.size() >0){
				result.put("top",topList);
			}
			if(otherProjectList != null && otherProjectList.size() >0){
				result.put("tab",otherProjectList);
			}
		}
		return  result;
	}
	@Override
	public List selectSomeSpecials(String orgAdminId) throws TRSException {
		//最终返回结果集
		List resultList = new ArrayList();

		//先把无分组的专题放入
		Criteria<SpecialProject> criteriaProject2 = new Criteria<>();
		criteriaProject2.add(Restrictions.eq("userId", orgAdminId));
		criteriaProject2.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
		List<SpecialProject> listSpecialProject = specialProjectService.findAll(criteriaProject2);
	//	listSpecialProject = this.removeSomeProjects(listSpecialProject,user);
		for (SpecialProject special : listSpecialProject) {
			if (StringUtil.isEmpty(special.getGroupId())) {
				resultList.add(special);
			}
		}

		//查询一级分组
		Criteria<SpecialSubject> criteria = new Criteria<>();
		criteria.add(Restrictions.eq("flag", 0));
		criteria.add(Restrictions.eq("userId", orgAdminId));
		criteria.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
		// 查询主题（一级分类）
		// 主题是一级 只按照拖拽顺序排 不按照修改时间排
		List<SpecialSubject> listSubject = specialSubjectRepository.findAll(criteria);

		//listSubject = this.removeSomeSubjects(listSubject,user);
		//查询二级
		if (ObjectUtil.isNotEmpty(listSubject)){

			for (SpecialSubject specialSubject : listSubject) {

				//放入 一级分组下的 专题 或者 二级分组
				List listMiddle = new ArrayList();
				// 查询一级下 的 专题
				Criteria<SpecialProject> criteria3 = new Criteria<>();
				criteria3.add(Restrictions.eq("groupId", specialSubject.getId()));
				criteria3.add(Restrictions.eq("userId",orgAdminId));
				criteria3.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
				List<SpecialProject> listSmall = specialProjectService.findAll(criteria3);// 放到第二级
			//	listSmall = this.removeSomeProjects(listSmall,user);
				listMiddle.addAll(listSmall);

				//查询二级 主题分组
				Criteria<SpecialSubject> criteriaSubject = new Criteria<>();
				criteriaSubject.add(Restrictions.eq("subjectId", specialSubject.getId()));
				criteriaSubject.add(Restrictions.eq("flag", 1));//二级分类
				criteriaSubject.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
				List<SpecialSubject> list = specialSubjectService.findAll(criteriaSubject);
				if (ObjectUtil.isNotEmpty(list)){
					for (SpecialSubject subject : list) {
						//查询 二级分组下的专题
						Criteria<SpecialProject> criteria2 = new Criteria<>();
						criteria2.add(Restrictions.eq("groupId", subject.getId()));
						criteria2.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
						List<SpecialProject> list2 = specialProjectService.findAll(criteria2);// 放到第三级

						Map<String, Object> putValue = null;
						 if(ObjectUtil.isNotEmpty(list2)){//若分组下无专题 则不放在结果集内
							putValue = MapUtil.putValue(
									new String[] { "specialName", "flag", "zhuantiDetail", "currentTheme", "id","groupId" },
									subject.getName(), subject.getFlag(), list2, subject.isCurrentTheme(),
									subject.getId(),specialSubject.getId());
							listMiddle.add(putValue);
						 }
					}
				}

				//如果该一级分组下有专题存在 则放入结果集内
				if (ObjectUtil.isNotEmpty(listMiddle)){
					Map<String, Object> putValue = MapUtil.putValue(
							new String[] { "specialName", "flag", "zhutiDetail", "currentTheme", "id" }, specialSubject.getName(),
							specialSubject.getFlag(), listMiddle, specialSubject.isCurrentTheme(),specialSubject.getId());
					resultList.add(putValue);
				}

			}
		}

		return resultList;
	}

	@Override
	public void copyOrgSpecial2Common(User orgUser, User commonUser) {
		List<SpecialSubject> orgSubjects = this.specialSubjectRepository.findByUserId(orgUser.getId(),
				new Sort(Sort.Direction.DESC, "lastModifiedTime"));

		// 复制专题相关
		if (orgSubjects != null && orgSubjects.size() > 0) {
			for (SpecialSubject orgSubject : orgSubjects) {

				// subjectId为空,断言为一级主题
				if (StringUtils.isBlank(orgSubject.getSubjectId())) {
					SpecialSubject newInstance = orgSubject.newInstance(commonUser.getId());
					newInstance.setOrganizationId(commonUser.getOrganizationId());
					SpecialSubject newSubject = this.specialSubjectRepository.save(newInstance);

					// 查询对应二级主题
					Criteria<SpecialSubject> criteria = new Criteria<>();
					criteria.add(Restrictions.eq("subjectId", orgSubject.getId()));
					criteria.add(Restrictions.eq("flag", 1));
					criteria.add(Restrictions.eq("userId", orgUser.getId()));
					List<SpecialSubject> secondSubjects = specialSubjectService.findAll(criteria);

					if (secondSubjects != null && secondSubjects.size() > 0) {
						for (SpecialSubject specialSubject : secondSubjects) {

							// 保存二级主题
							SpecialSubject secondInstance = specialSubject.newInstance(newSubject.getId(),
									commonUser.getId());
							secondInstance.setOrganizationId(commonUser.getOrganizationId());
							SpecialSubject newSecondSubject = this.specialSubjectRepository.save(secondInstance);

							// 根据二级主题对应专题集合
							List<SpecialProject> projects = this.specialProjectRepository
									.findByGroupId(specialSubject.getId());
							for (SpecialProject specialProject : projects) {

								// 保存专题
								SpecialProject newProject = specialProject.newInstance(newSecondSubject.getId(),
										commonUser.getId());
								newProject.setOrganizationId(commonUser.getOrganizationId());
								this.specialProjectRepository.save(newProject);
							}
							//
						}
					}

					// 查询一级主题对应的专题集合
					List<SpecialProject> projectsByOneSubject = this.specialProjectRepository
							.findByGroupId(orgSubject.getId());
					if (projectsByOneSubject != null && projectsByOneSubject.size() > 0) {

						for (SpecialProject specialProject : projectsByOneSubject) {
							SpecialProject newProject = specialProject.newInstance(newSubject.getId(), commonUser.getId());
							newProject.setOrganizationId(commonUser.getOrganizationId());
							this.specialProjectRepository.save(newProject);
						}
					}

				}

			}

		}

		// 复制无分组专题
		Criteria<SpecialProject> criteria = new Criteria<>();
		criteria.add(Restrictions.eq("groupId", ""));
		criteria.add(Restrictions.eq("userId", orgUser.getId()));
		List<SpecialProject> list = specialProjectService.findAll(criteria);
		for (SpecialProject specialProject : list) {
			SpecialProject newProject = specialProject.newInstance(commonUser.getId());
			newProject.setOrganizationId(commonUser.getOrganizationId());
			this.specialProjectRepository.save(newProject);
		}
	}

	@Override
	public void copySomeSpecialToUserGroup(String[] specialSync, String specialSyncLevel, SubGroup subGroup) {
		if (ObjectUtil.isNotEmpty(specialSync)){
			//同步无分组专题
			List<SpecialProject> projects = specialProjectService.findByIds(Arrays.asList(specialSync));
			for (SpecialProject project : projects) {
				SpecialProject newSpecial = project.newInstanceForSubGroup(subGroup.getId());
				newSpecial.setOrganizationId(subGroup.getOrganizationId());
				newSpecial.setUserId("dataSync");
				this.specialProjectRepository.save(newSpecial);
			}
		}

		//有级别
		if (StringUtil.isNotEmpty(specialSyncLevel)){
			//同步 有 分组 专题
			List<DataSyncSpecial> dataSyncSpecials = JSONArray.parseArray(specialSyncLevel, DataSyncSpecial.class);
			if (ObjectUtil.isNotEmpty(dataSyncSpecials)){
				//List<String> firstIds = new ArrayList<>();
				for (DataSyncSpecial dataSyncSpecial : dataSyncSpecials) {
					//同步一级的
					if (0 == dataSyncSpecial.getFlag() && StringUtil.isNotEmpty(dataSyncSpecial.getId())){
						//一级分组id
						//firstIds.add(dataSync.getOneId());
						//添加一级分组
						SpecialSubject subject = this.specialSubjectService.findOne(dataSyncSpecial.getId());
						SpecialSubject newInstance = subject.newInstanceForSubGroup(subGroup.getId());
						newInstance.setOrganizationId(subGroup.getOrganizationId());
						newInstance.setUserId("dataSync");
						SpecialSubject newSubject = this.specialSubjectRepository.save(newInstance);

						//一级 下面 的 专题 或者 二级分组
						List<DataSyncSpecial> childs = dataSyncSpecial.getZhuantiDetail();

						if (ObjectUtil.isNotEmpty(childs)){
							for (DataSyncSpecial child : childs) {
								if (1 == child.getFlag()){
									//同步 一级 下 的 二级分组
									//添加二级分组id
									//添加 二级下的 分组
									SpecialSubject secondSub = this.specialSubjectService.findOne(child.getId());
									SpecialSubject newsecondSub = secondSub.newInstanceForSubGroup(subGroup.getId());
									newsecondSub.setOrganizationId(subGroup.getOrganizationId());
									newsecondSub.setSubjectId(newSubject.getId());
									newsecondSub.setUserId("dataSync");
									SpecialSubject newSubSubject = this.specialSubjectRepository.save(newsecondSub);

									//二级下的 专题
									List<DataSyncSpecial> childs1 = child.getZhuantiDetail();
									for (DataSyncSpecial sync : childs1) {
										//二级分组下的三级 专题
										//thirdIds.add(sync.getOneId());
										// 保存专题
										SpecialProject specialProject = this.specialProjectService.findOne(sync.getId());
										SpecialProject newProject = specialProject.newInstanceForSubGroup(newSubSubject.getId(),
												subGroup.getId());
										newProject.setOrganizationId(subGroup.getOrganizationId());
										newProject.setUserId("dataSync");
										this.specialProjectRepository.save(newProject);
									}
								}else if (2==child.getFlag()){
									//三级 专题
									//thirdIds.add(child.getOneId());
									SpecialProject specialProject = this.specialProjectService.findOne(child.getId());
									SpecialProject newProject = specialProject.newInstanceForSubGroup(newSubject.getId(),
											subGroup.getId());
									newProject.setOrganizationId(subGroup.getOrganizationId());
									newProject.setUserId("dataSync");
									this.specialProjectRepository.save(newProject);
								}
							}
						}
					}
				}

			}
		}
	}

	@Override
	public Object move(String ids) {
		String[] split = ids.split(";");
		for (int i = 0; i < split.length; i++) {
			SpecialProject findOne = specialProjectService.findOne(split[i]);
			findOne.setSequence(i + 1);
			specialProjectRepository.save(findOne);
		}
		return "success";
	}

	@Override
	public Object moveList(String ids, String twoOrThree) {
		String[] split = ids.split(";");
		String[] split2 = twoOrThree.split(";");
		for (int i = 0; i < split.length; i++) {
			if ("oneOrtwo".equals(split2[i])) {// 一二级
				SpecialSubject subject = specialSubjectRepository.findOne(split[i]);
				subject.setSequence(i + 1);
				specialSubjectRepository.save(subject);
			} else if ("three".equals(split2[i])) {// 专项
				SpecialProject special = specialProjectService.findOne(split[i]);
				special.setSequence(i + 1);
				specialProjectService.save(special);
			}
		}
		return "success";
	}

	@Override
	public Object moveProjectSequence(String data, String moveData, String parentId, User user)throws OperationException {
		try {

			SpecialSubject parent = null;
			if (StringUtil.isNotEmpty(parentId)) {
				parent = specialSubjectRepository.findOne(parentId);
			}
			JSONObject move = JSONObject.parseObject(moveData);
			String moveId = move.getString("id");
			SpecialFlag moveFlag = SpecialFlag.values()[move.getInteger("flag")];
			Boolean topFlag = move.getBoolean("topFlag");
			String moveParentId = null;

			//修改同级下的其他分组的顺序
			if (moveFlag.equals(SpecialFlag.SpecialSubjectFlag)) {
				SpecialSubject indexPage = specialSubjectRepository.findOne(moveId);
				moveParentId = indexPage.getSubjectId();
			} else if (moveFlag.equals(SpecialFlag.SpecialProjectFlag)) {
				SpecialProject mapper = specialProjectRepository.findOne(moveId);
				if(ObjectUtil.isNotEmpty(mapper.getGroupId())){
					moveParentId = mapper.getGroupId();
				}
			}
			if (parentId == null) {
				parentId = "";
			}
			if (moveParentId == null) {
				moveParentId = "";
			}
			//被拖拽的元素更换了层级，需要对元素本来的层级的数据重新排序
			if (!topFlag && !parentId.equals(moveParentId)) {
				//对原来的同层级的数据排序
				this.moveSequenceForSpecial(moveId,moveFlag, user);
			}
			JSONArray array = JSONArray.parseArray(data);
			List<Object> filter = array.stream().filter(json -> topFlag.equals(JSONObject.parseObject(String.valueOf(json)).getBoolean("topFlag")) ).collect(Collectors.toList());
			Integer sequence = 0;
			for (Object json : filter) {
				sequence += 1;
				JSONObject parseObject = JSONObject.parseObject(String.valueOf(json));
				String id = parseObject.getString("id");
				//如果是分组为0 ，如果是栏目为1
				SpecialFlag flag = SpecialFlag.values()[parseObject.getInteger("flag")];
				if (flag.equals(SpecialFlag.SpecialSubjectFlag)) {
					SpecialSubject indexPage = specialSubjectRepository.findOne(id);
					indexPage.setSubjectId(null);
					if (parent != null) {
						indexPage.setSubjectId(parentId);
					}
					indexPage.setSequence(sequence);
					specialSubjectRepository.save(indexPage);
				} else if (flag.equals(SpecialFlag.SpecialProjectFlag)) {
					SpecialProject indexTab = specialProjectRepository.findOne(id);
					if (parent != null) {
						indexTab.setSpecialSubject(parent);
						indexTab.setGroupId(parent.getId());
					} else {
						indexTab.setSpecialSubject(null);
						indexTab.setGroupId(null);
					}
					indexTab.setSequence(sequence);
						specialProjectRepository.save(indexTab);
				}
			}
			specialProjectRepository.flush();
			specialSubjectRepository.flush();
			return "success";
		} catch (Exception e) {
			throw new OperationException("移动失败：" + e.getMessage());
		}
	}

	@Override
	public Object moveListNew(String id,String pid,String typeFlag,String[] ids,int[] typeFlags) throws TRSException {
		User loginUser = UserUtils.getUser();
		//1、根据typeFlag判断是文件夹还是专题，决定去查SpecialSubject还是查SpecialProject
		if (StringUtil.isNotEmpty(id)){
			if ("0".equals(typeFlag) || "1".equals(typeFlag)){
				//2、如果是文件夹
				SpecialSubject subject = specialSubjectRepository.findOne(id);
				if (ObjectUtil.isEmpty(subject)){
					throw new TRSException(CodeUtils.FAIL,"未知id");
				}
				int subjectFlag = subject.getFlag();
				String subjectId = subject.getSubjectId();
				int sequence = subject.getSequence();
				if (StringUtil.isNotEmpty(pid)){
					//2-1、帮前端处理  一级下有二级的情况下不能拖到一级下成为二级
					List<SpecialSubject> specialSubjects = specialSubjectRepository.findBySubjectId(id);
					if (ObjectUtil.isNotEmpty(specialSubjects) || specialSubjects.size() > 0){
						throw new TRSException(CodeUtils.FAIL,"该菜单不能拖动！");
					}
					//2-2、有父级id，则为二级文件夹  typeFlag改为 1、subjectId放入pid
					subject.setFlag(1);

				}else {
					//2-2、无父级id，则为一级文件夹  typeFlag改为 0
					subject.setFlag(0);
				}
				subject.setSubjectId(pid);
				SpecialSubject specialSubject = specialSubjectRepository.save(subject);
				//修改原来等级的sequence
				if (specialSubject.getFlag() != subjectFlag){
					//2-3、若typeFlag与当前拖动id的不同，则修改拖动id原来级别的sequence，相同则按前端传的ids顺序修改sequence即步骤4
					if (StringUtil.isNotEmpty(subjectId)){
						//说明之前就是二级
						//根据一级栏目和位置查询
						//对文件夹进行排序
						changeSpecialSubject(subjectId,sequence);

						//对专题进行排序
						changeSpecialProject(subjectId,sequence);
						//对其他的进行排序end
					}else {
						//该专题是无父级专题
						changeOneSpecialOrOneSubject(loginUser,sequence);
					}

				}
			}else if ("2".equals(typeFlag)){
				//3、如果是专题
				SpecialProject specialProject = specialProjectService.findOne(id);
				if (ObjectUtil.isEmpty(specialProject)){
					throw new TRSException(CodeUtils.FAIL,"未知id");
				}
				String groupId = specialProject.getGroupId();
				int sequence = specialProject.getSequence();
				//3-1、专题固定typeFlag 为 2，groupId放入pid
				specialProject.setFlag(2);
				specialProject.setGroupId(pid);

				if (!StringUtils.equals(groupId,pid)){
					//3-2、若groupId与当前拖动id的pid不同，修改拖动id原来级别的sequence，相同则按前端传的ids顺序修改sequence即步骤4
					if (StringUtil.isNotEmpty(groupId)){
						//该专题为 一级或二级下的专题
						SpecialSubject specialSubject = specialSubjectRepository.findOne(groupId);
						if (StringUtil.isNotEmpty(specialSubject.getSubjectId())){
							//该专题为 二级下的专题
							//进行专题排序
							changeSpecialProject(groupId,sequence);
						}else {
							//该专题为一级下的专题
							//一级下二级文件夹排序
							changeSpecialSubject(groupId,sequence);
							//一级下的专题进行排序
							changeSpecialProject(groupId,sequence);
						}
					}else {
						//该专题无父级
						changeOneSpecialOrOneSubject(loginUser,sequence);
					}
				}

			}

			//4、处理拖动顺序
			changeSameLevelSequence(ids,typeFlags);

			return "success";
		}else {
			throw new TRSException(CodeUtils.FAIL,"未知id");
		}

	}

	@Override
	public Object crossLevelDragging(String parentId, String specialId, String ids, String twoOrThree) {

		if (StringUtils.isNotBlank(specialId)) {
			String[] specialIds = specialId.split(";");
			if (specialIds != null && specialIds.length == 2) {
				String subjectOrProject = specialIds[0];
				String id = specialIds[1];
				// 获取到当前拖动的分类或者专题
				if ("oneOrtwo".equals(subjectOrProject)) {
					SpecialSubject subject = specialSubjectRepository.findOne(id);
					int sequence = subject.getSequence();
					String subjectId = subject.getSubjectId();
					//根据一级栏目和位置查询
					Specification<SpecialSubject> specificationSpecialSubject = new Specification<SpecialSubject>() {
						@Override
						public Predicate toPredicate(Root<SpecialSubject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
							List<Object> predicates = new ArrayList<>();
							predicates.add(cb.equal(root.get("subjectId"),subjectId));
							predicates.add(cb.gt(root.get("sequence"), sequence));
							Predicate[] pre = new Predicate[predicates.size()];

							return query.where(predicates.toArray(pre)).getRestriction();
						}
					};
					//对其他的进行排序begin
					List<SpecialSubject> specialSubjectList = specialSubjectRepository.findAll(specificationSpecialSubject);
					for (SpecialSubject specialSubject : specialSubjectList) {
						specialSubject.setSequence(specialSubject.getSequence()-1);
					}
					specialSubjectRepository.save(specialSubjectList);
					Specification<SpecialProject> specificationSpecialProject = new Specification<SpecialProject>() {
						@Override
						public Predicate toPredicate(Root<SpecialProject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
							List<Object> predicates = new ArrayList<>();
							predicates.add(cb.equal(root.get("groupName"),subjectId));
							predicates.add(cb.gt(root.get("sequence"), sequence));
							Predicate[] pre = new Predicate[predicates.size()];

							return query.where(predicates.toArray(pre)).getRestriction();
						}
					};
					List<SpecialProject> specialProjectList = specialProjectRepository.findAll(specificationSpecialProject);
					for (SpecialProject specialProject : specialProjectList) {
						specialProject.setSequence(specialProject.getSequence()-1);
					}
					specialProjectRepository.save(specialProjectList);
					//对其他的进行排序end
					
					subject.setSubjectId(parentId);
					specialSubjectRepository.save(subject);
					moveList(ids, twoOrThree);
				} else if ("three".equals(subjectOrProject)) {
					SpecialProject special = specialProjectService.findOne(id);
					int sequence = special.getSequence();
					String groupId = special.getGroupId();
					//根据一级栏目和位置查询
					Specification<SpecialSubject> specificationSpecialSubject = new Specification<SpecialSubject>() {
						@Override
						public Predicate toPredicate(Root<SpecialSubject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
							List<Object> predicates = new ArrayList<>();
							predicates.add(cb.equal(root.get("subjectId"),groupId));
							predicates.add(cb.gt(root.get("sequence"), sequence));
							Predicate[] pre = new Predicate[predicates.size()];

							return query.where(predicates.toArray(pre)).getRestriction();
						}
					};
					//对其他的进行排序begin
					List<SpecialSubject> specialSubjectList = specialSubjectRepository.findAll(specificationSpecialSubject);
					for (SpecialSubject specialSubject : specialSubjectList) {
						specialSubject.setSequence(specialSubject.getSequence()-1);
					}
					specialSubjectRepository.save(specialSubjectList);
					Specification<SpecialProject> specificationSpecialProject = new Specification<SpecialProject>() {
						@Override
						public Predicate toPredicate(Root<SpecialProject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
							List<Object> predicates = new ArrayList<>();
							predicates.add(cb.equal(root.get("groupName"),groupId));
							predicates.add(cb.gt(root.get("sequence"), sequence));
							Predicate[] pre = new Predicate[predicates.size()];

							return query.where(predicates.toArray(pre)).getRestriction();
						}
					};
					List<SpecialProject> specialProjectList = specialProjectRepository.findAll(specificationSpecialProject);
					for (SpecialProject specialProject : specialProjectList) {
						specialProject.setSequence(specialProject.getSequence()-1);
					}
					specialProjectRepository.save(specialProjectList);
					//对其他的进行排序end
					special.setGroupId(parentId);
					specialProjectService.save(special);
					moveList(ids, twoOrThree);
				}
			}
		}

		return "success";
	}



	private List<SpecialProject> removeSomeProjects(List<SpecialProject> specialProjects,User user){
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			List<SpecialProject> listRemove = new ArrayList<>();
			for (SpecialProject specialProject : specialProjects) {
				if (StringUtil.isNotEmpty(specialProject.getSubGroupId())) {
					listRemove.add(specialProject);
				}

			}
			specialProjects.removeAll(listRemove);
		}
		return specialProjects;
	}

	/**
	 * 修改某用户或用户分组下一级专题或一级文件夹顺序
	 * @param loginUser
	 * @param sequence
	 */
	private void changeOneSpecialOrOneSubject(User loginUser,int sequence){
		//查一级文件夹
		Criteria<SpecialSubject> criteria = new Criteria<>();
		criteria.add(Restrictions.eq("flag", 0));
		if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
			criteria.add(Restrictions.eq("userId", loginUser.getId()));
		}else {
			criteria.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
		}
		criteria.add(Restrictions.gt("sequence",sequence));
		criteria.orderByASC("sequence");
		List<SpecialSubject> listSubject = specialSubjectRepository.findAll(criteria);
		for (SpecialSubject specialSub : listSubject) {
			specialSub.setSequence(specialSub.getSequence()-1);
		}
		specialSubjectRepository.save(listSubject);

		// 查专题
		Criteria<SpecialProject> criteriaProject2 = new Criteria<>();
		if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
			criteriaProject2.add(Restrictions.eq("userId", loginUser.getId()));
		}else {
			criteriaProject2.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
		}
		criteriaProject2.add(Restrictions.gt("sequence",sequence));
		criteriaProject2.orderByASC("sequence");
		List<SpecialProject> listSpecialProject = specialProjectService.findAll(criteriaProject2);
		List<SpecialProject> noGroupAndTop = new ArrayList<>();
		for (SpecialProject special : listSpecialProject) {
			//无父级且不是置顶专题
			if (StringUtil.isEmpty(special.getGroupId()) && !"top".equals(special.getTopFlag())) {
				noGroupAndTop.add(special);
			}
		}
		for (SpecialProject specialProject : noGroupAndTop) {
			specialProject.setSequence(specialProject.getSequence()-1);
		}
		specialProjectRepository.save(noGroupAndTop);
	}

	/**
	 * 对一级下的二级文件夹进行排序
	 * @param subjectId
	 * @param sequence
	 */
	private void  changeSpecialSubject(String subjectId,int sequence){
		Specification<SpecialSubject> specificationSpecialSubject = new Specification<SpecialSubject>() {
			@Override
			public Predicate toPredicate(Root<SpecialSubject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Object> predicates = new ArrayList<>();
				predicates.add(cb.equal(root.get("subjectId"),subjectId));
				predicates.add(cb.gt(root.get("sequence"), sequence));
				Predicate[] pre = new Predicate[predicates.size()];

				return query.where(predicates.toArray(pre)).getRestriction();
			}
		};
		//对其他的进行排序begin
		List<SpecialSubject> specialSubjectList = specialSubjectRepository.findAll(specificationSpecialSubject);
		for (SpecialSubject specialSub : specialSubjectList) {
			specialSub.setSequence(specialSub.getSequence()-1);
		}
		specialSubjectRepository.save(specialSubjectList);
	}
	/**
	 * 对某级下的专题进行排序
	 * @param groupId
	 * @param sequence
	 */
	private void  changeSpecialProject(String groupId,int sequence){
		Specification<SpecialProject> specificationSpecialProject = new Specification<SpecialProject>() {
			@Override
			public Predicate toPredicate(Root<SpecialProject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Object> predicates = new ArrayList<>();
				predicates.add(cb.equal(root.get("groupId"),groupId));
				predicates.add(cb.gt(root.get("sequence"), sequence));
				Predicate[] pre = new Predicate[predicates.size()];

				return query.where(predicates.toArray(pre)).getRestriction();
			}
		};
		List<SpecialProject> specialProjectList = specialProjectRepository.findAll(specificationSpecialProject);
		for (SpecialProject specialProject : specialProjectList) {
			specialProject.setSequence(specialProject.getSequence()-1);
		}
		specialProjectRepository.save(specialProjectList);
	}

	/**
	 * 专题分析左侧列表修改同级别sequence
	 * @param ids
	 * @param typeFlags
	 * @throws TRSException
	 */
	private void changeSameLevelSequence(String[] ids, int[] typeFlags) throws TRSException {
		if (ObjectUtil.isNotEmpty(ids)){
			if (ObjectUtil.isNotEmpty(ids) && ObjectUtil.isNotEmpty(typeFlags) && ids.length == typeFlags.length){
				//4-1、遍历ids
				for (int i = 0; i < ids.length; i++) {
					//4-1-1、判断是文件夹还是专题
					if (0 == typeFlags[i] || 1 == typeFlags[i]) {
						//4-1-1-1、文件夹 , 查询SpecialSubject，修改sequence
						SpecialSubject subject = specialSubjectRepository.findOne(ids[i]);
						subject.setSequence(i + 1);
						specialSubjectRepository.save(subject);
					} else if (2 == typeFlags[i]) {
						//4-1-1-2、专题 , 查询SpecialProject，修改sequence
						SpecialProject special = specialProjectService.findOne(ids[i]);
						special.setSequence(i + 1);
						specialProjectService.save(special);
					}
				}
			}else {
				throw new TRSException(CodeUtils.FAIL,"fail");
			}
		}

	}

	private Map<String,Object> forSpeicalProjectToMap(SpecialProject specialProject){
		if (ObjectUtil.isNotEmpty(specialProject)){
			Map<String, Object> map = new HashMap<>();
			map.put("id",specialProject.getId());
			map.put("specialName",specialProject.getSpecialName());
			map.put("source",specialProject.getSource());
			map.put("startTime",specialProject.getStartTime());
			map.put("endTime",specialProject.getEndTime());
			map.put("timeRange",specialProject.getTimeRange());
			map.put("flag",specialProject.getFlag());
			map.put("flagColor",false);
			map.put("weight",specialProject.isWeight());

			return map;
		}
		return null;
	}

}
