package xyz.codeark.rest;

import org.springframework.stereotype.Component;
import xyz.codeark.service.GitService;

import javax.annotation.Resource;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@Path(RestConstants.GIT)
public class GitResource {
    @Resource
    private GitService gitService;

    @POST
    public Response gitPull(
            @DefaultValue("true") @QueryParam("rebase") Boolean rebase,
            String repositoryPath) {

        if (Files.notExists(Paths.get(repositoryPath))) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestConstants.INVALID_PATH)
                    .build();
        }

        gitService.pull(repositoryPath, rebase);

        return Response.ok().build();
    }
}
