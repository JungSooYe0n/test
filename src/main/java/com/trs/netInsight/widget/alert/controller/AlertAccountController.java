package com.trs.netInsight.widget.alert.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.SubGroup;
import com.trs.netInsight.widget.user.service.IOrganizationService;
import com.trs.netInsight.widget.user.service.ISubGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.alert.entity.AlertAccount;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.alert.entity.repository.AlertAccountRepository;
import com.trs.netInsight.widget.alert.service.IAlertAccountService;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.service.IUserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * 预警账号Controller
 *
 * Create by yan.changjiang on 2017年11月21日
 */
@Slf4j
@RestController
@RequestMapping("/account")
@Api(description = "预警账号接口")
public class AlertAccountController {

	@Autowired
	private IAlertAccountService alertAccountService;

	@Autowired
	private IUserService userService;

	@Autowired
	private AlertAccountRepository alertAccountRepository;

	@Autowired
	private IOrganizationService organizationService;

	@Autowired
	private ISubGroupService subGroupService;

	/**
	 * 预警账号查询
	 * 
	 * @param type
	 *            类型
	 * @param account
	 *            账号
	 * @return
	 * @throws OperationException
	 * @throws TRSException
	 */
	@ApiOperation("预警账号查询")
	@GetMapping("/list")
	@FormatResult
	public Object pageList(
			@ApiParam("页码") @RequestParam(value = "pageNo", required = false, defaultValue = "0") int pageNo,
			@ApiParam("size") @RequestParam(value = "pageSize", required = false, defaultValue = "15") int pageSize,
			@ApiParam("类型 : SMS, EMAIL, WE_CHAT, APP, COMPOSITE") @RequestParam(value = "type", required = false) SendWay type,
			@ApiParam("账号") @RequestParam("account") String account) throws OperationException {
		Page<AlertAccount> pageList = null;
		//防止前端乱输入
		pageSize = pageSize>=1?pageSize:15;
		try {
			// 如果是站内 就查这个机构下所有账号(权限改造后普通用户 站内 按用户分组)
			if (SendWay.SMS.equals(type)) {
				Page<User> pageOrganList = userService.pageOrganListOrSubGroup(pageNo, pageSize);
				if (ObjectUtil.isNotEmpty(pageOrganList) && ObjectUtil.isEmpty(pageOrganList.getContent())) {
					return null;
				}
				return pageOrganList;
			} else {
				pageList = alertAccountService.pageList(pageNo, pageSize, type, account);
				if (ObjectUtil.isNotEmpty(pageList) && ObjectUtil.isEmpty(pageList.getContent())) {
					return null;
				}
			}
		} catch (Exception e) {
			throw new OperationException("预警账号查询失败,message:" + e, e);
		}
		return pageList;

	}

	/**
	 * 返回给前端这个账号还能添加多少条
	 * 
	 * @return
	 */
	@ApiOperation("返回给前端这个账号还能添加多少条")
	@GetMapping("/lastNum")
	@FormatResult
	public Object lastNum() {
		User loginUser = UserUtils.getUser();
		String userId = loginUser.getId();
		String organizationId = loginUser.getOrganizationId();
		int accountNum = 5;
		if (UserUtils.isRoleAdmin()){
			//机构管理员
			Organization organization = organizationService.findById(organizationId);
			accountNum = organization.getAlertAccountNum();
		}else if (UserUtils.isRoleOrdinary(loginUser)){
			SubGroup subGroup = subGroupService.findOne(loginUser.getSubGroupId());
			accountNum = subGroup.getAlertAccountNum();
		}
		//超管没有 预警中心 ，运维还不清楚按多少来（暂时按原来的5）
		List<AlertAccount> findByUserIdAndType = alertAccountService.findByUserIdAndType(userId, SendWay.WE_CHAT);
		int lastNum = accountNum - findByUserIdAndType.size();
		List<AlertAccount> list = alertAccountService.findByUserIdAndType(userId, SendWay.EMAIL);
		int last = accountNum - list.size();
		Map<String, Object> map = new HashMap<>();
		map.put("can",accountNum);
		map.put("weChatLast", lastNum);
		map.put("emailLast", last);
		return map;
	}

