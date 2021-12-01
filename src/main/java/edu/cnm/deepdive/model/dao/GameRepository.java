package edu.cnm.deepdive.model.dao;

import edu.cnm.deepdive.model.entity.Game;
import edu.cnm.deepdive.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID> {

    Optional<Game> findByExternalKey(UUID key);

    Optional<Game> findByExternalKeyAndUser(UUID key, User user);
}
