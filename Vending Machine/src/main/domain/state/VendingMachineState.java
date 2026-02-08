package domain.state;

import domain.Denomination;
import domain.VendingMachine;

public interface VendingMachineState {

    public void insertMoney(VendingMachine vm, Denomination d);

    public void cancel(VendingMachine vm);

    public String name();
}
