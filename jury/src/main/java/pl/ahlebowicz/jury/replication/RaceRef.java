package pl.ahlebowicz.jury.replication;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class RaceRef {

    @Id
    private Long raceId;

    @Column(nullable = false)
    private Long regattaId;

    private Integer raceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RaceRefStatus status;

    private Instant closedAt;

    private Integer protestTimeLimitMinutes;

    @Column(nullable = false)
    private long version;
}
