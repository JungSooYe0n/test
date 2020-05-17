/*
 * Project: netInsight
 *
 * File Created at 2017年11月24日
 *
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.config.constant;

import com.trs.netInsight.widget.config.entity.HybaseDatabaseConfig;
import com.trs.netInsight.widget.config.service.ISystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Type Const.java
 * @Desc 系统公共常量类
 * @date 2017年11月24日 下午4:07:17
 */
@Component
public class Const {

    /**
     * *************************************************************************************************************************************
     * hybase对应的数据库库名
     */
    /**
     * 插入数据库  默认
     */
    public static final String DEFAULT_INSERT = "system2.entry_180411";
    /**
     * 微信库  默认
     */
    public static final String DEFAULT_WECHAT = "system2.weixin_180325";

    /**
     * hybase传统库  默认
     */
    public static final String DEFAULT_HYBASE_NI_INDEX = "system2.traditionalmedia_180330";

    /**
     * hybase微博库  默认
     */
    public static final String DEFAULT_WEIBO = "system2.weibo_180418";
    /**
     * hybase微博用户库  默认
     */
    public static final String DEFAULT_SINAUSERS = "system2.sinaweibo_users";

    /**
     * FaceBook 和twitter等海外媒体  默认
     */
    public static final String DEFAULT_HYBASE_OVERSEAS = "system2.overseasMedia_180419";


    /**
     * 微信库
     */
    public static String WECHAT = "system2.weixin_180325";//weixin
    /**
     * 插入数据库
     */
    public static String INSERT = "system2.entry_180411";

    /**
     * 微信库(新库,可以进行多表联合查询)
     */
    public static String WECHAT_COMMON = "system2.weixin_180325";
    /**
     * FaceBook 和twitter等海外媒体
     */
    public static String HYBASE_OVERSEAS = "system2.overseasMedia_180419";

    /**
     * hybase传统库
     */
    public static String HYBASE_NI_INDEX = "system2.traditionalmedia_180330";

    /**
     * hybase微博库
     */
    public static String WEIBO = "system2.weibo_180418";
    /**
     * hybase微博用户库
     */
    public static String SINAUSERS = "system2.sinaweibo_users";
    /**
     * 全库混合
     */
    public static String MIX_DATABASE = HYBASE_NI_INDEX + ";" + WECHAT_COMMON + ";" + WEIBO + ";" + HYBASE_OVERSEAS;

    /**
     * es微信库
     */
    public static final String ES_INDEX_WEIXIN = "dc_weixin0711";
    /**
     * es传统库
     */
    public static final String ES_INDEX_CHUANTONG = "dc_chuantong_0913";

    /**
     * es微博库
     */
    public static final String ES_INDEX_WEIBO = "dc_sina_weibo0711";

    /**
     * hybase微博热门评论库
     */
    public static final String SINAREVIEWS = "system2.sinaweibo_reviews";

    /**
     * 返乡日记日记数据
     */
    public static final String HYBASE_HOMECOMING = "system2.homecoming_190225";

    /**
     * 词云库
     */
    public static final String WORDCLOUD_MIX_DATABASE = HYBASE_NI_INDEX;

/**hybase库名区间结束
 * ********************************************************************************************************************************************
 */

    /**
     * hybase数据源类型
     * ********************************************************************************************************************************************
     */
    public static final List<String> MEDIA_TYPE_NEWS = Arrays.asList(
            Const.GROUPNAME_XINWEN,
            Const.GROUPNAME_GUOWAIXINWEN,
            Const.GROUPNAME_DIANZIBAO,
            Const.GROUPNAME_LUNTAN,
            Const.GROUPNAME_BOKE,
            Const.GROUPNAME_KEHUDUAN,
            Const.GROUPNAME_ZIMEITI,
            Const.PAGE_SHOW_XINWEN,
            Const.PAGE_SHOW_GUOWAIXINWEN,
            Const.PAGE_SHOW_LUNTAN,
            Const.PAGE_SHOW_BOKE,
            Const.PAGE_SHOW_DIANZIBAO,
            Const.PAGE_SHOW_KEHUDUAN,
            Const.PAGE_SHOW_ZIMEITI,

            Const.PAGE_SHOW_DUANSHIPIN,
            Const.PAGE_SHOW_CHANGSHIPIN,

            "传统媒体",    "客户端","手机客户端","境外新闻",
            "国外新闻_敏感", "忽翌仟療", "电子报", "客户端", "国外", "境外媒体", "境外网站");


    public static final String ALL_GROUP = Const.GROUPNAME_XINWEN + ";" + Const.GROUPNAME_KEHUDUAN + ";" + Const.GROUPNAME_DIANZIBAO
            + ";" + Const.GROUPNAME_WEIBO + ";" + Const.GROUPNAME_WEIXIN + ";" +
            Const.GROUPNAME_ZIMEITI + ";" + Const.GROUPNAME_LUNTAN + ";" + Const.GROUPNAME_DUANSHIPIN + ";" +
            Const.GROUPNAME_CHANGSHIPIN + ";" + Const.GROUPNAME_BOKE + ";" + Const.GROUPNAME_GUOWAIXINWEN + ";" +
            Const.GROUPNAME_TWITTER + ";" + Const.GROUPNAME_FACEBOOK;
    //"国内新闻;国内论坛;国内博客;国内新闻_电子报;国内新闻_手机客户端;国外新闻;微信;微博;Twitter;FaceBook";

