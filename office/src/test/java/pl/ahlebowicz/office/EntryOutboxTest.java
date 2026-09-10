package pl.ahlebowicz.office;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;
import pl.ahlebowicz.office.competitor.CompetitorService;
import pl.ahlebowicz.office.competitor.CreateCompetitorRequest;
import pl.ahlebowicz.office.entry.CreateEntryRequest;
import pl.ahlebowicz.office.entry.EntryRepository;
import pl.ahlebowicz.office.entry.EntryService;
import pl.ahlebowicz.office.messaging.OutboxMessageRepository;
import pl.ahlebowicz.office.regatta.CreateRegattaRequest;
import pl.ahlebowicz.office.regatta.RegattaService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EntryOutboxTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    @Autowired
    private EntryService entryService;

    @Autowired
    private RegattaService regattaService;

    @Autowired
    private CompetitorService competitorService;

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void acceptEntry_writesEntryAndEventInOneTransaction() {
        Long regattaId = createRegatta("Puchar Zatoki");
        Long competitorId = registerCompetitor("Anna", "Kowalska", "POL-0011");
        long entriesBefore = entryRepository.count();
        long messagesBefore = outboxMessageRepository.count();

        transactionTemplate.execute(status -> {
            entryService.acceptEntry(regattaId, new CreateEntryRequest(competitorId, "POL-11"));
            status.setRollbackOnly();
            return null;
        });

        assertThat(entryRepository.count()).isEqualTo(entriesBefore);
        assertThat(outboxMessageRepository.count()).isEqualTo(messagesBefore);
    }

    @Test
    void acceptedEntry_isPublishedAndStamped() {
        Long regattaId = createRegatta("Regaty Jesienne");
        Long competitorId = registerCompetitor("Piotr", "Nowak", "POL-0012");

        entryService.acceptEntry(regattaId, new CreateEntryRequest(competitorId, "POL-12"));

        await().atMost(TIMEOUT)
                .untilAsserted(() -> assertThat(outboxMessageRepository.findTop100BySentAtIsNullOrderByIdAsc()).isEmpty());
    }

    private Long registerCompetitor(String firstName, String lastName, String licenceNumber) {
        return competitorService.registerCompetitor(
                new CreateCompetitorRequest(firstName, lastName, "YKP Gdynia", licenceNumber)).getId();
    }

    private Long createRegatta(String name) {
        return regattaService.createRegatta(new CreateRegattaRequest(name, "Gdynia", "Optimist",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 3), 60, "4, 8"), List.of(4, 8)).getId();
    }
}
