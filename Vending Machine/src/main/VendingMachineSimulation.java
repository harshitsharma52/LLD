
import domain.Denomination;
import domain.Product;
import domain.VendingMachine;

public class VendingMachineSimulation {

    public static void main(String[] args) {
        Product coke = new Product(1, "Coke", 2.5);

        VendingMachine vm = new VendingMachine(101);
        vm.addInventory(coke, 5);

        vm.selectProduct(coke);
        vm.insertMoney(Denomination.ONE);
        vm.insertMoney(Denomination.ONE);
        vm.insertMoney(Denomination.ONE);

    }

    // Failure Time Recovery Action
    // During payment ->  Refund
    // During dispensing ->  Complete dispense
    // After completion No action

    // Guarantee: No double dispense, no money loss


    // “I used the State pattern to control valid operations per vending machine state. To handle power failures, I persist the transaction and machine 
    // state before critical operations. On restart, the system recovers safely by either refunding or completing dispensing, ensuring consistency.”
}
