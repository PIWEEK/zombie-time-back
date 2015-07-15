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


    Integer getFlatPoint(Game game) {
        return point.getFlatPoint(game.missionStatus.mission.mapWidth)
    }


    Map asMap(Integer mapWidth) {
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
                point             : point.getFlatPoint(mapWidth)
        ]
    }

}
