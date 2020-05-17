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
 * 单条微博分析 传播路径
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/2/28 17:37.
 * @desc
 */
@Slf4j
public class SpreadPathTask implements Runnable {
    //单条微博分析数据查询服务层
    private ISingleMicroblogService singleMicroblogService = (ISingleMicroblogService)ObjectContainer.getBean(ISingleMicroblogService.class);

    //mongoDB操作服务层
    private ISingleMicroblogDataService singleMicroblogDataService = (ISingleMicroblogDataService)ObjectContainer.getBean(ISingleMicroblogDataService.class);


    private String originalUrl;

    private String currentUrl;

    private User user;

    private String random;

    public SpreadPathTask(String originalUrl,String currentUrl,User user,String random){
        this.originalUrl = originalUrl;
        this.currentUrl = currentUrl;
        this.user = user;
        this.random = random;
    }
    @Override
    public void run() {
       SingleMicroblogData microblogList = singleMicroblogDataService.findSMDBySth(user, currentUrl, MicroblogConst.MICROBLOGLIST,random);
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
    }
}
