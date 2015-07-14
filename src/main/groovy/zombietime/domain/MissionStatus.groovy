package zombietime.domain

class MissionStatus {
    Mission mission
    List<SurvivorStatus> survivors = []
    List<ZombieStatus> zombies = []
    List<Object> remainObjects = []
    Integer time = 900
    Integer zombieTimeInterval = 60

}
