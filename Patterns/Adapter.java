package Patterns;

interface PaymentGateway {
    void pay(double amount);
}

class PayPalGateway implements PaymentGateway {
    @Override
    public void pay(double amount) {
        System.out.println("Processing payment of $" + amount + " through PayPal...");
    }
}

// Adaptee: 
// An existing class with an incompatible interface
class RazorpayGateway  {
    
    public void makePayment(double amount) {
        System.out.println("Processing payment of $" + amount + " through Razorpay...");
    }
}


// Adapter Class:
// Allows RazorpayAPI to be used where PaymentGateway is expected
class RazorpayAdapter implements PaymentGateway {

    private RazorpayGateway razorpayGateway;

    public RazorpayAdapter(RazorpayGateway razorpayGateway) {
        this.razorpayGateway = razorpayGateway;
    }

    @Override
    public void pay(double amount) {
        // Adapting the makePayment method to the pay method
        razorpayGateway.makePayment(amount);
    }
}

class CheckoutService {

    private PaymentGateway gateway;

    public CheckoutService(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    public void processPayment(double amount) {
        gateway.pay(amount);
    }
   public static void main(String[] args) {
    
      PaymentGateway paypalGateway = new PayPalGateway();
      CheckoutService checkoutService1 = new CheckoutService(paypalGateway);
      checkoutService1.processPayment(100.0);

      // Using razorpay payment gateway adapter to process payment
      RazorpayGateway razorpayGateway = new RazorpayGateway();
      PaymentGateway razorpayAdapter = new RazorpayAdapter(razorpayGateway);
      CheckoutService checkoutService2 = new CheckoutService(razorpayAdapter);
      checkoutService2.processPayment(200.0);
    }
}