package org.example.numberguessinggame.controllers;

import jakarta.validation.Valid;
import java.util.List;
import org.example.numberguessinggame.dtos.GuessRequest;
import org.example.numberguessinggame.dtos.GuessResponse;
import org.example.numberguessinggame.dtos.LeaderboardEntryDto;
import org.example.numberguessinggame.entities.User;
import org.example.numberguessinggame.repositories.UserRepository;
import org.example.numberguessinggame.services.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;
    private final UserRepository userRepository;

    public GameController(GameService gameService, UserRepository userRepository) {
        this.gameService = gameService;
        this.userRepository = userRepository;
    }

    @PostMapping("/guess")
    public GuessResponse guess(Authentication authentication, @Valid @RequestBody GuessRequest request) {
        User user = requireUser(authentication);
        return gameService.guess(user, request);
    }

    // Demo: free +5 turns — disabled; use POST /payment/buy-turns on PaymentController (VNPay) instead.
    // @PostMapping("/buy-turns")
    // public BuyTurnsResponse buyTurns(Authentication authentication) {
    //     User user = requireUser(authentication);
    //     return gameService.buyTurns(user);
    // }

    @GetMapping("/leaderboard")
    public List<LeaderboardEntryDto> leaderboard() {
        return gameService.leaderboard();
    }

    private User requireUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
