# DependencyOrderRule

This is a custom rule for Maven Enforcer Plugin. The rule checks the order of dependencies and dependencyManagement dependencies in your Maven project 
and throws an exception if they are not in correct order.

- [Prerequisites](#prerequisites)
- [How to use](#how-to-use)
- [Available SortOrders](#available-sortorders)
- [Hierarchical sorting](#hierarchical-sorting)
- [Examples](#examples)
- [License](#license)

## Prerequisites
 - Java 8 or later
 - Maven 3.2.5 or later

## How to use
1. In your pom.xml, add maven-enforcer-plugin and configure it to use DependencyOrderRule.
2. Configure DependencyOrderRule by adding SortOrder elements to the rule configuration. 
3. Run your Maven build:
   - Run `mvn validate` or other Maven commands (enforcer runs in the validate phase by default)

If dependencies are not in correct order according to your SortOrder configuration, the build will fail with an EnforcerRuleException.

## Configuration syntax

Configure the rule using the simple syntax:

```xml
<rules>
    <DependencyOrderRule>
        <SortOrders>
            <AlphabeticalOrder>
                <inversed>false</inversed>
            </AlphabeticalOrder>
        </SortOrders>
    </DependencyOrderRule>
</rules>
```

### Available SortOrders

- **ScopeOrder**: Used to sort based on scope tags.
  - Takes `<first>` and `<then>` tags for simple configuration
  - Supports multi-scope configuration with multiple `<then>` tags
  - First tag should be the scope that should come first, then-tags should be scopes that should come after
  - All should be valid Maven dependency scopes
  - Examples: `compile`, `provided`, `runtime`, `test`, `system`, `import`

- **OptionalOrder**: Used to sort based on the optional tag.
  - Takes `<first>` and `<then>` tags
  - First tag should be the optional value that should come first, then-tag should be the optional value that should come after
  - Both should be valid values for the optional tag in Maven (`true` or `false`)

- **AlphabeticalOrder**: Used to sort based on groupId:artifactId tags.
  - Takes an `<inversed>` tag
  - If the inversed tag is set to true, alphabetical order will be reversed

### Hierarchical sorting

DependencyOrderRule supports hierarchical sorting where dependencies are first grouped according to the first rule, 
then remaining rules are applied within each group.

To enable hierarchical sorting:
- Add `<groupMode>true</groupMode>` element to the configuration
- First SortOrder is used for grouping
- Remaining SortOrders are applied within each group

### Type-safe Scope configuration

ScopeOrder uses a type-safe Scope enum internally that provides:
- **Type safety**: Misspellings are converted to COMPILE default
- **Backward compatibility**: Old configurations still work
- **IDE support**: Autocomplete for scope values in code
- **Documentation**: All available scopes are found in the Scope enum

Available scopes: `compile`, `provided`, `runtime`, `test`, `system`, `import`

**Note**: DependencyOrderRule only checks the order of dependencies within the same pom.xml file. 
It does not check the order of transitive dependencies.

It is also worth noting that multiple rules can be used simultaneously BUT the rules may conflict.

## Examples

### Simple sorting
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <version>3.4.1</version>
    <dependencies>
        <dependency>
            <groupId>se.payerl</groupId>
            <artifactId>DependencyOrderRule</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
    <executions>
        <execution>
            <id>enforce-rules</id>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <DependencyOrderRule>
                        <SortOrders>
                            <ScopeOrder>
                                <first>compile</first>
                                <then>test</then>
                            </ScopeOrder>
                            <OptionalOrder>
                                <first>false</first>
                                <then>true</then>
                            </OptionalOrder>
                            <AlphabeticalOrder>
                                <inversed>false</inversed>
                            </AlphabeticalOrder>
                        </SortOrders>
                    </DependencyOrderRule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Hierarchical sorting with multi-scope
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    <version>3.4.1</version>
    <dependencies>
        <dependency>
            <groupId>se.payerl</groupId>
            <artifactId>DependencyOrderRule</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
    <executions>
        <execution>
            <id>enforce-hierarchical-order</id>
            <goals>
                <goal>enforce</goal>
            </goals>
            <configuration>
                <rules>
                    <DependencyOrderRule>
                        <groupMode>true</groupMode>
                        <SortOrders>
                            <ScopeOrder>
                                <first>compile</first>
                                <then>provided</then>
                                <then>test</then>
                            </ScopeOrder>
                            <AlphabeticalOrder>
                                <inversed>false</inversed>
                            </AlphabeticalOrder>
                        </SortOrders>
                    </DependencyOrderRule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
