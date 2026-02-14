package strategy;

import domain.PricingStrategy;
import domain.Ticket;
import domain.VehicleType;
import java.util.Map;

public class HourlyPricingStrategy implements PricingStrategy {

    private Map<VehicleType, Integer> hourlyRates;

    public HourlyPricingStrategy(Map<VehicleType, Integer> hourlyRates) {
        this.hourlyRates = hourlyRates;
    }

    @Override
    public int calculatePrice(Ticket ticket) {

        long exitTime = ticket.getEntryTime() + (3 * 60 * 60 * 1000);

        long durationMillis = exitTime - ticket.getEntryTime();

        long hours = (durationMillis / (1000 * 60 * 60)) ;

        int rate = hourlyRates.getOrDefault(ticket.vehicle.type, 0);

        return (int) (hours * rate);
    }
}