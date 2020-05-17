package com.trs.netInsight.widget.analysis.service.impl;

import com.trs.netInsight.widget.analysis.entity.BigScreenDistrictInfo;
import com.trs.netInsight.widget.analysis.repository.BigScreenDistrictInfoRepository;
import com.trs.netInsight.widget.analysis.service.IBigScreenDistrictInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 大屏 地域信息 业务实现类
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/12/19.
 * @desc
 */
@Service
public class BigScreenDistrictInfoServiceImpl implements IBigScreenDistrictInfoService {
    @Autowired
    private BigScreenDistrictInfoRepository bigScreenDistrictInfoRepository;
    @Override
    public List<BigScreenDistrictInfo> findAll() {
        return bigScreenDistrictInfoRepository.findAll();
    }

    @Override
    public void save(List<BigScreenDistrictInfo> bigScreenDistrictInfos) {
        bigScreenDistrictInfoRepository.save(bigScreenDistrictInfos);
    }
}
