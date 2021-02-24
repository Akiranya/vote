package co.mcsky.vote.file.serializer;

import co.mcsky.vote.type.Vote;
import co.mcsky.vote.type.Game;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Handles de(serialization) for an instance of {@link Vote} (not {@link Game})
 */
public class VoteSerializer implements TypeSerializer<Vote> {

    private final Logger logger;

    public VoteSerializer(Logger logger) {
        this.logger = logger;
    }

    @Override
    public Vote deserialize(Type type, ConfigurationNode node) throws SerializationException {
        UUID uuid = node.node("uuid").get(UUID.class);
        boolean absent = node.node("absent").getBoolean();
        return new Vote(uuid, absent);
    }

    @Override
    public void serialize(Type type, @Nullable Vote vote, ConfigurationNode node) throws SerializationException {
        if (vote == null) {
            throw new SerializationException("null vote");
        }
        node.node("name").set(vote.getRaterName());
        node.node("uuid").set(vote.getRater());
        node.node("absent").set(vote.isAbsent());
    }
}
