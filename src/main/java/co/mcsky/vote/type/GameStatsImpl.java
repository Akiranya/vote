package co.mcsky.vote.type;

import me.lucko.helper.cache.Expiring;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides useful methods to get the statistics from the instance of {@link Game}.
 */
public class GameStatsImpl implements GameStats {

    // The instance of Votes from which the statistics is generated
    private final Game game;

    // All caches
    private final Map<UUID, Expiring<Stream<Work>>> cachedMissedWorks;
    private Expiring<Stream<UUID>> cachedRaters;
    private Expiring<Set<Work>> cachedRedWorks;
    private Expiring<Set<Work>> cachedGreenWorks;

    private static <T> Expiring<T> wrapExpiring(Supplier<T> supp) {
        // define the settings of cache
        return Expiring.suppliedBy(supp, 60, TimeUnit.SECONDS);
    }

    public GameStatsImpl(Game game) {
        this.game = game;
        this.cachedMissedWorks = new HashMap<>();
    }

    @Override
    public Stream<Work> missedWorks(UUID rater) {
        if (!cachedMissedWorks.containsKey(rater)) {
            // if not cached, initialize the cache
            Supplier<Stream<Work>> supp = () -> game.getWorkAll().stream().filter(work -> work.invoted(rater));
            cachedMissedWorks.put(rater, wrapExpiring(supp));
        }
        return cachedMissedWorks.get(rater).get();
    }

    @Override
    public boolean valid(UUID rater) {
        return missedWorks(rater).noneMatch(Work::isDone);
    }

    @Override
    public boolean invalid(UUID rater) {
        return missedWorks(rater).anyMatch(Work::isDone);
    }

    @Override
    public Stream<UUID> raters() {
        if (cachedRaters == null) {
            // if not cached, initialize the cache
            Supplier<Stream<UUID>> supp = () -> game.getWorkAll().stream()
                    .flatMap(w -> w.getVotes().stream())
                    .map(Vote::getRater)
                    .distinct();
            cachedRaters = wrapExpiring(supp);
        }
        return cachedRaters.get();
    }

    @Override
    public Set<UUID> validRaters() {
        return raters().filter(this::valid)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<UUID> invalidRaters() {
        return raters().filter(this::invalid)
                .collect(Collectors.toSet());
    }

    @Override
    public Stream<Vote> validVotes(UUID work) {
        return game.getWork(work)
                .map(Work::getVotes)
                .orElse(Set.of()).stream()
                .filter(vote -> valid(vote.getRater()));
    }

    @Override
    public Set<Vote> redVotes(UUID work) {
        return validVotes(work)
                .filter(Vote::isAbsent)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Vote> greenVotes(UUID work) {
        return validVotes(work)
                .filter(Vote::isPresent)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Work> redWorks(UUID rater) {
        if (cachedRedWorks == null) {
            // if not cached
            Supplier<Set<Work>> supp = () -> game.getWorkAll().stream()
                    .filter(work -> work.voted(rater))
                    .filter(work -> work.absent(rater))
                    .collect(Collectors.toUnmodifiableSet());
            cachedRedWorks = wrapExpiring(supp);
        }
        return cachedRedWorks.get();
    }

    @Override
    public Set<Work> greenWorks(UUID rater) {
        if (cachedGreenWorks == null) {
            // if not cached, initialize the cache
            Supplier<Set<Work>> supp = () -> game.getWorkAll().stream()
                    .filter(work -> work.voted(rater))
                    .filter(work -> work.present(rater))
                    .collect(Collectors.toUnmodifiableSet());
            cachedGreenWorks = wrapExpiring(supp);
        }
        return cachedGreenWorks.get();
    }

}
