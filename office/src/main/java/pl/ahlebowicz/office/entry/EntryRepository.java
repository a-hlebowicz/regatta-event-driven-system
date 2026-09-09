package pl.ahlebowicz.office.entry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    boolean existsByRegattaIdAndSailNumber(Long regattaId, String sailNumber);

    boolean existsByRegattaIdAndCompetitorId(Long regattaId, Long competitorId);

    // Competitor is a separate aggregate with no mapped relation
    @Query("""
            select new pl.ahlebowicz.office.entry.EntryRow(e.id, e.regatta.id, e.sailNumber,
                                                          c.firstName, c.lastName, c.club, e.status, e.version)
            from Entry e
            join Competitor c on c.id = e.competitorId
            where e.regatta.id = :regattaId
            order by e.sailNumber
            """)
    List<EntryRow> findRowsForRegatta(@Param("regattaId") Long regattaId);
}
