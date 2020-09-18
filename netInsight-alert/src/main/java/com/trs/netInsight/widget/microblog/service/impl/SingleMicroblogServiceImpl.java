package com.trs.netInsight.widget.microblog.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.config.constant.ESFieldConst;
import com.trs.netInsight.config.constant.FtsFieldConst;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.exception.TRSSearchException;
import com.trs.netInsight.support.fts.FullTextSearch;
import com.trs.netInsight.support.fts.builder.QueryBuilder;
import com.trs.netInsight.support.fts.builder.condition.Operator;
import com.trs.netInsight.support.fts.entity.FtsDocumentReviews;
import com.trs.netInsight.support.fts.entity.FtsDocumentStatus;
import com.trs.netInsight.support.fts.entity.StatusUser;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.support.fts.model.result.GroupResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.report.excel.ExcelData;
import com.trs.netInsight.support.report.excel.ExcelFactory;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.analysis.service.IDistrictInfoService;
import com.trs.netInsight.widget.microblog.constant.MicroblogConst;
import com.trs.netInsight.widget.microblog.entity.SingleMicroblogData;
import com.trs.netInsight.widget.microblog.entity.SpreadObject;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogDataService;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogService;
import com.trs.netInsight.widget.microblog.task.*;
import com.trs.netInsight.widget.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 单条微博分析业务层接口实现类
 * @author 拓尔思信息技术股份有限公司
 * Created by 拓尔思信息技术股份有限公司 on 2019/1/14.
 * @desc
 */
@Service
@Slf4j
public class SingleMicroblogServiceImpl implements ISingleMicroblogService {

    @Value("${single.microblog.hotReviews.url}")
    private String hotReviewsUrl;

    @Autowired
    private FullTextSearch hybase8SearchService;

    @Autowired
    private IDistrictInfoService districtInfoService;

    @Autowired
    private ISingleMicroblogDataService singleMicroblogDataService;

    @Autowired
    private ISingleMicroblogService singleMicroblogService;

    /**
     * 单拿一个线程计算数据
     * */
    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(15);

    @Override
    public Map<String, Object> judgeData(String urlName) throws TRSException {
        if (StringUtil.isEmpty(urlName)){
            throw new TRSException(CodeUtils.STATUS_URLNAME, "请输入要分析的微博地址！");
        }
        if (urlName.indexOf("?") != -1){
            //有问号
            urlName = urlName.split("\\?")[0];
        }
        urlName = urlName.replace("https","http");
        QueryBuilder queryBuilder = new QueryBuilder();
        //根据urlName查询(按机构允许最大时间查询）
        queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME,DateUtil.formatTimeRange("365d"), Operator.Between);
        queryBuilder.filterField(FtsFieldConst.FIELD_URLNAME,"\""+urlName+"\"",Operator.Equal);
        List<SpreadObject> statuses = hybase8SearchService.ftsQuery(queryBuilder, SpreadObject.class, false, false,false,null);
        if (ObjectUtil.isEmpty(statuses)){
            return null;
        }
        SpreadObject spreadObject = statuses.get(0);
       // SpreadObject spreadObject = currentStatus(urlName);
//        if (ObjectUtil.isEmpty(spreadObject)){
//            return null;
//        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("urlTime",spreadObject.getUrlTime());

        return dataMap;
    }

    @Override
    public String dataAnalysis(String originalUrl,String currentUrl,String random) throws TRSException {
        if (StringUtil.isEmpty(originalUrl)){
            //这块异常怎么处理，还待商榷
            throw new TRSException(CodeUtils.STATUS_URLNAME, "请输入要分析的微博地址！");
        }

        User loginUser = UserUtils.getUser();
        //修改统计时间
        SingleMicroblogData microblogData = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, currentUrl, MicroblogConst.MICROBLOGLIST);
        microblogData.setLastModifiedTime(new Date());
        microblogData = singleMicroblogDataService.save(microblogData);
        //另起线程 查询并存储数据
        //查询热门评论
        fixedThreadPool.execute(new HotReviewsTask(originalUrl,currentUrl,loginUser,microblogData.getRandom()));
        //查询除热门评论以外的模块
       // fixedThreadPool.execute(new MicroblogDataTask(originalUrl,currentUrl,loginUser,microblogData.getRandom()));
        fixedThreadPool.execute(new MicroblogDetailTask(originalUrl,currentUrl,loginUser,microblogData.getRandom()));
        fixedThreadPool.execute(new SpreadAnalysisTask(originalUrl,currentUrl,loginUser,microblogData.getRandom()));
        fixedThreadPool.execute(new ForwardedTrendTask(originalUrl,currentUrl,loginUser,microblogData.getRandom()));
        fixedThreadPool.execute(new SpreadPathTask(originalUrl,currentUrl,loginUser,microblogData.getRandom()));
        fixedThreadPool.execute(new CoreForwardTask(originalUrl,currentUrl,loginUser,microblogData.getRandom()));
        fixedThreadPool.execute(new OpinionLeadersTask(originalUrl,currentUrl,loginUser,microblogData.getRandom()));
        fixedThreadPool.execute(new AreaAnalysisForwardersTask(originalUrl,currentUrl,loginUser,microblogData.getRandom()));
        fixedThreadPool.execute(new EmojiAnalysisForwardTask(originalUrl,currentUrl,loginUser,microblogData.getRandom()));
        fixedThreadPool.execute(new GenderRatioTask(originalUrl,currentUrl,loginUser,microblogData.getRandom()));
        fixedThreadPool.execute(new CertifiedRatioTask(originalUrl,currentUrl,loginUser,microblogData.getRandom()));
        fixedThreadPool.execute(new DispatchFrequencyTask(originalUrl,currentUrl,loginUser,microblogData.getRandom()));
        fixedThreadPool.execute(new TakeSuperLanguageTask(originalUrl,currentUrl,loginUser,microblogData.getRandom()));
        fixedThreadPool.execute(new EmotionStatisticsTask(originalUrl,currentUrl,loginUser,microblogData.getRandom()));
        fixedThreadPool.execute(new PrimaryForwardRatioTask(originalUrl,currentUrl,loginUser,microblogData.getRandom()));
        return currentUrl;
    }

    @Override
    public String confirmStep(String urlName) throws TRSException {
        if (StringUtil.isEmpty(urlName)){
            throw new TRSException(CodeUtils.STATUS_URLNAME, "请输入要分析的微博地址！");
        }
        User loginUser = UserUtils.getUser();
        SingleMicroblogData microblogData = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.MICROBLOGLIST);
        //查看热门评论进度
        SingleMicroblogData hotStep = singleMicroblogDataService.findSMDBySth(loginUser, urlName, MicroblogConst.HOTREVIEWS,microblogData.getRandom());

        //查看所有进度
        List<SingleMicroblogData> allSMD = singleMicroblogDataService.findAllSMDWithRandom(loginUser, urlName,microblogData.getRandom());

        if (allSMD.size() == 16){
            if (ObjectUtil.isNotEmpty(microblogData)){
                microblogData.setState("完成");
            }
            singleMicroblogDataService.save(microblogData);
        }
        return String.valueOf(allSMD.size()-1);