	@ApiOperation("自定义微信名")
	@GetMapping("/definedName")
	@FormatResult
	public Object definedNickname(@ApiParam("id") @RequestParam(value = "id", required = true) String id,
			@ApiParam("definedName") @RequestParam(value = "definedName", required = true) String definedName) {
		AlertAccount alertAccount = alertAccountRepository.findOne(id);
		alertAccount.setDefinedName(definedName);
		return alertAccountRepository.save(alertAccount);
	}

	/**
	 * 预警账号添加
	 * 
	 * @param name
	 *            账号名称
	 * @param type
	 *            类型
	 * @param account
	 *            账号
	 * @return
	 * @throws TRSException
	 */
	@ApiOperation("预警账号添加")
	@FormatResult
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Object add(@ApiParam("账号名称") @RequestParam(value = "name", required = false) String name,
			@ApiParam("类型 :短信，邮件，微信，客户端") @RequestParam("type") SendWay type,
			@ApiParam("账号") @RequestParam("account") String account) throws TRSException {
		// 根据预警账号和类型查询 >5就不添加
		User loginUser = UserUtils.getUser();
		String organizationId = loginUser.getOrganizationId();
		int accountNumCan = 5;
		int accountNumHad = 0;
		if (UserUtils.isRoleAdmin()){
			//机构管理员
			Organization organization = organizationService.findById(organizationId);
			accountNumCan = organization.getAlertAccountNum();
		}else if (UserUtils.isRoleOrdinary(loginUser)){
			SubGroup subGroup = subGroupService.findOne(loginUser.getSubGroupId());
			accountNumCan = subGroup.getAlertAccountNum();
		}
		accountNumHad = alertAccountService.getSubGroupAlertAccountCount(loginUser, type);
		if (accountNumHad < accountNumCan) {
			AlertAccount alertAccount = new AlertAccount();
			alertAccount.setName(name);
			alertAccount.setType(type);
			alertAccount.setAccount(account);
			alertAccount.setDelFlag(true);
			try {
				alertAccountService.add(alertAccount);
				return "添加预警账号成功！";
			} catch (Exception e) {
				// log.error("添加预警账号失败：" , e);
				throw new OperationException("添加预警账号失败,message:" + e, e);
			}
		}
		throw new TRSException(CodeUtils.FAIL, "您目前绑定的预警账号已达上限，如需更多，请联系相关运维人员。");

	}

	/**
	 * 预警账号删除
	 * 
	 * @param id
	 *            账号id
	 * @return 执行成功或者失败
	 * @throws OperationException
	 * @throws TRSException
	 */
	@ApiOperation("预警账号删除")
	@FormatResult
	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public Object delete(@ApiParam("账号id") @RequestParam("id") String id) throws OperationException {
		try {
			alertAccountService.delete(id);
			return "删除预警账号成功！";
		} catch (Exception e) {
			throw new OperationException("预警账号删除失败,message:" + e, e);
		}
	}

	/**
	 * 预警账号修改 账号不能修改
	 * 
	 * @param name
	 *            账号名称
	 * @param type
	 *            类型
	 * @param account
	 *            账号
	 * @return Object
	 * @throws OperationException
	 */
	@ApiOperation("预警账号修改")
	@FormatResult
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public Object update(@ApiParam("账号id") @RequestParam("id") String id,
			@ApiParam("账号名称") @RequestParam(value = "name", required = false) String name,
			@ApiParam("类型") @RequestParam(value = "type", required = false) SendWay type,
			@ApiParam("账号") @RequestParam(value = "account", required = false) String account)
			throws OperationException {
		try {
			alertAccountService.update(id, name, type, account);
			return "修改预警账号成功！";
		} catch (Exception e) {
			throw new OperationException("预警账号修改失败,message:" + e, e);
		}
	}
}
