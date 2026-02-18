package domain;

enum RecoveryStatus {
    PENDING, COMPLETED
}

enum MachineStateType {
    PROCESSING, DISPENSING
}

class Recovery {
    int machineId;
    int transactionId;
    MachineStateType state;

    boolean moneyCollected;
    boolean inventoryDeducted;

    RecoveryStatus status;

    Recovery(int machineId, int txnId, MachineStateType state) {
        this.machineId = machineId;
        this.transactionId = txnId;
        this.state = state;
        this.status = RecoveryStatus.PENDING;
        this.moneyCollected = false;
        this.inventoryDeducted = false;
    }
}
