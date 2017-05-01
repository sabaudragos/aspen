package xyz.codeark.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xyz.codeark.dto.MavenModule;
import xyz.codeark.maven.MavenLifeCycle;
import xyz.codeark.maven.MavenPhase;
import xyz.codeark.service.MavenService;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Component
@Path(RestConstants.MAVEN)
public class MavenResource {

    @Resource
    private MavenService mavenService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response mvnBuild(
            @DefaultValue("CLEAN") @QueryParam("lifecycle") MavenLifeCycle mavenLifeCycle,
            @DefaultValue("INSTALL") @NotNull @QueryParam("mavenphase") MavenPhase mavenPhase,
            @DefaultValue("true") @QueryParam("skiptests") Boolean skipTests,
            MavenModule mavenModule) {

        if ((StringUtils.isEmpty(mavenModule.getPath())) || (Files.notExists(Paths.get(mavenModule.getPath())))) {
            log.error("Invalid module path {}", mavenModule.getPath());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestConstants.INVALID_MVN_MODULE_PATH)
                    .build();
        }

        return Response.ok()
                .entity(mavenService.build(mavenLifeCycle, mavenPhase, skipTests, mavenModule))
                .build();
    }
}
