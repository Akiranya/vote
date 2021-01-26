package co.mcsky.vote;

import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.TerminableConsumer;

import javax.annotation.Nonnull;
import java.util.*;

// TODO implement a votes bus
public class VotesBus implements Terminable, TerminableConsumer {

    public static VotesBus create() {
        return new VotesBus();
    }

    private final Map<String, Votes> votes;

    private VotesBus() {
        this.votes = new HashMap<>();
    }

    /**
     * @param worldName
     */
    public void init(String worldName) {
        this.votes.put(worldName, new Votes(worldName));
    }

    /**
     * @param worldName
     * @return
     */
    public boolean exist(String worldName) {
        return this.votes.containsKey(worldName);
    }

    /**
     * @param worldName
     * @return
     */
    public Optional<Votes> get(String worldName) {
        return Optional.ofNullable(this.votes.get(worldName));
    }

    @Override
    public void close() throws Exception {

    }

    @Nonnull
    @Override
    public <T extends AutoCloseable> T bind(@Nonnull T terminable) {
        return null;
    }
}
