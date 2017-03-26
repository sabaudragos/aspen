package xyz.codeark.service;

import org.springframework.stereotype.Service;

/**
 * A Git operations service class
 */
@Service
public interface GitService {
    /**
     * Pulls data from origin. It does a 'git pull'
     *
     * @param repositoryPath the path to repository
     * @param userRebase     true if --rebase should be used
     */
    boolean pull(String repositoryPath, Boolean userRebase);
}
