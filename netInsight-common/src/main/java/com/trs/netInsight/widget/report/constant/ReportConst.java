package com.trs.netInsight.widget.report.constant;

import java.util.ArrayList;
import java.util.HashMap;

/***
 *  Created by shao.guangze on 2018/6/29
 */
public class ReportConst {

    public static final String FAIL = "fail";

    public static final String SEMICOLON = ";";

    public static final String REPORTINTRO = "报告简介"; // 报告简介
    public static final String OVERVIEWOFDATA = "数据统计概述"; // 数据统计概述
    public static final String NEWSTOP10 = "新闻网站TOP10"; // 新闻网站TOP10
    public static final String WEIBOTOP10 = "微博TOP10"; // 微博TOP10
    public static final String WECHATTOP10 = "微信TOP10"; // 微信TOP10

    //活跃账号 新 20200609
    public static final String ACTIVEACCOUNT = "活跃账号";


    public static final String DATATRENDANALYSIS = "各舆论场趋势分析"; // 数据趋势分析
    public static final String OPININOANALYSIS = "观点分析"; // 观点分析
    public static final String DATASOURCEANALYSIS = "各舆论场发布统计"; // 各舆论场发布统计
    public static final String WEBSITESOURCETOP10 = "网站来源TOP10"; // 网站来源TOP10
    public static final String WEIBOACTIVETOP10 = "活跃账号"; // 微博活跃用户TOP10
    public static final String WECHATACTIVETOP10 = "微信活跃用户TOP10"; // 微信活跃用户TOP10
    public static final String AREA = "地域统计"; // 全国地域分布
    public static final String EMOTIONANALYSIS = "正负面占比"; // 正负面占比
    public static final String MOODSTATISTICS = "情绪统计";
    public static final String NEWSHOTTOPICS = "新闻热点话题"; // 新闻热点话题
    public static final String WEIBOHOTTOPICS = "微博热点话题排行"; // 微博热点话题
    public static final String WORDCLOUDSTATISTICS = "热点词云";

    //新网察
    public static final String SITUATIONACCESSMENT = "态势评估";
    //专题报 改造
    public static final String NEWSHOTTOP10 = "新闻热点"; // 新闻热点TOP10
    public static final String WEIBOHOTTOP10 = "微博热点"; // 微博热点TOP10
    public static final String WECHATHOTTOP10 = "微信热点"; // 微信热点TOP10
    public static final String WEMEDIA = "自媒体号热点"; // 自媒体号

    //事件脉络
    public static final String WECHATEVENTCONTEXT = "微信事件脉络";
    public static final String WEIBOEVENTCONTEXT = "微博事件脉络";
    public static final String NEWSEVENTCONTEXT = "新闻网站事件脉络";
    public static final String WEMEDIAEVENTCONTEXT = "自媒体号事件脉络";

    //传播分析
    public static final String PROPAFATIONANALYSIS = "传播分析";
    public static final String NEWSPROPAFATIONANALYSISTIMELIST = "新闻传播分析时间轴";
    public static final String WEMEDIAPROPAFATIONANALYSISTIMELIST = "自媒体传播分析时间轴";


    public static final String REPORTINTRONew = "REPORTINTRO"; // 报告简介
    public static final String OVERVIEWOFDATANew = "OVERVIEWOFDATA"; // 数据统计概述
    public static final String NEWSTOP10New = "NEWSTOP10"; // 新闻网站TOP10
    public static final String WEIBOTOP10New = "WEIBOTOP10"; // 微博TOP10
    public static final String WECHATTOP10New = "WECHATTOP10"; // 微信TOP10
    public static final String DATATRENDANALYSISNew = "DATATRENDANALYSIS"; // 数据趋势分析
    public static final String DATASOURCEANALYSISNew = "DATASOURCEANALYSIS"; // 数据来源对比
    public static final String WEBSITESOURCETOP10New = "WEBSITESOURCETOP10"; // 网站来源TOP10
    public static final String WEIBOACTIVETOP10New = "WEIBOACTIVETOP10"; // 微博活跃用户TOP10
    public static final String WECHATACTIVETOP10New = "WECHATACTIVETOP10"; // 微信活跃用户TOP10
    public static final String AREANew = "AREA"; // 全国地域分布
    public static final String EMOTIONANALYSISNew = "EMOTIONANALYSIS"; // 情感分析
    public static final String NEWSHOTTOPICSNew = "NEWSHOTTOPICS"; // 新闻热点话题
    public static final String WEIBOHOTTOPICSNew = "WEIBOHOTTOPICS"; // 微博热点话题
    //专题报 改造 20191121
    public static final String NEWSHOTTOP10New = "NEWSHOTTOP10"; // 新闻热点TOP10
    public static final String WEIBOHOTTOP10New = "WEIBOHOTTOP10"; // 微博热点TOP10
    public static final String WECHATHOTTOP10New = "WECHATHOTTOP10"; // 微信热点TOP10
    public static final String STATISTICSTIMEFORMAT = "yyyy年MM月dd日HH时";

