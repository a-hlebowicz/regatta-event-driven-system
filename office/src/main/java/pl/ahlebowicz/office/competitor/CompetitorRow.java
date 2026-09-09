package pl.ahlebowicz.office.competitor;

public record CompetitorRow(Long id, String firstName, String lastName, String club, String licenceNumber) {

    public String fullName() {
        return firstName + " " + lastName;
    }
}
