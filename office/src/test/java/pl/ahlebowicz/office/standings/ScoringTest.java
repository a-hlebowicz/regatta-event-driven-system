package pl.ahlebowicz.office.standings;

import org.junit.jupiter.api.Test;
import pl.ahlebowicz.office.race.FinishCode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringTest {

    private static final Long ANNA = 1L;
    private static final Long PIOTR = 2L;
    private static final Long MAREK = 3L;

    @Test
    void finishersScoreTheirPosition() {
        List<BoatScore> scores = Scoring.calculate(new ScoringInput(
                List.of(ANNA, PIOTR, MAREK),
                List.of(race(1, finished(ANNA, 1), finished(PIOTR, 2), finished(MAREK, 3))),
                List.of()));

        assertThat(totals(scores)).containsExactly(1, 2, 3);
        assertThat(order(scores)).containsExactly(ANNA, PIOTR, MAREK);
    }

    @Test
    void everyCodeOtherThanFinishedScoresOneMoreThanTheFleet() {
        List<BoatScore> scores = Scoring.calculate(new ScoringInput(
                List.of(ANNA, PIOTR, MAREK),
                List.of(race(1, finished(ANNA, 1), coded(PIOTR, FinishCode.DNF), coded(MAREK, FinishCode.DNS))),
                List.of()));

        assertThat(pointsOf(scores, PIOTR)).containsExactly(4);
        assertThat(pointsOf(scores, MAREK)).containsExactly(4);
    }

    @Test
    void everyBoatRetiring_leavesTheWholeFleetOnTheSameScore() {
        List<BoatScore> scores = Scoring.calculate(new ScoringInput(
                List.of(ANNA, PIOTR, MAREK),
                List.of(race(1, coded(ANNA, FinishCode.RET), coded(PIOTR, FinishCode.OCS), coded(MAREK, FinishCode.DNS))),
                List.of()));

        assertThat(totals(scores)).containsExactly(4, 4, 4);
    }

    @Test
    void entryWithNoRecordInAClosedRace_scoresOneMoreThanTheFleet() {
        List<BoatScore> scores = Scoring.calculate(new ScoringInput(
                List.of(ANNA, PIOTR, MAREK),
                List.of(race(1, finished(ANNA, 1), finished(PIOTR, 2))),
                List.of()));

        assertThat(pointsOf(scores, MAREK)).containsExactly(4);
    }

    @Test
    void positionsAreRenumberedFromOne() {
        List<BoatScore> scores = Scoring.calculate(new ScoringInput(
                List.of(ANNA, PIOTR, MAREK),
                List.of(race(1, finished(ANNA, 1), finished(PIOTR, 3), finished(MAREK, 7))),
                List.of()));

        assertThat(totals(scores)).containsExactly(1, 2, 3);
    }

    @Test
    void fewerRacesThanTheThreshold_discardsNothing() {
        List<BoatScore> scores = Scoring.calculate(new ScoringInput(
                List.of(ANNA, PIOTR),
                List.of(race(1, finished(ANNA, 1), finished(PIOTR, 2)),
                        race(2, finished(ANNA, 2), finished(PIOTR, 1)),
                        race(3, finished(ANNA, 1), finished(PIOTR, 2))),
                List.of(4, 8)));

        assertThat(discardedCount(scores, ANNA)).isZero();
        assertThat(totalOf(scores, ANNA)).isEqualTo(4);
    }

    @Test
    void reachingTheThreshold_dropsTheWorstResult() {
        List<BoatScore> scores = Scoring.calculate(new ScoringInput(
                List.of(ANNA, PIOTR),
                List.of(race(1, finished(ANNA, 1), finished(PIOTR, 2)),
                        race(2, finished(ANNA, 2), finished(PIOTR, 1)),
                        race(3, finished(ANNA, 1), finished(PIOTR, 2)),
                        race(4, finished(ANNA, 1), finished(PIOTR, 2))),
                List.of(4, 8)));

        assertThat(discardedCount(scores, ANNA)).isEqualTo(1);
        assertThat(totalOf(scores, ANNA)).isEqualTo(3);
    }

    @Test
    void reachingTheSecondThreshold_dropsTwoResults() {
        List<BoatScore> scores = Scoring.calculate(new ScoringInput(
                List.of(ANNA, PIOTR),
                List.of(race(1, finished(ANNA, 2), finished(PIOTR, 1)),
                        race(2, finished(ANNA, 2), finished(PIOTR, 1)),
                        race(3, finished(ANNA, 1), finished(PIOTR, 2)),
                        race(4, finished(ANNA, 1), finished(PIOTR, 2)),
                        race(5, finished(ANNA, 1), finished(PIOTR, 2)),
                        race(6, finished(ANNA, 1), finished(PIOTR, 2)),
                        race(7, finished(ANNA, 1), finished(PIOTR, 2)),
                        race(8, finished(ANNA, 1), finished(PIOTR, 2))),
                List.of(4, 8)));

        assertThat(discardedCount(scores, ANNA)).isEqualTo(2);
        assertThat(totalOf(scores, ANNA)).isEqualTo(6);
    }

    @Test
    void equalTotals_areBrokenByTheBetterSingleResult() {
        List<BoatScore> scores = Scoring.calculate(new ScoringInput(
                List.of(ANNA, PIOTR),
                List.of(race(1, finished(ANNA, 1), finished(PIOTR, 2)),
                        race(2, finished(PIOTR, 1), finished(ANNA, 2))),
                List.of()));

        assertThat(totalOf(scores, ANNA)).isEqualTo(totalOf(scores, PIOTR));
        assertThat(order(scores)).containsExactly(ANNA, PIOTR);
    }

    @Test
    void noRaces_leavesEveryoneOnZero() {
        List<BoatScore> scores = Scoring.calculate(new ScoringInput(List.of(ANNA, PIOTR), List.of(), List.of(4)));

        assertThat(totals(scores)).containsExactly(0, 0);
    }

    private ScoringInput.Race race(int raceNumber, ScoringInput.Finish... finishes) {
        return new ScoringInput.Race(raceNumber, List.of(finishes));
    }

    private ScoringInput.Finish finished(Long entryId, int position) {
        return new ScoringInput.Finish(entryId, FinishCode.FINISHED, position);
    }

    private ScoringInput.Finish coded(Long entryId, FinishCode code) {
        return new ScoringInput.Finish(entryId, code, null);
    }

    private List<Integer> totals(List<BoatScore> scores) {
        return scores.stream().map(BoatScore::total).toList();
    }

    private List<Long> order(List<BoatScore> scores) {
        return scores.stream().map(BoatScore::entryId).toList();
    }

    private int totalOf(List<BoatScore> scores, Long entryId) {
        return scoreOf(scores, entryId).total();
    }

    private List<Integer> pointsOf(List<BoatScore> scores, Long entryId) {
        return scoreOf(scores, entryId).racePoints().stream().map(BoatScore.RacePoints::points).toList();
    }

    private long discardedCount(List<BoatScore> scores, Long entryId) {
        return scoreOf(scores, entryId).racePoints().stream().filter(BoatScore.RacePoints::discarded).count();
    }

    private BoatScore scoreOf(List<BoatScore> scores, Long entryId) {
        return scores.stream().filter(score -> score.entryId().equals(entryId)).findFirst().orElseThrow();
    }
}
