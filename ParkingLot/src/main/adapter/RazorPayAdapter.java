package adapter;

public class RazorPayAdapter implements PaymentProcessor {
    RazorPayGateway gateway;

    public RazorPayAdapter(RazorPayGateway gateway) {
        this.gateway = gateway;
    }

    public boolean pay(int amount) {
        return gateway.makePayment(amount);
    }
}
