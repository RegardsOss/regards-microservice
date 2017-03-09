/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.modules.plugins.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;

/***
 * Unit testing of {@link PluginParameter} persistence.
 *
 * @author Christophe Mertz
 *
 */

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { PluginDaoTestConfig.class })
public class PluginParameterIT extends PluginDaoUtility {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginParameterIT.class);

    @Before
    public void before() {
        injectToken(PROJECT);
        cleanDb();
    }

    /**
     * Unit testing for the creation of a {@link PluginParameter}
     */
    @Test
    public void createPluginParameter() {
        final long nPluginParameter = paramRepository.count();

        paramRepository.save(A_PARAMETER);
        paramRepository.save(PARAMETERS2);

        Assert.assertEquals(nPluginParameter + 1 + PARAMETERS2.size(), paramRepository.count());

        plgRepository.deleteAll();
    }

    /**
     * Unit testing for the update of a {@link PluginParameter}
     */
    @Test
    public void updatePluginParameter() {
        paramRepository.save(INTERFACEPARAMETERS.get(0));
        final PluginParameter paramJpa = paramRepository.save(PARAMETERS2.get(0));
        Assert.assertEquals(paramJpa.getName(), PARAMETERS2.get(0).getName());

        paramRepository.findAll().forEach(p -> LOGGER.info(p.getName()));

        paramRepository.save(paramJpa);

        final PluginParameter paramFound = paramRepository.findOne(paramJpa.getId());
        Assert.assertEquals(paramFound.getName(), paramJpa.getName());

        paramRepository.deleteAll();
        plgRepository.deleteAll();
    }

    /**
     * Unit testing for the delete of a {@link PluginParameter}
     */
    @Test
    public void deletePluginParameter() {
        final PluginParameter paramJpa = paramRepository.save(A_PARAMETER);
        paramRepository.save(PARAMETERS2);
        Assert.assertEquals(1 + PARAMETERS2.size(), paramRepository.count());

        // Delete a plugin parameter
        paramRepository.delete(paramJpa);
        Assert.assertEquals(PARAMETERS2.size(), paramRepository.count());

        // Delete a plugin parameter
        paramRepository.delete(PARAMETERS2.get(0));
        Assert.assertEquals(PARAMETERS2.size() - 1, paramRepository.count());
    }

    /**
     * Unit testing about the dynamic values of a {@link PluginParameter}
     */
    @Test
    public void createAndFindPluginParameter() {
        // first plugin parameter
        final PluginParameter savedParam = paramRepository.save(A_PARAMETER);
        Assert.assertNotNull(savedParam.getId());
        Assert.assertEquals(1, paramRepository.count());

        // second plugin parameter with dynamic values
        final PluginParameter paramWithDynValues = paramRepository.save(PARAMETERS2.get(0));
        Assert.assertNotNull(paramWithDynValues.getId());
        Assert.assertEquals(2, paramRepository.count());

        // search the first plugin parameter
        final PluginParameter paramFound2 = paramRepository.findOne(savedParam.getId());
        Assert.assertNotNull(paramFound2);
        Assert.assertEquals(savedParam.isDynamic(), paramFound2.isDynamic());
        Assert.assertNull(savedParam.getDynamicsValues());
        Assert.assertEquals(savedParam.getName(), paramFound2.getName());
        Assert.assertEquals(savedParam.getValue(), paramFound2.getValue());
        Assert.assertEquals(savedParam.getId(), paramFound2.getId());
    }

    /**
     * Unit testing about the dynamic values of a {@link PluginParameter}
     */
    @Test
    public void controlPluginParameterDynamicValues() {
        // first plugin parameter
        final PluginParameter savedParam = paramRepository.save(A_PARAMETER);
        Assert.assertNotNull(savedParam.getId());
        Assert.assertEquals(1, paramRepository.count());

        // second plugin parameter with dynamic values
        final PluginParameter paramWithDynValues = paramRepository.save(PARAMETERS2.get(0));
        Assert.assertNotNull(paramWithDynValues.getId());
        Assert.assertEquals(2, paramRepository.count());

        // search the second plugin parameter
        final PluginParameter paramFound = paramRepository.findOneWithDynamicsValues(paramWithDynValues.getId());
        Assert.assertNotNull(paramFound);
        paramFound.getDynamicsValues().stream().forEach(p -> LOGGER.info(p.getValue()));

        // test dynamics values of the second parameter
        Assert.assertEquals(paramWithDynValues.isDynamic(), paramFound.isDynamic());
        Assert.assertEquals(paramWithDynValues.getDynamicsValues().size(), paramFound.getDynamicsValues().size());
        Assert.assertEquals(paramWithDynValues.getName(), paramFound.getName());
        Assert.assertEquals(paramWithDynValues.getValue(), paramFound.getValue());
        Assert.assertEquals(paramWithDynValues.getId(), paramFound.getId());
        Assert.assertEquals(paramWithDynValues.getDynamicsValuesAsString().size(),
                            paramFound.getDynamicsValuesAsString().size());
        paramWithDynValues.getDynamicsValuesAsString().stream()
                .forEach(s -> paramFound.getDynamicsValuesAsString().contains(s));
    }

}
