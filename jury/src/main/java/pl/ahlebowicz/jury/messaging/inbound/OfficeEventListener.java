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
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfficeEventListener {

    public static final String EVENT_TYPE_HEADER = "event-type";

    private static final String ENTRY_ACCEPTED = "EntryAccepted";

    private final CompetitorSnapshotRepository competitorSnapshotRepository;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = Topics.OFFICE_EVENTS)
    @Transactional
    public void onOfficeEvent(@Payload String payload,
                              @Header(name = EVENT_TYPE_HEADER, required = false) String eventType) {
        if (!ENTRY_ACCEPTED.equals(eventType)) {
            log.debug("Ignoring event of type {}", eventType);
            return;
        }

        onEntryAccepted(jsonMapper.readValue(payload, EntryAccepted.class));
    }

    private void onEntryAccepted(EntryAccepted event) {
        CompetitorSnapshot snapshot = competitorSnapshotRepository.findById(event.entryId())
                .orElseGet(CompetitorSnapshot::new);

        if (snapshot.getEntryId() != null && snapshot.getVersion() >= event.version()) {
            return;
        }

        snapshot.setEntryId(event.entryId());
        snapshot.setRegattaId(event.regattaId());
        snapshot.setSailNumber(event.sailNumber());
        snapshot.setVersion(event.version());

        competitorSnapshotRepository.save(snapshot);
    }
}
