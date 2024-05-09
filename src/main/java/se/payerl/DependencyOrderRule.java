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
        List<String> errors = new ArrayList<>();

        if(dependencies.size() > 1) {
            SortOrders.forEach(sortOrder -> {
                getLog().info(sortOrder.getJob());
                List<Dependency> listOfDeps = dependencies.stream()
                        .filter(sortOrder::isDependencyApplicable)
                        .collect(Collectors.toList());
                for(int i = 1; i < listOfDeps.size(); i++) {
                    sortOrder.compareTo(listOfDeps.get(i-1), listOfDeps.get(i), errors);
                }
            });
        } else {
            getLog().info("Not enough dependencies to order");
        }

        if(!errors.isEmpty()) {
            String exceptionMessages = String.join("\n", errors);
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
}
