package zombietime.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import zombietime.domain.DefenseStatus
import zombietime.domain.Game
import zombietime.domain.Message
import zombietime.domain.Point
import zombietime.domain.SurvivorStatus
import zombietime.domain.Tile
import zombietime.domain.User
import zombietime.domain.WeaponStatus
import zombietime.domain.ZombieStatus
import zombietime.repository.DefenseRepository
import zombietime.repository.GameRepository
import zombietime.repository.ItemRepository
import zombietime.repository.WeaponRepository
import zombietime.repository.SurvivorRepository
import zombietime.repository.TileRepository
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
    private WeaponRepository weaponRepository


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
                case MessageType.GET_OBJECT:
                    processGetObjectMessage(game, user, message.data)
                    return
                case MessageType.DISCARD_OBJECT:
                    processDiscardObjectMessage(game, user, message.data)
                    return
                case MessageType.EQUIP:
                    processEquipObjectMessage(game, user, message.data)
                    return
                case MessageType.UNEQUIP:
                    processUnEquipObjectMessage(game, user, message.data)
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
            _sendFullGameMessage(game)
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


            Integer width = game.getWidth()

            Integer startFlatPoint = survivor.point.getFlatPoint(width)
            Integer endFlatPoint = Integer.parseInt(data.point)

            Point startPoint = survivor.point
            Point endPoint = Point.getPointFromFlatPoint(endFlatPoint, width)



            if (_canMove(game, startPoint, endPoint, startFlatPoint, endFlatPoint)) {
                survivor.remainingActions--
                messageService.sendMoveAnimationMessage(game, survivor.id, startFlatPoint, endFlatPoint)
                survivor.point = endPoint
                _sendFullGameMessage(game)
            }


        }
    }

    void processSearchMessage(Game game, User player, Map data) {
        def survivor = _getPlayerCurrentSurvivor(game, player)
        if (game.hasStarted &&
                game.playerTurn == player &&
                survivor.remainingActions > 0 &&
                survivor.inventory.size() < survivor.remainingInventory
        ) {
            Point startPoint = survivor.point

            if (_canSearch(game, startPoint)) {
                survivor.remainingActions--
                game.missionStatus.remainingObjects = game.missionStatus.remainingObjects.sort { Math.random() }
                def item = game.missionStatus.remainingObjects.first()
                game.token = UUID.randomUUID()
                _sendFullGameMessage(game)
                messageService.sendFindItemsMessage(game, survivor.player, [item], game.token)
            }
        }
    }

    void processSearchMoreMessage(Game game, User player, Map data) {
        def survivor = _getPlayerCurrentSurvivor(game, player)
        if (game.hasStarted &&
                game.playerTurn == player &&
                game.token == data.token
        ) {
            Point startPoint = survivor.point

            if (_canSearch(game, startPoint)) {
                def item1 = game.missionStatus.remainingObjects[0]
                def item2 = game.missionStatus.remainingObjects[1]
                messageService.sendFindItemsMessage(game, survivor.player, [item1, item2])
                game.token = ''
            }
        }
    }


    void processGetObjectMessage(Game game, User player, Map data) {
        def survivor = _getPlayerCurrentSurvivor(game, player)
        if (game.hasStarted &&
                game.playerTurn == player
        ) {
            def item1 = game.missionStatus.remainingObjects[0]
            def item2 = game.missionStatus.remainingObjects[1]

            game.missionStatus.remainingObjects.remove(item1)
            game.missionStatus.remainingObjects.remove(item2)
            if (data.item == item1.id) {
                survivor.inventory << item1
                game.missionStatus.remainingObjects << item2

            } else if (data.item == item2.id) {
                survivor.inventory << item2
                game.missionStatus.remainingObjects << item1
            }
            _sendFullGameMessage(game)
        }
    }

    void processDiscardObjectMessage(Game game, User player, Map data) {
        def survivor = _getPlayerCurrentSurvivor(game, player)
        if (game.hasStarted &&
                game.playerTurn == player
        ) {
            def item = survivor.inventory.find { it.id == data.item }
            survivor.inventory.remove(item)
            game.missionStatus.remainingObjects << item
            _sendFullGameMessage(game)
        }
    }

    void processEquipObjectMessage(Game game, User player, Map data) {
        def survivor = _getPlayerCurrentSurvivor(game, player)
        if (game.hasStarted &&
                game.playerTurn == player
        ) {
            def item = survivor.inventory.find { it.id == data.item }
            if (item instanceof WeaponStatus || item instanceof DefenseStatus) {
                survivor.inventory.remove(item)
                if (item instanceof WeaponStatus) {
                    if (survivor.weapon && survivor.weapon.weapon.slug != 'fist') {
                        survivor.inventory << survivor.weapon
                    }
                    survivor.weapon = item
                }

                if (item instanceof DefenseStatus) {
                    if (survivor.defense) {
                        survivor.inventory << survivor.defense
                    }
                    survivor.defense = item
                }

                _sendFullGameMessage(game)
            }
        }
    }


    void processUnEquipObjectMessage(Game game, User player, Map data) {
        def survivor = _getPlayerCurrentSurvivor(game, player)
        if (game.hasStarted &&
                game.playerTurn == player
        ) {
            if (survivor.inventory.size() < survivor.remainingInventory) {
                if (data.item == survivor.weapon?.id) {
                    survivor.inventory << survivor.weapon
                    survivor.weapon = weaponRepository.get('fist').createStatus()
                }
                if (data.item == survivor.defense?.id) {
                    survivor.inventory << survivor.defense
                    survivor.defense = null
                }
                _sendFullGameMessage(game)
            }
        }
    }

    boolean _canSearch(Game game, Point point) {
        Integer flatPoint = point.getFlatPoint(game.getWidth())

        //If there is zombies on the player position, can't search
        if (_zombiesOnFlatPoint(game, flatPoint)) {
            return false
        }

        Tile tile = _getTileFromFlatPoint(game, flatPoint)
        return tile.search
    }


    boolean _canMove(Game game, Integer startFlatPoint, Integer endFlatPoint) {
        Point startPoint = Point.getPointFromFlatPoint(startFlatPoint, game.getWidth())
        Point endPoint = Point.getPointFromFlatPoint(endFlatPoint, game.getWidth())

        return _canMove(game, startPoint, endPoint, startFlatPoint, endFlatPoint)
    }

    boolean _canMove(Game game, Point startPoint, Point endPoint, Integer startFlatPoint, Integer endFlatPoint) {

        //If there is zombies on the player position, can't move
        if (_zombiesOnFlatPoint(game, startFlatPoint)) {
            return false
        }

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
            element = weaponRepository.get(slug)
        }
        if (!element) {
            element = itemRepository.get(slug)
        }

        return element
    }


    List<ZombieStatus> _zombiesOnFlatPoint(Game game, Integer flatPoint) {
        return game.missionStatus.zombies.findAll { it.point.getFlatPoint(game.width) == flatPoint }
    }

    List<SurvivorStatus> _survivorsOnFlatPoint(Game game, Integer flatPoint) {
        return game.missionStatus.survivors.findAll { it.point.getFlatPoint(game.width) == flatPoint }
    }


    List<Integer> _reacheableFlatPoints(Game game, Integer startPoint, int numMoves) {
        if (numMoves > 0) {
            def reacheablePoints = [
                    _getFlatPointUp(game, startPoint),
                    _getFlatPointLeft(game, startPoint),
                    _getFlatPointDown(game, startPoint),
                    _getFlatPointRight(game, startPoint)
            ]
            reacheablePoints = reacheablePoints.findAll {
                (it != -1) && _canMove(game, startPoint, it)
            }
            def finalPoints = []
            finalPoints.addAll(reacheablePoints)

            reacheablePoints.each {
                finalPoints.addAll(_reacheableFlatPoints(game, it, numMoves - 1))
            }

            return finalPoints.unique() - startPoint
        }
        return []


    }

    List<Integer> _attackableFlatPoints(Game game, Integer startPoint, boolean longRange) {
        def attackableFlatPoints = []
        if (_zombiesOnFlatPoint(game, startPoint)) {
            attackableFlatPoints << startPoint
        }

        if (longRange) {
            def points = [
                    _getFlatPointUp(game, startPoint),
                    _getFlatPointLeft(game, startPoint),
                    _getFlatPointDown(game, startPoint),
                    _getFlatPointRight(game, startPoint)
            ]
            points.each { p ->
                if (_zombiesOnFlatPoint(game, p)) {
                    attackableFlatPoints << p
                }
            }

        }

        return attackableFlatPoints
    }


    Integer _getFlatPointUp(Game game, Integer startPoint) {
        Integer point = startPoint - game.getWidth()
        return (point >= 0) ? point : -1
    }

    Integer _getFlatPointLeft(Game game, Integer startPoint) {
        if (startPoint % game.getWidth() == 0) {
            return -1
        }
        return startPoint - 1
    }

    Integer _getFlatPointDown(Game game, Integer startPoint) {
        Integer point = startPoint + game.getWidth()
        return (point < game.getNumPoints()) ? point : -1
    }

    Integer _getFlatPointRight(Game game, Integer startPoint) {
        if (startPoint % game.getWidth() == game.getWidth()) {
            return -1
        }
        return startPoint + 1
    }


    void _sendFullGameMessage(Game game) {
        def data = _getFullGameData(game)
        messageService.sendFullGameMessage(data)
    }

    void addUserToGame(User user, Game game) {
        game.players << user
        def data = _getFullGameData(game)
        messageService.sendConnectMessage(data)
    }

    Map _getFullGameData(Game game) {
        def data = game.missionStatus.asMap()
        data.playerTurn = game.playerTurn ? game.playerTurn.username : ''
        data.slots = game.slots
        data.numPlayers = game.players.size()
        data.gameId = game.id

        game.missionStatus.survivors.eachWithIndex { survivor, i ->
            def flatPoint = survivor.point.getFlatPoint(game.getWidth())
            def reacheable = _reacheableFlatPoints(game, flatPoint, survivor.remainingMovement)
            def attackable = _attackableFlatPoints(game, flatPoint, survivor.weapon.weapon.longRange)
            data.survivors[i].canMoveTo = reacheable
            data.survivors[i].canAttackTo = attackable
            data.survivors[i].canSearch = (survivor.inventory.size() < survivor.remainingInventory) && _canSearch(game, survivor.point)
        }
        return data
    }


}
