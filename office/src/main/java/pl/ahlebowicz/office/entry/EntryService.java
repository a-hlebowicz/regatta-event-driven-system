package pl.ahlebowicz.office.entry;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ahlebowicz.office.exception.ConflictException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;

    @Transactional
    public Entry acceptEntry(Long regattaId, String sailNumber) {
        if (entryRepository.existsByRegattaIdAndSailNumber(regattaId, sailNumber)) {
            throw new ConflictException("Numer na żaglu " + sailNumber + " jest już zgłoszony w regatach " + regattaId);
        }

        Entry entry = new Entry();
        entry.setRegattaId(regattaId);
        entry.setSailNumber(sailNumber);
        entry.setStatus(EntryStatus.ACCEPTED);
        entry.setVersion(Entry.INITIAL_VERSION);

        return entryRepository.save(entry);
    }

    public List<EntryRow> listEntries() {
        return entryRepository.findAllRows();
    }
}
