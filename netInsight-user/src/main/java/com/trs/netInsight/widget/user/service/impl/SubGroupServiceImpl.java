package com.trs.netInsight.widget.user.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.AlertAccount;
import com.trs.netInsight.widget.alert.entity.AlertRule;
import com.trs.netInsight.widget.alert.entity.repository.AlertAccountRepository;
import com.trs.netInsight.widget.alert.entity.repository.AlertRuleRepository;
import com.trs.netInsight.widget.column.entity.IndexPage;
import com.trs.netInsight.widget.column.entity.IndexTab;
import com.trs.netInsight.widget.column.entity.NavigationConfig;
import com.trs.netInsight.widget.column.entity.mapper.IndexTabMapper;
import com.trs.netInsight.widget.column.repository.IndexPageRepository;
import com.trs.netInsight.widget.column.repository.IndexTabRepository;
import com.trs.netInsight.widget.column.repository.NavigationRepository;
import com.trs.netInsight.widget.special.entity.SpecialProject;
import com.trs.netInsight.widget.special.entity.SpecialSubject;
import com.trs.netInsight.widget.special.entity.repository.SpecialProjectRepository;
import com.trs.netInsight.widget.special.entity.repository.SpecialSubjectRepository;
import com.trs.netInsight.widget.user.entity.*;
import com.trs.netInsight.widget.user.entity.enums.CheckRole;
import com.trs.netInsight.widget.user.entity.enums.Status;
import com.trs.netInsight.widget.user.repository.SubGroupRepository;
import com.trs.netInsight.widget.user.service.IOrganizationService;
import com.trs.netInsight.widget.user.service.IRoleService;
import com.trs.netInsight.widget.user.service.ISubGroupService;
import com.trs.netInsight.widget.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.util.*;


/**
 * 分组业务层接口实现类
 * @author 北京拓尔思信息技术股份有限公司
 * Created by yangyanyan on 2019/7/24 14:54.
 * @desc
 */
@Slf4j
@Service
public class SubGroupServiceImpl implements ISubGroupService {


    @Autowired
    private IndexPageRepository indexPageRepository;

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
    private SubGroupRepository subGroupRepository;
    @Autowired
    private IUserService userService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private HelpService helpService;

    @Autowired
    private IOrganizationService organizationService;

    private int projectNum = 0;
    private int cloumCutentNum = 0;

