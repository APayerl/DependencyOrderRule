package se.payerl.orders;

import org.apache.maven.model.Dependency;
import se.payerl.SortOrder;

import java.util.List;

public class AlphabeticalOrder extends SortOrder {
    boolean inversed = false;

    public AlphabeticalOrder() { }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{inversed=" + inversed + "}";
    }

    @Override
    protected String getFilter(Dependency dep) {
        return dep.getGroupId() + ":" + dep.getArtifactId();
    }

    @Override
    protected String depToStr(Dependency dep) {
        return getFilter(dep);
    }

    @Override
    protected String getJob() {
        return "Checking for" + (inversed ? " inversed " : " ") + "alphabetical order";
    }

    @Override
    protected void compareTo(Dependency prevDep, Dependency currentDep, List<String> errors) {
        String prev = prevDep.getGroupId() + ":" + prevDep.getArtifactId();
        String curr = currentDep.getGroupId() + ":" + currentDep.getArtifactId();
        if(!inversed && prev.compareToIgnoreCase(curr) > 0 ||
                inversed && prev.compareToIgnoreCase(curr) < 0) {
            errors.add("Dependency " + curr + " must be before " + prev);
        }
    }

    @Override
    protected boolean isDependencyApplicable(Dependency dep) {
        return true;
    }
}
