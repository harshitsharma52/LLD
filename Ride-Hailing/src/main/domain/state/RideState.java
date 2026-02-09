package domain.state;

import domain.Ride;

public interface RideState {
    void accept(Ride ride);
    void start(Ride ride);
    void complete(Ride ride);
    void cancel(Ride ride);
    String name();
}
