package com.trs.netInsight.support.autowork.task;

import com.trs.netInsight.support.autowork.exception.JobException;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.config.helper.ConfigConst;
import com.trs.netInsight.widget.config.helper.ConfigHelper;
import com.trs.netInsight.widget.notice.service.IMailSendService;
import com.trs.netInsight.widget.user.entity.Organization;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.user.repository.OrganizationRepository;
import com.trs.netInsight.widget.user.repository.UserRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpireatEmail extends AbstractTask {

	@Value("${email.service.platform}")
	private String platform;

	@Autowired
	public UserRepository userRepository;
	@Autowired
	public IMailSendService mailSendService;
	@Autowired
	private OrganizationRepository organizationRepository;

	/**
	 * 模板
	 */
	private static final String EXPIREAT_TEMPLATE = "expireat.ftl";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// 去用户表查询到期天数小于等于5的
		List<User> expireatList = userRepository.findByExpireatNot();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (User user : expireatList) {
			System.err.println("～～～～～～～～～～～～～～过期提醒能查到用户信息～～～～～～～～～～～～～～～～～");
			int n = -1;
			try {
				Date dateExpire = sdf.parse(user.getExpireAt());
				// 计算今天与到期时间差多少
				n = DateUtil.daysBetween(dateExpire);
			} catch (ParseException e) {
				throw new JobException(e);
			}
//			if (n == 10 || n == 5 || n == 3 || n == 2 || n == 1) {
			if (n < 10) {
				System.err.println("～～～～～～～～～～～～～～过期提醒满足发送条件～～～～～～～～～～～～～～～～～");
				// 机构名
				Organization organization = organizationRepository.findOne(user.getOrganizationId());
				String organizationName = organization.getOrganizationName();
				String email = user.getEmail();
				// 平台加上用户邮箱
				String configValueByKey = ConfigHelper.getConfigValueByKey(ConfigConst.EMAIL_SERVICES_PLATFORM);
				String receivers = null;
				if (StringUtil.isNotEmpty(configValueByKey) ){
					receivers = configValueByKey.endsWith(";") ? configValueByKey + user.getEmail()
							: configValueByKey + ";" + user.getEmail();

				}else if (StringUtil.isNotEmpty(platform)){
					receivers = platform.endsWith(";") ? platform + user.getEmail()
							: platform + ";" + user.getEmail();
				}
				//String configPlatform = ConfigHelper.getConfigValue(ConfigConst.EMAIL_SERVICES_PLATFORM, platform);

				// String receivers = platform;
				Map<String, Object> map = new HashMap<>();
				map.put("username", user.getUserName());
				map.put("organizationName", organizationName);
				map.put("displayName", user.getDisplayName());// 昵称
				String phone = user.getPhone();
				if (StringUtil.isNotEmpty(phone)) {
					map.put("phone", phone);
				} else {
					map.put("phone", 0);
				}
				map.put("email", email);
				map.put("expireat", user.getExpireAt());
				map.put("n", n);
				try {
					System.err.println("～～～～～～～～～～～～～～过期提醒能去发送，收件人："+receivers+"~~~~~~~~~~~~~~");
					mailSendService.sendEmail(EXPIREAT_TEMPLATE, "网察账号到期预警", map, receivers);
				} catch (Exception e) {
					System.err.println("～～～～～～～～～～～～～～过期提醒发送失败，收件人："+receivers+"~~~~~~~~~~~~~~");
					throw new JobException(e);
				}
			}
		}
	}

	@Override
	protected void before() {

	}

	@Override
	protected void after() {

	}

}
