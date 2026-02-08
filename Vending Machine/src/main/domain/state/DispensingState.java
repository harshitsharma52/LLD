package domain.state;

import domain.Denomination;
import domain.VendingMachine;

class DispensingState implements VendingMachineState {

    public void insertMoney(VendingMachine vm, Denomination d) {
        System.out.println("Cannot accept money while dispensing");
    }

    public void cancel(VendingMachine vm) {
        System.out.println("Cannot cancel dispensing");
    }

    public String name() {
        return "DISPENSING";
    }
}
