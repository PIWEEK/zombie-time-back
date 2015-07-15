package zombietime.domain

class Point {
    Integer x
    Integer y

    Integer getFlatPoint(Integer mapWidth) {
        return ((y * mapWidth) + x)
    }
}
