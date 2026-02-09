package domain;

import domain.state.DispensingState;
import domain.state.IdleState;
import domain.state.ProcessingState;
import domain.state.VendingMachineState;
import java.util.*;

public class VendingMachine {

    int id;
    VendingMachineState state;
    Map<Product, Integer> inventory = new HashMap<>();
    Product selectedProduct;
    Transaction transaction;
    RecoveryManager recoveryManager = new RecoveryManager();

    public boolean simulatePowerFailure = false;

    public VendingMachine(int id) {
        this.id = id;
        this.state = new IdleState();
        System.out.println("STATE → IDLE");
    }

    void setState(VendingMachineState s) {
        state = s;
        System.out.println("STATE → " + s.name());
    }

    public void selectProduct(Product p) {
        if (!inventory.containsKey(p) || inventory.get(p) == 0) {
            throw new RuntimeException("Product out of stock");
        }
        selectedProduct = p;
        System.out.println("Product selected: " + p.name
                + " (₹" + p.price + ")");
    }

    public void insertMoney(Denomination d) {
        state.insertMoney(this, d);
    }

    // ================= CORE LOGIC =================
    public void startTransaction(Denomination d) {

        transaction = new Transaction(1, selectedProduct);
        transaction.insertedAmount = d.value;

        setState(new ProcessingState());
        recoveryManager.save(
                new Recovery(id, transaction.id, "PROCESSING"));

        // ❌ insufficient money
        if (transaction.insertedAmount < selectedProduct.price) {
            refundAndReset(); // ✅ SAFE TERMINATION
            return;
        }

        // ✅ sufficient money

        // 💥 SIMULATE POWER FAILURE BEFORE DISPENSE
        if (simulatePowerFailure) {
            System.out.println("💥 POWER FAILURE BEFORE DISPENSING");
            return; // ⛔ STOP EXECUTION HERE
        }

        setState(new DispensingState());
        recoveryManager.save(
                new Recovery(id, transaction.id, "DISPENSING"));

        dispense();

    }

    public void dispense() {
        inventory.put(
                selectedProduct,
                inventory.get(selectedProduct) - 1);

        System.out.println("Dispensing product: " + selectedProduct.name);

        double change = transaction.insertedAmount - selectedProduct.price;

        System.out.println("Returning change: ₹" + change);

        transaction.status = TransactionStatus.COMPLETED;
        recoveryManager.markCompleted();
        setState(new IdleState());
    }

    // ================= POWER FAILURE RECOVERY =================
    public void recoverOnStartup() {
        Recovery r = recoveryManager.load();

        if (r == null || r.status == RecoveryStatus.COMPLETED) {
            setState(new IdleState());
            return;
        }

        if (r.state.equals("PROCESSING")) {
            System.out.println(
                    "Power failure during PROCESSING. Refunding ₹"
                            + transaction.insertedAmount);
            refundAndReset(); // 🔒 THIS IS THE KEY FIX
            return; // ⛔ STOP HERE
        }

        if (r.state.equals("DISPENSING")) {
            System.out.println(
                    "Power failure during DISPENSING. Completing dispense.");
            dispense();
        }
    }

    public void addInventory(Product product, int quantity) {
        inventory.put(product, quantity);
    }

    private void refundAndReset() {

        if (transaction == null) {
            setState(new IdleState());
            return;
        }
        System.out.println("Refunding ₹" + transaction.insertedAmount);

        transaction.status = TransactionStatus.CANCELLED;

        // 🔒 CRITICAL FIXES
        transaction = null; // terminate transaction
        recoveryManager.markCompleted(); // invalidate recovery

        setState(new IdleState());
    }

}
