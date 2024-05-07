package se.payerl.dependencyorderrule;

public record SortOrder(Scope first, Scope second) {
    @Override
    public String toString() {
        return "SortOrder{first=" + first + ", second=" + second + "}";
    }
}
