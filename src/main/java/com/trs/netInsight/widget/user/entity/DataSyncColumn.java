package com.trs.netInsight.widget.user.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 同步数据 时 传参 映射 实体(日常监测)
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/7/24 17:13.
 * @desc
 */
@Getter
@Setter
@NoArgsConstructor
public class DataSyncColumn implements Serializable {


    private static final long serialVersionUID = 4788827717009245239L;

    private String indexPageName;

    private String definedself;

    private String id;

    private List<DataSyncColumn> list;


}
