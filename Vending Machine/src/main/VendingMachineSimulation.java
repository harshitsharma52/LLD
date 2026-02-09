
import domain.Denomination;
import domain.Product;
import domain.VendingMachine;

public class VendingMachineSimulation {

    public static void main(String[] args) {

        Product coke = new Product(1, "Coke", 50); // ₹50

        VendingMachine vm = new VendingMachine(101);
        vm.addInventory(coke, 5);

        // ❌ Case 1: Invalid amount
        System.out.println("💥 INVALID AMOUNT\n");

        vm.selectProduct(coke);
        vm.insertMoney(Denomination.TWENTY);

        // ⚡ Case 2: Power failure during PROCESSING
        System.out.println("💥 POWER FAIL DURING PROCESSING\n");

        vm.selectProduct(coke);
        vm.simulatePowerFailure = true; // 💥 interrupt before dispense
        vm.insertMoney(Denomination.HUNDRED);

        // power restored
        vm.simulatePowerFailure = false;
        vm.recoverOnStartup();

        // ⚡ Case 3: Power failure during DISPENSING
        System.out.println("💥 POWER FAIL DURING DISPENSING\n");

        vm.selectProduct(coke);
        vm.insertMoney(Denomination.HUNDRED);
        System.out.println("💥 POWER FAILURE DURING DISPENSING");
        vm.recoverOnStartup();
    }

    // Failure Time Recovery Action
    // During payment -> Refund
    // During dispensing -> Complete dispense
    // After completion No action

    // Guarantee: No double dispense, no money loss

    // “I used the State pattern to control valid operations per vending machine
    // state. To handle power failures, I persist the transaction and machine
    // state before critical operations. On restart, the system recovers safely by
    // either refunding or completing dispensing, ensuring consistency.”
}
