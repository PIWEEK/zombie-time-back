package zombietime.controller

import zombietime.domain.Message
import zombietime.service.MessageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class ChatController {
    @Autowired
    private MessageService messageService

    @RequestMapping("/hello")
    public @ResponseBody
    String hello() {
        return "Hello world!"
    }


    @MessageMapping("/message")
    public void chatMessage(@Payload Message message) {
        Message mess = new Message(
                game: message.game,
                action: message.action,
                x: message.x,
                y: message.y
        )
        messageService.sendMessage(message)
    }

}
