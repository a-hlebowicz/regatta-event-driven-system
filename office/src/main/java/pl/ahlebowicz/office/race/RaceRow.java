package pl.ahlebowicz.office.race;

import java.time.Instant;

public record RaceRow(Long id, int raceNumber, Instant plannedStart, RaceStatus status, Instant closedAt) {
}
