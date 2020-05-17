package com.trs.netInsight.util;

import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.knowledgeBase.entity.KnowledgeBase;

import java.util.List;

/**
 * @program: netInsight
 * @description: 知识库工具类
 * @author: Ma guocai
 * @create: 2020-03-18 15:29
 */
public class KnowledgeBaseUtil {

    /*
    * @Description: 将某条知识库转化为表达式返回
    * @param: trsl
    * @param: list
    * @param: increaseOrDecrease true表示增加词，false表示排除词
    * @return: java.lang.String
    * @Author: Maguocai
    * @create time: 2020/3/25 14:06
    */
    public static String toTransforTrsl(String trsl,List<KnowledgeBase> list,boolean increaseOrDecrease){

        if (ObjectUtil.isNotEmpty(list) && list.size() > 0){
            String keywordsTotal = "";
            for (KnowledgeBase knowledgeBase : list) {
                String keywords = knowledgeBase.getKeywords();
                if (StringUtil.isNotEmpty(keywords)){
                    if (keywords.startsWith(";")){
                        keywords = keywords.substring(1,keywords.length());
                    }
                    if (keywords.endsWith(";")){
                        keywords = keywords.substring(0,keywords.length()-1);
                    }

                    keywordsTotal += ";"+keywords;
                }
            }
            keywordsTotal = keywordsTotal + ";";
            if (StringUtil.isNotEmpty(keywordsTotal)) {
                if (keywordsTotal.startsWith(";")) {
                    keywordsTotal = keywordsTotal.substring(1, keywordsTotal.length());
                }
                if (keywordsTotal.endsWith(";")) {
                    keywordsTotal = keywordsTotal.substring(0, keywordsTotal.length() - 1);
                }
                String trslEx = "";
                StringBuilder exbuilder = new StringBuilder();
                exbuilder.append("(\"")
                        .append(keywordsTotal.replaceAll("[;|；]+", "\" OR \"")).append("\")");
                if (StringUtil.isNotEmpty(trsl)) {
                    if(increaseOrDecrease){
                        trslEx = trsl + " AND (" + FtsFieldConst.FIELD_URLTITLE + ":" + exbuilder.toString() + ") AND (" + FtsFieldConst.FIELD_CONTENT + ":" + exbuilder.toString() + ")";
                    }else {
                        trslEx = trsl + " AND (*:* -" + FtsFieldConst.FIELD_URLTITLE + ":" + exbuilder.toString() + ") AND (*:* -" + FtsFieldConst.FIELD_CONTENT + ":" + exbuilder.toString() + ")";
                    }
                } else {
                    if(increaseOrDecrease){
                        trslEx = "(" + FtsFieldConst.FIELD_URLTITLE + ":" + exbuilder.toString() + ") AND (" + FtsFieldConst.FIELD_CONTENT + ":" + exbuilder.toString() + ")";
                    }else {
                        trslEx = "(*:* -" + FtsFieldConst.FIELD_URLTITLE + ":" + exbuilder.toString() + ") AND (*:* -" + FtsFieldConst.FIELD_CONTENT + ":" + exbuilder.toString() + ")";
                    }
                }

                if (!StringUtil.getStringKBIsTooLong(trslEx))
                    return trslEx;
            }
        }
        return trsl;
    }
    /*
  * @Description: 将知识库任务转化为表达式，并放到builder中
  * @param: builder
  * @param: id
  * @return: com.trs.netInsight.com.trs.netInsight.support.fts.builder.QueryBuilder
  * @Author: Maguocai
  * @create time: 2020/3/18 15:36
  */
    public static QueryBuilder toTransforBuilder(QueryBuilder builder,List<KnowledgeBase> list,boolean increaseOrDecrease){
        String trsl = toTransforTrsl("",list,increaseOrDecrease);
        builder.filterByTRSL(trsl);
        return builder;
    }
    /*
    * @Description:将知识库任务转化为表达式，并放到混合builder中。
    * @param: builder
    * @param: id
    * @return: com.trs.netInsight.com.trs.netInsight.support.fts.builder.QueryCommonBuilder
    * @Author: Maguocai
    * @create time: 2020/3/18 15:39
    */
    public static QueryCommonBuilder toTransforCommonBuilder(QueryCommonBuilder builder,List<KnowledgeBase> list,boolean increaseOrDecrease){
        String trsl = toTransforTrsl("",list,increaseOrDecrease);
        builder.filterByTRSL(trsl);
        return builder;
    }
}
