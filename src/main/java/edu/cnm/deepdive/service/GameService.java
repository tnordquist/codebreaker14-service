package edu.cnm.deepdive.service;

import edu.cnm.deepdive.model.dao.GameRepository;
import edu.cnm.deepdive.model.dao.GuessRepository;
import edu.cnm.deepdive.model.entity.Game;
import edu.cnm.deepdive.model.entity.Guess;
import edu.cnm.deepdive.model.entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
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

    public Guess processGuess(UUID gameKey, Guess guess, User user) {
        return gameRepository
                .findByExternalKeyAndUser(gameKey, user)
                .map((game) -> {
                    int[] guessCodePoints = preprocessGuess(guess, game);
                    int[] codeCodePoints = game
                            .getText()
                            .codePoints()
                            .toArray();
                    computeMatches(guess, guessCodePoints, codeCodePoints);
                    guess.setGame(game);
                    return guessRepository.save(guess);
                })
                .orElseThrow();
    }

    private void computeMatches(Guess guess, int[] guessCodePoints, int[] codeCodePoints) {
        // TODO Compute exact matches and near matches, and use setters of guess to set these values.
    }

    private int[] preprocessGuess(Guess guess, Game game) {
        if (game.isSolved()) {
            throw new IllegalStateException("Game is already solved.");
        }
        Set<Integer> poolCodePoints = game
                .getPool()
                .codePoints()
                .boxed()
                .collect(Collectors.toSet());
        int[] guessCodePoints = guess
                .getText()
                .codePoints()
                .toArray();
        if (guessCodePoints.length != game.getLength()) {
            throw new IllegalArgumentException(String.format(
                    "Guess must have the same length (%d) as the secret code.", game.getLength()));
        }
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

}
