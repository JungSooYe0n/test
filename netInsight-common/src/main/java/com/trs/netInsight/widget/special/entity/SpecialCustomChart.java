package com.trs.netInsight.widget.special.entity;


import com.fasterxml.jackson.annotation.JsonView;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.WordSpacingUtil;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.*;

/**
 * 专题分析自定义图表实体类
 * 一个栏目可以对应多个自定义图表
 *
 * @author 张娅
 */
@Entity(name = "special_custom_chart")
@Table
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SpecialCustomChart extends BaseEntity{
    @Column(name = "name")
    private String name;// 三级栏目（图）名

    /**
     * 专项类型
     */
    @Column(name = "special_type")
    @ApiModelProperty(notes = "专项类型")
    @Enumerated(EnumType.ORDINAL)
    private SpecialType specialType;
    public SpecialType getSpecialType(){
        if(this.specialType == null) {
            if (StringUtil.isNotEmpty(this.getXyTrsl()) || StringUtil.isNotEmpty(this.getTrsl())) {
                this.specialType = SpecialType.SPECIAL;
            } else {
                this.specialType = SpecialType.COMMON;
            }
        }
        return this.specialType;
    }

    @Column(name = "trsl", columnDefinition = "TEXT")
    private String trsl;// trs表达式

    @Column(name = "xy_trsl", columnDefinition = "TEXT")
    private String xyTrsl;// x轴y轴 表达式

    @Column(name = "type")
    private String type;// 图表类型

    /**
     * 分类对比类型
     */
    @Column(name = "contrast")
    private String contrast;

    @Column(name = "excludeWeb")
    private String excludeWeb;// 排除网站

    @Column(name = "monitor_site")
    private String monitorSite;// 监测网站

    @Column(name = "time_range")
    private String timeRange;// 发布时间范围

    /**
     * 检索关键词
     */
    @Column(name = "key_word", columnDefinition = "TEXT")
    private String keyWord;

    /**
     * 排除词
     */
    @Column(name = "exclude_words", columnDefinition = "TEXT")
    private String excludeWords;

    /**
     * 排除词检索位置 0：标题 1：标题+正文  2：标题+摘要
     */
    @Column(name = "exclude_words_index", columnDefinition = "TEXT")
    private String excludeWordsIndex;
    public String getExcludeWordsIndex(){
        if(StringUtil.isEmpty(this.excludeWordsIndex)){
            return this.keyWordIndex;
        }
        return this.excludeWordsIndex;
    }


    /**
     * 检索关键词位置 0：标题 1：标题+正文
     */
    @Column(name = "key_word_index", columnDefinition = "TEXT")
    private String keyWordIndex;

    /**
     * 保存栏目类型,多值,中间以';'分隔
     */
    @Column(name = "group_name", columnDefinition = "TEXT")
    private String groupName;

    /**
     * 是否排重
     */
    @Column(name = "similar")
    private boolean similar = false;

    /**
     * 按urlname排重  0排 1不排  true排 false不排
     */
    @Column(name = "ir_simflag")
    private boolean irSimflag = false;
//    public boolean isIrSimflag(){
//        if(similar == false && irSimflagAll == false){
//            return true;
//        }else{
//            return irSimflag;
//        }
//    }
    /**
     * 跨数据源排重
     */
    @Column(name = "ir_simflag_all")
    private boolean irSimflagAll = false;
    /**
     * 是否按照权重查找
     */
    @Column(name = "weight")
    private boolean weight = false;

    /**
     * 排序方式
     */
    @Column(name = "sort")
    private String sort;

    /**
     * 50为半栏 100为通栏
     */
    @Column(name = "tab_width")
    private int tabWidth = 50;
    public String getTabWidth(){
        return String.valueOf(this.tabWidth);
    }
    /**
     * 媒体等级
     */
    @Column(name = "media_level")
    private String mediaLevel;
    public String getMediaLevel(){
        if(StringUtil.isNotEmpty(this.mediaLevel)){
            return this.mediaLevel.replaceAll("其他","其它");
        }else{
            return StringUtils.join(Const.MEDIA_LEVEL,";").replaceAll("其他","其它");
        }
    }
    /**
     * 媒体行业
     */
    @Column(name = "media_industry")
    private String mediaIndustry;
    public String getMediaIndustry(){
        if(StringUtil.isNotEmpty(this.mediaIndustry)){
            return this.mediaIndustry.replaceAll("其他","其它");
        }else{
            return StringUtils.join(Const.MEDIA_INDUSTRY,";").replaceAll("其他","其它");
        }
    }
    /**
     * 内容行业
     */
    @Column(name = "content_industry")
    private String contentIndustry;
    public String getContentIndustry(){
        if(StringUtil.isNotEmpty(this.contentIndustry)){
            return this.contentIndustry.replaceAll("其他","其它");
        }else{
            return StringUtils.join(Const.CONTENT_INDUSTRY,";").replaceAll("其他","其它");
        }
    }
    /**
     * 信息过滤  -  信息性质打标，如抽奖
     */
    @Column(name = "filter_info")
    private String filterInfo;
    public String getFilterInfo(){
        if(StringUtil.isNotEmpty(this.filterInfo)){
            return this.filterInfo;
        }else{
            return StringUtils.join(Const.FILTER_INFO,";");
        }
    }
    /**
     * 内容所属地域
     */
    @Column(name = "content_area")
    private String contentArea;
    public String getContentArea(){
        if(StringUtil.isNotEmpty(this.contentArea)){
            return this.contentArea.replaceAll("其他","其它");
        }else{
            return StringUtils.join(Const.AREA_LIST,";").replaceAll("其他","其它");
        }
    }
    /**
     * 媒体所属地域
     */
    @Column(name = "media_area")
    private String mediaArea;
    public String getMediaArea(){
        if(StringUtil.isNotEmpty(this.mediaArea)){
            return this.mediaArea.replaceAll("其他","其它");
        }else{
            return StringUtils.join(Const.AREA_LIST,";").replaceAll("其他","其它");
        }
    }

    /**
     * 对应的日常监测的栏目id
     */
    @Column(name = "parent_id")
    private String parentId;
    /**
     * 顺序 - 统计分析暂不可改变顺序
     */
    @Column(name = "sequence")
    private Integer sequence;
    /**
     * 是否置顶 也就是缩略豆腐块时，是否展示
     */
    @Column(name = "is_top")
    private Boolean isTop = false;
    /**
     * 置顶时顺序
     */
    @Column(name = "top_sequence")
    private Integer topSequence;

    /**
     * 精准筛选
     */
    @Column(name = "precise_filter")
    private String preciseFilter;

    public SpecialCustomChart(String name, String trsl, String xyTrsl, String type, String contrast, String excludeWeb,String monitorSite, String timeRange, String keyWord, String excludeWords,
                       String keyWordIndex, String groupName, Boolean similar, Boolean irSimflag, Boolean irSimflagAll, Boolean weight, Integer tabWidth, String parentId, Integer sequence,SpecialType specialType) {
        this.name = name;
        this.trsl = trsl;
        this.xyTrsl = xyTrsl;
        this.type = type;
        this.contrast = contrast;
        this.excludeWeb = excludeWeb;
        this.monitorSite = monitorSite;
        this.timeRange = timeRange;
        this.keyWord = keyWord;
        this.excludeWords = excludeWords;
        this.keyWordIndex = keyWordIndex;
        this.groupName = groupName;
        this.similar = similar;
        this.irSimflag = irSimflag;
        this.irSimflagAll = irSimflagAll;
        this.weight = weight;
        this.tabWidth = tabWidth;
        this.sequence = sequence;
        this.parentId = parentId;
        this.specialType = specialType;
    }
    public SpecialCustomChart(String name, String trsl, String xyTrsl, String type, String contrast, String excludeWeb, String monitorSite,String timeRange, String keyWord, String excludeWords,
                              String keyWordIndex, String groupName, Boolean similar, Boolean irSimflag, Boolean irSimflagAll, Boolean weight, Integer tabWidth, String parentId, Integer sequence,SpecialType specialType,String mediaLevel,String mediaIndustry,String contentIndustry,
                              String filterInfo, String contentArea, String mediaArea, String preciseFilter) {
        this(name, trsl, xyTrsl, type, contrast, excludeWeb,monitorSite,timeRange, keyWord, excludeWords, keyWordIndex, groupName, similar, irSimflag, irSimflagAll, weight, tabWidth, parentId, sequence, specialType);
        this.mediaLevel = mediaLevel;
        this.mediaIndustry = mediaIndustry;
        this.contentIndustry = contentIndustry;
        this.filterInfo = filterInfo;
        this.contentArea = contentArea;
        this.mediaArea = mediaArea;
        this.preciseFilter = preciseFilter;
    }

    public QueryBuilder getQueryBuilder(Boolean withTime,int pageNo,int pageSize){
        QueryBuilder queryBuilder = new QueryBuilder();

        switch (this.specialType) {
            case COMMON:
                queryBuilder = WordSpacingUtil.handleKeyWords(this.keyWord, this.keyWordIndex, this.weight);

                String excludeIndex = this.getExcludeWordsIndex();
                //拼凑排除词
                String excludeWordTrsl = WordSpacingUtil.appendExcludeWords(excludeWords,excludeIndex);
                if(StringUtil.isNotEmpty(excludeWordTrsl)){
                    queryBuilder.filterByTRSL(excludeWordTrsl);
                }
                //监测网站
                if (StringUtil.isNotEmpty(this.monitorSite)) {
                    String addMonitorSite = addMonitorSite(this.monitorSite);
                    if(StringUtil.isNotEmpty(addMonitorSite)){
                        queryBuilder.filterField(FtsFieldConst.FIELD_SITENAME,addMonitorSite, Operator.Equal);
                    }
                }
                //排除网站
                if (StringUtil.isNotEmpty(this.excludeWeb) && this.excludeWeb.trim().split(";|；").length > 0) {
                    String[] excludeWebArr = this.excludeWeb.trim().split(";|；");
                    String asTRSL = queryBuilder.asTRSL();
                    if (excludeWebArr != null && excludeWebArr.length > 0) {
                        String notSite = "";
                        for (String site : excludeWebArr) {
                            notSite += site + " OR ";
                        }
                        if (notSite.endsWith(" OR ")) {
                            notSite = notSite.substring(0, notSite.length() - 4);
                        }
                        asTRSL += new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_SITENAME).append(":(").append(notSite)
                                .append(")").toString();
                    }
                    queryBuilder = new QueryBuilder();
                    queryBuilder.filterByTRSL(asTRSL);
                }
                //媒体等级
                if(StringUtil.isNotEmpty(this.mediaLevel)){
                    addFieldFilter(queryBuilder,FtsFieldConst.FIELD_MEDIA_LEVEL,this.mediaLevel,Const.MEDIA_LEVEL);
                }
                //媒体行业
                if(StringUtil.isNotEmpty(this.mediaIndustry )){
                    addFieldFilter(queryBuilder,FtsFieldConst.FIELD_MEDIA_INDUSTRY,this.mediaIndustry,Const.MEDIA_INDUSTRY);
                }
                //内容行业
                if(StringUtil.isNotEmpty(this.contentIndustry )){
                    addFieldFilter(queryBuilder,FtsFieldConst.FIELD_CONTENT_INDUSTRY,this.contentIndustry,Const.CONTENT_INDUSTRY);
                }
                //内容地域
                if(StringUtil.isNotEmpty(this.contentArea )){
                    addAreaFilter(queryBuilder,FtsFieldConst.FIELD_CATALOG_AREA,this.contentArea);
                }
                //媒体地域
                if(StringUtil.isNotEmpty(this.mediaArea )){
                    addAreaFilter(queryBuilder,FtsFieldConst.FIELD_MEDIA_AREA,this.mediaArea);
                }
                //信息过滤
                if(StringUtil.isNotEmpty(this.filterInfo )&& !filterInfo.equals(Const.NOT_FILTER_INFO)){
                    String trsl = queryBuilder.asTRSL();
                    StringBuilder sb = new StringBuilder(trsl);
                    String[] valueArr = this.filterInfo.split(";");
                    Set<String> valueArrList = new HashSet<>();
                    for(String v : valueArr){
                        if(Const.FILTER_INFO.contains(v)){
                            valueArrList.add(v);
                        }
                    }
                    if (valueArrList.size() > 0 /*&& valueArrList.size() < Const.FILTER_INFO.size()*/) {
                        sb.append(" NOT (").append(FtsFieldConst.FIELD_FILTER_INFO).append(":(").append(StringUtils.join(valueArrList," OR ")).append("))");
                        queryBuilder = new QueryBuilder();
                        queryBuilder.filterByTRSL(sb.toString());
                    }
                }
                // 精准筛选
                if (StringUtils.isNoneBlank(this.preciseFilter) && !"ALL".equals(this.preciseFilter)) {

                    List<String> sourceList = CommonListChartUtil.formatGroupName(this.groupName);
                    //精准筛选 与上面论坛的主回帖和微博的原转发类似 ，都需要在数据源的基础上进行修改
                    if (sourceList.contains(Const.GROUPNAME_XINWEN) || sourceList.contains(Const.GROUPNAME_WEIBO) || sourceList.contains(Const.GROUPNAME_LUNTAN)) {
                        String[] arr = preciseFilter.split(";");
                        if (arr != null && arr.length > 0) {
                            List<String> preciseFilterList = new ArrayList<>();
                            for (String filter : arr) {
                                preciseFilterList.add(filter);
                            }
                            StringBuffer buffer = new StringBuffer();
                            // 新闻筛选  --- 屏蔽新闻转发 就是新闻 不要新闻不为空的时候，也就是要新闻原发
                            if (sourceList.contains(Const.GROUPNAME_XINWEN) && preciseFilterList.contains("notNewsForward")) {

                                buffer.append("(").append(FtsFieldConst.FIELD_GROUPNAME + ":(" + Const.GROUPNAME_XINWEN + ")");
                                buffer.append(" AND (").append(Const.SRCNAME_XINWEN).append(")");
                                buffer.append(")");
                                sourceList.remove(Const.GROUPNAME_XINWEN);
                            }

                            //论坛筛选  ---  屏蔽论坛主贴  -  为回帖  、屏蔽论坛回帖为主贴
                            if (sourceList.contains(Const.GROUPNAME_LUNTAN) && (preciseFilterList.contains("notLuntanForward") || preciseFilterList.contains("notLuntanPrimary"))) {
                                if (buffer.length() > 0) {
                                    buffer.append(" OR ");
                                }
                                buffer.append("(");
                                buffer.append(FtsFieldConst.FIELD_GROUPNAME + ":(" + Const.GROUPNAME_LUNTAN + ")");
                                if (preciseFilterList.contains("notLuntanForward")) { //屏蔽论坛回帖 -- 主贴
                                    buffer.append(" AND (").append(Const.NRESERVED1_LUNTAN).append(")");
                                }
                                if (preciseFilterList.contains("notLuntanPrimary")) { //屏蔽论坛主贴
                                    buffer.append(" NOT (").append(Const.NRESERVED1_LUNTAN).append(")");
                                }
                                buffer.append(")");

                                sourceList.remove(Const.GROUPNAME_LUNTAN);
                            }

                            //微博筛选  ----  微博筛选时 ，屏蔽微博原发 - 为转发、 屏蔽微博转发 - 为原发
                            if (sourceList.contains(Const.GROUPNAME_WEIBO) && (preciseFilterList.contains("notWeiboForward") || preciseFilterList.contains("notWeiboPrimary")
                                    || preciseFilterList.contains("notWeiboOrgAuthen") || preciseFilterList.contains("notWeiboPeopleAuthen")
                                    || preciseFilterList.contains("notWeiboAuthen") || preciseFilterList.contains("notWeiboLocation")
                                    || preciseFilterList.contains("notWeiboScreenName") || preciseFilterList.contains("notWeiboTopic")
                            )) {

                                if (buffer.length() > 0) {
                                    buffer.append(" OR ");
                                }
                                buffer.append("(");
                                buffer.append(FtsFieldConst.FIELD_GROUPNAME + ":(" + Const.GROUPNAME_WEIBO + ")");
                                if (preciseFilterList.contains("notWeiboForward")) {//屏蔽微博转发
                                    buffer.append(" AND (").append(Const.PRIMARY_WEIBO).append(")");
                                }
                                if (preciseFilterList.contains("notWeiboPrimary")) {//屏蔽微博原发
                                    buffer.append(" NOT (").append(Const.PRIMARY_WEIBO).append(")");
                                }
                                if (preciseFilterList.contains("notWeiboOrgAuthen")) {//屏蔽微博机构认证
                                    buffer.append(" NOT (").append(Const.ORGANIZATION_WEIBO).append(")");
                                }
                                if (preciseFilterList.contains("notWeiboPeopleAuthen")) {//屏蔽微博个人认证
                                    buffer.append(" NOT (").append(Const.PERSON_WEIBO).append(")");
                                }
                                if (preciseFilterList.contains("notWeiboAuthen")) {//屏蔽微博无认证
                                    buffer.append(" NOT (").append(Const.NONE_WEIBO).append(")");
                                }
                                if (StringUtil.isNotEmpty(this.keyWord)) {
                                    net.sf.json.JSONArray jsonArray = net.sf.json.JSONArray.fromObject(this.keyWord.trim());
                                    StringBuilder childTrsl = new StringBuilder();
                                    StringBuilder childTrsl2 = new StringBuilder();
                                    StringBuilder childTrsl3 = new StringBuilder();
                                    for (Object keyWord : jsonArray) {

                                        net.sf.json.JSONObject parseObject = net.sf.json.JSONObject.fromObject(String.valueOf(keyWord));
                                        String keyWordsSingle = parseObject.getString("keyWords");
                                        if (StringUtil.isNotEmpty(keyWordsSingle)) {
                                            //防止关键字以多个 , （逗号）结尾，导致表达式故障问题
                                            String[] split = keyWordsSingle.split(",");
                                            String splitNode = "";
                                            for (int i = 0; i < split.length; i++) {
                                                if (StringUtil.isNotEmpty(split[i])) {
                                                    if (split[i].endsWith(";")) {
                                                        split[i] = split[i].substring(0, split[i].length() - 1);
                                                    }
                                                    splitNode += split[i] + ",";
                                                }
                                            }
                                            keyWordsSingle = splitNode.substring(0, splitNode.length() - 1);
                                            childTrsl.append("((\"")
                                                    .append(keyWordsSingle.replaceAll("[,|，]", "*\") AND (\"").replaceAll("[;|；]+", "*\" OR \""))
                                                    .append("*\"))");
//										childTrsl2.append("((")
//												.append(keyWordsSingle.replaceAll("[,|，]", "*) AND (").replaceAll("[;|；]+", "* OR "))
//												.append("*))");
                                            childTrsl2.append("((")
                                                    .append(keyWordsSingle.replaceAll("[,|，]", ") AND (").replaceAll("[;|；]+", " OR "))
                                                    .append("))");
                                            childTrsl3.append("(").append(keyWordsSingle.replaceAll("[;|；|，|,]"," OR ")).append(")");
                                        }
                                    }
                                    if (preciseFilterList.contains("notWeiboLocation")) {//屏蔽命中微博位置信息
                                        buffer.append(" NOT (").append(FtsFieldConst.FIELD_LOCATION).append(":(").append(childTrsl3.toString()).append(") OR ").append(FtsFieldConst.FIELD_LOCATION_LIKE).append(":(").append(childTrsl3.toString()).append("))");
                                        buffer.append(" NOT (").append(FtsFieldConst.FIELD_RT_LOCATION).append(":(").append(childTrsl3.toString()).append(")) ");
                                    }
                                    if (preciseFilterList.contains("notWeiboScreenName")) {//忽略命中微博博主名
                                        buffer.append(" NOT (").append(FtsFieldConst.FIELD_AUTHORS_LIKE).append(":(").append(childTrsl3.toString()).append("))");
                                        buffer.append(" NOT (").append(FtsFieldConst.FIELD_RETWEETED_FROM_ALL).append(":(").append(childTrsl3.toString()).append(") OR ").append(FtsFieldConst.FIELD_RETWEETED_FROM_ALL_LIKE).append(":(").append(childTrsl3.toString()).append("))");
                                    }
                                    if (preciseFilterList.contains("notWeiboTopic")) {//屏蔽命中微博话题信息
                                        buffer.append(" NOT (").append(FtsFieldConst.FIELD_TAG).append(":(").append(childTrsl2.toString()).append(") OR ").append(FtsFieldConst.FIELD_TAG_LIKE).append(":(").append(childTrsl2.toString()).append("))");
                                    }
                                }
                                buffer.append(")");

                                sourceList.remove(Const.GROUPNAME_WEIBO);
                            }
                            if (buffer.length() > 0) {
                                if (sourceList.size() > 0) {
                                    if (buffer.length() > 0) {
                                        buffer.append(" OR ");
                                    }
                                    buffer.append("(").append(FtsFieldConst.FIELD_GROUPNAME).append(":(").append(StringUtils.join(sourceList, " OR ")).append("))");
                                }
                                queryBuilder.filterByTRSL(buffer.toString());
                            }
                        }
                    }
                }
                break;
            case SPECIAL:
                queryBuilder.filterByTRSL(this.trsl);
                break;
            default:
                break;
        }

        if (pageNo != -1) {
            queryBuilder.setPageNo(pageNo);
            queryBuilder.setPageSize(pageSize);
        }
        if(withTime){
            String[] timeArray = new String[2];
            try {
                timeArray = DateUtil.formatTimeRangeMinus1(this.timeRange);
            } catch (OperationException e) {
                e.printStackTrace();
            }
            queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME, timeArray, Operator.Between);
        }

        return queryBuilder;
    }

    private void addFieldFilter(QueryBuilder queryBuilder, String field, String value, List<String> allValue){
        if(StringUtil.isNotEmpty(value)){
            if(value.contains("其它")){
                value = value.replaceAll("其它","其他");
            }
            String[] valueArr = value.split(";");
            Set<String> valueArrList = new HashSet<>();
            for(String v : valueArr){
                if ("其他".equals(v) || "中性".equals(v)) {
                    valueArrList.add("\"\"");
                }
                if (allValue.contains(v)) {
                    valueArrList.add(v);
                }
            }
            //如果list中有其他，则其他为 其他+“”。依然是算两个
            if (valueArrList.size() > 0 && valueArrList.size() < allValue.size() + 1) {
                queryBuilder.filterField(field, StringUtils.join(valueArrList, " OR "), Operator.Equal);
            }
        }
    }

    private void addAreaFilter( QueryBuilder queryBuilder,String field,String areas){
        Map<String,String> areaMap = null;
        if(FtsFieldConst.FIELD_MEDIA_AREA.equals(field)){
            areaMap = Const.MEDIA_PROVINCE_NAME;
        }else if(FtsFieldConst.FIELD_CATALOG_AREA.equals(field)){
            areaMap = Const.CONTTENT_PROVINCE_NAME;
        }
        if(StringUtil.isNotEmpty(areas) && areaMap!= null){
            if(areas.contains("其它")){
                areas = areas.replaceAll("其它","其他");
            }
            String[] areaArr = areas.split(";");
            Set<String> areaList = new HashSet<>();
            for(String area : areaArr){
                if("其他".equals(area)){
                    areaList.add("\"\"");
                }
                if(areaMap.containsKey(area)){
                    areaList.add(areaMap.get(area));
                }
            }
            //如果list中有其他，则其他为 其他+“”。依然是算两个
            if (areaList.size() > 0 && areaList.size() < areaMap.size() + 1) {
                queryBuilder.filterField(field, StringUtils.join(areaList, " OR "), Operator.Equal);
            }
        }
    }

    /**
     * 增加监测站点
     *
     * @Return : void
     */
    private String addMonitorSite(String monitorSite) {
        if(StringUtil.isNotEmpty(monitorSite)){
            String[] splitArr  = monitorSite.split("[;|；]");
            List<String> list = new ArrayList<>();
            for(String split:splitArr){
                if(StringUtil.isNotEmpty(split)){
                    list.add(split);
                }
            }
            if(list.size()>0){
                return StringUtils.join(list," OR ");
            }else{
                return "";
            }
        }else{
            return "";
        }
    }

}
