/*
 * Project: netInsight
 * 
 * File Created at 2018年1月22日
 * 
 * Copyright 2017 trs Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * TRS Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */
package com.trs.netInsight.util;

import java.util.concurrent.TimeUnit;

import com.trs.netInsight.widget.weixin.entity.AlertTemplateMsg;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import com.alibaba.fastjson.JSONObject;
import com.trs.netInsight.widget.user.entity.User;
import com.trs.netInsight.widget.weixin.entity.menu.Menu;
import com.trs.netInsight.widget.weixin.entity.qrcode.QRCode;
import com.trs.netInsight.widget.weixin.entity.qrcode.Ticket;

import lombok.extern.slf4j.Slf4j;

/**
 * 微信请求工具类
 * @Type WeixinUtil.java
 * @author 谷泽昊
 * @date 2018年1月22日 下午3:38:45
 * @version
 */
@Slf4j
public class WeixinUtil {

	/**
	 * HTTP请求地址:获取access_token （2000 次/天）
	 */
	private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
	/**
	 * 获取用户列表
	 */
	private static final String GET_USER_URL = "https://api.weixin.qq.com/cgi-bin/user/get?access_token=ACCESS_TOKEN&next_openid=NEXT_OPENID";

	/**
	 * 菜单创建（POST） 限100（次/天）
	 */
	public static final String MENU_CREATE_URL = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";

	/**
	 * HTTP请求地址:发送客服消息 （ 500000 次/天）
	 */
	public static final String CUSTOM_MESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=ACCESS_TOKEN";

	/**
	 * HTTP请求地址:发送模板消息 （ 100000 次/天）
	 */
	public static final String TEMPLATE_MESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=ACCESS_TOKEN";

	/**
	 * HTTP请求地址:获取用户基本信息 （ 5000000 次/天）
	 */
	public static final String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID";

	/**
	 * HTTP请求地址:创建二维码
	 */
	public static final String QRCODE_CREATE_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=ACCESS_TOKEN";

	/**
	 * HTTP请求地址:获取二维码
	 */
	public static final String QRCODE_GET_URL = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=TICKET";

	/**
	 * oAuth授权：获取code
	 */
	public static final String OAUTH_GET_CODE = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=snsapi_base&#wechat_redirect";

	/**
	 * oAuth授权：换取access_token
	 */
	public static final String OAUTH_GET_ACCESSTOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=APPSECRET&code=CODE&grant_type=authorization_code";

	/**
	 * redis储存
	 */
	public static final String WEIXINREDISKEY = "WEI_XIN_REDIS_KEY";
	public static Integer counter = 0;

	/**
	 * 	正式环境获取微信token的接口
	 */
	public static final String NETINSIGHR_WECHATTOKEN = "http://www.netinsight.com.cn/netInsight/system/weixin/getWeiXinToken";

	/**
	 * 获取token
	 * 
	 * @date Created at 2018年1月22日 下午3:43:28
	 * @Author 谷泽昊
	 * @return
	 */
	public static String getToken() {
		Environment env = SpringUtil.getBean(Environment.class);
		String appid = env.getProperty("trs.weixin.AppID");
		String secret = env.getProperty("trs.weixin.AppSecret");
		Boolean master = Boolean.parseBoolean(env.getProperty("trs.master"));
		log.error("appid:" + appid);
		log.error("secret:" + secret);
		String format = ACCESS_TOKEN_URL.replace("APPID", appid).replace("APPSECRET", secret);
		//4.0 环境目前在测试上，为不影响3.0微信功能使用，微信的token从3.0获取，4.0上线正式后，将注释去掉
		//String token = RedisUtil.getString(WEIXINREDISKEY);
		String token = null;
		if(master != null && master){
			token = RedisUtil.getString(WEIXINREDISKEY);
		}else{
			token = HttpUtil.doGet(NETINSIGHR_WECHATTOKEN, "utf-8");
		}
		if (StringUtils.isNotBlank(token)) {
			return token;
		}
		String tokenMap = HttpUtil.doGet(format, "utf-8");
		log.error(tokenMap);
		JSONObject parseObject = JSONObject.parseObject(tokenMap);
		token = parseObject.getString("access_token");
		log.error("token:" + token);
		if (StringUtils.isNotBlank(token)) {
			int timeOut = parseObject.getInteger("expires_in");
			RedisUtil.setString(WEIXINREDISKEY, token, timeOut, TimeUnit.SECONDS);
		} else {
			token = parseObject.getString("errcode");
		}
		counter++;
		log.error("调用获取access_token接口，目前调用微信接口第"+counter+"次！返回结果："+tokenMap);
		return token;
	}

