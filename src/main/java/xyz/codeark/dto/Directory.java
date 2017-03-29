package xyz.codeark.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO class for a single file system directory
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Directory implements Comparable<Directory>{
    private String path;
    private String name;

    @Override
    public String toString() {
        return "Directory{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public int compareTo(Directory o) {
        return this.name.compareTo(o.name);
    }
}
