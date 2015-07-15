package zombietime.domain

import zombietime.utils.MessageType

class Message {
    String game
    String user
    MessageType type
    Map data
    Long timeStamp = System.currentTimeMillis()
}
