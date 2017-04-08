package xyz.codeark.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.codeark.dto.Directory;
import xyz.codeark.dto.GitRepository;
import xyz.codeark.dto.MavenModule;
import xyz.codeark.rest.RestConstants;
import xyz.codeark.rest.exceptions.AspenRestException;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
public class DiscoveryServiceImpl implements DiscoveryService {
    private final String POM_FILE = "pom.xml";
    private final String GIT_DIRECTORY = ".git";

    @Override
    public Map<String, Set<Directory>> discoverRepositoriesAndMvnModules(String rootDirectory,
                                                                          int maxDirectoryDepth) {
        log.info("Discovering mvn modules and git repositories in {}, max depth {}", rootDirectory, maxDirectoryDepth);
        Set<Directory> mavenModuleList = new TreeSet<>();
        Set<Directory> repositoryList = new TreeSet<>();
        //TODO performance might be improved if the .git directory is skipped
        try (Stream<Path> paths = Files.walk(Paths.get(rootDirectory), maxDirectoryDepth)) {
            paths.forEach(filePath -> {
                if (Files.isDirectory(filePath)) {
                    if (Files.exists(filePath.resolve(POM_FILE))) {
                        MavenModule mavenModule = new MavenModule();
                        mavenModule.setPath(filePath.toString());
                        mavenModule.setName(filePath.getFileName().toString());
                        mavenModuleList.add(mavenModule);
                    }
                    if (Files.exists(filePath.resolve(GIT_DIRECTORY))) {
                        GitRepository gitRepository = new GitRepository();
                        gitRepository.setPath(filePath.toString());
                        gitRepository.setName(filePath.getFileName().toString());
                        repositoryList.add(gitRepository);
                    }
                }
            });
        } catch (IOException e) {
            log.error("There was an error while discovering the directories. {}",
                    Arrays.toString(e.getStackTrace()));
            throw new AspenRestException(RestConstants.DIRECTORY_DISCOVERY_FAILURE,
                    Response.Status.ACCEPTED);
        }

        Map<String, Set<Directory>> repositoriesAndMvnModules = new HashMap<>();
        repositoriesAndMvnModules.put(DiscoveryConstants.MAVEN_MODULE, mavenModuleList);
        repositoriesAndMvnModules.put(DiscoveryConstants.GIT_REPO, repositoryList);

        log.info("Directory discovery completed successfully");
        return repositoriesAndMvnModules;
    }
}
