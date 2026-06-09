import java.util.*;
import java.util.concurrent.*;

class Message {

    private final String id;

    private final String content;

    public Message(
            String id,
            String content) {

        this.id = id;
        this.content = content;
    }

    public String getContent() {

        return content;
    }
}


interface Subscriber {

    void consume(Message message);
}

class EmailSubscriber implements Subscriber {

    private final String name;

    public EmailSubscriber(String name) {

        this.name = name;
    }

    @Override
    public void consume( Message message) {

        System.out.println(

                Thread.currentThread().getName()

                        + " -> "

                        + name

                        + " received : "

                        + message.getContent());
    }
}

class Topic {

    private final String topicName;

    //////////////////////////////////////////////////
    // THREAD SAFE
    //////////////////////////////////////////////////

    private final List<Subscriber>subscribers = new CopyOnWriteArrayList<>();

    //////////////////////////////////////////////////
    // PARALLEL PROCESSING
    //////////////////////////////////////////////////

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public Topic(String topicName) {

        this.topicName = topicName;
    }

    public void subscribe( Subscriber subscriber) {

        subscribers.add(subscriber);
    }

    public void publish(Message message) {

        for(Subscriber subscriber : subscribers){

            executor.submit(() -> {

                subscriber.consume( message);
            });
        }
    }
}

class MessageQueue {

    private final Map<String, Topic> topics = new ConcurrentHashMap<>();

    public void createTopic(String topicName) 
    {

        topics.putIfAbsent(topicName,  new Topic(topicName));
    }

    public Topic getTopic( String topicName) {

        return topics.get(topicName);
    }
}




public class Kafka {

    public static void main(
            String[] args) {

        MessageQueue queue =
                new MessageQueue();

        //////////////////////////////////////////////////
        // CREATE TOPIC
        //////////////////////////////////////////////////

        queue.createTopic(
                "Payments");

        Topic payments =
                queue.getTopic(
                        "Payments");

        //////////////////////////////////////////////////
        // SUBSCRIBERS
        //////////////////////////////////////////////////

        payments.subscribe(
                new EmailSubscriber(
                        "Subscriber-1"));

        payments.subscribe(
                new EmailSubscriber(
                        "Subscriber-2"));

        payments.subscribe(
                new EmailSubscriber(
                        "Subscriber-3"));

        //////////////////////////////////////////////////
        // PUBLISH MESSAGE
        //////////////////////////////////////////////////

        payments.publish(

                new Message(

                        "1",

                        "Payment Successful"));
    }

}
