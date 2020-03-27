package com.trs.netInsight.widget.special.entity;


import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 信息列表异步展示信息
 *
 * Created by ChangXiaoyang on 2017/5/8.
 */
@Getter
@Data
@NoArgsConstructor
public class AsyncDocument implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8181523299716823999L;

	/**
     * 文档SID
     */
    private String id;

    /**
     * 相似文章数量
     */
    private Long simNum;

    /**
     * 头像url
     */
    private String imgUrl;

    /**
     * 收藏 true otherwise false
     */
    private boolean favourite;
    
    /**
     * 是否预警 true 已预警 false未预警
     */
    private boolean send;

}
