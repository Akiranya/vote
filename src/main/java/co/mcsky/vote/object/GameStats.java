package co.mcsky.vote.object;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface GameStats {
    /**
     * @param rater the owner of the vote
     * @return stream of works which have not been voted by the specified vote owner
     */
    Stream<Work> ofMissedWorks(UUID rater);

    /**
     * @param rater the owner of a vote
     * @return true, if the owner has voted all works which are done, otherwise false
     */
    boolean isValidRater(UUID rater);

    /**
     * @param rater the owner of a vote
     * @return true, if the owner has NOT voted all works which are done, otherwise false
     */
    boolean isInvalidRater(UUID rater);

    /**
     * @return stream of UUIDs of players who have participated the vote, regardless of the raters are valid or not
     */
    Stream<UUID> getRaters();

    /**
     * @return list of UUIDs of valid raters
     */
    List<UUID> getValidRaters();

    /**
     * @return list of UUIDs of invalid raters
     */
    List<UUID> getInvalidRaters();

    /**
     * @param work the owner of a work
     * @return stream of all valid votes of the given work
     */
    Stream<Vote> ofValidVotes(UUID work);

    /**
     * @param work the owner of the work
     * @return list of valid red votes of the given work
     */
    List<Vote> ofRedVotes(UUID work);

    /**
     * @param work the owner of the work
     * @return list of valid green votes of the given work
     */
    List<Vote> ofGreenVotes(UUID work);

    /**
     * This statistics neglects whether the rater is valid or not.
     *
     * @param rater the rater
     * @return list of works which the rater gave a green vote
     */
    List<Work> ofGreenWorks(UUID rater);

    /**
     * This statistics neglects whether the rater is valid or not.
     *
     * @param rater the rater
     * @return list of works which the rater gave a red vote
     */
    List<Work> ofRedWorks(UUID rater);
}
