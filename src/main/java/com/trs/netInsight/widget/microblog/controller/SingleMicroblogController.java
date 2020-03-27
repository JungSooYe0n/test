package com.trs.netInsight.widget.microblog.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.microblog.constant.MicroblogConst;
import com.trs.netInsight.widget.microblog.entity.SingleMicroblogData;
import com.trs.netInsight.widget.microblog.entity.SpreadObject;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogDataService;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogService;
import com.trs.netInsight.widget.user.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *  单条微博分析控制层
 * @author 拓尔思信息技术股份有限公司
 * Created by 拓尔思信息技术股份有限公司 on 2019/1/14.
 * @desc
 */
@RestController
@RequestMapping("/single")
@Api(description = "单条微博分析")
@Slf4j
public class SingleMicroblogController {

    @Autowired
    private ISingleMicroblogService singleMicroblogService;

    @Autowired
    private ISingleMicroblogDataService singleMicroblogDataService;

    /**
     * 查询某用户账号下已完成分析的微博
     * @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/microblogList")
    @ApiOperation("查询已完成分析的微博")
    public Object microblogList() throws TRSException {
        long start = new Date().getTime();
        log.info("开始统计计算："+start);
        User loginUser = UserUtils.getUser();
        Sort sort = new Sort(Sort.Direction.DESC, "latelyTime");
        List<SingleMicroblogData> microblogDatas = singleMicroblogDataService.findAll(loginUser, MicroblogConst.MICROBLOGLIST,sort);
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，查询列表共用时长："+(end-start));
        return microblogDatas;
    }
    /**
     * 删除
     *  @author 拓尔思信息技术股份有限公司
     *  @Company 拓尔思信息技术股份有限公司
     *  @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @return
     * @throws TRSException
     */
    @FormatResult
    @PostMapping(value = "/deleteMicroblog")
    @ApiOperation("查询已完成分析的微博")
    public Object deleteMicroblog(@ApiParam("微博在mongodb里的id") @RequestParam(value = "id",required = true)String id) throws TRSException {
        long start = new Date().getTime();
        log.info("开始统计计算："+start);
        singleMicroblogDataService.remove(id);

        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，查询列表共用时长："+(end-start));
        return "删除成功";
    }


    /**
     * 判断该条微博是否满足被分析条件
     *  @author 拓尔思信息技术股份有限公司
     *  @Company 拓尔思信息技术股份有限公司
     *  @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName  传入用户提供链接
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/judgeData")
    @ApiOperation("判断该条微博是否满足被分析条件")
    public Object judgeData(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
        Object judgeData = singleMicroblogService.judgeData(urlName);
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，判断共用时长："+(end-start));
        return judgeData;
    }
    /**
     * 添加
     * @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @return
     * @throws TRSException
     */
    @FormatResult
    @PostMapping(value = "/saveMicroblog")
    @ApiOperation("添加要分析的微博")
    public Object saveMicroblog(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info("开始统计计算："+start);
        if (StringUtil.isEmpty(urlName)){
            return null;
        }
        if (urlName.indexOf("?") != -1){
            //有问号
            urlName = urlName.split("\\?")[0];
        }
        urlName = urlName.replace("https","http");
        String random = UUID.randomUUID().toString().replace("-", "");
        String currentUrl = urlName+random;
        User loginUser = UserUtils.getUser();
        List<SingleMicroblogData> states = singleMicroblogDataService.findStates(loginUser, MicroblogConst.MICROBLOGLIST,"分析中");
        //列表查询
        SingleMicroblogData microblogList = new SingleMicroblogData(MicroblogConst.MICROBLOGLIST,urlName,currentUrl);
        microblogList.setUserId(loginUser.getId());
        microblogList.setSubGroupId(loginUser.getSubGroupId());
        microblogList.setLatelyTime(new Date());
        if (ObjectUtil.isNotEmpty(states) && states.size() > 0){
            microblogList.setState("正在排队");
        }else {
            microblogList.setState("分析中");
        }

        try {
            SpreadObject spreadObject = singleMicroblogService.currentUrlMicroBlog(urlName);
            if (ObjectUtil.isEmpty(spreadObject)){
                return null;
            }
            microblogList.setData(spreadObject);
        } catch (TRSException e) {
            log.error(MicroblogConst.MICROBLOGLIST,e);
        }
        microblogList.setRandom(random);
        microblogList.setLastModifiedTime(new Date());
        SingleMicroblogData microblogData = singleMicroblogDataService.insert(microblogList);
        //查询hybase，并将结果放在mongodb中
        if (ObjectUtil.isNotEmpty(microblogData) && "分析中".equals(microblogData.getState())){
            singleMicroblogService.dataAnalysis(microblogData.getOriginalUrl(),microblogData.getCurrentUrl(),microblogData.getRandom());
        }

        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，添加共用时长："+(end-start));
        return microblogData;
    }

