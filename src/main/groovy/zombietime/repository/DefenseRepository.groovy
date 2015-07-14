package zombietime.repository

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import zombietime.domain.Defense

import java.util.concurrent.ConcurrentHashMap

@Component
class DefenseRepository {
    ConcurrentHashMap<String, Defense> defenses = new ConcurrentHashMap<String, Defense>()

    void create(Map args = [:]) {
        def defense = new Defense(args)
        defenses.put(defense.slug, defense)
    }

    Defense get(String slug) {
        return defenses.get(slug)
    }

    void remove(String slug) {
        defenses.remove(slug)
    }

    List<Defense> list() {
        return defenses.values().toList()
    }
}