//        if (allSMD.size() == 16){
//            if (ObjectUtil.isNotEmpty(microblogData)){
//                microblogData.setState("完成");
//            }
//            singleMicroblogDataService.save(microblogData);
//            return "finish";
//        }else if (ObjectUtil.isNotEmpty(hotStep)){
//            return "hot";
//        }else {
//            return "other";
//        }
    }

    @Override
    public SpreadObject currentUrlMicroBlog(String urlName) throws TRSException {
        SpreadObject spreadObject = currentStatus(urlName);
        if (ObjectUtil.isNotEmpty(spreadObject)){
            //只需要微博头像信息
            queryStatusUser(spreadObject);
        }
        return spreadObject;
    }

    @Override
    public Map<String, Object> microBlogDetail(String urlName) throws TRSException {
        SpreadObject spreadObject = currentStatus(urlName);
        if (ObjectUtil.isEmpty(spreadObject)){
            return null;
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("current",null);
        dataMap.put("primary",null);

        //加入 用户信息  及  被转载数
        queryForWardNum(spreadObject,spreadObject.getUrlName());
        queryStatusUser(spreadObject);
        if (ObjectUtil.isNotEmpty(spreadObject)){
            dataMap.put("current",spreadObject);
        }

        if ((StringUtil.isNotEmpty(spreadObject.getRetweetedMid()) || !"0".equals(spreadObject.getRetweetedMid())) && StringUtil.isNotEmpty(spreadObject.getRetweetedUrl())){
            //说明该条微博是转发
            //查询该条微博的原发微博
            SpreadObject primaryObject = currentStatus(spreadObject.getRetweetedUrl());
            //加入 用户信息  及  被转载数
            if (ObjectUtil.isNotEmpty(primaryObject)){
                queryForWardNum(primaryObject,primaryObject.getUrlName());
                queryStatusUser(primaryObject);
                if (ObjectUtil.isNotEmpty(spreadObject)){
                    dataMap.put("primary",primaryObject);
                }
            }
        }
        return dataMap;
    }

    @Override
    public Object hotReviews(String urlName) throws TRSException {
        long start = new Date().getTime();
        log.error("热门评论开始:"+start);
        if (StringUtil.isEmpty(urlName)){
            throw new TRSException(CodeUtils.STATUS_URLNAME, "请输入要分析的微博地址！");
        }

        //失败就再次请求，最多请求3次
        JSONObject jsonObject = null;
        for (int i = 0; i < 3; i++) {
            //请求采集接口
            String reviewsPost = hotReviewsPost(urlName);
            if (StringUtil.isNotEmpty(reviewsPost)) {
                jsonObject = JSONObject.parseObject(reviewsPost);
                System.err.println("首轮调采集接口次数："+i);
                log.error("调采集接口次数："+i);
                if ( StringUtil.isNotEmpty(jsonObject.getString("error"))) {
                    log.error("微博链接："+urlName+"热门评论采集入队列出错 errmsg:{}", jsonObject.getString("error"));
                }else {
                    log.info("微博链接："+urlName+"热门评论采集入队列成功 msg:{}", jsonObject.getString("OK"));
                    break;
                }
            }
        }

            //成功  等待3min 查询
            //微博链接成功入采集队列
            //等3min  去库里查询数据
        List<FtsDocumentReviews> ftsDocumentReviews = null;
            try {
                //首次查询需要等3min中去查询
                Thread.sleep(3*1000*60);
                //去hybase查询数据
                 ftsDocumentReviews = searchReviews(urlName);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        long end = new Date().getTime();
        log.error("热门评论结束："+end+"，共用时长："+(end-start));
        return ftsDocumentReviews;
    }

//    @Override
//    public Object microBlogDetail(String urlName, String content, String timeRange) throws TRSException {
//        //默认7天的
//        String[] timeRangeNew = DateUtil.formatTimeRange(timeRange);
//
//        QueryBuilder queryBuilder = new QueryBuilder();
//        queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME,timeRangeNew,Operator.Between);
//        queryBuilder.page(0,Integer.valueOf(timeRange.substring(0,timeRange.length()-1)));
//        //判断根据 urlNmae还是根据content 查询要分析的单条微博
//        if (StringUtil.isEmpty(urlName) && StringUtil.isNotEmpty(content)){
//            //根据内容
//            queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME,true);
//            queryBuilder.filterByTRSL(Const.PRIMARY_WEIBO);
//            content = content.replaceAll(";"," OR ");
//            if (content.endsWith(" OR ")){
//                content = content.substring(0,content.length()-4);
//            }
//            queryBuilder.filterField(FtsFieldConst.FIELD_CONTENT,content,Operator.Equal);
//
//        }else if (StringUtil.isNotEmpty(urlName)){
//            //根据urlName查询，就算content不为空 也忽略，直接查urlName
//            queryBuilder.filterField(FtsFieldConst.FIELD_URLNAME,"\""+urlName+"\"",Operator.Equal);
//        }
//        log.info("详情表达式："+queryBuilder.asTRSL());
//       List<FtsDocumentStatus> ftsDocumentStatuses = hybase8SearchService.ftsQuery(queryBuilder, FtsDocumentStatus.class, false, false);
//        FtsDocumentStatus status = null;
//        if (ObjectUtil.isNotEmpty(ftsDocumentStatuses)){
//            //查询结果中最早的一条微博
//            status = ftsDocumentStatuses.get(0);
//        }
//        return status;
//    }

    @Override
    public Map<String, Object> spreadAnalysis(String urlName) throws TRSException {

        SpreadObject spreadObject = currentStatus(urlName);
        //urlName为空，查询内容不为空时，根据查询内容找到urlName，根据urlName定位微博详情
        if (ObjectUtil.isEmpty(spreadObject)){
            return null;
        }
        //24h
        QueryBuilder hBuilder = new QueryBuilder();
        hBuilder.filterField(FtsFieldConst.FIELD_URLTIME,DateUtil.formatTimeRange("24h"),Operator.Between);
        hBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_URL,"\""+spreadObject.getUrlName()+"\"",Operator.Equal);
       // hBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_FROM,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        hBuilder.setDatabase(Const.WEIBO);
        long hCount = hybase8SearchService.ftsCount(hBuilder, false, false,false,null);
        //近3天
        QueryBuilder tBuilder = new QueryBuilder();
        tBuilder.filterField(FtsFieldConst.FIELD_URLTIME,DateUtil.formatTimeRange("3d"),Operator.Between);
        tBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_URL,"\""+spreadObject.getUrlName()+"\"",Operator.Equal);
       // tBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_FROM,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        tBuilder.setDatabase(Const.WEIBO);
        long tCount = hybase8SearchService.ftsCount(tBuilder, false, false,false,null);
        //近7天
        QueryBuilder sBuilder = new QueryBuilder();
        sBuilder.filterField(FtsFieldConst.FIELD_URLTIME,DateUtil.formatTimeRange("7d"),Operator.Between);
        sBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_URL,"\""+spreadObject.getUrlName()+"\"",Operator.Equal);
       // sBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_FROM,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        sBuilder.setDatabase(Const.WEIBO);
        long sCount = hybase8SearchService.ftsCount(sBuilder, false, false,false,null);
        //截止当前时间被转发总次数
        String[] timeArray = new String[2];
        timeArray[0] = new SimpleDateFormat(DateUtil.yyyyMMddHHmmss).format(spreadObject.getUrlTime());
        timeArray[1] = new SimpleDateFormat(DateUtil.yyyyMMddHHmmss).format(new Date());
        QueryBuilder lBuilder = new QueryBuilder();
        lBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_URL,"\""+spreadObject.getUrlName()+"\"",Operator.Equal);
        sBuilder.filterField(FtsFieldConst.FIELD_URLTIME,timeArray,Operator.Between);
      //  lBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_FROM,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        lBuilder.orderBy(FtsFieldConst.FIELD_URLTIME,true);
        lBuilder.setDatabase(Const.WEIBO);
        long lCount = hybase8SearchService.ftsCount(lBuilder, false, false,false,null);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("today",hCount);
        dataMap.put("threeDay",tCount);
        dataMap.put("sevenDay",sCount);
        dataMap.put("totalCount",lCount);

        return dataMap;
    }

    @Override
    public Map<String, Object> forwardedTrendMap(String urlName) throws TRSException {

        SpreadObject spreadObject = currentStatus(urlName);
        if (ObjectUtil.isEmpty(spreadObject)){
            return null;
        }
        Map<String, Object> dataMap = new HashMap<>();

        //按小时分类统计
        List<String[]> times = new ArrayList<>();
        String[] totalTime = DateUtil.formatTimeRange("7d");
        String firstEnd = totalTime[0].substring(0, 8) + "235959";
         String[] firstTimeArray = {totalTime[0],firstEnd};
         times.add(firstTimeArray);
        List<String> betweenDateString = DateUtil.getBetweenDateString(totalTime[0], totalTime[1], DateUtil.yyyyMMddHHmmss, DateUtil.yyyyMMdd2);
        for (int i = 0; i < betweenDateString.size()-2; i++) {
            String time = betweenDateString.get(i+1);
            String start = time + "000000";
            String end = time + "235959";
            String[] erveryTimes = {start,end};
            times.add(erveryTimes);
        }
        String lastStart = betweenDateString.get(betweenDateString.size()-1)+"000000";
        String[] lastTimeArray = {lastStart,totalTime[1]};
        times.add(lastTimeArray);

        Map<String,Object> mapAll = new HashMap<String,Object>();
        List<String> totalDateList = new ArrayList<>();
        List<Long> totalCountList = new ArrayList<>();
        for (int i = 0; i < times.size(); i++) {
            QueryBuilder hBuilder = new QueryBuilder();
            hBuilder.filterField(FtsFieldConst.FIELD_URLTIME,times.get(i),Operator.Between);
            hBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_URL,"\""+spreadObject.getUrlName()+"\"",Operator.Equal);
            hBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_FROM,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
            hBuilder.page(0,24);
            GroupResult categoryQuery = hybase8SearchService.categoryQuery(hBuilder, false,false, false,FtsFieldConst.FIELD_URLTIME_HOUR, null,Const.WEIBO);
            List<String> dateList = DateUtil.getBetweenDateHourString2(times.get(i)[0], times.get(i)[1]);
            totalDateList.addAll(dateList);
            List<Long> countList = new ArrayList<>();
            Map<String, Long> erveryMap = new LinkedHashMap<>();
            for (String date : dateList) {
                erveryMap.put(date,0L);
            }
            if (ObjectUtil.isNotEmpty(categoryQuery)){
                List<GroupInfo> groupList = categoryQuery.getGroupList();
                if (ObjectUtil.isNotEmpty(groupList) && groupList.size() > 0){
                    for (GroupInfo groupInfo : groupList) {
                        if ("00".equals(groupInfo.getFieldValue())){
                            String s = times.get(i)[0].substring(0, 8) + groupInfo.getFieldValue();//每天的00点要加上具体年月日
                            String dateString = DateUtil.stringToStringDate(times.get(i)[0].substring(0, 8), DateUtil.yyyyMMdd2,DateUtil.yyyyMMdd3);
                            erveryMap.put(dateString,groupInfo.getCount());
                        }else {
                            erveryMap.put(groupInfo.getFieldValue(),groupInfo.getCount());
                        }
                    }
                }
            }
            for (Long aLong : erveryMap.values()) {
                countList.add(aLong);
            }

            totalCountList.addAll(countList);
        }
        mapAll.put("date",totalDateList);
        mapAll.put("count",totalCountList);
        dataMap.put("hour",mapAll);

        //按天分类统计
        QueryBuilder sBuilder = new QueryBuilder();
        //时间段30天
        String[] timeRange = DateUtil.formatTimeRange("30d");
        sBuilder.filterField(FtsFieldConst.FIELD_URLTIME,timeRange,Operator.Between);
        sBuilder.page(0,30);
        sBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_URL,"\""+spreadObject.getUrlName()+"\"",Operator.Equal);
        sBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_FROM,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        sBuilder.setDatabase(Const.WEIBO);
        GroupResult categoryQuery = hybase8SearchService.categoryQuery(sBuilder, false, false,false,FtsFieldConst.FIELD_URLTIME,null, Const.WEIBO);
        List<String> list = DateUtil.getBetweenDateString(timeRange[0], timeRange[1], DateUtil.yyyyMMddHHmmss, DateUtil.yyyyMMdd3);
        Map<String, Long> map = new LinkedHashMap<>();
        for (String date : list) {
            map.put(date,0L);
        }
        if (ObjectUtil.isNotEmpty(categoryQuery)){
            List<GroupInfo> groupList = categoryQuery.getGroupList();
            if (ObjectUtil.isNotEmpty(groupList) && groupList.size() > 0){
                for (GroupInfo groupInfo : groupList) {
                    String stringDate = DateUtil.stringToStringDate(groupInfo.getFieldValue(), DateUtil.yyyyMMdd4, DateUtil.yyyyMMdd3);
                    map.put(stringDate,groupInfo.getCount());
                }
            }
        }
        List<Long> countList = new ArrayList<>();
        Map<String, Object> totalMap = new HashMap<>();
        for (Long aLong : map.values()) {
            countList.add(aLong);
        }
        totalMap.put("date",list);
        totalMap.put("count",countList);
        dataMap.put("day",totalMap);

        return dataMap;
    }

    @Override
    public SpreadObject spreadPath(String urlName) throws TRSException {
        SpreadObject spreadObject = currentStatus(urlName);
        if (ObjectUtil.isEmpty(spreadObject)){
            return null;
        }
        if ((StringUtil.isNotEmpty(spreadObject.getRetweetedMid()) || !"0".equals(spreadObject.getRetweetedMid())) && StringUtil.isNotEmpty(spreadObject.getRetweetedUrl())){
            //以原发作为首个节点
            spreadObject = currentStatus(spreadObject.getRetweetedUrl());
        }
        String retweetedUrl = spreadObject.getUrlName();
        //放入对应 被转发数
        queryForWardNum(spreadObject,retweetedUrl);
        //放入对应 微博用户信息
        queryStatusUser(spreadObject);
        QueryBuilder builder = new QueryBuilder();
        //一级转发
        builder.filterField(FtsFieldConst.FIELD_RETWEETED_URL,"\""+retweetedUrl+"\"",Operator.Equal);
        builder.filterField(FtsFieldConst.FIELD_RETWEETED_FROM,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        builder.page(0,9999);
        List<SpreadObject> spreadObjects = hybase8SearchService.ftsQuery(builder, SpreadObject.class, false, false,false,null);
        if (ObjectUtil.isEmpty(spreadObjects)){
            return spreadObject;
        }
        long start = new Date().getTime();
        System.err.println("开始查询转发数，长度为："+spreadObjects.size()+"，开始时间："+start);
        for (SpreadObject object : spreadObjects) {
            //放入对应微博用户信息及被转发数
            queryForWardNum(object,retweetedUrl);
        }
        long end = new Date().getTime();
        System.err.println("结束转发数查询时间："+end+"，耗时："+(end-start));
        Collections.sort(spreadObjects, new Comparator<SpreadObject>() {
            @Override
            public int compare(SpreadObject o1, SpreadObject o2) {
                return (int) (o2.getForwardedNum()-o1.getForwardedNum());
            }
        });

        if (spreadObjects.size() > 10){
            spreadObjects = spreadObjects.subList(0,10);
        }
        for (SpreadObject object : spreadObjects) {
            //存用户信息
            queryStatusUser(object);
            //二级以及三级
            secondAndThird(object,true,4,retweetedUrl);
        }
        //一级转发
        if (ObjectUtil.isNotEmpty(spreadObjects) && spreadObjects.size() > 10){
            spreadObjects = spreadObjects.subList(0,10);
        }
        //放入转发该微博的微博
        spreadObject.setSubSpreadObjects(spreadObjects);

        return spreadObject;
     }

    @Override
    public SpreadObject spreadPathNew(String urlName) throws TRSException {
        //查询当前urlNmae对应的微博信息
        SpreadObject spreadObject = currentStatus(urlName);
        if (ObjectUtil.isEmpty(spreadObject)){
            return null;
        }
        if ((StringUtil.isNotEmpty(spreadObject.getRetweetedMid()) || !"0".equals(spreadObject.getRetweetedMid())) && StringUtil.isNotEmpty(spreadObject.getRetweetedUrl())){
            //若当前微博非原发，则查出对应原发微博，以原发作为首个节点
            spreadObject = currentStatus(spreadObject.getRetweetedUrl());

        }
        //放入对应 微博用户信息及被转发数
        if (ObjectUtil.isEmpty(spreadObject)){
            return null;
        }
        String retweetedUrl = spreadObject.getUrlName();
        queryForWardNum(spreadObject,retweetedUrl);
        queryStatusUser(spreadObject);
        QueryBuilder builder = new QueryBuilder();
        builder.setDatabase(Const.WEIBO);
        //所有转发
        builder.filterField(FtsFieldConst.FIELD_RETWEETED_URL,"\""+retweetedUrl+"\"",Operator.Equal);
        GroupResult categoryInfos = null;
        try {
            categoryInfos = hybase8SearchService.categoryQuery(false, builder.asTRSL(), false, false,false,
                    FtsFieldConst.FIELD_RETWEETED_FROM_ALL,Integer.MAX_VALUE, null,builder.getDatabase());
        } catch (TRSSearchException e) {
            throw new TRSSearchException(e);
        }

        if (ObjectUtil.isNotEmpty(categoryInfos)){
            StringBuilder sb = new StringBuilder();
            List<GroupInfo> groupList = categoryInfos.getGroupList();

            //控制表达式长度
            int size = groupList.size();
            if (size > 600){
                size = 600;
            }
            for (int i = 0; i < size; i++) {
                if (!spreadObject.getScreenName().equals(groupList.get(i).getFieldValue())){
                    sb.append("\"").append(groupList.get(i).getFieldValue()).append("\"").append(" OR ");
                }

            }
            String screenNames = sb.toString();
            if (screenNames.endsWith(" OR ")){
                screenNames = screenNames.substring(0,screenNames.length()-4);
            }
            //查询一级节点
            List<SpreadObject> spreadObjects = spreadPathNode(spreadObject, groupList, screenNames, 10, retweetedUrl,true,false);
            if (ObjectUtil.isNotEmpty(spreadObjects)){
                //放入一级节点
                spreadObject.setSubSpreadObjects(spreadObjects);
                for (SpreadObject object : spreadObjects) {
                    //查询二级节点
                    List<SpreadObject> objectList = spreadPathNode(object, groupList, screenNames, 4, retweetedUrl,true,false);
                    if (ObjectUtil.isNotEmpty(objectList)){
                        //放入二级节点
                        object.setSubSpreadObjects(objectList);

                        for (SpreadObject spreadObject1 : objectList) {
                            //查询三级
                            List<SpreadObject> objectList1 = spreadPathNode(spreadObject1, groupList, screenNames, 4, retweetedUrl,true,false);
                            if (ObjectUtil.isNotEmpty(spreadObject1)){
                                //放入三级节点
                                spreadObject1.setSubSpreadObjects(objectList1);
                            }
                        }
                    }
                }
            }
        }

        return spreadObject;
    }
    @Override
    public Map<String, Object> coreForward(String urlName) throws TRSException {
        //查询当前urlNmae对应的微博信息
        SpreadObject spreadObject = currentStatus(urlName);
        if (ObjectUtil.isEmpty(spreadObject)){
            return null;
        }

        if ((StringUtil.isNotEmpty(spreadObject.getRetweetedMid()) || !"0".equals(spreadObject.getRetweetedMid())) && StringUtil.isNotEmpty(spreadObject.getRetweetedUrl())){
            //若当前微博非原发，则查出对应原发微博，以原发作为首个节点
            spreadObject = currentStatus(spreadObject.getRetweetedUrl());
        }
        if (ObjectUtil.isEmpty(spreadObject)){
            return null;
        }
        String retweetedUrl = spreadObject.getUrlName();
        QueryBuilder builder = new QueryBuilder();
        builder.setDatabase(Const.WEIBO);
        //所有转发
        builder.filterField(FtsFieldConst.FIELD_RETWEETED_URL,"\""+retweetedUrl+"\"",Operator.Equal);
        GroupResult categoryInfos = null;
        try {
            categoryInfos = hybase8SearchService.categoryQuery(false, builder.asTRSL(), false, false,false,
                    FtsFieldConst.FIELD_RETWEETED_FROM_ALL, Integer.MAX_VALUE, null,builder.getDatabase());
        } catch (TRSSearchException e) {
            throw new TRSSearchException(e);
        }
        Map<String, Object> dataMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(categoryInfos)){
            StringBuilder sb = new StringBuilder();
            List<GroupInfo> groupList = categoryInfos.getGroupList();
            //控制表达式长度
            int size = groupList.size();
            if (size > 600){
                size = 600;
            }
            for (int i = 0; i < size; i++) {
                if (!spreadObject.getScreenName().equals(groupList.get(i).getFieldValue())){
                    sb.append("\"").append(groupList.get(i).getFieldValue()).append("\"").append(" OR ");
                }

            }
            String screenNames = sb.toString();
            if (screenNames.endsWith(" OR ")){
                screenNames = screenNames.substring(0,screenNames.length()-4);
            }
            //查询一级节点
            List<SpreadObject> spreadObjects = spreadPathNode(spreadObject, groupList, screenNames, 10, retweetedUrl,true,true);
            if (ObjectUtil.isNotEmpty(spreadObjects)){
                //放入一级节点
                //spreadObject.setSubSpreadObjects(spreadObjects);
                dataMap.put("first",spreadObjects);

                List<SpreadObject> second = new ArrayList<>();
                List<SpreadObject> third = new ArrayList<>();

                for (SpreadObject object : spreadObjects) {
                    //查询二级节点
                    List<SpreadObject> objectList = spreadPathNode(object, groupList, screenNames, 10, retweetedUrl,true,true);

                    if (ObjectUtil.isNotEmpty(objectList)){
                        //放入二级节点
                       // object.setSubSpreadObjects(objectList);

                        second.addAll(objectList);

                        for (SpreadObject spreadObject1 : objectList) {
                            //查询三级
                            List<SpreadObject> objectList1 = spreadPathNode(spreadObject1, groupList, screenNames, 10, retweetedUrl,true,true);
                            if (ObjectUtil.isNotEmpty(objectList1)){
                                //放入三级节点
                                //spreadObject1.setSubSpreadObjects(objectList1);
                                third.addAll(objectList1);

                            }
                        }
                    }
                }
                if (ObjectUtil.isNotEmpty(second) && second.size() > 10){
                    Collections.sort(second, new Comparator<SpreadObject>() {
                        @Override
                        public int compare(SpreadObject o1, SpreadObject o2) {
                            return (int) (o2.getForwardedNum()-o1.getForwardedNum());
                        }
                    });
                    second = second.subList(0,10);
                }
                dataMap.put("second",second);

                if (ObjectUtil.isNotEmpty(third) && third.size() > 10){
                    Collections.sort(third, new Comparator<SpreadObject>() {
                        @Override
                        public int compare(SpreadObject o1, SpreadObject o2) {
                            return (int) (o2.getForwardedNum()-o1.getForwardedNum());
                        }
                    });
                    third = third.subList(0,10);
                }
                dataMap.put("third",third);
            }
        }

        return dataMap;
    }
