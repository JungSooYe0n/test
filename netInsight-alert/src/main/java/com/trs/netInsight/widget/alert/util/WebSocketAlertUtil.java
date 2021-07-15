package com.trs.netInsight.widget.alert.util;

import com.trs.netInsight.util.RedisUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
@ServerEndpoint("/websocket/{uid}")
@Component
public class WebSocketAlertUtil {

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static ConcurrentHashMap<String, WebSocketAlertUtil> webSocketMap = new ConcurrentHashMap<String, WebSocketAlertUtil>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据--不同于HttpSession
    private Session WebSocketsession;
    //当前登录人员id
    private String uid="";
    /**
     * 连接建立成功调用的方法
     * @param uid 当前登录人的ID
     * @param WebSocketsession 创建会话域
     */
    @OnOpen
    public void onOpen(@PathParam(value = "uid")String uid, Session WebSocketsession) {
        this.WebSocketsession = WebSocketsession;
        if (webSocketMap.containsKey(uid)){
            webSocketMap.remove(uid);
            webSocketMap.put(uid,this);
        }
        webSocketMap.put(uid, this);//加入map中--每次从map中建立会话
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketMap.remove(uid);  //从map中删除
    }

    /**
     * 收到客户端消息后调用的方法
     * Onmessage只能有一个参数,前台将所有参数拼接后，后台解析
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message) throws IOException {
        String uid = message.split("[-]")[1];
        String receiveMessage = message.split("[-]")[0];
        if(webSocketMap.get(uid)!=null){//判断是否在线
            webSocketMap.get(uid).sendMessage(receiveMessage);
        }else{
            //不在线的存redis，等待后续主动拉取
            if(RedisUtil.exist(uid)){
                RedisUtil.deleteKey(uid);
                RedisUtil.setString(uid,receiveMessage);
            }else{
                RedisUtil.setString(uid,receiveMessage);
            }
        }
    }

    /**
     * 发生错误时调用
     * @param error
     */
    @OnError
    public void onError(Throwable error) {
        error.printStackTrace();
    }
    /**
     * 发送消息功能
     */
    public void sendMessage(String message) throws IOException {
        this.WebSocketsession.getBasicRemote().sendText(message);
    }
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }



}
