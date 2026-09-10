package pl.ahlebowicz.office.race;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ahlebowicz.office.entry.EntryRow;
import pl.ahlebowicz.office.entry.EntryService;
import pl.ahlebowicz.office.entry.EntryStatus;
import pl.ahlebowicz.office.exception.ConflictException;
import pl.ahlebowicz.office.exception.NotFoundException;
import pl.ahlebowicz.office.messaging.OfficeTopics;
import pl.ahlebowicz.office.messaging.OutboxWriter;
import pl.ahlebowicz.office.messaging.outbound.RaceClosed;
import pl.ahlebowicz.office.regatta.Regatta;
import pl.ahlebowicz.office.regatta.RegattaService;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RaceService {

    private final RaceRepository raceRepository;
    private final RegattaService regattaService;
    private final EntryService entryService;
    private final OutboxWriter outboxWriter;

    @Transactional
    public Race scheduleRace(Long regattaId, Instant plannedStart) {
        Regatta regatta = regattaService.getRegatta(regattaId);

        Race race = new Race();
        race.setRegatta(regatta);
        race.setRaceNumber(raceRepository.findHighestRaceNumber(regattaId) + 1);
        race.setPlannedStart(plannedStart);
        race.setStatus(RaceStatus.PLANNED);

        return raceRepository.save(race);
    }

    @Transactional
    public Race closeRace(Long regattaId, Long raceId, List<FinishInput> finishes) {
        Regatta regatta = regattaService.getRegatta(regattaId);
        Race race = getRace(regattaId, raceId);

        if (race.getStatus() == RaceStatus.CLOSED) {
            throw new ConflictException("Wyścig " + race.getRaceNumber() + " jest już zamknięty");
        }

        rejectDuplicatePositions(finishes);

        race.getFinishes().clear();
        finishes.forEach(finish -> race.getFinishes().add(RaceFinish.of(finish.entryId(), finish.code(), finish.position())));
        race.setStatus(RaceStatus.CLOSED);
        race.setClosedAt(Instant.now());

        Race closed = raceRepository.saveAndFlush(race);
        outboxWriter.write(OfficeTopics.OFFICE_EVENTS, regattaId, toEvent(closed, regatta, acceptedEntryCount(regattaId)));

        return closed;
    }

    public Race getRace(Long regattaId, Long raceId) {
        Race race = raceRepository.findById(raceId)
                .orElseThrow(() -> new NotFoundException("Nie ma wyścigu o numerze " + raceId));

        if (!race.getRegatta().getId().equals(regattaId)) {
            throw new NotFoundException("Wyścig " + raceId + " nie należy do tych regat");
        }

        return race;
    }

    public List<RaceRow> listRaces(Long regattaId) {
        return raceRepository.findRowsForRegatta(regattaId);
    }

    @Transactional(readOnly = true)
    public List<FinishRow> finishFormRows(Long regattaId, Long raceId) {
        Race race = raceRepository.findByIdWithFinishes(raceId)
                .orElseThrow(() -> new NotFoundException("Nie ma wyścigu o numerze " + raceId));

        return acceptedEntries(regattaId).stream()
                .map(entry -> toFinishRow(entry, race))
                .toList();
    }

    private FinishRow toFinishRow(EntryRow entry, Race race) {
        return race.getFinishes().stream()
                .filter(finish -> finish.getEntryId().equals(entry.id()))
                .findFirst()
                .map(finish -> new FinishRow(entry.id(), entry.sailNumber(), entry.competitorName(), finish.getCode(), finish.getPosition()))
                .orElseGet(() -> new FinishRow(entry.id(), entry.sailNumber(), entry.competitorName(), FinishCode.FINISHED, null));
    }

    private List<EntryRow> acceptedEntries(Long regattaId) {
        return entryService.listEntries(regattaId).stream()
                .filter(entry -> entry.status() == EntryStatus.ACCEPTED)
                .toList();
    }

    private int acceptedEntryCount(Long regattaId) {
        return acceptedEntries(regattaId).size();
    }

    private void rejectDuplicatePositions(List<FinishInput> finishes) {
        List<Integer> positions = finishes.stream()
                .map(FinishInput::position)
                .filter(java.util.Objects::nonNull)
                .toList();

        Set<Integer> distinct = Set.copyOf(positions);
        if (distinct.size() != positions.size()) {
            throw new ConflictException("Pozycje na mecie muszą być różne");
        }
    }

    private RaceClosed toEvent(Race race, Regatta regatta, int entryCount) {
        List<RaceClosed.Finish> finishes = race.getFinishes().stream()
                .sorted(Comparator.comparing(RaceFinish::getPosition, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(finish -> new RaceClosed.Finish(finish.getEntryId(), finish.getPosition(), finish.getCode().name()))
                .collect(Collectors.toList());

        return new RaceClosed(UUID.randomUUID().toString(),
                regatta.getId(),
                race.getId(),
                race.getRaceNumber(),
                race.getClosedAt(),
                regatta.getProtestTimeLimitMinutes(),
                entryCount,
                finishes,
                race.getVersion(),
                Instant.now());
    }
}
