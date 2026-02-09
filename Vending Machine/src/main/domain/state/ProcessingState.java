package domain.state;

import domain.Denomination;
import domain.VendingMachine;
public class ProcessingState implements VendingMachineState {

    public void insertMoney(VendingMachine vm, Denomination d) {
        System.out.println("Already processing. Please wait.");
    }

    public String name() {
        return "PROCESSING";
    }
}

