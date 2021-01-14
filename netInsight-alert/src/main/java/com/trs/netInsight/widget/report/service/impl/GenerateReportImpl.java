package com.trs.netInsight.widget.report.service.impl;

import java.io.*;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.report.constant.Chapter;
import com.trs.netInsight.widget.report.constant.ReportConst;
import com.trs.netInsight.widget.report.constant.SimplerReportConst;
import com.trs.netInsight.widget.report.util.ReportUtil;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.trs.netInsight.widget.report.entity.ReportDataNew;
import com.trs.netInsight.widget.report.entity.ReportNew;
import com.trs.netInsight.widget.report.entity.ReportResource;
import com.trs.netInsight.widget.report.entity.TElementNew;
import com.trs.netInsight.widget.report.entity.TemplateNew;
import com.trs.netInsight.widget.report.service.IGenerateReport;
import org.springframework.util.CollectionUtils;

import static com.trs.netInsight.widget.report.constant.ReportConst.*;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by shao.guangze on 2018年6月4日 下午3:15:58
 */
@Service
@Slf4j
public class GenerateReportImpl implements IGenerateReport {
	
	@Value("${report.produceReport.path}")
	private String produceReport;
	/**
	 * 极简模式报存储路径
	 */
	@Value("${report.simplerReport.path}")
	private String simplerReport;

	@Value("${report.static.resource}")
	private String resourceUrl;

	@Autowired
	private OrganizationRepository organizationRepository;
	
	private static final String DOCX = ".docx";
	
	private static final Integer TEXTFONTSIZE = 12;	//小四
	private static final String TEXTFONTFAMILY = "仿宋";
	private static final Integer TITLEFONTSIZE = 14;	//四号
	private static final String TITLEFONTFAMILY = "黑体";
	private static final Integer PAGEHEADFONTSIZE = 12;
	private static final String PAGEHEADFONTFAMILY = "楷体";
	private static final String FONTCOLOR_BLACK = "000000";
	
	@Override
	public String generateReport(ReportNew report, ReportDataNew reportData,
			TemplateNew template, Map<String, List<Map<String, String>>> base64data) throws Exception{
		XWPFDocument xwpfDocument = new XWPFDocument();
		createFirstPage(xwpfDocument, report);
		List<TElementNew> elementList = JSONArray.parseArray(template.getTemplateList(), TElementNew.class);
		elementList = elementList.stream().filter(e -> e.getSelected() == 1).sorted(Comparator.comparing(TElementNew::getChapterPosition)).collect(Collectors.toList());
		int i = 0;
		for(TElementNew element : elementList){
			String chapterName = element.getChapterName();
			switch (element.getChapterDetail()) {
			case REPORTINTRONew:
				i++;
				singleParagraph(xwpfDocument, REPORTINTRO, reportData.getReportIntro(), i);
				log.info(String.format(GENERATEREPORTLOG,REPORTINTRO + DONE));
				break;
			case OVERVIEWOFDATANew:
				i++;
				JSONObject object = (JSONObject)(JSONObject.parseArray(reportData.getOverviewOfdata()).get(0));

				singleParagraph(xwpfDocument, OVERVIEWOFDATA, object.getString("imgComment"), i);
				log.info(String.format(GENERATEREPORTLOG,OVERVIEWOFDATA + DONE));
				break;
			case NEWSHOTTOP10New:
				String jsonStr = reportData.getNewsHotTopics();
				i++;
				//专题报 改造 20191121
				if (NEWSHOTTOPICS.equals(chapterName)){
					chapterName = NEWSHOTTOP10;
				}
				if(StringUtil.isNotEmpty(jsonStr)){
					//使用TypeReference解析带泛型的List
					List<ReportResource> chapaterContent = JSONObject.parseObject(jsonStr, new TypeReference<List<ReportResource>>() {});
					dataListParagraph(xwpfDocument, chapterName,element.getChapterDetail(),chapaterContent, i,element.getElementNewType());
				}else{
					dataListParagraph(xwpfDocument,chapterName,element.getChapterDetail(),null,i,element.getElementNewType());
				}
				log.info(String.format(GENERATEREPORTLOG,NEWSHOTTOP10 + DONE));
				break;
			case WEIBOHOTTOP10New:
				i++;
				//专题报 改造 20191121
				if (WEIBOHOTTOPICS.equals(chapterName)){
					chapterName = WEIBOHOTTOP10;
				}
				if(StringUtil.isNotEmpty(reportData.getWeiboTop10())){
					List<ReportResource> chapaterContent = JSONObject.parseObject(reportData.getWeiboTop10(), new TypeReference<List<ReportResource>>() {});
					dataListParagraph(xwpfDocument, chapterName,element.getChapterDetail(),chapaterContent, i,element.getElementNewType());
				}else{
					dataListParagraph(xwpfDocument,chapterName,element.getChapterDetail(),null,i,element.getElementNewType());
				}
				log.info(String.format(GENERATEREPORTLOG,WEIBOHOTTOP10 + DONE));
				break;
			case WECHATHOTTOP10New:
				i++;
				if(StringUtil.isNotEmpty(reportData.getWechatHotTop10())){
					List<ReportResource> chapaterContent = JSONObject.parseObject(reportData.getWechatHotTop10(), new TypeReference<List<ReportResource>>() {});
					dataListParagraph(xwpfDocument, element.getChapterName(),element.getChapterDetail(),chapaterContent, i,element.getElementNewType());
				}else{
					dataListParagraph(xwpfDocument,element.getChapterName(),element.getChapterDetail(),null,i,element.getElementNewType());
				}
				log.info(String.format(GENERATEREPORTLOG,WECHATHOTTOP10 + DONE));
				break;

			case WEMEDIAkey:
				i++;
				if(StringUtil.isNotEmpty(reportData.getWeMediaHot())){
					List<ReportResource> chapaterContent = JSONObject.parseObject(reportData.getWeMediaHot(), new TypeReference<List<ReportResource>>() {});
					dataListParagraph(xwpfDocument, element.getChapterName(),element.getChapterDetail(),chapaterContent, i,element.getElementNewType());
				}else{
					dataListParagraph(xwpfDocument,element.getChapterName(),element.getChapterDetail(),null,i,element.getElementNewType());
				}
				log.info(String.format(GENERATEREPORTLOG,WEMEDIA + DONE));
				break;
			//事件脉络
			case WEMEDIAEVENTCONTEXTkey:
				i++;
				if(StringUtil.isNotEmpty(reportData.getWemediaEventContext())){
					List<ReportResource> chapaterContent = JSONObject.parseObject(reportData.getWemediaEventContext(), new TypeReference<List<ReportResource>>() {});
					dataListParagraph(xwpfDocument, element.getChapterName(),element.getChapterDetail(),chapaterContent, i,element.getElementNewType());
				}else{
					dataListParagraph(xwpfDocument,element.getChapterName(),element.getChapterDetail(),null,i,element.getElementNewType());
				}
				log.info(String.format(GENERATEREPORTLOG,WEMEDIAEVENTCONTEXT + DONE));
				break;
			case WECHATEVENTCONTEXTkey:
				i++;
				if(StringUtil.isNotEmpty(reportData.getWechatEventContext())){
					List<ReportResource> chapaterContent = JSONObject.parseObject(reportData.getWemediaEventContext(), new TypeReference<List<ReportResource>>() {});
					dataListParagraph(xwpfDocument, element.getChapterName(),element.getChapterDetail(),chapaterContent, i,element.getElementNewType());
				}else{
					dataListParagraph(xwpfDocument,element.getChapterName(),element.getChapterDetail(),null,i,element.getElementNewType());
				}
				log.info(String.format(GENERATEREPORTLOG,WECHATEVENTCONTEXT + DONE));
				break;
			case NEWSEVENTCONTEXTkey:
				i++;
				if(StringUtil.isNotEmpty(reportData.getNewsEventContext())){
					List<ReportResource> chapaterContent = JSONObject.parseObject(reportData.getNewsEventContext(), new TypeReference<List<ReportResource>>() {});
					dataListParagraph(xwpfDocument, element.getChapterName(),element.getChapterDetail(),chapaterContent, i,element.getElementNewType());
				}else{
					dataListParagraph(xwpfDocument,element.getChapterName(),element.getChapterDetail(),null,i,element.getElementNewType());
				}
				log.info(String.format(GENERATEREPORTLOG,NEWSEVENTCONTEXT + DONE));
				break;
			case WEIBOEVENTCONTEXTkey:
				i++;
				if(StringUtil.isNotEmpty(reportData.getWeiboEventContext())){
					List<ReportResource> chapaterContent = JSONObject.parseObject(reportData.getWeiboEventContext(), new TypeReference<List<ReportResource>>() {});
					dataListParagraph(xwpfDocument, element.getChapterName(),element.getChapterDetail(),chapaterContent, i,element.getElementNewType());
				}else{
					dataListParagraph(xwpfDocument,element.getChapterName(),element.getChapterDetail(),null,i,element.getElementNewType());
				}
				log.info(String.format(GENERATEREPORTLOG,WEIBOEVENTCONTEXT + DONE));
				break;
			case SITUATIONACCESSMENTkey:
				i++;
				if(base64data.get(element.getChapterName()) != null){
					List<Map<String, String>> chapterContent = base64data.get(element.getChapterName());
					imgParagraph(xwpfDocument, element.getChapterName(), chapterContent, i);
				}else{
					imgParagraph(xwpfDocument, element.getChapterName(), null, i);
				}
				log.info(String.format(GENERATEREPORTLOG,SITUATIONACCESSMENT + DONE));
				break;
			case WEIBOHOTTOPICSkey:
				i++;
				String topicJson = reportData.getWeiboHotTopics();
				if (base64data.get(element.getChapterName()) != null){
					List<Map<String, String>> chapterContent = base64data.get(element.getChapterName());
					imgParagraph(xwpfDocument, element.getChapterName(), chapterContent, i);
				}else {
					imgParagraph(xwpfDocument, element.getChapterName(), null, i);
				}
				log.info(String.format(GENERATEREPORTLOG,WEIBOHOTTOPICS + DONE));
				break;
			case PROPAFATIONANALYSISkey:
				i++;
				if(base64data.get(element.getChapterName()) != null){
					List<Map<String, String>> chapterContent = base64data.get(element.getChapterName());
					imgParagraph(xwpfDocument, element.getChapterName(), chapterContent, i);
				}else{
					imgParagraph(xwpfDocument, element.getChapterName(), null, i);
				}
				log.info(String.format(GENERATEREPORTLOG,PROPAFATIONANALYSIS + DONE));
				break;
			case ACTIVEACCOUNTkey:
				i++;
				if(base64data.get(element.getChapterName()) != null){
					List<Map<String, String>> chapterContent = base64data.get(element.getChapterName());
					imgParagraph(xwpfDocument, element.getChapterName(), chapterContent, i);
				}else{
					imgParagraph(xwpfDocument, element.getChapterName(), null, i);
				}
				log.info(String.format(GENERATEREPORTLOG,ACTIVEACCOUNT + DONE));
				break;
			case DATATRENDANALYSISNew:
				i++;
				if(base64data.get(element.getChapterName()) != null){
					List<Map<String, String>> chapterContent = base64data.get(element.getChapterName());
					imgParagraph(xwpfDocument, element.getChapterName(), chapterContent, i);
				}else{
					imgParagraph(xwpfDocument, element.getChapterName(), null, i);
				}
				log.info(String.format(GENERATEREPORTLOG,DATATRENDANALYSIS + DONE));
				break;


			case DATASOURCEANALYSISNew:
				i++;
				if(base64data.get(element.getChapterName()) != null){
					List<Map<String, String>> chapterContent = base64data.get(element.getChapterName());
					imgParagraph(xwpfDocument, element.getChapterName(), chapterContent, i);
				}else{
					imgParagraph(xwpfDocument, element.getChapterName(), null, i);
				}
				log.info(String.format(GENERATEREPORTLOG,DATASOURCEANALYSIS + DONE));
				break;
			case OPINIONANALYSISkey:
				i++;
				if(base64data.get(element.getChapterName()) != null){
					List<Map<String, String>> chapterContent = base64data.get(element.getChapterName());
					imgParagraph(xwpfDocument, element.getChapterName(), chapterContent, i);
				}else{
					imgParagraph(xwpfDocument, element.getChapterName(), null, i);
				}
				log.info(String.format(GENERATEREPORTLOG,OPININOANALYSIS + DONE));
				break;



			case WEBSITESOURCETOP10New:
				i++;
				if(base64data.get(element.getChapterName()) != null){
					List<Map<String, String>> chapterContent = base64data.get(element.getChapterName());
					imgParagraph(xwpfDocument, element.getChapterName(), chapterContent, i);
				}else{
					imgParagraph(xwpfDocument, element.getChapterName(), null, i);
				}
				log.info(String.format(GENERATEREPORTLOG,WEBSITESOURCETOP10 + DONE));
				break;
			case WEIBOACTIVETOP10New:
				i++;
				if(base64data.get(element.getChapterName()) != null){
					List<Map<String, String>> chapterContent = base64data.get(element.getChapterName());
					imgParagraph(xwpfDocument, element.getChapterName(), chapterContent, i);
				}else{
					imgParagraph(xwpfDocument, element.getChapterName(), null, i);
				}
				log.info(String.format(GENERATEREPORTLOG,WEIBOACTIVETOP10 + DONE));
				break;
			case WECHATACTIVETOP10New:
				i++;
				if(base64data.get(element.getChapterName()) != null){
					List<Map<String, String>> chapterContent = base64data.get(element.getChapterName());
					imgParagraph(xwpfDocument, element.getChapterName(), chapterContent, i);
				}else{
					imgParagraph(xwpfDocument, element.getChapterName(), null, i);
				}
				log.info(String.format(GENERATEREPORTLOG,WECHATACTIVETOP10 + DONE));
				break;
			case AREANew:
				i++;
				if(base64data.get(element.getChapterName()) != null){
					List<Map<String, String>> chapterContent = base64data.get(element.getChapterName());
					imgParagraph(xwpfDocument, element.getChapterName(), chapterContent, i);
				}else{
					imgParagraph(xwpfDocument, element.getChapterName(), null, i);
				}
				log.info(String.format(GENERATEREPORTLOG,AREA + DONE));
				break;
			case EMOTIONANALYSISNew:
				i++;
				if(base64data.get(element.getChapterName()) != null){
					List<Map<String, String>> chapterContent = base64data.get(element.getChapterName());
					imgParagraph(xwpfDocument, element.getChapterName(), chapterContent, i);
				}else{
					imgParagraph(xwpfDocument, element.getChapterName(), null, i);
				}
				log.info(String.format(GENERATEREPORTLOG,EMOTIONANALYSIS + DONE));
				break;
			case MOODSTATISTICSkey:
				i++;
				if(base64data.get(element.getChapterName()) != null){
					List<Map<String, String>> chapterContent = base64data.get(element.getChapterName());
					imgParagraph(xwpfDocument, element.getChapterName(), chapterContent, i);
				}else{
					imgParagraph(xwpfDocument, element.getChapterName(), null, i);
				}
				log.info(String.format(GENERATEREPORTLOG,MOODSTATISTICS + DONE));
				break;
			case WORDCLOUDSTATISTICSkey:
				i++;
				if(base64data.get(element.getChapterName()) != null){
					List<Map<String, String>> chapterContent = base64data.get(element.getChapterName());
					imgParagraph(xwpfDocument, element.getChapterName(), chapterContent, i);
				}else{
					imgParagraph(xwpfDocument, element.getChapterName(), null, i);
				}
				log.info(String.format(GENERATEREPORTLOG,WORDCLOUDSTATISTICS + DONE));
				break;


			default:
				break;
			}
		}
		String reportPath =getReportPath(report);
		xwpfDocument.write(new FileOutputStream(reportPath));
		xwpfDocument.close();
		log.info(String.format(GENERATEREPORTLOG,"已完成"+",reportPath:	"+ reportPath));
		return reportPath;
	}

