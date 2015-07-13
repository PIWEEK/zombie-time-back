package zombietime.controller

import org.springframework.ui.Model
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


    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("games", gameRepository.list())
        return "home"
    }


    @RequestMapping(value = "/games", method = RequestMethod.POST)
    public @ResponseBody
    String createGame(@RequestParam("gameName") String gameName,
                      @RequestParam("gamePassword") String gamePassword,
                      @RequestParam("gameSlots") Integer gameSlots,
                      @RequestParam("gameDifficulty") String gameDifficulty,
                      @RequestParam("gameMission") String gameMission
    ) {
        def uuid = UUID.randomUUID().toString()
        Game game = new Game(id: uuid, name: gameName, password: gamePassword, slots: gameSlots, difficulty: gameDifficulty, mission: gameMission)
        gameRepository.create(game)
        return "Game created: $uuid"
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
