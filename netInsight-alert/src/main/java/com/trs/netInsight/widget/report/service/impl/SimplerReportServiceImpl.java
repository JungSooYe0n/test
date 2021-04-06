package com.trs.netInsight.widget.report.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.util.DateUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.report.constant.ReportConst;
import com.trs.netInsight.widget.report.entity.*;
import com.trs.netInsight.widget.report.entity.repository.ReportNewRepository;
import com.trs.netInsight.widget.report.entity.repository.ReportResourceRepository;
import com.trs.netInsight.widget.report.entity.repository.TemplateNewRepository;
import com.trs.netInsight.widget.report.service.IGenerateReport;
import com.trs.netInsight.widget.report.service.IMaterialLibraryNewService;
import com.trs.netInsight.widget.report.service.ISimplerReportService;
import com.trs.netInsight.widget.report.task.SimpleReportTask;
import com.trs.netInsight.widget.report.util.ReportUtil;
import com.trs.netInsight.widget.special.entity.CustomSpecial;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.SpecialSubject;
import com.trs.netInsight.widget.special.entity.repository.SpecialProjectRepository;
import com.trs.netInsight.widget.special.entity.repository.SpecialSubjectRepository;
import com.trs.netInsight.widget.special.service.ICustomSpecialService;
import com.trs.netInsight.widget.special.service.ISpecialProjectService;
import com.trs.netInsight.widget.special.service.ISpecialSubjectService;
import com.trs.netInsight.widget.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.trs.netInsight.widget.report.constant.ReportConst.*;

/**
 *  舆情报告 极简模式 业务层接口实现类
 *
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/10/9.
 */
@Service
@Slf4j
public class SimplerReportServiceImpl implements ISimplerReportService {

    @Autowired
    private ISpecialProjectService specialProjectService;
    @Autowired
    private ICustomSpecialService customSpecialService;

    @Autowired
    private ReportNewRepository reportNewRepository;

    @Autowired
    private TemplateNewRepository templateNewRepository;

    @Autowired
    private ReportResourceRepository reportResourceRepository;

    @Autowired
    private IGenerateReport generateReport;

    @Autowired
    private IMaterialLibraryNewService materialLibraryNewService;

    @Autowired
    private SpecialProjectRepository specialProjectRepository;

    @Autowired
    private SpecialSubjectRepository specialSubjectRepository;

    @Autowired
    private ISpecialSubjectService specialSubjectService;

    /**
     * 单拿一个线程计算数据
     * */
    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
    @Override
    public Object getGroupList(String type) throws OperationException {
        User loginUser = UserUtils.getUser();
        if ("special".equals(type)){
            //查询所有专题分析
            return getSpecialProjects();
            //return specialProjectService.findByUserId(UserUtils.getUser().getId(),new Sort(Sort.Direction.DESC,"lastModifiedTime"));
        }else if ("material".equals(type)){
            //查询收藏夹  需要分组  目前还未分组
            return materialLibraryNewService.findByUser(loginUser);
        }else if ("custom".equals(type)){
            //自定义专题
            return customSpecialService.getByUser(loginUser);
        }
        return null;
    }

    @Override
    public List<ReportNew> listSimplerReport(String reportType) {
        User loginUser = UserUtils.getUser();
        return findSimplerReports(loginUser,reportType);
    }

