package xyz.codeark.rest.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Custom REST exception
 */
public class AspenRestException extends WebApplicationException {
    /**
     * Creates a custom REST exception to deal with unexpected errors
     *
     * @param message Exception message
     * @param status  HTTP Response status
     */
    public AspenRestException(String message, Response.Status status) {
        super(message, status);
    }
}
