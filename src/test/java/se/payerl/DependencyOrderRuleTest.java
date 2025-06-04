package se.payerl;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerLogger;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.payerl.DependencyOrderRule;
import se.payerl.sort.SortOrder;
import se.payerl.AlphabeticalOrder;
import se.payerl.ScopeOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DependencyOrderRuleTest {

    @Mock
    private MavenProject mockProject;

    @Mock
    private Model mockModel;

    @Mock
    private EnforcerLogger mockLogger;

    private DependencyOrderRule rule;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        rule = new DependencyOrderRule();
        
        // Mock the getLog() method to return our mock logger
        rule = spy(new DependencyOrderRule());
        when(rule.getLog()).thenReturn(mockLogger);
        
        // Inject the mock project using reflection
        try {
            java.lang.reflect.Field projectField = DependencyOrderRule.class.getDeclaredField("project");
            projectField.setAccessible(true);
            projectField.set(rule, mockProject);
        } catch (Exception e) {
            fail("Failed to inject mock project: " + e.getMessage());
        }
    }

    @Test
    public void testExecute_WithCorrectAlphabeticalOrder_ShouldNotThrowException() throws EnforcerRuleException {
        // Arrange
        List<Dependency> dependencies = createDependenciesInAlphabeticalOrder();
        List<SortOrder> sortOrders = Arrays.asList(new AlphabeticalOrder());
        
        when(mockProject.getOriginalModel()).thenReturn(mockModel);
        when(mockModel.getDependencies()).thenReturn(dependencies);
        when(mockModel.getDependencyManagement()).thenReturn(null);
        
        setSortOrders(sortOrders);

        // Act & Assert - Should not throw exception
        rule.execute();
    }

    @Test(expected = EnforcerRuleException.class)
    public void testExecute_WithIncorrectAlphabeticalOrder_ShouldThrowException() throws EnforcerRuleException {
        // Arrange
        List<Dependency> dependencies = createDependenciesInWrongAlphabeticalOrder();
        List<SortOrder> sortOrders = Arrays.asList(new AlphabeticalOrder());
        
        when(mockProject.getOriginalModel()).thenReturn(mockModel);
        when(mockModel.getDependencies()).thenReturn(dependencies);
        when(mockModel.getDependencyManagement()).thenReturn(null);
        
        setSortOrders(sortOrders);

        // Act
        rule.execute();
    }

    @Test
    public void testExecute_WithCorrectScopeOrder_ShouldNotThrowException() throws EnforcerRuleException {
        // Arrange
        List<Dependency> dependencies = createDependenciesInCorrectScopeOrder();
        List<SortOrder> sortOrders = Arrays.asList(new ScopeOrder("compile", "test"));
        
        when(mockProject.getOriginalModel()).thenReturn(mockModel);
        when(mockModel.getDependencies()).thenReturn(dependencies);
        when(mockModel.getDependencyManagement()).thenReturn(null);
        
        setSortOrders(sortOrders);

        // Act & Assert - Should not throw exception
        rule.execute();
    }

    @Test(expected = EnforcerRuleException.class)
    public void testExecute_WithIncorrectScopeOrder_ShouldThrowException() throws EnforcerRuleException {
        // Arrange
        List<Dependency> dependencies = createDependenciesInWrongScopeOrder();
        List<SortOrder> sortOrders = Arrays.asList(new ScopeOrder("compile", "test"));
        
        when(mockProject.getOriginalModel()).thenReturn(mockModel);
        when(mockModel.getDependencies()).thenReturn(dependencies);
        when(mockModel.getDependencyManagement()).thenReturn(null);
        
        setSortOrders(sortOrders);

        // Act
        rule.execute();
    }

    @Test
    public void testExecute_WithNullDependencies_ShouldNotThrowException() throws EnforcerRuleException {
        // Arrange
        List<SortOrder> sortOrders = Arrays.asList(new AlphabeticalOrder());
        
        when(mockProject.getOriginalModel()).thenReturn(mockModel);
        when(mockModel.getDependencies()).thenReturn(null);
        when(mockModel.getDependencyManagement()).thenReturn(null);
        
        setSortOrders(sortOrders);

        // Act & Assert - Should not throw exception
        rule.execute();
    }

    @Test
    public void testExecute_WithEmptyDependencies_ShouldNotThrowException() throws EnforcerRuleException {
        // Arrange
        List<Dependency> dependencies = new ArrayList<>();
        List<SortOrder> sortOrders = Arrays.asList(new AlphabeticalOrder());
        
        when(mockProject.getOriginalModel()).thenReturn(mockModel);
        when(mockModel.getDependencies()).thenReturn(dependencies);
        when(mockModel.getDependencyManagement()).thenReturn(null);
        
        setSortOrders(sortOrders);

        // Act & Assert - Should not throw exception
        rule.execute();
    }

    @Test
    public void testExecute_WithDependencyManagement_ShouldCheckBothSections() throws EnforcerRuleException {
        // Arrange
        List<Dependency> dependencies = createDependenciesInAlphabeticalOrder();
        List<Dependency> managementDependencies = createDependenciesInAlphabeticalOrder();
        DependencyManagement dependencyManagement = new DependencyManagement();
        dependencyManagement.setDependencies(managementDependencies);
        
        List<SortOrder> sortOrders = Arrays.asList(new AlphabeticalOrder());
        
        when(mockProject.getOriginalModel()).thenReturn(mockModel);
        when(mockModel.getDependencies()).thenReturn(dependencies);
        when(mockModel.getDependencyManagement()).thenReturn(dependencyManagement);
        
        setSortOrders(sortOrders);

        // Act & Assert - Should not throw exception
        rule.execute();
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

    private List<Dependency> createDependenciesInCorrectScopeOrder() {
        List<Dependency> dependencies = new ArrayList<>();
        
        Dependency dep1 = new Dependency();
        dep1.setGroupId("com.google.guava");
        dep1.setArtifactId("guava");
        dep1.setVersion("31.1-jre");
        dep1.setScope("compile");
        dependencies.add(dep1);
        
        Dependency dep2 = new Dependency();
        dep2.setGroupId("junit");
        dep2.setArtifactId("junit");
        dep2.setVersion("4.13.2");
        dep2.setScope("test");
        dependencies.add(dep2);
        
        return dependencies;
    }

    private List<Dependency> createDependenciesInWrongScopeOrder() {
        List<Dependency> dependencies = new ArrayList<>();
        
        Dependency dep1 = new Dependency();
        dep1.setGroupId("junit");
        dep1.setArtifactId("junit");
        dep1.setVersion("4.13.2");
        dep1.setScope("test");
        dependencies.add(dep1);
        
        Dependency dep2 = new Dependency();
        dep2.setGroupId("com.google.guava");
        dep2.setArtifactId("guava");
        dep2.setVersion("31.1-jre");
        dep2.setScope("compile");
        dependencies.add(dep2);
        
        return dependencies;
    }

    private void setSortOrders(List<SortOrder> sortOrders) {
        try {
            java.lang.reflect.Field sortOrdersField = DependencyOrderRule.class.getDeclaredField("SortOrders");
            sortOrdersField.setAccessible(true);
            sortOrdersField.set(rule, sortOrders);
        } catch (Exception e) {
            fail("Failed to set sort orders: " + e.getMessage());
        }
    }
} 