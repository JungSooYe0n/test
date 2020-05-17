package com.trs.netInsight.support.Yiqing.service.impl;

import com.trs.netInsight.support.Yiqing.entity.Yiqing;
import com.trs.netInsight.support.Yiqing.repository.YiqingRepository;
import com.trs.netInsight.support.Yiqing.service.IYiqingService;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.ReadDayBaogao;
import com.trs.netInsight.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * API业务层接口实现类
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/4/10 18:39.
 * @desc
 */
@Service
public class YiqingServiceImpl implements IYiqingService {

    @Autowired
    YiqingRepository yiqingRepository;

    @Override
    public void readTxt(String name,String file) {
        String json = new ReadDayBaogao().readTxt(null);
        Yiqing yiqing = new Yiqing();
        yiqing.setName(name);
        yiqing.setValue(json);
        yiqingRepository.save(yiqing);
    }

    @Override
    public Yiqing getData(String name) {
        return yiqingRepository.findByName(name);
    }
}
