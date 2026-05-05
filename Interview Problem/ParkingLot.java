
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;




interface FeeStrategy {
    double calculate(Ticket t);
}

class HourlyFee implements FeeStrategy {
    public double calculate(Ticket t) {
        long hours = java.time.Duration.between(
            t.entryTime, java.time.LocalDateTime.now()
        ).toHours();
        return Math.max(1, hours) * 20;
    }
}


interface PaymentStrategy {
    void pay(double amount);
}

class UPI implements PaymentStrategy {
    public void pay(double amount) {
       System.out.println("Paid INR " + amount);
    }
}



enum VehicleType {
    BIKE, CAR, TRUCK
}

class Vehicle {
    String number;
    VehicleType type;

    Vehicle(String number, VehicleType type) {
        this.number = number;
        this.type = type;
    }
}



class Floor {
    int id;
    List<ParkingSlot> slots;

    Floor(int id, List<ParkingSlot> slots) {
        this.id = id;
        this.slots = slots;
    }

    ParkingSlot findSlot(VehicleType type) {
        for (ParkingSlot s : slots) {
            if (s.isEmpty() && s.type == type) return s;
        }
        return null;
    }
}


class ParkingSlot {
    int id;
    VehicleType type;
    Vehicle vehicle; // null if empty

    ParkingSlot(int id, VehicleType type) {
        this.id = id;
        this.type = type;
    }

    synchronized boolean park(Vehicle v) {
        if (vehicle == null && v.type == type) {
            vehicle = v;
            return true;
        }
        return false;
    }

    synchronized void unpark() {
        vehicle = null;
    }

    boolean isEmpty() {
        return vehicle == null;
    }
}




class Ticket {
    String id;
    Vehicle vehicle;
    ParkingSlot slot;
    LocalDateTime entryTime;

    Ticket(String id, Vehicle v, ParkingSlot s) {
        this.id = id;
        this.vehicle = v;
        this.slot = s;
        this.entryTime = LocalDateTime.now();
    }
}


class EntryGate {
    Ticket generate(Vehicle v, ParkingSlot s) {
        s.park(v);
        return new Ticket(UUID.randomUUID().toString(), v, s);
    }
}


class ExitGate {
    FeeStrategy feeStrategy;
    PaymentStrategy payment;

    ExitGate(FeeStrategy f, PaymentStrategy p) {
        this.feeStrategy = f;
        this.payment = p;
    }

    void process(Ticket t) {
        double fee = feeStrategy.calculate(t);
        payment.pay(fee);
        t.slot.unpark();
    }
}




class ParkingLotManager {
    List<Floor> floors;
    Map<String, Ticket> active = new ConcurrentHashMap<>();
    EntryGate entry;


    ParkingLotManager(List<Floor> floors) {
        this.floors = floors;
        this.entry = new EntryGate();
    }

    synchronized Ticket park(Vehicle v) {
        for (Floor f : floors) {
            ParkingSlot slot = f.findSlot(v.type);
            if (slot != null) {
                Ticket t = entry.generate(v, slot);
                active.put(v.number, t);
                return t;
            }
        }
        throw new RuntimeException("Full");
    }

    void unpark(String vehicleNo, ExitGate exit) {
        Ticket t = active.remove(vehicleNo);
        if (t != null) {
            exit.process(t);
        }
    }
}


public class ParkingLot {

    public static void main(String[] args) {


        List<Floor> floors = Arrays.asList(
            new Floor(1, Arrays.asList(
                new ParkingSlot(1, VehicleType.BIKE),
                new ParkingSlot(2, VehicleType.CAR),
                new ParkingSlot(3, VehicleType.TRUCK)
            )),
            new Floor(2, Arrays.asList(
                new ParkingSlot(4, VehicleType.BIKE),
                new ParkingSlot(5, VehicleType.CAR),
                new ParkingSlot(6, VehicleType.TRUCK)
            ))
        );

        ParkingLotManager manager = new ParkingLotManager(floors);
        ExitGate exit = new ExitGate(new HourlyFee(), new UPI());

        Vehicle v1 = new Vehicle("KA-01-1234", VehicleType.CAR);
        Ticket t1 = manager.park(v1);
        System.out.println("Parked: " + t1.id);

        // Simulate some time passing
        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        manager.unpark(v1.number, exit);
    }
    
}
