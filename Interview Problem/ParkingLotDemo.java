import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

enum VehicleType {
    MOTORCYCLE,
    CAR,
    TRUCK
}

enum SpotType {
    SMALL,
    MEDIUM,
    LARGE
}

// ==========================================================
// VEHICLE
// ==========================================================
class Vehicle {

    private final String licensePlate;
    private final VehicleType type;

    Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }

    public String getLicensePlate() { return licensePlate; }
    public VehicleType getType() { return type; }
}

// ==========================================================
// PARKING SPOT
// No lock needed on the spot itself -- see ParkingLot.parkVehicle().
// Once a thread successfully polls this spot out of the available
// queue, it is the ONLY thread that can ever hold a reference to it
// until it's offered back. Fields are still `volatile` so a different
// thread (e.g. a reporting/dashboard thread) reading isOccupied()
// always sees the latest value.
// ==========================================================
class ParkingSpot {

    private final String spotId;
    private final SpotType type;
    private volatile boolean occupied = false;
    private volatile Vehicle currentVehicle;

    ParkingSpot(String spotId, SpotType type) {
        this.spotId = spotId;
        this.type = type;
    }

    void occupy(Vehicle vehicle) {
        this.currentVehicle = vehicle;
        this.occupied = true;
    }

    void vacate() {
        this.currentVehicle = null;
        this.occupied = false;
    }

    public String getSpotId() { return spotId; }
    public SpotType getType() { return type; }
    public boolean isOccupied() { return occupied; }
    public Vehicle getCurrentVehicle() { return currentVehicle; }
}

// ==========================================================
// TICKET
// ==========================================================
class Ticket {

    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final LocalDateTime entryTime;

    Ticket(String ticketId, Vehicle vehicle, ParkingSpot spot, LocalDateTime entryTime) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = entryTime;
    }

    public String getTicketId() { return ticketId; }
    public Vehicle getVehicle() { return vehicle; }
    public ParkingSpot getSpot() { return spot; }
    public LocalDateTime getEntryTime() { return entryTime; }
}

// ==========================================================
// Simple mapping helper -- NOT a GoF Factory, just a lookup
// function. Worth naming precisely if asked in an interview.
// ==========================================================
class SpotTypeResolver {

    static SpotType resolve(VehicleType vehicleType) {
        switch (vehicleType) {
            case MOTORCYCLE: return SpotType.SMALL;
            case CAR:        return SpotType.MEDIUM;
            case TRUCK:      return SpotType.LARGE;
            default: throw new IllegalArgumentException("Unknown vehicle type: " + vehicleType);
        }
    }
}

// ==========================================================
// STRATEGY: PricingStrategy
// ==========================================================
interface PricingStrategy {
    double calculateFee(Ticket ticket, LocalDateTime exitTime);
}

class HourlyPricingStrategy implements PricingStrategy {

    private final Map<VehicleType, Double> hourlyRate = Map.of(
            VehicleType.MOTORCYCLE, 10.0,
            VehicleType.CAR, 20.0,
            VehicleType.TRUCK, 30.0
    );

    @Override
    public double calculateFee(Ticket ticket, LocalDateTime exitTime) {

        Duration duration = Duration.between(ticket.getEntryTime(), exitTime);
        long minutes = duration.toMinutes();

        long hours = minutes / 60;
        if (minutes % 60 != 0) hours++;   // round up any partial hour
        if (hours == 0) hours = 1;        // minimum 1-hour charge

        double rate = hourlyRate.get(ticket.getVehicle().getType());
        return rate * hours;
    }
}

// ==========================================================
// PARKING LOT (Singleton -- one physical lot)
//
// CONCURRENCY DESIGN:
// Each SpotType maps to a ConcurrentLinkedQueue of free spots.
// queue.poll() atomically removes and returns an element -- if two
// threads call poll() on the same queue at the same instant, the
// JVM/queue implementation guarantees they get DIFFERENT spots (or
// one gets null if the queue is empty). This gives us thread-safe
// spot allocation WITHOUT writing a single explicit lock ourselves.
// ==========================================================
class ParkingLot {

