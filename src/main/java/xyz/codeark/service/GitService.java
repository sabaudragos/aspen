package xyz.codeark.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
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


    /**
     * Creates a stash of the changes found in the
     *
     * @param git   the git repository
     * @return      a commit reference
     */
    RevCommit createStash(Git git);

    /**
     * Checks if the repository is up to date
     *
     * @param repositoryPath    the path to the repository
     * @return                  true if the repository is up to date, false otherwise
     */
    boolean isUpToDate(String repositoryPath);
}
