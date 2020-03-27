/*
 * Project: netInsight
 *
 * File Created at 2018/12/5
 *
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.support.api.controller;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.support.api.entity.ApiClient;
import com.trs.netInsight.support.api.entity.ApiFrequency;
import com.trs.netInsight.support.api.entity.repository.IClientRepository;
import com.trs.netInsight.support.api.result.ApiCommonResult;
import com.trs.netInsight.support.api.result.ApiResultType;
import com.trs.netInsight.support.api.service.IApiFrequencyService;
import com.trs.netInsight.support.api.utils.constance.ApiMethod;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.util.UserUtils;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.acl.LastOwnerException;

/**
 * api频率接口
 *
 * @author 北京拓尔思信息技术股份有限公司
 * @since yan.changjiang @ 2018/12/5
 */
@RestController
@RequestMapping("/api/frequency")
public class ApiFrequencyController {

    @Autowired
    private IApiFrequencyService apiFrequencyService;

    @Autowired
    private IClientRepository clientRepository;

    /**
     * 超管新增api频率记录
     *
     * @param name
     * @param code
     * @param frequencyLow
     * @param frequencyCommon
     * @param frequencyHigh
     * @return
     */
    @PostMapping("/registApiFrequency")
    public Object registApiFrequency(@ApiParam("api name") @RequestParam(value = "name") String name,
                                     @ApiParam("api code") @RequestParam(value = "code") int code,
                                     @ApiParam("api frequencyLow") @RequestParam(value = "frequencyLow") String frequencyLow,
                                     @ApiParam("api frequencyCommon") @RequestParam(value = "frequencyCommon") String frequencyCommon,
                                     @ApiParam("api frequencyHigh") @RequestParam(value = "frequencyHigh") String frequencyHigh) throws OperationException {
        if (!UserUtils.isSuperAdmin()) {
            throw new OperationException("权限认证失败!");
        }
        ApiFrequency frequency = new ApiFrequency();
        ApiMethod method = ApiMethod.findByCode(code);
        if (method == null) {
            throw new OperationException("未找到指定api资源!");
        }
        frequency.setCode(code);
        frequency.setName(name);
        frequency.setFrequencyCommon(StringUtils.isNotBlank(frequencyCommon) ? frequencyCommon : method.getFrequencyCommon());
        frequency.setFrequencyLow(StringUtils.isNotBlank(frequencyLow) ? frequencyLow : method.getFrequencyLow());
        frequency.setFrequencyHigh(StringUtils.isNotBlank(frequencyHigh) ? frequencyHigh : method.getFrequencyHigh());
        return apiFrequencyService.save(frequency);
    }

    /**
     * 设置指定apiClient的通用频率级别
     *
     * @param clientId
     * @param level
     * @return
     */
    @GetMapping("/setApiFrequencyLevel")
    public Object setApiFrequencyLevel(@ApiParam("api client id") @RequestParam(value = "clientId") String clientId,
                                       @ApiParam("api code") @RequestParam(value = "level") int level) {
        ApiCommonResult result = null;
        boolean superAdmin = UserUtils.isSuperAdmin();
        if (!superAdmin) {
            result = new ApiCommonResult(ApiResultType.Forbidden, null);
        } else {
            if (StringUtils.isBlank(clientId)) {
                result = new ApiCommonResult(ApiResultType.ParamError, null);
            } else {
                ApiClient client = clientRepository.findOne(clientId);
                if (client != null) {
                    client.setFrequencyLevel(chooseLevel(level));
                    client = clientRepository.save(client);
                    result = new ApiCommonResult(ApiResultType.Success, client.getClientName() + "通用频率级别设置成功");
                    result.setData(client);
                } else {
                    result = new ApiCommonResult(ApiResultType.NotFindSource, null);
                }
            }
        }
        return result;
    }

    /**
     * 自定义api频率
     *
     * @param clinetId
     * @param code            优先code值
     * @param setAll          是否设置全部api
     * @param frequencyCustom 自定义频率
     * @return
     */
    @PostMapping("/setFrequencyCustom")
    public Object setFrequencyCustom(@ApiParam("api clinet id") @RequestParam(value = "clinetId") String clinetId,
                                     @ApiParam("api code") @RequestParam(value = "code", defaultValue = "0") int code,
                                     @ApiParam("setAll") @RequestParam(value = "setAll", defaultValue = "false") boolean setAll,
                                     @ApiParam("api frequencyCustom") @RequestParam(value = "frequencyCustom") String frequencyCustom) {
        ApiCommonResult result = null;
        boolean superAdmin = UserUtils.isSuperAdmin();
        if (!superAdmin) {
            result = new ApiCommonResult(ApiResultType.Forbidden, null);
        } else {
            if (StringUtils.isNotBlank(frequencyCustom) && StringUtils.isNotBlank(clinetId)) {
                result = apiFrequencyService.setFrequencyCustom(clinetId,code, frequencyCustom,setAll);
            }else {
                result = new ApiCommonResult(ApiResultType.ParamError, null);
            }
        }
        return result;
    }

    /**
     * 选择频率级别
     *
     * @param level
     * @return
     */
    private String chooseLevel(int level) {
        switch (level) {
            case 0:
                return "Low";
            case 2:
                return "High";
            default:
                return "Common";
        }
    }
}

