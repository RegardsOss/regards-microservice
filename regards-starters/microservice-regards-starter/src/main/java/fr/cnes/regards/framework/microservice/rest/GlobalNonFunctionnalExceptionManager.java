/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.microservice.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import fr.cnes.regards.framework.microservice.manager.MaintenanceManager;
import fr.cnes.regards.framework.module.rest.representation.ServerErrorResponse;
import fr.cnes.regards.framework.security.utils.jwt.JWTService;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@RestControllerAdvice(annotations = RestController.class)
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalNonFunctionnalExceptionManager {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalNonFunctionnalExceptionManager.class);

    /**
     * Exception handler catching any exception that are not already handled
     *
     * @param pException
     *            exception thrown
     * @return response
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ServerErrorResponse> nonFunctionnalException(Exception pException) {
        LOGGER.error("Unexpected server error", pException);
        String tenant = JWTService.getActualTenant();
        MaintenanceManager.setMaintenance(tenant);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ServerErrorResponse(pException.getMessage()));
    }

}
