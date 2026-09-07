package pl.ahlebowicz.office.messaging.outbound;

import java.time.Instant;

public record EntryAccepted(String eventId,
                            Long regattaId,
                            Long entryId,
                            String sailNumber,
                            long version,
                            Instant occurredAt) {
}
