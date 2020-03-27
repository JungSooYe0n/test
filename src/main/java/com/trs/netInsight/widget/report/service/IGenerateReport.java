package com.trs.netInsight.widget.report.service;

import java.util.List;
import java.util.Map;

import com.trs.netInsight.widget.report.entity.ReportDataNew;
import com.trs.netInsight.widget.report.entity.ReportNew;
import com.trs.netInsight.widget.report.entity.ReportResource;
import com.trs.netInsight.widget.report.entity.TemplateNew;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by shao.guangze on 2018年6月4日 下午3:13:13
 */
public interface IGenerateReport {

	//专题报生成word报告  改造 20191121
	String generateReport(ReportNew report, ReportDataNew reportDataNew,
			TemplateNew templateNew, Map<String, List<Map<String, String>>> base64data)throws Exception;

	//日报周报月报生成word报告
	String generateReport(ReportNew report, Map<Integer, List<ReportResource>> collect,
			TemplateNew templateNew, Map<String, List<Map<String, String>>> base64data, String reportIntro)throws Exception;

	//极简报告生成word报告
	String generateSimplerReport(ReportNew report, Map<Integer, List<ReportResource>> collect, Map<String, List<Map<String, String>>> base64data, String reportIntro)throws Exception;
	//素材库报告生成word报告
	String generateMaterialReport(ReportNew report,String templateList)throws Exception;
}
