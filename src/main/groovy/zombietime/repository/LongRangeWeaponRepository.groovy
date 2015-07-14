package zombietime.repository

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import zombietime.domain.LongRangeWeapon

import java.util.concurrent.ConcurrentHashMap

@Component
class LongRangeWeaponRepository {
    ConcurrentHashMap<String, LongRangeWeapon> longRangeWeapons = new ConcurrentHashMap<String, LongRangeWeapon>()

    void create(Map args = [:]) {
        def longRangeWeapon = new LongRangeWeapon(args)
        longRangeWeapons.put(longRangeWeapon.slug, longRangeWeapon)
    }

    LongRangeWeapon get(String slug) {
        return longRangeWeapons.get(slug)
    }

    void remove(String slug) {
        longRangeWeapons.remove(slug)
    }

    List<LongRangeWeapon> list() {
        return longRangeWeapons.values().toList()
    }
}
