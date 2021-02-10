package co.mcsky.vote.type;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides useful methods to get the statistics from the instance of {@link Votes}.
 */
public class VotesCalc {

    // The instance of Votes from which the statistics is generated
    private final Votes votes;

    public VotesCalc(Votes votes) {
        this.votes = votes;
    }

    /**
     * @param rater the owner of the vote
     * @return set of works which have not been voted by the specified vote owner
     */
    public Stream<Work> missed(UUID rater) {
        return votes.getWorkAll().stream().filter(work -> work.invoted(rater));
    }

    /**
     * @param rater the owner of a vote
     * @return true, if the owner has voted all works which are done, otherwise false
     */
    public boolean valid(UUID rater) {
        return missed(rater).noneMatch(Work::isDone);
    }

    /**
     * @param rater the owner of a vote
     * @return true, if the owner has NOT voted all works which are done, otherwise false
     */
    public boolean invalid(UUID rater) {
        return missed(rater).anyMatch(Work::isDone);
    }

    /**
     * @return stream of UUIDs of players who have participated the vote, regardless of the raters are valid or not
     */
    public Stream<UUID> rawRaters() {
        return votes.getWorkAll().stream()
                .flatMap(w -> w.getVotes().stream())
                .map(Vote::getRater)
                .distinct();
    }

    /**
     * @return set of UUIDs of valid raters
     */
    public Set<UUID> validRaters() {
        return rawRaters().filter(this::valid).collect(Collectors.toSet());
    }

    /**
     * @return set of UUIDs of invalid raters
     */
    public Set<UUID> invalidRaters() {
        return rawRaters().filter(this::invalid).collect(Collectors.toSet());
    }

    /**
     * @param work the owner of a work
     * @return stream of all valid votes of the given work
     */
    public Stream<Vote> validVotes(UUID work) {
        return votes.getWork(work)
                .map(Work::getVotes)
                .orElse(Set.of()).stream()
                .filter(vote -> valid(vote.getRater()));
    }

    /**
     * @param work the owner of the work
     * @return set of valid red votes of the given work
     */
    public Set<Vote> redVotes(UUID work) {
        return validVotes(work).filter(Vote::isAbsent).collect(Collectors.toSet());
    }

    /**
     * @param work the owner of the work
     * @return set of valid green votes of the given work
     */
    public Set<Vote> greenVotes(UUID work) {
        return validVotes(work).filter(Vote::isPresent).collect(Collectors.toSet());
    }

    /**
     * This statistics neglects whether the rater is valid or not.
     *
     * @param rater the rater
     * @return set of works which the rater gave a green vote
     */
    public Set<Work> greenWorks(UUID rater) {
        return votes.getWorkAll().stream()
                .filter(work -> work.voted(rater))
                .filter(work -> work.present(rater))
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * This statistics neglects whether the rater is valid or not.
     *
     * @param rater the rater
     * @return set of works which the rater gave a red vote
     */
    public Set<Work> redWorks(UUID rater) {
        return votes.getWorkAll().stream()
                .filter(work -> work.voted(rater))
                .filter(work -> work.absent(rater))
                .collect(Collectors.toUnmodifiableSet());
    }

}