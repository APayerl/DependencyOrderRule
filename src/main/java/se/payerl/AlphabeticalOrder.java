package se.payerl;

import org.apache.maven.model.Dependency;

import java.util.List;

public class AlphabeticalOrder extends SortOrder<AlphabeticalOrder> {
    boolean inversed = false;

    public AlphabeticalOrder() { }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{inversed=" + inversed + "}";
    }

    @Override
    String getFilter(Dependency dep) {
        return dep.getGroupId() + ":" + dep.getArtifactId();
    }

    @Override
    String depToStr(Dependency dep) {
        return getFilter(dep);
    }

    @Override
    String getJob() {
        return "Checking for" + (inversed ? " inversed " : " ") + "alphabetical order";
    }

    @Override
    void compareTo(Dependency prevDep, Dependency currentDep, List<String> errors) {
        String prev = prevDep.getGroupId() + ":" + prevDep.getArtifactId();
        String curr = currentDep.getGroupId() + ":" + currentDep.getArtifactId();
        if(!inversed && prev.compareToIgnoreCase(curr) > 0 ||
                inversed && prev.compareToIgnoreCase(curr) < 0) {
            errors.add("Dependency " + curr + " must be before " + prev);
        }
    }

    @Override
    boolean isDependencyApplicable(Dependency dep) {
        return true;
    }
}
