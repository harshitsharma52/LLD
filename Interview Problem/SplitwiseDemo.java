import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.*;

enum SplitType {
    EQUAL,
    EXACT,
    PERCENT
}

// ==========================================================
// USER
// ==========================================================
class User {

    private final String userId;
    private final String name;

    User(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
}

// ==========================================================
// GROUP -- a named convenience wrapper over a set of users.
// Expense math itself never needs Group -- it works off a plain
// list of participant IDs. Group exists purely for organizing
// users under a shared label (e.g. "Goa Trip").
// ==========================================================
class Group {

    private final String groupId;
    private final String name;
    private final List<User> members = new ArrayList<>();

    Group(String groupId, String name) {
        this.groupId = groupId;
        this.name = name;
    }

    public void addMember(User user) { members.add(user); }
    public List<User> getMembers() { return members; }
}

// ==========================================================
// EXPENSE
// ==========================================================
class Expense {

    private final String expenseId;
    private final String description;
    private final double amount;
    private final User paidBy;
    private final Map<String, Double> splits; // userId -> amount owed by that user

    Expense(String expenseId, String description, double amount, User paidBy, Map<String, Double> splits) {
        this.expenseId = expenseId;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.splits = splits;
    }

    public String getDescription() { return description; }
    public User getPaidBy() { return paidBy; }
    public double getAmount() { return amount; }
    public Map<String, Double> getSplits() { return splits; }
}

// ==========================================================
// STRATEGY: SplitStrategy
// Each strategy answers ONE question: "given a total amount and
// some inputs, how much does each participant owe?" -- nothing
// about WHO paid or how balances get updated lives here.
// ==========================================================
interface SplitStrategy {
    Map<String, Double> calculateSplit(double totalAmount, List<String> participantIds, Map<String, Double> inputValues);
}

class EqualSplitStrategy implements SplitStrategy {

    @Override
    public Map<String, Double> calculateSplit(double totalAmount, List<String> participantIds, Map<String, Double> inputValues) {
        Map<String, Double> result = new HashMap<>();
        double share = round2(totalAmount / participantIds.size());
        for (String id : participantIds) {
            result.put(id, share);
        }
        return result;
    }

    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}

class ExactSplitStrategy implements SplitStrategy {

    @Override
    public Map<String, Double> calculateSplit(double totalAmount, List<String> participantIds, Map<String, Double> inputValues) {
        double sum = inputValues.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sum - totalAmount) > 0.01) {
            throw new IllegalArgumentException("Exact split amounts (" + sum + ") must sum to total (" + totalAmount + ")");
        }
        return new HashMap<>(inputValues);
    }
}

class PercentSplitStrategy implements SplitStrategy {

    @Override
    public Map<String, Double> calculateSplit(double totalAmount, List<String> participantIds, Map<String, Double> inputValues) {
        double sumPercent = inputValues.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sumPercent - 100.0) > 0.01) {
            throw new IllegalArgumentException("Percentages (" + sumPercent + ") must sum to 100");
        }
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, Double> entry : inputValues.entrySet()) {
            result.put(entry.getKey(), round2(totalAmount * entry.getValue() / 100.0));
        }
        return result;
    }

    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}

// Simple Factory -- same idiom as SpotTypeResolver / TransactionFactory,
// not GoF Factory Method. Worth naming precisely if asked.
class SplitStrategyFactory {

    static SplitStrategy getStrategy(SplitType type) {
        switch (type) {
            case EQUAL:   return new EqualSplitStrategy();
            case EXACT:   return new ExactSplitStrategy();
            case PERCENT: return new PercentSplitStrategy();
            default: throw new IllegalArgumentException("Unknown split type: " + type);
        }
    }
}

// ==========================================================
// OBSERVER: ExpenseObserver
// Decouples "an expense happened" from "what should react to it" --
// a notification service is the obvious example, but this could
// just as easily be an email service, a push-notification service,
// or an analytics logger, all without ExpenseManager knowing about any of them.
// ==========================================================
interface ExpenseObserver {
    void onExpenseAdded(Expense expense);
}

class NotificationService implements ExpenseObserver {

    @Override
    public void onExpenseAdded(Expense expense) {
        System.out.println("[notify] '" + expense.getDescription() + "' added by "
                + expense.getPaidBy().getName() + " for " + expense.getAmount());
    }
}

