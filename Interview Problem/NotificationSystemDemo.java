import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;


// ============================================================
// ENUMS
// ============================================================

enum ChannelType {
    EMAIL,
    SMS,
    PUSH
}

enum Priority {
    HIGH,
    LOW
}

enum NotificationStatus {
    PENDING,
    SENT,
    FAILED
}


// ============================================================
// USER
// ============================================================

class User {

    private final String userId;
    private final String name;
    private final String email;
    private final String phone;

    private final Set<ChannelType> optedOutChannels = new HashSet<>();

    public User(String userId, String name, String email, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void optOut(ChannelType type) {
        optedOutChannels.add(type);
    }

    public boolean isChannelEnabled(ChannelType type) {
        return !optedOutChannels.contains(type);
    }
}


// ============================================================
// NOTIFICATION
// ============================================================

class Notification implements Comparable<Notification> {

    private static final AtomicLong ID_COUNTER = new AtomicLong(0);
    private static final AtomicLong SEQUENCE_COUNTER = new AtomicLong(0);

    private final String notificationId;
    private final User user;
    private final String message;
    private final Priority priority;
    private final long sequenceId;

    private volatile NotificationStatus status =
            NotificationStatus.PENDING;

    public Notification(
            User user,
            String message,
            Priority priority) {

        this.notificationId =
                "N" + ID_COUNTER.incrementAndGet();

        this.sequenceId =
                SEQUENCE_COUNTER.incrementAndGet();

        this.user = user;
        this.message = message;
        this.priority = priority;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public Priority getPriority() {
        return priority;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }


    // HIGH first
    // If same priority -> FIFO
    @Override
    public int compareTo(Notification other) {

        if (this.priority != other.priority) {

            return this.priority == Priority.HIGH
                    ? -1
                    : 1;
        }

        return Long.compare(
                this.sequenceId,
                other.sequenceId
        );
    }
}


// ============================================================
// STRATEGY
// ============================================================

interface NotificationChannel {

    boolean send(Notification notification);
}


// ============================================================
// EMAIL
// ============================================================

class EmailChannel implements NotificationChannel {

    @Override
    public boolean send(Notification notification) {

        System.out.println(
                "[EMAIL] Sending to "
                        + notification.getUser().getEmail()
                        + " : "
                        + notification.getMessage()
        );

        return true;
    }
}


// ============================================================
// SMS
// ============================================================

class SmsChannel implements NotificationChannel {

    // Simulates a transient failure on the FIRST attempt per notification,
    // then succeeds -- this exists purely so the retry Decorator has
    // something real to do in the demo output. Without this, RetryingChannel
    // compiles and works correctly, but you'd never SEE it retry anything.
    private final Map<String, Integer> attemptsSoFar = new ConcurrentHashMap<>();

    @Override
    public boolean send(Notification notification) {

        int count = attemptsSoFar.merge(
                notification.getNotificationId(), 1, Integer::sum);

        if (count == 1) {
            System.out.println(
                    "[SMS] simulated transient failure for "
                            + notification.getNotificationId()
            );
            return false;
        }

        System.out.println(
                "[SMS] Sending to "
                        + notification.getUser().getPhone()
                        + " : "
                        + notification.getMessage()
        );

        return true;
    }
}


// ============================================================
// PUSH
// ============================================================

class PushChannel implements NotificationChannel {

    @Override
    public boolean send(Notification notification) {

        System.out.println(
                "[PUSH] Sending to "
                        + notification.getUser().getName()
                        + " : "
                        + notification.getMessage()
        );

        return true;
    }
}


// ============================================================
// FACTORY
// ============================================================

class ChannelFactory {

    public static NotificationChannel getChannel(
            ChannelType type) {

        switch (type) {

            case EMAIL:
                return new EmailChannel();

            case SMS:
                return new SmsChannel();

            case PUSH:
                return new PushChannel();

            default:
                throw new IllegalArgumentException(
                        "Unknown channel: " + type
                );
        }
    }
}


// ============================================================
// RETRY STRATEGY
// ============================================================

interface RetryPolicy {

    boolean shouldRetry(int attempts);

    long getDelayMillis(int attempts);
}


// ============================================================
// FIXED RETRY
// ============================================================

class FixedRetryPolicy implements RetryPolicy {

    private final int maxAttempts;
    private final long delayMillis;

