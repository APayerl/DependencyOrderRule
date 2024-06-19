package se.payerl.orders;

import org.apache.maven.model.Dependency;
import se.payerl.SortOrder;

import java.util.List;
import java.util.Objects;

public class OptionalOrder extends SortOrder {
    String first;
    String then;

    public OptionalOrder() { }

    private String getOptional(Dependency dep) {
        return Boolean.toString(Objects.equals(dep.getOptional(), "true"));
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{first=" + first + ", then=" + then + "}";
    }

    @Override
    public String getFilter(Dependency dep) {
        return getOptional(dep);
    }

    @Override
    public String depToStr(Dependency dep) {
        return dep.getGroupId() + ":" + dep.getArtifactId() + " optional:" + getOptional(dep);
    }

    @Override
    protected void compareTo(Dependency prevDep, Dependency currentDep, List<String> errors) {
        if(this.getFilter(prevDep).equalsIgnoreCase(then) &&
                this.getFilter(currentDep).equalsIgnoreCase(first)) {
            errors.add("Dependency " + this.depToStr(currentDep) + " must be before " + this.depToStr(prevDep));
        }
    }

    @Override
    protected String getJob() {
        return "Checking for " + first + " before " + then;
    }

    @Override
    protected boolean isDependencyApplicable(Dependency dep) {
        return this.getFilter(dep).equalsIgnoreCase(first) ||
                this.getFilter(dep).equalsIgnoreCase(then);
    }
}
