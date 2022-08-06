package co.mcsky.comment.object;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface GameStats {
    /**
     * @param reviewer the owner of the comment
     * @return stream of works which have not been voted by the specified comment owner
     */
    Stream<Artwork> ofMissedArtworks(UUID reviewer);

    /**
     * @param reviewer the owner of a comment
     * @return true, if the owner has voted all works which are done, otherwise false
     */
    boolean isValidReviewer(UUID reviewer);

    /**
     * @param reviewer the owner of a comment
     * @return true, if the owner has NOT voted all works which are done, otherwise false
     */
    boolean isInvalidReviewer(UUID reviewer);

    /**
     * @return stream of UUIDs of players who have participated the comment, regardless of the reviewers are valid or not
     */
    Stream<UUID> getReviewers();

    /**
     * @return list of UUIDs of valid reviewers
     */
    List<UUID> getValidReviewers();

    /**
     * @return list of UUIDs of invalid reviewers
     */
    List<UUID> getInvalidReviewers();

    /**
     * @param work the owner of a work
     * @return stream of all valid votes of the given work
     */
    Stream<Comment> ofValidVotes(UUID work);

    /**
     * @param work the owner of the work
     * @return list of valid red votes of the given work
     */
    List<Comment> ofRedVotes(UUID work);

    /**
     * @param work the owner of the work
     * @return list of valid green votes of the given work
     */
    List<Comment> ofGreenVotes(UUID work);

    /**
     * This statistics neglects whether the reviewer is valid or not.
     *
     * @param reviewer the reviewer
     * @return list of works which the reviewer gave a green comment
     */
    List<Artwork> ofGreenWorks(UUID reviewer);

    /**
     * This statistics neglects whether the reviewer is valid or not.
     *
     * @param reviewer the reviewer
     * @return list of works which the reviewer gave a red comment
     */
    List<Artwork> ofRedWorks(UUID reviewer);
}
