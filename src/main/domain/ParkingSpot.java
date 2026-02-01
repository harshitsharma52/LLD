package domain;

public class ParkingSpot {
    int spotId;
    VehicleType allowedType;
    Vehicle parkedVehicle;

    public ParkingSpot(int spotId, VehicleType allowedType) {
        this.spotId = spotId;
        this.allowedType = allowedType;
    }

    public boolean isFree() {
        return parkedVehicle == null;
    }

    public boolean canPark(Vehicle vehicle) {
        return isFree() && vehicle.type == allowedType;
    }

    public void park(Vehicle vehicle) {
        parkedVehicle = vehicle;
    }

    public void unpark() {
        parkedVehicle = null;
    }

    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }
}
