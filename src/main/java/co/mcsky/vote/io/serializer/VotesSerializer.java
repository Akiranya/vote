package co.mcsky.vote.io.serializer;

import co.mcsky.vote.type.Vote;
import co.mcsky.vote.type.Votes;
import co.mcsky.vote.type.Work;
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
 * Handles de(serialization) for an instance of {@link Votes}
 */
public class VotesSerializer implements TypeSerializer<Votes> {

    private final Logger logger;

    public VotesSerializer(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Votes deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String world = Objects.requireNonNull(node.node("world").getString(), "world");

        // Pull all works first. At this stage, all works have no votes
        Votes votes = new Votes(world);

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
    public void serialize(Type type, @Nullable Votes votes, ConfigurationNode node) throws SerializationException {
        Objects.requireNonNull(votes, "votes");

        node.node("world").set(votes.getPlotWorld());

        for (Work work : votes.getWorks()) {
            ConfigurationNode ownerUuid = node.node("works", work.getOwner().toString());
            ownerUuid.node("raters").setList(Vote.class, new ArrayList<>(work.getVotes()));

            // These nodes will not be deserialized, just for op's information
            ownerUuid.node("name").set(Players.getOffline(work.getOwner()).map(OfflinePlayer::getName).orElse("empty"));
            ownerUuid.node("done").set(work.isDone());
            ownerUuid.node("plot").set(work.getPlot().getId().toString());
        }
    }
}
