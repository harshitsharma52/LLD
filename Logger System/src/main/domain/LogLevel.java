package domain;

public enum LogLevel {
    DEBUG(1),
    INFO(2),
    WARNING(3),
    ERROR(4),
    FATAL(5);

    private final int priority;

    LogLevel(int priority) {
        this.priority = priority;
    }

    public boolean isGreaterOrEqual(LogLevel other) {
        return this.priority >= other.priority;
    }
}
