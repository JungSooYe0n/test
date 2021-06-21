package com.trs.netInsight.support.bigscreen.service;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;

/**
 *  大屏相关业务
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/12/18.
 * @desc
 */
public interface IBigScreenService {

    /**
     * 计算 今日、本周、本月总量
     * @return
     */
    public Object dataCount(String keyWords) throws OperationException;

    /**
     * 数据类型对比
     * @return
     */
    public Object dataTypeAnalysis(String keyWords,String groupName,String timeRange) throws OperationException;

    /**
     *  行业声量排行
     * @return
     */
    public Object industryVoice(String keyWords,String timeRange) throws OperationException;

    /**
     * 一周信息趋势图
     * @return
     */
  //  public Object messageOfWeek(String keyWords,String flag,String timeRange) throws OperationException;

    public Object messageOfWeek(String keyWords,String flag) throws OperationException;


    /**
     * 区域热力图
     * @return
     */
    public Object areaThermogram(String keyWords,String timeRange) throws OperationException;

    /**
     * 话题类型排行
     * @return
     */
    public Object topicType(String keyWords,String timeRange) throws OperationException;

    /**
     *  情感分析
     * @param keyWords
     * @param timeRange
     * @return
     * @throws OperationException
     */
    public Object emotionAnalysis(String keyWords,String timeRange) throws OperationException;

    /**
     * 热点词云
     * @return
     */
    public Object hotWordCloud(String keyWords,String timeRange) throws OperationException;
    public Object mapInfo(String timeRange) throws OperationException;

    /**
     * 热榜
     * @param timeRange
     * @param siteName
     * @param channelName
     * @param keyword
     * @return
     * @throws OperationException
     */
    public Object hotList(String timeRange,String siteName,String channelName,String keyword) throws TRSException;
}
