package se.payerl;

import org.apache.maven.enforcer.rule.api.AbstractEnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maven Enforcer Plugin rule that checks the order of dependencies in pom.xml.
 * 
 * This rule checks that dependencies in both &lt;dependencies&gt; and
 * &lt;dependencyManagement&gt; sections are sorted according to configured
 * sorting rules.
 * 
 * Supports both simple sorting with SortOrder and hierarchical sorting.
 * 
 * Example of simple rules:
 * <pre>
 * &lt;DependencyOrderRule&gt;
 *   &lt;SortOrders&gt;
 *     &lt;AlphabeticalOrder&gt;
 *       &lt;inversed&gt;false&lt;/inversed&gt;
 *     &lt;/AlphabeticalOrder&gt;
 *     &lt;ScopeOrder&gt;
 *       &lt;first&gt;compile&lt;/first&gt;
 *       &lt;then&gt;test&lt;/then&gt;
 *     &lt;/ScopeOrder&gt;
 *   &lt;/SortOrders&gt;
 * &lt;/DependencyOrderRule&gt;
 * </pre>
 * 
 * Example of hierarchical sorting:
 * <pre>
 * &lt;DependencyOrderRule&gt;
 *   &lt;groupMode&gt;true&lt;/groupMode&gt;
 *   &lt;SortOrders&gt;
 *     &lt;ScopeOrder&gt;
 *       &lt;first&gt;compile&lt;/first&gt;
 *       &lt;then&gt;provided&lt;/then&gt;
 *       &lt;then&gt;test&lt;/then&gt;
 *     &lt;/ScopeOrder&gt;
 *     &lt;AlphabeticalOrder&gt;
 *       &lt;inversed&gt;false&lt;/inversed&gt;
 *     &lt;/AlphabeticalOrder&gt;
 *   &lt;/SortOrders&gt;
 * &lt;/DependencyOrderRule&gt;
 * </pre>
 * 
 * In group mode:
 * - First rule is used for grouping
 * - Remaining rules are applied within each group in sequence
 * - Supports multi-scope configuration with multiple &lt;then&gt; tags
 */
@Named("DependencyOrderRule")
public class DependencyOrderRule extends AbstractEnforcerRule {
    @Inject
    private MavenProject project;

    // Sort orders configuration
    private List<SortOrder> SortOrders;
    
    // Flag for hierarchical sorting - set as XML element instead of attribute
    private boolean hierarchical = false;

    @Override
    public void execute() throws EnforcerRuleException {
        List<String> dependencyErrors = checkDependencyList(project.getOriginalModel().getDependencies());
        
        List<String> dependencyManagementErrors = new ArrayList<>();
        if (project.getOriginalModel().getDependencyManagement() != null) {
            dependencyManagementErrors = checkDependencyList(project.getOriginalModel().getDependencyManagement().getDependencies());
        }

        if((dependencyErrors.size() + dependencyManagementErrors.size()) > 0) {
            String exceptionMessages = "";

            if(!dependencyErrors.isEmpty()) {
                String exceptionMsg = String.join("\n", dependencyErrors);
                exceptionMessages += "<dependencies> dependencies are not in correct order:\n" + exceptionMsg;
            }

            if(!dependencyErrors.isEmpty() && !dependencyManagementErrors.isEmpty()) {
                exceptionMessages += "\n\n";
            }

            if(!dependencyManagementErrors.isEmpty()) {
                String exceptionMsg = String.join("\n", dependencyManagementErrors);
                exceptionMessages += "<dependencyManagement> dependencies are not in correct order:\n" + exceptionMsg;
            }

            throw new EnforcerRuleException(exceptionMessages);
        }
    }

    private List<String> checkDependencyList(List<Dependency> dependencies) {
        if (dependencies == null || dependencies.size() <= 1) {
            getLog().info("Not enough dependencies to order");
            return new ArrayList<>();
        }

        if (SortOrders != null && !SortOrders.isEmpty()) {
            getLog().info("Configured with " + SortOrders.size() + " sort rules, groupMode=" + hierarchical);
            if (hierarchical) {
                getLog().info("Using group sorting mode");
                return checkDependencyListWithHierarchicalSortOrders(dependencies);
            } else {
                getLog().info("Using simple sorting mode");
                return checkDependencyListWithSortOrders(dependencies);
            }
        } else {
            getLog().info("No sort rules configured");
            return new ArrayList<>();
        }
    }
    
    private List<String> checkDependencyListWithHierarchicalSortOrders(List<Dependency> dependencies) {
        if (SortOrders.size() == 1) {
            // Only one rule, use normal sorting
            return checkDependencyListWithSortOrders(dependencies);
        }
        
        // Build automatic hierarchical structure
        SortOrder groupingRule = SortOrders.get(0);
        getLog().info("Group sorting - Grouping by: " + groupingRule.getDescription());
        
        // Create child rule from remaining rules
        SortNode childRule = createChildRuleFromSortOrders(SortOrders.subList(1, SortOrders.size()));
        
        // Create GroupNode
        GroupNode groupNode = new GroupNode(groupingRule, childRule);
        
        return groupNode.validateDependencies(dependencies);
    }
    
    private SortNode createChildRuleFromSortOrders(List<SortOrder> sortOrders) {
        if (sortOrders.size() == 1) {
            return new SortLeaf(sortOrders.get(0));
        } else if (sortOrders.size() > 1) {
            // Recursively create hierarchy
            SortOrder groupingRule = sortOrders.get(0);
            SortNode childRule = createChildRuleFromSortOrders(sortOrders.subList(1, sortOrders.size()));
            return new GroupNode(groupingRule, childRule);
        } else {
            throw new IllegalArgumentException("Cannot create child rule from empty SortOrders list");
        }
    }

    private List<String> checkDependencyListWithSortOrders(List<Dependency> dependencies) {
        List<String> errors = new ArrayList<>();

        SortOrders.forEach(sortOrder -> {
            getLog().info(sortOrder.getDescription());
            List<Dependency> applicableDependencies = dependencies.stream()
                    .filter(sortOrder::isApplicable)
                    .collect(Collectors.toList());
                    
            for(int i = 1; i < applicableDependencies.size(); i++) {
                Optional<String> validationError = sortOrder.validateOrder(
                    applicableDependencies.get(i-1), 
                    applicableDependencies.get(i)
                );
                validationError.ifPresent(errors::add);
            }
        });

        return errors;
    }

    @Override
    public String toString() {
        if (SortOrders != null) {
            String mode = hierarchical ? "group mode" : "simple";
            return String.format("DependencyOrderRule[%s SortOrders=%s]", mode, listToString(SortOrders));
        } else {
            return "DependencyOrderRule[no rules configured]";
        }
    }

    private String listToString(List<SortOrder> list) {
        return "[" + list.stream().map(SortOrder::toString).collect(Collectors.joining(",")) + "]";
    }
    
    /**
     * Sets SortOrders for the rule.
     * 
     * @param sortOrders List of SortOrder to use
     */
    public void setSortOrders(List<SortOrder> sortOrders) {
        this.SortOrders = sortOrders;
    }
    
    /**
     * Sets group mode (user-friendly interface for hierarchical sorting).
     * In group mode, the first rule is used for grouping dependencies,
     * and remaining rules are applied within each group.
     * 
     * @param groupMode true for group sorting, false for simple sorting
     */
    public void setGroupMode(boolean groupMode) {
        this.hierarchical = groupMode;
    }
    
    /**
     * Returns if group mode is enabled.
     * 
     * @return true if group mode is enabled
     */
    public boolean isGroupMode() {
        return hierarchical;
    }
}
