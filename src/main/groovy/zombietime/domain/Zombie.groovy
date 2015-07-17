package zombietime.domain

class Zombie {

    String name
    String slug
    Integer avatar

    Integer life = 1
    Integer damage = 1

    ZombieStatus createZombieStatus(x, y){
        return new ZombieStatus(
                zombie: this,
                point: new Point(x: x, y: y),
                remainingLife: this.life,
                remainingDamage: this.damage
        )
    }

}
