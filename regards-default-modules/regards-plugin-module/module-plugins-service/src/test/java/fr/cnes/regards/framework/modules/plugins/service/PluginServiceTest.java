/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.framework.modules.plugins.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.plugins.IComplexInterfacePlugin;
import fr.cnes.regards.framework.plugins.SamplePlugin;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.framework.test.report.annotation.Requirements;

/**
 *
 * Unit testing of {@link PluginService}.
 *
 * @author Christophe Mertz
 * @author Sébastien Binda
 */
public class PluginServiceTest extends PluginServiceUtility {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginServiceTest.class);

    private final String pluginsPackage = "fr.cnes.regards.plugins.utils";

    /**
     *
     */
    private IPluginConfigurationRepository pluginConfRepositoryMocked;

    /**
     *
     */
    private IPluginService pluginServiceMocked;

    /**
     * This method is run before all tests
     */
    @Before
    public void init() {
        // create a mock repository
        pluginConfRepositoryMocked = Mockito.mock(IPluginConfigurationRepository.class);
        pluginServiceMocked = new PluginService(pluginConfRepositoryMocked);
        pluginServiceMocked.addPluginPackage("fr.cnes.regards.plugins");
        pluginServiceMocked.addPluginPackage("fr.cnes.regards.framework.plugins");
        pluginServiceMocked.addPluginPackage("fr.cnes.regards.framework.modules.plugins");
    }

    @Test
    @Requirement("REGARDS_DSL_CMZ_PLG_200")
    @Purpose("Load all plugin's metada.")
    public void getAllPlugins() {
        final List<PluginMetaData> metadaDatas = pluginServiceMocked.getPlugins();

        Assert.assertNotNull(metadaDatas);
        Assert.assertFalse(metadaDatas.isEmpty());

        LOGGER.debug("List all plugins :");
        metadaDatas.forEach(p -> LOGGER.debug(p.getPluginId()));
    }

    @Test
    public void getAllPluginTypes() {
        final List<String> types = pluginServiceMocked.getPluginTypes();

        Assert.assertNotNull(types);
        Assert.assertFalse(types.isEmpty());

        LOGGER.debug("List all plugin types :");
        types.forEach(p -> LOGGER.debug(p));
    }

    @Test
    @Requirement("REGARDS_DSL_CMZ_PLG_200")
    @Purpose("Load all plugin's metada for a specific plugin type identified by a Class.")
    public void getPluginOneType() {
        pluginServiceMocked.addPluginPackage("fr.cnes.regards.mypackage");
        final List<PluginMetaData> plugins = pluginServiceMocked.getPluginsByType(IComplexInterfacePlugin.class);

        Assert.assertNotNull(plugins);
        Assert.assertFalse(plugins.isEmpty());

        LOGGER.debug("List all plugins of type <IComplexInterfacePlugin.class> :");
        plugins.forEach(p -> LOGGER.debug(p.getPluginId()));
    }

    @Test
    @Requirement("REGARDS_DSL_CMZ_PLG_200")
    @Purpose("Load all plugin's metada for a specific plugin type identified by a class name.")
    public void getPluginTypesByString() {
        final String aClass = "fr.cnes.regards.framework.plugins.IComplexInterfacePlugin";
        List<PluginMetaData> plugins = null;

        try {
            plugins = pluginServiceMocked.getPluginsByType(Class.forName(aClass));
        } catch (final ClassNotFoundException e) {
            Assert.fail();
        }

        Assert.assertNotNull(plugins);
        Assert.assertFalse(plugins.isEmpty());

        LOGGER.debug(String.format("List all plugins of type <%s> :", aClass));
        plugins.forEach(p -> LOGGER.debug(p.getPluginId()));
    }

    /**
     * Get a {@link PluginConfiguration}.
     * 
     * @throws ModuleException
     */
    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_100")
    @Purpose("Get a plugin configuration identified by an identifier.")
    public void getAPluginConfiguration() {
        try {
            final PluginConfiguration aPluginConfiguration = getPluginConfigurationWithParameters();
            aPluginConfiguration.setId(AN_ID);
            Mockito.when(pluginConfRepositoryMocked.findOne(aPluginConfiguration.getId()))
                    .thenReturn(aPluginConfiguration);
            Mockito.when(pluginConfRepositoryMocked.exists(aPluginConfiguration.getId())).thenReturn(true);

            PluginConfiguration aConf;
            aConf = pluginServiceMocked.getPluginConfiguration(aPluginConfiguration.getId());
            Assert.assertEquals(aConf.getLabel(), aPluginConfiguration.getLabel());
        } catch (ModuleException e) {
            Assert.fail();
        }
    }

    /**
     * Delete a {@link PluginConfiguration}.
     */
    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_100")
    @Purpose("Delete a plugin configuration identified by an identifier")
    public void deleteAPluginConfiguration() {
        final Long aPlugnId = 3456L;
        try {
            Mockito.when(pluginConfRepositoryMocked.exists(aPlugnId)).thenReturn(true);
            pluginServiceMocked.deletePluginConfiguration(aPlugnId);
            Mockito.verify(pluginConfRepositoryMocked).delete(aPlugnId);
        } catch (final ModuleException e) {
            Assert.fail();
        }
    }

    /**
     * Delete a {@link PluginConfiguration}.
     */
    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_100")
    @Purpose("Delete a plugin configuration identified by an identifier")
    public void deleteAPluginConfigurationError() {
        final Long aPlugnId = 3457L;
        try {
            Mockito.when(pluginConfRepositoryMocked.exists(aPlugnId)).thenReturn(true);
            pluginServiceMocked.deletePluginConfiguration(aPlugnId);
            Mockito.verify(pluginConfRepositoryMocked).delete(aPlugnId);
        } catch (final ModuleException e) {
            Assert.fail();
        }
    }

    /**
     * Save a {@link PluginConfiguration}.
     */
    @Test
    @Requirements({ @Requirement("REGARDS_DSL_SYS_ARC_100"), @Requirement("REGARDS_DSL_CMP_PLG_320") })
    @Purpose("Create a new plugin configuration")
    public void saveAPluginConfiguration() {
        final PluginConfiguration aPluginConfiguration = getPluginConfigurationWithParameters();
        final PluginConfiguration aPluginConfigurationWithId = getPluginConfigurationWithParameters();
        aPluginConfigurationWithId.setId(AN_ID);
        try {
            Mockito.when(pluginConfRepositoryMocked.save(aPluginConfiguration)).thenReturn(aPluginConfigurationWithId);
            final PluginConfiguration savedPluginConfiguration = pluginServiceMocked
                    .savePluginConfiguration(aPluginConfiguration);
            Assert.assertEquals(aPluginConfiguration.getLabel(), savedPluginConfiguration.getLabel());
            Assert.assertEquals(aPluginConfiguration.getPluginId(), savedPluginConfiguration.getPluginId());
            Assert.assertEquals(aPluginConfiguration.isActive(), savedPluginConfiguration.isActive());
            Assert.assertEquals(aPluginConfiguration.getParameters().size(),
                                savedPluginConfiguration.getParameters().size());
        } catch (final ModuleException e) {
            Assert.fail();
        }
    }

    /**
     * Update a {@link PluginConfiguration}.
     * 
     * @throws ModuleException
     */
    @Test
    @Requirements({ @Requirement("REGARDS_DSL_SYS_ARC_100"), @Requirement("REGARDS_DSL_CMP_PLG_100") })
    @Purpose("Update a plugin configuration identified by an identifier")
    public void updateAPluginConfiguration() throws ModuleException {
        final PluginConfiguration aPluginConfiguration = getPluginConfigurationWithParameters();
        aPluginConfiguration.setId(AN_ID);
        Mockito.when(pluginConfRepositoryMocked.exists(aPluginConfiguration.getId())).thenReturn(true);
        Mockito.when(pluginConfRepositoryMocked.findOne(aPluginConfiguration.getId())).thenReturn(aPluginConfiguration);
        Mockito.when(pluginConfRepositoryMocked.save(aPluginConfiguration)).thenReturn(aPluginConfiguration);

        final PluginConfiguration updatedConf = pluginServiceMocked.updatePluginConfiguration(aPluginConfiguration);
        Assert.assertEquals(updatedConf.getLabel(), aPluginConfiguration.getLabel());
        Assert.assertEquals(updatedConf.getPluginId(), aPluginConfiguration.getPluginId());
    }

    @Test
    @Requirement("REGARDS_DSL_CMZ_PLG_200")
    @Purpose("Load a plugin's metada for a specific plugin type identified by a plugin identifier.")
    public void getPluginMetaDataById() {
        final PluginMetaData pluginMetaData = pluginServiceMocked.getPluginMetaDataById("aSamplePlugin");
        Assert.assertNotNull(pluginMetaData);
    }

    @Test
    public void getPluginConfigurationsByTypeWithClass() {
        final List<PluginConfiguration> pluginConfs = new ArrayList<>();
        pluginConfs.add(getPluginConfigurationWithParameters());
        pluginConfs.add(getPluginConfigurationWithDynamicParameter());
        Mockito.when(pluginConfRepositoryMocked.findByPluginIdOrderByPriorityOrderDesc(PLUGIN_PARAMETER_ID))
                .thenReturn(pluginConfs);
        final List<PluginConfiguration> results = pluginServiceMocked
                .getPluginConfigurationsByType(IComplexInterfacePlugin.class);

        Assert.assertNotNull(results);
        Assert.assertEquals(pluginConfs.size(), results.size());
    }

    @Test
    public void getPluginConfigurationsByTypeWithPluginId() {
        final List<PluginConfiguration> pluginConfs = new ArrayList<>();
        pluginConfs.add(getPluginConfigurationWithParameters());
        pluginConfs.add(getPluginConfigurationWithDynamicParameter());
        Mockito.when(pluginConfRepositoryMocked.findByPluginIdOrderByPriorityOrderDesc(PLUGIN_PARAMETER_ID))
                .thenReturn(pluginConfs);
        final List<PluginConfiguration> results = pluginServiceMocked
                .getPluginConfigurations(PLUGIN_PARAMETER_ID);

        Assert.assertNotNull(results);
        Assert.assertEquals(pluginConfs.size(), results.size());
    }

    @Test
    public void getAllPluginConfigurations() {
        final List<PluginConfiguration> pluginConfs = new ArrayList<>();
        pluginConfs.add(getPluginConfigurationWithParameters());
        pluginConfs.add(getPluginConfigurationWithDynamicParameter());
        Mockito.when(pluginConfRepositoryMocked.findAll()).thenReturn(pluginConfs);
        final List<PluginConfiguration> results = pluginServiceMocked.getAllPluginConfigurations();

        Assert.assertNotNull(results);
        Assert.assertEquals(pluginConfs.size(), results.size());
    }

    /**
     * Get the first plugin of a specific type
     *
     * @throws ModuleException
     *             throw if an error occurs
     * @throws ModuleException
     */
    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_120")
    @Purpose("Load a plugin from a specific type with a configuration and execute a method.")
    public void getFirstPluginByType() throws ModuleException {
        final List<PluginConfiguration> pluginConfs = new ArrayList<>();
        final PluginConfiguration aPluginConfiguration = getPluginConfigurationWithParameters();
        aPluginConfiguration.setId(AN_ID);

        pluginConfs.add(aPluginConfiguration);
        pluginConfs.add(getPluginConfigurationWithDynamicParameter());

        Mockito.when(pluginConfRepositoryMocked.findByPluginIdOrderByPriorityOrderDesc(PLUGIN_PARAMETER_ID))
                .thenReturn(pluginConfs);
        Mockito.when(pluginConfRepositoryMocked.findOne(aPluginConfiguration.getId())).thenReturn(aPluginConfiguration);
        Mockito.when(pluginConfRepositoryMocked.exists(aPluginConfiguration.getId())).thenReturn(true);

        pluginServiceMocked.addPluginPackage(pluginsPackage);
        final SamplePlugin aSamplePlugin = pluginServiceMocked.getFirstPluginByType(IComplexInterfacePlugin.class);

        Assert.assertNotNull(aSamplePlugin);

        final int result = aSamplePlugin.add(QUATRE, CINQ);
        LOGGER.debug(RESULT + result);
        Assert.assertTrue(result > 0);
        Assert.assertTrue(aSamplePlugin.echo(HELLO).contains(HELLO));
    }

    /**
     * Get twice a specific Plugin with the same PluginConfiguration
     *
     * @throws ModuleException
     *             throw if an error occurs
     */
    @Test
    @Purpose("Load twice a plugin with the same configuration.")
    public void getExistingFirstPluginByType() throws ModuleException {
        final List<PluginConfiguration> pluginConfs = new ArrayList<>();
        final PluginConfiguration aPluginConfiguration = getPluginConfigurationWithParameters();
        aPluginConfiguration.setId(AN_ID);

        pluginConfs.add(aPluginConfiguration);
        pluginConfs.add(getPluginConfigurationWithDynamicParameter());

        Mockito.when(pluginConfRepositoryMocked.findByPluginIdOrderByPriorityOrderDesc(PLUGIN_PARAMETER_ID))
                .thenReturn(pluginConfs);
        Mockito.when(pluginConfRepositoryMocked.findOne(aPluginConfiguration.getId())).thenReturn(aPluginConfiguration);
        Mockito.when(pluginConfRepositoryMocked.exists(aPluginConfiguration.getId())).thenReturn(true);

        pluginServiceMocked.addPluginPackage(pluginsPackage);
        final SamplePlugin aSamplePlugin = pluginServiceMocked.getFirstPluginByType(IComplexInterfacePlugin.class);
        Assert.assertNotNull(aSamplePlugin);

        final SamplePlugin bSamplePlugin = pluginServiceMocked.getFirstPluginByType(IComplexInterfacePlugin.class);
        Assert.assertNotNull(bSamplePlugin);

        Assert.assertEquals(aSamplePlugin.add(5, 3), bSamplePlugin.add(5, 3));
        Assert.assertEquals(aSamplePlugin.echo(GREEN), bSamplePlugin.echo(GREEN));

        Assert.assertEquals(aSamplePlugin, bSamplePlugin);
    }

    /**
     * Get twice a specific Plugin with the same PluginConfiguration with a dynamic parameter
     *
     * @throws ModuleException
     *             throw if an error occurs
     */
    @Test
    @Purpose("Load a plugin twice from a specific type with a configuration.")
    public void getExistingFirstPluginByTypeWithDynamicParameter() throws ModuleException {
        final List<PluginConfiguration> pluginConfs = new ArrayList<>();
        final PluginConfiguration aPluginConfiguration = getPluginConfigurationWithDynamicParameter();
        aPluginConfiguration.setId(AN_ID);

        pluginConfs.add(aPluginConfiguration);
        pluginConfs.add(getPluginConfigurationWithParameters());

        Mockito.when(pluginConfRepositoryMocked.findByPluginIdOrderByPriorityOrderDesc(PLUGIN_PARAMETER_ID))
                .thenReturn(pluginConfs);
        Mockito.when(pluginConfRepositoryMocked.findOne(aPluginConfiguration.getId())).thenReturn(aPluginConfiguration);
        Mockito.when(pluginConfRepositoryMocked.exists(aPluginConfiguration.getId())).thenReturn(true);

        // the argument for the dynamic parameter
        final PluginParameter aDynamicPlgParam = PluginParametersFactory.build()
                .addParameter(SamplePlugin.SUFFIXE, BLUE).getParameters().get(0);

        pluginServiceMocked.addPluginPackage(pluginsPackage);
        final SamplePlugin aSamplePlugin = pluginServiceMocked.getFirstPluginByType(IComplexInterfacePlugin.class,
                                                                                    aDynamicPlgParam);
        Assert.assertNotNull(aSamplePlugin);

        final SamplePlugin bSamplePlugin = pluginServiceMocked.getFirstPluginByType(IComplexInterfacePlugin.class,
                                                                                    aDynamicPlgParam);
        Assert.assertNotNull(bSamplePlugin);
        Assert.assertNotEquals(aSamplePlugin, bSamplePlugin);
    }

    /**
     * Get twice a specific Plugin with the same PluginConfiguration with a dynamic parameter the second time
     *
     * @throws ModuleException
     *             throw if an error occurs
     */
    @Test
    @Purpose("Load a plugin twice from a specific type with a configuration.")
    public void getExistingFirstPluginByTypeWithDynamicParameter2() throws ModuleException {
        final List<PluginConfiguration> pluginConfs = new ArrayList<>();
        final PluginConfiguration aPluginConfiguration = getPluginConfigurationWithDynamicParameter();
        aPluginConfiguration.setId(AN_ID);

        pluginConfs.add(aPluginConfiguration);
        pluginConfs.add(getPluginConfigurationWithParameters());

        Mockito.when(pluginConfRepositoryMocked.findByPluginIdOrderByPriorityOrderDesc(PLUGIN_PARAMETER_ID))
                .thenReturn(pluginConfs);
        Mockito.when(pluginConfRepositoryMocked.findOne(aPluginConfiguration.getId())).thenReturn(aPluginConfiguration);
        Mockito.when(pluginConfRepositoryMocked.exists(aPluginConfiguration.getId())).thenReturn(true);

        // the argument for the dynamic parameter
        final PluginParameter aDynamicPlgParam = PluginParametersFactory.build()
                .addParameter(SamplePlugin.SUFFIXE, BLUE).getParameters().get(0);

        pluginServiceMocked.addPluginPackage(pluginsPackage);
        final SamplePlugin aSamplePlugin = pluginServiceMocked.getFirstPluginByType(IComplexInterfacePlugin.class);
        Assert.assertNotNull(aSamplePlugin);

        final SamplePlugin bSamplePlugin = pluginServiceMocked.getFirstPluginByType(IComplexInterfacePlugin.class,
                                                                                    aDynamicPlgParam);
        Assert.assertNotNull(bSamplePlugin);
        Assert.assertNotEquals(aSamplePlugin, bSamplePlugin);
    }

    /**
     * Get the first plugin of a specific type with a specific parameter
     *
     * @throws ModuleException
     *             throw if an error occurs
     */
    @Test
    @Requirements({ @Requirement("REGARDS_DSL_SYS_ARC_120"), @Requirement("REGARDS_DSL_CMP_PLG_120"),
            @Requirement("REGARDS_DSL_CMP_PLG_310") })
    @Purpose("Load a plugin with a dynamic parameter from a specific type with a configuration and execute a method.")
    public void getFirstPluginByTypeWithADynamicParameter() throws ModuleException {
        final List<PluginConfiguration> pluginConfs = new ArrayList<>();
        final PluginConfiguration aPluginConfiguration = getPluginConfigurationWithParameters();
        aPluginConfiguration.setId(AN_ID);

        pluginConfs.add(aPluginConfiguration);
        pluginConfs.add(getPluginConfigurationWithDynamicParameter());

        Mockito.when(pluginConfRepositoryMocked.findByPluginIdOrderByPriorityOrderDesc(PLUGIN_PARAMETER_ID))
                .thenReturn(pluginConfs);
        Mockito.when(pluginConfRepositoryMocked.findOne(aPluginConfiguration.getId())).thenReturn(aPluginConfiguration);
        Mockito.when(pluginConfRepositoryMocked.exists(aPluginConfiguration.getId())).thenReturn(true);

        // the argument for the dynamic parameter
        final PluginParameter aDynamicPlgParam = PluginParametersFactory.build().addParameter(SamplePlugin.COEFF, "-1")
                .getParameters().get(0);

        pluginServiceMocked.addPluginPackage(pluginsPackage);
        final SamplePlugin aSamplePlugin = pluginServiceMocked.getFirstPluginByType(IComplexInterfacePlugin.class,
                                                                                    aDynamicPlgParam);

        Assert.assertNotNull(aSamplePlugin);

        final int result = aSamplePlugin.add(QUATRE, CINQ);
        LOGGER.debug(RESULT + result);

        Assert.assertTrue(result < 0);
        Assert.assertTrue(aSamplePlugin.echo(HELLO).contains(HELLO));
    }

    /**
     * Get the first plugin of a specific type with a dynamic parameter. Used the default value for the dynamic
     * parameter.
     *
     * @throws ModuleException
     *             throw if an error occurs
     */
    @Test
    @Requirements({ @Requirement("REGARDS_DSL_SYS_ARC_120"), @Requirement("REGARDS_DSL_CMP_PLG_310") })
    @Purpose("Load a plugin with a dynamic parameter with a list of value from a specific type with a configuration and execute a method.")
    public void getFirstPluginByTypeWithADynamicParameterWithAListOfValue() throws ModuleException {
        final List<PluginConfiguration> pluginConfs = new ArrayList<>();
        final PluginConfiguration aPluginConfiguration = getPluginConfigurationWithDynamicParameter();
        aPluginConfiguration.setId(AN_ID);

        pluginConfs.add(aPluginConfiguration);
        pluginConfs.add(getPluginConfigurationWithParameters());

        Mockito.when(pluginConfRepositoryMocked.findByPluginIdOrderByPriorityOrderDesc(PLUGIN_PARAMETER_ID))
                .thenReturn(pluginConfs);
        Mockito.when(pluginConfRepositoryMocked.findOne(aPluginConfiguration.getId())).thenReturn(aPluginConfiguration);
        Mockito.when(pluginConfRepositoryMocked.exists(aPluginConfiguration.getId())).thenReturn(true);

        pluginServiceMocked.addPluginPackage(pluginsPackage);
        final SamplePlugin aSamplePlugin = pluginServiceMocked.getFirstPluginByType(IComplexInterfacePlugin.class);

        Assert.assertNotNull(aSamplePlugin);
        Assert.assertTrue(aSamplePlugin.echo(HELLO).contains(RED));
    }

    /**
     * Get the first plugin of a specific type with a dynamic parameter. Set a value for the dynamic parameter.
     *
     * @throws ModuleException
     *             throw if an error occurs
     */
    @Test
    @Requirements({ @Requirement("REGARDS_DSL_SYS_ARC_120"), @Requirement("REGARDS_DSL_CMP_PLG_310") })
    @Purpose("Load a plugin with a dynamic parameter with a list of value from a specific type with a configuration and set a parameter value and execute a method.")
    public void getFirstPluginByTypeWithADynamicParameterWithAListOfValueAndSetAValue() throws ModuleException {
        final List<PluginConfiguration> pluginConfs = new ArrayList<>();
        final PluginConfiguration aPluginConfiguration = getPluginConfigurationWithDynamicParameter();
        aPluginConfiguration.setId(AN_ID);

        pluginConfs.add(aPluginConfiguration);
        pluginConfs.add(getPluginConfigurationWithParameters());

        Mockito.when(pluginConfRepositoryMocked.findByPluginIdOrderByPriorityOrderDesc(PLUGIN_PARAMETER_ID))
                .thenReturn(pluginConfs);
        Mockito.when(pluginConfRepositoryMocked.findOne(aPluginConfiguration.getId())).thenReturn(aPluginConfiguration);
        Mockito.when(pluginConfRepositoryMocked.exists(aPluginConfiguration.getId())).thenReturn(true);

        // the argument for the dynamic parameter
        final PluginParameter aDynamicPlgParam = PluginParametersFactory.build()
                .addParameter(SamplePlugin.SUFFIXE, BLUE).getParameters().get(0);

        pluginServiceMocked.addPluginPackage(pluginsPackage);
        final SamplePlugin aSamplePlugin = pluginServiceMocked.getFirstPluginByType(IComplexInterfacePlugin.class,
                                                                                    aDynamicPlgParam);

        Assert.assertNotNull(aSamplePlugin);
        Assert.assertTrue(aSamplePlugin.echo(HELLO).contains(BLUE));
    }

    /**
     * Get the first plugin of a specific type with a dynamic parameter. Used the default value for the dynamic
     * parameter.
     *
     * @throws ModuleException
     *             throw if an error occurs
     */
    @Test
    public void getAPluginWithBadVersionConfiguration() throws ModuleException {
        final List<PluginConfiguration> pluginConfs = new ArrayList<>();
        final PluginConfiguration aPluginConfiguration = getPluginConfigurationWithDynamicParameter();
        aPluginConfiguration.setVersion(BLUE);
        aPluginConfiguration.setId(AN_ID);

        pluginConfs.add(aPluginConfiguration);
        pluginConfs.add(getPluginConfigurationWithParameters());

        Mockito.when(pluginConfRepositoryMocked.findByPluginIdOrderByPriorityOrderDesc(PLUGIN_PARAMETER_ID))
                .thenReturn(pluginConfs);
        Mockito.when(pluginConfRepositoryMocked.findOne(aPluginConfiguration.getId())).thenReturn(aPluginConfiguration);
        Mockito.when(pluginConfRepositoryMocked.exists(aPluginConfiguration.getId())).thenReturn(true);

        pluginServiceMocked.addPluginPackage(pluginsPackage);
        final SamplePlugin aSamplePlugin = pluginServiceMocked.getFirstPluginByType(IComplexInterfacePlugin.class);

        Assert.assertNotNull(aSamplePlugin);
        Assert.assertTrue(aSamplePlugin.echo(HELLO).contains(RED));
    }

    /**
     * Get the first plugin with the configuration the most priority.
     *
     * @throws ModuleException
     *             throw if an error occurs
     */
    @Test
    public void getFirstPluginTheMostPrioritary() throws ModuleException {
        final List<PluginConfiguration> pluginConfs = new ArrayList<>();

        final PluginConfiguration aPluginConfiguration = getPluginConfigurationWithDynamicParameter();
        // this conf is the most priority
        aPluginConfiguration.setPriorityOrder(1);
        aPluginConfiguration.setId(AN_ID);

        final PluginConfiguration bPluginConfiguration = getPluginConfigurationWithParameters();
        bPluginConfiguration.setPriorityOrder(2);
        bPluginConfiguration.setId(1 + AN_ID);

        pluginConfs.add(aPluginConfiguration);
        pluginConfs.add(bPluginConfiguration);

        Mockito.when(pluginConfRepositoryMocked.findByPluginIdOrderByPriorityOrderDesc(PLUGIN_PARAMETER_ID))
                .thenReturn(pluginConfs);
        Mockito.when(pluginConfRepositoryMocked.findOne(aPluginConfiguration.getId())).thenReturn(aPluginConfiguration);
        Mockito.when(pluginConfRepositoryMocked.exists(aPluginConfiguration.getId())).thenReturn(true);

        pluginServiceMocked.addPluginPackage(pluginsPackage);
        final SamplePlugin aSamplePlugin = pluginServiceMocked.getFirstPluginByType(IComplexInterfacePlugin.class);

        Assert.assertNotNull(aSamplePlugin);
        Assert.assertTrue(aSamplePlugin.echo(HELLO).contains(RED));
    }

}