package com.trs.netInsight.support.mongo.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;

/**
 * 单条微博分析数据存储实体
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/2/13.
 * @desc
 */
//@Document(collection = "single_microblog_data")
public class SingleMicroblogData extends BaseEntity {

    private String name;

    private String data;

    private String microblogUrl;
}