    public static final String ALL_GROUP_COLLECT = Const.GROUPNAME_XINWEN + ";" + Const.GROUPNAME_KEHUDUAN + ";" + Const.GROUPNAME_DIANZIBAO + ";" + Const.GROUPNAME_WEIBO + ";" + Const.GROUPNAME_WEIXIN + ";" +
            Const.GROUPNAME_ZIMEITI + ";" + Const.GROUPNAME_LUNTAN + ";" + Const.GROUPNAME_DUANSHIPIN + ";" + Const.GROUPNAME_CHANGSHIPIN + ";" + Const.GROUPNAME_BOKE + ";" + Const.GROUPNAME_GUOWAIXINWEN + ";" +
            Const.GROUPNAME_TWITTER + ";" + Const.GROUPNAME_FACEBOOK;
    //"国内新闻;国内论坛;国内博客;国内新闻_电子报;国内新闻_手机客户端;国外新闻;微信;国内微信;微博;Twitter;Facebook";

    /**
     * 媒体类型:FaceBook and twitter
     */
    public static final List<String> MEDIA_TYPE_TF = Arrays.asList("FaceBook", "Twitter", "Facebook", "twitter", "facebook");
    /**
     * 文章类型:来源-传统媒体 2018/3/12启用此常量 请慎重修改内容 PS:特别是删除
     */
    public static final List<String> MEDIA_TYPE_FINAL_NEWS = Arrays.asList("国外新闻", "传统媒体", "国内新闻", "新闻", "论坛", "博客",
            "客户端", "电子报", "国内论坛", "国内博客", "国内新闻_电子报", "国内新闻_手机客户端", "境外媒体", "境外网站");

    public static final String TYPE_NEWS = "国内新闻;国内论坛;国内博客;国内新闻_电子报;国内新闻_手机客户端;境外媒体";
    public static final String TYPE_TF = "FaceBook;Twitter";
    /**
     * 时间：2019-11-21
     * 预警中心专家模式。查询传统媒体表达式时，需要查询传统媒体+tf类，所以加了一个
     */
    public static final String TYPE_NEWS_SPECIAL_ALERT = "国内新闻;国内论坛;国内博客;国内新闻_电子报;国内新闻_手机客户端;国外新闻;FaceBook;Twitter";
    /**
     * 文章类型:来源-微博
     */
    public static final List<String> MEDIA_TYPE_WEIBO = Arrays.asList("微博", "国内微博");

    public static final String TYPE_WEIBO = "微博";

    /**
     * 文章类型:来源-微信
     */
    public static final List<String> MEDIA_TYPE_WEIXIN = Arrays.asList("微信", "国内微信");

    public static final String TYPE_WEIXIN = "微信";

    public static final String TYPE_WEIXIN_GROUP = "国内微信";


    /**
     * 分类统计表格所需来源
     */
    public static final String STATTOTAL_GROUP = "国内新闻;国内论坛;国内博客;国内新闻_电子报;国内新闻_手机客户端;国外新闻;国内微信;微博;Twitter;FaceBook";

    /**
     * 分类统计表格所需来源
     */
    public static final String ACTIVE_LEVEL_SITENAME = "国内新闻;国内论坛;国内博客;国内新闻_电子报;国内新闻_手机客户端;国外新闻;国内微信;";
    /**
     * 分类统计表格所需来源
     */
    public static final String ACTIVE_LEVEL_AUTHORS = "微博;Twitter;FaceBook";

/**hybase数据源类型结束
 * ********************************************************************************************************************************************
 */

/**
 * ****************************************************************************************************************
 * 特定hybase检索表达式 - 例如排重、原转发、主回帖等
 */

    /**
     * 按照IR_SIMFLAG排重
     */
    public static final String IR_SIMFLAG_TRSL = FtsFieldConst.FIELD_IR_SIMFLAG + ":(0 OR \"\")";

    /**
     * 按照IR_SIMFLAGALL排重
     */
    public static final String IR_SIMFLAGALL_TRSL = FtsFieldConst.FIELD_IR_SIMFLAGALL + ":(\"0\" OR \"\")";
    /**
     * 微博 按照 IR_RETWEETED_MID=(0 OR "") 判断为原发
     */
    public static final String PRIMARY_WEIBO = FtsFieldConst.FIELD_RETWEETED_MID + ":(0 OR \"\")";

    /**
     * 论坛 按照 IR_NRESERVED1=(0 OR "") 判断为原发
     */
    public static final String NRESERVED1_LUNTAN = FtsFieldConst.FIELD_NRESERVED1 + ":(0 OR \"\")";

    /**
     * 预警标题长度
     */
    public static final int ALERT_NUM = 22;

    /**
     * ****************************************************************************************************************
     * 特定hybase检索表达式结束
     */

    /**
     * *********************************************************************************************************
     * 特殊词汇标识 - 例如一些特殊的情感词或者某个机构的铭敏感词
     */
    /**
     * 排除 IR_KEYWORDS 分类统计结果集中不合适词语
     */
    public static final List<String> NOT_KEYWORD = Arrays.asList("IMAGE", "SRC", "png", "gsp0", "sign", "IMAGE SRC", "com/forum/w",
            "5aAHeD3nKhI2p27j8IqW0jdnxx1xbK/t", "imgsrc", "jpg", "com/tb/editor/images/client/imag", "static", "imgsa", "3D580", "bdstatic", "tb2", "com/tb/editor/images/face/i", "gif", "url", "f25", "com/p", "B0%"
            , "com/tb/editor/images/tsj/t", "com/forum/abpic/item", "/", "com/tb/editor", "img src", "auto-orient/strip/crop", "format/jpg/quality", "gravity/center", "xiaohongshu", "src", "imageMogr2");

