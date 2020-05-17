package com.trs.netInsight.widget.special.service;

import com.trs.netInsight.widget.report.entity.ReportNew;
import com.trs.netInsight.widget.special.entity.CustomSpecial;
import com.trs.netInsight.widget.special.entity.enums.SearchScope;
import com.trs.netInsight.widget.special.entity.enums.SpecialType;
import com.trs.netInsight.widget.user.entity.User;

import java.util.List;

/**
 *  舆情报告 极简模式 自定义专题 业务层接口
 *
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2018/10/10.
 */
public interface ICustomSpecialService {

    /**
     *  添加
     * @param customSpecialProject
     * @return
     */
    public CustomSpecial save(CustomSpecial customSpecialProject);

    /**
     * 查询当前用户所有 不分页
     * @return
     */
    public Object getByUser(User user);

    /**
     *  查询当前用户所有 分页
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Object getByUserId(int pageNo, int pageSize);

    /**
     * 删除
     * @param id
     */
    public void delete(String id);

    /**
     *  修改   参数释义在controller控制层
     * @param specialId
     * @param specialType
     * @param specialName
     * @param anyKeyWords
     * @param excludeWords
     * @param trsl
     * @param searchScope
     * @param timeRange
     * @param source
     * @param irSimflag
     * @param similar
     * @param weight
     * @return
     */
    public CustomSpecial update(String specialId, SpecialType specialType, String specialName, String anyKeyWords, String excludeWords, String excludeWeb, String trsl, SearchScope searchScope, String timeRange, String source, boolean irSimflag, boolean irSimflagAll, boolean similar, boolean weight);

    /**
     *  根据id 查询某一个
     * @param id
     * @return
     */
    public CustomSpecial findOne(String id);

    public List<CustomSpecial> findBySimple(SpecialType specialType);



}
