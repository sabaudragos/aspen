package xyz.codeark.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Domain class for a single file system directory
 */
@Getter
@Setter
public class Directory {
    protected String path;
    protected String name;
    protected String status;

    @Override
    public String toString() {
        return "Directory{" +
                "name='" + name + '\'' +
                '}';
    }
}
