package domain;
import domain.state.IdleState;
import domain.state.VendingMachineState;
import java.util.*;

public class VendingMachine {

    int id;
    VendingMachineState state;
    Map<Product, Integer> inventory = new HashMap<>();
    Transaction transaction;
    RecoveryManager recoveryManager = new RecoveryManager();
    double currentAmount = 0;

    public VendingMachine(int id) {
        this.id = id;
        this.state = new IdleState();
    }

    public void addInventory(Product product, int quantity) {
        inventory.put(product, quantity);
    }

    public  void setState(VendingMachineState s) {
        state = s;
        System.out.println("STATE → " + s.name());
    }

    public void selectProduct(Product p) {
        if (!inventory.containsKey(p) || inventory.get(p) == 0)
            throw new RuntimeException("Out of stock");

        transaction = new Transaction(1, p);
    }

    public void insertMoney(Denomination d) {
        state.insertMoney(this, d);
    }

    public void cancel() {
        state.cancel(this);
    }

    public void addMoney(Denomination d) {
        currentAmount += d.value / 100.0;
        transaction.insertedAmount = currentAmount;
    }

    public boolean isPaymentComplete() {
        return currentAmount >= transaction.product.price;
    }

    public void dispense() {
        System.out.println("Dispensing " + transaction.product.name);
        inventory.put(transaction.product,
                inventory.get(transaction.product) - 1);

        double change = currentAmount - transaction.product.price;
        System.out.println("Change returned: $" + change);

        transaction.status = TransactionStatus.COMPLETED;
        currentAmount = 0;

        recoveryManager.markCompleted();
        setState(new IdleState());
    }

    public void refund() {
        System.out.println("Refunded $" + currentAmount);
        currentAmount = 0;
        transaction.status = TransactionStatus.CANCELLED;
        recoveryManager.markCompleted();
    }

    public void startRecovery(String stateName) {
        recoveryManager.save(
                new Recovery(id, transaction.id, stateName));
    }

    // POWER FAILURE RECOVERY
    public void recoverOnStartup() {
        Recovery r = recoveryManager.load();

        if (r == null || r.status == RecoveryStatus.COMPLETED) {
            setState(new IdleState());
            return;
        }

        if (r.state.equals("PROCESSING_PAYMENT")) {
            System.out.println("Recovering: refunding user");
            refund();
            setState(new IdleState());
        }

        if (r.state.equals("DISPENSING")) {
            System.out.println("Recovering: finishing dispense");
            dispense();
        }
    }
}
