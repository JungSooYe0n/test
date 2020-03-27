package com.trs.netInsight.widget.user.controller;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.AlertAccount;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.alert.service.IAlertAccountService;
import com.trs.netInsight.widget.alert.service.IAlertRuleService;
import com.trs.netInsight.widget.article.entity.ArticleDelete;
import com.trs.netInsight.widget.article.service.IArticleDeleteService;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.NavigationConfig;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.service.IIndexPageService;
import com.trs.netInsight.widget.column.service.IIndexTabMapperService;
import com.trs.netInsight.widget.column.service.IIndexTabService;
import com.trs.netInsight.widget.column.service.INavigationService;
import com.trs.netInsight.widget.config.entity.SystemConfig;
import com.trs.netInsight.widget.config.service.ISystemConfigService;
import com.trs.netInsight.widget.microblog.entity.SingleMicroblogData;
import com.trs.netInsight.widget.microblog.service.ISingleMicroblogDataService;
import com.trs.netInsight.widget.report.entity.Favourites;
import com.trs.netInsight.widget.report.entity.ReportNew;
import com.trs.netInsight.widget.report.entity.TemplateNew;
import com.trs.netInsight.widget.report.service.IFavouritesService;
import com.trs.netInsight.widget.report.service.IReportServiceNew;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.SpecialSubject;
import com.trs.netInsight.widget.special.service.ISpecialProjectService;
import com.trs.netInsight.widget.special.service.ISpecialService;
import com.trs.netInsight.widget.special.service.ISpecialSubjectService;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.Role;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.entity.enums.CheckRole;
import com.trs.netInsight.widget.user.entity.enums.Status;
import com.trs.netInsight.widget.user.service.IOrganizationService;
import com.trs.netInsight.widget.user.service.IRoleService;
import com.trs.netInsight.widget.user.service.ISubGroupService;
import com.trs.netInsight.widget.user.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

/**
 * 分组控制层接口
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/7/24 15:11.
 * @desc
 */
@Slf4j
@RestController
@Api(description = "分组控制层接口")
@RequestMapping(value = { "/group" })
public class SubGroupController {

    @Autowired
    private ISubGroupService subGroupService;

    @Autowired
    private IOrganizationService organizationService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IIndexPageService indexPageService;

    @Autowired
    private ISpecialService specialService;

    @Autowired
    private IIndexTabService indexTabService;

    @Autowired
    private IAlertRuleService alertRuleService;
    @Autowired
    private IAlertAccountService alertAccountService;
    @Autowired
    private ISpecialProjectService specialProjectService;
    @Autowired
    private ISpecialSubjectService specialSubjectService;
    @Autowired
    private IRoleService roleService;
    @Autowired
    private INavigationService navigationService;
    @Autowired
    private IIndexTabMapperService indexTabMapperService;
    @Autowired
    private IReportServiceNew reportServiceNew;
    @Autowired
    private ISingleMicroblogDataService singleMicroblogDataService;
    @Autowired
    private IFavouritesService favouritesService;
    @Autowired
    private IArticleDeleteService articleDeleteService;
    @Autowired
    private ISystemConfigService systemConfigService;


    /**
     *
     * @param pageNo
     * @param pageSize
     * @param surplusDateSort
     * @param status
     * @param retrievalCondition
     * @param retrievalInformation
     * @return
     * @throws TRSException
     */
    @ApiOperation("查询用户分组")
    @FormatResult
    @GetMapping(value = "/pageList")
    public Object pageList(
            @ApiParam("所属机构id") @RequestParam(value = "orgId", required = false) String orgId,
            @ApiParam("页码") @RequestParam(value = "pageNo", required = false, defaultValue = "0") int pageNo,
            @ApiParam("页长") @RequestParam(value = "pageSize", required = false, defaultValue = "20") int pageSize,
            @ApiParam("剩余有效期排序,默认不传值，后台按创建日期降序排") @RequestParam(value = "surplusDateSort", required = false) String surplusDateSort,
            @ApiParam("状态") @RequestParam(value = "status", required = false) String status,
            @ApiParam("检索条件:分组名称name，登录账号userName") @RequestParam(value = "retrievalCondition", required = false) String retrievalCondition,
            @ApiParam("检索信息") @RequestParam(value = "retrievalInformation", required = false) String retrievalInformation)
            throws TRSException {
        //防止前端乱输入
        pageSize = pageSize>=1?pageSize:10;
        User loginUser = UserUtils.getUser();
        String checkRole = loginUser.getCheckRole();
        if (!UserUtils.ROLE_LIST.contains(checkRole)){
            throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限查看！");
        }
        String organizationId = loginUser.getOrganizationId();
        if (UserUtils.isSuperAdmin() || UserUtils.isRolePlatform()){
            if (StringUtil.isEmpty(orgId)){
                throw new TRSException(CodeUtils.FAIL,"请告我属于哪个机构！");
            }
            //若是超管或者运维人员，需要知道是哪个机构下的用户分组
            organizationId = orgId;
        }
        if (pageNo < 0){
            pageNo = 0;
        }
        Page<SubGroup> subGroups = null;
        try {
            subGroups = subGroupService.findByOrgId(organizationId,pageNo, pageSize, surplusDateSort, status, retrievalCondition, retrievalInformation);
        } catch (TRSException e) {
            log.error("查询分组失败",e);
            throw new TRSException(CodeUtils.FAIL,"查询分组失败~");
        }

        return subGroups;
    }

