package zombietime.repository

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import zombietime.domain.ShortRangeWeapon

import java.util.concurrent.ConcurrentHashMap

@Component
class ShortRangeWeaponRepository {
    ConcurrentHashMap<String, ShortRangeWeapon> shortRangeWeapons = new ConcurrentHashMap<String, ShortRangeWeapon>()

    void create(Map args = [:]) {
        def shortRangeWeapon = new ShortRangeWeapon(args)
        shortRangeWeapons.put(shortRangeWeapon.slug, shortRangeWeapon)
    }

    ShortRangeWeapon get(String slug) {
        return shortRangeWeapons.get(slug)
    }

    void remove(String slug) {
        shortRangeWeapons.remove(slug)
    }

    List<ShortRangeWeapon> list() {
        return shortRangeWeapons.values().toList()
    }
}
