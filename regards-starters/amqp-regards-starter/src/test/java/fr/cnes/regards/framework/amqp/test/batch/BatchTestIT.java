/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.framework.amqp.test.batch;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;

/**
 * @author Marc SORDI
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = BatchConfiguration.class)
@TestPropertySource(properties = { "regards.amqp.management.mode=SINGLE",
        "regards.tenants=" + BatchTestIT.PROJECT + ", " + BatchTestIT.PROJECT1, "regards.tenant=" + BatchTestIT.PROJECT,
        "regards.amqp.internal.transaction=true", "spring.jmx.enabled=false" }, locations = "classpath:amqp.properties")
public class BatchTestIT {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchTestIT.class);

    static final String PROJECT = "PROJECT";

    static final String PROJECT1 = "PROJECT1";

    private static final Integer MESSAGE_NB_PROJECT = 10;

    private static final Integer MESSAGE_NB_PROJECT1 = 5;

    @Autowired
    private IPublisher publisher;

    @Autowired
    private ISubscriber subscriber;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private BatchHandler batchHandler;

    @Before
    public void before() {
        // Subscribe to message
        subscriber.subscribeTo(BatchMessage.class, batchHandler);
    }

    @After
    public void after() {
        // Unsubscribe
        subscriber.unsubscribeFrom(BatchMessage.class);
    }

    @Test
    public void processBatch() throws InterruptedException {

        // Publish message in default project
        for (int i = 1; i <= MESSAGE_NB_PROJECT; i++) {
            publisher.publish(BatchMessage.build(String.format("%s_batch_0%02d", PROJECT, i)));
        }

        // Publish messages in second project
        try {
            tenantResolver.forceTenant(PROJECT1);
            for (int i = 1; i <= MESSAGE_NB_PROJECT1; i++) {
                publisher.publish(BatchMessage.build(String.format("%s_batch_1%02d", PROJECT1, i)));
            }
        } finally {
            tenantResolver.clearTenant();
        }

        Thread.sleep(5000);
        Assert.assertTrue(batchHandler.getCountByTenant(PROJECT) == MESSAGE_NB_PROJECT);
        Assert.assertTrue(batchHandler.getCountByTenant(PROJECT1) == MESSAGE_NB_PROJECT1);
        Assert.assertTrue(batchHandler.getCalls() == 2);

        subscriber.unsubscribeFrom(BatchMessage.class);
    }

    @Test
    public void processUnknownTenant() throws InterruptedException {

        try {
            tenantResolver.forceTenant(BatchHandler.FAKE_TENANT);
            publisher.publish(BatchMessage.build(String.format("%s_batch", BatchHandler.FAKE_TENANT)));
        } finally {
            tenantResolver.clearTenant();
        }

        try {
            tenantResolver.forceTenant(PROJECT);
            publisher.publish(BatchMessage.build(String.format("%s_batch", PROJECT)));
        } finally {
            tenantResolver.clearTenant();
        }

        Thread.sleep(5000);
        Assert.assertTrue(batchHandler.getCountByTenant(BatchHandler.FAKE_TENANT) == 0);
        Assert.assertTrue(batchHandler.getCountByTenant(PROJECT) == 1);
    }

    @Test
    public void processingFailureTest() throws InterruptedException {
        try {
            tenantResolver.forceTenant(BatchHandler.FAIL_TENANT);
            publisher.publish(BatchMessage.build(String.format("%s_batch", BatchHandler.FAIL_TENANT)));
        } finally {
            tenantResolver.clearTenant();
        }

        Thread.sleep(5000);
        Assert.assertTrue(batchHandler.getFailsByTenant(BatchHandler.FAIL_TENANT) > 1);
        Assert.assertTrue(batchHandler.getCountByTenant(BatchHandler.FAIL_TENANT) == 0);
    }

}
