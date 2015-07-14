package zombietime.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import zombietime.domain.Game
import zombietime.repository.GameRepository

@Service
class GameService {

    @Autowired
    private GameRepository gameRepository


    List<Game> listOpenGames() {
        def games = gameRepository.list()
        return games.findAll { (!it.hasStarted) && (it.players.size() < it.slots) }
    }


    Game get(String gameId) {
        return gameRepository.get(gameId)
    }

    Game create(String gameName, String gamePassword, Integer gameSlots, String gameDifficulty, String gameMission) {
        def uuid = UUID.randomUUID().toString()
        Game game = new Game(id: uuid, name: gameName, password: gamePassword, slots: gameSlots, difficulty: gameDifficulty, mission: gameMission)
        gameRepository.create(game)
        return game
    }
}
