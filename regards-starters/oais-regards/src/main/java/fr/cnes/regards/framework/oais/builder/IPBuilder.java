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
package fr.cnes.regards.framework.oais.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import fr.cnes.regards.framework.oais.AbstractInformationPackage;
import fr.cnes.regards.framework.oais.ContentInformation;
import fr.cnes.regards.framework.oais.PreservationDescriptionInformation;
import fr.cnes.regards.framework.oais.urn.EntityType;

/**
 * Information package builder<br/>
 *
 * An {@link AbstractInformationPackage} contains :
 * <ul>
 * <li>An array of {@link ContentInformation} to describe related physical files</li>
 * <li>A {@link PreservationDescriptionInformation} object</li>
 * <li>A descriptive information object for any related metadata</li>
 * </ul>
 * <hr>
 * This builder helps to fill in these objects.
 * <br/>
 * <br/>
 * To create a {@link ContentInformation}, use {@link IPBuilder#getContentInformationBuilder()} to get current
 * builder.<br/>
 * Fill the object according to your need using this builder. Then, call {@link IPBuilder#addContentInformation()} to
 * build
 * current {@link ContentInformation} and initialize a new builder for a possible new {@link ContentInformation}.
 * <br/>
 * <br/>
 * To create {@link PreservationDescriptionInformation} object, use {@link IPBuilder#getPDIBuilder()}.
 * <br/>
 * <br/>
 * To define descriptive information, just call {@link IPBuilder#addDescriptiveInformation(String, Object)}.
 *
 * @author Marc Sordi
 *
 */
public abstract class IPBuilder<T extends AbstractInformationPackage<?>> implements IOAISBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IPBuilder.class);

    private final InformationPackagePropertiesBuilder ipPropertiesBuilder;

    protected final T ip;

    public IPBuilder(Class<T> clazz, EntityType ipType) {
        Assert.notNull(clazz, "Class is required");
        try {
            ip = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            String errorMessage = "Cannot instanciate information package";
            LOGGER.error(errorMessage, e);
            throw new IllegalArgumentException(errorMessage);
        }
        // Initialize information package properties wrapper
        ipPropertiesBuilder = new InformationPackagePropertiesBuilder(ipType);
    }

    @Override
    public T build() {
        ip.setProperties(ipPropertiesBuilder.build());
        return ip;
    }

    /**
     * @return builder for building <b>required</b> {@link ContentInformation}. At least one is required. When all
     *         information is set, you must call {@link IPBuilder#addContentInformation()} to effectively add the
     *         {@link ContentInformation} to this information package.
     */
    public ContentInformationBuilder getContentInformationBuilder() {
        return ipPropertiesBuilder.getContentInformationBuilder();
    }

    /**
     * Build current content information from the content information builder and add it to the set of content
     * informations of
     * this information package being built
     */
    public void addContentInformation() {
        ipPropertiesBuilder.addContentInformation();
    }

    /**
     * @return builder for <b>required</b> {@link PreservationDescriptionInformation}
     */
    public PDIBuilder getPDIBuilder() {
        return ipPropertiesBuilder.getPDIBuilder();
    }

    /**
     * Add <b>optional</b> descriptive information to the current information package.
     * @param key information key
     * @param value information value
     */
    public void addDescriptiveInformation(String key, Object value) {
        ipPropertiesBuilder.addDescriptiveInformation(key, value);
    }
}