//    @Override
//    public Map<String, Object> coreForward(String urlName) throws TRSException {
//        //查询当前urlNmae对应的微博信息
//        SpreadObject spreadObject = currentStatus(urlName);
//        if (ObjectUtil.isEmpty(spreadObject)){
//            return null;
//        }
//        if ((StringUtil.isNotEmpty(spreadObject.getRetweetedMid()) || !"0".equals(spreadObject.getRetweetedMid())) && StringUtil.isNotEmpty(spreadObject.getRetweetedUrl())){
//            //若当前微博非原发，则查出对应原发微博，以原发作为首个节点
//            spreadObject = currentStatus(spreadObject.getRetweetedUrl());
//        }
//        List<SpreadObject> spreadObjects = forWordStatus(spreadObject.getUrlName());
//        if (ObjectUtil.isEmpty(spreadObjects)){
//            return null;
//        }
//        Map<String, Object> dataMap = new HashMap<>();
//        List<SpreadObject> firstSpreads = new ArrayList<>();
//        if (ObjectUtil.isEmpty(spreadObjects)) {
//            return null;
//        }
//        for (SpreadObject spreadObjectFirst : spreadObjects) {
//            queryForWardNum(spreadObjectFirst,urlName);
//        }
//        Collections.sort(spreadObjects, new Comparator<SpreadObject>() {
//            @Override
//            public int compare(SpreadObject o1, SpreadObject o2) {
//                return (int) (o2.getForwardedNum()-o1.getForwardedNum());
//            }
//        });
//        firstSpreads = spreadObjects;
//        if (firstSpreads.size() > 10){
//            firstSpreads = firstSpreads.subList(0,10);
//        }
//        for (SpreadObject firstSpread : firstSpreads) {
//            //放入对应 微博用户信息及被转发数
//           // queryForWardNum(firstSpread,urlName);
//            queryStatusUser(firstSpread);
//        }
//
//
//        dataMap.put("first",firstSpreads);
//        dataMap.put("second",secondOrThird(spreadObjects,false));
//        dataMap.put("third",secondOrThird(spreadObjects,true));
//
//        return dataMap;
//    }

    @Override
    public List<SpreadObject> opinionLeaders(String urlName) throws TRSException {

        //查询当前urlNmae对应的微博信息
        SpreadObject spreadObject = currentStatus(urlName);
        if (ObjectUtil.isEmpty(spreadObject)){
            return null;
        }

        QueryBuilder builder = new QueryBuilder();
        builder.setDatabase(Const.WEIBO);
        //所有转发
        builder.filterField(FtsFieldConst.FIELD_RETWEETED_URL,"\""+spreadObject.getUrlName()+"\"",Operator.Equal);
        GroupResult categoryInfos = null;
        try {
            categoryInfos = hybase8SearchService.categoryQuery(false, builder.asTRSL(), false, false,false,
                    FtsFieldConst.FIELD_RETWEETED_FROM_ALL, Integer.MAX_VALUE,null, builder.getDatabase());
        } catch (TRSSearchException e) {
            throw new TRSSearchException(e);
        }
        if (ObjectUtil.isNotEmpty(categoryInfos)){
            List<GroupInfo> groupList = categoryInfos.getGroupList();
            //groupList = groupList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.getScreenName()))), ArrayList::new));

            List<SpreadObject> dataList = new ArrayList<>();
            //控制表达式长度
            List<List<GroupInfo>> lists = splitListGroup(groupList, 500);
            for (List<GroupInfo> list : lists) {
                StringBuilder screenNames = new StringBuilder();
                for (GroupInfo groupInfo : list) {
                    if (!spreadObject.getScreenName().equals(groupInfo.getFieldValue())){
                        screenNames.append("\"").append(groupInfo.getFieldValue()).append("\"").append(" OR ");
                    }
                }
                String newScreenNames = screenNames.toString();
                if (StringUtil.isNotEmpty(newScreenNames)) {
                    if (newScreenNames.endsWith(" OR ")) {
                        newScreenNames = newScreenNames.substring(0, newScreenNames.length() - 4);
                    }

                    //查询一级节点
                    List<SpreadObject> spreadObjects = spreadPathNode(spreadObject, groupList, newScreenNames, 5, spreadObject.getUrlName(),false,true);
                    dataList.addAll(spreadObjects);
                }

            }

            if (ObjectUtil.isNotEmpty(dataList)){
                Collections.sort(dataList);
                if (dataList.size() > 5){
                    dataList = dataList.subList(0,5);
                }
            }

            return dataList;
        }

        return null;
    }
