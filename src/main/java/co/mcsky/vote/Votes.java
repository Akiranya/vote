package co.mcsky.vote;

import co.mcsky.vote.helper.VoteCalculator;
import co.mcsky.vote.helper.VoteListener;
import co.mcsky.vote.helper.VoteUpdater;
import co.mcsky.vote.type.Vote;
import co.mcsky.vote.type.Work;
import com.google.common.base.Preconditions;
import com.plotsquared.core.api.PlotAPI;
import com.plotsquared.core.plot.Plot;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.composite.CompositeTerminable;
import me.lucko.helper.terminable.module.TerminableModule;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * The design is that each instance of {@link Votes} manages a distinct plot world. There is a one-to-one relationship
 * between a instance of {@code Votes} and a plot world.
 */
public class Votes implements Terminable, TerminableConsumer {

    // All vote stats, key is the UUID of owner of work
    private final LinkedHashMap<UUID, Work> workMap;
    // The game world where this Votes instance works
    private final String worldName;
    // PlotAPI
    private final PlotAPI api;

    // the backing terminable registry
    private final CompositeTerminable terminableRegistry;

    private final VoteCalculator voteCalculator;

    // True, if the vote system is ready (available), otherwise false
    private boolean ready;

    public Votes(String worldName) {
        this.worldName = worldName;
        this.api = new PlotAPI();
        this.workMap = new LinkedHashMap<>();
        this.voteCalculator = new VoteCalculator(this);
        this.terminableRegistry = CompositeTerminable.create();
        this.ready = false;

        bindModule(new VoteListener(this));
        bind(new VoteUpdater(this));

        // Pull plot information when initiated
        pull();
    }

    public PlotAPI getApi() {
        return api;
    }

    public String getWorldName() {
        return worldName;
    }

    public VoteCalculator getCalculator() {
        return voteCalculator;
    }

    /**
     * @return true, if the system is ready for voting, otherwise false
     */
    public boolean ready() {
        return ready;
    }

    /**
     * @param ready true, to mark vote system as ready, otherwise false to indicate it is ongoing and not ready for
     *              voting
     */
    public void ready(boolean ready) {
        this.ready = ready;
    }

    /**
     * @param workOwner the owner of the work you want to get
     * @return the work of the owner
     */
    public Optional<Work> getWork(UUID workOwner) {
        return Optional.ofNullable(workMap.get(workOwner));
    }

    /**
     * @return all works
     */
    public Collection<Work> getWorks() {
        return workMap.values();
    }

    /**
     * Votes the specified work, with given owner of the vote and whether the vote is marked as absent.
     *
     * @param workOwner the owner of the work you vote for
     * @param vote      the vote for this work
     */
    public void vote(UUID workOwner, Vote vote) {
        Preconditions.checkArgument(workMap.containsKey(workOwner), "Null work entry");
        workMap.get(workOwner).vote(vote);
    }

    /**
     * Adds the specified work entry owned by {@code workOwner}. This should be called when there is a new plot being
     * claimed.
     *
     * @param workOwner the owner of the work
     * @param plot      the plot in which the work is located
     */
    public void createEntry(UUID workOwner, Plot plot) {
        // If there is any duplicate entry, simply overwrite that
        workMap.put(workOwner, Work.builder(workOwner, plot).build());
    }

    /**
     * @param workOwner the owner of the work to be deleted
     */
    public void deleteEntry(UUID workOwner) {
        workMap.remove(workOwner);
    }

    /**
     * Updates work entries for this instance.
     */
    public void pull() {
        // Pull the data from all plots
        getApi().getAllPlots().parallelStream()
                .filter(p -> p.hasOwner() && p.getWorldName().equalsIgnoreCase(worldName))
                .forEach(p -> createEntry(p.getOwner(), p));
    }

    public VoteMain getPlugin() {
        return VoteMain.plugin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Votes votes = (Votes) o;
        return worldName.equals(votes.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName);
    }

    @Nonnull
    @Override
    public <T extends AutoCloseable> T bind(@Nonnull T terminable) {
        return this.terminableRegistry.bind(terminable);
    }

    @Nonnull
    @Override
    public <T extends TerminableModule> T bindModule(@Nonnull T module) {
        return this.terminableRegistry.bindModule(module);
    }

    @Override
    public void close() throws Exception {
        this.terminableRegistry.close();
    }
}
