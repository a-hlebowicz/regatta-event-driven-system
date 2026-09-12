package pl.ahlebowicz.jury.messaging.inbound;

import java.time.Instant;

public record EntryWithdrawn(String eventId,
                             Long regattaId,
                             Long entryId,
                             long version,
                             Instant occurredAt) {
}
