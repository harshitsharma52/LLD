package Appenders;

import domain.Formatter;
import domain.LogAppender;
import domain.LogMessage;

public class FileAppender implements LogAppender {
    private Formatter formatter;

    public FileAppender(Formatter formatter) {
        this.formatter = formatter;
    }

    public synchronized void append(LogMessage message) {
        // assume file write here
        System.out.println("FILE: " + formatter.format(message));
    }
}
