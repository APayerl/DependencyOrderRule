package se.payerl;

import org.apache.maven.model.Dependency;

import java.util.ArrayList;
import java.util.List;

public abstract class SortOrder {
    public List<? extends SortOrder> separates = new ArrayList<>();
    abstract public String toString();
    protected abstract String getFilter(Dependency dep);
    protected abstract String depToStr(Dependency dep);
    protected abstract String getJob();
    protected abstract void compareTo(Dependency prevDep, Dependency currentDep, List<String> errors);
    protected abstract boolean isDependencyApplicable(Dependency dep);
}