	@Override
	public String generateReport(ReportNew report, Map<Integer, List<ReportResource>> collect, TemplateNew templateNew, Map<String, List<Map<String, String>>> base64data, String reportIntro) throws Exception {
		XWPFDocument xwpfDocument = new XWPFDocument();
		createFirstPage(xwpfDocument, report);
		List<TElementNew> elementList = ReportUtil.setResoucesIntoElements(templateNew, collect);
//		elementList = ReportUtil.tElementListHandle(elementList);
		AtomicInteger i = new AtomicInteger(0);
		for(TElementNew e : elementList){
			Chapter chapter = Chapter.valueOf(e.getChapterDetail());
			switch (e.getChapterType()) {
				case ReportConst.SINGLERESOURCE:
					i.getAndIncrement();    //以原子方式将当前值 + 1
					List<ReportResource> chapaterContent = e.getChapaterContent();
					if (e.getChapterPosition() == 0 || chapter.equals(Chapter.Report_Synopsis)) {
						//报告简介
						singleParagraph(xwpfDocument, e.getChapterName(), reportIntro, i.intValue());
					} else if(e.getChapterPosition() == 1|| chapter.equals(Chapter.Statistics_Summarize)){
						//数据统计概述
						singleParagraph(xwpfDocument,  e.getChapterName(), CollectionUtils.isEmpty(chapaterContent) ? null : chapaterContent.get(0).getImgComment(), i.intValue());
					}
					break;
				case ReportConst.LISTRESOURCES:
					i.getAndIncrement();
					try {
						dataListParagraph(xwpfDocument, e.getChapterName(),e.getChapterDetail(), e.getChapaterContent(), i.intValue(),e.getElementNewType());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				case ReportConst.CHART:
					i.getAndIncrement();
					String base64MapKey = e.getChapterName();
					String keyName = e.getChapterDetail() + "_"+e.getChapterPosition();
					if(base64data.containsKey(keyName)){
						base64MapKey = keyName;
					}
					List<Map<String, String>> chapterContent = base64data.get(base64MapKey);
					try {
						imgParagraph(xwpfDocument, e.getChapterName(), chapterContent, i.intValue());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				default:
					break;
			}
			}
			//加入附件
		List<ReportResource> chapterContent = new ArrayList<>();
		for (TElementNew tElementNew : elementList) {
			if ("列表(标题+摘要,另附正文)".equals(tElementNew.getElementNewType())){
			//if (true){
				List<ReportResource> chapaterContent = tElementNew.getChapaterContent();
				if (null != chapaterContent && chapaterContent.size()>0){
					chapterContent.addAll(chapaterContent);
				}
			}
		}
		addEnclosure(xwpfDocument, chapterContent);

		String reportPath = getReportPath(report);
		xwpfDocument.write(new FileOutputStream(reportPath));
		xwpfDocument.close();
		System.out.println("报告生成完毕，reportPath:" + reportPath);
		return reportPath;
	}

	@Override
	public String generateSimplerReport(ReportNew report, Map<Integer, List<ReportResource>> collect, Map<String, List<Map<String, String>>> base64data, String reportIntro) throws Exception {
		XWPFDocument xwpfDocument = new XWPFDocument();
		createSimplerFirstPage(xwpfDocument, report);
		TemplateNew templateNew = new TemplateNew();
		templateNew.setTemplateList(report.getTemplateList());
		List<TElementNew> elementList = ReportUtil.setResoucesIntoElements(templateNew, collect);
		AtomicInteger i = new AtomicInteger(0);
		for(TElementNew e : elementList){
			switch (e.getChapterType()) {
				case ReportConst.SINGLERESOURCE:
					i.getAndIncrement();    //以原子方式将当前值 + 1
					List<ReportResource> chapaterContent = e.getChapaterContent();
					if (e.getChapterPosition() == 1) {
						//报告简介
						singleParagraph(xwpfDocument, e.getChapterName(), reportIntro, i.intValue());
					} else if(e.getChapterPosition() == 2){
						//数据统计概述
						singleParagraph(xwpfDocument,  e.getChapterName(), CollectionUtils.isEmpty(chapaterContent) ? null : chapaterContent.get(0).getImgComment(), i.intValue());
					}
					break;
				case ReportConst.LISTRESOURCES:
					i.getAndIncrement();
					try {
						dataSimplerListParagraph(xwpfDocument, e.getChapterName(), e.getChapaterContent(), i.intValue(),e.getElementNewType(),e.getChapterDetail());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				case ReportConst.CHART:
					i.getAndIncrement();
					List<Map<String, String>> chapterContent = base64data.get(e.getChapterName());
					try {
						imgParagraph(xwpfDocument, e.getChapterName(), chapterContent, i.intValue());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					break;
				default:
					break;
			}
		}

		String reportPath = getSimplerReportPath(report);
		xwpfDocument.write(new FileOutputStream(reportPath));
		xwpfDocument.close();
		System.out.println("报告生成完毕，reportPath:" + reportPath);
		return reportPath;
	}


	@Override
	public String generateMaterialReport(ReportNew report,String templateList) throws Exception {
		XWPFDocument xwpfDocument = new XWPFDocument();
		createMaterialPage(xwpfDocument,report);
		//String templateList = templateNew.getTemplateList();
		if (StringUtil.isNotEmpty(templateList)){
			List<TElementNew> elementList = JSONArray.parseArray(templateList, TElementNew.class);
			for (TElementNew tElementNew : elementList) {
				if ("舆情速览".equals(tElementNew.getChapterName())){
					addMaterialTitle(xwpfDocument,tElementNew.getChapaterContent());
				}else {
					addMaterialBody(xwpfDocument,tElementNew.getChapaterContent());

				}
			}
		}
		String reportPath = getSimplerReportPath(report);
		xwpfDocument.write(new FileOutputStream(reportPath));
		xwpfDocument.close();
		return reportPath;
	}

	/**
	 * 获取极简模式报告生成路径
	 * @param report
	 * @return
	 */
	private String getSimplerReportPath(ReportNew report) {
		String dir = getSimplerReportDir(report.getReportType());
		if (report.getReportName().length() > 16){
			report.setReportName(report.getReportName().substring(0,16)+"...");
		}
		return dir + "/" + report.getReportName() + "_"
				+ System.currentTimeMillis() + DOCX;
	}

	/***
	 * 获取极简模式报告生成的路径	默认：organization_name/user_name/report_type/
	 */
	private String getSimplerReportDir(String reportType) {
		String userName = UserUtils.getUser().getUserName();
		String organizationId = UserUtils.getUser().getOrganizationId();
		String organizationName = "";
		if(StringUtil.isEmpty(organizationId)){
			organizationName = "默认机构";
//			if(!new File(simplerReport + "/" + organizationName).isDirectory()){
//				new File(simplerReport + "/" + organizationName).mkdirs();
//			}
		}else{
			Organization organization = organizationRepository.findOne(organizationId);
			if (ObjectUtil.isEmpty(organization)){
				organizationName = "默认机构";
			}else {
				organizationName = organization.getOrganizationName();
			}
//			if(!new File(simplerReport + "/" + organizationName).isDirectory()){
//				new File(simplerReport + "/" + organizationName).mkdirs();
//			}
		}
		if(!new File(simplerReport + "/" + organizationName + "/" + userName).isDirectory()){
			new File(simplerReport + "/" + organizationName + "/" + userName).mkdirs();
		}
		String dir = simplerReport + "/" + organizationName+ "/" + userName + "/" + reportType ;
		File file = new File(dir);
		if(file.isDirectory()){
			return dir;
		}else{
			file.mkdirs();
		}
		return dir;
	}


	private String getReportPath(ReportNew report) {
		String dir = getReportDir(report.getReportType());
		if (report.getReportName().length() > 40){
			report.setReportName(report.getReportName().substring(0,40)+"...");
		}
		return dir + "/" + report.getReportName() + "_"
				+ System.currentTimeMillis() + DOCX;
	}

	/***
	 * 获取生成报告的路径	默认：organization_name/user_name/report_type/
	 */
	private String getReportDir(String reportType) {
		String userName = UserUtils.getUser().getUserName();
		String organizationId = UserUtils.getUser().getOrganizationId();
		String organizationName = "";
		if(StringUtil.isEmpty(organizationId)){
			organizationName = "默认机构";
//			if(!new File(produceReport + "/" + organizationName).isDirectory()){
//				new File(produceReport + "/" + organizationName).mkdirs();
//			}
		}else{
			Organization organization = organizationRepository.findOne(organizationId);
			if (ObjectUtil.isEmpty(organization)){
				organizationName = "默认机构";
			}else {
				organizationName = organization.getOrganizationName();
			}
//			if(!new File(produceReport + "/" + organizationName).isDirectory()){
//				new File(produceReport + "/" + organizationName).mkdirs();
//			}
		}
		if(!new File(produceReport + "/" + organizationName + "/" + userName).isDirectory()){
			new File(produceReport + "/" + organizationName + "/" + userName).mkdirs();
		}
		String dir = produceReport + "/" + organizationName+ "/" + userName + "/" + reportType ;
		File file = new File(dir);
		if(file.isDirectory()){
			return dir;
		}else{
			file.mkdirs();
		}
		return dir;
	}

	private void singleParagraph(XWPFDocument xwpfDocument, String title, String text, int i) {
		// 一级标题统一字体14号、黑体、加粗
		XWPFParagraph titleParagraph = xwpfDocument.createParagraph();
		titleParagraph.setAlignment(ParagraphAlignment.LEFT);
		XWPFRun titleLine = titleParagraph.createRun();
		titleLine.setFontSize(TITLEFONTSIZE); //
		titleLine.setText(IntUtil.toChinese(i) +"、"+title);
		titleLine.setFontFamily(TITLEFONTFAMILY);
		titleLine.setBold(true); // 加粗
		// 正文统一字体12号、仿宋、首行缩进
		XWPFParagraph mainBodyParagraph = xwpfDocument.createParagraph();
		mainBodyParagraph.setAlignment(ParagraphAlignment.LEFT);
		mainBodyParagraph.setIndentationFirstLine(600);
		XWPFRun textLine = mainBodyParagraph.createRun();
		textLine.setFontSize(TEXTFONTSIZE);
		textLine.setFontFamily(TEXTFONTFAMILY);
		if(StringUtil.isEmpty(text)){
			textLine.setText("无");
		}else{
			textLine.setText(text);
		}
	}
	
	private void imgParagraph(XWPFDocument xwpfDocument, String title, List<Map<String, String>> chapterContent, int i) throws Exception{
		XWPFParagraph titleParagraph = xwpfDocument.createParagraph();
		titleParagraph.setAlignment(ParagraphAlignment.LEFT);
		XWPFRun titleLine = titleParagraph.createRun();
		titleLine.setFontSize(TITLEFONTSIZE); //
		titleLine.setText(IntUtil.toChinese(i)+"、"+title);
		titleLine.setFontFamily(TITLEFONTFAMILY);
		titleLine.setBold(true); // 加粗
		
		if(chapterContent != null && chapterContent.size() != 0){
			Base64 base64 = new Base64();
			for(int j =0 ; j <chapterContent.size() ; j++){
				XWPFParagraph mainBodyParagraph = xwpfDocument.createParagraph();
				mainBodyParagraph.setAlignment(ParagraphAlignment.CENTER);
				XWPFRun textLine = mainBodyParagraph.createRun();
				String chapterDetail = chapterContent.get(j).get("chapterDetail");
				byte[] imgComments = base64.decode(chapterContent.get(j).get("imgComment"));
				byte[] decode = null;
				if (imgComments != null){
					decode = base64.decode(chapterContent.get(j).get("img_data"));
				}
				//动态设置
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decode);
				int width = 5600000;
				int height = 3000000;
				switch (title){
					case DATATRENDANALYSIS:
						height = 3200000;
						break;
					case DATASOURCEANALYSIS:
						height = 3800000;
						break;
					case EMOTIONANALYSIS:
						height = 3800000;
						break;
					case MOODSTATISTICS:
						height = 3800000;
						break;
					case AREA:
						height = 2800000;
						break;
					case ACTIVEACCOUNT:
						height = 4400000;
						break;
					case WEIBOHOTTOPICS:
						height = 4400000;
						break;
					case OPININOANALYSIS:
						height = 2800000;
						break;
					case SITUATIONACCESSMENT:
						height = 3550000;
						break;
                    case PROPAFATIONANALYSIS:
                        height = 4670000;
                        break;
					default:break;
				}
				if(StringUtil.isNotEmpty(chapterDetail)){
					if(Chapter.Source_contrast.toString().equals(chapterDetail)){
						width = 5000000;
						height = 3300000;
					}else if(Chapter.Emotion_Analyze.toString().equals(chapterDetail)){
						width = 5000000;
						height = 3300000;
					}
				}
				textLine.addPicture(byteArrayInputStream, Document.PICTURE_TYPE_PNG, "1.png", width, height);
				XWPFParagraph commentParagraph = xwpfDocument.createParagraph();
				commentParagraph.setAlignment(ParagraphAlignment.LEFT);
				commentParagraph.setIndentationFirstLine(600);
				XWPFRun comment = commentParagraph.createRun();
				comment.setFontSize(TEXTFONTSIZE);
				comment.setFontFamily(TEXTFONTFAMILY);
				comment.setText(chapterContent.get(j).get("imgComment"));
			}
		}else{
			XWPFParagraph mainBodyParagraph = xwpfDocument.createParagraph();
			mainBodyParagraph.setAlignment(ParagraphAlignment.LEFT);
			mainBodyParagraph.setIndentationFirstLine(600);
			XWPFRun textLine = mainBodyParagraph.createRun();
			textLine.setFontSize(TEXTFONTSIZE);
			textLine.setFontFamily(TEXTFONTFAMILY);
			textLine.setText("无");
		}		
	}

	private void dataListParagraph(XWPFDocument xdoc, String title,String chapterDetail, List<ReportResource> chapterContent, int i,String eleType) throws Exception {
        XWPFParagraph titleParagraph = xdoc.createParagraph();    
        titleParagraph.setAlignment(ParagraphAlignment.LEFT);  
        XWPFRun titleLine = titleParagraph.createRun();  
        titleLine.setText(ROMAN2CHINESE.get(i)+"、"+title);  
        titleLine.setFontSize(TITLEFONTSIZE);  
        titleLine.setFontFamily(TITLEFONTFAMILY);  
        titleLine.setBold(true);
        if(chapterContent == null || chapterContent.size()==0){
        	XWPFParagraph mainBodyParagraph = xdoc.createParagraph();
        	mainBodyParagraph.setAlignment(ParagraphAlignment.LEFT);
        	mainBodyParagraph.setIndentationFirstLine(600);
    		XWPFRun textLine = mainBodyParagraph.createRun();
    		textLine.setFontSize(TEXTFONTSIZE);
    		textLine.setFontFamily(TEXTFONTFAMILY);
    		textLine.setText("无");
        }else {
        	//需要切一下专报的数据
        	if(chapterContent.size() > 10){
        		chapterContent = chapterContent.subList(0, 10);
        	}
			if (null == eleType){//专报
        		if(chapterDetail.indexOf("EVENTCONTEXT")!=-1){
        			//事件脉络
					XWPFTable dTable = xdoc.createTable(chapterContent.size()+1, 5);
					createTable4Context(dTable, xdoc, chapterContent, chapterDetail,true);
				}else {
					//专题报 改造 20191121 （热点模块要加一列 热度）
					//列数应该按照章节类型进行判断，暂时写4
					XWPFTable dTable = xdoc.createTable(chapterContent.size()+1, 5);
					createTable(dTable, xdoc, chapterContent, chapterDetail,true);
				}
			}else if ("表格".equals(eleType)){//日报、周报、月报
				//列数应该按照章节类型进行判断，暂时写4
				XWPFTable dTable = xdoc.createTable(chapterContent.size()+1, 4);
				createTable(dTable, xdoc, chapterContent, chapterDetail,false);
			}else {
				//列表
				createWordList(xdoc,chapterContent,chapterDetail,eleType);
			}

        }
	}

	private void createTable(XWPFTable xTable, XWPFDocument xdoc, List<ReportResource> chapaterContent, String chapterDetail,boolean hotCountFlag)
			throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String bgColor = "111111";
		CTTbl ttbl = xTable.getCTTbl();
		CTTblPr tblPr = ttbl.getTblPr() == null ? ttbl.addNewTblPr() : ttbl.getTblPr();

		CTTblWidth tblWidth = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr.addNewTblW();
		tblWidth.setW(new BigInteger("8600"));
		tblWidth.setType(STTblWidth.DXA);
		setCellTitle(xdoc, getCellHight(xTable, 0, 0), "序号", bgColor, 700);	//1000
		setCellTitle(xdoc, getCellHight(xTable, 0, 1), "标题", bgColor, 4300);
		int indexHead = 2;
		if (hotCountFlag && (NEWSHOTTOP10key.equals(chapterDetail) || WEIBOHOTTOP10key.equals(chapterDetail) || WECHATHOTTOP10key.equals(chapterDetail)
				|| WEMEDIAkey.equals(chapterDetail) || NEWSHOTTOPICSkey.equals(chapterDetail) || WEIBOHOTTOPICSkey.equals(chapterDetail)
				|| Chapter.Hot_Weibo.equals(Chapter.valueOf(chapterDetail))|| Chapter.Hot_News.equals(Chapter.valueOf(chapterDetail))
		)){
			/*if (WEIBOHOTTOP10key.equals(chapterDetail)){
				setCellTitle(xdoc, getCellHight(xTable, 0, indexHead), "转发", bgColor, 1200);	//2000
				indexHead += 1;
			}else {*/
				setCellTitle(xdoc, getCellHight(xTable, 0, indexHead), "热度", bgColor, 1200);	//2000
				indexHead += 1;
			//}
		}
		setCellTitle(xdoc, getCellHight(xTable, 0, indexHead), "来源", bgColor, 1200);	//2000
		indexHead += 1;
		setCellTitle(xdoc, getCellHight(xTable, 0, indexHead), "时间", bgColor, 2150);	//3000

			for(int i = 0; i < chapaterContent.size(); i++){
				//序号
				setCellText(xdoc, getCellHight(xTable, i + 1, 0), i + 1 + "",bgColor, 700);
				//标题
				setCellText(xdoc, getCellHight(xTable, i + 1, 1),ReportUtil.replaceHtml(chapaterContent.get(i).getTitle()), bgColor, 4300);
				int index = 2;
				if (hotCountFlag && (NEWSHOTTOP10key.equals(chapterDetail) || WEIBOHOTTOP10key.equals(chapterDetail) || WECHATHOTTOP10key.equals(chapterDetail)
						|| WEMEDIAkey.equals(chapterDetail) || NEWSHOTTOPICSkey.equals(chapterDetail) || WEIBOHOTTOPICSkey.equals(chapterDetail)
						|| Chapter.Hot_Weibo.equals(Chapter.valueOf(chapterDetail))|| Chapter.Hot_News.equals(Chapter.valueOf(chapterDetail))
				)){
					/*if (WEIBOHOTTOP10key.equals(chapterDetail)){
						setCellText(xdoc, getCellHight(xTable, i + 1, index), chapaterContent.get(i).getRttCount()==null?"0":chapaterContent.get(i).getRttCount().toString(),bgColor, 1200);
					}else {*/
						setCellText(xdoc, getCellHight(xTable, i + 1, index), chapaterContent.get(i).getSimCount(),bgColor, 1200);
					//}
					index += 1;
				}
				//来源
				setCellText(xdoc, getCellHight(xTable, i + 1, index), chapaterContent.get(i).getSiteName(),bgColor, 1200);
				index += 1;
					//时间
				Date urlDate = chapaterContent.get(i).getUrlDate();
				if(urlDate != null){
					setCellText(xdoc, getCellHight(xTable, i + 1, index), dateFormat.format(urlDate), bgColor, 2150);
				}else{
					setCellText(xdoc, getCellHight(xTable, i + 1, index), "", bgColor, 2150);
				}
			}
	}

	//事件脉络表格处理
	private void createTable4Context(XWPFTable xTable, XWPFDocument xdoc, List<ReportResource> chapaterContent, String chapterDetail,boolean hotCountFlag)
			throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String bgColor = "111111";
		CTTbl ttbl = xTable.getCTTbl();
		CTTblPr tblPr = ttbl.getTblPr() == null ? ttbl.addNewTblPr() : ttbl.getTblPr();

		CTTblWidth tblWidth = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr.addNewTblW();
		tblWidth.setW(new BigInteger("8600"));
		tblWidth.setType(STTblWidth.DXA);
		setCellTitle(xdoc, getCellHight(xTable, 0, 0), "序号", bgColor, 700);	//1000
		setCellTitle(xdoc, getCellHight(xTable, 0, 1), "标题", bgColor, 4300);
		int indexHead = 2;
		setCellTitle(xdoc, getCellHight(xTable, 0, indexHead), "来源", bgColor, 1200);	//2000
		indexHead += 1;
		setCellTitle(xdoc, getCellHight(xTable, 0, indexHead), "相似文章", bgColor, 1200);	//2000
		indexHead += 1;
		setCellTitle(xdoc, getCellHight(xTable, 0, indexHead), "时间", bgColor, 2150);	//3000

		for(int i = 0; i < chapaterContent.size(); i++){
			//序号
			setCellText(xdoc, getCellHight(xTable, i + 1, 0), i + 1 + "",bgColor, 700);
			//标题
			setCellText(xdoc, getCellHight(xTable, i + 1, 1),ReportUtil.replaceHtml(chapaterContent.get(i).getTitle()), bgColor, 4300);
			int index = 2;
			//来源
			setCellText(xdoc, getCellHight(xTable, i + 1, index), chapaterContent.get(i).getSiteName(),bgColor, 1200);
			index += 1;

			setCellText(xdoc, getCellHight(xTable, i + 1, index), chapaterContent.get(i).getSimCount(),bgColor, 1200);
			index += 1;
			//时间
			Date urlDate = chapaterContent.get(i).getUrlDate();
			if(urlDate != null){
				setCellText(xdoc, getCellHight(xTable, i + 1, index), dateFormat.format(urlDate), bgColor, 2150);
			}else{
				setCellText(xdoc, getCellHight(xTable, i + 1, index), "", bgColor, 2150);
			}
		}
	}
	private void dataSimplerListParagraph(XWPFDocument xdoc, String title, List<ReportResource> chapterContent, int i,String eleType,String chapterDetail) throws Exception {
		XWPFParagraph titleParagraph = xdoc.createParagraph();
		titleParagraph.setAlignment(ParagraphAlignment.LEFT);
		XWPFRun titleLine = titleParagraph.createRun();
		titleLine.setText(ROMAN2CHINESE.get(i)+"、"+title);
		titleLine.setFontSize(TITLEFONTSIZE);
		titleLine.setFontFamily(TITLEFONTFAMILY);
		titleLine.setBold(true);
		if(chapterContent == null || chapterContent.size()==0){
			XWPFParagraph mainBodyParagraph = xdoc.createParagraph();
			mainBodyParagraph.setAlignment(ParagraphAlignment.LEFT);
			mainBodyParagraph.setIndentationFirstLine(600);
			XWPFRun textLine = mainBodyParagraph.createRun();
			textLine.setFontSize(TEXTFONTSIZE);
			textLine.setFontFamily(TEXTFONTFAMILY);
			textLine.setText("无");
		}else {
			//需要切一下专报的数据
			if(chapterContent.size() > 10){
				chapterContent = chapterContent.subList(0, 10);
			}
			if (null == eleType || "表格".equals(eleType)){//表格
				//列数应该按照章节类型进行判断，暂时写4
				XWPFTable dTable = xdoc.createTable(chapterContent.size()+1, 4);
				createSimplerTable(dTable, xdoc, chapterContent, title,chapterDetail);
			}else {
				//列表
				createWordList(xdoc,chapterContent,title,eleType);
			}

		}
	}
	private void createSimplerTable(XWPFTable xTable, XWPFDocument xdoc, List<ReportResource> chapaterContent, String title,String chapterDetail)
			throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String bgColor = "111111";
		CTTbl ttbl = xTable.getCTTbl();
		CTTblPr tblPr = ttbl.getTblPr() == null ? ttbl.addNewTblPr() : ttbl.getTblPr();

		CTTblWidth tblWidth = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr.addNewTblW();
		tblWidth.setW(new BigInteger("8600"));
		tblWidth.setType(STTblWidth.DXA);
		setCellTitle(xdoc, getCellHight(xTable, 0, 0), "序号", bgColor, 700);	//1000
		if(title.contains("微博")){
			setCellTitle(xdoc, getCellHight(xTable, 0, 1), "正文", bgColor, 4300);	//6400
		}else{
			setCellTitle(xdoc, getCellHight(xTable, 0, 1), "标题", bgColor, 4300);
		}
		setCellTitle(xdoc, getCellHight(xTable, 0, 2), "来源", bgColor, 1200);	//2000
		if (SimplerReportConst.NEWSHOTTOPICSkey.equals(chapterDetail) || SimplerReportConst.WEIBOHOTTOPICSkey.equals(chapterDetail)){
			setCellTitle(xdoc, getCellHight(xTable, 0, 3), "热度", bgColor, 2150);	//3000
		}else {
			setCellTitle(xdoc, getCellHight(xTable, 0, 3), "时间", bgColor, 2150);	//3000
		}

		for(int i = 0; i < chapaterContent.size(); i++){
			//序号
			setCellText(xdoc, getCellHight(xTable, i + 1, 0), i + 1 + "",bgColor, 700);
			//标题
			setCellText(xdoc, getCellHight(xTable, i + 1, 1),ReportUtil.replaceHtml(chapaterContent.get(i).getTitle()), bgColor, 4300);
			//来源
			setCellText(xdoc, getCellHight(xTable, i + 1, 2), chapaterContent.get(i).getSiteName(),bgColor, 1200);
			if (SimplerReportConst.NEWSHOTTOPICSkey.equals(chapterDetail) || SimplerReportConst.WEIBOHOTTOPICSkey.equals(chapterDetail)){
				//热度
				String simCount = chapaterContent.get(i).getSimCount();
				if (StringUtil.isEmpty(simCount)){
					simCount="早期报告暂无数据";
				}
				setCellText(xdoc, getCellHight(xTable, i + 1, 3), simCount, bgColor, 2150);
			}else {
				//时间
				Date urlDate = chapaterContent.get(i).getUrlDate();
				if(urlDate != null){
					setCellText(xdoc, getCellHight(xTable, i + 1, 3), dateFormat.format(urlDate), bgColor, 2150);
				}else{
					setCellText(xdoc, getCellHight(xTable, i + 1, 3), "", bgColor, 2150);
				}
			}

		}
	}

