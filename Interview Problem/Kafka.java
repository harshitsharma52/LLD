import java.util.*;
import java.util.concurrent.*;

//////////////////////////////////////////////////////////
// MESSAGE
//////////////////////////////////////////////////////////

class Message {

    private final String id;

    private final String content;

    public Message(
            String id,
            String content) {

        this.id = id;
        this.content = content;
    }

    public String getId() {

        return id;
    }

    public String getContent() {

        return content;
    }
}

//////////////////////////////////////////////////////////
// SUBSCRIBER
//////////////////////////////////////////////////////////

interface Subscriber {

    void consume(
            Message message);
}

//////////////////////////////////////////////////////////
// EMAIL SUBSCRIBER
//////////////////////////////////////////////////////////

class EmailSubscriber
        implements Subscriber {

    private final String name;

    public EmailSubscriber(
            String name) {

        this.name = name;
    }

    @Override
    public void consume(
            Message message) {

        System.out.println(

                Thread.currentThread()
                        .getName()

                        + " -> "

                        + name

                        + " received : "

                        + message.getContent());

        //////////////////////////////////////////////////
        // SIMULATE PROCESSING
        //////////////////////////////////////////////////

        try {

            Thread.sleep(1000);

        } catch(Exception e) {

            e.printStackTrace();
        }
    }
}

//////////////////////////////////////////////////////////
// SUBSCRIBER WORKER
//////////////////////////////////////////////////////////

class SubscriberWorker {

    //////////////////////////////////////////////////
    // FIFO QUEUE
    //
    // Guarantees ordering
    //////////////////////////////////////////////////

    private final BlockingQueue<Message>
            queue =
            new LinkedBlockingQueue<>();

    private final Subscriber subscriber;

    public SubscriberWorker(
            Subscriber subscriber) {

        this.subscriber = subscriber;

        startWorker();
    }

    //////////////////////////////////////////////////
    // DEDICATED THREAD
    //
    // One thread per subscriber
    // Messages processed sequentially
    //////////////////////////////////////////////////

    private void startWorker() {

        Thread worker =
                new Thread(() -> {

                    while(true) {

                        try {

                            Message message =
                                    queue.take();

                            subscriber.consume(
                                    message);

                        } catch(Exception e) {

                            e.printStackTrace();
                        }
                    }
                });

        worker.start();
    }

    //////////////////////////////////////////////////
    // ADD MESSAGE TO QUEUE
    //////////////////////////////////////////////////

    public void submit(
            Message message) {

        queue.offer(
                message);
    }
}

//////////////////////////////////////////////////////////
// TOPIC
//////////////////////////////////////////////////////////

class Topic {

    private final String topicName;

    //////////////////////////////////////////////////
    // THREAD SAFE
    //
    // Subscribers can be added while
    // publishing is happening
    //////////////////////////////////////////////////

    private final List<SubscriberWorker>
            workers =
            new CopyOnWriteArrayList<>();

    public Topic(
            String topicName) {

        this.topicName = topicName;
    }

    //////////////////////////////////////////////////
    // SUBSCRIBE
    //////////////////////////////////////////////////

    public void subscribe(
            Subscriber subscriber) {

        workers.add(

                new SubscriberWorker(
                        subscriber));
    }

    //////////////////////////////////////////////////
    // PUBLISH
    //
    // Fan-out message to all subscribers
    //////////////////////////////////////////////////

    public void publish(
            Message message) {

        for(SubscriberWorker worker
                : workers) {

            worker.submit(
                    message);
        }
    }
}

//////////////////////////////////////////////////////////
// MESSAGE QUEUE
//////////////////////////////////////////////////////////

class MessageQueue {

    //////////////////////////////////////////////////
    // THREAD SAFE TOPIC STORE
    //////////////////////////////////////////////////

    private final Map<String, Topic>
            topics =
            new ConcurrentHashMap<>();

    //////////////////////////////////////////////////
    // CREATE TOPIC
    //////////////////////////////////////////////////

    public void createTopic(
            String topicName) {

        topics.putIfAbsent(

                topicName,

                new Topic(
                        topicName));
    }

    //////////////////////////////////////////////////
    // GET TOPIC
    //////////////////////////////////////////////////

    public Topic getTopic(
            String topicName) {

        return topics.get(
                topicName);
    }
}

//////////////////////////////////////////////////////////
// MAIN
//////////////////////////////////////////////////////////

public class Kafka {

    public static void main(String[] args) throws Exception {

        //////////////////////////////////////////////////
        // MESSAGE QUEUE
        //////////////////////////////////////////////////

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
        // PUBLISH MESSAGES
        //////////////////////////////////////////////////

        payments.publish(

                new Message(
                        "1",
                        "Message-1"));

        payments.publish(

                new Message(
                        "2",
                        "Message-2"));

        payments.publish(

                new Message(
                        "3",
                        "Message-3"));

        //////////////////////////////////////////////////
        // KEEP JVM ALIVE
        //////////////////////////////////////////////////

        Thread.sleep(5000);
    }
}