package com.trs.netInsight.widget.column.entity.pageShow;

import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.emuns.ColumnFlag;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class IndexPageDTO implements Serializable {

    private String id;
    private String name;
    private Integer flag = ColumnFlag.IndexPageFlag.ordinal();
    private Integer flagSort;
    private Boolean show = false;


    private Boolean hide = false;
    private Boolean active = false;
    public void setActive(Boolean active){
        this.active = active;
    }
    private Object children;
    public void setChildren(Object children){
        this.children = children;
    }

    private Boolean topFlag = false;

    public IndexPageDTO(){}
    public IndexPageDTO(IndexPage indexPage){
        this.id = indexPage.getId();
        this.name = indexPage.getName();
        this.hide = indexPage.isHide();
    }
    public IndexPageDTO(IndexPage indexPage,Integer level){
        this.id = indexPage.getId();
        this.name = indexPage.getName();
        this.flagSort = level;
        this.hide = indexPage.isHide();
    }

}
