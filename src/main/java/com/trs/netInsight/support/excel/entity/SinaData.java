package com.trs.netInsight.support.excel.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/11/15.
 * @desc
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "`sina_data`")
public class SinaData extends BaseEntity {
    private static final long serialVersionUID = -1291452525256802399L;

    @Column(name = "`data`")
    private String data;

    @Column(name = "`count`")
    private long count;

    @Column(name = "`type`")
    private String type;

    @Column(name = "`uid`")
    private String uid;


    @Override
    public String toString() {
        return "SinaData{" +
                "data='" + data + '\'' +
                ", count=" + count +
                ", type='" + type + '\'' +
                ", uid='" + uid + '\'' +
                '}';
    }
}
