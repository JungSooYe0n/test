/*
 * Project: netInsight
 * 
 * File Created at 2017年11月20日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.user.service;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.Organization.OrganizationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 机构管理service
 * 
 * @Type IOrganizationService.java
 * @author 谷泽昊
 * @date 2017年11月20日 上午10:09:57
 * @version
 */
public interface IOrganizationService {

	/**
	 * 根据名字查找机构
	 * 
	 * @date Created at 2017年11月20日 下午2:06:24
	 * @Author 谷泽昊
	 * @param organizationName
	 * @return
	 */
	public List<Organization> findByOrganizationName(String organizationName);

	/**
	 * 添加机构
	 * 
	 * @date Created at 2017年11月20日 下午2:07:07
	 * @Author 谷泽昊
	 * @param organization
	 */
	public String add(Organization organization);

	/**
	 * 修改机构
	 * 
	 * @date Created at 2017年11月20日 下午2:07:07
	 * @Author 谷泽昊
	 * @param organization
	 */
	public String update(Organization organization);

	/**
	 * 根据id查找机构
	 * 
	 * @date Created at 2017年11月20日 下午2:32:56
	 * @Author 谷泽昊
	 * @param organizationId
	 * @return
	 */
	public Organization findById(String organizationId);

	/**
	 * 根据条件添加机构
	 * 
	 * @date Created at 2018年9月14日 下午2:28:10
	 * @Author 谷泽昊
	 * @param organizationType
	 *            机构类型:正式formal，使用trial
	 * @param organizationName
	 *            机构名称
	 * @param userName
	 *            管理员账号
	 * @param displayName
	 *            用户名
	 * @param password
	 *            登录密码
	 * @param email
	 *            联系邮箱
	 * @param phone
	 *            联系电话
	 * @param expireAt
	 *            有效期
	 * @param customerSource
	 *            客户来源
	 * @param headOfSales
	 *            销售负责人
	 * @param rolePlatformId
	 *            运维负责人
	 * @param descriptions
	 *            备注
	 * @param systemName
	 *            系统名称
	 * @param logoPicName
	 *            上传logo图片路径
	 * @param userLimit
	 *            机构用户限制
	 * @param sameTimeLogin
	 *            账号同时登录
	 * @param roleIds
	 *            普通账号权限
	 * @param dataSources
	 *            数据设置，全部传ALL
	 * @param dataDate
	 *            可检索数据，分别为1,3,6,12
	 * @return
	 */
//	public boolean add(OrganizationType organizationType, String organizationName, String userName, String displayName,
//			String password, String email, String phone, String expireAt, String customerSource, String headOfSales,
//			String[] rolePlatformIds, String descriptions, String systemName, String logoPicName, int userLimit,
//			boolean sameTimeLogin, String[] roleIds, String[] dataSources, int dataDate);

	/**
	 * 添加机构
	 * @param organizationType  机构类型:正式formal，使用trial
	 * @param organizationName  机构名称
	 * @param logoPicName       logo图片名称
	 * @param userName          管理员账号
	 * @param password          密码
	 * @param displayName       管理员昵称
	 * @param email             联系邮箱
	 * @param phone             联系电话
	 * @param expireAt          有效期
	 * @param customerSource    客户来源
	 * @param headOfSales       销售负责人
	 * @param rolePlatformIds   运维负责人
	 * @param descriptions      备注
	 * @param userLimit         机构用户限制
	 * @param columnNum         日常监测 数量限制
	 * @param specialNum        专题  数量限制
	 * @param alertNum          预警规则  数量限制
	 * @param alertAccountNum   预警账号  可绑定限制
	 * @param keyWordsNum       关键字 数量限制
	 * @param dataSources       数据源 限制
	 * @param columnDateLimit   日常监测 可检索数据限制
	 * @param specialDateLimit  专题分析 可检索数据限制
	 * @param aSearchDateLimit  高级搜索 可检索数据限制
	 * @param suffix  			机构登录页配置 链接后缀
	 * @param pageTitle         机构登录页配置 页面标签
	 * @param companyName       机构登录页配置 公司名称
	 * @param applyTel          机构登录页配置 申请电话
	 * @param loginLogoPic      机构登录页配置 登录页公司LOGO
	 * @param QRCodePic         机构登录页配置 登录页底栏二维码
	 * @param isShieldRegister  机构登录页配置 是否屏蔽申请注册
	 * @param isShowCarousel    机构登录页配置 是否展示轮播图
	 * @param isAdmin    自动添加用户的时候,是否是管理员
	 * @param isAutoAdd    是否是自动添加 1自动添加
	 * @param tenantId    天目云接口中用户id
	 * @param tradition 小库：传统
	 * @param weiBo 小库：微博
	 * @param weiXin 小库：微信
	 * @param overseas 小库：海外库
	 * @return
	 */
	boolean add(OrganizationType organizationType, String organizationName, String logoPicName, String userName, String password, String displayName,
                String email, String phone, String expireAt, String customerSource, String headOfSales,
                String[] rolePlatformIds, String descriptions, int userLimit, int columnNum, int specialNum, int alertNum, int alertAccountNum, int keyWordsNum,
                String[] dataSources, int columnDateLimit, int specialDateLimit, int aSearchDateLimit,
                String suffix, String pageTitle, String companyName, String applyTel, String loginLogoPic,
                String QRCodePic, Boolean isShieldRegister, Boolean isShowCarousel, int isAdmin, int isAutoAdd, String tenantId, String tradition, String weiBo, String weiXin, String overseas,String video);
	/**
	 * 根据id删除机构
	 *
	 * @date Created at 2017年11月20日 下午3:25:17
	 * @Author 谷泽昊
	 * @param id
	 */
	public void delete(String id);

