package zombietime.service

import groovy.json.JsonOutput
import zombietime.domain.Game
import zombietime.domain.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import zombietime.domain.Status
import zombietime.domain.SurvivorStatus
import zombietime.domain.User
import zombietime.utils.MessageType

@Service
class MessageService {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate

    public void sendMessage(Message message) {
        simpMessagingTemplate.convertAndSend("/topic/zombietime_${message.game}".toString(), message)
    }


    public void sendChatMessage(Game game, User user, String survivor, String text) {
        sendMessage(new Message(
                game: game.id,
                user: user.username,
                type: MessageType.CHAT,
                data: ['text': text, 'survivor': survivor]
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


    void sendFullGameMessage(Map data) {
        sendMessage(new Message(
                game: data.gameId,
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

    void sendFindItemsMessage(Game game, SurvivorStatus survivor, List<Status> items, String token = '') {
        sendMessage(new Message(
                game: game.id,
                user: survivor.player.username,
                type: MessageType.FIND_ITEM,
                data: [items: items*.asMap(), survivor: survivor.survivor.slug, token: token]
        ))
    }

    void sendMoveAnimationMessage(Game game, SurvivorStatus survivor, Integer start, Integer end) {
        sendMessage(new Message(
                game: game.id,
                user: survivor.player.username,
                type: MessageType.ANIMATION_MOVE,
                data: [
                        survivor: survivor.survivor.slug,
                        start   : start,
                        end     : end
                ]
        ))
    }

    void sendAtackAnimationMessage(Game game, SurvivorStatus survivor, Integer deaths) {
        sendMessage(new Message(
                game: game.id,
                user: survivor.player.username,
                type: MessageType.ANIMATION_ATTACK,
                data: [
                        survivor: survivor.survivor.slug,
                        weapon  : survivor.weapon.slug,
                        deaths  : deaths
                ]
        ))
    }

    void sendZombieAttackMessage(Game game, SurvivorStatus survivor, Integer damage, Boolean death) {
        sendMessage(new Message(
                game: game.id,
                user: survivor.player.username,
                type: MessageType.ZOMBIE_ATTACK,
                data: [
                        survivor: survivor.survivor.slug,
                        damage  : damage,
                        death   : death
                ]
        ))
    }

    void sendStartTurnMessage(Game game, SurvivorStatus survivor) {
        sendMessage(new Message(
                game: game.id,
                user: survivor.player.username,
                type: MessageType.START_TURN,
                data: [
                        survivor: survivor.survivor.slug
                ]
        ))
    }


    void sendEndGameMessage(Game game, Boolean win, List missions = []) {
        sendMessage(new Message(
                game: game.id,
                user: '',
                type: MessageType.END_GAME,
                data: [
                        win     : win,
                        missions: missions
                ]
        ))
    }


    void sendZombieTimeMessage(Game game, List damages, Integer numNewZombies) {
        sendMessage(new Message(
                game: game.id,
                user: '',
                type: MessageType.ZOMBIE_TIME,
                data: [damages: damages, numNewZombies: numNewZombies]
        ))
    }


    void sendConnectMessage(Map data) {
        Timer timer = new Timer()
        TimerTask action = new TimerTask() {
            public void run() {
                sendPreGameMessage(data)
            }
        }

        timer.schedule(action, 2000)
    }


    void sendPreGameMessage(Map data) {
        sendMessage(new Message(
                game: data.gameId,
                user: '',
                type: MessageType.PRE_GAME,
                data: data
        ))
    }


}
