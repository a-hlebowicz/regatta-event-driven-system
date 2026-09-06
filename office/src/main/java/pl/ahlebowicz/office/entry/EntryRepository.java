package pl.ahlebowicz.office.entry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    boolean existsByRegattaIdAndSailNumber(Long regattaId, String sailNumber);

    @Query("""
            select new pl.ahlebowicz.office.entry.EntryRow(e.id, e.regattaId, e.sailNumber, e.status, e.version)
            from Entry e
            order by e.regattaId, e.sailNumber
            """)
    List<EntryRow> findAllRows();
}
