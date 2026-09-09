package pl.ahlebowicz.office.competitor;

import jakarta.validation.constraints.NotBlank;

public record CreateCompetitorRequest(@NotBlank String firstName,
                                      @NotBlank String lastName,
                                      @NotBlank String club,
                                      @NotBlank String licenceNumber) {

    public static CreateCompetitorRequest empty() {
        return new CreateCompetitorRequest(null, null, null, null);
    }
}
