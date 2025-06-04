package se.payerl;

import org.apache.maven.model.Dependency;

import java.util.Objects;
import java.util.Optional;

/**
 * Sorting order that checks that dependencies with certain optional values
 * come in specified order.
 * 
 * For example, one can configure that non-optional dependencies should come before optional dependencies.
 */
public class OptionalOrder extends SortOrder {
    private String first;
    private String then;

    /**
     * Creates a new OptionalOrder without configuration.
     * Requires that first and then are set via setter methods.
     */
    public OptionalOrder() { }

    /**
     * Specifies which optional value should come first.
     *
     * @param first optional value that should come first ("true" or "false")
     */
    public void setFirst(String first) {
        this.first = first;
    }

    /**
     * Specifies which optional value should come after first.
     *
     * @param then optional value that should come after first ("true" or "false")
     */
    public void setThen(String then) {
        this.then = then;
    }

    /**
     * Returns optional value that should come first.
     *
     * @return optional value that should come first
     */
    public String getFirst() {
        return first;
    }

    /**
     * Returns optional value that should come after first.
     *
     * @return optional value that should come after first
     */
    public String getThen() {
        return then;
    }

    private String getOptionalValue(Dependency dependency) {
        return Boolean.toString(Objects.equals(dependency.getOptional(), "true"));
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{first=" + first + ", then=" + then + "}";
    }

    @Override
    public String extractSortKey(Dependency dependency) {
        requireNonNull(dependency, "dependency");
        return getOptionalValue(dependency);
    }

    @Override
    public String formatDependencyForError(Dependency dependency) {
        requireNonNull(dependency, "dependency");
        return dependency.getGroupId() + ":" + dependency.getArtifactId() + " optional:" + getOptionalValue(dependency);
    }

    @Override
    public Optional<String> validateOrder(Dependency previousDependency, Dependency currentDependency) {
        requireNonNull(previousDependency, "previousDependency");
        requireNonNull(currentDependency, "currentDependency");

        String prevOptional = extractSortKey(previousDependency);
        String currentOptional = extractSortKey(currentDependency);

        if (prevOptional.equalsIgnoreCase(then) && currentOptional.equalsIgnoreCase(first)) {
            String errorMessage = String.format("Dependency %s must be before %s", 
                                               formatDependencyForError(currentDependency),
                                               formatDependencyForError(previousDependency));
            return Optional.of(errorMessage);
        }
        
        return Optional.empty();
    }

    @Override
    public String getDescription() {
        return "Checking for " + first + " before " + then;
    }

    @Override
    public boolean isApplicable(Dependency dependency) {
        requireNonNull(dependency, "dependency");
        String optionalValue = extractSortKey(dependency);
        return optionalValue.equalsIgnoreCase(first) || optionalValue.equalsIgnoreCase(then);
    }
}
