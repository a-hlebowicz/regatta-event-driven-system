package pl.ahlebowicz.office.entry;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ahlebowicz.office.competitor.Competitor;
import pl.ahlebowicz.office.competitor.CompetitorService;
import pl.ahlebowicz.office.exception.ConflictException;
import pl.ahlebowicz.office.exception.NotFoundException;
import pl.ahlebowicz.office.messaging.OfficeTopics;
import pl.ahlebowicz.office.messaging.OutboxWriter;
import pl.ahlebowicz.office.messaging.outbound.EntryAccepted;
import pl.ahlebowicz.office.messaging.outbound.EntryWithdrawn;
import pl.ahlebowicz.office.regatta.Regatta;
import pl.ahlebowicz.office.regatta.RegattaService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final RegattaService regattaService;
    private final CompetitorService competitorService;
    private final OutboxWriter outboxWriter;

    @Transactional
    public Entry acceptEntry(Long regattaId, CreateEntryRequest request) {
        Regatta regatta = regattaService.getRegatta(regattaId);
        Competitor competitor = competitorService.getCompetitor(request.competitorId());

        if (entryRepository.existsByRegattaIdAndSailNumber(regattaId, request.sailNumber())) {
            throw new ConflictException("Numer na żaglu " + request.sailNumber() + " jest już zgłoszony w tych regatach");
        }

        if (entryRepository.existsByRegattaIdAndCompetitorId(regattaId, competitor.getId())) {
            throw new ConflictException("Zawodnik " + competitor.fullName() + " jest już zgłoszony do tych regat");
        }

        Entry entry = new Entry();
        entry.setRegatta(regatta);
        entry.setCompetitorId(competitor.getId());
        entry.setSailNumber(request.sailNumber());
        entry.setStatus(EntryStatus.ACCEPTED);

        Entry accepted = entryRepository.saveAndFlush(entry);
        outboxWriter.write(OfficeTopics.OFFICE_EVENTS, regattaId, toEvent(accepted, competitor));

        return accepted;
    }

    @Transactional
    public Entry withdrawEntry(Long regattaId, Long entryId) {
        Entry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new NotFoundException("Nie ma zgłoszenia o numerze " + entryId));

        if (!entry.getRegatta().getId().equals(regattaId)) {
            throw new NotFoundException("Zgłoszenie " + entryId + " nie należy do tych regat");
        }

        if (entry.getStatus() == EntryStatus.WITHDRAWN) {
            throw new ConflictException("Zgłoszenie " + entry.getSailNumber() + " jest już wycofane");
        }

        entry.setStatus(EntryStatus.WITHDRAWN);

        Entry withdrawn = entryRepository.saveAndFlush(entry);
        outboxWriter.write(OfficeTopics.OFFICE_EVENTS, regattaId, toEvent(withdrawn));

        return withdrawn;
    }

    public List<EntryRow> listEntries(Long regattaId) {
        return entryRepository.findRowsForRegatta(regattaId);
    }

    private EntryWithdrawn toEvent(Entry entry) {
        return new EntryWithdrawn(UUID.randomUUID().toString(),
                entry.getRegatta().getId(),
                entry.getId(),
                entry.getVersion(),
                Instant.now());
    }

    private EntryAccepted toEvent(Entry entry, Competitor competitor) {
        return new EntryAccepted(UUID.randomUUID().toString(),
                entry.getRegatta().getId(),
                entry.getId(),
                competitor.getId(),
                competitor.fullName(),
                entry.getSailNumber(),
                entry.getVersion(),
                Instant.now());
    }
}
