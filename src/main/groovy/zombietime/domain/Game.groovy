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
    boolean hasFinished = false
    User playerTurn
    String token


    MissionStatus missionStatus


    Integer getWidth() {
        return this.missionStatus.mission.mapWidth
    }

    Integer getNumPoints() {
        return this.missionStatus.mission.mapWidth * this.missionStatus.mission.mapHeight
    }

}
