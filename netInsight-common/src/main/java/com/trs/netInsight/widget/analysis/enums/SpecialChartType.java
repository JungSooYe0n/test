package com.trs.netInsight.widget.analysis.enums;

import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.config.constant.Const;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 专题分析图表枚举类
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2020/1/7 17:41.
 * @desc
 */
@Getter
@AllArgsConstructor
public enum SpecialChartType {

    WEBCOUNT	  ("webCount", 			"来源类型统计", 				   "ALL"),

    AREA		  ("area", 				"地域分布", 				   Const.HYBASE_NI_INDEX),

    ACTIVE_LEVEL  ("activeLevel", 		"媒体活跃等级", 			   "ALL"),

    EMOTIONOPTION	  ("emotionOption", 		"情感分析", 			   "ALL"),

    NETTENDENCY      ("netTendency", 	    	"网民参与趋势分布", 	   "ALL"),

    METATENDENCY      ("metaTendency", 	    	"媒体参与趋势分布", 	   "ALL"),

    TRENDMESSAGE  ("trendMessage", 	    "信息走势图", 		       "ALL"),

    NEWSSITEANALYSIS("newsSiteAnalysis", 	"新闻传播分析", 			    Const.HYBASE_NI_INDEX),

    WORDCLOUD	  ("wordCloud", 		"词云", 					   "ALL"),

    HOTWORDEXPLORE	 ("hotWordExplore", 		"热词探索", 			Const.HYBASE_NI_INDEX),

    USERVIEWS	  ("userViews", 		"网友情绪", 				   Const.HYBASE_NI_INDEX),

  //  TOPICEVOEXPLOR("topicEvoExplor", 	"话题演变探索", 			    Const.HYBASE_NI_INDEX);

    /**
     * 列表栏目
     */
    LIST_NO_SIM(ColumnConst.LIST_NO_SIM, "列表栏目","ALL"),
    /**
     * 热点栏目
     */
    HOT_LIST(ColumnConst.LIST_SIM, "热点栏目","ALL"),
    /**
     * 词云栏目
     */
    WORD_CLOUD(ColumnConst.CHART_WORD_CLOUD, "词云图","ALL"),
    /**
     * 地域热力图
     */
    MAP(ColumnConst.CHART_MAP, "地域热力图","ALL"),
    /**
     * 柱状图
     */
    CHART_BAR(ColumnConst.CHART_BAR, "柱状图","ALL"),
    /**
     * 柱状图  横向柱状图，主要为展示效果不同，则
     */
    CHART_BAR_CROSS(ColumnConst.CHART_BAR_CROSS, "活跃账号图","ALL"),

    /**
     * 折线图
     */
    CHART_LINE(ColumnConst.CHART_LINE, "折线图","ALL"),

    /**
     * 饼状图
     */
    CHART_PIE(ColumnConst.CHART_PIE, "饼状图","ALL"),
    /**
     * 饼状图 - 舆论场发布统计，这个图和上面的饼图几乎一样，只是前端页面的展示不一样
     */
    CHART_PIE_OPINION("opinionStatistics", "饼状图 - 舆论场发布统计","ALL"),

    /**
     * 饼状图
     */
    CHART_PIE_EMOTION(ColumnConst.CHART_PIE_EMOTION, "情感分析饼状图 - 正负面","ALL"),
    /**
     * 饼状图 - 情绪统计，喜怒哀乐惧等
     */
    CHART_PIE_MOOD("moodStatistics","情绪分析饼状图 - 喜怒哀乐惧等","ALL"),
    /**
     * 传播分析站点
     */
    CHART_LINE_SITE("spreadAnalysisSiteName","传播分析站点","ALL");


    private String typeCode;

    private String typeName;

    private String resource;

    public static SpecialChartType getSpecialChartType(String typeCode){
        for(SpecialChartType specialChartType :SpecialChartType.values()){
            if(specialChartType.typeCode.equals(typeCode)){
                return specialChartType;
            }
        }
        return null;
    }
}
