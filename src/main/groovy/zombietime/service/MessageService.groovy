package zombietime.service

import groovy.json.JsonOutput
import zombietime.domain.Game
import zombietime.domain.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import zombietime.domain.User
import zombietime.utils.MessageType

@Service
class MessageService {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate

    public void sendMessage(Message message) {
        simpMessagingTemplate.convertAndSend("/topic/zombietime_${message.game}".toString(), message)
    }


    public void sendChatMessage(Game game, User user, String text) {
        sendMessage(new Message(
                game: game.id,
                user: user.username,
                type: MessageType.CHAT,
                data: ['text': text]
        ))
    }


    void sendDisconnectMessage(Game game, User user) {
        sendMessage(new Message(
                game: game.id,
                user: user.username,
                type: MessageType.DISCONNECT,
                data: [:]
        ))

    }


    void sendFullGameMessage(Game game) {
        def data = game.missionStatus.asMap()
        sendMessage(new Message(
                game: game.id,
                user: '',
                type: MessageType.FULL_GAME,
                data: data
        ))
    }

    void sendConnectMessage(Game game) {
        Timer timer = new Timer()
        TimerTask action = new TimerTask() {
            public void run() {
                sendFullGameMessage(game)
            }
        }

        timer.schedule(action, 5000)
    }

}