    public static final String BIGSCREEN_EMTION = "政府;政府不作为;政府失职;官员腐败;公务员贪污;受贿;民告官;官商勾结;形象工程;索贿;拖欠农民工工资;讨要融资款;暴力拆迁;拆迁补偿不合理;违法;违纪;拆迁安置;违规拆迁;非法占地;房价涨;地价涨;油价涨;涨了还涨;上学难;就业难;买房难;难上加难;和谐社会;河蟹;天价;无语;强征;蒙羞;失职;乱纪;网站空壳;钓鱼执法;干部遭遇;强权;扬言;枉法裁判;强暴征地;如此村官;舞弊;买通政府;收礼钱;政府暴力;政府违法;收礼金;贿选;强制征收;强行征地;政府太欺人;非法霸占;骗取低保;非法开采;政府杀人;执法监察车;百姓被打;干部违纪;领导赚外快;村主任低保;暴力拆迁;乱作为;操控选举;政府网赚钱;官员免职;卫生局领导;不作为;弄虚作假;强行;政府哄骗农民;利用职权;百姓堪忧;强行占用;欺压;贪贿;咋没人管;免职;调离;候选;提名;停职;罢免;革职;下马;辞职;开除;落马;双规;越权;有干头;二奶;贪污;受贿;索贿;行贿;情妇;非正常死亡;自杀;仇杀;情杀;不作为;贿赂;行贿;索贿;受贿;保护费;天价过路费;贪官;贪污;贪腐;雅贿;失察失误;庇护伞;后台;保护伞;官商勾结;包庇;纵容;黑社会;恶势力;黑老大;私设小金库;谋取暴利举报;滥用职权;滥用公权;玩忽职守;失职渎职;违法违纪;违纪违法;损害群众利益;吃空饷;不出警;未出警;不予出警";


    /**
     * 情感  因库里 中性 没标  暂用 “” 空字符串代替中性情感
     */
    public static final List<String> APPRAISE = Arrays.asList("正面", "中性 OR \"\"", "负面");

    public static final List<String> SUB_INDEX = Arrays.asList("dc_chuantong_0913", "dc_sina_weibo0711");

    public static final String DATA_SOURCE = "data-source";
    public static final String USER_ID = "userId";
    public static final String TENANT_ID = "tenantId";

    public static final String DEFAULT_UID = "default-user-id";
    public static final String DATA_SOURCE_ES = "elasticsearch";
    public static final String DATA_SOURCE_HB = "hybase";
    public static final String DATA_SOURCE_HB8 = "hybase8";
    public static final String DEFAULT_TID = "2";
    public static final String ACCOUNT_SCREEN_NAME = "screenName";
    public static final String ACCOUNT_TYPE = "accountType";
    public static final String TRS_SEPARATOR = "#TRS#";

    public static final Integer SUCCESS_INT = 1;
    public static final Integer DOWNLOAD_INT = 2;
    public static final Integer FAIL_INT = -1;

    /**
     * 报告顶部模板
     */
    public static final String TOP_1 = "1";

    /**
     * 地域分布图模板
     */
    public static final String AREA_2 = "2";
    /**
     * 来源类型分析模板
     */
    public static final String SOURCE_3 = "3";
    /**
     * 媒体活跃度模板
     */
    public static final String ACTIVE_4 = "4";
    /**
     * 媒体扩散分析模板
     */
    public static final String DIFFUSE_5 = "5";
    /**
     * 情感分析模板
     */
    public static final String EMOTION_6 = "6";
    /**
     * 热词分布模板
     */
    public static final String HOTWORD_7 = "7";
    /**
     * 热点地名分布模板
     */
    public static final String HOTPLACE_8 = "8";
    /**
     * 热点机构分布模板
     */
    public static final String HOTORGAN_9 = "9";
    /**
     * 热点人名分布模板
     */
    public static final String HOTNAME_10 = "10";
    /**
     * 声量趋势图模板
     */
    public static final String VOLUME_11 = "11";
    /**
     * 引爆点模板
     */
    public static final String BOOM_12 = "12";
    /**
     * 舆情指数刻画模板
     */
    public static final String EXPONENT_13 = "13";
    /**
     * 最热新闻列表模板
     */
    public static final String HOTTEST_14 = "14";
    /**
     * 最新新闻列表模板
     */
    public static final String NEWEST_15 = "15";
    /**
     * 报告简介模板
     */
    public static final String INTRO_16 = "16";
    /**
     * 监测概述模板
     */
    public static final String SUMMARIZE_17 = "17";

    public static final String SUCCESS = "success";

    public static final int REDIS_KEEP_MINUTE = 30;

    /**
     * 内容标红定位显示 长度
     */
    public static final int CONTENT_LENGTH = 200;

    /**
     * 中国省份
     */
    public static final String[] CHINA_AREA = {"北京", "天津", "上海", "重庆", "河北", "山西", "辽宁", "吉林", "黑龙江", "江苏", "浙江", "安徽",
            "福建", "江西", "山东", "河南", "湖北", "湖南", "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "台湾", "内蒙古", "广西", "西藏",
            "宁夏", "新疆", "香港", "澳门"};

