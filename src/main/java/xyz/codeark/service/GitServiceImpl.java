package xyz.codeark.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
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

        PullResult pullResult = null;
        try (Repository repository = getJGitRepository(gitRepository.getPath())) {
            if (CollectionUtils.isEmpty(repository.getRemoteNames())) {
                log.error(RestConstants.GIT_REPOSITORY_NO_REMOTE_ORIGIN_FOUND_IN_THE_LOCAL_CONFIG);
                throw new AspenRestException(
                        RestConstants.GIT_REPOSITORY_NO_REMOTE_ORIGIN_FOUND_IN_THE_LOCAL_CONFIG,
                        Response.Status.ACCEPTED,
                        gitRepository.getPath(),
                        gitRepository.getName());
            }

            try (Git git = new Git(repository)) {
                Status status = git.status().call();
                RevCommit stash = null;

                if (status.hasUncommittedChanges()) {
                    stash = createStash(git, gitRepository.getPath(), gitRepository.getName());
                }

                pullResult = git.pull().setRebase(userRebase).call();

                //TODO: should stash be applied even when an exception happens
                if (stash != null) {
                    log.info("Applying stash");
                    ObjectId appliedStashId = git.stashApply().setStashRef(stash.getName()).call();
                    log.info("Stash with ID {} was applied successfully", appliedStashId);
                }
            } catch (GitAPIException e) {
                log.error(RestConstants.GIT_ERROR_WHILE_UPDATING_REPOSITORY, e);
                throw new AspenRestException(
                        RestConstants.GIT_ERROR_WHILE_UPDATING_REPOSITORY,
                        Response.Status.ACCEPTED,
                        gitRepository.getPath(),
                        gitRepository.getName());
            }
        } catch (IOException e) {
            log.error(RestConstants.ERROR_BUILDING_GIT_INSTANCE, e);
            throw new AspenRestException(
                    RestConstants.ERROR_BUILDING_GIT_INSTANCE,
                    Response.Status.ACCEPTED,
                    gitRepository.getPath(),
                    gitRepository.getName());
        }

        // should both the rebase and fetch results be treated?
