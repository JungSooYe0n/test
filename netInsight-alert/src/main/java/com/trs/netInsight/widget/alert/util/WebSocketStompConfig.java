package com.trs.netInsight.widget.alert.util;

import com.trs.netInsight.widget.alert.service.IAlertService;
import com.trs.netInsight.widget.notice.service.INoticeSendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketStompConfig {
    //这个bean的注册,用于扫描带有@ServerEndpoint的注解成为websocket  ,如果你使用外置的tomcat就不需要该配置文件
    @Bean
    public ServerEndpointExporter serverEndpointExporter()
    {
        return new ServerEndpointExporter();
    }
    //手动注入相关service
    @Autowired
    public void setIAlertService(IAlertService alertService){
        WebSocketAlertUtil.alertService = alertService;
    }

}
