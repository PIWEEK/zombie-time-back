package zombietime.domain

class SurvivorStatus {

    String id = UUID.randomUUID()
    Integer remainingLife
    Integer remainingActions
    Integer remainingInventory
    Integer remainingDefense

    Status weapon
    Status defense

    List<Status> inventory = []

    Boolean leader

    Survivor survivor
    User player

    Point point = new Point(x: 0, y: 0)


    Integer getFlatPoint(Game game) {
        return point.getFlatPoint(game.missionStatus.mission.mapWidth)
    }


    Map asMap(Integer mapWidth) {
        return [
                id              : id,
                player          : player.username,
                slug            : survivor.slug,
                avatar          : survivor.avatar,
                leader          : leader,
                currentLife     : remainingLife,
                currentActions  : remainingActions,
                currentInventory: remainingInventory,
                currentDefense  : remainingDefense,
                baseLife        : survivor.life,
                baseActions     : survivor.actions,
                baseInventory   : survivor.inventory,
                baseDefense     : survivor.defense,
                point           : leader ? point.getFlatPoint(mapWidth) : -1,
                weapon          : weapon.asMap(),
                defense         : defense ? defense.asMap() : [:],
                inventory       : inventory ? inventory*.asMap() : []
        ]
    }

}
