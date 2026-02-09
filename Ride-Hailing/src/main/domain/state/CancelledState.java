package domain.state;

import domain.Ride;
import domain.enums.DriverStatus;
import domain.enums.RideStatus;

public class CancelledState implements RideState {

    public void accept(Ride ride) {
        throw new RuntimeException("Ride cancelled");
    }

    public void start(Ride ride) {
        throw new RuntimeException("Ride cancelled");
    }

    public void complete(Ride ride) {
        throw new RuntimeException("Ride cancelled");
    }

    public void cancel(Ride ride) {
        throw new RuntimeException("Already cancelled");
    }

    public String name() {
        return RideStatus.CANCELLED.name();
    }
}
