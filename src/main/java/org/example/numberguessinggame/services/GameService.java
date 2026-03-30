package org.example.numberguessinggame.services;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.example.numberguessinggame.dtos.BuyTurnsResponse;
import org.example.numberguessinggame.dtos.GuessRequest;
import org.example.numberguessinggame.dtos.GuessResponse;
import org.example.numberguessinggame.dtos.LeaderboardEntryDto;
import org.example.numberguessinggame.entities.GuessLog;
import org.example.numberguessinggame.entities.TurnTransaction;
import org.example.numberguessinggame.entities.TurnTransactionType;
import org.example.numberguessinggame.entities.User;
import org.example.numberguessinggame.repositories.GuessLogRepository;
import org.example.numberguessinggame.repositories.TurnTransactionRepository;
import org.example.numberguessinggame.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GameService {

    public static final int BUY_PACK = 5;
    public static final int MIN_NUM = 1;
    public static final int MAX_NUM = 5;

    private final UserRepository userRepository;
    private final GuessLogRepository guessLogRepository;
    private final TurnTransactionRepository turnTransactionRepository;

    public GameService(
            UserRepository userRepository,
            GuessLogRepository guessLogRepository,
            TurnTransactionRepository turnTransactionRepository) {
        this.userRepository = userRepository;
        this.guessLogRepository = guessLogRepository;
        this.turnTransactionRepository = turnTransactionRepository;
    }

    @Transactional
    public GuessResponse guess(User user, GuessRequest request) {
        int guessed = request.getNumber();
        int secret = randomSecret();
        // Keep secret range 1..5, but require an additional acceptance gate so that
        // win rate is ~5% per guess:
        // P(win) = P(guessed == secret) * P(accept) = 1/5 * 1/4 = 5%.
        boolean secretMatched = guessed == secret;
        boolean correct = secretMatched && ThreadLocalRandom.current().nextInt(4) == 0;
        int delta = correct ? 1 : 0;

        // Atomically decrement turns (only when turns > 0) and increment score when correct.
        int updated = userRepository.decrementTurnsAndUpdateScore(user.getId(), delta);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No turns left. Buy more turns.");
        }

        // Reload the user to build an accurate response (turnsRemaining/score).
        User fresh = userRepository
                .findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        GuessLog log = new GuessLog();
        log.setUser(fresh);
        log.setGuessedNumber(guessed);
        log.setCorrect(correct);
        guessLogRepository.save(log);

        String message =
                correct ? "Correct! +1 point." : "Wrong guess. Keep trying if you have turns left.";

        return new GuessResponse(correct, fresh.getTurns(), fresh.getScore(), message);
    }

    @Transactional
    public BuyTurnsResponse buyTurns(User user) {
        user.setTurns(user.getTurns() + BUY_PACK);
        userRepository.save(user);

        TurnTransaction tx = new TurnTransaction();
        tx.setUser(user);
        tx.setType(TurnTransactionType.BUY);
        tx.setAmount(BUY_PACK);
        turnTransactionRepository.save(tx);

        return new BuyTurnsResponse(
                BUY_PACK, user.getTurns(), "Added " + BUY_PACK + " turns.");
    }

    /** Grants turns after successful VNPay payment (same pack size semantics as free buy). */
    @Transactional
    public void grantTurnsFromVnpay(User user, int turns) {
        if (turns <= 0) {
            return;
        }
        user.setTurns(user.getTurns() + turns);
        userRepository.save(user);
        TurnTransaction tx = new TurnTransaction();
        tx.setUser(user);
        tx.setType(TurnTransactionType.VNPAY);
        tx.setAmount(turns);
        turnTransactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> leaderboard() {
        List<User> top = userRepository.findTop10ByOrderByScoreDesc();
        return IntStream.range(0, top.size())
                .mapToObj(i -> new LeaderboardEntryDto(i + 1, top.get(i).getUsername(), top.get(i).getScore()))
                .toList();
    }

    private static int randomSecret() {
        return ThreadLocalRandom.current().nextInt(MIN_NUM, MAX_NUM + 1);
    }
}
