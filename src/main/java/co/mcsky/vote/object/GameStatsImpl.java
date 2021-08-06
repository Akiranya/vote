package co.mcsky.vote.object;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Provides useful methods to get the statistics from the instance of {@link Game}.
 */
public record GameStatsImpl(Game game) implements GameStats {

    @Override
    public Stream<Work> ofMissedWorks(UUID rater) {
        return game.getWorks().stream().filter(work -> work.hasNotVoted(rater));
    }

    @Override
    public boolean isValidRater(UUID rater) {
        return ofMissedWorks(rater).noneMatch(Work::isDone);
    }

    @Override
    public boolean isInvalidRater(UUID rater) {
        return ofMissedWorks(rater).anyMatch(Work::isDone);
    }

    @Override
    public Stream<UUID> getRaters() {
        return game.getWorks()
                .stream()
                .flatMap(work -> work.getVotes().stream())
                .distinct()
                .map(Vote::getRater);
    }

    @Override
    public List<UUID> getValidRaters() {
        return getRaters().filter(this::isValidRater).toList();
    }

    @Override
    public List<UUID> getInvalidRaters() {
        return getRaters().filter(this::isInvalidRater).toList();
    }

    @Override
    public Stream<Vote> ofValidVotes(UUID work) {
        return game.getWork(work)
                .stream()
                .flatMap(w -> w.getVotes().stream())
                .filter(v -> isValidRater(v.getRater()));
    }

    @Override
    public List<Vote> ofRedVotes(UUID work) {
        return ofValidVotes(work).filter(Vote::isAbsent).toList();
    }

    @Override
    public List<Vote> ofGreenVotes(UUID work) {
        return ofValidVotes(work).filter(Vote::isPresent).toList();
    }

    @Override
    public List<Work> ofGreenWorks(UUID rater) {
        return game.getWorks()
                .stream()
                .filter(work -> work.hasVoted(rater) && work.isPresent(rater))
                .toList();
    }

    @Override
    public List<Work> ofRedWorks(UUID rater) {
        return game.getWorks()
                .stream()
                .filter(work -> work.hasVoted(rater) && work.isAbsent(rater))
                .toList();
    }

}
