package xyz.codeark.dto;

/**
 * Holds details related to a repository.
 */
public class GitRepository extends Directory {
    protected String status;

    @Override
    public String toString() {
        return "Directory{" +
                "path='" + path + '\'' +
                "name='" + name + '\'' +
                "status='" + status + '\'' +
                '}';
    }
}