    /**
     * 维度模板路径
     */
    @SuppressWarnings("serial")
    public static final Map<String, String> pathMap = new HashMap<String, String>() {
        {
            put(TOP_1, "/TOP.dot");
            put(AREA_2, "/AREA.dot");
            put(SOURCE_3, "/SOURCE.dot");
            put(ACTIVE_4, "/ACTIVE.dot");
            put(DIFFUSE_5, "/DIFFUSE.dot");
            put(EMOTION_6, "/EMOTION.dot");
            put(HOTWORD_7, "/HOTWORD.dot");
            put(HOTPLACE_8, "/HOTPLACE.dot");
            put(HOTORGAN_9, "/HOTORGAN.dot");
            put(HOTNAME_10, "/HOTNAME.dot");
            put(VOLUME_11, "/VOLUME.dot");
            put(BOOM_12, "/BOOM.dot");
            put(EXPONENT_13, "/EXPONENT.dot");
            put(HOTTEST_14, "/HOTTEST.dot");
            put(NEWEST_15, "/NEWEST.dot");
            put(INTRO_16, "/INTRO.dot");
            put(SUMMARIZE_17, "/SUMMARIZE.dot");
        }
    };

    /**
     * 存放模板中不同的小木板对应的key值
     */
    @SuppressWarnings("serial")
    public static final Map<String, String[]> tempKeyMap = new HashMap<String, String[]>() {
        {
            put(TOP_1, new String[]{"organName", "createYear", "createDate"});
            put(AREA_2, new String[]{"territoryTitle", "territoryImage"});
            put(SOURCE_3, new String[]{"sourceTitle", "sourceImage"});
            put(ACTIVE_4, new String[]{"mediaTitle", "mediaImage"});
            put(DIFFUSE_5, new String[]{"mediaDiffuseTitle", "mediaDiffuseImage"});
            put(EMOTION_6, new String[]{"emotionTitle", "emotionImage"});
            put(HOTWORD_7, new String[]{"hotwordTitle", "hotwordImage"});
            put(HOTPLACE_8, new String[]{"hotplaceTitle", "hotplaceImage"});
            put(HOTORGAN_9, new String[]{"hotorganTitle", "hotorganImage"});
            put(HOTNAME_10, new String[]{"hotnameTitle", "hotnameImage"});
            put(VOLUME_11, new String[]{"volumeTitle", "volumeImage"});
            put(BOOM_12, new String[]{"boomTitle", "boomImage"});
            put(EXPONENT_13, new String[]{"exponentTitle", "exponentImage"});
            put(HOTTEST_14, new String[]{"hottestTitle", "hottest", "index", "title", "price"});
            put(NEWEST_15, new String[]{"newestTitle", "newest", "index", "title", "time"});
            put(INTRO_16, new String[]{"libraryName", "keyWords", "time", "count", "siteName", "introTitle"});
            put(SUMMARIZE_17, new String[]{"totalNum", "positiveNum", "positiveRat", "negativeNum", "negativeRat",
                    "sumTitle"});
        }
    };

    /**
     * 参数映射检索词
     */
    @SuppressWarnings("serial")
    public static final Map<String, String> PARAM_MAPPING = new HashMap<String, String>() {
        {
            put("keywords", "IR_KEYWORDS");
            put("people", "CQ_PEOPLE");
            put("location", "CATALOG_AREA1");
            put("weixinlocation", "CATALOG_AREA1");
            put("weibolocation", "CATALOG_AREA1");
            put("agency", "CQ_AGENCY");
            put("news", "国内新闻");
            put("weibo", "微博");
            put("weixin", "微信");
            put("app", "国内新闻_手机客户端");
        }
    };
    /**
     * 参数映射检索词
     */
    @SuppressWarnings("serial")
    public static final Map<String, String> DATE = new HashMap<String, String>() {
        {
            put("keywords", "IR_KEYWORDS");
            put("people", "CQ_PEOPLE");
            put("location", "CATALOG_AREA");
            put("agency", "CQ_AGENCY");
            put("news", "国内新闻");
            put("weibo", "微博");
            put("weixin", "微信");
            put("app", "国内新闻_手机客户端");
        }
    };

    /**
     * 数据来源
     */
   /* public static final Map<String, String> DATA_SOURCES = new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;

        {
            put("国内新闻", Const.GROUPNAME_XINWEN);
            put("新闻", Const.GROUPNAME_XINWEN);
            put("微博", Const.GROUPNAME_WEIBO);
            put("微信", Const.GROUPNAME_WEIXIN);
            put("国内微信", Const.GROUPNAME_WEIXIN);
            put("客户端", Const.GROUPNAME_KEHUDUAN);
            put("手机客户端", Const.GROUPNAME_KEHUDUAN);
            put("国内新闻_手机客户端", Const.GROUPNAME_KEHUDUAN);
            put("论坛", Const.GROUPNAME_LUNTAN);
            put("国内论坛", Const.GROUPNAME_LUNTAN);
            put("博客", Const.GROUPNAME_BOKE);
            put("国内博客", Const.GROUPNAME_BOKE);
            put("电子报", Const.GROUPNAME_DIANZIBAO);
            put("国内新闻_电子报", Const.GROUPNAME_DIANZIBAO);
            put("国外新闻", Const.GROUPNAME_GUOWAIXINWEN);
            put("境外媒体", Const.GROUPNAME_GUOWAIXINWEN);
            put("境外网站", Const.GROUPNAME_GUOWAIXINWEN);
            put("境外新闻", Const.GROUPNAME_GUOWAIXINWEN);
            put("Twitter", Const.GROUPNAME_TWITTER);
            put("twitter", Const.GROUPNAME_TWITTER);
            put("Facebook", Const.GROUPNAME_FACEBOOK);
            put("FaceBook", Const.GROUPNAME_FACEBOOK);
        }
    };*/

    /**
     * 微信网址的配置项
     */
    public static final String NETINSIGHT_URL = "netinsight.url";

