package domain;

public class PaymentProcessor {

    public static boolean pay(double amount) {
        int retries = 3;

        while (retries-- > 0) {
            if (Math.random() > 0.3) {
                System.out.println("Payment successful ₹" + amount);
                return true;
            }
            System.out.println("Payment failed, retrying...");
        }
        return false;
    }
}
