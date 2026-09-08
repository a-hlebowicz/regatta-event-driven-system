package pl.ahlebowicz.jury.messaging.inbound;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.ahlebowicz.jury.messaging.Topics;
import pl.ahlebowicz.jury.replication.CompetitorSnapshot;
import pl.ahlebowicz.jury.replication.CompetitorSnapshotRepository;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class OfficeEventListener {

    private final CompetitorSnapshotRepository competitorSnapshotRepository;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = Topics.OFFICE_EVENTS)
    @Transactional
    public void onOfficeEvent(String payload) {
        EntryAccepted event = jsonMapper.readValue(payload, EntryAccepted.class);

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
