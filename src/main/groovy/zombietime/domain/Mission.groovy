package zombietime.domain

import groovy.json.JsonSlurper

class Mission {
    Integer mapWidth = 0
    Integer mapHeight = 0
    List<Integer> mapFloorTiles = []
    List<Integer> mapWallTiles = []
    List<Integer> mapItemsTiles = []

    List<Object> objects = []

    List<Point> startSurvivalPoints = []
    List<Point> startZombiePoints = []
    List<Point> entryZombiePoints = []

    List<VictoryCondition> victoryConditions = []


    static Mission createMissionFromJsonString(String missionDataString) {
        Mission mission = Mission()
        def slurper = new JsonSlurper()
        def missionData = slurper.parseText(missionDataString)

        mission.mapWidth = missionData.map.width
        mission.mapHeight = missionData.map.height


        return mission
    }

}
