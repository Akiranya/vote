package co.mcsky.comment.io.serializer;

import co.mcsky.comment.object.Game;
import co.mcsky.comment.object.Comment;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.UUID;

/**
 * Handles de(serialization) for an instance of {@link Comment} (not {@link Game})
 */
public class VoteSerializer implements TypeSerializer<Comment> {

    @Override
    public Comment deserialize(Type type, ConfigurationNode node) throws SerializationException {
        UUID uuid = node.node("uuid").get(UUID.class);
        boolean absent = node.node("absent").getBoolean();
        return new Comment(uuid, absent);
    }

    @Override
    public void serialize(Type type, @Nullable Comment comment, ConfigurationNode node) throws SerializationException {
        if (comment == null) {
            throw new SerializationException("null comment");
        }
        node.node("name").set(comment.getReviewerName());
        node.node("uuid").set(comment.getReviewer());
        node.node("absent").set(comment.isAbsent());
    }
}
