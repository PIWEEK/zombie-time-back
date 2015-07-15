package zombietime.domain

class ZombieStatus {

    String id = UUID.randomUUID()
    Point point
    Integer remainingLife
    Integer remainingDamage

    Zombie zombie


    Map asMap() {
        return [
                id             : id,
                remainingLife  : remainingLife,
                remainingDamage: remainingDamage,
                point          : point.asMap(),
                avatar         : zombie.avatar
        ]
    }
}
