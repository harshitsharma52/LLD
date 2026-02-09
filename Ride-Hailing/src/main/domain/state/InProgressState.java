package domain.state;

import domain.Ride;
import domain.enums.DriverStatus;
import domain.enums.RideStatus;

public class InProgressState implements RideState {

    public void accept(Ride ride) {
        throw new RuntimeException("Already started");
    }

    public void start(Ride ride) {
        throw new RuntimeException("Already in progress");
    }

    public void complete(Ride ride) {
        ride.driver.status = DriverStatus.ONLINE;
        ride.rider.activeRide = null;
        ride.setState(new CompletedState());
    }

    public void cancel(Ride ride) {
        throw new RuntimeException("Cannot cancel after pickup");
    }

    public String name() {
        return RideStatus.IN_PROGRESS.name();
    }
}
