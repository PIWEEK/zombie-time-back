package zombietime.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import zombietime.domain.Game
import zombietime.domain.Message
import zombietime.domain.Point
import zombietime.domain.Survivor
import zombietime.domain.SurvivorStatus
import zombietime.domain.Tile
import zombietime.domain.User
import zombietime.repository.GameRepository
import zombietime.repository.SurvivorRepository
import zombietime.repository.TileRepository
import zombietime.repository.UserRepository
import zombietime.utils.MessageType

@Service
class GameMessageService {

    @Autowired
    private GameRepository gameRepository

    @Autowired
    private MessageService messageService

    @Autowired
    private SurvivorRepository survivorRepository

    @Autowired
    private TileRepository tileRepository


    void processMessage(Message message, User user) {
        def game = gameRepository.get(message.game)
        if ((game) && ((user in game.players))) {
            switch (message.type) {
                case MessageType.CHAT:
                    processChatMessage(game, user, message.data)
                    return
                case MessageType.SELECT_SURVIVOR:
                    processSelectSurvivorMessage(game, user, message.data)
                    return
                case MessageType.PLAYER_READY:
                    processPlayerReadyMessage(game, user, message.data)
                    return
                case MessageType.MOVE:
                    processMoveMessage(game, user, message.data)
                    return
            }
        }
    }

    void processChatMessage(Game game, User player, Map data) {
        //echo
        messageService.sendChatMessage(game, player, data.text)
    }


    void processSelectSurvivorMessage(Game game, User player, Map data) {
        if (!game.hasStarted) {
            def survivor = survivorRepository.get(data.survivor)
            boolean leader = (data.leader == 'true')
            def survivorStatus = survivor.createStatus()
            survivorStatus.player = player
            survivorStatus.leader = leader


            def oldSurvivor = game.missionStatus.survivors.find { (it.player == player) && (it.leader == leader) }
            game.missionStatus.survivors.remove(oldSurvivor)
            game.missionStatus.survivors << survivorStatus
            messageService.sendFullGameMessage(game)
        }
    }

    void processPlayerReadyMessage(Game game, User player, Map data) {
        if (!game.hasStarted) {
            def playerSurvivors = game.missionStatus.survivors.count { it.player == player }
            if (playerSurvivors == 2) {
                player.ready = true
            }

            if ((game.players.size() == game.slots) &&
                    (game.missionStatus.survivors.count { !it.player.ready } == 0)) {
                messageService.sendStartGameMessage(game)
                game.hasStarted = true
                game.playerTurn = game.players.first()
            }
        }
    }


    void processMoveMessage(Game game, User player, Map data) {
        def survivor = _getPlayerCurrentSurvivor(game, player)
        if (game.hasStarted &&
                game.playerTurn == player &&
                survivor.remainingActions > 0
        ) {

            Integer width = game.missionStatus.mission.mapWidth

            Integer startFlatPoint = survivor.point.getFlatPoint(width)
            Integer endFlatPoint = data.point

            Point startPoint = survivor.point
            Point endPoint = Point.getPointFromFlatPoint(endFlatPoint, width)

            if (_canMove(fame, startPoint, endPoint, startFlatPoint, endFlatPoint)) {
                survivor.remainingActions--
                messageService.sendMoveAnimationMessage(game, survivor.id, startFlatPoint, endFlatPoint)
                survivor.point = endPoint
                messageService.sendFullGameMessage(game)
            }


        }
    }

    boolean _canMove(Game game, Point startPoint, Point endPoint, Integer startFlatPoint, Integer endFlatPoint) {

        Tile startTile = _getTileFromFlatPoint(game, startFlatPoint)
        Tile endTile = _getTileFromFlatPoint(game, endFlatPoint)

        if (startPoint.y == endPoint.y) {
            //LEFT
            if (endPoint.x == startPoint.x - 1) {
                if (startTile.left && endTile.right) {
                    return true
                }
            }
            //RIGHT
            if (endPoint.x == startPoint.x + 1) {
                if (startTile.right && endTile.left) {
                    return true
                }
            }
        }
        if (startPoint.x == endPoint.x) {
            //UP
            if (endPoint.y == startPoint.y - 1) {
                if (startTile.up && endTile.down) {
                    return true
                }
            }
            //DOWN
            if (endPoint.y == startPoint.y + 1) {
                if (startTile.down && endTile.up) {
                    return true
                }
            }
        }
        return false
    }

    Tile _getTileFromFlatPoint(Game game, Integer flatPoint) {
        Integer numTile = game.missionStatus.mission.mapFloorTiles[flatPoint]
        return tileRepository.get(numTile)
    }


    SurvivorStatus _getPlayerCurrentSurvivor(Game game, User player) {
        return game.missionStatus.survivors.find { it.player == player && it.leader == true }
    }


}
