package zombietime.domain

class LongRangeWeapon extends Weapon {

    Integer ammo


    LongRangeWeaponStatus createStatus() {
        return new LongRangeWeaponStatus(longRangeWeapon: this, remainingAmmo: this.ammo)
    }
}
