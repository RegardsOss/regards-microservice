package fr.cnes.regards.framework.oais.urn.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;

import fr.cnes.regards.framework.oais.urn.OaisUniformResourceName;

/**
 * Annotation allowing to certifate that a String is a {@link OaisUniformResourceName} thanks to
 * {@link RegardsOaisUrnAsStringValidator}
 * @author Sylvain Vissiere-Guerinet
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Constraint(validatedBy = RegardsOaisUrnAsStringValidator.class)
@Documented
public @interface RegardsOaisUrnAsString {

}
