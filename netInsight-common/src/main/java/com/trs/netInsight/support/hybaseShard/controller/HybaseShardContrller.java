package com.trs.netInsight.support.hybaseShard.controller;

import com.trs.hybase.client.TRSConnection;
import com.trs.hybase.client.TRSDatabase;
import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.cache.RedisFactory;
import com.trs.netInsight.support.fts.model.factory.HybaseFactory;
import com.trs.netInsight.support.hybaseShard.entity.HybaseShard;
import com.trs.netInsight.support.hybaseShard.service.IHybaseShardService;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Author:拓尔思信息股份有限公司
 * @Description:hybase小库控制层
 * @Date:Created in  2020/3/3 16:06
 * @Created By yangyanyan
 */
@RestController
@RequestMapping("/trsshard")
@Api(description = "hybase小库控制层")
@Slf4j
public class HybaseShardContrller {
    @Autowired
    private IHybaseShardService hybaseShardService;

    @FormatResult
    @PostMapping(value = "/saveHybaseShard")
    @ApiOperation("添加hybase小库")
    public Object saveMicroblog(@ApiParam("传统库表名") @RequestParam(value = "tradition", required = false) String tradition,
                                @ApiParam("微博库表名") @RequestParam(value = "weiBo", required = false) String weiBo,
                                @ApiParam("微信库表名") @RequestParam(value = "weiXin", required = false) String weiXin,
                                @ApiParam("海外库表名") @RequestParam(value = "overseas", required = false) String overseas,
                                @ApiParam("视频库表名") @RequestParam(value = "video", required = false) String video,
                                @ApiParam("节点所属用户或用户分组id") @RequestParam(value = "ownerId", required = false) String ownerId,
                                @ApiParam("节点所属机构id") @RequestParam(value = "organizationId", required = false) String organizationId) throws TRSException {

        hybaseShardService.save(HybaseFactory.getServer(), HybaseFactory.getUserName(), HybaseFactory.getPassword(), tradition, weiBo, weiXin, overseas,video, ownerId, organizationId);
        return "success!";
    }

    @FormatResult
    @GetMapping(value = "/getDataBase")
    @ApiOperation("获取库列表")
    public Object getDataBase(@ApiParam("节点所属用户或用户分组id") @RequestParam(value = "ownerId", required = false) String ownerId,
                              @ApiParam("节点所属机构id") @RequestParam(value = "organizationId", required = false) String organizationId) throws TRSException {
        HybaseShard trsHybaseShard = null;
        if (StringUtil.isNotEmpty(ownerId)) {
            //运维
            String valueFromRedis = "";
            valueFromRedis = RedisFactory.getValueFromRedis(ownerId + "xiaoku");
            if (StringUtil.isNotEmpty(valueFromRedis)) {
                trsHybaseShard = ObjectUtil.toObject(valueFromRedis, HybaseShard.class);
            } else {
                trsHybaseShard = hybaseShardService.findByOwnerUserId(ownerId);
            }
        } else {
            String valueFromRedis = "";
            valueFromRedis = RedisFactory.getValueFromRedis(organizationId + "xiaoku");
            if (StringUtil.isNotEmpty(valueFromRedis)) {
                trsHybaseShard = ObjectUtil.toObject(valueFromRedis, HybaseShard.class);
            } else {
                if (StringUtil.isNotEmpty(organizationId)){
                    trsHybaseShard = hybaseShardService.findByOrganizationId(organizationId);
                }
            }

        }
        if (ObjectUtil.isNotEmpty(trsHybaseShard)) {
            trsHybaseShard.setHybaseServer("");
            trsHybaseShard.setHybasePwd("");
            trsHybaseShard.setHybaseUser("");
            if(StringUtil.isEmpty(trsHybaseShard.getWeiXin())){
                trsHybaseShard.setWeiXin(Const.WECHAT);
            }
            if(StringUtil.isEmpty(trsHybaseShard.getWeiBo())){
                trsHybaseShard.setWeiBo(Const.WEIBO);
            }
            if(StringUtil.isEmpty(trsHybaseShard.getTradition())){
                trsHybaseShard.setTradition(Const.HYBASE_NI_INDEX);
            }
            if(StringUtil.isEmpty(trsHybaseShard.getOverseas())){
                trsHybaseShard.setOverseas(Const.HYBASE_OVERSEAS);
            }
            if(StringUtil.isEmpty(trsHybaseShard.getVideo())){
                trsHybaseShard.setVideo(Const.HYBASE_VIDEO);
            }
        }else {
            //为空则是表示历史机构 添加默认库
            trsHybaseShard = new HybaseShard();
            trsHybaseShard.setHybaseServer("");
            trsHybaseShard.setHybasePwd("");
            trsHybaseShard.setHybaseUser("");
            trsHybaseShard.setWeiXin(Const.WECHAT);
            trsHybaseShard.setWeiBo(Const.WEIBO);
            trsHybaseShard.setTradition(Const.HYBASE_NI_INDEX);
            trsHybaseShard.setOverseas(Const.HYBASE_OVERSEAS);
            trsHybaseShard.setVideo(Const.HYBASE_VIDEO);
        }
        return trsHybaseShard;
    }

    @FormatResult
    @GetMapping(value = "/getSysDataBase")
    @ApiOperation("获取可选专享库")
    public Object getSysDataBase() throws TRSException {
        TRSConnection connection = HybaseFactory.getClient();
        try {
            TRSDatabase[] list = connection.getDatabases();
            List<String> tradition = new ArrayList<>();
            List<String> weibo = new ArrayList<>();
            List<String> weixin = new ArrayList<>();
            List<String> oversea = new ArrayList<>();
            List<String> video = new ArrayList<>();
            for (TRSDatabase hyBase : list) {
                if (hyBase.getName().contains("system2.traditionalmedia") && !hyBase.getName().equals(Const.HYBASE_NI_INDEX)) {
                    tradition.add(hyBase.getName());
                } else if (hyBase.getName().contains("system2.weibo") &&  !hyBase.getName().equals(Const.WEIBO)) {
                    weibo.add(hyBase.getName());
                } else if (hyBase.getName().contains("system2.weixin") && !hyBase.getName().equals(Const.WECHAT)) {
                    weixin.add(hyBase.getName());
                } else if (hyBase.getName().contains("system2.overseasMedia") && !hyBase.getName().equals(Const.HYBASE_OVERSEAS)) {
                    oversea.add(hyBase.getName());
                }else if (hyBase.getName().contains("system2.media") && !hyBase.getName().equals(Const.HYBASE_VIDEO)) {
                    video.add(hyBase.getName());
                }
            }
            HashMap<String, List<String>> hashMap = new HashMap<>();
            if (tradition.size() > 0) hashMap.put("tradition", tradition);
            if (weibo.size() > 0) hashMap.put("weibo", weibo);
            if (weixin.size() > 0) hashMap.put("weixin", weixin);
            if (oversea.size() > 0) hashMap.put("oversea", oversea);
            if (video.size() > 0) hashMap.put("video", video);
            return hashMap;
        } catch (com.trs.hybase.client.TRSException e) {
            e.printStackTrace();
        }
        return "";
    }
}