    @Override
    public List<Object> calculateReport(String reportType, String typeId, String templateId) throws Exception {
         String timeRange = "";
        if (StringUtils.isEmpty(typeId)){
            throw new OperationException("请选择报告查询内容");
        }

        if (StringUtils.isEmpty(templateId)){
            throw new OperationException("请选择模板");
        }
        ReportNew reportNew = new ReportNew();

        TemplateNew templateNew = templateNewRepository.findOne(templateId);


        reportNew.setTemplateId(templateId);
        reportNew.setTemplateList(templateNew.getTemplateList());
        ReportNew returnReport = reportNewRepository.save(reportNew);
        User loginUser = UserUtils.getUser();
        //根据reporttype 判断 拿着typeID 去哪里 查询
        if ("special".equals(reportType)){
            //专题分析
            returnReport.setReportType("special");

            //根据 typeID 查出对应实体 拿到生成报告的关键词
            SpecialProject specialProject = specialProjectService.findOne(typeId);
            returnReport.setReportName(specialProject.getSpecialName());
            timeRange = specialProject.getTimeRange();
            if (StringUtil.isEmpty(timeRange)){
                timeRange = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(specialProject.getStartTime()) + ";" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(specialProject.getEndTime());
            }
            fixedThreadPool.execute(new SimpleReportTask(specialProject.getAnyKeywords(),specialProject.getExcludeWords(),specialProject.getSearchScope().toString(),specialProject.getTrsl(),timeRange,specialProject.isSimilar(),specialProject.isIrSimflag(),specialProject.isIrSimflagAll(),specialProject.isWeight(),returnReport,loginUser.getId(),loginUser.getSubGroupId(),specialProject.getExcludeWeb()));
        }else if ("custom".equals(reportType)){
            //自定义专题
            //根据 typeID 查出对应实体 拿到生成报告的关键词
            returnReport.setReportType("custom");
            CustomSpecial customSpecial = customSpecialService.findOne(typeId);
            returnReport.setReportName(customSpecial.getSpecialName());
            timeRange = customSpecial.getTimeRange();

            fixedThreadPool.execute(new SimpleReportTask(customSpecial.getAnyKeyWords(),customSpecial.getExcludeWords(),customSpecial.getSearchScope().toString(),customSpecial.getTrsl(),timeRange,customSpecial.isSimilar(),customSpecial.isIrSimflag(),customSpecial.isIrSimflagAll(),customSpecial.isWeight(),returnReport,loginUser.getId(),loginUser.getSubGroupId(),customSpecial.getExcludeWeb()));

        }else if ("material".equals(reportType)){
            //素材库
            //根据 typeID 查出对应组块下 文章 放入报告中
            reportNew.setReportType("material");
            List<FtsDocumentCommonVO> documentCommonVOS = materialLibraryNewService.findMaterialResourceForReport(typeId);
            MaterialLibraryNew libraryNew = materialLibraryNewService.findOne(typeId);
            //实体转换
            List<ReportResource> reportResources = changesCommonToResource(documentCommonVOS,returnReport.getId());
            reportResourceRepository.save(reportResources);

            returnReport.setReportName(libraryNew.getSpecialName());
            reportNew.setPreparationUnits("XX单位");
            reportNew.setThisIssue("X");
            reportNew.setStatisticsTime(DateUtil.formatCurrentTime("yyyy年MM月dd日"));
            returnReport = reportNewRepository.save(returnReport);
        }else {
            throw new OperationException("传入 极简报告 类型为空");
        }

        List<Object> resultList = new ArrayList<>();
        resultList.add(returnReport);
        return resultList;
    }

    @Override
    public ReportNew createImplerReport(String reportId,String templateId,String jsonImgElements) throws Exception{
        if (StringUtil.isEmpty(reportId)){
            throw new OperationException("未获取到生成报告的报告id");
        }
        ReportNew reportNew = reportNewRepository.findOne(reportId);

        String docPath = null;
        if (reportNew != null){
            List<ReportResource> resources = reportResourceRepository.findByReportIdAndResourceStatus(reportNew.getId(), 0);

            if ("special".equals(reportNew.getReportType()) || "custom".equals(reportNew.getReportType())){

                Map<Integer, List<ReportResource>> collect = resources.stream().collect(Collectors.groupingBy(ReportResource::getChapterPosition));
                Map<String, List<Map<String, String>>> base64data = ReportUtil.getBase64data(jsonImgElements);
                docPath = generateReport.generateSimplerReport(reportNew, collect, base64data, null);
            }else if ("material".equals(reportNew.getReportType())){
                List<TElementNew> tElementNews = ReportUtil.createMaterialTemplate();
                for (TElementNew tElementNew : tElementNews) {
                    tElementNew.setChapaterContent(resources);
                }
                docPath = generateReport.generateMaterialReport(reportNew,JSONArray.toJSONString(tElementNews));
            }

        }
        reportNew.setDocPath(docPath);
        reportNewRepository.save(reportNew);
        return reportNew;
    }
    @Override
    public Object getTemplateNew(String type) {
        User loginUser = UserUtils.getUser();
        List<TemplateNew> templatePosition = null;
        if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
            templatePosition = templateNewRepository.findByUserIdAndTemplateType(loginUser.getId(), type, new Sort(Sort.Direction.ASC, "templatePosition"));
        }else {
            templatePosition = templateNewRepository.findBySubGroupIdAndTemplateType(loginUser.getSubGroupId(), type, new Sort(Sort.Direction.ASC, "templatePosition"));

        }

