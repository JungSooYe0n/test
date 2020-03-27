package com.trs.netInsight.widget.microblog.service;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.microblog.entity.SingleMicroblogData;
import com.trs.netInsight.widget.user.entity.User;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * 单条微博分析数据业务层
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/2/13.
 * @desc
 */
public interface ISingleMicroblogDataService {

    /**
     * 查询单条记录
     * @param id
     * @return
     */
    public SingleMicroblogData findOne(String id);

    /**
     * 删除
     * @param id
     */
    public void remove(String id);

    /**
     * 删除
     * @param id
     */
    public void delete(String id);

    /**
     * 添加
     * @param singleMicroblogData
     * @return
     */
    public SingleMicroblogData insert(SingleMicroblogData singleMicroblogData);

    /**
     * 修改
     * @param singleMicroblogData
     * @return
     */
    public SingleMicroblogData save(SingleMicroblogData singleMicroblogData);
    /**
     * 查询某用户账号下分析微博某模块
     * @param user
     * @return
     */
    public SingleMicroblogData findSMDBySth(User user,String currentUrl,String name,String random);

    /**
     * 查询某用户账号下分析微博
     * @param user
     * @return
     */
    public SingleMicroblogData findSMDBySthNoRandom(User user,String currentUrl,String name);

    /**
     * 查询一个微博所有分析结果
     * @param userId
     * @param currentUrl
     * @return
     */
    public List<SingleMicroblogData> findAllSMD(String userId,String currentUrl);

    /**
     * 查询一个微博所有分析结果
     * @param user
     * @param currentUrl
     * @return
     */
    public List<SingleMicroblogData> findAllSMDWithRandom(User user,String currentUrl,String random);

    /**
     * 查询某用户所有的分析微博
     * @param user
     * @param name
     * @return
     */
    public List<SingleMicroblogData> findAll(User user, String name, Sort sort)  throws TRSException;

    public List<SingleMicroblogData> findStates(User user,String name, String state);

    /**
     * 只为迁移历史数据
     * @param userId
     * @return
     */
    public List<SingleMicroblogData> findByUserId(String userId);
    public void updateAll(List<SingleMicroblogData> singleMicroblogData);

}
