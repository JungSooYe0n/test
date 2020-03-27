package com.trs.netInsight.support.log.controller;

import com.trs.netInsight.support.log.entity.BigScreenLog;
import com.trs.netInsight.support.log.service.IBigScreenLogService;
import com.trs.netInsight.util.DateUtil;
import com.trs.netInsight.util.NetworkUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sun.awt.image.GifImageDecoder;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author lilyy
 * @date 2020/2/20 10:58
 */
@RestController
@RequestMapping("/bigScreenLog")
public class BigScreenLogController {

    @Autowired
    IBigScreenLogService bigScreenLogService;

    /**
     * http://localhost:28088/netInsight/bigScreenLog/addBigScreenLog?descript=大屏点击计数
     * 应用大屏可以把描述添加为: 大屏点击计数
     * @param descript 可以区分不同应用: 大屏点击计数,登录访问 等等...
     * @param request
     */
    @ApiOperation("添加点击大屏次数记录")
    @RequestMapping(value = "/addBigScreenLog", method = RequestMethod.GET)
    public void addBigScreenLog(@ApiParam("描述") @RequestParam(value = "descript",defaultValue = "大屏点击计数",required = false) String descript,
                                HttpServletRequest request){
        Date createdTime = new Date();
        String createdDate = DateUtil.getNowDate();
        String osAndBrowserinfo = NetworkUtil.getOsAndBrowserInfo(request);
        String osInfo = osAndBrowserinfo.split(" --- ")[0];
        String browserInfo = osAndBrowserinfo.split(" --- ")[1];
        String sessionId = request.getSession().getId();
        BigScreenLog bigScreenLog = new BigScreenLog(createdTime,createdDate,NetworkUtil.getIpAddress(request),
                osInfo,browserInfo,sessionId,descript);

        bigScreenLogService.save(bigScreenLog);
    }

}
