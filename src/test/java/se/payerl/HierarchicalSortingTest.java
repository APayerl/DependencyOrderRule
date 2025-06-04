package se.payerl;

import org.apache.maven.model.Dependency;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Tests for hierarchical sorting with SortNode structure.
 */
public class HierarchicalSortingTest {

    @Test
    public void testSortLeaf_WithAlphabeticalOrder() {
        // Arrange
        AlphabeticalOrder alphabeticalOrder = new AlphabeticalOrder();
        SortLeaf sortLeaf = new SortLeaf(alphabeticalOrder);
        
        List<Dependency> dependencies = createDependenciesInWrongAlphabeticalOrder();
        
        // Act
        List<String> errors = sortLeaf.validateDependencies(dependencies);
        
        // Assert
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("com.google.guava:guava must be before org.apache.commons:commons-lang3"));
    }

    @Test
    public void testSortLeaf_WithCorrectOrder() {
        // Arrange
        AlphabeticalOrder alphabeticalOrder = new AlphabeticalOrder();
        SortLeaf sortLeaf = new SortLeaf(alphabeticalOrder);
        
        List<Dependency> dependencies = createDependenciesInAlphabeticalOrder();
        
        // Act
        List<String> errors = sortLeaf.validateDependencies(dependencies);
        
        // Assert
        assertTrue("Should have no errors", errors.isEmpty());
    }

    @Test
    public void testGroupNode_ScopeGroupingWithAlphabeticalSorting() {
        // Arrange
        ScopeOrder scopeOrder = new ScopeOrder("compile", "test");
        AlphabeticalOrder alphabeticalOrder = new AlphabeticalOrder();
        SortLeaf alphabeticalLeaf = new SortLeaf(alphabeticalOrder);
        
        GroupNode groupNode = new GroupNode(scopeOrder, alphabeticalLeaf, Arrays.asList("compile", "test"));
        
        List<Dependency> dependencies = createMixedScopesDependencies();
        
        // Act
        List<String> errors = groupNode.validateDependencies(dependencies);
        
        // Assert
        assertTrue("Should have no errors for correct grouping and sorting", errors.isEmpty());
    }

    @Test
    public void testGroupNode_WrongAlphabeticalOrderWithinGroup() {
        // Arrange
        ScopeOrder scopeOrder = new ScopeOrder("compile", "test");
        AlphabeticalOrder alphabeticalOrder = new AlphabeticalOrder();
        SortLeaf alphabeticalLeaf = new SortLeaf(alphabeticalOrder);
        
        GroupNode groupNode = new GroupNode(scopeOrder, alphabeticalLeaf, Arrays.asList("compile", "test"));
        
        List<Dependency> dependencies = createMixedScopesWithWrongAlphabeticalOrder();
        
        // Act
        List<String> errors = groupNode.validateDependencies(dependencies);
        
        // Assert
        assertFalse("Should have errors for wrong alphabetical order within groups", errors.isEmpty());
        assertTrue("Error should mention wrong alphabetical order", 
                  errors.stream().anyMatch(error -> error.contains("must be before")));
    }

    @Test
    public void testNestedGrouping() {
        // Arrange - Group first by scope, then by groupId within each scope
        ScopeOrder scopeOrder = new ScopeOrder("compile", "test");
        
        // Inner grouping by groupId (simplified - just check first part of groupId)
        SortOrder groupIdOrder = new SortOrder() {
            @Override
            public String toString() { return "GroupIdOrder"; }
            
            @Override
            public String extractSortKey(Dependency dependency) {
                return dependency.getGroupId().split("\\.")[0]; // First part of groupId
            }
            
            @Override
            public String formatDependencyForError(Dependency dependency) {
                return dependency.getGroupId() + ":" + dependency.getArtifactId();
            }
            
            @Override
            public String getDescription() { return "Grouping by first part of groupId"; }
            
            @Override
            public Optional<String> validateOrder(Dependency previousDependency, Dependency currentDependency) {
                return Optional.empty(); // Just grouping, not ordering
            }
            
            @Override
            public boolean isApplicable(Dependency dependency) { return true; }
        };
        
        AlphabeticalOrder alphabeticalOrder = new AlphabeticalOrder();
        SortLeaf alphabeticalLeaf = new SortLeaf(alphabeticalOrder);
        
        // Nested structure: Scope -> GroupId -> Alphabetical
        GroupNode innerGroupNode = new GroupNode(groupIdOrder, alphabeticalLeaf);
        GroupNode outerGroupNode = new GroupNode(scopeOrder, innerGroupNode, Arrays.asList("compile", "test"));
        
        List<Dependency> dependencies = createComplexHierarchyDependencies();
        
        // Act
        List<String> errors = outerGroupNode.validateDependencies(dependencies);
        
        // Assert
        assertTrue("Complex hierarchy should validate correctly", errors.isEmpty());
    }

    @Test
    public void testHierarchicalSortOrdersConfiguration() {
        // Arrange - test hierarchical logic directly without DependencyOrderRule
        ScopeOrder scopeOrder = new ScopeOrder("compile", "test");
        AlphabeticalOrder alphabeticalOrder = new AlphabeticalOrder(); 
        SortLeaf alphabeticalLeaf = new SortLeaf(alphabeticalOrder);
        
        GroupNode groupNode = new GroupNode(scopeOrder, alphabeticalLeaf, Arrays.asList("compile", "test"));
        
        List<Dependency> dependencies = createMixedScopesDependencies();
        
        // Act
        List<String> errors = groupNode.validateDependencies(dependencies);
        
        // Assert
        assertTrue("Hierarchical configuration should validate correctly", errors.isEmpty());
        assertEquals("Grouping by Checking for compile before test, then Checking for alphabetical order", 
                    groupNode.getDescription());
    }

    @Test
    public void testMultiScopeScopeOrder() {
        // Arrange - test the new multi-scope functionality with Scope enum
        ScopeOrder multiScopeOrder = new ScopeOrder(Scope.COMPILE, Scope.PROVIDED, Scope.TEST);
        
        List<Dependency> dependencies = createMultiScopeDependencies();
        
        // Act - test direct validation
        for (int i = 1; i < dependencies.size(); i++) {
            Optional<String> error = multiScopeOrder.validateOrder(dependencies.get(i-1), dependencies.get(i));
            
            // Assert
            assertFalse("Multi-scope order should validate correctly: " + error.orElse(""), error.isPresent());
        }
        
        // Test description
        assertEquals("Checking scope order: compile -> provided -> test", multiScopeOrder.getDescription());
        
        // Test applicability 
        assertTrue("Should apply to compile", multiScopeOrder.isApplicable(dependencies.get(0)));
        assertTrue("Should apply to provided", multiScopeOrder.isApplicable(dependencies.get(1)));
        assertTrue("Should apply to test", multiScopeOrder.isApplicable(dependencies.get(2)));
        
        // Test getter methods return correct Scope enums
        assertEquals("First should be COMPILE", Scope.COMPILE, multiScopeOrder.getFirst());
        assertEquals("Then should be PROVIDED", Scope.PROVIDED, multiScopeOrder.getThen());
        assertEquals("ThenList should contain PROVIDED and TEST", 
                    Arrays.asList(Scope.PROVIDED, Scope.TEST), multiScopeOrder.getThenList());
    }

    @Test
    public void testScopeOrderBackwardCompatibilityAndTypeSafety() {
        // Arrange - test backward compatibility with String constructor
        ScopeOrder stringBasedOrder = new ScopeOrder("compile", "test");
        ScopeOrder enumBasedOrder = new ScopeOrder(Scope.COMPILE, Scope.TEST);
        
        // Act & Assert - both should have same behavior
        assertEquals("String and enum based should have same description", 
                    stringBasedOrder.getDescription(), enumBasedOrder.getDescription());
        assertEquals("String and enum based should have same first", 
                    stringBasedOrder.getFirst(), enumBasedOrder.getFirst());
        assertEquals("String and enum based should have same then", 
                    stringBasedOrder.getThen(), enumBasedOrder.getThen());
        
        // Test type safety - invalid scopes are converted to COMPILE
        ScopeOrder invalidScope = new ScopeOrder("invalid-scope", "also-invalid");
        assertEquals("Invalid scope should default to COMPILE", Scope.COMPILE, invalidScope.getFirst());
        assertEquals("Invalid then should default to COMPILE", Scope.COMPILE, invalidScope.getThen());
        
        // Test setter methods with both types
        ScopeOrder mixedOrder = new ScopeOrder();
        mixedOrder.setFirst("provided");
        mixedOrder.addThen(Scope.TEST.getValue());
        
        assertEquals("String setter should work", Scope.PROVIDED, mixedOrder.getFirst());
        assertEquals("Enum addThen should work", Scope.TEST, mixedOrder.getThen());
    }

    private List<Dependency> createDependenciesInAlphabeticalOrder() {
        List<Dependency> dependencies = new ArrayList<>();
        
        Dependency dep1 = new Dependency();
        dep1.setGroupId("com.google.guava");
        dep1.setArtifactId("guava");
        dep1.setVersion("31.1-jre");
        dependencies.add(dep1);
        
        Dependency dep2 = new Dependency();
        dep2.setGroupId("org.apache.commons");
        dep2.setArtifactId("commons-lang3");
        dep2.setVersion("3.12.0");
        dependencies.add(dep2);
        
        return dependencies;
    }

    private List<Dependency> createDependenciesInWrongAlphabeticalOrder() {
        List<Dependency> dependencies = new ArrayList<>();
        
        Dependency dep1 = new Dependency();
        dep1.setGroupId("org.apache.commons");
        dep1.setArtifactId("commons-lang3");
        dep1.setVersion("3.12.0");
        dependencies.add(dep1);
        
        Dependency dep2 = new Dependency();
        dep2.setGroupId("com.google.guava");
        dep2.setArtifactId("guava");
        dep2.setVersion("31.1-jre");
        dependencies.add(dep2);
        
        return dependencies;
    }

    private List<Dependency> createMixedScopesDependencies() {
        List<Dependency> dependencies = new ArrayList<>();
        
        // Compile dependencies (alphabetical)
        Dependency compile1 = new Dependency();
        compile1.setGroupId("com.google.guava");
        compile1.setArtifactId("guava");
        compile1.setVersion("31.1-jre");
        compile1.setScope("compile");
        dependencies.add(compile1);
        
        Dependency compile2 = new Dependency();
        compile2.setGroupId("org.apache.commons");
        compile2.setArtifactId("commons-lang3");
        compile2.setVersion("3.12.0");
        compile2.setScope("compile");
        dependencies.add(compile2);
        
        // Test dependencies (alphabetical)
        Dependency test1 = new Dependency();
        test1.setGroupId("junit");
        test1.setArtifactId("junit");
        test1.setVersion("4.13.2");
        test1.setScope("test");
        dependencies.add(test1);
        
        Dependency test2 = new Dependency();
        test2.setGroupId("org.mockito");
        test2.setArtifactId("mockito-core");
        test2.setVersion("4.6.1");
        test2.setScope("test");
        dependencies.add(test2);
        
        return dependencies;
    }

    private List<Dependency> createMixedScopesWithWrongAlphabeticalOrder() {
        List<Dependency> dependencies = new ArrayList<>();
        
        // Compile dependencies (WRONG alphabetical order)
        Dependency compile1 = new Dependency();
        compile1.setGroupId("org.apache.commons");
        compile1.setArtifactId("commons-lang3");
        compile1.setVersion("3.12.0");
        compile1.setScope("compile");
        dependencies.add(compile1);
        
        Dependency compile2 = new Dependency();
        compile2.setGroupId("com.google.guava");
        compile2.setArtifactId("guava");
        compile2.setVersion("31.1-jre");
        compile2.setScope("compile");
        dependencies.add(compile2);
        
        // Test dependencies (correct alphabetical order)
        Dependency test1 = new Dependency();
        test1.setGroupId("junit");
        test1.setArtifactId("junit");
        test1.setVersion("4.13.2");
        test1.setScope("test");
        dependencies.add(test1);
        
        return dependencies;
    }

    private List<Dependency> createComplexHierarchyDependencies() {
        List<Dependency> dependencies = new ArrayList<>();
        
        // com.* compile dependencies
        Dependency compile1 = new Dependency();
        compile1.setGroupId("com.google.guava");
        compile1.setArtifactId("guava");
        compile1.setVersion("31.1-jre");
        compile1.setScope("compile");
        dependencies.add(compile1);
        
        // org.* compile dependencies
        Dependency compile2 = new Dependency();
        compile2.setGroupId("org.apache.commons");
        compile2.setArtifactId("commons-lang3");
        compile2.setVersion("3.12.0");
        compile2.setScope("compile");
        dependencies.add(compile2);
        
        // Test dependencies
        Dependency test1 = new Dependency();
        test1.setGroupId("junit");
        test1.setArtifactId("junit");
        test1.setVersion("4.13.2");
        test1.setScope("test");
        dependencies.add(test1);
        
        return dependencies;
    }

    private List<Dependency> createMultiScopeDependencies() {
        List<Dependency> dependencies = new ArrayList<>();
        
        Dependency compile1 = new Dependency();
        compile1.setGroupId("com.google.guava");
        compile1.setArtifactId("guava");
        compile1.setVersion("31.1-jre");
        compile1.setScope("compile");
        dependencies.add(compile1);
        
        Dependency provided1 = new Dependency();
        provided1.setGroupId("javax.servlet");
        provided1.setArtifactId("servlet-api");
        provided1.setVersion("2.5");
        provided1.setScope("provided");
        dependencies.add(provided1);
        
        Dependency test1 = new Dependency();
        test1.setGroupId("junit");
        test1.setArtifactId("junit");
        test1.setVersion("4.13.2");
        test1.setScope("test");
        dependencies.add(test1);
        
        return dependencies;
    }
} 