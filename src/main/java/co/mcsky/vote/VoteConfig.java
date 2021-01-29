package co.mcsky.vote;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;

import static co.mcsky.vote.VoteMain.plugin;

public final class VoteConfig {

    private static final String configFileName = "config.yml";

    private final YamlConfigurationLoader loader;
    private CommentedConfigurationNode root;

    public boolean allowVoteWhenUndone;
    public boolean allowVoteWhenNotEnded;

    public VoteConfig() {
        loader = YamlConfigurationLoader.builder()
                .path(new File(plugin.getDataFolder(), configFileName).toPath())
                .nodeStyle(NodeStyle.BLOCK)
                .build();
    }

    public void load() {
        try {
            root = loader.load();
        } catch (ConfigurateException e) {
            plugin.getLogger().severe(e.getMessage());
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        allowVoteWhenUndone = root.node("allow-vote-when-undone").comment("Whether allow to vote when the work is not done yet").getBoolean(false);
        allowVoteWhenNotEnded = root.node("allow-vote-when-not-ended").comment("Whether allow to vote the game is not ended yet").getBoolean(false);
    }

    public void save() {
        try {
            loader.save(root);
        } catch (ConfigurateException e) {
            plugin.getLogger().severe(e.getMessage());
        }
    }

    public CommentedConfigurationNode root() {
        return root;
    }

}
