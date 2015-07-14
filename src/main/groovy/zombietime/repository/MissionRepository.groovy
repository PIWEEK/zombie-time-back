package zombietime.repository

import org.springframework.stereotype.Component
import zombietime.domain.Mission
import zombietime.domain.Point
import zombietime.domain.VictoryCondition

import java.util.concurrent.ConcurrentHashMap

@Component
class MissionRepository {
    ConcurrentHashMap<String, Mission> missions = new ConcurrentHashMap<String, Mission>()

    void create(Mission mission) {
        missions.put(mission.slug, mission)
    }

    Mission get(String slug) {
        return missions.get(slug)
    }

    void remove(String slug) {
        missions.remove(slug)
    }

    List<Mission> list() {
        return missions.values().toList()
    }


    Mission create(String slug, Integer mapWidth, Integer mapHeight, List<Integer> mapFloorTiles, List<Integer> mapWallTiles,
                   List<Integer> mapItemTiles, List<Object> objects, List<Point> startSurvivalPoints,
                   List<Point> startZombiePoints, List<Point> entryZombiePoints, List<VictoryCondition> victoryConditions) {

        Mission mission = new Mission(
                slug: slug,
                mapWidth: mapWidth,
                mapHeight: mapHeight,
                mapFloorTiles: mapFloorTiles,
                mapWallTiles: mapWallTiles,
                mapItemTiles: mapItemTiles,
                objects: objects,
                startSurvivalPoints: startSurvivalPoints,
                startZombiePoints: startZombiePoints,
                entryZombiePoints: entryZombiePoints,
                victoryConditions: victoryConditions
        )


        missions.put(mission.slug, mission)

        return mission
    }
}


