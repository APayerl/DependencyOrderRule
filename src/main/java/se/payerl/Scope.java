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
}
