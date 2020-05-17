package com.trs.netInsight.widget.report.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/10/22.
 * @desc 舆情报告 极简模式  素材库分组实体类
 */
@SuppressWarnings("serial")
@Getter
@Setter
@Entity
@Table(name = "material_library_new")
public class MaterialLibraryNew extends BaseEntity {
    /**
     * 素材库 组名
     */
    @Column(name = "name")
    private String specialName;


}
