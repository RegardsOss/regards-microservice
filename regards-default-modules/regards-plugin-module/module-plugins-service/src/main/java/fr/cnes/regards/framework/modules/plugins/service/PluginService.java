/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.framework.modules.plugins.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;

import com.google.common.collect.Lists;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.event.PluginConfigurationEvent;
import fr.cnes.regards.framework.modules.plugins.domain.event.PluginServiceAction;
import fr.cnes.regards.plugins.utils.PluginInterfaceUtils;
import fr.cnes.regards.plugins.utils.PluginUtils;

/**
 * The implementation of {@link IPluginService}.
 *
 * @author Christophe Mertz
 * @author Sébastien Binda
 */
@MultitenantTransactional
public class PluginService implements IPluginService {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginService.class);

    @Value("${regards.plugins.packages-to-scan:#{null}}")
    private String[] packagesToScan;

    /**
     * The plugin's package to scan
     */
    private List<String> pluginPackage;

    /**
     * {@link PluginConfiguration} JPA Repository
     */
    private final IPluginConfigurationRepository pluginConfRepository;

    /**
     * Plugins implementation list sorted by plugin id. Plugin id, is the id of the "@PluginMetaData" annotation of the
     * implementation class.
     */
    private Map<String, PluginMetaData> plugins;

    /**
     * A {@link Map} with all the {@link Plugin} currently instantiate.</br>
     * This Map is used because for a {@link PluginConfiguration}, one and only one {@link Plugin} should be
     * instantiate.
     */
    private final Map<Long, Object> instantiatePlugins = new HashMap<Long, Object>();

    private final IPublisher publisher;

    public PluginService(final IPluginConfigurationRepository pPluginConfigurationRepository,
            final IPublisher publisher) {
        this.pluginConfRepository = pPluginConfigurationRepository;
        this.publisher = publisher;
    }

    private Map<String, PluginMetaData> getLoadedPlugins() {
        if (plugins == null) {
            plugins = PluginUtils.getPlugins(getPluginPackage());
        }
        return plugins;
    }

    @Override
    public List<String> getPluginTypes() {
        return PluginInterfaceUtils.getInterfaces(getPluginPackage());
    }

    @Override
    public List<PluginMetaData> getPlugins() {
        return getPluginsByType(null);
    }

    @Override
    public List<PluginMetaData> getPluginsByType(final Class<?> pInterfacePluginType) {
        final List<PluginMetaData> pluginAvailables = new ArrayList<>();

        getLoadedPlugins().forEach((pKey, pValue) -> {
            try {
                if ((pInterfacePluginType == null) || ((pInterfacePluginType != null)
                        && pInterfacePluginType.isAssignableFrom(Class.forName(pValue.getPluginClassName())))) {
                    pluginAvailables.add(pValue);
                }
            } catch (final ClassNotFoundException e) {
                LOGGER.error("cannot instantiate the class : %s" + pValue.getPluginClassName(), e);
            }
        });

        return pluginAvailables;
    }

    @Override
    public PluginConfiguration savePluginConfiguration(final PluginConfiguration pPluginConfiguration)
            throws ModuleException {
        // Check plugin configuration validity
        final StringBuilder msg = new StringBuilder("Impossible to save a plugin configuration");

        boolean throwError = false;

        if (pPluginConfiguration == null) {
            msg.append(". The plugin configuration cannot be null.");
            throwError = true;
        }
        if (!throwError && (pPluginConfiguration.getPluginId() == null)) {
            msg.append(". The unique identifier of the plugin (attribute pluginId) is required.");
            throwError = true;
        }
        if (!throwError && (pPluginConfiguration.getPriorityOrder() == null)) {
            msg.append(String.format(" <%s> without priority order.", pPluginConfiguration.getPluginId()));
            throwError = true;
        }
        if (!throwError && (pPluginConfiguration.getVersion() == null)) {
            msg.append(String.format(" <%s> without version.", pPluginConfiguration.getPluginId()));
            throwError = true;
        }
        if (!throwError && ((pPluginConfiguration.getLabel() == null) || pPluginConfiguration.getLabel().isEmpty())) {
            msg.append(String.format(" <%s> without label.", pPluginConfiguration.getPluginId()));
        }

        if (throwError) {
            throw new ModuleException(msg.toString());
        }

        getLoadedPlugins().forEach((pKey, pValue) -> {
            if (pValue.getPluginClassName().equals(pPluginConfiguration.getPluginClassName())) {
                pPluginConfiguration.setInterfaceNames(pValue.getInterfaceNames());
            }
        });

        final boolean shouldPublishCreation = pPluginConfiguration.getId() == null;

        final PluginConfiguration newConf = pluginConfRepository.save(pPluginConfiguration);
        if (shouldPublishCreation) {
            publisher.publish(new PluginConfigurationEvent(newConf.getId(), PluginServiceAction.CREATE,
                    newConf.getInterfaceNames()));
        }

        return newConf;
    }

    @Override
    public PluginConfiguration getPluginConfiguration(final Long pId) throws ModuleException {
        if (!pluginConfRepository.exists(pId)) {
            LOGGER.error(String.format("Error while getting the plugin configuration <%s>.", pId));
            throw new EntityNotFoundException(pId, PluginConfiguration.class);
        }
        return pluginConfRepository.findOne(pId);
    }

    @Override
    public PluginConfiguration loadPluginConfiguration(final Long id) {
        return pluginConfRepository.findById(id);
    }

    @Override
    public boolean exists(final Long pId) {
        return pluginConfRepository.exists(pId);
    }

    @Override
    public PluginConfiguration updatePluginConfiguration(final PluginConfiguration pPluginConf) throws ModuleException {
        final PluginConfiguration oldConf = pluginConfRepository.findById(pPluginConf.getId());
        if (oldConf == null) {
            LOGGER.error(String.format("Error while updating the plugin configuration <%d>.", pPluginConf.getId()));
            throw new EntityNotFoundException(pPluginConf.getId().toString(), PluginConfiguration.class);
        }
        final boolean oldConfActive = oldConf.isActive();
        final PluginConfiguration newPluginConfiguration = savePluginConfiguration(pPluginConf);

        if (oldConfActive != newPluginConfiguration.isActive()) {
            if (newPluginConfiguration.isActive()) {
                publisher.publish(new PluginConfigurationEvent(pPluginConf.getId(), PluginServiceAction.ACTIVATE,
                        newPluginConfiguration.getInterfaceNames()));
            } else {
                publisher.publish(new PluginConfigurationEvent(pPluginConf.getId(), PluginServiceAction.DESACTIVATE,
                        newPluginConfiguration.getInterfaceNames()));
            }
        }
        /**
         * Remove the PluginConfiguratin from the map
         */
        instantiatePlugins.remove(pPluginConf.getId());

        return newPluginConfiguration;
    }

    @Override
    public void deletePluginConfiguration(final Long pConfId) throws ModuleException {
        final PluginConfiguration toDelete = pluginConfRepository.findOne(pConfId);
        if (toDelete == null) {
            LOGGER.error(String.format("Error while deleting the plugin configuration <%d>.", pConfId));
            throw new EntityNotFoundException(pConfId.toString(), PluginConfiguration.class);
        }
        publisher.publish(new PluginConfigurationEvent(pConfId, PluginServiceAction.DELETE,
                toDelete.getInterfaceNames()));
        pluginConfRepository.delete(pConfId);

        /**
         * Remove the PluginConfiguratin from the map
         */
        instantiatePlugins.remove(pConfId);
    }

    @Override
    public List<PluginConfiguration> getPluginConfigurationsByType(final Class<?> pInterfacePluginType) {

        final Iterable<PluginConfiguration> plgConfs = pluginConfRepository.findAll();
        if (plgConfs != null) {
            return Lists.newArrayList(plgConfs).stream()
                    .filter(pc -> pc.getInterfaceNames().contains(pInterfacePluginType.getName()))
                    .collect(Collectors.toList());
        } else {
            return Lists.newArrayList();
        }
    }

    @Override
    public List<PluginConfiguration> getPluginConfigurations(final String pPluginId) {
        return pluginConfRepository.findByPluginIdOrderByPriorityOrderDesc(pPluginId);
    }

    @Override
    public PluginMetaData getPluginMetaDataById(final String pPluginImplId) {
        return getLoadedPlugins().get(pPluginImplId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getFirstPluginByType(final Class<?> pInterfacePluginType, final PluginParameter... pPluginParameters)
            throws ModuleException {

        // Get plugins configuration for given type
        final List<PluginConfiguration> confs = getPluginConfigurationsByType(pInterfacePluginType);

        if (confs.isEmpty()) {
            throw new ModuleException(String.format("No plugin configuration defined for the type <%s>.",
                                                    pInterfacePluginType.getName()));
        }

        // Search the configuration the most priority
        PluginConfiguration configuration = null;

        for (final PluginConfiguration conf : confs) {
            if ((configuration == null) || (conf.getPriorityOrder() < configuration.getPriorityOrder())) {
                configuration = conf;
            }
        }

        // Get the plugin associated to this configuration
        T resultPlugin = null;

        if (configuration != null) {
            if (!instantiatePlugins.containsKey(configuration.getId())
                    || (instantiatePlugins.containsKey(configuration.getId()) && (pPluginParameters.length > 0))) {

                resultPlugin = getPlugin(configuration.getId(), pPluginParameters);

                // Put in the map, only if there is no dynamic parameters
                if (pPluginParameters.length == 0) {
                    instantiatePlugins.put(configuration.getId(), resultPlugin);
                }

            } else {
                resultPlugin = (T) instantiatePlugins.get(configuration.getId());
            }
        }

        return resultPlugin;
    }

    @Override
    public <T> T getPlugin(final PluginConfiguration pPluginConfiguration) throws ModuleException {
        return getPlugin(pPluginConfiguration.getId(),
                         pPluginConfiguration.getParameters().toArray(new PluginParameter[0]));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getPlugin(final Long pPluginConfigurationId, final PluginParameter... pPluginParameters)
            throws ModuleException {
        // Get the plugin associated to this configuration
        T resultPlugin;

        if (!instantiatePlugins.containsKey(pPluginConfigurationId)
                || (instantiatePlugins.containsKey(pPluginConfigurationId) && (pPluginParameters.length > 0))) {

            // Get last saved plugin configuration
            final PluginConfiguration pluginConf = getPluginConfiguration(pPluginConfigurationId);

            // Get the plugin implementation associated
            final PluginMetaData pluginMetadata = getLoadedPlugins().get(pluginConf.getPluginId());

            // Check if plugin version has changed since the last saved configuration of the plugin
            if ((pluginConf.getVersion() != null) && !pluginConf.getVersion().equals(pluginMetadata.getVersion())) {
                LOGGER.warn(String.format("Plugin version <%s> changed since last configuration <%s>.",
                                          pluginConf.getVersion(), pluginMetadata.getVersion()));
            }

            resultPlugin = PluginUtils.getPlugin(pluginConf, pluginMetadata, getPluginPackage(), pPluginParameters);

            // Put in the map, only if there is no dynamic parameters
            if (pPluginParameters.length == 0) {
                instantiatePlugins.put(pPluginConfigurationId, resultPlugin);
            }

        } else {
            resultPlugin = (T) instantiatePlugins.get(pPluginConfigurationId);
        }

        return resultPlugin;
    }

    @Override
    public List<PluginConfiguration> getAllPluginConfigurations() {
        final Iterable<PluginConfiguration> confs = pluginConfRepository.findAll();
        return (confs != null) ? Lists.newArrayList(confs) : Collections.emptyList();
    }
    
    private List<String> getPluginPackage() {
        // Manage scan packages
        if (pluginPackage == null) {
            pluginPackage = new ArrayList<>();
            if ((packagesToScan != null) && (packagesToScan.length > 0)) {
                for (String packageToScan : packagesToScan) {
                    pluginPackage.add(packageToScan);
                }
            }
        }
        return pluginPackage;
    }

    @Override
    @MultitenantTransactional(propagation = Propagation.SUPPORTS)
    public void addPluginPackage(final String pPluginPackage) {
        if (!getPluginPackage().contains(pPluginPackage)) {
            getPluginPackage().add(pPluginPackage);
            final Map<String, PluginMetaData> newPlugins = PluginUtils.getPlugins(pPluginPackage, getPluginPackage());
            if (plugins == null) {
                // in case the plugin service has been initialized with PluginService(IPluginRepository) constructor
                plugins = new HashMap<>();
            }
            plugins.putAll(newPlugins);
        }
    }

    @Override
    public PluginMetaData checkPluginClassName(final Class<?> pClass, final String pPluginClassName)
            throws EntityInvalidException {
        PluginMetaData metaData = null;
        boolean isFound = false;

        // Search all the plugins of type pClass
        for (final PluginMetaData pMd : getPluginsByType(pClass)) {
            if (!isFound && pMd.getPluginClassName().equals(pPluginClassName)) {
                isFound = true;
                metaData = pMd;
            }
        }

        if (!isFound) {
            throw new EntityInvalidException("Any plugin's type match the plugin class name : " + pPluginClassName);
        }

        return metaData;
    }

    @Override
    public PluginConfiguration getPluginConfigurationByLabel(final String pConfigurationLabel)
            throws EntityNotFoundException {
        final PluginConfiguration conf = pluginConfRepository.findOneByLabel(pConfigurationLabel);
        if (conf == null) {
            throw new EntityNotFoundException(pConfigurationLabel, PluginConfiguration.class);
        }
        return conf;
    }

    /* (non-Javadoc)
     * @see fr.cnes.regards.framework.modules.plugins.service.IPluginService#cleanPluginCache(java.lang.Long)
     */
    @Override
    public void cleanPluginCache(Long pConfId) {
        if (pConfId != null) {
            instantiatePlugins.remove(pConfId);
        }
    }

}
