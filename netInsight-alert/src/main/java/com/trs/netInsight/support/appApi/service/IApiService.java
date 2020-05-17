package com.trs.netInsight.support.appApi.service;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;
import com.trs.netInsight.widget.alert.entity.PageAlertSend;

/**
 * api业务层接口
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/4/10 18:39.
 * @desc
 */
public interface IApiService {

    /**
     * 验证用户名和密码获取token
     */
    public Object loginAndGetToken(String userAccount, String passWord) throws TRSException;

    /**
     * 修改三级标签的顺序以及隐藏状态
     */
    public Object thirdColumnOp(String showIds, String hideIds, String parentId);

    /**
     * 获取所有的收藏列表
     * @param userId
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Object getAllFavourites(String userId,String subGroupId, int pageNo, int pageSize);

    /**
     * 查询
     * @param userId
     * @param alertSource
     * @param pageNo
     * @param pageSize
     * @return
     */
    public PageAlertSend getAppAlertData(String userId,String alertSource,int pageNo,int pageSize);

    /**
     * 查询一条
     * @param alertId
     * @return
     */
    public PageAlertSend getOneAlertData(String alertId, String userId);
}
