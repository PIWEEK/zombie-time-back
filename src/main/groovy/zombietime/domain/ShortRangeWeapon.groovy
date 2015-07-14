package zombietime.domain

class ShortRangeWeapon extends Weapon {

    Integer attacks

    ShortRangeWeaponStatus createStatus() {
        return new ShortRangeWeaponStatus(shortRangeWeapon: this, remainingAttacks: this.attacks)
    }

}
