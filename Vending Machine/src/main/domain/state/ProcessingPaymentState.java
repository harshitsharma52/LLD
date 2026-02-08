package domain.state;

import domain.Denomination;
import domain.VendingMachine;

class ProcessingPaymentState implements VendingMachineState {

    public void insertMoney(VendingMachine vm, Denomination d) {
        vm.addMoney(d);

        if (vm.isPaymentComplete()) {
            vm.startRecovery("DISPENSING");
            vm.setState(new DispensingState());
            vm.dispense();
        }
    }

    public void cancel(VendingMachine vm) {
        vm.refund();
        vm.setState(new IdleState());
    }

    public String name() {
        return "PROCESSING_PAYMENT";
    }
}
