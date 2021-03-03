package com.trs.netInsight.widget.report.service;

import com.trs.netInsight.widget.report.entity.Report;
import com.trs.netInsight.widget.report.entity.ReportNew;
import com.trs.netInsight.widget.report.entity.SpecialReportGroup;
import com.trs.netInsight.widget.report.entity.TemplateNew;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import freemarker.template.Template;

import java.text.ParseException;
import java.util.List;

/***
 *  Created by shao.guangze on 2018/7/10
 */
public interface ISpecialReportService {
    /**
     * 专报预览页 ,简单模式
     * @author shao.guangze
     * @param report
     * @param keyWords
     * @param excludeWords
     * @param keyWordsIndex
     * @param excludeWebs
     * @param simflag
     * @param timeRange
     * @param weight
     */
    List<Object> calculateSpecialReportData(boolean server, ReportNew report, String keyWords,
                                            String excludeWords, Integer keyWordsIndex, String excludeWebs,
                                            String simflag, String timeRange, String trsl, Integer searchType, boolean weight, SpecialProject specialProject) throws Exception;
    /**
     * 查询专报中的report_data.
     * @author shao.guangze
     * @param reportId
     * @return
     */
    List<Object> findSpecialData(String reportId) throws ParseException;


    List<Object> findIndexTabData(String reportId) throws ParseException;

    /**
     * @author shao.guangze
     * @param reportId
     * @param templateId
     * @return
     */
    List<Object> findSRDByTemplate(String reportId, String templateId);

    /**
     * 添加专报分组
     * @author shao.guangze
     * @param groupName
     * @return
     */
    String saveSpecialReportGroup(String groupName);

    /**
     * 根据当前userId查找所有分组
     * @author shao.guangze
     * @return
     */
    List<SpecialReportGroup> findAllGroup();

    /**
     * 删除专报列表数据中的1条数据
     * 根据reportId， chapterName sid锁定删除1条记录
     * 列表资源的删除实质上是修改
     * 图片资源的删除是真删除
     * @author shao.guangze
     * @param reportId
     * @param chapterName
     * @param sid
     */
    void delReportResource(String reportId, String chapterName) throws Exception ;

    /**
     * 专报报告生成
     * @author shao.guangze
     * @param reportId
     * @param templateId
     * @param reportIntro
     * @param statisticsTime
     * @param reportName
     * @param thisIssue
     * @param totalIssue
     * @param preparationAuthors
     */
    ReportNew createSepcial(String reportId, String templateId,String templateList,String jsonImgElements, String reportIntro, String statisticsTime, String reportName, String thisIssue, String totalIssue, String preparationUnits, String preparationAuthors,String dataSummary) throws Exception;

    /***
     * 从ReportServiceNewImpl跳转过来，这样专报、日报的listPreview都用1个接口
     * 根据report_type判断走专报还是日报
     * @param reportId
     * @param reportType
     * @return
     */
    List<Object> listPreview(String reportId, String reportType);

    /***
     * 从专题分析跳转到专题报告
     * @param specialId
     */
    List<Object> jumptoSpecialReport(String specialId) throws Exception;

    /***
     * 从日常监测跳转到专题报告
     * @param indexTabMapperId
     */
    List<Object> jumptoIndexTabReport(String indexTabMapperId) throws Exception;

    /***
     * 删除专报分组，一并删除该分组下的报告
     * ATTENTION: special_report_group表也要删除相应数据
     * @param groupName 分组名称
     */
    void delSpecialReportGroup(String groupName);

    /***
     * 编辑专报分组名称
     * @param originGroup
     * @param currentGroup
     */
    String editSpecialReportGroup(String originGroup, String currentGroup);

    /**
     * 生成专报时编辑图表模块类下面的文字
     * @param reportId  报告ID
     * @param chapterDetail 模块对应不变属性值
     * @param resourceId          章节中资源ID
     *  @param imgComment         修改内容
     * @throws Exception
     */
    void updateChartData(String reportId, String chapterDetail,
                         String resourceId,String imgComment) throws Exception;

    /**
     * 生成日报、周报 、月报时编辑数据概述模块
     * @param templateId
     * @param resourceId
     * @param imgComment
     * @throws Exception
     */
    void updateOverView(String templateId,String resourceId,String imgComment) throws Exception;

    List<ReportNew> findAllSpecialReports();

    List<TemplateNew> findMoRenSpecialByUserId(String userId);
    List<TemplateNew> findMoRenSpecialBySubGroupId(String subGroupId);
    void updateSpecialReport(ReportNew reportNew);

    Object findReportById(String id);
}
