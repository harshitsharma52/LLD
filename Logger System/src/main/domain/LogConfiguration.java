package domain;

public class LogConfiguration {
    private volatile RecoveryStatus rootLevel;

    public LogConfiguration(RecoveryStatus rootLevel) {
        this.rootLevel = rootLevel;
    }

    public RecoveryStatus getRootLevel() {
        return rootLevel;
    }

    public void setRootLevel(RecoveryStatus level) {
        this.rootLevel = level;
    }
}
