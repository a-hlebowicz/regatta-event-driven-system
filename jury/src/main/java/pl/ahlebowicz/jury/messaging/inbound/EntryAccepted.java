package pl.ahlebowicz.jury.messaging.inbound;

import java.time.Instant;

public record EntryAccepted(String eventId, Long regattaId, Long entryId, String sailNumber, long version, Instant occurredAt) {
}
