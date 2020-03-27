package com.trs.netInsight.widget.user.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.trs.netInsight.config.constant.ChartConst;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.login.service.ILoginService;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.Role;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.util.PublicSuffixList;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author lilyy
 * @date 2019/9/26 14:11
 * 通过该接口自动添加机构,生成用户名密码, 并且用户可以自动登录
 */
@Slf4j
@Controller
//@RestController
@Api(description = "机构自动生成免登录接口")
public class OrganAutoRegisterController {

    @Autowired
    private IOrganizationService organizationService;
    @Autowired
    private ILoginService loginService;
    @Autowired
    private  IUserService userService;
    @Autowired
    private ISubGroupService subGroupService;
    @Autowired
    private IRoleService roleService;

    @Value("${spring.session.timeout}")
    private int sessionTimeout;
    @Value("${autoRegisterRedirect}")
    private String autoRegisterRedirect;


    public static final String autopwd = "1q2w3e4R";
    public static final String expireAt = "2050-01-01 00:00:00";
    public static final int isAutoAdd = 1;

    /**
     * 天目云 使用接口
     * 自动添加登录接口--也是走Nginx
     * http://localhost:28088/netInsight/autoLogin?userName=tt001&msec=2019&tenantId=123456&isAdmin=1&encrypting=e531b4f05e3d3bfdd79ae9eec81bb4ae
     * 等会成功后,用户请求该地址即可直接登录进去
     * http://localhost:28088/#/CustomizeMenus
     * http://www.netinsight.com.cn/#/CustomizeMenus
     * @param userName  一点登录用户名
     * @param msec  时间戳
     * @param encrypting  验证蜜月
     * @param tenantId  /机构名
     * @param isAdmin  是否管理员--具有新建/编辑权限
     * @return
     */
    @ApiOperation("根据用户名自动添加机构")
//    @GetMapping("/autoLogin")
    @RequestMapping(value = "autoLogin", method = { RequestMethod.POST,RequestMethod.GET })
    public String autoLogin(@ApiParam("管理员账号") @RequestParam(value = "username") String userName,
                            @ApiParam("时间戳") @RequestParam("msec") String msec,
                            @ApiParam("加密后的密文") @RequestParam("encrypting") String encrypting,
                            @ApiParam("租户ID") @RequestParam("tenantId") String tenantId,
                            @ApiParam("网察机构名") @RequestParam("tenantName") String tenantName,
                            @ApiParam("是否租户管理员") @RequestParam("isAdmin") int isAdmin,
                            HttpServletRequest request) throws TRSException{

        log.info("根据用户名自动添加机构,并自动登录");
        String token = PasswordUtil.getToken(userName,msec);
        log.info("token--------->"+token);
        log.info("encrypting---->"+encrypting);
//        if(!encrypting.equals(token)){
//            log.error("自动添加,验证失败");
//            return "密文验证失败!";
//        }
        // 判断账号是否为空
        if (StringUtils.isBlank(userName)) {
            log.info("账号不能为空!");
            return "账号不能为空！";
        }
        String dispname = userName;
        //添加标识,防止和原有的机构,账号冲突
        userName = ChartConst.AUTOLOGINUSERNAME+userName;
        tenantName = ChartConst.AUTOLOGIN+tenantName;
        // 判断机构名是否存在--存在直接登录
        // 可以在结构和用户前加标识,防止冲突
        List<Organization> orgg = organizationService.findByOrganizationName(tenantName);
        Organization organization = null;
        for(Organization oo : orgg){
            if(oo.getAutoAdd()!=null && oo.getAutoAdd().equals("1")) organization = oo;
        }
        if (organization == null) {
            log.info(tenantName+"机构不存在,自动添加");
            Organization.OrganizationType organizationType = Organization.OrganizationType.formal;
            String[] rolePlatformIds = {"4028b8736ca8942e016ca89fa98b001b"};
            String[] dataSource = {"新闻","微博","微信","客户端","论坛","博客","电子报","国外新闻"};
            organizationService.add(organizationType, tenantName,null, userName,autopwd,
                    dispname, null, null, "2050-01-01 00:00:00", "其他",
                    "天目云接口", rolePlatformIds,
                    null, 5, 50, 10,10,
                    5,500, dataSource,
                    90,365,90,null,"网察",
                    "网察大数据分析平台","010-64859900",null,
                    null,true,true,isAdmin,isAutoAdd,tenantId,null,null,null,null);
            //修改状态
            List<Organization> organizations = organizationService.findByOrganizationName(tenantName);
            Organization organ1 = null;
            for(Organization oo : organizations){
                if(oo.getAutoAdd()!=null && oo.getAutoAdd().equals("1")){
                    organ1 = oo;
                    oo.setStatus(Status.getStatusByValue("0"));
                    organizationService.update(oo);
                    break;
                }
            }
            ///////////////////////////////
            /////////////1.如果用户是管理员上面直接添加了,不是管理员则要添加分组以及用户//////////////////
            if(isAdmin==0){
                addSubGroup(organ1.getId(),tenantName,userName,dispname,0,tenantId);
            }
        }else{
            //////判断用户是否存在,如果存在则返回  admin创建的机构只能添加一个管理员
            ////////用户不存在,是否是管理员-->是管理员查看上层机构中是否存在用户,不存在添加到上层机构中
            ////////1.上层用户存在,添加分组以及用户
            User userIsexit = userService.findByUserName(userName);
            if(userIsexit==null){//用户不存在
                if(isAdmin==0){ //是普通用户,分组添加用户和权限---没有新建和编辑权限
                    addSubGroup(organization.getId(),tenantName,userName,dispname,0,tenantId);
                }else{
                    //用户是管理员,判断顶层机构是否有管理员,没有添加到顶层机构管理员,存在则添加子机构管理员
                    List<User> users = userService.findByOrganizationId(organization.getId());
                    if(users.size()==0){
                        User user = addUser(userName,dispname,organization.getId());
                        String add = userService.add(user, false);
                        organization = organizationService.findById(organization.getId());
                        if (organization != null) {
                            organization.setAdminUserId(add);
                            organizationService.update(organization);
                        }
                    }else{
                        addSubGroup(organization.getId(),tenantName,userName,dispname,1,tenantId);
                    }
                }
            }else{
                log.info(userName+"该用户已经存在,可以直接登录!"); //权限修改后
                if(isAdmin==1 && userIsexit.getCheckRole().equals(CheckRole.ROLE_ORDINARY.toString())
                    && userIsexit.getRoles().size()<=0){
                    log.info(userName+"管理员与否修改,更新权限,自动登录接口!"); //权限修改后
                    List<Role> roles = roleService.findByDescriptions("日常监测、专题分析、预警中心");
                    userIsexit.setRoles(new HashSet<>(roles));
                    userService.update(userIsexit,false);
                }
            }
        }
        //免登录暂时注释
        //免登录暂时注释
        boolean flag = login(userName,request);
        if(!flag){
            return "登录失败,请稍后再试!";
        }
        //重定向的地址--第一次可能来源选择 需要刷新出来,第二种可以解决,但是有个登录过程
        String radd = autoRegisterRedirect.replace("{userName}",userName).replace("{password}",autopwd);
        log.info("--->一键登录登录成功!,跳转到-->"+radd);
        return "redirect:"+radd;
    }
//
    //
    public boolean login(String userName,HttpServletRequest request){
        ////获取SecurityManager工厂
        Factory<SecurityManager> factory = new IniSecurityManagerFactory();
        //得到SecurityManager实例并绑定给SecurityUtils
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
        //得到Subject及创建用户名/密码身份验证Token（即用户身份/凭证）
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(userName, autopwd, false);

        //登录，即身份验证
        try {
            Session session = subject.getSession();
            subject.login(token);

            // 验证是否登录成功
            if (subject.isAuthenticated()) {
                User user = userService.findByUserName(userName);
                user = UserUtils.checkOrganization(user);
                // 将用户信息存入redis
                RedisUtil.setString(UserUtils.USERNAME_LOGIN_USER + user.getUserName(), session.getId().toString(),
                        sessionTimeout, TimeUnit.SECONDS);

                // 登录成功，更改ip和时间
                user.setLastLoginIp(NetworkUtil.getIpAddress(request));
                user.setLastLoginTime(DateUtil.formatCurrentTime(DateUtil.yyyyMMdd));
                try {
                    userService.update(user, false);
                } catch (Exception e) {
                    log.error("修改失败!", e);
                    subject.logout();
                    RedisUtil.deleteKey(UserUtils.SESSION_LOGIN_USER + session.getId());
                    RedisUtil.deleteKey(UserUtils.USERNAME_LOGIN_USER + user.getUserName());
                }
                // 将登录次数存入redis（按用户）
                UserUtils.setLoginCount(userName+user.getId());
                // 将登录次数存入redis（按分组）
                String subGroupId = user.getSubGroupId();
                UserUtils.setLoginCount(subGroupId);
            }else{
                token.clear();
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
            //登录失败
            System.out.println("用户信息验证失败");
            token.clear();
            return false;  //这里是登录失败的页面
        }
        return true;
    }
    //分组添加用户和权限  --新建编辑权限
    public void addSubGroup(String organizationId,String name,String username,String displayName,int isAdmin,String tenantId) throws TRSException{
        List<Role> roles = roleService.findByDescriptions("日常监测、专题分析、预警中心");

        JsonArray jsonArrayRole2 = new JsonArray();
        String roleIds[] = new String[roles.size()];
        for(int i=0;i<roles.size();i++){
            roleIds[i] = roles.get(i).getId();

            JsonObject rolejson1 = new JsonObject();
            rolejson1.addProperty("id",roles.get(i).getId());
            rolejson1.addProperty("roleName",roles.get(i).getRoleName());
            jsonArrayRole2.add(rolejson1);
        }
        //添加用户json字符串
        JsonArray jrAll = new JsonArray();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userName",username);
        jsonObject.addProperty("password",autopwd);
        jsonObject.addProperty("passwordAgain",autopwd);
        jsonObject.addProperty("displayName",displayName);
        jsonObject.addProperty("email","");//
        jsonObject.addProperty("phone","");//
        jsonObject.addProperty("tenantId",tenantId);//

        jsonObject.add("rolesAll",jsonArrayRole2);//
        //管理员有新建编辑权限,非管理员没有
        if(isAdmin==1){
            JsonArray jsonArrayRole = new JsonArray();
            for(String r: roleIds) jsonArrayRole.add(r);
            jsonObject.add("roleIds",jsonArrayRole);//
        }else{
            jsonObject.add("roleIds",new JsonArray());//
        }

        jrAll.add(jsonObject);
        String userjson = jrAll.toString();
        log.info("自动添加json--->"+userjson);
        subGroupService.save(isAutoAdd,organizationId,name,null,roleIds,50,10,10,5,
                expireAt,null,null,null,1,userjson);
    }
    //添加管理员格式化参数
    public User addUser(String userName,String displayName,String organizationId){
        User user = new User();
        user.setUserName(userName);
        user.setDisplayName(displayName);
        // 加密
        String salt = UUID.randomUUID().toString();// 加密的salt
        // 加密后的密码
        String encryptPsw = UserUtils.getEncryptPsw(autopwd, salt);
        user.setSalt(salt);
        user.setPassword(encryptPsw);

        user.setEmail("");
        user.setPhone("");
        user.setExpireAt(expireAt);
        user.setStatus(Status.normal);

        //机构管理员
        user.setCheckRole(CheckRole.ROLE_ADMIN);
        user.setOrganizationId(organizationId);

        //机构管理员 有所有权限
        List<Role> roles =  roleService.findByRoleTypeAndDes(CheckRole.ROLE_ADMIN,"日常监测、专题分析、预警中心");
        if (ObjectUtil.isNotEmpty(roles)){
            user.setRoles(new HashSet<>(roles));
        }else{
            user.setRoles(null);
        }

        return user;
    }



    //////////////

//
//    @ApiOperation("根据用户名自动添加机构")
//    @FormatResult
//    @GetMapping("/autoLogin")
//    public Object autoLogin(@ApiParam("管理员账号") @RequestParam(value = "userName") String userName,
//                            @ApiParam("时间戳") @RequestParam("msec") String msec,
//                            @ApiParam("加密后的密文") @RequestParam("encrypting") String encrypting,
//                            @ApiParam("租户ID") @RequestParam("tenantId") String tenantId,
//                            @ApiParam("是否租户管理员") @RequestParam("isAdmin") int isAdmin,
//                            HttpServletRequest request) throws TRSException{
//
//        log.info("根据用户名自动添加机构,并自动登录");
//        String token = PasswordUtil.getToken(userName,msec);
//        if(!encrypting.equals(token)){
//            log.error("自动添加,验证失败");
//            return "验证失败!";
//        }
//        // 判断账号是否为空
//        if (StringUtils.isBlank(userName)) {
//            log.info("账号不能为空!");
//            return "账号不能为空！";
//        }
//        String dispname = userName;
//        //添加标识,防止和原有的机构,账号冲突
//        userName = ChartConst.AUTOLOGIN+userName;
//        // 判断机构名是否存在--存在直接登录
//        // 可以在结构和用户前加标识,防止冲突
//        List<Organization> orgg = organizationService.findByOrganizationName(tenantId);
//        Organization organization = null;
//        for(Organization oo : orgg){
//            if(oo.getAutoAdd()!=null && oo.getAutoAdd().equals("1")) organization = oo;
//        }
//        if (organization == null) {
//            log.info(tenantId+"机构不存在,自动添加");
//            Organization.OrganizationType organizationType = Organization.OrganizationType.formal;
//            String[] rolePlatformIds = {"4028b8736ca8942e016ca89fa98b001b"};
//            String[] dataSource = {"新闻","微博","微信","客户端","论坛","博客","电子报","国外新闻"};
//            organizationService.add(organizationType, tenantId,null, userName,autopwd,
//                    dispname, null, null, "2050-01-01 00:00:00", "其他",
//                    "天目云接口", rolePlatformIds,
//                    null, 5, 50, 10,10,
//                    5,500, dataSource,
//                    90,365,90,null,"网察",
//                    "网察大数据分析平台","010-64859900",null,
//                    null,true,true,isAdmin,isAutoAdd);
//            //修改状态
//            List<Organization> organizations = organizationService.findByOrganizationName(tenantId);
//            Organization organ1 = null;
//            for(Organization oo : organizations){
//                if(oo.getAutoAdd()!=null && oo.getAutoAdd().equals("1")){
//                    organ1 = oo;
//                    oo.setStatus(Status.getStatusByValue("0"));
//                    organizationService.update(oo);
//                    break;
//                }
//            }
//            ///////////////////////////////
//            /////////////1.如果用户是管理员上面直接添加了,不是管理员则要添加分组以及用户//////////////////
//            if(isAdmin==0){
//                addSubGroup(organ1.getId(),tenantId,userName,dispname,0);
//            }
//        }else{
//            ////////判断用户是否存在,如果存在则返回  admin创建的机构只能添加一个管理员
//            ////////用户不存在,是否是管理员-->是管理员查看上层机构中是否存在用户,不存在添加到上层机构中
//            ////////1.上层用户存在,添加分组以及用户
//            User userIsexit = userService.findByUserName(userName);
//            if(userIsexit==null){//用户不存在
//                if(isAdmin==0){ //是普通用户,分组添加用户和权限---没有新建和编辑权限
//                    addSubGroup(organization.getId(),tenantId,userName,dispname,0);
//                }else{
//                    //用户是管理员,判断顶层机构是否有管理员,没有添加到顶层机构管理员,存在则添加子机构管理员
//                    List<User> users = userService.findByOrganizationId(organization.getId());
//                    if(users.size()==0){
//                        User user = addUser(userName,dispname,organization.getId());
//                        String add = userService.add(user, false);
//                        organization = organizationService.findById(organization.getId());
//                        if (organization != null) {
//                            organization.setAdminUserId(add);
//                            organizationService.update(organization);
//                        }
//                    }else{
//                        addSubGroup(organization.getId(),tenantId,userName,dispname,isAdmin);
//                    }
//                }
//            }else{
//                log.info(userName+"该用户已经存在,可以直接登录!");
//            }
//        }
////        String address = "http://119.254.92.55:8019/#/login?user="+userName+"&pass=1q2w3e4R";
////        map.put("address",address);
////        return map;
//
//        //免登录暂时注释
//        //免登录暂时注释
//        boolean flag = login(userName,request);
//        if(!flag){
//            return "登录失败";
//        }
//        log.info("---------->一键登录登录成功!");
//        return "可以登录了!";
//    }



//    @ApiOperation("根据用户名自动添加机构")
//    @FormatResult
//    @GetMapping("/autoLogin")
//    public Object autoLogin(@ApiParam("管理员账号") @RequestParam(value = "userName") String userName,
//                            @ApiParam("时间戳") @RequestParam("msec") String msec,
//                            @ApiParam("加密后的密文") @RequestParam("encrypting") String encrypting,
//                            @ApiParam("租户ID") @RequestParam("tenantId") String tenantId,
//                            @ApiParam("是否租户管理员") @RequestParam("isAdmin") int isAdmin,
//                            HttpServletRequest request) throws TRSException{
//
//        log.info("根据用户名自动添加机构,并自动登录");
//        String token = PasswordUtil.getToken(userName,msec);
//        if(!encrypting.equals(token)){
//            log.error("自动添加,验证失败");
//            return "验证失败!";
//        }
//        // 判断账号是否为空
//        if (StringUtils.isBlank(userName)) {
//            log.info("账号不能为空!");
//            return "账号不能为空！";
//        }
//        String dispname = userName;
//        //添加标识,防止和原有的机构,账号冲突
//        userName = ChartConst.AUTOLOGIN+userName;
//        // 判断机构名是否存在--存在直接登录
//        // 可以在结构和用户前加标识,防止冲突
//        List<Organization> orgg = organizationService.findByOrganizationName(tenantId);
//        Organization organization = null;
//        for(Organization oo : orgg){
//            if(oo.getAutoAdd()!=null && oo.getAutoAdd().equals("1")) organization = oo;
//        }
//        if (organization == null) {
//            log.info(tenantId+"机构不存在,自动添加");
//            Organization.OrganizationType organizationType = Organization.OrganizationType.formal;
//            String[] rolePlatformIds = {"4028b8736ca8942e016ca89fa98b001b"};
//            String[] dataSource = {"新闻","微博","微信","客户端","论坛","博客","电子报","国外新闻"};
//            organizationService.add(organizationType, tenantId,null, userName,autopwd,
//                    dispname, null, null, "2050-01-01 00:00:00", "其他",
//                    "天目云接口", rolePlatformIds,
//                    null, 5, 50, 10,10,
//                    5,500, dataSource,
//                    90,365,90,null,"网察",
//                    "网察大数据分析平台","010-64859900",null,
//                    null,true,true,isAdmin,isAutoAdd);
//            //修改状态
//            List<Organization> organizations = organizationService.findByOrganizationName(tenantId);
//            Organization organ1 = null;
//            for(Organization oo : organizations){
//                if(oo.getAutoAdd()!=null && oo.getAutoAdd().equals("1")){
//                    organ1 = oo;
//                    oo.setStatus(Status.getStatusByValue("0"));
//                    organizationService.update(oo);
//                    break;
//                }
//            }
//            ///////////////////////////////
//            /////////////1.如果用户是管理员上面直接添加了,不是管理员则要添加分组以及用户//////////////////
//            if(isAdmin==0){
//                addSubGroup(organ1.getId(),tenantId,userName,dispname,0);
//            }
//        }else{
//            ////////判断用户是否存在,如果存在则返回  admin创建的机构只能添加一个管理员
//            ////////用户不存在,是否是管理员-->是管理员查看上层机构中是否存在用户,不存在添加到上层机构中
//            ////////1.上层用户存在,添加分组以及用户
//            User userIsexit = userService.findByUserName(userName);
//            if(userIsexit==null){//用户不存在
//                if(isAdmin==0){ //是普通用户,分组添加用户和权限---没有新建和编辑权限
//                    addSubGroup(organization.getId(),tenantId,userName,dispname,0);
//                }else{
//                    //用户是管理员,判断顶层机构是否有管理员,没有添加到顶层机构管理员,存在则添加子机构管理员
//                    List<User> users = userService.findByOrganizationId(organization.getId());
//                    if(users.size()==0){
//                        User user = addUser(userName,dispname,organization.getId());
//                        String add = userService.add(user, false);
//                        organization = organizationService.findById(organization.getId());
//                        if (organization != null) {
//                            organization.setAdminUserId(add);
//                            organizationService.update(organization);
//                        }
//                    }else{
//                        addSubGroup(organization.getId(),tenantId,userName,dispname,isAdmin);
//                    }
//                }
//            }else{
//                log.info(userName+"该用户已经存在,可以直接登录!");
//            }
//        }
////        String address = "http://119.254.92.55:8019/#/login?user="+userName+"&pass=1q2w3e4R";
////        map.put("address",address);
////        return map;
//
//        //免登录暂时注释
//        //免登录暂时注释
//        boolean flag = login(userName,request);
//        if(!flag){
//            return "登录失败";
//        }
//        log.info("---------->一键登录登录成功!");
//        return "可以登录了!";
//    }

}
