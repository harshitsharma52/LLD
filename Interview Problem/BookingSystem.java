import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

//////////////////////////////////////////////////////////
// ENUMS
/////////////////////////////////////////////////////////

enum SeatStatus {
    AVAILABLE,
    HELD,
    BOOKED
}

enum BookingStatus {
    PENDING,
    CONFIRMED,
    FAILED,
    EXPIRED
}

/////////////////////////////////////////////////////////
// USER
/////////////////////////////////////////////////////////

class User {

    String userId;

    String name;

    public User(
            String userId,
            String name) {

        this.userId = userId;
        this.name = name;
    }
}

/////////////////////////////////////////////////////////
// SEAT
/////////////////////////////////////////////////////////

class Seat {

    int seatId;

    String seatNumber;

    volatile SeatStatus status;

    /////////////////////////////////////////////////////
    // THREAD SAFETY
    /////////////////////////////////////////////////////

    private final Lock seatLock = new ReentrantLock();

    public Seat(
            int seatId,
            String seatNumber) {

        this.seatId = seatId;
        this.seatNumber = seatNumber;

        this.status = SeatStatus.AVAILABLE;
    }

    /////////////////////////////////////////////////////
    // HOLD SEAT
    /////////////////////////////////////////////////////

    public boolean holdSeat() {

        seatLock.lock();

        try {

            if (status != SeatStatus.AVAILABLE) {

                return false;
            }

            status = SeatStatus.HELD;

            return true;

        } finally {

            seatLock.unlock();
        }
    }

    /////////////////////////////////////////////////////
    // CONFIRM SEAT
    /////////////////////////////////////////////////////

    public void confirmSeat() {

        status = SeatStatus.BOOKED;
    }

    /////////////////////////////////////////////////////
    // RELEASE SEAT
    /////////////////////////////////////////////////////

    public void releaseSeat() {

        status = SeatStatus.AVAILABLE;
    }
}

/////////////////////////////////////////////////////////
// SHOW
/////////////////////////////////////////////////////////

class Show {

    String showId;

    String movieName;

    Map<String, Seat> seats;

    public Show(
            String showId,
            String movieName,
            List<Seat> seatList) {

        this.showId = showId;
        this.movieName = movieName;

        seats = new HashMap<>();

        for (Seat seat : seatList) {

            seats.put(
                    seat.seatNumber,
                    seat);
        }
    }

    public Seat getSeat(
            String seatNumber) {

        return seats.get(seatNumber);
    }
}

/////////////////////////////////////////////////////////
// BOOKING
/////////////////////////////////////////////////////////

class Booking {

    String bookingId;

    User user;

    Seat seat;

    BookingStatus status;

    long createdTime;

    public Booking(
            String bookingId,
            User user,
            Seat seat,
            BookingStatus status) {

        this.bookingId = bookingId;
        this.user = user;
        this.seat = seat;
        this.status = status;

        this.createdTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {

        return "Booking{"
                + "bookingId='"
                + bookingId
                + '\''
                + ", user="
                + user.name
                + ", seat="
                + seat.seatNumber
                + ", status="
                + status
                + '}';
    }
}

/////////////////////////////////////////////////////////
// PAYMENT STRATEGY
/////////////////////////////////////////////////////////

interface PaymentStrategy {

    boolean pay(int amount);
}

class CreditCardPayment
        implements PaymentStrategy {

    @Override
    public boolean pay(int amount) {

        System.out.println(
                Thread.currentThread().getName()
                        + " PAYMENT SUCCESS : "
                        + amount);

        return true;
    }
}

/////////////////////////////////////////////////////////
// BOOKING SERVICE
/////////////////////////////////////////////////////////

class BookingService {

    /////////////////////////////////////////////////////
    // THREAD SAFE STORAGE
    /////////////////////////////////////////////////////

    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();

    /////////////////////////////////////////////////////
    // SCHEDULER
    /////////////////////////////////////////////////////

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /////////////////////////////////////////////////////
    // HOLD SEAT
    /////////////////////////////////////////////////////

