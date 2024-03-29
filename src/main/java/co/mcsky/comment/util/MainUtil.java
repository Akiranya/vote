package co.mcsky.comment.util;

import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class MainUtil {

    /**
     * @param uuid the uuid of the expected player
     * @return the name of the player, or {@code None} if the player does not exist
     */
    public static String getPlayerName(UUID uuid) {
        return Players.getOffline(uuid).map(OfflinePlayer::getName).orElse("OFFLINE");
    }

}
