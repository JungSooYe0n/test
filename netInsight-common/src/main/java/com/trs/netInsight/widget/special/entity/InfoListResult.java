package com.trs.netInsight.widget.special.entity;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 信息列表包装结构
 *
 * Created by ChangXiaoyang on 2017/5/4.
 */
@NoArgsConstructor
@Data
public class InfoListResult<T> implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 8291643598304750791L;

	private String pageId;

    private Object content;

    private String nextPageId;
    
    private int totalItemCount;//总条数
    
    private int totalList;//总页数
    
    private String trslk;

    public InfoListResult(String pageId, Object content, String nextPageId,long count,String trslk) {
        this.pageId = pageId;
        this.content = content;
        this.nextPageId = nextPageId;
        this.trslk=trslk;
    }
    
    public InfoListResult(Object content, int totalItemCount,int totalList) {
        this.content = content;
        this.totalItemCount=totalItemCount;
        this.totalList=totalList;
    }

}
