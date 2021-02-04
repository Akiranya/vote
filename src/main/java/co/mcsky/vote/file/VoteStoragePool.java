package co.mcsky.vote.file;

import co.mcsky.vote.type.Votes;
import co.mcsky.vote.type.VotesPool;
import com.google.common.io.Files;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static co.mcsky.vote.VoteMain.plugin;

/**
 * Manages all instances of {@link VoteStorage}.
 * <p>
 * Operation on this instance should reflect on the backed {@link VotesPool}.
 */
@SuppressWarnings("UnstableApiUsage")
public enum VoteStoragePool {

    // singleton
    INSTANCE;

    private static final File dataFolder = new File(plugin.getDataFolder(), "saves");
    private static final String fileExtension = ".yml";

    // key is world name
    private final Map<String, VoteStorage> storageMap;

    VoteStoragePool() {
        this.storageMap = new HashMap<>();
    }

    public void read(String filename) {
        String world = Files.getNameWithoutExtension(filename);
        VoteStorage storage = storageMap.computeIfAbsent(world, k -> new VoteStorage(k, dataFolder));
        Votes data = storage.load().orElseThrow();
        VotesPool.INSTANCE.register(data);
    }

    public void save(String filename) {
        String world = Files.getNameWithoutExtension(filename);
        Votes data = VotesPool.INSTANCE.get(world).orElseThrow();
        VoteStorage storage = storageMap.computeIfAbsent(world, k -> new VoteStorage(k, dataFolder));
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
        for (Votes votes : VotesPool.INSTANCE) {
            save(votes.getWorld());
        }
    }

}
