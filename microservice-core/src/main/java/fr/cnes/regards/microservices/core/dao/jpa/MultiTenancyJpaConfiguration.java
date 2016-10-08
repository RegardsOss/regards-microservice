/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.core.dao.jpa;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.sql.DataSource;

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import fr.cnes.regards.domain.annotation.InstanceEntity;
import fr.cnes.regards.microservices.core.configuration.common.MicroserviceConfiguration;

/**
 *
 * Configuration class to define hibernate/jpa multitenancy databases strategy
 *
 * @author CS
 * @since 1.0-SNAPSHOT
 */
@Configuration
@EnableJpaRepositories(excludeFilters = {
        @ComponentScan.Filter(value = InstanceEntity.class,
                              type = FilterType.ANNOTATION) }, basePackages = DaoUtils.PACKAGES_TO_SCAN,
                       entityManagerFactoryRef = "projectsEntityManagerFactory",
                       transactionManagerRef = "projectsJpaTransactionManager")
@EnableTransactionManagement
@ConditionalOnProperty("microservice.dao.enabled")
public class MultiTenancyJpaConfiguration {

    /**
     * JPA Persistence unit name. Used to separate multiples databases.
     */
    private static final String PERSITENCE_UNIT_NAME = "projects";

    /**
     * Data sources pool
     */
    @Autowired
    @Qualifier("dataSources")
    private Map<String, DataSource> dataSources;

    /**
     * Microservice global configuration
     */
    @Autowired
    private MicroserviceConfiguration configuration;

    /**
     * JPA Configuration
     */
    @Autowired
    private JpaProperties jpaProperties;

    /**
     * Multitenant connection provider
     */
    @Autowired
    private MultiTenantConnectionProvider multiTenantConnectionProvider;

    /**
     * Tenant resolver.
     */
    @Autowired
    private CurrentTenantIdentifierResolver currentTenantIdentifierResolver;

    /**
     *
     * Create Transaction manager for multitenancy projects datasources
     *
     * @param pBuilder
     *            EntityManagerFactoryBuilder
     * @return PlatformTransactionManager
     * @since 1.0-SNAPSHOT
     */
    @Bean
    public PlatformTransactionManager projectsJpaTransactionManager(EntityManagerFactoryBuilder pBuilder) {
        final JpaTransactionManager jtm = new JpaTransactionManager();
        jtm.setEntityManagerFactory(projectsEntityManagerFactory(pBuilder).getObject());
        return jtm;
    }

    /**
     *
     * Create EntityManagerFactory for multitenancy projects datasources
     *
     * @param pBuilder
     *            EntityManagerFactoryBuilder
     * @return LocalContainerEntityManagerFactoryBean
     * @since 1.0-SNAPSHOT
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean projectsEntityManagerFactory(EntityManagerFactoryBuilder pBuilder) {
        // Use the first dataSource configuration to init the entityManagerFactory
        final DataSource defaultDataSource = dataSources.values().iterator().next();

        final Map<String, Object> hibernateProps = new LinkedHashMap<>();
        hibernateProps.putAll(jpaProperties.getHibernateProperties(defaultDataSource));

        hibernateProps.put(Environment.MULTI_TENANT, MultiTenancyStrategy.DATABASE);
        hibernateProps.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        hibernateProps.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);
        if (configuration.getDao().getEmbedded()) {
            hibernateProps.put(Environment.DIALECT, DataSourcesConfiguration.EMBEDDED_HSQLDB_HIBERNATE_DIALECT);
        } else {
            hibernateProps.put(Environment.DIALECT, configuration.getDao().getDialect());
        }

        // Find classpath for each Entity and not NonStandardEntity
        final List<Class<?>> packages = DaoUtils.scanForJpaPackages(DaoUtils.PACKAGES_TO_SCAN, Entity.class,
                                                                    InstanceEntity.class);

        return pBuilder.dataSource(defaultDataSource).persistenceUnit(PERSITENCE_UNIT_NAME)
                .packages(packages.toArray(new Class[packages.size()])).properties(hibernateProps).jta(false).build();
    }
}