    /**
     *  修改更新
     *  @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param id
     * @return
     * @throws TRSException
     */
    @FormatResult
    @PostMapping(value = "/updateMicroblog")
    @ApiOperation("更新已完成分析的微博")
    public Object updateMicroblog(@ApiParam("微博id") @RequestParam(value = "id",required = false)String id) throws TRSException {
        long start = new Date().getTime();
        log.info("开始统计计算："+start);
        if (StringUtil.isEmpty(id)){
            return null;
        }
        SingleMicroblogData singleMicroblogData = singleMicroblogDataService.findOne(id);
        if (ObjectUtil.isEmpty(singleMicroblogData)){
            throw new OperationException("无效id");
        }

        String random = UUID.randomUUID().toString().replace("-", "");
        singleMicroblogData.setRandom(random);
        User loginUser = UserUtils.getUser();
        List<SingleMicroblogData> states = singleMicroblogDataService.findStates(loginUser, MicroblogConst.MICROBLOGLIST,"分析中");
        //列表查询
        singleMicroblogData.setLatelyTime(new Date());
        if (ObjectUtil.isNotEmpty(states) && states.size() > 0){
            singleMicroblogData.setState("正在排队");

        }else {
            singleMicroblogData.setState("分析中");
        }

        try {
            SpreadObject spreadObject = singleMicroblogService.currentUrlMicroBlog(singleMicroblogData.getOriginalUrl());
            if (ObjectUtil.isEmpty(spreadObject)){
                return null;
            }
            singleMicroblogData.setData(spreadObject);
        } catch (TRSException e) {
            log.error(MicroblogConst.MICROBLOGLIST,e);
        }
        SingleMicroblogData microblogData = singleMicroblogDataService.save(singleMicroblogData);
        //查询hybase，并将结果放在mongodb中
        if (ObjectUtil.isNotEmpty(microblogData) && "分析中".equals(microblogData.getState())){
            singleMicroblogService.dataAnalysis(microblogData.getOriginalUrl(),microblogData.getCurrentUrl(),microblogData.getRandom());
        }


        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，查询列表共用时长："+(end-start));
        return microblogData;
    }


