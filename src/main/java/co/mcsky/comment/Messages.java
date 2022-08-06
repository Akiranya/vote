package co.mcsky.comment;

import de.themoep.utils.lang.bukkit.LanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Messages {

    private final LanguageManager lang;
    private static final String PREFIX_INFO_KEY = "prefix.info";
    private static final String PREFIX_WARN_KEY = "prefix.warn";

    public Messages(JavaPlugin plugin) {
        this.lang = new LanguageManager(plugin, "languages", "zh");
        this.lang.setPlaceholderPrefix("{");
        this.lang.setPlaceholderSuffix("}");
    }

    public String getRaw(CommandSender sender, String key, String... replacements) {
        String message;
        if (replacements.length == 0) {
            message = this.lang.getConfig(sender).get(key);
        } else {
            String[] list = new String[replacements.length];
            System.arraycopy(replacements, 0, list, 0, replacements.length);
            message = this.lang.getConfig(sender).get(key, list);
        }
        return message;
    }

    public String get(CommandSender sender, String key, String... replacements) {
        return ChatColor.translateAlternateColorCodes('&', getRaw(sender, key, replacements));
    }

    public String get(String key, String... replacements) {
        return get(null, key, replacements);
    }

    public Component getMiniMessage(CommandSender sender, String key, String... replacements) {
        return MiniMessage.miniMessage().deserialize(getRaw(sender, key, replacements));
    }

    public Component getMiniMessage(String key, String... replacements) {
        return getMiniMessage(null, key, replacements);
    }

    public void info(CommandSender sender, String key, String... replacements) {
        sender.sendMessage(getMiniMessage(sender, PREFIX_INFO_KEY).append(getMiniMessage(sender, key, replacements)));
    }

    public void warn(CommandSender sender, String key, String... replacements) {
        sender.sendMessage(getMiniMessage(sender, PREFIX_WARN_KEY).append(getMiniMessage(sender, key, replacements)));
    }

}
