package zombietime.service

import groovy.json.JsonOutput
import zombietime.domain.Game
import zombietime.domain.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import zombietime.domain.Status
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
        data.playerTurn = game.playerTurn ? game.playerTurn.username : ''
        sendMessage(new Message(
                game: game.id,
                user: '',
                type: MessageType.FULL_GAME,
                data: data
        ))
    }

    void sendStartGameMessage(Game game) {
        sendMessage(new Message(
                game: game.id,
                user: '',
                type: MessageType.START_GAME,
                data: [:]
        ))
    }

    void sendFindItemsMessage(Game game, User user, List<Status> items) {
        sendMessage(new Message(
                game: game.id,
                user: user.username,
                type: MessageType.FIND_ITEM,
                data: items*.asMap()
        ))
    }

    void sendMoveAnimationMessage(Game game, String id, Integer start, Integer end) {
        sendMessage(new Message(
                game: game.id,
                user: '',
                type: MessageType.ANIMATION_MOVE,
                data: [
                        id   : id,
                        start: start,
                        end  : end
                ]
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