	// 设置表格高度
	private XWPFTableCell getCellHight(XWPFTable xTable, int rowNomber,
			int cellNumber) {
		XWPFTableRow row = null;
		row = xTable.getRow(rowNomber);
		row.setHeight(100);
		XWPFTableCell cell = null;
		cell = row.getCell(cellNumber);
		return cell;
	}

	private void setCellText(XWPFDocument xDocxument,
			XWPFTableCell cell, String text, String bgcolor, int width)
			throws Exception {
		CTTc cttc = cell.getCTTc();
		CTTcPr cellPr = cttc.addNewTcPr();
		cellPr.addNewTcW().setW(BigInteger.valueOf(width));
		//cell.addParagraph();
		XWPFParagraph pIO = cell.addParagraph();
		pIO.setAlignment(ParagraphAlignment.CENTER);
		cell.removeParagraph(0);
		XWPFRun rIO = pIO.createRun();
			rIO.setFontFamily(TEXTFONTFAMILY);
			rIO.setColor(FONTCOLOR_BLACK);
			rIO.setFontSize(TEXTFONTSIZE);
			rIO.setText(text);
	}

	private void setCellTitle(XWPFDocument xDocxument,
			XWPFTableCell cell, String text, String bgcolor, int width) {
		CTTc cttc = cell.getCTTc();
		CTTcPr cellPr = cttc.addNewTcPr();
		cellPr.addNewTcW().setW(BigInteger.valueOf(width));
		XWPFParagraph pIO = cell.addParagraph();
		pIO.setAlignment(ParagraphAlignment.CENTER);
		cell.removeParagraph(0);
		XWPFRun rIO = pIO.createRun();
		rIO.setFontFamily(TEXTFONTFAMILY);
		rIO.setColor(FONTCOLOR_BLACK);
		rIO.setFontSize(TEXTFONTSIZE);
		rIO.setText(text);
		rIO.setBold(true);
	}

