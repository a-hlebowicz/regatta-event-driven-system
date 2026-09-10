package pl.ahlebowicz.office.standings;

import java.util.List;

public record BoatScore(Long entryId, List<RacePoints> racePoints, int total) {

    public record RacePoints(int raceNumber, int points, boolean discarded) {
    }
}
