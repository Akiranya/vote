package co.mcsky.vote.helper;

import co.mcsky.vote.Votes;
import co.mcsky.vote.type.Vote;
import co.mcsky.vote.type.Work;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides useful methods to get the statistics from the instance of {@link Votes}.
 */
public class VoteCalculator {

    // The instance of Votes from which the statistics is generated
    private final Votes votes;

    public VoteCalculator(Votes votes) {
        this.votes = votes;
    }

    /**
     * This statistics neglects whether the raters are valid or not.
     *
     * @return a stream of UUID of players who have participated the vote.
     */
    public Stream<UUID> rawRaters() {
        return this.votes.getWorks().stream()
                .flatMap(w -> w.getVotes().stream())
                .map(Vote::getRater)
                .distinct();
    }

    /**
     * @return stream of works which are done
     */
    public Stream<Work> done() {
        return this.votes.getWorks().stream()
                .filter(Work::done);
    }

    /**
     * @param rater the owner of the vote
     * @return set of works which have not been voted by the specified vote owner
     */
    public Stream<Work> missed(UUID rater) {
        return this.votes.getWorks().parallelStream()
                .filter(work -> work.invoted(rater));
    }

    /**
     * @param rater the owner of a vote
     * @return true, if the owner has voted all works which are done, otherwise false
     */
    public boolean valid(UUID rater) {
        return missed(rater)
                .filter(Work::done)
                .findAny()
                .isEmpty();
    }

    /**
     * @param rater the owner of a vote
     * @return true, if the owner has NOT voted all works which are done, otherwise false
     */
    public boolean invalid(UUID rater) {
        return !valid(rater);
    }

    /**
     * @return set of UUID of valid raters
     */
    public Set<UUID> validRaters() {
        return rawRaters().filter(this::valid).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * @return set UUID of invalid raters
     */
    public Set<UUID> invalidRaters() {
        return rawRaters().filter(this::invalid).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * @param workOwner the owner of a work
     * @return set of votes of the work, regardless of the vote is valid
     */
    public Set<Vote> rawVotes(UUID workOwner) {
        return this.votes.getWork(workOwner)
                .map(Work::getVotes)
                .orElse(Set.of());
    }

    /**
     * @param workOwner the owner of a work
     * @return stream of all valid votes of the given work
     */
    public Stream<Vote> validVotes(UUID workOwner) {
        return this.votes.getWork(workOwner)
                .map(Work::getVotes)
                .orElse(Set.of()).stream()
                .filter(vote -> valid(vote.getRater()));
    }

    /**
     * @param workOwner the owner of the work
     * @return set of valid red votes of the given work
     */
    public Set<Vote> redVotes(UUID workOwner) {
        return validVotes(workOwner)
                .filter(Vote::isAbsent)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * @param workOwner the owner of the work
     * @return set of valid green votes of the given work
     */
    public Set<Vote> greenVotes(UUID workOwner) {
        return validVotes(workOwner)
                .filter(Vote::isPresent)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * This statistics neglects whether the rater is valid or not.
     *
     * @param rater the rater
     * @return set of works which the rater gave a green vote
     */
    public Set<Work> greenWorks(UUID rater) {
        return this.votes.getWorks().stream()
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
        return this.votes.getWorks().stream()
                .filter(work -> work.voted(rater))
                .filter(work -> work.absent(rater))
                .collect(Collectors.toUnmodifiableSet());
    }

}
