package com.trs.netInsight.util;

import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.QueryCommonBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 普通模式词距关键词处理
 * //主要是混合列表有微博原发/转发、国内论坛主贴/回帖刷选情况下，对表达式的处理
 *
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/3/13 17:47.
 * @desc
 */
public class WordSpacingUtil {

    /**
     * 处理关键词 并返回queryBuilder
     * @param keyWords  关键字（JsonArrary格式，包含关键词、词距和词序）
     * @param keyWordindex  关键词检索位置 （标题+正文 | 仅标题）
     * @param weight        权重（是否优先命中标题）
     * @return  QueryBuilder
     */
    public static QueryBuilder handleKeyWords(String keyWords,String keyWordindex,boolean weight){
        if (StringUtil.isEmpty(keyWords) || StringUtil.isEmpty(keyWordindex)){
            return null;
        }
        // 切割关键词位置
        keyWords=keyWords.trim();
        String expressionTotal = "";
        if (StringUtil.isNotEmpty(keyWords)){
            JSONArray jsonArray = JSONArray.fromObject(keyWords);
            for (Object keyWord : jsonArray) {
                QueryBuilder queryBuilder = new QueryBuilder();

                StringBuilder childTrsl = new StringBuilder();

                JSONObject parseObject = JSONObject.fromObject(String.valueOf(keyWord));
                String keyWordsSingle = parseObject.getString("keyWords");
                if (StringUtil.isNotEmpty(keyWordsSingle)){
                    int wordSpace = parseObject.getInt("wordSpace");
                    boolean wordOrder = parseObject.getBoolean("wordOrder");
                    //防止关键字以多个 , （逗号）结尾，导致表达式故障问题
                    String[] split = keyWordsSingle.split(",");
                    String splitNode = "";
                    for (int i = 0; i < split.length; i++) {
                        if (StringUtil.isNotEmpty(split[i])) {
                            if (split[i].endsWith(";")){
                                split[i] = split[i].substring(0, split[i].length() - 1);
                            }
                            splitNode += split[i] + ",";
                        }
                    }
                    keyWordsSingle = splitNode.substring(0, splitNode.length() - 1);

                    String replaceAnyKey = "";
                    if (wordSpace > 0){
                        if (keyWordsSingle.endsWith(";")|| keyWordsSingle.endsWith("；")) {
                            replaceAnyKey = keyWordsSingle.substring(0, keyWordsSingle.length() - 1);
                            childTrsl.append("((\\\"")
                                    .append(replaceAnyKey.replaceAll("[,|，]", "\\\\\") AND (\\\\\"").replaceAll("[;|；]+", "\\\\\" OR \\\\\""))
                                    .append("\\\"))");
                        } else {
                            childTrsl.append("((\\\"")
                                    .append(keyWordsSingle.replaceAll("[,|，]", "\\\\\") AND (\\\\\"").replaceAll("[;|；]+", "\\\\\" OR \\\\\""))
                                    .append("\\\"))");
                        }
                    }else {
                        if (keyWordsSingle.endsWith(";")|| keyWordsSingle.endsWith("；")) {
                            replaceAnyKey = keyWordsSingle.substring(0, keyWordsSingle.length() - 1);
                            childTrsl.append("((\"")
                                    .append(replaceAnyKey.replaceAll("[,|，]", "\") AND (\"").replaceAll("[;|；]+", "\" OR \""))
                                    .append("\"))");
                        } else {
                            childTrsl.append("((\"")
                                    .append(keyWordsSingle.replaceAll("[,|，]", "\") AND (\"").replaceAll("[;|；]+", "\" OR \""))
                                    .append("\"))");
                        }
                    }



                    String expressionElement = childTrsl.toString();
                    if (keyWordindex.trim().equals("1")) {// 标题加正文
                        if (StringUtil.isNotEmpty(expressionElement)){
                            //词距+标题命中
                            if (ObjectUtil.isNotEmpty(wordSpace) && wordSpace > 0 && weight){
                                if (wordOrder){
                                    //#PRE
                                    expressionElement = FtsFieldConst.FIELD_TITLE + FtsFieldConst.PRE +":(\"" + expressionElement + "\"~" + wordSpace + ")"+FtsFieldConst.WEIGHT +
                                            " OR "+FtsFieldConst.FIELD_CONTENT+ FtsFieldConst.PRE +":(\""+childTrsl.toString()+"\"~"+wordSpace+")";
                                }else{
                                    //#POS
                                    expressionElement = FtsFieldConst.FIELD_TITLE + FtsFieldConst.POS +":(\"" + expressionElement + "\"~" + wordSpace + ")"+FtsFieldConst.WEIGHT +
                                            " OR "+FtsFieldConst.FIELD_CONTENT+ FtsFieldConst.POS +":(\""+childTrsl.toString()+"\"~"+wordSpace+")";
                                }
                            }else if (ObjectUtil.isNotEmpty(wordSpace) && wordSpace > 0){
                                //仅词距
                                if (wordOrder){
                                    //#PRE
                                    expressionElement = FtsFieldConst.FIELD_TITLE + FtsFieldConst.PRE +":(\"" + expressionElement + "\"~" + wordSpace+ ")" +
                                            " OR "+FtsFieldConst.FIELD_CONTENT+ FtsFieldConst.PRE +":(\""+childTrsl.toString()+"\"~"+wordSpace+ ")";
                                }else{
                                    //#POS
                                    expressionElement = FtsFieldConst.FIELD_TITLE + FtsFieldConst.POS +":(\"" + expressionElement + "\"~" + wordSpace+ ")" +
                                            " OR "+FtsFieldConst.FIELD_CONTENT+ FtsFieldConst.POS +":(\""+childTrsl.toString()+"\"~"+wordSpace+ ")";
                                }
                            }else if (weight){
                                //仅标题命中
                                queryBuilder.filterChildField(FtsFieldConst.FIELD_TITLE, childTrsl.toString(), Operator.Equal);
                                expressionElement = queryBuilder.asTRSL()+FtsFieldConst.WEIGHT+
                                        " OR "+FtsFieldConst.FIELD_CONTENT+":"+childTrsl.toString();
                            }else {
                                //标题+正文  时间排序  无词距
                                queryBuilder.filterChildField(FtsFieldConst.FIELD_TITLE, childTrsl.toString(), Operator.Equal);
                                expressionElement = queryBuilder.asTRSL() +
                                        " OR "+FtsFieldConst.FIELD_CONTENT+":"+childTrsl.toString();
                            }
                        }
                    } else {// 仅标题

                        if (ObjectUtil.isNotEmpty(wordSpace) && wordSpace > 0){
                            //仅词距
                            if (wordOrder){
                                //#PRE
                                expressionElement = FtsFieldConst.FIELD_TITLE + FtsFieldConst.PRE +":(\"" + expressionElement + "\"~" + wordSpace + ")";
                            }else{
                                //#POS
                                expressionElement = FtsFieldConst.FIELD_TITLE + FtsFieldConst.POS +":(\"" + expressionElement + "\"~" + wordSpace + ")" ;
                            }
                        }else {
                            expressionElement = FtsFieldConst.FIELD_TITLE+":"+childTrsl.toString();
                        }
                    }
                    expressionElement = "("+expressionElement+") OR ";
                    expressionTotal += expressionElement;
                }

            }

        }
        QueryBuilder searchBuilder = new QueryBuilder();
        if (StringUtil.isNotEmpty(expressionTotal) && expressionTotal.endsWith(" OR ")){
            expressionTotal = expressionTotal.substring(0,expressionTotal.length()-4);
        }
        //各个关键词组必须在一个小括号内，而filterByTRSL方法会为当前表达式加上小括号
        searchBuilder.filterByTRSL(expressionTotal);
      //  queryBuilder = searchBuilder;
        return searchBuilder;
    }

