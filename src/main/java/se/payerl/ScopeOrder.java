package se.payerl;

import org.apache.maven.model.Dependency;

import java.util.List;

public class ScopeOrder extends SortOrder<ScopeOrder> {
    String first;
    String then;

    public ScopeOrder() { }
    public ScopeOrder(String first, String then) {
        this.first = first;
        this.then = then;
    }

    @Override
    public String toString() {
        return "ScopeOrder{first=" + first + ", then=" + then + "}";
    }

    @Override
    public String getFilter(Dependency dep) {
        return dep.getScope();
    }

    @Override
    public String depToStr(Dependency dep) {
        return dep.getGroupId() + ":" + dep.getArtifactId() + " scope:" + dep.getScope();
    }

    @Override
    void compareTo(Dependency prevDep, Dependency currentDep, List<String> errors) {
        if(this.getFilter(prevDep).equalsIgnoreCase(then) &&
                this.getFilter(currentDep).equalsIgnoreCase(first)) {
            errors.add("Dependency " + this.depToStr(currentDep) + " must be before " + this.depToStr(prevDep));
        }
    }

    @Override
    String getJob() {
        return "Checking for " + first + " before " + then;
    }

    @Override
    boolean isDependencyApplicable(Dependency dep) {
        return this.getFilter(dep).equalsIgnoreCase(first) ||
                this.getFilter(dep).equalsIgnoreCase(then);
    }
}
