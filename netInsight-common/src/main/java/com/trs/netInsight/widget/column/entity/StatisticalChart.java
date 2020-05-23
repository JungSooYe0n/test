package com.trs.netInsight.widget.column.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 日常监测统计分析图表
 * 一个栏目对应多个统计分析图表
 * 因为日常监测中某个统计分析图表可以进行置顶操作，所以存库
 *统计分析图表的查询条件为其对应栏目的条件
 * @author 张娅
 *
 */
@Entity(name = "statistical_chart")
@Table
@Setter
@Getter
@NoArgsConstructor
public class StatisticalChart extends BaseEntity {

    /**
     * 图类型
     */
    @Column(name = "chart_type")
    private String chartType;

    /**
     * 顺序 - 统计分析暂不可改变顺序
     */
    @Column(name = "sequence")
    private Integer sequence;
    /**
     * 是否置顶 也就是缩略豆腐块时，是否展示
     */
    @Column(name = "is_top")
    private Boolean isTop;

    /**
     * 置顶顺序
     */
    @Column(name = "top_sequence")
    private Integer topSequence;

    /**
     * 图的名字
     */
    @Column(name = "name")
    private String name;

    /**
     * 对应的日常监测的栏目id
     */
    @Column(name = "parent_id")
    private String parentId;

    public StatisticalChart(String chartType ,Integer sequence ,Boolean isTop ,String  name ,String  parentId  ){
        this.chartType = chartType;
        this.sequence = sequence;
        this.isTop = isTop;
        this.name = name;
        this.parentId = parentId;
    }


}
