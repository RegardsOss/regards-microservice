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
package fr.cnes.regards.framework.modules.plugins.domain.event;

import java.util.Set;

import fr.cnes.regards.framework.amqp.event.Event;
import fr.cnes.regards.framework.amqp.event.ISubscribable;
import fr.cnes.regards.framework.amqp.event.Target;

/**
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Event(target = Target.ALL)
public class BroadcastPluginConfEvent extends AbstractPluginConfEvent implements ISubscribable {

    public BroadcastPluginConfEvent(Long pPluginConfId, PluginServiceAction pAction, Set<String> pPluginTypes) {
        super(pPluginConfId, pAction, pPluginTypes);
    }

    public BroadcastPluginConfEvent() {
    }
}