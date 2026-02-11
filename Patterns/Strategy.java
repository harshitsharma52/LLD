package Patterns;


// 1️⃣ Strategy Interface
interface PaymentStrategy {
    void pay(int amount);
}

class CreditCardPayment implements PaymentStrategy {
    public void pay(int amount) {
        System.out.println("Paid ₹" + amount + " using Credit Card");
    }
}

class UpiPayment implements PaymentStrategy {
    public void pay(int amount) {
        System.out.println("Paid ₹" + amount + " using UPI");
    }
}

class CashPayment implements PaymentStrategy {
    public void pay(int amount) {
        System.out.println("Paid ₹" + amount + " using Cash");
    }
}


class PaymentService {
    private PaymentStrategy strategy;

    public PaymentService(PaymentStrategy strategy) {
        this.strategy = strategy;
    }


// using setter to change the strategy at runtime
    public void setStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public void makePayment(int amount) {
        strategy.pay(amount);
    }
}

public class Strategy {

    public static void main(String[] args) {

        PaymentService payment = new PaymentService(new UpiPayment());
        payment.makePayment(500);

        payment.setStrategy(new CreditCardPayment());
        payment.makePayment(2000);

        payment.setStrategy(new CashPayment());
        payment.makePayment(100);
    }
    
}
