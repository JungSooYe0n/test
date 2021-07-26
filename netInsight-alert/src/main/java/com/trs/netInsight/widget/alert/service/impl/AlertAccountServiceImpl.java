package com.trs.netInsight.widget.alert.service.impl;

import java.util.List;

import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.widget.user.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.trs.netInsight.util.UserUtils;
import com.trs.netInsight.widget.alert.entity.AlertAccount;
import com.trs.netInsight.widget.alert.entity.enums.SendWay;
import com.trs.netInsight.widget.alert.entity.repository.AlertAccountRepository;
import com.trs.netInsight.widget.alert.service.IAlertAccountService;
import com.trs.netInsight.widget.user.entity.User;
/**
 * 预警账号接口实现类
 *
 * Create by yan.changjiang on 2017年11月20日
 */
@Service
public class AlertAccountServiceImpl implements IAlertAccountService {

	@Autowired
	private AlertAccountRepository alertAccountRepository;

	@Autowired
	private UserRepository userRepository;


	@Override
	public AlertAccount add(AlertAccount alertAccount) {
		return alertAccountRepository.save(alertAccount);
	}

	@Override
	public void delete(String countID) {
		alertAccountRepository.delete(countID);
	}

	@Override
	public AlertAccount update(String id, String name, SendWay type, String account) {
		AlertAccount result = alertAccountRepository.findOne(id);
		if (!SendWay.WE_CHAT.equals(type)) {
			result.setAccount(account);
		}
		result.setName(name);
		result.setType(type);
		return alertAccountRepository.save(result);
	}

	@Override
	public void delete(AlertAccount alertAccount) {
		alertAccountRepository.delete(alertAccount);

	}

	@Override
	public void delete(List<AlertAccount> list) {
		alertAccountRepository.delete(list);
		alertAccountRepository.flush();
	}

