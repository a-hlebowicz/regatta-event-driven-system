package pl.ahlebowicz.office.entry;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/regattas/{regattaId}/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService entryService;

    @PostMapping
    public String acceptEntry(@PathVariable Long regattaId,
                              @Valid @ModelAttribute("entryForm") CreateEntryRequest form,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/regattas/" + regattaId;
        }

        entryService.acceptEntry(regattaId, form.sailNumber());

        return "redirect:/regattas/" + regattaId;
    }
}
