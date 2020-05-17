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
package com.trs.netInsight.widget.user.service.impl;

import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.support.fts.model.factory.HybaseFactory;
import com.trs.netInsight.support.hybaseShard.service.IHybaseShardService;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.config.service.ISystemConfigService;
import com.trs.netInsight.widget.user.entity.*;
import com.trs.netInsight.widget.user.entity.Organization.OrganizationType;
import com.trs.netInsight.widget.user.entity.enums.CheckRole;
import com.trs.netInsight.widget.user.entity.enums.Status;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import com.trs.netInsight.widget.user.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.*;

/**
 * 机构管理service
 * 
 * @Type OrganizationServiceImpl.java
 * @author 谷泽昊
 * @date 2017年11月20日 上午10:10:33
 * @version
 */
@Service
@Slf4j
public class OrganizationServiceImpl implements IOrganizationService {

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private IUserService userService;
	@Autowired
	private IRoleService roleService;
	@Autowired
	private ISubGroupService subGroupService;
	@Autowired
	private ILoginPageConfigService loginPageConfigService;

	@Autowired
	private ISystemConfigService systemConfigService;

	@Autowired
	private IHybaseShardService hybaseShardService;

	@Override
	public List<Organization> findByOrganizationName(String organizationName) {
		return organizationRepository.findByOrganizationName(organizationName);
	}

	@Override
	public String add(Organization organization) {
		Organization save = organizationRepository.save(organization);
		return save.getId();
	}

	@Override
	public String update(Organization organization) {
		organization.setOrganizationName(organization.getOrganizationName());
		Organization save = organizationRepository.saveAndFlush(organization);
		return save.getId();
	}

	@Override
	public Organization findById(String organizationId) {
		return organizationRepository.findOne(organizationId);
	}

