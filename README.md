# DependencyOrderRule

This is a custom rule for the Maven Enforcer Plugin. The rule checks the order of dependencies in your Maven project 
and throws an exception if they are not in the correct order.

- [Prerequisites](#prerequisites)
- [How to use](#how-to-use)
- [Available SortOrders](#available-sortorders)
- [Example](#example)

## Prerequisites
 - Java 8 or later
 - Maven 3.2.5 or later

## How to use
1. In your pom.xml, add the maven-enforcer-plugin and configure it to use the DependencyOrderRule.
2. Configure the DependencyOrderRule by adding SortOrder elements to the rule configuration. Each SortOrder element represents a pair of dependency scopes that should be ordered in a certain way. The first attribute should be the scope that should come first, and the then attribute should be the scope that should come after.
3. Run your Maven build. If the dependencies are not in the correct order according to your SortOrder configuration, the build will fail with an EnforcerRuleException.

### Available SortOrders
**ScopeOrder**: Used to sort based on the scope tag.
   
Please note that the DependencyOrderRule only checks the order of dependencies within the same pom.xml file. It does not check the order of transitive dependencies.

## Example
```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <executions>
        <execution>
            <id>enforce-rules</id>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <dependencyOrderRule implementation="se.payerl.DependencyOrderRule">
                        <SortOrders>
                            <ScopeOrder>
                                <first>compile</first>
                                <then>test</then>
                            </ScopeOrder>
                        </SortOrders>
                    </dependencyOrderRule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```