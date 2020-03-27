package com.trs.netInsight.widget.user.service.impl;

import com.trs.netInsight.util.PictureUtil;
import com.trs.netInsight.widget.user.entity.LoginPageConfig;
import com.trs.netInsight.widget.user.repository.LoginPageConfigRepository;
import com.trs.netInsight.widget.user.service.ILoginPageConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 机构登录配置页管理service
 *
 * @Type LoginPageConfigServiceImpl.java
 * @author
 * @date
 * @version
 */
@Service
public class LoginPageConfigServiceImpl implements ILoginPageConfigService {

    @Autowired
    private LoginPageConfigRepository loginPageConfigRepository;

    @Override
    public LoginPageConfig findBySuffix(String suffix) {
        return loginPageConfigRepository.findBySuffix(suffix);
    }

    @Override
    public LoginPageConfig findByOrgId(String orgId) {
        return loginPageConfigRepository.findByOrgId(orgId);
    }

    @Override
    public String add(LoginPageConfig loginPageConfig) {
        LoginPageConfig add = loginPageConfigRepository.save(loginPageConfig);
        return add.getId();
    }

    @Override
    public String update(LoginPageConfig loginPageConfig) {
        LoginPageConfig save = loginPageConfigRepository.saveAndFlush(loginPageConfig);
        return save.getId();
    }

    @Override
    public void delete(String id) {
        LoginPageConfig loginPageConfig = loginPageConfigRepository.findOne(id);
        // 删除机构下登录页的LOGO图片
        PictureUtil.deletePic(loginPageConfig.getLogoPicName(),"org");
        // 删除机构下登录页二维码图片
        PictureUtil.deletePic(loginPageConfig.getQRCodeName(),"org");
        loginPageConfigRepository.delete(loginPageConfig.getId());
    }
}
