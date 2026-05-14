import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

//////////////////////////////////////////////////////////
// ENUMS
//////////////////////////////////////////////////////////

enum PaymentStatus {
    SUCCESS,
    FAILED
}

enum PaymentType {
    UPI,
    CARD
}

//////////////////////////////////////////////////////////
// USER
//////////////////////////////////////////////////////////

class User {

    String userId;
    String name;
    double balance;

    // Thread safety:
    // only one payment request for same user
    // can modify balance at a time

    Lock lock = new ReentrantLock();

    User(
            String userId,
            String name,
            double balance) {

        this.userId = userId;
        this.name = name;
        this.balance = balance;
    }
}

//////////////////////////////////////////////////////////
// PAYMENT
//////////////////////////////////////////////////////////

class Payment {

    String paymentId;

    User user;

    double amount;

    PaymentStatus status;

    String paymentMethod;

    Payment(
            String paymentId,
            User user,
            double amount,
            PaymentStatus status,
            String paymentMethod) {

        this.paymentId = paymentId;
        this.user = user;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    @Override
    public String toString() {

        return "Payment{"
                + "paymentId='"
                + paymentId
                + '\''
                + ", amount="
                + amount
                + ", status="
                + status
                + ", method="
                + paymentMethod
                + '}';
    }
}

//////////////////////////////////////////////////////////
// STRATEGY PATTERN
//////////////////////////////////////////////////////////

interface PaymentMethod {

    void pay(
            User user,
            double amount);
}

//////////////////////////////////////////////////////////
// UPI PAYMENT
//////////////////////////////////////////////////////////

class UPIPayment
        implements PaymentMethod {

    @Override
    public void pay(
            User user,
            double amount) {

        System.out.println(
                "UPI processing ₹"
                        + amount);
    }
}

//////////////////////////////////////////////////////////
// CARD PAYMENT
//////////////////////////////////////////////////////////

class CardPayment
        implements PaymentMethod {

    @Override
    public void pay(
            User user,
            double amount) {

        System.out.println(
                "Card processing ₹"
                        + amount);
    }
}

//////////////////////////////////////////////////////////
// FACTORY PATTERN
//////////////////////////////////////////////////////////

class PaymentFactory {

    public PaymentMethod
    getPaymentMethod(
            PaymentType type) {

        switch(type) {

            case UPI:

                return new UPIPayment();

            case CARD:

                return new CardPayment();

            default:

                throw new RuntimeException(
                        "Invalid payment type");
        }
    }
}

//////////////////////////////////////////////////////////
// PAYMENT SERVICE
//////////////////////////////////////////////////////////

class PaymentService {

    //////////////////////////////////////////////////
    // TRANSACTION STORE
    //////////////////////////////////////////////////

    Map<String, Payment>
            transactions =
            new ConcurrentHashMap<>();


    //////////////////////////////////////////////////
    // IDEMPOTENCY STORE
    //////////////////////////////////////////////////

    Map<String, Payment>
            idempotencyStore =
            new ConcurrentHashMap<>();


    //////////////////////////////////////////////////
    // FACTORY
    //////////////////////////////////////////////////

    PaymentFactory factory =
            new PaymentFactory();



    public Payment processPayment(

            String idempotencyKey,

            User user,

            double amount,

            PaymentType type) {

        //////////////////////////////////////////////////
        // CHECK IDEMPOTENCY
        //////////////////////////////////////////////////

        Payment existingPayment =
                idempotencyStore.get(
                        idempotencyKey);

        if(existingPayment != null) {

            System.out.println(

                    Thread.currentThread()
                            .getName()

                            + " DUPLICATE REQUEST");

            return existingPayment;
        }


        //////////////////////////////////////////////////
        // USER LOCK
        //////////////////////////////////////////////////

        user.lock.lock();

        try {

            //////////////////////////////////////////////////
            // CHECK AGAIN
            //////////////////////////////////////////////////

            existingPayment =
                    idempotencyStore.get(
                            idempotencyKey);

            if(existingPayment != null) {

                return existingPayment;
            }


            //////////////////////////////////////////////////
            // CHECK BALANCE
            //////////////////////////////////////////////////

            if(user.balance < amount) {

                System.out.println(
                        "Insufficient balance");

                return new Payment(

                        UUID.randomUUID()
                                .toString(),

                        user,

                        amount,

                        PaymentStatus.FAILED,

                        type.name());
            }

            //////////////////////////////////////////////////
            // DEDUCT BALANCE
            //////////////////////////////////////////////////

            user.balance -= amount;


            //////////////////////////////////////////////////
            // STRATEGY + FACTORY
            //////////////////////////////////////////////////

            PaymentMethod method =
                    factory.getPaymentMethod(
                            type);

            method.pay(
                    user,
                    amount);


            //////////////////////////////////////////////////
            // CREATE PAYMENT
            //////////////////////////////////////////////////

            Payment payment =
                    new Payment(

                            UUID.randomUUID()
                                    .toString(),

                            user,

                            amount,

                            PaymentStatus.SUCCESS,

                            type.name());


            //////////////////////////////////////////////////
            // STORE TRANSACTION
            //////////////////////////////////////////////////

            transactions.put(
                    payment.paymentId,
                    payment);


            //////////////////////////////////////////////////
            // STORE IDEMPOTENCY
            //////////////////////////////////////////////////

            idempotencyStore.put(
                    idempotencyKey,
                    payment);


            System.out.println(

                    Thread.currentThread()
                            .getName()

                            + " PAYMENT SUCCESS");

            System.out.println(

                    "Remaining balance : "

                            + user.balance);


            return payment;

        }

        finally {

            user.lock.unlock();
        }
    }
}

//////////////////////////////////////////////////////////
// MAIN
//////////////////////////////////////////////////////////

public class PaymentSystem {

    public static void main(
            String[] args) {

        //////////////////////////////////////////////////
        // USER
        //////////////////////////////////////////////////

        User harsh =
                new User(
                        "U1",
                        "Harsh",
                        1000);

        //////////////////////////////////////////////////
        // SERVICE
        //////////////////////////////////////////////////

        PaymentService service =
                new PaymentService();

        //////////////////////////////////////////////////
        // THREAD POOL
        //////////////////////////////////////////////////

        ExecutorService executor =
                Executors.newFixedThreadPool(
                        2);

        //////////////////////////////////////////////////
        // SAME REQUEST RETRY
        //////////////////////////////////////////////////

        String key="PAY123";


        executor.submit(() -> {

            service.processPayment(

                    key,

                    harsh,

                    500,

                    PaymentType.UPI);

        });

        executor.submit(() -> {

            service.processPayment(

                    key,

                    harsh,

                    500,

                    PaymentType.UPI);

        });


        executor.shutdown();
    }
}