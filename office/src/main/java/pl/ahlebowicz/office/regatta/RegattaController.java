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
import pl.ahlebowicz.office.competitor.CompetitorService;
import pl.ahlebowicz.office.entry.CreateEntryRequest;
import pl.ahlebowicz.office.entry.EntryService;

@Controller
@RequestMapping("/regattas")
@RequiredArgsConstructor
public class RegattaController {

    private final RegattaService regattaService;
    private final EntryService entryService;
    private final CompetitorService competitorService;

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

        regattaService.createRegatta(form);

        return "redirect:/regattas";
    }

    @GetMapping("/{regattaId}")
    public String showRegatta(@PathVariable Long regattaId, Model model) {
        model.addAttribute("regatta", regattaService.getRegattaDetails(regattaId));
        model.addAttribute("entries", entryService.listEntries(regattaId));
        model.addAttribute("competitors", competitorService.listCompetitors());
        model.addAttribute("entryForm", CreateEntryRequest.empty());

        return "regatta";
    }
}
