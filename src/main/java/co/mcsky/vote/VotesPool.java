package co.mcsky.vote;

import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.composite.CompositeTerminable;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Represents all entire votes for all building games.
 */
public class VotesPool implements Terminable {

    public static VotesPool create() {
        return new VotesPool();
    }

    private final Map<String, Votes> votes;
    private final CompositeTerminable compositeTerminable;

    private String currentWorldName;

    private VotesPool() {
        this.votes = new HashMap<>();
        this.compositeTerminable = CompositeTerminable.create();
    }

    /**
     * Registers a entire vote for the given world. This changes {@link VotesPool#currentWorldName} to the specified
     * one. Duplicate registration does not overwrite existing instances. To delete existing instance, use {@link
     * #unregister(String)}.
     *
     * @param worldName the name of plot world
     */
    public void register(String worldName) {
        currentWorldName = worldName;
        votes.computeIfAbsent(worldName, k -> compositeTerminable.bind(new Votes(k)));
    }

    /**
     * Warning: this deletes all the votes for the given world.
     *
     * @param worldName the world to be unregistered
     */
    public void unregister(String worldName) {
        try {
            votes.remove(worldName).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the current {@link Votes} instance
     */
    public Optional<Votes> acquire() {
        return Optional.ofNullable(votes.get(currentWorldName));
    }

    /**
     * @return the current {@link Votes} instance
     */
    @Nullable
    public Votes get() {
        return votes.get(currentWorldName);
    }

    /**
     * @param worldName the name of plot world
     * @return true, if the plot world already registered, otherwise false
     */
    public boolean contains(String worldName) {
        return votes.containsKey(worldName);
    }

    /**
     * @return true, if there is at least one instance of Votes registered, otherwise false
     */
    public boolean containsAny() {
        return votes.keySet().stream().findAny().isPresent();
    }

    @Override
    public void close() throws Exception {
        // TODO cleanup - save votes to file
        compositeTerminable.close();
    }

}
