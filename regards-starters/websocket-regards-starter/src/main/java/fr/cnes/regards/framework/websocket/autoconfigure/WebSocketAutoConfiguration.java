/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.framework.websocket.autoconfigure;

import java.util.List;

import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.jetty.JettyRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * @author msordi
 *
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(WebSocketBehavior.class)
@ConditionalOnProperty(prefix = "regards.websocket", name = "enabled", matchIfMissing = true)
@EnableWebSocketMessageBroker
public class WebSocketAutoConfiguration implements WebSocketMessageBrokerConfigurer {

    /**
     * Buffer size
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * Web socket requests timeout
     */
    private static final long TIMEOUT = 600000;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry pRegistry) {
        // Endpoint to which the client must access to connect to websocket server
        pRegistry.addEndpoint("/wsconnect").setHandshakeHandler(handshakeHandler()).setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry pConfig) {
        // endpoint to which websocket client should listen to get messages
        pConfig.enableSimpleBroker("/topic");
        pConfig.setApplicationDestinationPrefixes("/myApp");

    }

    /**
     *
     * TODO
     *
     * @return DefaultHandshakeHandler
     * @since 1.0-SNAPSHOT
     */
    @Bean
    public DefaultHandshakeHandler handshakeHandler() {

        final WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
        policy.setInputBufferSize(BUFFER_SIZE);
        policy.setIdleTimeout(TIMEOUT);

        return new DefaultHandshakeHandler(new JettyRequestUpgradeStrategy(new WebSocketServerFactory(policy)));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> pArg0) {
        // Not implemented
    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> pArg0) {
        // Not implemented
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration pArg0) {
        // Not implemented
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration pArg0) {
        // Not implemented
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> pArg0) {
        return false;
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration pArg0) {
        // Not implemented
    }
}
