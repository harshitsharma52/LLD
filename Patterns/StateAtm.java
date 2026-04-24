interface AtmState {
    void insertCard(ATMMachine atm);
    void ejectCard(ATMMachine atm);
    void enterPin(ATMMachine atm, int pin);
    void requestCash(ATMMachine atm, int amount);
}

// state 1 → NoCardState
class NoCardState implements AtmState {

    public void insertCard(ATMMachine atm) {
        System.out.println("Card inserted");
        atm.setState(new HasCardState()); // ✅ fixed
    }

    public void ejectCard(ATMMachine atm) {
        System.out.println("No card to eject");
    }

    public void enterPin(ATMMachine atm, int pin) {
        System.out.println("Insert card first");
    }

    public void requestCash(ATMMachine atm, int amount) {
        System.out.println("Insert card first");
    }
}

// state 2 → HasCardState
class HasCardState implements AtmState {

    public void insertCard(ATMMachine atm) {
        System.out.println("Card already inserted");
    }

    public void ejectCard(ATMMachine atm) {
        System.out.println("Card ejected");
        atm.setState(new NoCardState()); // ✅ fixed
    }

    public void enterPin(ATMMachine atm, int pin) {
        if (pin == 1234) {
            System.out.println("Correct PIN");
            atm.setState(new HasPinState()); // ✅ fixed
        } else {
            System.out.println("Wrong PIN");
        }
    }

    public void requestCash(ATMMachine atm, int amount) {
        System.out.println("Enter PIN first");
    }
}

// state 3 → HasPinState
class HasPinState implements AtmState {

    public void insertCard(ATMMachine atm) {
        System.out.println("Card already inserted");
    }

    public void ejectCard(ATMMachine atm) {
        System.out.println("Card ejected");
        atm.setState(new NoCardState()); // ✅ fixed
    }

    public void enterPin(ATMMachine atm, int pin) {
        System.out.println("PIN already entered");
    }

    public void requestCash(ATMMachine atm, int amount) {
        if (atm.cash >= amount) {
            System.out.println("Cash withdrawn: " + amount);
            atm.cash -= amount;
            atm.setState(new NoCardState()); // ✅ fixed
        } else {
            System.out.println("Insufficient balance");
            atm.setState(new NoCardState());
        }
    }
}

// ATM Machine
class ATMMachine {
    AtmState currentState;
    int cash = 1000;

    public ATMMachine() {
        currentState = new NoCardState();
    }

    public void setState(AtmState state) {
        currentState = state;
    }

    public void insertCard() {
        currentState.insertCard(this);
    }

    public void ejectCard() {
        currentState.ejectCard(this);
    }

    public void enterPin(int pin) {
        currentState.enterPin(this, pin);
    }

    public void requestCash(int amount) {
        currentState.requestCash(this, amount);
    }
    public void balance() {
        System.out.println("Current balance: " + cash);
    }
}

// Main
class StateAtm {
    public static void main(String[] args) {

        ATMMachine atm = new ATMMachine();

        atm.requestCash(100);   // ❌
        atm.insertCard();       // ✅
        atm.requestCash(100);   // ❌
        atm.enterPin(1234);     // ✅
        atm.requestCash(200);   // ✅
        atm.balance();
    }
}