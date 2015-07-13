package zombietime.config

import org.springframework.context.annotation.Bean
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.config.ChannelRegistration
import zombietime.event.PresenceEventListener
import zombietime.interceptor.AuthInterceptor
import zombietime.repository.UserRepository
import zombietime.service.MessageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
    @Autowired
    AuthInterceptor authInterceptor

    @Autowired
    UserRepository userRepository

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic/")
        config.setApplicationDestinationPrefixes("/app")
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/message").withSockJS()
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.setInterceptors(authInterceptor)
    }

    @Bean
    public PresenceEventListener presenceEventListener(SimpMessagingTemplate messagingTemplate) {
        return new PresenceEventListener(userRepository: userRepository)
    }

}
