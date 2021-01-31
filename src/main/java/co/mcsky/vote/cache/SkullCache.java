package co.mcsky.vote.cache;

import com.destroystokyo.paper.profile.PlayerProfile;
import me.lucko.helper.Schedulers;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.terminable.Terminable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class SkullCache {

    // All async tasks
    private final Set<Terminable> asyncTasks;
    // Only store the profile which contains texture
    private final Map<UUID, PlayerProfile> cacheMap;
    // uuid which are already scheduled to fetch skin
    private final Set<UUID> fetching;

    /**
     * @return a new cache pool for skin textures.
     */
    public static SkullCache create() {
        return new SkullCache();
    }

    private SkullCache() {
        this.cacheMap = new HashMap<>();
        this.fetching = new HashSet<>();
        this.asyncTasks = new HashSet<>();

        // Clear cache at some interval
        Schedulers.builder()
                .async()
                .afterAndEvery(30, TimeUnit.MINUTES)
                .run(this::clear);
    }

    /**
     * Clear cache immediately.
     */
    public void clear() {
        asyncTasks.forEach(Terminable::closeSilently);
        asyncTasks.clear();
        cacheMap.clear();
        fetching.clear();
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

        if (cacheMap.containsKey(id)) {
            // if cached, updates the skull's texture

            SkullMeta skullMeta = (SkullMeta) itemMeta;
            // only change the profile to avoid the lore duplicate bug
            skullMeta.setPlayerProfile(cacheMap.get(id));
            item.setItemMeta(skullMeta);
            return;
        }

        fetch(item, id);
    }

    /**
     * Schedules a task to fetch the skull texture, then put it into the map when the task completes successfully.
     *
     * @param id the UUID for the skull texture
     */
    private void fetch(ItemStack item, UUID id) {
        // schedule the fetch task iff the id is not being fetched right now
        if (!fetching.contains(id)) {
            // mark this id is being fetched
            fetching.add(id);

            Promise<Void> fetchTask = Promise.start().thenApplyAsync(n -> {
                SkullMeta skullMeta = (SkullMeta) SkullCreator.itemWithUuid(item, id).getItemMeta();
                return skullMeta.getPlayerProfile();
            }).thenAcceptSync(profile -> {
                cacheMap.put(id, profile);

                // fetched, unmark the id
                fetching.remove(id);
            });

            asyncTasks.add(fetchTask);
        }
    }

}
