package co.mcsky.vote.object;

import co.mcsky.vote.VoteMain;
import co.mcsky.vote.listener.VoteLimiter;
import co.mcsky.vote.listener.WorkUpdater;
import com.google.common.base.Preconditions;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.composite.CompositeTerminable;

import java.util.*;

/**
 * Represents an entire vote for a building game. The design is that each instance of this class manages a distinct plot
 * world. That is, there is a one-to-one relationship between a instance of this class and a plot world.
 */
public class Game implements Terminable {

    // the game world where this instance manages
    private final String gameWorld;
    // all works in this entire vote, where key is the UUID of owner of work
    private final LinkedHashMap<UUID, Work> workList;
    // the backing terminable registry
    private final CompositeTerminable terminableRegistry;
    // a class to get statistics about this game
    private final GameStats gameStats;
    // true, if the vote system is ready (available), otherwise false
    private boolean ready;

    /**
     * Direct initialization is discouraged, instead use {@link GamePool} to get an instance.
     *
     * @param gameWorld the plot world this instance manages
     */
    public Game(String gameWorld) {
        this.gameWorld = gameWorld;
        this.workList = new LinkedHashMap<>();
        this.ready = false;

        this.terminableRegistry = CompositeTerminable.create();
        this.terminableRegistry.bind(new WorkUpdater(this));
        this.terminableRegistry.bindModule(new VoteLimiter(this));

        this.gameStats = new GameStatsImpl(this);

        // Pull plot information when initiated
        pull();
    }

    public String getWorld() {
        return gameWorld;
    }

    public GameStats getStatistics() {
        return gameStats;
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
        return Optional.ofNullable(workList.get(owner));
    }

    /**
     * @return all works
     */
    public Collection<Work> getWorks() {
        return workList.values();
    }

    /**
     * Votes the specified work, with given owner of the vote and whether the vote is marked as absent.
     *
     * @param owner the owner of the work you vote for
     * @param vote  the vote for this work
     */
    public void vote(UUID owner, Vote vote) {
        Preconditions.checkArgument(workList.containsKey(owner), "null work entry");
        workList.get(owner).vote(vote);
    }

    /**
     * Adds the specified work entry owned by {@code owner}. This should be called when there is a new plot being
     * claimed. Note that adding any existing entry will be overwritten.
     *
     * @param owner the owner of the work
     * @param plot  the plot in which the work is located
     */
    public void createEntry(UUID owner, GamePlot plot) {
        workList.put(owner, new Work(owner, plot));
    }

    /**
     * @param owner the owner of the work to be deleted
     */
    public void deleteEntry(UUID owner) {
        workList.remove(owner);
    }

    /**
     * Updates work entries from all legal plots.
     */
    public void pull() {
        VoteMain.plugin.getPlots().getAllPlots().parallelStream()
                .filter(p -> p.hasOwner() && p.getWorldName().equalsIgnoreCase(gameWorld))
                .forEach(p -> createEntry(p.getOwner(), p));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return gameWorld.equals(game.gameWorld);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameWorld);
    }

    @Override
    public void close() throws Exception {
        terminableRegistry.close();
    }
}
