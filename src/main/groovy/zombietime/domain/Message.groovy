package zombietime.domain

class Message {
    String game
    String action
    Integer x
    Integer y
    Long timeStamp = System.currentTimeMillis()
}
