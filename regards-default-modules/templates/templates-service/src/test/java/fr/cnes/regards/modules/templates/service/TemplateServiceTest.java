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
package fr.cnes.regards.modules.templates.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mail.SimpleMailMessage;

import fr.cnes.regards.framework.module.rest.exception.EntityException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.templates.dao.ITemplateRepository;
import fr.cnes.regards.modules.templates.domain.Template;

/**
 * Test suite for {@link TemplateService}.
 *
 * @author Xavier-Alexandre Brochard
 */
public class TemplateServiceTest {

    /**
     * Code
     */
    public static final String CODE = "DEFAULT";

    /**
     * Content
     */
    public static final String CONTENT = "Hello ${name}. You are ${age} years old and ${height} m tall.";

    /**
     * Subject
     */
    public static final String SUBJECT = "Subject of a templated mail";

    /**
     * A value stored in the data map
     */
    public static final String DATA_VALUE_1 = "26";

    /**
     * A value stored in the data map
     */
    public static final String DATA_VALUE_2 = "1.79";

    /**
     * A recipient for the mail
     */
    public static final String RECIPIENT_0 = "email@test.com";

    /**
     * A recipient for the mail
     */
    public static final String RECIPIENT_1 = "otheremail@test.com";

    /**
     * The recipients as an array
     */
    public static final String[] RECIPIENTS = { RECIPIENT_0, RECIPIENT_1 };

    /**
     * Data
     */
    // @formatter:off
    @SuppressWarnings("serial")
    public static final Map<String, String> DATA = new HashMap<String, String>() {{ put("name", "Defaultname");put("age", DATA_VALUE_1);put("height", DATA_VALUE_2); }};
    // @formatter:on

    /**
     * A template
     */
    private static Template template;

    /**
     * A template id
     */
    private static final Long ID = 0L;

    /**
     * Tested service
     */
    private ITemplateService templateService;

    /**
     * Mocked CRUD repository managing {@link Template}s
     */
    private ITemplateRepository templateRepository;

    /**
     * Mocked tenant resolver
     */
    private ITenantResolver tenantResolver;

