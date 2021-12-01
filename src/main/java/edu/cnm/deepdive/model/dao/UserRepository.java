package edu.cnm.deepdive.model.dao;

import edu.cnm.deepdive.model.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByOauthKey(String oauthKey);

  Optional<User> findByExternalKey(UUID externalKey);

  Iterable<User> getAllByOrderByDisplayNameAsc();
}
