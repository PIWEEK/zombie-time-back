package zombietime.domain

class VictoryCondition {
    String name
    String description
    Point point
    List<String> things

    Map asMap(Integer mapWidth) {
        return [
                name       : name,
                description: description,
                point      : point.getFlatPoint(mapWidth),
                things     : things
        ]
    }
}
