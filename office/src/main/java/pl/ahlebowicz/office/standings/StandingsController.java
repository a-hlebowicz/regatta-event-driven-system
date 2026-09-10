package pl.ahlebowicz.office.standings;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.ahlebowicz.office.regatta.RegattaService;

@Controller
@RequestMapping("/regattas/{regattaId}/standings")
@RequiredArgsConstructor
public class StandingsController {

    private final ScoringService scoringService;
    private final RegattaService regattaService;

    @GetMapping
    public String showStandings(@PathVariable Long regattaId, Model model) {
        model.addAttribute("regatta", regattaService.getRegattaDetails(regattaId));
        model.addAttribute("standings", scoringService.standings(regattaId));

        return "standings";
    }
}