	@Override
	@Transactional
	public boolean add(OrganizationType organizationType, String organizationName, String logoPicName,String userName,String password, String displayName,
					   String email, String phone, String expireAt, String customerSource, String headOfSales,
					   String[] rolePlatformIds,String descriptions, int userLimit,int columnNum,int specialNum,int alertNum,int alertAccountNum,int keyWordsNum,
					   String[] dataSources, int columnDateLimit,int specialDateLimit,int aSearchDateLimit,
					   String suffix, String pageTitle, String companyName, String applyTel, String loginLogoPic,
					   String QRCodePic,Boolean isShieldRegister ,Boolean isShowCarousel,int isAdmin,int isAutoAdd,String tenantId,String tradition,String weiBo,String weiXin,String overseas) {
		try {
			// 保存机构
			Organization organization = new Organization();
			organization.setOrganizationType(organizationType);
			organization.setOrganizationName(organizationName);
			//添加字段,机构是否为自动添加,通过接口可以自动添加
			if(isAutoAdd==1){
				organization.setAutoAdd("1");
				//默认admin添加的
				organization.setOrganizationId("trsadminorganizationid");
			}
			organization.setLogoPicName(logoPicName);

			//organization.setSameTimeLogin(sameTimeLogin);
			organization.setUserLimit(userLimit);
			organization.setExpireAt(expireAt);
			organization.setCustomerSource(customerSource);
			organization.setHeadOfSales(headOfSales);
			//日常监测 、专题、预警、预警账号、关键字 数量限制
			organization.setColumnNum(columnNum);
			organization.setSpecialNum(specialNum);
			organization.setAlertNum(alertNum);
			organization.setAlertAccountNum(alertAccountNum);
			organization.setKeyWordsNum(keyWordsNum);
			//日常监测 、专题、高级搜索可检索时间限制
			organization.setColumnDateLimit(columnDateLimit);
			organization.setSpecialDateLimit(specialDateLimit);
			organization.setASearchDateLimit(aSearchDateLimit);

			//organization.setDataDate(dataDate);
			//organization.setSystemName(systemName);
			if (dataSources != null && dataSources.length > 0) {
				organization.setDataSources(String.join(",", dataSources));
			}
			organization.setDescriptions(descriptions);
			// 运维负责人
			// 因为是第一次，所以直接添加添加
			if (rolePlatformIds != null && rolePlatformIds.length > 0) {
				List<User> rolePlatforms = userService.findByIds(rolePlatformIds);
				for (User user : rolePlatforms) {
					user.getOrganizations().add(organization);
				}
			}
			// 保存机构
			String organizationId = this.add(organization);
			hybaseShardService.save(HybaseFactory.getServer(),HybaseFactory.getUserName(),HybaseFactory.getPassword(),tradition,weiBo,weiXin,overseas,null,organizationId);
//			hybaseShardContrller.saveMicroblog(tradition,weiBo,weiXin,overseas,null,organizationId);
			//自动添加的时候如果部署管理员不添加用户
			if(isAdmin == 1){
				// 保存用户
				User user = new User();
				user.setUserName(userName);
				user.setDisplayName(displayName);
				user.setTenantId(tenantId);

				// 加密
				String salt = UUID.randomUUID().toString();// 加密的salt
				// 加密后的密码
				String encryptPsw = UserUtils.getEncryptPsw(password, salt);
				user.setSalt(salt);
				user.setPassword(encryptPsw);
				user.setEmail(email);
				user.setPhone(phone);
				user.setExpireAt(expireAt);
				user.setStatus(Status.normal);
				//机构管理员
				user.setCheckRole(CheckRole.ROLE_ADMIN);
				//	user.setSameTimeLogin(sameTimeLogin);
				user.setOrganizationId(organizationId);
				//机构管理员 有所有权限
				List<Role> roles =  roleService.findByRoleTypeAndDes(CheckRole.ROLE_ADMIN,"日常监测、专题分析、预警中心");
				if (ObjectUtil.isNotEmpty(roles)){
					user.setRoles(new HashSet<>(roles));
				}else {
					user.setRoles(null);
				}

				String add = userService.add(user, false);

				// 将机构管理员存入机构
				//自动添加用户的时候,添加相应的id,是否是管理员
				organization = this.findById(organizationId);
				if (organization != null) {
					organization.setAdminUserId(add);
					this.update(organization);
				}
			}

			if(suffix != null && !"".equals(suffix)){
				LoginPageConfig loginPageConfig = new LoginPageConfig();
				loginPageConfig.setSuffix(suffix);
				loginPageConfig.setCompanyName(companyName);
				loginPageConfig.setPageTitle(pageTitle);
				loginPageConfig.setLogoPicName(loginLogoPic);
				loginPageConfig.setApplyTel(applyTel);
				loginPageConfig.setIsShowCarousel(isShowCarousel);
				loginPageConfig.setIsShieldRegister(isShieldRegister);
				loginPageConfig.setQRCodeName(QRCodePic);
				loginPageConfig.setRelevanceOrganizationId(organizationId);
				loginPageConfigService.add(loginPageConfig);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("报错,事务回滚");
			throw new RuntimeException();
		}
		return true;
	}

	@Override
	@Transactional
	public void delete(String id) {
		Organization organization = findById(id);
		// 删除机构下的logo图片
		PictureUtil.deletePic(organization.getLogoPicName(),"org");
		// 获取所有的运维
		Set<User> rolePlatforms = organization.getRolePlatforms();
		if (rolePlatforms != null && rolePlatforms.size() > 0) {
			// 解除关联
			for (User user : rolePlatforms) {
				Set<Organization> organizations = user.getOrganizations();
				organizations.remove(organization);
			}
		}
		// 删除机构
		organizationRepository.delete(id);

		//删除机构下的用户分组
		List<SubGroup> subGroups = subGroupService.findByOrgId(id);
		if (ObjectUtil.isNotEmpty(subGroups)){
			for (SubGroup subGroup : subGroups) {
				subGroupService.delete(subGroup.getId());
			}
		}

		// 删除该机构下的用户
		List<User> list = userService.findByOrganizationId(id);
		userService.deleteByUser(list);

		//删除机构配置的登录页
		if(loginPageConfigService.findByOrgId(id) != null){

			loginPageConfigService.delete(loginPageConfigService.findByOrgId(id).getId());
		}

		// 删除机构时删除机构创建的所有东西
		// 待实现

	}

	@Override
	public Page<Organization> pageList(int pageNo, int pageSize, String organizationName,List<Sort.Order> listSort) {
		PageRequest pageable = new PageRequest(pageNo, pageSize, new Sort(listSort));
		Page<Organization> page;
		if (StringUtils.isNotBlank(organizationName)) {
			page = organizationRepository.findByOrganizationNameLike("%" + organizationName + "%", pageable);
		} else {
			page = organizationRepository.findAll(pageable);
		}

		return addUser(page);
	}

	@Override
	public void updateOrganization(String id,OrganizationType organizationType, String organizationName, String logoPicName,String userName,String password, String displayName,
								   String email, String phone, String expireAt, String customerSource, String headOfSales,
								   String[] rolePlatformIds,String descriptions, int userLimit,int columnNum,int specialNum,int alertNum,int alertAccountNum,int keyWordsNum,
								   String[] dataSources, int columnDateLimit,int specialDateLimit,int aSearchDateLimit,
								   String suffix, String pageTitle, String companyName, String applyTel, String loginLogoPic,
								   String QRCodePic,Boolean isShieldRegister ,Boolean isShowCarousel, String loginPagePictureName,
								   String QRCodePictureName) {

		// 保存机构
		Organization organization = organizationRepository.findOne(id);
		organization.setOrganizationType(organizationType);
		organization.setOrganizationName(organizationName);
		organization.setLogoPicName(logoPicName);

		organization.setUserLimit(userLimit);
		organization.setExpireAt(expireAt);
		organization.setCustomerSource(customerSource);
		organization.setHeadOfSales(headOfSales);
		//日常监测 、专题、预警、预警账号、关键字 数量限制
		organization.setColumnNum(columnNum);
		organization.setSpecialNum(specialNum);
		organization.setAlertNum(alertNum);
		organization.setAlertAccountNum(alertAccountNum);
		organization.setKeyWordsNum(keyWordsNum);
		//日常监测 、专题、高级搜索可检索时间限制
		organization.setColumnDateLimit(columnDateLimit);
		organization.setSpecialDateLimit(specialDateLimit);
		organization.setASearchDateLimit(aSearchDateLimit);
		if (dataSources != null && dataSources.length > 0) {
			organization.setDataSources(String.join(",", dataSources));
		}
		organization.setDescriptions(descriptions);

		// 运维
		// 先删除
		Set<User> rolePlatformsOld = organization.getRolePlatforms();
		for (User user : rolePlatformsOld) {
			user.getOrganizations().remove(organization);
		}
		// 在添加
		if (rolePlatformIds != null && rolePlatformIds.length > 0) {
			List<User> rolePlatforms = userService.findByIds(rolePlatformIds);
			for (User user : rolePlatforms) {
				user.getOrganizations().add(organization);
			}
		}

		// 保存机构
		String organizationId = this.add(organization);
		// 保存用户
		User user = userService.findById(organization.getAdminUserId());
		user.setUserName(userName);

		// 加密后的密码
		if (StringUtil.isNotEmpty(password)){
			// 加密
			String salt = UUID.randomUUID().toString();// 加密的salt
			String encryptPsw = UserUtils.getEncryptPsw(password, salt);
			user.setSalt(salt);
			user.setPassword(encryptPsw);

		}

		user.setDisplayName(displayName);

		user.setEmail(email);
		user.setPhone(phone);
		user.setExpireAt(expireAt);
		user.setStatus(Status.normal);
		user.setCheckRole(CheckRole.ROLE_ADMIN);
		//user.setSameTimeLogin(sameTimeLogin);
		user.setOrganizationId(organizationId);


		String add = userService.update(user, false);

		if ( ! UserUtils.FOREVER_DATE.equals(expireAt)){
			//修改机构下 用户分组及账号的有效期
			List<SubGroup> subGroupList = subGroupService.findByOrgId(id);
			if (ObjectUtil.isNotEmpty(subGroupList)){
				for (SubGroup subGroup : subGroupList) {
					//比较 当前 用户分组 与 机构现在的有效期， 若长于当前机构有效期，则按当前机构有效期来
					if (DateUtil.isExpire(subGroup.getExpireAt(),expireAt)){
						subGroup.setExpireAt(expireAt);
						subGroupService.update(subGroup);

						//修改用户分组下的用户有效期
						List<User> userList = userService.findBySubGroupId(subGroup.getId());
						if (ObjectUtil.isNotEmpty(userList)){
							for (User subUser : userList) {
								//用户有效期与用户分组是保持一致的，无需判断，直接修改
								subUser.setExpireAt(expireAt);
								userService.update(subUser,false);
							}
						}
					}

				}
			}
		}

		// 将机构存入管理员账号
		organization = null;
		organization = this.findById(organizationId);
		if (organization != null) {
			organization.setAdminUserId(add);
			this.update(organization);
		}
		if(suffix != null && !"".equals(suffix)){
			LoginPageConfig loginPageConfig = new LoginPageConfig();
			loginPageConfig.setRelevanceOrganizationId(id);
			loginPageConfig.setSuffix(suffix);
			loginPageConfig.setCompanyName(companyName);
			loginPageConfig.setPageTitle(pageTitle);
			if(loginLogoPic == null || "".equals(loginLogoPic)){
				if("无logo".equals(loginPagePictureName)){
					loginPagePictureName = null;
				}
				loginPageConfig.setLogoPicName(loginPagePictureName);
			}else {
				loginPageConfig.setLogoPicName(loginLogoPic);
			}
			loginPageConfig.setApplyTel(applyTel);
			loginPageConfig.setIsShowCarousel(isShowCarousel);
			loginPageConfig.setIsShieldRegister(isShieldRegister);
			if(QRCodePic == null || "".equals(QRCodePic)){
				if("无logo".equals(QRCodePictureName)){
					QRCodePictureName = null;
				}
				loginPageConfig.setQRCodeName(QRCodePictureName);
			}else {
				loginPageConfig.setQRCodeName(QRCodePic);
			}

			if(loginPageConfigService.findByOrgId(id) != null){
				LoginPageConfig loginPageConfig_old = loginPageConfigService.findByOrgId(id);
				if((loginLogoPic != null && !"".equals(loginLogoPic))
						|| (loginPageConfig.getLogoPicName() == null || "".equals(loginPageConfig.getLogoPicName()))){
					PictureUtil.deletePic( loginPageConfig_old.getLogoPicName(),"org");
				}
				if( (QRCodePic != null && !"".equals(QRCodePic))|| (loginPageConfig.getQRCodeName() == null || "".equals(loginPageConfig.getQRCodeName()))  ){
					PictureUtil.deletePic( loginPageConfig_old.getQRCodeName(),"org");
				}

				loginPageConfig.setId(loginPageConfig_old.getId());
				loginPageConfigService.update(loginPageConfig);
			}else{
				loginPageConfigService.add(loginPageConfig);
			}
		}else{
			if(loginPageConfigService.findByOrgId(id) != null){
				loginPageConfigService.delete(loginPageConfigService.findByOrgId(id).getId());
			}
		}
	}

	/**
	 * 给用户赋角色
	 * 
	 * @date Created at 2018年9月28日 上午9:54:39
	 * @Author 谷泽昊
	 * @param users
	 * @param roles
	 *            如果为null时，则赋值为null，如果不为null时，则是需要对比的权限
	 * @return
	 */
	private List<User> setRole(List<User> users, Set<Role> roles) {
		for (User user : users) {
			if (roles == null) {
				user.setRoles(roles);
			} else {
				Set<Role> userRoles = user.getRoles();
				if (userRoles != null && userRoles.size() > 0) {
					// 对比一下，如果最新权限不包含以前的权限，就去掉
					Iterator<Role> it = userRoles.iterator();
					while (it.hasNext()) {
						if (!roles.contains(it.next())) {
							it.remove();
						}
					}
					user.setRoles(userRoles);
				}
			}
		}
		return users;
	}

	@Override
	public Page<Organization> pageList(int pageNo, int pageSize, List<String> customerSource,List<String>  headOfSales,String surplusDateSort,String organizationType,
									   String status,String retrievalCondition, String retrievalInformation) throws TRSException {

		Sort sort = null;
		if (StringUtils.isBlank(surplusDateSort)){
			//默认排序（机构创建日期降序排，即最新创建的在上面）
			sort = new Sort(Direction.DESC, "createdTime");
		}else if ("desc".equals(surplusDateSort)){
			//剩余有效期 降序排(即 到期字段降序排)
			sort = new Sort(Direction.DESC, "expireAt");
		}else if ("asc".equals(surplusDateSort)){
			//剩余有效期 升序排(即 到期字段升序排)
			sort = new Sort(Direction.ASC, "expireAt");
		}else {
			throw new TRSException(CodeUtils.FAIL, "请输入正确的排序方式！");
		}

		PageRequest pageable = new PageRequest(pageNo, pageSize, sort);
		Page<Organization> organizations = null;

			// 自定义,trs包的Criteria 一直报空指针，不明白为啥，所以用原生的写
			Specification<Organization> criteria = new Specification<Organization>() {

				@Override
				public Predicate toPredicate(Root<Organization> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					List<Predicate> predicate = new ArrayList<>();
					if (ObjectUtil.isNotEmpty(customerSource)) {
						In<String> in = cb.in(root.get("customerSource").as(String.class));
						for (int i = 0; i < customerSource.size(); i++) {
							in.value(customerSource.get(i));
						}
						predicate.add(in);
					}
					if (ObjectUtil.isNotEmpty(headOfSales)) {
						In<String> in = cb.in(root.get("headOfSales").as(String.class));
						for (int i = 0; i < headOfSales.size(); i++) {
							in.value(headOfSales.get(i));
						}
						predicate.add(in);
						//predicate.add(cb.equal(root.get("headOfSales"), headOfSales));
					}
					if (StringUtils.isNotBlank(organizationType)) {
						predicate.add(cb.equal(root.get("organizationType"), organizationType));
					}
					if (StringUtils.isNotBlank(status)) {
						predicate.add(cb.equal(root.get("status"), status));
					}

					if (StringUtils.isNotBlank(retrievalInformation) && StringUtils.isNotBlank(retrievalCondition)) {
						if ("userName".equals(retrievalCondition)) {
							Specification<User> criteriaUser = new Specification<User>() {
								@Override
								public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
									List<Predicate> predicateUser = new ArrayList<>();
									if (StringUtils.isNotBlank(retrievalInformation) && StringUtils.isNotBlank(retrievalCondition)) {
										predicateUser.add(cb.like(root.get(retrievalCondition), "%" + retrievalInformation + "%"));
									}
									Predicate[] pre = new Predicate[predicateUser.size()];
									return query.where(predicateUser.toArray(pre)).getRestriction();
								}
							};
							List<User> users = userService.findAll(criteriaUser);
                            In<String> in = cb.in(root.get("id").as(String.class));
                            if(users != null && users.size() > 0){
								for (User user : users) {
									in.value(user.getOrganizationId());
								}
							}else{
								in.value("");
							}
                            predicate.add(in);

						}else {

							predicate.add(cb.like(root.get(retrievalCondition), "%" + retrievalInformation + "%"));
						}
					}
					Predicate[] pre = new Predicate[predicate.size()];
					return query.where(predicate.toArray(pre)).getRestriction();
				}
			};
			organizations = organizationRepository.findAll(criteria, pageable);


		return addUser(organizations);
	}

