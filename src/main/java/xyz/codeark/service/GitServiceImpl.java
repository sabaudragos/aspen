package xyz.codeark.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
public class GitServiceImpl implements GitService {

    @Override
    public boolean pull(String repositoryPath, Boolean userRebase) {
        log.info("Updating repository: ", repositoryPath);

        Git git = getGitInstance(repositoryPath);
        PullResult pullResult = null;

        try {
            Status status = git.status().call();
            RevCommit stash = null;

            if (status.hasUncommittedChanges()) {
                stash = createStash(git);
            }

            pullResult = git.pull().setRebase(true).call();

            if (stash != null){
                log.info("Applying stash");
                ObjectId appliedStashId = git.stashApply().setStashRef(stash.getName()).call();
                log.info("Stash with ID {} was applied successfully", appliedStashId);
            }
        } catch (GitAPIException e) {
            log.error("Error while updating the repository", e);
            e.printStackTrace();
        }

        // should both the rebase and fetch results be treated?
        if (pullResult.getRebaseResult().getStatus().isSuccessful()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public RevCommit createStash(Git git) {
        log.info("Git repo {} has uncommitted changes. Creating a stash");
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
    public boolean isUpToDate(String repositoryPath) {
        log.info("Checking if repository \'{}\' is up to date", repositoryPath);
        Git git = getGitInstance(repositoryPath);
        try {
            FetchResult fetchResult = git.fetch().call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Git getGitInstance(String repositoryPath) {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = null;
        try {
            repository = builder.setGitDir(new File(repositoryPath))
                    .readEnvironment() // scan environment GIT_* variables
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Git(repository);
    }
}
