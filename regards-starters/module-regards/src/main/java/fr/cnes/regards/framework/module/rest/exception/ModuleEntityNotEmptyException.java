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
package fr.cnes.regards.framework.module.rest.exception;

/**
 *
 * Exception may occur when trying to delete a parent entity already linked to child entities.
 *
 * @deprecated use {@link EntityNotEmptyException}
 *
 * @author Marc Sordi
 *
 */
@Deprecated
public class ModuleEntityNotEmptyException extends ModuleException {

    private static final long serialVersionUID = 1642202168751223657L;

    public ModuleEntityNotEmptyException(final Long pEntityIdentifier, final Class<?> pEntityClass) {
        super(String.format("Entity of type \"%s\" with id \"%s\" is not empty and cannot be removed.",
                            pEntityClass.getName(), pEntityIdentifier));
    }
}