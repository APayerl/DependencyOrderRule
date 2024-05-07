package se.payerl;

public enum Scope {
    TEST("test"),
    RUNTIME("runtime"),
    IMPORT("import"),
    SYSTEM("system"),
    PROVIDED("provided"),
    COMPILE("compile");

    public final String value;

    Scope(String value) {
        this.value = value;
    }

    public static Scope fromString(String value) {
        for (Scope scope : Scope.values()) {
            if (scope.value.equalsIgnoreCase(value)) {
                return scope;
            }
        }
        throw new IllegalArgumentException("No enum constant " + Scope.class.getCanonicalName() + "." + value);
    }
}
