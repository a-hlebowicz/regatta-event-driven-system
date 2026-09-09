package pl.ahlebowicz.office.regatta;

import java.time.LocalDate;
import java.util.List;

public record RegattaDetails(Long id,
                             String name,
                             String venue,
                             String boatClass,
                             LocalDate startDate,
                             LocalDate endDate,
                             int protestTimeLimitMinutes,
                             RegattaStatus status,
                             List<Integer> discardThresholds) {
}
