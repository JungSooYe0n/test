package com.trs.netInsight.support.appApi.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@Table(name = "apkinfo")
public class ApkInfo extends BaseEntity {
    private static final long serialVersionUID = 6396284105648871816L;

    /**
     * apk上传路径
     */
    @Column(name = "filepath")
    private String filepath;

    /**
     * apk文件名
     */
    @Column(name = "filename")
    private String filename;

    /**
     * apk版本号
     */
    @Column(name = "version")
    private String version;

    /**
     * apk 版本code
     */
    @Column(name = "versioncode")
    private int versioncode;

    /**
     * apk 版本描述
     */
    @Column(name = "description")
    private String description;

    /**
     * apk是否强制更新
    * 0不需要，1需要强制更新
     */
    @Column(name = "isforce")
    private int isforce;

}