    /**
     * Mocked runtime tenant resolver
     */
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Before
    public void setUp() throws IOException {
        template = new Template(CODE, CONTENT, DATA, SUBJECT);
        templateRepository = Mockito.mock(ITemplateRepository.class);
        tenantResolver = Mockito.mock(ITenantResolver.class);
        runtimeTenantResolver = Mockito.mock(IRuntimeTenantResolver.class);
        templateService = new TemplateService(templateRepository, tenantResolver, runtimeTenantResolver);
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.templates.service.TemplateService#findAll()}.
     */
    @Test
    @Purpose("Check that the system allows to retrieve the list of all templates.")
    @Requirement("REGARDS_DSL_SYS_ERG_310")
    @Requirement("REGARDS_DSL_ADM_ADM_440")
    @Requirement("REGARDS_DSL_ADM_ADM_460")
    public final void testFindAll() {
        // Define expected
        final List<Template> expected = Arrays.asList(template, new Template(), new Template());

        // Mock
        Mockito.when(templateRepository.findAll()).thenReturn(expected);

        // Define actual
        final List<Template> actual = templateService.findAll();

        // Check
        Assert.assertThat(actual, CoreMatchers.is(CoreMatchers.equalTo(expected)));
    }

    /**
     * Test method for {@link TemplateService#create(Template)}.
     */
    @Test
    @Purpose("Check that the system allows to create templates.")
    @Requirement("REGARDS_DSL_SYS_ERG_310")
    @Requirement("REGARDS_DSL_ADM_ADM_440")
    @Requirement("REGARDS_DSL_ADM_ADM_460")
    public final void testCreate() {
        // Mock
        Mockito.when(templateRepository.findOne(ID)).thenReturn(null);
        Mockito.when(templateRepository.exists(ID)).thenReturn(false);

        // Call tested method
        templateService.create(template);

        // Check
        Mockito.verify(templateRepository).save(Mockito.refEq(template, "id"));
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.templates.service.TemplateService#findById(Long)}.
     *
     * @throws EntityNotFoundException
     *             if no template with passed id could be found
     */
    @Test
    @Purpose("Check that the system allows to retrieve a single template.")
    @Requirement("REGARDS_DSL_SYS_ERG_310")
    @Requirement("REGARDS_DSL_ADM_ADM_440")
    @Requirement("REGARDS_DSL_ADM_ADM_460")
    public final void testFindById() throws EntityNotFoundException {
        // Mock
        Mockito.when(templateRepository.findOne(ID)).thenReturn(template);
        Mockito.when(templateRepository.exists(ID)).thenReturn(true);

        // Trigger expected exception
        final Template actual = templateService.findById(ID);

        // Check
        Assert.assertThat(actual, CoreMatchers.is(CoreMatchers.equalTo(template)));
        Mockito.verify(templateRepository).findOne(ID);
    }

    /**
     * Test method for {@link TemplateService#findById(java.lang.Long)}.
     *
     * @throws EntityNotFoundException
     *             if no template with passed id could be found
     */
    @Test(expected = EntityNotFoundException.class)
    @Purpose("Check that the system handles the case where trying to retrieve a template of unknown id.")
    @Requirement("REGARDS_DSL_SYS_ERG_310")
    @Requirement("REGARDS_DSL_ADM_ADM_440")
    @Requirement("REGARDS_DSL_ADM_ADM_460")
    public final void testFindByIdNotFound() throws EntityNotFoundException {
        // Mock
        Mockito.when(templateRepository.findOne(ID)).thenReturn(null);
        Mockito.when(templateRepository.exists(ID)).thenReturn(false);

        // Trigger expected exception
        templateService.findById(ID);
    }

    /**
     * Test method for {@link TemplateService#update(Long, Template)}.
     *
     * @throws EntityException
     *             <br>
     *             {@link EntityNotFoundException} if no template with passed id could be found<br>
     *             {@link EntityInconsistentIdentifierException} if the path id differs from the template id<br>
     */
    @Test
    @Purpose("Check that the system allows to update a template.")
    @Requirement("REGARDS_DSL_SYS_ERG_310")
    @Requirement("REGARDS_DSL_ADM_ADM_440")
    @Requirement("REGARDS_DSL_ADM_ADM_460")
    public final void testUpdate() throws EntityException {
        // Prepare the case
        template.setId(ID);
        template.setDescription("Updated description");

        // Mock
        Mockito.when(templateRepository.findOne(ID)).thenReturn(template);
        Mockito.when(templateRepository.exists(ID)).thenReturn(true);

        // Call tested method
        templateService.update(ID, template);

        // Check
        Mockito.verify(templateRepository).save(Mockito.refEq(template));
    }

    /**
     * Test method for {@link TemplateService#update(Long, Template)}.
     *
     * @throws EntityException
     *             <br>
     *             {@link EntityNotFoundException} if no template with passed id could be found<br>
     *             {@link EntityInconsistentIdentifierException} if the path id differs from the template id<br>
     */
    @Test(expected = EntityNotFoundException.class)
    @Purpose("Check that the system handles the case of updating an not existing template.")
    @Requirement("REGARDS_DSL_SYS_ERG_310")
    @Requirement("REGARDS_DSL_ADM_ADM_440")
    @Requirement("REGARDS_DSL_ADM_ADM_460")
    public final void testUpdateNotFound() throws EntityException {
        // Prepare the case
        template.setId(ID);

        // Mock
        Mockito.when(templateRepository.findOne(ID)).thenReturn(null);
        Mockito.when(templateRepository.exists(ID)).thenReturn(false);

        // Trigger expected exception
        templateService.update(ID, template);
    }

    /**
     * Test method for {@link TemplateService#update(Long, Template)}.
     *
     * @throws EntityException
     *             <br>
     *             {@link EntityNotFoundException} if no template with passed id could be found<br>
     *             {@link EntityInconsistentIdentifierException} if the path id differs from the template id<br>
     */
    @Test(expected = EntityInconsistentIdentifierException.class)
    @Purpose("Check that the system allows the case of inconsistency of ids in the request.")
    @Requirement("REGARDS_DSL_SYS_ERG_310")
    @Requirement("REGARDS_DSL_ADM_ADM_440")
    @Requirement("REGARDS_DSL_ADM_ADM_460")
    public final void testUpdateInconsistentIdentifier() throws EntityException {
        // Prepare the case
        template.setId(ID);

        // Mock
        Mockito.when(templateRepository.findOne(ID)).thenReturn(template);
        Mockito.when(templateRepository.exists(ID)).thenReturn(true);

        // Trigger expected exception
        templateService.update(1L, template);
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.templates.service.TemplateService#delete(java.lang.Long)}.
     *
     * @throws EntityNotFoundException
     *             if no template with passed id could be found
     */
    @Test
    @Purpose("Check that the system allows to delete a single template.")
    @Requirement("REGARDS_DSL_SYS_ERG_310")
    @Requirement("REGARDS_DSL_ADM_ADM_440")
    @Requirement("REGARDS_DSL_ADM_ADM_460")
    public final void testDelete() throws EntityNotFoundException {
        // Mock
        Mockito.when(templateRepository.exists(ID)).thenReturn(true);

        // Call tested method
        templateService.delete(ID);

        // Check
        Mockito.verify(templateRepository).delete(ID);
    }

    /**
     * Test method for {@link fr.cnes.regards.modules.templates.service.TemplateService#delete(java.lang.Long)}.
     *
     * @throws EntityNotFoundException
     *             if no template with passed id could be found
     */
    @Test(expected = EntityNotFoundException.class)
    @Purpose("Check that the system handles the case of deleting an inexistent template.")
    @Requirement("REGARDS_DSL_SYS_ERG_310")
    @Requirement("REGARDS_DSL_ADM_ADM_440")
    @Requirement("REGARDS_DSL_ADM_ADM_460")
    public final void testDeleteNotFound() throws EntityNotFoundException {
        // Mock
        Mockito.when(templateRepository.exists(ID)).thenReturn(false);

        // Trigger expected exception
        templateService.delete(ID);
    }

    /**
     * Test method for {@link SimpleMailMessageTemplateWriter#writeToEmail(Template, Map, String[])}.
     *
     * @throws EntityNotFoundException
     *             no template of passed code could be found
     */
    @Test
    @Purpose("Check that the system uses templates to send emails.")
    @Requirement("REGARDS_DSL_SYS_ERG_310")
    @Requirement("REGARDS_DSL_ADM_ADM_440")
    @Requirement("REGARDS_DSL_ADM_ADM_460")
    public final void testWrite() throws EntityNotFoundException {
        // Mock
        Mockito.when(templateRepository.findOneByCode(CODE)).thenReturn(Optional.ofNullable(template));

        // Define expected
        final String expectedSubject = SUBJECT;
        final String expectedText = "Hello Defaultname. You are 26 years old and 1.79 m tall.";

        // Define actual
        final SimpleMailMessage message = templateService.writeToEmail(CODE, DATA, RECIPIENTS);

        // Check
        Assert.assertEquals(expectedSubject, message.getSubject());
        Assert.assertEquals(expectedText, message.getText());
        Assert.assertArrayEquals(RECIPIENTS, message.getTo());
    }

}
