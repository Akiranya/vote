package co.mcsky.vote;

import co.mcsky.vote.file.GameFileHandlerPool;
import co.mcsky.vote.object.GamePlots;
import co.mcsky.vote.object.factory.PlotsFactory;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.utils.Log;

public class VoteMain extends ExtendedJavaPlugin {

    private static VoteMain p;

    public VoteConfig config;
    public VoteMessages messages;
    public VoteCommands commands;

    private GamePlots plots;

    public static VoteMain inst() {
        return p;
    }

    public static void debug(String message) {
        if (VoteMain.config().getDebug()) {
            Log.info("[DEBUG] " + message);
        }
    }

    public static void debug(Throwable message) {
        if (VoteMain.config().getDebug()) {
            Log.info("[DEBUG] " + message.getMessage());
        }
    }

    public static VoteMessages lang() {
        return p.messages;
    }

    public static VoteConfig config() {
        return p.config;
    }

    @Override
    public void enable() {
        p = this;

        plots = PlotsFactory.create();
        messages = new VoteMessages(this);
        config = new VoteConfig(this);
        config.loadDefaultConfig();
        commands = new VoteCommands();
        commands.register();
        loadData();
    }

    @Override
    public void disable() {
        GameFileHandlerPool.INSTANCE.saveAll();
    }

    public void loadData() {
        GameFileHandlerPool.INSTANCE.readAll();
    }

    public GamePlots getPlots() {
        return plots;
    }

    public void reload() {
        messages = new VoteMessages(this);
        config.loadDefaultConfig();
    }

}
