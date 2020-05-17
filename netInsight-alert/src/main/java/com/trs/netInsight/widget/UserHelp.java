package com.trs.netInsight.widget;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.DateUtil;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import com.trs.netInsight.widget.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class UserHelp {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrganizationRepository organizationRepository;

    @Value("${spring.session.timeout}")
    private int sessionTimeout;

    @Autowired
    private SessionDAO sessionDAO;

    public List<User> findOrgAmdin(String orgId) {
        return changeUser(userRepository.findByOrganizationIdAndCheckRole(orgId, UserUtils.ROLE_ADMIN));
    }

    /**
     * 将机构名字添加到机构
     *
     * @date Created at 2017年12月15日 下午4:58:16
     * @Author 谷泽昊
     * @param users
     * @return
     */
    private List<User> changeUser(List<User> users) {
        if (users == null) {
            return null;
        }
        Map<String,String> organInfo = new HashMap<>();
        for (User user : users) {
            String organizationId = user.getOrganizationId();
            if (StringUtils.isNotBlank(organizationId)) {
                //查询过的保存map中,防止多次查询
                if(organInfo.get(organizationId)!=null){
                    user.setOrganizationName(organInfo.get(organizationId));
                }else{
                    Organization organization = organizationRepository.findOne(organizationId);
                    if (organization != null) {
                        user.setOrganizationName(organization.getOrganizationName());
                        organInfo.put(organizationId,organization.getOrganizationName());
                    }
                }
            }
            //剩余有效期转换
            if (UserUtils.FOREVER_DATE.equals(user.getExpireAt())){
                user.setSurplusDate("永久");
            }else {
                String days = DateUtil.timeDifferenceDays(user.getExpireAt());
                user.setSurplusDate(days);
            }
            user.setLoginCount(UserUtils.getLoginCount(user.getUserName()+user.getId()));

        }
        return users;
    }

    private Page<User> changeUser(Page<User> page) {
        if (page == null) {
            return null;
        }
        List<User> content = page.getContent();
        for (User user : content) {
            String organizationId = user.getOrganizationId();
            if (StringUtils.isNotBlank(organizationId)) {
                Organization organization = organizationRepository.findOne(organizationId);
                if (organization != null) {
                    user.setOrganizationName(organization.getOrganizationName());
                }
            }
            //剩余有效期转换
            if (UserUtils.FOREVER_DATE.equals(user.getExpireAt())){
                user.setSurplusDate("永久");
            }else {
                String days = DateUtil.timeDifferenceDays(user.getExpireAt());
                user.setSurplusDate(days);
            }
            user.setLoginCount(UserUtils.getLoginCount(user.getUserName()+user.getId()));
        }
        return page;
    }

    public User findByUserName(String userName) {
        List<User> list = userRepository.findByUserName(userName);
        if (list != null && list.size() > 0) {
            User user = list.get(0);
            String organizationId = user.getOrganizationId();
            if (StringUtils.isNotBlank(organizationId)) {
                Organization organization = organizationRepository.findOne(user.getOrganizationId());
                if (organization != null) {
                    user.setOrganizationName(organization.getOrganizationName());
                }
            }
            return user;
        }
        return null;
    }

    public String addUser(User user, boolean copyFlag) {
        User save = userRepository.save(user);
        if (save == null) {
            return null;
        }
        return save.getId();
    }

    public User findById(String id) {
        return userRepository.findOne(id);
    }

    public List<User> findByOrganizationIdAndIdNot(String organizationId, String userId) {
        return changeUser(userRepository.findByOrganizationIdAndIdNot(organizationId, userId));
    }

    public Page<User> pageOrganListOrSubGroup(int pageNo, int pageSize) {
        Sort sort = new Sort(Sort.Direction.DESC, "createdTime");
        Pageable pageable = new PageRequest(pageNo, pageSize, sort);
        User loginUser = UserUtils.getUser();
        String organizationId = loginUser.getOrganizationId();
        return changeUser(userRepository.findByOrganizationId(organizationId, pageable));

    }

    public String login(UsernamePasswordToken token, String userName, String ip) throws TRSException {
        // 调用接口获取access_token
        //log.error("用户[" + userName + "]登录认证通过(这里可以进行一些认证通过后的一些系统参数初始化操作)");
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        try {
            // 在调用了login方法后,SecurityManager会收到AuthenticationToken,并将其发送给已配置的Realm执行必须的认证检查
            // 每个Realm都能在必要时对提交的AuthenticationTokens作出反应
            // 所以这一步在调用login(token)方法时,它会走到MyShiroRealm.doGetAuthenticationInfo()方法中,具体验证方式详见此方法
            log.error("对用户[" + userName + "]进行登录验证..验证开始");
            currentUser.login(token);
        } catch (UnknownAccountException uae) {
            log.error("对用户[" + userName + "]进行登录验证..验证未通过,未知账户");
            throw new TRSException(CodeUtils.FAIL, "账号或密码错误！");
        } catch (IncorrectCredentialsException ice) {

            log.error("对用户[" + userName + "]进行登录验证..验证未通过,错误的凭证");
            // 计算次数
            // Long increment =
            // RedisUtil.increment(UserUtils.REDIS_SHIRO_ACCOUNT + userName,
            // 1L);
            // log.error("increment:" + increment);
            // if (increment >= 5) {
            // RedisUtil.setString(UserUtils.REDIS_SHIRO_ACCOUNT + userName,
            // "LOCK", 1, TimeUnit.HOURS);
            // }
            throw new TRSException(CodeUtils.FAIL, "账号或密码错误！", ice);

        } catch (LockedAccountException lae) {
            log.error("对用户[" + userName + "]进行登录验证..验证未通过,账户已锁定");
            throw new TRSException(CodeUtils.ACCOUNT_LOCKOUT, "[" + userName + "]账户已锁定！", lae);
        } catch (ExcessiveAttemptsException eae) {
            log.error("对用户[" + userName + "]进行登录验证..验证未通过,错误次数大于5次,账户已锁定");
            throw new TRSException(CodeUtils.ACCOUNT_LOCKOUT, "[" + userName + "]错误次数大于5次，账户已锁定！", eae);
        } catch (DisabledAccountException sae) {
            log.error("对用户[" + userName + "]进行登录验证..验证未通过,帐号已经禁止登录");
            throw new TRSException(CodeUtils.UNKNOWN_ACCOUNT, "[" + userName + "]账号已经禁用！", sae);
        } catch (ExpiredCredentialsException ee) {
            log.error("对用户[" + userName + "]进行登录验证..验证未通过,帐号已经过期");
            throw new TRSException(CodeUtils.ACCOUNT_ERROR, "[" + userName + "]机构或账号已经过期！", ee);
        } catch (AuthenticationException ae) {
            // 通过处理Shiro的运行时AuthenticationException就可以控制用户登录失败或密码错误时的情景
            log.error("对用户[" + userName + "]进行登录验证..验证未通过,堆栈轨迹如下:" + ae);
            throw new TRSException(CodeUtils.UNKNOWN_FAIL, "[" + userName + "]未知错误！", ae);

        }
        // 验证是否登录成功
        if (currentUser.isAuthenticated()) {
            log.error("用户[" + userName + "]登录认证通过(这里可以进行一些认证通过后的一些系统参数初始化操作)");
            User user = findByUserName(userName);
            user = UserUtils.checkOrganization(user);
            // 将用户信息存入redis
            RedisUtil.setString(UserUtils.USERNAME_LOGIN_USER + user.getUserName(), session.getId().toString(),
                    sessionTimeout, TimeUnit.SECONDS);
            // 登录成功，更改ip和时间
            user.setLastLoginIp(ip);
            user.setLastLoginTime(com.trs.netInsight.support.fts.util.DateUtil.formatCurrentTime(com.trs.netInsight.support.fts.util.DateUtil.yyyyMMdd));
            user.setIsAlert(true);
            try {
                update(user, false);

            } catch (Exception e) {
                log.error("修改失败!", e);
                currentUser.logout();
                RedisUtil.deleteKey(UserUtils.SESSION_LOGIN_USER + session.getId());
                RedisUtil.deleteKey(UserUtils.USERNAME_LOGIN_USER + user.getUserName());
                throw new TRSException(CodeUtils.UNKNOWN_FAIL, "[" + userName + "]未知错误！", e);
            }
            // 将登录次数存入redis（按用户）
            Integer count = UserUtils.setLoginCount(userName+user.getId());
            //loginFrequencyLogService.save(count,user.getId());
            // 将登录次数存入redis（按分组）
            String subGroupId = user.getSubGroupId();
            UserUtils.setLoginCount(subGroupId);
            return "登录成功！";
        } else {
            token.clear();
        }
        throw new TRSException(CodeUtils.UNKNOWN_FAIL, "[" + userName + "]未知错误！");
    }

    public String update(User user, boolean isResetPassword) {
        User saveAndFlush = userRepository.saveAndFlush(user);
        if (saveAndFlush == null) {
            return null;
        }
        // 判断是否强行掉线
        if (isResetPassword) {
            compulsoryDownline(user.getUserName());
        }
        return saveAndFlush.getId();
    }

    /**
     * 强制下线
     *
     * @date Created at 2018年9月19日 下午2:42:18
     * @Author 谷泽昊
     * @param userName
     */
    private void compulsoryDownline(String userName) {
        String sessionId = RedisUtil.getString(UserUtils.USERNAME_LOGIN_USER + userName);
        if (StringUtils.isNotBlank(sessionId)) {
            Session session = null;
            try {
                session = sessionDAO.readSession(sessionId);
            } catch (Exception e) {
            }
            if (session != null) {
                // 强制退出
                sessionDAO.delete(session);
            }
        }
    }
}
