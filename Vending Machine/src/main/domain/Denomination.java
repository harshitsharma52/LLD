package domain;

public enum Denomination {
    ONE(1), FIVE(5), TEN(10), TWENTY(20), FIFTY(50), HUNDRED(100);

    public int value; // rupees

    Denomination(int value) {
        this.value = value;
    }
}

