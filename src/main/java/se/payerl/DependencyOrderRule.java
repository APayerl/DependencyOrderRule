package se.payerl;

import org.apache.maven.enforcer.rule.api.AbstractEnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named("dependencyOrderRule")
public class DependencyOrderRule extends AbstractEnforcerRule {
    @Inject
    private MavenProject project;

    private List<SortOrder> SortOrders;

    @Override
    public void execute() throws EnforcerRuleException {
        List<String> dependencyErrors = checkDependencyList(project.getDependencies());
//        List<String> dependencyManagementErrors = checkDependencyList(project.getDependencyManagement().getDependencies());

//        if((dependencyErrors.size() + dependencyManagementErrors.size()) > 0) {
            String exceptionMessages = "";

            if(!dependencyErrors.isEmpty()) {
                String exceptionMsg = String.join("\n", dependencyErrors);
                exceptionMessages += "<dependencies> dependencies are not in correct order:\n" + exceptionMsg;
            }

//            if(!dependencyErrors.isEmpty() && !dependencyManagementErrors.isEmpty()) {
//                exceptionMessages += "\n\n";
//            }
//
//            if(!dependencyManagementErrors.isEmpty()) {
//                String exceptionMsg = String.join("\n", dependencyManagementErrors);
//                exceptionMessages += "<dependencyManagement> dependencies are not in correct order:\n" + exceptionMsg;
//            }

            throw new EnforcerRuleException(exceptionMessages);
//        }
    }

    private List<String> checkDependencyList(List<Dependency> dependencies) {
        List<String> errors = new ArrayList<>();

        if(dependencies.size() > 1) {
            SortOrders.forEach(order -> {
                getLog().info(order.getJob());
                List<Dependency> listOfDeps = dependencies.stream()
                        .filter(order::isDependencyApplicable)
                        .collect(Collectors.toList());
                Map<String, List<Dependency>> depGroups = new HashMap<>();

                depGroups.put("default", new ArrayList<>());
                for(Dependency dep: listOfDeps) {
                    boolean grouped = false;
                    for(SortOrder separate: order.separates) {
                        if(separate.isDependencyApplicable(dep)) {
                            String key = separate.toString();
                            if(!depGroups.containsKey(key)) {
                                depGroups.put(key, new ArrayList<>());
                            }
                            depGroups.get(key).add(dep);
                            grouped = true;
                            break;
                        }
                    }
                    if(!grouped) depGroups.get("default").add(dep);
                }

                depGroups.values().forEach(deps -> {
                    if(deps.size() > 1) {
                        for (int i = 1; i < deps.size(); i++) {
                            order.compareTo(deps.get(i - 1), deps.get(i), errors);
                        }
                    } else getLog().info("Not enough dependencies to order");
                });
            });
        } else {
            getLog().info("Not enough dependencies to order");
        }

        return errors;
    }

    @Override
    public String toString() {
        return String.format("DependencyOrderRule[SortOrders=%b]", listToString(SortOrders));
    }

    private String listToString(List<SortOrder> list) {
        return "[" + list.stream().map(SortOrder::toString).collect(Collectors.joining(",")) + "]";
    }
}
