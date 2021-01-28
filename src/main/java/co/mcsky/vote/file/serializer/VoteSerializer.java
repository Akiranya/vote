package co.mcsky.vote.file.serializer;

import co.mcsky.vote.type.Vote;
import co.mcsky.vote.type.Votes;
import co.mcsky.vote.util.PlayerUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles de(serialization) for a instance of {@link Vote} (not {@link Votes})
 */
public class VoteSerializer implements TypeSerializer<Vote> {

    private static String toNaiveVote(Vote vote) {
        char separator = ';';
        return "name:" + PlayerUtil.getName(vote.getRater()) + separator +
               "uuid:" + vote.getRater().toString() + separator +
               "absent:" + vote.isAbsent();
    }

    private static Vote fromNaiveVote(String val) {
        String[] split = val.split(";");
        Map<String, String> collect = Arrays.stream(split).collect(Collectors.toMap(s -> s.split(":")[0], s -> s.split(":")[1]));
        return Vote.create(UUID.fromString(collect.get("uuid")))
                .absent(Boolean.parseBoolean(collect.get("absent")))
                .build();
    }

    @Override
    public Vote deserialize(Type type, ConfigurationNode node) {
        return fromNaiveVote(Objects.requireNonNull(node.getString()));
    }

    @Override
    public void serialize(Type type, @Nullable Vote vote, ConfigurationNode node) throws SerializationException {
        node.set(toNaiveVote(Objects.requireNonNull(vote)));
    }
}
