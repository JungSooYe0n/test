package com.trs.netInsight.support.knowledgeBase.entity;

import com.trs.netInsight.widget.base.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @Author:拓尔思信息股份有限公司
 * @Description: 知识库 实体类
 * @Date:Created in  2020/3/9 14:38
 * @Created By yangyanyan
 */
@Table(name = "KNOWLEDGE_BASE")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBase extends BaseEntity {

    /**
     * 输入关键词
     */
    @Column(name = "keywords", columnDefinition = "mediumtext")
    private String keywords;

    /**

     * 表示分类
     */
    @Column(name = "classify")
    private KnowledgeClassify classify;

}

