package co.mcsky.vote.cache;

import com.destroystokyo.paper.profile.PlayerProfile;
import me.lucko.helper.Schedulers;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.terminable.Terminable;
import org.bukkit.Material;
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

        // Update cache at some interval
        Schedulers.builder()
                .async()
                .afterAndEvery(30, TimeUnit.MINUTES)
                .run(() -> cacheMap.forEach((k, v) -> fetch(k)));
    }

    /**
     * Clear cache immediately.
     */
    public void clear() {
        cacheMap.clear();
        asyncTasks.forEach(Terminable::closeAndReportException);
    }

    /**
     * Modifies the head to give it a texture of the given player's UUID. The {@code item} will leave unchanged if the
     * texture has not been fetched yet when calling this method.
     *
     * @param uuid uuid of the player
     * @param item the head to be modified
     */
    public void mutateMeta(UUID uuid, ItemStack item) {
        // Gets a copy of item meta of the item
        ItemMeta itemMeta = item.getItemMeta();

        if (!(itemMeta instanceof SkullMeta)) {
            // item is not a skull
            return;
        }

        if (cacheMap.containsKey(uuid)) {
            // if cached, updates the skull's texture
            SkullMeta skullMeta = (SkullMeta) itemMeta;
            skullMeta.setPlayerProfile(cacheMap.get(uuid));
            item.setItemMeta(skullMeta);
            return;
        }

        fetch(uuid);
    }

    /**
     * Schedules a task to fetch the skull texture, then put it into the map when the task completes successfully.
     *
     * @param uuid the uuid for the skull texture
     */
    private void fetch(UUID uuid) {
        // schedule the fetch task iff the uuid is not being fetched right now
        if (!fetching.contains(uuid)) {
            // mark this uuid is being fetched
            fetching.add(uuid);

            Promise<Void> fetchTask = Promise.start().thenApplyAsync(n -> {
                ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) SkullCreator.itemWithUuid(item, uuid).getItemMeta();
                return skullMeta.getPlayerProfile();
            }).thenAcceptSync(profile -> {
                cacheMap.put(uuid, profile);

                // fetched, unmark the uuid
                fetching.remove(uuid);
            });

            asyncTasks.add(fetchTask);
        }
    }

}
