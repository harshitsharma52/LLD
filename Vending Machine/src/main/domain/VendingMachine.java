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

    public boolean simulateDispenseFailure = false;

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

        // Save recovery checkpoint
        Recovery recovery =
                new Recovery(id, transaction.id, MachineStateType.PROCESSING);
        recovery.moneyCollected = true;
        recovery.inventoryDeducted = false;
        recoveryManager.save(recovery);

        // ❌ insufficient money
        if (transaction.insertedAmount < selectedProduct.price) {
            refundAndReset();
            return;
        }

        // 💥 SIMULATE FAILURE BEFORE DISPENSE
        if (simulatePowerFailure) {
            System.out.println("💥 POWER FAILURE BEFORE DISPENSING");
            return;
        }

        setState(new DispensingState());

        // Update recovery state
        recovery.state = MachineStateType.DISPENSING;
        recoveryManager.save(recovery);

        dispense();
    }

 public void dispense() {

    Recovery recovery = recoveryManager.load();

    // Deduct inventory ONLY ONCE
    if (!recovery.inventoryDeducted) {
        inventory.put(
                selectedProduct,
                inventory.get(selectedProduct) - 1);

        recovery.inventoryDeducted = true;
        recoveryManager.save(recovery);
    }

    // 💥 Simulate failure DURING dispensing
    if (simulateDispenseFailure) {
        System.out.println("💥 POWER FAILURE DURING DISPENSING");
        return; // Crash before completion
    }

    System.out.println("Dispensing product: " + selectedProduct.name);

    double change =
            transaction.insertedAmount - selectedProduct.price;

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

        System.out.println("Recovering machine... Last state: " + r.state);

        if (r.state == MachineStateType.PROCESSING) {

            if (r.moneyCollected) {
                System.out.println(
                        "Power failure during PROCESSING. Refunding ₹"
                                + transaction.insertedAmount);

                transaction.status = TransactionStatus.CANCELLED;
            }

            recoveryManager.markCompleted();
            setState(new IdleState());
            return;
        }

        if (r.state == MachineStateType.DISPENSING) {

            System.out.println(
                    "Power failure during DISPENSING. Completing dispense.");

            dispense(); // safe (idempotent)
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
        recoveryManager.markCompleted();

        transaction = null;
        setState(new IdleState());
    }
}
