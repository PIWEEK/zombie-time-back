package zombietime.domain

class Point {
    Integer x
    Integer y

    Integer getFlatPoint(Integer mapWidth) {
        return ((y * mapWidth) + x)
    }

    static Point getPointFromFlatPoint(Integer flatPoint, Integer mapWidth) {
        def y = Math.floor(flatPoint / mapWidth)
        return new Point(
                y: y,
                x: flatPoint - y
        )
    }
}
