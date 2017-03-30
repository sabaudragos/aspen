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
}
