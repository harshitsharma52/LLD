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

// WHY volatile?

// Root log level can change at runtime.

// Example:

// Initially: WARNING

// Later changed to: DEBUG

// And logging may be happening from multiple threads.

// So we must ensure:

// 👉 All threads see the updated value.