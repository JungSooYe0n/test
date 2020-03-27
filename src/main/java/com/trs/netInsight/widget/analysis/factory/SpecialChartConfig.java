package com.trs.netInsight.widget.analysis.factory;

import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.util.TrslUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 专题分析图表配置类
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2020/1/7 17:30.
 * @desc
 */
@Data
@Slf4j
public class SpecialChartConfig {
    /**
     * 专题分析实体
     */
    private SpecialProject specialProject;

    /**
     * 页码
     */
    private int pageNo;

    /**
     * 每页展示条数
     */
    private int pageSize;


    /**
     * 情感值筛选
     */
    private String emotion;

    /**
     * 排序字段
     */
    private String orderBy;
    /**
     * 来源筛选
     */
    private String groupName;


    /**
     * 检索构造器
     */
    private QueryBuilder queryBuilder;

    /**
     * 联合查询通用构造器
     */
    private QueryCommonBuilder commonBuilder;


    public void init(SpecialProject specialProject, String timeRange, int pageNo, int pageSize, String entityType, String orderBy,
                     String fuzzyValue) throws OperationException {
       //专题配置
        queryBuilder =  specialProject.toBuilder(pageNo,pageSize);
        //情感选项
        if (!"ALL".equals(emotion)) { // 情感
            if ("中性".equals(emotion)) {
                // 因为hybase库里没有中性标，默认IR_APPRAISE字段值为""时 是中性
                // 直接为某字段赋空置，是不行的
                String trsl = queryBuilder.asTRSL();
                trsl += new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_APPRAISE).append(":(")
                        .append("正面").append(" OR ").append("负面").append(")").toString();
                queryBuilder = new QueryBuilder();
                queryBuilder.filterByTRSL(trsl);
            } else {
                queryBuilder.filterField(FtsFieldConst.FIELD_APPRAISE, emotion, Operator.Equal);
            }
        }

        if (!"ALL".equals(groupName) && StringUtil.isNotEmpty(groupName)){
            if ("境外网站".equals(groupName)){
                groupName = "国外新闻";
            }else if ("微信".equals(groupName)){
                groupName = "国内微信";
            }else if ("新闻".equals(groupName)){
                groupName = "国内新闻";
            }else if ("客户端".equals(groupName)){
                groupName = "国内新闻_手机客户端";
            }else if ("论坛".equals(groupName)){
                groupName = "国内论坛";
            }else if ("博客".equals(groupName)){
                groupName = "国内博客";
            }else if ("电子报".equals(groupName)){
                groupName = "国内新闻_电子报";
            }
            queryBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,groupName,Operator.Equal);
            String database = TrslUtil.chooseDatabases(groupName);
            queryBuilder.setDatabase(database);
        }

        // 结果中搜索
        if (StringUtil.isNotEmpty(fuzzyValue)) {
            String trsl = new StringBuffer().append(FtsFieldConst.FIELD_TITLE).append(":\"").append(fuzzyValue)
                    .append("\" OR ").append(FtsFieldConst.FIELD_CONTENT).append(":\"").append(fuzzyValue)
                    .append("\"").toString();
            queryBuilder.filterByTRSL(trsl);
        }

        //排序
        switch (orderBy) { // 排序
            case "desc":
                queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, true);
                break;
            case "asc":
                queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME, false);
                break;
//            case "hot":
//                return infoListService.getHotListWeChat(builder, countBuilder, loginUser,"special");
            default:
                queryBuilder.orderBy(FtsFieldConst.FIELD_RELEVANCE, true);
                break;
        }
        // 检索关键词转换为trsl
        if ("ALL".equals(groupName)){
            String source = specialProject.getSource();
            String[] databases  = TrslUtil.chooseDatabases(source.split(";"));
            commonBuilder.filterByTRSL(queryBuilder.asTRSL());
            commonBuilder.setDatabase(databases);

            source.replace("微信","国内微信").replace("境外媒体","国外新闻").replace("境外网站","国外新闻").replace(";"," OR ");
            commonBuilder.filterField(FtsFieldConst.FIELD_GROUPNAME,source,Operator.Equal);
        }
    }
}
