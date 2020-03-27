package com.trs.netInsight.widget.report.service;

import com.trs.netInsight.widget.report.entity.Favourites;
import com.trs.netInsight.widget.user.entity.User;

import java.util.List;

/**
 * 收藏 业务层接口
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/8/2 10:57.
 * @desc
 */
public interface IFavouritesService {

    /**
     * 查找某用户 或 用户分组下的收藏数据
     * @param user
     * @return
     */
    public List<Favourites> findAll(User user);

    public List<Favourites> findByUserAndSid(User user,String sid);

    /**
     * 只为迁移数据
     * @param userId
     * @return
     */
    public List<Favourites> findByUserId(String userId);
    public void updaeAll(List<Favourites> favourites);
}
