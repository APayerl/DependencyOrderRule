package se.payerl.orders;

import org.apache.maven.model.Dependency;
import se.payerl.SortOrder;

import java.util.List;
import java.util.Objects;

public class ScopeOrder extends SortOrder {
    String first;
    String then;

    public ScopeOrder() { }
    public ScopeOrder(String first, String then) {
        this.first = first;
        this.then = then;
    }

    @Override
    protected List<? extends SortOrder> getSeparates() {
        return List.of();
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

    private void preFormat(Dependency dep) {
        if(dep.getScope() == null) dep.setScope("compile");
    }

    @Override
    protected void compareTo(Dependency prevDep, Dependency currentDep, List<String> errors) {
        preFormat(prevDep);
        preFormat(currentDep);

        if(Objects.equals(this.getFilter(prevDep), then) && Objects.equals(this.getFilter(currentDep), first)) {
            errors.add("Dependency " + this.depToStr(currentDep) + " must be before " + this.depToStr(prevDep));
        }
    }

    @Override
    protected String getJob() {
        return "Checking for " + first + " before " + then;
    }

    @Override
    protected boolean isDependencyApplicable(Dependency dep) {
        preFormat(dep);
        return Objects.equals(this.getFilter(dep), first) || Objects.equals(this.getFilter(dep), then);
    }
}
