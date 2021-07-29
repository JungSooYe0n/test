package com.trs.netInsight.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.trs.netInsight.widget.weixin.message.req.ImageMessage;
import com.trs.netInsight.widget.weixin.message.req.VoiceMessage;
import com.trs.netInsight.widget.weixin.message.resp.MusicMessage;
import com.trs.netInsight.widget.weixin.message.resp.NewsMessage;
import com.trs.netInsight.widget.weixin.message.resp.TextMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * 微信消息工具类
 * 
 * @Type WeixinMessageUtil.java
 * @Desc
 * @author 谷泽昊
 * @date 2018年1月24日 下午4:18:45
 * @version
 */
@Slf4j
public class WeixinMessageUtil {
	
	/**
	 * 微信登录标示  有这个标示的情况下 shiro不再对密码再次加密进行对比
	 */
	public static final String WEIXIN_MARK = "weixinLogin";
	
	public static final String WEIXIN_LOGIN_REIDS = "WEIXIN_LOGIN_REIDS";
	
	public static final String WEIXIN_USERNAME = "WEIXINUSERNAME";
	
	public static final String WEIXIN_IP = "WEIXIN_IP";
	
	public static final String WEIXIN_SESSIONID = "WEIXINSESSIONID";
	
	public static final String WEIXIN_TICKET = "WEIXINTICKET";
	
	public static final String WEIXIN_BIND_ALERT = "WEIXINBINDALERT";
	
	/**
	 * 二维码登录确认
	 */
	public static final String LOGIN_CONFIRM_INFO = "即将在浏览器上登录网察，请确认是否本人操作！\n<a href=\""
			+ "http://sunnyxyy.viphk.ngrok.org/"
			+ "netInsight/system/qrcode/loginByQrcode?userName=USERNAME&ip=IP&sessionId=SESSIONID&ticket=TICKET\">我确认登录网察</a>";

	/**
	 * 未绑定，提示绑定
	 */
	public static final String NOT_BIND_INFO = "<a href=\""
			+ "http://sunnyxyy.viphk.ngrok.org/"
			+ "netInsight/weixinbind/bindLogin?openId=OPENID&ticket=Ticket" + "\">点击这里，手动绑定</a>";


	/**
	 * 微信登录未绑定 提示文案
	 */
	public static final String NO_BIND_INFO = "您还没有绑定账号，请按以下步骤绑定账号后扫码登录。\n" +
			"1.使用账户密码登录系统。\n" +
			"2.个人中心进行认证，点击个人信息-个人中心-微信认证-认证，扫描二维码进行认证。\n" +
			"3.扫描二维码后微信认证中提示：已认证。\n" +
			"4.网察首页选择扫码登录。";

	/**
	 * 微信登录成功后， 提示文案 2019-12-30
	 */
	public static final String LOGIN_INFO = "您已登录PLATFORM_INFO。\n" +
			"网察大数据分析平台是拓尔思基于大数据技术和业务深度融合建立的全网信息洞察在线云服务平台。";

	public static final String ALERT_DETAILS_URL = "NETINSIGHT_URL/thymeleaf/alertDetails/ID";
	/**
	 * 关注后提示
	 */
	public static final String SUBSCRIBE_INFO = "感谢您关注TRS网络舆情大数据分析平台。";
	
	/**
	 * 成功解除绑定
	 */
	public static final String UNBIND_INFO = "您已成功解除与（USERNAME）账号的绑定 ";
	
	/**
	 * 成功绑定
	 */
	public static final String BIND_SUCCESS_INFO = "您已成功绑定（ USERNAME）账号，绑定后可收到来自（ USERNAME ）账号的预警信息。";
	/**
	 * 成功绑定
	 */
	public static final String BIND_SUCCESS_INFO_OLD = "您已成功绑定过（ USERNAME）账号。";
	/**
	 * 预警列表
	 */
	public static final String ALERT_LIST_INFO = "您已绑定以下账号：DISPLAYNAME（USERNAME）\t <a href=\"NETINSIGHT_URL/system/weixin/isActive?openId=OPENID&userName=USERNAME&active=ACTIVE\">HANYU</a>";
	/**
	 * 绑定列表
	 */
	public static final String BIND_LIST_INFO = "您已绑定以下账号：DISPLAYNAME（USERNAME） <a href=\"NETINSIGHT_URL/system/weixin/unBind?openId=OPENID&userName=USERNAME\">解除绑定</a>";

