package pl.ahlebowicz.office.standings;

import java.util.List;

public record Standings(List<Integer> raceNumbers, List<StandingsRow> rows) {
}
