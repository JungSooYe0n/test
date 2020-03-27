package com.trs.netInsight.support.hybaseShard.service.impl;

import com.trs.netInsight.support.cache.RedisFactory;
import com.trs.netInsight.support.hybaseShard.entity.HybaseShard;
import com.trs.netInsight.support.hybaseShard.repository.HybaseShardRepository;
import com.trs.netInsight.support.hybaseShard.service.IHybaseShardService;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author:拓尔思信息股份有限公司
 * @Description:hybase小库业务层实现
 * @Date:Created in  2020/3/3 16:05
 * @Created By yangyanyan
 */
@Service
public class HybaseShardServiceImpl implements IHybaseShardService {
    @Autowired
    private HybaseShardRepository hybaseShardRepository;
    @Override
    public void save(String hybaseServer, String hybaseUser, String hybasePwd, String tradition, String weiBo, String weiXin, String overseas, String ownerId,String organizationId) {

        HybaseShard trsHybaseShard;
        if (ObjectUtil.isNotEmpty(ownerId)){
            //运维
            trsHybaseShard = this.findByOwnerUserId(ownerId);
        }else {
            trsHybaseShard = this.findByOrganizationId(organizationId);
        }
        if (ObjectUtil.isEmpty(trsHybaseShard)){
            //保存
            trsHybaseShard = new HybaseShard(hybaseServer, hybaseUser, hybasePwd, tradition, weiBo, weiXin, overseas, ownerId,organizationId);
            hybaseShardRepository.save(trsHybaseShard);

        }else {
            //修改
            trsHybaseShard.setTradition(tradition);
            trsHybaseShard.setWeiBo(weiBo);
            trsHybaseShard.setWeiXin(weiXin);
            trsHybaseShard.setOverseas(overseas);
            hybaseShardRepository.save(trsHybaseShard);
        }
        if (ObjectUtil.isNotEmpty(ownerId)){
            RedisFactory.setValueToRedis(ownerId+"xiaoku",trsHybaseShard);
        }else {
            RedisFactory.setValueToRedis(organizationId+"xiaoku",trsHybaseShard);
        }
    }

    @Override
    public HybaseShard findByOwnerUserId(String ownerId) {
        return hybaseShardRepository.findByOwnerId(ownerId);
    }

    @Override
    public HybaseShard findByOrganizationId(String organizationId) {
        return hybaseShardRepository.findByOrganizationId(organizationId);
    }
}
