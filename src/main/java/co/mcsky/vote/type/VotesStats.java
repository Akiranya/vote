package co.mcsky.vote.type;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public interface VotesStats {
    /**
     * @param rater the owner of the vote
     * @return set of works which have not been voted by the specified vote owner
     */
    Stream<Work> missed(UUID rater);

    /**
     * @param rater the owner of a vote
     * @return true, if the owner has voted all works which are done, otherwise false
     */
    boolean valid(UUID rater);

    /**
     * @param rater the owner of a vote
     * @return true, if the owner has NOT voted all works which are done, otherwise false
     */
    boolean invalid(UUID rater);

    /**
     * @return stream of UUIDs of players who have participated the vote, regardless of the raters are valid or not
     */
    Stream<UUID> rawRaters();

    /**
     * @return set of UUIDs of valid raters
     */
    Set<UUID> validRaters();

    /**
     * @return set of UUIDs of invalid raters
     */
    Set<UUID> invalidRaters();

    /**
     * @param work the owner of a work
     * @return stream of all valid votes of the given work
     */
    Stream<Vote> validVotes(UUID work);

    /**
     * @param work the owner of the work
     * @return set of valid red votes of the given work
     */
    Set<Vote> redVotes(UUID work);

    /**
     * @param work the owner of the work
     * @return set of valid green votes of the given work
     */
    Set<Vote> greenVotes(UUID work);

    /**
     * This statistics neglects whether the rater is valid or not.
     *
     * @param rater the rater
     * @return set of works which the rater gave a green vote
     */
    Set<Work> greenWorks(UUID rater);

    /**
     * This statistics neglects whether the rater is valid or not.
     *
     * @param rater the rater
     * @return set of works which the rater gave a red vote
     */
    Set<Work> redWorks(UUID rater);
}
