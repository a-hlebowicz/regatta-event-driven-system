package pl.ahlebowicz.office.regatta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RegattaRepository extends JpaRepository<Regatta, Long> {

    @Query("""
            select new pl.ahlebowicz.office.regatta.RegattaRow(r.id, r.name, r.venue, r.boatClass, r.startDate, r.endDate, r.status)
            from Regatta r
            order by r.startDate desc, r.name
            """)
    List<RegattaRow> findAllRows();
}
