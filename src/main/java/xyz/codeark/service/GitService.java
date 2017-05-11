package xyz.codeark.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Service;
import xyz.codeark.dto.GitRepository;

/**
 * A Git operations service class
 */
@Service
public interface GitService {
    /**
     * Pulls data from origin. It does a 'git pull'
     *
     * @param gitRepository git repository dto
     * @param userRebase    true if --rebase should be used
     */
    GitRepository pull(GitRepository gitRepository, Boolean userRebase);

    /**
     * Creates a stash of the changes found in the repository
     *
     * @param git   the git repository
     * @param path  the git repository's path
     * @param name  the git repository's name
     * @return a commit reference
     */
    RevCommit createStash(Git git, String path, String name);

    /**
     * Checks if the repository is up to date
     *
     * @param repositoryPath    the git repository path
     * @param username          the user name used to access the repository
     * @param password          the user password used to access the repository
     * @return the dto for the repository (name, path and status of the repository (is up to date, behind or ahead origin))
     */
    GitRepository checkRepositoryStatus(String repositoryPath, String username, String password);
}
