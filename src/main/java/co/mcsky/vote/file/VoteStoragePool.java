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
 * Operation on this instance will reflect on the backed {@link #votesPool}.
 */
@SuppressWarnings("UnstableApiUsage")
public class VoteStoragePool {

    private static final File dataFolder = new File(plugin.getDataFolder(), "saves");
    private static final String fileExtension = ".yml";

    private final VotesPool votesPool;
    // Key is world name
    private final Map<String, VoteStorage> storageMap;

    public static VoteStoragePool create(VotesPool votesPool) {
        return new VoteStoragePool(votesPool);
    }

    private VoteStoragePool(VotesPool votesPool) {
        this.votesPool = votesPool;
        this.storageMap = new HashMap<>();
    }

    public void read(String filename) {
        String world = Files.getNameWithoutExtension(filename);
        VoteStorage storage = storageMap.computeIfAbsent(world, k -> new VoteStorage(k, dataFolder));
        Votes data = storage.load().orElseThrow();
        votesPool.register(data);
    }

    public void save(String filename) {
        String world = Files.getNameWithoutExtension(filename);
        Votes data = votesPool.get(world).orElseThrow();
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
        for (Votes votes : votesPool) {
           save(votes.getPlotWorld());
        }
    }

}
