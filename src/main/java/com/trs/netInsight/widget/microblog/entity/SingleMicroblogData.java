package com.trs.netInsight.widget.microblog.entity;

import com.trs.netInsight.support.template.ObjectContainer;
import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Entity;
import java.util.Date;


/**
 * 单条微博分析数据存储实体
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/2/13.
 * @desc
 */
@Setter
@Getter
@Document(collection = "single_microblog_data")
public class SingleMicroblogData extends BaseEntity {

    private String name;

    private Object data;

    private String originalUrl;

    private String currentUrl;

    /**
     * 用来区分是否是在更新
     * */
    private String random;

    /**
     * 排队
     * 分析中
     * 完成
      */
    private String state;

    private Date latelyTime;


    public SingleMicroblogData(String name, String originalUrl, String currentUrl) {
        this.name = name;
        this.originalUrl = originalUrl;
        this.currentUrl = currentUrl;
        super.setCreatedTime(new Date());
        super.setLastModifiedTime(new Date());
    }
}

