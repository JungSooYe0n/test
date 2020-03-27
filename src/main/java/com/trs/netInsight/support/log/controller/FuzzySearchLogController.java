package com.trs.netInsight.support.log.controller;


import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.support.log.entity.FuzzySearchLog;
import com.trs.netInsight.support.log.entity.enums.DepositPattern;
import com.trs.netInsight.support.log.entity.enums.SystemLogType;
import com.trs.netInsight.support.log.service.IFuzzySearchLogService;
import com.trs.netInsight.util.StringUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;


/**
 * 日志类
 *
 * @author 张娅
 * @version add
 * @Type SystemLogController.java
 * @date 2020年3月16日11:08:06
 */
@Slf4j
@RestController
@RequestMapping("/fuzzySearchLog")
public class FuzzySearchLogController {

    @Autowired
    private IFuzzySearchLogService fuzzySearchLogService;

    /**
     * 添加点击日志
     *
     * @param
     * @param
     * @param
     * @param
     */
    @ApiOperation(value = "导出模糊查询日志(Swagger导出文件有问题，建议用postman或者地址栏导出)", produces = "application/octet-stream")
    @RequestMapping(value = "/exportFuzzySearchLog", method = RequestMethod.GET)
    public void exportFuzzySearchLog(HttpServletResponse response,
                                     @ApiParam("用户名") @RequestParam(value = "userName", defaultValue = "",required = false) String userName,
                                     @ApiParam("开始时间，格式：2020-03-01 00:00:00") @RequestParam(value = "startTime",defaultValue = "", required = false) String startTime,
                                     @ApiParam("结束时间，格式：2020-03-10 00:00:00") @RequestParam(value = "endTime",defaultValue = "", required = false) String endTime) throws TRSException {

        if (StringUtil.isNotEmpty(startTime)) {
            if (!DateUtil.isTimeFormatter(startTime)) {
                throw new TRSException("开始时间格式不正确");
            }
        }
        if (StringUtil.isNotEmpty(endTime)) {
            if (!DateUtil.isTimeFormatter(endTime)) {
                throw new TRSException("结束时间格式不正确");
            }
            if (StringUtil.isEmpty(startTime)) {
                throw new TRSException("有结束时间时，开始时间不能为空");
            }
        }
        try {
            response.resetBuffer();
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=logExcel.xlsx");
            ServletOutputStream outputStream = response.getOutputStream();
            List<FuzzySearchLog> list = fuzzySearchLogService.findFuzzySearchLogListByCondition(userName, startTime, endTime);
            fuzzySearchLogService.exportFuzzySearchLog(list).writeTo(outputStream);
        } catch (Exception e) {
            log.error("导出excel出错！", e);
        }
    }
}
