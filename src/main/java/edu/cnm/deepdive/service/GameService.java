package edu.cnm.deepdive.service;

import edu.cnm.deepdive.model.dao.GameRepository;
import edu.cnm.deepdive.model.dao.GuessRepository;
import edu.cnm.deepdive.model.entity.Game;
import edu.cnm.deepdive.model.entity.Guess;
import edu.cnm.deepdive.model.entity.User;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final GuessRepository guessRepository;
    private final Random rng;

    public GameService(GameRepository gameRepository, GuessRepository guessRepository, Random rng) {
        this.gameRepository = gameRepository;
        this.guessRepository = guessRepository;
        this.rng = rng;
    }

    public Optional<Game> get(UUID id) {
        return gameRepository.findByExternalKey(id);
    }

    public Optional<Game> get(UUID id, User user) {
        return gameRepository.findByExternalKeyAndUser(id, user);
    }

    public void delete(UUID id) {
        gameRepository.deleteById(id);
    }

    public void delete(UUID key, User user) {
        gameRepository
                .findByExternalKeyAndUser(key, user)
                .ifPresent(gameRepository::delete);
    }

    public Game startGame(String pool, int length, User user) {
        int[] codePoints = preprocess(pool);
        String code = generateCode(codePoints, length);
        Game game = new Game();
        game.setUser(user);
        game.setPool(new String(codePoints, 0, codePoints.length));
        game.setText(code);
        game.setLength(length);
        game.setPoolSize(codePoints.length);
        return gameRepository.save(game);
    }

    public Guess processGuess(UUID gameKey, Guess guess, User user)
            throws IllegalArgumentException, IllegalStateException, NoSuchElementException {
        return gameRepository
                .findByExternalKeyAndUser(gameKey, user)
                .map((game) -> {
                    int[] guessCodePoints = preprocessGuess(guess, game);
                    int[] codeCodePoints = getCodePoints(game.getText());
                    computeMatches(guess, guessCodePoints, codeCodePoints);
                    guess.setGame(game);
                    return guessRepository.save(guess);
                })
                .orElseThrow();
    }

    private void computeMatches(Guess guess, int[] guessCodePoints, int[] codeCodePoints) {
        int exactMatches = 0;
        Map<Integer, Integer> guessCodePointCounts = new HashMap<>();
        Map<Integer, Integer> codeCodePointCounts = new HashMap<>();
        for (int i = 0; i < guessCodePoints.length; i++) {
            if (guessCodePoints[i] == codeCodePoints[i]) {
                exactMatches++;
            } else {
                guessCodePointCounts.put(guessCodePoints[i],
                        1 + guessCodePointCounts.getOrDefault(guessCodePoints[i], 0));
                codeCodePointCounts.put(codeCodePoints[i],
                        1 + codeCodePointCounts.getOrDefault(codeCodePoints[i], 0));
            }
            guess.setExactMatches(exactMatches);
            int nearMatches = guessCodePointCounts
                    .entrySet()
                    .stream()
                    .mapToInt((entry) ->
                            Math.min(entry.getValue(), codeCodePointCounts.getOrDefault(entry.getKey(), 0)))
                    .sum();
            guess.setNearMatches(nearMatches);
        }
    }

    private int[] preprocessGuess(Guess guess, Game game) {
        if (game.isSolved()) {
            throw new IllegalStateException("Game is already solved.");
        }
        int[] guessCodePoints = getCodePoints(guess.getText());
        if (guessCodePoints.length != game.getLength()) {
            throw new IllegalArgumentException(String.format(
                    "Guess must have the same length (%d) as the secret code.", game.getLength()));
        }
        Set<Integer> poolCodePoints = game
                .getPool()
                .codePoints()
                .boxed()
                .collect(Collectors.toSet());
        if (IntStream
                .of(guessCodePoints)
                .anyMatch((codePoint) -> !poolCodePoints.contains(codePoint))) {
            throw new IllegalArgumentException(String.format("Guess may only contain the characters in the pool \"%s\"", game.getPool()));
        }
        return guessCodePoints;
    }

    private int[] preprocess(String pool) {
        return pool
                .codePoints()
                .peek((codePoint) -> {
                    if (!Character.isDefined(codePoint)) {
                        throw new IllegalArgumentException(
                                String.format("Undefined character in pool: %d", codePoint));
                    } else if (Character.isWhitespace(codePoint)) {
                        throw new IllegalArgumentException(
                                String.format("Whitespace character in pool: %d", codePoint));
                    }
                })
                .sorted()
                .distinct()
                .toArray();
    }

    private String generateCode(int[] codePoints, int length) {
        int[] code = IntStream
                .generate(() -> codePoints[rng.nextInt(codePoints.length)])
                .limit(length)
                .toArray();
        return new String(code, 0, codePoints.length);
    }

    private int[] getCodePoints(String source) {
        return source
                .codePoints()
                .toArray();
    }

}
