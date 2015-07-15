package zombietime.domain

class VictoryCondition {
    Integer numPlayers
    Point point
    List<Status> objects

    Map asMap(Integer mapWidth) {
        return [
                numPlayers: numPlayers,
                point     : point.getFlatPoint(mapWidth),
                objects   : objects*.slug
        ]
    }
}
