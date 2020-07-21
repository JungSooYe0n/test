package com.trs.netInsight.widget.alert.service;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.alert.entity.AlertSend;
import com.trs.netInsight.widget.alert.entity.PageAlertSend;
import com.trs.netInsight.widget.alert.entity.enums.AlertSource;
import org.springframework.data.domain.Page;

/**
 * app(目前针对app)预警推送提示业务层接口
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/4/22 17:22.
 * @desc
 */
public interface IAlertSendService {
    /**
     *
     * 添加app(目前针对app)预警推送记录
     * @param alertSend
     * @return
     * @throws OperationException
     */
    public AlertSend add(AlertSend alertSend) throws OperationException;

    /**
     * 删除app预警推送记录
     * @param alertSend
     */
    public void delete(AlertSend alertSend);

    /**
     *
     * 查询一条app预警推送记录
     * @param id
     * @return
     * @throws OperationException
     */
    public PageAlertSend findOne(String id,String userId) throws TRSException;

    /**
     * 查询 APP推送
     * @param pageNo
     * @param pageSize
     * @param userId
     * @param sendType
     * @return
     */
    public PageAlertSend findByUserIdAndSendType(int pageNo, int pageSize, String userId, String sendType);
}
