package pl.ahlebowicz.office.race;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.ahlebowicz.office.exception.ConflictException;
import pl.ahlebowicz.office.regatta.RegattaService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/regattas/{regattaId}/races")
@RequiredArgsConstructor
public class RaceController {

    private final RaceService raceService;
    private final RegattaService regattaService;

    @PostMapping
    public String scheduleRace(@PathVariable Long regattaId,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime plannedStart) {
        raceService.scheduleRace(regattaId, plannedStart.atZone(ZoneId.systemDefault()).toInstant());

        return "redirect:/regattas/" + regattaId;
    }

    @GetMapping("/{raceId}/finish")
    public String showFinishForm(@PathVariable Long regattaId, @PathVariable Long raceId, Model model) {
        model.addAttribute("regatta", regattaService.getRegattaDetails(regattaId));
        model.addAttribute("race", raceService.getRace(regattaId, raceId));
        model.addAttribute("finishes", raceService.finishFormRows(regattaId, raceId));
        model.addAttribute("codes", FinishCode.values());

        return "finish";
    }

    @PostMapping("/{raceId}/finish")
    public String closeRace(@PathVariable Long regattaId,
                            @PathVariable Long raceId,
                            @RequestParam("entryId") List<Long> entryIds,
                            @RequestParam("code") List<FinishCode> codes,
                            @RequestParam("position") List<String> positions) {
        raceService.closeRace(regattaId, raceId, toFinishInputs(entryIds, codes, positions));

        return "redirect:/regattas/" + regattaId;
    }

    private List<FinishInput> toFinishInputs(List<Long> entryIds, List<FinishCode> codes, List<String> positions) {
        if (entryIds.size() != codes.size() || entryIds.size() != positions.size()) {
            throw new ConflictException("Niekompletny formularz mety");
        }

        return IntStream.range(0, entryIds.size())
                .mapToObj(row -> new FinishInput(entryIds.get(row), codes.get(row), position(codes.get(row), positions.get(row))))
                .toList();
    }

    private Integer position(FinishCode code, String position) {
        if (code != FinishCode.FINISHED) {
            return null;
        }

        try {
            return Integer.valueOf(position.trim());
        } catch (NumberFormatException exception) {
            throw new ConflictException("Kod FINISHED wymaga podania pozycji na mecie");
        }
    }
}
