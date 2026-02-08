package domain;

enum TransactionStatus {
    PENDING, COMPLETED, CANCELLED
}

public class Transaction {
    int id;
    Product product;
    double insertedAmount;
    TransactionStatus status;

    Transaction(int id, Product product) {
        this.id = id;
        this.product = product;
        this.insertedAmount = 0;
        this.status = TransactionStatus.PENDING;
    }
}
