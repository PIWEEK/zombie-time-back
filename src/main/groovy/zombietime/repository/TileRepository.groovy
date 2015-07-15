package zombietime.repository

import org.springframework.stereotype.Component
import zombietime.domain.Tile

import java.util.concurrent.ConcurrentHashMap

@Component
class TileRepository {
    ConcurrentHashMap<Integer, Tile> tiles = new ConcurrentHashMap<Integer, Tile>()

    void create(Map args = [:]) {
        def tile = new Tile(args)
        tiles.put(tile.num, tile)
    }

    Tile get(Integer num) {
        return tiles.get(num)
    }

    void remove(Integer num) {
        tiles.remove(num)
    }

    List<Tile> list() {
        return tiles.values().toList()
    }
}
