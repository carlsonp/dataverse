package edu.harvard.iq.dataverse.api.auth;

import edu.harvard.iq.dataverse.authorization.users.User;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import static edu.harvard.iq.dataverse.api.ApiConstants.CONTAINER_REQUEST_CONTEXT_USER;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecurityFilter implements ContainerRequestFilter {

    @Inject
    private ApiKeyAuthMechanism apiKeyAuthMechanism;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        CompoundAuthMechanism compoundAuthMechanism = new CompoundAuthMechanism(apiKeyAuthMechanism);
        try {
            User user = compoundAuthMechanism.findUserFromRequest(containerRequestContext);
            containerRequestContext.setProperty(CONTAINER_REQUEST_CONTEXT_USER, user);
        } catch (WrappedAuthErrorResponse e) {
            containerRequestContext.abortWith(e.getResponse());
        }
    }
}
