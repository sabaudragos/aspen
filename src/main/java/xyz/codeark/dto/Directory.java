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
