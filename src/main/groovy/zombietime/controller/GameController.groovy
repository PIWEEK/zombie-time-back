package zombietime.controller

import org.springframework.messaging.MessageHeaders
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import zombietime.domain.Game
import zombietime.domain.Message
import zombietime.repository.UserRepository
import zombietime.service.GameMessageService
import zombietime.service.GameService
import zombietime.service.MessageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

import javax.servlet.http.HttpSession


@Controller
class GameController {
    @Autowired
    private MessageService messageService

    @Autowired
    private GameService gameService

    @Autowired
    private GameMessageService gameMessageService

    @Autowired
    private UserRepository userRepository


    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("games", gameService.listOpenGames())
        return "home"
    }

    @RequestMapping(value = "/game", method = RequestMethod.POST)
    public String joinGame(@RequestParam("id") String gameId,
                           @RequestParam("username") String username,
                           @RequestParam("password") String password,
                           Model model,
                           RedirectAttributes redirectAttributes
    ) {

        def game = gameService.get(gameId)

        if (!game ||
                game.hasStarted ||
                game.players.size() >= game.slots ||
                ((game.password) && (game.password != password))) {
            redirectAttributes.addFlashAttribute("flash.error", "true");
            return "redirect:/#section2"
        }

        model.addAttribute("gameId", gameId)
        model.addAttribute("username", username)
        model.addAttribute("password", password)
        model.addAttribute("gameName", game.name)
        return "game"
    }


    @RequestMapping(value = "/games", method = RequestMethod.POST)
    String createGame(@RequestParam("userName") String userName,
                      @RequestParam("gameName") String gameName,
                      @RequestParam("gamePassword") String gamePassword,
                      @RequestParam("gameSlots") Integer gameSlots,
                      @RequestParam("gameZombieTimeInterval") Integer gameZombieTimeInterval,
                      @RequestParam("gameMission") String gameMission,
                      Model model
    ) {

        Game game = gameService.create(gameName, gamePassword, gameSlots, gameZombieTimeInterval, gameMission)

        model.addAttribute("gameId", game.id)
        model.addAttribute("username", userName)
        model.addAttribute("password", gamePassword)
        model.addAttribute("gameName", game.name)
        return "game"
    }

    @MessageMapping("/message")
    public void processMessage(@Payload Message message, MessageHeaders headers) {
        def user = userRepository.get(headers.simpSessionId)
        if (user) {
            gameMessageService.processMessage(message, user)
        }
    }

}