    /**
     * 查看查询进度
     * @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName  传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/confirmStep")
    @ApiOperation("查看查询进度")
    public Object confirmStep(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
        Object confirmStep = singleMicroblogService.confirmStep(urlName);
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，查看查询进度共用时长："+(end-start));
        return confirmStep;
    }
    /**
     *  被分析微博详情
     *  @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName  传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/microBlogDetail")
    @ApiOperation("被分析微博详情")
    public Object microBlogDetail(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
       // Object microBlog = singleMicroblogService.microBlogDetail(urlName);
        User loginUser = UserUtils.getUser();
        SingleMicroblogData microBlog = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.MICROBLOGDETAIL);
        if (ObjectUtil.isEmpty(microBlog)){
            return null;
        }
        Object microBlogData = microBlog.getData();
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，微博详情共用时长："+(end-start));
        return microBlogData;
    }

    /**
     * 热门评论
     * @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName 传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/hotReviews")
    @ApiOperation("热门评论")
    public Object hotReviews(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException{
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
       // Object hotReviews = singleMicroblogService.hotReviews(urlName);
        User loginUser = UserUtils.getUser();
        SingleMicroblogData hotReviews = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.HOTREVIEWS);
        if (ObjectUtil.isEmpty(hotReviews)){
            return null;
        }
        Object hotReviewsData = hotReviews.getData();
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，热门评论共用时长："+(end-start));
        return hotReviewsData;
    }

    /**
     * 热门评论
     * @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/hotReviewsTest")
    @ApiOperation("热门评论")
    public Object hotReviewsTest(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException{
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
        SingleMicroblogData singleMicroblogData = new SingleMicroblogData(MicroblogConst.HOTREVIEWS, urlName, "");

        singleMicroblogData.setData("测试mongo");
        singleMicroblogData.setUserId(UserUtils.getUser().getId());
        User loginUser = UserUtils.getUser();
        SingleMicroblogData smdBySth = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.HOTREVIEWS);
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，热门评论共用时长："+(end-start));
        return smdBySth;
    }
    /**
     *  传播分析
     *  @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName 传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/spreadAnalysis")
    @ApiOperation("传播分析")
    public Object spreadAnalysis(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
       // Object spreadAnalysis = singleMicroblogService.spreadAnalysis(urlName);
        User loginUser = UserUtils.getUser();
        SingleMicroblogData spreadAnalysis = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.SPREADANALYSIS);
        if (ObjectUtil.isEmpty(spreadAnalysis)){
            return null;
        }
        Object spreadAnalysisData = spreadAnalysis.getData();
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，传播分析共用时长："+(end-start));
        return spreadAnalysisData;
    }

    /**
     *  被转载趋势图
     *  @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName 传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/forwardedTrend")
    @ApiOperation("被转载趋势图")
    public Object forwardedTrend(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName,
                                 @ApiParam("微博发布时间") @RequestParam(value = "urlTime",required = false)String urlTime) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
       // Object forwardedTrendMap = singleMicroblogService.forwardedTrendMap(urlName);
        User loginUser = UserUtils.getUser();
        SingleMicroblogData forwardedTrend = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.FORWARDEDTREND);
        if (ObjectUtil.isEmpty(forwardedTrend)){
            return null;
        }
        Map<String, Map<String, List<Object>>> forwardedTrendData = (Map<String, Map<String, List<Object>>>) forwardedTrend.getData();
        if (urlTime != null && urlTime.length() > 0) {
            if (urlTime.length() == 10) {
                urlTime = urlTime + " 00:00:00";
            }
            String start_day = urlTime.substring(0, 10);
            String start_hour = urlTime.substring(11, 13);
            Map<String, List<Object>> map_hour = (Map<String, List<Object>>) forwardedTrendData.get("hour");
            List<Object> hour_date = (List<Object>) map_hour.get("date");
            int hour_num = hour_date.indexOf(start_day);
            if (hour_num != -1) {
                for (int i = hour_num; i < hour_date.size(); i++) {
                    if (start_hour.equals((String) hour_date.get(i))) {
                        hour_num = i;
                        break;
                    }
                }
                List<Object> hour_count = (List<Object>) map_hour.get("count");
                for (int i = hour_num - 1; i >= 0; i--) {
                    hour_date.remove(i);
                    hour_count.remove(i);
                }
            }
            Map<String, List<Object>> map_day = (Map<String, List<Object>>) forwardedTrendData.get("day");
            List<Object> day_date = (List<Object>) map_day.get("date");
            int day_num = day_date.indexOf(start_day);
            if (day_num != -1) {
                List<Object> day_count = (List<Object>) map_day.get("count");
                for (int i = day_num - 1; i >= 0; i--) {
                    day_date.remove(i);
                    day_count.remove(i);
                }
            }
        }
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，被转载趋势图共用时长："+(end-start));
        return forwardedTrendData;
    }

    /**
     *  传播路径
     *  @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName 传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/spreadPath")
    @ApiOperation("传播路径")
    public Object spreadPath(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
        //Object spreadPath = singleMicroblogService.spreadPathNew(urlName);
        User loginUser = UserUtils.getUser();
        SingleMicroblogData spreadPath = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.SPREADPATH);
        if (ObjectUtil.isEmpty(spreadPath)){
            return null;
        }
        Object spreadPathData = spreadPath.getData();
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，传播路径共用时长："+(end-start));
        return spreadPathData;
    }
    /**
     *  传播路径
     *  @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName 传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/spreadPathNew")
    @ApiOperation("传播路径")
    public Object spreadPathNew(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
      //  Object spreadPath = singleMicroblogService.spreadPathNew(urlName);
        User loginUser = UserUtils.getUser();
        SingleMicroblogData spreadPath = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.SPREADPATH);
        if (ObjectUtil.isEmpty(spreadPath)){
            return null;
        }
        Object spreadPathData = spreadPath.getData();
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，传播路径新逻辑共用时长："+(end-start));
        return spreadPathData;
    }


    /**
     * 核心转发
     * @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName 传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/coreForward")
    @ApiOperation("核心转发")
    public Object coreForward(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
       // Object coreForward = singleMicroblogService.coreForward(urlName);
        User loginUser = UserUtils.getUser();
        SingleMicroblogData coreForward = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.COREFORWARD);
        if (ObjectUtil.isEmpty(coreForward)){
            return null;
        }
        Object coreForwardData = coreForward.getData();
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，核心转发共用时长："+(end-start));
        return coreForwardData;
    }

    /**
     *  意见领袖
     *  @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName  传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/opinionLeaders")
    @ApiOperation("意见领袖")
    public Object opinionLeaders(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
        //Object opinionLeaders = singleMicroblogService.opinionLeaders(urlName);
        User loginUser = UserUtils.getUser();
        SingleMicroblogData opinionLeaders = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.OPINIONLEADERS);
        if (ObjectUtil.isEmpty(opinionLeaders)){
            return null;
        }
        Object opinionLeadersData = opinionLeaders.getData();
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，意见领袖共用时长："+(end-start));
        return opinionLeadersData;
    }

    /**
     *  转发博主地域分析
     *  @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName 传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/areaAnalysisOfForWarders")
    @ApiOperation("转发博主地域分析")
    public Object areaAnalysisOfForWarders(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
        //Object opinionLeaders = singleMicroblogService.areaAnalysisOfForWarders(urlName);
        User loginUser = UserUtils.getUser();
        SingleMicroblogData areaAnalysisOfForWarders = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.AREAANALYSISOFFORWARDERS);
        if (ObjectUtil.isEmpty(areaAnalysisOfForWarders)){
            return null;
        }
        Object areaAnalysisOfForWardersData = areaAnalysisOfForWarders.getData();
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，转发博主地域分析共用时长："+(end-start));
        return areaAnalysisOfForWardersData;
    }

    /**
     *  转发微博情绪分析
     *  @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName 传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/emojiAnalysisOfForward")
    @ApiOperation("转发微博情绪分析")
    public Object emojiAnalysisOfForward(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
        //Object emojiAnalysisOfForword = singleMicroblogService.emojiAnalysisOfForward(urlName);
        User loginUser = UserUtils.getUser();
        SingleMicroblogData emojiAnalysisOfForward = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.EMOJIANALYSISOFFORWARD);
        if (ObjectUtil.isEmpty(emojiAnalysisOfForward)){
            return null;
        }
        List<GroupInfo> emojiAnalysisOfForwardData = (List<GroupInfo>)emojiAnalysisOfForward.getData();
        if (ObjectUtil.isNotEmpty(emojiAnalysisOfForwardData)){
            for (GroupInfo groupInfo : emojiAnalysisOfForwardData) {
                String pinyin = PinyinUtil.toPinyinWithPolyphone(groupInfo.getFieldValue());
                groupInfo.setFieldValue(pinyin);
            }
           // return groupInfos.getGroupList();
        }
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，转发微博情绪分析共用时长："+(end-start));
        return emojiAnalysisOfForwardData;
    }

    /**
     * 男女比例
     * @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName 传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/genderOfRatio")
    @ApiOperation("男女比例")
    public Object genderOfRatio(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
        //Object emojiAnalysisOfForword = singleMicroblogService.genderOfRatio(urlName);
        User loginUser = UserUtils.getUser();
        SingleMicroblogData genderOfRatio = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.GENDEROFRATIO);
        if (ObjectUtil.isEmpty(genderOfRatio)){
            return null;
        }
        Object genderOfRatioData = genderOfRatio.getData();
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，男女比例共用时长："+(end-start));
        return genderOfRatioData;
    }

    /**
     * 认证比例
     * @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName 传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/certifiedOfRatio")
    @ApiOperation("认证比例")
    public Object certifiedOfRatio(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
        //Object emojiAnalysisOfForword = singleMicroblogService.certifiedOfRatio(urlName);
        User loginUser = UserUtils.getUser();
        SingleMicroblogData certifiedOfRatio = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.CERTIFIEDOFRATIO);
        if (ObjectUtil.isEmpty(certifiedOfRatio)){
            return null;
        }
        Object certifiedOfRatioData = certifiedOfRatio.getData();
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，认证比例共用时长："+(end-start));
        return certifiedOfRatioData;
    }

    /**
     *  博主发文频率
     *  @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName 传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/dispatchFrequency")
    @ApiOperation("博主发文频率")
    public Object dispatchFrequency(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
        //Object frequency = singleMicroblogService.dispatchFrequency(urlName);
        User loginUser = UserUtils.getUser();
        SingleMicroblogData dispatchFrequency = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.DISPATCHFREQUENCY);
        if (ObjectUtil.isEmpty(dispatchFrequency)){
            return null;
        }
        Object dispatchFrequencyData = dispatchFrequency.getData();
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，博主发文频率共用时长："+(end-start));
        return dispatchFrequencyData;
    }

    /**
     *  参与话题统计
     *  @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName 传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/takeSuperLanguage")
    @ApiOperation("参与话题统计")
    public Object takeSuperLanguage(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
        //Object takeSuperLanguage = singleMicroblogService.takeSuperLanguage(urlName);
        User loginUser = UserUtils.getUser();
        SingleMicroblogData takeSuperLanguage = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.TAKESUPERLANGUAGE);
        if (ObjectUtil.isEmpty(takeSuperLanguage)){
            return null;
        }
        Object takeSuperLanguageData = takeSuperLanguage.getData();
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，参与话题统计共用时长："+(end-start));
        return takeSuperLanguageData;
    }

    /**
     *  发文情感统计
     *  @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName 传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/emotionStatistics")
    @ApiOperation("发文情感统计")
    public Object emotionStatistics(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
       // Object emotionStatistics = singleMicroblogService.emotionStatistics(urlName);
        User loginUser = UserUtils.getUser();
        SingleMicroblogData emotionStatistics = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.EMOTIONSTATISTICS);
        if (ObjectUtil.isEmpty(emotionStatistics)){
            return null;
        }
        Object emotionStatisticsData = emotionStatistics.getData();
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，发文情感统计共用时长："+(end-start));
        return emotionStatisticsData;
    }


    /**
     *  原发转发占比
     *  @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @param urlName 传入程序改造后微博地址
     * @return
     * @throws TRSException
     */
    @FormatResult
    @GetMapping(value = "/primaryForwardRatio")
    @ApiOperation("原发转发占比")
    public Object primaryForwardRatio(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
       // Object forwardRatio = singleMicroblogService.primaryForwardRatio(urlName);
        User loginUser = UserUtils.getUser();
        SingleMicroblogData primaryForwardRatio = singleMicroblogDataService.findSMDBySthNoRandom(loginUser, urlName, MicroblogConst.PRIMARYFORWARDRATIO);
        if (ObjectUtil.isEmpty(primaryForwardRatio)){
            return null;
        }
        Object primaryForwardRatioData = primaryForwardRatio.getData();
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，原发转发占比共用时长："+(end-start));
        return primaryForwardRatioData;
    }

