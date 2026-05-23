import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.*;

enum LogLevel {

    DEBUG(1), // create DEBUG object with priority = 1
    INFO(2),
    ERROR(3);

    int priority;

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

    private LogLevel currentLevel = LogLevel.INFO;

    private final List<LogAppender> appenders = new ArrayList<>();

    private final LogFormatter formatter = new LogFormatter();

    ////////////////////////////////////////////////
    // ADD APPENDER
    ////////////////////////////////////////////////

    public void addAppender(LogAppender appender) {

        appenders.add(appender);
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
        ////////////////////////////////////////////////

        for (LogAppender appender : appenders) {

            appender.append(
                    formattedMessage);
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
        // LOGS
        ////////////////////////////////////////////////

        logger.log(
                LogLevel.INFO,
                "Application started");

        logger.log(
                LogLevel.ERROR,
                "Payment failed");

        logger.log(
                LogLevel.DEBUG,
                "Debugging application");
    }

}