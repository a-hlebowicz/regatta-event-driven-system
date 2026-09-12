package pl.ahlebowicz.jury.messaging.inbound;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.ahlebowicz.jury.messaging.Topics;
import pl.ahlebowicz.jury.replication.CompetitorSnapshot;
import pl.ahlebowicz.jury.replication.CompetitorSnapshotRepository;
import pl.ahlebowicz.jury.replication.RaceRef;
import pl.ahlebowicz.jury.replication.RaceRefRepository;
import pl.ahlebowicz.jury.replication.RaceRefStatus;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfficeEventListener {

    public static final String EVENT_TYPE_HEADER = "event-type";

    private static final String ENTRY_ACCEPTED = "EntryAccepted";
    private static final String ENTRY_WITHDRAWN = "EntryWithdrawn";
    private static final String RACE_CLOSED = "RaceClosed";

    private final CompetitorSnapshotRepository competitorSnapshotRepository;
    private final RaceRefRepository raceRefRepository;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = Topics.OFFICE_EVENTS)
    @Transactional
    public void onOfficeEvent(@Payload String payload,
                              @Header(name = EVENT_TYPE_HEADER, required = false) String eventType) {
        switch (eventType) {
            case ENTRY_ACCEPTED -> onEntryAccepted(jsonMapper.readValue(payload, EntryAccepted.class));
            case ENTRY_WITHDRAWN -> onEntryWithdrawn(jsonMapper.readValue(payload, EntryWithdrawn.class));
            case RACE_CLOSED -> onRaceClosed(jsonMapper.readValue(payload, RaceClosed.class));
            case null, default -> log.debug("Ignoring event of type {}", eventType);
        }
    }

    private void onEntryAccepted(EntryAccepted event) {
        CompetitorSnapshot snapshot = competitorSnapshotRepository.findById(event.entryId())
                .orElseGet(CompetitorSnapshot::new);

        if (snapshot.getEntryId() != null && snapshot.getVersion() >= event.version()) {
            return;
        }

        snapshot.setEntryId(event.entryId());
        snapshot.setRegattaId(event.regattaId());
        snapshot.setCompetitorId(event.competitorId());
        snapshot.setCompetitorName(event.competitorName());
        snapshot.setSailNumber(event.sailNumber());
        snapshot.setActive(true);
        snapshot.setVersion(event.version());

        competitorSnapshotRepository.save(snapshot);
    }

    private void onEntryWithdrawn(EntryWithdrawn event) {
        CompetitorSnapshot snapshot = competitorSnapshotRepository.findById(event.entryId())
                .orElseGet(CompetitorSnapshot::new);

        if (snapshot.getEntryId() != null && snapshot.getVersion() >= event.version()) {
            return;
        }

        // the withdrawal may arrive before the acceptance; the snapshot is written anyway,
        // without the sail number and the name, and the late acceptance loses on version
        snapshot.setEntryId(event.entryId());
        snapshot.setRegattaId(event.regattaId());
        snapshot.setActive(false);
        snapshot.setVersion(event.version());

        competitorSnapshotRepository.save(snapshot);
    }

    private void onRaceClosed(RaceClosed event) {
        RaceRef raceRef = raceRefRepository.findById(event.raceId())
                .orElseGet(RaceRef::new);

        if (raceRef.getRaceId() != null && raceRef.getVersion() >= event.version()) {
            return;
        }

        raceRef.setRaceId(event.raceId());
        raceRef.setRegattaId(event.regattaId());
        raceRef.setRaceNumber(event.raceNumber());
        raceRef.setStatus(RaceRefStatus.CLOSED);
        raceRef.setClosedAt(event.closedAt());
        raceRef.setProtestTimeLimitMinutes(event.protestTimeLimitMinutes());
        raceRef.setVersion(event.version());

        raceRefRepository.save(raceRef);
    }
}
