package co.mcsky.vote;

import co.mcsky.moecore.config.YamlConfigFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;

import static co.mcsky.vote.VoteMain.plugin;

public final class VoteConfig {

    private static final String FILENAME = "config.yml";

    /* config starts */

    public boolean allow_vote_when_undone;
    public boolean allow_vote_when_not_ended;

    /* config ends */

    private YamlConfigurationLoader loader;
    private CommentedConfigurationNode root;

    public VoteConfig() {
        loader = YamlConfigFactory.loader(new File(plugin.getDataFolder(), FILENAME));
    }

    public void load() {
        try {
            root = loader.load();
        } catch (ConfigurateException e) {
            plugin.getLogger().severe(e.getMessage());
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

        allow_vote_when_undone = root.node("allow-vote-when-undone").getBoolean(false);
        allow_vote_when_not_ended = root.node("allow-vote-when-not-ended").getBoolean(false);
    }

    public void save() {
        try {
            loader.save(root);
        } catch (ConfigurateException e) {
            plugin.getLogger().severe(e.getMessage());
        }
    }

}