    /**
     * 添加用户分组
     * @param name        分组名称
     * @param filePicture 上传logo图片
     * @param roleIds     分组权限，多个用逗号隔开
     * @param columnNum   数量限制——日常监测
     * @param specialNum  数量限制——专题
     * @param alertNum    数量限制——预警主题
     * @param alertAccountNum 数量限制——预警账号
     * @param expireAt        有效期，默认永久2050-01-01 00:00:00
     * @param columnSync      同步数据，同步日常监测
     * @param specialSyncLevel 同步数据，同步专题分析(带级别)
     * @param specialSync  同步数据，同步专题分析(不带级别)
     * @param userLimit    机构用户限制（登录账号个数）
     * @param userJson   批量添加用户json
     * @return
     * @throws TRSException
     */
    @ApiOperation("添加用户分组")
    @FormatResult
    @PostMapping(value = "/add")
    public Object addSubGroup(@ApiParam("机构id（主要是运维账号）") @RequestParam(value = "orgId",required = false) String orgId,@ApiParam("分组名称") @RequestParam(value = "name") String name,
                              @ApiParam(name = "上传logo图片", required = false) @RequestParam(value = "filePicture", required = false) MultipartFile filePicture,
                              @ApiParam("分组权限，多个用逗号隔开") @RequestParam(value = "roleIds", required = false) String[] roleIds,
                              @ApiParam(name = "数量限制——日常监测") @RequestParam(value = "columnNum", defaultValue = "50") int columnNum,
                              @ApiParam(name = "数量限制——专题") @RequestParam(value = "specialNum",  defaultValue = "10") int specialNum,
                              @ApiParam(name = "数量限制——预警主题") @RequestParam(value = "alertNum",  defaultValue = "10") int alertNum,
                              @ApiParam(name = "数量限制——预警账号") @RequestParam(value = "alertAccountNum", defaultValue = "5") int alertAccountNum,
                              @ApiParam("有效期，默认永久2050-01-01 00:00:00") @RequestParam(value = "expireAt", defaultValue = "2050-01-01 00:00:00") String expireAt,
                              @ApiParam("同步数据，同步日常监测") @RequestParam(value = "columnSync",required = false) String columnSync,
                              @ApiParam("同步数据，同步专题分析(带级别)") @RequestParam(value = "specialSyncLevel",required = false) String specialSyncLevel,
                              @ApiParam("同步数据，同步专题分析(不带级别)") @RequestParam(value = "specialSync",required = false) String[] specialSync,
                              @ApiParam(name = "机构用户限制（登录账号个数）") @RequestParam(value = "userLimit", defaultValue = "5") int userLimit,
                              @ApiParam(name = "批量添加用户json") @RequestParam(value = "userJson",required = false) String userJson) throws TRSException{


        // 判断权限
        User loginUser = UserUtils.getUser();
        if (!UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())) {
            throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限添加用户分组！");

        }
        //运维是没有机构的
        String organizationId = UserUtils.getUser().getOrganizationId();
        //超管或者运维
        if (UserUtils.isSuperAdmin() || UserUtils.isRolePlatform()){
            if (StringUtil.isEmpty(orgId)){
                throw new TRSException(CodeUtils.FAIL,"请告诉属于哪个机构！");
            }
            organizationId = orgId;
        }

        Organization organization = organizationService.findById(organizationId);
        if (ObjectUtil.isEmpty(organization)){
            throw new TRSException(CodeUtils.FAIL,"找不到所属机构！");
        }
        //当前分组与机构有效期校验
        if (! DateUtil.isExpire(organization.getExpireAt(),expireAt)){
            throw new TRSException(CodeUtils.FAIL,"当前有效期不能长于机构有效期！");
        }

        //查询机构下 的 分组
        List<SubGroup> subGroups = subGroupService.findByOrgId(organizationId);
        int currentColumnNum = organization.getColumnNum();
        int currentSpecialNum = organization.getSpecialNum();
        int currentAlertNum = organization.getAlertNum();
        int currentAlertAccountNum = organization.getAlertAccountNum();
        int currentUserLimit = organization.getUserLimit();
        if (ObjectUtil.isNotEmpty(subGroups)){
            for (SubGroup subGroup : subGroups) {
                currentColumnNum -= subGroup.getColumnNum();
                currentSpecialNum -= subGroup.getSpecialNum();
                currentAlertNum -= subGroup.getAlertNum();
                currentAlertAccountNum -= subGroup.getAlertAccountNum();
                currentUserLimit -= subGroup.getUserLimit();
            }
        }
        // 判断分组名称是否为空
        if (StringUtils.isBlank(name)) {
            throw new TRSException(CodeUtils.FAIL, "分组名称不能为空！");
        }

        // 登录账号个数
        if (userLimit < 1) {
            throw new TRSException(CodeUtils.FAIL, "登录账号个数不能小于 1 ！");
        }
        if (userLimit > currentUserLimit) {
            throw new TRSException(CodeUtils.FAIL, "登录账号个数已超出当前可添加总数！");
        }
        // 日常监测栏目数
        if (columnNum < 1) {
            throw new TRSException(CodeUtils.FAIL, "日常监测栏目数不能小于 1 ！");
        }
        if (columnNum > currentColumnNum){
            throw new TRSException(CodeUtils.FAIL,"日常监测数已超出当前可添加总数！");
        }
        // 专题个数
        if (specialNum < 1) {
            throw new TRSException(CodeUtils.FAIL, "专题事件数不能小于 1 ！");
        }
        if (specialNum > currentSpecialNum){
            throw new TRSException(CodeUtils.FAIL,"专题事件数已超出当前可添加总数！");
        }
        // 预警
        if (alertNum < 1) {
            throw new TRSException(CodeUtils.FAIL, "预警主题个数不能小于 1 ！");
        }
        if (alertNum > currentAlertNum) {
            throw new TRSException(CodeUtils.FAIL, "预警主题数已超出当前可添加总数 ！");
        }