	/**
	 * 微信预警通知标题
	 */
	public static final String ALERT_TITLE="关于（SUBJECT）预警信息共SIZE条\\n来自账号：USERNAME\\n预警类型：ALERTTYPE";
	/**
	 * 微信预警单条推送标题
	 */
	public static final String ALERT_TITLE_SINGLE="预警信息单条推送\\n预警标题：SUBJECT\\n正负面：APPRAISE\\n预警类型：ALERTTYPE\\n来自账号：USERNAME";

	/**
	 * 微信预警通知标题(手动未填写标题)
	 */
	public static final String ALERT_TITLE_TWO="预警信息共SIZE条\\n来自账号：USERNAME\\n预警类型：ALERTTYPE";

	// ----------------------------------------------------------------
	/**
	 * 请求消息类型：文本
	 */
	public static final String REQ_MESSAGE_TYPE_TEXT = "text";
	/**
	 * 请求消息类型：图片
	 */
	public static final String REQ_MESSAGE_TYPE_IMAGE = "image";
	/**
	 * 请求消息类型：语音
	 */
	public static final String REQ_MESSAGE_TYPE_VOICE = "voice";
	/**
	 * 请求消息类型：视频
	 */
	public static final String REQ_MESSAGE_TYPE_VIDEO = "video";
	/**
	 * 请求消息类型：地理位置
	 */
	public static final String REQ_MESSAGE_TYPE_LOCATION = "location";
	/**
	 * 请求消息类型：链接
	 */
	public static final String REQ_MESSAGE_TYPE_LINK = "link";
	/**
	 * 请求消息类型：事件推送
	 */
	public static final String REQ_MESSAGE_TYPE_EVENT = "event";

	// ---------------------------------------------------------------

	/**
	 * 事件类型：subscribe(订阅)
	 */
	public static final String EVENT_TYPE_SUBSCRIBE = "subscribe";
	/**
	 * 事件类型：unsubscribe(取消订阅)
	 */
	public static final String EVENT_TYPE_UNSUBSCRIBE = "unsubscribe";
	/**
	 * 事件类型：scan(用户已关注时的扫描带参数的二维码)
	 */
	public static final String EVENT_TYPE_SCAN = "SCAN";
	/**
	 * 事件类型：LOCATION(上报地理位置)
	 */
	public static final String EVENT_TYPE_LOCATION = "LOCATION";
	/**
	 * 事件类型：CLICK(自定义菜单)
	 */
	public static final String EVENT_TYPE_CLICK = "CLICK";
	
	/**
	 * 模板事件回调
	 */
	public static final String EVENT_TYPE_TEMPLATESENDJOBFINISH = "TEMPLATESENDJOBFINISH";
	
	// ------------------------------------------------------------
	/**
	 * 菜单类型：激活预警
	 */
	public static final String MENU_KEY_ALERT_GET = "ALERT";
	/**
	 * 菜单类型：激活预警
	 */
	public static final String MENU_KEY_ALERT_MANAGE = "MANAGE";

	/**
	 * 菜单类型：停用预警
	 */
	public static final String MENU_KEY_ALERT_STOP = "STOP";
	/**
	 * 菜单类型：绑定/解绑
	 */
	public static final String MENU_KEY_ACCOUNT = "ACCOUNT";
	

	// ---------------------------------------------------------------

	/**
	 * 响应消息类型：文本
	 */
	public static final String RESP_MESSAGE_TYPE_TEXT = "text";
	/**
	 * 响应详细类型：图片
	 */
	public static final String RESP_MESSAGE_TYPE_IMAGE = "image";
	/**
	 * 响应消息类型：语音
	 */
	public static final String RESP_MESSAGE_TYPE_VOICE = "voice";
	/**
	 * 响应消息类型：视频
	 */
	public static final String RESP_MESSAGE_TYPE_VIDEO = "video";
	/**
	 * 响应详细类型：音乐
	 */
	public static final String RESP_MESSAGE_TYPE_MUSIC = "music";
	/**
	 * 响应消息类型：图文
	 */
	public static final String RESP_MESSAGE_TYPE_NEWS = "news";

	// ----------------------------------------------------------------
	/**
	 * 二维码类型：登录
	 */
	public static final int QRCODE_TYPE_LOGIN = 0;

	/**
	 * 二维码类型：绑定
	 */
	public static final int QRCODE_TYPE_BIND = 1;
	
	/**
	 * 二维码类型：登录
	 */
	public static final int QRCODE_TYPE_LOGIN_BIND = 2;

	/**
	 * 二维码 -redis
	 */
	public static final String QRCODE_REDIS = "QRCODE_REDIS:";

