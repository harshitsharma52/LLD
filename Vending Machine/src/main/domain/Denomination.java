package domain;

public enum Denomination 
{
    ONE(100), FIVE(500), TEN(1000);

    int value;
    Denomination(int value) {
        this.value = value;
    }
}
