package adapter;

public class RazorPayGateway {
    boolean makePayment(int amount) {
        System.out.println("Paid ₹" + amount);
        return true;
    }
}