	/**
	 * 二维码登录状态：已过期
	 */
	public static final int QRCODE_LOGIN_OLD = -1;
	/**
	 * 二维码登录状态：初始
	 */
	public static final int QRCODE_LOGIN_NEW = 0;

	/**
	 * 二维码登录状态：已扫描
	 */
	public static final int QRCODE_LOGIN_SCAN = 1;

	/**
	 * 二维码登录状态：确认
	 */
	public static final int QRCODE_LOGIN_OK = 2;

	/**
	 * 从流中解析出每个节点的内容
	 * 
	 * @date Created at 2018年1月25日 下午4:49:16
	 * @Author 谷泽昊
	 * @param request
	 * @return
	 */
	public static Map<String, String> parseXml(HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();

		try {
			// 从流中获得文档对象
			// 从输入流中获取流对象
			InputStream in = request.getInputStream();

			// 构建SAX阅读器对象
			SAXReader reader = new SAXReader();
			Document doc = reader.read(in);

			// 获得根节点
			Element root = doc.getRootElement();

			// 获取根节点下的所有子节点
			@SuppressWarnings("unchecked")
			List<Element> children = root.elements();

			for (Element e : children) {
				// 遍历每一个节点，并按照节点名--节点值放入map中
				map.put(e.getName(), e.getText());
				log.error("用户发送的消息XML解析为：" + e.getName() + e.getText());
			}

			// 关闭流
			in.close();
			in = null;
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("解析出错：" + e);
			return null;
		} catch (IOException e1) {
			e1.printStackTrace();
			log.error("解析出错：" + e1);
			return null;
		}

		return map;
	}

	/**
	 * 解析微信发来的请求（XML）
	 * 
	 * @param request
	 * @return
	 * @throws DocumentException
	 */
	public static Map<String, String> parseXml(String xml) throws DocumentException {
		// 将解析结果存储在HashMap中
		Map<String, String> map = new HashMap<String, String>();

		SAXReader reader = new SAXReader();
		Document document = reader.read(new StringReader(xml));
		// 得到xml根元素
		Element root = document.getRootElement();
		// 得到根元素的所有子节点
		@SuppressWarnings("unchecked")
		List<Element> elementList = root.elements();

		// 遍历所有子节点
		for (Element e : elementList)
			map.put(e.getName(), e.getText());

		return map;
	}

	/**
	 * 用于扩展节点数据按照<ToUserName><![CDATA[toUser]]></ToUserName>，中间加了CDATA段
	 */
	private static XStream xstream = new XStream(new XppDriver() {
		public HierarchicalStreamWriter createWriter(Writer out) {
			return new PrettyPrintWriter(out) {
				boolean cdata = true;

				@SuppressWarnings("rawtypes")
				public void startNode(String name, Class clazz) {
					super.startNode(name, clazz);
				}

				protected void writeText(QuickWriter writer, String text) {
					if (cdata) {
						writer.write("<![CDATA[");
						writer.write(text);
						writer.write("]]>");
					} else {
						writer.write(text);
					}
				}
			};
		}
	});

	/**
	 * 将文本消息对象转换成XML格式 params:textMessage 文本消息对象 return:xml
	 */
	public static String messageToXml(TextMessage textMessage) {
		xstream.alias("xml", textMessage.getClass());
		return xstream.toXML(textMessage);
	}

	/**
	 * 将图片消息对象转换成XML格式 params:imageMessage return:xml
	 */
	public static String messageToXml(ImageMessage imageMessage) {
		xstream.alias("xml", imageMessage.getClass());
		return xstream.toXML(imageMessage);
	}

	/**
	 * 将语音消息对象转换成XML格式 params:voiceMessage return:xml
	 */
	public static String messageToXml(VoiceMessage voiceMessage) {
		xstream.alias("xml", voiceMessage.getClass());
		return xstream.toXML(voiceMessage);
	}

	// /**
	// * 将视频消息对象转换成XML格式
	// * params:videoMessage
	// * return:xml
	// */
	// public static String messageToXml(VideoMessage videoMessage){
	// xstream.alias("xml",videoMessage.getClass());
	// return xstream.toXML(videoMessage);
	// }

	/**
	 * 将音乐消息对象转换成XML格式 params:musicMessage return:xml
	 */
	public static String messageToXml(MusicMessage musicMessage) {
		xstream.alias("xml", musicMessage.getClass());
		return xstream.toXML(musicMessage);
	}

	/**
	 * 将图文消息对象转换成XML格式 params:newsMessage return:xml
	 */
	public static String messageToXml(NewsMessage newsMessage) {
		xstream.alias("xml", newsMessage.getClass());
		return xstream.toXML(newsMessage);
	}
}
