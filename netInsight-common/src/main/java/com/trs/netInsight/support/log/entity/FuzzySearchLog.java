package com.trs.netInsight.support.log.entity;

import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.User;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "fuzzy_search_log")
public class FuzzySearchLog implements Serializable {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "`id`")
    private String id;

    /**
     * 操作用户id
     */
    @Column(name = "`user_id`")
    private String userId;

    /**
     * 操作用户的用户名
     */
    @Column(name = "`user_name`")
    private String userName;

    /**
     * 创建时间
     */
    /**
     * 对象创建时间
     */
    @Column(name = "`created_time`", updatable = true)
    private Date createdTime;
    @PrePersist
    protected void onCreate() {
        createdTime = new Date();
        User user = UserUtils.getUser();
        if(StringUtils.isBlank(userId)){
            userId = user.getId();
        }
        if(StringUtils.isBlank(userName)){
            userName=user.getUserName();
        }
    }

    /**
     * 修改时间 主要是多个关键字重复搜索记录使用
     */
    @Column(name = "`update_time`")
    private Date updateTime;
    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }

    /**
     * 关键字 - 页面输入的（包含空格的）
     */
    @Column(name = "`is_fuzzy_search`")
    private Boolean isFuzzySearch;

    /**
     * 关键字 - 页面输入的（包含空格的）
     */
    @Column(name = "`origin_keyword`")
    private String originKeyword;

    /**
     * 关键词 - 页面输入的（空格已被转化为逗号）
     */
    @Column(name = "`keywords`")
    private String keywords;

    /**
     * 模糊关键词 - 将页面关键词分词过后的  - 》 没有分词的情况下为空
     */
    @Column(name = "`fuzzy_keywords`")
    private String fuzzyKeywords;

    /**
     * 关键字  -  符号和空格替换为 AND OR
     */
    @Column(name = "`replace_keywords`")
    private String replaceKeywords;

    /**
     * 模糊关键词  -  将部分关键字分词之后  符号和空格替换为 AND OR
     */
    @Column(name = "`replace_fuzzy_keywords`")
    private String replaceFuzzyKeywords;

    /**
     * 精准查询表达式 - 对应页面输入的关键词拼接成的表达式
     */
    @Column(name = "`trsl`", columnDefinition = "TEXT")
    private String trsl;

    /**
     * 模糊查询表达式  - 为精准表达式 + 模糊关键词拼接后的表达式 模糊查询时，真正的表达式  - 》 没有分词的情况下为空
     */
    @Column(name = "`fuzzy_trsl`", columnDefinition = "TEXT")
    private String fuzzyTrsl;


}
