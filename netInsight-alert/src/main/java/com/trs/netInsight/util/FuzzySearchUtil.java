package com.trs.netInsight.util;

import com.trs.ckm.soap.CkmSoapException;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.support.ckm.ICkmService;
import com.trs.netInsight.support.ckm.entity.SegWord;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.log.entity.FuzzySearchLog;
import com.trs.netInsight.support.log.service.IFuzzySearchLogService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class FuzzySearchUtil {

    @Autowired
    private IFuzzySearchLogService iFuzzySearchLogService;
    private static IFuzzySearchLogService fuzzySearchLogService;

    @Autowired
    private ICkmService iCkmService;
    private static ICkmService ckmService;

    private static String splitWord = "splitWord";
    private static String splitWordNew = "splitWordNew";
    private static String splitWordInclude = "splitWordInclude";
    private static String splitMethod = splitWordInclude;

    @PostConstruct
    public void init() {
        fuzzySearchLogService = this.iFuzzySearchLogService;
        ckmService = this.iCkmService;
    }

    //获取初始表达式
		/*
		(IR_URLTITLE:("李文亮" AND "调查结果")^50)
			OR
			(IR_CONTENT:("李文亮" AND "调查结果"))
		 */
    //要拼为：
		/*
		(
		(
			(IR_URLTITLE:("李文亮" AND "调查结果")^50)
			OR
			(IR_CONTENT:("李文亮" AND "调查结果"))
		)^50
		OR
		(
			(IR_URLTITLE:("李文亮" AND ("调查" OR "结果")))
			OR
			(IR_CONTENT:("李文亮" AND ("调查" OR "结果")))
		)
	 )
		 */

    /**
     * 拼接表达式 - 主要是针对模糊查询
     *
     * @param keywordTrsl   关键字拼接的表达式  如 (("李文亮") AND ("调查结果"))
     * @param keywords      关键字   如 李文亮，调查结果
     * @param keyWordIndex  关键字检索位置
     * @param weight        优先命中标题  权重
     * @param isFuzzySearch 是否模糊查询 - >关键字拆分
     * @return
     */
    public static String filterFuzzyTrsl(String originKeyword, StringBuilder keywordTrsl, String keywords, String keyWordIndex, Boolean weight, Boolean isFuzzySearch) {
        Boolean isInfo = false;
        return filterFuzzyTrsl(originKeyword, keywordTrsl, keywords, keyWordIndex, weight, isFuzzySearch, isInfo);
    }

    /**
     * 拼接表达式 - 主要是针对模糊查询
     *
     * @param keywordTrsl   关键字拼接的表达式  如 (("李文亮") AND ("调查结果"))
     * @param keywords      关键字   如 李文亮，调查结果
     * @param keyWordIndex  关键字检索位置
     * @param weight        优先命中标题  权重
     * @param isFuzzySearch 是否模糊查询 - >关键字拆分
     * @param isInfo        是否存储查询记录  -  >普通搜索只需要存储统计的查询记录即可
     * @return
     */
    public static String filterFuzzyTrsl(String originKeyword, StringBuilder keywordTrsl, String keywords, String keyWordIndex, Boolean weight, Boolean isFuzzySearch, Boolean isInfo) {
        // searchType 为检索范围 仅标题 ：positionKey 标题+正文：positioCon
        //querybuilder 类型有两种
        if (StringUtil.isEmpty(keywords)) {
            return null;
        }
        if ("positioCon".equals(keyWordIndex)) {//标题+正文
            keyWordIndex = "1";
        } else if ("positionKey".equals(keyWordIndex)) {//仅标题
            keyWordIndex = "0";
        }

        QueryBuilder newBuilder = new QueryBuilder();
        newBuilder.filterChildField(FtsFieldConst.FIELD_TITLE, keywordTrsl.toString(), Operator.Equal);
        StringBuilder initTrsl = new StringBuilder();
        if ("1".equals(keyWordIndex)) {//标题+正文
            if (weight) {
                initTrsl.append(newBuilder.asTRSL()).append(FtsFieldConst.WEIGHT).append(" OR ")
                        .append(FtsFieldConst.FIELD_CONTENT).append(":(").append(keywordTrsl.toString()).append(")");
                //20200718 - 查询时，需要对ocrContent一起进行查询，不管命中位置是哪里（原因：因为只有微薄有ocrContent，所以这个虽然查了所有库，但是只对微博生效，微博只有正文字段，所以两个字段一起查）
                initTrsl.append(" OR ")
                        .append(FtsFieldConst.FIELD_OCR_CONTENT).append(":(").append(keywordTrsl.toString()).append(")");
            } else {
                newBuilder.filterChildField(FtsFieldConst.FIELD_CONTENT, keywordTrsl.toString(), Operator.Equal);
                newBuilder.filterChildField(FtsFieldConst.FIELD_OCR_CONTENT, keywordTrsl.toString(), Operator.Equal);
                initTrsl.append(newBuilder.asTRSL());
            }
        }else if ("2".equals(keyWordIndex)) {//标题 + 摘要
            if (weight) {
                initTrsl.append(newBuilder.asTRSL()).append(FtsFieldConst.WEIGHT).append(" OR ")
                        .append(FtsFieldConst.FIELD_ABSTRACTS).append(":(").append(keywordTrsl.toString()).append(")");
                initTrsl.append(" OR ")
                        .append(FtsFieldConst.FIELD_OCR_CONTENT).append(":(").append(keywordTrsl.toString()).append(")");
            } else {
                newBuilder.filterChildField(FtsFieldConst.FIELD_ABSTRACTS, keywordTrsl.toString(), Operator.Equal);
                newBuilder.filterChildField(FtsFieldConst.FIELD_OCR_CONTENT, keywordTrsl.toString(), Operator.Equal);
                initTrsl.append(newBuilder.asTRSL());
            }
        } else if ("0".equals(keyWordIndex)) {//仅标题
            initTrsl.append(newBuilder.asTRSL());
        }
        if (!isFuzzySearch) {
            if (isInfo) {
                recordFuzzySearchInfo(isFuzzySearch,originKeyword, keywords, "", keywordTrsl.toString(), "", initTrsl.toString(), "");
            }
            return initTrsl.toString();
        }
        //获取分词后关键词
        String words = "";
        String wordsNew = "";
        String wordsNewInclude = "";
        if(splitMethod.equals(splitWordInclude)){
            words = splitWordInclude(keywords);
            wordsNewInclude = words;
        }else if(splitMethod.equals(splitWord)){
            words = splitWord(keywords);
        }else if(splitMethod.equals(splitWordNew)){
            words = splitWordNew(keywords);
            wordsNew = words;
        }


        if (words == null) {
            if (isInfo) {
                recordFuzzySearchInfo(isFuzzySearch,originKeyword, keywords, "", keywordTrsl.toString(), "", initTrsl.toString(), "");
            }
            //words为NULL时，代表没有可以分词的数据，则不分词
            return initTrsl.toString();
        }

        StringBuilder filterTrsl = new StringBuilder();
        StringBuilder filterTrsl1 = new StringBuilder();
        filterTrsl.append("((").append(initTrsl).append(")").append(FtsFieldConst.WEIGHT).append(" OR ");
        filterTrsl1.append("((").append(initTrsl).append(")").append(FtsFieldConst.WEIGHT).append(" OR ");

        if (splitMethod.equals(splitWordInclude)) {
            //上面都是一样的，下面接着判断，用是否有相似度

            String fuzzyKeywords = "";
            //and关系之间用 表达式连接
            StringBuilder fuzzyTrsl = new StringBuilder();
            String[] andArr = wordsNewInclude.split("[,|，]");

            for (String and : andArr) {
                StringBuilder str = new StringBuilder();
                if (and.contains(FtsFieldConst.INCLUDE)) {
                    String[] orArr = and.split("[;|；]");
                    StringBuilder orStr = new StringBuilder();
                    for (String or : orArr) {
                        if (orStr.length() > 0) {
                            orStr.append(" OR ");
                        }
                        if (or.contains(FtsFieldConst.INCLUDE)) {
                            or = or.replaceAll(FtsFieldConst.INCLUDE, "");
                            String include = "~" + or.split("~")[1];
                            or = or.replaceAll(include, "");
                            orStr.append("(").append(FtsFieldConst.FIELD_TITLE).append(FtsFieldConst.INCLUDE).append(":\"").append(or).append("\"").append(include).append(")");
                        } else {
                            or = or.replaceAll("[,|，]", "\" AND \"").replaceAll(" AND ", "\" AND \"");
                            or = "(\"" + or + "\")";
                            or = or.replaceAll("\"[(]", "(\"").replaceAll("[)]\"", "\")");
                            orStr.append("(").append(FtsFieldConst.FIELD_TITLE).append(":").append("").append(or).append(")");
                        }
                    }
                    str.append("(").append(orStr.toString()).append(")");
                    /*and = and.replaceAll("[;|；]"," ").replaceAll(FtsFieldConst.INCLUDE,"");
                    str.append("(").append(FtsFieldConst.FIELD_TITLE).append(FtsFieldConst.INCLUDE).append(":\"").append(and).append("\"").append(includeNum).append(")");//拼标题*/
                } else {
                    and = and.replaceAll("[;|；]+", "\" OR \"");
                    and = and.replaceAll(" AND ", "\" AND \"");
                    and = "(\"" + and + "\")";
                    and = and.replaceAll("\"[(]", "(\"").replaceAll("[)]\"", "\")");
                    str.append("(").append(FtsFieldConst.FIELD_TITLE).append(":").append(and).append(")");
                }
                if (fuzzyTrsl.length() > 0) {
                    fuzzyTrsl.append(" AND ");
                }
                fuzzyTrsl.append(str.toString());
            }
            if (fuzzyTrsl.length() > 0) {
                String title = fuzzyTrsl.toString();
                String content = "";
                filterTrsl1.append("(").append(fuzzyTrsl).append(")");
                String ocrContent = title.replaceAll(FtsFieldConst.FIELD_TITLE, FtsFieldConst.FIELD_OCR_CONTENT);
                filterTrsl1.append(" OR ").append("(").append(ocrContent).append(")");
                if ("1".equals(keyWordIndex)) {//标题+正文
                    content = title.replaceAll(FtsFieldConst.FIELD_TITLE, FtsFieldConst.FIELD_CONTENT);
                    filterTrsl1.append(" OR ").append("(").append(content).append(")");
                }else if ("2".equals(keyWordIndex)) {//标题+摘要
                    content = title.replaceAll(FtsFieldConst.FIELD_TITLE, FtsFieldConst.FIELD_ABSTRACTS);
                    filterTrsl1.append(" OR ").append("(").append(content).append(")");
                }
                filterTrsl1.append(")");
            }
            if (isInfo) {
                recordFuzzySearchInfo(isFuzzySearch,originKeyword, keywords, wordsNewInclude, keywordTrsl.toString(), fuzzyTrsl.toString(), initTrsl.toString(), filterTrsl1.toString());
            }
            return filterTrsl1.toString();

        } else if (splitMethod.equals(splitWord)) {
            if (words.endsWith(";") || words.endsWith(",") || words.endsWith("；")
                    || words.endsWith("，")) {
                words = words.substring(0, words.length() - 1);
            }
            String replaceWord = words.replaceAll("[,|，]+", "\" ) AND ( \"").replaceAll("[;|；]+", "\" OR \"");

            //replaceWord = replaceWord.replaceAll("\"[(]","(\"").replaceAll("[)]\"","\")");
            StringBuilder childBuilder = new StringBuilder();
            childBuilder.append("((\"")
                    .append(replaceWord)
                    .append("\"))");
            replaceWord = childBuilder.toString().replaceAll("\"[(]", "(\"").replaceAll("[)]\"", "\")");

            if ("1".equals(keyWordIndex)) {//标题+正文
                filterTrsl.append("((").append(FtsFieldConst.FIELD_TITLE).append(":").append("(").append(replaceWord).append("))");//拼标题
                filterTrsl.append(" OR (").append(FtsFieldConst.FIELD_CONTENT).append(":").append("(").append(replaceWord).append(")))");//拼正文
            }else if ("2".equals(keyWordIndex)) {//标题+摘要
                filterTrsl.append("((").append(FtsFieldConst.FIELD_TITLE).append(":").append("(").append(replaceWord).append("))");//拼标题
                filterTrsl.append(" OR (").append(FtsFieldConst.FIELD_ABSTRACTS).append(":").append("(").append(replaceWord).append(")))");//拼正文
            } else if ("0".equals(keyWordIndex)) {//仅标题
                filterTrsl.append("((").append(FtsFieldConst.FIELD_TITLE).append(":").append("(").append(replaceWord).append(")))");
            }
            filterTrsl.append(")");
            if (isInfo) {
                recordFuzzySearchInfo(isFuzzySearch,originKeyword, keywords, words, keywordTrsl.toString(), replaceWord, initTrsl.toString(), filterTrsl.toString());
            }
            return filterTrsl.toString();
        } else if (splitMethod.equals(splitWordNew)) {

        }
        return null;
    }


    /**
     *
     */


    /**
     * 分词，传进来的次是，；分割的次，
     * 这次只做普通搜索的版本，普通搜索情况下，只有 AND 和 OR 的逻辑
     *
     * @param word
     * @return
     */
    public static String splitWord(String word) {
        //，AND  ;OR
        //查询逻辑 and （ or ）
		/*
			李文亮，调查结果 --》 李文亮，调查；结果
			科比遗照泄露 --》科比;遗照；泄露
		*/
        /**
         * 地名一般为必要词，
         */
        //判断是否有词被分了
        Boolean isSplit = false;
        String[] andArr = word.split("[,|，]");
        List<String> andList = new ArrayList<>();
        for (String and : andArr) {
            String[] orArr = and.split(";|；");
            List<String> orList = new ArrayList<>();
            for (String or : orArr) {
                //获取到每个词
                //如果不可分，直接就是这个词，如果可以分，用；分号间隔
                List<SegWord> segList = null;
                try {
                    segList = ckmService.SegMakeWord(or);
                } catch (CkmSoapException e) {
                    segList = new ArrayList<>();
                    e.printStackTrace();
                }
                String split = or;
                if (segList.size() > 0) {
                    if (segList.size() > 1) {
                        isSplit = true;
                    }

                    List<String> splitList = new ArrayList<>();
                    List<String> nslist = new ArrayList<>();

                    for (SegWord seg : segList) {
                        if (seg.getCate().contains("ns") || seg.getCate().contains("nt")) {//地名或者机构名
                            nslist.add(seg.getWord());
                        } else {
                            splitList.add(seg.getWord());
                        }
                    }
                    if (nslist.size() > 0) {
                        if (splitList.size() > 0) {
                            split = "(" + StringUtils.join(nslist, ";") + "," + StringUtils.join(splitList, ";") + ")";
                        } else {
                            split = "(" + StringUtils.join(nslist, ";") + ")";
                        }
                    } else {
                        split = StringUtils.join(splitList, ";");
                    }
                   /* for(SegDictWord seg : segList){
                        splitList.add(seg.getword());
                    }*/
                }
                orList.add(split);
            }
            if (orList.size() > 0) {
                //一个and词组内部，如调查结果， 调查结果应用或关联
                andList.add(StringUtils.join(orList, ";"));
            }
        }
        //如果有词被分了，才需要模糊查询
        if (isSplit) {
            if (andList.size() > 0) {
                String value = StringUtils.join(andList, ",");
                return value;
            } else {
                return null;
            }
        } else {

            return null;
        }
    }


    /**
     * 分词，传进来的次是，；分割的次，
     * 这次只做普通搜索的版本，普通搜索情况下，只有 AND 和 OR 的逻辑
     * <p>
     * 这个方法是Ckm之前进行了语义分析
     *
     * @param word
     * @return
     */
    public static String splitWordNew(String word) {
        //，AND  ;OR
        //查询逻辑 and （ or ）
		/*
			李文亮，调查结果 --》 李文亮，调查；结果
			科比遗照泄露 --》科比;遗照；泄露
		*/
        /**
         * 地名一般为必要词，
         */
        //判断是否有词被分了
        Boolean isSplit = false;
        String[] andArr = word.split("[,|，]");
        List<String> andList = new ArrayList<>();
        for (String and : andArr) {
            String[] orArr = and.split(";|；");
            List<String> orList = new ArrayList<>();
            for (String or : orArr) {
                //获取到每个词
                //如果不可分，直接就是这个词，如果可以分，用；分号间隔
                List<SegWord> segList = null;
                try {
                    segList = ckmService.SegMakeWord(or);
                } catch (CkmSoapException e) {
                    segList = new ArrayList<>();
                    e.printStackTrace();
                }
                String split = or;
                if (segList.size() > 0) {
                    if (segList.size() > 1) {
                        isSplit = true;
                    }

                    /*
                     分词过后的词分为语义部分和不重要部分
                     如果包含主句，则为 主句之间AND   OR其他
                     */
                    List<String> splitList = new ArrayList<>();
                    List<String> otherList = new ArrayList<>();
                    List<String> mainList = new ArrayList<>();
                    for (SegWord seg : segList) {
                        splitList.add(seg.getWord());
                        if (seg.isMain()) {
                            mainList.add(seg.getWord());
                        } else {
                            otherList.add(seg.getWord());
                        }
                    }
                    /*
                    如何判断呢？
                    1、语句中提取到了主词和副词
                        1.1如果主词只有1个
                            1.1.1副词有1个***
                                主词和副词都命中，AND 关系
                            1.1.2副词有2个
                                主词命中，副词命中一个   A AND (B OR C)
                            1.1.3副词有多个
                                主词必须命中，副词至少命中一半
                        1.2如果主词有2个
                            1.2.1副词有1个
                                OR 关系，至少命中2个
                            1.2.2 副词有2个
                                主词必须命中，副词命中1个A AND B and(C OR D)
                            1.2.3副词有多个
                                主词必须命中，副词至少命中一个
                        1.3如果主词有多个
                            1.3.1副词有1个
                                主词命中，副词无所谓命中
                            1.3.2副词有2个
                                主词必须命中，副词命中1个，A AND B and(C OR D)
                            1.3.3副词有多个
                                主词命中，副词至少命中一个
                    2、语句中只提取到了主词
                        2.1 主词1个***
                            全命中
                        2.2 主词两个***
                            全命中
                        2.3 主词多个
                            命中一半

                    3、语句中只提取到了副词
                        3.1 副词1个****
                            全命中
                        3.2 副词两个***
                            全命中
                        3.3 副词多个
                            命中一半
                    */
                    /*
                    因为不能改变用户输入的逻辑，所以需要在OR之间 去进行修改，
                     */
                    //代表的是 1.1.1  2.1  2.2   3.1  3.2
                    if (splitList.size() <= 2) {
                        split = StringUtils.join(otherList, " AND ");
                    } else {
                        if (mainList.size() > 0) {
                            if (otherList.size() == 0) {
                                //2.3
                                split = FtsFieldConst.INCLUDE + StringUtils.join(mainList, " OR ");
                            } else {
                                if (otherList.size() == 1) {
                                    //两种情况
                                    if (mainList.size() == 2) {
                                        //1.2.1
                                        mainList.addAll(otherList);
                                        split = FtsFieldConst.INCLUDE + StringUtils.join(mainList, " OR ");
                                    } else if (mainList.size() > 2) {
                                        //1.3.1
                                        String mainStr = StringUtils.join(mainList, " AND ");
                                        split = mainStr + " AND ((" + mainStr + ") OR " + otherList.get(0) + ")";

                                    }
                                } else if (otherList.size() == 2) {
                                    //1.1.2  1.2.2  1.3.2
                                    mainList.add("(" + StringUtils.join(otherList, " OR ") + ")");
                                    split = StringUtils.join(mainList, " AND ");
                                } else {
                                    if (mainList.size() == 1) {
                                        //1.1.3
                                        split = mainList.get(0) + " AND (" + FtsFieldConst.INCLUDE + StringUtils.join(otherList, " OR ") + ")";
                                    } else {
                                        //1.2.3  1.3.3
                                        mainList.add("(" + StringUtils.join(otherList, " OR ") + ")");
                                        split = StringUtils.join(mainList, " AND ");
                                    }
                                }
                            }
                        } else {
                            //3.3
                            //全部是从句，则命中部分词  FtsFieldConst.INCLUDE
                            split = FtsFieldConst.INCLUDE + StringUtils.join(otherList, " OR ");
                        }
                    }
                }
                orList.add(split);
            }
            //原有分的逻辑不能改变，
            if (orList.size() > 0) {
                //一个and词组内部，如调查结果， 调查结果应用或关联
                andList.add(StringUtils.join(orList, ";"));
            }
        }
        //如果有词被分了，才需要模糊查询
        if (isSplit) {
            if (andList.size() > 0) {
                String value = StringUtils.join(andList, ",");
                return value;
            } else {
                return null;
            }
        } else {

            return null;
        }
    }

    /**
     * 分词，传进来的次是，；分割的次，
     * 这次只做普通搜索的版本，普通搜索情况下，只有 AND 和 OR 的逻辑
     *
     * @param word
     * @return
     */
    public static String splitWordInclude(String word) {
        //，AND  ;OR
        //查询逻辑 and （ or ）
		/*
			李文亮，调查结果 --》 李文亮，调查；结果
			科比遗照泄露 --》科比;遗照；泄露
		*/
        /**
         * 地名一般为必要词，
         */
        //判断是否有词被分了
        Boolean isSplit = false;
        String[] andArr = word.split("[,|，]");
        List<String> andList = new ArrayList<>();
        for (String and : andArr) {
            Boolean isIncluede = false;
            String[] orArr = and.split(";|；");
            List<String> orList = new ArrayList<>();
            for (String or : orArr) {
                //获取到每个词
                //如果不可分，直接就是这个词，如果可以分，用；分号间隔
                List<SegWord> segList = null;
                try {
                    segList = ckmService.SegMakeWord(or);
                } catch (Exception e) {
                    segList = new ArrayList<>();
                    e.printStackTrace();
                }
                String split = or;
                if (segList.size() > 0) {
                    if (segList.size() > 1) {
                        isSplit = true;
                    }
                    /*
                     分词之后 本来的OR之间还是用OR关联，同时，分掉的词也用OR
                     */
                    List<String> splitList = new ArrayList<>();
                    for (SegWord seg : segList) {
                        splitList.add(seg.getWord());
                    }
                    if (splitList.size() > 0) {
                        if (splitList.size() == 1) {
                            split = or;
                        } else if (splitList.size() == 2) {
                            //必须加括号，否则会跟其他的表达式混在一起
                            split = "(" +StringUtils.join(splitList, " AND ") +")";
                        } else {
                            Integer include = 50;
                            if (splitList.size() % 2 != 0) {
                                include = splitList.size() / 2 + 1;
                            }
                            split = FtsFieldConst.INCLUDE + StringUtils.join(splitList, " ") + "~" + include;
                        }
                    }
                }
                orList.add(split);
            }
            if (orList.size() > 0) {
                String orSplit = StringUtils.join(orList, ";");
                //一个and词组内部，如调查结果， 调查结果应用或关联
                andList.add(orSplit);
            }
        }
        //如果有词被分了，才需要模糊查询
        if (isSplit) {
            if (andList.size() > 0) {
                String value = StringUtils.join(andList, ",");
                return value;
            } else {
                return null;
            }
        } else {

            return null;
        }
    }

    /**
     * 分词，传进来的次是，；分割的次，
     * 这个方法只对词进行分词，不进行表达式拼接，被拆分的词用or拼接
     *
     * @param word
     * @return
     */
    public static String splitKeyWordsForOr(String word) {
        //，AND  ;OR
        //判断是否有词被分了
        Boolean isSplit = false;
        String[] andArr = word.split("[,|，]");
        List<String> andList = new ArrayList<>();
        for (String and : andArr) {
            String[] orArr = and.split(";|；");
            List<String> orList = new ArrayList<>();
            for (String or : orArr) {
                //获取到每个词
                //如果不可分，直接就是这个词，如果可以分，用；分号间隔
                List<SegWord> segList = null;
                try {
                    segList = ckmService.SegMakeWord(or);
                } catch (Exception e) {
                    segList = new ArrayList<>();
                    e.printStackTrace();
                }
                String split = or;

                if (segList.size() > 0) {
                    if (segList.size() == 1 && !split.equals(segList.get(0))) {
                        isSplit = true;
                        orList.add(segList.get(0).getWord());
                    } else if (segList.size() > 1) {
                        isSplit = true;
                        for (SegWord seg : segList) {
                            orList.add(seg.getWord());
                        }
                    }
                }
                orList.add(or);

            }
            if (orList.size() > 0) {
                String orSplit = StringUtils.join(orList, ";");
                //一个and词组内部，如调查结果， 调查结果应用或关联
                andList.add(orSplit);
            }
        }
        //如果有词被分了，才需要模糊查询
        if (isSplit) {
            if (andList.size() > 0) {
                return StringUtils.join(andList, ",");
            } else {
                return null;
            }
        } else {

            return null;
        }
    }



    /**
     * @param isFuzzySearch 是否是模糊查询，还是精准
     * @param originKeyword 关键词 - 页面输入的（包含空格的）
     * @param keywords      关键词 - 页面输入的（空格已被转化为逗号）
     * @param fuzzyKeywords 模糊关键词 - 将页面关键词分词过后的  - 》 没有分词的情况下为空
     * @param keywords      关键字  -  符号和空格替换为 AND OR
     * @param fuzzyKeywords 模糊关键词  -  将部分关键字分词之后  符号和空格替换为 AND OR
     * @param trsl          精准查询表达式 - 对应页面输入的关键词拼接成的表达式
     * @param fuzzyTrsl     模糊查询表达式  - 为精准表达式 + 模糊关键词拼接后的表达式 模糊查询时，真正的表达式  - 》 没有分词的情况下为空
     */
    private static void recordFuzzySearchInfo(Boolean isFuzzySearch,String originKeyword, String keywords, String fuzzyKeywords,
                                              String replaceKeywords, String replaceFuzzyKeywords, String trsl, String fuzzyTrsl) {
        FuzzySearchLog fuzzySearchLog = new FuzzySearchLog();
        fuzzySearchLog.setIsFuzzySearch(isFuzzySearch);
        fuzzySearchLog.setOriginKeyword(originKeyword);
        fuzzySearchLog.setKeywords(keywords);
        fuzzySearchLog.setFuzzyKeywords(fuzzyKeywords);
        fuzzySearchLog.setReplaceKeywords(replaceKeywords);
        fuzzySearchLog.setReplaceFuzzyKeywords(replaceFuzzyKeywords);
        fuzzySearchLog.setTrsl(trsl);
        fuzzySearchLog.setFuzzyTrsl(fuzzyTrsl);
        fuzzySearchLogService.save(fuzzySearchLog);
    }

}
