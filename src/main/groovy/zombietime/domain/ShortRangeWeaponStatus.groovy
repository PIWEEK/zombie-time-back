package zombietime.domain

class ShortRangeWeaponStatus implements Status {

    String id = UUID.randomUUID()
    Integer remainingAttacks

    ShortRangeWeapon shortRangeWeapon

}
