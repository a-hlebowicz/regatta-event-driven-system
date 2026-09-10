package pl.ahlebowicz.office;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pl.ahlebowicz.office.competitor.CompetitorService;
import pl.ahlebowicz.office.competitor.CreateCompetitorRequest;
import pl.ahlebowicz.office.entry.CreateEntryRequest;
import pl.ahlebowicz.office.entry.EntryService;
import pl.ahlebowicz.office.race.FinishCode;
import pl.ahlebowicz.office.race.FinishInput;
import pl.ahlebowicz.office.race.RaceService;
import pl.ahlebowicz.office.regatta.CreateRegattaRequest;
import pl.ahlebowicz.office.regatta.RegattaService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class DevData implements CommandLineRunner {

    private static final String REGATTA_NAME = "Mistrzostwa Polski Optimist 2026";
    private static final int RACES = 5;

    private static final String[][] COMPETITORS = {
            {"Anna", "Kowalska", "YKP Gdynia"},
            {"Piotr", "Nowak", "SEJK Pogoń Szczecin"},
            {"Maria", "Wiśniewska", "MKS Wisła Kraków"},
            {"Jakub", "Wójcik", "YKP Gdynia"},
            {"Zofia", "Kamińska", "UKS Navigo Puck"},
            {"Michał", "Lewandowski", "SEJK Pogoń Szczecin"},
            {"Julia", "Zielińska", "MOS Sopot"},
            {"Kacper", "Szymański", "UKS Navigo Puck"},
            {"Lena", "Woźniak", "MOS Sopot"},
            {"Filip", "Dąbrowski", "YKP Gdynia"},
            {"Maja", "Kozłowska", "MKS Wisła Kraków"},
            {"Antoni", "Mazur", "UKS Navigo Puck"}
    };

    private final RegattaService regattaService;
    private final CompetitorService competitorService;
    private final EntryService entryService;
    private final RaceService raceService;

    @Override
    public void run(String... args) {
        if (regattaService.listRegattas().stream().anyMatch(regatta -> regatta.name().equals(REGATTA_NAME))) {
            log.info("Dane demonstracyjne już istnieją, pomijam");
            return;
        }

        Long regattaId = createRegatta();
        List<Long> entryIds = acceptEntries(regattaId);
        sailRaces(regattaId, entryIds);

        log.info("Dane demonstracyjne gotowe: regaty {}, {} zgłoszeń, {} wyścigów", regattaId, entryIds.size(), RACES);
    }

    private Long createRegatta() {
        return regattaService.createRegatta(new CreateRegattaRequest(REGATTA_NAME, "Puck", "Optimist",
                LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 14), 90, "4, 8"), List.of(4, 8)).getId();
    }

    private List<Long> acceptEntries(Long regattaId) {
        List<Long> entryIds = new ArrayList<>();

        for (int boat = 0; boat < COMPETITORS.length; boat++) {
            String[] competitor = COMPETITORS[boat];
            Long competitorId = competitorService.registerCompetitor(
                    new CreateCompetitorRequest(competitor[0], competitor[1], competitor[2], "POL-%03d".formatted(boat + 1))).getId();

            entryIds.add(entryService.acceptEntry(regattaId,
                    new CreateEntryRequest(competitorId, "POL-%d".formatted(100 + boat))).getId());
        }

        return entryIds;
    }

    // every race is a rotation of the same fleet so the standings stay interesting, with one
    // retirement and one boat over the line early to exercise the codes other than FINISHED
    private void sailRaces(Long regattaId, List<Long> entryIds) {
        for (int raceNumber = 1; raceNumber <= RACES; raceNumber++) {
            Long raceId = raceService.scheduleRace(regattaId,
                    Instant.now().plus(raceNumber, ChronoUnit.HOURS)).getId();

            List<Long> order = new ArrayList<>(entryIds);
            Collections.rotate(order, raceNumber);

            raceService.closeRace(regattaId, raceId, finishes(order, raceNumber));
        }
    }

    private List<FinishInput> finishes(List<Long> order, int raceNumber) {
        List<FinishInput> finishes = new ArrayList<>();
        int position = 1;

        for (int boat = 0; boat < order.size(); boat++) {
            if (raceNumber == 2 && boat == order.size() - 1) {
                finishes.add(new FinishInput(order.get(boat), FinishCode.RET, null));
            } else if (raceNumber == 4 && boat == 0) {
                finishes.add(new FinishInput(order.get(boat), FinishCode.OCS, null));
            } else {
                finishes.add(new FinishInput(order.get(boat), FinishCode.FINISHED, position++));
            }
        }

        return finishes;
    }
}