    /**
     * 判断关键字里用 ；|;|,|，|(|)|I|R|_|C|O|N|T|E|N|:|U|L|E|A|D
     */
    public static final List<String> EXSTR = Arrays.asList(";", "；", ",", "，", "(", ")", "I", "R", "_", "C", "O", "N", "T", "E", "N", ":", "U", "L", "\"", "A", "D", " ");

    /**
     * 数据源 - 国内新闻
     */
    public static final String GROUPNAME_XINWEN = "国内新闻";

    /**
     * 数据源 - 微博
     */
    public static final String GROUPNAME_WEIBO = "微博";

    /**
     * 数据源 - 国内微信
     */
    public static final String GROUPNAME_WEIXIN = "国内微信";
    /**
     * 数据源 - 自媒体
     */
    public static final String GROUPNAME_ZIMEITI = "自媒体";

    /**
     * 数据源 - 国内论坛
     */
    public static final String GROUPNAME_LUNTAN = "国内论坛";

    /**
     * 数据源 - 国内博客
     */
    public static final String GROUPNAME_BOKE = "国内博客";

    /**
     * 数据源 - 国内新闻_电子报
     */
    public static final String GROUPNAME_DIANZIBAO = "国内新闻_电子报";

    /**
     * 数据源 - 国内新闻_手机客户端
     */
    public static final String GROUPNAME_KEHUDUAN = "国内新闻_手机客户端";
    /**
     * 数据源 - 国外新闻
     */
    public static final String GROUPNAME_GUOWAIXINWEN = "国外新闻";

    /**
     * 数据源 - Facebook
     */
    public static final String GROUPNAME_FACEBOOK = "Facebook";

    /**
     * 数据源 - Twitter
     */
    public static final String GROUPNAME_TWITTER = "Twitter";
    /**
     * 数据源 - 短视频
     */
    public static final String GROUPNAME_DUANSHIPIN = "短视频";
    /**
     * 数据源 - 长视频
     */
    public static final String GROUPNAME_CHANGSHIPIN = "视频";




    /**
     * ---------------------------- 前端页面显示的数据源--------------------------------
     */
    /**
     * sheet名 - 国内新闻
     */
    public static final String PAGE_SHOW_XINWEN = "新闻";

    /**
     * sheet名 - 微博
     */
    public static final String PAGE_SHOW_WEIBO = "微博";

    /**
     * sheet名 - 国内微信
     */
    public static final String PAGE_SHOW_WEIXIN = "微信";

    /**
     * sheet名 - 国内论坛
     */
    public static final String PAGE_SHOW_LUNTAN = "论坛";
    /**
     * sheet名 - 国内论坛
     */
    public static final String PAGE_SHOW_ZIMEITI = "自媒体号";

    /**
     * sheet名 - 国内博客
     */
    public static final String PAGE_SHOW_BOKE = "博客";

    /**
     * sheet名 - 国内新闻_电子报
     */
    public static final String PAGE_SHOW_DIANZIBAO = "电子报";

    /**
     * sheet名 - 国内新闻_手机客户端
     */
    public static final String PAGE_SHOW_KEHUDUAN = "新闻app";
    /**
     * sheet名 - 国外新闻
     */
    public static final String PAGE_SHOW_GUOWAIXINWEN = "境外";

    /**
     * sheet名 - Facebook
     */
    public static final String PAGE_SHOW_FACEBOOK = "Facebook";

    /**
     * sheet名 - Twitter
     */
    public static final String PAGE_SHOW_TWITTER = "Twitter";
    /**
     * sheet名 - 短视频
     */
    public static final String PAGE_SHOW_DUANSHIPIN = "短视频";
    /**
     * sheet名 - 长视频
     */
    public static final String PAGE_SHOW_CHANGSHIPIN = "视频";




