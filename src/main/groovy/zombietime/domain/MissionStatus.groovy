package zombietime.domain

class MissionStatus {
    Mission mission
    Game game
    List<SurvivorStatus> survivors = []
    List<ZombieStatus> zombies = []
    List<Status> remainingObjects = []
    Integer time = 900


    Map asMap() {
        return [
                survivors         : survivors*.asMap(),
                zombies           : zombies*.asMap(),
                time              : time,
                zombieTimeInterval: game.zombieTimeInterval,
                map               : [
                        width     : mission.mapWidth,
                        height    : mission.mapHeight,
                        floorTiles: mission.mapFloorTiles,
                        wallTiles : mission.mapWallTiles,
                        itemTiles : mission.mapItemTiles
                ],
                victoryConditions : mission.victoryConditions*.asMap()
        ]
    }

}
