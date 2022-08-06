package co.mcsky.vote;

public final class VoteConfig {

    private final VoteMain p;

    public VoteConfig(VoteMain p) {
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

    public boolean isAllowVoteWhenUndone() {
        return p.getConfig().getBoolean("isAllowVoteWhenUndone");
    }

    public boolean isAllowVoteWhenNotEnded() {
        return p.getConfig().getBoolean("isAllowVoteWhenNotEnded");
    }

}
