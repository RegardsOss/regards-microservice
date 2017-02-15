/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.amqp.test;
/*
 * LICENSE_PLACEHOLDER
 */

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.amqp.Publisher;
import fr.cnes.regards.framework.amqp.configuration.IRabbitVirtualHostAdmin;
import fr.cnes.regards.framework.amqp.configuration.RegardsAmqpAdmin;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.amqp.event.Target;
import fr.cnes.regards.framework.amqp.event.WorkerMode;
import fr.cnes.regards.framework.amqp.test.domain.TestEvent;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;

/**
 * @author svissier
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { AmqpTestsConfiguration.class })
public class PublisherIT {

    /**
     * LOGGER used to populate logs when needed
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PublisherIT.class);

    /**
     * Static default tenant
     */
    @Value("${regards.tenant}")
    private String tenant;

    /**
     * configuration
     */
    @Autowired
    private RegardsAmqpAdmin amqpConfiguration;

    /**
     * Bean allowing us to declare queue, exchange, binding
     */
    @Autowired
    private RabbitAdmin rabbitAdmin;

    /**
     * bean used to publish message to the message broker and which is tested here
     */
    @Autowired
    private Publisher publisher;

    /**
     * bean provided
     */
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * bean allowing us to know if the broker is running
     */
    @Autowired
    private IRabbitVirtualHostAdmin rabbitVirtualHostAdmin;

    /**
     * create and start a message listener to receive the published event
     *
     */
    @Before
    public void init() {
        Assume.assumeTrue(rabbitVirtualHostAdmin.brokerRunning());
        rabbitVirtualHostAdmin.addVhost(tenant);
    }

    @After
    public void clean() {
        rabbitVirtualHostAdmin.removeVhost(tenant);
    }

    /**
     * Purpose: Send a message to the message broker using the publish client
     */
    @Purpose("Send a message to the message broker using the publish client")
    @Requirement("REGARDS_DSL_CMP_ARC_030")
    @Test
    public void testPublishOneToManyExternal() {
        Queue queue;
        Exchange exchange;
        Binding binding;
        try {
            amqpConfiguration.bind(tenant);

            exchange = amqpConfiguration.declareExchange(tenant, TestEvent.class, WorkerMode.ALL, Target.ALL);
            queue = amqpConfiguration.declareQueue(tenant, TestEvent.class, WorkerMode.ALL, Target.ALL);
            binding = amqpConfiguration.declareBinding(tenant, queue, exchange, WorkerMode.ALL);

        } finally {
            amqpConfiguration.unbind();
        }

        final TestEvent sended = new TestEvent("test1");
        publisher.publish(sended, WorkerMode.ALL, Target.ALL, 0);
        LOGGER.info("SENDED " + sended);

        try {
            amqpConfiguration.bind(tenant);
            // CHECKSTYLE:OFF
            @SuppressWarnings("unchecked")
            final TenantWrapper<TestEvent> wrappedMessage = (TenantWrapper<TestEvent>) rabbitTemplate
                    .receiveAndConvert(amqpConfiguration.getQueueName(TestEvent.class, WorkerMode.ALL, Target.ALL));
            // CHECKSTYLE:ON

            final TestEvent received = wrappedMessage.getContent();
            Assert.assertEquals(sended, received);
        } finally {
            amqpConfiguration.unbind();
        }

        // Clean
        try {
            amqpConfiguration.bind(tenant);
            rabbitAdmin.removeBinding(binding);
            rabbitAdmin.deleteQueue(queue.getName());
            rabbitAdmin.deleteExchange(exchange.getName());
        } finally {
            amqpConfiguration.unbind();
        }
    }

    /**
     * Purpose: Send a message to the message broker using the publish client with priority
     */
    @SuppressWarnings("unchecked")
    @Purpose("Send a message to the message broker using the publish client with priority")
    @Test
    public void testPublishPriorityExternal() {
        Queue queue;
        Exchange exchange;
        Binding binding;
        try {
            amqpConfiguration.bind(tenant);

            exchange = amqpConfiguration.declareExchange(tenant, TestEvent.class, WorkerMode.SINGLE, Target.ALL);
            queue = amqpConfiguration.declareQueue(tenant, TestEvent.class, WorkerMode.SINGLE, Target.ALL);
            binding = amqpConfiguration.declareBinding(tenant, queue, exchange, WorkerMode.SINGLE);
        } finally {
            amqpConfiguration.unbind();
        }
        final TestEvent priority0 = new TestEvent("priority 0");
        final TestEvent priority1 = new TestEvent("priority 1");
        final TestEvent priority02 = new TestEvent("priority 02");

        publisher.publish(priority0, WorkerMode.SINGLE, Target.ALL, 0);
        publisher.publish(priority1, WorkerMode.SINGLE, Target.ALL, 1);
        publisher.publish(priority02, WorkerMode.SINGLE, Target.ALL, 0);

        try {
            amqpConfiguration.bind(tenant);

            TenantWrapper<TestEvent> wrappedMessage = (TenantWrapper<TestEvent>) rabbitTemplate
                    .receiveAndConvert(amqpConfiguration.getQueueName(TestEvent.class, WorkerMode.SINGLE, Target.ALL));
            Assert.assertEquals(priority1, wrappedMessage.getContent());

            wrappedMessage = (TenantWrapper<TestEvent>) rabbitTemplate
                    .receiveAndConvert(amqpConfiguration.getQueueName(TestEvent.class, WorkerMode.SINGLE, Target.ALL));
            Assert.assertEquals(priority0, wrappedMessage.getContent());

            wrappedMessage = (TenantWrapper<TestEvent>) rabbitTemplate
                    .receiveAndConvert(amqpConfiguration.getQueueName(TestEvent.class, WorkerMode.SINGLE, Target.ALL));
            Assert.assertEquals(priority02, wrappedMessage.getContent());

        } finally {
            amqpConfiguration.unbind();
        }

        // Clean
        try {
            amqpConfiguration.bind(tenant);
            rabbitAdmin.removeBinding(binding);
            rabbitAdmin.deleteQueue(queue.getName());
            rabbitAdmin.deleteExchange(exchange.getName());
        } finally {
            amqpConfiguration.unbind();
        }
    }
}
