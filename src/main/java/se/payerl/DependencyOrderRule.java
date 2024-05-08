package se.payerl;

import org.apache.maven.enforcer.rule.api.AbstractEnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named("dependencyOrderRule")
public class DependencyOrderRule extends AbstractEnforcerRule {
    @Inject
    private MavenProject project;

    private List<SortOrder> SortOrders;

    @Override
    public void execute() throws EnforcerRuleException {
        List<Dependency> dependencies = project.getDependencies();
        List<String> exceptions = new ArrayList<>();

        if(dependencies.size() > 1) {
            SortOrders.forEach(sortOrder -> {
                getLog().info("Checking for " + sortOrder.getFirst() + " before " + sortOrder.getThen());
                List<Dependency> listOfDeps = dependencies.stream()
                        .filter(dep ->
                                dep.getScope().equalsIgnoreCase(sortOrder.getFirst()) ||
                                dep.getScope().equalsIgnoreCase(sortOrder.getThen()))
                        .collect(Collectors.toList());
                for(int i = 1; i < listOfDeps.size(); i++) {
                    compDeps(sortOrder, exceptions, listOfDeps.get(i-1), listOfDeps.get(i));
                }
            });
        } else {
            getLog().info("Not enough dependencies to order");
        }

        if(!exceptions.isEmpty()) {
            String exceptionMessages = String.join("\n", exceptions);
            throw new EnforcerRuleException("Dependencies are not in correct order:\n" + exceptionMessages);
        }
    }

    @Override
    public String toString() {
        return String.format("DependencyOrderRule[SortOrders=%b]", listToString(SortOrders));
    }

    private String listToString(List<SortOrder> list) {
        return "[" + list.stream().map(SortOrder::toString).collect(Collectors.joining(",")) + "]";
    }

    private String depToStr(Dependency dep) {
        return dep.getGroupId() + ":" + dep.getArtifactId() + " scope:" + dep.getScope();
    }

    private void compDeps(SortOrder sortOrder, List<String> exceps, Dependency prevDep, Dependency currentDep) {
        if(prevDep.getScope().equalsIgnoreCase(sortOrder.getThen()) &&
                currentDep.getScope().equalsIgnoreCase(sortOrder.getFirst())) {
            exceps.add("Dependency " + depToStr(currentDep) + " must be before " + depToStr(prevDep));
        }
    }
}
