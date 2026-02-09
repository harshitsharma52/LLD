package domain;

import domain.enums.RideStatus;
import domain.state.*;
import domain.state.RideState;
import domain.state.RequestedState;


public class Ride {

    public int id;
    public Rider rider;
    public Driver driver;
    public Location pickup;
    public Location drop;
    public double fare;

    private RideState state;

    public Ride(int id, Rider rider, Location pickup, Location drop, double fare) {
        this.id = id;
        this.rider = rider;
        this.pickup = pickup;
        this.drop = drop;
        this.fare = fare;
        this.state = new RequestedState();
        System.out.println("Ride state → REQUESTED");
    }

    public void setState(RideState state) {
        this.state = state;
        System.out.println("Ride state → " + state.name());
    }

    public RideStatus getStatus() {
        return RideStatus.valueOf(state.name());
    }

    public void accept()   { state.accept(this); }
    public void start()    { state.start(this); }
    public void complete() { state.complete(this); }
    public void cancel()   { state.cancel(this); }
}