    public Booking holdSeat(
            User user,
            Show show,
            String seatNumber) {

        Seat seat = show.getSeat(seatNumber);

        if (seat == null) {

            System.out.println(
                    "Seat not found");

            return null;
        }

        /////////////////////////////////////////////////
        // THREAD SAFE HOLD
        /////////////////////////////////////////////////

        boolean success = seat.holdSeat();

        if (!success) {

            System.out.println(
                    Thread.currentThread().getName()
                            + " FAILED holding "
                            + seatNumber);

            return null;
        }

        /////////////////////////////////////////////////
        // CREATE BOOKING
        /////////////////////////////////////////////////

        Booking booking = new Booking(
                UUID.randomUUID().toString(),
                user,
                seat,
                BookingStatus.PENDING);

        bookings.put(
                booking.bookingId,
                booking);

        System.out.println(
                Thread.currentThread().getName()
                        + " HELD seat "
                        + seatNumber
                        + " for "
                        + user.name);

        /////////////////////////////////////////////////
        // AUTO EXPIRE AFTER 30 SECONDS
        /////////////////////////////////////////////////

        scheduler.schedule(() -> {

            expireBooking(
                    booking.bookingId);

        }, 10, TimeUnit.SECONDS);

        return booking;
    }

    /////////////////////////////////////////////////////
    // CONFIRM BOOKING
    /////////////////////////////////////////////////////

    public void confirmBooking(
            String bookingId,
            PaymentStrategy paymentStrategy) {

        Booking booking = bookings.get(bookingId);

        if (booking == null) {

            return;
        }

        /////////////////////////////////////////////////
        // CHECK STATUS
        /////////////////////////////////////////////////

        if (booking.status != BookingStatus.PENDING) {

            System.out.println(
                    "Booking already "
                            + booking.status);

            return;
        }

        /////////////////////////////////////////////////
        // PAYMENT
        /////////////////////////////////////////////////

        boolean paymentSuccess = paymentStrategy.pay(500);

        if (!paymentSuccess) {

            booking.status = BookingStatus.FAILED;

            booking.seat.releaseSeat();

            System.out.println(
                    "Payment failed");

            return;
        }

        /////////////////////////////////////////////////
        // CONFIRM BOOKING
        /////////////////////////////////////////////////

        booking.seat.confirmSeat();

        booking.status = BookingStatus.CONFIRMED;

        System.out.println(
                "BOOKING CONFIRMED -> "
                        + booking);
    }

    /////////////////////////////////////////////////////
    // EXPIRE BOOKING
    /////////////////////////////////////////////////////

    public void expireBooking(
            String bookingId) {

        Booking booking = bookings.get(bookingId);

        if (booking == null) {

            return;
        }

        /////////////////////////////////////////////////
        // EXPIRE ONLY IF STILL PENDING
        /////////////////////////////////////////////////

        if (booking.status != BookingStatus.PENDING) {

            return;
        }

        booking.status = BookingStatus.EXPIRED;

        booking.seat.releaseSeat();

        System.out.println(
                "BOOKING EXPIRED -> "
                        + booking);
    }

    /////////////////////////////////////////////////////
    // SHUTDOWN
    /////////////////////////////////////////////////////

    public void shutdown() {

        scheduler.shutdown();
    }
}

/////////////////////////////////////////////////////////
// MAIN
/////////////////////////////////////////////////////////

public class BookingSystem {

    public static void main(String[] args)
            throws Exception {

        /////////////////////////////////////////////////
        // CREATE SEATS
        /////////////////////////////////////////////////

        List<Seat> seats = List.of(
                new Seat(1, "A1"));

        /////////////////////////////////////////////////
        // CREATE SHOW
        /////////////////////////////////////////////////

        Show show = new Show(
                "S1",
                "Avengers",
                seats);

        /////////////////////////////////////////////////
        // USERS
        /////////////////////////////////////////////////

        User harsh = new User(
                "U1",
                "Harsh");

        User aman = new User(
                "U2",
                "Aman");

        /////////////////////////////////////////////////
        // SERVICE
        /////////////////////////////////////////////////

        BookingService bookingService = new BookingService();

        /////////////////////////////////////////////////
        // THREAD POOL
        /////////////////////////////////////////////////

        ExecutorService executor = Executors.newFixedThreadPool(2);

        /////////////////////////////////////////////////
        // USER 1
        /////////////////////////////////////////////////

        executor.submit(() -> {

            Booking booking = bookingService.holdSeat(
                    harsh,
                    show,
                    "A1");

            if (booking != null) {

                bookingService.confirmBooking(
                        booking.bookingId,
                        new CreditCardPayment());
            }
        });

        /////////////////////////////////////////////////
        // USER 2
        /////////////////////////////////////////////////

        executor.submit(() -> {

            Booking booking = bookingService.holdSeat(
                    aman,
                    show,
                    "A1");

            if (booking != null) {

                bookingService.confirmBooking(
                        booking.bookingId,
                        new CreditCardPayment());
            }
        });

        /////////////////////////////////////////////////
        // WAIT
        /////////////////////////////////////////////////

        executor.shutdown();

        executor.awaitTermination(
                10,
                TimeUnit.SECONDS);

        bookingService.shutdown();

    }
}