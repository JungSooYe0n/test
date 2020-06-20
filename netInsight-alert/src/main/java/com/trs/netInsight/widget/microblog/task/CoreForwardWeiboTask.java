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
@Slf4j
public class CoreForwardWeiboTask implements Runnable{
    //单条微博分析数据查询服务层
    private ISingleMicroblogService singleMicroblogService = (ISingleMicroblogService)ObjectContainer.getBean(ISingleMicroblogService.class);

    //mongoDB操作服务层
    private ISingleMicroblogDataService singleMicroblogDataService = (ISingleMicroblogDataService)ObjectContainer.getBean(ISingleMicroblogDataService.class);


    private String originalUrl;

    private String currentUrl;

    private User user;

    private String random;
    public CoreForwardWeiboTask(String originalUrl,String currentUrl,User user,String random){
        this.originalUrl = originalUrl;
        this.currentUrl = currentUrl;
        this.user = user;
        this.random = random;
    }
    @Override
    public void run() {
        //若分析途中 将该条微博删除，则下面分析结果讲不做查询
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
}
