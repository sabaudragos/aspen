package xyz.codeark.rest;

import org.springframework.stereotype.Component;
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

@Component
@Path(RestConstants.MAVEN)
public class MavenResource {

    @Resource
    private MavenService mavenService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response mvnBuild(
            @DefaultValue("CLEAN") @QueryParam("lifecycle") MavenLifeCycle mavenLifeCycle,
            @DefaultValue("INSTALL") @NotNull @QueryParam("mavenphase") MavenPhase mavenPhase,
            @DefaultValue("true") @QueryParam("skiptests") Boolean skipTests,
            @FormParam("mvnModulePath") String mvnModulePath) {

        if (Files.notExists(Paths.get(mvnModulePath))) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(RestConstants.INVALID_PATH)
                    .build();
        }

        mavenService.install(mavenLifeCycle, mavenPhase, skipTests, mvnModulePath);

        return Response.ok()
                .entity(RestConstants.MAVEN_SUCCESS)
                .build();
    }
}
// 3 basic green plants
// mother in law tongs plant (sansevieria trifasciata) - bedroom plant -  6-8 per person (weist heigh)
// areca palm (chrysalidocarpus lutescens) - the living room plant - 3 per person shoulder heigh
// money plant (epipremnum aurem) - corn de iedera, potos sau iedera diavolului -
