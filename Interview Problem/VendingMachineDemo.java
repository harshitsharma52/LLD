
import java.util.*;

// ==========================================================
// PRODUCT
// ==========================================================

class Product {

    private final String code;
    private final String name;
    private final int priceInCents;

    Product(String code, String name, int priceInCents) {
        this.code = code;
        this.name = name;
        this.priceInCents = priceInCents;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return priceInCents;
    }
}

// ==========================================================
// COIN
// ==========================================================

enum Coin {

    PENNY(1),
    NICKEL(5),
    DIME(10),
    QUARTER(25),
    DOLLAR(100);

    private final int valueInCents;

    Coin(int valueInCents) {
        this.valueInCents = valueInCents;
    }

    public int getValueInCents() {
        return valueInCents;
    }
}

// ==========================================================
// SLOT
// ==========================================================

class Slot {

    private final Product product;
    private int quantity;

    Slot(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public boolean isEmpty() {
        return quantity <= 0;
    }

    public void decrement() {
        if (quantity > 0) {
            quantity--;
        }
    }
}

// ==========================================================
// INVENTORY
// ==========================================================

class Inventory {

    private final Map<String, Slot> slots = new HashMap<>();

    public void addSlot(String code, Product product, int quantity) {
        slots.put(code, new Slot(product, quantity));
    }

    public boolean isAvailable(String code) {
        Slot slot = slots.get(code);
        return slot != null && !slot.isEmpty();
    }

    public Product getProduct(String code) {
        Slot slot = slots.get(code);
        return slot == null ? null : slot.getProduct();
    }

    public void dispense(String code) {
        Slot slot = slots.get(code);
        if (slot != null) {
            slot.decrement();
        }
    }
}

// ==========================================================
// STATE INTERFACE
// ==========================================================

interface VendingMachineState {

    void insertCoin(VendingMachine machine, Coin coin);

    void selectProduct(VendingMachine machine, String code);

    void refund(VendingMachine machine);
}

// ==========================================================
// IDLE STATE
// ==========================================================

class IdleState implements VendingMachineState {

    @Override
    public void insertCoin(VendingMachine machine, Coin coin) {
        machine.addBalance(coin.getValueInCents());
        System.out.println("Coin inserted: " + coin + " | Balance: " + machine.getBalance() + "c");
        machine.setState(new HasMoneyState());
    }

    @Override
    public void selectProduct(VendingMachine machine, String code) {
        System.out.println("Please insert coins before selecting a product.");
    }

    @Override
    public void refund(VendingMachine machine) {
        System.out.println("No balance to refund.");
    }
}

// ==========================================================
// HAS MONEY STATE
// ==========================================================

class HasMoneyState implements VendingMachineState {

    @Override
    public void insertCoin(VendingMachine machine, Coin coin) {
        machine.addBalance(coin.getValueInCents());
        System.out.println("Coin inserted: " + coin + " | Balance: " + machine.getBalance() + "c");
    }

    @Override
    public void selectProduct(VendingMachine machine, String code) {
        if (!machine.getInventory().isAvailable(code)) {
            System.out.println("Product " + code + " is out of stock.");
            return;
        }

        Product product = machine.getInventory().getProduct(code);
        if (product == null) {
            System.out.println("Product " + code + " not found.");
            return;
        }

        if (machine.getBalance() < product.getPrice()) {
            int needed = product.getPrice() - machine.getBalance();
            System.out.println("Insufficient balance. Need " + needed + "c more.");
            return;
        }

        machine.setSelectedProduct(code);
        machine.setState(new DispensingState());
        machine.dispenseSelected();
    }

    @Override
    public void refund(VendingMachine machine) {
        System.out.println("Refunding " + machine.getBalance() + "c");
        machine.resetBalance();
        machine.setState(new IdleState());
    }
}

// ==========================================================
// DISPENSING STATE
// ==========================================================

class DispensingState implements VendingMachineState {

    @Override
    public void insertCoin(VendingMachine machine, Coin coin) {
        System.out.println("Please wait, dispensing in progress.");
    }

    @Override
    public void selectProduct(VendingMachine machine, String code) {
        System.out.println("Please wait, dispensing in progress.");
    }

    @Override
    public void refund(VendingMachine machine) {
        System.out.println("Cannot refund while dispensing.");
    }
}

// ==========================================================
// VENDING MACHINE
// CONTEXT
// ==========================================================

class VendingMachine {

    private VendingMachineState state;
    private final Inventory inventory = new Inventory();
    private int balance = 0;
    private String selectedProductCode;

    public VendingMachine() {
        state = new IdleState();
    }

    public void insertCoin(Coin coin) {
        state.insertCoin(this, coin);
    }

    public void selectProduct(String code) {
        state.selectProduct(this, code);
    }

    public void refund() {
        state.refund(this);
    }

    public void setState(VendingMachineState state) {
        this.state = state;
    }

    public void loadProduct(String code, Product product, int quantity) {
        inventory.addSlot(code, product, quantity);
    }

    void addBalance(int cents) {
        balance += cents;
    }

    int getBalance() {
        return balance;
    }

    void resetBalance() {
        balance = 0;
        selectedProductCode = null;
    }

    void setSelectedProduct(String code) {
        selectedProductCode = code;
    }

    Inventory getInventory() {
        return inventory;
    }

    void dispenseSelected() {
        Product product = inventory.getProduct(selectedProductCode);
        if (product == null) {
            System.out.println("Selected product is invalid.");
            resetBalance();
            setState(new IdleState());
            return;
        }

        int change = balance - product.getPrice();
        inventory.dispense(selectedProductCode);

        System.out.println("Dispensing: " + product.getName());
        if (change > 0) {
            System.out.println("Returning change: " + change + "c");
        }

        resetBalance();
        setState(new IdleState());
    }
}

// ==========================================================
// DEMO
// ==========================================================

public class VendingMachineDemo {

    public static void main(String[] args) {
        VendingMachine machine = new VendingMachine();

        System.out.println("---- Buying Coke ----");

        machine.loadProduct("A1", new Product("A1", "Coke", 75), 2);

        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.QUARTER);
        machine.insertCoin(Coin.QUARTER);

        machine.selectProduct("A1");

        System.out.println("\n---- Out of Stock ----");

        machine.loadProduct("A2", new Product("A2", "Chips", 100), 0);

        machine.insertCoin(Coin.DOLLAR);
        machine.selectProduct("A2");
        machine.refund();

        System.out.println("\n---- Select Without Coin ----");

        machine.selectProduct("A1");
    }
}