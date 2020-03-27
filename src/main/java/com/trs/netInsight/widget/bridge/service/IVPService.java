package com.trs.netInsight.widget.bridge.service;

import com.trs.netInsight.handler.exception.AuthorityException;
import com.trs.netInsight.widget.bridge.entity.enums.PropsType;

/**
 * 虚拟道具服务类
 *
 * Created by ChangXiaoyang on 2017/9/14.
 */
public interface IVPService {

    /**
     * 检验可用方案点数
     */
    public void checkProps( PropsType type) throws AuthorityException;

}
