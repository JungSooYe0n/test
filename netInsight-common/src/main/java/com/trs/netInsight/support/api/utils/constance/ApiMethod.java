package com.trs.netInsight.support.api.utils.constance;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统默认api
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年7月3日
 */
@Getter
@AllArgsConstructor
public enum ApiMethod {

    // -------- Column ---------
    /**
     * 获取indexPage列表
     */
    IndexPage(1001, "indexPage list", "1000"),

    /**
     * 获取indexPage详细信息
     */
    IndexPageInfo(1002, "indexPage info", "1000", new String[]{"indexPageId"}),
    /**
     * 获取indexTable列表
     */
    IndexTable(1003, "indexTable list", "1000", new String[]{"indexPageId"}),
    /**
     * 获取indexTable详细信息
     */
    IndexTabInfo(1004, "indexTable info", "1000", new String[]{"indexTabId"}),
    /**
     * 获取indexTable栏目数据
     */
    IndexTabData(1005, "indexTable data", "1000", new String[]{"indexTabId"}),
    /**
     * 获取indexTable列表数据
     */
    IndexTabListData(1006, "indexTable listData", "1000", new String[]{"indexTabId"}),
    /**
     *  获取栏目组，带着导航信息
     */
    IndexPageNavigation(1007, "indexPage list with navigatio", "1000"),
    /**
     * 获取indexTable列表
     */
    IndexTableWithPageName(1008, "indexTable list with pagename", "1000", new String[]{"indexPageId"}),


    // -------- Project --------
    /**
     * 获取专项列表数据
     */
    SpecialSubject(2001, "specialSubject list", "2000"),
    /**
     * 获取专项详细信息
     */
    SpecialSubjectInfo(2002, "specialSubject info", "2000", new String[]{"specialSubjectId"}),
    /**
     * 获取专题列表数据
     */
    SpecialProject(2003, "specialProject list", "2000", new String[]{"specialSubjectId"}),
    /**
     * 获取专题详细信息
     */
    SpecialProjectInfo(2004, "specialProject info", "2000", new String[]{"specialProjectId"}),
    /**
     * 获取专题内模块数据
     */
    SpecialProjectModelData(2005, "specialProject modelData", "2000"),
    /**
     * 获取专题列表树
     */
    SpecialAll(2006, "get all special", "2000"),

    /**
     * 获取专题监测统计表格数据
     */
    SpecialStatTotal(2101, "specialProject statTotal", "2000"),

    /**
     * 获取专题来源数据统计
     */
    SpecialWebCount(2102, "specialProject webCount", "2000"),
    /**
     * 获取专题微博TOP5
     */
    SpecialStatusTop5(2103, "specialProject statusTop5", "2000"),
    /**
     * 获取专题地域分布数据
     */
    SpecialArea(2104, "specialProject area", "2000"),
    /**
     * 媒体活跃等级
     */
    SpecialActiveLevel(2105, "specialProject activeLevel", "2000"),
    /**
     * 获取专题情感分析数据
     */
    SpecialStatusOption(2106, "specialProject statusOption", "2000"),
    /**
     * 获取专题事件溯源数据
     */
    SpecialTrendTime(2107, "specialProject trendTime", "2000"),
    /**
     * 获取专题信息走势图数据
     */
    SpecialTrendMessage(2108, "specialProject trendMessage", "2000"),
    /**
     * 获取专题引爆点数据
     */
    SpecialTippingPoint(2109, "specialProject tippingPoint", "2000"),
    /**
     * 获取专题新闻传播分析数据
     */
    SpecialNewsSiteAnalysis(2110, "specialProject newsSiteAnalysis", "2000"),
    /**
     * 获取专题网友情绪数据
     */
    SpecialUserViews(2111, "specialProject userViews", "2000"),
    /**
     * 获取专题情感走势数据
     */
    SpecialVolume(2112, "specialProject volume", "2000"),
    /**
     * 获取专题词云数据
     */
    SpecialWordCloud(2113, "specialProject wordCloud", "2000"),
    /**
     * 获取专题热词探索数据
     */
    SpecialTopicEvoExplor(2114, "specialProject topicEvoExplor", "2000"),
    /**
     * 获取专题信息列表
     */
    SpecialListInfo(2115, "specialProject list info", "2000"),
    /**
     * 获取态势评估数据
     */
    SituationAssessment(2116, "specialProject situationAssessment", "2000"),
    /**
     * 获取各舆论场趋势分析
     */
    WebCountLine(2117, "specialProject webCountLine", "2000"),
    /**
     * 获取观点分析数据
     */
    SentimentAnalysis(2118, "specialProject sentimentAnalysis", "2000"),
    /**
     * 获取正负面数据
     */
    EmotionOption(2119, "specialProject emotionOption", "2000"),
    /**
     * 获取情绪统计数据
     */
    MoodStatistics(2120, "specialProject moodStatistics", "2000"),
    /**
     * 获取舆论场发布统计数据
     */
    WebCommitCount(2121, "specialProject webCommitCount", "2000"),
    /**
     * 获取舆论场发布统计数据
     */
    HotMessage(2122, "specialProject hotMessage", "2000"),
    /**
     * 获取传播分析站点数据
     */
    SpreadAnalysisSiteName(2123, "specialProject spreadAnalysisSiteName", "2000"),
    /**
     * 获取传播分析站点数据
     */
    ActiveAccount(2124, "specialProject activeAccount", "2000"),

