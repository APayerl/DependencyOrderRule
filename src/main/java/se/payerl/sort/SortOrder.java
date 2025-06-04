package se.payerl.sort;

import org.apache.maven.model.Dependency;

import java.util.List;
import java.util.Optional;

/**
 * Abstract base class for defining different sorting orders for Maven dependencies.
 * 
 * This class is used by DependencyOrderRule to check that dependencies
 * in a Maven pom.xml are sorted according to specific rules. Each implementation
 * defines its own logic for how dependencies should be compared and sorted.
 * 
 * Available implementations include:
 * <ul>
 *   <li>{@code AlphabeticalOrder} - Sorts dependencies alphabetically based on groupId:artifactId</li>
 *   <li>{@code ScopeOrder} - Sorts dependencies based on scope (e.g. compile before test)</li>
 *   <li>{@code OptionalOrder} - Sorts dependencies based on the optional flag</li>
 * </ul>
 */
public abstract class SortOrder {
    
    /**
     * Returns a string representation of this sorting order.
     * Used for logging and debugging.
     *
     * @return A descriptive string of the sorting order and its configuration
     */
    @Override
    public abstract String toString();
    
    /**
     * Extracts the sorting key from a dependency used for comparison.
     * 
     * Different implementations extract different values:
     * <ul>
     *   <li>AlphabeticalOrder: returns "groupId:artifactId"</li>
     *   <li>ScopeOrder: returns dependency scope</li>
     *   <li>OptionalOrder: returns optional value as string</li>
     * </ul>
     *
     * @param dependency Maven dependency to extract sorting key from
     * @return The sorting key used for comparison
     * @throws IllegalArgumentException if dependency is null
     */
    public abstract String extractSortKey(Dependency dependency);
    
    /**
     * Formats a dependency to a readable string representation for error messages.
     * Used to generate informative error messages when dependencies are not in correct order.
     *
     * @param dependency Maven dependency to format
     * @return A descriptive string of the dependency (e.g. "groupId:artifactId scope:test")
     * @throws IllegalArgumentException if dependency is null
     */
    public abstract String formatDependencyForError(Dependency dependency);
    
    /**
     * Returns a description of what this sorting rule checks.
     * Used for logging when the rule is executed.
     *
     * @return Description of the sorting rule for logging
     */
    public abstract String getDescription();
    
    /**
     * Validates that two dependencies are in correct order according to this sorting rule.
     * 
     * This method implements the core logic for each sorting rule. It checks
     * if the current dependency should come before the previous dependency according to
     * this sorting order.
     *
     * @param previousDependency The previous dependency in the list
     * @param currentDependency The current dependency to compare with
     * @return Optional containing error message if dependencies are not in correct order, 
     *         or Optional.empty() if the order is correct
     * @throws IllegalArgumentException if any of the dependencies is null
     */
    public abstract Optional<String> validateOrder(Dependency previousDependency, Dependency currentDependency);
    
    /**
     * Checks if this sorting rule is applicable to a given dependency.
     * 
     * Some sorting rules only apply to specific dependencies. For example
     * ScopeOrder only applies to dependencies with the scopes that have been configured.
     *
     * @param dependency Maven dependency to check
     * @return {@code true} if the rule should be applied to this dependency, otherwise {@code false}
     * @throws IllegalArgumentException if dependency is null
     */
    public abstract boolean isApplicable(Dependency dependency);
    
    /**
     * Helper method to check that a dependency is not null.
     *
     * @param dependency dependency to check
     * @param parameterName name of the parameter for the error message
     * @throws IllegalArgumentException if dependency is null
     */
    protected final void requireNonNull(Dependency dependency, String parameterName) {
        if (dependency == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
    }
} 