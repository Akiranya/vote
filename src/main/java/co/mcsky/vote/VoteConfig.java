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

    public boolean getVoteConditionDone() {
        return p.getConfig().getBoolean("voteSettings.conditions.done");
    }

    public boolean getVoteConditionEnded() {
        return p.getConfig().getBoolean("voteSettings.conditions.ended");
    }

}
