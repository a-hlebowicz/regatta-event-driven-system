package pl.ahlebowicz.office.standings;

import java.util.List;

public record StandingsRow(int rank,
                           String sailNumber,
                           String competitorName,
                           String club,
                           List<BoatScore.RacePoints> racePoints,
                           int total) {
}
