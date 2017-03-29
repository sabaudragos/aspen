package xyz.codeark.service;

import org.springframework.stereotype.Service;
import xyz.codeark.dto.Directory;

import java.util.Map;
import java.util.Set;

/**
 * Service class used to discover the repositories and maven modules. It's desirable to
 * discover the git repositories and maven modules with a single call. Making two calls
 * will not only be less resource efficient but it could lead to perceived inconsistencies
 * (e.g. the end user will be shown an up to date maven modules set but an out of date
 * git repositories set)
 */
@Service
public interface DiscoveryService {
    /**
     * It will walk through the directory and search for repositories
     * and maven modules.
     *
     * @param directoryToSearch The directory where the method will look for
     *                          git repositories and maven modules
     * @param maxDirectoryDepth The maximum depth the search will go. E.g. 1 - means
     *                          that it will search the contents of the directoryToSearch,
     *                          it will not search the subdirectories
     * @return a map of directories (repositories and maven modules)
     */
    Map<String, Set<Directory>> discoverRepositoriesAndMvnModules(String directoryToSearch,
                                                                  int maxDirectoryDepth);
}
