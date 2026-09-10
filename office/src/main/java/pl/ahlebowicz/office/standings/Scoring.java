package pl.ahlebowicz.office.standings;

import pl.ahlebowicz.office.race.FinishCode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Scoring {

    private Scoring() {
    }

    public static List<BoatScore> calculate(ScoringInput input) {
        int fleetSize = input.entryIds().size();
        int discards = discardCount(input.discardThresholds(), input.races().size());

        Map<Long, List<BoatScore.RacePoints>> pointsByEntry = new LinkedHashMap<>();
        input.entryIds().forEach(entryId -> pointsByEntry.put(entryId, new ArrayList<>()));

        for (ScoringInput.Race race : input.races()) {
            Map<Long, Integer> points = pointsForRace(race, input.entryIds(), fleetSize);
            input.entryIds().forEach(entryId ->
                    pointsByEntry.get(entryId).add(new BoatScore.RacePoints(race.raceNumber(), points.get(entryId), false)));
        }

        return pointsByEntry.entrySet().stream()
                .map(entry -> score(entry.getKey(), entry.getValue(), discards))
                .sorted(Scoring::compare)
                .toList();
    }

    private static int discardCount(List<Integer> thresholds, int racesSailed) {
        return (int) thresholds.stream().filter(threshold -> threshold <= racesSailed).count();
    }

    private static Map<Long, Integer> pointsForRace(ScoringInput.Race race, List<Long> entryIds, int fleetSize) {
        List<ScoringInput.Finish> finished = race.finishes().stream()
                .filter(finish -> finish.code() == FinishCode.FINISHED)
                .sorted(Comparator.comparing(ScoringInput.Finish::position))
                .toList();

        Map<Long, Integer> points = new HashMap<>();
        int correctedPosition = 1;
        for (ScoringInput.Finish finish : finished) {
            points.put(finish.entryId(), correctedPosition++);
        }

        entryIds.forEach(entryId -> points.putIfAbsent(entryId, fleetSize + 1));

        return points;
    }

    private static BoatScore score(Long entryId, List<BoatScore.RacePoints> racePoints, int discards) {
        Set<Integer> discarded = worstResults(racePoints, discards);

        List<BoatScore.RacePoints> marked = new ArrayList<>();
        int total = 0;
        for (int race = 0; race < racePoints.size(); race++) {
            BoatScore.RacePoints points = racePoints.get(race);
            boolean isDiscarded = discarded.contains(race);

            marked.add(new BoatScore.RacePoints(points.raceNumber(), points.points(), isDiscarded));
            if (!isDiscarded) {
                total += points.points();
            }
        }

        return new BoatScore(entryId, marked, total);
    }

    private static Set<Integer> worstResults(List<BoatScore.RacePoints> racePoints, int discards) {
        return new HashSet<>(java.util.stream.IntStream.range(0, racePoints.size())
                .boxed()
                .sorted(Comparator.comparingInt((Integer race) -> racePoints.get(race).points()).reversed())
                .limit(discards)
                .toList());
    }

    private static int compare(BoatScore first, BoatScore second) {
        int byTotal = Integer.compare(first.total(), second.total());
        if (byTotal != 0) {
            return byTotal;
        }

        List<Integer> firstCounted = countedPoints(first);
        List<Integer> secondCounted = countedPoints(second);

        for (int result = 0; result < Math.min(firstCounted.size(), secondCounted.size()); result++) {
            int byResult = Integer.compare(firstCounted.get(result), secondCounted.get(result));
            if (byResult != 0) {
                return byResult;
            }
        }

        return 0;
    }

    private static List<Integer> countedPoints(BoatScore score) {
        return score.racePoints().stream()
                .filter(points -> !points.discarded())
                .map(BoatScore.RacePoints::points)
                .sorted()
                .toList();
    }
}
