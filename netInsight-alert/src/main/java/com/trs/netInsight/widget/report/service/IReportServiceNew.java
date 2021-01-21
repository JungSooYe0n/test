package com.trs.netInsight.widget.report.service;

import java.util.List;


import com.trs.netInsight.widget.report.entity.TemplateNew;
import com.trs.netInsight.widget.user.entity.User;
import org.springframework.data.domain.Page;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.widget.report.entity.ReportNew;
import com.trs.netInsight.widget.report.entity.TElementNew;
import com.trs.netInsight.widget.report.entity.TemplateNew4Page;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by shao.guangze on 2018年5月24日 下午6:01:06
 */
public interface IReportServiceNew {
	public List<TemplateNew4Page> findAllTemplate(String templateType, String groupName);

	public List<TemplateNew4Page> findEmptyTemplate(String reportType);

	public String deleteTemplate(String templateId);

	public String saveTemplate(String templateId, String templateName,String reportName,
                               String templateList, String templateType, String totalIssue, String thisIssue,
							   String preparationUnits, String preparationAuthors, String statisticsTime);

	public String saveSpecialTemplate(String templateId, String templateName,
			String templateList, String groupName);

	/**
	 * 极简模式 专报 添加|编辑 模板
	 * @param templateId
	 * @param templateName
	 * @param templateList
	 * @param templateType
	 * @return
	 */
	public String saveCustomTemplate(String templateId, String templateName,
									  String templateList, String templateType);

	public List<TElementNew> listAllReportResource( String reportType,
												   String templateId) throws TRSSearchException, TRSException;
	
	/**
	 * 加入资源池
	 * @throws Exception 
	 */
	public Object saveReportResource(String sids, String trslk,String userId, String groupName, String chapter,
									 String img_data, String reportType,
									 String templateId, String img_type, Integer chapterPosition,
									 String reportId,String mapto) throws Exception;

	public Object saveOverView( String userId, String chapter,String imgComment,String reportType, String templateId) throws Exception;

	public Object delReportResource(String sids);

	/**
	 * 修改历史报告中的资源
	 * @param id
	 * @param imgComment
	 * @return
	 */
	public Object updateReportResource(String id,String imgComment);
	/**
	 * 编辑报告页面点击预览
	 * @author shao.guangze
	 * @param reportName
	 * @param totalIssue
	 * @param thisIssue
	 * @param preparationUnits
	 * @param preparationAuthors
	 * @param templateId
	 * @param reportIntro
	 * @param statisticsTime
	 * @param resourceDeleted
	 * @return
	 * @throws Exception
	 */
	public List<Object> preview(String reportName, String totalIssue, String thisIssue,
								String preparationUnits, String preparationAuthors, String templateId,
								String reportIntro, String statisticsTime, Integer resourceDeleted) throws Exception;
	/**
	 * 没有走预览，直接生成报告
	 * @author shao.guangze
	 * @param reportIntro
	 * @param jsonImgElements
	 * @param report
	 */
	public ReportNew create(String reportIntro, String jsonImgElements,
			ReportNew report) throws Exception ;
	/**
	 * 已经走过预览
	 * @author shao.guangze
	 * @param reportIntro
	 * @param jsonImgElements
	 * @param reportId
	 */
	public ReportNew create(String reportIntro, String jsonImgElements,
			String reportId) throws Exception ;
	
	/**
	 * 报告列表，根据报告类型，分页展示。
	 * @author shao.guangze
	 * @param reportType
	 * @return
	 */
	public Page<ReportNew> listAllReport(String reportType, String searchText,
										 String groupName, Integer pageNum, Integer pageSize,String time);


	/**
	 * 从报告列表点击预览
	 * @author shao.guangze
     * @param reportId
     * @param reportType
     */
	public List<Object> listPreview(String reportId, String reportType);
	/**
	 * 删除报告 
	 * @author shao.guangze
	 * @param reportId
	 */
	public String deleteReport(String reportId);
	/**
	 * 更改模板的顺序calculateSpecialReportData
	 * @author shao.guangze
	 * @param templateId
	 * @param templatePosition
	 */
	public void templateOrderSet(String templateId, String templatePosition);

	
	/**
	 * 根据Id查询出该报告
	 * @author shao.guangze
	 * @param reportId
	 */
	public ReportNew download(String reportId);

	/**
	 * 日报周报月报
	 * 删除所有报告资源
	 * @author shao.guangze
	 * @param templateId
	 */
	public void delAllReportResource(String templateId);

    String reBuildReport(String reportId, String jsonImgElements) throws Exception;

	Object searchResources(String keyWords, String statisticsTime, Integer pageNum, Integer pageSize) throws TRSException;

	Object changePosition(Integer docPosition, Integer newPosition, String chapter, String templateId, int resourceStatus, String id, String reportDataId);

	/**
	 * 只为迁移历史数据
	 * @param userId
	 * @return
	 */
	public List<ReportNew> findReportByUserId(String userId);
	public void updateReportAll(List<ReportNew> reportNews);

	/**
	 * 只为迁移历史数据
	 * @param userId
	 * @return
	 */
	public List<TemplateNew> findTemplateByUserId(String userId);
	public void updateTemplateAll(List<TemplateNew> templateNews);
}
