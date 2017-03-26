package xyz.codeark.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.shared.invoker.*;
import org.springframework.stereotype.Service;
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
    public boolean install(MavenLifeCycle mavenLifeCycle,
                           MavenPhase mavenPhase,
                           Boolean skipTests,
                           String mvnModulePath) {

        List<String> goalList = new ArrayList<>();
        if (skipTests) {
            goalList.add(SKIP_TESTS);
        }

        if (mavenLifeCycle != null) {
            goalList.add(mavenLifeCycle.toString());
        }

        InvocationResult invocationResult =
                executeMavenCommand(goalList, mvnModulePath);

        if (invocationResult.getExitCode() == 0) {
            return true;
        } else {
            throw new AspenRestException(RestConstants.MAVEN_FAILURE + invocationResult.getExitCode(),
                    Response.Status.OK);
        }
    }

    private InvocationResult executeMavenCommand(List<String> goalList, String mvnModulePath) {
        log.info("Executing maven command for module: ", mvnModulePath);

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(mvnModulePath + "/pom.xml"));
        request.setGoals(goalList);

        Invoker invoker = new DefaultInvoker();
        // TODO should take the maven home from the env variable
        invoker.setMavenHome(new File("/usr/share/maven"));
        InvocationResult invocationResult = null;
        try {
            invocationResult = invoker.execute(request);
        } catch (MavenInvocationException e) {
            log.error("Maven operation failed.", e);
        }

        log.info("Maven command was executed successfully");
        return invocationResult;
    }
}
