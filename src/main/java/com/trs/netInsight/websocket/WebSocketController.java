package com.trs.netInsight.websocket;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.trs.netInsight.handler.result.FormatResult;
import com.trs.netInsight.support.fts.util.DateUtil;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.util.RedisUtil;
import com.trs.netInsight.util.WeixinMessageUtil;
import com.trs.netInsight.widget.alert.entity.AlertBackups;
import com.trs.netInsight.widget.alert.entity.AlertEntity;
import com.trs.netInsight.widget.alert.service.IAlertBackupsService;
import com.trs.netInsight.widget.alert.service.IAlertService;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

//@RestController
@Controller
//@RequestMapping("/webSocket")
@Slf4j
public class WebSocketController {
	
	@Autowired
    private SimpMessagingTemplate messagingTemplate;
	
	@Autowired
    private SimpUserRegistry userRegistry;
	
	
	@Autowired
	private IAlertService alerService;
	
	@Autowired
	private IAlertBackupsService alertBackupsService;

//	@RequestMapping("/send")
//	@SendTo("/sendTo")
//	@MessageMapping("/send")//浏览器发送请求通过@messageMapping 映射/welcome 这个地址。
//    @SendTo("/topic/send")//服务器端有消息时,会订阅@SendTo 中的路径的浏览器发送消息。
//	public Object sendWebSocket(Word message){
////		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
////        message.date = df.format(new Date());
//        return message;
//	}
//	
//	@Scheduled(fixedRate = 3000)
//	@SendTo("/topic/callback")
//    public Object callback() throws Exception {
//        // 发现消息
//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        messagingTemplate.convertAndSend("/topic/callback", df.format(new Date()));
//        return "callback";
//    }
	
	 @MessageMapping("/chat") //在springmvc 中可以直接获得principal,principal 中包含当前用户的信息
	 public void handleChat(Principal principal, Word message) {
		 	String name = principal.getName();
		 	log.error("11111111111111111111111111111");
//		 	SimpUser user = userRegistry.getUser("trs_shadmin");
		 	List<Map<String,String>> ftsQuery = new ArrayList<>();
		 	Map<String,String> map = new HashMap<>();
		 	map.put("username", "xiaoying");
		 	map.put("notice", "记录");
		 	ftsQuery.add(map);
            messagingTemplate.convertAndSendToUser(name,
                    "/topic/greetings", ftsQuery);
    }
	 
		@MessageMapping("/checkStatus")
		public void checkStatus(Principal principal, String ticket) {
			String name = principal.getName();
			Map<String,String> map = new HashMap<>();
			//去redis检查登录状态
			String userName = RedisUtil.getString(ticket+WeixinMessageUtil.WEIXIN_USERNAME);
			map.put(WeixinMessageUtil.WEIXIN_USERNAME, userName);
			String ip = RedisUtil.getString(ticket+WeixinMessageUtil.WEIXIN_IP);
			map.put(WeixinMessageUtil.WEIXIN_IP, ip);
			String status = RedisUtil.getString(ticket+String.valueOf(WeixinMessageUtil.QRCODE_TYPE_LOGIN));
			map.put("status", status);
			Iterator<SimpUser> iterator = userRegistry.getUsers().iterator();
			SimpUser next = iterator.next();
			Iterator<SimpSession> iterator2 = next.getSessions().iterator();
			String sessionId = iterator2.next().getId();
			messagingTemplate.convertAndSendToUser(name,
                    "/topic/greetings", map);
	}
	 
	 /**
	  * 那些当时不在线的 等他在线再发
	  * @return
	  * @throws Exception
	  */
//	@Scheduled(fixedRate = 5000)
    public void callback() throws Exception {
		Iterable<AlertBackups> findAll = alertBackupsService.findAll();
		//查没发的 发送成功后存到那个正经的表
		for(AlertBackups relate : findAll){
			String receiver = relate.getReceiver();
			if(ObjectUtil.isNotEmpty(receiver)){
				SimpUser user = userRegistry.getUser(receiver);
				//在线  就发送 并且从这个表删除  添加到另外的表去
				if(ObjectUtil.isNotEmpty(user)){
					//去预警结果表根据id查数据 然后发送这些数据
					List<Map<String,String>> list = new ArrayList<>();
					//为了保证前端取值不变
					Map<String,String> map = new HashMap<>();
					map.put("url", relate.getUrlName());
					map.put("title", relate.getTitle());
					Date urlTime =  relate.getTime();
					SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyyMMddHH);
					map.put("urlTime", sdf.format(urlTime));
					map.put("sid", relate.getSid());
					map.put("source", relate.getGroupName());
					map.put("siteName", relate.getSiteName());
					map.put("groupName", relate.getGroupName());
					list.add(map);
					messagingTemplate.convertAndSendToUser(receiver, "/topic/greetings", list);
					alertBackupsService.delete(relate.getId());
					AlertEntity alert = new AlertEntity();
					alert.setUrlName(relate.getUrlName());
					alert.setTitle(relate.getTitle());
					alert.setTime(urlTime);
					alert.setContent(relate.getContent());
					//alert.setAuthor(relate.geta);
					alert.setSid(relate.getSid());
					alert.setGroupName(relate.getGroupName());
					alert.setUserId(relate.getUserId());
					alert.setAlertRuleBackupsId(relate.getId());
					alert.setOrganizationId(relate.getOrganizationId());
					alerService.save(alert);
				}
			}
		}
    }
	 
}
