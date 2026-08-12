import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.*;

enum LogLevel {

    DEBUG(1), // create DEBUG object with priority = 1
    INFO(2),
    ERROR(3);

    int priority;


    // This constructor is called when Java creates each enum value.
    LogLevel(int priority) {

        this.priority = priority;
    }
}

class LogMessage {

    String message;

    LogLevel level;

    LocalDateTime timestamp;

    LogMessage(
            String message,
            LogLevel level) {

        this.message = message;
        this.level = level;
        this.timestamp = LocalDateTime.now();
    }
}

// formatter
class LogFormatter {

    public String format(LogMessage log) {

        return "["

                + log.level

                + "] "

                + log.timestamp

                + " : "

                + log.message;
    }
}
// Strategy pattern

interface LogAppender {

    void append(String formattedMessage);
}

// console appender
class ConsoleAppender implements LogAppender {

    @Override
    public void append(
            String formattedMessage) {

        System.out.println(
                formattedMessage);
    }
}

// FILE APPENDER

class FileAppender
        implements LogAppender {

    ////////////////////////////////////////////////
    // THREAD SAFETY
    ////////////////////////////////////////////////

    Lock lock = new ReentrantLock();

    @Override
    public void append(
            String formattedMessage) {

        lock.lock();

        try {

            ////////////////////////////////////////////////
            // SIMULATE FILE WRITE
            ////////////////////////////////////////////////

            System.out.println(
                    "Writing to file : "
                            + formattedMessage);

        }

        finally {

            lock.unlock();
        }
    }
}

// Logger Singeton

class Logger {

    // SINGLETON Eager Initialization

    private static Logger instance = new Logger();

    public static Logger getInstance() {

        return instance;
    }

    ////////////////////////////////////////////////
    // PRIVATE CONSTRUCTOR
    ////////////////////////////////////////////////

    private Logger() {
    }

    ////////////////////////////////////////////////
    // LOGGER DATA
    ////////////////////////////////////////////////

    // volatile so a setLevel() call on one thread is immediately
    // visible to log() calls happening on other threads
    private volatile LogLevel currentLevel = LogLevel.INFO;

    // CopyOnWriteArrayList instead of ArrayList:
    // appenders are added rarely (usually just at startup) but
    // read on EVERY single log() call from potentially many threads.
    // CopyOnWriteArrayList makes reads/iteration lock-free and safe
    // even if addAppender() is called concurrently with log().
    private final List<LogAppender> appenders = new CopyOnWriteArrayList<>();

    private final LogFormatter formatter = new LogFormatter();

    ////////////////////////////////////////////////
    // ADD APPENDER
    ////////////////////////////////////////////////

    public void addAppender(LogAppender appender) {

        appenders.add(appender);
    }

    ////////////////////////////////////////////////
    // SET LOG LEVEL AT RUNTIME
    ////////////////////////////////////////////////

    public void setLevel(LogLevel level) {

        this.currentLevel = level;
    }

    public LogLevel getLevel() {

        return this.currentLevel;
    }

    ////////////////////////////////////////////////
    // LOG
    ////////////////////////////////////////////////

    public void log(
            LogLevel level,
            String message) {

        ////////////////////////////////////////////////
        // LEVEL FILTER
        ////////////////////////////////////////////////

        if (level.priority < currentLevel.priority) {

            return;
        }

        ////////////////////////////////////////////////
        // CREATE LOG
        ////////////////////////////////////////////////

        LogMessage logMessage = new LogMessage(
                message,
                level);

        ////////////////////////////////////////////////
        // FORMAT
        ////////////////////////////////////////////////

        String formattedMessage = formatter.format(
                logMessage);

        ////////////////////////////////////////////////
        // SEND TO ALL APPENDERS
        // Each appender call is isolated in its own try/catch so
        // one failing appender (e.g. disk full in FileAppender)
        // doesn't stop the remaining appenders from receiving the log.
        ////////////////////////////////////////////////

        for (LogAppender appender : appenders) {

            try {

                appender.append(
                        formattedMessage);

            } catch (Exception e) {

                System.err.println(
                        "Appender failed: " + e.getMessage());
            }
        }
    }
}

class LogSystem {

    public static void main(
            String[] args) {

        ////////////////////////////////////////////////
        // LOGGER
        ////////////////////////////////////////////////

        Logger logger = Logger.getInstance();

        ////////////////////////////////////////////////
        // APPENDERS
        ////////////////////////////////////////////////

        logger.addAppender(
                new ConsoleAppender());

        logger.addAppender(
                new FileAppender());

        ////////////////////////////////////////////////
        // LOGS AT DEFAULT LEVEL (INFO)
        ////////////////////////////////////////////////

        logger.log(
                LogLevel.INFO,
                "Application started");

        logger.log(
                LogLevel.ERROR,
                "Payment failed");

        logger.log(
                LogLevel.DEBUG,
                "Debugging application"); // filtered out, priority < INFO

        ////////////////////////////////////////////////
        // CHANGE LOG LEVEL AT RUNTIME, THEN LOG AGAIN
        ////////////////////////////////////////////////

        logger.setLevel(LogLevel.DEBUG);

        logger.log(
                LogLevel.DEBUG,
                "Debugging application"); // now printed, level lowered to DEBUG
    }

}