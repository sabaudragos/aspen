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
     * Creates a stash of the changes found in the
     *
     * @param git the git repository
     * @return a commit reference
     */
    RevCommit createStash(Git git);

    /**
     * Checks if the repository is up to date
     *
     * @param gitRepository git repository dto
     * @return the status of the repository (is up to date, behind or ahead origin)
     */
    String isUpToDate(GitRepository gitRepository);
}
