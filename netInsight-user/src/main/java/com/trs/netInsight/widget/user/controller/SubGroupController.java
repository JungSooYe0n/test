package com.trs.netInsight.widget.user.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.trs.jpa.utils.Criteria;
import com.trs.jpa.utils.Restrictions;
import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.AlertAccount;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.alert.entity.repository.AlertAccountRepository;
import com.trs.netInsight.widget.alert.entity.repository.AlertRuleRepository;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.NavigationConfig;
import com.trs.netInsight.widget.column.entity.NavigationEnum;
import com.trs.netInsight.widget.column.entity.emuns.ColumnFlag;
import com.trs.netInsight.widget.column.entity.emuns.SpecialFlag;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.repository.IndexPageRepository;
import com.trs.netInsight.widget.column.repository.IndexTabMapperRepository;
import com.trs.netInsight.widget.column.repository.IndexTabRepository;
import com.trs.netInsight.widget.column.repository.NavigationRepository;
import com.trs.netInsight.widget.common.util.CommonListChartUtil;
import com.trs.netInsight.widget.config.entity.SystemConfig;
import com.trs.netInsight.widget.config.service.ISystemConfigService;
import com.trs.netInsight.widget.microblog.entity.SingleMicroblogData;
import com.trs.netInsight.widget.report.entity.Favourites;
import com.trs.netInsight.widget.report.entity.ReportNew;
import com.trs.netInsight.widget.report.entity.TemplateNew;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.SpecialSubject;
import com.trs.netInsight.widget.special.entity.repository.SpecialProjectRepository;
import com.trs.netInsight.widget.special.entity.repository.SpecialSubjectRepository;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
    private IndexPageRepository indexPageRepository;
    @Autowired
    private IndexTabMapperRepository tabMapperRepository;

    @Autowired
    private NavigationRepository navigationRepository;

    @Autowired
    private IndexTabRepository indexTabRepository;

    @Autowired
    private AlertRuleRepository alertRuleRepository;


    @Autowired
    private SpecialProjectRepository specialProjectRepository;

    @Autowired
    private SpecialSubjectRepository specialSubjectRepository;

    @Autowired
    private AlertAccountRepository alertAccountRepository;

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
                              @ApiParam("同步数据，同步日常监测") @RequestParam(value = "columnSync",required = false) String[] columnSync,
                              @ApiParam("同步数据，同步日常监测") @RequestParam(value = "columnSyncLevel",required = false) String[] columnSyncLevel,
                              @ApiParam("同步数据，同步专题分析(带级别)") @RequestParam(value = "specialSyncLevel",required = false) String[] specialSyncLevel,
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

       return subGroupService.save(0,organizationId,name,filePicture,roleIds,columnNum,specialNum,alertNum,alertAccountNum,expireAt,columnSync,columnSyncLevel,specialSyncLevel,specialSync,userLimit,userJson);
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
    public Object updateSubGroup(@ApiParam("分组id") @RequestParam(value = "groupId") String groupId,
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
            organizationId = subGroupService.findOne(groupId).getOrganizationId();
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
                if (!subGroup.getId().equals(groupId)){
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
        List<User> users = userService.findBySubGroupId(groupId);
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

        SubGroup subGroup = subGroupService.findOne(groupId);
        if (ObjectUtil.isEmpty(subGroup)){
            throw new TRSException(CodeUtils.FAIL,"无法查到当前id对应的用户分组！");
        }
        List<IndexTab> bySubGroupId = indexTabRepository.findBySubGroupId(groupId);
        int colNum = (bySubGroupId==null)?0:bySubGroupId.size();
        if (columnNum < colNum){
            throw new TRSException(CodeUtils.FAIL,"当前设置日常监测栏目数 小于 该分组所拥有的栏目个数！");
        }
        // 专题个数
        if (specialNum < 1) {
            throw new TRSException(CodeUtils.FAIL, "专题事件数不能小于 1 ！");
        }
        if (specialNum > currentSpecialNum){
            throw new TRSException(CodeUtils.FAIL,"专题事件数已超出当前可添加总数！");
        }
        List<SpecialProject> specialProjects = specialProjectRepository.findBySubGroupId(groupId);
        int speciaCount = (specialProjects == null)?0:specialProjects.size();
        if (specialNum < speciaCount){
            throw new TRSException(CodeUtils.FAIL,"当前设置专题事件数 小于 该分组所拥有的专题事件个数！");
        }
        // 预警
        if (alertNum < 1) {
            throw new TRSException(CodeUtils.FAIL, "预警主题个数不能小于 1 ！");
        }
        if (alertNum > currentAlertNum) {
            throw new TRSException(CodeUtils.FAIL, "预警主题数已超出当前可添加总数 ！");
        }
        List<AlertRule> alertRules = alertRuleRepository.findBySubGroupId(groupId, new Sort(Sort.Direction.DESC, "createdTime"));
        int alertRuleCount = (alertRules == null)?0:alertRules.size();
        if (alertNum < alertRuleCount){
            throw new TRSException(CodeUtils.FAIL,"当前设置预警主题数 小于 该分组所拥有的预警主题个数！");
        }

        // 预警账号个数
        if (alertAccountNum < 1) {
            throw new TRSException(CodeUtils.FAIL, "可绑定预警账号数不能小于 1 ！");
        }
        if (alertAccountNum > currentAlertAccountNum) {
            throw new TRSException(CodeUtils.FAIL, "可绑定预警账号数已超出当前可绑定数 ！");
        }
        List<AlertAccount> alertWechatAccounts = alertAccountRepository.findBySubGroupIdAndType(groupId, SendWay.WE_CHAT);
        List<AlertAccount> alertEmailAccounts = alertAccountRepository.findBySubGroupIdAndType(groupId, SendWay.EMAIL);
        int alertWechatAccountCount = (alertWechatAccounts == null)?0:alertWechatAccounts.size();
        int alertEmailAccountCount = (alertEmailAccounts == null)?0:alertEmailAccounts.size();

        if (alertAccountNum < alertWechatAccountCount || alertAccountNum < alertEmailAccountCount){
            throw new TRSException(CodeUtils.FAIL,"当前设置可绑定预警账号数 小于 该分组所拥有的可绑定预警账号个数！");
        }

        subGroupService.update(groupId,name,filePicture,pictureName,roleIds,columnNum,specialNum,alertNum,alertAccountNum,expireAt,userLimit,userJson);

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
        User user= null;
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
        user = userService.findById(orgAdminId);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("column",selectColumn(user,""));
        //查询 该用户 下 的专题分析（一级或者二级分组下无专题则不返给前端）且带着层级关系
        resultMap.put("special",selectSomeSpecialsNew(user));
        return resultMap;
    }
    public List selectSomeSpecialsNew(User user) throws TRSException {
        //最终返回结果集

        List<SpecialSubject> oneIndexSubject = null;
        List<SpecialProject> oneIndexProject = null;
        List<SpecialProject> top = null;
        Map<String,Object> oneSpecial = this.getOneLevelSpecialForMap(user);
        if (oneSpecial.containsKey("page")) {
            oneIndexSubject = (List<SpecialSubject>) oneSpecial.get("page");
        }
        if (oneSpecial.containsKey("tab")) {
            oneIndexProject = (List<SpecialProject>) oneSpecial.get("tab");
        }
        if (oneSpecial.containsKey("top")) {
            top = (List<SpecialProject>) oneSpecial.get("top");
        }
        List<Object> result = new ArrayList<>();
        if(top != null && top.size()>0){
            result.addAll(top);
        }
        //获取到了第一层的栏目和分组信息，现在对信息进行排序
        result =  sortSpecial(result,oneIndexProject,oneIndexSubject,true,false);
        List<Boolean> isGetOne = new ArrayList<>();
        isGetOne.add(false);
        return formatResultSpecial(result,0,isGetOne,false);

    }
    public Object selectColumn(User user,String typeId) throws OperationException {
        List<IndexPage> oneIndexPage = null;
        List<IndexTabMapper> oneIndexTab = null;
        List<IndexTabMapper> top = null;
        Map<String,Object> oneColumn = this.getOneLevelColumnForMap(typeId,user);
        if (oneColumn.containsKey("page")) {
            oneIndexPage = (List<IndexPage>) oneColumn.get("page");
        }
        if (oneColumn.containsKey("tab")) {
            oneIndexTab = (List<IndexTabMapper>) oneColumn.get("tab");
        }
        if (oneColumn.containsKey("top")) {
            top = (List<IndexTabMapper>) oneColumn.get("top");
        }
        List<Object> result = new ArrayList<>();
        if(top != null && top.size()>0){
            result.addAll(top);
        }
        //获取到了第一层的栏目和分组信息，现在对信息进行排序
        List<Object> column =  sortColumn(result,oneIndexTab,oneIndexPage,true,false);
        List<Boolean> isGetOne = new ArrayList<>();
        isGetOne.add(false);
        return formatResultColumn(column,0,isGetOne,null);
    }
    public Map<String,Object> getOneLevelColumnForMap(String typeId,User loginUser){
        List<Sort.Order> orders=new ArrayList<Sort.Order>();
        orders.add( new Sort.Order(Sort.Direction.ASC, "sequence"));
        orders.add( new Sort.Order(Sort.Direction.ASC, "createdTime"));
        Specification<IndexPage> criteria_page = new Specification<IndexPage>(){

            @Override
            public Predicate toPredicate(Root<IndexPage> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicate = new ArrayList<>();
                if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
                    predicate.add(cb.equal(root.get("userId"),loginUser.getId()));
                    predicate.add(cb.isNull(root.get("subGroupId")));
                }else {
                    predicate.add(cb.equal(root.get("subGroupId"),loginUser.getSubGroupId()));
                }
                //predicate.add(cb.equal(root.get("typeId"),typeId));
                List<Predicate> predicateParent = new ArrayList<>();
                predicateParent.add(cb.isNull(root.get("parentId")));
                predicateParent.add(cb.equal(root.get("parentId"),""));

                predicate.add(cb.or(predicateParent.toArray(new Predicate[predicateParent.size()])));
                Predicate[] pre = new Predicate[predicate.size()];
                return query.where(predicate.toArray(pre)).getRestriction();
            }
        };
        Specification<IndexTabMapper> criteria_tab_mapper = new Specification<IndexTabMapper>(){

            @Override
            public Predicate toPredicate(Root<IndexTabMapper> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicate = new ArrayList<>();
                if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
                    predicate.add(cb.equal(root.get("userId"),loginUser.getId()));
                    predicate.add(cb.isNull(root.get("subGroupId")));
                }else {
                    predicate.add(cb.equal(root.get("subGroupId"),loginUser.getSubGroupId()));
                }
                //predicate.add(cb.equal(root.get("typeId"),typeId));
                predicate.add(cb.isNull(root.get("indexPage")));
                Predicate[] pre = new Predicate[predicate.size()];
                return query.where(predicate.toArray(pre)).getRestriction();
            }
        };

        Map<String,Object> result = new HashMap<>();
        //获取当前用户一层分组内的所有内容
        List<IndexPage> oneIndexPage = null;
        List<IndexTabMapper> oneIndexTab = null;

        oneIndexPage = indexPageRepository.findAll(criteria_page,new Sort(orders));
        oneIndexTab = tabMapperRepository.findAll(criteria_tab_mapper,new Sort(orders));
        if(oneIndexPage != null && oneIndexPage.size() >0){
            result.put("page",oneIndexPage);
        }
        if(oneIndexTab != null && oneIndexTab.size() >0){
            List<IndexTabMapper> topList = new ArrayList<>();
            List<IndexTabMapper> otherIndexTab = new ArrayList<>();
            for(IndexTabMapper mapper :oneIndexTab){
                if("top".equals(mapper.getTopFlag())){
                    topList.add(mapper);
                }else{
                    otherIndexTab.add(mapper);
                }
            }
            if(topList != null && topList.size() >0){
                result.put("top",topList);
            }
            if(otherIndexTab != null && otherIndexTab.size() >0){
                result.put("tab",otherIndexTab);
            }
        }
        return  result;
    }
    public List<Object> sortColumn(List<Object> result,List<IndexTabMapper> mapperList,List<IndexPage> indexPageList,Boolean sortAll,Boolean onlySortPage){

        List<Map<String,Object>> sortList = new ArrayList<>();
        if(!onlySortPage){
            if(mapperList != null && mapperList.size() >0){
                for(int i =0;i<mapperList.size();i++){
                    Map<String,Object> map = new HashMap<>();
                    map.put("id",mapperList.get(i).getId());
                    map.put("sequence",mapperList.get(i).getSequence());
                    map.put("index",i);
                    //栏目类型为1
                    map.put("flag",ColumnFlag.IndexTabFlag);
                    sortList.add(map);
                }
            }
        }
        if(indexPageList != null && indexPageList.size() > 0){
            for(int i =0;i<indexPageList.size();i++){
                Map<String,Object> map = new HashMap<>();
                map.put("id",indexPageList.get(i).getId());
                map.put("sequence",indexPageList.get(i).getSequence());
                map.put("index",i);
                //分组类型为0
                map.put("flag", ColumnFlag.IndexPageFlag);
                sortList.add(map);
            }
        }
        if(sortList.size() >0){
            Collections.sort(sortList, (o1, o2) -> {
                Integer seq1 = (Integer) o1.get("sequence");
                Integer seq2 = (Integer) o2.get("sequence");
                return seq1.compareTo(seq2);
            });
            //sortList 排序过后的数据
            //只排序当前层，排序过后的数据，按顺序取出并返回
            for(Map<String,Object> map : sortList){
                ColumnFlag flag = (ColumnFlag) map.get("flag");
                Integer index = (Integer) map.get("index");
                if(flag.equals(ColumnFlag.IndexPageFlag)){
                    IndexPage indexPage = indexPageList.get(index);
                    if(sortAll){
                        //获取子类的数据进行排序
                        List<IndexPage> child_page = indexPage.getChildrenPage();
                        List<IndexTabMapper> child_mapper = null;
                        if(!onlySortPage){
                            child_mapper = indexPage.getIndexTabMappers();
                        }
                        if( (child_mapper != null && child_mapper.size()>0) || (child_page != null && child_page.size() >0) ){
                            indexPage.setColumnList(sortColumn(new ArrayList<>(),child_mapper,child_page,sortAll,onlySortPage));
                        }
                    }
                    result.add(indexPage);
                }else if(flag.equals(ColumnFlag.IndexTabFlag)){
                    IndexTabMapper mapper = mapperList.get(index);
                    //栏目只有一层，直接添加就行
                    result.add(mapper);
                }
            }
        }

        return result;
    }
    private Object formatResultColumn(List<Object> list,Integer level,List<Boolean> isGetOne ,Object returnResult) {
        Map<String,Object> topMap = new HashMap<>();
        if(returnResult != null){
            topMap= (Map<String,Object>) returnResult;
        }
        List<Object> result = new ArrayList<>();
        Map<String, Object> map = null;
        if (list != null && list.size() > 0) {
            for (Object obj : list) {
                map = new HashMap<>();
                if (obj instanceof IndexTabMapper) {
                    IndexTabMapper mapper = (IndexTabMapper) obj;
                    IndexTab tab = mapper.getIndexTab();
                    map.put("id", mapper.getId());
                    map.put("name", tab.getName());
                    map.put("parentId",tab.getParentId());
                    map.put("flag", ColumnFlag.IndexTabFlag.ordinal());
                    result.add(map);
                }

            }
            for (Object obj : list) {
                map = new HashMap<>();
            if (obj instanceof IndexPage) {
                    IndexPage page = (IndexPage) obj;
                    map.put("id", page.getId());
                    map.put("name", page.getName());
                map.put("parentId",page.getParentId());
                    map.put("flag", ColumnFlag.IndexPageFlag.ordinal());
                    List<Object> childColumn = page.getColumnList();
                    Object child = null;
                    map.put("children", child);
                    if (childColumn != null && childColumn.size() > 0) {
                        child = this.formatResultColumn(childColumn,level+1,isGetOne,map);
                        map.put("children", child);
                    }
                result.add(map);
                }

            }
        }
        return result;
    }
    public List selectSomeSpecials(String orgAdminId) throws TRSException {
        //最终返回结果集
        List resultList = new ArrayList();

        //先把无分组的专题放入
        Criteria<SpecialProject> criteriaProject2 = new Criteria<>();
        criteriaProject2.add(Restrictions.eq("userId", orgAdminId));
        criteriaProject2.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
        List<SpecialProject> listSpecialProject = specialProjectRepository.findAll(criteriaProject2);
        //	listSpecialProject = this.removeSomeProjects(listSpecialProject,user);
        for (SpecialProject special : listSpecialProject) {
            if (StringUtil.isEmpty(special.getGroupId())) {
                resultList.add(special);
            }
        }

        //查询一级分组
        Criteria<SpecialSubject> criteria = new Criteria<>();
        criteria.add(Restrictions.eq("flag", 0));
        criteria.add(Restrictions.eq("userId", orgAdminId));
        criteria.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
        // 查询主题（一级分类）
        // 主题是一级 只按照拖拽顺序排 不按照修改时间排
        List<SpecialSubject> listSubject = specialSubjectRepository.findAll(criteria);

        //listSubject = this.removeSomeSubjects(listSubject,user);
        //查询二级
        if (ObjectUtil.isNotEmpty(listSubject)){

            for (SpecialSubject specialSubject : listSubject) {

                //放入 一级分组下的 专题 或者 二级分组
                List listMiddle = new ArrayList();
                // 查询一级下 的 专题
                Criteria<SpecialProject> criteria3 = new Criteria<>();
                criteria3.add(Restrictions.eq("groupId", specialSubject.getId()));
                criteria3.add(Restrictions.eq("userId",orgAdminId));
                criteria3.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
                List<SpecialProject> listSmall = specialProjectRepository.findAll(criteria3);// 放到第二级
                //	listSmall = this.removeSomeProjects(listSmall,user);
                listMiddle.addAll(listSmall);

                //查询二级 主题分组
                Criteria<SpecialSubject> criteriaSubject = new Criteria<>();
                criteriaSubject.add(Restrictions.eq("subjectId", specialSubject.getId()));
                criteriaSubject.add(Restrictions.eq("flag", 1));//二级分类
                criteriaSubject.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
                List<SpecialSubject> list = specialSubjectRepository.findAll(criteriaSubject);
                if (ObjectUtil.isNotEmpty(list)){
                    for (SpecialSubject subject : list) {
                        //查询 二级分组下的专题
                        Criteria<SpecialProject> criteria2 = new Criteria<>();
                        criteria2.add(Restrictions.eq("groupId", subject.getId()));
                        criteria2.orderByASC("sequence");// 只按照拖拽顺序排 不按照修改时间排
                        List<SpecialProject> list2 = specialProjectRepository.findAll(criteria2);// 放到第三级

                        Map<String, Object> putValue = null;
                        if(ObjectUtil.isNotEmpty(list2)){//若分组下无专题 则不放在结果集内
                            putValue = MapUtil.putValue(
                                    new String[] { "specialName", "flag", "zhuantiDetail", "currentTheme", "id","groupId" },
                                    subject.getName(), subject.getFlag(), list2, subject.isCurrentTheme(),
                                    subject.getId(),specialSubject.getId());
                            listMiddle.add(putValue);
                        }
                    }
                }

                //如果该一级分组下有专题存在 则放入结果集内
                if (ObjectUtil.isNotEmpty(listMiddle)){
                    Map<String, Object> putValue = MapUtil.putValue(
                            new String[] { "specialName", "flag", "zhutiDetail", "currentTheme", "id" }, specialSubject.getName(),
                            specialSubject.getFlag(), listMiddle, specialSubject.isCurrentTheme(),specialSubject.getId());
                    resultList.add(putValue);
                }

            }
        }

        return resultList;
    }

    public List<Map<String, Object>> findByOrgAdminId(String orgAdminId) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> data = null;
        // 优化 一次性把一二级都返回给前端
        // if (one) { // 一级栏目组（导航栏标记）
        List<NavigationConfig> navigations = navigationRepository.findByUserIdAndSubGroupIdNull(orgAdminId);
        if (CollectionsUtil.isNotEmpty(navigations)) {
            for (NavigationConfig navigationConfig : navigations) {
                if (navigationConfig.getType() == NavigationEnum.column
                        || navigationConfig.getType() == NavigationEnum.definedself) {
                    String tpId = "";
                    data = new HashMap<>();
                    data.put("indexPageName", navigationConfig.getName());
                    data.put("id", navigationConfig.getId());
                    data.put("definedself", 1);
                    if (navigationConfig.getType() == NavigationEnum.definedself) {
                        tpId = navigationConfig.getId();
                        data.put("definedself", 0);
                    }
                    List<IndexPage> indexPages = indexPageRepository.findByUserIdAndTypeId(orgAdminId, tpId);
                    List<Map<String, Object>> list = new ArrayList<>();
                    if (CollectionsUtil.isNotEmpty(indexPages)) {
                        for (IndexPage indexPage : indexPages) {
                            if (StringUtil.isEmpty(indexPage.getParentId())) {
                                Map<String, Object> datalist = new HashMap<>();
                                datalist.put("indexPageName", indexPage.getName());
                                datalist.put("id", indexPage.getId());
                                datalist.put("definedself", 999);// 前端忽略改值
                                list.add(datalist);
                            }
                        }
                    }
                    data.put("list", list);
                    result.add(data);
                }
            }
        }
        return result;
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
    public Map<String,Object> getOneLevelSpecialForMap(User loginUser){
        Sort sort = new Sort(Sort.Direction.ASC,"sequence");
        Specification<SpecialSubject> criteria_page = new Specification<SpecialSubject>(){

            @Override
            public Predicate toPredicate(Root<SpecialSubject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicate = new ArrayList<>();
                if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
                    predicate.add(cb.equal(root.get("userId"),loginUser.getId()));
                }else {
                    predicate.add(cb.equal(root.get("subGroupId"),loginUser.getSubGroupId()));
                }
                List<Predicate> predicateParent = new ArrayList<>();
                predicateParent.add(cb.isNull(root.get("subjectId")));
                predicateParent.add(cb.equal(root.get("subjectId"),""));

                predicate.add(cb.or(predicateParent.toArray(new Predicate[predicateParent.size()])));
                Predicate[] pre = new Predicate[predicate.size()];
                return query.where(predicate.toArray(pre)).getRestriction();
            }
        };
        Specification<SpecialProject> criteria_tab_mapper = new Specification<SpecialProject>(){

            @Override
            public Predicate toPredicate(Root<SpecialProject> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicate = new ArrayList<>();
                if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
                    predicate.add(cb.equal(root.get("userId"),loginUser.getId()));
                }else {
                    predicate.add(cb.equal(root.get("subGroupId"),loginUser.getSubGroupId()));
                }
                predicate.add(cb.isNull(root.get("groupId")));
                Predicate[] pre = new Predicate[predicate.size()];
                return query.where(predicate.toArray(pre)).getRestriction();
            }
        };

        Map<String,Object> result = new HashMap<>();
        //获取当前用户一层分组内的所有内容
        List<SpecialSubject> oneIndexPage = null;
        List<SpecialProject> oneIndexTab = null;

        oneIndexPage = specialSubjectRepository.findAll(criteria_page,sort);
        oneIndexTab = specialProjectRepository.findAll(criteria_tab_mapper,sort);
        if(oneIndexPage != null && oneIndexPage.size() >0){
            result.put("page",oneIndexPage);
        }
        if(oneIndexTab != null && oneIndexTab.size() >0){
            List<SpecialProject> topList = new ArrayList<>();
            List<SpecialProject> otherProjectList = new ArrayList<>();
            for(SpecialProject specialProject :oneIndexTab){
                if("top".equals(specialProject.getTopFlag())){
                    topList.add(specialProject);
                }else{
                    otherProjectList.add(specialProject);
                }
            }
            if(topList != null && topList.size() >0){
                result.put("top",topList);
            }
            if(otherProjectList != null && otherProjectList.size() >0){
                result.put("tab",otherProjectList);
            }
        }
        return  result;
    }
    public List<Object> sortSpecial(List<Object> result,List<SpecialProject> mapperList,List<SpecialSubject> indexPageList,Boolean sortAll,Boolean onlySortPage){
        List<Map<String,Object>> sortList = new ArrayList<>();
        if(!onlySortPage){
            if(mapperList != null && mapperList.size() >0){
                for(int i =0;i<mapperList.size();i++){
                    Map<String,Object> map = new HashMap<>();
                    map.put("id",mapperList.get(i).getId());
                    map.put("sequence",mapperList.get(i).getSequence());
                    map.put("index",i);
                    //栏目类型为1
                    map.put("flag",SpecialFlag.SpecialProjectFlag);
                    sortList.add(map);
                }
            }
        }
        if(indexPageList != null && indexPageList.size() > 0){
            for(int i =0;i<indexPageList.size();i++){
                Map<String,Object> map = new HashMap<>();
                map.put("id",indexPageList.get(i).getId());
                map.put("sequence",indexPageList.get(i).getSequence());
                map.put("index",i);
                //分组类型为0
                map.put("flag", SpecialFlag.SpecialSubjectFlag);
                sortList.add(map);
            }
        }
        if(sortList.size() >0){
            Collections.sort(sortList, (o1, o2) -> {
                Integer seq1 = (Integer) o1.get("sequence");
                Integer seq2 = (Integer) o2.get("sequence");
                return seq1.compareTo(seq2);
            });
            //sortList 排序过后的数据
            //只排序当前层，排序过后的数据，按顺序取出并返回
            for(Map<String,Object> map : sortList){
                SpecialFlag flag = (SpecialFlag) map.get("flag");
                Integer index = (Integer) map.get("index");
                if(flag.equals(SpecialFlag.SpecialSubjectFlag)){
                    SpecialSubject indexPage = indexPageList.get(index);
                    if(sortAll){
                        //获取子类的数据进行排序
                        List<SpecialSubject> child_page = indexPage.getChildrenPage();
                        List<SpecialProject> child_mapper = null;
                        if(!onlySortPage){
                            child_mapper = indexPage.getIndexTabMappers();
                        }
                        if( (child_mapper != null && child_mapper.size()>0) || (child_page != null && child_page.size() >0) ){
                            indexPage.setColumnList(sortSpecial(new ArrayList<Object>(),child_mapper,child_page,sortAll,onlySortPage));
                        }
                    }
                    result.add(indexPage);
                }else if(flag.equals(SpecialFlag.SpecialProjectFlag)){
                    SpecialProject mapper = mapperList.get(index);
                    //栏目只有一层，直接添加就行
                    result.add(mapper);
                }
            }
        }

        return result;
    }
    private List<Object> formatResultSpecial(List<Object> list,Integer level,List<Boolean> isGetOne,Boolean parentHide) {
        List<Object> result = new ArrayList<>();
        Map<String, Object> map = null;
        if (list != null && list.size() > 0) {
            for (Object obj : list) {
                map = new HashMap<>();
                if (obj instanceof SpecialProject) {
                    SpecialProject tab = (SpecialProject) obj;
                    map.put("id",  tab.getId());
                    map.put("parentId",tab.getGroupId());
                    map.put("name", tab.getSpecialName());
                    map.put("flag", SpecialFlag.SpecialProjectFlag.ordinal());
                    result.add(map);
                }
            }
            for (Object obj : list) {
                map = new HashMap<>();
                if (obj instanceof SpecialSubject) {
                    SpecialSubject page = (SpecialSubject) obj;
                    map.put("id", page.getId());
                    map.put("name", page.getName());
                    map.put("flag", SpecialFlag.SpecialSubjectFlag.ordinal());
                    map.put("parentId",page.getSubjectId());
                    List<Object> childColumn = page.getColumnList();
                    List<Object> child = new ArrayList<>();
                    //如果父级被隐藏，这一级也会被隐藏，直接用父级的隐藏值
                    //如果父级没被隐藏，当前级被隐藏，则用当前级的隐藏值
                    //如果父级没隐藏，当前级没隐藏，用没隐藏，父级则可
                    if(!parentHide){
                        parentHide = true;
                    }
                    //如果分组被隐藏了，前端不会显示，所以这里不查询了
                    if (childColumn != null && childColumn.size() > 0) {
                        child = this.formatResultSpecial(childColumn,level+1,isGetOne,parentHide);
                    }
                    map.put("children", child);
                    result.add(map);
                }

            }
        }
        return result;
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
 }