        // 预警账号个数
        if (alertAccountNum < 1) {
            throw new TRSException(CodeUtils.FAIL, "可绑定预警账号数不能小于 1 ！");
        }
        if (alertAccountNum > currentAlertAccountNum) {
            throw new TRSException(CodeUtils.FAIL, "可绑定预警账号数已超出当前可绑定数 ！");
        }

       return subGroupService.save(0,organizationId,name,filePicture,roleIds,columnNum,specialNum,alertNum,alertAccountNum,expireAt,columnSync,specialSyncLevel,specialSync,userLimit,userJson);
    }

    /**
     * 修改用户分组
     * @param name        分组名称
     * @param filePicture 上传logo图片
     * @param roleIds     分组权限，多个用逗号隔开
     * @param columnNum   数量限制——日常监测
     * @param specialNum  数量限制——专题
     * @param alertNum    数量限制——预警主题
     * @param alertAccountNum 数量限制——预警账号
     * @param expireAt        有效期，默认永久2050-01-01 00:00:00
    // * @param columnSync      同步数据，同步日常监测
    // * @param specialSyncLevel 同步数据，同步专题分析(带级别)
    // * @param specialSync  同步数据，同步专题分析(不带级别)
     * @param userLimit    机构用户限制（登录账号个数）
     * @param userJson   批量添加用户json
     * @return
     * @throws TRSException
     */
    @ApiOperation("修改用户分组")
    @FormatResult
    @PostMapping(value = "/update")
    public Object updateSubGroup(@ApiParam("分组id") @RequestParam(value = "id") String id,
                                 @ApiParam("分组名称") @RequestParam(value = "name") String name,
                              @ApiParam(name = "上传logo图片", required = false) @RequestParam(value = "filePicture", required = false) MultipartFile filePicture,
                              @ApiParam("上传logo图片的名字") @RequestParam(value = "pictureName", required = false) String pictureName,
                              @ApiParam("分组权限，多个用逗号隔开") @RequestParam(value = "roleIds", required = false) String[] roleIds,
                              @ApiParam(name = "数量限制——日常监测") @RequestParam(value = "columnNum", defaultValue = "50") int columnNum,
                              @ApiParam(name = "数量限制——专题") @RequestParam(value = "specialNum",  defaultValue = "10") int specialNum,
                              @ApiParam(name = "数量限制——预警主题") @RequestParam(value = "alertNum",  defaultValue = "10") int alertNum,
                              @ApiParam(name = "数量限制——预警账号") @RequestParam(value = "alertAccountNum", defaultValue = "5") int alertAccountNum,
                              @ApiParam("有效期，默认永久2050-01-01 00:00:00") @RequestParam(value = "expireAt", defaultValue = "2050-01-01 00:00:00") String expireAt,
                              @ApiParam(name = "机构用户限制（登录账号个数）") @RequestParam(value = "userLimit", defaultValue = "5") int userLimit,
                              @ApiParam(name = "批量添加或修改用户json") @RequestParam(value = "userJson",required = false) String userJson) throws TRSException{


        // 判断权限
        User loginUser = UserUtils.getUser();
        if (!UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())) {
            throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限修改用户分组！");

        }
        String organizationId = UserUtils.getUser().getOrganizationId();
        //机构运维员
        if (UserUtils.isSuperAdmin() || UserUtils.isRolePlatform()){
            organizationId = subGroupService.findOne(id).getOrganizationId();
        }
        if (StringUtils.isBlank(organizationId)){
            throw new TRSException(CodeUtils.FAIL, "找不到所属机构id！");
        }

        Organization organization = organizationService.findById(organizationId);
        if (ObjectUtil.isEmpty(organization)){
            throw new TRSException(CodeUtils.FAIL,"找不到所属机构！");
        }
        // 判断日期
        if (StringUtils.isBlank(expireAt)) {
            throw new TRSException(CodeUtils.FAIL, "日期状态不能为空！");
        } else {
            if (!expireAt.equals(UserUtils.FOREVER_DATE) && !com.trs.netInsight.support.fts.util.DateUtil.isValidDate(expireAt, com.trs.netInsight.support.fts.util.DateUtil.yyyyMMdd)) {
                throw new TRSException(CodeUtils.FAIL, "日期格式不正确，应为" + com.trs.netInsight.support.fts.util.DateUtil.yyyyMMdd + "！");
            }
        }
        //当前分组与机构有效期校验
        if (! DateUtil.isExpire(organization.getExpireAt(),expireAt)){
            throw new TRSException(CodeUtils.FAIL,"当前有效期不能晚于机构有效期！");
        }
        //查询机构下 的 分组
        List<SubGroup> subGroups = subGroupService.findByOrgId(organizationId);
        int currentColumnNum = organization.getColumnNum();
        int currentSpecialNum = organization.getSpecialNum();
        int currentAlertNum = organization.getAlertNum();
        int currentAlertAccountNum = organization.getAlertAccountNum();
        int currentUserLimit = organization.getUserLimit();
        if (ObjectUtil.isNotEmpty(subGroups)){
            for (SubGroup subGroup : subGroups) {
                //排除掉当前被编辑的用户分组
                if (!subGroup.getId().equals(id)){
                    currentColumnNum -= subGroup.getColumnNum();
                    currentSpecialNum -= subGroup.getSpecialNum();
                    currentAlertNum -= subGroup.getAlertNum();
                    currentAlertAccountNum -= subGroup.getAlertAccountNum();
                    currentUserLimit -= subGroup.getUserLimit();
                }
            }
        }
        // 判断分组名称是否为空
        if (StringUtils.isBlank(name)) {
            throw new TRSException(CodeUtils.FAIL, "分组名称不能为空！");
        }
        /*if(subGroups!= null && subGroups.size()>0){
            for(SubGroup subGroup:subGroups){
                if(name.equals(subGroup.getName()) && !subGroup.getId().equals(id)){
                    throw new TRSException(CodeUtils.FAIL,"该分组名称已存在！");
                }
            }
        }*/
        // 登录账号个数
        if (userLimit < 1) {
            throw new TRSException(CodeUtils.FAIL, "登录账号个数不能小于 1 ！");
        }
        if (userLimit > currentUserLimit) {
            throw new TRSException(CodeUtils.FAIL, "登录账号个数已超出当前可添加总数！");
        }
        //查询当前分组下的用户数量
        List<User> users = userService.findBySubGroupId(id);
        if (ObjectUtil.isNotEmpty(users) && userLimit < users.size()){
            throw new TRSException(CodeUtils.FAIL,"当前设置登录账号个数 小于 该分组所拥有的账号个数！");
        }
        // 日常监测栏目数
        if (columnNum < 1) {
            throw new TRSException(CodeUtils.FAIL, "日常监测栏目数不能小于 1 ！");
        }
        if (columnNum > currentColumnNum){
            throw new TRSException(CodeUtils.FAIL,"日常监测数已超出当前可添加总数！");
        }

        SubGroup subGroup = subGroupService.findOne(id);
        if (ObjectUtil.isEmpty(subGroup)){
            throw new TRSException(CodeUtils.FAIL,"无法查到当前id对应的用户分组！");
        }
        if (columnNum < indexTabService.getSubGroupColumnCountForSubGroup(id)){
            throw new TRSException(CodeUtils.FAIL,"当前设置日常监测栏目数 小于 该分组所拥有的栏目个数！");
        }
        // 专题个数
        if (specialNum < 1) {
            throw new TRSException(CodeUtils.FAIL, "专题事件数不能小于 1 ！");
        }
        if (specialNum > currentSpecialNum){
            throw new TRSException(CodeUtils.FAIL,"专题事件数已超出当前可添加总数！");
        }
        if (specialNum < specialProjectService.getSubGroupSpecialCountForSubGroup(id)){
            throw new TRSException(CodeUtils.FAIL,"当前设置专题事件数 小于 该分组所拥有的专题事件个数！");
        }
        // 预警
        if (alertNum < 1) {
            throw new TRSException(CodeUtils.FAIL, "预警主题个数不能小于 1 ！");
        }
        if (alertNum > currentAlertNum) {
            throw new TRSException(CodeUtils.FAIL, "预警主题数已超出当前可添加总数 ！");
        }
        if (alertNum < alertRuleService.getSubGroupAlertCountForSubGroup(id)){
            throw new TRSException(CodeUtils.FAIL,"当前设置预警主题数 小于 该分组所拥有的预警主题个数！");
        }

        // 预警账号个数
        if (alertAccountNum < 1) {
            throw new TRSException(CodeUtils.FAIL, "可绑定预警账号数不能小于 1 ！");
        }
        if (alertAccountNum > currentAlertAccountNum) {
            throw new TRSException(CodeUtils.FAIL, "可绑定预警账号数已超出当前可绑定数 ！");
        }
        if (alertAccountNum < alertAccountService.getSubGroupAlertAccountCountForSubGroup(id,SendWay.WE_CHAT) || alertAccountNum < alertAccountService.getSubGroupAlertAccountCountForSubGroup(id,SendWay.EMAIL)){
            throw new TRSException(CodeUtils.FAIL,"当前设置可绑定预警账号数 小于 该分组所拥有的可绑定预警账号个数！");
        }

        subGroupService.update(id,name,filePicture,pictureName,roleIds,columnNum,specialNum,alertNum,alertAccountNum,expireAt,userLimit,userJson);

        return "success";
    }

    /**
     * 用户分组详情
     * @param id
     * @return
     * @throws TRSException
     */
    @ApiOperation("用户分组详情")
    @FormatResult
    @GetMapping(value = "/getGroupDetail")
    public Object getGroupDetail(@ApiParam("用户分组id") @RequestParam(value = "id") String id)
            throws TRSException {

        try {
            return subGroupService.detail(id);
        } catch (Exception e) {
            throw new TRSException(CodeUtils.FAIL,"获取用户分组详情失败！");
        }
    }

    @ApiOperation("删除用户分组")
    @FormatResult
    @GetMapping(value = "/delete/{id}")
    public Object delete(@ApiParam("用户分组id") @PathVariable(value = "id") String id)
            throws TRSException {

        try {
            subGroupService.delete(id);
        } catch (Exception e) {
            throw new TRSException(CodeUtils.FAIL,"删除用户分组失败！");
        }
        return "success";
    }

    @ApiOperation("设置分组")
    @FormatResult
    @PostMapping(value = "/setUpGroup")
    public Object setUpGroup(@ApiParam("用户分组id") @RequestParam(value = "id") String id,
                             @ApiParam(name = "数量限制——日常监测") @RequestParam(value = "columnNum", defaultValue = "-1") int columnNum,
                             @ApiParam(name = "数量限制——专题") @RequestParam(value = "specialNum",  defaultValue = "-1") int specialNum,
                             @ApiParam(name = "数量限制——预警主题") @RequestParam(value = "alertNum",  defaultValue = "-1") int alertNum,
                             @ApiParam(name = "数量限制——预警账号") @RequestParam(value = "alertAccountNum", defaultValue = "-1") int alertAccountNum,
                             @ApiParam(name = "机构用户限制（登录账号个数）") @RequestParam(value = "userLimit", defaultValue = "-1") int userLimit)
            throws TRSException {


       subGroupService.setUpGroup(id,columnNum,specialNum,alertNum,alertAccountNum,userLimit);
        return "success";
    }

    /**
     * 修改用户分组状态
     * @param id
     * @param status
     * @return
     * @throws TRSException
     */
    @ApiOperation("修改用户分组状态")
    @FormatResult
    @GetMapping(value = "/updateGroupStatus/{id}")
    public Object updateStatus(@ApiParam("用户分组id") @PathVariable(value = "id") String id,
                               @ApiParam("状态  1为冻结 0为正常") @RequestParam(value = "status") String status) throws TRSException {
        // 判断权限
        if (!UserUtils.ROLE_LIST.contains(UserUtils.getUser().getCheckRole())) {
            throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限修改状态！");
        }

        SubGroup subGroup = subGroupService.findOne(id);
        Organization organization = organizationService.findById(subGroup.getOrganizationId());
        if ("1".equals(status) && organization.getStatus().equals(status)){
            throw new TRSException(CodeUtils.FAIL,"当前分组所属机构呈冻结状态，所以其状态无法开启！");
        }
        subGroupService.updateStatus(subGroup,status);
        return "修改用户分组状态成功！";
    }
    /**
     * 查询弹框内容
     * @return
     * @throws TRSException
     */
    @ApiOperation("查询同步数据（弹框内容）")
    @FormatResult
    @GetMapping(value = "/selectDataSync")
    public Object selectDataSync(@ApiParam("机构管理员id") @RequestParam(value = "orgAdminId",required = false) String orgAdminId) throws TRSException{
        // 判断权限
        User loginUser = UserUtils.getUser();
        String userId = loginUser.getId();
        if (!UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())) {
            throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限同步数据！");
        }

        if (UserUtils.isSuperAdmin() || UserUtils.isRolePlatform()){
            //若为 超管或者运维 同样 同步 机构管理员的数据
            if (StringUtils.isBlank(orgAdminId)){
                throw new TRSException(CodeUtils.FAIL,"请告诉我要同步哪个机构管理员的数据！");
            }

            userId = orgAdminId;
        }

        //查询该用户下的日常监测（包括自定义的）以及包含的栏目组
        List<Map<String, Object>> columnNavs = indexPageService.findByOrgAdminId(userId);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("column",columnNavs);
        //查询 该用户 下 的专题分析（一级或者二级分组下无专题则不返给前端）且带着层级关系
        List list = specialService.selectSomeSpecials(userId);
        resultMap.put("special",list);
        return resultMap;
    }

    /**
     * 获取用户分组logo图标
     * @param groupId
     * @param request
     * @param response
     */
    @ApiOperation("获取用户分组logo图标")
    @GetMapping(value = "/getGroupLogo")
    public void getGroupLogo(@ApiParam("用户分组id") @RequestParam(value = "groupId") String groupId,
                               HttpServletRequest request, HttpServletResponse response) {
        SubGroup subGroup = subGroupService.findOne(groupId);
        String logoPicName = "wangcha.png";
        String type = "group";
        if (subGroup != null) {
            String logoPic = subGroup.getLogoPicName();
            if (StringUtils.isNotBlank(logoPic)) {
                logoPicName = logoPic;
            }
        }
        if("wangcha.png".equals(logoPicName) || "无logo".equals(logoPicName)){
            SystemConfig systemConfig = systemConfigService.findSystemConfig();
            if(!"".equals(systemConfig.getLogoPicName()) && !"无logo".equals(systemConfig.getLogoPicName())){
                logoPicName = systemConfig.getLogoPicName();
                type = "default";
            }
        }
        if("无logo".equals(logoPicName)){
            logoPicName = "wangcha.png";
        }
        File file = PictureUtil.getLogoPic(logoPicName,type);
        if (!file.exists()) {
            return;
        }
        FileInputStream fis = null;
        try {
            // 去指定上传目录 获取当前下载图片的输入流
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            log.error("图片转流失败", e);
        }
        ServletOutputStream os = null;
        try {
            // 获取response响应流
            os = response.getOutputStream();
            // 设置下载的响应类型
            response.setContentType("image/jpeg");
            response.setHeader("content-disposition", "inline; name= " + URLEncoder.encode(logoPicName, "UTF-8"));
            int len = 0;
            byte[] b = new byte[1024];
            while (true) {
                len = fis.read(b);
                if (len == -1)
                    break;
                os.write(b, 0, len);
            }
        } catch (IOException e) {
            log.error("图片 获取 失败", e);
        } finally {
            // 无论如何 关流
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(os);
        }

    }

    /**
     * 修改历史数据 用户、机构
     * @param request
     * @param response
     */
    @ApiOperation("修改历史数据 用户、机构")
    @PostMapping(value = "/changHistoryDataForUserAndOrg")
    public void changHistoryDataForUserAndOrg(HttpServletRequest request, HttpServletResponse response) {
        //先删除 role和perss...表
         //所有普通用户  每个用户 属于一个用户分组
        //机构限制 按 当前机构所拥有的来
        //查询所有机构  然后遍历
        //找出 机构下的普通用户  创建用户分组
        List<Organization> organizations = organizationService.findAll();
        if (ObjectUtil.isNotEmpty(organizations)){
            List<Role> roles = roleService.findByDescriptions("日常监测、专题分析、预警中心");
            int i = 0;
            for (Organization organization : organizations) {
                //查找机构下的用户（包括机构管理员，主要修改它的权限） 每个用户属于一个新建的用户分组  分组限制数量为当前用户所拥有的+5
                List<User> users = userService.findByOrganizationId(organization.getId());
                User userAdmin = userService.findById(organization.getAdminUserId());
                organization.setUser(userAdmin);
                if (ObjectUtil.isNotEmpty(users)){
                    for (User user : users) {
                        // 修改用户权限
                        if (ObjectUtil.isNotEmpty(user.getRoles())){
                            if (ObjectUtil.isNotEmpty(roles)){
                                Set<Role> userRoles = new HashSet<>(roles);
                                user.setRoles(new HashSet<>(userRoles));
                            }else {
                                user.setRoles(null);
                            }
                        }
                        if (CheckRole.ROLE_ORDINARY.toString().equals(user.getCheckRole())){
                            //如果是普通用户 需要创建一个用户分组
                            SubGroup subGroup = new SubGroup();
                            //分组名 就用用户名加个 group 算了
                            subGroup.setName(user.getUserName()+"分组");
                            //分组logo 就用机构的吧
                            subGroup.setLogoPicName(organization.getLogoPicName());
                            //分组权限  就用 用户的权限吧
                            Set<Role> userRoles = user.getRoles();
                            Set<Role> userRolesNew = new HashSet<>(userRoles);
                            subGroup.setRoles(userRolesNew);
                            //分组 数量限制
                            //查询该用户下的日常监测数
                            List<IndexTab> tabs = indexTabService.findByUserId(user.getId());
                            if (ObjectUtil.isNotEmpty(tabs)){
                                subGroup.setColumnNum(tabs.size() + 5);
                            }else {
                                subGroup.setColumnNum(1);
                            }
                            //查询该用户下的  专题分析数
                            List<SpecialProject> specialProjects = specialProjectService.findByUserId(user.getId(), new Sort(Sort.Direction.DESC, "lastModifiedTime"));
                            if (ObjectUtil.isNotEmpty(specialProjects)){
                                subGroup.setSpecialNum(specialProjects.size());
                            }else {
                                subGroup.setSpecialNum(1);
                            }

                            //查询该用户下的预警数量
                            List<AlertRule> alertRules = alertRuleService.findByUserId(user.getId());
                            if (ObjectUtil.isNotEmpty(alertRules)){
                                subGroup.setAlertNum(alertRules.size() + 5);
                            }else {
                                subGroup.setAlertNum(1);
                            }

                            //查询该用户下的预警账号
                            List<AlertAccount> alertAccounts = alertAccountService.findByUserIdAndType(user.getId(), SendWay.EMAIL);
                            List<AlertAccount> alertAccountsW = alertAccountService.findByUserIdAndType(user.getId(), SendWay.WE_CHAT);
                            if (ObjectUtil.isNotEmpty(alertAccounts) && ObjectUtil.isNotEmpty(alertAccountsW)){
                                if (alertAccounts.size() >= alertAccountsW.size()){
                                    subGroup.setAlertAccountNum(alertAccounts.size()+1);
                                }else {
                                    subGroup.setAlertAccountNum(alertAccountsW.size()+1);
                                }
                            }else if (ObjectUtil.isNotEmpty(alertAccounts)){
                                subGroup.setAlertAccountNum(alertAccounts.size()+1);
                            }else if (ObjectUtil.isNotEmpty(alertAccountsW)){
                                subGroup.setAlertAccountNum(alertAccountsW.size()+1);
                            }else {
                                subGroup.setAlertAccountNum(2);
                            }
                            //有效期
                            if ("0".equals(user.getExpireAt())){
                                subGroup.setExpireAt(UserUtils.FOREVER_DATE);
                                user.setExpireAt(UserUtils.FOREVER_DATE);
                            }else {
                                subGroup.setExpireAt(user.getExpireAt());
                            }
                            //登录账号个数 1 即当前用户
                            subGroup.setUserLimit(1);
                            if ("0".equals(user.getStatus())){
                                subGroup.setStatus(Status.normal);
                            }else if ("1".equals(user.getStatus())){
                                subGroup.setStatus(Status.frozen);
                            }else {
                                subGroup.setStatus(Status.abnormal);
                            }
                            //所属机构
                            subGroup.setOrganizationId(organization.getId());
                            subGroup.setUserAccount(organization.getUser().getUserName());
                            subGroup.setUserId(organization.getUser().getId());
                            SubGroup subGroupSave = subGroupService.save(subGroup);
                            //修改用户
                            user.setSubGroupId(subGroupSave.getId());
                            userService.update(user,false);
                        }
                    }
                }
                //查询机构下的日常监测，专题分析，预警，预警账号，到期时间（永久的改为2050-01-10 00:00:00 考虑sql修改），可查询时间范围  设置为该机构的最大限制
                if ("0".equals(organization.getExpireAt())){
                    organization.setExpireAt(UserUtils.FOREVER_DATE);
                }
                int userLimt = organization.getUserLimit();
                int columnNum = 50;
                int specialNum = 10;
                int alertNum = 10;
                int alertAccountNum = 5;

                //查询机构下的用户分组 每个用户分组的限制数量加在一起  就是机构的数据量限制
                List<SubGroup> subGroups = subGroupService.findByOrgId(organization.getId());
                if (ObjectUtil.isNotEmpty(subGroups)){
                    userLimt = subGroups.size();
                    for (SubGroup subGroup : subGroups) {
                        columnNum += subGroup.getColumnNum();
                        specialNum += subGroup.getSpecialNum();
                        alertNum += subGroup.getAlertNum();
                        alertAccountNum += subGroup.getAlertAccountNum();
                    }
                }

                organization.setUserLimit(userLimt);
                organization.setColumnNum(columnNum);
                organization.setSpecialNum(specialNum);
                organization.setAlertNum(alertNum);
                organization.setAlertAccountNum(alertAccountNum);
                organization.setKeyWordsNum(500);
                organization.setColumnDateLimit(organization.getDataDate());
                organization.setSpecialDateLimit(organization.getDataDate());
                organization.setASearchDateLimit(organization.getDataDate());
                
                //修改
                organizationService.update(organization);
                System.err.println(i+=1);
            }
        }
        System.err.println("机构用户修改完成！");
    }

    /**
     * 修改历史数据  机构，栏目相关数据
     * @param request
     * @param response
     */
    @ApiOperation("修改历史数据 机构，用户下栏目相关数据")
    @PostMapping(value = "/changHistoryDataForColumn")
    public void changHistoryDataForColumn(HttpServletRequest request, HttpServletResponse response) {
        //普通用户下的就行（超管、运维和机构管理员subGriupId为null，可以不分用户角色，把每个用户下的数据放入该用户的subGroupId）
        //查询普通用户（效率问题  不查无需用户数据）
        List<User> users = userService.findByCheckRole(CheckRole.ROLE_ORDINARY.toString());
        if (ObjectUtil.isNotEmpty(users)){
            int i = 0;
            for (User user : users) {
                //导航
                List<NavigationConfig> navigationConfigs = navigationService.findByUserId(user.getId());
                if (ObjectUtil.isNotEmpty(navigationConfigs)){
                    for (NavigationConfig navigationConfig : navigationConfigs) {
                        navigationConfig.setSubGroupId(user.getSubGroupId());
                    }
                    navigationService.updateAll(navigationConfigs);
                }

                //日常监测
                //分组
                List<IndexPage> indexPages = indexPageService.findByUserIdForHistory(user.getId());
                if (ObjectUtil.isNotEmpty(indexPages)){
                    for (IndexPage indexPage : indexPages) {
                        indexPage.setSubGroupId(user.getSubGroupId());
                    }
                    indexPageService.saveAndFulsh(indexPages);
                }
                //栏目
                List<IndexTab> indexTabs = indexTabService.findByUserId(user.getId());
                if (ObjectUtil.isNotEmpty(indexTabs)){
                    for (IndexTab indexTab : indexTabs) {
                        indexTab.setSubGroupId(user.getSubGroupId());

                        indexTabService.update(indexTab);
                    }
                }
                //index_tab_mapper
                List<IndexTabMapper> indexTabMappers = indexTabMapperService.findByUserId(user.getId());
                if (ObjectUtil.isNotEmpty(indexTabMappers)){
                    for (IndexTabMapper indexTabMapper : indexTabMappers) {
                        indexTabMapper.setSubGroupId(user.getSubGroupId());
                        indexTabMapperService.update(indexTabMapper);
                    }
                }
                System.err.println(i+=1);
            }
        }
        System.err.println("栏目修改完成！");
    }

    /**
     * 修改历史数据  机构，用户下专题相关数据
     * @param request
     * @param response
     */
    @ApiOperation("修改历史数据 机构，用户下专题相关数据")
    @PostMapping(value = "/changHistoryDataForSpecial")
    public void changHistoryDataForSpecial(HttpServletRequest request, HttpServletResponse response) {
        //普通用户下的就行（超管、运维和机构管理员subGriupId为null，可以不分用户角色，把每个用户下的数据放入该用户的subGroupId）
        //查询普通用户（效率问题  不查无需用户数据）
        List<User> users = userService.findByCheckRole(CheckRole.ROLE_ORDINARY.toString());
        if (ObjectUtil.isNotEmpty(users)){
            int i = 0;
            for (User user : users) {
                //专题分析
                //分组
                List<SpecialSubject> specialSubjects = specialSubjectService.findByUserId(user.getId());
                if (ObjectUtil.isNotEmpty(specialSubjects)){
                    for (SpecialSubject specialSubject : specialSubjects) {
                        specialSubject.setSubGroupId(user.getSubGroupId());
                    }
                    specialSubjectService.updateAll(specialSubjects);
                }
                //专题
                List<SpecialProject> specialProjects = specialProjectService.findByUserId(user.getId());
                if (ObjectUtil.isNotEmpty(specialProjects)){
                    for (SpecialProject specialProject : specialProjects) {
                        specialProject.setSubGroupId(user.getSubGroupId());
                    }
                    specialProjectService.updateAll(specialProjects);
                }
                System.err.println(i+=1);
            }
        }

        System.err.println("专题修改完成！");
    }

    /**
     * 修改历史数据  机构，用户下预警相关数据
     * @param request
     * @param response
     */
    @ApiOperation("修改历史数据 机构，用户下预警相关数据")
    @PostMapping(value = "/changHistoryDataForAlert")
    public void changHistoryDataForAlert(HttpServletRequest request, HttpServletResponse response) {
        //普通用户下的就行（超管、运维和机构管理员subGriupId为null，可以不分用户角色，把每个用户下的数据放入该用户的subGroupId）
        //查询普通用户（效率问题  不查无需用户数据）
        List<User> users = userService.findByCheckRole(CheckRole.ROLE_ORDINARY.toString());
        if (ObjectUtil.isNotEmpty(users)){
            int i = 0;
            for (User user : users) {
               //预警规则
                List<AlertRule> alertRules = alertRuleService.findByUserId(user.getId());
                if (ObjectUtil.isNotEmpty(alertRules)){
                    for (AlertRule alertRule : alertRules) {
                        alertRule.setSubGroupId(user.getSubGroupId());
                    }
                    alertRuleService.updateAll(alertRules);
                }

                //预警账号
                List<AlertAccount> alertAccounts = alertAccountService.findByUserId(user.getId());
                if (ObjectUtil.isNotEmpty(alertAccounts)){
                    for (AlertAccount alertAccount : alertAccounts) {
                        alertAccount.setSubGroupId(user.getSubGroupId());
                    }
                    alertAccountService.updateAll(alertAccounts);
                }
                System.err.println(i+=1);
            }
        }
        System.err.println("预警修改完成！");
    }
    /**
     * 修改历史数据  机构，用户下舆情报告相关数据
     * @param request
     * @param response
     */
    @ApiOperation("修改历史数据 机构，用户下舆情报告相关数据")
    @PostMapping(value = "/changHistoryDataForReport")
    public void changHistoryDataForReport(HttpServletRequest request, HttpServletResponse response) {
        //普通用户下的就行（超管、运维和机构管理员subGriupId为null，可以不分用户角色，把每个用户下的数据放入该用户的subGroupId）
        //查询普通用户（效率问题  不查无需用户数据）
        List<User> users = userService.findByCheckRole(CheckRole.ROLE_ORDINARY.toString());
        if (ObjectUtil.isNotEmpty(users)){
            int i = 0;
            for (User user : users) {
                //舆情报告
                List<ReportNew> reportNews = reportServiceNew.findReportByUserId(user.getId());
                if (ObjectUtil.isNotEmpty(reportNews)){
                    for (ReportNew reportNew : reportNews) {
                        reportNew.setSubGroupId(user.getSubGroupId());
                    }
                    reportServiceNew.updateReportAll(reportNews);
                }
                //模板
                List<TemplateNew> templateNews = reportServiceNew.findTemplateByUserId(user.getId());

                if (ObjectUtil.isNotEmpty(templateNews)){
                    for (TemplateNew templateNew : templateNews) {
                        templateNew.setSubGroupId(user.getSubGroupId());
                    }
                    reportServiceNew.updateTemplateAll(templateNews);
                }
                //资源
                System.err.println(i+=1);
            }
        }

        System.err.println("舆情报告修改完成！");
    }

    /**
     * 修改历史数据  机构，用户下其他相关数据
     * @param request
     * @param response
     */
    @ApiOperation("修改历史数据 机构，用户下其他相关数据")
    @PostMapping(value = "/changHistoryDataForOther")
    public void changHistoryDataForOther(HttpServletRequest request, HttpServletResponse response) {
        //普通用户下的就行（超管、运维和机构管理员subGriupId为null，可以不分用户角色，把每个用户下的数据放入该用户的subGroupId）
        //查询普通用户（效率问题  不查无需用户数据）
        List<User> users = userService.findByCheckRole(CheckRole.ROLE_ORDINARY.toString());
        if (ObjectUtil.isNotEmpty(users)){
            int i = 0;
            for (User user : users) {
                //应用中心
                List<SingleMicroblogData> singleMicroblogData = singleMicroblogDataService.findByUserId(user.getId());
               if (ObjectUtil.isNotEmpty(singleMicroblogData)){
                   for (SingleMicroblogData singleMicroblogDatum : singleMicroblogData) {
                       singleMicroblogDatum.setSubGroupId(user.getSubGroupId());
                   }
                   singleMicroblogDataService.updateAll(singleMicroblogData);
               }

                //收藏
                List<Favourites> favourites = favouritesService.findByUserId(user.getId());
               if (ObjectUtil.isNotEmpty(favourites)){
                   for (Favourites favourite : favourites) {
                       favourite.setSubGroupId(user.getSubGroupId());
                   }
                   favouritesService.updaeAll(favourites);
               }
                System.err.println(i+=1);
            }
        }
        System.err.println("其他数据修改完成！");
    }
    /**
     * 修改历史数据  用户分组权限
     * @param request
     * @param response
     */
    @ApiOperation("修改历史数据 用户分组权限")
    @PostMapping(value = "/changHistoryDataForSubGroup")
    public void changHistoryDataForSubGroup(HttpServletRequest request, HttpServletResponse response) {
        //查询所有用户分组
        List<SubGroup> subGroups = subGroupService.findAll();
        if (ObjectUtil.isNotEmpty(subGroups)){
            for (SubGroup subGroup : subGroups) {
                //查找分组下的用户 仅取第一个即可
                List<User> users = userService.findBySubGroupId(subGroup.getId());
                if (ObjectUtil.isNotEmpty(users)){
                    User user = users.get(0);
                    Set<Role> roles = user.getRoles();
                    if (ObjectUtil.isNotEmpty(roles)){
                        subGroup.setRoles(roles);
                    }
                }
            }
        }
        System.err.println("用户分组修改成功！");
    }

    /**
     * 修改历史数据  用户分组权限
     * @param request
     * @param response
     */
    @ApiOperation("删除没有用户的用户分组")
    @PostMapping(value = "/deleteHistoryDataForSubGroup")
    public void deleteHistoryDataForSubGroup(HttpServletRequest request, HttpServletResponse response) {
        //查询所有用户分组
        List<SubGroup> subGroups = subGroupService.findAll();
        if (ObjectUtil.isNotEmpty(subGroups)){
            for (SubGroup subGroup : subGroups) {
                //查找分组下的用户 仅取第一个即可
                List<User> users = userService.findBySubGroupId(subGroup.getId());
                if (ObjectUtil.isEmpty(users)){
                    subGroupService.delete(subGroup.getId());
                }

            }
        }
    }

    @ApiOperation("修改删除文章")
    @PostMapping(value = "/updateHistoryDataForSubGroup")
    public void updateHistoryDataForSubGroup(HttpServletRequest request, HttpServletResponse response) {
        List<User> users = userService.findByCheckRole(CheckRole.ROLE_ORDINARY.toString());
        if (ObjectUtil.isNotEmpty(users)){
            for (User user : users) {
                List<ArticleDelete> articleDeletes = articleDeleteService.findByUserId(user.getId());
                if (ObjectUtil.isNotEmpty(articleDeletes)){
                    for (ArticleDelete articleDelete : articleDeletes) {
                        articleDelete.setSubGroupId(user.getSubGroupId());
                    }
                    articleDeleteService.updateForHistoryData(articleDeletes);
                }
            }
        }
        System.err.println("修改完成！");
    }
 }
