package pl.ahlebowicz.office.entry;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ahlebowicz.office.exception.ConflictException;
import pl.ahlebowicz.office.messaging.OfficeTopics;
import pl.ahlebowicz.office.messaging.OutboxWriter;
import pl.ahlebowicz.office.messaging.outbound.EntryAccepted;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final OutboxWriter outboxWriter;

    @Transactional
    public Entry acceptEntry(Long regattaId, String sailNumber) {
        if (entryRepository.existsByRegattaIdAndSailNumber(regattaId, sailNumber)) {
            throw new ConflictException("Numer na żaglu " + sailNumber + " jest już zgłoszony w regatach " + regattaId);
        }

        Entry entry = new Entry();
        entry.setRegattaId(regattaId);
        entry.setSailNumber(sailNumber);
        entry.setStatus(EntryStatus.ACCEPTED);

        // flush before building the event: @Version is settled by the flush,
        // and the event has to carry the version the row will have after commit
        Entry accepted = entryRepository.saveAndFlush(entry);
        outboxWriter.write(OfficeTopics.OFFICE_EVENTS, accepted.getRegattaId(), toEvent(accepted));

        return accepted;
    }

    public List<EntryRow> listEntries() {
        return entryRepository.findAllRows();
    }

    private EntryAccepted toEvent(Entry entry) {
        return new EntryAccepted(UUID.randomUUID().toString(),
                entry.getRegattaId(),
                entry.getId(),
                entry.getSailNumber(),
                entry.getVersion(),
                Instant.now());
    }
}