    public FixedRetryPolicy(
            int maxAttempts,
            long delayMillis) {

        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    @Override
    public boolean shouldRetry(int attempts) {

        return attempts < maxAttempts;
    }

    @Override
    public long getDelayMillis(int attempts) {

        return delayMillis;
    }
}


// // ============================================================
// // EXPONENTIAL BACKOFF
// // ============================================================

// class ExponentialBackoffRetryPolicy implements RetryPolicy {

//     private final int maxAttempts;
//     private final long baseDelayMillis;

//     public ExponentialBackoffRetryPolicy(
//             int maxAttempts,
//             long baseDelayMillis) {

//         this.maxAttempts = maxAttempts;
//         this.baseDelayMillis = baseDelayMillis;
//     }

//     @Override
//     public boolean shouldRetry(int attempts) {

//         return attempts < maxAttempts;
//     }

//     @Override
//     public long getDelayMillis(int attempts) {

//         return baseDelayMillis * (1L << attempts);
//     }
// }


// ============================================================
// RETRY DECORATOR
// ============================================================

class RetryingChannel implements NotificationChannel {

    private final NotificationChannel delegate;
    private final RetryPolicy retryPolicy;

    public RetryingChannel(
            NotificationChannel delegate,
            RetryPolicy retryPolicy) {

        this.delegate = delegate;
        this.retryPolicy = retryPolicy;
    }

    @Override
    public boolean send(Notification notification) {

        int attempts = 0;

        while (true) {

            attempts++;

            boolean success =
                    delegate.send(notification);

            if (success) {
                return true;
            }

            if (!retryPolicy.shouldRetry(attempts)) {
                return false;
            }

            try {

                Thread.sleep(
                        retryPolicy.getDelayMillis(attempts)
                );

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                return false;
            }
        }
    }
}


// ============================================================
// NOTIFICATION SERVICE
// ============================================================

class NotificationService {

    // Thread-safe priority queue
    private final BlockingQueue<Notification> queue =
            new PriorityBlockingQueue<>();


    // 3 concurrent workers
    private final ExecutorService workers =
            Executors.newFixedThreadPool(3);


    private final RetryPolicy retryPolicy =
            new FixedRetryPolicy(3, 200);


    private volatile boolean running = true;


    public NotificationService() {

        for (int i = 0; i < 3; i++) {

            workers.submit(this::processLoop);
        }
    }


    // Producer
    public void enqueue(Notification notification) {

        queue.offer(notification);
    }


    // Consumer
    private void processLoop() {

        while (running) {

            try {

                Notification notification =
                        queue.take();

                dispatch(notification);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                return;
            }
        }
    }


    private void dispatch(Notification notification) {

        User user = notification.getUser();

        boolean anySucceeded = false;


        for (ChannelType type : ChannelType.values()) {

            // Respect user's preference
            if (!user.isChannelEnabled(type)) {
                continue;
            }


            NotificationChannel channel =
                    ChannelFactory.getChannel(type);


            // Add retry behavior
            NotificationChannel retryingChannel =
                    new RetryingChannel(
                            channel,
                            retryPolicy
                    );


            boolean success =
                    retryingChannel.send(notification);


            if (success) {

                anySucceeded = true;

            } else {

                System.out.println(
                        notification.getNotificationId()
                                + " failed on "
                                + type
                );
            }
        }


        notification.setStatus(
                anySucceeded
                        ? NotificationStatus.SENT
                        : NotificationStatus.FAILED
        );
    }


    public void shutdown() {

        running = false;

        workers.shutdownNow();
    }
}


// ============================================================
// DEMO
// ============================================================

public class NotificationSystemDemo {

    public static void main(String[] args)
            throws InterruptedException {


        NotificationService service =
                new NotificationService();


        User alice =
                new User(
                        "U1",
                        "Alice",
                        "alice@example.com",
                        "9990001111"
                );


        User bob =
                new User(
                        "U2",
                        "Bob",
                        "bob@example.com",
                        "9990002222"
                );


        // Bob doesn't want SMS
        bob.optOut(ChannelType.SMS);


        // LOW priority
        service.enqueue(
                new Notification(
                        alice,
                        "Your order has shipped",
                        Priority.LOW
                )
        );


        // HIGH priority
        service.enqueue(
                new Notification(
                        bob,
                        "Suspicious login detected!",
                        Priority.HIGH
                )
        );


        // LOW priority
        service.enqueue(
                new Notification(
                        alice,
                        "Subscription renewal tomorrow",
                        Priority.LOW
                )
        );


        // Give workers time to process
        Thread.sleep(2000);


        service.shutdown();
    }
}