/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.security.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import fr.cnes.regards.framework.security.domain.HttpConstants;
import fr.cnes.regards.framework.security.utils.jwt.JWTAuthentication;

/**
 * Stateless JWT filter set in the SPRING security chain to authenticate request issuer.<br/>
 * Use {@link JWTAuthenticationProvider} to do it through {@link AuthenticationManager} and its default implementation
 * that delegated authentication to {@link AuthenticationProvider}.
 *
 * @author msordi
 *
 */
public class JWTAuthenticationFilter extends GenericFilterBean {

    /**
     * Default security authentication manager
     */
    private final AuthenticationManager authenticationManager;

    public JWTAuthenticationFilter(final AuthenticationManager pAuthenticationManager) {
        this.authenticationManager = pAuthenticationManager;
    }

    @Override
    public void doFilter(final ServletRequest pRequest, final ServletResponse pResponse, final FilterChain pFilterChain)
            throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) pRequest;
        final HttpServletResponse response = (HttpServletResponse) pResponse;

        // Retrieve authentication header
        String jwt = request.getHeader(HttpConstants.AUTHORIZATION);
        if (jwt == null) {
            throw new InsufficientAuthenticationException("Authorization header not found");
        }

        // Extract JWT from retrieved header
        if (!jwt.startsWith(HttpConstants.BEARER)) {
            throw new InsufficientAuthenticationException("Authorization schema not found");
        }
        jwt = jwt.substring(HttpConstants.BEARER.length()).trim();

        // Init authentication object
        final Authentication jwtAuthentication = new JWTAuthentication(jwt);
        // Authenticate user with JWT
        final Authentication authentication = authenticationManager.authenticate(jwtAuthentication);
        // Set security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Continue the filtering chain
        pFilterChain.doFilter(request, response);
    }

}