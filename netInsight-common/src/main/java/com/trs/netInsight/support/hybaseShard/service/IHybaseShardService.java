package com.trs.netInsight.support.hybaseShard.service;

import com.trs.netInsight.support.hybaseShard.entity.HybaseShard;


/**
 * @Author:拓尔思信息股份有限公司
 * @Description:hybase小库业务层接口
 * @Date:Created in  2020/3/3 15:43
 * @Created By yangyanyan
 */
public interface IHybaseShardService {
    /**
     * 添加
     * @param hybaseServer
     * @param hybaseUser
     * @param hybasePwd
     * @param tradition
     * @param weiBo
     * @param weiXin
     * @param overseas
     * @param video
     * @param ownerId
     */
    public void save(String hybaseServer,String hybaseUser,String hybasePwd,String tradition,String weiBo,String weiXin,String overseas,String video,String ownerId,String organizationId);

    /**
     * 查某用户所拥有小库资源
     * @param ownerId
     * @return
     */
    public HybaseShard findByOwnerUserId(String ownerId);

    public HybaseShard findByOrganizationId(String organizationId);

}
