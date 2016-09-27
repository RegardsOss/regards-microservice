/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.core.dao.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import fr.cnes.regards.microservices.core.configuration.common.MicroserviceConfiguration;
import fr.cnes.regards.microservices.core.configuration.common.ProjectConfiguration;

/**
 *
 * Configuration class to define the default PostgresSQL Data base
 *
 * @author CS
 * @since 1.0-SNAPSHOT
 */
@Configuration
@EnableConfigurationProperties(MicroserviceConfiguration.class)
@ConditionalOnProperty("microservice.dao.enabled")
public class DataSourcesConfiguration {

    public static final String EMBEDDED_HSQLDB_HIBERNATE_DIALECT = "org.hibernate.dialect.HSQLDialect";

    public static final String EMBEDDED_HSQL_DRIVER_CLASS = "org.hsqldb.jdbcDriver";

    public static final String EMBEDDED_HSQL_URL = "jdbc:hsqldb:file:";

    public static final String EMBEDDED_URL_SEPARATOR = "/";

    public static final String EMBEDDED_URL_BASE_NAME = "applicationdb";

    @Autowired
    private MicroserviceConfiguration configuration_;

    /**
     *
     * List of datasources for each configured project.
     *
     * @return
     * @since 1.0-SNAPSHOT
     */
    @Bean(name = { "dataSources" })
    public Map<String, DataSource> getDataSources() {

        Map<String, DataSource> datasources = new HashMap<>();

        for (ProjectConfiguration project : configuration_.getProjects()) {
            if (configuration_.getDao().getEmbedded()) {
                DriverManagerDataSource dataSource = new DriverManagerDataSource();
                dataSource.setDriverClassName(EMBEDDED_HSQL_DRIVER_CLASS);
                dataSource.setUrl(EMBEDDED_HSQL_URL + configuration_.getDao().getEmbeddedPath()
                        + DataSourcesConfiguration.EMBEDDED_URL_SEPARATOR + project.getName()
                        + DataSourcesConfiguration.EMBEDDED_URL_SEPARATOR
                        + DataSourcesConfiguration.EMBEDDED_URL_BASE_NAME);
                datasources.put(project.getName(), dataSource);
            }
            else {
                DataSourceBuilder factory = DataSourceBuilder.create(project.getDatasource().getClassLoader())
                        .driverClassName(configuration_.getDao().getDriverClassName())
                        .username(project.getDatasource().getUsername()).password(project.getDatasource().getPassword())
                        .url(project.getDatasource().getUrl());
                datasources.put(project.getName(), factory.build());
            }
        }

        return datasources;
    }

    /**
     *
     * Default datasource for persitence unit projects.
     *
     * @return
     * @since 1.0-SNAPSHOT
     */
    @Bean
    @Primary
    public DataSource projectsDataSource() {

        ProjectConfiguration project = configuration_.getProjects().get(0);

        if (configuration_.getDao().getEmbedded()) {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(EMBEDDED_HSQL_DRIVER_CLASS);
            dataSource.setUrl(EMBEDDED_HSQL_URL + configuration_.getDao().getEmbeddedPath()
                    + DataSourcesConfiguration.EMBEDDED_URL_SEPARATOR + project.getName()
                    + DataSourcesConfiguration.EMBEDDED_URL_SEPARATOR
                    + DataSourcesConfiguration.EMBEDDED_URL_BASE_NAME);

            return dataSource;
        }
        else {
            DataSourceBuilder factory = DataSourceBuilder.create(project.getDatasource().getClassLoader())
                    .driverClassName(configuration_.getDao().getDriverClassName())
                    .username(project.getDatasource().getUsername()).password(project.getDatasource().getPassword())
                    .url(project.getDatasource().getUrl());
            return factory.build();
        }
    }

    /**
     *
     * Default datasource for persitence unit instance.
     *
     * @return
     * @since 1.0-SNAPSHOT
     */
    @Bean
    @ConditionalOnProperty("microservice.dao.instance.enabled")
    public DataSource instanceDataSource() {

        if (configuration_.getDao().getEmbedded()) {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(EMBEDDED_HSQL_DRIVER_CLASS);
            dataSource.setUrl(EMBEDDED_HSQL_URL + configuration_.getDao().getEmbeddedPath()
                    + DataSourcesConfiguration.EMBEDDED_URL_SEPARATOR + "instance"
                    + DataSourcesConfiguration.EMBEDDED_URL_SEPARATOR
                    + DataSourcesConfiguration.EMBEDDED_URL_BASE_NAME);
            return dataSource;
        }
        else {
            DataSourceBuilder factory = DataSourceBuilder
                    .create(configuration_.getDao().getInstance().getDatasource().getClassLoader())
                    .driverClassName(configuration_.getDao().getDriverClassName())
                    .username(configuration_.getDao().getInstance().getDatasource().getUsername())
                    .password(configuration_.getDao().getInstance().getDatasource().getPassword())
                    .url(configuration_.getDao().getInstance().getDatasource().getUrl());
            return factory.build();
        }
    }

}
