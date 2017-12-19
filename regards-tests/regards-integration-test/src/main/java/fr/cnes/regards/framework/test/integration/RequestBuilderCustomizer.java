package fr.cnes.regards.framework.test.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.cnes.regards.framework.security.utils.HttpConstants;

/**
 * Allow to customize the request done thanks to {@link MockMvc}.
 * Methods "performXX" are considered terminal and so applies coherence controls on the customizations.
 *
 * @author Sylvain VISSIERE-GUERINET
 */
public class RequestBuilderCustomizer {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestBuilderCustomizer.class);

    /**
     * Default request headers
     */
    private static final HttpHeaders DEFAULT_HEADERS = new HttpHeaders();

    /**
     * Headers
     */
    private final HttpHeaders headers = new HttpHeaders();

    /**
     * Request parameter builder
     */
    private final RequestParamBuilder requestParamBuilder = RequestParamBuilder.build();

    /**
     * Documentation snippets
     */
    private final List<Snippet> documentationSnippets = Lists.newArrayList();

    /**
     * Request result expectations
     */
    private final List<ResultMatcher> expectations = Lists.newArrayList();

    /**
     * Should the doc be skip
     */
    private boolean skipDocumentation = false;

    /**
     * Gson builder
     */
    private final GsonBuilder gsonBuilder;

    static {
        //lets initiate the default headers!
        DEFAULT_HEADERS.add(HttpConstants.CONTENT_TYPE, "application/json");
        DEFAULT_HEADERS.add(HttpConstants.ACCEPT, "application/json");
    }

    /**
     * Constructor setting the parameter as attribute
     * @param gsonBuilder
     */
    public RequestBuilderCustomizer(GsonBuilder gsonBuilder) {
        this.gsonBuilder = gsonBuilder;
    }

    /**
     * @return the customizer configured to skip the documentation
     */
    public RequestBuilderCustomizer skipDocumentation() {
        skipDocumentation = true;
        return this;
    }

    /**
     * Allows to perform GET request
     */
    protected ResultActions performGet(MockMvc mvc, String urlTemplate, String authToken, String errorMsg,
            Object... urlVariables) {
        return performRequest(mvc, getRequestBuilder(authToken, HttpMethod.GET, urlTemplate, urlVariables), errorMsg);
    }

    /**
     * Allows to perform DELETE request
     */
    protected ResultActions performDelete(MockMvc mvc, String urlTemplate, String authToken, String errorMsg,
            Object... urlVariables) {
        return performRequest(mvc,
                              getRequestBuilder(authToken, HttpMethod.DELETE, urlTemplate, urlVariables),
                              errorMsg);
    }

    /**
     * Allows to perform POSTn request
     */
    protected ResultActions performPost(MockMvc mvc, String urlTemplate, String authToken, Object content,
            String errorMsg, Object... urlVariables) {
        return performRequest(mvc,
                              getRequestBuilder(authToken, HttpMethod.POST, content, urlTemplate, urlVariables),
                              errorMsg);
    }

    /**
     * Allows to perform PUT request
     */
    protected ResultActions performPut(MockMvc mvc, String urlTemplate, String authToken, Object content,
            String errorMsg, Object... urlVariables) {
        return performRequest(mvc,
                              getRequestBuilder(authToken, HttpMethod.PUT, content, urlTemplate, urlVariables),
                              errorMsg);
    }

    /**
     * Allows to perform multipart request providing the multiple parts
     */
    protected ResultActions performFileUpload(MockMvc mvc, String urlTemplate, String authToken,
            List<MockMultipartFile> files, String errorMsg, Object... urlVariables) {
        return performRequest(mvc, getMultipartRequestBuilder(authToken, files, urlTemplate, urlVariables), errorMsg);
    }

    /**
     * Allows to perform multipart request providing the path of the file that should be uploaded
     */
    protected ResultActions performFileUpload(MockMvc mvc, String urlTemplate, String authToken, Path filePath,
            String errorMsg, Object... urlVariables) {
        return performRequest(mvc,
                              getMultipartRequestBuilder(authToken, filePath, urlTemplate, urlVariables),
                              errorMsg);
    }

    /**
     * @return {@link MockHttpServletRequestBuilder} customized with RequestBuilderCustomizer#headers or default ones if none has been specified
     */
    private MockHttpServletRequestBuilder getRequestBuilder(String authToken, HttpMethod method, Object content,
            String urlTemplate, Object... urlVariables) {
        MockHttpServletRequestBuilder requestBuilder = getRequestBuilder(authToken, method, urlTemplate, urlVariables);
        String jsonContent = gson(content);
        requestBuilder.content(jsonContent);
        return requestBuilder;
    }

    /**
     * @return {@link MockHttpServletRequestBuilder} customized with RequestBuilderCustomizer#headers or default ones if none has been specified
     */
    protected MockHttpServletRequestBuilder getRequestBuilder(String authToken, HttpMethod httpMethod,
            String urlTemplate, Object... urlVars) {
        checkCustomizationCoherence(httpMethod);
        MockHttpServletRequestBuilder requestBuilder = RestDocumentationRequestBuilders
                .request(httpMethod, urlTemplate, urlVars);
        addSecurityHeader(requestBuilder, authToken);

        requestBuilder.headers(getHeaders());

        return requestBuilder;
    }

    /**
     * @param object
     * @return jsonified object using GSON
     */
    protected String gson(Object object) {
        if (object instanceof String) {
            return (String) object;
        }
        Gson gson = gsonBuilder.create();
        return gson.toJson(object);
    }

    /**
     * Grants access to the {@link RequestParamBuilder} used to add request parameters to the request
     * @return requestParamBuilder for further customization
     */
    public RequestParamBuilder customizeRequestParam() {
        return requestParamBuilder;
    }

    /**
     * Grants access to the {@link HttpHeaders} used to add request parameters to the request
     * @return http headers for further customization
     */
    public HttpHeaders customizeHeaders() {
        return headers;
    }

    /**
     * Add a whole list of ResultMatcher to be matched. Mainly here for easier refactor. We strongly advise to use {@link RequestBuilderCustomizer#addExpectation(ResultMatcher)}.
     * @param matchers list of matcher to be matched after by the server response
     */
    public void addExpectations(List<ResultMatcher> matchers) {
        expectations.addAll(matchers);
    }

    /**
     * Add {@link ResultMatcher} to the already present matchers
     * @param matcher
     */
    public void addExpectation(ResultMatcher matcher) {
        expectations.add(matcher);
    }

    /**
     * Add snippets to be used to generate specific documentation.
     * For exemple, request parameters and path parameters require too much specific information to be generalized. <br/>
     * Request parameters can be documented thanks to {@link org.springframework.restdocs.request.RequestParametersSnippet} <br/>
     * Path parameters cna be documented thanks to {@link org.springframework.restdocs.request.PathParametersSnippet} <br/>
     * @param snippet documentation snippet to be added.
     */
    public void addDocumentationSnippet(Snippet snippet) {
        documentationSnippets.add(snippet);
    }

    /**
     * perform a request and generate the documentation
     */
    private ResultActions performRequest(MockMvc mvc, MockHttpServletRequestBuilder requestBuilder, String errorMsg) {
        Assert.assertFalse("At least one expectation is required", expectations.isEmpty());
        try {
            Map<String, Object> queryParams = Maps.newHashMap();
            List<ParameterDescriptor> queryParamDescriptors = Lists.newArrayList();
            if (requestParamBuilder != null) {
                // lets create the attributes and description for the documentation snippet
                requestBuilder.params(requestParamBuilder.getParameters());
                for (Map.Entry<String, List<String>> entry : requestParamBuilder.getParameters().entrySet()) {
                    if (entry.getValue().size() == 1) {
                        queryParams.put(entry.getKey(), entry.getValue().get(0));
                    } else {
                        queryParams.put(entry.getKey(), entry.getValue());
                    }
                    queryParamDescriptors.add(RequestDocumentation.parameterWithName(entry.getKey()).description(""));
                }
            }
            ResultActions request = mvc.perform(requestBuilder);
            for (ResultMatcher matcher : expectations) {
                request = request.andExpect(matcher);
            }
            if (!skipDocumentation) {
                request.andDo(MockMvcRestDocumentation.document("{ClassName}/{methodName}",
                                                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint(),
                                                                                                Preprocessors.removeHeaders("Authorization",
                                                                                                "Host",
                                                                                                "Content-Length")),
                                                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint(),
                                                                                   Preprocessors.removeHeaders("Content-Length")),
                                                                documentationSnippets
                                                                        .toArray(new Snippet[documentationSnippets
                                                                                .size()])));
            }
            return request;
            // CHECKSTYLE:OFF
        } catch (Exception e) {
            // CHECKSTYLE:ON
            LOGGER.error(errorMsg, e);
            throw new AssertionError(errorMsg, e);
        }
    }

    /**
     * Build a multi-part request builder based on file {@link Path}
     * @param authToken authorization token
     * @param filePath {@link Path}
     * @param urlTemplate URL template
     * @param urlVars URL vars
     * @return {@link MockMultipartHttpServletRequestBuilder}
     */
    protected MockMultipartHttpServletRequestBuilder getMultipartRequestBuilder(String authToken, Path filePath,
            String urlTemplate, Object... urlVars) {

        try {
            MockMultipartFile file = new MockMultipartFile("file", Files.newInputStream(filePath));
            List<MockMultipartFile> fileList = new ArrayList<>(1);
            fileList.add(file);
            return getMultipartRequestBuilder(authToken, fileList, urlTemplate, urlVars);
        } catch (IOException e) {
            String message = String.format("Cannot create input stream for file %s", filePath.toString());
            LOGGER.error(message, e);
            throw new AssertionError(message, e);
        }
    }

    /**
     * Build a multi-part request builder based on file {@link Path}
     * @param authToken authorization token
     * @param files {@link MockMultipartFile}s
     * @param urlTemplate URL template
     * @param urlVars URL vars
     * @return {@link MockMultipartHttpServletRequestBuilder}
     */
    protected MockMultipartHttpServletRequestBuilder getMultipartRequestBuilder(String authToken,
            List<MockMultipartFile> files, String urlTemplate, Object... urlVars) {
        // we check with HttpMethod POST because fileUpload method generates a POST request.
        checkCustomizationCoherence(HttpMethod.POST);

        MockMultipartHttpServletRequestBuilder multipartRequestBuilder = RestDocumentationRequestBuilders
                .fileUpload(urlTemplate, urlVars);
        for (MockMultipartFile file : files) {
            multipartRequestBuilder.file(file);
        }
        addSecurityHeader(multipartRequestBuilder, authToken);
        multipartRequestBuilder.headers(getHeaders());
        return multipartRequestBuilder;
    }

    /**
     * Check if the request customizer is coherent towards the multiple options used
     * @param httpMethod
     */
    protected void checkCustomizationCoherence(HttpMethod httpMethod) {
        // constaints are only on DELETE, PUT and POST, for now, as they cannot have request parameters
        switch (httpMethod) {
            case DELETE:
            case PUT:
            case POST:
                if (!requestParamBuilder.getParameters().isEmpty()) {
                    throw new IllegalStateException(String.format("Method %s cannot have request parameters"));
                }
                break;
            default:
                break;
        }
    }

    /**
     * Add the authorization header to the request
     * @param requestBuilder
     * @param authToken
     */
    protected void addSecurityHeader(MockHttpServletRequestBuilder requestBuilder, String authToken) {
        requestBuilder.header(HttpConstants.AUTHORIZATION, HttpConstants.BEARER + " " + authToken);
    }

    /**
     * Contains logic on which headers should be used for a request.
     * @return default headers if no header customization has been done. Customized headers otherwise.
     */
    protected HttpHeaders getHeaders() {
        if (headers.isEmpty()) {
            return DEFAULT_HEADERS;
        } else {
            return headers;
        }
    }

}