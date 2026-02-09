package domain;

import domain.enums.RideStatus;

public class Rider {
    public int id;
    public String name;
    public Ride activeRide;

    public Rider(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public boolean hasActiveRide() {
        return activeRide != null &&
               activeRide.getStatus() != RideStatus.COMPLETED &&
               activeRide.getStatus() != RideStatus.CANCELLED;
    }
}
