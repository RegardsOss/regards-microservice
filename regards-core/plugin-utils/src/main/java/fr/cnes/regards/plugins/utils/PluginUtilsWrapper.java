/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.plugins.utils;

import java.util.List;

import fr.cnes.regards.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.modules.plugins.domain.PluginParameter;

/**
 *
 * This class is intended to be used for test purpose only. It uses the core plugin mechanism to instantiate a plugin so
 * the test should be as realistic as possible.
 *
 * @author cmertz
 */
public final class PluginUtilsWrapper {

    /**
     * Default constructor
     */
    private PluginUtilsWrapper() {
    }

    /**
     *
     * Create an instance of plugin based on its configuration and metadata
     *
     * @param <T>
     *            a plugin
     * @param pParameters
     *            the plugin parameters
     * @param pReturnInterfaceType
     *            the required returned type
     * @param pPluginParameters
     *            an optional list of {@link PluginParameter}
     * 
     * @return an instance
     * @throws PluginUtilsException
     *             if problem occurs
     */
    public static <T> T getPlugin(final List<PluginParameter> pParameters, final Class<T> pReturnInterfaceType,
            final PluginParameter... pPluginParameters) throws PluginUtilsException {
        // Build plugin metadata
        final PluginMetaData pluginMetadata = PluginUtils.createPluginMetaData(pReturnInterfaceType);

        final PluginConfiguration pluginConfiguration = new PluginConfiguration(pluginMetadata, "", pParameters, 0);
        return PluginUtils.getPlugin(pluginConfiguration, pluginMetadata, pPluginParameters);
    }

    /**
     *
     * Create an instance of plugin configuration
     *
     * @param <T>
     *            a plugin
     * @param pParameters
     *            the plugin parameters
     * @param pReturnInterfaceType
     *            the required returned type
     * 
     * @return an instance
     * @throws PluginUtilsException
     *             if problem occurs
     */
    public static <T> PluginConfiguration getPluginConfiguration(final List<PluginParameter> pParameters,
            final Class<T> pReturnInterfaceType) throws PluginUtilsException {
        // Build plugin metadata
        final PluginMetaData pluginMetadata = PluginUtils.createPluginMetaData(pReturnInterfaceType);

        return new PluginConfiguration(pluginMetadata, "", pParameters, 0);
    }

}