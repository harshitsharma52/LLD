package domain;

public class LogConfiguration {
    private volatile LogLevel rootLevel;

    public LogConfiguration(LogLevel rootLevel) {
        this.rootLevel = rootLevel;
    }

    public LogLevel getRootLevel() {
        return rootLevel;
    }

    public void setRootLevel(LogLevel level) {
        this.rootLevel = level;
    }
}
