package edu.cnm.deepdive.model.dao;

import edu.cnm.deepdive.model.entity.Guess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GuessRepository extends JpaRepository<Guess, UUID> {
}
