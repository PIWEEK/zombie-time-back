package zombietime.repository

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import zombietime.domain.Survivor

import java.util.concurrent.ConcurrentHashMap

@Component
class SurvivorRepository {
    ConcurrentHashMap<String, Survivor> survivors = new ConcurrentHashMap<String, Survivor>()

    void create(Map args = [:]) {
        def survivor = new Survivor(args)
        survivors.put(survivor.slug, survivor)
    }

    Survivor get(String slug) {
        return survivors.get(slug)
    }

    void remove(String slug) {
        survivors.remove(slug)
    }

    List<Survivor> list() {
        return survivors.values().toList()
    }
}
