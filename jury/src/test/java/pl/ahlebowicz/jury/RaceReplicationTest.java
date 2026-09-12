package pl.ahlebowicz.jury;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import pl.ahlebowicz.jury.messaging.Topics;
import pl.ahlebowicz.jury.messaging.inbound.OfficeEventListener;
import pl.ahlebowicz.jury.replication.RaceRefRepository;
import pl.ahlebowicz.jury.replication.RaceRefStatus;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class RaceReplicationTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final long REGATTA_ID = 1L;
    private static final Instant CLOSED_AT = Instant.parse("2026-07-11T14:30:00Z");

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private RaceRefRepository raceRefRepository;

    // the payload carries the finishing order and the entry count as the office sends them,
    // so this also proves that fields outside the jury contract do not break deserialization
    @Test
    void raceClosed_createsRaceRef() {
        publish(raceClosed(701L, 1, 1L));

        await().atMost(TIMEOUT).untilAsserted(() -> assertThat(raceRefRepository.findById(701L))
                .hasValueSatisfying(raceRef -> {
                    assertThat(raceRef.getRegattaId()).isEqualTo(REGATTA_ID);
                    assertThat(raceRef.getRaceNumber()).isEqualTo(1);
                    assertThat(raceRef.getStatus()).isEqualTo(RaceRefStatus.CLOSED);
                    assertThat(raceRef.getClosedAt()).isEqualTo(CLOSED_AT);
                    assertThat(raceRef.getProtestTimeLimitMinutes()).isEqualTo(90);
                    assertThat(raceRef.getVersion()).isEqualTo(1L);
                }));
    }

    @Test
    void sameVersionWithDifferentContent_isIgnored() {
        publish(raceClosed(702L, 2, 1L));
        await().atMost(TIMEOUT).untilAsserted(() -> assertThat(raceRefRepository.findById(702L)).isPresent());

        publish(raceClosed(702L, 22, 1L));
        awaitEveryEarlierRecordProcessed(902L);

        assertThat(raceRefRepository.findById(702L))
                .hasValueSatisfying(raceRef -> assertThat(raceRef.getRaceNumber()).isEqualTo(2));
    }

    @Test
    void olderVersion_doesNotOverwriteNewerRaceRef() {
        publish(raceClosed(703L, 3, 5L));
        await().atMost(TIMEOUT).untilAsserted(() -> assertThat(raceRefRepository.findById(703L)).isPresent());

        publish(raceClosed(703L, 33, 2L));
        awaitEveryEarlierRecordProcessed(903L);

        assertThat(raceRefRepository.findById(703L)).hasValueSatisfying(raceRef -> {
            assertThat(raceRef.getRaceNumber()).isEqualTo(3);
            assertThat(raceRef.getVersion()).isEqualTo(5L);
        });
    }

    private void awaitEveryEarlierRecordProcessed(long markerRaceId) {
        publish(raceClosed(markerRaceId, 99, 1L));
        await().atMost(TIMEOUT)
                .untilAsserted(() -> assertThat(raceRefRepository.findById(markerRaceId)).isPresent());
    }

    private void publish(String payload) {
        kafkaTemplate.send(new ProducerRecord<>(Topics.OFFICE_EVENTS, null, String.valueOf(REGATTA_ID), payload,
                List.of(new RecordHeader(OfficeEventListener.EVENT_TYPE_HEADER, "RaceClosed".getBytes(StandardCharsets.UTF_8)))));
    }

    private String raceClosed(long raceId, int raceNumber, long version) {
        return """
                {
                  "eventId": "%s",
                  "regattaId": %d,
                  "raceId": %d,
                  "raceNumber": %d,
                  "closedAt": "%s",
                  "protestTimeLimitMinutes": 90,
                  "entryCount": 12,
                  "finishes": [
                    {"entryId": 101, "position": 1, "code": "FINISHED"},
                    {"entryId": 102, "position": null, "code": "DNF"}
                  ],
                  "version": %d,
                  "occurredAt": "2026-07-11T14:31:00Z"
                }
                """.formatted(UUID.randomUUID(), REGATTA_ID, raceId, raceNumber, CLOSED_AT, version);
    }
}
