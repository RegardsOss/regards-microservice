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
package fr.cnes.regards.framework.utils.cycle.detection;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.Gson;

import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.framework.utils.plugins.PluginUtilsRuntimeException;
import fr.cnes.regards.framework.utils.plugins.SamplePlugin;

/**
 * Unit testing of {@link PluginUtils}.
 *
 * @author Christophe Mertz
 */
@RunWith(SpringRunner.class)
@EnableAutoConfiguration(exclude = JpaRepositoriesAutoConfiguration.class)
public class CycleDetectionTest {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CycleDetectionTest.class);

    private static final String PLUGIN_PACKAGE = "fr.cnes.regards.framework.utils.plugins";

    @Autowired
    Gson gson;

    @Test
    public void cycleDetectionOK() {
        List<String> values = new ArrayList<String>();
        values.add("test1");
        values.add("test2");
        OffsetDateTime ofdt = OffsetDateTime.now().minusDays(5);

        TestPojo pojoParam = new TestPojo();
        pojoParam.setValue("first");
        pojoParam.setValues(values);
        pojoParam.setDate(ofdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        pojoParam.addIntValues(1);
        pojoParam.addIntValues(2);
        pojoParam.addIntValues(3);
        pojoParam.addIntValues(4);

        //        TestPojo pojoParam2 = new TestPojo();
        //        pojoParam2.setValue("second");
        //        pojoParam2.setValues(values);
        //        pojoParam2.setDate(ofdt.minusHours(55).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        //        pojoParam2.addIntValues(99);
        //        pojoParam2.addIntValues(98);
        //        pojoParam2.addIntValues(97);
        //
        //        TestPojo pojoParam3 = new TestPojo();
        //        pojoParam3.setValue("third");
        //        pojoParam3.setValues(values);
        //        pojoParam3.setDate(ofdt.minusHours(165).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        //        pojoParam3.addIntValues(101);
        //        pojoParam3.addIntValues(102);
        //        pojoParam3.addIntValues(103);

        //        Set<TestPojo> pojos = new HashSet<>();
        //        pojos.add(pojoParam2);
        //        pojos.add(pojoParam3);

        /*
         * Set all parameters
         */
        List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(SamplePluginWithPojo.ACTIVE, "true").addParameter(SamplePlugin.COEFF, "12345")
                .addParameter(SamplePluginWithPojo.POJO, gson.toJson(pojoParam))
                .addParameter(SamplePluginWithPojo.SUFFIXE, "chris_test_1").getParameters();

        // instantiate plugin
        SamplePluginWithPojo samplePlugin = PluginUtils.getPlugin(parameters, SamplePluginWithPojo.class,
                                                                  Arrays.asList(PLUGIN_PACKAGE), new HashMap<>());

        Assert.assertNotNull(samplePlugin);

        /*
         * Use the plugin
         */
        Assert.assertEquals(samplePlugin.getPojo().getValue(), pojoParam.getValue());
        Assert.assertEquals(samplePlugin.getPojo().getValues().size(), values.size());
        Assert.assertEquals(OffsetDateTime.parse(samplePlugin.getPojo().getDate(),
                                                 DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                            ofdt);
    }

    @Test(expected = PluginUtilsRuntimeException.class)
    public void cycleDetectedWithTwoLevel() {
        List<String> values = new ArrayList<String>();
        values.add("test1");
        values.add("test2");
        values.add("test3");
        values.add("test4");
        OffsetDateTime ofdt = OffsetDateTime.now().minusYears(10);

        TestPojoParent pojoParent = new TestPojoParent();
        pojoParent.setValue("parent");
        pojoParent.setValues(values);
        pojoParent.setDate(ofdt.minusHours(55).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        pojoParent.addIntValues(99);
        pojoParent.addIntValues(98);
        pojoParent.addIntValues(97);

        TestPojoChild pojoChild = new TestPojoChild();
        pojoChild.setValue("child");
        pojoChild.setValues(values);
        pojoChild.setDate(ofdt.minusHours(1999).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        pojoChild.addIntValues(101);
        pojoChild.addIntValues(102);
        pojoChild.addIntValues(103);

        TestPojoParent otherPojoParent = new TestPojoParent();
        pojoParent.setValue("other parent");
        pojoParent.setValues(values);
        pojoParent.setDate(ofdt.minusSeconds(3333).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        pojoParent.addIntValues(501);
        pojoParent.addIntValues(502);

        pojoParent.setChild(pojoChild);
        pojoChild.setParent(otherPojoParent);

        /*
         * Set all parameters
         */
        List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(SamplePluginWithPojoCycleDetected.ACTIVE, "true")
                .addParameter(SamplePluginWithPojoCycleDetected.COEFF, "12345")
                .addParameter(SamplePluginWithPojoCycleDetected.POJO, gson.toJson(pojoParent)).getParameters();

        // instantiate plugin
        PluginUtils.getPlugin(parameters, SamplePluginWithPojoCycleDetected.class, Arrays.asList(PLUGIN_PACKAGE),
                              new HashMap<>());

        Assert.fail();
    }

    @Test(expected = PluginUtilsRuntimeException.class)
    public void cycleDetectedWithThreeLevel() {
        List<String> values = new ArrayList<String>();
        values.add("test1");
        values.add("test2");
        values.add("test3");
        values.add("test4");
        OffsetDateTime ofdt = OffsetDateTime.now().minusYears(10);

        TestPojoGrandParent pojoGrandParent = new TestPojoGrandParent();
        pojoGrandParent.setValue("grand parent");
        pojoGrandParent.setValues(values);
        pojoGrandParent.setDate(ofdt.minusHours(55).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        pojoGrandParent.addIntValues(65);

        TestPojoParent pojoParent = new TestPojoParent();
        pojoParent.setValue("parent");
        pojoParent.setValues(values);
        pojoParent.setDate(ofdt.minusHours(55).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        pojoParent.addIntValues(99);
        pojoParent.addIntValues(98);
        pojoParent.addIntValues(97);

        TestPojoChild pojoChild = new TestPojoChild();
        pojoChild.setValue("child");
        pojoChild.setValues(values);
        pojoChild.setDate(ofdt.minusHours(1999).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        pojoChild.addIntValues(101);
        pojoChild.addIntValues(102);
        pojoChild.addIntValues(103);

        TestPojoParent otherPojoParent = new TestPojoParent();
        pojoParent.setValue("other parent");
        pojoParent.setValues(values);
        pojoParent.setDate(ofdt.minusSeconds(3333).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        pojoParent.addIntValues(501);
        pojoParent.addIntValues(502);

        pojoGrandParent.setChild(pojoParent);
        pojoParent.setChild(pojoChild);
        pojoChild.setParent(otherPojoParent);

        /*
         * Set all parameters
         */
        List<PluginParameter> parameters = PluginParametersFactory.build()
                .addParameter(SamplePluginWithPojoCycleDetectedLevelThree.ACTIVE, "true")
                .addParameter(SamplePluginWithPojoCycleDetectedLevelThree.COEFF, "12345")
                .addParameter(SamplePluginWithPojoCycleDetectedLevelThree.POJO, gson.toJson(pojoGrandParent))
                .getParameters();

        // instantiate plugin
        PluginUtils.getPlugin(parameters, SamplePluginWithPojoCycleDetectedLevelThree.class,
                              Arrays.asList(PLUGIN_PACKAGE), new HashMap<>());

        Assert.fail();
    }

}