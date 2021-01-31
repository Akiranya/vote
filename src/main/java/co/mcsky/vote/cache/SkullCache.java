package co.mcsky.vote.cache;

import me.lucko.helper.Schedulers;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.terminable.Terminable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SkullCache {

    // the skull cache
    private final Map<UUID, ItemStack> cache;

    // all scheduled (async) tasks
    // track them so that we can terminate when needed
    private final Set<Terminable> scheduledTasks;

    // uuids which are already scheduled tasks to fetch skin
    private final Set<UUID> fetching;

    /**
     * @return a new cache pool for skin textures.
     */
    public static SkullCache create() {
        return new SkullCache();
    }

    private SkullCache() {
        this.cache = new HashMap<>();
        this.fetching = new HashSet<>();
        this.scheduledTasks = new HashSet<>();

        // refresh the cache at certain interval
        Schedulers.builder()
                .async()
                .afterAndEvery(30, TimeUnit.MINUTES)
                .run(() -> {
                    scheduledTasks.forEach(Terminable::closeSilently);
                    cache.forEach((id, item) -> fetch(item, id));
                });
    }

    /**
     * Clears the cache and terminates all scheduled tasks.
     */
    public void clear() {
        scheduledTasks.forEach(Terminable::closeSilently);
        scheduledTasks.clear();
        fetching.clear();
        cache.clear();
    }

    /**
     * Modifies the head to give it a texture of the given player's UUID. The {@code item} will leave unchanged if the
     * texture has not been fetched yet when calling this method.
     *
     * @param id   the UUID of the player
     * @param item the head to be modified
     */
    public void mutateMeta(ItemStack item, UUID id) {
        // Gets a copy of item meta of the item
        ItemMeta itemMeta = item.getItemMeta();

        if (!(itemMeta instanceof SkullMeta)) {
            // item is not a skull
            return;
        }

        if (cache.containsKey(id)) {
            // if cached, updates the skull's texture

            SkullMeta otherSkullMeta = (SkullMeta) itemMeta;
            SkullMeta cachedSkullMeta = (SkullMeta) cache.get(id).getItemMeta();
            // only set the player profile to avoid unexpected lore duplicate
            otherSkullMeta.setPlayerProfile(cachedSkullMeta.getPlayerProfile());
            item.setItemMeta(otherSkullMeta);
        } else {
            // schedules a task to fetch the skull texture,
            // put it in the cache when the task completes successfully

            fetch(item, id);
        }
    }

    /**
     * Schedules a task to fetch the skull texture.
     * <p>
     * The texture will be put into the cache ONLY when the task completes so that subsequent "get" calls on the cache
     * will return the fetched textures.
     *
     * @param id the UUID of the skull texture
     */
    private void fetch(ItemStack item, UUID id) {
        if (!fetching.contains(id)) {
            // schedule the fetch task iff the id is not being fetched right now

            // mark this id is being fetched
            fetching.add(id);

            Promise<Void> fetchTask = Promise.start()
                    .thenApplyAsync(n -> SkullCreator.itemWithUuid(item, id))
                    .thenAcceptSync(fetchedItem -> {
                        cache.put(id, fetchedItem);

                        // fetched, unmark the id
                        fetching.remove(id);
                    });

            scheduledTasks.add(fetchTask);
        }
    }

}
