/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.amqp.test;
/*
 * LICENSE_PLACEHOLDER
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author svissier
 *
 */
@SpringBootApplication(scanBasePackages = { "fr.cnes.regards.framework.security.utils.jwt",
        "fr.cnes.regards.framework.amqp", "fr.cnes.regards.modules.project" })
public class Application {

    /**
     * main
     *
     * @param pArgs
     *            args
     * @throws InterruptedException
     *             Exception
     */
    public static void main(String[] pArgs) throws InterruptedException {
        SpringApplication.run(Application.class, pArgs);
    }

}