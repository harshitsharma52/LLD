package domain;

import java.util.HashSet;
import java.util.Set;

public class DistributedLockManager {

    private static final Set<String> locks = new HashSet<>();

    public static boolean acquireLock(String key) {
        synchronized (locks) {
            if (locks.contains(key)) return false;
            locks.add(key);
            return true;
        }
    }

    public static void releaseLock(String key) {
        synchronized (locks) {
            locks.remove(key);
        }
    }
}
