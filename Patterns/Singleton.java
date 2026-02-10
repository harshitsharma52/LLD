package Patterns;

public class Singleton {

    public static void main(String[] args) {

        // Eager Singleton test
        EagerSingleton eager1 = EagerSingleton.getInstance();
        EagerSingleton eager2 = EagerSingleton.getInstance();

        System.out.println("Eager Singleton:");
        System.out.println(eager1.hashCode());
        System.out.println(eager2.hashCode());

        // Lazy Singleton test
        LazySingleton lazy1 = LazySingleton.getInstance();
        LazySingleton lazy2 = LazySingleton.getInstance();

        System.out.println("\nLazy Singleton:");
        System.out.println(lazy1.hashCode());
        System.out.println(lazy2.hashCode());

        // // thread safety test for Lazy Singleton

        // Runnable task = () -> {
        //     LazySingleton instance = LazySingleton.getInstance();
        //     System.out.println(
        //             Thread.currentThread().getName() + " -> " + instance.hashCode());
        // };

        // Thread t1 = new Thread(task, "Thread-1");
        // Thread t2 = new Thread(task, "Thread-2");

        // t1.start();
        // t2.start();
        //  // thread safety test for Lazy Singleton
    }
}

// ---------------- EAGER SINGLETON ----------------
class EagerSingleton {

    private static final EagerSingleton instance = new EagerSingleton();

    private EagerSingleton() {
        System.out.println("EagerSingleton instance created");
    }

    public static EagerSingleton getInstance() {
        return instance;
    }
}

// ---------------- LAZY SINGLETON ----------------
class LazySingleton {

    private static LazySingleton instance;

    private LazySingleton() {
        // System.out.println("LazySingleton instance created by " +
        //         Thread.currentThread().getName());
        // try {
        //     Thread.sleep(100); // force race condition
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }
    }

    public static LazySingleton getInstance() {
        if (instance == null) {
            instance = new LazySingleton();
        }
        return instance;
    }
}
