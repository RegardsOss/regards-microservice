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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Sets;
import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginDynamicValue;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;

/**
 * Unit testing of {@link PluginService}.
 * @author Christophe Mertz
 */
public class PluginServiceUpdateDynamicParameterTest extends PluginServiceUtility {

    private IPluginConfigurationRepository pluginConfRepositoryMocked;

    private IPluginService pluginServiceMocked;

    private IRuntimeTenantResolver runtimeTenantResolver;

    /**
     * This method is run before all tests
     */
    @Before
    public void init() {
        runtimeTenantResolver = Mockito.mock(IRuntimeTenantResolver.class);
        Mockito.when(runtimeTenantResolver.getTenant()).thenReturn("tenant");

        // create a mock repository
        pluginConfRepositoryMocked = Mockito.mock(IPluginConfigurationRepository.class);
        pluginServiceMocked = new PluginService(pluginConfRepositoryMocked, Mockito.mock(IPublisher.class),
                                                runtimeTenantResolver);
    }

    /**
     * Update a {@link PluginConfiguration} : change the parameter's status from dynamic to not dynamic
     */
    @Test
    @Requirement("REGARDS_DSL_CMP_PLG_330")
    @Purpose("Change the parameter's status from dynamic to not dynamic.")
    public void updateDynamicParameter() {
        final PluginConfiguration aPluginConfiguration = getPluginConfigurationWithoutParametersToUpdate();
        aPluginConfiguration.setId(AN_ID);
        try {
            Mockito.when(pluginConfRepositoryMocked.findById(aPluginConfiguration.getId()))
                    .thenReturn(aPluginConfiguration);
            Mockito.when(pluginConfRepositoryMocked.save(aPluginConfiguration)).thenReturn(aPluginConfiguration);

            PluginConfiguration updatedConf = pluginServiceMocked.updatePluginConfiguration(aPluginConfiguration);

            Assert.assertEquals(aPluginConfiguration.getParameters().stream().filter(p -> p.isDynamic()).count(),
                                updatedConf.getParameters().stream().filter(p -> p.isDynamic()).count());
            Assert.assertEquals(aPluginConfiguration.getParameters().stream().filter(p -> !p.isDynamic()).count(),
                                updatedConf.getParameters().stream().filter(p -> !p.isDynamic()).count());

            aPluginConfiguration.logParams();
            final List<PluginParameter> parameters = aPluginConfiguration.getParameters();
            for (final PluginParameter p : updatedConf.getParameters()) {
                if (p.isDynamic()) {
                    if (!p.getDynamicsValuesAsString().isEmpty()) {
                        parameters.remove(p);
                        p.setIsDynamic(false);
                        p.getDynamicsValues().removeAll(p.getDynamicsValues());
                        parameters.add(p);
                        break;
                    }
                }
            }

            aPluginConfiguration.setParameters(parameters);
            updatedConf = pluginServiceMocked.updatePluginConfiguration(aPluginConfiguration);

            Assert.assertEquals(aPluginConfiguration.getParameters().stream().filter(p -> p.isDynamic()).count(),
                                updatedConf.getParameters().stream().filter(p -> p.isDynamic()).count());
            Assert.assertEquals(aPluginConfiguration.getParameters().stream().filter(p -> !p.isDynamic()).count(),
                                updatedConf.getParameters().stream().filter(p -> !p.isDynamic()).count());
            aPluginConfiguration.logParams();

        } catch (ModuleException e) {
            Assert.fail();
        }
    }

    /**
     * Update a {@link PluginConfiguration} : change the parameter's status from dynamic to not dynamic
     */
    @Test
    @Requirement("REGARDS_DSL_CMP_PLG_330")
    @Purpose("Change the parameter's status from not dynamic to dynamic.")
    public void updateParameterToDynamic() {
        final PluginConfiguration aPluginConfiguration = getPluginConfigurationWithoutParametersToUpdate();
        aPluginConfiguration.setId(AN_ID);
        try {
            Mockito.when(pluginConfRepositoryMocked.findById(aPluginConfiguration.getId()))
                    .thenReturn(aPluginConfiguration);
            Mockito.when(pluginConfRepositoryMocked.save(aPluginConfiguration)).thenReturn(aPluginConfiguration);

            PluginConfiguration updatedConf = pluginServiceMocked.updatePluginConfiguration(aPluginConfiguration);

            Assert.assertEquals(aPluginConfiguration.getParameters().stream().filter(p -> p.isDynamic()).count(),
                                updatedConf.getParameters().stream().filter(p -> p.isDynamic()).count());
            Assert.assertEquals(aPluginConfiguration.getParameters().stream().filter(p -> !p.isDynamic()).count(),
                                updatedConf.getParameters().stream().filter(p -> !p.isDynamic()).count());

            aPluginConfiguration.logParams();
            final List<PluginParameter> parameters = aPluginConfiguration.getParameters();
            for (final PluginParameter p : updatedConf.getParameters()) {
                if (!p.isDynamic()) {
                    parameters.remove(p);
                    p.setIsDynamic(true);
                    p.setDynamicsValues(Sets.newHashSet(new PluginDynamicValue("one"), new PluginDynamicValue("two"),
                                                        new PluginDynamicValue("three"), new PluginDynamicValue("for"),
                                                        new PluginDynamicValue("five"), new PluginDynamicValue("six")));
                    p.setValue(p.getDynamicsValues().iterator().next().getValue());
                    parameters.add(p);
                    break;
                }
            }

            aPluginConfiguration.setParameters(parameters);
            updatedConf = pluginServiceMocked.updatePluginConfiguration(aPluginConfiguration);

            Assert.assertEquals(aPluginConfiguration.getParameters().stream().filter(p -> p.isDynamic()).count(),
                                updatedConf.getParameters().stream().filter(p -> p.isDynamic()).count());
            Assert.assertEquals(aPluginConfiguration.getParameters().stream().filter(p -> !p.isDynamic()).count(),
                                updatedConf.getParameters().stream().filter(p -> !p.isDynamic()).count());
            aPluginConfiguration.logParams();

        } catch (ModuleException e) {
            Assert.fail();
        }
    }

}