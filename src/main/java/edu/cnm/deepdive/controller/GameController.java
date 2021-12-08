package edu.cnm.deepdive.controller;

import edu.cnm.deepdive.model.entity.Game;
import edu.cnm.deepdive.service.GameService;
import edu.cnm.deepdive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/games")
public class GameController {

    private final UserService userService;
    private final GameService gameService;

    @Autowired
    public GameController(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Game post(@RequestBody Game game) {
        return gameService.startGame(game, userService.getCurrentUser());
    }

    @GetMapping(value = "/{externalKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Game get(@PathVariable UUID externalKey) {
        return gameService
                .get(externalKey, userService.getCurrentUser())
                .orElseThrow();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<Game> get() {
        return userService
                .getCurrentUser()
                .getGames();
    }

}