    private static final ParkingLot instance = new ParkingLot();
    public static ParkingLot getInstance() { return instance; }

    private final Map<SpotType, ConcurrentLinkedQueue<ParkingSpot>> availableSpots = new ConcurrentHashMap<>();
    private final Map<String, Ticket> activeTickets = new ConcurrentHashMap<>();
    private final java.util.concurrent.atomic.AtomicLong ticketCounter = new java.util.concurrent.atomic.AtomicLong(0);
    private final PricingStrategy pricingStrategy;

    private ParkingLot() {
        for (SpotType type : SpotType.values()) {
            availableSpots.put(type, new ConcurrentLinkedQueue<>());
        }
        this.pricingStrategy = new HourlyPricingStrategy();
    }

    public void addSpot(ParkingSpot spot) {
        availableSpots.get(spot.getType()).offer(spot);
    }

    public Ticket parkVehicle(Vehicle vehicle) {

        SpotType requiredType = SpotTypeResolver.resolve(vehicle.getType());
        ConcurrentLinkedQueue<ParkingSpot> queue = availableSpots.get(requiredType);

        // ATOMIC: exactly one thread can ever receive a given spot from poll()
        ParkingSpot spot = queue.poll();

        if (spot == null) {
            System.out.println("REJECTED: No available " + requiredType + " spot for " + vehicle.getLicensePlate());
            return null;
        }

        spot.occupy(vehicle);

        String ticketId = "T" + ticketCounter.incrementAndGet(); // thread-safe unique ID, no lock needed
        Ticket ticket = new Ticket(ticketId, vehicle, spot, LocalDateTime.now());
        activeTickets.put(ticketId, ticket);

        System.out.println("PARKED: " + vehicle.getLicensePlate() + " -> spot " + spot.getSpotId()
                + " (ticket " + ticketId + ")");
        return ticket;
    }

    public double unparkVehicle(String ticketId) {

        Ticket ticket = activeTickets.remove(ticketId); // atomic remove

        if (ticket == null) {
            System.out.println("REJECTED: invalid ticket " + ticketId);
            return -1;
        }

        LocalDateTime exitTime = LocalDateTime.now();
        double fee = pricingStrategy.calculateFee(ticket, exitTime);

        ParkingSpot spot = ticket.getSpot();
        spot.vacate();
        availableSpots.get(spot.getType()).offer(spot); // return spot to the free pool

        System.out.println("EXIT: " + ticket.getVehicle().getLicensePlate()
                + " | spot " + spot.getSpotId() + " freed | fee: " + fee);
        return fee;
    }
}

// ==========================================================
// DEMO
// ==========================================================
public class ParkingLotDemo {

    public static void main(String[] args) throws InterruptedException {

        ParkingLot lot = ParkingLot.getInstance();

        lot.addSpot(new ParkingSpot("S1", SpotType.SMALL));
        lot.addSpot(new ParkingSpot("M1", SpotType.MEDIUM));
        lot.addSpot(new ParkingSpot("M2", SpotType.MEDIUM));
        lot.addSpot(new ParkingSpot("L1", SpotType.LARGE));

        System.out.println("---- Single-threaded flow ----");
        Ticket t1 = lot.parkVehicle(new Vehicle("CAR-1", VehicleType.CAR));
        Thread.sleep(50);
        lot.unparkVehicle(t1.getTicketId());

        System.out.println("\n---- Concurrency test: 5 cars competing for 2 MEDIUM spots ----");
        ExecutorService executor = Executors.newFixedThreadPool(5);

        for (int i = 1; i <= 5; i++) {
            int id = i;
            executor.submit(() -> lot.parkVehicle(new Vehicle("CAR-" + id, VehicleType.CAR)));
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("\nExpected: exactly 2 PARKED messages, exactly 3 REJECTED messages,");
        System.out.println("no spot ID ever assigned to two different cars -- regardless of thread timing.");
    }
}