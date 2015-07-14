package zombietime.repository

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import zombietime.domain.Zombie

import java.util.concurrent.ConcurrentHashMap

@Component
class ZombieRepository {
    ConcurrentHashMap<String, Zombie> zombies = new ConcurrentHashMap<String, Zombie>()

    void create(Map args = [:]) {
        def zombie = new Zombie(args)
        zombies.put(zombie.slug, zombie)
    }

    Zombie get(String slug) {
        return zombies.get(slug)
    }

    void remove(String slug) {
        zombies.remove(slug)
    }

    List<Zombie> list() {
        return zombies.values().toList()
    }
}