    @Override
    public Page<SubGroup> findByOrgId( String orgId,int pageNo,int pageSize,String surplusDateSort,
                                      String status,String retrievalCondition, String retrievalInformation) throws TRSException {
        Sort sort = null;
        if (StringUtils.isBlank(surplusDateSort)){
            //默认排序（机构创建日期降序排，即最新创建的在上面）
            sort = new Sort(Sort.Direction.DESC, "createdTime");
        }else if ("desc".equals(surplusDateSort)){
            //剩余有效期 降序排(即 到期字段降序排)
            sort = new Sort(Sort.Direction.DESC, "expireAt");
        }else if ("asc".equals(surplusDateSort)){
            //剩余有效期 升序排(即 到期字段升序排)
            sort = new Sort(Sort.Direction.ASC, "expireAt");
        }else {
            throw new TRSException(CodeUtils.FAIL, "请输入正确的排序方式！");
        }
        Pageable pageable = new PageRequest(pageNo, pageSize, sort);

        Page<SubGroup> subGroups = null;
        if ("userName".equals(retrievalCondition)){
            Specification<User> specification = new Specification<User>() {
                @Override
                public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    List<Predicate> predicates = new ArrayList<>();
                    if (StringUtils.isNotBlank(status)) {
                        predicates.add(cb.equal(root.get("status"), status));
                    }
                    if (StringUtils.isNotBlank(retrievalInformation) && StringUtils.isNotBlank(retrievalCondition)) {
                        predicates.add(cb.like(root.get(retrievalCondition), "%" + retrievalInformation + "%"));
                    }
                    //当前机构下
                    if (StringUtils.isNotBlank(orgId)){
                        predicates.add(cb.equal(root.get("organizationId"),orgId));
                    }
                    Predicate[] pre = new Predicate[predicates.size()];
                    return query.where(predicates.toArray(pre)).getRestriction();
                }
            };
            List<User> users = userService.findAll(specification);
            if (ObjectUtil.isNotEmpty(users)){
                List<String> subGroupIds = new ArrayList<>();
                for (User user : users) {
                    if (StringUtil.isNotEmpty(user.getSubGroupId())){
                        subGroupIds.add(user.getSubGroupId());
                    }
                }
                subGroups = subGroupRepository.findByIdIn(subGroupIds,pageable);
            }
        }else {
            Specification<SubGroup> specification = new Specification<SubGroup>() {
                @Override
                public Predicate toPredicate(Root<SubGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    List<Predicate> predicates = new ArrayList<>();
                    if (StringUtils.isNotBlank(status)) {
                        predicates.add(cb.equal(root.get("status"), status));
                    }
                    if (StringUtils.isNotBlank(retrievalInformation) && StringUtils.isNotBlank(retrievalCondition)) {
                        predicates.add(cb.like(root.get(retrievalCondition), "%" + retrievalInformation + "%"));
                    }
                    //当前机构下
                    if (StringUtils.isNotBlank(orgId)){
                        predicates.add(cb.equal(root.get("organizationId"),orgId));
                    }
                    Predicate[] pre = new Predicate[predicates.size()];
                    return query.where(predicates.toArray(pre)).getRestriction();
                }
            };
           subGroups = subGroupRepository.findAll(specification,pageable);
        }
        return addExpireDateAndLoginCount(subGroups);
    }

    @Override
    public List<SubGroup> findByOrgId(String orgId) {
        return subGroupRepository.findByOrganizationId(orgId);
    }

    /**
     * 放入 剩余有效期 的天数 和 该分组下用户登录次数
     * @param page
     * @return
     */
    private Page<SubGroup> addExpireDateAndLoginCount(Page<SubGroup> page){
        if (page != null && page.getContent() != null && page.getContent().size() > 0){
            List<SubGroup> content = page.getContent();
            for (SubGroup subGroup : content) {
                if (subGroup.getRoles().size() > 1){
                    Set<Role> roles = subGroup.getRoles();
                    Set<Role> roleTreeSet = new TreeSet<>();

                    for (Role role : roles) {
                        if ("新建".equals(role.getRoleName())){
                            roleTreeSet.add(role);
                        }else {
                            roleTreeSet.add(role);
                        }
                    }

                    subGroup.setRoles(roleTreeSet);
                }

                //剩余有效期
                //剩余有效期转换
                if (UserUtils.FOREVER_DATE.equals(subGroup.getExpireAt())){
                    subGroup.setSurplusDate("永久");
                }else {
                    String days = DateUtil.timeDifferenceDays(subGroup.getExpireAt());
                    subGroup.setSurplusDate(days);
                }
                //今日登录次数
                subGroup.setLoginCount(UserUtils.getLoginCount(subGroup.getId()));
                //放入机构名称
                Organization organization = organizationService.findById(subGroup.getOrganizationId());
                if (ObjectUtil.isNotEmpty(organization)){
                    subGroup.setOrganizationName(organization.getOrganizationName());
                }
                //放入当前用户分组下的用户账号信息
                subGroup.setUsers(userService.findBySubGroupId(subGroup.getId()));
            }
        }
        return page;
    }
    @Override
    public boolean save(int isAutoadd,String orgId,String name, MultipartFile picture, String[] roleIds, int columnNum, int specialNum, int alertNum, int alertAccount, String expireAt, String[] columnSync,String[] columnSyncLevel,
                        String[] specialSyncLevel,String[] specialSync, int userLimit, String userJson) throws TRSException {
        /*List<SubGroup> subGroupList = subGroupRepository.findByOrganizationId(orgId);
        if(subGroupList!= null && subGroupList.size()>0){
            for(SubGroup subGroup:subGroupList){
                if(name.equals(subGroup.getName())){
                    throw new TRSException(CodeUtils.FAIL,"该分组名称已存在！");
                }
            }
        }*/
        if (ObjectUtil.isNotEmpty(columnSync)){
//            List<DataSyncColumn> columnData = JSONArray.parseArray(columnSync, DataSyncColumn.class);
//            if (ObjectUtil.isNotEmpty(columnData)){
//                int columnJsonNums = 0;
//                for (DataSyncColumn columnDatum : columnData) {
//                    columnJsonNums += columnDatum.getList().size();
//                }
//                if (columnJsonNums > columnNum){
//                    throw new TRSException(CodeUtils.FAIL,"当前同步栏目总数 大于 栏目限制数！");
//                }
//            }
        }

        if (ObjectUtil.isNotEmpty(columnSync) || ObjectUtil.isNotEmpty(columnSyncLevel)){
            int columJsonNums = 0;
            cloumCutentNum = 0;
            List<IndexPage> dataSyncSpecials = indexPageRepository.findByIdIn(Arrays.asList(columnSyncLevel));
            if (ObjectUtil.isNotEmpty(dataSyncSpecials)){
                for (IndexPage dataSyncSpecial : dataSyncSpecials) {
                    if (0 == dataSyncSpecial.getFlag() && StringUtil.isNotEmpty(dataSyncSpecial.getId())) {
                        //分组
                        setCloumNum(dataSyncSpecial.getId());
                    }
                }
            }
            columJsonNums += columnSync.length + cloumCutentNum;
            if (columJsonNums > columnNum){
                throw new TRSException(CodeUtils.FAIL,"当前同步栏目总数 大于 栏目限制数！");
            }
        }
        if (ObjectUtil.isNotEmpty(specialSync) || ObjectUtil.isNotEmpty(specialSyncLevel)){
            int specialJsonNums = 0;
            projectNum = 0;
            List<SpecialSubject> dataSyncSpecials = specialSubjectRepository.findByIdIn(Arrays.asList(specialSyncLevel));
            if (ObjectUtil.isNotEmpty(dataSyncSpecials)){
                for (SpecialSubject dataSyncSpecial : dataSyncSpecials) {
                    if (0 == dataSyncSpecial.getFlag() && StringUtil.isNotEmpty(dataSyncSpecial.getId())) {
                        //分组
                        setProjectNum(dataSyncSpecial.getId());
                    }
                }
            }
            specialJsonNums += specialSync.length + projectNum;
            if (specialJsonNums > specialNum){
                throw new TRSException(CodeUtils.FAIL,"当前同步专题总数 大于 专题限制数！");
            }
        }

        //上传logo
        String fileName = null;
        if (null != picture){
            try {
                fileName = PictureUtil.transferLogo(picture, name, "group");
            } catch (IOException e) {
                log.error("添加分组时上传logo图片出错！",e);
                throw new TRSException(CodeUtils.FAIL,"添加分组时上传logo图片出错！");
            }
        }

        SubGroup subGroup = new SubGroup();
        subGroup.setName(name);
        subGroup.setAlertAccountNum(alertAccount);
        subGroup.setAlertNum(alertNum);
        subGroup.setColumnNum(columnNum);
        subGroup.setSpecialNum(specialNum);
        subGroup.setExpireAt(expireAt);
        subGroup.setUserLimit(userLimit);
        Organization organization = organizationService.findById(orgId);
        Status status = Status.getStatusByValue(organization.getStatus());
        subGroup.setStatus(status);
        subGroup.setLogoPicName(fileName);
        subGroup.setOrganizationId(orgId);
        //权限
        if (roleIds == null || roleIds.length <= 0){
            subGroup.setRoles(null);
        }else {
            List<Role> roles = roleService.findByIds(roleIds);
            subGroup.setRoles(new HashSet<>(roles));
        }
        //添加当前分组 并获取当前分组的id
        String subGroupId = this.save(subGroup,columnSync,columnSyncLevel,specialSync,specialSyncLevel);

        if (StringUtil.isNotEmpty(userJson)){
            List<User> users = JSONArray.parseArray(userJson, User.class);
            if (ObjectUtil.isNotEmpty(users)){
                if (users.size() > userLimit){
                    this.delete(subGroupId);
                    throw new TRSException(CodeUtils.USER_LIMIT,"当前创建的用户数 大于 限制的登录账号个数！");
                }
                //添加用户
                for (int j = 0; j <users.size() ; j++) {

                    User user = users.get(j);
                    // 判断账号是否为空
                    if (StringUtils.isBlank(user.getUserName())) {
                        this.delete(subGroupId);
                        throw new TRSException(CodeUtils.ACCOUNT_NULL, "账号不能为空！");
                    }
                    // 判断账号是否有空格
                    if (RegexUtils.checkBlankSpace(user.getUserName())) {
                        this.delete(subGroupId);
                        throw new TRSException(CodeUtils.ACCOUNT_SPACE, "账号中不能含有空格！");
                    }

                    // 判断用户名是否为空
                    if (StringUtils.isBlank(user.getDisplayName())) {
                        this.delete(subGroupId);
                        throw new TRSException(CodeUtils.DISPLAYNAME_NULL, "用户名不能为空！");
                    }

                    // 判断密码是否为空
                    if (StringUtils.isBlank(user.getPassword())) {
                        this.delete(subGroupId);
                        throw new TRSException(CodeUtils.PASSWORD_NULL, "密码不能为空！");
                    }
                    // 判断密码强度
                    if (!RegexUtils.isLowSafely(user.getPassword())) {
                        this.delete(subGroupId);
                        throw new TRSException(CodeUtils.PASSWORD_LOW, "密码需要在8-16位，并且有大写、小写、数字和特殊字符至少三种！");
                    }

                    // 判断邮件是否为空
                    if (StringUtils.isNotBlank(user.getEmail())) {
                        // 判断邮件是否正确
                        if (!RegexUtils.checkEmail(user.getEmail())) {
                            this.delete(subGroupId);
                            throw new TRSException(CodeUtils.EMAIL_FALSE, "邮箱格式不正确！");
                        }
                    }

                    // 判断两次密码是否一致
                    if (!user.getPassword().equals(user.getPasswordAgain())) {
                        this.delete(subGroupId);
                        throw new TRSException(CodeUtils.PASSWORD_NOT_SAME, "两次密码不一样！");
                    }
                    // 验证手机号
                    if (StringUtils.isNotBlank(user.getPhone()) && !RegexUtils.checkMobile(user.getPhone())) {
                        this.delete(subGroupId);
                        throw new TRSException(CodeUtils.PHONE_FAIL, "手机号填写错误！");
                    }
                    // 判断用户账号是否存在
                    if (userService.findByUserName(user.getUserName()) != null) {
                        this.delete(subGroupId);
                        throw new TRSException(CodeUtils.USERNAME_EXISTED, "用户账号已存在！");
                    }
                    User loginUser = UserUtils.getUser();
                    // 判断用户是否有权限新建 -- 自动创建不验证权限
                    if(isAutoadd != 1){ //等于1自动创建,不判断权限
                        if (!UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())) {
                            this.delete(subGroupId);
                            throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限新建用户！");
                        }
                    }

                    //普通用户
                    user.setCheckRole(CheckRole.ROLE_ORDINARY);
                    user.setSubGroupId(subGroupId);
                    // 盐
                    String salt = UUID.randomUUID().toString();
                    // 加密的盐
                    String encryptPsw = UserUtils.getEncryptPsw(user.getPassword(), salt);
                    user.setPassword(encryptPsw);
                    user.setSalt(salt);
                    user.setLastLoginTime(null);
                    user.setLastLoginIp(null);
                    user.setStatus(Status.normal);
                    user.setOrganizationId(orgId);
                    user.setExpireAt(subGroup.getExpireAt());
                    //判断角色
                    List<String> subGroupRoles = Arrays.asList(roleIds);
                    String[] userRoleIds = user.getRoleIds();
                    if (ObjectUtil.isNotEmpty(userRoleIds)){
                        for (int i = 0; i < userRoleIds.length; i++) {
                           if ( ! subGroupRoles.contains(userRoleIds[i])){
                               if (users.size() == 1){
                                   this.delete(subGroupId);
                                   throw new TRSException(CodeUtils.FAIL,"当前用户添加了无效权限");
                               }else if (users.size() > 1){
                                   this.delete(subGroupId);
                                   throw new TRSException(CodeUtils.FAIL,"第"+(j+1)+"用户添加了无效权限");
                               }

                           }
                        }

                        List<Role> roles = roleService.findByIds(userRoleIds);
                        user.setRoles(new HashSet<>(roles));
                    }else {
                        user.setRoleIds(null);
                    }
                    try {
                        userService.add(user,false);
                    } catch (Exception e) {
                        this.delete(subGroupId);
                        e.printStackTrace();
                    }
                }
            }
        }else {
            this.delete(subGroupId);
            throw new TRSException(CodeUtils.FAIL,"请告诉登录账号信息！");
        }

        return true;
    }
    public void setProjectNum(String id){
        SpecialSubject subject = specialSubjectRepository.findOne(id);
        if (ObjectUtil.isNotEmpty(subject.getIndexTabMappers())){
            projectNum += subject.getIndexTabMappers().size();
        }
        if (ObjectUtil.isNotEmpty(subject.getChildrenPage())){
            for (SpecialSubject specialSubject : subject.getChildrenPage()){
                setProjectNum(specialSubject.getId());
            }
        }
    }
    public void setCloumNum(String id){
        IndexPage indexPage = indexPageRepository.findOne(id);
        if (ObjectUtil.isNotEmpty(indexPage.getIndexTabMappers())){
            cloumCutentNum += indexPage.getIndexTabMappers().size();
        }
        if (ObjectUtil.isNotEmpty(indexPage.getChildrenPage())){
            for (IndexPage page : indexPage.getChildrenPage()){
                setCloumNum(page.getId());
            }
        }
    }
    @Override
    public String save(SubGroup subGroup,String[] columnSync,String[] columnSyncLevel,String[] specialSync,String[] specialSyncLevel) throws TRSException {
        SubGroup group = subGroupRepository.save(subGroup);
        if (null == group){
            return null;
        }
//        if (StringUtils.isNotBlank(columnSync)){
//            //同步选中的日常监测数据
//            List<DataSyncColumn> columnData = JSONArray.parseArray(columnSync, DataSyncColumn.class);
//            if (ObjectUtil.isNotEmpty(columnData)){
//                List<String> navIds = new ArrayList<>();
//                List<String> pageIds = new ArrayList<>();
//                for (DataSyncColumn dataSyncColumn : columnData) {
//                    //oneId对应导航
//                    navIds.add(dataSyncColumn.getId());
//                    List<DataSyncColumn> list = dataSyncColumn.getList();
//                    if (ObjectUtil.isNotEmpty(list)){
//                        for (DataSyncColumn dataSyncColumn1 : list) {
//                            pageIds.add(dataSyncColumn1.getId());
//                        }
//                    }
//                }
//                try {
//                    //同步导航 到 该用户分组下
//                    helpService.copySomeNavigationToUserGroup(navIds,subGroup);
//                    //同步栏目组及栏目  到 该用户分组下
//                    helpService.copySomePageAndTabToUserGroup(pageIds,subGroup);
//                } catch (Exception e) {
//                    this.delete(subGroup.getId());
//                    throw new TRSException(CodeUtils.FAIL,"同步栏目数据出错！");
//                }
//            }
//        }
        try {
            helpService.copySomePageAndTabToUserGroupNew(columnSync,columnSyncLevel,subGroup);
        } catch (Exception e) {
            this.delete(subGroup.getId());
            throw new TRSException(CodeUtils.FAIL,"同步日常监测数据出错！");
        }
        //同步专题分析
        try {
            helpService.copySomeSpecialToUserGroup(specialSync,specialSyncLevel,subGroup);
        } catch (Exception e) {
            this.delete(subGroup.getId());
            throw new TRSException(CodeUtils.FAIL,"同步专题数据出错！");
        }
        return subGroupRepository.save(subGroup).getId();
    }


    @Override
    public SubGroup save(SubGroup subGroup){
        return subGroupRepository.save(subGroup);
    }

    @Override
    public void update(String id,String name, MultipartFile picture,String pictureName,String[] roleIds,int columnNum,int specialNum,int alertNum,int alertAccount,String expireAt,int userLimit,String userJson) throws TRSException {
        SubGroup subGroup = subGroupRepository.findOne(id);
        if (ObjectUtil.isEmpty(subGroup)){
            throw new TRSException(CodeUtils.FAIL,"无法查到当前id对应的用户分组！");
        }

        //上传logo
        String fileName = null;
        if (picture != null ){
            //上传了新的logo图片
            try {
                fileName = PictureUtil.transferLogo(picture, name, "group");

                // 只要有上传新的logo 原有logo必会删除
                // 删除原有图片
                PictureUtil.deletePic( subGroup.getLogoPicName(),"group");
            } catch (IOException e) {
                log.error("修改分组时上传logo图片出错！",e);
                throw new TRSException(CodeUtils.FAIL,"修改分组时上传logo图片出错！");
            }
        }
        // 认为删除 原来的logo图片（删除原有logo，同时没有上传新的logo图片）
        if ("".equals(pictureName) && fileName == null) {
            // 删除机构下的logo图片
            PictureUtil.deletePic(subGroup.getLogoPicName(),"group");
        }
        if (fileName == null && pictureName != "无logo" && !"".equals(pictureName)) {
            // 说明未上传新的logo 则采用原来的logo名
            // 此时 若pictureName也是null 说明该机构需要换回原有默认网察logo
            fileName = pictureName;
        }

       // SubGroup subGroup = new SubGroup();
        subGroup.setName(name);
        subGroup.setAlertAccountNum(alertAccount);
        subGroup.setAlertNum(alertNum);
        subGroup.setColumnNum(columnNum);
        subGroup.setSpecialNum(specialNum);
        subGroup.setExpireAt(expireAt);
        subGroup.setUserLimit(userLimit);
        //用户分组编辑页面没有 状态更改按钮
        //subGroup.setStatus(Status.normal);
        subGroup.setLogoPicName(fileName);
        //权限
        if (roleIds == null || roleIds.length <= 0){
            subGroup.setRoles(null);
        }else {
            List<Role> roles = roleService.findByIds(roleIds);
            subGroup.setRoles(new HashSet<>(roles));
        }
        //修改当前分组 并获取当前分组的id
        String subGroupId = this.update(subGroup);

        if (StringUtil.isNotEmpty(userJson)){
            List<User> users = JSONArray.parseArray(userJson, User.class);
            if (ObjectUtil.isNotEmpty(users)){
//                if (users.size() > userLimit){
//                    throw new TRSException(CodeUtils.FAIL,"当前创建的用户数 大于 限制的登录账号个数！");
//                }
                //添加用户
                for (int j = 0; j <users.size() ; j++) {

                    User user = users.get(j);
                    User updateUser = new User();

                    // 判断账号是否为空
                    if (StringUtils.isBlank(user.getUserName())) {
                        throw new TRSException(CodeUtils.ACCOUNT_NULL, "账号不能为空！");
                    }
                    // 判断账号是否有空格
                    if (RegexUtils.checkBlankSpace(user.getUserName())) {
                        throw new TRSException(CodeUtils.ACCOUNT_SPACE, "账号中不能含有空格！");
                    }

                    // 判断邮件是否为空
                    if (StringUtils.isNotBlank(user.getEmail())) {
                        // 判断邮件是否正确
                        if (!RegexUtils.checkEmail(user.getEmail())) {
                            throw new TRSException(CodeUtils.EMAIL_FALSE, "邮箱格式不正确！");
                        }
                    }
                    // 验证手机号
                    if (StringUtils.isNotBlank(user.getPhone()) && !RegexUtils.checkMobile(user.getPhone())) {
                        throw new TRSException(CodeUtils.PHONE_FAIL, "手机号填写错误！");
                    }

                    User loginUser = UserUtils.getUser();
                    // 判断用户是否有权限新建
                    if (!UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())) {
                        throw new TRSException(CodeUtils.FORBIDDEN_FAIL, "该账号没有权限新建或修改用户！");
                    }
                    // 判断用户名是否为空
                    if (StringUtils.isBlank(user.getDisplayName())) {
                        throw new TRSException(CodeUtils.DISPLAYNAME_NULL, "用户名不能为空！");
                    }

                    if (StringUtil.isEmpty(user.getId())){
                        //新建用户时
                        // 判断用户账号是否存在
                        if (userService.findByUserName(user.getUserName()) != null) {
                            if (users.size() == 1){
                                throw new TRSException(CodeUtils.USERNAME_EXISTED,"用户账号已存在");
                            }else if (users.size() > 1){
                                throw new TRSException(CodeUtils.USERNAME_EXISTED,"第"+(j+1)+"用户账号已存在");
                            }
                           // throw new TRSException(CodeUtils.USERNAME_EXISTED, "用户账号已存在！");
                        }
                        // 判断用户账号是否存在
                        if (userService.findByUserName(user.getUserName()) != null) {
                            throw new TRSException(CodeUtils.USERNAME_EXISTED, "用户账号已存在！");
                        }
                        // 判断密码是否为空
                        if (StringUtils.isBlank(user.getPassword())) {
                            throw new TRSException(CodeUtils.PASSWORD_NULL, "密码不能为空！");
                        }
                        // 判断密码强度
                        if (!RegexUtils.isLowSafely(user.getPassword())) {
                            throw new TRSException(CodeUtils.PASSWORD_LOW, "密码需要在8-16位，并且有大写、小写、数字和特殊字符至少三种！");
                        }
                        // 判断两次密码是否一致
                        if (!user.getPassword().equals(user.getPasswordAgain())) {
                            throw new TRSException(CodeUtils.PASSWORD_NOT_SAME, "两次密码不一样！");
                        }

                        //普通用户
                        user.setCheckRole(CheckRole.ROLE_ORDINARY);
                        user.setSubGroupId(subGroupId);
                        // 盐
                        String salt = UUID.randomUUID().toString();
                        // 加密的盐
                        String encryptPsw = UserUtils.getEncryptPsw(user.getPassword(), salt);
                        user.setPassword(encryptPsw);
                        user.setSalt(salt);
                        user.setLastLoginTime(null);
                        user.setLastLoginIp(null);
                        user.setStatus(Status.normal);
                        user.setOrganizationId(subGroup.getOrganizationId());
                        user.setExpireAt(subGroup.getExpireAt());
                    }else {
                        updateUser =  userService.findById(user.getId());
                        if (userService.findByUserName(user.getUserName()) != null && !updateUser.getUserName().equals(user.getUserName())) {
                            if (users.size() == 1){
                                throw new TRSException(CodeUtils.USERNAME_EXISTED,"用户账号已存在");
                            }else if (users.size() > 1){
                                throw new TRSException(CodeUtils.USERNAME_EXISTED,"第"+(j+1)+"用户账号已存在");
                            }
                          //  throw new TRSException(CodeUtils.USERNAME_EXISTED, "用户账号已存在！");
                        }
                        updateUser.setUserName(user.getUserName());
                        updateUser.setDisplayName(user.getDisplayName());
                        updateUser.setEmail(user.getEmail());
                        updateUser.setPhone(user.getPhone());
                        updateUser.setExpireAt(subGroup.getExpireAt());

                    }

                    //判断角色
                    List<String> subGroupRoles = Arrays.asList(roleIds);
                    String[] userRoleIds = user.getRoleIds();
                    if (ObjectUtil.isNotEmpty(userRoleIds)){
                        for (int i = 0; i < userRoleIds.length; i++) {
                            if ( ! subGroupRoles.contains(userRoleIds[i])){
                                if (users.size() == 1){
                                    throw new TRSException(CodeUtils.FAIL,"当前用户添加了无效权限");
                                }else if (users.size() > 1){
                                    throw new TRSException(CodeUtils.FAIL,"第"+(j+1)+"用户添加了无效权限");
                                }

                            }
                        }

                        List<Role> roles = roleService.findByIds(userRoleIds);
                        if (StringUtil.isNotEmpty(user.getId())){
                            updateUser.setRoles(new HashSet<>(roles));
                        }else {
                            user.setRoles(new HashSet<>(roles));
                        }
                    }else {
                        if (StringUtil.isNotEmpty(user.getId())){
                            updateUser.setRoles(null);
                        }else {
                            user.setRoleIds(null);
                        }
                    }

                    if (StringUtil.isEmpty(user.getId())){
                        //新建
                        userService.add(user,false);
                    }else {
                        //编辑
                        userService.update(updateUser,false);
                    }
                }
            }
        }

    }

    @Override
    public String update(SubGroup subGroup) {
       return subGroupRepository.saveAndFlush(subGroup).getId();
    }

    @Override
    public void updateStatus(SubGroup subGroup,String status){

        subGroup.setStatus(Status.getStatusByValue(status));

      //  if ("1".equals(status)){
            //关闭当前用户分组 需要将该分组下用户状态也关掉
            List<User> users = userService.findBySubGroupId(subGroup.getId());
            if (ObjectUtil.isNotEmpty(users)){
                for (User user : users) {
                    user.setStatus(Status.getStatusByValue(status));
                    userService.update(user,false);
                }
            }
    //    }
       subGroupRepository.saveAndFlush(subGroup);
    }

    @Override
    public void delete(String id) {
        SubGroup subGroup = subGroupRepository.findOne(id);
        // 删除分组下的logo图片
        PictureUtil.deletePic(subGroup.getLogoPicName(),"group");

        subGroupRepository.delete(id);

        //删除用户分组下的用户
        List<User> users = userService.findBySubGroupId(id);
        if (ObjectUtil.isNotEmpty(users)){
            userService.deleteByUser(users);
        }
    }

    @Override
    public SubGroup findOne(String id) {
        return subGroupRepository.findOne(id);
    }

    @Override
    public SubGroup detail(String id) {
        SubGroup subGroup = subGroupRepository.findOne(id);
        if (ObjectUtil.isNotEmpty(subGroup)){
            subGroup.setUsers(userService.findBySubGroupId(id));
        }
        return subGroup;
    }

