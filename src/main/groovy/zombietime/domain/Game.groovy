package zombietime.domain

class Game {
    String id
    String name
    String password
    Integer slots = 4
    List<User> players = []
    String difficulty = "Medium"
    String mission = "1"
    boolean hasStarted = false
}
