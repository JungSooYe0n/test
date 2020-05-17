package com.trs.netInsight.widget.user.service;

import com.trs.netInsight.widget.user.entity.LoginPageConfig;

/**
 * 机构登录配置页管理service
 * 
 * @Type ILoginPageConfigService.java
 * @author
 * @date
 * @version
 */
public interface ILoginPageConfigService {

	/**
	 * 根据链接后缀寻找页面配置
	 *
	 * @date
	 * @Author
	 * @param
	 * @return
	 */
	public LoginPageConfig findBySuffix(String suffix);

	/**
	 * 根据链接后缀寻找页面配置
	 *
	 * @date
	 * @Author
	 * @param
	 * @return
	 */
	public LoginPageConfig findByOrgId(String orgId);

	/**
	 * 添加机构页面配置
	 *
	 * @date
	 * @Author
	 * @param
	 */
	public String add(LoginPageConfig loginPageConfig);

	/**
	 * 修改机构页面配置
	 *
	 * @date
	 * @Author
	 * @param
	 */
	public String update(LoginPageConfig loginPageConfig);


	/**
	 * 根据id删除机构页面配置
	 *
	 * @date
	 * @Author
	 * @param id
	 */
	public void delete(String id);


}

