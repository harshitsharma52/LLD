package domain.state;

import domain.Ride;
import domain.enums.RideStatus;

public class RequestedState implements RideState {

    public void accept(Ride ride) {
        ride.setState(new AssignedState());
    }

    public void start(Ride ride) {
        throw new RuntimeException("Ride not assigned");
    }

    public void complete(Ride ride) {
        throw new RuntimeException("Ride not started");
    }

    public void cancel(Ride ride) {
        ride.setState(new CancelledState());
    }

    public String name() {
        return RideStatus.REQUESTED.name();
    }
}
