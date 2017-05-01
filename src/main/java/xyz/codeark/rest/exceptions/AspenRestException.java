package xyz.codeark.rest.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Custom REST exception
 */
public class AspenRestException extends WebApplicationException {

    private String path;
    private String name;

    /**
     * Creates a custom REST exception to deal with unexpected errors
     *
     * @param message Exception message
     * @param status  HTTP Response status
     */
    public AspenRestException(String message, Response.Status status) {
        super(message, status);
    }

    /**
     * Creates a custom REST exception to deal with unexpected errors
     *
     * @param message Exception message
     * @param status  HTTP Response status
     * @param name    Name of the git repository or maven module
     * @param path    Path of the git repository or maven module
     */
    public AspenRestException(String message, Response.Status status, String path, String name) {
        super(message, status);
        this.path = path;
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }
}