    /**
     * groupName 和 页面显示数据源的关系
     *主要是把后端的数据源，转化为与前端显示对应的数据
     *
     * SOURCE_GROUPNAME_CONTRAST
     */
    public static final Map<String, String> PAGE_SHOW_GROUPNAME_CONTRAST = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(GROUPNAME_XINWEN,PAGE_SHOW_XINWEN);
            put(GROUPNAME_WEIBO,PAGE_SHOW_WEIBO);
            put(GROUPNAME_WEIXIN,PAGE_SHOW_WEIXIN);
            put(GROUPNAME_KEHUDUAN,PAGE_SHOW_KEHUDUAN);
            put(GROUPNAME_LUNTAN,PAGE_SHOW_LUNTAN);
            put(GROUPNAME_BOKE,PAGE_SHOW_BOKE);
            put(GROUPNAME_DIANZIBAO,PAGE_SHOW_DIANZIBAO);
            put(GROUPNAME_GUOWAIXINWEN,PAGE_SHOW_GUOWAIXINWEN);
            put(GROUPNAME_ZIMEITI,PAGE_SHOW_ZIMEITI);
            put(GROUPNAME_TWITTER,PAGE_SHOW_TWITTER);
            put("twitter",PAGE_SHOW_TWITTER);
            put(GROUPNAME_FACEBOOK,PAGE_SHOW_FACEBOOK);
            put("FaceBook",PAGE_SHOW_FACEBOOK);
            put(GROUPNAME_DUANSHIPIN,PAGE_SHOW_DUANSHIPIN);
            put(GROUPNAME_CHANGSHIPIN,PAGE_SHOW_CHANGSHIPIN);
        }
    };

    /**
     * source和geoupName的对应关系
     * 主要是为了对前端的显示到后端的转化
     * <p>
     * SOURCE_GROUPNAME_CONTRAST
     */
    public static final Map<String, String> SOURCE_GROUPNAME_CONTRAST = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(GROUPNAME_XINWEN, GROUPNAME_XINWEN);
            put(PAGE_SHOW_XINWEN, GROUPNAME_XINWEN);
            put(GROUPNAME_WEIBO, GROUPNAME_WEIBO);
            put(PAGE_SHOW_WEIXIN, GROUPNAME_WEIXIN);
            put(GROUPNAME_WEIXIN, GROUPNAME_WEIXIN);
            put(PAGE_SHOW_KEHUDUAN, GROUPNAME_KEHUDUAN);
            put("客户端", GROUPNAME_KEHUDUAN);
            put("手机客户端", GROUPNAME_KEHUDUAN);
            put(GROUPNAME_KEHUDUAN, GROUPNAME_KEHUDUAN);
            put(PAGE_SHOW_LUNTAN, GROUPNAME_LUNTAN);
            put(GROUPNAME_LUNTAN, GROUPNAME_LUNTAN);
            put(PAGE_SHOW_BOKE, GROUPNAME_BOKE);
            put(GROUPNAME_BOKE, GROUPNAME_BOKE);
            put(PAGE_SHOW_DIANZIBAO, GROUPNAME_DIANZIBAO);
            put(GROUPNAME_DIANZIBAO, GROUPNAME_DIANZIBAO);
            put(GROUPNAME_GUOWAIXINWEN, GROUPNAME_GUOWAIXINWEN);
            put("境外媒体", GROUPNAME_GUOWAIXINWEN);
            put("境外网站", GROUPNAME_GUOWAIXINWEN);
            put(PAGE_SHOW_GUOWAIXINWEN, GROUPNAME_GUOWAIXINWEN);
            put("境外新闻", GROUPNAME_GUOWAIXINWEN);
            put(GROUPNAME_ZIMEITI, GROUPNAME_ZIMEITI);
            put(PAGE_SHOW_ZIMEITI, GROUPNAME_ZIMEITI);
            put("自媒体号", GROUPNAME_ZIMEITI);
            put(GROUPNAME_TWITTER, GROUPNAME_TWITTER);
            put("twitter", GROUPNAME_TWITTER);
            put(GROUPNAME_FACEBOOK, GROUPNAME_FACEBOOK);
            put(PAGE_SHOW_FACEBOOK, GROUPNAME_FACEBOOK);
            put(GROUPNAME_CHANGSHIPIN, GROUPNAME_CHANGSHIPIN);
            put(PAGE_SHOW_CHANGSHIPIN, GROUPNAME_CHANGSHIPIN);
            put(GROUPNAME_DUANSHIPIN, GROUPNAME_DUANSHIPIN);
            put(PAGE_SHOW_DUANSHIPIN, GROUPNAME_DUANSHIPIN);
        }
    };



    /**
     * 导出需要的判断字段值
     */
    public static final List<String> EXPORT_WEIXIN_SOURCE = Arrays.asList("微信", "国内微信");

    //----------------------------------要导出的sheet的名字-------------------------------------------------
    /**
     * sheet名 - 国内新闻
     */
    public static final String SHEET_XINWEN = "新闻";

    /**
     * sheet名 - 微博
     */
    public static final String SHEET_WEIBO = "微博";

    /**
     * sheet名 - 国内微信
     */
    public static final String SHEET_WEIXIN = "微信";

    /**
     * sheet名 - 国内论坛
     */
    public static final String SHEET_LUNTAN = "论坛";

    /**
     * sheet名 - 国内博客
     */
    public static final String SHEET_BOKE = "博客";

    /**
     * sheet名 - 国内新闻_电子报
     */
    public static final String SHEET_DIANZIBAO = "电子报";

    /**
     * sheet名 - 国内新闻_手机客户端
     */
    public static final String SHEET_KEHUDUAN = "客户端";
    /**
     * sheet名 - 国外新闻
     */
    public static final String SHEET_GUOWAIXINWEN = "境外网站";

    /**
     * sheet名 - Facebook
     */
    public static final String SHEET_FACEBOOK = "Facebook";

    /**
     * sheet名 - Twitter
     */
    public static final String SHEET_TWITTER = "Twitter";

    /**
     * 数据来源  数据的groupName与要导出的excel  的sheet的关系  --->部分groupName对应多个sheet原因是因为数据采集可能出现变动，例如Facebook
     */
    public static final Map<String, String> EXPORT_SOURCE_EXCEL_SHEET = new HashMap<String, String>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put(GROUPNAME_XINWEN, SHEET_XINWEN);
            put(GROUPNAME_WEIBO, SHEET_WEIBO);
            put(GROUPNAME_WEIXIN, SHEET_WEIXIN);
            put("微信", SHEET_WEIXIN);
            put(GROUPNAME_KEHUDUAN, SHEET_KEHUDUAN);
            put(GROUPNAME_LUNTAN, SHEET_LUNTAN);
            put(GROUPNAME_BOKE, SHEET_BOKE);
            put(GROUPNAME_DIANZIBAO, SHEET_DIANZIBAO);
            put(GROUPNAME_GUOWAIXINWEN, SHEET_GUOWAIXINWEN);
            put(GROUPNAME_TWITTER, SHEET_TWITTER);
            put(GROUPNAME_FACEBOOK, SHEET_FACEBOOK);
            put("FaceBook", SHEET_FACEBOOK);
        }
    };
    /**
     * 导出excel的sheet名与数据源的对应关系   -- >与前端传值一一对应，若修改，则需要告知前端
     * <p>
     * FaceBook 与Facebook的对应，有时是小b，主要是查询hybase用
     */
    public static final Map<String, String> EXPORT_SHEET_SOURCE = new HashMap<String, String>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put(SHEET_XINWEN, GROUPNAME_XINWEN);
            put(SHEET_WEIBO, GROUPNAME_WEIBO);
            put(SHEET_WEIXIN, GROUPNAME_WEIXIN);
            put(SHEET_KEHUDUAN, GROUPNAME_KEHUDUAN);
            put(SHEET_LUNTAN, GROUPNAME_LUNTAN);
            put(SHEET_BOKE, GROUPNAME_BOKE);
            put(SHEET_DIANZIBAO, GROUPNAME_DIANZIBAO);
            put(SHEET_GUOWAIXINWEN, GROUPNAME_GUOWAIXINWEN);
            put(SHEET_TWITTER, GROUPNAME_TWITTER);
            put(SHEET_FACEBOOK, GROUPNAME_FACEBOOK);
        }
    };


    /**
     * 数据来源 - 新闻
     */
    public static final Map<String, String> EXPORT_FIRLD_XINWEN = new HashMap<String, String>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put("序号", "序号");
            put("标题", "title");
            put("媒体名称", "siteName");
            put("发布时间", "urlTime");
            put("链接", "urlName");
            put("频道", "channel");
            put("原发网站", "srcName");
            put("作者", "authors");
            put("摘要", "abstracts");
            put("正文", "exportContent");
            put("命中词", "hitWord");
            put("命中句", "hit");
        }
    };
    /**
     * 数据来源 - 微博
     */
    public static final Map<String, String> EXPORT_FIRLD_WEIBO = new HashMap<String, String>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put("序号", "序号");
            put("博主名称", "screenName");
            put("原发博主", "retweetedScreenName");
            put("发布时间", "urlTime");
            put("链接", "urlName");
            put("正文", "exportContent");
            put("转发数", "rttCount");
            put("评论数", "commtCount");
            put("点赞数", "approveCount");
            put("博主粉丝数", "followersCount");
            put("命中词", "hitWord");
            put("命中句", "hit");
        }
    };
    /**
     * 数据来源 - 微信
     */
    public static final Map<String, String> EXPORT_FIRLD_WEIXIN = new HashMap<String, String>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put("序号", "序号");
            put("标题", "urlTitle");
            put("公众号名称", "siteName");
            put("发布时间", "urlTime");
            put("链接", "urlName");
            put("阅读数", "rdcount");
            put("点赞数", "prcount");
            put("正文", "exportContent");
            put("命中词", "hitWord");
            put("命中句", "hit");
        }
    };
    /**
     * 数据来源 - 客户端
     */
    public static final Map<String, String> EXPORT_FIRLD_KEHUDUAN = new HashMap<String, String>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put("序号", "序号");
            put("标题", "title");
            put("媒体名称", "siteName");
            put("发布时间", "urlTime");
            put("链接", "urlName");
            put("频道", "channel");
            put("原发网站", "srcName");
            put("作者", "authors");
            put("摘要", "abstracts");
            put("正文", "exportContent");
            put("命中词", "hitWord");
            put("命中句", "hit");
        }
    };
    /**
     * 数据来源 - 论坛
     */
    public static final Map<String, String> EXPORT_FIRLD_LUNTAN = new HashMap<String, String>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put("序号", "序号");
            put("标题", "title");
            put("发文媒体", "siteName");
            put("发布时间", "urlTime");
            put("链接", "urlName");
            put("楼主", "authors");
            put("频道", "channel");
            put("正文", "exportContent");
            put("命中词", "hitWord");
            put("命中句", "hit");
        }
    };
    /**
     * 数据来源 - 博客
     */
    public static final Map<String, String> EXPORT_FIRLD_BOKE = new HashMap<String, String>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put("序号", "序号");
            put("标题", "title");
            put("发文媒体", "siteName");
            put("发布时间", "urlTime");
            put("链接", "urlName");
            put("作者", "authors");
            put("正文", "exportContent");
            put("命中词", "hitWord");
            put("命中句", "hit");
        }
    };
    /**
     * 数据来源 - 电子报
     */
    public static final Map<String, String> EXPORT_FIRLD_DIANZIBAO = new HashMap<String, String>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put("序号", "序号");
            put("标题", "title");
            put("媒体名称", "siteName");
            put("发布时间", "urlTime");
            put("版面位置", "vreserved");
            put("链接", "urlName");
            put("作者", "authors");
            put("摘要", "abstracts");
            put("正文", "exportContent");
            put("命中词", "hitWord");
            put("命中句", "hit");
        }
    };
    /**
     * 数据来源 - 国外新闻
     */
    public static final Map<String, String> EXPORT_FIRLD_GUOWEIXINWEN = new HashMap<String, String>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put("序号", "序号");
            put("标题", "title");
            put("媒体名称", "siteName");
            put("发布时间", "urlTime");
            put("链接", "urlName");
            put("频道", "channel");
            put("原发网站", "srcName");
            put("作者", "authors");
            put("摘要", "abstracts");
            put("正文", "exportContent");
            put("命中词", "hitWord");
            put("命中句", "hit");
        }
    };
    /**
     * 数据来源 - Twitter
     */
    public static final Map<String, String> EXPORT_FIRLD_TWITTER = new HashMap<String, String>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put("序号", "序号");
            put("作者", "authors");
            put("发布时间", "urlTime");
            put("链接", "urlName");
            put("正文", "exportContent");
            put("转发数", "rttCount");
            put("评论数", "commtCount");
            put("点赞数", "approveCount");
            put("命中词", "hitWord");
            put("命中句", "hit");
        }
    };
    /**
     * 数据来源 - FaceBook
     */
    public static final Map<String, String> EXPORT_FIRLD_FACEBOOK = new HashMap<String, String>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put("序号", "序号");
            put("作者", "authors");
            put("发布时间", "urlTime");
            put("链接", "urlName");
            put("正文", "exportContent");
            put("转发数", "rttCount");
            put("评论数", "commtCount");
            put("点赞数", "approveCount");
            put("命中词", "hitWord");
            put("命中句", "hit");
        }
    };

    /**
     * 数据源-字段对应   -- >与前端传值一一对应，若修改，则需要告知前端
     */
    public static final Map<String, Map<String, String>> EXPORT_EXCEL_SHEET_FIELD = new HashMap<String, Map<String, String>>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            put(SHEET_XINWEN, EXPORT_FIRLD_XINWEN);
            put(SHEET_WEIBO, EXPORT_FIRLD_WEIBO);
            put(SHEET_WEIXIN, EXPORT_FIRLD_WEIXIN);
            put(SHEET_KEHUDUAN, EXPORT_FIRLD_KEHUDUAN);
            put(SHEET_LUNTAN, EXPORT_FIRLD_LUNTAN);
            put(SHEET_BOKE, EXPORT_FIRLD_BOKE);
            put(SHEET_DIANZIBAO, EXPORT_FIRLD_DIANZIBAO);
            put(SHEET_GUOWAIXINWEN, EXPORT_FIRLD_GUOWEIXINWEN);
            put(SHEET_TWITTER, EXPORT_FIRLD_TWITTER);
            put(SHEET_FACEBOOK, EXPORT_FIRLD_FACEBOOK);
        }
    };

    /**
     * 省份 - 和省份全称对应
     */
    public static final Map<String, String> PROVINCE_FULL_NAME = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put("北京", "北京市");
            put("天津", "天津市");
            put("上海", "上海市");
            put("重庆", "重庆市");
            put("河北", "河北省");
            put("山西", "山西省");
            put("辽宁", "辽宁省");
            put("吉林", "吉林省");
            put("黑龙江", "黑龙江省");
            put("江苏", "江苏省");
            put("浙江", "浙江省");
            put("安徽", "安徽省");
            put("福建", "福建省");
            put("江西", "江西省");
            put("山东", "山东省");
            put("河南", "河南省");
            put("湖北", "湖北省");
            put("湖南", "湖南省");
            put("广东", "广东省");
            put("海南", "海南省");
            put("四川", "四川省");
            put("贵州", "贵州省");
            put("云南", "云南省");
            put("陕西", "陕西省");
            put("甘肃", "甘肃省");
            put("青海", "青海省");
            put("台湾", "台湾省");
            put("内蒙古", "内蒙古自治区");
            put("广西", "广西壮族自治区");
            put("西藏", "西藏自治区");
            put("宁夏", "宁夏回族自治区");
            put("新疆", "新疆自治区");// hybase中是这个 正确是新疆维吾尔自治区
            put("香港", "香港特别行政区");
            put("澳门", "澳门特别行政区");
        }
    };
    @Autowired
    private ISystemConfigService scService;
    private static ISystemConfigService systemConfigService;

    @PostConstruct
    public void init() {
        systemConfigService = this.scService;
        HybaseDatabaseConfig hybaseDatabaseConfig = systemConfigService.queryHybaseDatabases();
        HYBASE_NI_INDEX = hybaseDatabaseConfig.getTraditional();
        WEIBO = hybaseDatabaseConfig.getWeibo();
        WECHAT_COMMON = hybaseDatabaseConfig.getWeixin();
        WECHAT = hybaseDatabaseConfig.getWeixin();
        SINAUSERS = hybaseDatabaseConfig.getSinaweiboUsers();
        INSERT = hybaseDatabaseConfig.getInsert();
        HYBASE_OVERSEAS = hybaseDatabaseConfig.getOverseas();
    }

    public static final List<String> PAGE_SHOW_DATASOURCE_SORT = Arrays.asList(Const.PAGE_SHOW_XINWEN,  Const.PAGE_SHOW_KEHUDUAN,Const.PAGE_SHOW_DIANZIBAO,Const.PAGE_SHOW_WEIBO, Const.PAGE_SHOW_WEIXIN,
            Const.PAGE_SHOW_ZIMEITI,Const.PAGE_SHOW_LUNTAN,Const.PAGE_SHOW_DUANSHIPIN,Const.PAGE_SHOW_CHANGSHIPIN, Const.PAGE_SHOW_BOKE,  Const.PAGE_SHOW_GUOWAIXINWEN, Const.PAGE_SHOW_TWITTER, Const.PAGE_SHOW_FACEBOOK);

    public static final List<String> ALL_GROUPNAME_SORT = Arrays.asList(Const.GROUPNAME_XINWEN,  Const.GROUPNAME_KEHUDUAN,Const.GROUPNAME_DIANZIBAO,Const.GROUPNAME_WEIBO, Const.GROUPNAME_WEIXIN,
            Const.GROUPNAME_ZIMEITI,Const.GROUPNAME_LUNTAN,Const.GROUPNAME_DUANSHIPIN,Const.GROUPNAME_CHANGSHIPIN, Const.GROUPNAME_BOKE,  Const.GROUPNAME_GUOWAIXINWEN, Const.GROUPNAME_TWITTER,
            Const.GROUPNAME_FACEBOOK);



}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * <p>
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月24日 Administrator creat
 */