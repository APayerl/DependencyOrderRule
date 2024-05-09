package se.payerl;

import org.apache.maven.model.Dependency;

import java.util.List;

public abstract class SortOrder<Order> {
    abstract String getFirst();
    abstract String getThen();
    abstract Order setFirst(String first);
    abstract Order setThen(String then);
    abstract public String toString();
    abstract String getFilter(Dependency dep);
    abstract String depToStr(Dependency dep);
    void compareTo(Dependency prevDep, Dependency currentDep, List<String> errors) {
        if(this.getFilter(prevDep).equalsIgnoreCase(this.getThen()) &&
                this.getFilter(currentDep).equalsIgnoreCase(this.getFirst())) {
            errors.add("Dependency " + this.depToStr(currentDep) + " must be before " + this.depToStr(prevDep));
        }
    }
}
