/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */

package fr.cnes.regards.framework.modules.plugins.service;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;

/**
 * Plugin management service.
 *
 * @author Christophe Mertz
 * @author Sébastien Binda
 * @author Xavier-Alexandre Brochard
 */
public interface IPluginService {

    /**
     * Return all plugin types available.
     *
     * @return List<String>
     */
    List<String> getPluginTypes();

    /**
     * Return all {@link PluginMetaData} available
     *
     * @return list of {@link PluginMetaData}
     */
    List<PluginMetaData> getPlugins();

    /**
     * Return all {@link PluginMetaData} available for a specific plugin type.
     *
     * @param pInterfacePluginType a specific interface plugin type
     * @return list of {@link PluginMetaData}
     */
    List<PluginMetaData> getPluginsByType(Class<?> pInterfacePluginType);

    boolean canInstantiate(Long pluginConfigurationId)
            throws ModuleException;

    /**
     * Get a plugin instance for a given configuration. The pReturnInterfaceType attribute indicates the PluginInterface
     * return type.
     *
     * @param <T> a plugin instance
     * @param pPluginConfigurationId the id of a {@link PluginConfiguration}.
     * @param dynamicPluginParameters list of dynamic {@link PluginParameter}
     * @return a plugin instance
     * @throws ModuleException thrown if we cannot find any PluginConfiguration corresponding to pId
     */
    <T> T getPlugin(Long pPluginConfigurationId, final PluginParameter... dynamicPluginParameters)
            throws ModuleException;

    /**
     * Get a plugin instance for a {@link PluginConfiguration} and dynamic plugin parameters<br/>
     *
     * Note : this method is just a proxy for {@link IPluginService#getPlugin(Long, PluginParameter...)}
     * so plugin configuration is reloaded from database before instanciation.
     *
     * Use {@link IPluginService#getPlugin(Long, PluginParameter...)} instead.
     *
     * @param <T> a plugin instance
     * @param pPluginConfiguration a {@link PluginConfiguration}.
     * @param dynamicPluginParameters list of dynamic {@link PluginParameter}
     * @return a plugin instance
     * @throws ModuleException thrown if we cannot find any PluginConfiguration corresponding to pId
     */
    @Deprecated
    <T> T getPlugin(PluginConfiguration pPluginConfiguration, final PluginParameter... dynamicPluginParameters)
            throws ModuleException;

    /**
     * Get the first plugin instance of a plugin type. The pReturnInterfaceType attribute indicates the PluginInterface
     * return type.
     *
     * @param <T> a plugin instance
     * @param pInterfacePluginType a specific interface plugin type
     * @param pPluginParameters an optional list of {@link PluginParameter}
     * @return a plugin instance
     * @throws ModuleException thrown if an error occurs
     */
    <T> T getFirstPluginByType(Class<?> pInterfacePluginType, final PluginParameter... pPluginParameters)
            throws ModuleException;

    /**
     * Get a specific plugin implementation.
     *
     * @param pPluginImplId the id of the specific metadata
     * @return a {@link PluginMetaData} for a specific {@link PluginConfiguration}
     */
    PluginMetaData getPluginMetaDataById(String pPluginImplId);

    /**
     * Save a {@link PluginConfiguration} in internal database.
     *
     * @param pPluginConfiguration the plugin configuration to saved
     * @return the saved {@link PluginConfiguration}
     * @throws ModuleException thrown if an error occurs
     */
    PluginConfiguration savePluginConfiguration(PluginConfiguration pPluginConfiguration) throws ModuleException;

    /**
     * Delete a {@link PluginConfiguration}.
     *
     * @param pConfId a specific configuration
     * @return
     * @throws ModuleException Entity to delete does not exist
     */
    void deletePluginConfiguration(Long pConfId) throws ModuleException;

    /**
     * Update a {@link PluginConfiguration}.
     *
     * @param pPlugin the {@link PluginConfiguration} to update
     * @return the updated {@link PluginConfiguration}
     * @throws ModuleException plugin to update does not exists
     */
    PluginConfiguration updatePluginConfiguration(PluginConfiguration pPlugin) throws ModuleException;

    /**
     * Get the {@link PluginConfiguration}.
     *
     * @param pId a plugin identifier
     * @return a specific configuration
     * @throws ModuleException thrown if we cannot find any PluginConfiguration corresponding to pId
     */
    PluginConfiguration getPluginConfiguration(Long pId) throws ModuleException;

    /**
     * Load a PluginConfiguration with all its relations
     */
    PluginConfiguration loadPluginConfiguration(Long id);

    /**
     * Does given PluginConfiguration exist ?
     * @param pId PluginConfiguration id to test
     * @return true or false (it's a boolean !!!)
     */
    boolean exists(Long pId);

    /**
     * Does given PluginConfiguration exist ?
     * @param pluginConfLabel PluginConfiguration label to test
     * @return true or false (it's a boolean !!!)
     */
    boolean existsByLabel(String pluginConfLabel);

    /**
     * Get all plugin's configuration for a specific plugin type.
     *
     * @param pInterfacePluginType a specific interface plugin type
     * @return all the {@link PluginConfiguration} for a specific plugin type.
     */
    List<PluginConfiguration> getPluginConfigurationsByType(Class<?> pInterfacePluginType);

    /**
     * Get all plugin's configuration.
     *
     * @return all the {@link PluginConfiguration}.
     */
    List<PluginConfiguration> getAllPluginConfigurations();

    /**
     * Get all plugin's configuration for a specific plugin Id.
     *
     * @param pPluginId a specific plugin Id
     * @return all the {@link PluginConfiguration} for a specific plugin Id
     */
    List<PluginConfiguration> getPluginConfigurations(String pPluginId);

    /**
     * Add a package to scan to find the plugins.
     *
     * @param pPluginPackage A package name to scan to find the plugins.
     */
    public void addPluginPackage(String pPluginPackage);

    /**
     * Get {@link PluginMetaData} for a plugin of a specific plugin type.</br>
     * If the plugin class name does not match a plugin of the plugin type, a exception is thrown.
     *
     * @param pClass the plugin type
     * @param pPluginClassName a plugin class name
     * @return the {@link PluginMetaData} of the plugin of plugin type
     * @throws EntityInvalidException Any plugin of plugin type is find.
     */
    public PluginMetaData checkPluginClassName(Class<?> pClass, String pPluginClassName) throws EntityInvalidException;

    /**
     * Get a PluginConfiguration according to its unique label
     *
     * @param pConfigurationLabel the configuration label
     * @return the plugin configuration
     * @throws EntityNotFoundException
     */
    PluginConfiguration getPluginConfigurationByLabel(String pConfigurationLabel) throws EntityNotFoundException;

    /**
     * Add plugin instance to cache (resolving tenant internally)
     * @param confId configuration identifier
     * @param plugin plugin instance corresponding to the configuration
     */
    void addPluginToCache(Long confId, Object plugin);

    /**
     * Check if plugin is cached (resolving tenant internally)
     * @param confId configuration identifier
     * @return
     */
    boolean isPluginCached(Long confId);

    /**
     * Remove plugin instance cache with specified configuration identifier (resolving tenant internally)
     * @param confId configuration identifier
     */
    void cleanPluginCache(Long confId);

    /**
     * @return tenant plugin cache
     */
    ConcurrentMap<Long, Object> getPluginCache();

    /**
     *
     * @param confId configuration identifier
     * @return tenant plugin instance
     */
    Object getCachedPlugin(Long confId);
}