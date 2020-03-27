package com.trs.netInsight.widget.analysis.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
/**
 * @Desc 用来给前端返回各级专题名称
 * @author yang.yanyan
 * @date 2018/3/5  17:03
 * @version
 */
@Setter
@Getter
@NoArgsConstructor
public class SpecialParam implements Serializable {

    private static final long serialVersionUID = 8120907343484231169L;

    /**
     * 一级专题名字
     */
    private String firstName;

    /**
     * 二级专题名字
     */
    private String secondName;

    /**
     * 三级专题名字
     */
    private String thirdName;

    public SpecialParam(String firstName, String secondName, String thirdName) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.thirdName = thirdName;
    }

    @Override
    public String toString() {
        return "SpecialParam{" +
                "firstName='" + firstName + '\'' +
                ", secondName='" + secondName + '\'' +
                ", thirdName='" + thirdName + '\'' +
                '}';
    }
}
