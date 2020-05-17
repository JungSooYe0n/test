package com.trs.netInsight.widget.report.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 *  导出字段
 *  数据类型和对应字段
 *
 * Created by zhangya 2019/12/03
 */
@Getter
@Setter
@NoArgsConstructor
public class ExportParam {

    private String key;

    private String name;

    private List<ExportField> exportField;

}
