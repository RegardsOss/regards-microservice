/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.jpa.multitenant.autoconfigure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.cnes.regards.framework.jpa.multitenant.properties.MultitenantDaoProperties;
import fr.cnes.regards.framework.jpa.multitenant.properties.TenantConnection;
import fr.cnes.regards.framework.jpa.multitenant.resolver.ITenantConnectionResolver;
import fr.cnes.regards.framework.jpa.utils.DataSourceHelper;

/**
 *
 * Configuration class to define the default PostgresSQL Data base
 *
 * @author Sébastien Binda
 * @since 1.0-SNAPSHOT
 */
@Configuration
@EnableConfigurationProperties(MultitenantDaoProperties.class)
@ConditionalOnProperty(prefix = "regards.jpa", name = "multitenant.enabled", matchIfMissing = true)
public class DataSourcesAutoConfiguration {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(DataSourcesAutoConfiguration.class);

    /**
     * Microservice globale configuration
     */
    @Autowired
    private MultitenantDaoProperties daoProperties;

    /**
     * Custom projects dao connection reader
     */
    @Autowired
    private ITenantConnectionResolver multitenantResolver;

    /**
     *
     * List of data sources for each configured tenant.
     *
     * @return Map<Tenant, DataSource>
     * @since 1.0-SNAPSHOT
     */
    @Bean(name = { "multitenantsDataSources" })
    public Map<String, DataSource> getDataSources() {

        final Map<String, DataSource> datasources = new HashMap<>();

        // Add datasources from bean configuration
        final List<TenantConnection> connections = multitenantResolver.getTenantConnections();
        datasources.putAll(createDataSourcesFromTenants(connections));

        // Add datasources configuration from properties file.
        for (final TenantConnection tenant : daoProperties.getTenants()) {
            DataSource datasource;
            if (daoProperties.getEmbedded()) {
                datasource = DataSourceHelper.createEmbeddedDataSource(tenant.getName(),
                                                                       daoProperties.getEmbeddedPath());

            } else {
                datasource = DataSourceHelper.createDataSource(tenant.getUrl(), tenant.getDriverClassName(),
                                                               tenant.getUserName(), tenant.getPassword());
            }
            if (!datasources.containsKey(tenant.getName())) {
                // Initialize connection in the administration service
                multitenantResolver.addTenantConnection(tenant);
                // Add datasource to managed datasources pool
                datasources.put(tenant.getName(), datasource);
            } else {
                LOG.warn(String.format("Datasource for tenant %s already defined.", tenant.getName()));
            }
        }

        return datasources;
    }

    /**
     *
     * Create the datasources from the TenantConfiguration list given
     *
     * @param pTenants
     *            pTenants tenants configuration
     * @return datasources created
     * @since 1.0-SNAPSHOT
     */
    private Map<String, DataSource> createDataSourcesFromTenants(final List<TenantConnection> pTenants) {

        final Map<String, DataSource> datasources = new HashMap<>();

        for (final TenantConnection tenant : pTenants) {
            if (!datasources.containsKey(tenant.getName())) {
                if (daoProperties.getEmbedded()) {
                    datasources.put(tenant.getName(), DataSourceHelper
                            .createEmbeddedDataSource(tenant.getName(), daoProperties.getEmbeddedPath()));
                } else {
                    datasources.put(tenant.getName(),
                                    DataSourceHelper.createDataSource(tenant.getUrl(), tenant.getDriverClassName(),
                                                                      tenant.getUserName(), tenant.getPassword()));
                }
            } else {
                LOG.warn(String.format("Datasource for tenant %s already defined.", tenant.getName()));
            }
        }
        return datasources;
    }

    /**
     *
     * Default data source for persistence unit projects.
     *
     * ConditionalOnMissingBean : In case of jpa-instance-regards-starter activated. There can't be two datasources.
     *
     * @return datasource
     * @since 1.0-SNAPSHOT
     */
    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource projectsDataSource() {
        final Map<String, DataSource> multitenantsDataSources = getDataSources();
        DataSource datasource = null;
        if ((multitenantsDataSources != null) && !multitenantsDataSources.isEmpty()) {
            datasource = multitenantsDataSources.values().iterator().next();
        } else {
            LOG.error("No datasource defined for MultitenantcyJpaAutoConfiguration !");
        }
        return datasource;
    }
}