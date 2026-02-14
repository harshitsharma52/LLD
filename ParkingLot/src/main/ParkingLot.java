
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
import strategy.HourlyPricingStrategy;

public class ParkingLot {

    List<ParkingFloor> floors;
    Map<String, Ticket> activeTickets = new HashMap<>();
    PaymentProcessor paymentProcessor;
    private PricingStrategy pricingStrategy;

    public ParkingLot(List<ParkingFloor> floors, PaymentProcessor paymentProcessor, PricingStrategy pricingStrategy) {
        this.floors = floors;
        this.paymentProcessor = paymentProcessor;
        this.pricingStrategy = pricingStrategy;
    }

    public Ticket parkVehicle(Vehicle vehicle) {
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.findFreeSpot(vehicle);
            if (spot != null) {
                spot.park(vehicle);
                Ticket ticket = new Ticket(vehicle, spot);
                activeTickets.put(vehicle.number, ticket);
                return ticket;
            } else {
                System.out.println("No available spot for vehicle: " + vehicle.number);
            }
        }
        return null;
    }

    public Receipt unparkVehicle(String vehicleNumber) {
        Ticket ticket = activeTickets.get(vehicleNumber);
        if (ticket == null) {
            return null;
        }

        int amount = pricingStrategy.calculatePrice(ticket);
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

        Map<VehicleType, Integer> rates = Map.of(
                VehicleType.BIKE, 10,
                VehicleType.CAR, 20,
                VehicleType.TRUCK, 50);

        ParkingFloor floor1 = new ParkingFloor(1, spots);

        PaymentProcessor payment = new RazorPayAdapter(new RazorPayGateway());
        PricingStrategy pricing = new HourlyPricingStrategy(rates);

        ParkingLot lot = new ParkingLot(List.of(floor1), payment, pricing);

        Vehicle car = new Vehicle("KA-01-1111", VehicleType.CAR);
        Ticket ticket = lot.parkVehicle(car);

        Receipt receipt = lot.unparkVehicle("KA-01-1111");

    }
}
