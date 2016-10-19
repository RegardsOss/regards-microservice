/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.core.amqp.domain;

/**
 * @param <T>
 *            Type of Event you are handling
 *
 *
 * @author svissier
 *
 */
public interface IHandler<T> {

    public void handle(TenantWrapper<T> pT);
}