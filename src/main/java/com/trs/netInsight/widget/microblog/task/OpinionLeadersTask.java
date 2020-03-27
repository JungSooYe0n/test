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
 * 单条微博分析 意见领袖
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/2/28 17:41.
 * @desc
 */
@Slf4j
public class OpinionLeadersTask implements Runnable {
    //单条微博分析数据查询服务层
    private ISingleMicroblogService singleMicroblogService = (ISingleMicroblogService)ObjectContainer.getBean(ISingleMicroblogService.class);

    //mongoDB操作服务层
    private ISingleMicroblogDataService singleMicroblogDataService = (ISingleMicroblogDataService)ObjectContainer.getBean(ISingleMicroblogDataService.class);


    private String originalUrl;

    private String currentUrl;

    private User user;

    private String random;

    public OpinionLeadersTask(String originalUrl,String currentUrl,User user,String random){
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

    }
}
