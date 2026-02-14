package domain;

public class Ticket {
    public Vehicle vehicle;
    public ParkingSpot spot;
    public long entryTime;

    public Ticket(Vehicle vehicle, ParkingSpot spot) {
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = System.currentTimeMillis();
    }

    public long getEntryTime() {
        return entryTime;
    }
        
}
