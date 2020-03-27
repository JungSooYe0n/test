package com.trs.netInsight.widget.thinkTank.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

/**
 * 舆情智库pdf与小图片存储类
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/9/23 15:15.
 * @desc
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "think_tank")
public class ThinkTankData extends BaseEntity {
    private static final long serialVersionUID = 7420399597522799834L;

    /**
     * 报告名称
     */
    @Column(name = "report_title")
    private String reportTitle;

    /**
     * 智库报告对应小图片
     */
    @Column(name = "picture_name")
    private String pictureName;

    /**
     * 报告时间
     */
    @Column(name = "report_time")
    private String reportTime;

    /**
     * 疫情pic名称   上传的第二张图片
     */
    @Column(name = "picDetail_name")
    private String picDetailName;

    //
    @Column(name = "pdf_name")
    private String pdfName;

   @Transient
   private String pdfToPngName;

    public String getPdfToPngName() {
        if(pdfName==null) return pdfName;
        return pdfName.replace(".pdf", ".png");
    }

    public ThinkTankData(String reportTitle, String pictureName, String reportTime, String pdfName,String picDetailName) {
        this.reportTitle = reportTitle;
        this.pictureName = pictureName;
        this.reportTime = reportTime;
        this.pdfName = pdfName;
        this.picDetailName = picDetailName;
    }
}