    public static final String DAILYREPORT = "日报";
    public static final String WEEKLYREPORT = "周报";
    public static final String MONTHLYREPORT = "月报";
    public static final String SPECIALREPORT = "专报";
    public static final String INDEXTABREPORT = "日常监测报";

    public static final String DEFAULTDAILYTEMPLATE = "日报默认模版";
    public static final String DEFAULTWEEKLYTEMPLATE = "周报默认模版";
    public static final String DEFAULTMONTHLYTEMPLATE = "月报默认模版";
    public static final String DEFAULTSPECIALTEMPLATE= "专报默认模版";
    public static final String DEFAULTINDEXTABTEMPLATE= "日常监测报默认模版";

    public static final String REPORTINTROkey = "REPORTINTRO"; // 报告简介
    public static final String OVERVIEWOFDATAkey = "OVERVIEWOFDATA"; // 数据统计概述

    public static final String SITUATIONACCESSMENTkey = "SITUATIONACCESSMENT";
    public static final String DATATRENDANALYSISkey = "DATATRENDANALYSIS"; // 数据趋势分析
    public static final String DATASOURCEANALYSISkey = "DATASOURCEANALYSIS"; // 数据来源对比
    //事件脉络
    public static final String WECHATEVENTCONTEXTkey = "WECHATEVENTCONTEXT";
    public static final String WEIBOEVENTCONTEXTkey = "WEIBOEVENTCONTEXT";
    public static final String NEWSEVENTCONTEXTkey = "NEWSEVENTCONTEXT";
    public static final String WEMEDIAEVENTCONTEXTkey = "WEMEDIAEVENTCONTEXT";

    //专题分析新增观点分析
    public static final String OPINIONANALYSISkey = "OPINIONANALYSIS";
    public static final String WEBSITESOURCETOP10key = "WEBSITESOURCETOP10"; // 网站来源TOP10
    public static final String WEIBOACTIVETOP10key = "WEIBOACTIVETOP10"; // 微博活跃用户TOP10
    public static final String WECHATACTIVETOP10key = "WECHATACTIVETOP10"; // 微信活跃用户TOP10
    public static final String AREAkey = "AREA"; // 全国地域分布
    public static final String MOODSTATISTICSkey = "MOODSTATISTICS"; // 情绪统计
    public static final String WORDCLOUDSTATISTICSkey = "WORDCLOUDSTATISTICS"; // 词云统计

    public static final String EMOTIONANALYSISkey = "EMOTIONANALYSIS"; // 情感分析
    public static final String NEWSHOTTOPICSkey = "NEWSHOTTOPICS"; // 新闻热点话题
    public static final String WEIBOHOTTOPICSkey = "WEIBOHOTTOPICS"; // 微博热点话题

    //传播分析
    public static final String PROPAFATIONANALYSISkey = "PROPAFATIONANALYSIS";
    //传播分析 时间轴，新闻和自媒体
    public static final String NEWSPROPAFATIONANALYSISTIMELISTkey = "NEWSPROPAFATIONANALYSISTIMELIST";
    public static final String WEMEDIAPROPAFATIONANALYSISTIMELISTkey = "WEMEDIAPROPAFATIONANALYSISTIMELIST";
    //专题报 改造 20191121
    public static final String NEWSHOTTOP10key = "NEWSHOTTOP10"; // 新闻热点TOP10
    public static final String WEIBOHOTTOP10key = "WEIBOHOTTOP10"; // 微博热点TOP10
    public static final String WECHATHOTTOP10key = "WECHATHOTTOP10"; // 微信热点TOP10
    public static final String WEMEDIAkey = "WEMEDIAHOT"; // 自媒体号热点

