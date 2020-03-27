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
 * 单条微博分析 转发博主地域分析
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/2/28 17:43.
 * @desc
 */
@Slf4j
public class AreaAnalysisForwardersTask implements Runnable {
    //单条微博分析数据查询服务层
    private ISingleMicroblogService singleMicroblogService = (ISingleMicroblogService)ObjectContainer.getBean(ISingleMicroblogService.class);

    //mongoDB操作服务层
    private ISingleMicroblogDataService singleMicroblogDataService = (ISingleMicroblogDataService)ObjectContainer.getBean(ISingleMicroblogDataService.class);


    private String originalUrl;

    private String currentUrl;

    private User user;

    private String random;

    public AreaAnalysisForwardersTask(String originalUrl,String currentUrl,User user,String random){
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

    }
}