	/**
	 * 分页查询机构
	 *
	 * @date Created at 2017年11月20日 下午3:48:57
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @param organizationName
	 * @return
	 */
	public Page<Organization> pageList(int pageNo, int pageSize, String organizationName, List<Sort.Order> listSort);

	/**
	 * 修改机构
	 *
	 * @date Created at 2018年9月17日 上午10:01:17
	 * @Author 谷泽昊
	 * @param id
	 * @param organizationType
	 * @param organizationName
	 * @param displayName
	 * @param email
	 * @param phone
	 * @param expireAt
	 * @param customerSource
	 * @param headOfSales
	 * @param rolePlatformIds
	 * @param descriptions
	 * @param systemName
	 * @param pictureName
	 * @param userLimit
	 * @param sameTimeLogin
	 * @param roleIds
	 * @param dataSources
	 * @param dataDate
	 */
//	public void updateOrganization(String id, OrganizationType organizationType, String organizationName,
//			String displayName, String email, String phone, String expireAt, String customerSource, String headOfSales,
//			String[] rolePlatformIds, String descriptions, String systemName, String pictureName, int userLimit,
//			boolean sameTimeLogin, String[] roleIds, String[] dataSources, int dataDate);
	/**
	 * 修改机构
	 * @param id                机构id
	 * @param organizationType  机构类型:正式formal，使用trial
	 * @param organizationName  机构名称
	 * @param logoPicName       logo图片名称
	 * @param userName          管理员账号
	 * @param password          密码
	 * @param displayName       管理员昵称
	 * @param email             联系邮箱
	 * @param phone             联系电话
	 * @param expireAt          有效期
	 * @param customerSource    客户来源
	 * @param headOfSales       销售负责人
	 * @param rolePlatformIds   运维负责人
	 * @param descriptions      备注
	 * @param userLimit         机构用户限制
	 * @param columnNum         日常监测 数量限制
	 * @param specialNum        专题  数量限制
	 * @param alertNum          预警规则  数量限制
	 * @param alertAccountNum   预警账号  可绑定限制
	 * @param keyWordsNum       关键字 数量限制
	 * @param dataSources       数据源 限制
	 * @param columnDateLimit   日常监测 可检索数据限制
	 * @param specialDateLimit  专题分析 可检索数据限制
	 * @param aSearchDateLimit  高级搜索 可检索数据限制
	 * @param suffix  			机构登录页配置 链接后缀
	 * @param pageTitle         机构登录页配置 页面标签
	 * @param companyName       机构登录页配置 公司名称
	 * @param applyTel          机构登录页配置 申请电话
	 * @param loginLogoPic      机构登录页配置 登录页公司LOGO
	 * @param QRCodePic         机构登录页配置 登录页底栏二维码
	 * @param isShieldRegister  机构登录页配置 是否屏蔽申请注册
	 * @param isShowCarousel    机构登录页配置 是否展示轮播图
	 * @return
	 */
	public void updateOrganization(String id, OrganizationType organizationType, String organizationName, String logoPicName, String userName, String password, String displayName,
                                   String email, String phone, String expireAt, String customerSource, String headOfSales,
                                   String[] rolePlatformIds, String descriptions, int userLimit, int columnNum, int specialNum, int alertNum, int alertAccountNum, int keyWordsNum,
                                   String[] dataSources, int columnDateLimit, int specialDateLimit, int aSearchDateLimit,
                                   String suffix, String pageTitle, String companyName, String applyTel, String loginLogoPic,
                                   String QRCodePic, Boolean isShieldRegister, Boolean isShowCarousel, String loginPagePictureName,
                                   String QRCodePictureName);

