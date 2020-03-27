package com.trs.netInsight.widget.microblog.task;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.template.ObjectContainer;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.widget.microblog.constant.MicroblogConst;
import com.trs.netInsight.widget.microblog.entity.SingleMicroblogData;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogDataService;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogService;
import com.trs.netInsight.widget.user.entity.User;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 单条微博分析 传播分析
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/2/28 17:32.
 * @desc
 */
@Slf4j
public class SpreadAnalysisTask implements Runnable  {
    //单条微博分析数据查询服务层
    private ISingleMicroblogService singleMicroblogService = (ISingleMicroblogService)ObjectContainer.getBean(ISingleMicroblogService.class);

    //mongoDB操作服务层
    private ISingleMicroblogDataService singleMicroblogDataService = (ISingleMicroblogDataService)ObjectContainer.getBean(ISingleMicroblogDataService.class);


    private String originalUrl;

    private String currentUrl;

    private User user;

    private String random;

    public SpreadAnalysisTask(String originalUrl,String currentUrl,User user,String random){
        this.originalUrl = originalUrl;
        this.currentUrl = currentUrl;
        this.user = user;
        this.random = random;
    }
    @Override
    public void run() {
        //若分析途中 将该条微博删除，则下面分析结果讲不做查询
        SingleMicroblogData microblogList = singleMicroblogDataService.findSMDBySth(user, currentUrl, MicroblogConst.MICROBLOGLIST,random);
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
    }
}
