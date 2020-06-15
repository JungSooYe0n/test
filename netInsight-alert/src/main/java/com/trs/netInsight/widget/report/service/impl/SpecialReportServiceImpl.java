package com.trs.netInsight.widget.report.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.repository.IndexTabMapperRepository;
import com.trs.netInsight.widget.column.service.IColumnChartService;
import com.trs.netInsight.widget.report.constant.ReportConst;
import com.trs.netInsight.widget.report.entity.*;
import com.trs.netInsight.widget.report.entity.repository.*;
import com.trs.netInsight.widget.report.service.IGenerateReport;
import com.trs.netInsight.widget.report.service.ISpecialReportService;
import com.trs.netInsight.widget.report.task.IndexTabReportTask;
import com.trs.netInsight.widget.report.task.SepcialReportTask;
import com.trs.netInsight.widget.report.util.ReportUtil;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.entity.repository.SpecialProjectRepository;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.NonUniqueResultException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.trs.netInsight.widget.report.constant.ReportConst.*;

/***
 *  Created by shao.guangze on 2018/7/10
 *  所有专报操作
 */
/**
 * @Desc
 * @author yang.yanyan
 * @date 2019/11/22  11:31
 * @version
 */
@Service
public class SpecialReportServiceImpl implements ISpecialReportService {
    @Autowired
    private TemplateNewRepository templateNewRepository;

    @Autowired
    private ReportNewRepository reportNewRepository;

    @Autowired
    private ReportDataNewRepository reportDataNewRepository;

    @Autowired
    private ReportGroupRepository reportGroupRepository;

    @Autowired
    private IGenerateReport generateReportImpl;

    @Autowired
    private SpecialProjectRepository specialProjectRepository;

    @Autowired
    private SpecialReportPreivewRepository specialReportPreivewRepository;

    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
	private ReportResourceRepository reportResourceRepository;

    @Autowired
    private IndexTabMapperRepository indexTabMapperRepository;

    @Autowired
    private IColumnChartService columnChartService;
    /**
     * 单拿一个线程计算数据
     * 日报月报周报 添加报告资源时需要
     * 专题报计算数据时需要
     * */
    private static ExecutorService fixedThreadPool = Executors
            .newFixedThreadPool(5);

    @Override
    public List<Object> calculateSpecialReportData(boolean server, ReportNew report, String keyWords,
                                                   String excludeWords, Integer keyWordsIndex, String excludeWebs,
                                                   String simflag, String timeRange, String trsl, Integer searchType, boolean weight,SpecialProject specialProject) throws Exception {
        //数据统计概述 和 数据来源对比公用1组数据
        //为了能够查出1个来页面就能显示1个，每查询完成1次，保存到数据库1次。
        //真正计算数据之前先保证report中有reportDataId
        ReportDataNew reportData = new ReportDataNew();
        reportData.setDoneFlag(0);
        reportData = reportDataNewRepository.save(reportData);
        report.setReportDataId(reportData.getId());
        //保存 report ，保存 report_data。用户进入专报预览页时，并未选择模板， 但是仍要保存数据。
        report.setTemplateId("无");	//后期废弃该字段
        report.setStatisticsTime(ReportUtil.statisticsTimeHandle(timeRange));
        reportNewRepository.save(report);
        //单起1个线程计算数据
        fixedThreadPool.execute(new SepcialReportTask(server, weight, keyWords, excludeWords, keyWordsIndex, excludeWebs, simflag, timeRange, trsl, searchType, reportData,UserUtils.getUser().getId(),specialProject));

        List<Object> resultList = new ArrayList<>();
        resultList.add(report);		//报告头
        return resultList;
    }

    public List<Object> calculateIndexTabReportData(ReportNew report, ArrayList<HashMap<String,Object>> statistics,String timeRange) throws Exception {

        ReportDataNew reportData = new ReportDataNew();
        reportData.setDoneFlag(0);
        reportData = reportDataNewRepository.save(reportData);
        report.setReportDataId(reportData.getId());
        //保存 report ，保存 report_data。用户进入专报预览页时，并未选择模板， 但是仍要保存数据。
        report.setTemplateId("无");	//后期废弃该字段
        report.setStatisticsTime(ReportUtil.statisticsTimeHandle(timeRange));
        reportNewRepository.save(report);
        //单起1个线程计算数据
        fixedThreadPool.execute(new IndexTabReportTask(statistics,reportData,reportDataNewRepository));

        List<Object> resultList = new ArrayList<>();
        resultList.add(report);		//报告头
        return resultList;
    }

