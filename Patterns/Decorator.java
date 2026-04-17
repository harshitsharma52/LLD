
package Patterns;

interface Logger {
    void log(String msg);
}

class ConsoleLogger implements Logger {
    @Override
    public void log(String msg) {
        System.out.println("Inside ConsoleLogger");
        System.out.println(msg);
    }
}

class FileLogger implements Logger {


        @Override
    public void log(String msg) {
        System.out.println("Inside FileLogger");
        System.out.println("File Logging: " + msg);
    }
}


// This is just a base wrapper — real behavior will come from child classes”
// Prevents wrong usage new LoggerDecorator(...) // ❌
// Gives common structure

// “Abstract is used to define a base class that cannot be instantiated and forces subclasses to provide implementation.”

abstract class LoggerDecorator implements Logger {
    protected Logger logger;

    public LoggerDecorator(Logger logger) {
        this.logger = logger;
    }
}

class TimeStampDecorator extends LoggerDecorator {

    public TimeStampDecorator(Logger logger) {
        super(logger);
    }
    
    @Override
    public void log(String msg) {
        System.out.println("Inside TimeStampDecorator");

        String newMsg = System.currentTimeMillis() + " : " + msg;
        logger.log(newMsg);
    }
}

class JsonParsing extends LoggerDecorator {

    public JsonParsing(Logger logger) {
        super(logger);
    }
    @Override
    public void log(String message) {
        System.out.println("Inside JsonParsing");
        System.out.println("Json parsing happening for message");

        logger.log(message);
    }
}

class Decorator{
    public static void main(String[] args) {

      Logger logger = new ConsoleLogger();



            // This is just linked objects (like linked list)
            // What happens during log() call?
            
            // Step 1: TimeStampDecorator modifies message calls: logger.log(newMsg); 👉 passes to JsonParsing
            //  Step 2: JsonParsing prints something calls: logger.log(message); 👉 passes to ConsoleLogger
            //  Step 3: ConsoleLogger
            //  finally prints
            
        //     Object 1: ConsoleLogger

        //     Object 2: JsonParsing
        //     logger → ConsoleLogger

        //     Object 3: TimeStampDecorator
        //     logger → JsonParsing
            
        logger = new TimeStampDecorator(
                                new JsonParsing(
                                    new ConsoleLogger())); // decorate
                                    
        
        //  logger = new JsonParsing(
        //                              new TimeStampDecorator(
        //                                                 new ConsoleLogger()));
                                    
                                    

        logger.log("message of user");
       
    }
}