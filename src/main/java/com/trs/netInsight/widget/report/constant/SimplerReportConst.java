package com.trs.netInsight.widget.report.constant;

import com.trs.netInsight.widget.report.entity.ReportResource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/10/18.
 * @desc 舆情报告 极简模式 常量类
 */
public class SimplerReportConst {

    public static final String REPORTINTROkey = "REPORTINTRO"; // 报告简介
    public static final String OVERVIEWOFDATAkey = "OVERVIEWOFDATA"; // 数据统计概述
    public static final String NEWSTOP10key = "NEWSTOP10"; // 网站数据TOP10
    public static final String WEIBOTOP10key = "WEIBOTOP10"; // 微博TOP10
    public static final String WECHATTOP10key = "WECHATTOP10"; // 微信TOP10
    public static final String DATATRENDANALYSISkey = "DATATRENDANALYSIS"; // 数据趋势分析
    public static final String DATASOURCEANALYSISkey = "DATASOURCEANALYSIS"; // 数据来源对比
    public static final String WEBSITESOURCETOP10key = "WEBSITESOURCETOP10"; // 网站来源TOP10
    public static final String WEIBOACTIVETOP10key = "WEIBOACTIVETOP10"; // 微博活跃用户TOP10
    public static final String WECHATACTIVETOP10key = "WECHATACTIVETOP10"; // 微信活跃用户TOP10
    public static final String AREAkey = "AREA"; // 全国地域分布
    public static final String EMOTIONANALYSISkey = "EMOTIONANALYSIS"; // 情感分析
    public static final String NEWSHOTTOPICSkey = "NEWSHOTTOPICS"; // 热点新闻
    public static final String WEIBOHOTTOPICSkey = "WEIBOHOTTOPICS"; // 热点微博
    public static final String CUSTOMMODULEkey = "WEIBOHOTTOPICS"; // 自定义模块


    public static final String REPORTINTRO = "报告简介"; // 报告简介
    public static final String OVERVIEWOFDATA = "数据统计"; // 数据统计概述
    public static final String NEWSTOP10 = "新闻数据TOP10"; // 新闻网站TOP10
    public static final String WEIBOTOP10 = "微博TOP10"; // 微博TOP10
    public static final String WECHATTOP10 = "微信TOP10"; // 微信TOP10
    public static final String DATATRENDANALYSIS = "数据趋势分析"; // 数据趋势分析
    public static final String DATASOURCEANALYSIS = "数据来源对比"; // 数据来源对比
    public static final String WEBSITESOURCETOP10 = "网站来源TOP10"; // 网站来源TOP10
    public static final String WEIBOACTIVETOP10 = "微博活跃用户TOP10"; // 微博活跃用户TOP10
    public static final String WECHATACTIVETOP10 = "微信活跃用户TOP10"; // 微信活跃用户TOP10
    public static final String AREA = "全国地域分布"; // 全国地域分布
    public static final String EMOTIONANALYSIS = "情感分析"; // 情感分析
    public static final String NEWSHOTTOPICS = "热点新闻"; // 热点新闻
    public static final String WEIBOHOTTOPICS = "热点微博"; // 热点微博
    public static final String CUSTOMMODULE = "自定义模块"; // 自定义模块



    public static final ArrayList<String> SIMCHAPTERS;

    static {
        SIMCHAPTERS = new ArrayList<>();
        SIMCHAPTERS.add("REPORTINTRO");   //报告简介
        SIMCHAPTERS.add("OVERVIEWOFDATA"); //数据概述
        SIMCHAPTERS.add("WEBSITETOP10");     //网站数据TOP10
        SIMCHAPTERS.add("WEIBOTOP10");    //微博TOP10
        SIMCHAPTERS.add("WECHATTOP10");   //微信TOP10
        SIMCHAPTERS.add("DATATRENDANALYSIS");//数据趋势分析
        SIMCHAPTERS.add("DATASOURCEANALYSIS");//数据来源对比
        SIMCHAPTERS.add("WEBSITESOURCETOP10");//网站来源TOP10
        SIMCHAPTERS.add("WEIBOACTIVETOP10");//微博活跃用户TOP10
        SIMCHAPTERS.add("WECHATACTIVETOP10");//微信活跃公众号TOP10
        SIMCHAPTERS.add("AREA");              //全国地域分布
        SIMCHAPTERS.add("EMOTIONANALYSIS");    //情感分析
        SIMCHAPTERS.add("NEWSHOTTOPICS");     //热点新闻
        SIMCHAPTERS.add("WEIBOHOTTOPICS");   //热点微博
        SIMCHAPTERS.add("CUSTOMMODULE");          //自定义模块
    }


}
