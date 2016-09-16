/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.core.configuration.security.jwt;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.cnes.regards.microservices.core.security.jwt.JWTAuthentication;
import fr.cnes.regards.microservices.core.security.jwt.JWTService;
import fr.cnes.regards.microservices.core.security.jwt.UserDetails;
import fr.cnes.regards.microservices.core.security.jwt.exception.InvalidJwtException;
import fr.cnes.regards.microservices.core.security.jwt.exception.MissingClaimException;

/**
 * @author msordi
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { JwtTestConfiguration.class })
public class JWTServiceTest {

    @Autowired
    private JWTService jwtService_;

    static final Logger LOG = LoggerFactory.getLogger(JWTServiceTest.class);

    @Test
    public void generateJWT() {
        String project = "PROJECT";
        String email = "marc.sordi@c-s.fr";
        String name = "Marc SORDI";
        String role = "USER";

        // Generate token
        String jwt = jwtService_.generateToken(project, email, name, role);
        LOG.debug("JWT = " + jwt);

        // Parse token and retrieve user information
        try {
            JWTAuthentication jwtAuth = jwtService_.parseToken(new JWTAuthentication(jwt));

            Assert.assertEquals(project, jwtAuth.getProject());

            UserDetails user = jwtAuth.getPrincipal();
            Assert.assertEquals(email, user.getEmail());
            Assert.assertEquals(name, user.getName());
        }
        catch (InvalidJwtException | MissingClaimException e) {
            String message = "JWT test error";
            Assert.fail(message);
            LOG.debug(message, e);
        }
    }

}