package com.trs.netInsight.widget.report.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.report.constant.Chapter;
import com.trs.netInsight.widget.report.entity.*;
import com.trs.netInsight.widget.report.entity.repository.ReportDataNewRepository;
import com.trs.netInsight.widget.report.entity.repository.ReportNewRepository;
import com.trs.netInsight.widget.report.entity.repository.ReportResourceRepository;
import com.trs.netInsight.widget.report.entity.repository.TemplateNewRepository;
import com.trs.netInsight.widget.report.service.IGenerateReport;
import com.trs.netInsight.widget.report.service.IReportServiceNew;
import com.trs.netInsight.widget.report.service.ISpecialReportService;
import com.trs.netInsight.widget.report.task.ReportResourceTask;
import com.trs.netInsight.widget.report.util.ReportUtil;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.trs.netInsight.widget.report.constant.ReportConst.*;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by shao.guangze on 2018年5月24日 下午6:02:27
 * 所有模板操作、日报周报月报类操作
 */
@Service
@Slf4j
public class ReportServiceNewImpl implements IReportServiceNew {

	@Autowired
	private TemplateNewRepository templateNewRepository;

	@Autowired
	private ReportResourceRepository reportResourceRepository;

	@Autowired
	private ReportNewRepository reportNewRepository;
	
	@Autowired
	private ReportDataNewRepository reportDataNewRepository;

	@Autowired
	private IGenerateReport generateReportImpl;

	@Autowired
	private ISpecialReportService sepcialReportService;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private FullTextSearch hybase8SearchService;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * 单拿一个线程计算数据
	 * 日报月报周报 添加报告资源时需要
	 * 专题报计算数据时需要
	 * */
	private static ExecutorService fixedThreadPool = Executors
			.newFixedThreadPool(5);
	
