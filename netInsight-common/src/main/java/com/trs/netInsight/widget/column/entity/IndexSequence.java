package com.trs.netInsight.widget.column.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.column.entity.emuns.IndexFlag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "index_sequence")
@Table
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IndexSequence extends BaseEntity {
    /**
     * 分组id
     */
    @Column(name = "parent_id")
    private String parentId;
    /**
     * 对应元素id
     */
    @Column(name = "index_id")
    private String indexId;
    /**
     * 栏目id  针对自定义和统计
     */
    @Column(name = "index_tab_id")
    private String indexTabId;
    /**
     * 类型
     */
    @Column(name = "index_flag")
    private IndexFlag indexFlag;
    /**
     * 序号
     */
    @Column(name = "sequence")
    private Integer sequence;
}
