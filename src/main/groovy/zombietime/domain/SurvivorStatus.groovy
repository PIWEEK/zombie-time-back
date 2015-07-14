package zombietime.domain

class SurvivorStatus {

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
