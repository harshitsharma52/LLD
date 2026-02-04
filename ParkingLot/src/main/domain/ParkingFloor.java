package domain;

import java.util.List;

public class ParkingFloor {
    int floorNumber;
    List<ParkingSpot> spots;

    public ParkingFloor(int floorNumber, List<ParkingSpot> spots) {
        this.floorNumber = floorNumber;
        this.spots = spots;
    }

    public ParkingSpot findFreeSpot(Vehicle vehicle) {
        for (ParkingSpot spot : spots) {
            if (spot.canPark(vehicle)) {
                return spot;
            }
        }
        return null;
    }
}
