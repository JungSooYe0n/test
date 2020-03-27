package com.trs.netInsight.widget.analysis.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *  大屏 地域信息
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/12/19.
 * @desc
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "bigscreen_district")
public class BigScreenDistrictInfo extends BaseEntity {


    private static final long serialVersionUID = -6353387169972779552L;

    /**
     * 地域名称
     */
    @Column(name = "area_name")
    private String areaName;

    public BigScreenDistrictInfo(String areaName) {
        this.areaName = areaName;
    }
}
