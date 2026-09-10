package pl.ahlebowicz.office.standings;

import pl.ahlebowicz.office.race.FinishCode;

import java.util.List;

public record ScoringInput(List<Long> entryIds, List<Race> races, List<Integer> discardThresholds) {

    public record Race(int raceNumber, List<Finish> finishes) {
    }

    public record Finish(Long entryId, FinishCode code, Integer position) {
    }
}
