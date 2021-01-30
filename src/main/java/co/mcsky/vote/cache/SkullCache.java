package co.mcsky.vote.cache;

import com.destroystokyo.paper.profile.PlayerProfile;
import me.lucko.helper.promise.Promise;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class SkullCache {

    // Only store the profile which contains texture
    private final Map<UUID, PlayerProfile> cacheMap;
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
            // if cached, updates the head's texture
            SkullMeta skullMeta = (SkullMeta) itemMeta;
            skullMeta.setPlayerProfile(cacheMap.get(uuid));
            item.setItemMeta(skullMeta);
            return;
        }

        if (!fetching.contains(uuid)) {
            // else schedule a task to fetch the skull and put it into the map

            fetching.add(uuid);
            Promise.start().thenApplyAsync(n -> {
                // blocking operation, run it async
                SkullMeta skullMeta = (SkullMeta) SkullCreator.itemWithUuid(item, uuid).getItemMeta();
                return skullMeta.getPlayerProfile();
            }).thenAcceptSync(profile -> {
                // after the async completes, sync put the cache into the map
                cacheMap.put(uuid, profile);
                fetching.remove(uuid);
            });
        }

    }

    public void clear() {
        cacheMap.clear();
    }
}
