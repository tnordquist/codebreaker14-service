package edu.cnm.deepdive.service;

import edu.cnm.deepdive.model.dao.GameRepository;
import edu.cnm.deepdive.model.entity.Game;
import edu.cnm.deepdive.model.entity.User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class GameService {

    private final GameRepository repository;

    public GameService(GameRepository repository) {
        this.repository = repository;
    }

    public Optional<Game> get(UUID id) {
        return repository.findByExternalKey(id);
    }

    public Optional<Game> get(UUID id, User user) {
        return repository.findByExternalKeyAndUser(id, user);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public void delete(UUID key, User user) {
        repository
                .findByExternalKeyAndUser(key,user)
                .ifPresent(repository::delete);
    }

}
