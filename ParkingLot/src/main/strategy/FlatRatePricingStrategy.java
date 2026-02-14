package strategy;

import domain.PricingStrategy;
import domain.Ticket;
import domain.VehicleType;
import java.util.Map;

public class FlatRatePricingStrategy implements PricingStrategy {

    private Map<VehicleType, Integer> flatRates;

    public FlatRatePricingStrategy(Map<VehicleType, Integer> flatRates) {
        this.flatRates = flatRates;
    }

    @Override
    public int calculatePrice(Ticket ticket) {
        return flatRates.getOrDefault(ticket.vehicle.type, 0);
    }
}
