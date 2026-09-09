package pl.ahlebowicz.office.entry;

public record EntryRow(Long id,
                       Long regattaId,
                       String sailNumber,
                       String firstName,
                       String lastName,
                       String club,
                       EntryStatus status,
                       long version) {

    public String competitorName() {
        return firstName + " " + lastName;
    }
}
