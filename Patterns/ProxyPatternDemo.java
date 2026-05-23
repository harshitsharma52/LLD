package Patterns;

// Common interface
interface Internet {
    void connect();
}

// Real object
class RealInternet implements Internet {

    public void connect() {
        System.out.println("Connected to Internet");
    }
}

// PROXY CLASS
// Controls access to RealInternet
class ProxyInternet implements Internet {

    // Real object
    private RealInternet realInternet;

    public void connect() {

        // Security check
        System.out.println("Checking access...");

        // Create real object only when needed
        if (realInternet == null) {
            realInternet = new RealInternet();
        }

        // Forward request to real object
        realInternet.connect();
    }
}

// Main Class
public class ProxyPatternDemo {

    public static void main(String[] args) {

        // User talks to proxy
        Internet internet = new ProxyInternet();

        internet.connect();
    }
}