package pl.ahlebowicz.office.regatta;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;
import pl.ahlebowicz.office.competitor.CompetitorService;
import pl.ahlebowicz.office.entry.CreateEntryRequest;
import pl.ahlebowicz.office.entry.EntryService;
import pl.ahlebowicz.office.exception.ConflictException;
import pl.ahlebowicz.office.race.RaceService;

@Controller
@RequestMapping("/regattas")
@RequiredArgsConstructor
public class RegattaController {

    private final RegattaService regattaService;
    private final EntryService entryService;
    private final CompetitorService competitorService;
    private final RaceService raceService;

    @GetMapping
    public String listRegattas(Model model) {
        model.addAttribute("regattas", regattaService.listRegattas());
        model.addAttribute("regattaForm", CreateRegattaRequest.empty());

        return "regattas";
    }

    @PostMapping
    public String createRegatta(@Valid @ModelAttribute("regattaForm") CreateRegattaRequest form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("regattas", regattaService.listRegattas());
            return "regattas";
        }

        regattaService.createRegatta(form, toThresholds(form.discardThresholds()));

        return "redirect:/regattas";
    }

    private List<Integer> toThresholds(String thresholds) {
        if (thresholds == null || thresholds.isBlank()) {
            return List.of();
        }

        try {
            return Arrays.stream(thresholds.split(","))
                    .map(String::trim)
                    .filter(threshold -> !threshold.isEmpty())
                    .map(Integer::valueOf)
                    .sorted()
                    .toList();
        } catch (NumberFormatException exception) {
            throw new ConflictException("Progi odrzutów podaje się jako liczby rozdzielone przecinkami, na przykład: 4, 8");
        }
    }

    @GetMapping("/{regattaId}")
    public String showRegatta(@PathVariable Long regattaId, Model model) {
        model.addAttribute("regatta", regattaService.getRegattaDetails(regattaId));
        model.addAttribute("entries", entryService.listEntries(regattaId));
        model.addAttribute("competitors", competitorService.listCompetitors());
        model.addAttribute("entryForm", CreateEntryRequest.empty());
        model.addAttribute("races", raceService.listRaces(regattaId));

        return "regatta";
    }
}
