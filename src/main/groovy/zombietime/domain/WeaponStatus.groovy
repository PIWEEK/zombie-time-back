package zombietime.domain

class WeaponStatus implements Status {

    String id = UUID.randomUUID()
    Integer remainingAmmo

    Weapon weapon

    Map asMap() {
        return [
                id         : id,
                currentAmmo: remainingAmmo,
                maxAmmo    : weapon.ammo,
                name       : weapon.name,
                avatar     : weapon.avatar,
                damage     : weapon.damage
        ]
    }

    String getSlug() {
        return weapon.slug
    }


}
