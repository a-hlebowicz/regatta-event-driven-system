package pl.ahlebowicz.office.regatta;

import java.time.LocalDate;

public record RegattaRow(Long id,
                         String name,
                         String venue,
                         String boatClass,
                         LocalDate startDate,
                         LocalDate endDate,
                         RegattaStatus status) {
}
