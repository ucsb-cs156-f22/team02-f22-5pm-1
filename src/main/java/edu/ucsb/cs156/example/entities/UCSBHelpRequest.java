package edu.ucsb.cs156.example.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "ucsbhelprequest")
public class UCSBDiningCommons {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String requesterEmail;
  private String teamId;
  private String tableOrBreakoutRoom;
  private LocalDateTime requestTime;
  private String explanation;
  private boolean solved;
}