	/**
	 * 获取用户列表
	 * 
	 * @date Created at 2018年1月22日 下午4:12:31
	 * @Author 谷泽昊
	 * @param access_token
	 *            调用接口凭证
	 * @param next_openid
	 *            第一个拉取的OPENID，不填默认从头开始拉取
	 * @return
	 */
	public static String getUserList(String access_token, String next_openid) {
		String format = GET_USER_URL.replace("ACCESS_TOKEN", access_token).replace("NEXT_OPENID", next_openid);
		String tokenMap = HttpUtil.doGet(format, "utf-8");
		log.error(tokenMap);
		return tokenMap;
	}

	/**
	 * 创建菜单
	 * 
	 * @date Created at 2018年1月25日 上午10:26:17
	 * @Author 谷泽昊
	 * @param menu
	 *            菜单实例
	 * @param accessToken
	 *            有效的access_token
	 * @return 0表示成功，其他值表示失败
	 */
	public static int createMenu(Menu menu, String accessToken) {
		int result = 0;
		// 拼装创建菜单的url
		String url = MENU_CREATE_URL.replace("ACCESS_TOKEN", accessToken);
		String string = JSONObject.toJSON(menu).toString();
		// 将菜单对象转换成json字符串
		String jsonMenu = HttpUtil.sendPost(url, string);
		// 调用接口创建菜单
		JSONObject jsonObject = JSONObject.parseObject(jsonMenu);
		if (null != jsonObject) {
			if (0 != jsonObject.getInteger("errcode")) {
				result = jsonObject.getInteger("errcode");
				log.error("创建菜单失败 errcode:{} errmsg:{}", jsonObject.getInteger("errcode"),
						jsonObject.getString("errmsg"));
			}
		}
		counter++;
		log.error("调用创建菜单接口，目前调用微信接口第"+counter+"次！");
		return result;
	}

	/**
	 * 获取二维码
	 * 
	 * @date Created at 2018年1月25日 下午1:07:31
	 * @Author 谷泽昊
	 * @param access_token
	 * @param code
	 * @return
	 */
	public static Ticket createQrcode(String access_token, QRCode code) {
		User user = UserUtils.getUser();
		String id = "";
		if(ObjectUtil.isNotEmpty(user)){
			id = user.getId();
		}
//		String id = user.getId();
		String redisKey = "RedisQr:" + id;
		String string = RedisUtil.getString(redisKey);
		if (StringUtils.isNotBlank(string)) {
			Ticket ticket = JSONObject.parseObject(string, Ticket.class);
			return ticket;
		}
		String url = QRCODE_CREATE_URL.replace("ACCESS_TOKEN", access_token);
		String jsonTicket = HttpUtil.sendPost(url, code.toJSON());
		Ticket ticket = JSONObject.parseObject(jsonTicket, Ticket.class);
		RedisUtil.setString(redisKey, jsonTicket, ticket.getExpireSeconds(), TimeUnit.SECONDS);
		counter++;
		log.error("调用获取二维码接口，目前调用微信接口第"+counter+"次！");
		return ticket;
	}

	/**
	 * 获取二维码图片
	 * 
	 * @date Created at 2018年1月25日 下午1:19:49
	 * @Author 谷泽昊
	 * @param ticket
	 * @return
	 */
	public static String showQrcode(String ticket) {
		counter++;
		log.error("调用获取二维码图片接口，目前调用微信接口第"+counter+"次！");
		return QRCODE_GET_URL.replace("TICKET", ticket);
	}

	/**
	 * 获取用户
	 * 
	 * @date Created at 2018年1月26日 下午5:18:25
	 * @Author 谷泽昊
	 * @param access_token
	 * @param openid
	 * @return
	 */
	public static String getUser(String access_token, String openid) {
		String doGet = HttpUtil.doGet(USER_INFO_URL.replace("ACCESS_TOKEN", access_token).replace("OPENID", openid),
				"utf-8");
		counter++;
		log.error("调用获取用户接口，扫描绑定用到。目前调用微信接口第"+counter+"次！");
		return doGet;
	}
	
	/**
	 * 发送模板消息
	 * @date Created at 2018年1月29日  下午5:49:36
	 * @Author 谷泽昊
	 * @param access_token
	 * @param alertTemplateMsg
	 * @return
	 */
	public static String sendWeixin(String access_token, AlertTemplateMsg alertTemplateMsg){
		log.error(alertTemplateMsg.toJson());
		String sendPost = HttpUtil.sendPost(TEMPLATE_MESSAGE_URL.replace("ACCESS_TOKEN", access_token), alertTemplateMsg.toJson());
		log.error("微信发送信息返回："+sendPost);
		JSONObject parseObject = JSONObject.parseObject(sendPost);
		counter++;
		log.error("调用获取信息发送接口，目前调用微信接口第"+counter+"次！返回信息为："+sendPost);
		if (ObjectUtil.isNotEmpty(parseObject)){
			return parseObject.getString("errmsg");
		}else {
			return "微信推送接口无任何返回信息！";
		}
	}
}

/**
 *
 * Revision history
 * -------------------------------------------------------------------------
 * 
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2018年1月22日 谷泽昊 creat
 */