package pl.ahlebowicz.office.messaging.outbound;

import java.time.Instant;
import java.util.List;

public record RaceClosed(String eventId,
                         Long regattaId,
                         Long raceId,
                         int raceNumber,
                         Instant closedAt,
                         int protestTimeLimitMinutes,
                         int entryCount,
                         List<Finish> finishes,
                         long version,
                         Instant occurredAt) {

    public record Finish(Long entryId, Integer position, String code) {
    }
}
