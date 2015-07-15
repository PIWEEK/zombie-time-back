package zombietime.domain

class SurvivorStatus {

    String id = UUID.randomUUID()
    Integer remainingLife
    Integer remainingActions
    Integer remainingInventory
    Integer remainingDefense

    Boolean leader

    Survivor survivor
    User player

    Point point = new Point(x: 0, y: 0)


    Map asMap() {
        return [
                id                : id,
                player            : player.username,
                slug              : survivor.slug,
                avatar            : survivor.avatar,
                leader            : leader,
                remainingLife     : remainingLife,
                remainingActions  : remainingActions,
                remainingInventory: remainingInventory,
                remainingDefense  : remainingDefense,
                point             : point.asMap()
        ]
    }

}
