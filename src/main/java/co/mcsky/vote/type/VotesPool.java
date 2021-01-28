package co.mcsky.vote.type;

import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.composite.CompositeTerminable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

/**
 * Represents all entire votes for all building games.
 */
public class VotesPool implements Terminable, Iterable<Votes> {

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
     * Adds an instance to the pool. Used when deserialized.
     * <p>
     * Warning: this overwrites any existing instances.
     *
     * @param instance the instance to be added to the pool
     */
    public void register(Votes instance) {
        currentWorldName = instance.getPlotWorld();
        votes.put(currentWorldName, instance);
    }

    /**
     * Registers a entire vote for the given world. This changes {@link VotesPool#currentWorldName} to the specified
     * one. Registering the same world multiple times does not overwrite anything. To delete/overwrite an existing
     * instance, use {@link #unregister(String)} instead.
     *
     * @param worldName the name of plot world
     * @return true, if the world is registered successfully, otherwise false to indicate it already registered
     */
    public boolean register(String worldName) {
        currentWorldName = worldName;
        if (votes.containsKey(worldName)) {
            return false;
        }
        votes.put(worldName, compositeTerminable.bind(new Votes(worldName)));
        return true;
    }

    /**
     * Warning: this deletes all the votes for the given world in the memory.
     *
     * @param worldName the world to be unregistered
     * @return true, if the world is unregistered successfully, otherwise false to indicate it already unregistered
     */
    public boolean unregister(String worldName) {
        try {
            Votes removed = votes.remove(worldName);
            if (removed != null) {
                removed.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @return the current {@link Votes} instance
     */
    public Optional<Votes> peek() {
        return Optional.ofNullable(votes.get(currentWorldName));
    }

    /**
     * @return the specified {@link Votes} instance
     */
    public Optional<Votes> get(String world) {
        return Optional.ofNullable(votes.get(world));
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

    /**
     * @return the current plot world
     */
    public String currentWorld() {
        return currentWorldName;
    }

    @Override
    public void close() throws Exception {
        compositeTerminable.close();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Votes> iterator() {
        return votes.values().iterator();
    }

    @Override
    public void forEach(Consumer<? super Votes> action) {
        votes.values().forEach(action);
    }
}
