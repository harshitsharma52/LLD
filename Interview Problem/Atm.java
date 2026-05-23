import java.util.*;
import java.util.concurrent.locks.*;

enum TransactionType {
    WITHDRAW,
    DEPOSIT,
    BALANCE_CHECK
}

class BankAccount {

    String accountNumber;

    double balance;

    // Thread safety

    Lock lock = new ReentrantLock();

    BankAccount(
            String accountNumber,
            double balance) {

        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public void withdraw(
            double amount) {

        balance -= amount;
    }

    public void deposit(
            double amount) {

        balance += amount;
    }
}

class Card {

    String cardNumber;

    int pin;

    BankAccount account;

    Card(
            String cardNumber,
            int pin,
            BankAccount account) {

        this.cardNumber = cardNumber;
        this.pin = pin;
        this.account = account;
    }

    public boolean validatePin(
            int enteredPin) {

        return pin == enteredPin;
    }
}

class User {

    String userId;

    String name;

    Card card;

    User(
            String userId,
            String name,
            Card card) {

        this.userId = userId;
        this.name = name;
        this.card = card;
    }
}

// Strategy pattern for transaction execution
interface Transaction {

    void execute(
            BankAccount account,
            double amount);
}

////////////////////////////////////////////////////////
// WITHDRAW
////////////////////////////////////////////////////////

class WithdrawTransaction
        implements Transaction {

    @Override
    public void execute(
            BankAccount account,
            double amount) {

        if (account.balance < amount) {

            System.out.println(
                    "Insufficient Balance");

            return;
        }

        account.withdraw(amount);

        System.out.println(
                "Withdraw success : "
                        + amount);
    }
}

////////////////////////////////////////////////////////
// DEPOSIT
////////////////////////////////////////////////////////

class DepositTransaction
        implements Transaction {

    @Override
    public void execute(
            BankAccount account,
            double amount) {

        account.deposit(amount);

        System.out.println(
                "Deposit success : "
                        + amount);
    }
}

////////////////////////////////////////////////////////
// BALANCE CHECK
////////////////////////////////////////////////////////

class BalanceCheckTransaction
        implements Transaction {

    @Override
    public void execute(
            BankAccount account,
            double amount) {

        System.out.println(
                "Balance : "
                        + account.balance);
    }
}

////////////////////////////////////////////////////////
// FACTORY give us the right transaction object based on type
////////////////////////////////////////////////////////

class TransactionFactory {

    private final Map<TransactionType, Transaction> map = new HashMap<>();

    TransactionFactory() {

        map.put(
                TransactionType.WITHDRAW,
                new WithdrawTransaction());

        map.put(
                TransactionType.DEPOSIT,
                new DepositTransaction());

        map.put(
                TransactionType.BALANCE_CHECK,
                new BalanceCheckTransaction());
    }

    public Transaction getTransaction(
            TransactionType type) {

        return map.get(type);
    }
}

// State pattern for ATM states
// 3 states : Idle, HasCard, Authenticated
interface ATMState {

    void insertCard(ATM atm, Card card);

    void authenticatePin(
            ATM atm,
            int pin);

    void selectTransaction(
            ATM atm,
            TransactionType type,
            double amount);
}

class IdleState
        implements ATMState {

    @Override
    public void insertCard(
            ATM atm,
            Card card) {

        atm.currentCard = card;

        atm.setState(
                new HasCardState());

        System.out.println(
                "Card inserted");
    }

    @Override
    public void authenticatePin(
            ATM atm,
            int pin) {

        System.out.println(
                "Insert card first");
    }

    @Override
    public void selectTransaction(
            ATM atm,
            TransactionType type,
            double amount) {

        System.out.println(
                "Insert card first");
    }
}

/////////////////////////////////////////////////////////
// HAS CARD STATE
/////////////////////////////////////////////////////////

class HasCardState
        implements ATMState {

    @Override
    public void insertCard(
            ATM atm,
            Card card) {

        System.out.println(
                "Card already inserted");
    }

    @Override
    public void authenticatePin(
            ATM atm,
            int pin) {

        if (atm.currentCard
                .validatePin(pin)) {

            atm.setState(
                    new AuthenticatedState());

            System.out.println(
                    "PIN verified");
        }

        else {

            System.out.println(
                    "Invalid PIN");
        }
    }

    @Override
    public void selectTransaction(
            ATM atm,
            TransactionType type,
            double amount) {

        System.out.println(
                "Authenticate first");
    }
}
// Authenticated state : user can select transaction, after transaction card is ejected and state goes to idle
class AuthenticatedState
        implements ATMState {

    TransactionFactory factory = new TransactionFactory();

    @Override
    public void insertCard(
            ATM atm,
            Card card) {

        System.out.println(
                "Card already inserted");
    }

    @Override
    public void authenticatePin(
            ATM atm,
            int pin) {

        System.out.println(
                "Already authenticated");
    }

    @Override
    public void selectTransaction(
            ATM atm,
            TransactionType type,
            double amount) {

        BankAccount account = atm.currentCard.account;

        /////////////////////////////////////////////////
        // THREAD SAFETY
        /////////////////////////////////////////////////

        account.lock.lock();

        try {

            Transaction transaction = factory.getTransaction(type);

            transaction.execute(
                    account,
                    amount);
        }

        finally {

            account.lock.unlock();
        }

        /////////////////////////////////////////////////
        // EJECT CARD
        /////////////////////////////////////////////////

        atm.currentCard = null;

        atm.setState(
                new IdleState());

        System.out.println(
                "Card ejected");
    }
}

class ATM {

    String atmId;

    ATMState currentState;

    Card currentCard;

    ATM(String atmId) {

        this.atmId = atmId;

        currentState = new IdleState();
    }

    public void setState(
            ATMState state) {

        currentState = state;
    }

    public void insertCard(
            Card card) {

        currentState.insertCard(
                this,
                card);
    }

    public void authenticatePin(
            int pin) {

        currentState.authenticatePin(
                this,
                pin);
    }

    public void selectTransaction(
            TransactionType type,
            double amount) {

        currentState.selectTransaction(
                this,
                type,
                amount);
    }
}

class AtmMain {
    public static void main(
            String[] args) {

        /////////////////////////////////////////////////
        // ACCOUNT
        /////////////////////////////////////////////////

        BankAccount account = new BankAccount(
                "ACC1",
                10000);

        /////////////////////////////////////////////////
        // CARD
        /////////////////////////////////////////////////

        Card card = new Card(
                "CARD1",
                1234,
                account);

        /////////////////////////////////////////////////
        // USER
        /////////////////////////////////////////////////

        User harsh = new User(
                "U1",
                "Harsh",
                card);

        /////////////////////////////////////////////////
        // ATM
        /////////////////////////////////////////////////

        ATM atm = new ATM("ATM1");

        /////////////////////////////////////////////////
        // FLOW
        /////////////////////////////////////////////////

        atm.insertCard(card);

        atm.authenticatePin(1234);

        atm.selectTransaction(
                TransactionType.WITHDRAW,
                2000);
    }
}
