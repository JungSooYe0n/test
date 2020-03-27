package com.trs.netInsight.support.excel.service;

import com.trs.netInsight.support.excel.entity.SinaData;

import java.util.List;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/11/15.
 * @desc
 */
public interface ISinaDataService {
    //添加
    public SinaData save(SinaData sinaData);
    //查询
    public List<SinaData> findByType(String type);

    public SinaData findByUid(String uid,String type);
}