// ==========================================================
// EXPENSE MANAGER (the core service -- deliberately NOT a Singleton,
// see walkthrough notes for why)
//
// BALANCE REPRESENTATION:
// balances[A][B] = net amount A owes B. Always maintained so that
// balances[A][B] == -balances[B][A]. Positive means A owes B;
// negative means B owes A. This "netted" representation means we
// never store two conflicting entries for the same pair.
//
// CONCURRENCY DESIGN:
// One expense can touch MANY participants' balances at once (a
// group dinner split five ways touches five pairs). That's a
// multi-step, multi-entry update -- NOT a simple counter and NOT a
// single collection operation -- so per the concurrency decision
// framework, this calls for a real Lock around the whole update,
// not just an Atomic* or a concurrent collection alone.
// ==========================================================
class ExpenseManager {

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Double>> balances = new HashMap<>();
    private final Lock ledgerLock = new ReentrantLock();
    private final List<ExpenseObserver> observers = new CopyOnWriteArrayList<>();
    private final AtomicLong expenseCounter = new AtomicLong(0);

    public void registerUser(User user) {
        users.put(user.getUserId(), user);
        ledgerLock.lock();
        try {
            balances.put(user.getUserId(), new HashMap<>());
        } finally {
            ledgerLock.unlock();
        }
    }

    public void addObserver(ExpenseObserver observer) {
        observers.add(observer);
    }

    // Convenience overload that actually uses Group -- pulls participant IDs
    // from the group's member list instead of making the caller list them out
    // by hand every time. This is ALL Group is for: it's a shortcut for
    // "split this among everyone already in this group," nothing more.
    // The underlying math still only ever sees a List<String> of IDs --
    // Group never touches balances directly.
    public Expense addGroupExpense(String description, double amount, User paidBy,
                                    Group group, SplitType type, Map<String, Double> inputValues) {
        List<String> participantIds = new ArrayList<>();
        for (User member : group.getMembers()) {
            participantIds.add(member.getUserId());
        }
        return addExpense(description, amount, paidBy, participantIds, type, inputValues);
    }

    public Expense addExpense(String description, double amount, User paidBy,
                               List<String> participantIds, SplitType type, Map<String, Double> inputValues) {

        SplitStrategy strategy = SplitStrategyFactory.getStrategy(type);
        Map<String, Double> splits = strategy.calculateSplit(amount, participantIds, inputValues);

        String expenseId = "E" + expenseCounter.incrementAndGet();
        Expense expense = new Expense(expenseId, description, amount, paidBy, splits);

        // All balance entries for this ONE expense must update together --
        // a reader must never see "half" of an expense reflected in balances.
        ledgerLock.lock();
        try {
            for (Map.Entry<String, Double> entry : splits.entrySet()) {
                String participantId = entry.getKey();
                double share = entry.getValue();
                if (!participantId.equals(paidBy.getUserId())) {
                    adjustBalance(participantId, paidBy.getUserId(), share);
                }
            }
        } finally {
            ledgerLock.unlock();
        }

        for (ExpenseObserver observer : observers) {
            observer.onExpenseAdded(expense);
        }

        return expense;
    }

    public void settlePayment(String payerId, String payeeId, double amount) {
        ledgerLock.lock();
        try {
            adjustBalance(payerId, payeeId, -amount); // payer's debt to payee shrinks
        } finally {
            ledgerLock.unlock();
        }
        System.out.println(users.get(payerId).getName() + " paid " + users.get(payeeId).getName() + " " + amount);
    }

    // Maintains the invariant balances[A][B] == -balances[B][A]
    private void adjustBalance(String userA, String userB, double amount) {
        balances.get(userA).merge(userB, amount, Double::sum);
        balances.get(userB).merge(userA, -amount, Double::sum);
    }

    public double getBalance(String userA, String userB) {
        ledgerLock.lock();
        try {
            return round2(balances.get(userA).getOrDefault(userB, 0.0));
        } finally {
            ledgerLock.unlock();
        }
    }

    public void showAllBalances() {
        ledgerLock.lock();
        try {
            boolean anyDebt = false;
            for (String userA : balances.keySet()) {
                for (Map.Entry<String, Double> entry : balances.get(userA).entrySet()) {
                    if (entry.getValue() > 0.01) {
                        anyDebt = true;
                        System.out.println(users.get(userA).getName() + " owes "
                                + users.get(entry.getKey()).getName() + " : " + round2(entry.getValue()));
                    }
                }
            }
            if (!anyDebt) System.out.println("All settled up.");
        } finally {
            ledgerLock.unlock();
        }
    }

