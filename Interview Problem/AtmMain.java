import java.util.*;
import java.util.concurrent.locks.*;

enum TransactionType {
    WITHDRAW,
    DEPOSIT,
    BALANCE_CHECK
}

// ==========================================================
// BANK ACCOUNT
// Locking now lives INSIDE the account, not at the call site.
// This means thread-safety is guaranteed no matter who calls
// withdraw()/deposit() -- it doesn't depend on every caller
// remembering to acquire a lock first.
// ==========================================================
class BankAccount {

    private final String accountNumber;
    private double balance;
    private final Lock lock = new ReentrantLock();

    BankAccount(String accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }

    public boolean withdraw(double amount) {
        lock.lock();
        try {
            if (amount <= 0) {
                System.out.println("Invalid withdrawal amount");
                return false;
            }
            if (balance < amount) {
                System.out.println("Insufficient balance");
                return false;
            }
            balance -= amount;
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void deposit(double amount) {
        lock.lock();
        try {
            if (amount <= 0) {
                System.out.println("Invalid deposit amount");
                return;
            }
            balance += amount;
        } finally {
            lock.unlock();
        }
    }

    public double getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }
}

// ==========================================================
// CARD
// ==========================================================
class Card {

    private final String cardNumber;
    private final int pin;
    private final BankAccount account;

    Card(String cardNumber, int pin, BankAccount account) {
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.account = account;
    }

    public boolean validatePin(int enteredPin) {
        return pin == enteredPin;
    }

    public BankAccount getAccount() { return account; }
}

// ==========================================================
// USER
// ==========================================================
class User {

    private final String userId;
    private final String name;
    private final Card card;

    User(String userId, String name, Card card) {
        this.userId = userId;
        this.name = name;
        this.card = card;
    }

    public Card getCard() { return card; }
}

// ==========================================================
// STRATEGY: Transaction
// Each transaction type is a stateless, reusable strategy --
// execute() reads only its parameters, never instance fields,
// so a single shared instance per type is safe to reuse.
// ==========================================================
interface Transaction {
    void execute(BankAccount account, double amount);
}

class WithdrawTransaction implements Transaction {
    @Override
    public void execute(BankAccount account, double amount) {
        if (account.withdraw(amount)) {
            System.out.println("Withdraw success: " + amount);
        }
    }
}

class DepositTransaction implements Transaction {
    @Override
    public void execute(BankAccount account, double amount) {
        account.deposit(amount);
        System.out.println("Deposit success: " + amount);
    }
}

class BalanceCheckTransaction implements Transaction {
    @Override
    public void execute(BankAccount account, double amount) {
        System.out.println("Balance: " + account.getBalance());
    }
}

// ==========================================================
// SIMPLE FACTORY (not GoF Factory Method -- see notes.md)
// Map-based registry avoids a switch/if-else on type.
// ==========================================================
class TransactionFactory {

    private static final Map<TransactionType, Transaction> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put(TransactionType.WITHDRAW, new WithdrawTransaction());
        REGISTRY.put(TransactionType.DEPOSIT, new DepositTransaction());
        REGISTRY.put(TransactionType.BALANCE_CHECK, new BalanceCheckTransaction());
    }

    public static Transaction getTransaction(TransactionType type) {
        Transaction t = REGISTRY.get(type);
        if (t == null) {
            throw new IllegalArgumentException("Unknown transaction type: " + type);
        }
        return t;
    }
}

// ==========================================================
// STATE PATTERN: ATMState
// 3 states: Idle, HasCard, Authenticated.
// States are singleton instances held by ATM (like VendingMachine),
// instead of being "new'd up" on every transition -- cheaper and
// makes each state's identity stable for comparisons/debugging.
// ==========================================================
interface ATMState {
    void insertCard(ATM atm, Card card);
    void authenticatePin(ATM atm, int pin);
    void selectTransaction(ATM atm, TransactionType type, double amount);
}

class IdleState implements ATMState {

    @Override
    public void insertCard(ATM atm, Card card) {
        atm.setCurrentCard(card);
        atm.resetPinAttempts();
        atm.setState(atm.getHasCardState());
        System.out.println("Card inserted");
    }

    @Override
    public void authenticatePin(ATM atm, int pin) {
        System.out.println("Insert card first");
    }

    @Override
    public void selectTransaction(ATM atm, TransactionType type, double amount) {
        System.out.println("Insert card first");
    }
}

class HasCardState implements ATMState {

    private static final int MAX_PIN_ATTEMPTS = 3;

