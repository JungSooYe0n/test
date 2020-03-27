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
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/2/28 17:58.
 * @desc
 */
@Slf4j
public class TakeSuperLanguageTask implements Runnable {
    //单条微博分析数据查询服务层
    private ISingleMicroblogService singleMicroblogService = (ISingleMicroblogService)ObjectContainer.getBean(ISingleMicroblogService.class);

    //mongoDB操作服务层
    private ISingleMicroblogDataService singleMicroblogDataService = (ISingleMicroblogDataService)ObjectContainer.getBean(ISingleMicroblogDataService.class);


    private String originalUrl;

    private String currentUrl;

    private User user;

    private String random;

    public TakeSuperLanguageTask(String originalUrl,String currentUrl,User user,String random){
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
    }
}
