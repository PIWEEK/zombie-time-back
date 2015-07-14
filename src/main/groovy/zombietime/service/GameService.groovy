package zombietime.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import zombietime.domain.Game
import zombietime.domain.Mission
import zombietime.domain.MissionStatus
import zombietime.domain.Point
import zombietime.domain.ZombieStatus
import zombietime.repository.GameRepository
import zombietime.repository.MissionRepository
import zombietime.repository.ZombieRepository

@Service
class GameService {

    @Autowired
    private GameRepository gameRepository

    @Autowired
    private MissionRepository missionRepository

    @Autowired
    private ZombieRepository zombieRepository


    List<Game> listOpenGames() {
        def games = gameRepository.list()
        return games.findAll { (!it.hasStarted) && (it.players.size() < it.slots) }
    }


    Game get(String gameId) {
        return gameRepository.get(gameId)
    }

    Game create(String gameName, String gamePassword, Integer gameSlots, Integer zombieTimeInterval, String missionSlug) {
        def uuid = UUID.randomUUID().toString()
        Game game = new Game(id: uuid, name: gameName, password: gamePassword, slots: gameSlots, zombieTimeInterval: zombieTimeInterval, mission: missionSlug)
        gameRepository.create(game)

        //Create Mission status
        createMissionStatus(missionSlug, zombieTimeInterval)


        return game
    }


    MissionStatus createMissionStatus(String missionSlug, Integer zombieTimeInterval) {


        Mission mission = missionRepository.get(missionSlug)
        def missionStatus = new MissionStatus(mission: mission, zombieTimeInterval: zombieTimeInterval)
        def zombie = zombieRepository.get('zombie1')

        //add zombies
        mission.startZombiePoints.each { point ->
            missionStatus.zombies << new ZombieStatus(
                    zombie: zombie,
                    point: new Point(x: point.x, y: point.y),
                    remainingLife: zombie.life,
                    remainingDamage: zombie.damage
            )
        }

        //add items and weapons
        mission.objects.each { item ->
            missionStatus.remainingObjects << item.createStatus()
        }

        return missionStatus


    }
}