        if((templatePosition == null || templatePosition.size() == 0)){//加入默认模板
            if ("material".equals(type)){
                TemplateNew templateNew = addDefaultMaterial();
                templateNewRepository.save(templateNew);
                templatePosition.add(templateNew);
            }else {
                TemplateNew templateNew = addDefaultSimplerTemplate();
                templateNewRepository.save(templateNew);
                templatePosition.add(templateNew);
            }

        }
        return templatePosition;
    }

    @Override
    public Object findOneTemplateNew(String templateId) {
        return templateNewRepository.findOne(templateId);
    }

    @Override
    public List<Object> reportDetail(String reportId) throws OperationException{
        ReportNew report = reportNewRepository.findOne(reportId);
        String reportType = report.getReportType();
        if (StringUtil.isEmpty(reportId)){
            throw new OperationException("未获取到要编辑报告的id");
        }

        String templateId = report.getTemplateId();
        TemplateNew templateNew = new TemplateNew();
        if (StringUtil.isNotEmpty(templateId)){
            templateNew = templateNewRepository.findOne(templateId);
        }

        List<Object> result = new ArrayList<>();
        if (report != null){
            if ("special".equals(reportType) || "custom".equals(reportType)){
                List<ReportResource> previewResources = reportResourceRepository.findByReportIdAndResourceStatus(report.getId(), 0).stream().sorted(Comparator.comparing(ReportResource::getDocPosition)).collect(Collectors.toList());

                //报告简介
                List<ReportResource> reportIntros = previewResources.stream().filter(e -> e.getChapterPosition() == 1).collect(Collectors.toList());//报告各章节内容
                List<TElementNew> previewData = getlistPreviewData(previewResources, report.getTemplateList());

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
                result.add(map);		//报告简介
               result.add(previewData);//报告各章节
               result.add(orderList);
            }else if ("material".equals(reportType)){
                if(templateNew == null){
                    List<TemplateNew> templateNewList = (List<TemplateNew>)getTemplateNew(reportType);
                    templateNew = templateNewList.get(0);
                }
                List<TElementNew> elementList = JSONArray.parseArray(templateNew.getTemplateList(), TElementNew.class);
                List<ReportResource> reportResources = reportResourceRepository.findByReportIdAndResourceStatus(report.getId(), 0);
                for (TElementNew tElementNew : elementList) {
                    tElementNew.setChapaterContent(reportResources);
                }
                result.add(elementList);//报告各章节
            }
        }
        result.add(report);
        return result;
    }

    @Override
    public ReportNew updateReport(String reportId, String reportName, String templateList) throws Exception {
        ReportNew reportNew = reportNewRepository.findOne(reportId);
        if (StringUtil.isNotEmpty(reportName)){
            reportNew.setReportName(reportName);
        }
        if (StringUtil.isNotEmpty(templateList)){
            reportNew.setTemplateList(templateList);
        }
        reportNew = reportNewRepository.save(reportNew);
        return reportNew;
    }


