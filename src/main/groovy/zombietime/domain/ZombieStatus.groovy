package zombietime.domain

class ZombieStatus {

    String id = UUID.randomUUID()
    Point point
    Integer remainingLife
    Integer remainingDamage

    Zombie zombie


    Map asMap(Integer mapWidth) {
        return [
                id             : id,
                remainingLife  : remainingLife,
                remainingDamage: remainingDamage,
                point          : point.getFlatPoint(mapWidth),
                avatar         : zombie.avatar
        ]
    }
}
