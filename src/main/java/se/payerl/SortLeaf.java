package se.payerl;

import org.apache.maven.model.Dependency;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SortNode that applies a SortOrder directly to a list of dependencies.
 * <p>
 * This is a leaf node in the sorting tree that performs actual sorting validation.
 */
public class SortLeaf extends SortNode {
    private final SortOrder sortOrder;
    
    /**
     * Creates a SortLeaf with specified sorting rule.
     *
     * @param sortOrder The sorting rule to apply
     */
    public SortLeaf(SortOrder sortOrder) {
        this.sortOrder = Objects.requireNonNull(sortOrder, "sortOrder cannot be null");
    }

    @Override
    public List<String> validateDependencies(List<Dependency> dependencies) {
        requireNonNull(dependencies, "dependencies");
        
        List<String> errors = new ArrayList<>();
        
        if (dependencies.size() <= 1) {
            return errors;
        }
        
        // Filter dependencies that the rule applies to
        List<Dependency> applicableDependencies = dependencies.stream()
                .filter(sortOrder::isApplicable)
                .collect(Collectors.toList());
        
        // Validate order for applicable dependencies
        for (int i = 1; i < applicableDependencies.size(); i++) {
            Optional<String> validationError = sortOrder.validateOrder(
                applicableDependencies.get(i-1), 
                applicableDependencies.get(i)
            );
            validationError.ifPresent(errors::add);
        }
        
        return errors;
    }

    @Override
    public String getDescription() {
        return sortOrder.getDescription();
    }
    
    /**
     * Returns the underlying SortOrder.
     *
     * @return SortOrder used by this leaf
     */
    public SortOrder getSortOrder() {
        return sortOrder;
    }
} 