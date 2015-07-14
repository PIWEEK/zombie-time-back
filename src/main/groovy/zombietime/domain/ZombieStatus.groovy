package zombietime.domain

class ZombieStatus {

    Point point
    Integer remainingLife
    Integer remainingDamage

    Zombie zombie


    Map asMap() {
        return [
                remainingLife  : remainingLife,
                remainingDamage: remainingDamage,
                point          : point.asMap(),
                avatar         : zombie.avatar
        ]
    }
}
