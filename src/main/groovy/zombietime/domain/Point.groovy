package zombietime.domain

class Point {
    Integer x
    Integer y

    Map asMap() {
        return [
                x: x,
                y: y
        ]
    }
}
