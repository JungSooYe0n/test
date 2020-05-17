package com.trs.netInsight.widget.report.service;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.widget.report.entity.ReportNew;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 舆情报告 极简模式 业务层接口
 *
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/10/9.
 */
public interface ISimplerReportService {

 /**
  * 查询专题分析、自定义专题、素材库列表
  * @param type
  * @return
  * @throws OperationException
  */
 public Object getGroupList(String type) throws OperationException;

 /**
  *  极简模式 下 报告列表
  * @param reportType
  * @return
  */
 public List<ReportNew> listSimplerReport(String reportType);

 /**
  *  计算 报告数据
  * @param reportType
  * @param typeId
  * @param templateId
  * @return
  * @throws Exception
  */
 public List<Object> calculateReport(String reportType, String typeId, String templateId)throws Exception;

 /**
  * 生成报告  添加报告到MySQL
  * @param reportId
  * @param templateId
  * @return
  */
 public  ReportNew createImplerReport(String reportId,String templateId,String jsonImgElements) throws Exception;

 /**
  * 通过模板类型 获取模板列表
  * @param type
  * @return
  */
 public Object getTemplateNew(String type);

 /**
  * 查一个模板详情
  * @param templateId
  * @return
  */
 public Object findOneTemplateNew(String templateId);

 /**
  * 编辑历史报告时查询报告详情
  * @param reportId
  * @return
  */
 public List<Object> reportDetail(String reportId) throws OperationException;
 /**
  * 报告编辑后保存
  * @param reportId
  * @param reportName
  * @return
  * @throws Exception
  */
 public ReportNew updateReport(String reportId,String reportName,String templateList) throws Exception;


 /**
  * 报告编辑后保存
  * @param reportId
  * @param jsonImgElemets
  * @param reportIntro
  * @return
  * @throws Exception
  */
 public String reBuildReport(String reportId,String jsonImgElemets,String reportIntro) throws Exception;

 /**
  * 获取报告图表类数据
  * @param reportId
  * @return
  */
 public Object imgData(String reportId);

 /**
  * 编辑报告时删除资源
  * @param reportResourceId
  * @param reportId
  * @return
  */
 public Object deleteReportResource(String reportResourceId,String reportId);
}
