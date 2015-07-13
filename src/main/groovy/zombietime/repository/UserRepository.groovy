package zombietime.repository

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import zombietime.domain.User

import java.util.concurrent.ConcurrentHashMap


@Component
class UserRepository {
    ConcurrentHashMap<String, User> sessions = new ConcurrentHashMap<String, User>()


    public void create(String sessionId, User user) {
        sessions.put(sessionId, user)
    }

    public User get(String sessionId) {
        return sessions.get(sessionId)
    }

    public void remove(String sessionId) {
        sessions.remove(sessionId)
    }
}
