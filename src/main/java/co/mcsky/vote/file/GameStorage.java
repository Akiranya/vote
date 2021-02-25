package co.mcsky.vote.file;

import co.mcsky.vote.file.serializer.GameSerializer;
import co.mcsky.vote.file.serializer.VoteSerializer;
import co.mcsky.vote.type.Game;
import co.mcsky.vote.type.Vote;
import me.lucko.helper.serialize.FileStorageHandler;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;

import static co.mcsky.vote.VoteMain.plugin;

/**
 * Each instance of {@link GameStorage} handles an instance of {@link Game}).
 */
@SuppressWarnings("NullableProblems")
public class GameStorage extends FileStorageHandler<Game> {

    private static final String fileExtension = ".yml";

    private final YamlConfigurationLoader loader;
    private CommentedConfigurationNode root;

    /**
     * @param fileName should be the name of the plot world, without file extension
     */
    public GameStorage(String fileName, File dataFolder) {
        super(fileName, fileExtension, dataFolder);

        TypeSerializerCollection serializers = TypeSerializerCollection.builder()
                .register(Game.class, new GameSerializer(plugin.getLogger()))
                .register(Vote.class, new VoteSerializer(plugin.getLogger()))
                .build();
        loader = YamlConfigurationLoader.builder()
                .path(new File(dataFolder, fileName + fileExtension).toPath())
                .defaultOptions(opts -> opts.serializers(builder -> builder.registerAll(serializers)))
                .build();

        try {
            root = loader.load();
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Game readFromFile(Path path) {
        try {
            return loader.load().get(Game.class);
        } catch (ConfigurateException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void saveToFile(Path path, Game votes) {
        try {
            loader.save(root.set(votes));
        } catch (ConfigurateException e) {
            e.printStackTrace();
        }
    }

}
