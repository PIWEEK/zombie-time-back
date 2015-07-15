package zombietime.domain

class ShortRangeWeaponStatus implements Status {

    String id = UUID.randomUUID()
    Integer remainingAttacks

    ShortRangeWeapon shortRangeWeapon

    Map asMap() {
        return [
                id           : id,
                currentAtacks: remainingAttacks,
                maxAtacks    : shortRangeWeapon.attacks,
                name         : shortRangeWeapon.name,
                avatar       : shortRangeWeapon.avatar,
                damage       : shortRangeWeapon.damage,
                noise        : shortRangeWeapon.noise
        ]
    }

}
