package domain.state;

import domain.Denomination;
import domain.VendingMachine;

public interface VendingMachineState {
    void insertMoney(VendingMachine vm, Denomination d);
    String name();
}
