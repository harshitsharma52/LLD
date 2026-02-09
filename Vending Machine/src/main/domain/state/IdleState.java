package domain.state;

import domain.Denomination;
import domain.VendingMachine;

public class IdleState implements VendingMachineState {

    public void insertMoney(VendingMachine vm, Denomination d) {
        System.out.println("Inserted ₹" + d.value);
        vm.startTransaction(d);
    }

    public String name() {
        return "IDLE";
    }
}
