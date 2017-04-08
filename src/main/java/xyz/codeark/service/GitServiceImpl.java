package xyz.codeark.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.stereotype.Service;
import xyz.codeark.dto.GitRepository;
import xyz.codeark.rest.RestConstants;
import xyz.codeark.rest.exceptions.AspenRestException;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class GitServiceImpl implements GitService {

    @Override
    public GitRepository pull(GitRepository gitRepository, Boolean userRebase) {
        log.info("Updating repository: {}", gitRepository.getName());

        Git git = getGitInstance(gitRepository.getPath());
        PullResult pullResult = null;

        try {
            Status status = git.status().call();
            RevCommit stash = null;

            if (status.hasUncommittedChanges()) {
                stash = createStash(git);
            }

            pullResult = git.pull().setRebase(userRebase).call();

            if (stash != null) {
                log.info("Applying stash");
                ObjectId appliedStashId = git.stashApply().setStashRef(stash.getName()).call();
                log.info("Stash with ID {} was applied successfully", appliedStashId);
            }
        } catch (GitAPIException e) {
            log.error("Error while updating the repository", e);
            throw new AspenRestException(RestConstants.GIT_PULL_FAILED, Response.Status.ACCEPTED);
        }

        // should both the rebase and fetch results be treated?

        pullResult.getRebaseResult().getStatus().equals(RebaseResult.Status.UP_TO_DATE);

        if (pullResult.getRebaseResult().getStatus().isSuccessful()) {
            gitRepository.setStatus(RestConstants.GIT_PULL_SUCCESS);
        } else {
            gitRepository.setStatus(RestConstants.GIT_PULL_FAILED);
        }
        return gitRepository;
    }

    @Override
    public RevCommit createStash(Git git) {
        log.info("Git repo {} has uncommitted changes. Creating a stash", git.getRepository());
        RevCommit revCommit = null;
        try {
            revCommit = git.stashCreate().call();
        } catch (GitAPIException e) {
            log.error("Error while stashing the changes of {}", git.getRepository());
            e.printStackTrace();
        }

        log.info("Uncommitted changes were successfully stashed");
        return revCommit;
    }

    @Override
    public String isUpToDate(GitRepository gitRepository) {
        log.info("Checking if repository \'{}\' is up to date", gitRepository);

        Git git = getGitInstance(gitRepository.getPath());

        try {
            BranchTrackingStatus branchTrackingStatus =
                    BranchTrackingStatus.of(git.getRepository(), git.getRepository().getBranch());

            if (branchTrackingStatus.getBehindCount() > 0){
                // local branch is behind origin by branchTrackingStatus.getBehindCount()
                // local branch is outdated
                return RestConstants.GIT_REPOSITORY_IS_BEHIND_OF_ORIGIN;
            }

            if (branchTrackingStatus.getBehindCount() > 0){
                // local branch is ahead origin by branchTrackingStatus.getBehindCount()
                // local branch is not outdated
                return RestConstants.GIT_REPOSITORY_IS_AHEAD_OF_ORIGIN;
            }

        } catch (IOException e) {
            log.error("Error while checking the status of {}", gitRepository.getName());
            e.printStackTrace();
        }

        return RestConstants.GIT_REPOSITORY_IS_UP_TO_DATE;
    }

    private Git getGitInstance(String repositoryPath) {
        // the .git directory inside the repository
        Path gitRepositoryConfigPath = Paths.get(repositoryPath, ".git");
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = null;
        try {
            repository = builder.setGitDir(gitRepositoryConfigPath.toFile())
                    .readEnvironment() // scan environment GIT_* variables
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Git(repository);
    }
}
