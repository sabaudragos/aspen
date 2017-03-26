package xyz.codeark.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Domain class for a single file system directory
 */
@Getter
@Setter
public class Directory {
    private String path;
    private String name;

    @Override
    public String toString() {
        return "Directory{" +
                "name='" + name + '\'' +
                '}';
    }
}