    // -------- Report ----------
    /**
     * 获取报告列表数据
     */
    Report(3001, "report list", "3000"),
    /**
     * 获取报告详细信息
     */
    ReportInfo(3002, "report info", "3000", new String[]{"reportInfo"}),

    // -------- Alert -----------
    /**
     * 预警规则列表
     */
    AlertRule(4001, "alert rule list", "4000"),



    /**
     * 根据条件查询列表，  针对关键词
     */
    SelectData(5001, "select data by keyWords", "5000"),

    /**
     *  根据条件查询列表 ，针对检索表达式
     */
    ExpertSearch(5002,"select data by trsl","5000"),
    /**
     * 高级检索
     */
    AdvancedSearch(5003,"advanced search","5000"),

    /**
     * 获取网察原数据
     */
    getOriginalData(6001, "get original data", "6000"),


    // -------- single microblog --------
    /**
     * 获取所有已分析过的微博
     */
    MicroblogList(7001,"get all single microblog list","7000"),
    /**
     * 获取当前微博博主信息
     */
    BloggerInfo(7002,"get blogger information","7000"),
    /**
     * 获取当前被分析微博信息
     */
    MicroBlogDetail(7003,"get blog information","7000"),
    /**
     * 热门评论
     */
    HotReviews(7004,"get hot reviews TOP5","7000"),
    /**
     * 传播分析
     */
    SpreadAnalysis(7005,"get spread analysis","7000"),
    /**
     * 被转发趋势
     */
    ForwardedTrend(7006,"get forwardedTrend data","7000"),
    /**
     * 传播路径
     */
    SpreadPath(7007,"get spreadPath data","7000"),
    /**
     * 核心转发
     */
    CoreForward(7008,"get coreForward data","7000"),
    /**
     * 意见领袖
     */
    OpinionLeaders(7009,"get opinionLeaders data","7000"),
    /**
     * 转发博主地域分析
     */
    AreaAnalysisOfForWarders(7010,"get areaAnalysisOfForWarders data","7000"),
    /**
     * 转发微博表情分析
     */
    EmojiAnalysisOfForward(7011,"get emojiAnalysisOfForward data","7000"),
    /**
     * 男女占比
     */
    GenderOfRatio(7012,"get genderOfRatio data","7000"),
    /**
     * 认证比例
     */
    CertifiedOfRatio(7013,"get certifiedOfRatio data","7000"),
    /**
     * 博主发文频率
     */
    DispatchFrequency(7014,"get dispatchFrequency data","7000"),
    /**
     * 参与话题统计
     */
    TakeSuperLanguage(7015,"get takeSuperLanguage data","7000"),
    /**
     * 发文情感统计
     */
    EmotionStatistics(7016,"get emotionStatistics data","7000"),
    /**
     * 原发转发占比
     */
    PrimaryForwardRatio(7017,"get primaryForwardRatio data","7000"),


    /**
     *  查询数据详情
     */
    DocumentDetail(8001,"select document detail","8000"),

    // -------- PlatForm --------
    /**
     * 获取用户列表
     */
    UserList(9001, "user list", "9000"),
    /**
     * 获取用户详细信息
     */
    UserInfo(9002, "user info", "9000", new String[]{"userId"}),

    /**
     * 大屏网民情感趋势数据
     */
    NetizensFeelingsCount(10001,"Emotional trend of large screen Internet users","10000"),
    /**
     * 数据中心 --- 微博主贴热点
     */
    HotSpotsOfWeiBo(20001,"Micro-blog hotspots for data center","20000");
    /**
     * api代码
     */
    private int code;

    /**
     * api
     */
    private String name;

    /**
     * 低频
     */
    private String frequencyLow;

    /**
     * 中频
     */
    private String frequencyCommon;

    /**
     * 高频
     */
    private String frequencyHigh;

    /**
     * 粗粒度授权范围
     */
    private String grantTypeCode;

    /**
     * 细粒度权限校验参数集
     */
    private String[] grantParams;

    /**
     * 使用默认频率构造
     *
     * @param code
     * @param name
     */
    private ApiMethod(int code, String name, String grantTypeCode, String[] grantParams) {
        this.code = code;
        this.name = name;
        this.grantTypeCode = grantTypeCode;
        this.grantParams = grantParams;
    }

    /**
     * 使用默认频率构造
     *
     * @param code
     * @param name
     */
    private ApiMethod(int code, String name, String grantTypeCode) {
        this.code = code;
        this.name = name;
        this.grantTypeCode = grantTypeCode;
    }

    /**
     * 根据code获取method
     *
     * @param code
     * @return
     */
    public static ApiMethod findByCode(int code) {
        for (ApiMethod method : ApiMethod.values()) {
            if (code == method.code) {
                return method;
            }
        }
        return null;
    }

}
