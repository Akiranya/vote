package co.mcsky.vote.pool;

import co.mcsky.vote.type.Game;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.composite.CompositeTerminable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents all entire votes for all building games.
 * <p>
 * "pool-design" is used because there are more than one building games.
 */
public enum GamePool implements Terminable, Iterable<Game> {

    // singleton
    INSTANCE;

    // world name - game
    private final Map<String, Game> gameMap;
    private final CompositeTerminable compositeTerminable;

    private String currentWorldName;

    GamePool() {
        this.gameMap = new HashMap<>();
        this.compositeTerminable = CompositeTerminable.create();
    }

    /**
     * Adds an instance to the pool. Used when deserialized.
     * <p>
     * Warning: this overwrites any existing instances.
     *
     * @param instance the instance to be added to the pool
     */
    public void register(Game instance) {
        currentWorldName = instance.getWorld();
        gameMap.put(currentWorldName, instance);
    }

    /**
     * Registers a entire vote for the given world. This changes {@link GamePool#currentWorldName} to the specified one,
     * which changes the instance obtained from {@link #get()}. Registering the same world multiple times does not
     * overwrite anything. To delete/overwrite an existing instance, use {@link #unregister(String)} instead.
     *
     * @param worldName the name of plot world
     * @return true, if the world is registered successfully, otherwise false to indicate it already registered
     */
    public boolean register(String worldName) {
        currentWorldName = worldName;
        if (gameMap.containsKey(worldName)) {
            return false;
        }
        gameMap.put(worldName, compositeTerminable.bind(new Game(worldName)));
        return true;
    }

    /**
     * Warning: this deletes the whole game for the given world in the memory.
     *
     * @param worldName the world to be unregistered
     * @return true, if the world is unregistered successfully, otherwise false to indicate it already unregistered
     */
    public boolean unregister(String worldName) {
        try {
            Game removed = gameMap.remove(worldName);
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
     * @return the current {@link Game} instance
     */
    public Optional<Game> peek() {
        return Optional.ofNullable(gameMap.get(currentWorldName));
    }

    /**
     * @return the specified {@link Game} instance
     */
    public Optional<Game> get(String world) {
        return Optional.ofNullable(gameMap.get(world));
    }

    /**
     * @return the current {@link Game} instance
     */
    @Nullable
    public Game get() {
        return gameMap.get(currentWorldName);
    }

    /**
     * @param worldName the name of plot world
     * @return true, if the plot world already registered, otherwise false
     */
    public boolean contains(String worldName) {
        return gameMap.containsKey(worldName);
    }

    /**
     * @return true, if there is at least one instance of {@link Game} registered, otherwise false
     */
    public boolean containsAny() {
        return gameMap.keySet().stream().findAny().isPresent();
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

    @Override
    public Iterator<Game> iterator() {
        return gameMap.values().iterator();
    }

    @Override
    public void forEach(Consumer<? super Game> action) {
        gameMap.values().forEach(action);
    }
}
