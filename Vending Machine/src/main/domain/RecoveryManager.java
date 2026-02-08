package domain;

class RecoveryManager {
    private Recovery recovery;

    void save(Recovery r) {
        recovery = r;
        System.out.println("[RECOVERY SAVED] State = " + r.state);
    }

    Recovery load() {
        return recovery;
    }

    void markCompleted() {
        if (recovery != null) {
            recovery.status = RecoveryStatus.COMPLETED;
        }
    }
}
