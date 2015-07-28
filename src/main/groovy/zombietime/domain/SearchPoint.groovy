package zombietime.domain

class SearchPoint {
    Object thing
    Point point

    Map asMap(Integer mapWidth) {
        return [
                point : point.getFlatPoint(mapWidth),
                avatar: 297
        ]
    }
}
