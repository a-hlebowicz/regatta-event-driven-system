package pl.ahlebowicz.office.entry;

import jakarta.validation.constraints.NotBlank;

public record CreateEntryRequest(@NotBlank String sailNumber) {

    public static CreateEntryRequest empty() {
        return new CreateEntryRequest(null);
    }
}
