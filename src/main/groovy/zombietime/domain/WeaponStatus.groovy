package zombietime.domain

class WeaponStatus implements Status {

    String id = UUID.randomUUID()
    Integer remainingAmmo

    Weapon weapon

    Map asMap() {
        return [
                id         : id,
                slug       : weapon.slug,
                currentAmmo: remainingAmmo,
                maxAmmo    : weapon.ammo,
                name       : weapon.name,
                avatar     : weapon.avatar,
                damage     : weapon.damage,
                longRange  : weapon.longRange,
                description: weapon.description
        ]
    }

    String getSlug() {
        return weapon.slug
    }


}
