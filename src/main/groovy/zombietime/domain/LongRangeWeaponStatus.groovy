package zombietime.domain

class LongRangeWeaponStatus implements Status {

    String id = UUID.randomUUID()
    Integer remainingAmmo

    LongRangeWeapon longRangeWeapon

}
