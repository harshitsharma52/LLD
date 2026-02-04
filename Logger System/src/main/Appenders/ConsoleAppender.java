package Appenders;

import domain.Formatter;
import domain.LogAppender;
import domain.LogMessage;

public class ConsoleAppender implements LogAppender {
    private Formatter formatter;

    public ConsoleAppender(Formatter formatter) {
        this.formatter = formatter;
    }

    public synchronized void append(LogMessage message) {
        System.out.println(formatter.format(message));
    }
}


//“Appender is synchronized to avoid mixed log lines.”
