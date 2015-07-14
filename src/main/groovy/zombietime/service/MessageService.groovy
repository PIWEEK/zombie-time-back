package zombietime.service

import groovy.json.JsonOutput
import zombietime.domain.Game
import zombietime.domain.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import zombietime.domain.User

@Service
class MessageService {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate

    public void sendMessage(Message message) {
        simpMessagingTemplate.convertAndSend("/topic/zombietime_${message.game}".toString(), message)
    }


    void sendDisconnectMessage(Game game, User user) {
        sendMessage(new Message(
                game: game.id,
                type: 'DISCONNECT',
                data: user.username
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

    void sendFullGameMessage(Game game) {
        def data = JsonOutput.toJson(game.missionStatus.asMap()).toString()
        sendMessage(new Message(
                game: game.id,
                type: "FULL_GAME",
                data: data
        ))
    }

}
