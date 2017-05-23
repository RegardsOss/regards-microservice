/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.modules.jpa.instance.autoconfigure;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

/**
 *
 * Class MultiTenancyDaoTestConfiguration
 *
 * Configuration class for DAO unit tests
 *
 * @author CS
 * @since 1.0-SNAPSHOT
 */
@ComponentScan(basePackages = "fr.cnes.regards.framework.modules.jpa.instance")
@EnableAutoConfiguration
@PropertySource("classpath:dao.properties")
public class InstanceDaoTestConfiguration {

}