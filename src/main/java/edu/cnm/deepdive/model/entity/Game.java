package edu.cnm.deepdive.model.entity;

import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.CreationTimestamp;

@Entity
public class Game {

  @Id
  @GeneratedValue
  @Column(name = "game_id", updatable = false, columnDefinition = "UUID")
  private UUID id;

  @Column(updatable = false, nullable = false, columnDefinition = "UUID")
  private UUID externalKey = UUID.randomUUID();

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false, updatable = false)
  private Date created;

  private String pool;

  private int length;

  private String text;
}