	@Override
	public String saveTemplate(String templateId, String templateName,String reportName, String templateList, String templateType,
							   String totalIssue, String thisIssue, String preparationUnits, String preparationAuthors,
							   String statisticsTime) {
		User user = UserUtils.getUser();
		if (StringUtils.isEmpty(templateId)
				&& StringUtils.isEmpty(templateList)) {
			// 用户未选中模板内容就点击保存
			return FAIL;
		} else if (StringUtils.isEmpty(templateId)) {
			// 新增
			List<TemplateNew> allTemplate = null;
			if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
				allTemplate = templateNewRepository.findByUserIdAndTemplateType(user.getId(), templateType, new Sort(Direction.DESC,"templatePosition"));
			}else {
				allTemplate = templateNewRepository.findBySubGroupIdAndTemplateType(user.getSubGroupId(), templateType, new Sort(Direction.DESC,"templatePosition"));
			}

			TemplateNew templateNew = new TemplateNew();
			templateNew.setTemplateName(templateName);
			if(allTemplate == null || allTemplate.size() == 0){
				templateNew.setTemplatePosition(1);
			}else{
				templateNew.setTemplatePosition(allTemplate.get(0).getTemplatePosition() + 1);
			}
			templateNew.setTemplateList(templateList);
			templateNew.setTemplateType(templateType);
			templateNew.setUserId(UserUtils.getUser().getId());
			setTemplateHeader(templateNew, totalIssue, thisIssue, preparationUnits, preparationAuthors, statisticsTime,reportName);
			// 同mybatis类似，结果映射到对象中
			templateNewRepository.save(templateNew);
		} else {
			//编辑
			TemplateNew templateNew = templateNewRepository.findOne(templateId);
			templateNew.setTemplateName(templateName);
			templateChangedHandle(templateNew,templateList);
			String templateListFormated = templateListPositionHandle(templateList);
			templateNew.setTemplateList(templateListFormated);
			templateNew.setTemplateType(templateType);
			setTemplateHeader(templateNew, totalIssue, thisIssue, preparationUnits, preparationAuthors, statisticsTime,reportName);
			templateNewRepository.save(templateNew);
		}
		return Const.SUCCESS;
	}

	/***
	 * 将原有报告头的内容移到了模板头中
	 * @param templateNew
	 * @param totalIssue
	 * @param thisIssue
	 * @param preparationUnits
	 * @param preparationAuthors
	 * @param statisticsTime
	 */
	private void setTemplateHeader(TemplateNew templateNew, String totalIssue, String thisIssue, String preparationUnits,
								   String preparationAuthors, String statisticsTime,String reportName) {
		ReportNew report = new ReportNew.Builder()
				.withTotalIssue(totalIssue)
				.withThisIssue(thisIssue)
				.withPreparationUnits(preparationUnits)
				.withPreparationAuthors(preparationAuthors)
				.withStatisticsTime(statisticsTime).withReportName(reportName).build();
		templateNew.setTemplateHeader(JSON.toJSONString(report));
	}

	/***
	 *	 修改模板的时候，如果当前模板下有数据，该数据也应一并挪动位置
	 * @param templateNew	original template
	 * @param templateList	current template
	 */
	private void templateChangedHandle(TemplateNew templateNew, String templateList) {
		List<ReportResource> allReportResource = reportResourceRepository.findByTemplateIdAndResourceStatus(templateNew.getId(), 0);
		if(CollectionUtils.isEmpty(allReportResource)){
		    return;
        }
//		Map<Integer, List<ReportResource>> collect = allReportResource.stream().collect(Collectors.groupingBy(ReportResource::getChapterPosition));
//		Iterator<Integer> iterator = collect.keySet().iterator();
//		List<TElementNew> originalElements = JSONArray.parseArray(templateNew.getTemplateList(), TElementNew.class);
		List<TElementNew> currentElements = JSONArray.parseArray(templateList, TElementNew.class);
		ArrayList<ReportResource> resources2DB = new ArrayList<>();
		for(TElementNew currentElement:currentElements){
			List<ReportResource> reportResources = currentElement.getChapaterContent();
			if(reportResources!=null){
				for(int i =0;i<reportResources.size();i++){
					if(!reportResources.get(i).getChapterPosition().equals(currentElement.getChapterPosition())){
						reportResources.get(i).setChapterPosition(currentElement.getChapterPosition());
					}
				}
				resources2DB.addAll(reportResources);
			}
		}

		if(!CollectionUtils.isEmpty(resources2DB)){
			reportResourceRepository.save(resources2DB);
		}
	}

	/**
	 * 修改模板操作时，这里特指模块顺序改变
	 * 前端只是按照各模块的修改后的数据位置传给了后台
	 * 并没有更改各模块中的chapterPosition。
	 * 在这里进行更改chapterPosition.
	 * */
	private String templateListPositionHandle(String templateList) {
		List<TElementNew> elements = JSONArray.parseArray(templateList, TElementNew.class);
		if(elements != null){
			for(int i = 0 ; i <elements.size() ; i ++){
				TElementNew element = elements.get(i);
				elements.remove(element);
				element.setChapterPosition(i+1);
				elements.add(i,element);
			}
		}
		return JSON.toJSONString(elements);
	}

	@Override
	public String saveSpecialTemplate(String templateId, String templateName,
			String templateList, String groupName) {
		if (StringUtils.isEmpty(templateId)
				&& StringUtils.isEmpty(templateList)) {
			// 用户未选中模板内容就点击保存
			return FAIL;
		} else if (StringUtils.isEmpty(templateId)) {
			// 新增
			List<TemplateNew> allTemplate = templateNewRepository.findByUserIdAndTemplateType(UserUtils.getUser().getId(), "专报", new Sort(Direction.DESC,"templatePosition"));
			TemplateNew templateNew = new TemplateNew();
			templateNew.setTemplateName(templateName);
			if(allTemplate == null || allTemplate.size() == 0){
				templateNew.setTemplatePosition(1);
			}else{
				templateNew.setTemplatePosition(allTemplate.size() + 1);
			}
			//JSONArray.parse(text)
			templateNew.setTemplateList(templateList);
			templateNew.setTemplateType("专报");
			templateNew.setGroupName(groupName);
			templateNew.setUserId(UserUtils.getUser().getId());
			// 同mybatis类似，结果映射到对象中
			templateNewRepository.save(templateNew);
		} else {
			TemplateNew templateNew = templateNewRepository.findOne(templateId);
			templateNew.setTemplateName(templateName);
			templateNew.setTemplateList(templateList);
			templateNewRepository.save(templateNew);
		}
		return Const.SUCCESS;
	}

	@Override
	public String saveCustomTemplate(String templateId, String templateName, String templateList, String templateType) {
		if (StringUtils.isEmpty(templateId) && StringUtils.isEmpty(templateList)){
			//用户未选中模板内容就保存
			return FAIL;
		}else if (StringUtils.isEmpty(templateId)){
			User loginUser = UserUtils.getUser();
			//新增
			List<TemplateNew> simplerTemplates = null;
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				simplerTemplates = templateNewRepository.findByUserIdAndTemplateType(loginUser.getId(), templateType, new Sort(Direction.DESC, "templatePosition"));
			}else {
				simplerTemplates = templateNewRepository.findBySubGroupIdAndTemplateType(loginUser.getSubGroupId(), templateType, new Sort(Direction.DESC, "templatePosition"));
			}
			TemplateNew templateNew = new TemplateNew();
			templateNew.setTemplateName(templateName);
			if (simplerTemplates == null || simplerTemplates.size() == 0){
				templateNew.setTemplatePosition(1);
			}else {
				templateNew.setTemplatePosition(simplerTemplates.size()+1);
			}
			templateNew.setTemplateList(templateList);
			templateNew.setTemplateType(templateType);
			templateNew.setUserId(loginUser.getId());
			templateNew.setSubGroupId(loginUser.getSubGroupId());
			// 同mybatis类似，结果映射到对象中
			templateNewRepository.save(templateNew);
		}else {
			//编辑
			TemplateNew templateNew = templateNewRepository.findOne(templateId);
			templateNew.setTemplateName(templateName);
			templateNew.setTemplateList(templateList);
			templateNew.setTemplateType(templateType);
			templateNewRepository.save(templateNew);
		}
		return Const.SUCCESS;
	}

	@Override
	public List<TemplateNew4Page> findAllTemplate(String templateType, String groupName) {
		if (StringUtils.isEmpty(templateType)) {
			return null;
		}
			//日报、周报、月报、专报
		User loginUser = UserUtils.getUser();
		List<TemplateNew> templateList = null;
		if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
			templateList = templateNewRepository.findByUserIdAndTemplateType(loginUser.getId(), templateType, new Sort(Direction.DESC,"templatePosition"));

		}else {
			templateList = templateNewRepository.findBySubGroupIdAndTemplateType(loginUser.getSubGroupId(), templateType, new Sort(Direction.DESC,"templatePosition"));
		}
		if(templateList == null || templateList.size() == 0){
			TemplateNew template = addDefaultTemplate(templateType,groupName);
			templateNewRepository.save(template);
			templateList.add(template);
		}
		return handleTemplateList(templateList);
	}

	private String getPreparedUnion(){
		String organizationId = UserUtils.getUser().getOrganizationId();
		if(StringUtil.isNotEmpty(organizationId)){
			Organization organization = organizationRepository.findOne(organizationId);
			return organization == null ? "XX编制单位" : organization.getOrganizationName();
		}
		return "XX编制单位";
	}
	private TemplateNew addDefaultTemplate(String templateType,
			String groupName) {
        TemplateNew templateNew = new TemplateNew();
        templateNew.setTemplatePosition(1);
		templateNew.setGroupName(groupName);
		templateNew.setIsDefault(1);
		String preparedAuthor = UserUtils.getUser().getDisplayName();
		if(SPECIALREPORT.equals(templateType)){
			templateNew.setTemplateList(JSON.toJSONString(ReportUtil.createEmptyTemplateForSpecial(1)));
			templateNew.setTemplateType(SPECIALREPORT);
			templateNew.setTemplateName(DEFAULTSPECIALTEMPLATE);
			return templateNew;
		}else if(INDEXTABREPORT.equals(templateType)){
			templateNew.setTemplateList(JSON.toJSONString(ReportUtil.createEmptyTemplateForIndexTab(1)));
			templateNew.setTemplateType(INDEXTABREPORT);
			templateNew.setTemplateName(DEFAULTINDEXTABTEMPLATE);
			return templateNew;
		}else if(DAILYREPORT.equals(templateType)){
			templateNew.setTemplateList(JSON.toJSONString(ReportUtil.createEmptyTemplate(1)));
			setTemplateHeader(templateNew,new String("30"), new String("1") ,getPreparedUnion(), preparedAuthor, "24h",DEFAULTDAILYTEMPLATE);
			templateNew.setTemplateType(DAILYREPORT);
			templateNew.setTemplateName(DEFAULTDAILYTEMPLATE);
			return templateNew;
		}else if(WEEKLYREPORT.equals(templateType)){
			templateNew.setTemplateList(JSON.toJSONString(ReportUtil.createEmptyTemplate(1)));
			setTemplateHeader(templateNew,new String("4"), new String("1") ,getPreparedUnion(), preparedAuthor, "7d",DEFAULTWEEKLYTEMPLATE);
			templateNew.setTemplateType(WEEKLYREPORT);
			templateNew.setTemplateName(DEFAULTWEEKLYTEMPLATE);
			return templateNew;
		}else{
			templateNew.setTemplateList(JSON.toJSONString(ReportUtil.createEmptyTemplate(1)));
			setTemplateHeader(templateNew,new String("12"), new String("1") ,getPreparedUnion(), preparedAuthor, "30d",DEFAULTMONTHLYTEMPLATE);
			templateNew.setTemplateType(MONTHLYREPORT);
            templateNew.setTemplateName(DEFAULTMONTHLYTEMPLATE);
			return templateNew;
		}
	}

	@Override
	public String deleteTemplate(String templateId) {
		if (StringUtils.isEmpty(templateId)) {
			return FAIL;
		}
		templateNewRepository.delete(templateId);
		//删除模板下的资源
		reportResourceRepository.deleteByTemplateId(templateId);
		return Const.SUCCESS;
	}
	/**
	 * 该操作主要是处理返回前端的数据格式，因为在添加资源时就顺带sid -> hybaseSearch -> db
	 * */
	@Override
	public List<TElementNew> listAllReportResource(String reportType, String templateId) {

		List<ReportResource> allReportResource = reportResourceRepository.findByTemplateIdAndResourceStatus(templateId, 0);
		Map<?, List<ReportResource>> collect;
		if(judgeNoPositionChapter(allReportResource)){
			collect = allReportResource.stream().collect(Collectors.groupingBy(ReportResource::getChapterPosition));
		}else{
			//这里只是保证旧版的，即没有重复报告章节的还能继续使用
			collect = allReportResource.stream().collect(Collectors.groupingBy(ReportResource::getChapter));
		}
		TemplateNew template = templateNewRepository.findOne(templateId);

		return  ReportUtil.setResoucesIntoElements(template, collect);
		//return allReportResource.isEmpty() ? null : ReportUtil.setResoucesIntoElements(template, collect);
	}

	/***
	 * 遍历报告资源，如果有chapterPosition则返回true
	 * @param allReportResource
	 * @return
	 */
	private boolean judgeNoPositionChapter(List<ReportResource> allReportResource) {
		AtomicBoolean flag = new AtomicBoolean(false);
		allReportResource.stream().forEach(e -> {
			if(e.getChapterPosition() != null){
				flag.set(true);
				return;
			}
		});
		return flag.get();
	}


	private Integer getChapterPosition(TemplateNew template, String chapterName) {
		String templateList = template.getTemplateList();
		List<TElementNew> elements = JSONArray.parseArray(templateList,
				TElementNew.class);
		for (TElementNew element : elements) {
			if (element.getChapterName().equals(chapterName)) {
				return element.getChapterPosition();
			}
		}
		return null;
	}


	@Override
	public Object saveReportResource(String sids,String trslk, String userId,
									 String groupName, String chapter, String img_data, String reportType, String templateId,
									 String imgType, Integer chapterPosition, String reportId,String mapto)  throws Exception{
		String[] sidArray = null;
		String[] groupNameArray = null;
		if(StringUtil.isNotEmpty(groupName) && StringUtil.isNotEmpty(sids)){
			sidArray = sids.split(SEMICOLON);
			groupNameArray = groupName.split(SEMICOLON);
			if (groupNameArray.length != sidArray.length) {
				return "fail";
			}
		}
		boolean isResourceSim;
		List<ReportResource> allReportResources = reportResourceRepository.findByTemplateIdAndResourceStatus(templateId, 0);
		List<ReportResource> reportResourcesList = new ArrayList<>();
		if(StringUtil.isNotEmpty(img_data)){
			//准备插入图表数据
			insertImgDataIntoResources(userId, chapter, img_data,null, null,
					reportType, templateId, imgType, chapterPosition, reportId, reportResourcesList,mapto);
		}else{
			for (int i = 0; i < sidArray.length; i++) {
				// 排重，资源池列表中没有此文章（即sid）或该文章没有被删除才执行add
				// 模板id是UUID，所以不再需要通过userId来锁定用户，
				// 必须加入模板条件，不然如果A模板的1章节由数据①(报告未生成)，那么向B模板的1章节中插入数据①将会提示已插入。
			/*ReportResource reportResource = reportResourceRepository
					.findBySidAndTemplateIdAndChapterPosition(sidArray[i], templateId, chapterPosition);*/
				//
				isResourceSim = judgeListResourceSim(allReportResources, sidArray[i], chapterPosition);

				if (isResourceSim) {
					List<ReportResource> thisChapterList = reportResourceRepository.findByTemplateIdAndChapter(templateId, chapter);
//					if("Hot_News_List".equals(chapter)) {
//						if (thisChapterList.size() > 0) {
//							for (ReportResource reportResource : thisChapterList)
//								reportResourceRepository.delete(reportResource);
//						}
//					}
//					List<ReportResource> newChapterList = reportResourceRepository.findByTemplateIdAndChapter(templateId, chapter);
					Integer docPosition = 0;
					if(thisChapterList.size() == 0){
						docPosition = -1;
					}else{
						docPosition = thisChapterList.size() + 1 + i;
					}
					//准备插入列表数据
					insertListDataIntoResources(docPosition, sidArray, i ,userId, groupNameArray, chapter, img_data, null,
							reportType, templateId, imgType, chapterPosition, reportId, reportResourcesList);
				}
			}
		}
		List<ReportResource> thisChapterList = reportResourceRepository.findByTemplateIdAndChapter(templateId, chapter);
		//资源池中完全没有数据，新加入数据时走这里
		if(thisChapterList.size() == 0){
			for(int i = 0; i < reportResourcesList.size(); i++){
				reportResourcesList.get(i).setDocPosition(i + 1);
			}
		}
		//区分加入报告资源池和列表预览资源池
		if(StringUtil.isEmpty(reportId)){
			fixedThreadPool.execute(new ReportResourceTask(reportResourcesList,trslk));
		}else{
			ReportResourceTask reportResourceTask = new ReportResourceTask();
			List<ReportResource> list = reportResourceTask.reportResourceHandle(reportResourcesList,trslk);
			reportResourceRepository.save(list);
			log.info(String.format(REPORTRESOURCELOG,"成功保存至数据库，size："+list.size()));
			return list;
		}

		if(reportResourcesList == null || reportResourcesList.size()==0){
			return "ALLSIMILAR";
		}else{
			return Const.SUCCESS;
		}
	}

	@Override
	public Object saveOverView(String userId, String chapter,String imgComment, String reportType, String templateId) throws Exception {
		List<ReportResource> reportResourcesList = new ArrayList<>();
		Chapter c = Chapter.valueOf(chapter);
		//准备插入图表数据
		insertImgDataIntoResources(userId, chapter, null, imgComment,null,
				reportType, templateId, ColumnConst.CHART_BAR, c.getSequence(), null, reportResourcesList,"");
		fixedThreadPool.execute(new ReportResourceTask(reportResourcesList));
		if(reportResourcesList == null || reportResourcesList.size()==0){
			return "ALLSIMILAR";
		}else{
			return Const.SUCCESS;
		}
	}

	/***
	 * 向 日报、周报、月报资源池或预览资源中添加Img Type Resource Data
	 * @param userId
	 * @param chapter
	 * @param img_data
	 * @param secondaryChapter
	 * @param reportType
	 * @param templateId
	 * @param imgType
	 * @param chapterPosition
	 * @param reportId
	 * @param reportResourcesList
	 */
	private void insertImgDataIntoResources( String userId,
											 String chapter, String img_data,String imgComment, String secondaryChapter,
											 String reportType, String templateId, String imgType,
											 Integer chapterPosition, String reportId, List<ReportResource> reportResourcesList,String mapto) {
		ReportResource newAdd = new ReportResource();
		newAdd.setChapter(chapter);
		newAdd.setSecondaryChapter(secondaryChapter);
		newAdd.setReportType(reportType);
		newAdd.setTemplateId(templateId);
		newAdd.setImg_data(img_data);
		newAdd.setImgComment(imgComment);
		newAdd.setImgType(imgType);
		newAdd.setUserId(UserUtils.getUser().getId());
		newAdd.setChapterPosition(chapterPosition);
		newAdd.setResourceStatus(StringUtil.isEmpty(reportId) ? 0 : 1);
		newAdd.setReportId(reportId);
		newAdd.setUserId(userId);
		newAdd.setMapto(mapto);
		reportResourcesList.add(newAdd);
	}

	/***
	 * 向 日报、周报、月报资源池或预览资源中添加List Type Resource Data
	 * @param sidArray
	 * @param i
	 * @param userId
	 * @param groupNameArray
	 * @param chapter
	 * @param img_data
	 * @param secondaryChapter
	 * @param reportType
	 * @param templateId
	 * @param imgType
	 * @param chapterPosition
	 * @param reportId
	 * @param reportResourcesList
	 */
	private void insertListDataIntoResources(Integer docPosition, String[] sidArray, int i, String userId, String[] groupNameArray,
											 String chapter, String img_data, String secondaryChapter,
											 String reportType, String templateId, String imgType,
											 Integer chapterPosition, String reportId, List<ReportResource> reportResourcesList) {
		ReportResource newAdd = new ReportResource(sidArray[i], userId,
				groupNameArray[i], chapter, img_data);
		newAdd.setDocPosition(docPosition);
		newAdd.setSecondaryChapter(secondaryChapter);
		newAdd.setReportType(reportType);
		newAdd.setTemplateId(templateId);
		newAdd.setImgType(imgType);
		newAdd.setChapterPosition(chapterPosition);
		newAdd.setResourceStatus(StringUtil.isEmpty(reportId) ? 0 : 1);
		newAdd.setReportId(reportId);
		reportResourcesList.add(newAdd);
//		reportResourceRepository.save(newAdd);
	}

	/***
	 *
	 * @param allReportResources
	 * @param sid
	 * @param chapterPosition
	 * @return collect中元素为0时说明没有重复元素,此时返回true
	 */
	private boolean judgeListResourceSim(List<ReportResource> allReportResources, String sid, Integer chapterPosition) {
		List<ReportResource> collect = allReportResources.stream().filter(e -> (e.getSid() == sid) && (e.getChapterPosition() == chapterPosition)).collect(Collectors.toList());
		return collect.isEmpty();
	}

	/**
	 * 	正确返回true
	 * @param groupName		数据来源
	 * @param chapter		章节
	 * @return
	 */
	@Deprecated
	private boolean judgeGroupAndChapter(String groupName, String chapter, String templateId) {
        TemplateNew template = templateNewRepository.findOne(templateId);
        List<TElementNew> elements = JSONArray.parseArray(template.getTemplateList(), TElementNew.class);
        TElementNew element = elements.stream().filter(e -> chapter.equals(e.getChapterName())).collect(Collectors.toList()).get(0);
        if(groupName != null){
			//此时为列表资源
            switch (element.getChapterDetail()){
                case "NEWSTOP10":
                    return "国内新闻".equals(groupName) || "国外新闻".equals(groupName) || "国内论坛".equals(groupName) || "国内博客".equals(groupName) || "客户端".equals(groupName) || "电子报".equals(groupName);
                case "WEIBOTOP10":
                    return "微博".equals(groupName);
                case "WECHATTOP10":
                    return "国内微信".equals(groupName);
                case "NEWSHOTTOPICS":
                    return "国内新闻".equals(groupName) || "国外新闻".equals(groupName) || "国内论坛".equals(groupName) || "国内博客".equals(groupName) || "客户端".equals(groupName) || "电子报".equals(groupName);
                case "WEIBOHOTTOPICS":
                    return "微博".equals(groupName);
                default:
                    break;
            }
		}
		return true;
	}

	@Override
	public Object delReportResource(String resourceId) {
		String[] resourceIdArry = resourceId.split(SEMICOLON);
		for (String resourceIdOne : resourceIdArry) {
			//删除之后排序顺序字段更新
			//通过模块的类型确定size，再减一
			ReportResource reportResource = reportResourceRepository.findOne(resourceIdOne);
			//该模块的类型
			String chapter = reportResource.getChapter();
			//该模板id
			String templateId = reportResource.getTemplateId();
			//该文章的位置
			Integer position = reportResource.getDocPosition();
			//该用户、这个资源模块的文章集合
			List<ReportResource> docList = reportResourceRepository.findByTemplateIdAndChapter(templateId, chapter);
			//查询数据的时候做了  位置信息为空的时候就赋值，所以这里不会出现为空的情况了
			for(int i = 0; i < docList.size(); i++){
				int intValue = docList.get(i).getDocPosition().intValue();
				if(intValue > position){
					docList.get(i).setDocPosition(intValue - 1);	//hibernate的游离态
					reportResourceRepository.save(docList.get(i));  //持久态
				}
			}
			reportResourceRepository.delete(resourceIdOne);
		}
		return Const.SUCCESS;
	}

	@Override
	public Object updateReportResource(String id, String imgComment) {
		ReportResource reportResource = reportResourceRepository.findOne(id);
		if (null != reportResource){
			reportResource.setImgComment(imgComment);
			reportResource = reportResourceRepository.save(reportResource);
		}
		return reportResource;
	}

	public static void main(String[] args) {
        System.out.println("ABC".equals(null));
        System.out.println(Chapter.values()[0].getValue());
        System.out.println(Chapter.values()[0].getValueType());
    }

	@Override
	public List<Object> preview(String reportName, String totalIssue,
			String thisIssue, String preparationUnits,
			String preparationAuthors, String templateId, String reportIntro, String statisticsTime, Integer resourceDeleted) throws Exception {
		//除了报告头、报告简介之外的数据
		List<TElementNew> result = this.listAllReportResource(null, templateId);

		//给前端返序号
		List<String> orderList = new ArrayList<>();
		for(int i=0 ;i<result.size();i++){
			orderList.add(ROMAN2CHINESE.get(i+1));
		}
		
		ReportNew report = new ReportNew.Builder().withReportName(reportName)
				.withTotalIssue(totalIssue)
				.withThisIssue(thisIssue)
				.withPreparationUnits(preparationUnits)
				.withPreparationAuthors(preparationAuthors)
				.withTemplateId(templateId)
				.withStatisticsTime(ReportUtil.statisticsTimeHandle(statisticsTime))
				.withResourceDeleted(resourceDeleted).build();
		reportNewRepository.save(report);
		
		Map<String,String> map = new HashMap<>();
		map.put("reportIntro", reportIntro);
		List<Object> resultList = new ArrayList<>();
		//这里返回的是List，前端写死了该list中0、1、2的内容分别是什么
		resultList.add(report);	//报告头内容
		resultList.add(result); //报告各个章节内容
		resultList.add(map);	//报告简介内容
		resultList.add(orderList);
		return resultList;
	}



	@Override
	public List<TemplateNew4Page> findEmptyTemplate(String reportType) {
		List<TemplateNew4Page> arrayList = new ArrayList<>();
		TemplateNew4Page templateNew = new TemplateNew4Page();
		if (SPECIALREPORT.equals(reportType)){
			templateNew.setTemplateListData(ReportUtil.createEmptyTemplateForSpecial(1));
		}else {
			templateNew.setTemplateListData(ReportUtil.createEmptyTemplate(1));
		}
		templateNew.setIsDefault(0);
		arrayList.add(templateNew);
		return arrayList;
	}
	
	/**
	 *转换数据为前端需要的格式
	 *
	 * @author shao.guangze
	 * @param templateList
	 * @return
	 */
	private List<TemplateNew4Page> handleTemplateList(List<TemplateNew> templateList){
		List<TemplateNew4Page> templateNew4PageList = new ArrayList<>();
		TemplateNew4Page templateNew4Page ;
		//String jsonData = null;
		for(TemplateNew templateNew : templateList){
			templateNew4Page = new TemplateNew4Page();
			templateNew4Page.setTemplateId(templateNew.getId());
			templateNew4Page.setTemplateName(templateNew.getTemplateName());
			templateNew4Page.setUserId(templateNew.getUserId());
			templateNew4Page.setTemplateType(templateNew.getTemplateType());
			List<TElementNew> parseArray = JSONArray.parseArray(templateNew.getTemplateList(), TElementNew.class);
			for (TElementNew tElementNew : parseArray){
				switch (tElementNew.getChapterDetail()){
					case OVERVIEWOFDATANew:
						tElementNew.setChapterTabName(OVERVIEWOFDATANew);
						break;
					case NEWSHOTTOP10key:
						tElementNew.setChapterTabName(NEWSHOTTOPICSkey);
						break;
					case WEIBOHOTTOP10key:
						tElementNew.setChapterTabName(NEWSHOTTOPICSkey);
						break;
					case WECHATHOTTOP10key:
						tElementNew.setChapterTabName(NEWSHOTTOPICSkey);
						break;
					case WEMEDIAkey:
						tElementNew.setChapterTabName(NEWSHOTTOPICSkey);
						break;
					case WECHATEVENTCONTEXTkey:
						tElementNew.setChapterTabName(NEWSEVENTCONTEXTkey);
						break;
					case WEIBOEVENTCONTEXTkey:
						tElementNew.setChapterTabName(NEWSEVENTCONTEXTkey);
						break;
					case NEWSEVENTCONTEXTkey:
						tElementNew.setChapterTabName(NEWSEVENTCONTEXTkey);
						break;
					case WEMEDIAEVENTCONTEXTkey:
						tElementNew.setChapterTabName(NEWSEVENTCONTEXTkey);
						break;
					case NEWSPROPAFATIONANALYSISTIMELISTkey:
						tElementNew.setChapterTabName(PROPAFATIONANALYSISkey);
						break;
					case WEMEDIAPROPAFATIONANALYSISTIMELISTkey:
						tElementNew.setChapterTabName(PROPAFATIONANALYSISkey);
						break;
					case SITUATIONACCESSMENTkey:
						tElementNew.setChapterTabName(SITUATIONACCESSMENTkey);
						break;
					case DATATRENDANALYSISkey:
						tElementNew.setChapterTabName(DATATRENDANALYSISkey);
						break;
					case DATASOURCEANALYSISkey:
						tElementNew.setChapterTabName(DATASOURCEANALYSISkey);
						break;
					case OPINIONANALYSISkey:
						tElementNew.setChapterTabName(OPINIONANALYSISkey);
						break;
					case EMOTIONANALYSISkey:
						tElementNew.setChapterTabName(EMOTIONANALYSISkey);
						break;
					case MOODSTATISTICSkey:
						tElementNew.setChapterTabName(MOODSTATISTICSkey);
						break;
					case WORDCLOUDSTATISTICSkey:
						tElementNew.setChapterTabName(WORDCLOUDSTATISTICSkey);
						break;
					case AREAkey:
						tElementNew.setChapterTabName(AREAkey);
						break;
					case ACTIVEACCOUNTkey:
						tElementNew.setChapterTabName(ACTIVEACCOUNTkey);
						break;
					case WEIBOHOTTOPICSkey:
						tElementNew.setChapterTabName(WEIBOHOTTOPICSkey);
						break;
				}
			}
            //List<TElementNew> parseArray2 = ReportUtil.tElementListHandle(parseArray);
			templateNew4Page.setTemplateListData(parseArray);
			
			templateNew4Page.setTemplatePosition(templateNew.getTemplatePosition());
			templateNew4Page.setGroupName(templateNew.getGroupName());
			templateNew4Page.setTemplateHeader(JSON.parseObject(templateNew.getTemplateHeader(), ReportNew.class));
			if(StringUtil.isNotEmpty(templateNew.getTemplateHeader())){
				ReportNew header = JSON.parseObject(templateNew.getTemplateHeader(), ReportNew.class);
				if(  StringUtil.isEmpty(header.getReportName())){
					header.setReportName(templateNew.getTemplateName());
				}
			}
			templateNew4Page.setTemplateActive(false);
			templateNew4Page.setIsDefault(templateNew.getIsDefault());
			templateNew4PageList.add(templateNew4Page);
		}
		return templateNew4PageList;
	}

    @Override
	public ReportNew create(String reportIntro, String jsonImgElements,
			ReportNew report, Integer isUpdateTemplate) throws Exception {
		//primaryStatisticsTime 供template_header使用
		String primaryStatisticsTime = report.getStatisticsTime();
		//修改时间格式 xxxx年xx月xx日
		if(report.getStatisticsTime().contains("年")){
			//说明走过预览，时间格式已经被改变了。
			primaryStatisticsTime = ReportUtil.statisticsTimeRestore(primaryStatisticsTime);
		}else{
			report.setStatisticsTime(ReportUtil.statisticsTimeHandle(report.getStatisticsTime()));
		}
		Map<String, List<Map<String, String>>> base64data = ReportUtil.getBase64data(jsonImgElements);
		List<ReportResource> reportResources = reportResourceRepository.findByTemplateIdAndResourceStatus(report.getTemplateId(), 0);
		TemplateNew templateNew = templateNewRepository.findOne(report.getTemplateId());
		//获取一份模板
		TemplateNew oldTemplateNew = new TemplateNew();
		copyTemplateNew(templateNew, oldTemplateNew);

		Map<Integer, List<ReportResource>> collect = reportResources.stream().collect(Collectors.groupingBy(ReportResource::getChapterPosition));

		report.setReportType(templateNew.getTemplateType());

		String reportPath = generateReportImpl.generateReport(report, collect, templateNew, base64data,reportIntro);
		report.setDocPath(reportPath);
		User loginUser = UserUtils.getUser();
		report.setUserId(loginUser.getId());
		report.setSubGroupId(loginUser.getSubGroupId());
		report.setTemplateList(templateNew.getTemplateList());
		reportNewRepository.save(report);

		//保存报告简介
		ReportResource reportIntroResrouce = createResTypeReportIntro(reportIntro, report);
		reportResourceRepository.save(reportIntroResrouce);

		//如果生成报告时要同时修改改模板的内容
		if (isUpdateTemplate != null) {
			if (isUpdateTemplate == 1) {
				//修改对应模板的 templateList 内容
				templateNew.setTemplateList(report.getTemplateList());
				//template_header有可能被修改
				setTemplateHeader(templateNew, report.getTotalIssue(),
						report.getThisIssue(), report.getPreparationUnits(), report.getPreparationAuthors(), primaryStatisticsTime,report.getReportName());
			} else {
				templateNew = oldTemplateNew;
			}
		}

		//重新保存template
		templateNewRepository.save(templateNew);

		//是否保存报告资源处理
		resDelHandle(report, reportResources);
		return report;
	}

	private void copyTemplateNew(TemplateNew templateNew, TemplateNew copyTemplateNew) {
		copyTemplateNew.setTemplateList(templateNew.getTemplateList());
		copyTemplateNew.setId(templateNew.getId());
		copyTemplateNew.setTemplateName(templateNew.getTemplateName());
		copyTemplateNew.setTemplateType(templateNew.getTemplateType());
		copyTemplateNew.setTemplateHeader(templateNew.getTemplateHeader());
		copyTemplateNew.setTemplatePosition(templateNew.getTemplatePosition());
		copyTemplateNew.setGroupName(templateNew.getGroupName());
		copyTemplateNew.setIsDefault(templateNew.getIsDefault());
		copyTemplateNew.setCreatedTime(templateNew.getCreatedTime());
		copyTemplateNew.setCreatedUserId(templateNew.getCreatedUserId());
		copyTemplateNew.setLastModifiedTime(templateNew.getLastModifiedTime());
		copyTemplateNew.setLastModifiedUserId(templateNew.getLastModifiedUserId());
		copyTemplateNew.setOrganizationId(templateNew.getOrganizationId());
		copyTemplateNew.setSubGroupId(templateNew.getSubGroupId());
		copyTemplateNew.setUserId(templateNew.getUserId());
		copyTemplateNew.setUserAccount(templateNew.getUserAccount());
	}

	private void resDelHandle(ReportNew report, List<ReportResource> reportResources) {
		if(report.getResourceDeleted() == 0){
			//删除report_resource
			List<ReportResource> resourcesStatusChanged = reportResources.stream().map(e -> {
				e.setResourceStatus(1);
				e.setReportId(report.getId());
				return e;
			}).collect(Collectors.toList());
			reportResourceRepository.delete(reportResources);
			reportResourceRepository.save(getUsefulReportResource(resourcesStatusChanged));
		}else{
			//保留资源，而且列表预览可见
			List<ReportResource> resourcesStatusChanged = new ArrayList<>();
			reportResources.stream().forEach(e -> {
				e.setResourceStatus(1);
				e.setReportId(report.getId());
				e.setId(UUID.randomUUID().toString().replace("-",""));
				ReportResource resourceCopyed = copyResourceExId(e);
				resourcesStatusChanged.add(resourceCopyed);
			});
			entityManager.clear();
			reportResourceRepository.save(getUsefulReportResource(resourcesStatusChanged));
		}
	}

	private ReportResource createResTypeReportIntro(String reportIntro, ReportNew report) {
		ReportResource reportIntroResrouce = new ReportResource();
		reportIntroResrouce.setImgComment(reportIntro);
		reportIntroResrouce.setChapter(REPORTINTRO);
		reportIntroResrouce.setUserId(UserUtils.getUser().getId());
		reportIntroResrouce.setUserId(UserUtils.getUser().getSubGroupId());
		reportIntroResrouce.setReportType(report.getReportType());
		reportIntroResrouce.setTemplateId(report.getTemplateId());
		reportIntroResrouce.setChapterPosition(0);
		reportIntroResrouce.setResourceStatus(1);
		reportIntroResrouce.setReportId(report.getId());
		reportIntroResrouce.setId(UUID.randomUUID().toString().replace("-",""));
		return reportIntroResrouce;
	}

	/***
     * 把25条reportResource减到10条。
     * @param resources
     * @return
     */
    private List<ReportResource> getUsefulReportResource(List<ReportResource> resources) {
        Map<Integer, List<ReportResource>> collect = resources.stream().collect(Collectors.groupingBy(ReportResource::getChapterPosition));
        List<ReportResource> result = new ArrayList<>();
        collect.keySet().forEach(e -> {
            List<ReportResource> currentChapter = collect.get(e);
            if(currentChapter != null && currentChapter.size()>10){
                List<ReportResource> subList = currentChapter.subList(0, 10);
                result.addAll(subList);
            }else{
                result.addAll(currentChapter);
            }
        });
        return result;
    }

    private ReportResource copyResourceExId(ReportResource resource){
		ReportResource resourceCopyed = new ReportResource();
		resourceCopyed.setDocPosition(resource.getDocPosition());
		resourceCopyed.setChapter(resource.getChapter());
		resourceCopyed.setImgComment(resource.getImgComment());
		resourceCopyed.setResourceStatus(resource.getResourceStatus());
		resourceCopyed.setChapterPosition(resource.getChapterPosition());
		resourceCopyed.setImgType(resource.getImgType());
		resourceCopyed.setImg_data(resource.getImg_data());
		resourceCopyed.setContent(resource.getContent());
		resourceCopyed.setSiteName(resource.getSiteName());
		resourceCopyed.setTitle(resource.getTitle());
		resourceCopyed.setReportType(resource.getReportType());
		resourceCopyed.setSid(resource.getSid());
		resourceCopyed.setUrlDate(resource.getUrlDate());
		resourceCopyed.setMd5Tag(resource.getMd5Tag());
		resourceCopyed.setRttCount(resource.getRttCount());
		resourceCopyed.setSimNum(resource.getSimNum());
		resourceCopyed.setSimCount(resource.getSimCount());
		resourceCopyed.setSrcName(resource.getSrcName());
		resourceCopyed.setUrlName(resource.getUrlName());
		resourceCopyed.setTemplateId(resource.getTemplateId());
		resourceCopyed.setGroupName(resource.getGroupName());
		resourceCopyed.setSecondaryChapter(resource.getSecondaryChapter());
		resourceCopyed.setTimeAgo(resource.getTimeAgo());
		resourceCopyed.setLibraryId(resource.getLibraryId());
		resourceCopyed.setMediaType(resource.getMediaType());
		resourceCopyed.setReportId(resource.getReportId());
		resourceCopyed.setUseage(resource.getUseage());
		resourceCopyed.setId(resource.getId());
		resourceCopyed.setNewsAbstract(resource.getNewsAbstract());
		resourceCopyed.setMapto(resource.getMapto());
		return resourceCopyed;
	}
	@Override
	public ReportNew create(String reportIntro, String jsonImgElements,
			String reportId) throws Exception {
		//生成report_data 保存到数据库，report_data_id 加入到report中，保存到数据库
		ReportNew report = reportNewRepository.findOne(reportId);
		return this.create(reportIntro, jsonImgElements, report, 0);
	}

	@Override
	public Page<ReportNew> listAllReport(String reportType,  String searchText, String groupName, Integer pageNum, Integer pageSize,String time) {
		//Page<ReportNew> findByReportType ;
		User loginUser = UserUtils.getUser();
		//现在不需要删除历史报告
		//删除历史报告
		//deleteHistoryReportResource(reportType);
		//往期报告
		if("HistoryReport".equals(reportType)){
			return findHistoryReports(searchText ,loginUser, pageNum, pageSize);
		}else{
			return findCurrentReports(loginUser, reportType, searchText, groupName, pageNum, pageSize,time);
		}
	}

	/***
	 * 当前报告的报告列表，周报周报月报分别显示1个月内的、1一个月内的、半年内的，专报不做时间限制，但是有分组限制
	 * @param reportType	报告类型
	 * @param searchText	结果中搜过
	 * @param groupName		专报分组名称
	 * @param pageNum
	 * @param pageSize
	 * @return				报告列表
	 */
	private Page<ReportNew> findCurrentReports(User user, String reportType, String searchText, String groupName, Integer pageNum, Integer pageSize,String time) {
		if(StringUtil.isNotEmpty(searchText)){
			searchText = "%" + searchText + "%";
		}
		String finalSearchText = searchText;

		return  reportNewRepository.findAll((Root<ReportNew> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->{
			ArrayList<Predicate> allPredicates = new ArrayList<>();
			Predicate reportTypePredicate = cb.equal(root.get("reportType").as(String.class), reportType);
			if(StringUtil.isNotEmpty(time) && time.contains(";")){
				String[] timeArr = time.split(";");
				if(timeArr.length == 2 && DateUtil.isTimeFormatter(timeArr[0]) && DateUtil.isTimeFormatter(timeArr[1])){
					SimpleDateFormat format = new SimpleDateFormat(DateUtil.yyyyMMdd);
					Date startDate = null;
					Date endDate = null;
					try {
						startDate = format.parse(timeArr[0]);
						endDate = format.parse(timeArr[1]);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					Predicate reportDate = cb.between(root.get("createdTime"),startDate, endDate);
					allPredicates.add(reportDate);
				}
			}
			Predicate userPredicate = null;
			if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
				userPredicate = cb.equal(root.get("userId").as(String.class), user.getId());
			}else {
				userPredicate = cb.equal(root.get("subGroupId").as(String.class), user.getSubGroupId());
			}
			Predicate docPathPredicate = cb.isNotNull(root.get("docPath").as(String.class));
			Predicate reportTypeDiffPredicate = null;
			if(SPECIALREPORT.equals(reportType)){
				//专报
				reportTypeDiffPredicate = cb.equal(root.get("groupName").as(String.class), groupName);
			}else if(INDEXTABREPORT.equals(reportType)){
				//日常监测报
				reportTypeDiffPredicate = cb.equal(root.get("groupName").as(String.class), groupName);
			}/*else if(DAILYREPORT.equals(reportType)){
				//日报
				reportTypeDiffPredicate = cb.greaterThan(root.get("createdTime").as(Date.class), getDeletedNodeTime(DailyReportExpiration, Calendar.DAY_OF_MONTH));
			}else if(WEEKLYREPORT.equals(reportType)){
				//周报
				reportTypeDiffPredicate = cb.greaterThan(root.get("createdTime").as(Date.class), getDeletedNodeTime(WeeklyReportExpiration, Calendar.DAY_OF_MONTH));
			}else if(MONTHLYREPORT.equals(reportType)){
				//月报
				reportTypeDiffPredicate = cb.greaterThan(root.get("createdTime").as(Date.class), getDeletedNodeTime(MonthlyReportExpiration, Calendar.DAY_OF_MONTH));
			}*/
			if(StringUtil.isNotEmpty(finalSearchText)){
				Predicate searchTextPredicate = cb.like(root.get("reportName").as(String.class), "%"+finalSearchText+"%");
				allPredicates.add(searchTextPredicate);
			}
			if (reportTypeDiffPredicate != null) {
				allPredicates.add(reportTypeDiffPredicate);
			}
			allPredicates.add(reportTypePredicate);
			allPredicates.add(userPredicate);
			allPredicates.add(docPathPredicate);

			return cb.and(allPredicates.toArray(new Predicate[allPredicates.size()]));
		},new PageRequest(pageNum, pageSize, new Sort(Direction.DESC , "createdTime")));
	}

	/***
	 * 查找历史报告
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	private Page<ReportNew> findHistoryReports(String searchText, User user, Integer pageNum, Integer pageSize) {
    	//使用lambda表达式代替Specification匿名内部类
		return reportNewRepository.findAll((Root<ReportNew> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->{
			//日报部分条件
			ArrayList<Predicate> dairlyReportPredicatesTemp = new ArrayList<>();
			dairlyReportPredicatesTemp.add(cb.greaterThan(root.get("createdTime").as(Date.class), getDeletedNodeTime(DailyReportExpiration, Calendar.DAY_OF_MONTH)));
			dairlyReportPredicatesTemp.add(cb.equal(root.get("reportType").as(String.class), DAILYREPORT));
			Predicate dailyReportPredicate = cb.and(dairlyReportPredicatesTemp.toArray(new Predicate[dairlyReportPredicatesTemp.size()]));

			//周报部分条件
			ArrayList<Predicate> weeklyReportPredicatesTemp = new ArrayList<>();
			weeklyReportPredicatesTemp.add(cb.greaterThan(root.get("createdTime").as(Date.class), getDeletedNodeTime(WeeklyReportExpiration, Calendar.DAY_OF_MONTH)));
			weeklyReportPredicatesTemp.add(cb.equal(root.get("reportType").as(String.class), WEEKLYREPORT));
			Predicate weeklyReportPredicate = cb.and(weeklyReportPredicatesTemp.toArray(new Predicate[weeklyReportPredicatesTemp.size()]));

			//月报部分条件
			ArrayList<Predicate> monthlyReportPredicatesTemp = new ArrayList<>();
			monthlyReportPredicatesTemp.add(cb.greaterThan(root.get("createdTime").as(Date.class), getDeletedNodeTime(MonthlyReportExpiration, Calendar.DAY_OF_MONTH)));
			monthlyReportPredicatesTemp.add(cb.equal(root.get("reportType").as(String.class), MONTHLYREPORT));
			Predicate monthlyReportPredicate = cb.and(monthlyReportPredicatesTemp.toArray(new Predicate[monthlyReportPredicatesTemp.size()]));

			//其他条件
			Predicate docPathPredicate = cb.isNotNull(root.get("docPath").as(String.class));
			Predicate userPredicate = null;
			if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
				userPredicate = cb.equal(root.get("userId").as(String.class), user.getId());
			}else {
				userPredicate = cb.equal(root.get("subGroupId").as(String.class), user.getSubGroupId());
			}

			Predicate searchTextPredicate = StringUtil.isEmpty(searchText) ? null : cb.like(root.get("reportName").as(String.class), "%"+searchText+"%");
			//合并报告的三部分条件并用 or 连接
			ArrayList<Predicate> reportParts = new ArrayList<>();
			reportParts.add(dailyReportPredicate);
			reportParts.add(weeklyReportPredicate);
			reportParts.add(monthlyReportPredicate);
			Predicate reportPartesPredicate = cb.or(reportParts.toArray(new Predicate[reportParts.size()]));

			//合并条件，and连接
			ArrayList<Predicate> all = new ArrayList<>();
			all.add(docPathPredicate);
			all.add(userPredicate);
			all.add(reportPartesPredicate);
			if(searchTextPredicate != null){
				all.add(searchTextPredicate);
			}
			return cb.and(all.toArray(new Predicate[all.size()]));
		}, new PageRequest(pageNum, pageSize, new Sort(Direction.DESC , "createdTime")));
	}

	/***
	 * 删除历史报告资源,后期更改为定时任务模式
	 */
	private void deleteHistoryReportResource(String reportType) {
		deleteDairyReportResource(reportType);
		deleteWeeklyReportResource(reportType);
		deleteMonthlyReportResource(reportType);
	}

	/***
	 * 只保存1年的月报资源
	 */
	private void deleteMonthlyReportResource(String reportType) {
		Date deletedNodeTime = getDeletedNodeTime(MonthlyReportExpiration, Calendar.DAY_OF_MONTH);
		reportResourceRepository.deleteByReportTypeAndCreatedTimeLessThan(reportType, deletedNodeTime);
	}

	/***
	 * 只保存1个月内的周报资源
	 */
	private void deleteWeeklyReportResource(String reportType) {
		Date deletedNodeTime = getDeletedNodeTime(WeeklyReportExpiration, Calendar.DAY_OF_MONTH);
		reportResourceRepository.deleteByReportTypeAndCreatedTimeLessThan(reportType, deletedNodeTime);
	}

	/***
	 * 只保存1个月内的日报资源
	 */
	private void deleteDairyReportResource(String reportType) {
		Date deletedNodeTime = getDeletedNodeTime(DailyReportExpiration, Calendar.DAY_OF_MONTH);
		reportResourceRepository.deleteByReportTypeAndCreatedTimeLessThan(reportType, deletedNodeTime);

	}

	private Date getDeletedNodeTime(int amount, int field) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(field, amount);
		//new SimpleDateFormat(DBCREATEDTIMEPATTERN).format(calendar.getTime());
		return calendar.getTime();
	}

	@Override
	public List<Object> listPreview(String reportId, String reportType) {
		List<Object> result = new ArrayList<>();
		if("专报".equals(reportType)){
			return sepcialReportService.listPreview(reportId, reportType);
		}
		ReportNew report = reportNewRepository.findOne(reportId);

		List<ReportResource> previewResources = reportResourceRepository.findByReportIdAndResourceStatus(report.getId(), 1)
				.stream().sorted(Comparator.comparing(ReportResource::getDocPosition)).collect(Collectors.toList());
		//确保旧版的 日报、周报、月报依然可以使用
		if(previewResources == null || previewResources.size() == 0){
			return sepcialReportService.listPreview(reportId, reportType);
		}
		//报告各章节内容
		List<TElementNew> previewData = getlistPreviewData(previewResources, report.getTemplateList());
		//报告简介
		List<ReportResource> reportIntros = previewResources.stream().filter(e -> e.getChapterPosition() == 0).collect(Collectors.toList());
		ReportResource reportIntro = null;
		if(!CollectionUtils.isEmpty(reportIntros)){
			reportIntro = reportIntros.get(0);
		}
		//给前端返序号
		List<String> orderList = new ArrayList<>();
		for(int i=0 ;i<previewData.size();i++){
				orderList.add(ROMAN2CHINESE.get(i+1));
		}

		Map<String,String> map = new HashMap<>();
		map.put("reportIntro", reportIntro == null ? null : reportIntro.getImgComment());

		result.add(report);		//报告头
		result.add(previewData);//报告各章节
		result.add(map);		//报告简介
		result.add(orderList);
		return result;
	}

	private Boolean judgeTemplateChapter(String reportType,List<TElementNew> tElementNewList){
		//当前报告类型对应的可选模块的名字
		Set<String> chapter = new HashSet<>();
		findEmptyTemplate(reportType).get(0).getTemplateListData().stream().forEach(e ->{
			chapter.add(e.getChapterDetail());
		});
		Boolean flag = true;
		//比较新老模板，如果存在名字不一样的，直接返回，不允许查看
		for(TElementNew e:tElementNewList){
			if(!chapter.contains(e.getChapterDetail())){
				flag = false;
				break;
			}
		}
		return flag;
	}


	private List<TElementNew> getlistPreviewData(List<ReportResource> previewResources, String templateList) {
		List<TElementNew> elements = JSONArray.parseArray(templateList, TElementNew.class);
		//elements = ReportUtil.tElementListHandle(elements);
		return ReportUtil.setDataInElements(elements, previewResources);
	}
	


	@Override
	public String deleteReport(String reportId) {
		if(StringUtil.isNotEmpty(reportId)){
			reportNewRepository.delete(reportId);
			return Const.SUCCESS;
		}else{
			return "参数不能为空";
		}
	}

	@Override
	public void templateOrderSet(String templateId, String templatePosition) {
		String[] templateIds = templateId.split(SEMICOLON);
		String[] positions = templatePosition.split(SEMICOLON);
		for(int i= 0; i < templateIds.length ; i ++){
			TemplateNew template = templateNewRepository.findOne(templateIds[i]);
			template.setTemplatePosition(Integer.parseInt(positions[i]));
			templateNewRepository.save(template);
		}
	}

	@Override
	public ReportNew download(String reportId) {
		return reportNewRepository.findOne(reportId);
	}


	@SuppressWarnings("unused")
	@Deprecated
	private void setPreviewData(String jsonData, TemplateNew template, List<TElementNew> result, String chapter){
		TElementNew tElementNew = new TElementNew();
		tElementNew.setChapterName(chapter);
		tElementNew.setChapterPosition(getChapterPosition(template, chapter));
		if(StringUtil.isNotEmpty(jsonData)){
			List<ReportResource> list = JSONObject.parseObject(jsonData, new TypeReference<List<ReportResource>>(){});
			tElementNew.setChapaterContent(list);
		}
		result.add(tElementNew);
	}
	@SuppressWarnings("unused")
	@Deprecated
	private void addIntoResult(String chapter, TemplateNew templateNew,
			Map<String, List<ReportResource>> collect, List<TElementNew> result) {
		TElementNew tElementNew = new TElementNew();
		tElementNew.setChapterName(chapter);
		tElementNew.setChapterPosition(getChapterPosition(templateNew,
				chapter));
		tElementNew.setChapaterContent(collect.get(chapter));
		result.add(tElementNew);
	}
	/**
	 * 5 min 以内，则断定是当前报告;
	 * 查report表已经按时间倒序排序了，所以取到的一定是最新的
	 * 故而不存在用户“输完关键字，点下一步，再返回再点下一步”出现两个report不知道该选哪个
	 * 取createdTime最新的那一条
	 * @author shao.guangze
	 * @return
	 * @throws ParseException 
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private boolean isCurrentReport(ReportNew report) throws ParseException{
		if(report!=null){
			Date reportDate = report.getCreatedTime();
			if((new Date().getTime() - reportDate.getTime())/60000 <5){
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	@Deprecated
	private void savebase64ImagData(String chapater, TemplateNew templateNew,
			Map<String, List<ReportResource>> collect,
			Map<String, List<TElementNew>> base64data, List<TElementNew> result) {
		TElementNew tElementNew = new TElementNew();
		tElementNew.setChapterName(chapater);
		tElementNew.setChapterPosition(getChapterPosition(templateNew, chapater));
		result.add(tElementNew);
		// 更新数据库
		ReportResource reportResource = collect.get(chapater).get(0);
		reportResourceRepository.save(reportResource);
	}
	@SuppressWarnings("unused")
	@Deprecated
	private String chapterData2jsonStr(List<TElementNew> result){
		return JSON.toJSONString(result.get(result.size()-1));
	}
	@SuppressWarnings("unused")
	@Deprecated
	private String keywordsHandle(String keyWords){
		StringBuffer trsl = new StringBuffer();
		String[] andSplit = keyWords.split(",");
		for(int j = 0 ; j< andSplit.length ; j++){
			if(j == 0){
				String[] keyWordsSpilt = andSplit[j].split(";");
				if(keyWordsSpilt.length == 1){
					trsl.append(keyWordsSpilt[0] + "))");
				}else{
					for(int i = 0 ; i< keyWordsSpilt.length ; i ++){
						if(i == 0){
							trsl.append(keyWordsSpilt[i]);
						}else{
							trsl.append(" OR " + keyWordsSpilt[i]);
						}
					}
					trsl.append(")");
				}
			}else{
				trsl.append("AND (");
				String[] keyWordsSpilt = andSplit[j].split(";");
				if(keyWordsSpilt.length == 1){
					trsl.append(keyWordsSpilt[0] + "))");
				}else{
					for(int i = 0 ; i< keyWordsSpilt.length ; i ++){
						if(i == 0){
							trsl.append(keyWordsSpilt[i]);
						}else{
							trsl.append(" OR " + keyWordsSpilt[i]);
						}
					}
					trsl.append("))");
				}
				
			}
		}
		trsl.append(")");
		return trsl.toString();
	}

	@Override
	public void delAllReportResource(String templateId) {
		reportResourceRepository.deleteByTemplateIdAndResourceStatus(templateId, 0);
	}

	@Override
	public String reBuildReport(String reportId, String jsonImgElements) throws Exception {
		log.info("舆情报告列表预览页，重新生成报告");
		ReportNew report = reportNewRepository.findOne(reportId);
		List<ReportResource> resources = reportResourceRepository.findByReportIdAndResourceStatus(reportId, 1);
		Map<Integer, List<ReportResource>> collect = resources.stream().collect(Collectors.groupingBy(ReportResource::getChapterPosition));
		Map<String, List<Map<String, String>>> base64data = ReportUtil.getBase64data(jsonImgElements);
		TemplateNew templateNew = new TemplateNew();
		templateNew.setTemplateList(report.getTemplateList());
		String reportIntro = getReportIntro(collect);
		String docPath = generateReportImpl.generateReport(report, collect, templateNew, base64data, reportIntro);
		report.setDocPath(docPath);
		reportNewRepository.save(report);
		return Const.SUCCESS;
	}

	@Override
	public Object searchResources(String keyWords, String statisticsTime, Integer pageNum, Integer pageSize) throws TRSException {
		QueryCommonBuilder builder = new QueryCommonBuilder();
		builder.setDatabase(Const.MIX_DATABASE.split(";"));
		builder.filterField(FtsFieldConst.FIELD_GROUPNAME, "微博 OR 国内微信 OR \"Twitter\" OR \"FaceBook\" OR 国内新闻 OR 国内新闻_手机客户端 OR 国内论坛 OR 国内博客 OR 国内新闻_电子报 OR 国外新闻\n", Operator.Equal);
		String[] timeArray = DateUtil.formatTimeRange(statisticsTime);
		Date startDate = DateUtil.stringToDate(timeArray[0], "yyyyMMddHHmmss");
		Date endDate = DateUtil.stringToDate(timeArray[1], "yyyyMMddHHmmss");
		builder.setServer(false);
		builder.setStartTime(startDate);
		builder.setEndTime(endDate);
		appednTrsl(builder, keyWords, timeArray);
		builder.setOrderBy("-IR_URLTIME");
		builder.setPageNo(pageNum);
		builder.setPageSize(pageSize);
		return  hybase8SearchService.pageListCommon(builder, false, true,false,null);
	}

	/***
	 * 向QueryCommonBuilder中appendTrsl, keyWordsIndex 为 标题，
	 * @param builder
	 * @param keyWords
	 */
	private void appednTrsl(QueryCommonBuilder builder, String keyWords, String[] timeArray) {
		StringBuilder trsl = new StringBuilder();
		//拼装keyWords
		trsl.append("((IR_URLTITLE:(");
		String[] keyWordsArray = keyWords.split(";");
		for(int i = 0 ; i < keyWordsArray.length ; i++){
			trsl.append(" \"");
			trsl.append(keyWordsArray[i]);
			trsl.append("\" ");
			trsl.append(i == keyWordsArray.length - 1 ? "))" : "OR");
		}
		//拼装time
		trsl.append(" AND (IR_URLTIME:[");
		trsl.append(timeArray[0]);
		trsl.append(" TO ");
		trsl.append(timeArray[1]);
		trsl.append("]))");
		builder.setAppendTRSL(trsl);
	}

	private String getReportIntro(Map<Integer, List<ReportResource>> collect) {
		List<ReportResource> reportIntros = collect.get(0);
		if(!CollectionUtils.isEmpty(reportIntros)){
			ReportResource resReportIntro = reportIntros.get(0);
			String reportIntro = resReportIntro.getImgComment();
			return reportIntro;
		}
		return "";
	}

	@Override
	public Object changePosition(Integer docPosition, Integer newPosition, String chapter, String templateId, int resourceStatus, String id, String reportDataId,String reportId) {
		//专报中的拖拽，因操作对象不一样，另起
		if(StringUtil.isNotEmpty(reportDataId)){
			//获取到该条记录
			ReportDataNew reportDataNew = reportDataNewRepository.findOne(reportDataId);
			if("NEWSTOP10".equals(chapter)){
				String newsTop10 = reportDataNew.getNewsTop10();
				List<ReportResource> parseArray = JSONArray.parseArray(newsTop10, ReportResource.class);
				String reportSort = specialReportSort(docPosition, newPosition, parseArray, id);
				reportDataNew.setNewsTop10(reportSort);
			}else if("WEIBOTOP10".equals(chapter)){
				String weiboTop10 = reportDataNew.getWeiboTop10();
				List<ReportResource> parseArray = JSONArray.parseArray(weiboTop10, ReportResource.class);
				String reportSort = specialReportSort(docPosition, newPosition, parseArray, id);
				reportDataNew.setWeiboTop10(reportSort);
				
			}else if("WECHATTOP10".equals(chapter)){
				String wechatTop10 = reportDataNew.getWechatTop10();
				List<ReportResource> parseArray = JSONArray.parseArray(wechatTop10, ReportResource.class);
				String reportSort = specialReportSort(docPosition, newPosition, parseArray, id);
				reportDataNew.setWechatTop10(reportSort);
				
			}else if("NEWSHOTTOPICS".equals(chapter)){
				String newsHotTopics = reportDataNew.getNewsHotTopics();
				List<ReportResource> parseArray = JSONArray.parseArray(newsHotTopics, ReportResource.class);
				String reportSort = specialReportSort(docPosition, newPosition, parseArray, id);
				reportDataNew.setNewsHotTopics(reportSort);
				
			}else if("WEIBOHOTTOPICS".equals(chapter)){
				//微博热点话题
				String weiboHotTopics = reportDataNew.getWeiboHotTopics();
				List<ReportResource> parseArray = JSONArray.parseArray(weiboHotTopics, ReportResource.class);
				String reportSort = specialReportSort(docPosition, newPosition, parseArray, id);
				reportDataNew.setWeiboHotTopics(reportSort);
				
			}else if("NEWSHOTTOP10".equals(chapter)){
				//新闻热点TOP10
				String newsHotTopics = reportDataNew.getNewsHotTopics();
				List<ReportResource> parseArray = JSONArray.parseArray(newsHotTopics, ReportResource.class);
				String reportSort = specialReportSort(docPosition, newPosition, parseArray, id);
				reportDataNew.setNewsHotTopics(reportSort);

			}else if("WEIBOHOTTOP10".equals(chapter)){
				//微博热点TOP10
				String weiboHotTopics = reportDataNew.getWeiboHotTopics();
				List<ReportResource> parseArray = JSONArray.parseArray(weiboHotTopics, ReportResource.class);
				String reportSort = specialReportSort(docPosition, newPosition, parseArray, id);
				reportDataNew.setWeiboHotTopics(reportSort);

			}else if("WECHATHOTTOP10".equals(chapter)){
				//微信热点TOP10
				String wechatHotTop10 = reportDataNew.getWechatHotTop10();
				List<ReportResource> parseArray = JSONArray.parseArray(wechatHotTop10, ReportResource.class);
				String reportSort = specialReportSort(docPosition, newPosition, parseArray, id);
				reportDataNew.setWechatHotTop10(reportSort);

			}
			reportDataNewRepository.save(reportDataNew);
			log.error("拖拽完成！当前执行的操作是模块：" + chapter + ",拖动的顺序是将位置：" + docPosition + "拖到：" + newPosition);
			return Const.SUCCESS;
		}
		//在该模板中定位该条信息，制作是拖拽还是预览时拖拽
		ReportResource reportResource = reportResourceRepository.findOne(id);
		String sid = reportResource.getSid();
		
		List<ReportResource> docList = null;
		if(resourceStatus == 1){
			//该用户、这个资源模块的文章集合
//			docList = reportResourceRepository.findByReportIdAndChapterAndResourceStatus(templateId, chapter, resourceStatus);
			if (StringUtil.isNotEmpty(reportId)){
				docList = reportResourceRepository.findByReportIdAndChapterAndResourceStatus(reportId, chapter,resourceStatus)
						.stream().sorted(Comparator.comparing(ReportResource::getDocPosition)).collect(Collectors.toList());
			}
		}else{
			docList = reportResourceRepository.findByTemplateIdAndChapterAndResourceStatus(templateId, chapter, resourceStatus).stream().sorted(Comparator.comparing(ReportResource::getDocPosition)).collect(Collectors.toList());
		}
		//更新改doc的位置字段
//		reportResource.setDocPosition(newPosition);
		Integer tnewPosition = docList.get(newPosition).getDocPosition();
		Integer tdocPosition = docList.get(docPosition).getDocPosition();
		reportResource.setDocPosition(tnewPosition);
		reportResourceRepository.save(reportResource);
		if(docPosition > newPosition){//上移
			for(int i = 0; i < docList.size(); i++){
				String sid2 = docList.get(i).getSid();
				boolean flag = false;
				if(!sid.equals(sid2)){
					flag = true;
				}
				//各自的位置
				Integer position = docList.get(i).getDocPosition();

				// 其他文章原位置大于或等于新位置，且小于该文章的原位置
				if(position >= tnewPosition && position < tdocPosition && flag){
					docList.get(i).setDocPosition(position + 1);
					reportResourceRepository.save(docList.get(i));
				}
				//else暂时没想到
			}

		}else{//下移
			for(int i = 0; i < docList.size(); i++){
				String sid2 = docList.get(i).getSid();
				boolean flag = false;
				if(!sid.equals(sid2)){
					flag = true;
				}
				//各自的位置
				Integer position = docList.get(i).getDocPosition();
				// 其他文章原位置大于或等于新位置，且小于该文章的原位置
				if(position <= tnewPosition && position > tdocPosition && flag){
					docList.get(i).setDocPosition(position - 1);
					reportResourceRepository.save(docList.get(i));
				}
			}
		}
		log.error("拖拽完成！当前执行的操作是模块：" + chapter + ",拖动的顺序是将位置：" + docPosition + "拖到：" + newPosition);
		return Const.SUCCESS;
	}

	@Override
	public List<ReportNew> findReportByUserId(String userId) {
		return reportNewRepository.findByUserId(userId);
	}

	@Override
	public void updateReportAll(List<ReportNew> reportNews) {
		reportNewRepository.save(reportNews);
		reportNewRepository.flush();
	}

	@Override
	public List<TemplateNew> findTemplateByUserId(String userId) {
		return templateNewRepository.findByUserId(userId);
	}

	@Override
	public void updateTemplateAll(List<TemplateNew> templateNews) {
			templateNewRepository.save(templateNews);
			templateNewRepository.flush();
	}

	public static String specialReportSort(int docPosition, int newPosition, List<ReportResource> parseArray, String sid){
		List<ReportResource> saveList = new ArrayList<>();
		if(docPosition > newPosition){//上移
			for(int i = 0; i < parseArray.size(); i++){
				String sid2 = parseArray.get(i).getSid();
				boolean flag = false;
				if(!sid.equals(sid2)){
					flag = true;
				}else{
					parseArray.get(i).setDocPosition(newPosition);
				}
				//各自的位置
				Integer position = parseArray.get(i).getDocPosition();
				// 其他文章原位置大于或等于新位置，且小于该文章的原位置
				if(position >= newPosition && position < docPosition && flag){
					parseArray.get(i).setDocPosition(position + 1);
				}
				saveList.add(parseArray.get(i));
			}
		}else{//下移
			for(int i = 0; i < parseArray.size(); i++){
				String sid2 = parseArray.get(i).getSid();
				boolean flag = false;
				if(!sid.equals(sid2)){
					flag = true;
				}else{
					parseArray.get(i).setDocPosition(newPosition);
				}
				//各自的位置
				Integer position = parseArray.get(i).getDocPosition();
				// 其他文章原位置大于或等于新位置，且小于该文章的原位置
				if(position <= newPosition && position > docPosition && flag){
					parseArray.get(i).setDocPosition(position - 1);
				}
				saveList.add(parseArray.get(i));
			}
		}
		//saveList转回为Json并存储回去
		List<ReportResource> resultList = new ArrayList<>();
		// 将改变后的顺序再按 1234...存回去，不然专报下载的时候还是按数据库字段的保存顺序下载的
		resultList = saveList.stream().sorted(Comparator.comparing(ReportResource::getDocPosition)).collect(Collectors.toList());
		return JSONArray.parseArray(JSON.toJSONString(resultList)).toJSONString();
	}
	
}
