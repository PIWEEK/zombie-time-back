package zombietime.repository

import org.springframework.stereotype.Component
import zombietime.domain.Game
import zombietime.domain.Mission

import java.util.concurrent.ConcurrentHashMap

@Component
class MissionRepository {
    ConcurrentHashMap<Integer, Mission> missions = new ConcurrentHashMap<String, Mission>()

    void create(Mission mission) {
        mission.put(mission.id, mission)
    }

    Mission get(Integer id) {
        //return missions.get(id)

        return new Mission()

    }

    void remove(String id) {
        missions.remove(id)
    }

    List<Mission> list() {
        return missions.values().toList()
    }
}
