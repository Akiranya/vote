package co.mcsky.vote;

import co.aikar.commands.PaperCommandManager;
import co.mcsky.vote.io.VoteStoragePool;
import co.mcsky.vote.type.VotesPool;
import com.plotsquared.core.api.PlotAPI;
import de.themoep.utils.lang.bukkit.LanguageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

/**
 * The main class of this plugin.
 */
public class VoteMain extends JavaPlugin {

    public static VoteMain plugin;
    public static PlotAPI plotApi;

    public VotesPool votesPool;
    public VoteStoragePool voteStoragePool;

    public VoteConfig config;
    public LanguageManager lang;
    public PaperCommandManager commands;

    @Override
    public void onDisable() {
        this.voteStoragePool.saveAll();
    }

    @Override
    public void onEnable() {
        plugin = this;
        plotApi = new PlotAPI();

        this.config = new VoteConfig();
        this.config.load();
        this.config.save();

        this.votesPool = VotesPool.create();
        this.voteStoragePool = VoteStoragePool.create(votesPool);
        this.voteStoragePool.readAll();

        loadLanguages();
        registerCommands();
    }

    public void registerCommands() {
        commands = new PaperCommandManager(this);
        commands.registerDependency(VotesPool.class, votesPool);
        commands.registerDependency(VoteStoragePool.class, voteStoragePool);
        commands.registerDependency(PaperCommandManager.class, commands);
        commands.registerCommand(new VoteCommands(commands));
    }

    public void loadLanguages() {
        this.lang = new LanguageManager(this, "languages", "zh");
        this.lang.setPlaceholderPrefix("{");
        this.lang.setPlaceholderSuffix("}");
        this.lang.setProvider(sender -> {
            if (sender instanceof Player) {
                return ((Player) sender).getLocale();
            }
            return null;
        });
    }

    /**
     * Get a message from a language config for a certain sender
     *
     * @param sender       The sender to get the string for.
     * @param key          The language key in the config
     * @param replacements An option array for replacements. (2n)-th will be the placeholder, (2n+1)-th the value.
     *                     Placeholders have to be surrounded by percentage signs: {placeholder}
     * @return The string from the config which matches the sender's language (or the default one) with the replacements
     * replaced (or an error message, never null)
     */
    public String getMessage(CommandSender sender, String key, Object... replacements) {
        if (replacements.length == 0) {
            return lang.getConfig(sender).get(key);
        } else {
            return lang.getConfig(sender).get(key, Arrays.stream(replacements)
                    .map(Object::toString)
                    .toArray(String[]::new));
        }
    }

}
