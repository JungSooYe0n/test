package com.trs.netInsight.widget.column.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonView;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import com.trs.netInsight.widget.home.entity.enums.ColumnType;
import com.trs.netInsight.widget.home.entity.enums.TabType;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 活动栏目
 *
 * Created by trs on 2017/6/19.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "columns")
public class Columns extends BaseEntity{

    /**
	 * 
	 */
	private static final long serialVersionUID = 6396542529327929852L;


    @ApiModelProperty(required = true, notes = "栏目名称")
    @JsonView(DisplayView.class)
    private String columnName;

    /**
     * 栏目类型
     */
    @JsonView(DisplayView.class)
    @ApiModelProperty(required = true, notes = "栏目类型")
    private ColumnType columnType;

    /**
     * 页签关键词
     */
    @JsonView(SaveView.class)
    @ApiModelProperty(notes = "页签关键词")
    private String tabKeywords;

    /**
     * 页签类型
     */
    @JsonView(SaveView.class)
    @ApiModelProperty(notes = "页签类型")
    private TabType tabType;

    /**
     * 在本页中位置
     */
    @JsonView(DisplayView.class)
    @ApiModelProperty(hidden = true)
    private int position;

    /**
     * 检索关键字
     */
    @JsonView(SaveView.class)
    @ApiModelProperty(notes = "检索关键字")
    private String keywords;

    public Columns(String columnId, String columnName, ColumnType type) {
    	//先这么写  不知道对不对
    	super.setId(columnId);
        this.columnName = columnName;
        this.columnType = type;
    }

    public Columns(String columnName, ColumnType type,String tabKeywords,TabType tabType,int position,String keywords) {
        this.columnName = columnName;
        this.columnType = type;
        this.tabKeywords=tabKeywords;
        this.tabType=tabType;
        this.position=position;
        this.keywords=keywords;
    }


}
