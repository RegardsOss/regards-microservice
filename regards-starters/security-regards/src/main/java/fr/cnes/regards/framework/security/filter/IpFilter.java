/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.framework.security.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import fr.cnes.regards.framework.security.domain.SecurityException;
import fr.cnes.regards.framework.security.endpoint.IAuthoritiesProvider;
import fr.cnes.regards.framework.security.endpoint.MethodAuthorizationService;
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
    private final MethodAuthorizationService methodAuthService;

    /**
     *
     * Constructor
     *
     * @param pAuthoritiesProvider
     *            {@link IAuthoritiesProvider}
     * @since 1.0-SNAPSHOT
     */
    public IpFilter(final MethodAuthorizationService pMethodAuthService) {
        super();
        methodAuthService = pMethodAuthService;
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
            if (!roles.isEmpty()) {
                final List<String> authorizedAddresses = retrieveRoleAuthorizedAddresses(roles, authentication.getTenant());
                if (!checkAccessByAddress(authorizedAddresses, pRequest.getRemoteAddr())) {
                    final String message = String.format("[REGARDS IP FILTER] - %s - Authorization denied",
                                                         pRequest.getRemoteAddr());
                    LOG.error(message);
                    pResponse.sendError(HttpStatus.UNAUTHORIZED.value(), message);
                } else {
                    LOG.debug(String.format("[REGARDS IP FILTER] - %s - Authorization granted",
                                            pRequest.getRemoteAddr()));

                    // Continue the filtering chain
                    pFilterChain.doFilter(pRequest, pResponse);
                }
            } else {
                pResponse.sendError(HttpStatus.UNAUTHORIZED.value(), "No Authority Role defined");
            }
        } catch (final SecurityException e) {
            final String message = "[REGARDS IP FILTER] Error on access resolution: " + e.getMessage();
            LOG.error(message, e);
            pResponse.sendError(HttpStatus.UNAUTHORIZED.value(), message);
        }
    }

    /**
     *
     * Retrieve authorized addresses for the given roles.
     *
     * @param pRoles
     *            roles
     * @return authorized addresses
     * @throws SecurityException
     *             Error retrieving role informations
     * @since 1.0-SNAPSHOT
     */
    private List<String> retrieveRoleAuthorizedAddresses(final Collection<RoleAuthority> pRoles, final String pTenant)
            throws SecurityException {
        final List<String> authorizedAddresses = new ArrayList<>();
        for (final RoleAuthority role : pRoles) {
            // Role is a sys role then there is no ip limitation
            if (!RoleAuthority.isSysRole(role.getAuthority())
                    && !RoleAuthority.isInstanceAdminRole(role.getAuthority())) {
                final Optional<RoleAuthority> roleAuth = methodAuthService
                        .getRoleAuthority(RoleAuthority.getRoleName(role.getAuthority()), pTenant);
                roleAuth.ifPresent(r -> authorizedAddresses.addAll(r.getAuthorizedIpAdresses()));
            }
        }
        return authorizedAddresses;
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
