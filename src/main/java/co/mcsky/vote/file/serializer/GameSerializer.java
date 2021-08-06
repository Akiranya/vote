package co.mcsky.vote.file.serializer;

import co.mcsky.vote.object.Game;
import co.mcsky.vote.object.Vote;
import co.mcsky.vote.object.Work;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Logger;

/**
 * Handles de(serialization) for an instance of {@link Game}
 */
public class GameSerializer implements TypeSerializer<Game> {

    private final Logger logger;

    public GameSerializer(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Game deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String world = Objects.requireNonNull(node.node("world").getString(), "world");

        // Pull all works first. At this stage, all works have no votes
        Game votes = new Game(world);

        // Then for each work, we add votes from the file to the work
        ConfigurationNode worksNode = node.node("works");
        for (Map.Entry<Object, ? extends ConfigurationNode> workNode : worksNode.childrenMap().entrySet()) {
            UUID workUuid = UUID.fromString(workNode.getKey().toString());
            if (votes.getWork(workUuid).isPresent()) {
                Work work = votes.getWork(workUuid).get();
                workNode.getValue().node("raters").getList(Vote.class, List.of()).forEach(work::vote);
            } else {
                // Log the data alignment issue
                logger.warning("UUID is presented in file but not in plot database: " + workUuid.toString());
            }
        }

        return votes;
    }

    @Override
    public void serialize(Type type, @Nullable Game votes, ConfigurationNode node) throws SerializationException {
        Objects.requireNonNull(votes, "votes");

        node.node("world").set(votes.getWorld());

        for (Work work : votes.getWorkAll()) {
            ConfigurationNode ownerUuid = node.node("works", work.getOwner().toString());
            ownerUuid.node("raters").setList(Vote.class, new ArrayList<>(work.getVotes()));

            // These nodes will not be deserialized, just for op's information
            ownerUuid.node("name").set(Players.getOffline(work.getOwner()).map(OfflinePlayer::getName).orElse("empty"));
            ownerUuid.node("done").set(work.isDone());
            ownerUuid.node("plot").set(work.getPlot().getId());
        }
    }
}
