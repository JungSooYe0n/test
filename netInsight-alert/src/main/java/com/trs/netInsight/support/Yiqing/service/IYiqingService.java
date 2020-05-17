package com.trs.netInsight.support.Yiqing.service;

import com.trs.netInsight.support.Yiqing.entity.Yiqing;
import com.trs.netInsight.support.fts.entity.FtsDocumentCommonVO;

/**
 * api业务层接口
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/4/10 18:39.
 * @desc
 */
public interface IYiqingService {

    void readTxt(String name,String file);
    Yiqing getData(String name);
}
