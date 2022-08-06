package co.mcsky.comment.gui;

import co.mcsky.comment.object.Artwork;
import me.lucko.helper.function.Predicates;

import java.util.UUID;
import java.util.function.Predicate;

public final class WorkFilters {

    public static Predicate<Artwork> ALL() {
        return Predicates.alwaysTrue();
    }

    public static Predicate<Artwork> UNDONE(UUID player) {
        return work -> work.isDone() && work.hasNotVoted(player);
    }

}
