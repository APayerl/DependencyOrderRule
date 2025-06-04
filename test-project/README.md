# Testing DependencyOrderRule

This is a test project for testing the DependencyOrderRule Maven Enforcer Plugin.

## Files

### Basic tests
- `pom.xml` - Contains dependencies in **wrong order** (should fail)
- `pom-simple.xml` - Simple alphabetical sorting (should succeed)
- `pom-wrong.xml` - Wrong order according to AlphabeticalOrder (should fail)
- `pom-correct.xml` - Correct order according to multiple rules (should succeed)

### Hierarchical tests  
- `pom-hierarchical.xml` - Hierarchical sorting with compile->test grouping (should succeed)
- `pom-multi-scope.xml` - Multi-scope hierarchical sorting with compile->provided->test (should succeed)
- `pom-type-safe.xml` - Type-safe scope configuration with COMPILE->RUNTIME->PROVIDED->TEST (should succeed)

## How to test

### 1. Build the main project first (IMPORTANT!)
```bash
cd ..
mvn clean install
```
**NOTE:** This installs version `1.0.0-SNAPSHOT` in your local Maven repository so that the test project can find it.

### 2. Test different configurations

#### Simple alphabetical sorting (should succeed)
```bash
cd test-project
mvn validate -f pom-simple.xml
```

#### Wrong order (should fail)
```bash
mvn validate -f pom-wrong.xml
```

#### Hierarchical sorting with compile->test grouping (should succeed)
```bash
mvn validate -f pom-hierarchical.xml
```

#### Multi-scope hierarchical sorting (should succeed)
```bash
mvn validate -f pom-multi-scope.xml
```

#### Type-safe scope configuration (should succeed)
```bash
mvn validate -f pom-type-safe.xml
```

### 3. Test with correct multi-rule order (should succeed)
```bash
mvn validate -f pom-correct.xml
```

## Expected results

### With wrong order (pom-wrong.xml)
The plugin should fail and show which dependencies are in wrong order:
```
[ERROR] Rule 0: se.payerl.DependencyOrderRule failed with message:
<dependencies> dependencies are not in correct order:
Dependency org.apache.commons:commons-lang3 must be before com.google.guava:guava
```

### With hierarchical sorting (pom-hierarchical.xml)
The plugin should run without errors and show:
```
[INFO] Hierarchical sorting - Grouping by: Checking for compile before test  
[INFO] BUILD SUCCESS
```

### With multi-scope hierarchical sorting (pom-multi-scope.xml)
The plugin should run without errors and show:
```
[INFO] Hierarchical sorting - Grouping by: Checking for compile before provided before test
[INFO] BUILD SUCCESS
```

### With correct order  
The plugin should run without errors and show:
```
[INFO] BUILD SUCCESS
```

## Technical information

The test project uses version `1.0.0-SNAPSHOT` of DependencyOrderRule, which means it fetches the local development version from your `~/.m2/repository` instead of trying to download from Nexus.

### Tested features

- **Simple sorting**: AlphabeticalOrder, ScopeOrder, OptionalOrder
- **Hierarchical sorting**: Grouping with first rule, applying remaining rules within groups
- **Multi-scope configuration**: ScopeOrder with multiple `<then>` tags
- **Type-safe scopes**: Both lowercase and uppercase scope values
- **Error handling**: Correct error messages for dependencies in wrong order 