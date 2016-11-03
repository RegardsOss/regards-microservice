/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.test.integration;

import java.util.List;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;

import fr.cnes.regards.framework.security.domain.HttpConstants;
import fr.cnes.regards.framework.security.endpoint.MethodAuthorizationService;
import fr.cnes.regards.framework.security.utils.jwt.JWTService;

/**
 *
 * Base class to realize integration tests using JWT and MockMvc
 *
 * @author svissier
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractRegardsIT {

    /**
     * Default tenant configured in application properties
     */
    protected static final String DEFAULT_TENANT = "PROJECT";

    /**
     * Default user
     */
    protected static final String DEFAULT_USER = "default_user";

    /**
     * Default user email
     */
    protected static final String DEFAULT_USER_EMAIL = "default_user@regards.fr";

    /**
     * Default user role
     */
    protected static final String DEFAULT_ROLE = "ROLE_DEFAULT";

    // CHECKSTYLE:OFF
    /**
     * JWT service
     */
    @Autowired
    protected JWTService jwtService;

    /**
     * Authorization service method
     */
    @Autowired
    protected MethodAuthorizationService authService;
    // CHECKSTYLE:ON

    /**
     * Mock for MVC testing
     */
    @Autowired
    private MockMvc mvc;

    protected abstract Logger getLogger();

    protected void performGet(final String pUrlTemplate, final String pAuthenticationToken,
            final List<ResultMatcher> pMatchers, final String pErrorMessage, final Object... pUrlVariables) {
        performRequest(pAuthenticationToken, HttpMethod.GET, pUrlTemplate, pMatchers, pErrorMessage, pUrlVariables);
    }

    protected void performPost(final String pUrlTemplate, final String pAuthenticationToken, final Object pContent,
            final List<ResultMatcher> pMatchers, final String pErrorMessage, final Object... pUrlVariables) {
        performRequest(pAuthenticationToken, HttpMethod.POST, pUrlTemplate, pContent, pMatchers, pErrorMessage,
                       pUrlVariables);
    }

    protected void performPut(final String pUrlTemplate, final String pAuthenticationToken, final Object pContent,
            final List<ResultMatcher> pMatchers, final String pErrorMessage, final Object... pUrlVariables) {
        performRequest(pAuthenticationToken, HttpMethod.PUT, pUrlTemplate, pContent, pMatchers, pErrorMessage,
                       pUrlVariables);
    }

    protected void performDelete(final String pUrlTemplate, final String pAuthenticationToken,
            final List<ResultMatcher> pMatchers, final String pErrorMessage, final Object... pUrlVariables) {
        performRequest(pAuthenticationToken, HttpMethod.DELETE, pUrlTemplate, pMatchers, pErrorMessage, pUrlVariables);
    }

    // Automatic default security management methods

    protected void performDefaultGet(final String pUrlTemplate, final List<ResultMatcher> pMatchers,
            final String pErrorMessage, final Object... pUrlVariables) {
        final String jwt = manageDefaultSecurity(pUrlTemplate, RequestMethod.GET);
        performRequest(jwt, HttpMethod.GET, pUrlTemplate, pMatchers, pErrorMessage, pUrlVariables);
    }

    protected void performDefaultPost(final String pUrlTemplate, final Object pContent,
            final List<ResultMatcher> pMatchers, final String pErrorMessage, final Object... pUrlVariables) {
        final String jwt = manageDefaultSecurity(pUrlTemplate, RequestMethod.POST);
        performRequest(jwt, HttpMethod.POST, pUrlTemplate, pContent, pMatchers, pErrorMessage, pUrlVariables);
    }

    protected void performDefaultPut(final String pUrlTemplate, final Object pContent,
            final List<ResultMatcher> pMatchers, final String pErrorMessage, final Object... pUrlVariables) {
        final String jwt = manageDefaultSecurity(pUrlTemplate, RequestMethod.PUT);
        performRequest(jwt, HttpMethod.PUT, pUrlTemplate, pContent, pMatchers, pErrorMessage, pUrlVariables);
    }

    protected void performDefaultDelete(final String pUrlTemplate, final List<ResultMatcher> pMatchers,
            final String pErrorMessage, final Object... pUrlVariables) {
        final String jwt = manageDefaultSecurity(pUrlTemplate, RequestMethod.DELETE);
        performRequest(jwt, HttpMethod.DELETE, pUrlTemplate, pMatchers, pErrorMessage, pUrlVariables);
    }

    /**
     * Perform a REST request and control expectations
     *
     * @param pAuthenticationToken
     *            JWT token
     * @param pHttpMethod
     *            HTTP method
     * @param pUrlTemplate
     *            URL template
     * @param pContent
     *            content for {@link HttpMethod#POST} and {@link HttpMethod#PUT} methods
     * @param pMatchers
     *            expectations
     * @param pErrorMessage
     *            message if error occurs
     * @param pUrlVariables
     *            URL variables
     */
    protected void performRequest(final String pAuthenticationToken, final HttpMethod pHttpMethod,
            final String pUrlTemplate, final Object pContent, final List<ResultMatcher> pMatchers,
            final String pErrorMessage, final Object... pUrlVariables) {

        Assert.assertTrue(HttpMethod.POST.equals(pHttpMethod) || HttpMethod.PUT.equals(pHttpMethod));
        MockHttpServletRequestBuilder requestBuilder = getRequestBuilder(pAuthenticationToken, pHttpMethod,
                                                                         pUrlTemplate, pUrlVariables);
        final String content = gson(pContent);
        requestBuilder = requestBuilder.content(content).header(HttpHeaders.CONTENT_TYPE,
                                                                MediaType.APPLICATION_JSON_VALUE);
        performRequest(requestBuilder, pMatchers, pErrorMessage);
    }

    protected void performRequest(final String pAuthenticationToken, final HttpMethod pHttpMethod,
            final String pUrlTemplate, final List<ResultMatcher> pMatchers, final String pErrorMessage,
            final Object... pUrlVariables) {

        Assert.assertTrue(HttpMethod.GET.equals(pHttpMethod) || HttpMethod.DELETE.equals(pHttpMethod));
        final MockHttpServletRequestBuilder requestBuilder = getRequestBuilder(pAuthenticationToken, pHttpMethod,
                                                                               pUrlTemplate, pUrlVariables);
        performRequest(requestBuilder, pMatchers, pErrorMessage);
    }

    /**
     *
     * @param pRequestBuilder
     *            request builder
     * @param pMatchers
     *            expectations
     * @param pErrorMessage
     *            message if error occurs
     */
    protected void performRequest(final MockHttpServletRequestBuilder pRequestBuilder,
            final List<ResultMatcher> pMatchers, final String pErrorMessage) {
        try {
            ResultActions request = mvc.perform(pRequestBuilder);
            for (final ResultMatcher matcher : pMatchers) {
                request = request.andExpect(matcher);
            }
            // CHECKSTYLE:OFF
        } catch (final Exception e) {
            // CHECKSTYLE:ON
            getLogger().error(pErrorMessage, e);
            Assert.fail(pErrorMessage);
        }
    }

    protected MockHttpServletRequestBuilder getRequestBuilder(final String pAuthToken, final HttpMethod pHttpMethod,
            final String pUrlTemplate, final Object... pUrlVars) {
        return MockMvcRequestBuilders.request(pHttpMethod, pUrlTemplate, pUrlVars)
                .header(HttpConstants.AUTHORIZATION, HttpConstants.BEARER + " " + pAuthToken);
    }

    // CHECKSTYLE:OFF
    protected String gson(final Object pObject) {
        if (pObject instanceof String) {
            return (String) pObject;
        }
        final Gson gson = new Gson();
        return gson.toJson(pObject);
    }
    // CHECKSTYLE:ON

    /**
     * Generate token for default tenant
     *
     * @param pEmail
     *            user email
     * @param pName
     *            user name
     * @param pRole
     *            user role
     * @return JWT
     */
    protected String generateToken(final String pEmail, final String pName, final String pRole) {
        return jwtService.generateToken(DEFAULT_TENANT, pEmail, pName, pRole);
    }

    /**
     * Set authorities for default tenant
     *
     * @param pUrlPath
     *            endpoint
     * @param pMethod
     *            HTTP method
     * @param pRoleNames
     *            list of roles
     */
    protected void setAuthorities(final String pUrlPath, final RequestMethod pMethod, final String... pRoleNames) {
        authService.setAuthorities(DEFAULT_TENANT, pUrlPath, pMethod, pRoleNames);
    }

    /**
     * Helper method to manage default security with :
     * <ul>
     * <li>a default user</li>
     * <li>a default role</li>
     * </ul>
     * The helper generates a JWT using its default configuration and grants access to the endpoint for the default
     * role.
     *
     * @param pUrlPath
     *            target endpoint
     * @param pMethod
     *            target HTTP method
     * @return security token to authenticate user
     */
    protected String manageDefaultSecurity(final String pUrlPath, final RequestMethod pMethod) {
        final String jwt = generateToken(DEFAULT_USER_EMAIL, DEFAULT_USER, DEFAULT_ROLE);
        setAuthorities(pUrlPath, pMethod, DEFAULT_ROLE);
        return jwt;
    }
}
