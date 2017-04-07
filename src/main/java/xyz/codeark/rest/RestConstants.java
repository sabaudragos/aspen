package xyz.codeark.rest;

/**
 * Container class for constants used by the REST services
 */
public class RestConstants {
    /**
     * Paths
     */
    public static final String API = "api/";
    public static final String DISCOVERY = API + "discovery";
    public static final String MAVEN = API + "mvn";
    public static final String GIT = API + "git";

    /**
     * Directory related exception messages
     */
    public static final String INVALID_PATH = "Invalid directory path";
    public static final String DIRECTORY_DISCOVERY_FAILURE = "Directory discovery failed due to IO error";

    /**
     * Maven related constants
     */
    public static final String MAVEN_SUCCESS = "Maven executed successfully";
    public static final String MAVEN_FAILURE = "Maven execution failed";
    public static final String INVALID_MVN_MODULE_PATH = "Invalid maven module path";
    public static final String UNSUPPORTED_OPERATING_SYSTEM = "Unsupported operating system";
    public static final String MVN_PATH_NOT_FOUND_IN_PATH_VARIABLE = "Maven not found in path variable";

    /**
     * Git related constants
     */
    public static final String INVALID_GIT_REPOSITORY_PATH = "Invalid git repository path";
    public static final String GIT_PULL_FAILED = "Git pull failed";
    public static final String GIT_PULL_SUCCESS = "Git pull executed successfully";
    public static final String GIT_REPOSITORY_IS_UP_TO_DATE = "Git repository is up to date with origin";
    public static final String GIT_REPOSITORY_IS_AHEAD_OF_ORIGIN = "Git repository is ahead origin";
    public static final String GIT_REPOSITORY_IS_BEHIND_OF_ORIGIN = "Git repository is behind origin";
}
