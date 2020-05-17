/*
 * Project: netInsight
 * 
 * File Created at 2018年1月30日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.widget.notice.service.impl;

import com.trs.netInsight.handler.exception.OperationException;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.handler.result.Message;
import com.trs.netInsight.util.CodeUtils;
import com.trs.netInsight.util.StringUtil;
import com.trs.netInsight.widget.config.helper.ConfigConst;
import com.trs.netInsight.widget.config.helper.ConfigHelper;
import com.trs.netInsight.widget.notice.service.IMailSendService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

/**
 * 
 * @Type MailSendServiceImpl.java
 * @author 谷泽昊
 * @date 2018年1月30日 下午3:11:36
 * @version
 */
@Slf4j
@Service
public class MailSendServiceImpl implements IMailSendService {

	@Value(value = "${email.services.fromAddress}")
	private String mailSenderName;

	@Value(value = "${email.services.apply.recevice}")
	private String applyRecevice;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private Configuration initConfig;

	/**
	 * 发送邮件接口
	 * 
	 * @param template
	 *            模板名
	 * @param subject
	 *            邮件主题
	 * @param model
	 *            对应模板里边的对象的string
	 * @param receivers
	 *            邮箱
	 * @return
	 * @throws TRSException
	 */
	@Override
	public Message sendEmail(String template, String subject, Map<String, Object> model, String receivers) {
		// 获取当前用户 查找当前用户的所有邮箱
		try {
			String[] res = receivers.split(";");
			for (int i = 0; i < res.length; i++) {
				log.info(res[i]);
			}

			if (Arrays.stream(res).anyMatch(s -> !StringUtil.checkEmail(s)))
				throw new OperationException("含有不合法邮箱");

			template = template.endsWith(".ftl") ? template : template + ".ftl";
			Template tpl = initConfig.getTemplate(template, Locale.CHINA);
			String text = FreeMarkerTemplateUtils.processTemplateIntoString(tpl, model);
			boolean sendMail = this.sendMail(subject, text, res);
			if (sendMail) {
				log.error("邮件发送成功！");
				return Message.getMessage(CodeUtils.SUCCESS, "发送成功！", sendMail);
			}
			return Message.getMessage(CodeUtils.FAIL, "发送失败！", sendMail);
		} catch (TRSException e) {
			return Message.getMessage(CodeUtils.OPERATION_EXCEPTION, "发送邮箱出错，message:" + e, null);
		} catch (Exception e) {
			return Message.getMessage(CodeUtils.OPERATION_EXCEPTION, "发送邮箱出错，message:" + e, null);
		}
	}

	/**
	 * 发送邮件
	 *
	 * @param subject
	 *            主题
	 * @param text
	 *            内容
	 * @param receivers
	 *            接收者
	 * @return true or false
	 */
	@Override
	public boolean sendMail(String subject, String text, String[] receivers) throws Exception {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			helper.setFrom(mailSenderName);// 发送者.
			helper.setTo(receivers);// 接收者.
			helper.setSubject(subject);// 邮件主题.
			helper.setText(text, true);// 邮件内容.整个文件里边的东西都是文件内容
			mailSender.send(mimeMessage);// 发送邮件
		} catch (Exception e) {
			log.error("邮件发送失败, sender: " + mailSenderName + "; email's subject: " + subject + " , " + "email's text:"
					+ text + " , email's exception:" + e);
			throw e;
		}
		return true;
	}

	@Override
	public boolean sendOneMail(String subject, String text, String fromAddress) throws Exception {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			helper.setFrom(mailSenderName);// 发送者.
			// 读取MYSQL配置
			String emailServicesApplyRecevice = ConfigHelper.getConfigValueByKey(ConfigConst.EMAIL_SERVICES_APPLY_RECEVICE);
			//mysql值与配置文件的值合并
			String[] res = null;
			if (StringUtil.isNotEmpty(emailServicesApplyRecevice) ){

				 res = emailServicesApplyRecevice.split(";");

			}else if (StringUtil.isNotEmpty(applyRecevice)){
				res = applyRecevice.split(";");
			}else {
				throw new OperationException("暂无处理人员，请联系本司工作人员！");
			}

			helper.setTo(res);// 接收者.
			helper.setSubject(subject);// 邮件主题.
			helper.setText(text, true);// 邮件内容.整个文件里边的东西都是文件内容
			mailSender.send(mimeMessage);// 发送邮件
			log.info("邮件发送成功，sender: " + mailSenderName + "; email's subject: " + subject + " , " + "email's text:"
					+ text);
		} catch (Exception e) {
			log.error("邮件发送失败, sender: " + mailSenderName + "; email's subject: " + subject + " , " + "email's text:"
					+ text + " , email's exception:" + e);
			throw e;
		}
		return true;
	}
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年1月30日 谷泽昊 creat
 */