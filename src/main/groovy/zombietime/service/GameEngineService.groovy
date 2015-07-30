package zombietime.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import zombietime.domain.DefenseStatus
import zombietime.domain.Game
import zombietime.domain.ItemStatus
import zombietime.domain.Message
import zombietime.domain.Noise
import zombietime.domain.PersonalMission
import zombietime.domain.Point
import zombietime.domain.SearchPoint
import zombietime.domain.SurvivorStatus
import zombietime.domain.Tile
import zombietime.domain.User
import zombietime.domain.WeaponStatus
import zombietime.domain.Zombie
import zombietime.domain.ZombieStatus
import zombietime.repository.DefenseRepository
import zombietime.repository.GameRepository
import zombietime.repository.ItemRepository
import zombietime.repository.PersonalMissionRepository
import zombietime.repository.WeaponRepository
import zombietime.repository.SurvivorRepository
import zombietime.repository.TileRepository
import zombietime.repository.ZombieRepository
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

    @Autowired
    private PersonalMissionRepository personalMissionRepository
    @Autowired
    private ZombieRepository zombieRepository


    Random random = new Random()


    void processMessage(Message message, User user) {
        def game = gameRepository.get(message.game)
        if ((game) && (!game.hasFinished) && ((user in game.players))) {
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
                case MessageType.UNEQUIP:
                    processUnEquipObjectMessage(game, user, message.data)
                    return
                case MessageType.ATTACK:
                    processAttackMessage(game, user, message.data)
                    return
                case MessageType.NOISE:
                    processNoiseMessage(game, user, message.data)
                    return
                case MessageType.END_TURN:
                    processEndTurnMessage(game, user, message.data)
                    return
                case MessageType.USE_OBJECT:
                    processUseObjectMessage(game, user, message.data)
                    return
            }
        }
    }

    void processChatMessage(Game game, User player, Map data) {
        SurvivorStatus survivor = _getPlayerCurrentSurvivor(game, player)
        messageService.sendChatMessage(game, player, survivor.survivor.slug, data.text)
    }


    void processSelectSurvivorMessage(Game game, User player, Map data) {
        if (!game.hasStarted) {

            boolean leader = (data.leader == true)
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
            _sendPreGameMessage(game)
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
                    (game.players.count { !it.ready } == 0)) {
                game.hasStarted = true
                game.playerTurn = game.players[random.nextInt(game.players.size())]
                messageService.sendStartGameMessage(game)
                messageService.sendStartTurnMessage(game, _getPlayerCurrentSurvivor(game, game.playerTurn))

                _sendFullGameMessage(game)

                scheduleEndGame(game)
                scheduleZombieTime(game)
            }
        }
    }


    void processMoveMessage(Game game, User player, Map data) {
        def survivor = _getPlayerCurrentSurvivor(game, player)
        int startPoint = survivor.point.getFlatPoint(game.getWidth())
        def reacheables = _reacheableFlatPoints(game, startPoint, survivor.remainingMovement)
        Integer movePoint = Integer.parseInt(data.point)
        if (game.hasStarted &&
                game.playerTurn == player &&
                survivor.remainingActions > 0 &&
                movePoint in reacheables
        ) {
            survivor.remainingActions--
            messageService.sendMoveAnimationMessage(game, survivor, startPoint, movePoint)
            survivor.point = Point.getPointFromFlatPoint(movePoint, game.getWidth())
            _sendFullGameMessage(game)
        }
    }

    void processEndTurnMessage(Game game, User player, Map data) {
        def survivor = _getPlayerCurrentSurvivor(game, player)
        if (game.hasStarted &&
                game.playerTurn == player
        ) {

            def leaderes = game.missionStatus.survivors.findAll { it.leader == true }
            def nextSurvivor = leaderes[(leaderes.indexOf(survivor) + 1) % leaderes.size()]

            survivor.remainingActions = survivor.survivor.actions
            survivor.remainingMovement = survivor.survivor.movement
            survivor.remainingNoise = survivor.survivor.noise


            def (damage, death) = _attackSurvivor(game, survivor)

            def zombies = _zombiesOnFlatPoint(game, survivor.point.getFlatPoint(game.getWidth()))
            if (zombies) {
                messageService.sendZombieAttackMessage(game, survivor, damage, death)
                _sendFullGameMessage(game)
            }

            if (!damage && _victory(game)) {
                def missions = _personalMissionInfo(game)
                messageService.sendEndGameMessage(game, true, missions)
                game.hasFinished = true
            } else {
                _checkAllDead(game)
            }

            if (!game.hasFinished) {
                game.playerTurn = nextSurvivor.player
                messageService.sendStartTurnMessage(game, nextSurvivor)
                _sendFullGameMessage(game)
            }
        }
    }

    void _checkAllDead(Game game) {
        if (game.missionStatus.survivors.count { it.leader == true } == 0) {
            endGame(game)
        }
    }


    def _attackSurvivor(Game game, SurvivorStatus survivor) {
        boolean death = false
        int damage = 0
        def zombies = _zombiesOnFlatPoint(game, survivor.point.getFlatPoint(game.getWidth()))
        if (zombies) {
            damage = zombies.size() - survivor.remainingDefense
            if (survivor.defense) {
                if (survivor.defense.remainingLevel > damage) {
                    survivor.defense.remainingLevel -= damage
                    damage = 0
                } else {
                    damage -= survivor.defense.remainingLevel
                    survivor.defense = null
                }
            }
            survivor.remainingLife -= damage

            if (survivor.remainingLife <= 0) {
                //Kill!
                death = true
                game.missionStatus.deads << survivor
                game.missionStatus.survivors.remove(survivor)
                def newSurvivor = game.missionStatus.survivors.find { it.player == survivor.player }
                if (newSurvivor) {
                    newSurvivor.leader = true
                    newSurvivor.point = game.missionStatus.mission.startSurvivalPoints[random.nextInt(game.missionStatus.mission.startSurvivalPoints.size())]
                }
            }
        }
        return [damage, death]
    }

    List _personalMissionInfo(Game game) {
        def missions = []
        game.players.each { player ->
            def survivor = game.missionStatus.survivors.find { it.leader == true && it.player == player }
            if (!survivor) {
                survivor = game.missionStatus.deads.findAll { it.player == player }.last()
            }


            missions << [
                    player     : player.username,
                    survivor   : survivor.survivor.slug,
                    name       : player.personalMission.name,
                    description: player.personalMission.description,
                    success    : _personalMissionSuccess(game, player.personalMission, survivor)
            ]
        }
        return missions
    }

    boolean _personalMissionSuccess(Game game, PersonalMission mission, SurvivorStatus survivor) {
        if ("THINGS" == mission.type) {
            def elements = []
            elements.addAll(survivor.inventory*.slug)
            elements << survivor.weapon?.slug
            elements << survivor.defense?.slug
            return _haveElements(mission.things, elements)
        } else if ("LIFE" == mission.type) {
            return (survivor.remainingLife == mission.value)
        } else if ("DAMAGE" == mission.type) {
            return ((survivor.survivor.life - survivor.remainingLife) == mission.value)
        } else if ("SURVIVORS" == mission.type) {
            return (game.missionStatus.survivors.count { it.player == survivor.player } == mission.value)
        } else if ("DEADS" == mission.type) {
            return (game.missionStatus.deads.count { it.player == survivor.player } == mission.value)
        }
    }

    boolean _victory(Game game) {
        boolean victory = true
        Integer notOnVPSurvivors = game.missionStatus.survivors.count { it.leader == true }
        game.missionStatus.mission.victoryConditions.each { vc ->
            def survivors = game.missionStatus.survivors.findAll {
                vc.point.x == it.point.x && vc.point.y == it.point.y && it.leader == true
            }
            notOnVPSurvivors -= survivors.size()

            def groupThings = []
            survivors.each {
                groupThings.addAll(it.inventory*.slug)
                groupThings << it.weapon?.slug
                groupThings << it.defense?.slug
            }
            victory = victory && _haveElements(vc.things, groupThings)
        }

        return victory && (notOnVPSurvivors == 0)
    }

    boolean _haveElements(List need, List have) {
        def aux = []
        aux.addAll(need)

        have.each {
            aux.remove(it)
        }
        return aux.empty
    }


    void processNoiseMessage(Game game, User player, Map data) {
        def survivor = _getPlayerCurrentSurvivor(game, player)
        if (game.hasStarted &&
                game.playerTurn == player &&
                survivor.remainingActions > 0
        ) {
            survivor.remainingActions--
            _addNoise(game, survivor.point.getFlatPoint(game.getWidth()), survivor.remainingNoise)
            messageService.sendNoiseAnimationMessage(game, survivor)
            _sendFullGameMessage(game)
        }
    }

    void processAttackMessage(Game game, User player, Map data) {
        def survivor = _getPlayerCurrentSurvivor(game, player)

        def attackableFlatPoints = _attackableFlatPoints(game,
                survivor.point.getFlatPoint(game.getWidth()),
                survivor.weapon.weapon.longRange,
                survivor.weapon.remainingAmmo
        )

        Integer attackPoint = Integer.parseInt(data.point)


        if (game.hasStarted &&
                game.playerTurn == player &&
                survivor.remainingActions > 0 &&
                attackPoint in attackableFlatPoints
        ) {
            survivor.remainingActions--
            if (survivor.weapon.weapon.noise) {
                _addNoise(game, survivor.point.getFlatPoint(game.getWidth()), survivor.weapon.weapon.noise)
            }
            int damage = survivor.weapon.weapon.damage
            int deaths = 0
            def zombies = _zombiesOnFlatPoint(game, attackPoint)
            damage.times {
                if (zombies) {
                    def zombie = zombies.pop()
                    game.missionStatus.zombies.remove(zombie)
                    deaths++
                }
            }

            if (survivor.weapon.remainingAmmo) {
                survivor.weapon.remainingAmmo--
            }

            messageService.sendAtackAnimationMessage(game, survivor, deaths)
            _sendFullGameMessage(game)
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
                def item
                def searchPoint = _getSearchPoint(game, startPoint)
                if (searchPoint) {
                    item = searchPoint.thing.createStatus()
                } else {
                    item = game.missionStatus.remainingObjects.first()
                }


                survivor.remainingActions--
                Collections.shuffle(game.missionStatus.remainingObjects)
                game.token = UUID.randomUUID()
                Integer flatPoint = startPoint.getFlatPoint(game.getWidth())
                game.missionStatus.searchs[flatPoint] = game.missionStatus.searchs[flatPoint] ? game.missionStatus.searchs[flatPoint] + 1 : 1
                _sendFullGameMessage(game)
                messageService.sendFindItemsMessage(game, survivor, [item], game.token)
            }
        }
    }

    SearchPoint _getSearchPoint(Game game, Point point) {
        return game.missionStatus.mission.searchPoints.find {
            it.point.x == point.x && it.point.y == point.y
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
                _addNoise(game, startPoint.getFlatPoint(game.getWidth()), 1)
                def item1 = game.missionStatus.remainingObjects[0]
                def item2 = game.missionStatus.remainingObjects[1]
                messageService.sendFindItemsMessage(game, survivor.player, [item1, item2])
                game.token = ''
                _sendFullGameMessage(game)
            }
        }
    }


    void processGetObjectMessage(Game game, User player, Map data) {
        def survivor = _getPlayerCurrentSurvivor(game, player)
        if (game.hasStarted &&
                game.playerTurn == player
        ) {
            def item
            Point startPoint = survivor.point
            def searchPoint = _getSearchPoint(game, startPoint)
            if (searchPoint) {
                item = searchPoint.thing.createStatus()
            } else {
                item = game.missionStatus.remainingObjects.find { it.id == data.item }
                game.missionStatus.remainingObjects.remove(item)
            }
            survivor.inventory << item


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

            if (survivor.weapon.id == data.item) {
                survivor.weapon = weaponRepository.get('fist').createStatus()
            }

            if (survivor.defense?.id == data.item) {
                survivor.defense = null
            }

            _sendFullGameMessage(game)
        }
    }

    void processUseObjectMessage(Game game, User player, Map data) {
        def survivor = _getPlayerCurrentSurvivor(game, player)
        if (game.hasStarted &&
                game.playerTurn == player
        ) {
            def item = survivor.inventory.find { it.id == data.item }

            if (item instanceof WeaponStatus) {
                if (survivor.weapon.id == data.item) {
                    processUnEquipObjectMessage(game, player, data)
                } else {
                    processEquipObjectMessage(game, player, data)
                }
            } else if (item instanceof DefenseStatus) {
                if (survivor.defense?.id == data.item) {
                    processUnEquipObjectMessage(game, player, data)
                } else {
                    processEquipObjectMessage(game, player, data)
                }
            } else if (item instanceof ItemStatus) {
                if (item.item.addsLife()) {
                    if (survivor.remainingLife < survivor.survivor.life) {
                        survivor.remainingLife += item.item.life
                        survivor.inventory.remove(item)
                    }
                } else if (item.item.addsAmmo()) {
                    if (survivor.weapon?.weapon.longRange && survivor.weapon?.weapon.slug != 'molotov-cocktail') {
                        if (survivor.weapon.remainingAmmo < survivor.weapon.weapon.ammo) {
                            survivor.weapon.remainingAmmo = survivor.weapon.weapon.ammo
                            survivor.inventory.remove(item)
                        }
                    }
                } else if (item.item.addsGas()) {
                    if (survivor.weapon?.weapon.slug == 'molotov-cocktail') {
                        if (survivor.weapon.remainingAmmo < survivor.weapon.weapon.ammo) {
                            survivor.weapon.remainingAmmo = survivor.weapon.weapon.ammo
                            survivor.inventory.remove(item)
                        }
                    }
                } else if (item.item.addsMovement()) {
                    if (survivor.remainingActions > 0) {
                        survivor.remainingMovement += item.item.movement
                        survivor.inventory.remove(item)
                    }
                } else if (item.item.makesNoise()) {
                    if (survivor.remainingActions > 0) {
                        survivor.remainingNoise += item.item.noise
                        survivor.inventory.remove(item)
                    }
                }
                _sendFullGameMessage(game)
            }
        }
    }

    void processEquipObjectMessage(Game game, User player, Map data) {
        def survivor = _getPlayerCurrentSurvivor(game, player)
        if (game.hasStarted &&
                game.playerTurn == player
        ) {
            def item = survivor.inventory.find { it.id == data.item }
            if (item instanceof WeaponStatus || item instanceof DefenseStatus) {
                if (item instanceof WeaponStatus) {
                    survivor.weapon = item
                }

                if (item instanceof DefenseStatus) {
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
            if (data.item == survivor.weapon?.id) {
                survivor.weapon = weaponRepository.get('fist').createStatus()
            }
            if (data.item == survivor.defense?.id) {
                survivor.defense = null
            }
            _sendFullGameMessage(game)
        }
    }

    boolean _canSearch(Game game, Point point) {
        Integer flatPoint = point.getFlatPoint(game.getWidth())

        //If the point has been searched two times, can't search
        if (game.missionStatus.searchs[flatPoint] > 1) {
            return false
        }

        //If there is zombies on the player position, can't search
        if (_zombiesOnFlatPoint(game, flatPoint)) {
            return false
        }

        Tile tile = _getTileFromFlatPoint(game, flatPoint)
        return tile.search
    }


    boolean _canMove(Game game, Integer startFlatPoint, Integer endFlatPoint, boolean isZombie = false) {
        Point startPoint = Point.getPointFromFlatPoint(startFlatPoint, game.getWidth())
        Point endPoint = Point.getPointFromFlatPoint(endFlatPoint, game.getWidth())

        return _canMove(game, startPoint, endPoint, startFlatPoint, endFlatPoint, isZombie)
    }

    boolean _canMove(Game game, Point startPoint, Point endPoint, Integer startFlatPoint, Integer endFlatPoint, boolean isZombie = false) {

        if (!isZombie) {
            //If is a survivor and there is zombies on the player position, can't move
            if (_zombiesOnFlatPoint(game, startFlatPoint)) {
                return false
            }
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


    List<Integer> _reacheableFlatPoints(Game game, Integer startPoint, int numMoves, boolean isZombie = false) {
        if (numMoves > 0) {
            def reacheablePoints = [
                    _getFlatPointUp(game, startPoint),
                    _getFlatPointLeft(game, startPoint),
                    _getFlatPointDown(game, startPoint),
                    _getFlatPointRight(game, startPoint)
            ]
            reacheablePoints = reacheablePoints.findAll {
                (it != -1) && _canMove(game, startPoint, it, isZombie)
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

    List<Integer> _attackableFlatPoints(Game game, Integer startPoint, boolean longRange, Integer ammo) {
        def attackableFlatPoints = []
        if (ammo) {
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
                    if (_zombiesOnFlatPoint(game, p) && _canMove(game, startPoint, p, true)) {
                        attackableFlatPoints << p
                    }
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
        user.personalMission = personalMissionRepository.getRandom()
        game.players << user
        def data = _getPreGameData(game)
        messageService.sendConnectMessage(data)
    }

    Map _getFullGameData(Game game) {
        def data = game.missionStatus.asMap()
        data.playerTurn = game.playerTurn ? game.playerTurn.username : ''
        data.slots = game.slots
        data.numPlayers = game.players.size()
        data.gameId = game.id

        game.missionStatus.survivors.eachWithIndex { survivor, i ->
            if (survivor.remainingActions > 0) {
                def flatPoint = survivor.point.getFlatPoint(game.getWidth())
                def reacheable = _reacheableFlatPoints(game, flatPoint, survivor.remainingMovement)
                def attackable = _attackableFlatPoints(game, flatPoint, survivor.weapon.weapon.longRange, survivor.weapon.remainingAmmo)
                data.survivors[i].canMoveTo = reacheable
                data.survivors[i].canAttackTo = attackable
                data.survivors[i].canSearch = (survivor.inventory.size() < survivor.remainingInventory) && _canSearch(game, survivor.point)
            } else {
                data.survivors[i].canMoveTo = []
                data.survivors[i].canAttackTo = []
                data.survivors[i].canSearch = false
            }
        }

        def missions = [:]
        game.players.each {
            missions["${it.username}"] = [
                    player     : it.username,
                    name       : it.personalMission.name,
                    description: it.personalMission.description
            ]
        }

        data.missions = missions



        data.catchedSurvivors = [:]
        game.missionStatus.survivors.each {
            data.catchedSurvivors[it.survivor.slug] = data.catchedSurvivors[it.survivor.slug] ?: []
            data.catchedSurvivors[it.survivor.slug] << it.player.username
        }


        return data
    }


    void _sendPreGameMessage(Game game) {
        def data = _getPreGameData(game)
        messageService.sendPreGameMessage(data)
    }

    Map _getPreGameData(Game game) {
        def data = [:]
        data.gameId = game.id
        def survivorList = survivorRepository.list()
        data.survivors = []
        def pickedSurvivors = game.missionStatus.survivors
        survivorList.each { s ->
            def survivorData = s.asMap()
            def pickedSurvivor = pickedSurvivors.find { it.survivor.slug == s.slug }
            survivorData.player = pickedSurvivor ? pickedSurvivor.player.username : ""
            survivorData.leader = pickedSurvivor ? pickedSurvivor.leader : false
            data.survivors << survivorData
        }


        return data
    }


    void _addNoise(Game game, Integer flatPoint, Integer level) {
        Noise noise = game.missionStatus.noise.find { it.flatPoint == flatPoint }
        if (!noise) {
            noise = new Noise(flatPoint: flatPoint, level: 0)
            game.missionStatus.noise << noise
        }
        noise.level += level
    }

    void endGame(Game game) {
        if (game.hasStarted && (!game.hasFinished)) {
            def missions = _personalMissionInfo(game)
            messageService.sendEndGameMessage(game, false, missions)
            game.hasFinished = true
        }
    }


    void zombieTime(Game game) {
        if (game.hasStarted && (!game.hasFinished)) {
            def numNewZombies = 0
            def damages = []
            def zombies = []
            zombies.addAll(game.missionStatus.zombies.clone())

            //First attack
            getCurrentLeaderes(game).each { survivor ->
                def point = survivor.point.getFlatPoint(game.getWidth())
                zombies = zombies - _zombiesOnFlatPoint(game, point)
                def (damage, death) = _attackSurvivor(game, survivor)
                damages << [
                        player  : survivor.player.username,
                        survivor: survivor.survivor.slug,
                        damage  : damage,
                        death   : death
                ]
            }


            def leaderes = getCurrentLeaderes(game)
            def points = leaderes.collect {
                it.point.getFlatPoint(game.getWidth())
            }

            //Then move
            zombies.each { z ->

                def point = z.point.getFlatPoint(game.getWidth())
                def reacheable = _reacheableFlatPoints(game, point, 1, true).sort { Math.random() }

                //Move to survivor
                def newPoint = reacheable.find { it in points }


                if (!newPoint) {
                    newPoint = reacheable.max { _calcNoise(game, it) }
                }

                def p = Point.getPointFromFlatPoint(newPoint, game.getWidth())

                z.point.x = p.x
                z.point.y = p.y
            }

            //Add new zombies
            Zombie zombie = zombieRepository.get('zombie1')
            def noiseTotal = (game.missionStatus.noise?.level?.sum()) ?: 0
            numNewZombies = (leaderes.size() * 2) + noiseTotal
            numNewZombies.times {
                def entryPoint = game.missionStatus.mission.entryZombiePoints[it % game.missionStatus.mission.entryZombiePoints.size()]
                def z = zombie.createZombieStatus(entryPoint.x, entryPoint.y)

                game.missionStatus.zombies << z
            }

            //Eliminate noise
            game.missionStatus.noise.clear()

            messageService.sendZombieTimeMessage(game, damages, numNewZombies)
            _sendFullGameMessage(game)

            //New zombietime
            scheduleZombieTime(game)

            _checkAllDead(game)
        }

    }


    void scheduleZombieTime(Game game) {
        Timer timer = new Timer()
        TimerTask action = new TimerTask() {
            public void run() {
                zombieTime(game)
            }
        }

        timer.schedule(action, game.zombieTimeInterval * 1000)
    }

    void scheduleEndGame(Game game) {
        Timer timer = new Timer()
        TimerTask action = new TimerTask() {
            public void run() {
                endGame(game)
            }
        }

        timer.schedule(action, 900000) //15 min
    }

    List<SurvivorStatus> getCurrentLeaderes(Game game) {
        return game.missionStatus.survivors.findAll { it.leader == true }
    }

    double _calcNoise(Game game, Integer flatPoint) {
        def p = Point.getPointFromFlatPoint(flatPoint, game.getWidth())
        double sum = 0
        game.missionStatus.noise.each {
            def np = Point.getPointFromFlatPoint(it.flatPoint, game.getWidth())
            def dist = Math.sqrt((np.x - p.x)**2 + (np.y - p.y)**2)
            def level = it.level - dist
            sum += (level > 0) ? level : 0
        }
        return sum
    }


}
