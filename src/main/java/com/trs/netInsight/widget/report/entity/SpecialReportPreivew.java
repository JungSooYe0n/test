package com.trs.netInsight.widget.report.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/***
 *  Created by shao.guangze on 2018/7/16
 *  供专报两个preview接口使用，去除轮询时每次response的无用数据
 *  无用数据：页面已经正常展示的数据，不再返给页面
 *  speical_report_rollpoll_preview
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "special_report_preview")
public class SpecialReportPreivew extends BaseEntity {
    //chapter positions in which report chapter data is available.
    @Column(name = "chapter_position")
    private Integer chapterPosition;

    @Column(name = "report_id")
    private String reportId;

    public boolean equals(Object o){
        if(o != null && o instanceof SpecialReportPreivew){
            return this.chapterPosition.toString().equals(o.toString());
        }
        return false;
    }

    public String toString(){
        return this.chapterPosition.toString();
    }

    public SpecialReportPreivew(Integer chapterPosition){
        this.chapterPosition = chapterPosition;
    }
}
