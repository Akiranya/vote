package co.mcsky.vote.type;

import co.mcsky.vote.listener.VoteLimiter;
import co.mcsky.vote.listener.WorkUpdater;
import co.mcsky.vote.pool.VotesPool;
import com.google.common.base.Preconditions;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.composite.CompositeTerminable;

import java.util.*;

import static co.mcsky.vote.VoteMain.*;

/**
 * Represents an entire vote for a building game. The design is that each instance of this class manages a distinct plot
 * world. That is, there is a one-to-one relationship between a instance of this class and a plot world.
 */
public class Votes implements Terminable {

    // The game world where this instance manages
    private final String plotWorld;
    // All works in this entire vote, where key is the UUID of owner of work
    private final LinkedHashMap<UUID, Work> works;
    // True, if the vote system is ready (available), otherwise false
    private boolean ready;

    // The backing terminable registry
    private final CompositeTerminable terminableRegistry;

    // A calculator to get statistics about this votes
    private final VotesStats votesStats;

    /**
     * Direct initialization is discouraged, instead use {@link VotesPool} to get an instance.
     *
     * @param plotWorld the plot world this instance manages
     */
    public Votes(String plotWorld) {
        this.plotWorld = plotWorld;
        this.works = new LinkedHashMap<>();
        this.ready = false;

        this.terminableRegistry = CompositeTerminable.create();
        this.terminableRegistry.bind(new WorkUpdater(this));
        this.terminableRegistry.bindModule(new VoteLimiter(this));

        this.votesStats = new VotesStatsImpl(this);

        // Pull plot information when initiated
        pull();
    }

    public String getWorld() {
        return plotWorld;
    }

    public VotesStats getCalc() {
        return votesStats;
    }

    /**
     * @return true, if the system is ready for voting, otherwise false
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * @param ready true, to mark vote system as ready, otherwise false
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    /**
     * @param owner the owner of the work you want to get
     * @return the work of the owner
     */
    public Optional<Work> getWork(UUID owner) {
        return Optional.ofNullable(works.get(owner));
    }

    /**
     * @return all works
     */
    public Collection<Work> getWorkAll() {
        return works.values();
    }

    /**
     * Votes the specified work, with given owner of the vote and whether the vote is marked as absent.
     *
     * @param owner the owner of the work you vote for
     * @param vote  the vote for this work
     */
    public void vote(UUID owner, Vote vote) {
        Preconditions.checkArgument(works.containsKey(owner), "null work entry");
        works.get(owner).vote(vote);
    }

    /**
     * Adds the specified work entry owned by {@code owner}. This should be called when there is a new plot being
     * claimed. Note that adding any existing entry will be overwritten.
     *
     * @param owner the owner of the work
     * @param plot  the plot in which the work is located
     */
    public void createEntry(UUID owner, Plot plot) {
        works.put(owner, Work.create(owner, plot).build());
    }

    /**
     * @param owner the owner of the work to be deleted
     */
    public void deleteEntry(UUID owner) {
        works.remove(owner);
    }

    /**
     * Updates work entries from all legal plots.
     */
    public void pull() {
        plots.getAllPlots().parallelStream()
                .filter(p -> p.hasOwner() && p.getWorldName().equalsIgnoreCase(plotWorld))
                .forEach(p -> createEntry(p.getOwner(), p));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Votes votes = (Votes) o;
        return plotWorld.equals(votes.plotWorld);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plotWorld);
    }

    @Override
    public void close() throws Exception {
        terminableRegistry.close();
    }
}
