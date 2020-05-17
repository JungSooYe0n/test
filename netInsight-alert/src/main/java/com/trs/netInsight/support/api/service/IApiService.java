package com.trs.netInsight.support.api.service;

import com.trs.dev4.jdk16.dao.PagedList;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;

/**
 * api业务层接口
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/4/10 18:39.
 * @desc
 */
public interface IApiService {

    public PagedList<FtsDocumentCommonVO> expertSearch(String trsl,String sources,String sort,String time,String emtion,String invitationCard,String forwarPrimary,int pageNo,int pageSize ) throws OperationException;
}