    /**
     * 导出 被转发趋势图（横向）
     * @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @return
     *  导出 转发
     * @throws TRSException
     */
    @ApiOperation("导出 被转发趋势图")
    @PostMapping(value = "/exportSingleChartLine")
    public void exportSingleChartLine(HttpServletResponse response,
                                  @ApiParam("前端给回需要导出的内容") @RequestParam(value = "data", required = true) String data)  {

        try {
            ServletOutputStream outputStream = response.getOutputStream();
            JSONArray array = JSONArray.parseArray(data);
            singleMicroblogService.exportSingleChartLine(array).writeTo(outputStream);
        } catch (Exception e) {
            log.error("单条微博导出被转发趋势图数据出错",e);
        }
    }

    /**
     *  导出被转发趋势图、 转发微博情绪分析、男女比例、认证比例、发文情感统计、原发转发占比
     *  @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @return
     * @throws TRSException
     */
    @ApiOperation("导出 转发微博情绪分析、男女比例、认证比例、发文情感统计、原发转发占比")
    @PostMapping(value = "/exportSingleChart")
    public void exportSingleChart(HttpServletResponse response,
                                  @ApiParam("需要导出内容的类型") @RequestParam(value = "dataType", required = true) String dataType,
                                  @ApiParam("前端给回需要导出的内容") @RequestParam(value = "data", required = true) String data)  {

        try {
            ServletOutputStream outputStream = response.getOutputStream();
            JSONArray array = JSONObject.parseArray(data);
            singleMicroblogService.exportSingleChart(dataType,array).writeTo(outputStream);
        } catch (Exception e) {
            log.error("单条微博导出数据出错",e);
        }
    }

