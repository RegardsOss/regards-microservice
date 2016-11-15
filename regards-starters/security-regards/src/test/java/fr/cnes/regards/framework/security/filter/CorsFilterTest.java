/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.security.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import fr.cnes.regards.framework.security.domain.ResourceMapping;
import fr.cnes.regards.framework.security.endpoint.IAuthoritiesProvider;
import fr.cnes.regards.framework.security.utils.jwt.JWTAuthentication;
import fr.cnes.regards.framework.security.utils.jwt.UserDetails;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;

/**
 *
 * Class CorsFilterTest
 *
 * Cors filter test class
 *
 * @author Sébastien Binda
 * @since 1.0-SNAPSHOT
 */
public class CorsFilterTest {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(CorsFilterTest.class);

    /**
     * Test user Role
     */
    private static final String ROLE_NAME = "USER";

    /**
     *
     * Check security filter with cors requests
     *
     * @since 1.0-SNAPSHOT
     */
    @Requirement("REGARDS_DSL_SYS_ARC_030")
    @Purpose("Check security filter with cors requests")
    @Test
    public void corsFilterTest() {

        final HttpServletRequest mockedRequest = Mockito.mock(HttpServletRequest.class);
        final HttpServletResponse mockedResponse = new MockHttpServletResponse();

        final JWTAuthentication token = new JWTAuthentication("token");
        token.setRole(ROLE_NAME);
        final UserDetails user = new UserDetails();
        user.setName("test-user");
        token.setUser(user);
        SecurityContextHolder.getContext().setAuthentication(token);

        final CorsFilter filter = new CorsFilter(new IAuthoritiesProvider() {

            @Override
            public List<ResourceMapping> getResourcesAccessConfiguration() {
                return new ArrayList<>();
            }

            @Override
            public List<String> getRoleAuthorizedAddress(final String pRole) {
                return new ArrayList<>();
            }

            @Override
            public boolean hasCorsRequestsAccess(final String pAuthority) {
                return true;
            }

        });

        final String errorMessage = "Error creating response cors header";

        try {
            filter.doFilter(mockedRequest, mockedResponse, new MockFilterChain());

            if (mockedResponse.getStatus() != HttpStatus.OK.value()) {
                Assert.fail("Cors requests access should be granted for the given role.");
            }

            Assert.assertTrue(errorMessage, mockedResponse.getHeader(CorsFilter.ALLOW_ORIGIN).equals("*"));

            Assert.assertTrue(errorMessage, mockedResponse.getHeader(CorsFilter.ALLOW_METHOD)
                    .equals("POST, PUT, GET, OPTIONS, DELETE"));

            Assert.assertTrue(errorMessage,
                              mockedResponse.getHeader(CorsFilter.ALLOW_HEADER).equals("authorization, content-type"));

            Assert.assertTrue(errorMessage, mockedResponse.getHeader(CorsFilter.CONTROL_MAX_AGE).equals("3600"));
        } catch (ServletException | IOException e) {
            LOG.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
    }

    /**
     *
     * Check security filter with cors requests
     *
     * @since 1.0-SNAPSHOT
     */
    @Requirement("REGARDS_DSL_SYS_ARC_030")
    @Requirement("REGARDS_DSL_SYS_ARC_040")
    @Purpose("Check security filter with cors requests access denied for a given Role")
    @Test
    public void corsFilterRoleAccessDeniedTest() {

        final JWTAuthentication token = new JWTAuthentication("token");
        token.setRole(ROLE_NAME);
        final UserDetails user = new UserDetails();
        user.setName("test-user");
        token.setUser(user);
        SecurityContextHolder.getContext().setAuthentication(token);

        final HttpServletRequest mockedRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockedRequest.getMethod()).thenReturn("OPTIONS");
        final HttpServletResponse mockedResponse = new MockHttpServletResponse();

        final CorsFilter filter = new CorsFilter(new IAuthoritiesProvider() {

            @Override
            public List<ResourceMapping> getResourcesAccessConfiguration() {
                return new ArrayList<>();
            }

            @Override
            public List<String> getRoleAuthorizedAddress(final String pRole) {
                return new ArrayList<>();
            }

            @Override
            public boolean hasCorsRequestsAccess(final String pAuthority) {
                return false;
            }

        });

        try {
            filter.doFilter(mockedRequest, mockedResponse, new MockFilterChain());
        } catch (ServletException | IOException e) {
            LOG.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
        if (mockedResponse.getStatus() == HttpStatus.OK.value()) {
            Assert.fail("Cors requests access is denied for the given role.");
        }
    }

}
