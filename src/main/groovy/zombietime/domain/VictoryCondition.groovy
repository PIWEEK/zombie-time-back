package zombietime.domain

class VictoryCondition {
    Point point
    List<String> things

    Map asMap(Integer mapWidth) {
        return [
                point : point.getFlatPoint(mapWidth),
                things: things
        ]
    }
}
