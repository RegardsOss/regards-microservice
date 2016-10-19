/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.swagger.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 *
 * Auto configuration for swagger
 *
 * @author msordi
 *
 */
@Configuration
@ConditionalOnWebApplication
@Conditional(ServerProperties.class)
@ConditionalOnProperty(prefix = "regards.swagger", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(SwaggerProperties.class)
@EnableSwagger2
public class SwaggerAutoConfiguration {

    @Autowired
    private SwaggerProperties properties;

    private ApiInfoBuilder apiInfoBuilder() {
        return new ApiInfoBuilder().title(properties.getApiTitle()).description(properties.getApiDescription())
                .license(properties.getApiLicense()).version(properties.getApiVersion());
    }

    @Bean
    public Docket appApi() {

        final ApiInfo infos = apiInfoBuilder().build();

        return new Docket(DocumentationType.SWAGGER_2).apiInfo(infos).select().apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any()).build();
    }
}