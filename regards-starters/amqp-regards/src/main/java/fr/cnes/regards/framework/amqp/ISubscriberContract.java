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
package fr.cnes.regards.framework.amqp;

import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.event.ISubscribable;

/**
 *
 * {@link ISubscriberContract} allows to subscribe to {@link ISubscribable} events. This interface represents the common
 * subscriber contract whether we are in a multitenant or an instance context.
 *
 * @author Sylvain Vissière-Guérinet
 * @author Sébastien Binda
 * @author Marc Sordi
 * @since 1.0-SNAPSHOT
 */
public interface ISubscriberContract {

    /**
     * Subscribe to this {@link ISubscribable} event
     *
     * @param <E>
     *            {@link ISubscribable} event
     * @param eventType
     *            {@link ISubscribable} event
     * @param receiver
     *            event {@link IHandler}
     */
    <E extends ISubscribable> void subscribeTo(Class<E> eventType, IHandler<E> receiver);

    /**
     * Unsubscribe from this {@link ISubscribable} event.
     *
     * @param <T>
     *            {@link ISubscribable} event
     * @param eventType
     *            {@link ISubscribable} event
     */
    <T extends ISubscribable> void unsubscribeFrom(Class<T> eventType);
}
