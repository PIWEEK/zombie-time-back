package zombietime.event

import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import zombietime.domain.User
import zombietime.repository.UserRepository


class PresenceEventListener implements ApplicationListener<ApplicationEvent> {
    static final String HEADER_USERNAME = 'x-username'
    static final String HEADER_PASSWORD = 'x-password'

    UserRepository userRepository


    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof SessionConnectEvent) {
            handleSessionConnected((SessionConnectEvent) event)
        } else if (event instanceof SessionDisconnectEvent) {
            handleSessionDisconnect((SessionDisconnectEvent) event)
        }
    }

    private void handleSessionConnected(SessionConnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage())


        String username = headers.getFirstNativeHeader(HEADER_USERNAME)
        String gamePassword = headers.getFirstNativeHeader(HEADER_PASSWORD)

        User user = new User(username: username, gamePassword: gamePassword)
        userRepository.create(headers.getSessionId(), user)

    }

    private void handleSessionDisconnect(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage())
        String sessionId = headers.getSessionId()
        String user = userRepository.get(sessionId)
        userRepository.remove(sessionId)

        if (user) {
            //TODO: Remove user from game
        }
    }


}
