package domain.state;

import domain.Denomination;
import domain.VendingMachine;

public class IdleState implements VendingMachineState {

    public void insertMoney(VendingMachine vm, Denomination d) {
        System.out.println("Starting payment");
        vm.startRecovery("PROCESSING_PAYMENT");
        vm.addMoney(d);
        vm.setState(new ProcessingPaymentState());
    }

    public void cancel(VendingMachine vm) {
        System.out.println("Nothing to cancel");
    }

    public String name() {
        return "IDLE";
    }
}
