package com.trs.netInsight.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer{

	@Override
    public void registerStompEndpoints(StompEndpointRegistry registry) { //endPoint 注册协议节点,并映射指定的URl

        registry.addEndpoint("/websocket").setAllowedOrigins("*").withSockJS();//注册了一个前缀为/websocket的stomp终端，客户端可以使用该url来建立websocket连接。
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {//配置消息代理(message broker)
    	config.enableSimpleBroker("/topic");//这里配置了两个前缀，若是destination以这两个前缀开头，则会转发给该Broker 
    	config.setApplicationDestinationPrefixes("/app");//可以不配  但是配置后  前端访问messagemapping接口就必须有/app作为前缀
    }
    
    @Override
	public void configureWebSocketTransport(final WebSocketTransportRegistration registration) {
		registration.addDecoratorFactory(new WebSocketHandlerDecoratorFactory() {

			@Override
			public WebSocketHandler decorate(WebSocketHandler handler) {
				return new WebSocketHandlerDecorator(handler) {
					@Override
					public void afterConnectionEstablished(final WebSocketSession session) throws Exception {
						String userName = session.getPrincipal().getName();
						log.error("online: " + userName);
						super.afterConnectionEstablished(session);
					}

					@Override
					public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus)
							throws Exception {
//						String userName = session.getPrincipal().getName();
//						log.error("offline: " + userName);
						super.afterConnectionClosed(session, closeStatus);
					}
				};
			}
			
		});
		super.configureWebSocketTransport(registration);
	}
}
