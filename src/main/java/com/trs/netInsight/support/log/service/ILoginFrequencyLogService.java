package com.trs.netInsight.support.log.service;

import com.trs.netInsight.widget.user.entity.User;

public interface ILoginFrequencyLogService {
    void save(Integer num, String  id);

}
