package pl.ahlebowicz.office.entry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Entry {

    public static final long INITIAL_VERSION = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long regattaId;

    @Column(nullable = false)
    private String sailNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryStatus status;

    // plain field, not @Version; Hibernate would increment that only on flush after the event carrying this version
    @Column(nullable = false)
    private long version;
}