    public static final String ACTIVEACCOUNTkey = "ACTIVEACCOUNT";

    public static final String GENERATEREPORTLOG = "舆情报告生成word报告：%s";
    public static final String REPORTRESOURCELOG = "舆情报告添加报告资源：%s";
    public static final String SPECILAREPORTLOG = "舆情报告专题报告计算数据：%s";
    public static final String INDEXTABREPORTLOG = "舆情报告日常监测报告计算数据：%s";
    public static final String SPECIALREPORTTIMELOG = "耗时：%d";
    public static final String DONE = "生成完毕";

    public static final Integer REPORTLISTDATASIZE = 10;
    public static final Integer REPORTCHARTDATASIZE = 10;

    public static final String ALREADYEXIST = "already-exist";

    public static final String SINGLERESOURCE = "SingleResource";
    public static final String LISTRESOURCES = "ListResources";
    public static final String CHART = "chart";

    public static final String DBCREATEDTIMEPATTERN = "yyyy-MM-dd HH:mm:ss";//the pattern of createdTime in DB
    public static final Integer DailyReportExpiration = -31;    //1个月
    public static final Integer WeeklyReportExpiration = -31;   //1个月
    public static final Integer MonthlyReportExpiration = -182; //半年


    //此标志为准备生成的报告
    public static final String REPORTIDFLAG = "000000";

    public static final HashMap<Integer,String> ROMAN2CHINESE;

    public static final ArrayList<String> CHAPTERS;
    //专题分析返回数据顺序问题
    public static final ArrayList<String> CHAPTERS4SPECIAL;

    public static final HashMap<String,String> CHAPTERS2METHOD;
    public static final HashMap<String,String> CHAPTERS2METHODNEW;

    public static final HashMap<String, String> CHAPTERS2METHODSET;
    public static final HashMap<String, String> CHAPTERS2METHODSETNEW;

