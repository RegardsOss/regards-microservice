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

import javax.annotation.Nullable;
import java.net.URL;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.oais.AbstractInformationPackage;
import fr.cnes.regards.framework.oais.ContentInformation;
import fr.cnes.regards.framework.oais.Event;
import fr.cnes.regards.framework.oais.InformationPackageProperties;
import fr.cnes.regards.framework.oais.PreservationDescriptionInformation;
import fr.cnes.regards.framework.oais.urn.DataType;
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

    public static final String MD5_ALGORITHM = "MD5";

    private static final Logger LOGGER = LoggerFactory.getLogger(IPBuilder.class);

    protected final T ip;

    private final InformationPackagePropertiesBuilder ipPropertiesBuilder;

    public IPBuilder(Class<T> clazz, EntityType ipType) {
        Assert.notNull(clazz, "Class is required");
        try {
            ip = clazz.newInstance();
            ip.setIpType(ipType);
            ipPropertiesBuilder = new InformationPackagePropertiesBuilder();
        } catch (InstantiationException | IllegalAccessException e) {
            String errorMessage = "Cannot instanciate information package";
            LOGGER.error(errorMessage, e);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Constructor using an existing information package so it can be updated
     * @param toBeUpdated
     */
    public IPBuilder(T toBeUpdated) {
        Assert.notNull(toBeUpdated, "Given information package cannot be null!");
        this.ip = toBeUpdated;
        ipPropertiesBuilder = new InformationPackagePropertiesBuilder(toBeUpdated.getProperties());
    }

    @Override
    public T build() {
        return build(ipPropertiesBuilder.build());
    }

    /**
     * @param properties
     * @return the built information package thanks to the given properties
     */
    public T build(InformationPackageProperties properties) {
        ip.setProperties(properties);
        return ip;
    }

    /**
     * Set optional feature bounding box an CRS
     * @param bbox bounding box
     * @param crs coordinate reference system (default WGS 84)
     */
    public void setBbox(Double[] bbox, String crs) {
        ip.setBbox(bbox);
        ip.setCrs(crs);
    }

    /**
     * Set optional feature bounding box
     * @param bbox
     */
    public void setBbox(Double[] bbox) {
        setBbox(bbox, null);
    }

    /**
     * Set optional geometry. Use {@link IGeometry} to build a valid geometry.
     * @param geometry
     */
    public void setGeometry(IGeometry geometry) {
        ip.setGeometry(geometry);
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
     * informations of this information package being built.<br/>
     * This method has to be called after filling in each {@link ContentInformation} using
     * {@link IPBuilder#getContentInformationBuilder()}.
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

    /**
     * Add tags into context information
     * @param tags list of tags
     */
    public void addTags(String... tags) {
        ipPropertiesBuilder.addTags(tags);
    }

    /**
     * Add link into context information
     * @param key link key
     * @param value link value
     */
    public void addContextInformation(String key, Object value) {
        ipPropertiesBuilder.addContextInformation(key, value);
    }

    /**
     * Add reference information to the information package
     * @param key
     * @param value
     */
    public void addReferenceInformation(String key, String value) {
        ipPropertiesBuilder.addReferenceInformation(key, value);
    }

    /**
     * Add additional provenance information to the information package
     * @param key
     * @param value
     */
    public void addAdditionalProvenanceInformation(String key, Object value) {
        ipPropertiesBuilder.addAdditionalProvenanceInformation(key, value);
    }

    /**
     * Set the facility to the information package
     * @param facility
     */
    public void setFacility(String facility) {
        ipPropertiesBuilder.setFacility(facility);
    }

    /**
     * Set the instrument to the information package
     * @param instrument
     */
    public void setInstrument(String instrument) {
        ipPropertiesBuilder.setInstrument(instrument);
    }

    /**
     * Set the filter to the information package
     * @param filter
     */
    public void setFilter(String filter) {
        ipPropertiesBuilder.setFilter(filter);
    }

    /**
     * Set the detector to the information package
     * @param detector
     */
    public void setDetector(String detector) {
        ipPropertiesBuilder.setDetector(detector);
    }

    /**
     * Set the proposal to the information package
     * @param proposal
     */
    public void setProposal(String proposal) {
        ipPropertiesBuilder.setProposal(proposal);
    }

    /**
     * Add provenance information events to the information package
     * @param events
     */
    public void addProvenanceInformationEvents(Event... events) {
        ipPropertiesBuilder.addProvenanceInformationEvents(events);
    }

    /**
     * Add a provenance information event to the information package thanks to the given parameters
     * @param type
     * @param comment
     * @param date
     */
    public void addProvenanceInformationEvent(@Nullable String type, String comment, OffsetDateTime date) {
        ipPropertiesBuilder.addProvenanceInformationEvent(type, comment, date);
    }

    /**
     * Add a provenance information event to the information package thanks to the given parameters
     * @param comment
     * @param date
     */
    public void addProvenanceInformationEvent(String comment, OffsetDateTime date) {
        ipPropertiesBuilder.addProvenanceInformationEvent(comment, date);
    }

    /**
     * Add a provenance information event to the information package thanks to the given parameter
     * @param comment
     */
    public void addProvenanceInformationEvent(String comment) {
        ipPropertiesBuilder.addProvenanceInformationEvent(comment);
    }

    /**
     * Add fixity information to the information package thanks to the given parameters
     * @param key
     * @param value
     */
    public void addFixityInformation(String key, Object value) {
        ipPropertiesBuilder.addFixityInformation(key, value);
    }

    /**
     * Set the access right informtaion to the information package thanks to the given parameters
     * @param licence
     * @param dataRights
     * @param publicReleaseDate
     */
    public void setAccessRightInformation(String licence, String dataRights,
            @Nullable OffsetDateTime publicReleaseDate) {
        ipPropertiesBuilder.setAccessRightInformation(licence, dataRights, publicReleaseDate);
    }

    /**
     * Set access right information to the information package thanks to the given parameter
     * @param dataRights
     */
    public void setAccessRightInformation(String dataRights) {
        ipPropertiesBuilder.setAccessRightInformation(dataRights);
    }

    /**
     * Set the data object to the underlying content information builder thanks to the given parameters.
     * @param dataType
     * @param url
     * @param filename
     * @param algorithm
     * @param checksum
     * @param fileSize
     */
    public void setDataObject(DataType dataType, URL url, String filename, String algorithm, String checksum,
            Long fileSize) {
        ipPropertiesBuilder.setDataObject(dataType, url, filename, algorithm, checksum, fileSize);
    }

    /**
     * Set the data object to the underlying content information builder thanks to the given parameters.
     * @param dataType
     * @param filename
     * @param algorithm
     * @param checksum
     * @param fileSize
     */
    public void setDataObject(DataType dataType, Path filePath, String filename, String algorithm, String checksum,
            Long fileSize) {
        ipPropertiesBuilder.setDataObject(dataType, filePath, filename, algorithm, checksum, fileSize);
    }

    /**
     * Set the data object to the underlying content information builder thanks to the given parameters.
     * @param dataType
     * @param url
     * @param filename
     * @param checksum
     * @param fileSize
     */
    public void setDataObject(DataType dataType, URL url, String filename, String checksum, Long fileSize) {
        ipPropertiesBuilder.setDataObject(dataType, url, filename, checksum, fileSize);
    }

    /**
     * Set the data object to the underlying content information builder thanks to the given parameters.
     * @param dataType
     * @param filename
     * @param checksum
     * @param fileSize
     */
    public void setDataObject(DataType dataType, Path filePath, String filename, String checksum, Long fileSize) {
        ipPropertiesBuilder.setDataObject(dataType, filePath, filename, checksum, fileSize);
    }

    /**
     * Set the data object to the underlying content information builder thanks to the given parameters.
     * @param dataType
     * @param url
     * @param algorithm
     * @param checksum
     */
    public void setDataObject(DataType dataType, URL url, String algorithm, String checksum) {
        ipPropertiesBuilder.setDataObject(dataType, url, algorithm, checksum);
    }

    /**
     * Set the data object to the underlying content information builder thanks to the given parameters.
     * @param dataType
     * @param algorithm
     * @param checksum
     */
    public void setDataObject(DataType dataType, Path filePath, String algorithm, String checksum) {
        ipPropertiesBuilder.setDataObject(dataType, filePath, algorithm, checksum);
    }

    /**
     * Set the data object to the underlying content information builder thanks to the given parameters.
     * @param dataType
     * @param url
     * @param checksum
     */
    public void setDataObject(DataType dataType, URL url, String checksum) {
        ipPropertiesBuilder.setDataObject(dataType, url, checksum);
    }

    /**
     * Set the data object to the underlying content information builder thanks to the given parameters.
     * @param dataType
     * @param checksum
     */
    public void setDataObject(DataType dataType, Path filePath, String checksum) {
        ipPropertiesBuilder.setDataObject(dataType, filePath, checksum);
    }

    /**
     * Set the syntax to the information package thanks to the given parameters
     * @param mimeName
     * @param mimeDescription
     * @param mimeType
     */
    public void setSyntax(String mimeName, String mimeDescription, String mimeType) {
        ipPropertiesBuilder.setSyntax(mimeName, mimeDescription, mimeType);
    }

    /**
     * Set syntax and semantic to the information package thanks to the given parameters
     * @param mimeName
     * @param mimeDescription
     * @param mimeType
     * @param semanticDescription
     */
    public void setSyntaxAndSemantic(String mimeName, String mimeDescription, String mimeType,
            String semanticDescription) {
        ipPropertiesBuilder.setSyntaxAndSemantic(mimeName, mimeDescription, mimeType, semanticDescription);
    }

    /**
     * Add software environment property to the information package thanks to the given parameters
     * @param key
     * @param value
     */
    public void addSoftwareEnvironmentProperty(String key, Object value) {
        ipPropertiesBuilder.addSoftwareEnvironmentProperty(key, value);
    }

    /**
     * Add hardware environment property to the information package thanks to the given parameters
     * @param key
     * @param value
     */
    public void addHardwareEnvironmentProperty(String key, Object value) {
        ipPropertiesBuilder.addHardwareEnvironmentProperty(key, value);
    }

    /**
     * Add IP events
     * @param events events to add
     */
    public void addEvents(Event... events) {
        Assert.notEmpty(events, "At least one event is required if this method is called");
        getPDIBuilder().addProvenanceInformationEvents(events);
    }

    /**
     * Add IP events
     * @param events events to add
     */
    public void addEvents(Collection<Event> events) {
        Assert.notNull(events, "Collection of events cannot be null");
        addEvents(events.toArray(new Event[events.size()]));
    }

    /**
     * Add an IP event
     * @param type optional event type key (may be null)
     * @param comment event comment
     * @param date event date
     */
    public void addEvent(@Nullable String type, String comment, OffsetDateTime date) {
        Event event = new Event();
        event.setType(type);
        event.setComment(comment);
        event.setDate(date);
        addEvents(event);
    }

    /**
     * Add IP event
     * @param comment event comment
     * @param date event date
     */
    public void addEvent(String comment, OffsetDateTime date) {
        addEvent(null, comment, date);
    }

    /**
     * Add IP event
     * @param comment event comment
     */
    public void addEvent(String comment) {
        addEvent(null, comment, OffsetDateTime.now());
    }

    /**
     * Remove tags from the information package
     * @param tags
     */
    public void removeTags(String... tags) {
        ipPropertiesBuilder.removeTags(tags);
    }
}