    @Override
    public List<Object> findSpecialData(String reportId) {
        ReportNew report ;
        if(StringUtils.isEmpty(reportId)){
            //第一次请求
            User loginUser = UserUtils.getUser();
            Page<ReportNew> reports = null;
            if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
                reports = reportNewRepository.findByReportTypeAndUserIdAndTemplateId("专报",loginUser.getId(),"无", new PageRequest(0, 10, new Sort(Sort.Direction.DESC , "createdTime")));
            }else {
                reports = reportNewRepository.findByReportTypeAndSubGroupIdAndTemplateId("专报",loginUser.getSubGroupId(),"无", new PageRequest(0, 10, new Sort(Sort.Direction.DESC , "createdTime")));
            }
            if (reports != null && reports.getTotalElements() < 1) { // 未检索到指定专报
                return null;
            }
            report = reports.getContent().get(0);
        }else{
            report = reportNewRepository.findOne(reportId);
        }
        String reportDataId = report.getReportDataId();
        ReportDataNew reportData = reportDataId == null ? null :reportDataNewRepository.findOne(reportDataId);
        if (reportData == null){// 非空,return
            return null;
        }
        List<Object> resultList = new ArrayList<>();

        List<TElementNew> elements = ReportUtil.createEmptyTemplateForSpecial(1);
        resultList.add(report);	//报告头

        List<TElementNew> tElementNews = ReportUtil.setDataInElements(elements,reportData);
        if (StringUtil.isNotEmpty(reportId)){
            List<TElementNew> elements4page = getUsefulElements(tElementNews,reportId);
            resultList.add(elements4page);
        }else{
            resultList.add(tElementNews);	//报告各章节内容
        }

