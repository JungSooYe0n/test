package com.trs.netInsight.widget.user.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 同步数据 时 传参(前端) 映射 实体(专题分析)
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/7/30 20:43.
 * @desc
 */
@Getter
@Setter
@NoArgsConstructor
public class DataSyncSpecial implements Serializable {
    private static final long serialVersionUID = -4554545714385876554L;

    /**
     * 一级 id  （
     */
    private String id;

    /**
     * 级别 或者 专题  0 一级   1 二级
     */
    private int flag;

    /**
     * 二级
     */
    private List<DataSyncSpecial> zhuantiDetail;

}
