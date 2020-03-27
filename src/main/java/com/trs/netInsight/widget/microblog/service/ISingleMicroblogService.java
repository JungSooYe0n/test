package com.trs.netInsight.widget.microblog.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.model.result.GroupInfo;
import com.trs.netInsight.widget.microblog.entity.SpreadObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 单条微博分析业务层接口
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/1/14.
 * @desc
 */
public interface ISingleMicroblogService {

    /**
     * 判断该条微博是否满足被分析条件
     * @param urlName
     * @return
     * @throws TRSException
     */
    public Map<String , Object> judgeData(String urlName) throws TRSException;

    /**
     * 开始去hybase去查询各个模块数据并存储到mongodb
     * @param originalUrl
     * @param currentUrl
     * @return
     * @throws TRSException
     */
    public String dataAnalysis(String originalUrl,String currentUrl,String random) throws TRSException;



    /**
     * 查看查询进度
     * @param urlName
     * @return
     */
    public String confirmStep(String urlName) throws TRSException;

    /**
     * 查询当前地址对应的微博
     * @param urlName
     * @return
     * @throws TRSException
     */
    public SpreadObject currentUrlMicroBlog(String urlName) throws TRSException;

    /**
     * 被分析微博详情
     * @param urlName
     * @return
     * @throws TRSException
     */
    public Map<String, Object> microBlogDetail(String urlName) throws TRSException;

    /**
     * 热门评论
     * @param urlName
     * @return
     * @throws TRSException
     */
    public Object hotReviews(String urlName) throws TRSException;

    /**
     * 传播分析
     * @param urlName 微博地址
     * @return
     * @throws TRSException
     */
    public Map<String, Object> spreadAnalysis(String urlName)  throws TRSException;
    /**
     * 被转载数趋势图
     * @param urlName  微博地址
     * @return
     * @throws OperationException
     * @throws TRSException
     */
    public Map<String, Object> forwardedTrendMap(String urlName)  throws TRSException;

    /**
     * 传播路径
     * @param urlName
     * @return
     */
    public SpreadObject spreadPath(String urlName) throws TRSException;

    /**
     * 重新优化传播路径逻辑
     * @param urlName
     * @return
     * @throws TRSException
     */
    public SpreadObject spreadPathNew(String urlName) throws TRSException;

    /**
     * 核心转发
     * @param urlName
     * @return
     * @throws TRSException
     */
    public Map<String, Object> coreForward(String urlName) throws TRSException;


    /**
     * 意见领袖
     * @param urlName
     * @return
     * @throws TRSException
     */
    public List<SpreadObject> opinionLeaders(String urlName) throws TRSException;

    /**
     * 转发博主地域分析
     * @param urlName
     * @return
     * @throws TRSException
     */
    public List<Map<String, Object>> areaAnalysisOfForWarders(String urlName) throws TRSException;

    /**
     * 转发微博情绪分析
     * @param urlName
     * @return
     * @throws TRSException
     */
    public List<GroupInfo> emojiAnalysisOfForward(String urlName) throws TRSException;

    /**
     * 转发分析微博的微博用户的男女比例
     * @param urlName
     * @return
     * @throws TRSException
     */
    public List<Map<String, String>> genderOfRatio(String urlName) throws TRSException;

    /**
     * 转发分析微博的微博用户中机构认证、个人认证和普通用户的比例
     * @param urlName
     * @return
     * @throws TRSException
     */
    public List<Map<String, String>> certifiedOfRatio(String urlName) throws TRSException;

    /**
     * 博主发文频率
     * @param urlName
     * @return
     * @throws TRSException
     */
    public Map<String, Object> dispatchFrequency(String urlName) throws TRSException;

    /**
     * 博主参与超话
     * @param urlName
     * @return
     * @throws TRSException
     */
    public Map<String, Object> takeSuperLanguage(String urlName) throws TRSException;

    /**
     * 发布情感统计
     * @param urlName
     * @return
     * @throws TRSException
     */
    public Map<String, List<Map<String, String>>> emotionStatistics(String urlName) throws TRSException;

    /**
     *  原发转发占比
     * @param urlName
     * @return
     * @throws TRSException
     */
    public Map<String, Map<String, Long>> primaryForwardRatio(String urlName) throws TRSException;


    /**
     * 导出 被转发趋势图、转发微博情绪分析、男女比例、认证比例、发文情感统计、原发转发占比
     * @param array
     * @return
     * @throws IOException
     */
    public ByteArrayOutputStream exportSingleChart(String dataType,JSONArray array) throws IOException;

    /**
     * 导出 被转发趋势图
     * @param array
     * @return
     * @throws IOException
     */
    public ByteArrayOutputStream exportSingleChartLine(JSONArray array) throws IOException;
    /**
     * 导出 转发博主情绪分析 数据
     * @param array
     * @return
     * @throws IOException
     */
    public ByteArrayOutputStream exportSingleMap(JSONArray array) throws IOException;






    public Object testSinaUser(String urlName) throws TRSException;

    public Object testStatusData(String urlName,boolean first) throws TRSException;


    public Object forwardedTrendMapTest(String urlName,String timeRange)  throws OperationException,TRSException;
}
