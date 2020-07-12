package com.trs.netInsight.widget.analysis.controller;

import com.trs.netInsight.config.constant.Const;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.gather.entity.GatherPoint;
import com.trs.netInsight.widget.special.entity.HotRating;
import com.trs.netInsight.widget.special.entity.repository.HotRatingRepository;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 态势评估等级
 */
@RestController
@RequestMapping("/hotRating")
@Api(description = "态势评估热度评分接口")
@Slf4j
public class HotRatingController {
    @Autowired
    private HotRatingRepository hotRatingRepository;
//    public Object addHotRating(@ApiParam("微博低热指数") @RequestParam(value = "weiboLow", required = true) String weiboLow,
//                               @ApiParam("微博中热指数") @RequestParam(value = "weiboMiddle", required = true) String weiboMiddle) throws TRSException {
//
//        return Const.SUCCESS;
//    }
public Object updateHotRating(@ApiParam("微博低热指数") @RequestParam(value = "weiboLow", required = false) int weiboLow,
                               @ApiParam("微博中热指数") @RequestParam(value = "weiboMiddle", required = false) int weiboMiddle,
                              @ApiParam("微博高热指数") @RequestParam(value = "weiboHigh", required = false) int weiboHigh,
                              @ApiParam("微博vip低热指数") @RequestParam(value = "weiboVipLow", required = false) int weiboVipLow,
                              @ApiParam("微博vip中热指数") @RequestParam(value = "weiboVipMiddle", required = false) int weiboVipMiddle,
                              @ApiParam("微博vip高热指数") @RequestParam(value = "weiboVipHigh", required = false) int weiboVipHigh,
                              @ApiParam("微信低热指数") @RequestParam(value = "weixinLow", required = false) int weixinLow,
                              @ApiParam("微信中热指数") @RequestParam(value = "weixinMiddle", required = false) int weixinMiddle,
                              @ApiParam("微信高热指数") @RequestParam(value = "weixinHigh", required = false) int weixinHigh,
                              @ApiParam("新闻低热指数") @RequestParam(value = "newsLow", required = false) int newsLow,
                              @ApiParam("新闻中热指数") @RequestParam(value = "newsMiddle", required = false) int newsMiddle,
                              @ApiParam("新闻高热指数") @RequestParam(value = "newsHigh", required = false) int newsHigh,
                              @ApiParam("客户端低热指数") @RequestParam(value = "appLow", required = false) int appLow,
                              @ApiParam("客户端中热指数") @RequestParam(value = "appMiddle", required = false) int appMiddle,
                              @ApiParam("客户端高热指数") @RequestParam(value = "appHigh", required = false) int appHigh,
                              @ApiParam("自媒体低热指数") @RequestParam(value = "zimeitiLow", required = false) int zimeitiLow,
                              @ApiParam("自媒体中热指数") @RequestParam(value = "zimeitiMiddle", required = false) int zimeitiMiddle,
                              @ApiParam("自媒体高热指数") @RequestParam(value = "zimeitiHigh", required = false) int zimeitiHigh,
                              @ApiParam("论坛贴吧低热指数") @RequestParam(value = "luntanLow", required = false) int luntanLow,
                              @ApiParam("论坛贴吧中热指数") @RequestParam(value = "luntanMiddle", required = false) int luntanMiddle,
                              @ApiParam("论坛贴吧高热指数") @RequestParam(value = "luntanHigh", required = false) int luntanHigh) throws TRSException {
    User user = UserUtils.getUser();
    if (!UserUtils.ROLE_PLATFORM.contains(user.getCheckRole())) {
        throw new TRSException("非机构管理员没有此权限");
    }
    HotRating hotRating = new HotRating(weiboLow,weiboMiddle,weiboHigh,weiboVipLow,weiboVipMiddle,weiboVipHigh,weixinLow,weixinMiddle,weixinHigh,newsLow,newsMiddle,newsHigh,appLow,appMiddle,appHigh,zimeitiLow,zimeitiMiddle,zimeitiHigh,luntanLow,luntanMiddle,luntanHigh);
    hotRatingRepository.save(hotRating);
    return Const.SUCCESS;
}
    @ApiOperation("态势评估热度接口")
    @FormatResult
    @RequestMapping(value = "/getHotRating", method = RequestMethod.GET)
    public Object getHotRating() throws TRSException {
        User user = UserUtils.getUser();
        if (!UserUtils.ROLE_PLATFORM.contains(user.getCheckRole())) {
            throw new TRSException("非机构管理员没有此权限");
        }
        List<HotRating> hotRatingList = hotRatingRepository.findByOrganizationId(user.getOrganizationId());
        if (ObjectUtil.isEmpty(hotRatingList)){
            int weiboLow = 10000;
            int weiboMiddle = 15000;
            int weiboHigh = 20000;
            int weiboVipLow = 100;
            int weiboVipMiddle = 150;
            int weiboVipHigh = 200;
            int weixinLow = 200;
            int weixinMiddle = 300;
            int weixinHigh = 500;
            int newsLow = 300;
            int newsMiddle = 500;
            int newsHigh = 800;
            int appLow = 300;
            int appMiddle = 500;
            int appHigh = 800;
            int zimeitiLow = 200;
            int zimeitiMiddle = 300;
            int zimeitiHigh = 500;
            int luntanLow = 100;
            int luntanMiddle = 150;
            int luntanHigh = 200;
            HotRating hotRating = new HotRating(weiboLow,weiboMiddle,weiboHigh,weiboVipLow,weiboVipMiddle,weiboVipHigh,weixinLow,weixinMiddle,weixinHigh,newsLow,newsMiddle,newsHigh,appLow,appMiddle,appHigh,zimeitiLow,zimeitiMiddle,zimeitiHigh,luntanLow,luntanMiddle,luntanHigh);
       hotRatingRepository.save(hotRating);
       return hotRating;
        }
        return hotRatingList.get(0);
    }
}
