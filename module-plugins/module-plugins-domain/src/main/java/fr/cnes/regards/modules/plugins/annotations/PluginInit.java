/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.plugins.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * This annotation can be used to initialize a plugin. It must be used on a no-arg method. The method is call after
 * parameter injection.
 *
 * @author msordi
 * @since 1.0-SNAPSHOT
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PluginInit {
}