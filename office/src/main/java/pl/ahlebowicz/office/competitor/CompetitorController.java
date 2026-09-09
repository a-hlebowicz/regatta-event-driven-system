package pl.ahlebowicz.office.competitor;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/competitors")
@RequiredArgsConstructor
public class CompetitorController {

    private final CompetitorService competitorService;

    @GetMapping
    public String listCompetitors(Model model) {
        model.addAttribute("competitors", competitorService.listCompetitors());
        model.addAttribute("competitorForm", CreateCompetitorRequest.empty());

        return "competitors";
    }

    @PostMapping
    public String registerCompetitor(@Valid @ModelAttribute("competitorForm") CreateCompetitorRequest form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("competitors", competitorService.listCompetitors());
            return "competitors";
        }

        competitorService.registerCompetitor(form);

        return "redirect:/competitors";
    }
}
