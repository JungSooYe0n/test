package com.trs.netInsight.widget.Login;

import com.trs.netInsight.handler.exception.LoginException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.LoginUtil;
import com.trs.netInsight.util.NetworkUtil;
import com.trs.netInsight.widget.user.entity.User;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
public class NoSessionController {

    /**
     * 判断是否登录
     *
     * @date Created at 2017年11月17日 下午7:32:45
     * @Author 谷泽昊
     * @return
     * @throws TRSException
     * @throws TRSException
     */
    @FormatResult
    @ApiOperation("判断是否登录")
    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public Object login(HttpServletRequest request) throws TRSException {
        Subject currentUser = SecurityUtils.getSubject();
        if (currentUser.isAuthenticated()) {
            Object principal = currentUser.getPrincipal();
            if (principal instanceof User) {
                User user = (User) principal;
                // 到期提醒
                LoginUtil.rangeExpiret(user);
                return user;
            }
        }
        throw new LoginException(CodeUtils.NO_LOGIN, NetworkUtil.getIpAddress(request) + ":没有登录！");
    }
}