        HashMap<String, Integer> hashMap = new HashMap<>();
        hashMap.put("doneFlag", reportData.getDoneFlag() == null ||  reportData.getDoneFlag() == 0 ? 0 :reportData.getDoneFlag());
        resultList.add(hashMap);
        return resultList;
    }

    @Override
    public List<Object> findSRDByTemplate(String reportId, String templateId) {
        if(StringUtils.isEmpty(reportId) || StringUtils.isEmpty(templateId)){
            return null;
        }else{
            ReportNew report = reportNewRepository.findOne(reportId);
            ReportDataNew reportData = reportDataNewRepository.findOne(report.getReportDataId());
            TemplateNew template = templateNewRepository.findOne(templateId);
            List<TElementNew> elementList = JSONArray.parseArray(template.getTemplateList(), TElementNew.class);
            List<Object> resultList = new ArrayList<>();
            resultList.add(report);

            List<TElementNew> tElementNews = ReportUtil.setDataInElements(elementList, reportData);
            List<TElementNew> elements4page = getUsefulElements(tElementNews, reportId);
            resultList.add(elements4page);

            //resultList.add(tElementNews);
            stDoneFlagHandle(elementList, tElementNews, resultList, reportData);
            return resultList;
        }
    }

    /***
     * 如果该模板下数据已经能够全部展示，即该模板部分数据已经计算完成，
     * 则返回doneFlag 1 ，表示用户可以点击 "完成",去生成报告了;
     * 报告简介虽然页面上为空，但是传进来的tElementNews中是有1个empty的reportIntro的
     * @param elementList   模板
     * @param tElementNews  数据
     * @param resultList
     */
    private void stDoneFlagHandle(List<TElementNew> elementList, List<TElementNew> tElementNews, List<Object> resultList, ReportDataNew reportData) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        if(reportData != null && reportData.getDoneFlag() == 1){
            hashMap.put("doneFlag",1);
            resultList.add(hashMap);
            return;
        }
        AtomicReference<Integer> doneFlag = new AtomicReference<>(1);   //默认已经完成
        //只要模板中给定的章节里，tElementNews该章节没数据，那么doneFlag就为0
        elementList.stream().filter(e -> e.getSelected() == 1).forEach(e -> {
            List<TElementNew> collect = tElementNews.stream().filter(el -> el.getSelected() == 1 && el.getChapaterContent() != null).filter(el -> e.getChapterName().equals(el.getChapterName())).collect(Collectors.toList());
            if(collect == null || collect.size() == 0){
                doneFlag.set(0);
                return;
            }
        });
        hashMap.put("doneFlag",doneFlag.get());
        resultList.add(hashMap);
    }

    /***
     * 专报数据预览页，只返回有用数据给页面 ，已经返回给页面的数据不再返回。
     * 1.stPreview接口
     * 2.specialPreview 接口
     * 3.specialCreate 接口
     * @param tElementNews
     * @return
     */
    private List<TElementNew> getUsefulElements(List<TElementNew> tElementNews, String reportId) {
        //chapter positions in which report chapter data is available.
        List<Integer> positions = tElementNews.stream().filter(e -> e.getChapaterContent()!= null).map(e -> e.getChapterPosition()).collect(Collectors.toList());
        //已经保存到数据库中的位置
        User loginUser = UserUtils.getUser();
        List<SpecialReportPreivew> positionsInDB = null;
        if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
             positionsInDB = specialReportPreivewRepository.findByUserIdAndReportId(loginUser.getId(), reportId);
        }else {
            positionsInDB = specialReportPreivewRepository.findBySubGroupIdAndReportId(loginUser.getSubGroupId(),reportId);
        }
        //需要保存到数据库中的位置，同时也是需要返回给页面的位置
       // List<Integer> positionsUnsaved = positions.stream().filter(e -> !positionsInDB.contains(new SpecialReportPreivew(e))).collect(Collectors.toList());
        List<Integer> positionsUnsaved = new ArrayList<>();
        //改造上一行代码的遍历比较方式
        List<Integer> positionsSaved = new ArrayList<>();
        for (SpecialReportPreivew specialReportPreivew : positionsInDB) {
            positionsSaved.add(specialReportPreivew.getChapterPosition());
        }
        for (Integer position : positions) {
            if (!positionsSaved.contains(position)){
                positionsUnsaved.add(position);
            }
        }
        List<SpecialReportPreivew> positions4save = positionsUnsaved.stream().map(e -> new SpecialReportPreivew(e, reportId)).collect(Collectors.toList());
        //改造上一行代码的遍历比较方式
        specialReportPreivewRepository.save(positions4save);
        List<TElementNew> elements4page = tElementNews.stream().filter(e -> positionsUnsaved.contains(e.getChapterPosition())).map(e ->{
            //对应计算数据时所做的容错处理。
            if (e.getChapaterContent().size()>0){
                if(e.getChapterType().equals(ReportConst.LISTRESOURCES) && StringUtil.isEmpty(e.getChapaterContent().get(0).getContent())){
                    e.setChapaterContent(null);
                }else if(e.getChapterType().equals(ReportConst.CHART) && StringUtil.isEmpty(e.getChapaterContent().get(0).getImg_data())){
                    e.setChapaterContent(null);
                }

            }else {
                e.setChapaterContent(null);
            }
            return e;

        }).collect(Collectors.toList());
        return elements4page;

    }

    @Override
    public String  saveSpecialReportGroup(String groupName) {
        SpecialReportGroup group = reportGroupRepository.findByUserIdAndGroupName(UserUtils.getUser().getId(), groupName);
        if(group != null)
            return ALREADYEXIST;
        SpecialReportGroup specialReportGroup = new SpecialReportGroup();
        specialReportGroup.setGroupName(groupName);
        specialReportGroup.setUserId(UserUtils.getUser().getId());
        reportGroupRepository.save(specialReportGroup);
        return Const.SUCCESS;
    }

    @Override
    public List<SpecialReportGroup> findAllGroup() {
        User loginUser = UserUtils.getUser();
        List<SpecialReportGroup> allGroup = null;
        if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
            allGroup = reportGroupRepository.findByUserId(loginUser.getId());
        }else {
            allGroup = reportGroupRepository.findBySubGroupId(loginUser.getSubGroupId());
        }
        if(CollectionUtils.isEmpty(allGroup)){
            SpecialReportGroup defaultGroup = createDefaultGroup();
            reportGroupRepository.save(defaultGroup);
            List<SpecialReportGroup> result = new ArrayList<>();
            result.add(defaultGroup);
            return result;
        }else{
            return allGroup;
        }
    }

    /***
     * 创建默认分组
     * @return 创建的默认分组
     */
    private SpecialReportGroup createDefaultGroup() {
        SpecialReportGroup group = new SpecialReportGroup();
        group.setGroupName("默认分组");
        User loginUser = UserUtils.getUser();
        group.setUserId(loginUser.getId());
        group.setSubGroupId(loginUser.getSubGroupId());
        return group;
    }

    @Override
    public void delReportResource(String reportId, String chapterDetail,
                                  String resourceId) throws Exception {
        ReportNew report = reportNewRepository.findOne(reportId);
        ReportDataNew reportData = reportDataNewRepository.findOne(report.getReportDataId());
        String result = "";
        String jsonData = reportData.getClass().getDeclaredMethod(CHAPTERS2METHODNEW.get(chapterDetail)).invoke(reportData).toString();
        List<ReportResource> reportResources = JSONObject.parseObject(jsonData, new TypeReference<List<ReportResource>>(){});
        AtomicInteger i = new AtomicInteger(0);
        //filter 指的是过滤出满足某条件的数据。而不是将满足某条件的数据过滤走。
        List<ReportResource> reportResourcesResult = reportResources.stream()
                .filter(e -> ! e.getId().equals(resourceId))
                .sorted(Comparator.comparing(ReportResource::getDocPosition))
                .map(e ->{
                    e.setDocPosition(i.incrementAndGet());
                    return e;
                })
                .collect(Collectors.toList());
        result = JSON.toJSONString(reportResourcesResult);
        switch (chapterDetail) {
            case OVERVIEWOFDATANew:
                reportDataNewRepository.saveOverviewOfdata(null, reportData.getId());
                break;
            case NEWSTOP10New:
                reportDataNewRepository.saveNewsTop10(result, reportData.getId());
                break;
            case WEIBOTOP10New:
                reportDataNewRepository.saveWeiboTop10(result, reportData.getId());
                break;
            case WECHATTOP10New:
                reportDataNewRepository.saveWechatTop10(result, reportData.getId());
                break;
            case DATATRENDANALYSISNew:
                reportDataNewRepository.saveDataTrendAnalysis(null, reportData.getId());
                break;
            case DATASOURCEANALYSISNew:
                reportDataNewRepository.saveDataSourceAnalysis(null, reportData.getId());
                break;
            case WEBSITESOURCETOP10New:
                reportDataNewRepository.saveWebsiteSourceTop10(null, reportData.getId());
                break;
            case WEIBOACTIVETOP10New:
                reportDataNewRepository.saveWeiboActiveTop10(null, reportData.getId());
                break;
            case WECHATACTIVETOP10New:
                reportDataNewRepository.saveWechatActiveTop10(null, reportData.getId());
                break;
            case AREANew:
                reportDataNewRepository.saveArea(null, reportData.getId());
                break;
            case EMOTIONANALYSISNew:
                reportDataNewRepository.saveEmotionAnalysis(null, reportData.getId());
                break;
            case NEWSHOTTOPICSNew:
                reportDataNewRepository.saveNewsHotTopics(result, reportData.getId());
                break;
            case NEWSHOTTOP10New://（修复专题报 改造遗留bug 20191227）
                reportDataNewRepository.saveNewsHotTopics(result, reportData.getId());
                break;
            case WEIBOHOTTOP10New://（修复专题报 改造遗留bug 20191227）
                reportDataNewRepository.saveWeiboHotTopics(result, reportData.getId());
                break;
            case WEIBOHOTTOPICSNew:
                reportDataNewRepository.saveWeiboHotTopics(result, reportData.getId());
                break;
            case WECHATHOTTOP10New://（修复专题报 改造遗留bug 20191227）
                reportDataNewRepository.saveWechatHotTop10(result, reportData.getId());
                break;
            default:
                break;
        }
    }

    @Override
    public ReportNew createSepcial(String reportId, String templateId, String jsonImgElements, String reportIntro, String statisticsTime, String reportName, String thisIssue, String totalIssue, String preparationUnits, String preparationAuthors) throws Exception {
        ReportNew report = reportNewRepository.findOne(reportId);
        userInputFiledHandle(statisticsTime, reportName, thisIssue, totalIssue, preparationUnits, preparationAuthors, report);
        ReportDataNew reportData = reportDataNewRepository.findOne(report.getReportDataId());
        TemplateNew templateNew = templateNewRepository.findOne(templateId);
        Map<String, List<Map<String, String>>> base64data = ReportUtil.getBase64data(jsonImgElements);
        reportData.setReportIntro(reportIntro);
        String reportPath = generateReportImpl.generateReport(report, reportData, templateNew, base64data);
        report.setTemplateId(templateId);
        report.setDocPath(reportPath);
        report.setTemplateList(templateNew.getTemplateList());
        reportNewRepository.save(report);
        reportDataNewRepository.save((reportData));

        specialReportPreivewRepository.deleteByUserIdAndReportId(UserUtils.getUser().getId(), reportId);
        return report;
    }

    /***
     * 精简reportData,将数据编辑页的25条(或少余25条)数据精简到10条，
     * 因为listPreview页面是不允许用户再编辑的。
     * @param reportData
     * @return
     */
    private ReportDataNew getUsefulReportData(ReportDataNew reportData) {

        CHAPTERS2METHODNEW.keySet().forEach(e ->{
            String jsonData ;
            try {
                //解决数据控  json解析时报空指针异常问题
                //  报告简介                    数据统计概述
                if("REPORTINTRO".equals(e) || "OVERVIEWOFDATA".equals(e)){
                    return;
                }else if ("NEWSTOP10".equals(e)){
                    if (null == reportData.getNewsTop10()){
                        return;
                    }
                }else if ("WEIBOTOP10".equals(e)){
                    if (null == reportData.getWeiboTop10()){
                        return;
                    }
                }else if ("WECHATTOP10".equals(e)){
                    if (null == reportData.getWechatTop10()){
                        return;
                    }
                }else if ("DATATRENDANALYSIS".equals(e)){
                    if (null == reportData.getDataTrendAnalysis()){
                        return;
                    }
                }else if ("DATASOURCEANALYSIS".equals(e)){
                    if (null == reportData.getDataSourceAnalysis()){
                        return;
                    }
                }else if ("WEBSITESOURCETOP10".equals(e)){
                    if (null == reportData.getWebsiteSourceTop10()){
                        return;
                    }
                }else if ("WEIBOACTIVETOP10".equals(e)){
                    if (null == reportData.getWeiboActiveTop10()){
                        return;
                    }
                }else if ("WECHATACTIVETOP10".equals(e)){
                    if (null == reportData.getWechatActiveTop10()){
                        return;
                    }
                }else if ("AREA".equals(e)){
                    if (null == reportData.getArea()){
                        return;
                    }
                }else if ("EMOTIONANALYSIS".equals(e)){
                    if (null == reportData.getEmotionAnalysis()){
                        return;
                    }
                }else if ("NEWSHOTTOPICS".equals(e)){
                    if (null == reportData.getNewsHotTopics()){
                        return;
                    }
                }else if ("WEIBOHOTTOPICS".equals(e)){
                    if (null == reportData.getWeiboHotTopics()){
                        return;
                    }
                }else if ("NEWSHOTTOP10".equals(e)){//专题报 改造 20191121
                    if (null == reportData.getNewsHotTopics()){
                        return;
                    }
                }else if ("WEIBOHOTTOP10".equals(e)){
                    if (null == reportData.getWeiboHotTopics()){
                        return;
                    }
                }else if ("WECHATHOTTOP10".equals(e)){
                    if (null == reportData.getWechatHotTop10()){
                        return;
                    }
                }
                jsonData = reportData.getClass().getDeclaredMethod(CHAPTERS2METHODNEW.get(e)).invoke(reportData).toString();
                List<ReportResource> reportResources = JSONObject.parseObject(jsonData, new TypeReference<List<ReportResource>>(){});
                if(!CollectionUtils.isEmpty(reportResources) && reportResources.size()>10){
                    reportResources = reportResources.subList(0,10);//同样是包头不包尾
                    String args = JSONArray.toJSONString(reportResources);
                    reportData.getClass().getDeclaredMethod(CHAPTERS2METHODSETNEW.get(e),String.class).invoke(reportData, args);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }

        });
        return reportData;
    }

    private void userInputFiledHandle(String statisticsTime, String reportName, String thisIssue, String totalIssue, String preparationUnits, String preparationAuthors, ReportNew report) {
        report.setStatisticsTime(statisticsTime);
        report.setReportName(reportName);
        report.setThisIssue(thisIssue);
        report.setTotalIssue(totalIssue);
        report.setPreparationUnits(preparationUnits);
        //report.setPreparationAuthors(preparationAuthors);
    }

    @Override
    public List<Object> listPreview(String reportId, String reportType) {
        ReportNew report = reportNewRepository.findOne(reportId);
        ReportDataNew reportData = reportDataNewRepository.findOne(report.getReportDataId());
        List<TElementNew> previewData = ReportUtil.setDataInElements(JSONArray.parseArray(report.getTemplateList(), TElementNew.class), reportData);
        //对应专报所作的容错处理
        List<TElementNew> previewData2 = previewData.stream().map(e -> {
            //对应计算数据时所做的容错处理。
            if(!CollectionUtils.isEmpty(e.getChapaterContent())){
                if (e.getChapterType().equals(ReportConst.LISTRESOURCES) && StringUtil.isEmpty(e.getChapaterContent().get(0).getContent())) {
                    e.setChapaterContent(null);
                } else if (e.getChapterType().equals(ReportConst.CHART) && StringUtil.isEmpty(e.getChapaterContent().get(0).getImg_data())) {
                    e.setChapaterContent(null);
                }
            }
            return e;
        }).collect(Collectors.toList());
        //给前端返序号
        List<String> orderList = new ArrayList<>();
        for(int i=0 ;i<previewData.size();i++){
            orderList.add(ROMAN2CHINESE.get(i+1));
        }

        Map<String,String> map = new HashMap<>();
        map.put("reportIntro", reportData.getReportIntro());
        List<Object> result = new ArrayList<>();
        result.add(report);		//报告头
        result.add(previewData2);//报告各章节
        result.add(map);		//报告简介
        result.add(orderList);
        return result;
    }

    @Override
    public void jumptoSpecialReport(String specialId) throws Exception {
        SpecialProject specialProject = specialProjectRepository.findOne(specialId);
        //按照原来生成专题报告的流程走
        //一次性，所以报告分组用1个页面查不到的就可以
        ReportNew report = new ReportNew.Builder().withReportName(specialProject.getSpecialName())
                .withTotalIssue(" XX ")
                .withThisIssue(" XX ")
                .withPreparationUnits("XX编制单位")
                .withPreparationAuthors("XX编制作者")
                .withGroupName(String.valueOf(System.currentTimeMillis()))
                .withReportType("专报").build();
        String organizationId = UserUtils.getUser().getOrganizationId();
        if(StringUtil.isNotEmpty(organizationId)){
            Organization organization = organizationRepository.findOne(organizationId);
            report.setPreparationUnits(organization == null ? null : organization.getOrganizationName());
        }
        SearchScope searchScope = specialProject.getSearchScope();
        Integer keywordsIndex ;
        switch (searchScope.getField().length){
            case 1:
                //标题
                keywordsIndex = 0;
                break;
            case 2:
                //标题 + 正文
                keywordsIndex = 1;
                break;
            default:
                //默认 标题 + 正文
                keywordsIndex = 1;
                break;
        }
        //simFlag
        String simFlaf;
        if(specialProject.isSimilar()){
            simFlaf = "netRemove";
        }else if(specialProject.isIrSimflag()){
            simFlaf = "urlRemove";
        }else if(specialProject.isIrSimflagAll()){
            simFlaf = "sourceRemove";
        }else{
            simFlaf = "no";
        }
        //trsl
        String trsl = specialProject.getTrsl();
        //searchType;
        Integer searchType = StringUtil.isEmpty(trsl) ? 0 : 1;
        boolean weight =  specialProject.isWeight();
        String timeRange ;
        if(StringUtil.isEmpty(specialProject.getTimeRange())){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = simpleDateFormat.format(specialProject.getStartTime());
            String endTime = simpleDateFormat.format(specialProject.getEndTime());
            timeRange = startTime + ";" + endTime;
        }else{
            timeRange = specialProject.getTimeRange();
        }
        //设置默认分组
        List<SpecialReportGroup> groups = this.findAllGroup();
        report.setGroupName(groups.get(0).getGroupName());
        this.calculateSpecialReportData(specialProject.isServer(), report,specialProject.getAnyKeywords(),specialProject.getExcludeWords(),keywordsIndex,"" ,simFlaf ,timeRange ,trsl,searchType, weight,specialProject);
    }

    @Override
    public void jumptoIndexTabReport(String indexTabMapperId) throws Exception {
        User user = UserUtils.getUser();
        IndexTabMapper mapper = indexTabMapperRepository.findOne(indexTabMapperId);
        ReportNew report = new ReportNew.Builder().withReportName(mapper.getIndexTab().getName())
                .withTotalIssue(" XX ")
                .withThisIssue(" XX ")
                .withPreparationUnits("XX编制单位")
                .withPreparationAuthors("XX编制作者")
                .withGroupName(String.valueOf(System.currentTimeMillis()))
                .withReportType("专报").build();
        String organizationId = UserUtils.getUser().getOrganizationId();
        if(StringUtil.isNotEmpty(organizationId)){
            Organization organization = organizationRepository.findOne(organizationId);
            report.setPreparationUnits(organization == null ? null : organization.getOrganizationName());
        }
        //设置默认分组
        List<SpecialReportGroup> groups = this.findAllGroup();
        report.setGroupName(groups.get(0).getGroupName());
        if (ObjectUtil.isEmpty(mapper)) {
            throw new TRSException(CodeUtils.FAIL,"当前栏目不存在");
        }
        String timeRange = mapper.getIndexTab().getTimeRange();

        Object result = columnChartService.getColumnChart(indexTabMapperId);
        Map<String,Object> statisticMap = (HashMap<String,Object>)result;
        ArrayList<HashMap<String,Object>> statistics =  (ArrayList<HashMap<String,Object>>)statisticMap.get("statisticalChart");
        this.calculateIndexTabReportData(report,statistics,timeRange);
    }

    @Override
    public void delSpecialReportGroup(String groupName) {
        String userId = UserUtils.getUser().getId();
        //删除special_report_group里的该条分组
        reportGroupRepository.deleteByUserIdAndGroupName(userId, groupName);
        //删除report_new表里该分组下的所有报告
        //Page<ReportNew> reports = reportNewRepository.findByReportTypeAndUserIdAndGroupNameAndDocPathNotNull("专报", userId, groupName, new PageRequest(0, 10, new Sort(Sort.Direction.DESC, "createdTime")));
        //if(reports.getTotalElements() != 0){
            reportNewRepository.deleteByUserIdAndGroupName(userId,groupName);
        //}
    }

    @Override
    public String editSpecialReportGroup(String originGroup, String currentGroup) {
        //判空处理
        if(StringUtil.isEmpty(originGroup) || StringUtil.isEmpty(currentGroup)){
            return "not-null";
        }
        String userId = UserUtils.getUser().getId();
        //验证currentGroup是否存在
        try {
            SpecialReportGroup judgeGroup = reportGroupRepository.findByUserIdAndGroupName(userId, currentGroup);
            if(judgeGroup != null)
                return ALREADYEXIST;
        }catch (NonUniqueResultException e){
            //测试数据错乱，因为原则上是不可能出现两个相同的分组的
        }catch (IncorrectResultSizeDataAccessException e){
            //测试数据错乱，因为原则上是不可能出现两个相同的分组的
        }

        //修改report_new表中的groupName
        List<ReportNew> originReports = reportNewRepository.findAllByReportTypeAndUserIdAndGroupName("专报", userId, originGroup);
        if(originReports != null && originReports.size() > 0){
            List<ReportNew> currentReports = new ArrayList<>();
            originReports.forEach(e -> {
                e.setGroupName(currentGroup);
                currentReports.add(e);
            });
            reportNewRepository.save(currentReports);
        }
        //修改special_report_group中的groupName
        SpecialReportGroup originReportGroup = reportGroupRepository.findByUserIdAndGroupName(userId, originGroup);
        originReportGroup.setGroupName(currentGroup);
        reportGroupRepository.save(originReportGroup);
        return Const.SUCCESS;
    }

    @Override
    public void updateChartData(String reportId, String chapterDetail, String resourceId,String imgComment) throws Exception {
        ReportNew report = reportNewRepository.findOne(reportId);
        ReportDataNew reportData = reportDataNewRepository.findOne(report.getReportDataId());

        if (StringUtils.equals(OVERVIEWOFDATANew,chapterDetail)){
            reportDataNewRepository.saveOverviewOfdata(imgComment, reportData.getId());
        }else {
            String jsonData = reportData.getClass().getDeclaredMethod(CHAPTERS2METHODNEW.get(chapterDetail)).invoke(reportData).toString();
            List<ReportResource> reportResources = JSONObject.parseObject(jsonData, new TypeReference<List<ReportResource>>(){});
            for (ReportResource reportResource : reportResources) {
                if (StringUtils.equals(resourceId,reportResource.getId())){
                    reportResource.setImgComment(imgComment);
                    String toJSONString = JSON.toJSONString(Collections.singletonList(reportResource));
                    switch (chapterDetail) {
                        case OVERVIEWOFDATANew:
                            reportDataNewRepository.saveOverviewOfdata(imgComment, reportData.getId());
                            break;
                        case DATATRENDANALYSISNew:
                            reportDataNewRepository.saveDataTrendAnalysis(toJSONString, reportData.getId());
                            break;
                        case DATASOURCEANALYSISNew:
                            reportDataNewRepository.saveDataSourceAnalysis(toJSONString, reportData.getId());
                            break;
                        case WEBSITESOURCETOP10New:
                            reportDataNewRepository.saveWebsiteSourceTop10(toJSONString, reportData.getId());
                            break;
                        case WEIBOACTIVETOP10New:
                            reportDataNewRepository.saveWeiboActiveTop10(toJSONString, reportData.getId());
                            break;
                        case WECHATACTIVETOP10New:
                            reportDataNewRepository.saveWechatActiveTop10(toJSONString, reportData.getId());
                            break;
                        case AREANew:
                            reportDataNewRepository.saveArea(toJSONString, reportData.getId());
                            break;
                        case EMOTIONANALYSISNew:
                            reportDataNewRepository.saveEmotionAnalysis(toJSONString, reportData.getId());
                            break;
                        default:
                            break;
                    }
                }
            }
        }

    }

    @Override
    public void updateOverView(String templateId, String resourceId, String imgComment) throws Exception {
        if (StringUtil.isNotEmpty(resourceId)){
            ReportResource reportResource = reportResourceRepository.findOne(resourceId);
            if (null != reportResource){
                reportResource.setImgComment(imgComment);
                reportResourceRepository.save(reportResource);
            }
        }
    }

    @Override
    public List<ReportNew> findAllSpecialReports() {

        return reportNewRepository.findByReportTypeAndTemplateListIsNotNull("专报");
    }

    @Override
    public List<TemplateNew> findMoRenSpecialByUserId(String userId) {
        return templateNewRepository.findByUserIdAndTemplateTypeAndIsDefault(userId,"专报",1);
    }

    @Override
    public List<TemplateNew> findMoRenSpecialBySubGroupId(String subGroupId) {
        return templateNewRepository.findBySubGroupIdAndTemplateTypeAndIsDefault(subGroupId,"专报",1);
    }

    @Override
    public void updateSpecialReport(ReportNew reportNew) {
        reportNewRepository.saveAndFlush(reportNew);
    }

    @Override
    public Object findReportById(String id) {
        return reportDataNewRepository.findOne(id);
    }

    public static String deleteOne(List<ReportResource> newsTop10Array, String resourceId){
    	List<ReportResource> saveList = new ArrayList<>();
    	//先定位该记录信息再二次遍历比较
		int j = 0;
		for (ReportResource reportResource : newsTop10Array) {
			//定位该记录的位置信息
			if(reportResource.equals(resourceId)){
				j = reportResource.getDocPosition();
			}
		}
		for(int i = 0; i < newsTop10Array.size(); i++){
			//每条记录原来的位置
			Integer docPosition = newsTop10Array.get(i).getDocPosition();
			if(j != 0 && newsTop10Array.get(i).getDocPosition() > j){
				newsTop10Array.get(i).setDocPosition(docPosition - 1);
			}else{
//				System.out.println("原位置信息为空！");
			}
			saveList.add(newsTop10Array.get(i));
		}
		return JSONArray.parseArray(JSON.toJSONString(saveList)).toJSONString();
    }

}
