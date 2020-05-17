package com.trs.netInsight.support.api.service;

/**
 * api频率日志记录相关服务接口
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since zhangya @ 2019年10月16日
 */
public interface IApiFrequencyLogService {

    /**
     * 新增
     *
     * @param questClientId
     * @param questOrgId
     * @param questMethodCode
     * @param questMethod
     * @return
     */
    public void recordFrequency(String  questClientId, String questOrgId, Integer questMethodCode,String questMethod);

}
