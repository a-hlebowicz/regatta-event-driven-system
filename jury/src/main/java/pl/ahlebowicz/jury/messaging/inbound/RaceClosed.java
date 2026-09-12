package pl.ahlebowicz.jury.messaging.inbound;

import java.time.Instant;

public record RaceClosed(String eventId,
                         Long regattaId,
                         Long raceId,
                         Integer raceNumber,
                         Instant closedAt,
                         Integer protestTimeLimitMinutes,
                         long version,
                         Instant occurredAt) {
}
