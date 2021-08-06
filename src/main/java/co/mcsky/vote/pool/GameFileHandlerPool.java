package co.mcsky.vote.pool;

import co.mcsky.vote.file.GameFileHandler;
import co.mcsky.vote.type.Game;
import com.google.common.io.Files;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static co.mcsky.vote.VoteMain.plugin;

/**
 * Manages all instances of {@link GameFileHandler}.
 * <p>
 * Operation on this instance should reflect on the backed {@link GamePool}.
 */
@SuppressWarnings("UnstableApiUsage")
public enum GameFileHandlerPool {

    // singleton
    INSTANCE;

    private static final File dataFolder = new File(plugin.getDataFolder(), "saves");
    private static final String fileExtension = ".yml";

    // key is world name
    private final Map<String, GameFileHandler> storageMap;

    GameFileHandlerPool() {
        this.storageMap = new HashMap<>();
    }

    public void read(String filename) {
        String world = Files.getNameWithoutExtension(filename);
        GameFileHandler storage = storageMap.computeIfAbsent(world, k -> new GameFileHandler(k, dataFolder));
        Game data = storage.load().orElseThrow();
        GamePool.INSTANCE.register(data);
    }

    public void save(String filename) {
        String world = Files.getNameWithoutExtension(filename);
        Game data = GamePool.INSTANCE.get(world).orElseThrow();
        GameFileHandler storage = storageMap.computeIfAbsent(world, k -> new GameFileHandler(k, dataFolder));
        storage.save(data);
    }

    public void readAll() {
        File[] files = dataFolder.listFiles(f -> f.getName().endsWith(fileExtension));
        if (files != null) {
            for (File file : files) {
                String filename = file.getName();
                plugin.getLogger().info("Loading file " + filename);
                read(filename);
            }
        }
    }

    public void saveAll() {
        for (Game game : GamePool.INSTANCE) {
            save(game.getWorld());
        }
    }

}
