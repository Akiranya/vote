package co.mcsky.vote.gui;

import co.mcsky.vote.object.Work;
import me.lucko.helper.function.Predicates;

import java.util.UUID;
import java.util.function.Predicate;

public final class WorkFilters {

    public static Predicate<Work> ALL() {
        return Predicates.alwaysTrue();
    }

    public static Predicate<Work> UNDONE(UUID player) {
        return work -> work.isDone() && work.hasNotVoted(player);
    }

}
