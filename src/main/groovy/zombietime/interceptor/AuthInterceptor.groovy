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
import zombietime.service.GameEngineService
import zombietime.service.GameService

@Slf4j
@Component
class AuthInterceptor extends ChannelInterceptorAdapter {
    @Autowired
    UserRepository userRepository

    @Autowired
    GameRepository gameRepository

    @Autowired
    GameEngineService gameEngineService


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

        if (!user) {
            throw new IllegalArgumentException("No permission")
        }

        if (isSubscribeCommand(headers)) {
            def destination = headers.getHeader("simpDestination")
            def gameId = destination.substring(18)
            Game game = gameRepository.get(gameId)

            if ((!game) || (user.gamePassword != game.password)) {
                throw new IllegalArgumentException("No permission")
            }

            //User join game
            gameEngineService.addUserToGame(user, game)
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
