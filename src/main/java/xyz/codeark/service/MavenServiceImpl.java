package xyz.codeark.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.shared.invoker.*;
import org.springframework.stereotype.Service;
import xyz.codeark.dto.MavenModule;
import xyz.codeark.maven.MavenLifeCycle;
import xyz.codeark.maven.MavenPhase;
import xyz.codeark.rest.RestConstants;
import xyz.codeark.rest.exceptions.AspenRestException;

import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MavenServiceImpl implements MavenService {
    private static final String SKIP_TESTS = "-DskipTests";

    @Override
    public MavenModule build(MavenLifeCycle mavenLifeCycle,
                             MavenPhase mavenPhase,
                             Boolean skipTests,
                             MavenModule mavenModule) {

        List<String> goalList = new ArrayList<>();
        if (skipTests) {
            goalList.add(SKIP_TESTS);
        }

        if (mavenLifeCycle != null) {
            goalList.add(mavenLifeCycle.toString());
        }

        if (mavenPhase != null) {
            goalList.add(mavenPhase.toString());
        }

        InvocationResult invocationResult =
                executeMavenCommand(goalList, mavenModule.getPath());

        if (invocationResult.getExitCode() == 0) {
            mavenModule.setStatus(RestConstants.MAVEN_SUCCESS);
            return mavenModule;
        } else {
            mavenModule.setStatus(RestConstants.MAVEN_FAILURE);
            return mavenModule;
        }

    }

    private InvocationResult executeMavenCommand(List<String> goalList, String mvnModulePath) {
        log.info("Executing maven command for module: {}", mvnModulePath);

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(mvnModulePath + "/pom.xml"));
        request.setGoals(goalList);

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(getMvnPath()));
        InvocationResult invocationResult = null;
        try {
            invocationResult = invoker.execute(request);
        } catch (MavenInvocationException e) {
            log.error("Maven operation failed. {}", e);
            throw new AspenRestException(RestConstants.MAVEN_FAILURE, Response.Status.BAD_REQUEST);
        }

        log.info("Maven command was executed successfully");
        return invocationResult;
    }

    private String getMvnPath() {
        if (System.getProperty("os.name").contains("Linux")) {
            return extractPathVariable(System.getenv().get("PATH").split(":"));
        } else if (System.getProperty("os.name").contains("Win")) {
            return extractPathVariable(System.getenv().get("Path").split(";"));
        }

        log.error("Unsupported operating system");
        throw new AspenRestException(RestConstants.UNSUPPORTED_OPERATING_SYSTEM, Response.Status.BAD_REQUEST);
    }

    private String extractPathVariable(String[] pathVariables) {
        for (String pathVariable : pathVariables) {
            if (pathVariable.contains("maven") || pathVariable.contains("mvn")) {
                if (pathVariable.contains("bin")) {
                    return pathVariable.substring(0, pathVariable.length() - 3);
                }

                return pathVariable;
            }
        }

        log.error("Maven path not found in the Path variable");
        throw new AspenRestException(RestConstants.MVN_PATH_NOT_FOUND_IN_PATH_VARIABLE, Response.Status.BAD_REQUEST);
    }
}
