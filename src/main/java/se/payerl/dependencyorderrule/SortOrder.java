package se.payerl.dependencyorderrule;

import jakarta.inject.Named;

@Named("SortOrder")
public class SortOrder {

    Scope first;
    Scope second;

    public SortOrder(Scope first, Scope second) {
        this.first = first;
        this.second = second;
    }

    public Scope getFirst() {
        return first;
    }

    public Scope getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "SortOrder{first=" + first + ", second=" + second + "}";
    }
}
