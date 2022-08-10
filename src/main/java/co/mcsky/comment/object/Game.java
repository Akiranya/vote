package co.mcsky.comment.object;

import co.mcsky.comment.Main;
import co.mcsky.comment.listener.ArtworkUpdater;
import co.mcsky.comment.listener.CommentLimiter;
import com.google.common.base.Preconditions;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.composite.CompositeTerminable;

import java.util.*;

/**
 * Represents all the comments for an entire building game. The design is, each
 * instance manages a distinct plot world. That is, there is a one-to-one
 * relationship between an instance of this class and a plot world.
 */
public class Game implements Terminable {

    // the game world where this instance manages
    private final String world;
    // all works in this entire comment, where key is the UUID of owner of work
    private final LinkedHashMap<UUID, Artwork> workMap;
    // the backing terminable registry
    private final CompositeTerminable terminableRegistry;
    // a class to get statistics about this game
    private final GameStats stats;
    // true if the comment system is ready (available), otherwise false
    private boolean openVote;

    /**
     * Direct initialization is discouraged, instead use {@link GamePool} to get
     * an instance.
     *
     * @param world the plot world this instance manages
     */
    public Game(String world) {
        this.world = world;
        this.workMap = new LinkedHashMap<>();
        this.openVote = false;

        this.terminableRegistry = CompositeTerminable.create();
        this.terminableRegistry.bind(new ArtworkUpdater(this));
        this.terminableRegistry.bindModule(new CommentLimiter(this));

        this.stats = new GameStatsImpl(this);

        // Pull plot information when initiated
        pull();
    }

    public String getWorld() {
        return world;
    }

    public GameStats getStatistics() {
        return stats;
    }

    /**
     * @return true, if the system is open for voting, otherwise false
     */
    public boolean isOpenVote() {
        return openVote;
    }

    /**
     * @param open true, to mark comment system as open, otherwise false
     */
    public void setOpenVote(boolean open) {
        this.openVote = open;
    }

    /**
     * @param owner the owner of the work you want to get
     * @return the work of the owner
     */
    public Optional<Artwork> getWork(UUID owner) {
        return Optional.ofNullable(workMap.get(owner));
    }

    /**
     * @return all works
     */
    public List<Artwork> getWorks() {
        return new ArrayList<>(workMap.values());
    }

    /**
     * Votes the specified work, with given owner of the comment and whether the
     * comment is marked as absent.
     *
     * @param owner   the owner of the work you comment for
     * @param comment the comment for this work
     */
    synchronized public void comment(UUID owner, Comment comment) {
        Preconditions.checkArgument(workMap.containsKey(owner), "null work entry");
        workMap.get(owner).comment(comment);
    }

    /**
     * Adds the specified work entry owned by {@code owner}. This should be
     * called when there is a new plot being claimed. Note that adding any
     * existing entry will be overwritten.
     *
     * @param owner the owner of the work
     * @param plot  the plot in which the work is located
     */
    public void createEntry(UUID owner, GamePlot plot) {
        workMap.put(owner, new Artwork(owner, plot));
    }

    /**
     * @param owner the owner of the work to be deleted
     */
    public void deleteEntry(UUID owner) {
        workMap.remove(owner);
    }

    /**
     * Updates work entries from all legal plots.
     */
    public void pull() {
        Main.inst().getPlots().getAllPlots().stream()
                .filter(p -> p.hasOwner() && p.getWorldName().equalsIgnoreCase(world))
                .forEach(p -> createEntry(p.getOwner(), p));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return world.equals(game.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world);
    }

    @Override
    public void close() throws Exception {
        terminableRegistry.close();
    }
}
