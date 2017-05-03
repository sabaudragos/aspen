package xyz.codeark.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.codeark.dto.GitRepository;
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
            GitRepository gitRepository) {

        if ((StringUtils.isEmpty(gitRepository.getPath())) || (Files.notExists(Paths.get(gitRepository.getPath())))) {
            log.error("Invalid git repository path {}", gitRepository);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestConstants.INVALID_GIT_REPOSITORY_PATH)
                    .build();
        }

        return Response.status(Response.Status.OK)
                .entity(gitService.pull(gitRepository, rebase))
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkIfRepositoryIsUpToDate(
            @QueryParam("repositoryPath") String repositoryPath) {

        if ((StringUtils.isEmpty(repositoryPath)) || (Files.notExists(Paths.get(repositoryPath)))) {
            log.error("Invalid git repository path {}", repositoryPath);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestConstants.INVALID_GIT_REPOSITORY_PATH)
                    .build();
        }

        GitRepository gitRepository = gitService.checkRepositoryStatus(repositoryPath);

        if (gitRepository.getStatus().equals(RestConstants.GIT_NO_REMOTE_TRACKING_OF_BRANCH)){
            Response.status(Response.Status.ACCEPTED)
                    .entity(gitRepository)
                    .build();
        }

        return Response.status(Response.Status.OK)
                .entity(gitRepository)
                .build();
    }
}