	/**
	 * 多维度检索
	 *
	 * @date Created at 2018年9月14日 下午3:25:34
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @param customerSource
	 * @param organizationType
	 * @param retrievalCondition
	 * @param retrievalInformation
	 * @return
	 */
//	public Page<Organization> pageList(int pageNo, int pageSize, String customerSource, String organizationType,
//			String retrievalCondition, String retrievalInformation);

	/**
	 * 机构列表查询
	 * @param pageNo
	 * @param pageSize
	 * @param customerSource
	 * @param headOfSales
	 * @param surplusDateSort
	 * @param organizationType
	 * @param status
	 * @param retrievalCondition
	 * @param retrievalInformation
	 * @return
	 */
	public Page<Organization> pageList(int pageNo, int pageSize, List<String> customerSource, List<String> headOfSales, String surplusDateSort, String organizationType,
                                       String status, String retrievalCondition, String retrievalInformation) throws TRSException;

	/**
	 * 批量查询
	 *
	 * @date Created at 2018年9月17日 下午1:50:35
	 * @Author 谷泽昊
	 * @param ids
	 * @return
	 */
	public List<Organization> findByIds(String[] ids);

	/**
	 * 批量查询
	 *
	 * @date Created at 2018年9月17日 下午1:50:35
	 * @Author 谷泽昊
	 * @param ids
	 * @return
	 */
	public List<Organization> findByIds(Collection<String> ids);

	/**
	 * 查询运维管理的机构或者没有管理的机构
	 *
	 * @date Created at 2018年9月26日 下午4:51:12
	 * @Author 谷泽昊
	 * @param pageNo
	 * @param pageSize
	 * @param governing
	 * @param id
	 * @param customerSource
	 * @param organizationType
	 * @param retrievalCondition
	 * @param retrievalInformation
	 * @return
	 */
	public Page<Organization> findByIsPlatformHold(int pageNo, int pageSize, boolean governing, String id,
                                                   List<String> customerSource, List<String> headOfSales, String organizationType, String retrievalCondition, String retrievalInformation, String surplusDateSort, String status) throws TRSException;

	/**
	 * 根据机构id获取机构权限和是否同时登录
	 *
	 * @date Created at 2018年9月26日 下午10:17:00
	 * @Author 谷泽昊
	 * @param organizationId
	 * @return
	 */
	public Map<String, Object> getOrganizationRoleANdSameTimeLogin(String organizationId);

	/**
	 * 批量查询
	 *
	 * @date Created at 2018年11月7日 上午10:50:22
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param ids
	 * @param pageable
	 * @return
	 */
	public Page<Organization> findByIdIn(Collection<String> ids, Pageable pageable);

	/**
	 * 查询所有
	 *
	 * @date Created at 2018年11月7日 上午10:52:11
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @return
	 */
	public List<Organization> findAll();

	/**
	 * 根据机构名和id查询
	 *
	 * @date Created at 2018年11月7日 上午10:52:25
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param
	 * @param ids
	 * @return
	 */
	public Collection<Organization> findByOrganizationNameLikeAndIdIn(String organizationName, List<String> ids);

	/**
	 * 根据系统名和id查询
	 *
	 * @date Created at 2018年11月7日 上午10:52:56
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param systemName
	 * @param ids
	 * @return
	 */
	/*public Collection<Organization> findBySystemNameLikeAndIdIn(String systemName, List<String> ids);*/

	/**
	 * 根据系统名和id分页查询
	 *
	 * @date Created at 2018年11月7日 上午10:53:37
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param organizationName
	 * @param ids
	 * @param pageable
	 * @return
	 */
	public Page<Organization> findByOrganizationNameLikeAndIdIn(String organizationName, List<String> ids,
                                                                Pageable pageable);

	/**
	 * 根据机构名分页查询
	 *
	 * @date Created at 2018年11月7日 上午10:54:12
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param
	 * @param pageable
	 * @return
	 */
	public Page<Organization> findByOrganizationNameLike(String organizationName, Pageable pageable);

	/**
	 * 判断指定机构下是否有此用户
	 *
	 * @date Created at 2018年12月10日 下午2:56:31
	 * @author 北京拓尔思信息技术股份有限公司
	 * @author 谷泽昊
	 * @param organizationId
	 * @param userId
	 * @return
	 */
	public boolean isOrganizationExistUser(String organizationId, String userId);

	/**
	 * 查询所有的销售负责人
	 * @return
	 */
	public List<String> findAllForHeadOfSales();

	/**
	 * 根据条件查询
	 * @param criteria
	 * @param pageable
	 * @return
	 */
	Page<Organization> findByCriteria(Specification<Organization> criteria, PageRequest pageable);
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2017年11月20日 谷泽昊 creat
 */