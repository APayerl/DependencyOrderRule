package se.payerl;

import org.apache.maven.model.Dependency;
import se.payerl.model.Scope;
import se.payerl.sort.SortOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sorting order that controls that dependencies with certain scopes
 * come in specified order.
 * 
 * Supports both simple two-scope configurations and complex multi-scope orders:
 * 
 * Simple configuration (backward compatibility):
 * <pre>
 * &lt;ScopeOrder&gt;
 *   &lt;first&gt;compile&lt;/first&gt;
 *   &lt;then&gt;test&lt;/then&gt;
 * &lt;/ScopeOrder&gt;
 * </pre>
 * 
 * Multi-scope configuration:
 * <pre>
 * &lt;ScopeOrder&gt;
 *   &lt;first&gt;compile&lt;/first&gt;
 *   &lt;then&gt;provided&lt;/then&gt;
 *   &lt;then&gt;test&lt;/then&gt;
 * &lt;/ScopeOrder&gt;
 * </pre>
 */
public class ScopeOrder extends SortOrder {
    private Scope firstScope;
    private List<Scope> thenScopeList = new ArrayList<>();

    /**
     * Creates a new ScopeOrder without configuration.
     * Requires that first and then are set via setter methods.
     */
    public ScopeOrder() { }
    
    /**
     * Creates a new ScopeOrder with specified order.
     *
     * @param first scope that should come first
     * @param then scope that should come after first
     */
    public ScopeOrder(Scope first, Scope then) {
        this.firstScope = first;
        this.thenScopeList.add(then);
    }
    
    /**
     * Creates a new ScopeOrder with specified order.
     * Backward compatibility for string-based constructors.
     *
     * @param first scope that should come first
     * @param then scope that should come after first
     */
    public ScopeOrder(String first, String then) {
        this.firstScope = Scope.fromValue(first);
        this.thenScopeList.add(Scope.fromValue(then));
    }
    
    /**
     * Creates a new ScopeOrder with specified order for multiple scopes.
     *
     * @param first scope that should come first
     * @param thenScopes scopes that should come after first in this order
     */
    public ScopeOrder(Scope first, Scope... thenScopes) {
        this.firstScope = first;
        this.thenScopeList.addAll(Arrays.asList(thenScopes));
    }
    
    /**
     * Creates a new ScopeOrder with specified order for multiple scopes.
     * Backward compatibility for string-based constructors.
     *
     * @param first scope that should come first
     * @param thenScopes scopes that should come after first in this order
     */
    public ScopeOrder(String first, String... thenScopes) {
        this.firstScope = Scope.fromValue(first);
        this.thenScopeList.addAll(
                Arrays.stream(thenScopes)
                      .map(Scope::fromValue)
                      .collect(Collectors.toList()));
    }

    /**
     * Specifies which scope should come first.
     * Maven XML compatibility - converts string to Scope.
     *
     * @param first scope that should come first
     */
    public void setFirst(String first) {
        this.firstScope = Scope.fromValue(first);
    }

    /**
     * Specifies which scope should come after first.
     * Maven XML compatibility - converts string to Scope.
     *
     * @param then scope that should come after first
     */
    public void setThen(String... then) {
        this.thenScopeList.clear();
        this.thenScopeList.addAll(Arrays.stream(then)
                                     .map(Scope::fromValue)
                                     .collect(Collectors.toList()));
    }

    /**
     * Adds a scope that should come after previous scopes.
     * Maven XML compatibility - converts string to Scope.
     *
     * @param then scope to add to the order
     */
    public void addThen(String then) {
        this.thenScopeList.add(Scope.fromValue(then));
    }

    /**
     * Returns scope that should come first.
     *
     * @return scope that should come first
     */
    public Scope getFirst() {
        return firstScope;
    }

    /**
     * Returns first scope that should come after first.
     * Backward compatibility.
     *
     * @return first scope in then-list or null if empty
     */
    public Scope getThen() {
        return thenScopeList.isEmpty() ? null : thenScopeList.get(0);
    }
    
    /**
     * Returns all scopes that should come after first.
     *
     * @return list of scopes in order
     */
    public List<Scope> getThenList() {
        return thenScopeList;
    }

    @Override
    public String toString() {
        if (thenScopeList.size() == 1) {
            return "ScopeOrder{first=" + firstScope + ", then=" + thenScopeList.get(0) + "}";
        } else {
            return "ScopeOrder{first=" + firstScope + ", thenList=" + thenScopeList + "}";
        }
    }

    @Override
    public String extractSortKey(Dependency dependency) {
        requireNonNull(dependency, "dependency");
        String scope = dependency.getScope();
        return scope != null ? scope : "compile";
    }

    @Override
    public String formatDependencyForError(Dependency dependency) {
        requireNonNull(dependency, "dependency");
        return dependency.getGroupId() + ":" + dependency.getArtifactId() + " scope:" + extractSortKey(dependency);
    }

    @Override
    public Optional<String> validateOrder(Dependency previousDependency, Dependency currentDependency) {
        requireNonNull(previousDependency, "previousDependency");
        requireNonNull(currentDependency, "currentDependency");

        Scope prevScope = Scope.fromValue(extractSortKey(previousDependency));
        Scope currentScope = Scope.fromValue(extractSortKey(currentDependency));
        
        // Create complete order list
        List<Scope> scopeOrder = new ArrayList<>();
        scopeOrder.add(getFirst());
        scopeOrder.addAll(getThenList());
        
        int prevIndex = scopeOrder.indexOf(prevScope);
        int currentIndex = scopeOrder.indexOf(currentScope);
        
        // If any scope is not in the list, no validation
        if (prevIndex == -1 || currentIndex == -1) {
            return Optional.empty();
        }
        
        // Check that the order is correct
        if (prevIndex > currentIndex) {
            String errorMessage = String.format("Dependency %s (scope: %s) must be before %s (scope: %s)", 
                                               formatDependencyForError(currentDependency), currentScope,
                                               formatDependencyForError(previousDependency), prevScope);
            return Optional.of(errorMessage);
        }
        
        return Optional.empty();
    }

    @Override
    public String getDescription() {
        if (thenScopeList.size() == 1) {
            return "Checking for " + firstScope + " before " + thenScopeList.get(0);
        } else {
            String thenChain = String.join(" -> ", thenScopeList.stream().map(Scope::toString).collect(Collectors.toList()));
            return "Checking scope order: " + firstScope + " -> " + thenChain;
        }
    }

    @Override
    public boolean isApplicable(Dependency dependency) {
        requireNonNull(dependency, "dependency");
        Scope scope = Scope.fromValue(extractSortKey(dependency));
        return Objects.equals(scope, getFirst()) || getThenList().contains(scope);
    }
}
