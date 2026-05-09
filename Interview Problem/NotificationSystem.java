import java.util.*;
import java.util.concurrent.*;

//////////////////////////////////////////////////////////
// ENUMS
//////////////////////////////////////////////////////////

enum NotificationType {
    PROMOTIONAL,
    TRANSACTIONAL,
    INFO
}

enum Priority {
    HIGH(3),
    MEDIUM(2),
    LOW(1);

    int value;

    Priority(int value) {
        this.value = value;
    }
}

//////////////////////////////////////////////////////////
// NOTIFICATION
//////////////////////////////////////////////////////////

class Notification implements Comparable<Notification> {

    int id;
    String message;
    String userId;
    NotificationType type;
    Priority priority;

    public Notification(
            int id,
            String message,
            String userId,
            NotificationType type,
            Priority priority) 
            {

        this.id = id;
        this.message = message;
        this.userId = userId;
        this.type = type;
        this.priority = priority;
    }

    @Override
    public int compareTo(Notification other) {

        // Higher priority first
        return other.priority.value - this.priority.value;
    }

    @Override
    public String toString() {

        return "["
                + priority
                + "] "
                + message;
    }
}

//////////////////////////////////////////////////////////
// CHANNELS
//////////////////////////////////////////////////////////

interface NotificationChannel {

    void send(Notification notification);
}

class SMSChannel implements NotificationChannel {

    @Override
    public void send(Notification notification) {

        System.out.println(
                Thread.currentThread().getName()
                        + " -> SMS sent to "
                        + notification.userId
                        + " : "
                        + notification.message);
    }
}

class EmailChannel implements NotificationChannel {

    @Override
    public void send(Notification notification) {

        System.out.println(
                Thread.currentThread().getName()
                        + " -> EMAIL sent to "
                        + notification.userId
                        + " : "
                        + notification.message);
    }
}

//////////////////////////////////////////////////////////
// USER PREFERENCE REPOSITORY
//////////////////////////////////////////////////////////

class UserPreferenceRepository {

    private final Map<String, Set<NotificationChannel>>
            preferences = new ConcurrentHashMap<>();

    public void addPreference(
            String userId,
            Set<NotificationChannel> channels) {

        preferences.put(userId, channels);
    }

    public Set<NotificationChannel> getChannels(
            String userId) {

        return preferences.getOrDefault(
                userId,
                Collections.emptySet());
    }
}

//////////////////////////////////////////////////////////
// DISPATCH STRATEGY
//////////////////////////////////////////////////////////

interface NotificationDispatchStrategy {

    void dispatch(
            Notification notification,
            Set<NotificationChannel> channels);
}

class AsyncDispatchStrategy
        implements NotificationDispatchStrategy {

    private final ExecutorService executor =
            Executors.newFixedThreadPool(1);

    @Override
    public void dispatch(
            Notification notification,
            Set<NotificationChannel> channels) {

        for (NotificationChannel channel : channels) {

            executor.submit(() -> {

                try {

                    channel.send(notification);

                } catch (Exception e) {

                    System.out.println(
                            "Failed to send notification");
                }
            });
        }
    }
}

//////////////////////////////////////////////////////////
// NOTIFICATION QUEUE
//////////////////////////////////////////////////////////

class NotificationQueue {

    private final PriorityBlockingQueue<Notification>
            queue = new PriorityBlockingQueue<>();

    public void addNotification(
            Notification notification) {

        queue.offer(notification);
    }

    public Notification takeNotification()
            throws InterruptedException {

        return queue.take();
    }
}

//////////////////////////////////////////////////////////
// WORKER
//////////////////////////////////////////////////////////

class NotificationWorker implements Runnable {

    private final NotificationQueue queue;

    private final UserPreferenceRepository repository;

    private final NotificationDispatchStrategy strategy;

    public NotificationWorker(
            NotificationQueue queue,
            UserPreferenceRepository repository,
            NotificationDispatchStrategy strategy) {

        this.queue = queue;
        this.repository = repository;
        this.strategy = strategy;
    }

    @Override
    public void run() {

        while (true) {

            try {

                Notification notification =
                        queue.takeNotification();

                System.out.println(
                        Thread.currentThread().getName()
                                + " PROCESSING -> "
                                + notification);

                Set<NotificationChannel> channels =
                        repository.getChannels(
                                notification.userId);

                strategy.dispatch(
                        notification,
                        channels);

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }
}

//////////////////////////////////////////////////////////
// SERVICE
//////////////////////////////////////////////////////////

class NotificationService {

    private final NotificationQueue queue;

    public NotificationService(
            NotificationQueue queue) {

        this.queue = queue;
    }

    public void sendNotification(
            Notification notification) {

        queue.addNotification(notification);
    }
}

//////////////////////////////////////////////////////////
// MAIN
//////////////////////////////////////////////////////////

public class NotificationSystem {

    public static void main(String[] args)
            throws Exception {

        //////////////////////////////////////////////////
        // SETUP
        //////////////////////////////////////////////////

        UserPreferenceRepository repository =
                new UserPreferenceRepository();

        Set<NotificationChannel> harshChannels =
                new HashSet<>();

        harshChannels.add(new SMSChannel());

        harshChannels.add(new EmailChannel());

        repository.addPreference(
                "harsh",
                harshChannels);

        //////////////////////////////////////////////////

        NotificationQueue queue =
                new NotificationQueue();

        NotificationDispatchStrategy strategy =
                new AsyncDispatchStrategy();

        //////////////////////////////////////////////////
        // START WORKERS
        //////////////////////////////////////////////////

        ExecutorService workerPool =
                Executors.newFixedThreadPool(1);

        for (int i = 0; i < 3; i++) {

            workerPool.submit(
                    new NotificationWorker(
                            queue,
                            repository,
                            strategy));
        }

        //////////////////////////////////////////////////

        NotificationService service =
                new NotificationService(queue);

        //////////////////////////////////////////////////
        // ADD NOTIFICATIONS
        //////////////////////////////////////////////////

        service.sendNotification(
                new Notification(
                        1,
                        "Big Sale Offer",
                        "harsh",
                        NotificationType.PROMOTIONAL,
                        Priority.LOW));

        service.sendNotification(
                new Notification(
                        2,
                        "OTP Verification",
                        "harsh",
                        NotificationType.TRANSACTIONAL,
                        Priority.HIGH));

        service.sendNotification(
                new Notification(
                        3,
                        "Account Update",
                        "harsh",
                        NotificationType.INFO,
                        Priority.MEDIUM));

        //////////////////////////////////////////////////

        Thread.sleep(5000);

        workerPool.shutdown();
    }
}