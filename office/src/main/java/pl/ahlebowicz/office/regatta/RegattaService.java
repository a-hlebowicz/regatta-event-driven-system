package pl.ahlebowicz.office.regatta;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ahlebowicz.office.exception.NotFoundException;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegattaService {

    private final RegattaRepository regattaRepository;

    @Transactional
    public Regatta createRegatta(CreateRegattaRequest request) {
        Regatta regatta = new Regatta();
        regatta.setName(request.name());
        regatta.setVenue(request.venue());
        regatta.setBoatClass(request.boatClass());
        regatta.setStartDate(request.startDate());
        regatta.setEndDate(request.endDate());
        regatta.setProtestTimeLimitMinutes(request.protestTimeLimitMinutes());
        regatta.setStatus(RegattaStatus.PLANNED);
        regatta.setDiscardThresholds(parseThresholds(request.discardThresholds()));

        return regattaRepository.save(regatta);
    }

    public Regatta getRegatta(Long regattaId) {
        return regattaRepository.findById(regattaId).orElseThrow(() -> new NotFoundException("Nie ma regat o numerze " + regattaId));
    }

    public List<RegattaRow> listRegattas() {
        return regattaRepository.findAllRows();
    }

    @Transactional(readOnly = true)
    public RegattaDetails getRegattaDetails(Long regattaId) {
        Regatta regatta = getRegatta(regattaId);

        return new RegattaDetails(regatta.getId(), regatta.getName(), regatta.getVenue(), regatta.getBoatClass(),
                regatta.getStartDate(), regatta.getEndDate(), regatta.getProtestTimeLimitMinutes(),
                regatta.getStatus(), List.copyOf(regatta.getDiscardThresholds()));
    }

    private List<Integer> parseThresholds(String thresholds) {
        if (thresholds == null || thresholds.isBlank()) {
            return List.of();
        }

        return Arrays.stream(thresholds.split(","))
                .map(String::trim)
                .filter(threshold -> !threshold.isEmpty())
                .map(Integer::valueOf)
                .sorted()
                .toList();
    }
}
