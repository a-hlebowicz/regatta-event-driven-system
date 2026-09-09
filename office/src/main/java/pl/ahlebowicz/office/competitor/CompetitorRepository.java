package pl.ahlebowicz.office.competitor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CompetitorRepository extends JpaRepository<Competitor, Long> {

    boolean existsByLicenceNumber(String licenceNumber);

    @Query("""
            select new pl.ahlebowicz.office.competitor.CompetitorRow(c.id, c.firstName, c.lastName, c.club, c.licenceNumber)
            from Competitor c
            order by c.lastName, c.firstName
            """)
    List<CompetitorRow> findAllRows();
}
