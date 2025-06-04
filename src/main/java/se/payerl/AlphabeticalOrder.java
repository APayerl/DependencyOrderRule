package se.payerl;

import org.apache.maven.model.Dependency;
import se.payerl.sort.SortOrder;

import java.util.Optional;

/**
 * Sorting order that checks that dependencies are sorted alphabetically
 * based on groupId:artifactId.
 * <p>
 * Can be configured to sort in reverse order by setting inversed to true.
 * </p>
 */
public class AlphabeticalOrder extends SortOrder {
    private boolean inversed = false;

    /**
     * Creates a new AlphabeticalOrder with default settings.
     * By default dependencies are sorted in normal alphabetical order.
     */
    public AlphabeticalOrder() { }

    /**
     * Specifies if the sorting should be reversed.
     *
     * @param inversed true for reverse alphabetical order, false for normal order
     */
    public void setInversed(boolean inversed) {
        this.inversed = inversed;
    }

    /**
     * Returns if the sorting is reversed.
     *
     * @return true if the sorting is reversed, otherwise false
     */
    public boolean isInversed() {
        return inversed;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{inversed=" + inversed + "}";
    }

    @Override
    public String extractSortKey(Dependency dependency) {
        requireNonNull(dependency, "dependency");
        return dependency.getGroupId() + ":" + dependency.getArtifactId();
    }

    @Override
    public String formatDependencyForError(Dependency dependency) {
        requireNonNull(dependency, "dependency");
        return extractSortKey(dependency);
    }

    @Override
    public String getDescription() {
        return "Checking for" + (inversed ? " inversed " : " ") + "alphabetical order";
    }

    @Override
    public Optional<String> validateOrder(Dependency previousDependency, Dependency currentDependency) {
        requireNonNull(previousDependency, "previousDependency");
        requireNonNull(currentDependency, "currentDependency");
        
        String previousKey = extractSortKey(previousDependency);
        String currentKey = extractSortKey(currentDependency);
        
        boolean isWrongOrder = (!inversed && previousKey.compareToIgnoreCase(currentKey) > 0) ||
                              (inversed && previousKey.compareToIgnoreCase(currentKey) < 0);
        
        if (isWrongOrder) {
            String errorMessage = String.format("Dependency %s must be before %s", 
                                               formatDependencyForError(currentDependency),
                                               formatDependencyForError(previousDependency));
            return Optional.of(errorMessage);
        }
        
        return Optional.empty();
    }

    @Override
    public boolean isApplicable(Dependency dependency) {
        requireNonNull(dependency, "dependency");
        return true;
    }
}