	/**
	 * 第一段;return 的值供月报模板使用
	 * @author shao.guangze
	 * @param xdoc
	 * @param reportName
	 */
	private Integer createP1(XWPFDocument xdoc, String reportName){
		XWPFParagraph createParagraph = xdoc.createParagraph();
		createParagraph.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun createRun = createParagraph.createRun();
		createRun.setFontSize(33);
		createRun.setColor("ff0000");//176 B0 240 f0
		createRun.setFontFamily("楷体GB2312");
		createRun.setBold(true);
		if(reportName.length() > 8){
			createRun.setText(reportName.substring(0, 8));
			createRun.addBreak();
			createRun.setText(reportName.substring(8));
			 xdoc.createParagraph().createRun().setText("");//加1个空行
			return 0;
		}else{
			createRun.setText(reportName);
			 xdoc.createParagraph().createRun().setText("");
			return 1;
		}
	}
	private void createP2(XWPFDocument xdoc, String thisIssue,String totalIssue){
		XWPFParagraph createParagraph2 = xdoc.createParagraph();
		createParagraph2.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun createRun2 = createParagraph2.createRun();
		createRun2.setFontSize(PAGEHEADFONTSIZE);
		createRun2.setText(new SimpleDateFormat("yyyy年").format(new Date())+"第" + thisIssue + "期（总共" +totalIssue+ "期）");
		createRun2.setFontFamily(PAGEHEADFONTFAMILY);
	}
	/**
	 * 第三段
	 * @author shao.guangze
	 */
	private void createP3(XWPFDocument xdoc, String preparationUnits){
		XWPFParagraph createParagraph4 = xdoc.createParagraph();
		createParagraph4.setAlignment(ParagraphAlignment.LEFT);
		XWPFRun createRun4 = createParagraph4.createRun();
		createRun4.setFontSize(PAGEHEADFONTSIZE);
		createRun4.setFontFamily(PAGEHEADFONTFAMILY);
			createRun4.setText(preparationUnits + countSpace(preparationUnits) + new SimpleDateFormat("yyyy年MM月dd日").format(new Date()));
	}
	private void createFirstPage(XWPFDocument xwpfDocument, ReportNew report) throws Exception{
		if("日报".equals(report.getReportType()) || "专报".equals(report.getReportType()) || "日常监测报".equals(report.getReportType())){
			//第一段：红色，33号字体，居中，楷体GB2312
			createP1(xwpfDocument, report.getReportName());
			//第二段：黑色 12号字 居中 楷体
			createP2(xwpfDocument,report.getThisIssue(), report.getTotalIssue());
			//第三段：黑色 12号字 居中 楷体
			createP3(xwpfDocument, report.getPreparationUnits());
			//第四段：
			XWPFParagraph createParagraph3 = xwpfDocument.createParagraph();
			createParagraph3.setAlignment(ParagraphAlignment.CENTER);
			XWPFRun createRun3 = createParagraph3.createRun();
			createRun3.addPicture(getRedLine(),
					Document.PICTURE_TYPE_PNG, "1.png", 5500000, 50000);
			//第五段
			XWPFParagraph createParagraph5 = xwpfDocument.createParagraph();
			createParagraph5.setAlignment(ParagraphAlignment.CENTER);
			XWPFRun createRun5 = createParagraph5.createRun();
			createRun5.setFontSize(PAGEHEADFONTSIZE);
			createRun5.setFontFamily(PAGEHEADFONTFAMILY);
			createRun5.setText("统计时间段:"+ report.getStatisticsTime());
			
			//加个回车
			xwpfDocument.createParagraph().createRun().setText("");
			
		}else if(("月报".equals(report.getReportType()) || ("周报".equals(report.getReportType())))){
			//第一段：红色，33号字体，居中，楷体GB2312
			Integer count = createP1(xwpfDocument, report.getReportName());
			//第二段：处理首页空白
			XWPFParagraph createParagraph3 = xwpfDocument.createParagraph();
			createParagraph3.setAlignment(ParagraphAlignment.CENTER);
			XWPFRun createRun3 = createParagraph3.createRun();
			createRun3.setFontSize(PAGEHEADFONTSIZE);
			createRun3.setText("（" +new SimpleDateFormat("yyyy").format(new Date()) + "年第" + report.getThisIssue() + "期  总第" + report.getTotalIssue()+"期）");
			createRun3.setFontFamily(PAGEHEADFONTFAMILY);
			
			for(int i=0; i<(8 + count); i++){
				XWPFParagraph createParagraph22 = xwpfDocument.createParagraph();
				createParagraph22.setAlignment(ParagraphAlignment.CENTER);
				XWPFRun createRun22 = createParagraph22.createRun();
				createRun22.setText(" ");
				createRun22.setFontSize(20);
				createRun22.setTextPosition(120);
				createRun22.addPicture(getRedLine(), Document.PICTURE_TYPE_PNG, "1.png", 50, 50);
			}
			
			//第四段：
			XWPFParagraph createParagraph4 = xwpfDocument.createParagraph();
			createParagraph4.setAlignment(ParagraphAlignment.CENTER);
			XWPFRun createRun4 = createParagraph4.createRun();
			createRun4.setFontSize(PAGEHEADFONTSIZE);
			createRun4.setText(report.getPreparationUnits());
			createRun4.setFontFamily(PAGEHEADFONTFAMILY);
			createRun4.setBold(true);
			
			//第五段
			XWPFParagraph createParagraph5 = xwpfDocument.createParagraph();
			createParagraph5.setAlignment(ParagraphAlignment.CENTER);
			XWPFRun createRun5 = createParagraph5.createRun();
			createRun5.setFontSize(PAGEHEADFONTSIZE);
			createRun5.setText(new SimpleDateFormat("yyyy年MM月dd日").format(new Date()));
			createRun5.setFontFamily(PAGEHEADFONTFAMILY);
			createRun5.setBold(true);
			
			xwpfDocument.createParagraph().createRun().setText("");
			xwpfDocument.createParagraph().createRun().setText("");
		}
		log.info(String.format(GENERATEREPORTLOG,"报告头" + DONE));
	}
	private InputStream getRedLine() throws IOException {
		File file = new File(resourceUrl + "RedLine.png");
		if(file.exists()){
			return new FileInputStream(file);
		}else{
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("src/main/resources/report/littleTemplate/RedLine.png");
            return resourceAsStream;
		}
	}

