package pl.ahlebowicz.office.competitor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ahlebowicz.office.exception.ConflictException;
import pl.ahlebowicz.office.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetitorService {

    private final CompetitorRepository competitorRepository;

    @Transactional
    public Competitor registerCompetitor(CreateCompetitorRequest request) {
        if (competitorRepository.existsByLicenceNumber(request.licenceNumber())) {
            throw new ConflictException("Zawodnik o numerze licencji " + request.licenceNumber() + " jest już zarejestrowany");
        }

        Competitor competitor = new Competitor();
        competitor.setFirstName(request.firstName());
        competitor.setLastName(request.lastName());
        competitor.setClub(request.club());
        competitor.setLicenceNumber(request.licenceNumber());

        return competitorRepository.save(competitor);
    }

    public Competitor getCompetitor(Long competitorId) {
        return competitorRepository.findById(competitorId).orElseThrow(() -> new NotFoundException("Nie ma zawodnika o numerze " + competitorId));
    }

    public List<CompetitorRow> listCompetitors() {
        return competitorRepository.findAllRows();
    }
}