//    @Override
//    public List<SpreadObject> opinionLeaders(String urlName) throws TRSException {
//
//        //找到被分析微博的一级转发
//        List<SpreadObject> spreadObjects = forWordStatus(urlName);
//        if (ObjectUtil.isEmpty(spreadObjects)){
//            return null;
//        }
//        if (spreadObjects.size() > 50){
//            spreadObjects = spreadObjects.subList(0,50);
//        }
//        for (SpreadObject spreadObject : spreadObjects) {
//            //用户信息 及 被转载数
//            queryForWardNum(spreadObject,urlName);
//            queryStatusUser(spreadObject);
//        }
//        List<SpreadObject> finalSpreads = new ArrayList<>();
//        for (SpreadObject spreadObject : spreadObjects) {
//            if (ObjectUtil.isNotEmpty(spreadObject.getStatusUser()))
//                finalSpreads.add(spreadObject);
//        }
//
//        Collections.sort(finalSpreads);
//
//        if (finalSpreads.size() > 5){
//            finalSpreads  = finalSpreads.subList(0,5);
//        }
//        return finalSpreads;
//    }

    @Override
    public List<Map<String, Object>> areaAnalysisOfForWarders(String urlName) throws TRSException {

        List<SpreadObject> spreadObjects = forWordStatus(urlName);
        if (ObjectUtil.isEmpty(spreadObjects)){
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();

        List<List<SpreadObject>> listList = splitList(spreadObjects, 500);
        List<GroupResult> groupResults = new ArrayList<>();
        for (List<SpreadObject> objects : listList) {
            StringBuilder screenNames = new StringBuilder();
            for (SpreadObject object : objects) {
                screenNames.append("\"").append(object.getScreenName()).append("\"").append(" OR ");
            }
            String newScreenNames = screenNames.toString();
            if (StringUtil.isNotEmpty(newScreenNames)) {
                if (newScreenNames.endsWith(" OR ")){
                    newScreenNames = newScreenNames.substring(0, newScreenNames.length() - 4);
                }

                QueryBuilder queryStatusUser = new QueryBuilder();
                queryStatusUser.filterField(FtsFieldConst.FIELD_SCREEN_NAME, newScreenNames, Operator.Equal);
                queryStatusUser.setDatabase(Const.SINAUSERS);
                //根据地域分类统计

                GroupResult categoryInfos = null;
                try {
                    categoryInfos = hybase8SearchService.categoryQuery(false, queryStatusUser.asTRSL(), false, false,false,
                            FtsFieldConst.FIELD_USER_LOCATION, Integer.MAX_VALUE, null,queryStatusUser.getDatabase());
                    groupResults.add(categoryInfos);
                } catch (TRSSearchException e) {
                    throw new TRSSearchException(e);
                }
            }
        }

        // 获取区域map
        Map<String, List<String>> areaMap = districtInfoService.allAreas();
        for (Map.Entry<String, List<String>> entry : areaMap.entrySet()) {
            Map<String, Object> reMap = new HashMap<String, Object>();
            int num = 0;
            // 查询结果之间相互对比 所以把城市放开也不耽误查询速度
            for (GroupResult groupResult : groupResults) {
                for (GroupInfo classEntry : groupResult) {
                    if (classEntry.getFieldValue().contains(entry.getKey())) {
                        num += classEntry.getCount();
                    }
                }
            }
            List<Map<String, String>> citys = new ArrayList<Map<String, String>>();
            for (String city : entry.getValue()) {
                Map<String, String> cityMap = new HashMap<String, String>();
                int num2 = 0;
                int numJiLin = 0;
                // 因为吉林省市同名,单独拿出,防止按区域名称分类统计错误
                for (GroupResult groupResult : groupResults) {
                    for (GroupInfo classEntry : groupResult) {
                        if (classEntry.getFieldValue().contains(city) && !classEntry.getFieldValue().contains("吉林省 吉林")) {
                            num2 += classEntry.getCount();
                        } else if (classEntry.getFieldValue().contains("吉林省 吉林")) {
                            numJiLin += classEntry.getCount();
                        }
                    }
                }
                // 把.之前的去掉
                String[] citySplit = city.split(".");
                if (citySplit.length > 1) {
                    city = citySplit[citySplit.length - 1];
                }
                cityMap.put("areaName", city);
                cityMap.put("areaCount", String.valueOf(num2));
                if ("吉林".equals(city)) {
                    cityMap.put("areaCount", String.valueOf(numJiLin));
                }
                citys.add(cityMap);

            }
            reMap.put("areaName", entry.getKey());
            reMap.put("areaCount", num);
            reMap.put("citys", citys);
            list.add(reMap);
        }
        List<Map<String, Object>> sortByValue = MapUtil.sortByValue(list, "areaCount");

        return sortByValue;
    }

    @Override
    public List<GroupInfo> emojiAnalysisOfForward(String urlName) throws TRSException {

        SpreadObject spreadObject = currentStatus(urlName);
        if (ObjectUtil.isEmpty(spreadObject)){
            return null;
        }
        String retweetedUrl = spreadObject.getUrlName();

        if (StringUtil.isNotEmpty(spreadObject.getUrlName())) {
            QueryBuilder builder = new QueryBuilder();
            builder.setDatabase(Const.WEIBO);
            builder.filterField(FtsFieldConst.FIELD_RETWEETED_URL, "\"" + retweetedUrl + "\"", Operator.Equal);
            //包含多级
            //builder.filterField(FtsFieldConst.FIELD_RETWEETED_FROM,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
            GroupResult groupInfos = hybase8SearchService.categoryQuery(false, builder.asTRSL(), false, false,false, FtsFieldConst.FIELD_EMOJI, 10,null, builder.getDatabase());

            if (ObjectUtil.isNotEmpty(groupInfos)){
//                List<GroupInfo> groupList = groupInfos.getGroupList();
//                for (GroupInfo groupInfo : groupList) {
//                    String pinyin = PinyinUtil.toPinyinWithPolyphone(groupInfo.getFieldValue());
//                    groupInfo.setFieldValue(pinyin);
//                }
                return groupInfos.getGroupList();
            }
        }
        return null;
    }

    @Override
    public List<Map<String, String>> genderOfRatio(String urlName) throws TRSException {
        List<SpreadObject> spreadObjects = forWordStatus(urlName);
        if (ObjectUtil.isEmpty(spreadObjects)){
            return null;
        }

        List<List<GroupInfo>> dataList = new ArrayList<>();
        List<List<SpreadObject>> listList = splitList(spreadObjects, 500);
        for (List<SpreadObject> objects : listList) {
            StringBuilder screenNames = new StringBuilder();
            for (SpreadObject object : objects) {
                screenNames.append("\"").append(object.getScreenName()).append("\"").append(" OR ");
            }
            String newScreenNames = screenNames.toString();
            if (StringUtil.isNotEmpty(newScreenNames)) {
                if (newScreenNames.endsWith(" OR ")) {
                    newScreenNames = newScreenNames.substring(0, newScreenNames.length() - 4);
                }

                QueryBuilder queryStatusUser = new QueryBuilder();
                queryStatusUser.filterField(FtsFieldConst.FIELD_SCREEN_NAME, newScreenNames, Operator.Equal);
                queryStatusUser.setDatabase(Const.SINAUSERS);
                //根据性别分类统计
                GroupResult categoryInfos = null;
                try {
                    categoryInfos = hybase8SearchService.categoryQuery(false, queryStatusUser.asTRSL(), false, false,false,
                            FtsFieldConst.FIELD_GENDER, 2,null, queryStatusUser.getDatabase());
                } catch (TRSSearchException e) {
                    throw new TRSSearchException(e);
                }
                if (ObjectUtil.isNotEmpty(categoryInfos)){
                    dataList.add(categoryInfos.getGroupList());
                }
            }
        }

        if (ObjectUtil.isEmpty(dataList)){
            return null;
        }

        List<Map<String, String>> list=new ArrayList<>();

        Map<String, String> maleMap = new HashMap<String, String>();
        Map<String, String> femaleMap = new HashMap<String, String>();
        int maleCount = 0;
        int femaleCount = 0;
        maleMap.put("fieldValue", "男");
        maleMap.put("count", "0");
        femaleMap.put("fieldValue", "女");
        femaleMap.put("count", "0");
        for (List<GroupInfo> groupInfos : dataList) {
            for (GroupInfo groupInfo : groupInfos) {
                if ("男".equals(groupInfo.getFieldValue())){
                    maleCount += groupInfo.getCount();
                }else if ("女".equals(groupInfo.getFieldValue())){
                    femaleCount += groupInfo.getCount();
                }
            }

        }
        maleMap.put("fieldValue", "男");
        maleMap.put("count", String.valueOf(maleCount));
        list.add(maleMap);
        femaleMap.put("fieldValue", "女");
        femaleMap.put("count", String.valueOf(femaleCount));
        list.add(femaleMap);


        List<String> keys = new ArrayList<>();
        for (Map<String, String> map : list) {
            Set<String> strings = map.keySet();
            for (String string : strings) {
                if ("fieldValue".equals(string)) {
                    String value = map.get(string);
                    keys.add(value);
                }
            }
        }
        if (!keys.contains("男")) {
            Map<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("fieldValue", "男");
            hashMap.put("count", "0");
            list.add(hashMap);
        }
        if(!keys.contains("女")){
            Map<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("fieldValue", "女");
            hashMap.put("count", "0");
            list.add(hashMap);

        }
        return list;
    }

    @Override
    public List<Map<String, String>> certifiedOfRatio(String urlName) throws TRSException {
        List<SpreadObject> spreadObjects = forWordStatus(urlName);
        if (ObjectUtil.isEmpty(spreadObjects)){
            return null;
        }
        List<Map<String, String>> list=new ArrayList<>();
        List<List<GroupInfo>> dataList = new ArrayList<>();
        List<List<SpreadObject>> listList = splitList(spreadObjects, 500);
        for (List<SpreadObject> objects : listList) {
            StringBuilder screenNames = new StringBuilder();
            for (SpreadObject object : objects) {
                screenNames.append("\"").append(object.getScreenName()).append("\"").append(" OR ");
            }
            String newScreenNames = screenNames.toString();
            if (StringUtil.isNotEmpty(newScreenNames)) {
                if (newScreenNames.endsWith(" OR ")) {
                    newScreenNames = newScreenNames.substring(0, newScreenNames.length() - 4);
                }

                QueryBuilder queryStatusUser = new QueryBuilder();
                queryStatusUser.filterField(FtsFieldConst.FIELD_SCREEN_NAME, newScreenNames, Operator.Equal);
                queryStatusUser.setDatabase(Const.SINAUSERS);
                //根据认证类型分类统计
                GroupResult categoryInfos = null;
                try {
                    categoryInfos = hybase8SearchService.categoryQuery(false, queryStatusUser.asTRSL(), false, false,false,
                            FtsFieldConst.FIELD_VERIFIED, 3,null, queryStatusUser.getDatabase());
                } catch (TRSSearchException e) {
                    throw new TRSSearchException(e);
                }
                if (ObjectUtil.isNotEmpty(categoryInfos)){
                    dataList.add(categoryInfos.getGroupList());
                }
            }
        }

        if (ObjectUtil.isNotEmpty(dataList)){
            Map<String, String> ordinaryMap = new HashMap<String, String>();
            Map<String, String> personalMap = new HashMap<String, String>();
            Map<String, String> organizationMap = new HashMap<String, String>();
            int ordinaryCount = 0;
            int personalCount = 0;
            int organizationCount = 0;
            ordinaryMap.put("fieldValue", "普通用户");
            ordinaryMap.put("count", "0");
            personalMap.put("fieldValue", "新浪个人认证");
            personalMap.put("count", "0");
            organizationMap.put("fieldValue", "新浪机构认证");
            organizationMap.put("count", "0");
            for (List<GroupInfo> groupInfos : dataList) {
                for (GroupInfo groupInfo : groupInfos) {
                    if ("普通用户".equals(groupInfo.getFieldValue())){
                        ordinaryCount += groupInfo.getCount();
                    }else if ("新浪个人认证".equals(groupInfo.getFieldValue())){
                        personalCount += groupInfo.getCount();
                    }else if ("新浪机构认证".equals(groupInfo.getFieldValue())){
                        organizationCount += groupInfo.getCount();
                    }
                }

            }
            ordinaryMap.put("fieldValue", "普通用户");
            ordinaryMap.put("count", String.valueOf(ordinaryCount));
            list.add(ordinaryMap);
            personalMap.put("fieldValue", "新浪个人认证");
            personalMap.put("count", String.valueOf(personalCount));
            list.add(personalMap);
            organizationMap.put("fieldValue", "新浪机构认证");
            organizationMap.put("count", String.valueOf(organizationCount));
            list.add(organizationMap);
        }


        List<String> keys = new ArrayList<>();
        for (Map<String, String> map : list) {
            Set<String> strings = map.keySet();
            for (String string : strings) {
                if ("fieldValue".equals(string)) {
                    String value = map.get(string);
                    keys.add(value);
                }
            }
        }
        if (!keys.contains("普通用户")) {
            Map<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("fieldValue", "普通用户");
            hashMap.put("count", "0");
            list.add(hashMap);
        }
        if(!keys.contains("新浪个人认证")){
            Map<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("fieldValue", "新浪个人认证");
            hashMap.put("count", "0");
            list.add(hashMap);

        }
        if(!keys.contains("新浪机构认证")){
            Map<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("fieldValue", "新浪机构认证");
            hashMap.put("count", "0");
            list.add(hashMap);

        }
        return list;

    }

    @Override
    public Map<String, Object> dispatchFrequency(String urlName) throws TRSException {

        SpreadObject spreadObject = currentStatus(urlName);
        if (ObjectUtil.isEmpty(spreadObject)){
            return null;
        }
        //放入用户信息
        queryStatusUser(spreadObject);
        //24h内
        QueryBuilder hBuilder = new QueryBuilder();
        hBuilder.filterField(FtsFieldConst.FIELD_URLTIME,DateUtil.formatTimeRange("24h"),Operator.Between);
        hBuilder.filterField(FtsFieldConst.FIELD_SCREEN_NAME,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        hBuilder.setDatabase(Const.WEIBO);
        long hCount = hybase8SearchService.ftsCount(hBuilder, false, false,false,null);
        //7天内
        QueryBuilder sBuilder = new QueryBuilder();
        sBuilder.filterField(FtsFieldConst.FIELD_URLTIME,DateUtil.formatTimeRange("7d"),Operator.Between);
        sBuilder.filterField(FtsFieldConst.FIELD_SCREEN_NAME,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        sBuilder.setDatabase(Const.WEIBO);
        long sCount = hybase8SearchService.ftsCount(sBuilder, false, false,false,null);
        //30天内
        QueryBuilder tBuilder = new QueryBuilder();
        tBuilder.filterField(FtsFieldConst.FIELD_URLTIME,DateUtil.formatTimeRange("30d"),Operator.Between);
        tBuilder.filterField(FtsFieldConst.FIELD_SCREEN_NAME,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        tBuilder.setDatabase(Const.WEIBO);
        long tCount = hybase8SearchService.ftsCount(tBuilder, false, false,false,null);

        //最近发文
        QueryBuilder lBuilder = new QueryBuilder();
        lBuilder.filterField(FtsFieldConst.FIELD_SCREEN_NAME,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        lBuilder.orderBy(FtsFieldConst.FIELD_URLTIME,true);
        lBuilder.setDatabase(Const.WEIBO);
        List<SpreadObject> ftsDocumentStatuses = hybase8SearchService.ftsQuery(lBuilder, SpreadObject.class, false, false,false,null);
        Date urlTime = null;
        if (ObjectUtil.isNotEmpty(ftsDocumentStatuses)){
            urlTime = ftsDocumentStatuses.get(0).getUrlTime();
        }

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("statusMessage",spreadObject);
        dataMap.put("today",hCount);
        dataMap.put("sevenDay",sCount);
        dataMap.put("thirtyDay",tCount);
        dataMap.put("lastTime",urlTime);

        return dataMap;
    }

    @Override
    public Map<String, Object> takeSuperLanguage(String urlName) throws TRSException {

        SpreadObject spreadObject = currentStatus(urlName);
        if (ObjectUtil.isEmpty(spreadObject)){
            return null;
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("sevenDay",null);
        dataMap.put("oneMonth",null);
        dataMap.put("threeMonths",null);
        //7天内
        GroupResult groupInfos = everyTakeSuperLanguage(spreadObject, "7d");
        if (ObjectUtil.isNotEmpty(groupInfos)){
            dataMap.put("sevenDay",groupInfos.getGroupList());
        }
        //30天内
        GroupResult groupInfos1 = everyTakeSuperLanguage(spreadObject, "30d");
        if (ObjectUtil.isNotEmpty(groupInfos1)){
            dataMap.put("oneMonth",groupInfos1.getGroupList());
        }
        //近3个月
        GroupResult groupInfos2 = everyTakeSuperLanguage(spreadObject, "90d");
        if (ObjectUtil.isNotEmpty(groupInfos2)){
            dataMap.put("threeMonths",groupInfos2.getGroupList());
        }
        if (ObjectUtil.isEmpty(groupInfos) && ObjectUtil.isEmpty(groupInfos1) && ObjectUtil.isEmpty(groupInfos2)){
            return null;
        }
        return dataMap;
    }

    @Override
    public Map<String, List<Map<String, String>>> emotionStatistics(String urlName) throws TRSException {

        SpreadObject spreadObject = currentStatus(urlName);
        if (ObjectUtil.isEmpty(spreadObject)){
            return null;
        }
        Map<String, List<Map<String, String>>> dataMap = new HashMap<>();
        dataMap.put("sevenDay",null);
        dataMap.put("oneMonth",null);
        dataMap.put("threeMonths",null);
        //近7天
        List<Map<String, String>> sMapData = everyEmotionStatistics(spreadObject, "7d");
        if (ObjectUtil.isNotEmpty(sMapData)){
            dataMap.put("sevenDay",sMapData);
        }
        //近一个月
        List<Map<String, String>> oMapData = everyEmotionStatistics(spreadObject, "30d");
        if (ObjectUtil.isNotEmpty(oMapData)){
            dataMap.put("oneMonth",oMapData);
        }
        //近三个月
        List<Map<String, String>> tMapData = everyEmotionStatistics(spreadObject, "90d");
        if (ObjectUtil.isNotEmpty(tMapData)){
            dataMap.put("threeMonths",tMapData);
        }

        if (ObjectUtil.isEmpty(sMapData) && ObjectUtil.isEmpty(oMapData) && ObjectUtil.isEmpty(tMapData)){
            return null;
        }
        return dataMap;
    }

    @Override
    public Map<String, Map<String, Long>> primaryForwardRatio(String urlName) throws TRSException {

        SpreadObject spreadObject = currentStatus(urlName);
        if (ObjectUtil.isEmpty(spreadObject)){
            return null;
        }
        Map<String, Map<String, Long>> dataMap = new HashMap<>();
        dataMap.put("sevenDay",null);
        dataMap.put("oneMonth",null);
        dataMap.put("threeMonths",null);
        //近7天
        Map<String, Long> sData = everyPrimaryForwardRatio(spreadObject, "7d");
        if (ObjectUtil.isNotEmpty(sData)){
            dataMap.put("sevenDay",sData);
        }
        //近一个月
        Map<String, Long> oData = everyPrimaryForwardRatio(spreadObject, "30d");
        if (ObjectUtil.isNotEmpty(oData)){
            dataMap.put("oneMonth",oData);
        }
        //近7天
        Map<String, Long> nData = everyPrimaryForwardRatio(spreadObject, "90d");
        if (ObjectUtil.isNotEmpty(nData)){
            dataMap.put("threeMonths",nData);
        }
        if (ObjectUtil.isEmpty(sData) && ObjectUtil.isEmpty(oData) && ObjectUtil.isEmpty(nData)){
            return null;
        }
        return dataMap;
    }

    @Override
    public ByteArrayOutputStream exportSingleChart(String dataType,JSONArray array) throws IOException {
        ExcelData content = new ExcelData();
        if (MicroblogConst.EMOJIANALYSISOFFORWARD.equals(dataType)){
            content.setHead(MicroblogConst.HEAD_OF_EMOJI);
        }else if (MicroblogConst.GENDEROFRATIO.equals(dataType)){
            content.setHead(MicroblogConst.HEAD_OF_GENDER);
        }else if (MicroblogConst.CERTIFIEDOFRATIO.equals(dataType)){
            content.setHead(MicroblogConst.HEAD_OF_CERTIFIED);
        }else if (MicroblogConst.EMOTIONSTATISTICS.equals(dataType)){
            content.setHead(MicroblogConst.HEAD_OF_EMOTION);
        }else if (MicroblogConst.PRIMARYFORWARDRATIO.equals(dataType)){
            content.setHead(MicroblogConst.HEAD_OF_PF);
        }else if (MicroblogConst.FORWARDEDTRENDE.equals(dataType)){
            content.setHead(MicroblogConst.HEAD_OF_TREND);
        }

        for (Object object : array) {
            JSONObject parseObject = JSONObject.parseObject(String.valueOf(object));
            //对接口的时候再判断 key,value
            String groupNameValue = "";
            String numValue = "";
            if (MicroblogConst.EMOTIONSTATISTICS.equals(dataType) || MicroblogConst.PRIMARYFORWARDRATIO.equals(dataType)){
                groupNameValue = parseObject.get("name").toString();
                numValue = parseObject.get("value").toString();
            }else if (MicroblogConst.FORWARDEDTRENDE.equals(dataType)){
                groupNameValue = parseObject.get("date").toString();
                numValue = parseObject.get("count").toString();
            }else {
                groupNameValue = parseObject.get("fieldValue").toString();
                numValue = parseObject.get("count").toString();
            }
            content.addRow(groupNameValue, numValue);
        }
        return ExcelFactory.getInstance().export(content);
    }

    @Override
    public ByteArrayOutputStream exportSingleChartLine(JSONArray array) throws IOException {
        ExcelData data = new ExcelData();
        List<String[]> arrayList = new ArrayList<>();
        String[] arrDate = new String[array.size()+1];
        arrDate[0] = "时间";
        //arrayList.add(arrDate);
        String[] arrCount = new String[array.size()+1];
        arrCount[0] = "被转载数";
       // arrayList.add(arrCount);
        for (int i = 0; i < array.size(); i++) {

            JSONObject parseObject = JSONObject.parseObject(String.valueOf(array.get(i)));

            arrDate[i+1] = parseObject.get("date").toString();

            arrCount[i+1] = parseObject.get("count").toString();

        }
        arrayList.add(arrDate);
        arrayList.add(arrCount);

        for (String[] strings : arrayList) {
            data.addRow(strings);
        }
        return ExcelFactory.getInstance().export(data);
    }

    @Override
    public ByteArrayOutputStream exportSingleMap(JSONArray array) throws IOException {
        ExcelData content = new ExcelData();
        content.setHead(MicroblogConst.HEAD_OF_MAP); // { "地域", "信息数量"};
        for (Object object : array) {
            JSONObject parseObject = JSONObject.parseObject(String.valueOf(object));

            String areaName = parseObject.get("areaName").toString();
            String areaCount = parseObject.get("areaCount").toString();
            content.addRow(areaName, areaCount);
        }
        return ExcelFactory.getInstance().export(content);
    }


    @Override
    public Object forwardedTrendMapTest(String urlName, String timeRange) throws OperationException, TRSException {
//        QueryBuilder queryBuilder = new QueryBuilder();
//
//        //默认7天的
//        String[] timeRangeNew = DateUtil.formatTimeRange(timeRange);
//        queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME,timeRangeNew,Operator.Between);
//        queryBuilder.page(0,Integer.valueOf(timeRange.substring(0,timeRange.length()-1)));
//        queryBuilder.setDatabase(Const.WEIBO);
//        queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME,true);
//        queryBuilder.filterByTRSL(Const.PRIMARY_WEIBO);
//
//        queryBuilder.filterField(FtsFieldConst.FIELD_CONTENT,"很感激这一年收获了大家的喜爱。因为有你们的鼓励，我才有了变更好的动力。",Operator.Equal);
//
//        String s1 = queryBuilder.asTRSL();
//        log.info("表达式1："+s1);
        //List<FtsDocumentStatus> ftsDocumentStatuses = hybase8SearchService.ftsQuery(queryBuilder, FtsDocumentStatus.class, false, false);

//        if (StringUtil.isEmpty(urlName) && ObjectUtil.isNotEmpty(ftsDocumentStatuses)){
//            urlName = ftsDocumentStatuses.get(ftsDocumentStatuses.size()-1).getUrlName();
//        }
//        StringBuilder sb = new StringBuilder();
//        sb.append("\"").append(urlName).append("\"");
        QueryBuilder builder = new QueryBuilder();
        String[] timeRangeNew = DateUtil.formatTimeRange(timeRange);
       // builder.filterField(FtsFieldConst.FIELD_URLTIME,timeRangeNew,Operator.Between);
      //  builder.page(0,Integer.valueOf(timeRange.substring(0,timeRange.length()-1)));
        builder.page(0,5050);
        builder.setDatabase(Const.WEIBO);
        builder.filterField(FtsFieldConst.FIELD_RETWEETED_URL,"\""+urlName+"\"",Operator.Equal);
        String s = builder.asTRSL();
        List<FtsDocumentStatus> ftsDocumentStatuses = hybase8SearchService.ftsQuery(builder, FtsDocumentStatus.class, false, false,false,null);
        log.info("转发表达式："+s);
        GroupResult categoryQuery = null;
//        if (timeRange.contains("h")){
//            categoryQuery = hybase8SearchService.categoryQuery(builder, false, false,FtsFieldConst.FIELD_URLTIME_HOUR, Const.WEIBO);
//        }else {
//            categoryQuery = hybase8SearchService.categoryQuery(builder, false, false,FtsFieldConst.FIELD_URLTIME, Const.WEIBO);
//        }

        //return categoryQuery.getGroupList();
        return ftsDocumentStatuses;


        //        QueryBuilder queryBuilder = new QueryBuilder();
//        queryBuilder.filterField(FtsFieldConst.FIELD_URLTIME,timeRangeNew,Operator.Between);
//        queryBuilder.setDatabase(Const.WEIBO);
//        queryBuilder.orderBy(FtsFieldConst.FIELD_URLTIME,true);
//        queryBuilder.filterByTRSL(Const.PRIMARY_WEIBO);
//        if (StringUtil.isNotEmpty(content)){
//            content = content.replaceAll(";"," OR ");
//            if (content.endsWith(" OR ")){
//                content = content.substring(0,content.length()-4);
//            }
//            queryBuilder.filterField(FtsFieldConst.FIELD_CONTENT,content,Operator.Equal);
//        }
//        String s1 = queryBuilder.asTRSL();
//        log.info("表达式1："+s1);
//        List<FtsDocumentStatus> ftsDocumentStatuses = hybase8SearchService.ftsQuery(queryBuilder, FtsDocumentStatus.class, false, false);
//
//        if (StringUtil.isEmpty(urlName) && ObjectUtil.isNotEmpty(ftsDocumentStatuses)){
//            urlName = ftsDocumentStatuses.get(ftsDocumentStatuses.size()-1).getUrlName();
//        }
//        QueryBuilder builder = new QueryBuilder();
//        builder.filterField(FtsFieldConst.FIELD_URLTIME,timeRangeNew,Operator.Between);
//        builder.page(0,Integer.valueOf(timeRange.substring(0,timeRange.length()-1)));
//
//        String s = builder.asTRSL();
//        log.info("表达式："+s);
//        GroupResult categoryQuery = null;
//        if (timeRange.contains("h")){
//            categoryQuery = hybase8SearchService.categoryQuery(builder, false, false,FtsFieldConst.FIELD_URLTIME_HOUR, Const.WEIBO);
//        }else {
//            categoryQuery = hybase8SearchService.categoryQuery(builder, false, false,FtsFieldConst.FIELD_URLTIME, Const.WEIBO);
//        }

        //近3天
//        if (ObjectUtil.isNotEmpty(categoryQuery)){
//            List<GroupInfo> groupList = categoryQuery.getGroupList();
//            if (ObjectUtil.isNotEmpty(groupList)){
//                for (GroupInfo groupInfo : groupList) {
//
//                }
//            }
//        }
        //       return categoryQuery.getGroupList();
    }

    @Override
    public Object testStatusData(String urlName,boolean first) throws TRSException {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.filterField(FtsFieldConst.FIELD_URLNAME,"\""+urlName+"\"",Operator.Equal);
        queryBuilder.setDatabase(Const.WEIBO);
        List<SpreadObject> statuses = hybase8SearchService.ftsQuery(queryBuilder, SpreadObject.class, false, false,false,null);
        if (ObjectUtil.isEmpty(statuses)){
            return null;
        }
        SpreadObject spreadObject = statuses.get(0);
        //放入对应 微博用户信息 及被转发数
        String retweetedUrl = spreadObject.getUrlName();
        queryForWardNum(spreadObject,retweetedUrl);
        queryStatusUser(spreadObject);

        if (StringUtil.isNotEmpty(spreadObject.getUrlName())){
            QueryBuilder builder = new QueryBuilder();
            builder.filterField(FtsFieldConst.FIELD_RETWEETED_URL,"\""+retweetedUrl+"\"",Operator.Equal);
            List<SpreadObject> spreadObjects = hybase8SearchService.ftsQuery(builder, SpreadObject.class, false, false,false,null);
            if (ObjectUtil.isEmpty(spreadObjects)){
                return spreadObject;
            }
            if (first){
                if (ObjectUtil.isNotEmpty(spreadObjects) && spreadObjects.size() > 10){
                    spreadObjects = spreadObjects.subList(0,10);
                }
            }else {
                if (ObjectUtil.isNotEmpty(spreadObjects) && spreadObjects.size() > 4){
                    spreadObjects = spreadObjects.subList(0,4);
                }
            }
            //放入对应微博用户信息及被转发数
            for (SpreadObject object : spreadObjects) {
                queryForWardNum(object,retweetedUrl);
                queryStatusUser(object);
                secondAndThird(object,true,4,retweetedUrl);
            }
            //放入转发该微博的微博
            spreadObject.setSubSpreadObjects(spreadObjects);
            return spreadObject;
        }
        return null;
    }

    @Override
    public List<StatusUser> testSinaUser(String urlName) throws TRSException{
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.filterField(FtsFieldConst.FIELD_URLNAME,"\""+urlName+"\"",Operator.Equal);
        log.info("urlName表达式："+queryBuilder.asTRSL());
        List<SpreadObject> spreadObjects = hybase8SearchService.ftsQuery(queryBuilder, SpreadObject.class, false, false,false,null);
        if (ObjectUtil.isEmpty(spreadObjects)){
            log.info("urlName没有");
            return null;
        }
        SpreadObject spreadObject = spreadObjects.get(0);
        QueryBuilder builder = new QueryBuilder();
        builder.filterField(FtsFieldConst.FIELD_HKEY,spreadObject.getHKey(),Operator.Equal);
      //  log.info("hkey表达式："+builder.asTRSL());

      //  List<StatusUser> statusUsers = hybase8SearchService.ftsQuery(builder, StatusUser.class, false, false);


       // if (ObjectUtil.isEmpty(statusUsers)){
           // log.info("hkey没有");
            QueryBuilder builder1 = new QueryBuilder();
            builder1.filterField(FtsFieldConst.FIELD_SID,spreadObject.getSid(),Operator.Equal);
            //log.info("sid表达式："+builder.asTRSL());
           // statusUsers = hybase8SearchService.ftsQuery(builder1, StatusUser.class, false, false);
           // if (ObjectUtil.isEmpty(statusUsers)){
              //  log.info("sid没有");
                QueryBuilder builder2 = new QueryBuilder();
                builder2.filterField(FtsFieldConst.FIELD_SCREEN_NAME,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
                log.info("screenName表达式："+builder2.asTRSL());
        List<StatusUser> statusUsers = hybase8SearchService.ftsQuery(builder2, StatusUser.class, false, false,false,null);
          //  }
        //}
        return statusUsers;
    }

    /**
     * 转发被分析微博的微博
     * @param urlName
     * @return
     * @throws TRSException
     */
    private List<SpreadObject> forWordStatus(String urlName) throws TRSException{
        if (StringUtil.isEmpty(urlName)){
            throw new TRSException(CodeUtils.STATUS_URLNAME, "请输入要分析的微博地址！");
        }
        if (urlName.indexOf("?") != -1){
            //有问号
            urlName = urlName.split("\\?")[0];
        }
        urlName = urlName.replace("https","http");
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.filterField(FtsFieldConst.FIELD_URLNAME,"\""+urlName+"\"",Operator.Equal);
        queryBuilder.setDatabase(Const.WEIBO);
        List<SpreadObject> statuses = hybase8SearchService.ftsQuery(queryBuilder, SpreadObject.class, false, false,false,null);
        if (ObjectUtil.isEmpty(statuses)){
            return null;
        }
        SpreadObject spreadObject = statuses.get(0);
        //放入对应 微博用户信息及被转发数
        String retweetedUrl = spreadObject.getUrlName();
        queryForWardNum(spreadObject,retweetedUrl);
        queryStatusUser(spreadObject);

        if (StringUtil.isNotEmpty(spreadObject.getUrlName())) {
            QueryBuilder builder = new QueryBuilder();
            builder.filterField(FtsFieldConst.FIELD_RETWEETED_URL, "\"" + retweetedUrl + "\"", Operator.Equal);
            //builder.filterField(FtsFieldConst.FIELD_RETWEETED_FROM,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
            builder.page(0,9999);
            List<SpreadObject> spreadObjects = hybase8SearchService.ftsQuery(builder, SpreadObject.class, false, false,false,null);
            if (ObjectUtil.isNotEmpty(spreadObjects)){
                return spreadObjects;
            }
        }

        return null;
    }
    /**
     * 查询 微博转发的转发 即二级转发及三级 供传播路径使用
     * @param spreadObject
     * @param second
     * @throws TRSException
     */
    private void secondAndThird(SpreadObject spreadObject,boolean second,int node,String retweetedUrl) throws TRSException {

        QueryBuilder builder = new QueryBuilder();
        builder.filterField(FtsFieldConst.FIELD_RETWEETED_URL, "\"" + retweetedUrl + "\"", Operator.Equal);
        builder.filterField(FtsFieldConst.FIELD_RETWEETED_FROM, "\"" + spreadObject.getScreenName() + "\"", Operator.Equal);
        builder.filterField(FtsFieldConst.FIELD_SCREEN_NAME, "\"" + spreadObject.getScreenName() + "\"", Operator.NotEqual);
        //二级
        List<SpreadObject> spreadObjects = hybase8SearchService.ftsQuery(builder, SpreadObject.class, false, false,false,null);
        if (ObjectUtil.isNotEmpty(spreadObjects)) {

            //放入二级
            if (ObjectUtil.isNotEmpty(spreadObjects)){

                for (SpreadObject object : spreadObjects) {
                    //放入对应微博的被转发数
                    queryForWardNum(object,retweetedUrl);
                }
                Collections.sort(spreadObjects, new Comparator<SpreadObject>() {
                    @Override
                    public int compare(SpreadObject o1, SpreadObject o2) {
                        return (int) (o2.getForwardedNum()-o1.getForwardedNum());
                    }
                });

                //取4个节点
                if (spreadObjects.size() > node) {
                    spreadObjects = spreadObjects.subList(0, node);
                }

                spreadObject.setSubSpreadObjects(spreadObjects);


                for (SpreadObject finalSpread : spreadObjects) {
                    //放入微博对应用户信息
                    queryStatusUser(finalSpread);
                    if (second){
                        //二级进入查询第三级数据
                        secondAndThird(finalSpread,false,4,retweetedUrl);
                    }
                }

            }
        }
    }

    /**
     * 计算二级或者三级转发 供核心转发使用
     * @param spreadObjects
     * @param third
     * @return
     * @throws TRSException
     */
    private List<SpreadObject> secondOrThird(List<SpreadObject> spreadObjects,boolean third) throws TRSException {
        if (ObjectUtil.isNotEmpty(spreadObjects)){
            String retweetedUrl = spreadObjects.get(0).getRetweetedUrl();
            List<SpreadObject> dataList = new ArrayList<>();
            for (SpreadObject object : spreadObjects) {
                QueryBuilder builder = new QueryBuilder();
                builder.filterField(FtsFieldConst.FIELD_RETWEETED_FROM, "\""+object.getScreenName()+"\"", Operator.Equal);
                builder.filterField(FtsFieldConst.FIELD_SCREEN_NAME, "\""+object.getScreenName()+"\"", Operator.NotEqual);
                builder.filterField(FtsFieldConst.FIELD_RETWEETED_URL, "\""+retweetedUrl+"\"", Operator.Equal);
                //二级
                List<SpreadObject> nodesSpread = hybase8SearchService.ftsQuery(builder, SpreadObject.class, false, false,false,null);
                if (ObjectUtil.isNotEmpty(nodesSpread)){

                    dataList.addAll(nodesSpread);
                }

            }

            if (ObjectUtil.isNotEmpty(dataList)){
                for (SpreadObject spreadObject : dataList) {
                    //放入对应微博的被转发数
                    queryForWardNum(spreadObject,retweetedUrl);
                }
                Collections.sort(dataList, new Comparator<SpreadObject>() {
                    @Override
                    public int compare(SpreadObject o1, SpreadObject o2) {
                        return (int) (o2.getForwardedNum()-o1.getForwardedNum());
                    }
                });
                List<SpreadObject> finalDataList = new ArrayList<>();
                finalDataList = dataList;
                if (finalDataList.size() > 10){
                    finalDataList = finalDataList.subList(0,10);
                }
                for (SpreadObject object : finalDataList) {
                    //放入对应微博用户信息 及对应被转发数
                  //  queryForWardNum(object,retweetedUrl);
                    queryStatusUser(object);
                }
                if (third){
                    List<SpreadObject> thirdSpreads = secondOrThird(dataList, false);
                    return thirdSpreads;
                }
                return finalDataList;
            }

        }
        return null;
    }

    /**
     * 查询 对应用户信息 及 被转发数
     * @param spreadObject
     * @param retweetedUrl
     * @throws TRSException
     */
    private void queryForWardNum(SpreadObject spreadObject,String retweetedUrl) throws TRSException{

        if (retweetedUrl.indexOf("?") != -1){
            //有问号
            retweetedUrl = retweetedUrl.split("\\?")[0];
        }
        retweetedUrl = retweetedUrl.replace("https","http");
        //计算被转发数
        QueryBuilder builder = new QueryBuilder();
        builder.filterField(FtsFieldConst.FIELD_RETWEETED_URL, "\""+retweetedUrl+"\"", Operator.Equal);
        builder.filterField(FtsFieldConst.FIELD_RETWEETED_FROM,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        //二级
        long spreadCount = hybase8SearchService.ftsCount(builder, SpreadObject.class,false, false, false,null);
        spreadObject.setForwardedNum(spreadCount);
    }

    /**
     * 当前文章对应的用户信息
     * @param spreadObject
     * @throws TRSException
     */
    private void queryStatusUser(SpreadObject spreadObject) throws TRSException{
        QueryBuilder queryStatusUser = new QueryBuilder();
        queryStatusUser.filterField(FtsFieldConst.FIELD_SCREEN_NAME,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        queryStatusUser.setDatabase(Const.SINAUSERS);
        //查询微博用户信息
        List<StatusUser> statusUsers = hybase8SearchService.ftsQuery(queryStatusUser, StatusUser.class, false, false,false,null);
        if (ObjectUtil.isEmpty(statusUsers)){
            QueryBuilder queryStatusUser1 = new QueryBuilder();
            queryStatusUser1.filterField(FtsFieldConst.FIELD_UID,"\""+spreadObject.getUid()+"\"",Operator.Equal);
            queryStatusUser1.setDatabase(Const.SINAUSERS);
            //查询微博用户信息
            statusUsers = hybase8SearchService.ftsQuery(queryStatusUser1, StatusUser.class, false, false,false,null);
        }
        if (ObjectUtil.isNotEmpty(statusUsers)){
            //放入该条微博对应的 发布人信息
            spreadObject.setStatusUser(statusUsers.get(0));
        }
    }

    /**
     * 微博评论人信息
     * @param ftsDocumentReviews
     * @throws TRSException
     */
    private void queryStatusUser(FtsDocumentReviews ftsDocumentReviews) throws TRSException{
        QueryBuilder queryStatusUser = new QueryBuilder();
        queryStatusUser.filterField(FtsFieldConst.FIELD_SCREEN_NAME,"\""+ftsDocumentReviews.getAuthors()+"\"",Operator.Equal);
        queryStatusUser.setDatabase(Const.SINAUSERS);
        //查询微博用户信息
        List<StatusUser> statusUsers = hybase8SearchService.ftsQuery(queryStatusUser, StatusUser.class, false, false,false,null);
//        if (ObjectUtil.isEmpty(statusUsers)){
//            QueryBuilder queryStatusUser1 = new QueryBuilder();
//            queryStatusUser1.filterField(FtsFieldConst.FIELD_UID,"\""+spreadObject.getUid()+"\"",Operator.Equal);
//            queryStatusUser1.setDatabase(Const.SINAUSERS);
//            //查询微博用户信息
//            statusUsers = hybase8SearchService.ftsQuery(queryStatusUser1, StatusUser.class, false, false);
//        }
        if (ObjectUtil.isNotEmpty(statusUsers)){
            //放入该条微博对应的 发布人信息
            ftsDocumentReviews.setStatusUser(statusUsers.get(0));
        }
    }
    private SpreadObject currentStatus(String urlName) throws TRSException{
        if (StringUtil.isEmpty(urlName)){
            throw new TRSException(CodeUtils.STATUS_URLNAME, "请输入要分析的微博地址！");
        }
        if (urlName.indexOf("?") != -1){
            //有问号
            urlName = urlName.split("\\?")[0];
        }
        urlName = urlName.replace("https","http");
        QueryBuilder queryBuilder = new QueryBuilder();
        //根据urlName查询
        queryBuilder.filterField(FtsFieldConst.FIELD_URLNAME,"\""+urlName+"\"",Operator.Equal);
        List<SpreadObject> statuses = hybase8SearchService.ftsQuery(queryBuilder, SpreadObject.class, false, false,false,null);
        if (ObjectUtil.isEmpty(statuses)){
            return null;
        }
        return statuses.get(0);
    }

    private List<SpreadObject> spreadPathNode(SpreadObject spreadObject,List<GroupInfo> groupList,String screenNames,int node,String retweetedUrl,boolean flag,boolean users) throws TRSException{
            if (ObjectUtil.isNotEmpty(groupList)){
                List<SpreadObject> first = new ArrayList<>();
                QueryBuilder queryBuilder = new QueryBuilder();
                queryBuilder.filterField(FtsFieldConst.FIELD_SCREEN_NAME, screenNames, Operator.Equal);
                queryBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_URL, "\"" + retweetedUrl + "\"", Operator.Equal);
                queryBuilder.filterField(FtsFieldConst.FIELD_RETWEETED_FROM, "\"" + spreadObject.getScreenName() + "\"", Operator.Equal);
                queryBuilder.page(0, 500);
                List<SpreadObject> spreadObjects = hybase8SearchService.ftsQuery(queryBuilder, SpreadObject.class, false, false,false,null);
                //去重
//                List<SpreadObject> tempList = new ArrayList<>();
//                if (ObjectUtil.isNotEmpty(spreadObjects)){
//                    tempList = spreadObjects.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.getScreenName()))), ArrayList::new));
//                    first.addAll(tempList);
//                }
                first.addAll(spreadObjects);
                if (ObjectUtil.isEmpty(spreadObjects) || spreadObjects.size() < node) {
                    QueryBuilder queryBuilderNew = new QueryBuilder();
                    queryBuilderNew.filterField(FtsFieldConst.FIELD_SCREEN_NAME, screenNames, Operator.NotEqual);
                    queryBuilderNew.filterField(FtsFieldConst.FIELD_RETWEETED_URL, "\"" + retweetedUrl + "\"", Operator.Equal);
                    queryBuilderNew.filterField(FtsFieldConst.FIELD_RETWEETED_FROM, "\"" + spreadObject.getScreenName() + "\"", Operator.Equal);
                    queryBuilderNew.page(0, 500);
                    List<SpreadObject> spreadObjectsNew = hybase8SearchService.ftsQuery(queryBuilderNew, SpreadObject.class, false, false,false,null);
                    //去重
//                    if (ObjectUtil.isNotEmpty(spreadObjectsNew)){
//                        List<SpreadObject> tempObject = spreadObjectsNew.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.getScreenName()))), ArrayList::new));
//                    }
                    first.addAll(spreadObjectsNew);
                }

                //去重
                List<SpreadObject> tempList = first.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.getScreenName()))), ArrayList::new));

                if (ObjectUtil.isNotEmpty(tempList)){
                    //放入转发数
                    for (SpreadObject object : tempList) {
                        object.setForwardedNum(0);

                        //放入转发数
                        for (GroupInfo groupInfo : groupList) {
                            if (object.getScreenName().equals(groupInfo.getFieldValue())){
                                object.setForwardedNum(groupInfo.getCount());
                            }
                        }

                    }
                    if (flag){
                        Collections.sort(tempList, new Comparator<SpreadObject>() {
                            @Override
                            public int compare(SpreadObject o1, SpreadObject o2) {
                                return (int) (o2.getForwardedNum()-o1.getForwardedNum());
                            }
                        });

                    }else {
                        //通过现有微博用户的uid去微博用户库查询用户信息
                        List<StatusUser> statusUsers = null;
                        if (ObjectUtil.isNotEmpty(tempList)){

                            StringBuilder sb = new StringBuilder();
                            for (SpreadObject spreadObjectUid : tempList) {
                                sb.append("\"").append(spreadObjectUid.getUid()).append("\"").append(" OR ");

                            }
                            String uids = sb.toString();
                            if (uids.endsWith(" OR ")){
                                uids = uids.substring(0,uids.length()-4);
                            }
                            QueryBuilder queryStatusUser1 = new QueryBuilder();
                            queryStatusUser1.filterField(FtsFieldConst.FIELD_UID,uids,Operator.Equal);
                            queryStatusUser1.setDatabase(Const.SINAUSERS);
                            queryStatusUser1.page(0,500);
                            //查询微博用户信息
                            statusUsers = hybase8SearchService.ftsQuery(queryStatusUser1, StatusUser.class, false, false,false,null);

                            //放入对应用户信息
                            for (SpreadObject object : tempList) {
                                for (StatusUser statusUser : statusUsers) {
                                    if (object.getUid().equals(statusUser.getUid())){
                                        object.setStatusUser(statusUser);
                                    }
                                }
                            }
                        }
                        //按照用户粉丝数排序
                        Collections.sort(tempList);
                    }


                    if (tempList.size() > node){
                        tempList = tempList.subList(0,node);
                    }
                    if (users){
                        //需要查询用户信息
                        for (SpreadObject object : tempList) {
                            queryStatusUser(object);
                        }

                    }
                    return tempList;
                }
            }
        return null;
    }


    private SpreadObject subMicroBlog(String urlName, boolean first) throws TRSException{
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.filterField(FtsFieldConst.FIELD_URLNAME,"\""+urlName+"\"",Operator.Equal);
        queryBuilder.setDatabase(Const.WEIBO);
        List<SpreadObject> statuses = hybase8SearchService.ftsQuery(queryBuilder, SpreadObject.class, false, false,false,null);
        if (ObjectUtil.isEmpty(statuses)){
            return null;
        }
        SpreadObject spreadObject = statuses.get(0);
        //放入对应 微博用户信息
        queryForWardNum(spreadObject,spreadObject.getUrlName());
        queryStatusUser(spreadObject);

        if (StringUtil.isNotEmpty(urlName)){
            QueryBuilder builder = new QueryBuilder();
            builder.filterField(FtsFieldConst.FIELD_RETWEETED_URL,"\""+urlName+"\"",Operator.Equal);
            List<SpreadObject> spreadObjects = hybase8SearchService.ftsQuery(builder, SpreadObject.class, false, false,false,null);
            if (ObjectUtil.isEmpty(spreadObjects)){
                return spreadObject;
            }
            if (first){
                if (ObjectUtil.isNotEmpty(spreadObjects) && spreadObjects.size() > 10){
                    spreadObjects = spreadObjects.subList(0,10);
                }
            }else {
                if (ObjectUtil.isNotEmpty(spreadObjects) && spreadObjects.size() > 4){
                    spreadObjects = spreadObjects.subList(0,4);
                }
            }
            //放入对应微博用户信息
            for (SpreadObject object : spreadObjects) {
                queryForWardNum(object,object.getUrlName());
                queryStatusUser(object);
                object = subMicroBlog(object.getUrlName(), false);
            }
            //放入转发该微博的微博
            spreadObject.setSubSpreadObjects(spreadObjects);
            return spreadObject;
        }
        return null;
    }

    /**
     * 数据中心采集入队列接口
     * @param url
     * @return
     */
    private String hotReviewsPost(String url){
        //调数据中心接口
        String param = "{"+"\"link\":"+"\""+url+"\""+"}";
        JSONObject jsonObject = JSONObject.parseObject(param);
        String sendPost = HttpUtil.sendPost(hotReviewsUrl, jsonObject);
        return sendPost;
    }

    private List<FtsDocumentReviews> searchReviews(String urlName) throws TRSException{
        //去hybase微博库查询数据
        urlName = urlName.replace("https","http");
        QueryBuilder builder = new QueryBuilder();
        builder.filterField(FtsFieldConst.FIELD_URLNAME,"\""+urlName+"\"",Operator.Equal);
        builder.setDatabase(Const.WEIBO);
        System.err.println("查询热门评论微博:"+builder.asTRSL());
        List<FtsDocumentStatus> ftsDocumentStatuses = hybase8SearchService.ftsQuery(builder, FtsDocumentStatus.class, false, false,false,null);

        if (ObjectUtil.isNotEmpty(ftsDocumentStatuses)){
            QueryBuilder builderUser = new QueryBuilder();
            builderUser.filterField(FtsFieldConst.FIELD_MID,"\""+ftsDocumentStatuses.get(0).getMid()+"\"",Operator.Equal);
            builderUser.orderBy(FtsFieldConst.FIELD_URLTIME,true);
            builderUser.page(0,10);
            builderUser.setDatabase(Const.SINAREVIEWS);
            List<FtsDocumentReviews> ftsDocumentReviews = hybase8SearchService.ftsQuery(builderUser, FtsDocumentReviews.class, false, false,false,null);
            log.info("首次查询结果："+ftsDocumentReviews.size());
            //如果查询为null
            if (ObjectUtil.isEmpty(ftsDocumentReviews)){
                urlName = urlName.replace("http","https");
                //再次将微博链接插入采集队列，不管插入成功与否
                //请求采集接口
                String reviewsPost = hotReviewsPost(urlName);
                if (StringUtil.isNotEmpty(reviewsPost)) {
                    JSONObject jsonObject = JSONObject.parseObject(reviewsPost);
                    if ( StringUtil.isNotEmpty(jsonObject.getString("error"))) {
                        log.error("首次调采集接口，3分钟后查询无结果，故第二次调采集接口，微博链接："+urlName+"，且热门评论采集入队列出错 errmsg:{}", jsonObject.getString("error"));
                    }else {
                        log.info("首次调采集接口，3分钟后查询无结果，故第二次调采集接口，微博链接："+urlName+"，热门评论采集入队列成功 msg:{}", jsonObject.getString("OK"));
                    }
                }
                //请求后 3min分段查询hybase库
                for (int i = 1; i < 4; i++) {
                    try {
                        Thread.sleep(1000*60);
                        ftsDocumentReviews = hybase8SearchService.ftsQuery(builderUser, FtsDocumentReviews.class, false, false,false,null);
                        System.err.println("再次:"+i+",查询结果："+ftsDocumentReviews.size());
                        log.info("再次:"+i+",查询结果："+ftsDocumentReviews.size());
                        if (ObjectUtil.isNotEmpty(ftsDocumentReviews)){
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //3min结束 还是查不到数据，
                if (ObjectUtil.isEmpty(ftsDocumentReviews)){
                    // 就再入采集队列
                    reviewsPost = hotReviewsPost(urlName);
                    if (StringUtil.isNotEmpty(reviewsPost)) {
                        JSONObject jsonObject = JSONObject.parseObject(reviewsPost);
                        if ( StringUtil.isNotEmpty(jsonObject.getString("error"))) {
                            log.error("第二次调采集接口，3分钟后查询无结果，故第三次调采集接口，微博链接："+urlName+"，且热门评论采集入队列出错 errmsg:{}", jsonObject.getString("error"));
                        }else {
                            log.info("第二次调采集接口，3分钟后查询无结果，故第三次调采集接口，微博链接："+urlName+"，热门评论采集入队列成功 msg:{}", jsonObject.getString("OK"));
                        }
                    }
                    //再以3min中分段查询，以这个结果作为最终结果返回
                    for (int i = 1; i < 4; i++) {
                        try {
                            Thread.sleep(1000*60);
                            ftsDocumentReviews = hybase8SearchService.ftsQuery(builderUser, FtsDocumentReviews.class, false, false,false,null);
                            log.info("再再次:"+i+",查询结果："+ftsDocumentReviews.size());
                            if (ObjectUtil.isNotEmpty(ftsDocumentReviews)){
                                break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }

            if (ObjectUtil.isNotEmpty(ftsDocumentReviews)){
                Collections.sort(ftsDocumentReviews);

                if (ftsDocumentReviews.size() > 10){
                    ftsDocumentReviews = ftsDocumentReviews.subList(0,10);
                }

                //查询对应用户名称
                for (FtsDocumentReviews ftsDocumentReview : ftsDocumentReviews) {
                    queryStatusUser(ftsDocumentReview);
                }
                return ftsDocumentReviews;
            }
        }


        return null;

    }

    /**
     * 将目标集合分割固定长度的几个集合
     * @param targe 目标集合
     * @param size  分割集合长度
     * @return
     */
    private List<List<SpreadObject>> splitList(List<SpreadObject> targe,int size){
        List<List<SpreadObject>> listArr = new ArrayList<List<SpreadObject>>();
        //获取被拆分的数组个数
        int arrSize = targe.size()%size==0?targe.size()/size:targe.size()/size+1;
        for(int i=0;i<arrSize;i++) {
            List<SpreadObject>  sub = new ArrayList<SpreadObject>();
            //把指定索引数据放入到list中
            for(int j=i*size;j<=size*(i+1)-1;j++) {
                if(j<=targe.size()-1) {
                    sub.add(targe.get(j));
                }
            }
            listArr.add(sub);
        }
        return listArr;
    }
    private List<List<GroupInfo>> splitListGroup(List<GroupInfo> targe,int size){
        List<List<GroupInfo>> listArr = new ArrayList<List<GroupInfo>>();
        //获取被拆分的数组个数
        int arrSize = targe.size()%size==0?targe.size()/size:targe.size()/size+1;
        for(int i=0;i<arrSize;i++) {
            List<GroupInfo>  sub = new ArrayList<GroupInfo>();
            //把指定索引数据放入到list中
            for(int j=i*size;j<=size*(i+1)-1;j++) {
                if(j<=targe.size()-1) {
                    sub.add(targe.get(j));
                }
            }
            listArr.add(sub);
        }
        return listArr;
    }
    private GroupResult everyTakeSuperLanguage(SpreadObject spreadObject,String urlTime) throws OperationException{

        QueryBuilder sBuilder = new QueryBuilder();
        sBuilder.filterField(FtsFieldConst.FIELD_URLTIME,DateUtil.formatTimeRange(urlTime),Operator.Between);
        sBuilder.filterField(FtsFieldConst.FIELD_SCREEN_NAME,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        sBuilder.page(0,Integer.MAX_VALUE);
        sBuilder.setDatabase(Const.WEIBO);
        GroupResult groupInfos = hybase8SearchService.categoryQuery(sBuilder, false, false,false, FtsFieldConst.FIELD_TAG, null,sBuilder.getDatabase());
        return groupInfos;
    }
    /**
     * 发文情感统计
     * @param spreadObject
     * @param urlTime
     * @return
     * @throws OperationException
     */
    private List<Map<String, String>> everyEmotionStatistics(SpreadObject spreadObject,String urlTime) throws OperationException{
        List<Map<String, String>> list=new ArrayList<>();
        QueryBuilder sBuilder = new QueryBuilder();
        sBuilder.filterField(FtsFieldConst.FIELD_SCREEN_NAME,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        sBuilder.filterField(FtsFieldConst.FIELD_URLTIME,DateUtil.formatTimeRange(urlTime),Operator.Between);
        sBuilder.setDatabase(Const.WEIBO);
        String trsl = sBuilder.asTRSL();
        GroupResult groupInfos = hybase8SearchService.categoryQuery(false, trsl, false, false,false,
                ESFieldConst.IR_APPRAISE, 3,null, Const.WEIBO);
        trsl += new StringBuffer().append(" NOT ").append(FtsFieldConst.FIELD_APPRAISE).append(":(").append("正面")
                .append(" OR ").append("负面").append(")").toString();
        sBuilder = new QueryBuilder();
        sBuilder.filterByTRSL(trsl);
        sBuilder.setDatabase(Const.WEIBO);
        long ftsCount = hybase8SearchService.ftsCount(sBuilder, false, false,false,null);

        List<GroupInfo> groupList = groupInfos.getGroupList();
        for (GroupInfo group : groupList) {
            Map<String, String> map = new HashMap<String, String>();
            String fieldValue = group.getFieldValue();
            if ("负面".equals(fieldValue) || "正面".equals(fieldValue)) {
                map.put("name", group.getFieldValue());
                map.put("value", String.valueOf(group.getCount()));
            } else {
                map.put("name", "中性");
                map.put("value", String.valueOf(ftsCount));
            }
            list.add(map);
        }

        List<String> keys = new ArrayList<>();
        for (Map<String, String> map : list) {
            Set<String> strings = map.keySet();
            for (String string : strings) {
                if ("name".equals(string)) {
                    String value = map.get(string);
                    keys.add(value);
                }
            }
        }
        if (!keys.contains("正面")) {
            Map<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("name", "正面");
            hashMap.put("value", "0");
            list.add(hashMap);
        }
        if (!keys.contains("负面")) {
            Map<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("name", "负面");
            hashMap.put("value", "0");
            list.add(hashMap);
        }
        if(!keys.contains("中性")){
            Map<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("name", "中性");
            hashMap.put("value", String.valueOf(ftsCount));
            list.add(hashMap);

        }
        return list;
    }

    private Map<String,Long> everyPrimaryForwardRatio(SpreadObject spreadObject,String urlTime) throws OperationException{
        Map<String, Long> dataMap = new HashMap<String, Long>();
        //原发
        QueryBuilder primaryBuilder = new QueryBuilder();
        primaryBuilder.filterField(FtsFieldConst.FIELD_SCREEN_NAME,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        primaryBuilder.filterByTRSL(Const.PRIMARY_WEIBO);
        primaryBuilder.setDatabase(Const.WEIBO);
        primaryBuilder.filterField(FtsFieldConst.FIELD_URLTIME,DateUtil.formatTimeRange(urlTime),Operator.Between);
        long primaryCount = hybase8SearchService.ftsCount(primaryBuilder, false, false,false,null);

        //转发 = 总数-原发
        QueryBuilder totalBuilder = new QueryBuilder();
        totalBuilder.filterField(FtsFieldConst.FIELD_SCREEN_NAME,"\""+spreadObject.getScreenName()+"\"",Operator.Equal);
        totalBuilder.setDatabase(Const.WEIBO);
        totalBuilder.filterField(FtsFieldConst.FIELD_URLTIME,DateUtil.formatTimeRange(urlTime),Operator.Between);
        long totalCount = hybase8SearchService.ftsCount(totalBuilder, false, false,false,null);

        dataMap.put("primary",primaryCount);
        dataMap.put("forward",(totalCount-primaryCount));

        return dataMap;
    }
}
