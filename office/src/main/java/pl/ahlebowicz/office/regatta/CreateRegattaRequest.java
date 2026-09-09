package pl.ahlebowicz.office.regatta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record CreateRegattaRequest(@NotBlank String name,
                                   @NotBlank String venue,
                                   @NotBlank String boatClass,
                                   @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                   @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                   @NotNull @Positive Integer protestTimeLimitMinutes,
                                   String discardThresholds) {

    public static CreateRegattaRequest empty() {
        return new CreateRegattaRequest(null, null, null, null, null, null, null);
    }
}
