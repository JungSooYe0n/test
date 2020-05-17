package com.trs.netInsight.widget.analysis.enums;

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

    USERVIEWS	  ("userViews", 		"网友情绪", 				   Const.HYBASE_NI_INDEX);

  //  TOPICEVOEXPLOR("topicEvoExplor", 	"话题演变探索", 			    Const.HYBASE_NI_INDEX);

    private String typeCode;

    private String typeName;

    private String resource;

}
