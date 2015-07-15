package zombietime.domain

class LongRangeWeaponStatus implements Status {

    String id = UUID.randomUUID()
    Integer remainingAmmo

    LongRangeWeapon longRangeWeapon

    Map asMap() {
        return [
                id         : id,
                currentAmmo: remainingAmmo,
                maxAmmo    : longRangeWeapon.ammo,
                name       : longRangeWeapon.name,
                avatar     : longRangeWeapon.avatar,
                damage     : longRangeWeapon.damage
        ]
    }

}
