package domain;

import domain.enums.DriverStatus;
import domain.state.AssignedState;

public class BuggyRideMatcher {

    public static void assignDriver(Ride ride, Driver driver) {

        String dLock = "driver_" + driver.id;

        if (!DistributedLockManager.acquireLock(dLock)) {
            System.out.println("Thread-1: Could not lock driver");
            return;
        }

        try {
            System.out.println("Thread-1: Driver lock acquired");

            // ⏱ Artificial delay to create race condition
            Thread.sleep(200);

            driver.status = DriverStatus.ON_TRIP;
            ride.driver = driver;
            ride.setState(new AssignedState());

            System.out.println("Thread-1: Driver assigned to ride");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DistributedLockManager.releaseLock(dLock);
            System.out.println("Thread-1: Driver lock released");
        }
    }
}
