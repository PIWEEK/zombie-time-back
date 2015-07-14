package zombietime.domain

class Game {
    String id
    String name
    String password
    Integer slots = 4
    List<User> players = []
    Integer zombieTimeInterval = 60
    Integer mission = 1
    boolean hasStarted = false


    MissionStatus missionStatus
}
