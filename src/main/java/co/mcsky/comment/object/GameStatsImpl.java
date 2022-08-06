package co.mcsky.comment.object;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Provides useful methods to get the statistics from the instance of {@link Game}.
 */
public record GameStatsImpl(Game game) implements GameStats {

    @Override
    public Stream<Artwork> ofMissedArtworks(UUID reviewer) {
        return game.getWorks().stream().filter(work -> work.hasNotVoted(reviewer));
    }

    @Override
    public boolean isValidReviewer(UUID reviewer) {
        return ofMissedArtworks(reviewer).noneMatch(Artwork::isDone);
    }

    @Override
    public boolean isInvalidReviewer(UUID reviewer) {
        return ofMissedArtworks(reviewer).anyMatch(Artwork::isDone);
    }

    @Override
    public Stream<UUID> getReviewers() {
        return game.getWorks()
                .stream()
                .flatMap(work -> work.getVotes().stream())
                .distinct()
                .map(Comment::getReviewer);
    }

    @Override
    public List<UUID> getValidReviewers() {
        return getReviewers().filter(this::isValidReviewer).toList();
    }

    @Override
    public List<UUID> getInvalidReviewers() {
        return getReviewers().filter(this::isInvalidReviewer).toList();
    }

    @Override
    public Stream<Comment> ofValidVotes(UUID work) {
        return game.getWork(work)
                .stream()
                .flatMap(w -> w.getVotes().stream())
                .filter(v -> isValidReviewer(v.getReviewer()));
    }

    @Override
    public List<Comment> ofRedVotes(UUID work) {
        return ofValidVotes(work).filter(Comment::isAbsent).toList();
    }

    @Override
    public List<Comment> ofGreenVotes(UUID work) {
        return ofValidVotes(work).filter(Comment::isPresent).toList();
    }

    @Override
    public List<Artwork> ofGreenWorks(UUID reviewer) {
        return game.getWorks()
                .stream()
                .filter(work -> work.hasVoted(reviewer) && work.isPresent(reviewer))
                .toList();
    }

    @Override
    public List<Artwork> ofRedWorks(UUID reviewer) {
        return game.getWorks()
                .stream()
                .filter(work -> work.hasVoted(reviewer) && work.isAbsent(reviewer))
                .toList();
    }

}
