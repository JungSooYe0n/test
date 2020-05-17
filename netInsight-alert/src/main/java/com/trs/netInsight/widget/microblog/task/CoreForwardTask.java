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
 * 单条微博分析 核心转发
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/2/28 17:38.
 * @desc
 */
@Slf4j
public class CoreForwardTask implements Runnable {
    //单条微博分析数据查询服务层
    private ISingleMicroblogService singleMicroblogService = (ISingleMicroblogService)ObjectContainer.getBean(ISingleMicroblogService.class);

    //mongoDB操作服务层
    private ISingleMicroblogDataService singleMicroblogDataService = (ISingleMicroblogDataService)ObjectContainer.getBean(ISingleMicroblogDataService.class);


    private String originalUrl;

    private String currentUrl;

    private User user;

    private String random;

    public CoreForwardTask(String originalUrl,String currentUrl,User user,String random){
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
}
