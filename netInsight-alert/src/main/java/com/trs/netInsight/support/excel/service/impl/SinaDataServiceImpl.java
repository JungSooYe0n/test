package com.trs.netInsight.support.excel.service.impl;

import com.trs.netInsight.support.excel.entity.SinaData;
import com.trs.netInsight.support.excel.repository.SinaDataRepository;
import com.trs.netInsight.support.excel.service.ISinaDataService;
import com.trs.netInsight.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/11/15.
 * @desc
 */
@Service
public class SinaDataServiceImpl implements ISinaDataService {
    @Autowired
    private SinaDataRepository sinaDataRepository;
    @Override
    public SinaData save(SinaData sinaData) {
        return sinaDataRepository.save(sinaData);
    }

    @Override
    public List<SinaData> findByType(String type) {
        return sinaDataRepository.findAllByTypeAndUserId(type,UserUtils.getUser().getId());
    }

    @Override
    public SinaData findByUid(String uid,String type) {
        return sinaDataRepository.findByUidAndUserIdAndType(uid,UserUtils.getUser().getId(),type);
    }
}