    public static final HashMap<String,HashMap> chapterInfoForIndexTab = new HashMap<>() ;
    static{


        HashMap<String,String> item0 = new HashMap<>();
        item0.put("key",OVERVIEWOFDATAkey);
        item0.put("name",OVERVIEWOFDATA);
        item0.put("mapContrast","contrastGroup");
        item0.put("entityType","keywords");
        item0.put("chapterType","SingleResource");
        //当前模块的数据可以来自 统计分析中的哪一种图
        item0.put("StatisticalChartInfo","CHART_PIE");
        chapterInfoForIndexTab.put("数据统计概述",item0);

        HashMap<String,String> item1 = new HashMap<>();
        item1.put("key",DATATRENDANALYSISkey);
        item1.put("name",DATATRENDANALYSIS);
        item1.put("mapContrast","contrastGroup");
        item1.put("showType","day;hour");
        item1.put("entityType","keywords");
        item1.put("chapterType","chart");
        //当前模块的数据可以来自 统计分析中的哪一种图
        item1.put("StatisticalChartInfo","CHART_LINE");
        chapterInfoForIndexTab.put("各舆论场趋势分析",item1);

        HashMap<String,String> item2 = new HashMap<>();
        item2.put("key",DATASOURCEANALYSISkey);
        item2.put("name",DATASOURCEANALYSIS);
        item2.put("mapContrast","contrastGroup");
        item2.put("entityType","keywords");
        item2.put("chapterType","chart");
        //当前模块的数据可以来自 统计分析中的哪一种图
        item2.put("StatisticalChartInfo","CHART_PIE");
        chapterInfoForIndexTab.put("媒体来源占比",item2);

        HashMap<String,String> item3 = new HashMap<>();
        item3.put("key",EMOTIONANALYSISkey);
        item3.put("name",EMOTIONANALYSIS);
        item3.put("mapContrast","contrastEmotion");
        item3.put("entityType","keywords");
        item3.put("chapterType","chart");
        //当前模块的数据可以来自 统计分析中的哪一种图
        item3.put("StatisticalChartInfo","CHART_PIE_EMOTION");
        chapterInfoForIndexTab.put("正负面占比",item3);

        HashMap<String,String> item4 = new HashMap<>();
        item4.put("key",ACTIVEACCOUNTkey);
        item4.put("name",ACTIVEACCOUNT);
        item4.put("mapContrast","contrastSite");
        item4.put("entityType","keywords");
        item4.put("chapterType","chart");
        //当前模块的数据可以来自 统计分析中的哪一种图
        item4.put("StatisticalChartInfo","CHART_BAR_CROSS");
        chapterInfoForIndexTab.put("活跃帐号",item4);

        HashMap<String,String> item5 = new HashMap<>();
        item5.put("key",WEIBOHOTTOPICSkey);
        item5.put("name",WEIBOHOTTOPICS);
        item5.put("mapContrast","contrastTopic");
        item5.put("entityType","keywords");
        item5.put("chapterType","chart");
        //当前模块的数据可以来自 统计分析中的哪一种图
        item5.put("StatisticalChartInfo","HOT_TOPIC_SORT");
        chapterInfoForIndexTab.put("微博热点话题排行",item5);

        HashMap<String,String> item6 = new HashMap<>();
        item6.put("key",WORDCLOUDSTATISTICSkey);
        item6.put("name",WORDCLOUDSTATISTICS);
        item6.put("mapContrast","hitArticle");
        item6.put("entityType","keywords;people;location;agency");
        item6.put("chapterType","chart");
        //当前模块的数据可以来自 统计分析中的哪一种图
        item6.put("StatisticalChartInfo","WORD_CLOUD");
        chapterInfoForIndexTab.put("词云统计",item6);

        HashMap<String,String> item7 = new HashMap<>();
        item7.put("key",AREAkey);
        item7.put("name",AREA);
        item7.put("mapContrast","hitArticle;mediaArea");
        item7.put("keyStr","catalogArea;mediaArea");
        item7.put("entityType","keywords");
        item7.put("chapterType","chart");
        //当前模块的数据可以来自 统计分析中的哪一种图
        item7.put("StatisticalChartInfo","MAP");
        chapterInfoForIndexTab.put("地域统计",item7);
        // 上面为日常监测的模板

        ROMAN2CHINESE = new HashMap<>();
        ROMAN2CHINESE.put(1, "一");
        ROMAN2CHINESE.put(2, "二");
        ROMAN2CHINESE.put(3, "三");
        ROMAN2CHINESE.put(4, "四");
        ROMAN2CHINESE.put(5, "五");
        ROMAN2CHINESE.put(6, "六");
        ROMAN2CHINESE.put(7, "七");
        ROMAN2CHINESE.put(8, "八");
        ROMAN2CHINESE.put(9, "九");
        ROMAN2CHINESE.put(10, "十");
        ROMAN2CHINESE.put(11, "十一");
        ROMAN2CHINESE.put(12, "十二");
        ROMAN2CHINESE.put(13, "十三");
        ROMAN2CHINESE.put(14, "十四");
        ROMAN2CHINESE.put(15, "十五");
        ROMAN2CHINESE.put(16, "十六");
        //舆情报告新版
        CHAPTERS = new ArrayList<>();
        CHAPTERS.add("REPORTINTRO");
        CHAPTERS.add("OVERVIEWOFDATA");
        CHAPTERS.add("SITUATIONACCESSMENT");
        CHAPTERS.add("DATATRENDANALYSIS");
        CHAPTERS.add("DATASOURCEANALYSIS");
        CHAPTERS.add(OPINIONANALYSISkey);
        CHAPTERS.add("EMOTIONANALYSIS");
        CHAPTERS.add(MOODSTATISTICSkey);
        CHAPTERS.add(WORDCLOUDSTATISTICSkey);
        CHAPTERS.add("AREA");
        //专题报 改造 20191121
        CHAPTERS.add("NEWSHOTTOP10");
        CHAPTERS.add("WEIBOHOTTOP10");
        CHAPTERS.add("WECHATHOTTOP10");
        CHAPTERS.add(WEMEDIAkey);

        // 事件脉络
        CHAPTERS.add(WEMEDIAEVENTCONTEXTkey);
        CHAPTERS.add(WECHATEVENTCONTEXTkey);
        CHAPTERS.add(WEIBOEVENTCONTEXTkey);
        CHAPTERS.add(NEWSEVENTCONTEXTkey);

        //活跃账号
        CHAPTERS.add(ACTIVEACCOUNTkey);

        CHAPTERS.add("WEBSITESOURCETOP10");
        CHAPTERS.add("WEIBOACTIVETOP10");
        CHAPTERS.add("WECHATACTIVETOP10");

        CHAPTERS.add("NEWSHOTTOPICS");
        CHAPTERS.add("WEIBOHOTTOPICS");

        //传播分析
        CHAPTERS.add(PROPAFATIONANALYSISkey);
        CHAPTERS.add(NEWSPROPAFATIONANALYSISTIMELISTkey);
        CHAPTERS.add(WEMEDIAPROPAFATIONANALYSISTIMELISTkey);


        CHAPTERS4SPECIAL = new ArrayList<>();
        CHAPTERS4SPECIAL.add("REPORTINTRO");
        CHAPTERS4SPECIAL.add("OVERVIEWOFDATA");
        CHAPTERS4SPECIAL.add("SITUATIONACCESSMENT");
        CHAPTERS4SPECIAL.add("DATATRENDANALYSIS");
        CHAPTERS4SPECIAL.add("DATASOURCEANALYSIS");
        CHAPTERS4SPECIAL.add(OPINIONANALYSISkey);
        CHAPTERS4SPECIAL.add("EMOTIONANALYSIS");
        CHAPTERS4SPECIAL.add(MOODSTATISTICSkey);
        CHAPTERS4SPECIAL.add(WORDCLOUDSTATISTICSkey);
        CHAPTERS4SPECIAL.add("AREA");
        //专题报 改造 20191121
        CHAPTERS4SPECIAL.add("NEWSHOTTOP10");
        CHAPTERS4SPECIAL.add("WEIBOHOTTOP10");
        CHAPTERS4SPECIAL.add("WECHATHOTTOP10");
        CHAPTERS4SPECIAL.add(WEMEDIAkey);
        // 事件脉络
        CHAPTERS4SPECIAL.add(WEMEDIAEVENTCONTEXTkey);
        CHAPTERS4SPECIAL.add(WECHATEVENTCONTEXTkey);
        CHAPTERS4SPECIAL.add(WEIBOEVENTCONTEXTkey);
        CHAPTERS4SPECIAL.add(NEWSEVENTCONTEXTkey);
        //活跃账号
        CHAPTERS4SPECIAL.add(ACTIVEACCOUNTkey);

        CHAPTERS2METHODSET = new HashMap<>();
        CHAPTERS2METHODSET.put("报告简介", "setReportIntro");
        CHAPTERS2METHODSET.put("数据统计概述", "setOverviewOfdata");
        CHAPTERS2METHODSET.put("新闻网站TOP10", "setNewsTop10");
        CHAPTERS2METHODSET.put("微博TOP10", "setWeiboTop10");
        CHAPTERS2METHODSET.put("微信TOP10", "setWechatTop10");
        CHAPTERS2METHODSET.put("数据趋势分析", "setDataTrendAnalysis");
        CHAPTERS2METHODSET.put("数据来源对比", "setDataSourceAnalysis");
        CHAPTERS2METHODSET.put("网站来源TOP10", "setWebsiteSourceTop10");
        CHAPTERS2METHODSET.put("微博活跃用户TOP10", "setWeiboActiveTop10");
        CHAPTERS2METHODSET.put("微信活跃用户TOP10", "setWechatActiveTop10");
        CHAPTERS2METHODSET.put("全国地域分布", "setArea");
        CHAPTERS2METHODSET.put("情感分析", "setEmotionAnalysis");
        CHAPTERS2METHODSET.put("新闻热点话题", "setNewsHotTopics");
        CHAPTERS2METHODSET.put("微博热点话题", "setWeiboHotTopics");

        CHAPTERS2METHOD = new HashMap<>();
        CHAPTERS2METHOD.put("报告简介", "getReportIntro");
        CHAPTERS2METHOD.put("数据统计概述", "getOverviewOfdata");
        CHAPTERS2METHOD.put("新闻网站TOP10", "getNewsTop10");
        CHAPTERS2METHOD.put("微博TOP10", "getWeiboTop10");
        CHAPTERS2METHOD.put("微信TOP10", "getWechatTop10");
        CHAPTERS2METHOD.put("数据趋势分析", "getDataTrendAnalysis");
        CHAPTERS2METHOD.put("数据来源对比", "getDataSourceAnalysis");
        CHAPTERS2METHOD.put("网站来源TOP10", "getWebsiteSourceTop10");
        CHAPTERS2METHOD.put("微博活跃用户TOP10", "getWeiboActiveTop10");
        CHAPTERS2METHOD.put("微信活跃用户TOP10", "getWechatActiveTop10");
        CHAPTERS2METHOD.put("全国地域分布", "getArea");
        CHAPTERS2METHOD.put("情感分析", "getEmotionAnalysis");
        CHAPTERS2METHOD.put("新闻热点话题", "getNewsHotTopics");
        CHAPTERS2METHOD.put("微博热点话题", "getWeiboHotTopics");

        CHAPTERS2METHODSETNEW = new HashMap<>();
        CHAPTERS2METHODSETNEW.put("REPORTINTRO", "setReportIntro");
        CHAPTERS2METHODSETNEW.put("OVERVIEWOFDATA", "setOverviewOfdata");
        CHAPTERS2METHODSETNEW.put("NEWSTOP10", "setNewsTop10");
        CHAPTERS2METHODSETNEW.put("WEIBOTOP10", "setWeiboTop10");
        CHAPTERS2METHODSETNEW.put("WECHATTOP10", "setWechatTop10");
        CHAPTERS2METHODSETNEW.put("DATATRENDANALYSIS", "setDataTrendAnalysis");
        CHAPTERS2METHODSETNEW.put("DATASOURCEANALYSIS", "setDataSourceAnalysis");
        CHAPTERS2METHODSETNEW.put("WEBSITESOURCETOP10", "setWebsiteSourceTop10");
        CHAPTERS2METHODSETNEW.put("WEIBOACTIVETOP10", "setWeiboActiveTop10");
        CHAPTERS2METHODSETNEW.put("WECHATACTIVETOP10", "setWechatActiveTop10");
        CHAPTERS2METHODSETNEW.put("AREA", "setArea");
        CHAPTERS2METHODSETNEW.put("EMOTIONANALYSIS", "setEmotionAnalysis");
        CHAPTERS2METHODSETNEW.put("NEWSHOTTOPICS", "setNewsHotTopics");
        CHAPTERS2METHODSETNEW.put("WEIBOHOTTOPICS", "setWeiboHotTopics");
        CHAPTERS2METHODSETNEW.put(SITUATIONACCESSMENTkey, "setSituationAccessment");
        CHAPTERS2METHODSETNEW.put(OPINIONANALYSISkey, "setOpinionAnalysis");

        CHAPTERS2METHODSETNEW.put(MOODSTATISTICSkey, "setMoodStatistics");
        CHAPTERS2METHODSETNEW.put(WORDCLOUDSTATISTICSkey, "setWordCloudStatistics");
        CHAPTERS2METHODSETNEW.put(WEMEDIAkey, "setWeMediaHot");
        CHAPTERS2METHODSETNEW.put(ACTIVEACCOUNTkey, "setActiveAccount");
        CHAPTERS2METHODSETNEW.put(WEIBOEVENTCONTEXTkey, "setWeiboEventContext");
        CHAPTERS2METHODSETNEW.put(WEMEDIAEVENTCONTEXTkey, "setWemediaEventContext");
        CHAPTERS2METHODSETNEW.put(WECHATEVENTCONTEXTkey, "setWechatEventContext");
        CHAPTERS2METHODSETNEW.put(NEWSEVENTCONTEXTkey, "setNewsEventContext");

        CHAPTERS2METHODSETNEW.put("WECHATHOTTOP10", "setWechatHotTop10");
        //配合历史数据 （专题报  改造 20191121）
        CHAPTERS2METHODSETNEW.put("NEWSHOTTOP10", "setNewsHotTopics");
        CHAPTERS2METHODSETNEW.put("WEIBOHOTTOP10", "setWeiboHotTopics");
        CHAPTERS2METHODSETNEW.put(PROPAFATIONANALYSISkey, "setSpreadAnalysisSiteName");
        CHAPTERS2METHODSETNEW.put(NEWSPROPAFATIONANALYSISTIMELISTkey, "setNewsSpreadAnalysisTimeList");
        CHAPTERS2METHODSETNEW.put(WEMEDIAPROPAFATIONANALYSISTIMELISTkey, "setWemediaSpreadAnalysisTimeList");

        CHAPTERS2METHODNEW = new HashMap<>();
        CHAPTERS2METHODNEW.put("REPORTINTRO", "getReportIntro");

        CHAPTERS2METHODNEW.put("OVERVIEWOFDATA", "getOverviewOfdata");
        CHAPTERS2METHODNEW.put("NEWSTOP10", "getNewsTop10");
        CHAPTERS2METHODNEW.put("WEIBOTOP10", "getWeiboTop10");
        CHAPTERS2METHODNEW.put("WECHATTOP10", "getWechatTop10");
        CHAPTERS2METHODNEW.put("DATATRENDANALYSIS", "getDataTrendAnalysis");
        CHAPTERS2METHODNEW.put("DATASOURCEANALYSIS", "getDataSourceAnalysis");
        CHAPTERS2METHODNEW.put("WEBSITESOURCETOP10", "getWebsiteSourceTop10");
        CHAPTERS2METHODNEW.put("WEIBOACTIVETOP10", "getWeiboActiveTop10");
        CHAPTERS2METHODNEW.put("WECHATACTIVETOP10", "getWechatActiveTop10");
        CHAPTERS2METHODNEW.put("AREA", "getArea");
        CHAPTERS2METHODNEW.put("EMOTIONANALYSIS", "getEmotionAnalysis");
        CHAPTERS2METHODNEW.put("NEWSHOTTOPICS", "getNewsHotTopics");
        CHAPTERS2METHODNEW.put("WEIBOHOTTOPICS", "getWeiboHotTopics");

        CHAPTERS2METHODNEW.put("WECHATHOTTOP10", "getWechatHotTop10");
        //配合历史数据  （专题报  改造 20191121）
        CHAPTERS2METHODNEW.put("NEWSHOTTOP10", "getNewsHotTopics");
        CHAPTERS2METHODNEW.put("WEIBOHOTTOP10", "getWeiboHotTopics");

        CHAPTERS2METHODNEW.put(SITUATIONACCESSMENTkey, "getSituationAccessment");
        CHAPTERS2METHODNEW.put(OPINIONANALYSISkey, "getOpinionAnalysis");
        CHAPTERS2METHODNEW.put(MOODSTATISTICSkey, "getMoodStatistics");
        CHAPTERS2METHODNEW.put(WEMEDIAkey, "getWeMediaHot");
        CHAPTERS2METHODNEW.put(WEIBOEVENTCONTEXTkey, "getWeiboEventContext");
        CHAPTERS2METHODNEW.put(WECHATEVENTCONTEXTkey, "getWechatEventContext");
        CHAPTERS2METHODNEW.put(WEMEDIAEVENTCONTEXTkey, "getWemediaEventContext");
        CHAPTERS2METHODNEW.put(NEWSEVENTCONTEXTkey, "getNewsEventContext");
        CHAPTERS2METHODNEW.put(ACTIVEACCOUNTkey, "getActiveAccount");
        CHAPTERS2METHODNEW.put(WORDCLOUDSTATISTICSkey, "getWordCloudStatistics");
        CHAPTERS2METHODNEW.put(PROPAFATIONANALYSISkey, "getSpreadAnalysisSiteName");
        CHAPTERS2METHODNEW.put(NEWSPROPAFATIONANALYSISTIMELISTkey, "getNewsSpreadAnalysisTimeList");
        CHAPTERS2METHODNEW.put(WEMEDIAPROPAFATIONANALYSISTIMELISTkey, "getWemediaSpreadAnalysisTimeList");
    }

    //handmade

    //手动报告的模板
    public static final String Hand_Made_Report_Synopsis = "报告简介";
    public static final String Hand_Made_Statistics_Summarize = "数据统计概述";
    public static final String Hand_Made_Source_contrast = "数据来源对比";
    public static final String Hand_Made_Data_Statistics = "数据统计";
    public static final String Hand_Made_Area = "全国地域分布";
    public static final String Hand_Made_Emotion_Analyze = "情感分析";
    public static final String Hand_Made_Hot_News = "热点新闻";
    public static final String Hand_Made_TOP10_News = "网站来源TOP10";
    public static final String Hand_Made_Hot_Weibo = "热点微博";
    public static final String Hand_Made_TOP10_Weibo = "微博活跃用户TOP10";
    public static final String Hand_Made_TOP10_Wechat = "微信活跃用户TOP10";
    public static final String Hand_Made_TOP10_User = "活跃用户TOP10";
    public static final String Hand_Made_Hot_News_List = "热点信息列表";
    public static final String Hand_Made_Word_Cloud = "词云统计";


}
