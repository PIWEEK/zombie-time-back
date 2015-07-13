package zombietime.interceptor

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import zombietime.domain.Game
import zombietime.domain.User
import zombietime.repository.GameRepository
import zombietime.repository.UserRepository
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptorAdapter

@Slf4j
@Component
class AuthInterceptor extends ChannelInterceptorAdapter {
    @Autowired
    UserRepository userRepository

    @Autowired
    GameRepository gameRepository


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(message)

        // We always let pass Heartbeat, connection and disconnection messages
        if (headers.isHeartbeat() || isDisconnect(headers) || isConnect(headers)) {
            return message
        }

        if (!(isSubscribeCommand(headers) || isSend(headers))) {
            throw new IllegalArgumentException("No permission")
        }

        User user = userRepository.get(headers.getSessionId())

        def destination = headers.getHeader("simpDestination")
        Game game = gameRepository.get(destination.substring(18))

        if ((!user) ||
                (!game) ||
                ((user.gamePassword != game.password))) {
            throw new IllegalArgumentException("No permission")
        }

        if (isSubscribeCommand(headers)) {
            //User join game
            game.players << user
        }

        return message
    }


    private boolean isSubscribeCommand(StompHeaderAccessor headers) {
        StompCommand.SUBSCRIBE == headers.command
    }

    private boolean isDisconnect(StompHeaderAccessor headers) {
        StompCommand.DISCONNECT == headers.command
    }

    private boolean isConnect(StompHeaderAccessor headers) {
        StompCommand.CONNECT == headers.command
    }

    private boolean isSend(StompHeaderAccessor headers) {
        StompCommand.SEND == headers.command
    }


}
