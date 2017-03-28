package xyz.codeark.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.codeark.dto.Directory;
import xyz.codeark.rest.exceptions.AspenRestException;
import xyz.codeark.service.DiscoveryServiceImpl;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Path(RestConstants.DISCOVERY)
public class DirectoryDiscoveryResource {

    @Resource
    private DiscoveryServiceImpl discoveryServiceImpl;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRepositoriesAndMvnModules(
            @DefaultValue("1") @QueryParam("maxDirectoryDepth") int maxDirectoryDepth,
            @NotNull @QueryParam("directoryToSearch") String directoryToSearch
    ) {

        log.debug("Discovering directories in {}", directoryToSearch);
        if (Files.notExists(Paths.get(directoryToSearch))) {
            throw new AspenRestException(RestConstants.INVALID_PATH, Response.Status.BAD_REQUEST);
        }

        Map<String, List<Directory>> repositoriesAndModules =
                discoveryServiceImpl.discoverRepositoriesAndMvnModules(directoryToSearch, maxDirectoryDepth);

        return Response.ok()
                .entity(repositoriesAndModules)
                .build();
    }
}
