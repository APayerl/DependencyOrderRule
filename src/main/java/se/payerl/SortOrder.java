package se.payerl;

import jakarta.inject.Named;

@Named("SortOrder")
public class SortOrder {

    Scope first;
    Scope then;

    public SortOrder() { }
    public SortOrder(Scope first, Scope then) {
        this.first = first;
        this.then = then;
    }

    public Scope getFirst() {
        return first;
    }

    public Scope getThen() {
        return then;
    }

    public SortOrder setFirst(Scope first) {
        this.first = first;
        return this;
    }

    public SortOrder setThen(Scope then) {
        this.then = then;
        return this;
    }

    @Override
    public String toString() {
        return "SortOrder{first=" + first + ", then=" + then + "}";
    }
}