    /**
     *  导出 转发微博地域分析
     *  @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     * @return
     * @throws TRSException
     */
    @ApiOperation("导出 转发微博地域分析")
    @PostMapping(value = "/exportSingleMap")
    public void exportSingleMap(HttpServletResponse response,
                                @ApiParam("需要导出的内容的") @RequestParam(value = "data", required = true) String data)  {
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            JSONArray array = JSONObject.parseArray(data);
            singleMicroblogService.exportSingleMap(array).writeTo(outputStream);
        } catch (Exception e) {
            log.error("单条微博导出转发微博地域数据出错",e);
        }

    }

    /**
     * 被转载趋势图
     * @author 拓尔思信息技术股份有限公司
     * @Company 拓尔思信息技术股份有限公司
     * @Copyright: Copyright (c) 拓尔思信息技术股份有限公司
     */
    @FormatResult
    @PostMapping(value = "/forwardedTrendTest")
    @ApiOperation("被转载趋势图")
    public Object forwardedTrendTest(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName,
                                 @ApiParam("统计时间段") @RequestParam(value = "urlTime",defaultValue = "7d")String urlTime) throws OperationException,TRSException {
        long start = new Date().getTime();
        log.info(urlName);
        log.info("开始统计计算："+start);
        Map<String, String> map = new HashMap<String, String>();
        map.put("fieldValue", "1");
        map.put("count","2");
        map.put("fieldValue", "1");
        map.put("count","2");
        Object trendMap = singleMicroblogService.forwardedTrendMapTest(urlName, urlTime);
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，共用时长："+(end-start)+"，统计时间段："+urlTime);
        return trendMap;
    }

    /**
     *
     */
    @FormatResult
    @PostMapping(value = "/statusUserTest")
    @ApiOperation("被转载趋势图")
    public Object statusUserTest(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws OperationException,TRSException {
        long start = new Date().getTime();
        log.info("开始统计计算："+start);
        Object trendMap = singleMicroblogService.testSinaUser(urlName);
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，共用时长："+(end-start));
        return trendMap;
    }

    /**
     *
     */
    @FormatResult
    @PostMapping(value = "/statusDataTest")
    @ApiOperation("测试微博数据")
    public Object statusDataTest(@ApiParam("微博地址") @RequestParam(value = "urlName",required = false)String urlName) throws OperationException,TRSException {
        long start = new Date().getTime();
        log.info("开始统计计算："+start);
       // Object trendMap = singleMicroblogService.coreForward(urlName);
        SpreadObject spreadObject = singleMicroblogService.spreadPathNew(urlName);
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，共用时长："+(end-start));
        return spreadObject;
    }


    @FormatResult
    @PostMapping(value = "/informaAcTest")
    @ApiOperation("测试热点评论微博数据")
    public Object informaAcTest() throws OperationException,TRSException {
        long start = new Date().getTime();
        log.info("开始统计计算："+start);
        String url = "{"+"\"link\":"+"\"https://weibo.com/1916655407/HblL1fOgp?ref=feedsdk\""+"}";
        System.err.println(url);
        JSONObject jsonObject = JSONObject.parseObject(url);
        String sendPost = HttpUtil.sendPost("http://192.168.201.253:8000/url/", jsonObject);
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，共用时长："+(end-start));
        return null;
    }

    @FormatResult
    @PostMapping(value = "/testThreeMin")
    @ApiOperation("测试热点评论微博数据")
    public void testThreeMin() throws OperationException,TRSException {
        long start = new Date().getTime();
        log.info("开始统计计算："+start);
//        Map<String, String> map = new HashMap<>();
//        long nowTime = new Date().getTime();
//        SingleMicroblogData microblogData = singleMicroblogDataService.getOne("5c764638b9f2ec2370a399fc");
//        long lastTime = microblogData.getLastModifiedTime().getTime();
//        long result = nowTime - lastTime;
//        int days = (int) (result / (1000 * 60 * 60 * 24));
//        // 计算小时查
//        int hours = (int) (result / (1000 * 60 * 60));
//        // 计算分钟差
//        int minutes = (int) (result / (1000 * 60));
//        if (0 != minutes) {
//            map.put("timeAgo", minutes + "分钟前");
//        }
//        if (0 != hours) {
//            map.put("timeAgo", hours + "小时前");
//        }
//        if (0 != days) {
//            map.put("timeAgo", days + "天前");
//        }

        String str = "江苏火锅底料供应商贴牌丨加工欢迎光临/咨询正宗重庆火锅底料丨批发丨加工丨贴牌丨供应的火锅，历史悠久，源远流长。浙江等地曾出土5000多年前的与陶釜配套使用的小陶灶，可以很方便地移动，可以算是火锅初级形式。<font color=red>北京</font>延庆龙庆峡山戎文化遗址中出土的春秋时期青铜火锅，有加热过的痕迹。&lt;IMAGE&nbsp;SRC=&quot;http://img.qiyezhanlan.com/cqlslcy/2018/12/20181210abb1544434090.jpg&quot;&nbsp;&gt;&nbsp;奴隶社会后期，出现了一种小铜鼎，高不超过20厘米，口径15厘米左右。有的鼎与炉合二为一，即在鼎中铸有一个隔层，将鼎腹分为上下两部分，下层一个开口，可以送入炭火，四周镂空作通风的烟孔有的鼎腹较浅，鼎中间夹一炭盘，人们称这种类型的鼎为“温鼎”，它小巧便利，说是一种的火锅了。&nbsp;汉代出现一种称为“染炉”、“染杯”的小铜器，构造分为三部分主体为炭炉；上面有盛食物的杯，容积一般为250至300毫升；下面有承接炭火的盘。可以推断这就是古代单人使用的小火锅。&nbsp;唐宋时，火锅开始盛行，官府和名流家中设宴，多备火锅。&nbsp;在五代时，就出现过五格火锅，就是将火锅分成五格供客人涮用。那时的火锅又称暖锅，一种是铜制的，一种是陶制的，主要作用是煮肉食用。&nbsp;到了清代，各种涮肉火锅已成为宫廷冬令佳肴。火锅是所特有的食用食品的，火锅的圆形设计，使就餐者集成一个圆圈，也在预示着人讲究团圆的传统习俗，这种在冬天采取碳加热水烹饪牛羊肉的吃饭，也符合了满族人的饮食习惯。嘉庆登基时，在的宫廷宴席中，除山珍海味、水陆并陈外，特地用了1650只火锅宴请嘉宾，成为我国历的火锅宴。&lt;IMAGE&nbsp;SRC=&quot;http://img.qiyezhanlan.com/cqlslcy/2018/12/20181210nlf1544434101.jpg&quot;&nbsp;&gt;重庆火锅学习中心建议，只有对各种产品有了基本的了解和理解，才能集中精力挑选菜肴，避免对“自以为是”的误解。根据我联系的情况，这是伟大的。大多数从事食品和饮料的新手朋友在开始做出决定和选择时，对菜肴的知识知之甚少。有很多妄想，例如，如果你找到一个好主人，那菜是什么不是问题。??特殊菜肴更容易形成眼球和源效应。期初的和推广很方便。与其他类型不同，特殊菜肴的大特点是及时性一旦时尚流行，它将被市场抛弃并被其他人使用。-服务。材料的来源和季节性。好的火锅底料生产必须严格控制材料的质量，否则肯定会造成问题。火锅底料的主要原料主要是农副产品，季节性很强。赛季结束后，质量将大大。例如，主要材料辣椒，你必须使用辣椒夏季采摘，秋季辣椒的质量要差得多。如辣椒，四川玉文辣椒色红，麻木，香气大，基本没有苦味，而陕西大红袍辣椒的苦味较重，这是由于两种辣椒生长的地理差异造成的。，投机者的随机性。我们反复观察了推测投机者的。在材料时，很少有人去做材料。这是因为油炸担心火锅不够，加入多余的香料。??解决方<font color=red>北京</font>案事实上，在重庆煎炸火锅底料时，添加的味道不宜过多。通常，克基料中使用的香料不应超过克，所用香料的类型不应太多。各种香料适用于八种八角，三颈，肉桂，茴香，豆粕，草果，香果，丁香等八种，因为香料本身就是一种，不仅不可能。通过使用各种香料提取风味。相反，它会产生的味道，因为它具有杀灭作用。问题黄油味道不纯净。在重庆火锅中，或多或少必须加入黄油，因为黄油会火锅的味道。但是，在市场上，重庆火锅底料非常多，口味多样，品牌也各不相同。重庆火锅底料排名，你知道有多少种。因此，重庆的许多<font color=red>上海</font>火锅有相似之处，因为这些火锅店和火锅底料厂并不是多年前传下来的。他们是从事火锅行业多年的人，在炒作，食谱，秘密等方面。已经学会了重庆火锅精华的人已经出来创业，所以他们在很多方面有相似之处。地方，但每个品牌都经过改进和创新，使其更符合现代人的消费习惯。重庆老火锅公司是由重庆东子老火锅研制而成。&lt;IMAGE&nbsp;SRC=&quot;http://img.qiyezhanlan.com/cqlslcy/2018/12/20181210oeg1544434138.jpg&quot;&nbsp;&gt;江苏火锅底料供应商贴牌丨加工欢迎光临/咨询lslpmhhhh";
//        Matcher m = Pattern.compile("<font.*?>([\\s\\S]*?)</font>").matcher(str);
//        while (m.find()) {
//            String link = m.group(1).trim();
//            System.err.println(link);
//        }
        Matcher m = Pattern.compile("<font color=red>([\\s\\S]*?)</font>").matcher(str);
        String[] split = str.split("。");
        int index = 0;
        int count = 0;
        StringBuilder sb = new StringBuilder();
        String subStr = "";
        List<String> res = new ArrayList<>();
        while (m.find()){
            String trim = m.group(1).trim();
            if (!res.contains(trim)){
                res.add(trim);
            }
        }
            int begin = str.indexOf("北京",index);
        List<String> result = new ArrayList<>();
        for (String re : res) {
            for (String s : split) {
                if (s.contains(re) && !result.contains(s)){
                    result.add(s);
                }
            }
        }

           // System.err.println("index："+index);
          //  System.err.println("begin："+begin);
          //  int end = str.indexOf("。",begin+1)+1;
           // subStr = str.substring(begin,end);

//            if (index != -1){
//                index = index + subStr.length()+index;
//                count++;
//            }

        String join = StringUtils.join(result.toArray(), "。");
        System.err.println("结果："+join);
//        String subStr = "";
//        int begin = str.indexOf("<font color=red>");
//        int end = str.indexOf("。",begin+1)+1;
//        subStr = str.substring(begin,end);
//        System.err.println(subStr);
        long end = new Date().getTime();
        log.info("结束统计计算："+end+"，共用时长："+(end-start));

     //  return map;
    }

}


