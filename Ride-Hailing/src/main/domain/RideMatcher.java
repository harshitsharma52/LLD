package domain;

import domain.enums.DriverStatus;
import domain.enums.RideStatus;
import java.util.List;

public class RideMatcher {

    public static Driver match(Ride ride, List<Driver> drivers) {

        for (Driver driver : drivers) {

            if (!driver.isAvailable())
                continue;

            String dLock = "driver_" + driver.id;
            String rLock = "ride_" + ride.id;

            if (!DistributedLockManager.acquireLock(dLock))
                continue;
            if (!DistributedLockManager.acquireLock(rLock)) {
                DistributedLockManager.releaseLock(dLock);
                continue;
            }

            try {

                // Re-validate inside lock (CRITICAL)
                if (!driver.isAvailable() ||
                        ride.getStatus() != RideStatus.REQUESTED)
                    continue;
                // Assign driver (ATOMIC)
               

                driver.status = DriverStatus.ON_TRIP;
                ride.driver = driver;
                ride.setState(new domain.state.AssignedState());
                 // Driver-1 → ON_TRIP
                // Ride-1 → ASSIGNED
                return driver;

            } finally {
                DistributedLockManager.releaseLock(rLock);
                DistributedLockManager.releaseLock(dLock);
            }
        }
        return null;
    }
}