	/**
	 * 把管理员id换成实体类
	 * 
	 * @date Created at 2018年9月14日 下午3:32:23
	 * @Author 谷泽昊
	 * @param page
	 * @return
	 */
	private Page<Organization> addUser(Page<Organization> page) {
		if (page != null && page.getContent() != null && page.getContent().size() > 0) {
			List<Organization> list = page.getContent();
			for (Organization organization : list) {
				if(organization.getAdminUserId()==null) continue;
				User user = userService.findById(organization.getAdminUserId());
				organization.setUser(user);
				//剩余有效期转换
				if (UserUtils.FOREVER_DATE.equals(organization.getExpireAt())){
					organization.setSurplusDate("永久");
				}else {
					String days = DateUtil.timeDifferenceDays(organization.getExpireAt());
					organization.setSurplusDate(days);
				}
			}
		}
		return page;
	}

	@Override
	public List<Organization> findByIds(String[] ids) {
		if (ids == null || ids.length <= 0) {
			return null;
		}
		return findByIds(Arrays.asList(ids));
	}

	@Override
	public List<Organization> findByIds(Collection<String> ids) {
		if (ids == null || ids.size() <= 0) {
			return null;
		}
		return organizationRepository.findAll(ids);
	}

	@Override
	public Page<Organization> findByIsPlatformHold(int pageNo, int pageSize, boolean governing, String id,
                                                   List<String> customerSource,List<String>  headOfSales, String organizationType, String retrievalCondition, String retrievalInformation,String surplusDateSort,String status) throws TRSException {
		Sort sort = null;
		if (StringUtils.isBlank(surplusDateSort)){
			//默认排序（机构创建日期降序排，即最新创建的在上面）
			sort = new Sort(Direction.DESC, "createdTime");
		}else if ("desc".equals(surplusDateSort)){
			//剩余有效期 降序排(即 到期字段降序排)
			sort = new Sort(Direction.DESC, "expireAt");
		}else if ("asc".equals(surplusDateSort)){
			//剩余有效期 升序排(即 到期字段升序排)
			sort = new Sort(Direction.ASC, "expireAt");
		}else {
			throw new TRSException(CodeUtils.FAIL, "请输入正确的排序方式！");
		}

		//Sort sort = new Sort(Direction.DESC, "createdTime");
		PageRequest pageable = new PageRequest(pageNo, pageSize, sort);
		User user = userService.findById(id);
		if (user != null) {
			Set<Organization> organizations = user.getOrganizations();
			// 自定义,trs包的Criteria 一直报空指针，不明白为啥，所以用原生的写
			Specification<Organization> criteria = new Specification<Organization>() {
				@Override
				public Predicate toPredicate(Root<Organization> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					List<Predicate> predicate = new ArrayList<>();
					if (ObjectUtil.isNotEmpty(customerSource)) {
                        In<String> in = cb.in(root.get("customerSource").as(String.class));
                        for (int i = 0; i < customerSource.size(); i++) {
                            in.value(customerSource.get(i));
                        }
                        predicate.add(in);
					}
                    if (ObjectUtil.isNotEmpty(headOfSales)) {
                        In<String> in = cb.in(root.get("headOfSales").as(String.class));
                        for (int i = 0; i < headOfSales.size(); i++) {
                            in.value(headOfSales.get(i));
                        }
                        predicate.add(in);
                    }
					if (StringUtils.isNotBlank(organizationType)) {
						predicate.add(cb.equal(root.get("organizationType"), organizationType));
					}
                    if (StringUtils.isNotBlank(status)) {
                        predicate.add(cb.equal(root.get("status"), status));
                    }

                    if (StringUtils.isNotBlank(retrievalInformation) && StringUtils.isNotBlank(retrievalCondition)) {
                        if ("userName".equals(retrievalCondition)) {
                            Specification<User> criteriaUser = new Specification<User>() {
                                @Override
                                public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                                    List<Predicate> predicateUser = new ArrayList<>();
                                    if (StringUtils.isNotBlank(retrievalInformation) && StringUtils.isNotBlank(retrievalCondition)) {
                                        predicateUser.add(cb.like(root.get(retrievalCondition), "%" + retrievalInformation + "%"));
                                    }
                                    Predicate[] pre = new Predicate[predicateUser.size()];
                                    return query.where(predicateUser.toArray(pre)).getRestriction();
                                }
                            };
                            List<User> users = userService.findAll(criteriaUser);
                            In<String> in = cb.in(root.get("id").as(String.class));
                            for (User user : users) {
                                in.value(user.getOrganizationId());
                            }
                            predicate.add(in);

                        }else {

                            predicate.add(cb.like(root.get(retrievalCondition), "%" + retrievalInformation + "%"));
                        }
					//	predicate.add(cb.like(root.get(retrievalCondition), "%" + retrievalInformation + "%"));
					}

					// 机构
					if (organizations != null && organizations.size() > 0) {
						Set<String> ids = new HashSet<>();
						for (Organization organization : organizations) {
							ids.add(organization.getId());
						}
						if (governing) {
							In<String> in = cb.in(root.get("id").as(String.class));
							for (String id : ids) {
								in.value(id);
							}
							predicate.add(in);
						} else {
							for (String id : ids) {
								predicate.add(cb.notEqual(root.get("id"), id));
							}
						}
					}

					Predicate[] pre = new Predicate[predicate.size()];
					return query.where(predicate.toArray(pre)).getRestriction();
				}
			};
			if (organizations != null && organizations.size() > 0) {
				return addUser(organizationRepository.findAll(criteria, pageable));
			} else {
				if (governing) {
					return null;
				} else {
					return addUser(organizationRepository.findAll(criteria, pageable));
				}
			}
		}
		return null;
	}

	@Override
	public Map<String, Object> getOrganizationRoleANdSameTimeLogin(String organizationId) {
		if (StringUtils.isBlank(organizationId)) {
			organizationId = UserUtils.getUser().getOrganizationId();
		}
		Map<String, Object> map = new HashMap<>();
		map.put("sameTimeLogin", false);
		map.put("role", null);
		if (StringUtils.isBlank(organizationId)) {
			return map;
		}
		Organization organization = organizationRepository.findOne(organizationId);
		if (organization != null) {
		//	map.put("sameTimeLogin", organization.isSameTimeLogin());
			String adminUserId = organization.getAdminUserId();
			User user = userService.findById(adminUserId);
			if (user != null) {
				List<Role> roles = new ArrayList<>(user.getRoles());
				roles.sort(new Comparator<Role>() {
					@Override
					public int compare(Role o1, Role o2) {
						return o1.getButtonName().length()-o2.getButtonName().length();//一个是update.一个是add.避免无序
					}
				});
				map.put("role", roles);
			}
		}
		return map;
	}

	@Override
	public Page<Organization> findByIdIn(Collection<String> ids, Pageable pageable) {
		return organizationRepository.findByIdIn(ids, pageable);
	}

	@Override
	public List<Organization> findAll() {
		return organizationRepository.findAll();
	}

	@Override
	public Collection<Organization> findByOrganizationNameLikeAndIdIn(String organizationName, List<String> ids) {
		return organizationRepository.findByOrganizationNameLikeAndIdIn(organizationName, ids);
	}