    // ==========================================================
    // ADVANCED EXTENSION: Debt simplification
    // Reduces N pairwise debts down to a smaller set of transactions
    // using a greedy "largest debtor pays largest creditor" approach.
    // This does NOT mutate balances -- it only SUGGESTS transactions;
    // actually applying them would mean calling settlePayment() for each.
    //
    // Note of honesty for the interview: this greedy approach is a
    // well-known, efficient heuristic, but it is not proven to always
    // produce the mathematically minimum number of transactions in
    // every case -- the true minimum-transaction problem is NP-hard.
    // ==========================================================
    public List<String> simplifyDebts() {
        ledgerLock.lock();
        try {
            Map<String, Double> net = new HashMap<>();
            for (String u : balances.keySet()) {
                double total = 0;
                for (double v : balances.get(u).values()) total += v;
                net.put(u, round2(total));
            }

            PriorityQueue<Map.Entry<String, Double>> debtors =
                    new PriorityQueue<>((a, b) -> Double.compare(b.getValue(), a.getValue()));
            PriorityQueue<Map.Entry<String, Double>> creditors =
                    new PriorityQueue<>((a, b) -> Double.compare(b.getValue(), a.getValue()));

            for (Map.Entry<String, Double> e : net.entrySet()) {
                if (e.getValue() > 0.01) {
                    debtors.add(new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue()));
                } else if (e.getValue() < -0.01) {
                    creditors.add(new AbstractMap.SimpleEntry<>(e.getKey(), -e.getValue()));
                }
            }

            List<String> transactions = new ArrayList<>();

            while (!debtors.isEmpty() && !creditors.isEmpty()) {
                Map.Entry<String, Double> debtor = debtors.poll();
                Map.Entry<String, Double> creditor = creditors.poll();

                double settleAmount = round2(Math.min(debtor.getValue(), creditor.getValue()));
                transactions.add(users.get(debtor.getKey()).getName() + " pays "
                        + users.get(creditor.getKey()).getName() + " : " + settleAmount);

                double remainingDebtor = round2(debtor.getValue() - settleAmount);
                double remainingCreditor = round2(creditor.getValue() - settleAmount);

                if (remainingDebtor > 0.01) debtors.add(new AbstractMap.SimpleEntry<>(debtor.getKey(), remainingDebtor));
                if (remainingCreditor > 0.01) creditors.add(new AbstractMap.SimpleEntry<>(creditor.getKey(), remainingCreditor));
            }

            return transactions;
        } finally {
            ledgerLock.unlock();
        }
    }

    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}

// ==========================================================
// DEMO
// ==========================================================
public class SplitwiseDemo {

    public static void main(String[] args) {

        ExpenseManager manager = new ExpenseManager();

        User alice = new User("U1", "Alice");
        User bob = new User("U2", "Bob");
        User charlie = new User("U3", "Charlie");

        manager.registerUser(alice);
        manager.registerUser(bob);
        manager.registerUser(charlie);
        manager.addObserver(new NotificationService());

        Group goaTrip = new Group("G1", "Goa Trip");
        goaTrip.addMember(alice);
        goaTrip.addMember(bob);
        goaTrip.addMember(charlie);

        System.out.println("---- EQUAL split via Group: Alice pays 300 for the whole Goa Trip group ----");
        manager.addGroupExpense("Dinner", 300, alice, goaTrip, SplitType.EQUAL, null);

        System.out.println("\n---- EXACT split: Bob pays 100 ----");
        Map<String, Double> exact = Map.of("U1", 40.0, "U2", 30.0, "U3", 30.0);
        manager.addExpense("Groceries", 100, bob, List.of("U1", "U2", "U3"), SplitType.EXACT, exact);

        System.out.println("\n---- PERCENT split: Charlie pays 200 ----");
        Map<String, Double> percent = Map.of("U1", 50.0, "U2", 25.0, "U3", 25.0);
        manager.addExpense("Movie night", 200, charlie, List.of("U1", "U2", "U3"), SplitType.PERCENT, percent);

        System.out.println("\n---- Current balances ----");
        manager.showAllBalances();

        System.out.println("\n---- Bob settles 50 with Alice ----");
        manager.settlePayment("U2", "U1", 50);

        System.out.println("\n---- Balances after settlement ----");
        manager.showAllBalances();

        System.out.println("\n---- Simplified debts (minimum transaction suggestion) ----");
        for (String transaction : manager.simplifyDebts()) {
            System.out.println(transaction);
        }
    }
}