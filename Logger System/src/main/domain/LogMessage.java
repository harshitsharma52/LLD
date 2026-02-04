package domain;

public class LogMessage {
    public long timestamp;
    public LogLevel level;
    public String message;
    public String source;

    public LogMessage(LogLevel level, String message, String source) {
        this.timestamp = System.currentTimeMillis();
        this.level = level;
        this.message = message;
        this.source = source;
    }
}
