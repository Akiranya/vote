package co.mcsky.vote.file;

import co.mcsky.vote.VoteMain;
import co.mcsky.vote.object.Game;
import co.mcsky.vote.object.GamePool;
import com.google.common.io.Files;
import me.lucko.helper.utils.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages all instances of {@link GameFileHandler}.
 * <p>
 * Operation on this instance should reflect on the backed {@link GamePool}.
 */
public enum GameFileHandlerPool {

    // singleton
    INSTANCE;

    private static final File DATA_FOLDER = new File(VoteMain.inst().getDataFolder(), "saves");
    private static final String FILE_EXTENSION = ".yml";

    // key is world name
    private final Map<String, GameFileHandler> storageMap;

    GameFileHandlerPool() {
        this.storageMap = new HashMap<>();
    }

    public void read(String filename) {
        String world = Files.getNameWithoutExtension(filename);
        GameFileHandler fileHandler = storageMap.computeIfAbsent(world, k -> new GameFileHandler(k, DATA_FOLDER));
        Game data = fileHandler.load().orElseThrow();
        GamePool.INSTANCE.register(data);
    }

    public void save(String filename) {
        String world = Files.getNameWithoutExtension(filename);
        Game data = GamePool.INSTANCE.get(world).orElseThrow();
        GameFileHandler fileHandler = storageMap.computeIfAbsent(world, k -> new GameFileHandler(k, DATA_FOLDER));
        fileHandler.save(data);
    }

    public void readAll() {
        File[] files = DATA_FOLDER.listFiles(f -> f.getName().endsWith(FILE_EXTENSION));
        if (files != null) {
            for (int i = files.length - 1; i >= 0; i--) {
                String filename = files[i].getName();
                Log.info("Loading file " + filename);
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
