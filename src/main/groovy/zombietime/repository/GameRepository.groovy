package zombietime.repository

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import zombietime.domain.Game

import java.util.concurrent.ConcurrentHashMap

@Component
class GameRepository {
    ConcurrentHashMap<String, Game> games = new ConcurrentHashMap<String, Game>()

    void create(Game game) {
        games.put(game.id, game)
    }

    Game get(String id) {
        return games.get(id)
    }

    void remove(String id) {
        games.remove(id)
    }

    List<Game> list() {
        return games.values().toList()
    }
}
