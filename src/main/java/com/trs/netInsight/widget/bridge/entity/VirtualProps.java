package com.trs.netInsight.widget.bridge.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.bridge.entity.enums.PropsType;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户虚拟道具
 *
 * Created by ChangXiaoyang on 2017/9/11.
 */
@Entity
@Table(name = "virtual_props")
@Setter
@Getter
public class VirtualProps extends BaseEntity {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7342679914228974105L;


    /**
     * 剩余可用点数
     */
    private int usableProps = 5;

    /**
     * 已消费的点数
     */
    private int usedProps = 0;

    /**
     * 道具类型
     */
    private PropsType propsType;

}
