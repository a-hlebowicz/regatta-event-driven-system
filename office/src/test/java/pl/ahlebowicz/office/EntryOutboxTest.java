package pl.ahlebowicz.office;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;
import pl.ahlebowicz.office.entry.EntryRepository;
import pl.ahlebowicz.office.entry.EntryService;
import pl.ahlebowicz.office.messaging.OutboxMessageRepository;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EntryOutboxTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    @Autowired
    private EntryService entryService;

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void acceptEntry_writesEntryAndEventInOneTransaction() {
        long entriesBefore = entryRepository.count();
        long messagesBefore = outboxMessageRepository.count();

        transactionTemplate.execute(status -> {
            entryService.acceptEntry(11L, "POL-11");
            status.setRollbackOnly();
            return null;
        });

        assertThat(entryRepository.count()).isEqualTo(entriesBefore);
        assertThat(outboxMessageRepository.count()).isEqualTo(messagesBefore);
    }

    @Test
    void acceptedEntry_isPublishedAndStamped() {
        entryService.acceptEntry(12L, "POL-12");

        await().atMost(TIMEOUT)
                .untilAsserted(() -> assertThat(outboxMessageRepository.findTop100BySentAtIsNullOrderByIdAsc()).isEmpty());
    }
}
