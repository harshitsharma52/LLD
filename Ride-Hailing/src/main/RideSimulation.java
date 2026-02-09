
import domain.Driver;
import domain.Location;
import domain.PaymentProcessor;
import domain.Ride;
import domain.RideMatcher;
import domain.Rider;
import domain.enums.DriverStatus;
import java.util.Arrays;
import java.util.List;

public class RideSimulation {

    public static void main(String[] args) {

        Rider rider = new Rider(1, "Harsh");
        Location pickup = new Location(12.9, 77.6);
        Location drop = new Location(13.0, 77.7);

        Driver d1 = new Driver(101, "Driver-1", pickup);
        Driver d2 = new Driver(102, "Driver-2", pickup);

        d1.status = DriverStatus.ONLINE;
        d2.status = DriverStatus.ONLINE;

        List<Driver> drivers = Arrays.asList(d1, d2);

        Ride ride = new Ride(1, rider, pickup, drop, 250);
        rider.activeRide = ride;

        Driver assigned = RideMatcher.match(ride, drivers);
        System.out.println("Driver assigned → " + assigned.name);

        ride.accept();
        ride.start();

        // when rider tries to request another ride while one is active
        System.out.println("\n--- Rider tries to request another ride ---");

        if (rider.hasActiveRide()) {
        System.out.println("❌ Rider already has an active ride. Request rejected.");
        } else {
        Ride ride2 = new Ride(2, rider, pickup, drop, 300);
        rider.activeRide = ride2;
        }

        // when rider tries to request another ride while one is active

        if (PaymentProcessor.pay(ride.fare)) {
        ride.complete();
        }

        // DRIVER DOUBLE ASSIGNMENT

        // Location pickup = new Location(12.9, 77.6);
        // Location drop = new Location(13.0, 77.7);

        // Driver d1 = new Driver(101, "Driver-1", pickup);
        // d1.status = DriverStatus.ONLINE;

        // List<Driver> drivers = Arrays.asList(d1);

        // Rider r1 = new Rider(1, "Rider-1");
        // Rider r2 = new Rider(2, "Rider-2");

        // Ride ride1 = new Ride(1, r1, pickup, drop, 200);
        // Ride ride2 = new Ride(2, r2, pickup, drop, 220);

        // r1.activeRide = ride1;
        // r2.activeRide = ride2;

        // Thread t1 = new Thread(() -> {
        // Driver assigned = RideMatcher.match(ride1, drivers);
        // System.out.println("Thread-1 assigned: " +
        // (assigned != null ? assigned.name : "NO DRIVER"));
        // });

        // Thread t2 = new Thread(() -> {
        // Driver assigned = RideMatcher.match(ride2, drivers);
        // System.out.println("Thread-2 assigned: " +
        // (assigned != null ? assigned.name : "NO DRIVER"));
        // });

        // t1.start();
        // t2.start();

        // Without rideLock, a ride can be cancelled and assigned at the same time,
        // causing an inconsistent system state.
        // We will simulate:

        // Thread-1 → matching driver to ride
        // Thread-2 → cancelling the same ride concurrently
        // eg : Rider Cancels While Matching Is Running

        // Location pickup = new Location(12.9, 77.6);
        // Location drop = new Location(13.0, 77.7);

        // Driver driver = new Driver(101, "Driver-1", pickup);
        // driver.status = DriverStatus.ONLINE;

        // Rider rider = new Rider(1, "Rider-1");
        // Ride ride = new Ride(1, rider, pickup, drop, 200);
        // rider.activeRide = ride;

        // // 🧵 Thread-1 → Assign driver
        // Thread t1 = new Thread(() -> {
        //     BuggyRideMatcher.assignDriver(ride, driver);
        // }, "Thread-1");

        // // 🧵 Thread-2 → Cancel SAME ride
        // Thread t2 = new Thread(() -> {
        //     try {
        //         Thread.sleep(100); // ensures overlap
        //         ride.cancel();
        //         System.out.println("Thread-2: Ride cancelled by user");
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        // }, "Thread-2");

        // t1.start();
        // t2.start();

        // try {
        //     t1.join();
        //     t2.join();
        // } catch (InterruptedException e) {
        //     Thread.currentThread().interrupt(); // best practice
        //     System.out.println("Main thread interrupted");
        // }

    }
}