@Override
public String reBuildReport(String reportId,String jsonImgElemets,String reportIntro) throws Exception {
    if (StringUtil.isEmpty(reportId)){
        throw new OperationException("未获取到所编辑报告id");
    }
    ReportNew reportNew = reportNewRepository.findOne(reportId);
    String docPath = null;
    if (reportNew != null){
        List<ReportResource> resources = reportResourceRepository.findByReportIdAndResourceStatus(reportId, 0);
        if ("special".equals(reportNew.getReportType()) || "custom".equals(reportNew.getReportType())){
            Map<Integer, List<ReportResource>> collect = resources.stream().collect(Collectors.groupingBy(ReportResource::getChapterPosition));

            Map<String, List<Map<String, String>>> base64data = ReportUtil.getBase64data(jsonImgElemets);

            if (StringUtils.isEmpty(reportIntro)){
                reportIntro = getReportIntro(collect);
            }else{
                ReportResource reportResource = new ReportResource();
                reportResource.setChapter("报告简介");
                reportResource.setReportType(reportNew.getReportType());
                reportResource.setTemplateId(reportNew.getTemplateId());
                reportResource.setUserId(UserUtils.getUser().getId());
                reportResource.setChapterPosition(1);
                reportResource.setReportId(reportNew.getId());
                reportResource.setResourceStatus(0);
                reportResourceRepository.save(reportResource);
            }
            docPath = generateReport.generateSimplerReport(reportNew, collect, base64data, reportIntro);
        }else if ("material".equals(reportNew.getReportType())){

            List<ReportResource> reportResources = reportResourceRepository.findByReportIdAndResourceStatus(reportNew.getId(), 0);
            List<TElementNew> tElementNews = ReportUtil.createMaterialTemplate();
            for (TElementNew tElementNew : tElementNews) {
                tElementNew.setChapaterContent(reportResources);
            }
            docPath = generateReport.generateMaterialReport(reportNew,JSONArray.toJSONString(tElementNews));

        }
    }
    reportNew.setDocPath(docPath);
    reportNewRepository.save(reportNew);
    return Const.SUCCESS;
}
    @Override
    public Object imgData(String reportId) {
        ReportNew reportNew = reportNewRepository.findOne(reportId);
        List<TElementNew> previewData = new ArrayList<>();
        if (reportNew != null){
            List<ReportResource> previewResources = reportResourceRepository.findByReportIdAndResourceStatus(reportNew.getId(), 0)
                        .stream().sorted(Comparator.comparing(ReportResource::getDocPosition)).collect(Collectors.toList());
            String templateId = reportNew.getTemplateId();
            if (StringUtils.isNotEmpty(templateId)){
                TemplateNew templateNew = templateNewRepository.findOne(templateId);
                previewData = getlistPreviewData(previewResources, templateNew.getTemplateList());
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("doneFlag",reportNew.getDoneFlag());
        map.put("data",previewData);
        return map;
    }

    @Override
    public Object deleteReportResource(String reportResourceId, String reportId) {
        String[] resourceIdArry = reportResourceId.split(SEMICOLON);
        return null;
    }

    /**
     *  查询 极简模式 特定条件下 报告列表
     * @param user
     * @param reportType
     * @return
     */
    private List<ReportNew> findSimplerReports(User user,String reportType){
        Specification<ReportNew> specification = new Specification<ReportNew>(){

            @Override
            public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();

                predicates.add(cb.equal(root.get("reportType").as(String.class), reportType));
                if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
                    predicates.add(cb.equal(root.get("userId").as(String.class), user.getId()));
                }else {
                    predicates.add(cb.equal(root.get("subGroupId").as(String.class), user.getSubGroupId()));
                }
                predicates.add(cb.isNotNull(root.get("docPath").as(String.class)));

                Predicate[] pre = new Predicate[predicates.size()];
                return query.where(predicates.toArray(pre)).getRestriction();
            }
        };

        List<ReportNew> reportNewList = reportNewRepository.findAll(specification, new Sort(Sort.Direction.DESC, "createdTime"));
        return reportNewList;

//        return  reportNewRepository.findAll((Root<ReportNew> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//            Predicate reportTypePredicate = criteriaBuilder.equal(root.get("reportType").as(String.class), reportType);
//            Predicate userIdPredicate = criteriaBuilder.equal(root.get("userId").as(String.class), userId);
//            Predicate docPathPredicate = criteriaBuilder.isNotNull(root.get("docPath").as(String.class));
//
//            predicates.add(reportTypePredicate);
//            predicates.add(userIdPredicate);
//            predicates.add(docPathPredicate);
//
//            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
//        },new PageRequest(pageNum,pageSize,new Sort(Sort.Direction.DESC,"createdTime")));
    }


    /**
     * 将查询的 hybase混合实体转化为 reportResourece实体
     * @param commonVOS
     * @return
     */
    private List<ReportResource> changesCommonToResource(List<FtsDocumentCommonVO> commonVOS,String reportId){
        List<ReportResource> reportResources = new ArrayList<>();
        List<ReportResource> news = new ArrayList<>();
        List<ReportResource> newspaper = new ArrayList<>();
        List<ReportResource> otherTradition = new ArrayList<>();
        List<ReportResource> weChat = new ArrayList<>();
        List<ReportResource> weiBo = new ArrayList<>();
        if (commonVOS != null && commonVOS.size()>0){
            ReportResource reportResourceTemp = null;
            for (FtsDocumentCommonVO ftsDocumentCommonVO : commonVOS) {
                reportResourceTemp = new ReportResource();
                reportResourceTemp.setReportId(reportId);
                reportResourceTemp.setUrlDate(ftsDocumentCommonVO.getUrlTime());
                reportResourceTemp.setGroupName(ftsDocumentCommonVO
                        .getGroupName());
                reportResourceTemp.setMd5Tag(ftsDocumentCommonVO.getMd5Tag());
                reportResourceTemp.setUrlName(ftsDocumentCommonVO.getUrlName());
                reportResourceTemp.setResourceStatus(0);
                /*
                 * 获取 传统媒体、微信的标题, 微博获取正文 来源，微信微博都是获取authoers
                 * 放到sitename里，传统媒体是获取srcname放到srcname里。
                 * 微信唯一标识使用的hkey,但再ReportResource里不做区分，一律使用sid 微信微博获取正文，传统媒体获取摘要
                 */
                if ("微博".equals(ftsDocumentCommonVO.getGroupName())) {
                    reportResourceTemp.setTitle(ReportUtil.replaceHtml(StringUtil.removeFourChar(ftsDocumentCommonVO
                            .getContent())));
                    reportResourceTemp.setSiteName(ftsDocumentCommonVO
                            .getAuthors());
                    reportResourceTemp.setSid(ftsDocumentCommonVO.getSid());
                    reportResourceTemp.setContent(ReportUtil.replaceHtml(StringUtil.removeFourChar(StringUtil.replaceImg(ftsDocumentCommonVO
                            .getContent()))));
                    weiBo.add(reportResourceTemp);
                } else if ("国内微信".equals(ftsDocumentCommonVO.getGroupName())) {
                    reportResourceTemp.setTitle(ReportUtil.replaceHtml(StringUtil.removeFourChar(ftsDocumentCommonVO.getTitle())));
                    reportResourceTemp.setSiteName(ftsDocumentCommonVO
                            .getSiteName());
                    reportResourceTemp.setSid(ftsDocumentCommonVO.getHkey());
                    reportResourceTemp.setContent(ReportUtil.replaceHtml(StringUtil.removeFourChar(StringUtil.replaceImg(ftsDocumentCommonVO
                            .getContent()))));
                    //reportResourceTemp.setNewsAbstract(ReportUtil.replaceHtml(StringUtil.removeFourChar(ftsDocumentCommonVO.getAbstractContent())));
                    weChat.add(reportResourceTemp);
                } else {
                    if (Const.MEDIA_TYPE_ZIMEITI_BOKE.contains(ftsDocumentCommonVO.getGroupName()) || Const.MEDIA_TYPE_VIDEO.contains(ftsDocumentCommonVO.getGroupName())){
                        //自媒体号、博客、视频、短视频
                        reportResourceTemp.setSiteName(ftsDocumentCommonVO.getSiteName() + (StringUtil.isNotEmpty(ftsDocumentCommonVO.getAuthors()) && !"undefined".equals(ftsDocumentCommonVO.getAuthors()) ?  "-" + ftsDocumentCommonVO.getAuthors() : ""));
                    } else {
                        //传统媒体
                        reportResourceTemp.setSiteName(ftsDocumentCommonVO.getSiteName());
                    }
                    // 传统媒体
                    reportResourceTemp.setTitle(ReportUtil.replaceHtml(StringUtil.removeFourChar(ftsDocumentCommonVO.getTitle())));
                    reportResourceTemp.setSrcName(ftsDocumentCommonVO
                            .getSrcName());
                    reportResourceTemp.setSid(ftsDocumentCommonVO.getSid());
                    reportResourceTemp.setContent(ReportUtil.replaceHtml(StringUtil.removeFourChar(StringUtil.replaceImg(ftsDocumentCommonVO
                            .getContent()))));
                    reportResourceTemp.setNewsAbstract(ReportUtil.replaceHtml(StringUtil.removeFourChar(StringUtil.replaceImg(ftsDocumentCommonVO.getAbstracts()))));
                    if ("国内新闻".equals(ftsDocumentCommonVO.getGroupName())){
                        news.add(reportResourceTemp);
                    }else if ("国内新闻_电子报".equals(ftsDocumentCommonVO.getGroupName())){
                        newspaper.add(reportResourceTemp);
                    }else {
                        otherTradition.add(reportResourceTemp);
                    }
                }


            }
        }
        if (news.size() != 0){
            reportResources.addAll(news);
        }
        if (otherTradition.size() != 0){
            reportResources.addAll(otherTradition);
        }
        if (newspaper.size() != 0){
            reportResources.addAll(newspaper);
        }
        if (weChat.size() != 0){
            reportResources.addAll(weChat);
        }
        if (weiBo.size() != 0){
            reportResources.addAll(weiBo);
        }
        return reportResources;
    }

    private List<TElementNew> getlistPreviewData(List<ReportResource> previewResources, String templateList) {
        List<TElementNew> elements = JSONArray.parseArray(templateList, TElementNew.class);
        return ReportUtil.setDataInElements(elements, previewResources);
    }

    private String getReportIntro(Map<Integer, List<ReportResource>> collect) {
        List<ReportResource> reportIntros = collect.get(1);
        if(!CollectionUtils.isEmpty(reportIntros)){
            ReportResource resReportIntro = reportIntros.get(0);
            String reportIntro = resReportIntro.getImgComment();
            return reportIntro;
        }
        return null;
    }

    private TemplateNew addDefaultSimplerTemplate() {
        TemplateNew templateNew = new TemplateNew();
        templateNew.setTemplatePosition(1);
        templateNew.setIsDefault(1);
        templateNew.setTemplateList(JSON.toJSONString(ReportUtil.createSimplerTemplate()));
        templateNew.setTemplateType("special");
        templateNew.setTemplateName("默认模版");
        return templateNew;
    }

    /**
     * 添加默认素材库模板
     * @return
     */
    private TemplateNew addDefaultMaterial(){

        TemplateNew templateNew = new TemplateNew();
        templateNew.setIsDefault(1);
        templateNew.setTemplateType("material");

        List<TElementNew> allChapters = new ArrayList<>();
        TElementNew quickView = new TElementNew();
        quickView.setChapterName("舆情速览");
        quickView.setChapterType(ReportConst.LISTRESOURCES);
        quickView.setChapterDetail("QUICKVIEW");
        quickView.setChapterPosition(1);
        //0未选中，1选中
        quickView.setSelected(1);
        allChapters.add(quickView);

        TElementNew listData = new TElementNew();
        listData.setChapterName("数据详情");
        listData.setChapterType(ReportConst.LISTRESOURCES);
        listData.setChapterDetail("LISTDATA");
        listData.setChapterPosition(2);
        listData.setSelected(1);
        allChapters.add( listData);


        templateNew.setTemplateList(JSON.toJSONString(allChapters));
        templateNew.setTemplateName("默认模版");
        return templateNew;
    }

    /**
     * 查询当前用户的所有专题  并且专题排列顺序与专题分析保持一致
     * @return
     */
    private List<SpecialProject> getSpecialProjects(){
        User loginUser = UserUtils.getUser();
        //重构表时出现的bug 三级都有时  第三集没删除  这段代码为了清除以前数据
        Iterable<SpecialProject> findAll = specialProjectRepository.findAll();
        for(SpecialProject s : findAll){
            if(StringUtil.isNotEmpty(s.getGroupId())){
                SpecialSubject findOne = specialSubjectRepository.findOne(s.getGroupId());
                if(ObjectUtil.isEmpty(findOne)){
                    specialProjectRepository.delete(s);
                }
            }
        }

        //置顶专题
        Criteria<SpecialProject> criteriaProject = new Criteria<>();
        criteriaProject.add(Restrictions.eq("topFlag", "top"));
        if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
            criteriaProject.add(Restrictions.eq("userId", loginUser.getId()));
        }else {
            criteriaProject.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
        }
        criteriaProject.orderByASC("sequence");//只按照拖拽顺序排  不按照修改时间排
        List<SpecialProject> topList = specialProjectService.findAll(criteriaProject);

        //其他专题
        List<SpecialProject> listBig = new ArrayList();
        Criteria<SpecialSubject> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("flag", 0));
        if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
            criteria.add(Restrictions.eq("userId", loginUser.getId()));
        }else {
            criteria.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
        }
        criteria.orderByASC("sequence");//只按照拖拽顺序排  不按照修改时间排
        //把一级的栏目和专项都装起来按照lastModifiedTime排序
        List<Object> oneSpecialOrSubject = new ArrayList<>();
        // 查询主题
        //主题是一级 只按照拖拽顺序排  不按照修改时间排
        List<SpecialSubject> listSubject = specialSubjectRepository.findAll(criteria);
        oneSpecialOrSubject.addAll(listSubject);
        // 查方案
        Criteria<SpecialProject> criteriaProject2 = new Criteria<>();
        if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
            criteriaProject2.add(Restrictions.eq("userId", loginUser.getId()));
        }else {
            criteriaProject2.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
        }

        criteriaProject2.orderByASC("sequence");//只按照拖拽顺序排  不按照修改时间排
        List<SpecialProject> listSpecialProject = specialProjectService.findAll(criteriaProject2);
        List<SpecialProject> noGroupAndTop = new ArrayList<>();
        for(SpecialProject special:listSpecialProject){
            if (StringUtil.isEmpty(special.getGroupId()) && !"top".equals(special.getTopFlag())) {
                noGroupAndTop.add(special);
            }
        }
        oneSpecialOrSubject.addAll(noGroupAndTop);
        //只按照拖拽顺序排  不按照修改时间排
        ObjectUtil.sort(oneSpecialOrSubject,"sequence", "asc");

        for(int n=0;n<oneSpecialOrSubject.size();n++){
            Object one=oneSpecialOrSubject.get(n);
            if(one instanceof SpecialSubject){//专题
                SpecialSubject subject = (SpecialSubject)one;
                String name = subject.getName();// 主题名字放到一级
                // 根据主题id查专题
                String id = subject.getId();
                Criteria<SpecialSubject> criteriaSubject = new Criteria<>();
                criteriaSubject.add(Restrictions.eq("subjectId", id));
                criteriaSubject.add(Restrictions.eq("flag", 1));
                criteriaSubject.orderByASC("sequence");//只按照拖拽顺序排  不按照修改时间排
                List<SpecialSubject> list = specialSubjectService.findAll(criteriaSubject);
                List<Object> subjectAndProject = new ArrayList<>();
                subjectAndProject.addAll(list);
                // 根据主题id查方案
                Criteria<SpecialProject> criteria3 = new Criteria<>();
                criteria3.add(Restrictions.eq("groupId", id));
                if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
                    criteria3.add(Restrictions.eq("userId", loginUser.getId()));
                }else {
                    criteria3.add(Restrictions.eq("subGroupId", loginUser.getSubGroupId()));
                }
                criteria3.orderByASC("sequence");//只按照拖拽顺序排  不按照修改时间排
                List<SpecialProject> listSmall = specialProjectService.findAll(criteria3);// 放到第二级
                subjectAndProject.addAll(listSmall);
                //只按照拖拽顺序排  不按照修改时间排
                ObjectUtil.sort(subjectAndProject,"sequence", "asc");
                //反射识别是专题还是专项进行查找下一级
                for(int i=0;i<subjectAndProject.size();i++){
                    Object o = subjectAndProject.get(i);
                    if(o instanceof SpecialSubject){//专题
                        SpecialSubject specialSubject = (SpecialSubject) o;
                        String specialSubjectId = specialSubject.getId();
                        // 根据专题id查方案
                        Criteria<SpecialProject> criteria2 = new Criteria<>();
                        criteria2.add(Restrictions.eq("groupId", specialSubjectId));
                        criteria2.orderByASC("sequence");//只按照拖拽顺序排  不按照修改时间排
                        List<SpecialProject> list2 = specialProjectService.findAll(criteria2);// 放到第三级
                        // 之前参数写错
                        // 重复了
                        for (SpecialProject special : list2) {
                            if (!"top".equals(special.getTopFlag())) {
                                // 把不置顶的放进去 左侧竖着显示
                                listBig.add(special);
                            }
                        }
                    }else if(o instanceof SpecialProject){//专项
                        SpecialProject specialProject = (SpecialProject) o;
                        if (ObjectUtil.isNotEmpty(specialProject) && !"top".equals(specialProject.getTopFlag())) {
                            // 之前某个主题下没有直属的方案
                            // 所以会返回一个空数组
                            listBig.add(specialProject);
                        }
                    }
                }

            }else if(one instanceof SpecialProject){//专项
                SpecialProject specialProject = (SpecialProject) one;
                listBig.add(specialProject);
            }
        }

        List<SpecialProject> returnSpecialProjects = new ArrayList<>();

        returnSpecialProjects.addAll(topList);
        returnSpecialProjects.addAll(listBig);

        return returnSpecialProjects;
    }

}
