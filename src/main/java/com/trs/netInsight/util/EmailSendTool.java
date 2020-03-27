package com.trs.netInsight.util;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.util.MailSSLSocketFactory;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 邮件类
 * 
 * @author
 * @since 谷泽昊 2017年12月21日
 */
@Data
@Slf4j
public class EmailSendTool {
	
	// 邮箱服务器
	private String host;
	// 这个是你的邮箱用户名
	private String username;
	// 你的邮箱密码
	private String password;

	private String mail_head_name = "this is head of this mail";

	private String mail_head_value = "this is head of this mail";

	private String mail_to;

	private String mail_from;

	private String mail_subject = "this is the subject of this test mail";

	private String mail_body = "this is the mail_body of this test mail";

	private String personalName = "";
	
	public static final String EXPIREAT_EMAIL = "com.trs.netInsight.support.autowork.task.ExpireatEmail";

	public EmailSendTool() {
	}

	public EmailSendTool(String host, String username, String password, String mailto, String subject, String text,
			String name, String head_name, String head_value) {
		this.host = host;
		this.username = username;
		this.mail_from = username;
		this.password = password;
		this.mail_to = mailto;
		this.mail_subject = subject;
		this.mail_body = text;
		this.personalName = name;
		this.mail_head_name = head_name;
		this.mail_head_value = head_value;
	}

	/**
	 * 此段代码用来发送普通电子邮件
	 * 
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 * @throws GeneralSecurityException
	 */
	public boolean send() {
		Properties props = new Properties();
		Authenticator auth = new Email_Autherticator(); // 进行邮件服务器用户认证
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.auth", "true");
		props.put("mail.transport.protocol", "smtp");
		try {
			MailSSLSocketFactory sf = new MailSSLSocketFactory();// ssl设置
			sf.setTrustAllHosts(true);
			props.put("mail.smtp.ssl.enable", "true");
			props.put("mail.smtp.ssl.socketFactory", sf);

			// 最后再补充一下,session.getdefaultinstance和getinstance的区别 :
			// 如果想要同时使用两个帐号发送javamail，比如使用1@a.com发送1#邮件，使用2@a.com发送2#邮件，这时候，
//			你就需要同时创建两个java.mail.Session对象。但是如果你仍然使用Session.getDefaultInstance创建session对象的话，
//			你会发现，第二个username：2@a.com创建的session永远都和第一个一样，这是为什么呢？因为，getDefaultInstance是真正单例模式，
//			而且，里面的username和password属性是final型的，无法更改。所以，你会发现两封email都是由1@a.com发出来的。
//			所以这个时候你要使用javax.mail.Session.getInstance()方法来创建session对象。
			// Session session = Session.getDefaultInstance(props, auth);
			Session session = Session.getInstance(props, auth);
			// 设置session,和邮件服务器进行通讯。
			MimeMessage message = new MimeMessage(session);
			// message.setContent("foobar, "application/x-foobar"); // 设置邮件格式
			message.setContent(mail_body, "text/html;charset=utf-8");
			message.setSubject(mail_subject); // 设置邮件主题
			// message.setText(mail_body); // 设置邮件正文
			message.setHeader(mail_head_name, mail_head_value); // 设置邮件标题

			message.setSentDate(new Date()); // 设置邮件发送日期
			Address address = new InternetAddress(mail_from, personalName);
			message.setFrom(address); // 设置邮件发送者的地址
			Address toAddress = new InternetAddress(mail_to); // 设置邮件接收方的地址
			message.addRecipient(Message.RecipientType.TO, toAddress);
			Transport.send(message); // 发送邮件
			return true;
		} catch (Exception e) {
			log.error("发送邮件失败：" + e);
		}
		return false;
	}

	/**
	 * 用来进行服务器对用户的认证
	 */
	public class Email_Autherticator extends Authenticator {
		public Email_Autherticator() {
			super();
		}

		public Email_Autherticator(String user, String pwd) {
			super();
			username = user;
			password = pwd;
		}

		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(username, password);
		}
	}
}