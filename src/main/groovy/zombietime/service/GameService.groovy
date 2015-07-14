package zombietime.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import zombietime.domain.Game
import zombietime.domain.Mission
import zombietime.domain.MissionStatus
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

    Game create(String gameName, String gamePassword, Integer gameSlots, Integer zombieTimeInterval, Integer missionId) {
        def uuid = UUID.randomUUID().toString()
        Game game = new Game(id: uuid, name: gameName, password: gamePassword, slots: gameSlots, zombieTimeInterval: zombieTimeInterval, mission: missionId)
        gameRepository.create(game)

        //Create Mission status
        createMissionStatus(missionId, zombieTimeInterval)


        return game
    }


    MissionStatus createMissionStatus(Integer missionId, Integer zombieTimeInterval) {
        Mission mission = missionRepository.get(missionId)
        def missionStatus = new MissionStatus(mission: mission, zombieTimeInterval: zombieTimeInterval)
        def zombie = zombieRepository.get()

        //add zombies
        mission.startZombiePoints.each { point ->
            missionStatus.zombies << new ZombieStatus(
                    zombie: zombie,
                    point: point,
                    remainingLife: zombie.life,
                    remainingDamage: zombie.damage
            )
        }

        //add items and weapons
        mission.objects.each { item ->
            missionStatus.remainObjects << item.createStatus()
        }


    }
}
