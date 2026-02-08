package domain;

enum RecoveryStatus {
    PENDING, COMPLETED
}

class Recovery {
    int machineId;
    int transactionId;
    String state;
    RecoveryStatus status;

    Recovery(int machineId, int txnId, String state) {
        this.machineId = machineId;
        this.transactionId = txnId;
        this.state = state;
        this.status = RecoveryStatus.PENDING;
    }
}
