package se.payerl;

import org.apache.maven.model.Dependency;

import java.util.List;

public abstract class SortOrder<Order> {
    abstract public String toString();
    abstract String getFilter(Dependency dep);
    abstract String depToStr(Dependency dep);
    abstract String getJob();
    abstract void compareTo(Dependency prevDep, Dependency currentDep, List<String> errors);
    abstract boolean isDependencyApplicable(Dependency dep);
}
