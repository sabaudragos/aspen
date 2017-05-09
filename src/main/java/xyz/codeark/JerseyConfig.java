package xyz.codeark;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.stereotype.Component;
import xyz.codeark.rest.DirectoryDiscoveryResource;
import xyz.codeark.rest.GitResource;
import xyz.codeark.rest.MavenResource;
import xyz.codeark.rest.RestConstants;
import xyz.codeark.rest.exceptions.AspenRestException;
import xyz.codeark.rest.exceptions.AspenRestExceptionMapper;


/**
 * Allows static content to be displayed
 */
@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(DirectoryDiscoveryResource.class);
        register(GitResource.class);
        register(MavenResource.class);
        register(RestConstants.class);
        register(AspenRestException.class);
        register(AspenRestExceptionMapper.class);
        property(ServletProperties.FILTER_FORWARD_ON_404, true);
    }
}
