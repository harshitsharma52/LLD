package domain.state;

import domain.Ride;
import domain.enums.RideStatus;

public class CompletedState implements RideState {

    public void accept(Ride ride) {
        throw new RuntimeException("Ride completed");
    }

    public void start(Ride ride) {
        throw new RuntimeException("Ride completed");
    }

    public void complete(Ride ride) {
        throw new RuntimeException("Already completed");
    }

    public void cancel(Ride ride) {
        throw new RuntimeException("Cannot cancel");
    }

    public String name() {
        return RideStatus.COMPLETED.name();
    }
}
