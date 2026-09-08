package pl.ahlebowicz.jury.replication;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CompetitorSnapshot {

    @Id
    private Long entryId;

    @Column(nullable = false)
    private Long regattaId;

    @Column(nullable = false)
    private String sailNumber;

    @Column(nullable = false)
    private long version;
}
