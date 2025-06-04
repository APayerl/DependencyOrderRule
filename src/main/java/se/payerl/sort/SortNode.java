package se.payerl.sort;

import org.apache.maven.model.Dependency;

import java.util.List;
import java.util.Optional;

/**
 * Abstract base class for hierarchical sorting of Maven dependencies.
 * <p>
 * A SortNode can be either a GroupNode that groups dependencies
 * or a SortLeaf that sorts dependencies directly.
 * <p>
 * This enables complex hierarchical sorting such as:
 * <ul>
 *   <li>First group by scope</li>
 *   <li>Within each scope, group by groupId</li>
 *   <li>Within each groupId, sort alphabetically by artifactId</li>
 * </ul>
 */
public abstract class SortNode {
    
    /**
     * Validates the order of a list of dependencies according to this node's rules.
     * 
     * @param dependencies List of dependencies to validate
     * @return List of error messages, empty if everything is correct
     * @throws IllegalArgumentException if dependencies is null
     */
    public abstract List<String> validateDependencies(List<Dependency> dependencies);
    
    /**
     * Returns a description of what this node checks.
     * Used for logging.
     * 
     * @return Description of the sorting rule
     */
    public abstract String getDescription();
    
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
    
    /**
     * Helper method to check that a list is not null.
     *
     * @param dependencies list to check
     * @param parameterName name of the parameter for the error message
     * @throws IllegalArgumentException if the list is null
     */
    protected final void requireNonNull(List<Dependency> dependencies, String parameterName) {
        if (dependencies == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
    }
} 