	/**
	 * 楷体_GB2312 16号字体，一行按照49个字符处理。1个汉字两个字符。statisticsTime 固定占有14个字符。即 preparationUnits + space = 35;
	 * 楷体 小四字体(12),一行按照63个字符处理。 63 - 14  = 49
	 * @author shao.guangze
	 * @param preparationUnits
	 * @param
	 * @return
	 */
	private String countSpace(String preparationUnits){
		int letterANDfigureCount = charCount(preparationUnits);
		String space = "";
		int spaceLength = 49  - ((preparationUnits.length() - letterANDfigureCount)* 2 + letterANDfigureCount);
		for(int i =0; i<spaceLength; i++){
			space += " ";
		}
		return space;
	}
	
	private int charCount(String chaSquen){
		int letterANDfigureCount = 0;
		char[] charArray = chaSquen.toCharArray();
		for(char each : charArray){
			//根据ASCII判断
			if((Integer.valueOf(each) > 33) && (Integer.valueOf(each) < 127)){
				letterANDfigureCount ++ ;
			}
		}
		return letterANDfigureCount;
	}

	/**
	 *  创建  列表 类 报告
	 * @param xdoc
	 * @param chapaterContent
	 * @param chapterDetail
	 * @param index
	 */
	public void createWordList(XWPFDocument xdoc, List<ReportResource> chapaterContent, String chapterDetail,String index){

			//只有正文
			for (int i = 0; i < chapaterContent.size(); i++) {

				if (chapterDetail.contains("WEIBO")){
					//标题
					XWPFParagraph turnTitle = xdoc.createParagraph();
					turnTitle.setAlignment(ParagraphAlignment.LEFT);
					//1.5倍行间距
					//turnTitle.setSpacingBetween(1.5);
					XWPFRun turnTitleRun = turnTitle.createRun();
					turnTitleRun.setText(i+1+". "+chapaterContent.get(i).getSiteName()+"：");
					turnTitleRun.setFontSize(TEXTFONTSIZE);
					turnTitleRun.setFontFamily(TEXTFONTFAMILY);
					//加粗
					turnTitleRun.setBold(true);

					//时间
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					XWPFParagraph turnTime = xdoc.createParagraph();
					turnTime.setAlignment(ParagraphAlignment.RIGHT);
					//1.5倍行间距
					//turnTime.setSpacingBetween(1.5);
					XWPFRun turnTimeRun = turnTime.createRun();
					turnTimeRun.setText(dateFormat.format(chapaterContent.get(i).getUrlDate()));
					turnTimeRun.setFontSize(TEXTFONTSIZE);
					turnTimeRun.setFontFamily(TEXTFONTFAMILY);
					//加粗
					turnTimeRun.setBold(true);

					//内容
					XWPFParagraph turnBody = xdoc.createParagraph();
					turnBody.setAlignment(ParagraphAlignment.BOTH);
					//首行缩进
					turnBody.setIndentationFirstLine(500);
					//1.5倍行间距
					turnBody.setSpacingBetween(1.5);
					XWPFRun turnBodyRun = turnBody.createRun();
					turnBodyRun.setText(chapaterContent.get(i).getContent());
					turnBodyRun.setFontSize(TEXTFONTSIZE);
					turnBodyRun.setFontFamily(TEXTFONTFAMILY);

				}else {

					//标题
					XWPFParagraph turnTitle = xdoc.createParagraph();
					turnTitle.setAlignment(ParagraphAlignment.LEFT);
					//1.5倍行间距
					//turnTitle.setSpacingBetween(1.5);
					XWPFRun turnTitleRun = turnTitle.createRun();
					turnTitleRun.setText(i+1+". "+chapaterContent.get(i).getTitle());
					turnTitleRun.setFontSize(TEXTFONTSIZE);
					turnTitleRun.setFontFamily(TEXTFONTFAMILY);
					turnTitleRun.setBold(true);

					//siteName + 时间
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					XWPFParagraph turnTime = xdoc.createParagraph();
					turnTime.setAlignment(ParagraphAlignment.RIGHT);
					//1.5倍行间距
					//turnTime.setSpacingBetween(1.5);
					XWPFRun turnTimeRun = turnTime.createRun();
					turnTimeRun.setText(chapaterContent.get(i).getSiteName()+"  "+dateFormat.format(chapaterContent.get(i).getUrlDate()));
					turnTimeRun.setFontSize(TEXTFONTSIZE);
					turnTimeRun.setFontFamily(TEXTFONTFAMILY);
					turnTimeRun.setBold(true);


					//内容
					if ("列表(标题+正文)".equals(index)){//标题+正文
						String content = chapaterContent.get(i).getContent();
						String[] split = content.split("\\s{1,}");
						for (String s : split) {
							XWPFParagraph turnBody = xdoc.createParagraph();
							//两边对齐
							turnBody.setAlignment(ParagraphAlignment.BOTH);
							//1.5倍行间距
							turnBody.setSpacingBetween(1.5);
							XWPFRun turnBodyRun = turnBody.createRun();
							turnBodyRun.setText(s);
							turnBodyRun.setFontSize(TEXTFONTSIZE);
							turnBodyRun.setFontFamily(TEXTFONTFAMILY);
						}
					}else if ("列表(标题+摘要)".equals(index) || "列表(标题+摘要,另附正文)".equals(index)){//标题+摘要
						//标题+摘要
						XWPFParagraph turnBody = xdoc.createParagraph();
						//两边对齐
						turnBody.setAlignment(ParagraphAlignment.BOTH);
						//首行缩进
						turnBody.setIndentationFirstLine(500);
						//1.5倍行间距
						turnBody.setSpacingBetween(1.5);
						XWPFRun turnBodyRun = turnBody.createRun();
						turnBodyRun.setText(chapaterContent.get(i).getNewsAbstract());
						turnBodyRun.setFontSize(TEXTFONTSIZE);
						turnBodyRun.setFontFamily(TEXTFONTFAMILY);
					}

				}

				//文章地址
				XWPFParagraph turnLink = xdoc.createParagraph();
				turnLink.setAlignment(ParagraphAlignment.LEFT);
				turnLink.setSpacingBetween(1.5);
				XWPFRun turnLinkRun = turnLink.createRun();
				turnLinkRun.setText("文章地址：");
				turnLinkRun.setFontSize(TEXTFONTSIZE);
				turnLinkRun.setFontFamily(TEXTFONTFAMILY);
				turnLinkRun.setBold(true);

				//放入超链接
				//appendExternalHyperlink(chapaterContent.get(i).getUrlName(),chapaterContent.get(i).getUrlName(),xdoc.createParagraph());

				//加个空行
				XWPFParagraph turnLinkContent = xdoc.createParagraph();
				//1.5倍行间距
				turnLinkContent.setSpacingBetween(1.5);
				XWPFRun turnLinkContentRun = turnLinkContent.createRun();
				turnLinkContentRun.setText(chapaterContent.get(i).getUrlName());
				turnLinkContentRun.setFontSize(TEXTFONTSIZE);
				turnLinkContentRun.setFontFamily(TEXTFONTFAMILY);
				turnLinkContentRun.setBold(true);//加粗
				turnLinkContentRun.setColor("4472c4");//颜色*/

				//回车
				turnLinkContentRun.addCarriageReturn();

			}

	}


