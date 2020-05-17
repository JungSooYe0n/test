package com.trs.netInsight.widget.analysis.service;

import com.trs.netInsight.widget.analysis.entity.BigScreenDistrictInfo;

import java.util.List;

/**
 * 大屏 地域信息 业务层
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/12/19.
 * @desc
 */
public interface IBigScreenDistrictInfoService {
    //查询所有
    public List<BigScreenDistrictInfo> findAll();

    //添加
    public void save(List<BigScreenDistrictInfo> bigScreenDistrictInfos);
}
