package co.mcsky.vote.io;

import co.mcsky.mewcore.config.YamlConfigFactory;
import co.mcsky.vote.io.serializer.GameSerializer;
import co.mcsky.vote.io.serializer.VoteSerializer;
import co.mcsky.vote.object.Game;
import co.mcsky.vote.object.Vote;
import me.lucko.helper.serialize.FileStorageHandler;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Each instance of {@link GameFileHandler} handles an instance of {@link
 * Game}).
 */
public class GameFileHandler extends FileStorageHandler<Game> {

    private static final String FILE_EXTENSION = ".yml";

    private final YamlConfigurationLoader loader;
    private CommentedConfigurationNode root;

    /**
     * @param fileName should be the name of the plot world, without file
     *                 extension
     */
    public GameFileHandler(String fileName, File dataFolder) {
        super(fileName, FILE_EXTENSION, dataFolder);

        TypeSerializerCollection serializers = YamlConfigFactory.typeSerializers().childBuilder()
                .register(Game.class, new GameSerializer())
                .register(Vote.class, new VoteSerializer())
                .build();
        loader = YamlConfigurationLoader.builder()
                .file(new File(dataFolder, fileName + FILE_EXTENSION))
                .defaultOptions(opts -> opts.serializers(serializers))
                .build();
        root = loader.createNode();
    }

    @Override
    protected Game readFromFile(Path path) {
        try {
            return Objects.requireNonNull((root = loader.load()).get(Game.class));
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