    @Override
    public void insertCard(ATM atm, Card card) {
        System.out.println("Card already inserted");
    }

    @Override
    public void authenticatePin(ATM atm, int pin) {

        if (atm.getCurrentCard().validatePin(pin)) {
            atm.setState(atm.getAuthenticatedState());
            System.out.println("PIN verified");
            return;
        }

        atm.incrementPinAttempts();
        System.out.println("Invalid PIN");

        if (atm.getPinAttempts() >= MAX_PIN_ATTEMPTS) {
            System.out.println("Too many failed attempts. Ejecting card.");
            atm.setCurrentCard(null);
            atm.setState(atm.getIdleState());
        }
    }

    @Override
    public void selectTransaction(ATM atm, TransactionType type, double amount) {
        System.out.println("Authenticate first");
    }
}

class AuthenticatedState implements ATMState {

    @Override
    public void insertCard(ATM atm, Card card) {
        System.out.println("Card already inserted");
    }

    @Override
    public void authenticatePin(ATM atm, int pin) {
        System.out.println("Already authenticated");
    }

    @Override
    public void selectTransaction(ATM atm, TransactionType type, double amount) {

        BankAccount account = atm.getCurrentCard().getAccount();
        Transaction transaction = TransactionFactory.getTransaction(type);
        transaction.execute(account, amount);

        atm.setCurrentCard(null);
        atm.setState(atm.getIdleState());
        System.out.println("Card ejected");
    }
}

// ==========================================================
// ATM (Context)
// Deliberately NOT a Singleton -- unlike Logger or VendingMachine,
// a real-world system has MANY physical ATMs. Forcing this to a
// Singleton would be a modeling mistake, not a simplification.
// ==========================================================
class ATM {

    private final String atmId;

    private final ATMState idleState = new IdleState();
    private final ATMState hasCardState = new HasCardState();
    private final ATMState authenticatedState = new AuthenticatedState();

    private ATMState currentState;
    private Card currentCard;
    private int pinAttempts = 0;

    ATM(String atmId) {
        this.atmId = atmId;
        this.currentState = idleState;
    }

    public void insertCard(Card card) { currentState.insertCard(this, card); }
    public void authenticatePin(int pin) { currentState.authenticatePin(this, pin); }
    public void selectTransaction(TransactionType type, double amount) {
        currentState.selectTransaction(this, type, amount);
    }

    // package-private helpers used only by state classes
    void setState(ATMState state) { currentState = state; }
    void setCurrentCard(Card card) { currentCard = card; }
    Card getCurrentCard() { return currentCard; }
    void incrementPinAttempts() { pinAttempts++; }
    void resetPinAttempts() { pinAttempts = 0; }
    int getPinAttempts() { return pinAttempts; }

    ATMState getIdleState() { return idleState; }
    ATMState getHasCardState() { return hasCardState; }
    ATMState getAuthenticatedState() { return authenticatedState; }
}

// ==========================================================
// DEMO
// ==========================================================
class AtmMain {

    public static void main(String[] args) {

        BankAccount account = new BankAccount("ACC1", 10000);
        Card card = new Card("CARD1", 1234, account);
        User harsh = new User("U1", "Harsh", card);

        ATM atm = new ATM("ATM1");

        System.out.println("---- Successful withdrawal ----");
        atm.insertCard(harsh.getCard());
        atm.authenticatePin(1234);
        atm.selectTransaction(TransactionType.WITHDRAW, 2000);

        System.out.println("\n---- Wrong PIN lockout after 3 attempts ----");
        atm.insertCard(harsh.getCard());
        atm.authenticatePin(9999);
        atm.authenticatePin(1111);
        atm.authenticatePin(2222); // 3rd failure -> card auto-ejected, back to Idle

        System.out.println("\n---- Selecting transaction without a card ----");
        atm.selectTransaction(TransactionType.BALANCE_CHECK, 0);

        System.out.println("\n---- Deposit then check balance ----");
        atm.insertCard(harsh.getCard());
        atm.authenticatePin(1234);
        atm.selectTransaction(TransactionType.DEPOSIT, 500);

        atm.insertCard(harsh.getCard());
        atm.authenticatePin(1234);
        atm.selectTransaction(TransactionType.BALANCE_CHECK, 0);

        System.out.println("\n---- Withdrawing more than balance ----");
        atm.insertCard(harsh.getCard());
        atm.authenticatePin(1234);
        atm.selectTransaction(TransactionType.WITHDRAW, 999999);
    }
}