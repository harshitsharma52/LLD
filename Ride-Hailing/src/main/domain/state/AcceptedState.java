package domain.state;

import domain.Ride;
import domain.enums.RideStatus;

public class AcceptedState implements RideState {

    public void accept(Ride ride) {
        throw new RuntimeException("Already accepted");
    }

    public void start(Ride ride) {
        ride.setState(new InProgressState());
    }

    public void complete(Ride ride) {
        throw new RuntimeException("Ride not started");
    }

    public void cancel(Ride ride) {
        ride.setState(new CancelledState());
    }

    public String name() {
        return RideStatus.ACCEPTED.name();
    }
}
