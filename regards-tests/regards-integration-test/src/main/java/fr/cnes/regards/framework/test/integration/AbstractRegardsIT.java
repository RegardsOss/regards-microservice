/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.test.integration;

import java.io.IOException;
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

import com.fasterxml.jackson.databind.ObjectMapper;

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
        try {
            MockHttpServletRequestBuilder requestBuilder = getRequestBuilder(pAuthenticationToken, pHttpMethod,
                                                                             pUrlTemplate, pUrlVariables);
            requestBuilder = requestBuilder.content(json(pContent)).header(HttpHeaders.CONTENT_TYPE,
                                                                           MediaType.APPLICATION_JSON_VALUE);
            performRequest(requestBuilder, pMatchers, pErrorMessage);
        } catch (final IOException e) {
            final String message = "Cannot (de)serialize model";
            getLogger().error(message, e);
            Assert.fail(message);
        }
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

    protected String json(final Object pObject) throws IOException {
        final String result;
        if (pObject instanceof String) {
            result = (String) pObject;
        } else {
            final ObjectMapper mapper = new ObjectMapper();
            result = mapper.writeValueAsString(pObject);
        }
        return result;
    }

    public JWTService getJwtService() {
        return jwtService;
    }

    public MethodAuthorizationService getAuthService() {
        return authService;
    }
}