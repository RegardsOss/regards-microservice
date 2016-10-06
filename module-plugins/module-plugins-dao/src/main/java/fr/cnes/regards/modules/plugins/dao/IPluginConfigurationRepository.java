/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.plugins.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import fr.cnes.regards.modules.plugins.domain.PluginConfiguration;

/**
 * Interface plugin repository
 * 
 * @author cmertz
 *
 */
public interface IPluginConfigurationRepository extends CrudRepository<PluginConfiguration, Long> {

    /**
    *
    * Find a {@link List} of {@link PluginConfiguration} for a plugin and for current tenant
    *
    * @param pPluginId
    *            plugin identifier
    * @param pTenant
    *            tenant
    * @return a {@link List} of {@link PluginConfiguration}
    * @since 1.0-SNAPSHOT
    */
   List<PluginConfiguration> findByPluginIdAndTenantOrderByPriorityOrderDesc(String pPluginId);
}