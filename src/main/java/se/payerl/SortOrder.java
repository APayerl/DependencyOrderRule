package se.payerl;

import jakarta.inject.Named;

@Named("SortOrder")
public class SortOrder {
    String first;
    String then;

    public SortOrder() { }
    public SortOrder(String first, String then) {
        this.first = first;
        this.then = then;
    }

    public String getFirst() {
        return first;
    }

    public String getThen() {
        return then;
    }

    public SortOrder setFirst(String first) {
        this.first = first;
        return this;
    }

    public SortOrder setThen(String then) {
        this.then = then;
        return this;
    }

    @Override
    public String toString() {
        return "SortOrder{first=" + first + ", then=" + then + "}";
    }
}
