package zombietime.domain

class Noise {
    Integer flatPoint
    Integer level

    Map asMap() {
        return [
                point: flatPoint,
                level: level
        ]
    }
}
