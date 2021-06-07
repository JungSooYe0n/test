package com.trs.netInsight.widget.column.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "hot_top")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class HotTop extends BaseEntity {
    /**
     * 榜单名称
     */
    @Column(name="name")
    private String name;
    /**
     * 顺序
     */
    @Column(name = "sequence")
    private Integer sequence;
    /**
     * 是否隐藏
     */
    @Column(name = "hide")
    private boolean hide = false;
    @Column(name = "children_sort")
    private String childrenSort;
    public HotTop(String name,Integer sequence,boolean hide,String childrenSort){
        this.name = name;
        this.sequence = sequence;
        this.hide = hide;
        this.childrenSort = childrenSort;
    }
}
