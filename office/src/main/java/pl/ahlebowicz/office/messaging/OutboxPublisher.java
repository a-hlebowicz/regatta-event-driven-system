package pl.ahlebowicz.office.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

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
            kafkaTemplate.send(message.getTopic(), message.getMessageKey(), message.getPayload())
                    .get(SEND_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception exception) {
            log.warn("Outbox message {} not published, retrying later: {}", message.getId(), exception.toString());
            return false;
        }
    }
}
