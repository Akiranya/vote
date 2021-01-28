package co.mcsky.vote.file.serializer;

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

/**
 * Handles de(serialization) for a instance of {@link Votes}
 */
public class VotesSerializer implements TypeSerializer<Votes> {

    @Override
    public Votes deserialize(Type type, ConfigurationNode node) throws SerializationException {
        // There should be only a key, so using findAny() to get the world name is okay
        String world = Objects.requireNonNull(node.node("world").getString(), "world");

        // Pull all works first
        // At this stage, all works have no votes
        Votes votes = new Votes(world);

        // Then for each work, we add votes from the file to the work
        ConfigurationNode worksNode = node.node("works");
        for (Map.Entry<Object, ? extends ConfigurationNode> workNode : worksNode.childrenMap().entrySet()) {
            UUID workUuid = UUID.fromString(workNode.getKey().toString());
            Work work = votes.getWork(workUuid).orElseThrow(() -> new SerializationException("Work is presented in file but not in plot database"));
            workNode.getValue().node("raters").getList(Vote.class, List.of()).forEach(work::vote);
        }

        return votes;
    }

    @Override
    public void serialize(Type type, @Nullable Votes votes, ConfigurationNode node) throws SerializationException {
        Objects.requireNonNull(votes, "votes");

        node.node("world").set(votes.getPlotWorld());

        for (Work work : votes.getWorks()) {
            ConfigurationNode ownerUuid = node.node("works", work.getOwner().toString());

            // Just for op's information
            ownerUuid.node("name").set(Players.getOffline(work.getOwner()).map(OfflinePlayer::getName).orElse("empty"));
            ownerUuid.node("done").set(work.isDone());
            ownerUuid.node("plot").set(work.getPlot().getId().toString());

            List<Vote> collect = new ArrayList<>(work.getVotes());
            ownerUuid.node("raters").setList(Vote.class, collect);
        }
    }
}
