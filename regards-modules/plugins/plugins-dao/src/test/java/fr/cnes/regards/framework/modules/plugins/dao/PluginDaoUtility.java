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
package fr.cnes.regards.framework.modules.plugins.dao;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractDaoTest;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.utils.plugins.PluginParametersFactory;

/***
 * Constants and datas for unit testing of plugin's DAO.
 *
 * @author Christophe Mertz
 */
public class PluginDaoUtility extends AbstractDaoTest {

    /**
     * Class logger
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(PluginDaoUtility.class);

    /**
     * Project used for test
     */
    static final String PROJECT = "test1";

    /**
     * Version
     */
    static final String VERSION = "12345-6789-11";

    /**
     * Role used for test
     */
    static final String USERROLE = "USERROLE";

    /**
     * RED constant {@link String}
     */
    static final String RED = "red";

    /**
     * GREEN constant {@link String}
     */
    static final String GREEN = "green";

    /**
     * BLUE constant {@link String}
     */
    static final String BLUE = "blue";

    /**
     * BLUE constant {@link String}
     */
    static final String INVALID_JWT = "Invalid JWT";

    /**
     * A {@link PluginParameter}
     */
    static final PluginParameter ONE_PARAMETER = PluginParametersFactory.build().addParameter("param11", "value11")
            .getParameters().get(0);

    /**
     * A {@link List} of values
     */
    static final List<String> DYNAMICVALUES = Arrays.asList(RED, BLUE, GREEN);

    /**
     * A {@link List} of {@link PluginParameter}
     */
    static final List<PluginParameter> LIST_PARAMETERS = PluginParametersFactory.build()
            .addDynamicParameter("param-dyn21", RED, DYNAMICVALUES)
            .addDynamicParameter("param-dyn31", GREEN, DYNAMICVALUES).addParameter("param31", "value31")
            .addParameter("param51", "value51").addParameter("param61", "value61").getParameters();

    /**
     * A list of {@link PluginParameter}
     */
    static final List<PluginParameter> INTERFACEPARAMETERS = PluginParametersFactory.build()
            .addParameter("param41", "value41").addParameter("param42", "value42").addParameter("param43", "value43")
            .addParameter("param44", "value44").addParameter("param45", "value45").getParameters();

    /**
     * A {@link PluginConfiguration}
     */
    private static PluginConfiguration pluginConfiguration1 = new PluginConfiguration(getPluginMetaData(),
            "a configuration from PluginDaoUtility", INTERFACEPARAMETERS, 0);

    /**
     * A list of {@link PluginParameter} with a dynamic {@link PluginParameter}
     */
    private static PluginConfiguration pluginConfiguration2 = new PluginConfiguration(getPluginMetaData(),
            "second configuration from PluginDaoUtility", LIST_PARAMETERS, 0);

    /**
     * IPluginConfigurationRepository
     */
    @Autowired
    protected IPluginConfigurationRepository plgRepository;

    /**
     * IPluginParameterRepository
     */
    @Autowired
    protected IPluginParameterRepository paramRepository;

    static PluginMetaData getPluginMetaData() {
        final PluginMetaData pluginMetaData = new PluginMetaData();
        pluginMetaData.setPluginClassName(Integer.class.getCanonicalName());
        pluginMetaData.getInterfaceNames().add("TestInterface");
        pluginMetaData.setPluginId("plugin-id");
        pluginMetaData.setAuthor("CS-SI");
        pluginMetaData.setVersion(VERSION);
        return pluginMetaData;
    }

    public static PluginConfiguration getPlgConfWithParameters() {
        return pluginConfiguration1;
    }

    public static PluginConfiguration getPlgConfWithDynamicParameter() {
        return pluginConfiguration2;
    }

    protected void cleanDb() {
        paramRepository.deleteAll();
        plgRepository.deleteAll();
        resetId();
    }

    protected static void resetId() {
        getPlgConfWithDynamicParameter().setId(null);
        getPlgConfWithDynamicParameter().getParameters().forEach(p -> p.setId(null));

        getPlgConfWithParameters().setId(null);
        getPlgConfWithParameters().getParameters().forEach(p -> p.setId(null));

        INTERFACEPARAMETERS.forEach(p -> p.setId(null));
    }

    protected void displayParams() {
        LOGGER.info("=====> parameter");
        paramRepository.findAll().forEach(p -> LOGGER.info("name=" + p.getName() + "-value=" + p.getValue()
                + "-nb dyns=" + p.getDynamicsValuesAsString().size()));
        for (PluginParameter pP : paramRepository.findAll()) {
            if ((pP.getDynamicsValues() != null) && !pP.getDynamicsValues().isEmpty()) {
                pP.getDynamicsValues().forEach(p -> LOGGER.info("-value=" + p.getValue()));
            }
        }
        LOGGER.info("<=====");
    }

}