//        pullResult.getRebaseResult().getStatus().equals(RebaseResult.Status.UP_TO_DATE);

        if (pullResult.getRebaseResult().getStatus().isSuccessful()) {
            gitRepository.setStatus(RestConstants.GIT_PULL_SUCCESS);
        } else {
            gitRepository.setStatus(RestConstants.GIT_PULL_FAILED);
        }

        return gitRepository;
    }

    @Override
    public RevCommit createStash(Git git, String path, String name) {
        log.info("Git repo {} has uncommitted changes. Creating a stash", git.getRepository());
        RevCommit revCommit = null;
        try {
            revCommit = git.stashCreate().call();
        } catch (GitAPIException e) {
            log.error("Error while stashing the changes of {}", git.getRepository());
            throw new AspenRestException(
                    RestConstants.ERROR_WHILE_STASHING_CHANGES,
                    Response.Status.ACCEPTED,
                    path,
                    name);
        }

        log.info("Uncommitted changes were successfully stashed");

        return revCommit;
    }

    @Override
    public GitRepository checkRepositoryStatus(String repositoryPath) {
        log.info("Checking if repository \'{}\' is up to date", repositoryPath);

        String repositoryName = repositoryPath.substring(repositoryPath.lastIndexOf('/') + 1);
        GitRepository gitRepository = new GitRepository();
        gitRepository.setPath(repositoryPath);
        gitRepository.setName(repositoryName);

        try (Repository repository = getJGitRepository(repositoryPath)) {
            if (CollectionUtils.isEmpty(repository.getRemoteNames())) {
                log.debug(String.format(RestConstants.GIT_REPOSITORY_NO_REMOTE_ORIGIN_FOUND_IN_THE_LOCAL_CONFIG + " Repository name: %s", repositoryName));
                throw new AspenRestException(
                        RestConstants.GIT_REPOSITORY_NO_REMOTE_ORIGIN_FOUND_IN_THE_LOCAL_CONFIG,
                        Response.Status.ACCEPTED,
                        repositoryPath,
                        repositoryName);
            }
            fetchBranches(repository, repositoryPath, repositoryName);

            BranchTrackingStatus branchTrackingStatus =
                    BranchTrackingStatus.of(repository, repository.getFullBranch());

            if (branchTrackingStatus == null) {
                gitRepository.setStatus(RestConstants.GIT_NO_REMOTE_TRACKING_OF_BRANCH);
                log.debug(RestConstants.GIT_NO_REMOTE_TRACKING_OF_BRANCH);

                return gitRepository;
            }

            if (branchTrackingStatus.getBehindCount() > 0) {
                // local branch is outdated, is behind origin by branchTrackingStatus.getBehindCount()
                gitRepository.setStatus(RestConstants.GIT_REPOSITORY_IS_BEHIND_OF_ORIGIN);
                log.debug(RestConstants.GIT_REPOSITORY_IS_BEHIND_OF_ORIGIN + ": " + repositoryName);

                return gitRepository;
            }

            if (branchTrackingStatus.getAheadCount() > 0) {
                // local branch NOT outdated, is ahead origin by branchTrackingStatus.getAheadCount()
                gitRepository.setStatus(RestConstants.GIT_REPOSITORY_IS_AHEAD_OF_ORIGIN);
                log.debug(RestConstants.GIT_REPOSITORY_IS_AHEAD_OF_ORIGIN + ": " + repositoryName);

                return gitRepository;
            }

        } catch (IOException e) {
            log.error("Error while building a git instance", e);
            throw new AspenRestException(
                    RestConstants.ERROR_BUILDING_GIT_INSTANCE,
                    Response.Status.ACCEPTED,
                    repositoryPath,
                    repositoryName);
        }

        log.debug(RestConstants.GIT_REPOSITORY_IS_UP_TO_DATE + ": " + repositoryName);
        gitRepository.setStatus(RestConstants.GIT_REPOSITORY_IS_UP_TO_DATE);

        return gitRepository;
    }

    private void fetchBranches(Repository repository, String repositoryPath, String repositoryName) {
        log.info(String.format("Fetching branches for repository: %s", repositoryName));
        try (Git git = new Git(repository)) {
            git.fetch().call();
        } catch (InvalidRemoteException e) {
            log.error(RestConstants.ERROR_FETCHING_INVALID_REMOTE + " Repository name: " + repositoryName, e);
            throw new AspenRestException(
                    RestConstants.ERROR_FETCHING_INVALID_REMOTE,
                    Response.Status.ACCEPTED,
                    repositoryPath,
                    repositoryName);
        } catch (TransportException e) {
            log.error(RestConstants.ERROR_FETCHING_TRANSPORT_FAILED + " Repository name: " + repositoryName, e);
            throw new AspenRestException(
                    RestConstants.ERROR_FETCHING_TRANSPORT_FAILED,
                    Response.Status.ACCEPTED,
                    repositoryPath,
                    repositoryName);
        } catch (GitAPIException e) {
            log.error(RestConstants.ERROR_FETCHING_GITAPI_EXCEPTION + " Repository name: " + repositoryName, e);
            throw new AspenRestException(
                    RestConstants.ERROR_FETCHING_GITAPI_EXCEPTION,
                    Response.Status.ACCEPTED,
                    repositoryPath,
                    repositoryName);
        }
    }

    private Repository getJGitRepository(String repositoryPath) throws IOException {
        // the .git directory inside the repository
        Path gitRepositoryConfigPath = Paths.get(repositoryPath, ".git");

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder
                .setGitDir(gitRepositoryConfigPath.toFile())
                .readEnvironment() // scan environment GIT_* variables
                .build();
    }
}
