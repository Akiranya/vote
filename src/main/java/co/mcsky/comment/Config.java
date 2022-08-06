package co.mcsky.comment;

import org.bukkit.configuration.ConfigurationSection;

public final class Config {

    private final Main p;

    public Config(Main p) {
        this.p = p;
    }

    public void loadDefaultConfig() {
        p.saveDefaultConfig();
        p.reloadConfig();
    }

    public void reloadConfig() {
        p.reloadConfig();
    }

    public boolean getDebug() {
        return p.getConfig().getBoolean("debug");
    }

    public boolean getVoteConditionDone() {
        return p.getConfig().getBoolean("comments.conditions.done");
    }

    public boolean getVoteConditionEnded() {
        return p.getConfig().getBoolean("comments.conditions.ended");
    }

    public ConfigurationSection getGuiBase() {
        return p.getConfig().getConfigurationSection("gui.base");
    }

    public ConfigurationSection getGuiListing() {
        return p.getConfig().getConfigurationSection("gui.listing");
    }

    public ConfigurationSection getGuiOption() {
        return p.getConfig().getConfigurationSection("gui.options");
    }

}
