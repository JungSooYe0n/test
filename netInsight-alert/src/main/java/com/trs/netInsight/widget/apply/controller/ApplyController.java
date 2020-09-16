package com.trs.netInsight.widget.apply.controller;

import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.apply.entity.Apply;
import com.trs.netInsight.widget.apply.entity.enums.ApplyUserType;
import com.trs.netInsight.widget.apply.service.IApplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/apply")
@Api(description = "申请试用接口")
@Slf4j
public class ApplyController {


    @Autowired
    private IApplyService applyService;

    /**
     * 刚加载页面时查询所有栏目（分组）
     */
    @FormatResult
    @GetMapping(value = "/list")
    @ApiOperation("刚加载页面时查询所有导航栏")
    public Object list(@ApiParam("申请用户类型，老用户、新用户") @RequestParam("applyUserType") String applyUserType,
                       @ApiParam("页码") @RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
                       @ApiParam("条数") @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        ApplyUserType type = ApplyUserType.valueOf(applyUserType);
        return applyService.list(type, pageNo, pageSize);
    }

    /**
     * 刚加载页面时查询所有栏目（分组）
     */
    @FormatResult
    @GetMapping(value = "/addApplyUser")
    @ApiOperation("刚加载页面时查询所有导航栏")
    public Object addApplyUser(@ApiParam("申请用户类型，老用户、新用户") @RequestParam(value = "applyUserType") String applyUserType,

                               @ApiParam("单位名称") @RequestParam(value = "unitName",required = false) String unitName,
                               @ApiParam("姓名") @RequestParam(value = "name",required = false) String name,
                               @ApiParam("手机号") @RequestParam(value = "phone",required = false) String phone,
                               @ApiParam("工作电话") @RequestParam(value = "workPhone",required = false) String workPhone,
                               @ApiParam("工作邮箱") @RequestParam(value = "email") String email,
                               @ApiParam("来源渠道") @RequestParam(value = "sourceWay",required = false) String sourceWay,
                               @ApiParam("原有账号") @RequestParam(value = "originalAccount",required = false) String originalAccount,
                               @ApiParam("用户类型：正式formal，试用trial") @RequestParam(value = "accountType",required = false) String accountType) {
        ApplyUserType type = ApplyUserType.valueOf(applyUserType);
        Apply apply = new Apply(applyUserType,unitName,name,phone,workPhone,email,sourceWay,originalAccount,accountType);
        applyService.updateApply(apply);
        return "success";
    }

    /**
     *
     */
    @FormatResult
    @GetMapping(value = "/updateApplyStatus")
    @ApiOperation("刚加载页面时查询所有导航栏")
    public Object updateApplyStatus(@ApiParam("当前条信息的id") @RequestParam(value = "id") String id,

                               @ApiParam("账号状态 - 是否开通") @RequestParam(value = "accountStatus",required = false) String accountStatus,
                               @ApiParam("数据迁移状态") @RequestParam(value = "moveStatus",required = false) String moveStatus) {

        Apply apply = applyService.findOne(id);
        apply.setAccountStatus(accountStatus);
        apply.setMoveStatus(moveStatus);
        applyService.updateApply(apply);
        return "success";
    }
    /**
     *
     */
    @FormatResult
    @GetMapping(value = "/updateMoved")
    @ApiOperation("刚加载页面时查询所有导航栏")
    public Object updateMoved(@ApiParam("当前条信息的id") @RequestParam(value = "id") String id,
                              @ApiParam("数据迁移状态") @RequestParam(value = "moveStatus",defaultValue = "moved",required = false) String moveStatus) {

        Apply apply = applyService.findOne(id);
        apply.setMoveStatus(moveStatus);
        applyService.updateApply(apply);
        return "success";
    }



}
