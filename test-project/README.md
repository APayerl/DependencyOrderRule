# Test av DependencyOrderRule

Detta är ett testprojekt för att testa DependencyOrderRule Maven Enforcer Plugin.

## Filer

- `pom.xml` - Innehåller dependencies i **fel ordning** (ska ge fel)
- `pom-correct.xml` - Innehåller dependencies i **korrekt ordning** (ska lyckas)

## Hur man testar

### 1. Bygg huvudprojektet först (VIKTIGT!)
```bash
cd ..
mvn clean install
```
**OBS:** Detta installerar version `1.0.0-SNAPSHOT` i din lokala Maven repository så att testprojektet kan hitta den.

### 2. Testa med fel ordning (ska misslyckas)
```bash
cd test-project
mvn enforcer:enforce
```

### 3. Testa med korrekt ordning (ska lyckas)
```bash
cp pom-correct.xml pom.xml
mvn enforcer:enforce
```

## Förväntade resultat

### Med fel ordning
Plugin:en ska ge fel och visa vilka dependencies som är i fel ordning:
```
[ERROR] Rule 0: se.payerl.DependencyOrderRule failed with message:
<dependencies> dependencies are not in correct order:
Dependency com.google.guava:guava must be before org.apache.commons:commons-lang3
Dependency org.apache.commons:commons-lang3 must be before junit:junit scope:test
```

### Med korrekt ordning  
Plugin:en ska köra utan fel och visa:
```
[INFO] BUILD SUCCESS
```

## Teknisk information

Testprojektet använder version `1.0.0-SNAPSHOT` av DependencyOrderRule, vilket betyder att den hämtar den lokala utvecklingsversionen från din `~/.m2/repository` istället för att försöka ladda ner från Nexus. 