package pl.ahlebowicz.office.regatta;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Regatta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String venue;

    @Column(nullable = false)
    private String boatClass;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private int protestTimeLimitMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegattaStatus status;

    @ElementCollection
    @CollectionTable(name = "regatta_discard_threshold", joinColumns = @JoinColumn(name = "regatta_id"))
    @Column(name = "threshold", nullable = false)
    private List<Integer> discardThresholds = new ArrayList<>();
}
