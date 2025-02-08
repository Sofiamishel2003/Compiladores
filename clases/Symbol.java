package clases;

public class Symbol {
    private final String value;
    private final boolean isOperator;

    public Symbol(String value, boolean isOperator) {
        this.value = value;
        this.isOperator = isOperator;
    }

    public String getValue() {
        return value;
    }

    public boolean isOperator() {
        return isOperator;
    }

    @Override
    public String toString() {
        return value;
    }
}
