package pl.ahlebowicz.office.entry;

public record EntryRow(Long id, Long regattaId, String sailNumber, EntryStatus status, long version) {
}
