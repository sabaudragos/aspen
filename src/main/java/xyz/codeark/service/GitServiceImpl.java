package xyz.codeark.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.stereotype.Service;
import xyz.codeark.rest.exceptions.AspenRestException;

import javax.ws.rs.core.Response;
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
            pullResult = git.pull().setRebase(true).call();
        } catch (GitAPIException e) {
            log.error("Error while updating the repository", e);
            e.printStackTrace();
        }

        // should both the rebase and fetch results be treated?
        if (pullResult.getRebaseResult().getStatus().isSuccessful()) {
            return true;
        } else {
            throw new AspenRestException("Git rebase failed." + pullResult.getRebaseResult().getStatus().name(),
                    Response.Status.OK);
        }
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
