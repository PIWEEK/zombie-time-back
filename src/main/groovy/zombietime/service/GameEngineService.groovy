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
import zombietime.repository.DefenseRepository
import zombietime.repository.GameRepository
import zombietime.repository.ItemRepository
import zombietime.repository.LongRangeWeaponRepository
import zombietime.repository.ShortRangeWeaponRepository
import zombietime.repository.SurvivorRepository
import zombietime.repository.TileRepository
import zombietime.repository.UserRepository
import zombietime.utils.MessageType

@Service
class GameEngineService {

    @Autowired
    private GameRepository gameRepository

    @Autowired
    private MessageService messageService

    @Autowired
    private SurvivorRepository survivorRepository

    @Autowired
    private TileRepository tileRepository

    @Autowired
    private DefenseRepository defenseRepository

    @Autowired
    private ItemRepository itemRepository

    @Autowired
    private ShortRangeWeaponRepository shortRangeWeaponRepository

    @Autowired
    private LongRangeWeaponRepository longRangeWeaponRepository


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
                case MessageType.SEARCH:
                    processSearchMessage(game, user, message.data)
                    return
                case MessageType.SEARCH_MORE:
                    processSearchMoreMessage(game, user, message.data)
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

            boolean leader = (data.leader == 'true')
            def oldSurvivor = game.missionStatus.survivors.find { (it.player == player) && (it.leader == leader) }
            game.missionStatus.survivors.remove(oldSurvivor)


            def survivor = survivorRepository.get(data.survivor)
            def survivorStatus = survivor.createStatus()
            survivorStatus.player = player
            survivorStatus.leader = leader
            survivorStatus.weapon = _findElementBySlug('fist').createStatus()

            def startPoint = game.missionStatus.mission.startSurvivalPoints[game.players.size() % game.missionStatus.mission.startSurvivalPoints.size()]


            survivorStatus.point.x = startPoint.x
            survivorStatus.point.y = startPoint.y

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
            Integer endFlatPoint = Integer.parseInt(data.point)

            Point startPoint = survivor.point
            Point endPoint = Point.getPointFromFlatPoint(endFlatPoint, width)

            if (_canMove(game, startPoint, endPoint, startFlatPoint, endFlatPoint)) {
                survivor.remainingActions--
                messageService.sendMoveAnimationMessage(game, survivor.id, startFlatPoint, endFlatPoint)
                survivor.point = endPoint
                messageService.sendFullGameMessage(game)
            }


        }
    }

    void processSearchMessage(Game game, User player, Map data) {
        def survivor = _getPlayerCurrentSurvivor(game, player)
        if (game.hasStarted &&
                game.playerTurn == player &&
                survivor.remainingActions > 0
        ) {


            Point startPoint = survivor.point

            if (_canSearch(game, startPoint)) {
                survivor.remainingActions--
                def item = game.missionStatus.remainingObjects.first()
                messageService.sendFindItemsMessage(game, survivor.id, [item])
            }
        }
    }

    void processSearchMoreMessage(Game game, User player, Map data) {
        def survivor = _getPlayerCurrentSurvivor(game, player)
        if (game.hasStarted &&
                game.playerTurn == player
        ) {
            Point startPoint = survivor.point

            if (_canSearch(game, startPoint)) {
                def item1 = game.missionStatus.remainingObjects[0]
                def item2 = game.missionStatus.remainingObjects[1]
                messageService.sendFindItemsMessage(game, survivor.id, [item1, item2])
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


    Object _findElementBySlug(String slug) {
        def element = defenseRepository.get(slug)
        if (!element) {
            element = longRangeWeaponRepository.get(slug)
        }
        if (!element) {
            element = shortRangeWeaponRepository.get(slug)
        }
        if (!element) {
            element = itemRepository.get(slug)
        }

        return element
    }


}
