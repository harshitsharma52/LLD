package Formatters;

import domain.Formatter;
import domain.LogMessage;

public class SimpleFormatter implements Formatter {

    public String format(LogMessage msg) {
        return "[" + msg.timestamp + "] ["
                + msg.level + "] ["
                + msg.source + "] - "
                + msg.message;
    }
}
