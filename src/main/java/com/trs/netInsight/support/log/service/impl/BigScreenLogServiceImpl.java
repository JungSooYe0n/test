package com.trs.netInsight.support.log.service.impl;

import com.trs.netInsight.support.log.entity.BigScreenLog;
import com.trs.netInsight.support.log.repository.BigScreenLogRepository;
import com.trs.netInsight.support.log.service.IBigScreenLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author lilyy
 * @date 2020/2/20 11:27
 */
@Service
@Slf4j
public class BigScreenLogServiceImpl implements IBigScreenLogService {

    @Autowired
    private BigScreenLogRepository bigScreenLogRepository;

    @Override
    public BigScreenLog save(BigScreenLog bigScreenLog) {
        return bigScreenLogRepository.save(bigScreenLog);
    }
}
