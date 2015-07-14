package zombietime.domain

class Mission {
    String slug
    Integer mapWidth = 0
    Integer mapHeight = 0
    List<Integer> mapFloorTiles = []
    List<Integer> mapWallTiles = []
    List<Integer> mapItemTiles = []

    List<Object> objects = []

    List<Point> startSurvivalPoints = []
    List<Point> startZombiePoints = []
    List<Point> entryZombiePoints = []

    List<VictoryCondition> victoryConditions = []


}
