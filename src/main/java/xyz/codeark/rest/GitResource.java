package xyz.codeark.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.codeark.service.GitService;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Component
@Path(RestConstants.GIT)
public class GitResource {
    @Resource
    private GitService gitService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response gitPull(
            @DefaultValue("true") @QueryParam("rebase") Boolean rebase,
            String repositoryPath) {

        if ((StringUtils.isEmpty(repositoryPath)) || (Files.notExists(Paths.get(repositoryPath)))) {
            log.error("Invalid git repository path {}", repositoryPath);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestConstants.INVALID_GIT_REPOSITORY_PATH)
                    .build();
        }

        gitService.isUpToDate(repositoryPath);

        if (gitService.pull(repositoryPath, rebase)) {
            return Response.status(Response.Status.OK)
                    .entity(RestConstants.GIT_SUCCESS)
                    .build();
        } else {
            return Response.status(Response.Status.OK)
                    .entity(RestConstants.GIT_PULL_FAILED)
                    .build();
        }
    }
}
