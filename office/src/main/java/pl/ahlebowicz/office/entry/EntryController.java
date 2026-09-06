package pl.ahlebowicz.office.entry;

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
@RequestMapping("/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    @GetMapping
    public String listEntries(Model model) {
        model.addAttribute("entries", entryService.listEntries());
        model.addAttribute("form", CreateEntryRequest.empty());

        return "entries";
    }

    @PostMapping
    public String acceptEntry(@Valid @ModelAttribute("form") CreateEntryRequest form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("entries", entryService.listEntries());
            return "entries";
        }

        entryService.acceptEntry(form.regattaId(), form.sailNumber());

        return "redirect:/entries";
    }
}
