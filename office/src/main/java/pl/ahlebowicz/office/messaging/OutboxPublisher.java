package pl.ahlebowicz.office.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    public static final String EVENT_TYPE_HEADER = "event-type";

    private static final Duration SEND_TIMEOUT = Duration.ofSeconds(10);

    private final OutboxMessageRepository outboxMessageRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPending() {
        for (OutboxMessage message : outboxMessageRepository.findTop100BySentAtIsNullOrderByIdAsc()) {
            if (!send(message)) {
                // stop at the first failure so the remaining messages keep their order;
                return;
            }

            message.setSentAt(Instant.now());
        }
    }

    private boolean send(OutboxMessage message) {
        try {
            kafkaTemplate.send(toRecord(message)).get(SEND_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception exception) {
            log.warn("Outbox message {} not published, retrying later: {}", message.getId(), exception.toString());
            return false;
        }
    }

    private ProducerRecord<String, String> toRecord(OutboxMessage message) {
        return new ProducerRecord<>(message.getTopic(), null, message.getMessageKey(), message.getPayload(),
                List.of(new RecordHeader(EVENT_TYPE_HEADER, message.getEventType().getBytes(StandardCharsets.UTF_8))));
    }
}
