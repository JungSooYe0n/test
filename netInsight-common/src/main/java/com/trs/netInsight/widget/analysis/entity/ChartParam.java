package com.trs.netInsight.widget.analysis.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


/**
 * @Desc 图表列表参数实体
 * @author yang.yanyan
 * @date 2018/3/1  10:39
 * @version
 */
@Setter
@Getter
@NoArgsConstructor
public class ChartParam implements Serializable{

    private static final long serialVersionUID = -5546780537273107512L;

    /**
     * 专项id
     */
    private String specialId;

    /**
     * 高级筛选时间段
     */
    private String timeRange;

    /**
     * 行业类型
     */
    private String industryType;

    /**
     * 地域分布
     */
    private String area;

    /**
     * 图表类型
     */
    private String chartType;
    
    /**
     * 页码
     */
    private int pageNo;
    
    /**
     * 步长
     */
    private int pageSize;
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
    
    /**
     * 项目名
     */
    private String projectName;



    public ChartParam(String specialId, String timeRange, String industryType, String area, String chartType, String firstName, String secondName, String thirdName,String projectName) {
        this.specialId = specialId;
        this.timeRange = timeRange;
        this.industryType = industryType;
        this.area = area;
        this.chartType = chartType;
        this.firstName = firstName;
        this.secondName = secondName;
        this.thirdName = thirdName;
        this.projectName = projectName;
    }

    @Override
    public String toString() {
        return "ChartParam{" +
                "specialId='" + specialId + '\'' +
                ", timeRange='" + timeRange + '\'' +
                ", industryType='" + industryType + '\'' +
                ", area='" + area + '\'' +
                ", chartType='" + chartType + '\'' +
                '}';
    }



	public ChartParam(String specialId, String timeRange, String industryType, String area, String chartType,
			int pageNo, int pageSize) {
		super();
		this.specialId = specialId;
		this.timeRange = timeRange;
		this.industryType = industryType;
		this.area = area;
		this.chartType = chartType;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
	}


}
