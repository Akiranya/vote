package co.mcsky.comment.io.serializer;

import co.mcsky.comment.object.Game;
import co.mcsky.comment.object.Comment;
import co.mcsky.comment.object.Artwork;
import com.google.common.base.Preconditions;
import me.lucko.helper.utils.Log;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Handles de(serialization) for an instance of {@link Game}
 */
public class GameSerializer implements TypeSerializer<Game> {

    @Override
    public Game deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String world = Objects.requireNonNull(node.node("world").getString(), "world");

        // Pull all works first. At this stage, all works have no votes
        Game game = new Game(world);

        // Then for each work, we add votes from the file to the work
        ConfigurationNode worksNode = node.node("works");
        for (Map.Entry<Object, ? extends ConfigurationNode> workNode : worksNode.childrenMap().entrySet()) {
            UUID workUuid = UUID.fromString(workNode.getKey().toString());
            if (game.getWork(workUuid).isPresent()) {
                Artwork artwork = game.getWork(workUuid).get();
                workNode.getValue().node("reviewers").getList(Comment.class, List.of()).forEach(artwork::comment);
            } else {
                // Log the data alignment issue
                Log.warn("UUID is presented in file but not in plot database: " + workUuid);
            }
        }

        return game;
    }

    @Override
    public void serialize(Type type, @Nullable Game votes, ConfigurationNode node) throws SerializationException {
        Preconditions.checkNotNull(votes, "votes");
        node.node("world").set(votes.getWorld());
        for (Artwork artwork : votes.getWorks()) {
            ConfigurationNode ownerUuid = node.node("works", artwork.getOwner().toString());
            ownerUuid.node("reviewers").setList(Comment.class, new ArrayList<>(artwork.getVotes()));

            // These nodes will not be deserialized, just for op's information
            ownerUuid.node("name").set(Players.getOffline(artwork.getOwner()).map(OfflinePlayer::getName).orElse("empty"));
            ownerUuid.node("done").set(artwork.isDone());
            ownerUuid.node("plot").set(artwork.getPlot().getId());
        }
    }
}