/*	@Override
	public Collection<Organization> findBySystemNameLikeAndIdIn(String systemName, List<String> ids) {
		return organizationRepository.findBySystemNameLikeAndIdIn(systemName, ids);
	}*/

	@Override
	public Page<Organization> findByOrganizationNameLikeAndIdIn(String organizationName, List<String> ids,
			Pageable pageable) {
		return organizationRepository.findByOrganizationNameLikeAndIdIn(organizationName, ids, pageable);
	}

	@Override
	public Page<Organization> findByOrganizationNameLike(String organizationName, Pageable pageable) {
		return organizationRepository.findByOrganizationNameLike(organizationName, pageable);
	}

	@Override
	public boolean isOrganizationExistUser(String organizationId, String userId) {
		if (StringUtils.isBlank(userId) || StringUtils.isBlank(organizationId)) {
			return false;
		}
		User user = userService.findById(userId);
		if (user == null) {
			return false;
		}
		return StringUtils.equals(organizationId, user.getOrganizationId());
	}

	@Override
	public List<String> findAllForHeadOfSales() {
		return organizationRepository.findAllForHeadOfSale();
	}

	@Override
	public Page<Organization> findByCriteria(Specification<Organization> criteria, PageRequest pageable) {
		return organizationRepository.findAll(criteria,pageable);
	}

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