	/**
	 * 创建极简报告 头
	 * @param xwpfDocument
	 * @param report
	 * @throws Exception
	 */
	private void createSimplerFirstPage(XWPFDocument xwpfDocument, ReportNew report) throws Exception{
		if("special".equals(report.getReportType()) || "custom".equals(report.getReportType())){
			//第一段：红色，33号字体，居中，楷体GB2312
			createP1(xwpfDocument, report.getReportName());
			//第二段：黑色 12号字 居中 楷体
			//createP2(xwpfDocument,report.getThisIssue(), report.getTotalIssue());
			//第三段：黑色 12号字 居中 楷体
			//createP3(xwpfDocument, report.getPreparationUnits());
			//第二段：
			XWPFParagraph createParagraph3 = xwpfDocument.createParagraph();
			createParagraph3.setAlignment(ParagraphAlignment.CENTER);
			XWPFRun createRun3 = createParagraph3.createRun();
			createRun3.addPicture(getRedLine(),
					Document.PICTURE_TYPE_PNG, "RedLine.png", 5500000, 50000);

			//加个回车
			xwpfDocument.createParagraph().createRun().setText("");

		}
	}
	private void createMaterialPage(XWPFDocument xwpfDocument,ReportNew reportNew) throws Exception{
		//第一段：红色，33号字体，居中，楷体GB2312
		createP1(xwpfDocument, reportNew.getReportName());

		//第二段：黑色 12号字 靠左 楷体
		//createP2(xwpfDocument,report.getThisIssue(), report.getTotalIssue());
		XWPFParagraph untisParagraph = xwpfDocument.createParagraph();
		untisParagraph.setAlignment(ParagraphAlignment.LEFT);
		XWPFRun untisRun = untisParagraph.createRun();
		untisRun.setFontSize(PAGEHEADFONTSIZE);
		untisRun.setText(reportNew.getPreparationUnits()+"				         "+"每日舆情简报（第"+reportNew.getThisIssue()+"期）"+"				      "+reportNew.getStatisticsTime());
		untisRun.setFontFamily(PAGEHEADFONTFAMILY);

		//第五段
		XWPFParagraph pictureParagraph3 = xwpfDocument.createParagraph();
		pictureParagraph3.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun pictureRun3 = pictureParagraph3.createRun();
		pictureRun3.addPicture(getRedLine(),
				Document.PICTURE_TYPE_PNG, "RedLine.png", 5500000, 50000);

		//加个回车
		xwpfDocument.createParagraph().createRun().setText("");


	}
	/**
	 *  报告 素材库资源 加入 舆情速览
	 * @param xdoc
	 * @param chapaterContent
	 */
	private void addMaterialTitle(XWPFDocument xdoc, List<ReportResource> chapaterContent) throws InvalidFormatException, IOException{
//		XWPFNumbering numbering = xdoc.createNumbering();
//		//numbering.
//		XWPFNumbering numbering1 = xdoc.getNumbering();
//		int nextPicNameNumber = xdoc.getNextPicNameNumber(1);
		if (null != chapaterContent && chapaterContent.size()>0){
			XWPFParagraph quickView = xdoc.createParagraph();

			quickView.setAlignment(ParagraphAlignment.LEFT);
			//quickView.setPageBreak(true);
			quickView.setSpacingBetween(1.5);
			//黑体 14号 四号字 加粗
			XWPFRun lastFontRun = quickView.createRun();
			lastFontRun.setText("【舆情速览】");
			lastFontRun.setFontSize(TITLEFONTSIZE);
			lastFontRun.setFontFamily(TITLEFONTFAMILY);
			lastFontRun.setBold(true);

			for (int i = 0; i < chapaterContent.size(); i++) {
				XWPFParagraph turnTitle = xdoc.createParagraph();
				turnTitle.setAlignment(ParagraphAlignment.BOTH);
				turnTitle.setSpacingBetween(1.5);
				//空两格
				//turnTitle.setIndentationFirstLine(600);
				XWPFRun turnTitleRun = turnTitle.createRun();
				//插入图片
				//turnTitleRun.addPicture(getRedPictue(), Document.PICTURE_TYPE_PNG, "material.png", 50, 50);
				//turnTitleRun.addPicture()
				turnTitleRun.setText(StringUtil.cutContent(chapaterContent.get(i).getTitle(),30));
				turnTitleRun.setFontSize(TEXTFONTSIZE);
				turnTitleRun.setFontFamily(TEXTFONTFAMILY);
				//turnTitleRun.setBold(true);
				if (i == chapaterContent.size()-1){
//					//回车
					turnTitleRun.addCarriageReturn();
				}

			}
		}
	}

