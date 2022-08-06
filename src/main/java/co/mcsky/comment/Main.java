package co.mcsky.comment;

import co.mcsky.comment.io.GameFileHandlerPool;
import co.mcsky.comment.object.GamePlots;
import co.mcsky.comment.object.factory.PlotsFactory;
import me.lucko.helper.Schedulers;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.utils.Log;

public class Main extends ExtendedJavaPlugin {

    private static Main p;

    public Config config;
    public Messages messages;
    public Commands commands;

    private GamePlots plots;

    public static Main inst() {
        return p;
    }

    public static void debug(String message) {
        if (Main.config().getDebug()) {
            Log.info("[DEBUG] " + message);
        }
    }

    public static void debug(Throwable message) {
        if (Main.config().getDebug()) {
            Log.info("[DEBUG] " + message.getMessage());
        }
    }

    public static Messages lang() {
        return p.messages;
    }

    public static Config config() {
        return p.config;
    }

    @Override
    public void enable() {
        p = this;

        plots = PlotsFactory.create();
        messages = new Messages(this);
        config = new Config(this);
        config.loadDefaultConfig();
        commands = new Commands();
        commands.register();

        // since PlotSquared loads the plot worlds very late
        // we must load data after "Done!" to ensure we can safely
        // get all the player UUIDs from the PlotSquared database
        Schedulers.async().runLater(this::loadData, 10).bindWith(this);
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
        messages = new Messages(this);
        config.loadDefaultConfig();
    }

}
