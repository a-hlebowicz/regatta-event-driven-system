package pl.ahlebowicz.office.entry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateEntryRequest(@NotNull @Positive Long regattaId, @NotBlank String sailNumber) {

    public static CreateEntryRequest empty() {
        return new CreateEntryRequest(null, null);
    }
}
