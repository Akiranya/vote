package co.mcsky.vote.type;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides useful methods to get the statistics from the instance of {@link Votes}.
 */
public class VotesStatsImpl implements VotesStats {

    // The instance of Votes from which the statistics is generated
    private final Votes votes;

    public VotesStatsImpl(Votes votes) {
        this.votes = votes;
    }

    @Override
    public Stream<Work> missed(UUID rater) {
        return votes.getWorkAll().stream().filter(work -> work.invoted(rater));
    }

    @Override
    public boolean valid(UUID rater) {
        return missed(rater).noneMatch(Work::isDone);
    }

    @Override
    public boolean invalid(UUID rater) {
        return missed(rater).anyMatch(Work::isDone);
    }

    @Override
    public Stream<UUID> rawRaters() {
        return votes.getWorkAll().stream()
                .flatMap(w -> w.getVotes().stream())
                .map(Vote::getRater)
                .distinct();
    }

    @Override
    public Set<UUID> validRaters() {
        return rawRaters().filter(this::valid).collect(Collectors.toSet());
    }

    @Override
    public Set<UUID> invalidRaters() {
        return rawRaters().filter(this::invalid).collect(Collectors.toSet());
    }

    @Override
    public Stream<Vote> validVotes(UUID work) {
        return votes.getWork(work)
                .map(Work::getVotes)
                .orElse(Set.of()).stream()
                .filter(vote -> valid(vote.getRater()));
    }

    @Override
    public Set<Vote> redVotes(UUID work) {
        return validVotes(work).filter(Vote::isAbsent).collect(Collectors.toSet());
    }

    @Override
    public Set<Vote> greenVotes(UUID work) {
        return validVotes(work).filter(Vote::isPresent).collect(Collectors.toSet());
    }

    @Override
    public Set<Work> greenWorks(UUID rater) {
        return votes.getWorkAll().stream()
                .filter(work -> work.voted(rater))
                .filter(work -> work.present(rater))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<Work> redWorks(UUID rater) {
        return votes.getWorkAll().stream()
                .filter(work -> work.voted(rater))
                .filter(work -> work.absent(rater))
                .collect(Collectors.toUnmodifiableSet());
    }

}
