package com.trs.netInsight.widget.microblog.task;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.template.ObjectContainer;
import com.trs.netInsight.util.DateUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.widget.microblog.constant.MicroblogConst;
import com.trs.netInsight.widget.microblog.entity.SingleMicroblogData;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogDataService;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogService;
import com.trs.netInsight.widget.user.entity.User;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @desc 单条微博数据查询存储任务
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/2/14 11:20.
 */
@Slf4j
public class MicroblogDataTask implements Runnable{

    //单条微博分析数据查询服务层
    private ISingleMicroblogService singleMicroblogService = (ISingleMicroblogService)ObjectContainer.getBean(ISingleMicroblogService.class);

    //mongoDB操作服务层
    private ISingleMicroblogDataService singleMicroblogDataService = (ISingleMicroblogDataService)ObjectContainer.getBean(ISingleMicroblogDataService.class);


    private String originalUrl;

    private String currentUrl;

    private User user;

    private String random;

    public MicroblogDataTask(String originalUrl,String currentUrl,User user,String random){
        this.originalUrl = originalUrl;
        this.currentUrl = currentUrl;
        this.user = user;
        this.random = random;
    }
    @Override
    public void run() {
        //若分析途中 将该条微博删除，则下面分析结果讲不做查询
        SingleMicroblogData microblogList = singleMicroblogDataService.findSMDBySth(user, currentUrl, MicroblogConst.MICROBLOGLIST,random);
        if (ObjectUtil.isNotEmpty(microblogList)){
            //微博详情
            SingleMicroblogData microblogDetail = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.MICROBLOGDETAIL);

            SingleMicroblogData microblogDetailNew = new SingleMicroblogData(MicroblogConst.MICROBLOGDETAIL,originalUrl,currentUrl);
            microblogDetailNew.setUserId(user.getId());
            microblogDetailNew.setSubGroupId(user.getSubGroupId());
            microblogDetailNew.setRandom(random);
            try {
                Object blogDetail = singleMicroblogService.microBlogDetail(originalUrl);
                microblogDetailNew.setData(blogDetail);
                microblogDetailNew.setLastModifiedTime(new Date());
            } catch (TRSException e) {
                log.error(MicroblogConst.MICROBLOGDETAIL,e);
            }
            singleMicroblogDataService.save(microblogDetailNew);

            if (ObjectUtil.isNotEmpty(microblogDetail)){
                singleMicroblogDataService.delete(microblogDetail.getId());
            }
        }

        //若分析途中 将该条微博删除，则下面分析结果讲不做查询
        microblogList = singleMicroblogDataService.findSMDBySth(user, currentUrl, MicroblogConst.MICROBLOGLIST,random);
        if (ObjectUtil.isNotEmpty(microblogList)) {
            //传播分析
            SingleMicroblogData microblogAnalysis = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.SPREADANALYSIS);

