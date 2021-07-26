package com.trs.netInsight.widget.alert.util;

import com.alibaba.fastjson.JSON;
import com.trs.netInsight.handler.exception.TRSException;
import com.trs.netInsight.util.*;
import com.trs.netInsight.widget.alert.entity.AlertWebSocket;
import com.trs.netInsight.widget.alert.service.IAlertService;
import com.trs.netInsight.widget.user.entity.User;
import org.apache.avro.data.Json;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
@Component//扫描普通的pojo-必须有--{userId}
@ServerEndpoint("/websocket")//请求接口
public class WebSocketAlertUtil {

    public static IAlertService alertService;//websocket不在spring bean统一管理，需要在WebSocketStompConfig中手动注入

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static ConcurrentHashMap<String, WebSocketAlertUtil> webSocketMap = new ConcurrentHashMap<String, WebSocketAlertUtil>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据--不同于HttpSession
    private Session WebSocketsession;

    private String uid = "";//用户ID
    /**
     * 连接建立成功调用的方法
     * @param WebSocketsession 创建会话域
     */
    @OnOpen
    public void onOpen(Session WebSocketsession) throws IOException {
        this.WebSocketsession = WebSocketsession;
        User loginUser = UserUtils.getUser();
        this.uid=loginUser.getUserName();
        System.out.println("当前登录账号："+uid);
        /*if (webSocketMap.containsKey(uid)){//判断用户集合是否存在
            webSocketMap.remove(uid);
            webSocketMap.put(uid,this);
        }else{
            webSocketMap.put(uid,this);
        }
        if(alertService.findReceiveAlert(uid)!=null){//主动推送预警弹窗信息
            String message= JSON.toJSONString(alertService.findReceiveAlert(uid));//最新的三条数据
            webSocketMap.get(uid).sendMessage(message);
            alertService.deleteReceiveAlert(uid);//推送后删除相关信息
        }*/
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() throws IOException {
        System.out.println("窗口执行了断开方法");
        webSocketMap.remove(this);  //从map中删除
        WebSocketsession.close();//默认关闭状态1000的，正常断开连接
    }

    /**
     * 收到客户端消息后调用的方法
     * Onmessage只能有一个参数,前台将所有参数拼接后，后台解析
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message) throws IOException {

        System.out.println("接收到用户发来的消息:"+message);
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
     * 发送消息功能-目前只能传String类型数据，传递数据需要Json化
     */
    public void sendMessage(String message) throws IOException {
        this.WebSocketsession.getBasicRemote().sendText(message);
        //this.WebSocketsession.getAsyncRemote().sendText(message);可使用异步进行消息推送
    }
    /**
     * 给某人发消息
     */
    public void sendMessageToUser(String receivers,List<AlertWebSocket> message) throws IOException{
        String[] receiver=receivers.split(";");
        for (String send:receiver) {
            if(webSocketMap.get(send)!=null){//判断当前用户是否在线-在线直接转发
                String sendmessage=JSON.toJSONString(message);
                webSocketMap.get(send).sendMessage(sendmessage);
            }else{//不在线保存数据库-对应数据库alert_websocket表——字段可扩展，根据需要的数据进行添加，下次登录后主动拉取
                for(int i=0;i<message.size();i++){
                        AlertWebSocket alertWebSocket=new AlertWebSocket();
                        alertWebSocket.setReceiveid(send);
                        alertWebSocket.setReceivemessage(message.get(i).getReceivemessage());
                        alertWebSocket.setReceivefrom(message.get(i).getReceivefrom());
                        alertWebSocket.setReceivetime(message.get(i).getReceivetime());
                        alertWebSocket.setReceiveurl(message.get(i).getReceiveurl());
                        alertService.saveReceiveAlert(alertWebSocket);
                }
            }
        }
    }
}
