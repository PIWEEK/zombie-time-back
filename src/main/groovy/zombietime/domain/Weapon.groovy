package zombietime.domain

class Weapon {

    String name
    String slug
    Integer avatar
    Integer damage
    Integer noise
    Integer ammo
    boolean longRange


    WeaponStatus createStatus() {
        return new WeaponStatus(weapon: this, remainingAmmo: this.ammo)
    }

}
