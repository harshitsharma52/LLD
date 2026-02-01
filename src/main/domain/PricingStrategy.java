package domain;

public class PricingStrategy {
    public static int calculatePrice(Ticket ticket) {
        VehicleType type = ticket.vehicle.type;
        return switch (type) {
            case BIKE -> 10;
            case CAR -> 20;
            case TRUCK -> 50;
        };
    }
}
