package xyz.codeark.rest.exceptions;


import xyz.codeark.dto.Directory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Exception mapper for the AspenRestException. Designed to add a JSON entity to the response.
 */
@Provider
public class AspenRestExceptionMapper implements ExceptionMapper<AspenRestException> {

    @Override
    public Response toResponse(AspenRestException aspenRestException) {
        Directory directory = new Directory();
        directory.setName(aspenRestException.getName());
        directory.setPath(aspenRestException.getPath());
        directory.setStatus(aspenRestException.getMessage());

        return Response.status(aspenRestException.getResponse().getStatus())
                .entity(directory)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
