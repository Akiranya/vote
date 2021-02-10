package co.mcsky.vote.type;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface Plot {

    void teleport(Player player);

    UUID getOwner();

    String getOwnerName();

    boolean hasOwner();

    boolean isDone();

    String getWorldName();

    String getId();

}
