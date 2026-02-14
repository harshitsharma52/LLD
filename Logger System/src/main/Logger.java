import Appenders.ConsoleAppender;
import Appenders.FileAppender;
import Formatters.SimpleFormatter;
import domain.Formatter;
import domain.LogAppender;
import domain.LogConfiguration;
import domain.LogLevel;
import domain.LogMessage;
import java.util.ArrayList;
import java.util.List;

public class Logger {
    private LogConfiguration config;
    private List<LogAppender> appenders = new ArrayList<>();

    Logger(LogConfiguration config) {
        this.config = config;
    }

    void addAppender(LogAppender appender) {
        appenders.add(appender);
    }

    void log(LogLevel level, String message, String source) {
        if (!level.isGreaterOrEqual(config.getRootLevel())) {
            return;
        }

        LogMessage logMessage = new LogMessage(level, message, source);

        for (LogAppender appender : appenders) {
            appender.append(logMessage);
        }
    }

    // Convenience methods
    void debug(String msg, String src) {
        log(LogLevel.DEBUG, msg, src);
    }

    void info(String msg, String src) {
        log(LogLevel.INFO, msg, src);
    }

    void warning(String msg, String src) {
        log(LogLevel.WARNING, msg, src);
    }

    void error(String msg, String src) {
        log(LogLevel.ERROR, msg, src);
    }

    void fatal(String msg, String src) {
        log(LogLevel.FATAL, msg, src);
    }

    public static void main(String[] args) {

        LogConfiguration config = new LogConfiguration(LogLevel.WARNING); // Set root level to WARNING and log msg < warning will be ignored

        Logger logger = new Logger(config);

        Formatter formatter = new SimpleFormatter();

        // Formatter follows Strategy Pattern. It allows different formatting algorithms to be plugged in at runtime without modifying existing Logger or Appender code

        logger.addAppender(new ConsoleAppender(formatter));
        logger.addAppender(new FileAppender(formatter));

        logger.debug("This won't be logged", "AuthService");
        logger.info("This won't be logged", "AuthService");
        logger.warning("Invalid password attempt", "AuthService");
        logger.error("Payment failed", "PaymentService");

    }
}