	/**
	 *  报告 素材库资源 舆情速览 下面的内容
	 * @param xdoc
	 * @param chapaterContent
	 */
	private void addMaterialBody(XWPFDocument xdoc, List<ReportResource> chapaterContent) throws InvalidFormatException, IOException{
		if (null != chapaterContent && chapaterContent.size()>0){

			for (int i = 0; i < chapaterContent.size(); i++) {
				//标题
				XWPFParagraph turnTitle = xdoc.createParagraph();
				turnTitle.setAlignment(ParagraphAlignment.LEFT);
				if (0==i){
					turnTitle.setPageBreak(true);
				}
				//turnTitle.setIndentationFirstLine(600);
				XWPFRun turnTitleRun = turnTitle.createRun();
				turnTitleRun.setText(chapaterContent.get(i).getTitle());
				turnTitleRun.setFontSize(TEXTFONTSIZE);
				turnTitleRun.setFontFamily(TEXTFONTFAMILY);
				turnTitleRun.setBold(true);
				turnTitleRun.addCarriageReturn();

				//发布时间+siteName
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				XWPFParagraph turnTime = xdoc.createParagraph();
				turnTime.setAlignment(ParagraphAlignment.LEFT);
				//turnTime.setIndentationFirstLine(600);

				//1.5倍行间距
				//turnTime.setSpacingBetween(1.5);
				XWPFRun turnTimeRun = turnTime.createRun();
				turnTimeRun.setText("发布时间："+dateFormat.format(chapaterContent.get(i).getUrlDate())+"                  "+"来源："+chapaterContent.get(i).getSiteName());
				turnTimeRun.setFontSize(TEXTFONTSIZE);
				turnTimeRun.setFontFamily(TEXTFONTFAMILY);
				//turnTimeRun.setBold(true);
				turnTimeRun.addCarriageReturn();

				//内容
				if ("微博".equals(chapaterContent.get(i).getGroupName()) ||"国内微信".equals(chapaterContent.get(i).getGroupName()) || "微信".equals(chapaterContent.get(i).getGroupName()) || "国内新闻_电子报".equals(chapaterContent.get(i).getGroupName())){
					String content = StringUtil.cutContent(chapaterContent.get(i).getContent(),150);
					//String[] split = content.split("\\s{1,}");
					//for (int j = 0; j < split.length; j++) {
					XWPFParagraph turnBody = xdoc.createParagraph();
					turnBody.setAlignment(ParagraphAlignment.BOTH);
					//turnBody.setIndentationFirstLine(600);
					//1.5倍行间距
					turnBody.setSpacingBetween(1.5);
					XWPFRun turnBodyRun = turnBody.createRun();
					turnBodyRun.setText("摘要："+content);
					turnBodyRun.setFontSize(TEXTFONTSIZE);
					turnBodyRun.setFontFamily(TEXTFONTFAMILY);
//					if (j == split.length-1){
//					//回车
					turnBodyRun.addCarriageReturn();
//					}
					//}
				}else {//摘要
					XWPFParagraph turnBody = xdoc.createParagraph();
					turnBody.setAlignment(ParagraphAlignment.BOTH);
					//turnBody.setIndentationFirstLine(600);
					//1.5倍行间距
					turnBody.setSpacingBetween(1.5);
					XWPFRun turnBodyRun = turnBody.createRun();
					turnBodyRun.setText("摘要："+chapaterContent.get(i).getNewsAbstract());
					turnBodyRun.setFontSize(TEXTFONTSIZE);
					turnBodyRun.setFontFamily(TEXTFONTFAMILY);
					//if (j == split.length-1){
					//回车
					turnBodyRun.addCarriageReturn();
				}



				//文章地址
				XWPFParagraph turnLink = xdoc.createParagraph();
				turnLink.setAlignment(ParagraphAlignment.LEFT);
				turnLink.setSpacingBetween(1.5);
				//turnLink.setIndentationFirstLine(600);
				XWPFRun turnLinkRun = turnLink.createRun();
				turnLinkRun.setText("原文链接：");
				turnLinkRun.setFontSize(TEXTFONTSIZE);
				turnLinkRun.setFontFamily(TEXTFONTFAMILY);
				//turnLinkRun.setBold(true);

				//放入超链接
				//appendExternalHyperlink(chapaterContent.get(i).getUrlName(),chapaterContent.get(i).getUrlName(),xdoc.createParagraph());

				//加个空行
				XWPFParagraph turnLinkContent = xdoc.createParagraph();
				//1.5倍行间距
				turnLinkContent.setSpacingBetween(1.5);
				XWPFRun turnLinkContentRun = turnLinkContent.createRun();
				turnLinkContentRun.setText(chapaterContent.get(i).getUrlName());
				turnLinkContentRun.setFontSize(TEXTFONTSIZE);
				turnLinkContentRun.setFontFamily(TEXTFONTFAMILY);
				turnLinkContentRun.setBold(true);//加粗
				turnLinkContentRun.setColor("4472c4");//颜色*/

				//回车
				turnLinkContentRun.addCarriageReturn();
			}

		}
	}

