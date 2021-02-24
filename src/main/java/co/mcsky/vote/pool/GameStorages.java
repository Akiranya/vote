package co.mcsky.vote.pool;

import co.mcsky.vote.file.GameStorage;
import co.mcsky.vote.type.Game;
import com.google.common.io.Files;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static co.mcsky.vote.VoteMain.plugin;

/**
 * Manages all instances of {@link GameStorage}.
 * <p>
 * Operation on this instance should reflect on the backed {@link Games}.
 */
@SuppressWarnings("UnstableApiUsage")
public enum GameStorages {

    // singleton
    INSTANCE;

    private static final File dataFolder = new File(plugin.getDataFolder(), "saves");
    private static final String fileExtension = ".yml";

    // key is world name
    private final Map<String, GameStorage> storageMap;

    GameStorages() {
        this.storageMap = new HashMap<>();
    }

    public void read(String filename) {
        String world = Files.getNameWithoutExtension(filename);
        GameStorage storage = storageMap.computeIfAbsent(world, k -> new GameStorage(k, dataFolder));
        Game data = storage.load().orElseThrow();
        Games.INSTANCE.register(data);
    }

    public void save(String filename) {
        String world = Files.getNameWithoutExtension(filename);
        Game data = Games.INSTANCE.get(world).orElseThrow();
        GameStorage storage = storageMap.computeIfAbsent(world, k -> new GameStorage(k, dataFolder));
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
        for (Game game : Games.INSTANCE) {
            save(game.getWorld());
        }
    }

}
