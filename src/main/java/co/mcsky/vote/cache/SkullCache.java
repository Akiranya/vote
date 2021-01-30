package co.mcsky.vote.cache;

import me.lucko.helper.promise.Promise;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class SkullCache {

    // Only store the skull meta, which is enough
    private final Map<UUID, SkullMeta> cacheMap;
    // uuid which are already scheduled to fetch skin
    private final Set<UUID> fetching;

    public static SkullCache create() {
        return new SkullCache();
    }

    private SkullCache() {
        this.cacheMap = new HashMap<>();
        this.fetching = new HashSet<>();
    }

    /**
     * Modifies the head to give it a texture of the given player's UUID.
     *
     * @param uuid uuid of the player
     * @param item the head to be modified
     */
    public void mutateMeta(UUID uuid, ItemStack item) {
        if (!(item.getItemMeta() instanceof SkullMeta)) {
            // item is not a skull
            return;
        }

        if (cacheMap.containsKey(uuid)) {
            // if cached, updates the head's texture
            item.setItemMeta(cacheMap.get(uuid));
            return;
        }

        if (!fetching.contains(uuid)) {
            // else schedule a task to fetch the skull and put it into the map

            fetching.add(uuid);
            Promise.start().thenApplyAsync(n -> {
                // blocking operation, run it async
                return (SkullMeta) SkullCreator.itemWithUuid(item, uuid).getItemMeta();
            }).thenAcceptSync(skullMeta -> {
                // after the async completes, sync put the cache into the map
                cacheMap.put(uuid, skullMeta);
                fetching.remove(uuid);
            });
        }
    }

    public void clear() {
        cacheMap.clear();
    }
}
