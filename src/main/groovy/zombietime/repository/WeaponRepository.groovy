package zombietime.repository

import org.springframework.stereotype.Component
import zombietime.domain.Weapon

import java.util.concurrent.ConcurrentHashMap

@Component
class WeaponRepository {
    ConcurrentHashMap<String, Weapon> weapons = new ConcurrentHashMap<String, Weapon>()

    void create(Map args = [:]) {
        def weapon = new Weapon(args)
        weapons.put(weapon.slug, weapon)
    }

    Weapon get(String slug) {
        return weapons.get(slug)
    }

    void remove(String slug) {
        weapons.remove(slug)
    }

    List<Weapon> list() {
        return weapons.values().toList()
    }
}
