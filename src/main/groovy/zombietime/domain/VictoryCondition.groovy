package zombietime.domain

class VictoryCondition {
    Integer numPlayers
    Point point
    List<Status> objects

    Map asMap() {
        return [
                numPlayers: numPlayers,
                point     : point.asMap(),
                objects   : objects*.slug
        ]
    }
}
