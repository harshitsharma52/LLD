package domain;

import domain.enums.DriverStatus;

public class Driver {
    public int id;
    public String name;
    public DriverStatus status;
    public Location location;

    public Driver(int id, String name, Location location) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.status = DriverStatus.OFFLINE;
    }

    public boolean isAvailable() {
        return status == DriverStatus.ONLINE;
    }
}
