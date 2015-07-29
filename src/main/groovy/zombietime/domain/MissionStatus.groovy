package zombietime.domain

class MissionStatus {
    Mission mission
    Game game
    List<SurvivorStatus> survivors = []
    List<SurvivorStatus> deads = []
    List<ZombieStatus> zombies = []
    List<Status> remainingObjects = []
    List<Noise> noise = []
    Integer time = 900
    Map searchs = [:]


    Map asMap() {
        return [
                survivors         : survivors*.asMap(mission.mapWidth),
                zombies           : zombies*.asMap(mission.mapWidth),
                noise             : noise*.asMap(),
                time              : time,
                zombieTimeInterval: game.zombieTimeInterval,
                map               : [
                        width     : mission.mapWidth,
                        height    : mission.mapHeight,
                        floorTiles: mission.mapFloorTiles,
                        wallTiles : mission.mapWallTiles,
                        itemTiles : mission.mapItemTiles
                ],
                victoryConditions : mission.victoryConditions*.asMap(mission.mapWidth),
                searchPoints      : mission.searchPoints*.asMap(mission.mapWidth)

        ]
    }

}
