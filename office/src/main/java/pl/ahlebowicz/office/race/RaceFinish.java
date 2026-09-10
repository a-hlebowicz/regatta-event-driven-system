package pl.ahlebowicz.office.race;

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
public class RaceFinish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long entryId;

    private Integer position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinishCode code;

    public static RaceFinish of(Long entryId, FinishCode code, Integer position) {
        if ((code == FinishCode.FINISHED) != (position != null)) {
            throw new IllegalArgumentException("Pozycja jest wymagana dla FINISHED i zabroniona dla pozostałych kodów");
        }

        RaceFinish finish = new RaceFinish();
        finish.setEntryId(entryId);
        finish.setCode(code);
        finish.setPosition(position);

        return finish;
    }
}