	@Override
	public AlertAccount findByAccountAndUserIdAndType(String fromUserName, String id, SendWay weChat) {
		List<AlertAccount> list = alertAccountRepository.findByAccountAndUserIdAndType(fromUserName, id, weChat);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public AlertAccount findByAccountAndSubGroupIdAndType(String fromUserName, String id, SendWay weChat) {
		List<AlertAccount> list = alertAccountRepository.findByAccountAndSubGroupIdAndType(fromUserName, id, weChat);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public List<AlertAccount> findByAccount(String account) {
		List<AlertAccount> findByCount = alertAccountRepository.findByAccount(account);
		return findByCount;
	}

	@Override
	public List<AlertAccount> findByUserIdAndType(String id, SendWay weChat) {
		List<AlertAccount> accountList = alertAccountRepository.findByUserIdAndType(id, weChat);
//		for(AlertAccount account:accountList){
////			account.setName(userService.findById(account.getUserId()).getUserName());
//			account.setName(account.getAccount());
//		}
		return accountList;
	}

	@Override
	public List<AlertAccount> findByUserIdAndAccount(String id, String account) {
		List<AlertAccount> accountList = alertAccountRepository.findByUserIdAndAccount(id, account);
		return accountList;
	}

	@Override
	public List<AlertAccount> findByUserAndType(User user, SendWay type) {
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			//这些角色 按 用户id 查询
			return alertAccountRepository.findByUserIdAndType(user.getId(), type);

		}else {
			//return alertAccountRepository.findBySubGroupIdAndType(user.getSubGroupId(), type);
			return alertAccountRepository.findByUserIdAndType(user.getId(), type);
		}
	}


	@Override
	public Page<AlertAccount> pageList(int pageNo, int pageSize, SendWay type, String account) {
		Sort sort = new Sort(Direction.DESC, "createdTime");
		Pageable pageable = new PageRequest(pageNo, pageSize, sort);
		String id = UserUtils.getUser().getId();
		User loginUser = UserUtils.getUser();
		if (StringUtils.isNotBlank(String.valueOf(type)) && StringUtils.isNotBlank(account)) {
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				return changUserAccount(
						alertAccountRepository.findByTypeAndAccountContainingAndUserId(type, account, id, pageable));
			}else {
				return changUserAccount(
						alertAccountRepository.findByTypeAndAccountContainingAndSubGroupId(type, account, loginUser.getSubGroupId(), pageable));
			}
		}
		if (StringUtils.isNotBlank(String.valueOf(type)) && StringUtils.isBlank(account)) {
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				return changUserAccount(alertAccountRepository.findByTypeAndUserId(type, id, pageable));
			}else {
				//return changUserAccount(alertAccountRepository.findByTypeAndSubGroupId(type, loginUser.getSubGroupId(), pageable));
				return changUserAccount(alertAccountRepository.findByTypeAndUserId(type, id, pageable));
			}
		}
		if (StringUtils.isBlank(String.valueOf(type)) && StringUtils.isNotBlank(account)) {
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				return changUserAccount(alertAccountRepository.findByAccountContainingAndUserId(account, id, pageable));
			}else {
				return changUserAccount(alertAccountRepository.findByAccountContainingAndSubGroupId(account, loginUser.getSubGroupId(), pageable));
			}
		}
		if (StringUtils.isBlank(String.valueOf(type)) && StringUtils.isBlank(account)) {
			if (UserUtils.ROLE_LIST.contains(loginUser.getCheckRole())){
				return changUserAccount(alertAccountRepository.findByUserId(id, pageable));
			}else {
				return changUserAccount(alertAccountRepository.findBySubGroupId(loginUser.getSubGroupId(), pageable));
			}
		}
		return null;
	}

	/**
	 * 将userid变成userAccount
	 * 
	 * @date Created at 2018年3月20日 下午10:12:58
	 * @Author 谷泽昊
	 * @param page
	 * @return
	 */
	private Page<AlertAccount> changUserAccount(Page<AlertAccount> page) {
		if (page != null) {
			List<AlertAccount> content = page.getContent();
			if (content != null && content.size() > 0) {
				for (AlertAccount alertAccount : content) {
					String userId = alertAccount.getUserId();
					User user = userRepository.findOne(userId);
					if (user != null) {
						alertAccount.setUserAccount(user.getUserName());
					}
				}
				return page;
			}
		}
		return null;
	}

	@Override
	public List<AlertAccount> findByUserAccountAndType(String userName, SendWay weChat) {
		return alertAccountRepository.findByUserAccountAndType(userName, weChat);
	}

	@Override
	public AlertAccount findByUserAccountAndUserIdAndType(String fromUserName, String id, SendWay weChat) {
		List<AlertAccount> list = alertAccountRepository.findByUserAccountAndUserIdAndType(fromUserName, id, weChat);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public List<AlertAccount> findByAccountAndType(String account, SendWay send) {
		return alertAccountRepository.findByAccountAndType(account, send);
	}

	@Override
	public List<AlertAccount> findByUserId(String userId) {
		return alertAccountRepository.findByUserId(userId,new Sort(Sort.Direction.DESC, "createdTime"));
	}

	@Override
	public int getSubGroupAlertAccountCount(User user, SendWay type) {
		if (UserUtils.ROLE_LIST.contains(user.getCheckRole())){
			//这些角色 按 用户id 查询
			List<AlertAccount> accounts = alertAccountRepository.findByUserIdAndType(user.getId(), type);
			if (ObjectUtil.isNotEmpty(accounts)){
				return accounts.size();
			}
		}else {
			List<AlertAccount> alertAccounts = alertAccountRepository.findBySubGroupIdAndType(user.getSubGroupId(), type);
			if (ObjectUtil.isNotEmpty(alertAccounts)){
				return alertAccounts.size();
			}
		}
		return 0;

	}

	@Override
	public int getSubGroupAlertAccountCountForSubGroup(String subGroupId, SendWay type) {
		List<AlertAccount> bySubGroupId = alertAccountRepository.findBySubGroupIdAndType(subGroupId, type);
		if (ObjectUtil.isNotEmpty(bySubGroupId)){
			return bySubGroupId.size();
		}
		return 0;
	}

	@Override
	public void updateAll(List<AlertAccount> alertAccounts) {
		alertAccountRepository.save(alertAccounts);
		alertAccountRepository.flush();
	}

}
