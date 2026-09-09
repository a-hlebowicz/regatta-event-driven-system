package pl.ahlebowicz.office.entry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEntryRequest(@NotNull Long competitorId, @NotBlank String sailNumber) {

    public static CreateEntryRequest empty() {
        return new CreateEntryRequest(null, null);
    }
}