	/**
	 * 读取素材报告需要的图片
	 * @return
	 * @throws IOException
	 */
	private InputStream getRedPictue() throws IOException {
		File file = new File(resourceUrl + "material.png");
		if(file.exists()){
			return new FileInputStream(file);
		}else{
			InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("src/main/resources/report/littleTemplate/material.png");
			return resourceAsStream;
		}
	}
	/**
	 *  报告 加入附件
	 * @param xdoc
	 * @param chapaterContent
	 */
	private void addEnclosure(XWPFDocument xdoc, List<ReportResource> chapaterContent){
		if (null != chapaterContent && chapaterContent.size()>0){
			XWPFParagraph lastFont = xdoc.createParagraph();
			lastFont.setAlignment(ParagraphAlignment.LEFT);
			lastFont.setPageBreak(true);
			XWPFRun lastFontRun = lastFont.createRun();
			lastFontRun.setText("附件：");
			lastFontRun.setFontSize(TEXTFONTSIZE);
			lastFontRun.setFontFamily(TEXTFONTFAMILY);
			lastFontRun.setBold(true);

			for (int i = 0; i < chapaterContent.size(); i++) {
				XWPFParagraph turnTitle = xdoc.createParagraph();
				turnTitle.setAlignment(ParagraphAlignment.LEFT);
				//turnTitle.setIndentationFirstLine(600);
				XWPFRun turnTitleRun = turnTitle.createRun();
				turnTitleRun.setText(i+1+". "+chapaterContent.get(i).getTitle());
				turnTitleRun.setFontSize(TEXTFONTSIZE);
				turnTitleRun.setFontFamily(TEXTFONTFAMILY);
				turnTitleRun.setBold(true);

				String content = chapaterContent.get(i).getContent();
				String[] split = content.split("\\s{1,}");
				for (int j = 0; j < split.length; j++) {
					XWPFParagraph turnBody = xdoc.createParagraph();
					turnBody.setAlignment(ParagraphAlignment.BOTH);
					//1.5倍行间距
					turnBody.setSpacingBetween(1.5);
					XWPFRun turnBodyRun = turnBody.createRun();
					turnBodyRun.setText(split[j]);
					turnBodyRun.setFontSize(TEXTFONTSIZE);
					turnBodyRun.setFontFamily(TEXTFONTFAMILY);
					if (j == split.length-1){
						//回车
						turnBodyRun.addCarriageReturn();
					}
				}
			}
		}
	}
	public static void appendExternalHyperlink(String url, String text,
											   XWPFParagraph paragraph) {
		String id = paragraph
				.getDocument()
				.getPackagePart()
				.addExternalRelationship(url,
						XWPFRelation.HYPERLINK.getRelation()).getId();
		CTHyperlink cLink = paragraph.getCTP().addNewHyperlink();
		cLink.setId(id);

		CTText ctText = CTText.Factory.newInstance();
		ctText.setStringValue(text);
		CTR ctr = CTR.Factory.newInstance();
		CTRPr rpr = ctr.addNewRPr();

		//加粗
		CTOnOff ctOnOff = rpr.addNewB();
		ctOnOff.setVal(STOnOff.TRUE);


		//设置超链接样式
		CTColor color = CTColor.Factory.newInstance();
		color.setVal("4472c4");
		rpr.setColor(color);
		rpr.addNewU().setVal(STUnderline.SINGLE);

		//设置字体
		CTFonts fonts = rpr.isSetRFonts() ? rpr.getRFonts() : rpr.addNewRFonts();
		fonts.setAscii(TEXTFONTFAMILY);
		fonts.setEastAsia(TEXTFONTFAMILY);
		fonts.setHAnsi(TEXTFONTFAMILY);

		//设置字体大小
		CTHpsMeasure sz = rpr.isSetSz() ? rpr.getSz() : rpr.addNewSz();
		sz.setVal(new BigInteger("24"));

		ctr.setTArray(new CTText[] { ctText });
		cLink.setRArray(new CTR[] { ctr });

		//设置段落居中
		paragraph.setAlignment(ParagraphAlignment.LEFT);
		paragraph.setVerticalAlignment(TextAlignment.CENTER);

	}

	public static void main(String[] args) throws Exception{
//		XWPFDocument xwpfDocument = new XWPFDocument();
//		try {
//			testStyle(xwpfDocument);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

//	public static void testStyle(XWPFDocument document){
//		try {
//			//	document.addPictureData(getTestRedPictue(),1);
//			XWPFParagraph paragraph = document.createParagraph();
//			//paragraph.setIndentationFirstLine(600);
//			//XWPFRun run = paragraph.createRun();
//			//run.addPicture(getTestRedPictue(),Document.PICTURE_TYPE_PNG,"material.png",55000,55000);
//
//			for (int i = 0; i <10; i++) {
//				//回车
//				XWPFRun run = paragraph.createRun();
//				run.addPicture(getTestRedPictue(),Document.PICTURE_TYPE_PNG,"material.png",55000,55000);
//				run.addTab();
//				run.setText("能否同一行换行");
//				run.addCarriageReturn();
//
//				//run.addBreak();
//				//加个回车
//				//document.createParagraph().createRun().setText("");
//
//			}
//
//
//
//		} catch (InvalidFormatException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		String dir = "D:/IdeaProjects/netInsight/report/simplerReport" + "/" + "ceshireport"+ "/" + "yangyanyan" + "/" + "material" ;
//		File file = new File(dir);
//		if(!file.isDirectory()){
//			file.mkdirs();
//		}
//		String reportPath = dir + "/" + "项目符号测试" + "_"
//				+ System.currentTimeMillis() + ".docx";
//		try {
//			document.write(new FileOutputStream(reportPath));
//			document.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.err.println("素材报告生成完毕，reportPath:" + reportPath);
//	}
//
//	private static InputStream getTestRedPictue() throws IOException {
//		File file = new File("D:/picture/material.png");
//
//		return new FileInputStream(file);
//	}

}
