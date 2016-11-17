/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.security.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import fr.cnes.regards.framework.security.domain.SecurityException;
import fr.cnes.regards.framework.security.endpoint.IAuthoritiesProvider;
import fr.cnes.regards.framework.security.utils.endpoint.RoleAuthority;
import fr.cnes.regards.framework.security.utils.jwt.JWTAuthentication;

/**
 *
 * Class IPFilter
 *
 * Spring MVC request filter by IP
 *
 * @author sbinda
 * @since 1.0-SNAPSHOT
 */
public class IpFilter extends OncePerRequestFilter {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(IpFilter.class);

    /**
     * Provider of authorities entities
     */
    private final IAuthoritiesProvider authoritiesProvider;

    /**
     *
     * Constructor
     *
     * @param pAuthoritiesProvider
     *            {@link IAuthoritiesProvider}
     * @since 1.0-SNAPSHOT
     */
    public IpFilter(final IAuthoritiesProvider pAuthoritiesProvider) {
        super();
        authoritiesProvider = pAuthoritiesProvider;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest pRequest, final HttpServletResponse pResponse,
            final FilterChain pFilterChain) throws ServletException, IOException {

        // Get authorized ip associated to given role
        final JWTAuthentication authentication = (JWTAuthentication) SecurityContextHolder.getContext()
                .getAuthentication();

        @SuppressWarnings("unchecked")
        final Collection<RoleAuthority> roles = (Collection<RoleAuthority>) authentication.getAuthorities();

        try {
            final List<String> authorizedAddresses = new ArrayList<>();
            if (!roles.isEmpty()) {
                for (final RoleAuthority role : roles) {
                    authorizedAddresses.addAll(authoritiesProvider.getRoleAuthorizedAddress(role.getAuthority()));
                }
                if (!checkAccessByAddress(authorizedAddresses, pRequest.getRemoteAddr())) {
                    final String message = String.format("[REGARDS IP FILTER] - %s - Authorization denied",
                                                         pRequest.getRemoteAddr());
                    LOG.error(message);
                    pResponse.sendError(HttpStatus.UNAUTHORIZED.value(), message);
                } else {
                    LOG.info(String.format("[REGARDS IP FILTER] - %s - Authorization granted",
                                           pRequest.getRemoteAddr()));

                    // Continue the filtering chain
                    pFilterChain.doFilter(pRequest, pResponse);
                }
            } else {
                pResponse.sendError(HttpStatus.UNAUTHORIZED.value(), "No Authority Role defined");
            }
        } catch (final SecurityException e) {
            final String message = "[REGARDS IP FILTER] Error on access resolution: " + e.getMessage();
            LOG.debug(message, e);
            pResponse.sendError(HttpStatus.UNAUTHORIZED.value(), message);
        }
    }

    /**
     *
     * Check if the user adress match ones of the role authorized addresses.
     *
     * @param pAuthorizedAddress
     *            Role authorized addresses
     * @param pUserAdress
     *            user address
     * @return [true|false]
     * @since 1.0-SNAPSHOT
     */
    private boolean checkAccessByAddress(final List<String> pAuthorizedAddress, final String pUserAdress) {
        boolean accessAuthorized = false;
        if ((pAuthorizedAddress != null) && !pAuthorizedAddress.isEmpty()) {
            if ((pUserAdress != null) && !pUserAdress.isEmpty()) {
                for (final String authorizedAddress : pAuthorizedAddress) {
                    final Pattern pattern = Pattern.compile(authorizedAddress);
                    accessAuthorized |= pattern.matcher(pUserAdress).matches();
                }
            }
        } else {
            accessAuthorized = true;
        }
        return accessAuthorized;
    }

}
