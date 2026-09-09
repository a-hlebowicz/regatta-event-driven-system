package pl.ahlebowicz.office.messaging.outbound;

import java.time.Instant;

public record EntryWithdrawn(String eventId,
                             Long regattaId,
                             Long entryId,
                             long version,
                             Instant occurredAt) {
}
