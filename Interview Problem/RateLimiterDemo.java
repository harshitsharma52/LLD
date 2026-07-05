import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

enum ClientType {
    NORMAL,
    PREMIUM
}

class Client {

    String id; //  name
    ClientType type;

    Client(String id, ClientType type) {
        this.id = id;
        this.type = type;
    }
}

class RateLimitConfig {

    int capacity;
    double refillRate;

    RateLimitConfig(int capacity, double refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
    }
}

interface RateLimiterStrategy {

    boolean allowRequest();
}

class TokenBucket implements RateLimiterStrategy {

    int capacity;
    double refillRate;

    double tokens;
    long lastRefillTime;

    TokenBucket(RateLimitConfig config) {

        this.capacity = config.capacity;
        this.refillRate = config.refillRate;

        this.tokens = capacity;
        this.lastRefillTime = System.nanoTime();
    }

    @Override
    public synchronized boolean allowRequest() {

        refill();

        System.out.println(
                Thread.currentThread().getName()
                        + " sees tokens = "
                        + tokens
        );

        if (tokens >= 1) {

            tokens--;

            return true;
        }

        return false;
    }

    private void refill() {

        long currentTime = System.nanoTime();

        double secondsPassed =
                (currentTime - lastRefillTime)
                        / 1_000_000_000.0;

        double tokensToAdd =
                secondsPassed * refillRate;

        tokens = Math.min(
                capacity,
                tokens + tokensToAdd
        );

        lastRefillTime = currentTime;
    }
}

class RateLimiter {

    Map<String, RateLimiterStrategy> buckets =
            new ConcurrentHashMap<>(); // which client id(name) has which bucket

    Map<ClientType, RateLimitConfig> configs =
            new ConcurrentHashMap<>(); // which client type has which config

    public void addConfig(
            ClientType type,
            RateLimitConfig config
    ) {

        configs.put(type, config);
    }

    public boolean allowRequest(Client client) {

//         computeIfAbsent in English:

// “Give me the value for this key. If the key is absent, compute/create the value and store it.”



// Check buckets map
//        ↓
// Is client.id present?
//    /              \
//  YES               NO
//   |                 |
// return bucket    create bucket
//                     |
//                  store bucket
//                     |
//                  return bucket

        RateLimiterStrategy bucket =
                buckets.computeIfAbsent(
                        client.id,
                        id -> new TokenBucket(
                                configs.get(client.type)
                        )
                );

        return bucket.allowRequest();
    }
}

public class RateLimiterDemo {

    public static void main(String[] args)
            throws Exception {

        RateLimiter rateLimiter =
                new RateLimiter();

        rateLimiter.addConfig(
                ClientType.NORMAL,
                new RateLimitConfig(1, 0)
        );

        rateLimiter.addConfig(
                ClientType.PREMIUM,
                new RateLimitConfig(5, 2)
        );

        Client harshit =
                new Client(
                        "harshit",
                        ClientType.NORMAL
                );

        Client rahul =
                new Client(
                        "rahul",
                        ClientType.PREMIUM
                );

        Runnable request = () -> {

            boolean allowed =
                    rateLimiter.allowRequest(harshit);

            System.out.println(
                    Thread.currentThread().getName()
                            + " -> "
                            + (allowed ? "ALLOWED" : "REJECTED")
            );
        };

        Thread t1 =
                new Thread(request, "Thread-1");

        Thread t2 =
                new Thread(request, "Thread-2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println(
                "\nPremium User Requests"
        );

        System.out.println(
                rateLimiter.allowRequest(rahul)
        );

        System.out.println(
                rateLimiter.allowRequest(rahul)
        );

        System.out.println(
                rateLimiter.allowRequest(rahul)
        );
    }
}