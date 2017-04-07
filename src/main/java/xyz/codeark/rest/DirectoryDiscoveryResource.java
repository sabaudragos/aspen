package xyz.codeark.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.codeark.dto.Directory;
import xyz.codeark.service.DiscoveryServiceImpl;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

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
        if ((StringUtils.isEmpty(directoryToSearch)) || Files.notExists(Paths.get(directoryToSearch))) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestConstants.INVALID_PATH)
                    .build();
        }

        Map<String, Set<Directory>> repositoriesAndModules =
                discoveryServiceImpl.discoverRepositoriesAndMvnModules(directoryToSearch, maxDirectoryDepth);

        return Response.ok()
                .entity(repositoriesAndModules)
                .build();
    }
}
