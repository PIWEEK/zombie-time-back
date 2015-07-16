package zombietime.repository

import org.springframework.stereotype.Component
import zombietime.domain.PersonalMission

import java.util.concurrent.ConcurrentHashMap

@Component
class PersonalMissionRepository {
    ConcurrentHashMap<String, PersonalMission> personalMissions = new ConcurrentHashMap<String, PersonalMission>()

    void create(Map args = [:]) {
        def mission = new PersonalMission(args)
        personalMissions.put(mission.name, mission)
    }

    PersonalMission get(String name) {
        return personalMissions.get(name)
    }

    void remove(String name) {
        personalMissions.remove(name)
    }

    List<PersonalMission> list() {
        return personalMissions.values().toList()
    }

    PersonalMission getRandom() {
        def list = list().sort { Math.random() }
        return list.first()
    }
}
