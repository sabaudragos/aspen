package xyz.codeark.service;

import org.springframework.stereotype.Service;
import xyz.codeark.dto.MavenModule;
import xyz.codeark.maven.MavenLifeCycle;
import xyz.codeark.maven.MavenPhase;

/**
 * A Maven operations service class
 */
@Service
public interface MavenService {

    /**
     * It builds the maven module. It can include 'clean'.
     *
     * @param mavenLifeCycle added for the clean option
     * @param mavenPhase     one of the 7 maven phases
     * @param skipTests      true if tests should be skipped
     * @param mavenModule    dto for the maven module
     * @return true if the execution was successful, false otherwise
     */
    MavenModule build(MavenLifeCycle mavenLifeCycle,
                      MavenPhase mavenPhase,
                      Boolean skipTests,
                      MavenModule mavenModule);
}