    /**
     * 处理关键词 并返回queryBuilder（只针对微博，因为微博不论是 仅标题 还是 标题+正文，只查content字段即可，微博没有单独的标题字段）
     * @param keyWords   关键字（JsonArrary格式，包含关键词、词距和词序）
     * @param weight
     * @return
     */
    public static QueryBuilder handleKeyWordsToWeiboTF(String keyWords,boolean weight){
        if (StringUtil.isEmpty(keyWords)){
            return null;
        }
        // 切割关键词位置
        keyWords=keyWords.trim();
        String expressionTotal = "";
        JSONArray jsonArray = JSONArray.fromObject(keyWords);
        for (Object keyWord : jsonArray) {
            QueryBuilder searchBuilder = new QueryBuilder();

            StringBuilder childBuilder = new StringBuilder();
            JSONObject parseObject = JSONObject.fromObject(String.valueOf(keyWord));
            String keyWordsSingle = parseObject.getString("keyWords");
            if (StringUtil.isNotEmpty(keyWordsSingle)){
                int wordSpace = parseObject.getInt("wordSpace");
                boolean wordOrder = parseObject.getBoolean("wordOrder");
                String[] split = keyWordsSingle.split(",");
                String splitNode = "";
                for (int i = 0; i < split.length; i++) {
                    if (StringUtil.isNotEmpty(split[i])) {
                        splitNode += split[i] + ",";
                    }
                }
                keyWordsSingle = splitNode.substring(0, splitNode.length() - 1);
                // 防止全部关键词结尾为;报错
                String replaceAnyKey = "";
                if (wordSpace > 0){
                    if (keyWordsSingle.endsWith(";")) {
                        replaceAnyKey = keyWordsSingle.substring(0, keyWordsSingle.length() - 1);
                        childBuilder.append("((\\\"").append(replaceAnyKey.replaceAll(",", "\\\\\") AND (\\\\\"").replaceAll("[;|；]+", "\\\\\" OR \\\\\""))
                                .append("\\\"))");
                    } else {
                        childBuilder.append("((\\\"")
                                .append(keyWordsSingle.replaceAll(",", "\\\\\") AND (\\\\\"").replaceAll("[;|；]+", "\\\\\" OR \\\\\"")).append("\\\"))");
                    }
                }else {
                    if (keyWordsSingle.endsWith(";")) {
                        replaceAnyKey = keyWordsSingle.substring(0, keyWordsSingle.length() - 1);
                        childBuilder.append("((\"").append(replaceAnyKey.replaceAll(",", "\") AND (\"").replaceAll("[;|；]+", "\" OR \""))
                                .append("\"))");
                    } else {
                        childBuilder.append("((\"")
                                .append(keyWordsSingle.replaceAll(",", "\") AND (\"").replaceAll("[;|；]+", "\" OR \"")).append("\"))");
                    }
                }


                //微博没有title，这个if判断是进不去的，会直接去else
                String expressionElement = childBuilder.toString();
                //词距+标题命中
                if (ObjectUtil.isNotEmpty(wordSpace) && wordSpace > 0 && weight) {
                    if (wordOrder) {
                        //#PRE
                        expressionElement = FtsFieldConst.FIELD_CONTENT + FtsFieldConst.PRE + ":(\"" + expressionElement + "\"~" + wordSpace + ")" + FtsFieldConst.WEIGHT;
                    } else {
                        //#POS
                        expressionElement = FtsFieldConst.FIELD_CONTENT + FtsFieldConst.POS + ":(\"" + expressionElement + "\"~" + wordSpace + ")" + FtsFieldConst.WEIGHT;
                    }
                } else if (ObjectUtil.isNotEmpty(wordSpace) && wordSpace > 0) {
                    //仅词距
                    if (wordOrder) {
                        //#PRE
                        expressionElement = FtsFieldConst.FIELD_CONTENT + FtsFieldConst.PRE + ":(\"" + expressionElement + "\"~" + wordSpace + ")";
                    } else {
                        //#POS
                        expressionElement = FtsFieldConst.FIELD_CONTENT + FtsFieldConst.POS + ":(\"" + expressionElement + "\"~" + wordSpace + ")";
                    }
                } else if (weight) {
                    //仅标题命中
                    searchBuilder.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(), Operator.Equal);
                    expressionElement = searchBuilder.asTRSL() + FtsFieldConst.WEIGHT;
                } else {
                    //标题+正文  时间排序  无词距
                    searchBuilder.filterChildField(FtsFieldConst.FIELD_CONTENT, childBuilder.toString(), Operator.Equal);
                    expressionElement = searchBuilder.asTRSL();
                }

                expressionElement = "(" + expressionElement + ") OR ";
                expressionTotal += expressionElement;
            }

        }
         QueryBuilder queryBuilder = new QueryBuilder();
        if (StringUtil.isNotEmpty(expressionTotal) && expressionTotal.endsWith(" OR ")){
            expressionTotal = expressionTotal.substring(0,expressionTotal.length()-4);
        }
        //各个关键词组必须在一个小括号内，而filterByTRSL方法会为当前表达式加上小括号
        queryBuilder.filterByTRSL(expressionTotal);
      //  searchBuilder = queryBuilder;
        return queryBuilder;
    }



    public static QueryCommonBuilder handleCommonTrsl(QueryCommonBuilder builder,String groupNames){
        String asTrsl = builder.asTRSL();
        String[] database = builder.getDatabase();
        String orderBy = builder.getOrderBy();
        if (StringUtil.isNotEmpty(asTrsl)){
            if ((asTrsl.contains(FtsFieldConst.FIELD_RETWEETED_MID) || asTrsl.contains(FtsFieldConst.FIELD_NRESERVED1)) && groupNames.length() > 1){
                String newAsTrsl = asTrsl;
                //1、
                String exForWeibo = newAsTrsl.replace(" AND (IR_NRESERVED1:(0))","").replace(" AND (IR_NRESERVED1:(1))","").replace("((IR_NRESERVED1:(0)) AND ","").replace("((IR_NRESERVED1:(1)) AND ","");
                String weiboTrsl = FtsFieldConst.FIELD_GROUPNAME + ":(微博) AND " + exForWeibo;

                //2、
                String exForLuntan = newAsTrsl.replace(" AND (IR_RETWEETED_MID:(0 OR \"\"))", "").replace(" NOT IR_RETWEETED_MID:(0 OR \"\")", "");
                String luntanTrsl = FtsFieldConst.FIELD_GROUPNAME + ":(国内论坛) AND " + exForLuntan;

                //3、
                String exGForOther = "";
                if (asTrsl.contains(FtsFieldConst.FIELD_RETWEETED_MID) && asTrsl.contains(FtsFieldConst.FIELD_NRESERVED1)){
                    exGForOther = groupNames.replaceAll("国内论坛 OR ", "").replaceAll(" OR 国内论坛", "").replaceAll("国内论坛", "").replaceAll("微博 OR ", "").replaceAll(" OR 微博", "").replaceAll("微博", "");
                }else if (asTrsl.contains(FtsFieldConst.FIELD_RETWEETED_MID)){
                    exGForOther = groupNames.replaceAll("微博 OR ", "").replaceAll(" OR 微博", "").replaceAll("微博", "");
                }else if (asTrsl.contains(FtsFieldConst.FIELD_NRESERVED1)){
                    exGForOther = groupNames.replaceAll("国内论坛 OR ", "").replaceAll(" OR 国内论坛", "").replaceAll("国内论坛", "");
                }
                String exMNForOther = newAsTrsl.replace(" AND "+"("+"IR_NRESERVED1:(0))","").replace(" AND (IR_NRESERVED1:(1))","").replace("((IR_NRESERVED1:(0)) AND ","").replace("((IR_NRESERVED1:(1)) AND ","").replace(" AND (IR_RETWEETED_MID:(0 OR \"\"))", "").replace(" NOT IR_RETWEETED_MID:(0 OR \"\")", "");

                String otherTrsl = FtsFieldConst.FIELD_GROUPNAME + ":("+exGForOther+") AND " + exMNForOther;

                builder = new QueryCommonBuilder();
                builder.setDatabase(database);
                builder.setOrderBy(orderBy);
                if (asTrsl.contains(FtsFieldConst.FIELD_RETWEETED_MID) && asTrsl.contains(FtsFieldConst.FIELD_NRESERVED1)){
                    String zongTrsl = "("+weiboTrsl + ") OR (" + luntanTrsl + ") OR (" + otherTrsl+")";
                    builder.filterByTRSL(zongTrsl);
                }else if (asTrsl.contains(FtsFieldConst.FIELD_RETWEETED_MID)){
                    String twoTrsl = "("+weiboTrsl + ") OR (" + otherTrsl+")";
                    builder.filterByTRSL(twoTrsl);
                }else if (asTrsl.contains(FtsFieldConst.FIELD_NRESERVED1)){
                    String twoTrsl1 = "("+luntanTrsl + ") OR (" + otherTrsl+")";
                    builder.filterByTRSL(twoTrsl1);
                }
            }else {
                builder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupNames, Operator.Equal);
            }
        }else {
            builder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupNames, Operator.Equal);
        }

        return builder;
    }

    public static QueryBuilder handleTrsl(QueryBuilder builder, String groupNames){
        String asTrsl = builder.asTRSL();
        String database = builder.getDatabase();
        String orderBy = builder.getOrderBy();
        if (StringUtil.isNotEmpty(asTrsl)){
            if ((asTrsl.contains(FtsFieldConst.FIELD_RETWEETED_MID) || asTrsl.contains(FtsFieldConst.FIELD_NRESERVED1)) && groupNames.length() > 1){
                String newAsTrsl = asTrsl;
                //1、
                String exForWeibo = newAsTrsl.replace(" AND (IR_NRESERVED1:(0))","").replace(" AND (IR_NRESERVED1:(1))","").replace("(IR_NRESERVED1:(0)) AND ","").replace("(IR_NRESERVED1:(1)) AND ","");
                String weiboTrsl = FtsFieldConst.FIELD_GROUPNAME + ":(微博) AND " + exForWeibo;

                //2、
                String exForLuntan = newAsTrsl.replace(" AND (IR_RETWEETED_MID:(0 OR \"\"))", "").replace(" NOT IR_RETWEETED_MID:(0 OR \"\")", "");
                String luntanTrsl = FtsFieldConst.FIELD_GROUPNAME + ":(国内论坛) AND " + exForLuntan;

                //3、
                String exGForOther = "";
                if (asTrsl.contains(FtsFieldConst.FIELD_RETWEETED_MID) && asTrsl.contains(FtsFieldConst.FIELD_NRESERVED1)){
                    exGForOther = groupNames.replaceAll("国内论坛 OR ", "").replaceAll(" OR 国内论坛", "").replaceAll("国内论坛", "").replaceAll("微博 OR ", "").replaceAll(" OR 微博", "").replaceAll("微博", "");
                }else if (asTrsl.contains(FtsFieldConst.FIELD_RETWEETED_MID)){
                    exGForOther = groupNames.replaceAll("微博 OR ", "").replaceAll(" OR 微博", "").replaceAll("微博", "");
                }else if (asTrsl.contains(FtsFieldConst.FIELD_NRESERVED1)){
                    exGForOther = groupNames.replaceAll("国内论坛 OR ", "").replaceAll(" OR 国内论坛", "").replaceAll("国内论坛", "");
                }
                String exMNForOther = newAsTrsl.replace(" AND (IR_NRESERVED1:(0))","").replace(" AND (IR_NRESERVED1:(1))","").replace("(IR_NRESERVED1:(0)) AND ","").replace("(IR_NRESERVED1:(1)) AND ","").replace(" AND (IR_RETWEETED_MID:(0 OR \"\"))", "").replace(" NOT IR_RETWEETED_MID:(0 OR \"\")", "");

                String otherTrsl = FtsFieldConst.FIELD_GROUPNAME + ":("+exGForOther+") AND " + exMNForOther;

                builder = new QueryBuilder();
                builder.setDatabase(database);
                builder.setOrderBy(orderBy);
                if (asTrsl.contains(FtsFieldConst.FIELD_RETWEETED_MID) && asTrsl.contains(FtsFieldConst.FIELD_NRESERVED1)){
                    String zongTrsl = "("+weiboTrsl + ") OR (" + luntanTrsl + ") OR (" + otherTrsl+")";
                    builder.filterByTRSL(zongTrsl);
                }else if (asTrsl.contains(FtsFieldConst.FIELD_RETWEETED_MID)){
                    String twoTrsl = "("+weiboTrsl + ") OR (" + otherTrsl+")";
                    builder.filterByTRSL(twoTrsl);
                }else if (asTrsl.contains(FtsFieldConst.FIELD_NRESERVED1)){
                    String twoTrsl1 = "("+luntanTrsl + ") OR (" + otherTrsl+")";
                    builder.filterByTRSL(twoTrsl1);
                }
            }else {
                builder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupNames, Operator.Equal);
            }
        }else {
            builder.filterField(FtsFieldConst.FIELD_GROUPNAME, groupNames, Operator.Equal);
        }

        return builder;
    }
}
