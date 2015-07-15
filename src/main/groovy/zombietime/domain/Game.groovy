package zombietime.domain

class Game {
    String id
    String name
    String password
    Integer slots = 4
    List<User> players = []
    Integer zombieTimeInterval = 60
    String mission = '01'
    boolean hasStarted = false
    User playerTurn
    String token


    MissionStatus missionStatus

}
