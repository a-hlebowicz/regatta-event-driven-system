package pl.ahlebowicz.office.standings;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ahlebowicz.office.entry.EntryRow;
import pl.ahlebowicz.office.entry.EntryService;
import pl.ahlebowicz.office.entry.EntryStatus;
import pl.ahlebowicz.office.race.Race;
import pl.ahlebowicz.office.race.RaceFinish;
import pl.ahlebowicz.office.race.RaceRepository;
import pl.ahlebowicz.office.regatta.Regatta;
import pl.ahlebowicz.office.regatta.RegattaService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ScoringService {

    private final EntryService entryService;
    private final RaceRepository raceRepository;
    private final RegattaService regattaService;

    @Transactional(readOnly = true)
    public Standings standings(Long regattaId) {
        Regatta regatta = regattaService.getRegatta(regattaId);
        List<EntryRow> entries = acceptedEntries(regattaId);
        List<Race> races = raceRepository.findClosedWithFinishes(regattaId);

        List<BoatScore> scores = Scoring.calculate(new ScoringInput(
                entries.stream().map(EntryRow::id).toList(),
                races.stream().map(this::toScoringRace).toList(),
                List.copyOf(regatta.getDiscardThresholds())));

        Map<Long, EntryRow> entriesById = entries.stream().collect(java.util.stream.Collectors.toMap(EntryRow::id, Function.identity()));

        return new Standings(races.stream().map(Race::getRaceNumber).toList(), toRows(scores, entriesById));
    }

    private List<StandingsRow> toRows(List<BoatScore> scores, Map<Long, EntryRow> entriesById) {
        return java.util.stream.IntStream.range(0, scores.size())
                .mapToObj(position -> toRow(position + 1, scores.get(position), entriesById))
                .toList();
    }

    private StandingsRow toRow(int rank, BoatScore score, Map<Long, EntryRow> entriesById) {
        EntryRow entry = entriesById.get(score.entryId());

        return new StandingsRow(rank, entry.sailNumber(), entry.competitorName(), entry.club(),
                score.racePoints(), score.total());
    }

    private ScoringInput.Race toScoringRace(Race race) {
        return new ScoringInput.Race(race.getRaceNumber(), race.getFinishes().stream()
                .map(this::toScoringFinish)
                .toList());
    }

    private ScoringInput.Finish toScoringFinish(RaceFinish finish) {
        return new ScoringInput.Finish(finish.getEntryId(), finish.getCode(), finish.getPosition());
    }

    private List<EntryRow> acceptedEntries(Long regattaId) {
        return entryService.listEntries(regattaId).stream()
                .filter(entry -> entry.status() == EntryStatus.ACCEPTED)
                .toList();
    }
}