//
//    @Override
//    public int getSubGroupColumnCount(String subGroupId) {
//        List<User> users = userService.findBySubGroupId(subGroupId);
//        int tabsCount = 0;
//        if (ObjectUtil.isNotEmpty(users)){
//            for (User user : users) {
//                List<IndexTab> tabs = indexTabRepository.findByUserId(user.getId());
//                tabsCount += tabs.size();
//            }
//        }
//
//        return tabsCount;
//    }
//
//    @Override
//    public int getSubGroupSpecialCount(String subGroupId) {
//        List<User> users = userService.findBySubGroupId(subGroupId);
//        int projectsCount = 0;
//        if (ObjectUtil.isNotEmpty(users)){
//            for (User user : users) {
//                List<SpecialProject> projects = specialProjectService.findByUserId(user.getId(), new Sort(Sort.Direction.DESC, "createdTime"));
//                projectsCount += projects.size();
//            }
//        }
//        return projectsCount;
//    }

//    @Override
//    public int getSubGroupAlertCount(String subGroupId) {
//        List<User> users = userService.findBySubGroupId(subGroupId);
//        int rulesCount = 0;
//        if (ObjectUtil.isNotEmpty(users)){
//            for (User user : users) {
//                List<AlertRule> rules = alertRuleService.findByUserId(user.getId());
//                rulesCount += rules.size();
//            }
//        }
//        return alertRuleService.getSubGroupAlertCount(subGroupId);
//    }

    @Override
    public int getSubGroupAlertAccountCount(String subGroupId) {
        List<User> users = userService.findBySubGroupId(subGroupId);
        int accountsCount = 0;
        if (ObjectUtil.isNotEmpty(users)){
            for (User user : users) {
                List<AlertAccount> accounts = helpService.findByUserId(user.getId());
                accountsCount += accounts.size();
            }
        }
        return accountsCount;
    }

    @Override
    public void setUpGroup(String id, int columnNum, int specialNum, int alertNum, int alertAccountNum, int userLimit) throws TRSException {
        //说明：各个数值为-1时，代表该值未做任何修改

       //通过用户分组id 查询 用户分组
        SubGroup subGroupOne = subGroupRepository.findOne(id);
        if (ObjectUtil.isEmpty(subGroupOne)){
            throw new TRSException(CodeUtils.FAIL,"找不到该用户分组！");
        }
        //查询该用户分组所属的机构
        Organization organization = organizationService.findById(subGroupOne.getOrganizationId());
        if (ObjectUtil.isEmpty(organization)){
            throw new TRSException(CodeUtils.FAIL,"找不到该用户分组所属的机构！");
        }
        //查询机构下 的 分组
        List<SubGroup> subGroups = this.findByOrgId(organization.getId());
        int currentColumnNum = organization.getColumnNum();
        int currentSpecialNum = organization.getSpecialNum();
        int currentAlertNum = organization.getAlertNum();
        int currentAlertAccountNum = organization.getAlertAccountNum();
        int currentUserLimit = organization.getUserLimit();
        if (ObjectUtil.isNotEmpty(subGroups)){
            for (SubGroup subGroup : subGroups) {
                if (!subGroup.getId().equals(id)){
                    currentColumnNum -= subGroup.getColumnNum();
                    currentSpecialNum -= subGroup.getSpecialNum();
                    currentAlertNum -= subGroup.getAlertNum();
                    currentAlertAccountNum -= subGroup.getAlertAccountNum();
                    currentUserLimit -= subGroup.getUserLimit();
                }
            }
        }

        // 登录账号个数
        if (-1 != userLimit){
            if (userLimit < 1) {
                throw new TRSException(CodeUtils.FAIL, "登录账号个数不能小于 1 ！");
            }
            if (userLimit > currentUserLimit) {
                throw new TRSException(CodeUtils.USER_LIMIT, "登录账号个数已超出当前可添加总数！");
            }
            //查询当前分组下的用户数量
            List<User> users = userService.findBySubGroupId(id);
            if (ObjectUtil.isNotEmpty(users) && userLimit < users.size()){
                throw new TRSException(CodeUtils.USER_DELETE,"当前设置登录账号个数 小于 该分组所拥有的账号个数！");
            }

            subGroupOne.setUserLimit(userLimit);

        }

        if (-1 != columnNum){
            // 日常监测栏目数
            if (columnNum < 1) {
                throw new TRSException(CodeUtils.FAIL, "日常监测栏目数不能小于 1 ！");
            }
            if (columnNum > currentColumnNum){
                throw new TRSException(CodeUtils.FAIL,"日常监测数已超出当前可添加总数！");
            }
            if (columnNum < helpService.getSubGroupColumnCountForSubGroup(id)){
                throw new TRSException(CodeUtils.FAIL,"当前设置日常监测栏目数 小于 该分组所拥有的栏目个数！");
            }

            subGroupOne.setColumnNum(columnNum);

        }

        if (-1 != specialNum){
            // 专题个数
            if (specialNum < 1) {
                throw new TRSException(CodeUtils.FAIL, "专题事件数不能小于 1 ！");
            }
            if (specialNum > currentSpecialNum){
                throw new TRSException(CodeUtils.FAIL,"专题事件数已超出当前可添加总数！");
            }
            List<SpecialProject> specialProjects = specialProjectRepository.findBySubGroupId(id);
            int speciaCount = (specialProjects == null)?0:specialProjects.size();
            if (specialNum < speciaCount){
                throw new TRSException(CodeUtils.FAIL,"当前设置专题事件数 小于 该分组所拥有的专题事件个数！");
            }

            subGroupOne.setSpecialNum(specialNum);

        }

        if (-1 != alertNum){
            // 预警
            if (alertNum < 1) {
                throw new TRSException(CodeUtils.FAIL, "预警主题个数不能小于 1 ！");
            }
            if (alertNum > currentAlertNum) {
                throw new TRSException(CodeUtils.FAIL, "预警主题数已超出当前可添加总数 ！");
            }
            List<AlertRule> alertRules = alertRuleRepository.findBySubGroupId(id, new Sort(Sort.Direction.DESC, "createdTime"));
            int alertRuleCount = (alertRules == null)?0:alertRules.size();
            if (alertNum < alertRuleCount){
                throw new TRSException(CodeUtils.FAIL,"当前设置预警主题数 小于 该分组所拥有的预警主题个数！");
            }

            subGroupOne.setAlertNum(alertNum);

        }

        if (-1 != alertAccountNum){
            // 预警账号个数
            if (alertAccountNum < 1) {
                throw new TRSException(CodeUtils.FAIL, "可绑定预警账号数不能小于 1 ！");
            }
            if (alertAccountNum > currentAlertAccountNum) {
                throw new TRSException(CodeUtils.FAIL, "可绑定预警账号数已超出当前可绑定数 ！");
            }
            if (alertAccountNum < this.getSubGroupAlertAccountCount(id)){
                throw new TRSException(CodeUtils.FAIL,"当前设置可绑定预警账号数 小于 该分组所拥有的可绑定预警账号个数！");
            }

            subGroupOne.setAlertAccountNum(alertAccountNum);

        }
        subGroupRepository.saveAndFlush(subGroupOne);

    }

    @Override
    public boolean isSubGroupExistUser(String subGroupId, String userId) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(subGroupId)) {
            return false;
        }
        User user = userService.findById(userId);
        if (user == null) {
            return false;
        }
        return StringUtils.equals(subGroupId, user.getOrganizationId());
    }

    @Override
    public List<SubGroup> findAll() {
        return subGroupRepository.findAll();
    }

