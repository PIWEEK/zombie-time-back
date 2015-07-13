package zombietime.controller

import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import zombietime.domain.Game
import zombietime.domain.Message
import zombietime.repository.GameRepository
import zombietime.service.MessageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class GameController {
    @Autowired
    private MessageService messageService

    @Autowired
    private GameRepository gameRepository

/*
    @RequestMapping(value = "/create-game", method = RequestMethod.GET)
    public @ResponseBody
    String showCreateGame() {

    }
*/

    @RequestMapping(value = "/create-game", method = RequestMethod.GET)
    public @ResponseBody
    String createGame(@RequestParam("name") String name, @RequestParam("password") String password) {
        def uuid = UUID.randomUUID().toString()
        Game game = new Game(id: uuid, name: name, password: password)
        gameRepository.create(game)
        return "Game created: $uuid"
    }

    @RequestMapping(value = "/games", method = RequestMethod.GET)
    public @ResponseBody
    String listGames() {
        StringBuffer list = new StringBuffer("<html><body><h1>Games</h1><ul>")
        gameRepository.list().each { game ->
            list.append("<li>Game: ${game.name} - ${game.id}</li>")
        }
        list.append("</ul></body></html>")
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
