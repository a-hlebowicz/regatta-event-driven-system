package pl.ahlebowicz.office.race;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RaceRepository extends JpaRepository<Race, Long> {

    @Query("select coalesce(max(r.raceNumber), 0) from Race r where r.regatta.id = :regattaId")
    int findHighestRaceNumber(@Param("regattaId") Long regattaId);

    @Query("""
            select new pl.ahlebowicz.office.race.RaceRow(r.id, r.raceNumber, r.plannedStart, r.status, r.closedAt)
            from Race r
            where r.regatta.id = :regattaId
            order by r.raceNumber
            """)
    List<RaceRow> findRowsForRegatta(@Param("regattaId") Long regattaId);

    @Query("select r from Race r left join fetch r.finishes where r.id = :raceId")
    Optional<Race> findByIdWithFinishes(@Param("raceId") Long raceId);
}
