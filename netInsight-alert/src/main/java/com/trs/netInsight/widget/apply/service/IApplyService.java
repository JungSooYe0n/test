package com.trs.netInsight.widget.apply.service;

import com.trs.netInsight.widget.apply.entity.Apply;
import com.trs.netInsight.widget.apply.entity.enums.ApplyUserType;

public interface IApplyService {
    Object list(ApplyUserType applyUserType, int pageNo, int pageSize);
    void updateApply(Apply apply);
    Apply findOne(String id);
}
