package zombietime.domain

class SurvivorStatus {

    Integer remainingLife
    Integer remainingActions
    Integer remainingInventory
    Integer remainingDefense

    Survivor survivor

    Point point = new Point(x: 0, y: 0)


    Map asMap() {
        return [
                name              : survivor.name,
                avatar            : survivor.avatar,
                remainingLife     : remainingLife,
                remainingActions  : remainingActions,
                remainingInventory: remainingInventory,
                remainingDefense  : remainingDefense,
                point             : point.asMap()
        ]
    }

}
