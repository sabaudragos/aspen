package xyz.codeark.maven;

/**
 * The three built in Maven life cycles
 */
public enum MavenLifeCycle {
    DEFAULT("default"),
    CLEAN("clean"),
    SITE("site");

    private String lifeCycle;

    MavenLifeCycle(String name) {
        this.lifeCycle = name;
    }

    @Override
    public String toString() {
        return lifeCycle;
    }
}