            SingleMicroblogData microblogAnalysisNew = new SingleMicroblogData(MicroblogConst.SPREADANALYSIS, originalUrl, currentUrl);
            microblogAnalysisNew.setUserId(user.getId());
            microblogAnalysisNew.setSubGroupId(user.getSubGroupId());
            microblogAnalysisNew.setLastModifiedTime(new Date());
            microblogAnalysisNew.setRandom(random);
            try {
                Object spreadAnalysis = singleMicroblogService.spreadAnalysis(originalUrl);
                microblogAnalysisNew.setData(spreadAnalysis);
            } catch (TRSException e) {
                log.error(MicroblogConst.SPREADANALYSIS, e);
            }
            singleMicroblogDataService.save(microblogAnalysisNew);
            if (ObjectUtil.isNotEmpty(microblogAnalysis)){
                singleMicroblogDataService.delete(microblogAnalysis.getId());
            }
        }

        //若分析途中 将该条微博删除，则下面分析结果讲不做查询
        microblogList = singleMicroblogDataService.findSMDBySth(user, currentUrl, MicroblogConst.MICROBLOGLIST,random);
        if (ObjectUtil.isNotEmpty(microblogList)) {
            //被转载趋势图
            SingleMicroblogData microblogForwardTrend = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.FORWARDEDTREND);

             SingleMicroblogData microblogForwardTrendNew = new SingleMicroblogData(MicroblogConst.FORWARDEDTREND, originalUrl, currentUrl);
            microblogForwardTrendNew.setUserId(user.getId());
            microblogForwardTrendNew.setSubGroupId(user.getSubGroupId());
            microblogForwardTrendNew.setRandom(random);
            microblogForwardTrendNew.setLastModifiedTime(new Date());
            try {
                Object forwardedTrendMap = singleMicroblogService.forwardedTrendMap(originalUrl);
                microblogForwardTrendNew.setData(forwardedTrendMap);
            } catch (TRSException e) {
                log.error(MicroblogConst.FORWARDEDTREND, e);
            }
            singleMicroblogDataService.save(microblogForwardTrendNew);
            if (ObjectUtil.isNotEmpty(microblogForwardTrend)){
                singleMicroblogDataService.delete(microblogForwardTrend.getId());
            }
        }

        //若分析途中 将该条微博删除，则下面分析结果讲不做查询
        microblogList = singleMicroblogDataService.findSMDBySth(user, currentUrl, MicroblogConst.MICROBLOGLIST,random);
        if (ObjectUtil.isNotEmpty(microblogList)) {
            //传播路径
            SingleMicroblogData microblogPath = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.SPREADPATH);

            SingleMicroblogData microblogPathNew = new SingleMicroblogData(MicroblogConst.SPREADPATH, originalUrl, currentUrl);
            microblogPathNew.setUserId(user.getId());
            microblogPathNew.setSubGroupId(user.getSubGroupId());
            microblogPathNew.setRandom(random);
            microblogPathNew.setLastModifiedTime(new Date());
            try {
                Object spreadPath = singleMicroblogService.spreadPathNew(originalUrl);
                microblogPathNew.setData(spreadPath);
            } catch (TRSException e) {
                log.error(MicroblogConst.SPREADPATH, e);
            }
            singleMicroblogDataService.save(microblogPathNew);
            if (ObjectUtil.isNotEmpty(microblogPath)){
                singleMicroblogDataService.delete(microblogPath.getId());
            }
        }

        //若分析途中 将该条微博删除，则下面分析结果讲不做查询
        microblogList = singleMicroblogDataService.findSMDBySth(user, currentUrl, MicroblogConst.MICROBLOGLIST,random);
        if (ObjectUtil.isNotEmpty(microblogList)) {
            //核心转发
            SingleMicroblogData microblogCore = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.COREFORWARD);

            SingleMicroblogData microblogCoreNew = new SingleMicroblogData(MicroblogConst.COREFORWARD, originalUrl, currentUrl);
            microblogCoreNew.setUserId(user.getId());
            microblogCoreNew.setSubGroupId(user.getSubGroupId());
            microblogCoreNew.setRandom(random);
            microblogCoreNew.setLastModifiedTime(new Date());
            try {
                Object coreForward = singleMicroblogService.coreForward(originalUrl);
                microblogCoreNew.setData(coreForward);
            } catch (TRSException e) {
                log.error(MicroblogConst.COREFORWARD, e);
            }
            singleMicroblogDataService.save(microblogCoreNew);
            if (ObjectUtil.isNotEmpty(microblogCore)){
                singleMicroblogDataService.delete(microblogCore.getId());
            }
        }

        //若分析途中 将该条微博删除，则下面分析结果讲不做查询
        microblogList = singleMicroblogDataService.findSMDBySth(user, currentUrl, MicroblogConst.MICROBLOGLIST,random);
        if (ObjectUtil.isNotEmpty(microblogList)) {
            //意见领袖
            SingleMicroblogData microblogLeaders = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.OPINIONLEADERS);

               SingleMicroblogData microblogLeadersNew = new SingleMicroblogData(MicroblogConst.OPINIONLEADERS, originalUrl, currentUrl);
            microblogLeadersNew.setUserId(user.getId());
            microblogLeadersNew.setSubGroupId(user.getSubGroupId());
            microblogLeadersNew.setRandom(random);
            microblogLeadersNew.setLastModifiedTime(new Date());
            try {
                Object opinionLeaders = singleMicroblogService.opinionLeaders(originalUrl);
                microblogLeadersNew.setData(opinionLeaders);
            } catch (TRSException e) {
                log.error(MicroblogConst.OPINIONLEADERS, e);
            }
            singleMicroblogDataService.save(microblogLeadersNew);
            if (ObjectUtil.isNotEmpty(microblogLeaders)){
                singleMicroblogDataService.delete(microblogLeaders.getId());
            }
        }

        //若分析途中 将该条微博删除，则下面分析结果讲不做查询
        microblogList = singleMicroblogDataService.findSMDBySth(user, currentUrl, MicroblogConst.MICROBLOGLIST,random);
        if (ObjectUtil.isNotEmpty(microblogList)) {
            //转发博主地域分析
            SingleMicroblogData microblogArea = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.AREAANALYSISOFFORWARDERS);

              SingleMicroblogData  microblogAreaNew = new SingleMicroblogData(MicroblogConst.AREAANALYSISOFFORWARDERS, originalUrl, currentUrl);
            microblogAreaNew.setUserId(user.getId());
            microblogAreaNew.setSubGroupId(user.getSubGroupId());
            microblogAreaNew.setRandom(random);
            microblogAreaNew.setLastModifiedTime(new Date());
            try {
                Object areaAnalysisOfForWarders = singleMicroblogService.areaAnalysisOfForWarders(originalUrl);
                microblogAreaNew.setData(areaAnalysisOfForWarders);
            } catch (TRSException e) {
                log.error(MicroblogConst.AREAANALYSISOFFORWARDERS, e);
            }
            singleMicroblogDataService.save(microblogAreaNew);
            if (ObjectUtil.isNotEmpty(microblogArea)){
                singleMicroblogDataService.delete(microblogArea.getId());
            }
        }

        //若分析途中 将该条微博删除，则下面分析结果讲不做查询
        microblogList = singleMicroblogDataService.findSMDBySth(user, currentUrl, MicroblogConst.MICROBLOGLIST,random);
        if (ObjectUtil.isNotEmpty(microblogList)) {
            //转发微博情绪分析
            SingleMicroblogData microblogEmoji = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.EMOJIANALYSISOFFORWARD);

            SingleMicroblogData microblogEmojiNew = new SingleMicroblogData(MicroblogConst.EMOJIANALYSISOFFORWARD, originalUrl, currentUrl);
            microblogEmojiNew.setUserId(user.getId());
            microblogEmojiNew.setSubGroupId(user.getSubGroupId());
            microblogEmojiNew.setRandom(random);
            microblogEmojiNew.setLastModifiedTime(new Date());
            try {
                Object analysisOfForward = singleMicroblogService.emojiAnalysisOfForward(originalUrl);
                microblogEmojiNew.setData(analysisOfForward);
            } catch (TRSException e) {
                log.error(MicroblogConst.EMOJIANALYSISOFFORWARD, e);
            }
            singleMicroblogDataService.save(microblogEmojiNew);
            if (ObjectUtil.isNotEmpty(microblogEmoji)){
                singleMicroblogDataService.delete(microblogEmoji.getId());
            }
        }

        //若分析途中 将该条微博删除，则下面分析结果讲不做查询
        microblogList = singleMicroblogDataService.findSMDBySth(user, currentUrl, MicroblogConst.MICROBLOGLIST,random);
        if (ObjectUtil.isNotEmpty(microblogList)) {
            //男女比例
            SingleMicroblogData microblogGender = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.GENDEROFRATIO);

            SingleMicroblogData microblogGenderNew = new SingleMicroblogData(MicroblogConst.GENDEROFRATIO, originalUrl, currentUrl);
            microblogGenderNew.setUserId(user.getId());
            microblogGenderNew.setSubGroupId(user.getSubGroupId());
            microblogGenderNew.setRandom(random);
            microblogGenderNew.setLastModifiedTime(new Date());
            try {
                Object genderOfRatio = singleMicroblogService.genderOfRatio(originalUrl);
                microblogGenderNew.setData(genderOfRatio);
            } catch (TRSException e) {
                log.error(MicroblogConst.GENDEROFRATIO, e);
            }
            singleMicroblogDataService.save(microblogGenderNew);
            if (ObjectUtil.isNotEmpty(microblogGender)){
                singleMicroblogDataService.delete(microblogGender.getId());
            }
        }

        //若分析途中 将该条微博删除，则下面分析结果讲不做查询
        microblogList = singleMicroblogDataService.findSMDBySth(user, currentUrl, MicroblogConst.MICROBLOGLIST,random);
        if (ObjectUtil.isNotEmpty(microblogList)) {
            //认证比例
            SingleMicroblogData microblogCertified = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.CERTIFIEDOFRATIO);

            SingleMicroblogData microblogCertifiedNew = new SingleMicroblogData(MicroblogConst.CERTIFIEDOFRATIO, originalUrl, currentUrl);
            microblogCertifiedNew.setUserId(user.getId());
            microblogCertifiedNew.setSubGroupId(user.getSubGroupId());
            microblogCertifiedNew.setRandom(random);
            microblogCertifiedNew.setLastModifiedTime(new Date());
            try {
                Object certifiedOfRatio = singleMicroblogService.certifiedOfRatio(originalUrl);
                microblogCertifiedNew.setData(certifiedOfRatio);
            } catch (TRSException e) {
                log.error(MicroblogConst.CERTIFIEDOFRATIO, e);
            }
            singleMicroblogDataService.save(microblogCertifiedNew);
            if (ObjectUtil.isNotEmpty(microblogCertified)){
                singleMicroblogDataService.delete(microblogCertified.getId());
            }
        }

        //若分析途中 将该条微博删除，则下面分析结果讲不做查询
        microblogList = singleMicroblogDataService.findSMDBySth(user, currentUrl, MicroblogConst.MICROBLOGLIST,random);
        if (ObjectUtil.isNotEmpty(microblogList)) {
            //博主发文频率
            SingleMicroblogData microblogDis = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.DISPATCHFREQUENCY);

               SingleMicroblogData microblogDisNew = new SingleMicroblogData(MicroblogConst.DISPATCHFREQUENCY, originalUrl, currentUrl);
            microblogDisNew.setUserId(user.getId());
            microblogDisNew.setSubGroupId(user.getSubGroupId());
            microblogDisNew.setRandom(random);
            microblogDisNew.setLastModifiedTime(new Date());
            try {
                Object dispatchFrequency = singleMicroblogService.dispatchFrequency(originalUrl);
                microblogDisNew.setData(dispatchFrequency);
            } catch (TRSException e) {
                log.error(MicroblogConst.DISPATCHFREQUENCY, e);
            }
            singleMicroblogDataService.save(microblogDisNew);
            if (ObjectUtil.isNotEmpty(microblogDis)){
                singleMicroblogDataService.delete(microblogDis.getId());
            }
        }

        //若分析途中 将该条微博删除，则下面分析结果讲不做查询
        microblogList = singleMicroblogDataService.findSMDBySth(user, currentUrl, MicroblogConst.MICROBLOGLIST,random);
        if (ObjectUtil.isNotEmpty(microblogList)) {
            //参与话题统计
            SingleMicroblogData microblogTake = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.TAKESUPERLANGUAGE);

            SingleMicroblogData microblogTakeNew = new SingleMicroblogData(MicroblogConst.TAKESUPERLANGUAGE, originalUrl, currentUrl);
            microblogTakeNew.setUserId(user.getId());
            microblogTakeNew.setSubGroupId(user.getSubGroupId());
            microblogTakeNew.setRandom(random);
            microblogTakeNew.setLastModifiedTime(new Date());
            try {
                Object superLanguage = singleMicroblogService.takeSuperLanguage(originalUrl);
                microblogTakeNew.setData(superLanguage);
            } catch (TRSException e) {
                log.error(MicroblogConst.TAKESUPERLANGUAGE, e);
            }
            singleMicroblogDataService.save(microblogTakeNew);
            if (ObjectUtil.isNotEmpty(microblogTake)){
                singleMicroblogDataService.delete(microblogTake.getId());
            }
        }

        //若分析途中 将该条微博删除，则下面分析结果讲不做查询
        microblogList = singleMicroblogDataService.findSMDBySth(user, currentUrl, MicroblogConst.MICROBLOGLIST,random);
        if (ObjectUtil.isNotEmpty(microblogList)) {
            //发文情感统计
            SingleMicroblogData microblogEmotion = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.EMOTIONSTATISTICS);

            SingleMicroblogData microblogEmotionNew = new SingleMicroblogData(MicroblogConst.EMOTIONSTATISTICS, originalUrl, currentUrl);
            microblogEmotionNew.setUserId(user.getId());
            microblogEmotionNew.setSubGroupId(user.getSubGroupId());
            microblogEmotionNew.setRandom(random);
            microblogEmotionNew.setLastModifiedTime(new Date());
            try {
                Object emotionStatistics = singleMicroblogService.emotionStatistics(originalUrl);
                microblogEmotionNew.setData(emotionStatistics);
            } catch (TRSException e) {
                log.error(MicroblogConst.EMOTIONSTATISTICS, e);
            }
            singleMicroblogDataService.save(microblogEmotionNew);
            if (ObjectUtil.isNotEmpty(microblogEmotion)){
                singleMicroblogDataService.delete(microblogEmotion.getId());
            }
        }

        //若分析途中 将该条微博删除，则下面分析结果讲不做查询
        microblogList = singleMicroblogDataService.findSMDBySth(user, currentUrl, MicroblogConst.MICROBLOGLIST,random);
        if (ObjectUtil.isNotEmpty(microblogList)) {
            //原发转发占比
            SingleMicroblogData microblogPF = singleMicroblogDataService.findSMDBySthNoRandom(user, currentUrl, MicroblogConst.PRIMARYFORWARDRATIO);

            SingleMicroblogData microblogPFNew = new SingleMicroblogData(MicroblogConst.PRIMARYFORWARDRATIO, originalUrl, currentUrl);
            microblogPFNew.setUserId(user.getId());
            microblogPFNew.setSubGroupId(user.getSubGroupId());
            microblogPFNew.setRandom(random);
            microblogPFNew.setLastModifiedTime(new Date());
            try {
                Object primaryForwardRatio = singleMicroblogService.primaryForwardRatio(originalUrl);
                microblogPFNew.setData(primaryForwardRatio);
            } catch (TRSException e) {
                log.error(MicroblogConst.PRIMARYFORWARDRATIO, e);
            }
            singleMicroblogDataService.save(microblogPFNew);
            if (ObjectUtil.isNotEmpty(microblogPF)){
                singleMicroblogDataService.delete(microblogPF.getId());
            }
        }
    }
}