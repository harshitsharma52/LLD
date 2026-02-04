package domain;

public class Receipt {
    public String vehicleNumber;
    public int amount;
    public long exitTime;

    public Receipt(String vehicleNumber, int amount) {
        this.vehicleNumber = vehicleNumber;
        this.amount = amount;
        this.exitTime = System.currentTimeMillis();
    }

    public void printReceipt() {
        System.out.println("Vehicle: " + vehicleNumber);
        System.out.println("Amount: ₹" + amount);
        System.out.println("Exit Time: " + exitTime);
    }
}
