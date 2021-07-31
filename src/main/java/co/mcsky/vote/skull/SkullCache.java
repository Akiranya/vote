package co.mcsky.vote.skull;

import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.terminable.Terminable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Head cache pool for non-blocking player head display.
 */
public enum SkullCache implements Terminable {

    // singleton
    INSTANCE;

    // the skull cache
    private final Map<UUID, ItemStack> cache;

    // all scheduled (async) tasks
    private final Set<Terminable> scheduledTasks;

    // uuids which are already scheduled tasks to fetch skin
    private final Set<UUID> fetching;

    // cooldown to fire the fetch complete event
    private final Cooldown eventFireCooldown;

    SkullCache() {
        this.cache = new HashMap<>();
        this.fetching = new HashSet<>();
        this.scheduledTasks = new HashSet<>();
        this.eventFireCooldown = Cooldown.of(1, TimeUnit.SECONDS);

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
    public void itemWithUuid(ItemStack item, UUID id) {
        // Gets a copy of item meta of the item
        ItemMeta itemMeta = item.getItemMeta();

        if (!(itemMeta instanceof SkullMeta)) {
            // item is not a skull
            return;
        }

        if (cache.containsKey(id)) {
            // if cached, updates the skull's texture

            mutateProfile(item, id);
        } else {
            // schedules a task to fetch the skull texture

            fetch(item, id);
        }
    }

    /**
     * Schedules a task to fetch the skull texture of given player's UUID.
     * <p>
     * The texture to be fetched will be put into the cache ONLY when the task completes so that subsequent "get" calls
     * on the cache will return the fetched textures.
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
                    .thenAcceptSync(fetched -> {
                        cache.put(id, fetched);
                        mutateProfile(item, id);

                        // call event so that GUIs containing skull items
                        // may be able to refresh the skull textures automatically
                        // if they listen to this event
                        if (eventFireCooldown.test()) {
                            Events.call(new SkinFetchCompleteEvent());
                        }

                        // fetched, un-mark the id
                        fetching.remove(id);
                    });

            scheduledTasks.add(fetchTask);
        }
    }

    /**
     * Modifies the head to have the texture obtained from the cache.
     *
     * @param origin the head to be modified
     * @param id     the player's UUID
     */
    private void mutateProfile(ItemStack origin, UUID id) {
        SkullMeta originSkullMeta = (SkullMeta) origin.getItemMeta();
        SkullMeta cachedSkullMeta = (SkullMeta) cache.get(id).getItemMeta();
        // only set the player profile to avoid unexpected lore duplicate
        originSkullMeta.setPlayerProfile(cachedSkullMeta.getPlayerProfile());
        origin.setItemMeta(originSkullMeta);
    }

    @Override
    public void close() {
        clear();
    }
}
