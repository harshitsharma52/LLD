
import adapter.PaymentProcessor;
import adapter.RazorPayAdapter;
import adapter.RazorPayGateway;
import domain.ParkingFloor;
import domain.ParkingSpot;
import domain.PricingStrategy;
import domain.Receipt;
import domain.Ticket;
import domain.Vehicle;
import domain.VehicleType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkingLot {

    List<ParkingFloor> floors;
    Map<String, Ticket> activeTickets = new HashMap<>();
    PaymentProcessor paymentProcessor;

    public ParkingLot(List<ParkingFloor> floors, PaymentProcessor paymentProcessor) {
        this.floors = floors;
        this.paymentProcessor = paymentProcessor;
    }

    public Ticket parkVehicle(Vehicle vehicle) {
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.findFreeSpot(vehicle);
            if (spot != null) {
                spot.park(vehicle);
                Ticket ticket = new Ticket(vehicle, spot);
                activeTickets.put(vehicle.number, ticket);
                return ticket;
            }
        }
        return null;
    }

    public Receipt unparkVehicle(String vehicleNumber) {
        Ticket ticket = activeTickets.get(vehicleNumber);
        if (ticket == null) {
            return null;
        }

        int amount = PricingStrategy.calculatePrice(ticket);
        if (!paymentProcessor.pay(amount)) {
            return null;
        }

        ticket.spot.unpark();
        activeTickets.remove(vehicleNumber);

        return new Receipt(vehicleNumber, amount);
    }

    public static void main(String[] args) {
        // Parking lot system entry point

        List<ParkingSpot> spots = List.of(new ParkingSpot(1, VehicleType.CAR), new ParkingSpot(2, VehicleType.BIKE));

        ParkingFloor floor1 = new ParkingFloor(1, spots);

        PaymentProcessor payment = new RazorPayAdapter(new RazorPayGateway());

        ParkingLot lot = new ParkingLot(List.of(floor1), payment);

        Vehicle car = new Vehicle("KA-01-1111", VehicleType.BIKE);
        Ticket ticket = lot.parkVehicle(car);

        Receipt receipt = lot.unparkVehicle("KA-01-1111");

    }
}
