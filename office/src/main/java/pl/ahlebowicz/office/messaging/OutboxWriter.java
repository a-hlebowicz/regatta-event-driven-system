package pl.ahlebowicz.office.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OutboxWriter {

    private final OutboxMessageRepository outboxMessageRepository;
    private final JsonMapper jsonMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void write(String topic, Long regattaId, Object event) {
        OutboxMessage message = new OutboxMessage();
        message.setTopic(topic);
        message.setEventType(event.getClass().getSimpleName());
        message.setMessageKey(String.valueOf(regattaId));
        message.setPayload(jsonMapper.writeValueAsString(event));
        message.setCreatedAt(Instant.now());

        outboxMessageRepository.save(message);
    }
}
