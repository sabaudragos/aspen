package xyz.codeark.maven;

/**
 * The maven lifecycle phases
 */
public enum MavenPhase {
    VALIDATE("validate"),
    COMPILE("compile"),
    TEST("test"),
    PACKAGE("package"),
    VERIFY("verify"),
    INSTALL("install"),
    DEPLOY("deploy");

    private String phase;

    MavenPhase(String phase) {
        this.phase = phase;
    }

    @Override
    public String toString() {
        return phase;
    }

}
