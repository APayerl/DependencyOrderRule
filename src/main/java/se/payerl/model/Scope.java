package se.payerl.model;

/**
 * Enum representing Maven dependency scopes.
 * 
 * This enum contains all standard Maven scopes and can be used
 * for type-safe configuration of dependency ordering rules.
 */
public enum Scope {
    /**
     * Compile scope - standard scope that is included in all phases.
     * Dependencies are available in all classpaths.
     */
    COMPILE("compile"),
    
    /**
     * Provided scope - available during compilation but not at runtime.
     * Expected to be provided by the runtime environment (e.g. servlet API).
     */
    PROVIDED("provided"),
    
    /**
     * Runtime scope - not necessary for compilation but required at runtime.
     * Included in runtime and test classpaths but not compile classpath.
     */
    RUNTIME("runtime"),
    
    /**
     * Test scope - only available for test compilation and test execution.
     * Not included in final artifact.
     */
    TEST("test"),
    
    /**
     * System scope - similar to provided but JAR must be provided explicitly.
     * Dependency should be available on the system and must specify systemPath.
     */
    SYSTEM("system"),
    
    /**
     * Import scope - only for pom dependencies in dependencyManagement.
     * Imports managed dependencies from another POM.
     */
    IMPORT("import");

    private final String value;

    Scope(String value) {
        this.value = value;
    }

    /**
     * Returns the string representation of the scope.
     *
     * @return Scope value as string (e.g. "compile", "provided")
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Converts a Maven scope string to Scope enum.
     * Handles both uppercase and lowercase letters.
     * 
     * @param scopeValue Maven scope value as string (e.g. "compile", "COMPILE", "test")
     * @return Corresponding Scope enum, or COMPILE if value is null or unknown
     */
    public static Scope fromValue(String scopeValue) {
        if (scopeValue == null) {
            return COMPILE; // Default scope
        }
        
        // Map to main (non-deprecated) enum constants based on value
        String normalizedValue = scopeValue.toLowerCase();
        switch (normalizedValue) {
            case "compile":
                return COMPILE;
            case "provided":
                return PROVIDED;
            case "runtime":
                return RUNTIME;
            case "test":
                return TEST;
            case "system":
                return SYSTEM;
            case "import":
                return IMPORT;
            default:
                // If scope is not recognized, return COMPILE as default
                return COMPILE;
        }
    }
    
    @Override
    public String toString() {
        return value;
    }
} 