//    @Override
//    public Object selectDataSync() {
//        //查询所有日常监测（包括自定义）
//        String userId = UserUtils.getUser().getId();
//        List<NavigationConfig> navigationConfigs = navigationService.findByUserIdAndSort(userId, "createdTime");
//        if (ObjectUtil.isNotEmpty(navigationConfigs)){
//            for (NavigationConfig navigationConfig : navigationConfigs) {
//                //自定义 || 日常监测
//                if (NavigationEnum.definedself.equals(navigationConfig.getType()) || NavigationEnum.column.equals(navigationConfig.getType())){
//                    //查询该导航下的栏目分组
//                }
//            }
//        }
//        //查询专题
//        return null;
//    }

    private List<DataSyncSpecial> removeSomeDataSyncSpecials(List<DataSyncSpecial> dataSyncSpecials){
        List<DataSyncSpecial> listRetain = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (DataSyncSpecial dataSyncSpecial : dataSyncSpecials) {
            if (!ids.contains(dataSyncSpecial.getId())) {
                ids.add(dataSyncSpecial.getId());
                listRetain.add(dataSyncSpecial);
            }
        }
        //将重复的子集迁移到一处
        if (ObjectUtil.isNotEmpty(listRetain)){
            for (DataSyncSpecial dataSyncSpecial : listRetain) {
                for (DataSyncSpecial syncSpecial : dataSyncSpecials) {
                    List<DataSyncSpecial> detail = syncSpecial.getZhuantiDetail();
                    if (syncSpecial.getId().equals(dataSyncSpecial.getId()) && ObjectUtil.isNotEmpty(detail)){
                        List<DataSyncSpecial> zhuantiDetail = dataSyncSpecial.getZhuantiDetail();
                        //二级重复情况下  下面的三级专题只会有一个 所以detail不用做循环处理
                        if (ObjectUtil.isNotEmpty(zhuantiDetail)){
                            List<String> thirdIds = new ArrayList<>();
                            for (DataSyncSpecial special : zhuantiDetail) {
                                thirdIds.add(special.getId());
                            }
                            if (!thirdIds.contains(detail.get(0).getId())){
                                zhuantiDetail.addAll(syncSpecial.getZhuantiDetail());
                                dataSyncSpecial.setZhuantiDetail(zhuantiDetail);
                            }
                        }else {
                            dataSyncSpecial.setZhuantiDetail(detail);
                        }

                    }
                }
            }
        }
        return listRetain;
    }
}
