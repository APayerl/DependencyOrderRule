package se.payerl.sort.node;

import org.apache.maven.model.Dependency;
import se.payerl.sort.SortNode;
import se.payerl.sort.SortOrder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SortNode that groups dependencies according to a SortOrder rule and applies
 * child rules to each group.
 * <p>
 * Example: Group by scope and within each scope apply alphabetical sorting.
 */
public class GroupNode extends SortNode {
    private final SortOrder groupingRule;
    private final SortNode childRule;
    private final List<String> groupOrder;
    
    /**
     * Creates a GroupNode with specified grouping rule and child rule.
     *
     * @param groupingRule The rule used to group dependencies
     * @param childRule The rule applied to each group
     * @param groupOrder The order the groups should be in (first element comes first)
     */
    public GroupNode(SortOrder groupingRule, SortNode childRule, List<String> groupOrder) {
        this.groupingRule = Objects.requireNonNull(groupingRule, "groupingRule cannot be null");
        this.childRule = Objects.requireNonNull(childRule, "childRule cannot be null");
        this.groupOrder = new ArrayList<>(Objects.requireNonNull(groupOrder, "groupOrder cannot be null"));
    }
    
    /**
     * Creates a GroupNode with automatic group ordering.
     * Groups will be sorted in the order they are encountered.
     *
     * @param groupingRule The rule used to group dependencies
     * @param childRule The rule applied to each group
     */
    public GroupNode(SortOrder groupingRule, SortNode childRule) {
        this.groupingRule = Objects.requireNonNull(groupingRule, "groupingRule cannot be null");
        this.childRule = Objects.requireNonNull(childRule, "childRule cannot be null");
        this.groupOrder = new ArrayList<>();
    }

    @Override
    public List<String> validateDependencies(List<Dependency> dependencies) {
        requireNonNull(dependencies, "dependencies");
        
        if (dependencies.size() <= 1) {
            return new ArrayList<>();
        }
        
        List<String> errors = new ArrayList<>();
        
        // Group dependencies according to the grouping rule
        Map<String, List<Dependency>> groups = groupDependencies(dependencies);
        
        // Validate the order between groups
        errors.addAll(validateGroupOrder(dependencies, groups));
        
        // Validate the order within each group
        for (Map.Entry<String, List<Dependency>> entry : groups.entrySet()) {
            List<String> groupErrors = childRule.validateDependencies(entry.getValue());
            errors.addAll(groupErrors);
        }
        
        return errors;
    }
    
    @Override
    public String getDescription() {
        return String.format("Grouping by %s, then %s", 
                           groupingRule.getDescription(), 
                           childRule.getDescription());
    }
    
    private Map<String, List<Dependency>> groupDependencies(List<Dependency> dependencies) {
        Map<String, List<Dependency>> groups = new LinkedHashMap<>();
        
        for (Dependency dependency : dependencies) {
            if (groupingRule.isApplicable(dependency)) {
                String groupKey = groupingRule.extractSortKey(dependency);
                groups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(dependency);
            }
        }
        
        return groups;
    }
    
    private List<String> validateGroupOrder(List<Dependency> dependencies, 
                                          Map<String, List<Dependency>> groups) {
        List<String> errors = new ArrayList<>();
        
        if (groupOrder.isEmpty()) {
            // No specific group order specified, accept any order
            return errors;
        }
        
        // Find first dependency in each group to check order
        Map<String, Integer> firstOccurrence = new HashMap<>();
        for (int i = 0; i < dependencies.size(); i++) {
            Dependency dep = dependencies.get(i);
            if (groupingRule.isApplicable(dep)) {
                String groupKey = groupingRule.extractSortKey(dep);
                firstOccurrence.putIfAbsent(groupKey, i);
            }
        }
        
        // Check that groups follow specified order
        String previousGroup = null;
        int previousGroupOrderIndex = -1;
        
        for (Map.Entry<String, Integer> entry : firstOccurrence.entrySet()) {
            String currentGroup = entry.getKey();
            int currentGroupOrderIndex = groupOrder.indexOf(currentGroup);
            
            if (currentGroupOrderIndex != -1) { // Group exists in group order
                if (previousGroupOrderIndex != -1 && currentGroupOrderIndex < previousGroupOrderIndex) {
                    errors.add(String.format("Group '%s' should come before group '%s'", 
                                            currentGroup, previousGroup));
                }
                previousGroup = currentGroup;
                previousGroupOrderIndex = currentGroupOrderIndex;
            }
        }
        
        return errors;
    }
} 