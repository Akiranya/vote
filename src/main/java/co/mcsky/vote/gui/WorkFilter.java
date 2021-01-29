package co.mcsky.vote.gui;

import co.mcsky.vote.type.Work;
import me.lucko.helper.function.Predicates;

import java.util.UUID;
import java.util.function.Predicate;

public final class WorkFilter {

    public static Predicate<Work> all() {
        return Predicates.alwaysTrue();
    }

    public static Predicate<Work> undone(UUID player) {
        return work -> work.isDone() && work.invoted(player);
    }

}
