package org.example.numberguessinggame.services;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.example.numberguessinggame.dtos.BuyTurnsResponse;
import org.example.numberguessinggame.dtos.GuessRequest;
import org.example.numberguessinggame.dtos.GuessResponse;
import org.example.numberguessinggame.dtos.LeaderboardEntryDto;
import org.example.numberguessinggame.dtos.RoundStatusResponse;
import org.example.numberguessinggame.dtos.StartRoundResponse;
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
    public StartRoundResponse startRound(User user) {
        int secret = randomSecret();
        user.setCurrentSecret(secret);
        userRepository.save(user);
        return new StartRoundResponse(true, "New round started. Guess a number from 1 to 5.");
    }

    @Transactional
    public GuessResponse guess(User user, GuessRequest request) {
        if (user.getCurrentSecret() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No active round. Call start-round before guessing.");
        }
        if (user.getTurns() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No turns left. Buy more turns.");
        }

        int guessed = request.getNumber();
        int secret = user.getCurrentSecret();

        user.setTurns(user.getTurns() - 1);

        boolean correct = guessed == secret;

        GuessLog log = new GuessLog();
        log.setUser(user);
        log.setGuessedNumber(guessed);
        log.setCorrect(correct);
        guessLogRepository.save(log);

        if (correct) {
            user.setScore(user.getScore() + 1);
            user.setCurrentSecret(null);
        }

        userRepository.save(user);

        boolean roundActive = user.getCurrentSecret() != null;
        String message =
                correct
                        ? "Correct! +1 point. Start a new round when ready."
                        : "Wrong guess. Keep trying if you have turns left.";

        return new GuessResponse(correct, user.getTurns(), user.getScore(), roundActive, message);
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
    public RoundStatusResponse roundStatus(User user) {
        User fresh = userRepository
                .findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        boolean roundActive = fresh.getCurrentSecret() != null;
        return new RoundStatusResponse(roundActive, fresh.getTurns(), fresh.getScore());
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
