package pl.ahlebowicz.jury;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import pl.ahlebowicz.jury.messaging.Topics;
import pl.ahlebowicz.jury.messaging.inbound.OfficeEventListener;
import pl.ahlebowicz.jury.replication.CompetitorSnapshotRepository;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EntryReplicationTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final long REGATTA_ID = 1L;
    private static final String COMPETITOR_NAME = "Anna Kowalska";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private CompetitorSnapshotRepository competitorSnapshotRepository;

    @Test
    void entryAccepted_createsSnapshot() {
        publish(entryAccepted(101L, "POL-101", 1L));

        await().atMost(TIMEOUT).untilAsserted(() -> assertThat(competitorSnapshotRepository.findById(101L))
                .hasValueSatisfying(snapshot -> {
                    assertThat(snapshot.getRegattaId()).isEqualTo(REGATTA_ID);
                    assertThat(snapshot.getCompetitorId()).isEqualTo(601L);
                    assertThat(snapshot.getCompetitorName()).isEqualTo(COMPETITOR_NAME);
                    assertThat(snapshot.getSailNumber()).isEqualTo("POL-101");
                    assertThat(snapshot.isActive()).isTrue();
                    assertThat(snapshot.getVersion()).isEqualTo(1L);
                }));
    }

    @Test
    void entryAccepted_deliveredTwice_leavesOneUnchangedSnapshot() {
        String event = entryAccepted(102L, "POL-102", 1L);

        publish(event);
        publish(event);
        awaitEveryEarlierRecordProcessed(902L);

        assertThat(competitorSnapshotRepository.findById(102L)).hasValueSatisfying(snapshot -> {
            assertThat(snapshot.getSailNumber()).isEqualTo("POL-102");
            assertThat(snapshot.getVersion()).isEqualTo(1L);
        });
    }

    @Test
    void sameVersionWithDifferentContent_isIgnored() {
        publish(entryAccepted(103L, "POL-103", 1L));
        await().atMost(TIMEOUT).untilAsserted(() -> assertThat(competitorSnapshotRepository.findById(103L)).isPresent());

        publish(entryAccepted(103L, "POL-103-CHANGED", 1L));
        awaitEveryEarlierRecordProcessed(903L);

        assertThat(competitorSnapshotRepository.findById(103L))
                .hasValueSatisfying(snapshot -> assertThat(snapshot.getSailNumber()).isEqualTo("POL-103"));
    }

    @Test
    void olderVersion_doesNotOverwriteNewerSnapshot() {
        publish(entryAccepted(104L, "POL-104-NEW", 5L));
        await().atMost(TIMEOUT).untilAsserted(() -> assertThat(competitorSnapshotRepository.findById(104L)).isPresent());

        publish(entryAccepted(104L, "POL-104-OLD", 2L));
        awaitEveryEarlierRecordProcessed(904L);

        assertThat(competitorSnapshotRepository.findById(104L)).hasValueSatisfying(snapshot -> {
            assertThat(snapshot.getSailNumber()).isEqualTo("POL-104-NEW");
            assertThat(snapshot.getVersion()).isEqualTo(5L);
        });
    }

    @Test
    void unknownEventType_isIgnoredWithoutBlockingReplication() {
        publish("RaceAbandoned", """
                {"eventId": "%s", "regattaId": 1, "raceId": 7, "version": 1}
                """.formatted(UUID.randomUUID()));

        publish(entryAccepted(105L, "POL-105", 1L));

        await().atMost(TIMEOUT)
                .untilAsserted(() -> assertThat(competitorSnapshotRepository.findById(105L)).isPresent());
    }

    @Test
    void entryWithdrawn_deactivatesSnapshotWithoutRemovingIt() {
        publish(entryAccepted(106L, "POL-106", 1L));
        await().atMost(TIMEOUT).untilAsserted(() -> assertThat(competitorSnapshotRepository.findById(106L)).isPresent());

        publish("EntryWithdrawn", entryWithdrawn(106L, 2L));
        awaitEveryEarlierRecordProcessed(906L);

        assertThat(competitorSnapshotRepository.findById(106L)).hasValueSatisfying(snapshot -> {
            assertThat(snapshot.isActive()).isFalse();
            assertThat(snapshot.getSailNumber()).isEqualTo("POL-106");
            assertThat(snapshot.getVersion()).isEqualTo(2L);
        });
    }

    @Test
    void entryWithdrawn_beforeAcceptance_leavesEntryInactive() {
        publish("EntryWithdrawn", entryWithdrawn(107L, 2L));
        await().atMost(TIMEOUT).untilAsserted(() -> assertThat(competitorSnapshotRepository.findById(107L)).isPresent());

        publish(entryAccepted(107L, "POL-107", 1L));
        awaitEveryEarlierRecordProcessed(907L);

        assertThat(competitorSnapshotRepository.findById(107L)).hasValueSatisfying(snapshot -> {
            assertThat(snapshot.isActive()).isFalse();
            assertThat(snapshot.getSailNumber()).isNull();
            assertThat(snapshot.getVersion()).isEqualTo(2L);
        });
    }

    private void awaitEveryEarlierRecordProcessed(long markerEntryId) {
        publish(entryAccepted(markerEntryId, "MARKER-" + markerEntryId, 1L));
        await().atMost(TIMEOUT)
                .untilAsserted(() -> assertThat(competitorSnapshotRepository.findById(markerEntryId)).isPresent());
    }

    private void publish(String payload) {
        publish("EntryAccepted", payload);
    }

    private void publish(String eventType, String payload) {
        kafkaTemplate.send(new ProducerRecord<>(Topics.OFFICE_EVENTS, null, String.valueOf(REGATTA_ID), payload,
                List.of(new RecordHeader(OfficeEventListener.EVENT_TYPE_HEADER, eventType.getBytes(StandardCharsets.UTF_8)))));
    }

    private String entryAccepted(long entryId, String sailNumber, long version) {
        return """
                {
                  "eventId": "%s",
                  "regattaId": %d,
                  "entryId": %d,
                  "competitorId": %d,
                  "competitorName": "%s",
                  "sailNumber": "%s",
                  "version": %d,
                  "occurredAt": "2026-09-07T10:15:30Z"
                }
                """.formatted(UUID.randomUUID(), REGATTA_ID, entryId, entryId + 500, COMPETITOR_NAME, sailNumber, version);
    }

    private String entryWithdrawn(long entryId, long version) {
        return """
                {
                  "eventId": "%s",
                  "regattaId": %d,
                  "entryId": %d,
                  "version": %d,
                  "occurredAt": "2026-09-07T11:20:00Z"
                }
                """.formatted(UUID.randomUUID(), REGATTA_ID, entryId, version);
    }
}
