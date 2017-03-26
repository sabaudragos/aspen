package xyz.codeark;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

/**
 * Allows static content to be displayed
 */
@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        packages("xyz.codeark.rest");
        property(ServletProperties.FILTER_FORWARD_ON_404, true);
    }
}
