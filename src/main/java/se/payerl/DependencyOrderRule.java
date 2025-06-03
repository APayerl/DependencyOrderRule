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
        List<String> errors = new ArrayList<>();

        if(dependencies != null && dependencies.size() > 1) {
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

        return errors;
    }

    @Override
    public String toString() {
        return String.format("DependencyOrderRule[SortOrders=%s]", listToString(SortOrders));
    }

    private String listToString(List<SortOrder> list) {
        return "[" + list.stream().map(SortOrder::toString).collect(Collectors.joining(",")) + "]";
    }
}
