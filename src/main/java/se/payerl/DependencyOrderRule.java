package se.payerl;

import org.apache.maven.enforcer.rule.api.AbstractEnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugins.annotations.Component;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named("DependencyOrderRule")
public class DependencyOrderRule extends AbstractEnforcerRule {
    @Component
    @jakarta.inject.Inject
    @javax.inject.Inject
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    private List<SortOrder> SortOrders;

    @Override
    public void execute() throws EnforcerRuleException {
        List<Dependency> dependencies = project.getDependencies();
        List<EnforcerRuleException> exceptions = new ArrayList<>();

        if(dependencies.size() > 1) {
            SortOrders.forEach(sortOrder -> {
                getLog().info("Checking for " + sortOrder.getFirst() + " before " + sortOrder.getThen());
                List<Dependency> listOfDeps = dependencies.stream()
                        .filter(dep ->
                                dep.getScope().equalsIgnoreCase(sortOrder.getFirst()) ||
                                dep.getScope().equalsIgnoreCase(sortOrder.getThen()))
                        .collect(Collectors.toList());
                for(int i = 1; i < listOfDeps.size(); i++) {
                    Dependency prevDep = listOfDeps.get(i-1);
                    Dependency currentDep = listOfDeps.get(i);

                    if(prevDep.getScope().equalsIgnoreCase(sortOrder.getThen()) &&
                            currentDep.getScope().equalsIgnoreCase(sortOrder.getFirst())) {
                        exceptions.add(new EnforcerRuleException("Dependency " + prevDep.getGroupId() + ":" + prevDep.getArtifactId() + " must be before " + currentDep.getGroupId() + ":" + currentDep.getArtifactId()));
                    }
                }
            });
        } else {
            getLog().info("Not enough dependencies to order");
        }

        if(!exceptions.isEmpty()) {
            EnforcerRuleException ex = new EnforcerRuleException("Dependencies are not in correct order");
            exceptions.forEach(ex::addSuppressed);
            throw ex;
        }
    }

    @Override
    public String toString() {
        return String.format("DependencyOrderRule[SortOrders=%b]", listToString(SortOrders));
    }

    private String listToString(List<SortOrder> list) {
        return "[" + list.stream().map(SortOrder::toString).collect(Collectors.joining(",")) + "]";
    }
}
