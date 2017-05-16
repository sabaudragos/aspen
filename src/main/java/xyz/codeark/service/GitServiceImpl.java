package xyz.codeark.service;

import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.*;
import org.springframework.stereotype.Service;
import xyz.codeark.dto.GitRepository;
import xyz.codeark.rest.RestConstants;
import xyz.codeark.rest.exceptions.AspenRestException;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class GitServiceImpl implements GitService {

    private UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider;

    private String sshPassphrase = "";

    @Override
    public GitRepository pull(GitRepository gitRepository, Boolean userRebase) {
        log.info("Updating repository: {}", gitRepository.getName());

        PullResult pullResult = null;
        try (Repository repository = getJGitRepository(gitRepository.getPath())) {
            if (CollectionUtils.isEmpty(repository.getRemoteNames())) {
                logAndThrow(gitRepository.getPath(), gitRepository.getName(), RestConstants.GIT_REPOSITORY_NO_REMOTE_ORIGIN_FOUND_IN_THE_LOCAL_CONFIG, null);
            }

            try (Git git = new Git(repository)) {
                Status status = git.status().call();
                RevCommit stash = null;

                if (status.hasUncommittedChanges()) {
                    stash = createStash(git, gitRepository.getPath(), gitRepository.getName());
                }

                GitTransportProtocol gitTransportProtocol = getTransportProtocol(repository, gitRepository);

                if (gitTransportProtocol.equals(GitTransportProtocol.HTTP)) {
                    pullResult = git.pull()
                            .setRebase(userRebase)
                            .setCredentialsProvider(usernamePasswordCredentialsProvider)
                            .call();
                } else if (gitTransportProtocol.equals(GitTransportProtocol.SSH)) {
                    pullResult = git.pull()
                            .setRebase(userRebase)
                            .setTransportConfigCallback(getTransportConfigCallback())
                            .call();
                }

                if (pullResult.getRebaseResult().getStatus().isSuccessful()) {
                    BranchTrackingStatus branchTrackingStatus =
                            BranchTrackingStatus.of(repository, repository.getFullBranch());

                    if ((branchTrackingStatus != null) && (branchTrackingStatus.getAheadCount() > 0)) {
                        // local branch NOT outdated, is ahead origin by branchTrackingStatus.getAheadCount()
                        gitRepository.setStatus(RestConstants.GIT_REPOSITORY_IS_AHEAD_OF_ORIGIN);
                        log.debug(RestConstants.GIT_REPOSITORY_IS_AHEAD_OF_ORIGIN + ": " + gitRepository.getName());

                        return gitRepository;
                    }

                    gitRepository.setStatus(RestConstants.GIT_PULL_SUCCESS);

                    return gitRepository;
                }

                //TODO: should stash be applied even when an exception happens
                if (stash != null) {
                    log.info("Applying stash");
                    ObjectId appliedStashId = git.stashApply().setStashRef(stash.getName()).call();
                    log.info("Stash with ID {} was applied successfully", appliedStashId);
                }
            } catch (GitAPIException e) {
                logAndThrow(gitRepository.getPath(), gitRepository.getName(), RestConstants.GIT_ERROR_WHILE_UPDATING_REPOSITORY, e);
            }
        } catch (IOException e) {
            logAndThrow(gitRepository.getPath(), gitRepository.getName(), RestConstants.ERROR_BUILDING_GIT_INSTANCE, e);
        }

        // should both the rebase and fetch results be treated?
        // pullResult.getRebaseResult().getStatus().equals(RebaseResult.Status.UP_TO_DATE);

        gitRepository.setStatus(RestConstants.GIT_PULL_FAILED);

        return gitRepository;
    }

    /**
     * Configures the ssh config session
     *
     * @return
     */
    private TransportConfigCallback getTransportConfigCallback() {
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                CredentialsProvider provider = new CredentialsProvider() {
                    @Override
                    public boolean isInteractive() {
                        return false;
                    }

                    @Override
                    public boolean supports(CredentialItem... items) {
                        return true;
                    }

                    @Override
                    public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
                        for (CredentialItem item : items) {
                            ((CredentialItem.StringType) item).setValue(sshPassphrase);
                        }
                        return true;
                    }
                };
                UserInfo userInfo = new CredentialsProviderUserInfo(session, provider);
                session.setUserInfo(userInfo);
            }
        };

        return new TransportConfigCallback() {
            @Override
            public void configure(Transport transport) {
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(sshSessionFactory);
            }
        };
    }

    @Override
    public RevCommit createStash(Git git, String path, String name) {
        log.info("Git repo {} has uncommitted changes. Creating a stash", git.getRepository());
        RevCommit revCommit = null;
        try {
            revCommit = git.stashCreate().call();
        } catch (GitAPIException e) {
            logAndThrow(path, name, RestConstants.ERROR_WHILE_STASHING_CHANGES, e);
        }

        log.info("Uncommitted changes were successfully stashed");

        return revCommit;
    }

    @Override
    public GitRepository checkRepositoryStatus(String repositoryPath, String username, String password) {
        log.info("Checking if repository \'{}\' is up to date", repositoryPath);

        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            // initialize the UsernamePasswordCredentialsProvider
            setUsernamePasswordCredentialsProvider(username, password);
        } else if (StringUtils.isNotEmpty(password)) {
            // initialize the sshPassphrase
            sshPassphrase = password;
        }

        String repositoryName = new File(repositoryPath).getName();
        GitRepository gitRepository = new GitRepository();
        gitRepository.setPath(repositoryPath);
        gitRepository.setName(repositoryName);

        try (Repository repository = getJGitRepository(repositoryPath)) {
            if (CollectionUtils.isEmpty(repository.getRemoteNames())) {
                logAndThrow(repositoryPath, repositoryName, RestConstants.GIT_REPOSITORY_NO_REMOTE_ORIGIN_FOUND_IN_THE_LOCAL_CONFIG, null);
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
                gitRepository.setStatus(RestConstants.GIT_REPOSITORY_IS_BEHIND_ORIGIN);
                log.debug(RestConstants.GIT_REPOSITORY_IS_BEHIND_ORIGIN + ": " + repositoryName);

                return gitRepository;
            }

            if (branchTrackingStatus.getAheadCount() > 0) {
                // local branch NOT outdated, is ahead origin by branchTrackingStatus.getAheadCount()
                gitRepository.setStatus(RestConstants.GIT_REPOSITORY_IS_AHEAD_OF_ORIGIN);
                log.debug(RestConstants.GIT_REPOSITORY_IS_AHEAD_OF_ORIGIN + ": " + repositoryName);

                return gitRepository;
            }

        } catch (IOException e) {
            logAndThrow(repositoryPath, repositoryName, RestConstants.ERROR_BUILDING_GIT_INSTANCE, e);
        }

        log.debug(RestConstants.GIT_REPOSITORY_IS_UP_TO_DATE + ": " + repositoryName);
        gitRepository.setStatus(RestConstants.GIT_REPOSITORY_IS_UP_TO_DATE);

        return gitRepository;
    }

    /**
     * Fetches branches for the repository located at the repositoryPath
     *
     * @param repository     the repository which branches will be fetched
     * @param repositoryPath the repository path
     * @param repositoryName the name of the repository
     */
    private void fetchBranches(Repository repository, String repositoryPath, String repositoryName) {
        log.info(String.format("Fetching branches for repository: %s", repositoryName));
        GitRepository gitRepository = new GitRepository();
        gitRepository.setName(repositoryName);
        gitRepository.setPath(repositoryPath);
        gitRepository.setStatus("");

        try (Git git = new Git(repository)) {
            GitTransportProtocol gitTransportProtocol = getTransportProtocol(repository, gitRepository);

            if (gitTransportProtocol.equals(GitTransportProtocol.HTTP)) {
                git.fetch()
                        .setCredentialsProvider(usernamePasswordCredentialsProvider)
                        .call();
            } else if (gitTransportProtocol.equals(GitTransportProtocol.SSH)) {
                git.fetch()
                        .setTransportConfigCallback(getTransportConfigCallback())
                        .call();
            }
        } catch (InvalidRemoteException e) {
            logAndThrow(repositoryPath, repositoryName, RestConstants.ERROR_FETCHING_INVALID_REMOTE, e);
        } catch (TransportException e) {
            if (e.getCause().getMessage().contains("Authentication is required but no CredentialsProvider has been registered")) {
                logAndThrow(repositoryPath, repositoryName,
                        RestConstants.ERROR_CONNECTING_TO_REMOTE_REPOSITOY_AUTHENTICATION_IS_REQUIRED,
                        e);
            }

            if (e.getCause().getMessage().contains("Auth fail")) {
                logAndThrow(repositoryPath, repositoryName,
                        RestConstants.ERROR_CONNECTING_TO_REMOTE_REPOSITOY_AUTH_FAIL,
                        e);
            }

            logAndThrow(repositoryPath, repositoryName, RestConstants.ERROR_FETCHING_TRANSPORT_FAILED, e);
        } catch (GitAPIException e) {
            logAndThrow(repositoryPath, repositoryName, RestConstants.ERROR_FETCHING_GITAPI_EXCEPTION, e);
        }
    }

    private void logAndThrow(String repositoryPath, String repositoryName, String errorMessage, Exception e) {
        if (e != null) {
            log.error(errorMessage + " Repository name: " + repositoryName, e);
        } else {
            log.error(errorMessage + " Repository name: " + repositoryName);
        }

        throw new AspenRestException(errorMessage, Response.Status.ACCEPTED, repositoryPath, repositoryName);
    }

    /**
     * Builds a Repository object based on the path provided
     *
     * @param repositoryPath the path to the git repository
     * @return the Repository object
     * @throws IOException
     */
    private Repository getJGitRepository(String repositoryPath) throws IOException {
        // the .git directory inside the repository
        Path gitRepositoryConfigPath = Paths.get(repositoryPath, ".git");

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder
                .setGitDir(gitRepositoryConfigPath.toFile())
                .readEnvironment() // scan environment GIT_* variables
                .build();
    }

    private void setUsernamePasswordCredentialsProvider(String username, String password) {
        usernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
    }

    /**
     * Fetches the transport protocol used to communicate with origin
     *
     * @param repository    jgit repository object
     * @param gitRepository holds the repository path, name and status
     * @return
     */
    private GitTransportProtocol getTransportProtocol(Repository repository, GitRepository gitRepository) {
        log.debug("Getting the transport protocol");
        try {
            for (TransportProtocol transportProtocol : TransportGitSsh.getTransportProtocols()) {
                boolean canHandle = transportProtocol
                        .canHandle(new URIish(repository.getConfig().getString("remote", "origin", "url")));

                if (canHandle) {
                    GitTransportProtocol gitTransportProtocol =
                            GitTransportProtocol.valueOf(transportProtocol.getName());
                    log.debug("Found the transport protocol {}", gitTransportProtocol.name());
                    return gitTransportProtocol;
                }
            }
        } catch (URISyntaxException e) {
            logAndThrow(gitRepository.getPath(), gitRepository.getName(),
                    RestConstants.ERROR_WHILE_GETTING_THE_TRANSPORT_PROTOCOL,
                    e);
        }

        throw new AspenRestException(RestConstants.NO_SUPPORTED_TRANSPORT_PROTOCOL_FOUND,
                Response.Status.ACCEPTED,
                gitRepository.getPath(),
                gitRepository.getName());
    }
}
