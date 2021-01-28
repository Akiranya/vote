package co.mcsky.vote.gui;

import co.mcsky.vote.type.Votes;
import co.mcsky.vote.events.PlayerVoteEvent;
import co.mcsky.vote.type.Vote;
import co.mcsky.vote.type.Work;
import me.lucko.helper.Events;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.scheme.MenuPopulator;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.menu.scheme.StandardSchemeMappings;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

import static co.mcsky.vote.VoteMain.*;

public class VoteOptionGui extends Gui {

    private final MenuScheme background = new MenuScheme(StandardSchemeMappings.STAINED_GLASS)
            .mask("111111111")
            .mask("111111111")
            .mask("110101011")
            .mask("111111111")
            .mask("111111111")
            .scheme(3, 3, 3, 3, 3, 3, 3, 3, 3)
            .scheme(3, 3, 3, 3, 3, 3, 3, 3, 3)
            .scheme(3, 3, 3, 3, 3, 3)
            .scheme(3, 3, 3, 3, 3, 3, 3, 3, 3)
            .scheme(3, 3, 3, 3, 3, 3, 3, 3, 3);
    private final MenuScheme options = new MenuScheme()
            .maskEmpty(2)
            .mask("001010100");
    private final int backSlot = new MenuScheme()
            .maskEmpty(4)
            .mask("000010000")
            .getMaskedIndexesImmutable().get(0);

    private final Votes votes;
    // The work data related to this vote option GUI.
    private final Work work;

    public VoteOptionGui(Player player, Gui fallback, Votes votes, Work work) {
        super(player, 5, plugin.getMessage(player, "gui.vote-options.title"));
        this.votes = votes;
        this.work = work;
        setFallbackGui(p -> fallback);
    }

    @Override
    public void redraw() {
        this.background.apply(this);

        if (isFirstDraw()) {
            MenuPopulator optionPopulator = this.options.newPopulator(this);

            // Populate the teleport button
            optionPopulator.accept(
                    ItemStackBuilder.of(Material.MINECART)
                            .name(plugin.getMessage(getPlayer(), "gui.vote-options.teleport-to-plot.name"))
                            .lore(plugin.getMessage(getPlayer(), "gui.vote-options.teleport-to-plot.lore1"))
                            .lore(plugin.getMessage(getPlayer(), "gui.vote-options.teleport-to-plot.lore2"))
                            .build(() -> {
                                playSound(Sound.UI_BUTTON_CLICK);
                                work.teleport(getPlayer());
                            }));

            // Populate the vote options
            UUID rater = getPlayer().getUniqueId();
            optionPopulator.accept(
                    ItemStackBuilder.of(Material.GREEN_WOOL)
                            .name(plugin.getMessage(getPlayer(), "gui.vote-options.vote-work.name"))
                            .lore(plugin.getMessage(getPlayer(), "gui.vote-options.vote-work.lore1"))
                            .lore(plugin.getMessage(getPlayer(), "gui.vote-options.vote-work.lore2"))
                            .build(() -> {
                                Vote vote = Vote.create(rater).absent(false).build();
                                if (!Events.callAndReturn(new PlayerVoteEvent(getPlayer(), work, vote, votes)).isCancelled()) {
                                    playSound(Sound.ENTITY_PLAYER_LEVELUP);
                                    getPlayer().sendMessage(plugin.getMessage(getPlayer(), "gui-message.vote-work", "player", work.getOwnerName()));
                                    votes.vote(work.getOwner(), vote);
                                    close();
                                } else {
                                    playSound(Sound.ENTITY_BLAZE_HURT);
                                }
                            }));
            optionPopulator.accept(
                    ItemStackBuilder.of(Material.RED_WOOL)
                            .name(plugin.getMessage(getPlayer(), "gui.vote-options.vote-work-absent.name"))
                            .lore(plugin.getMessage(getPlayer(), "gui.vote-options.vote-work-absent.lore1"))
                            .lore(plugin.getMessage(getPlayer(), "gui.vote-options.vote-work-absent.lore2"))
                            .build(() -> {
                                Vote vote = Vote.create(rater).absent(true).build();
                                if (!Events.callAndReturn(new PlayerVoteEvent(getPlayer(), this.work, vote, votes)).isCancelled()) {
                                    playSound(Sound.ENTITY_PLAYER_LEVELUP);
                                    getPlayer().sendMessage(plugin.getMessage(getPlayer(), "gui-message.vote-work-absent", "player", work.getOwnerName()));
                                    votes.vote(work.getOwner(), Vote.create(rater).absent(true).build());
                                    close();
                                } else {
                                    playSound(Sound.ENTITY_BLAZE_HURT);
                                }
                            }));

            // Populate the back button
            setItem(this.backSlot, ItemStackBuilder.of(Material.REDSTONE)
                    .name(plugin.getMessage(getPlayer(), "gui.vote-options.back.name"))
                    .lore(plugin.getMessage(getPlayer(), "gui.vote-options.back.lore1"))
                    .lore(plugin.getMessage(getPlayer(), "gui.vote-options.back.lore2"))
                    .build(() -> {
                        playSound(Sound.UI_BUTTON_CLICK);
                        close();
                    }));
        }
    }

    private void playSound(Sound sound) {
        getPlayer().playSound(getPlayer().getLocation(), sound, 1F, 1F);
    }

}
