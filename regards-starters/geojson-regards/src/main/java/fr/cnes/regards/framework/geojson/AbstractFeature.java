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
package fr.cnes.regards.framework.geojson;

import java.io.Serializable;

import fr.cnes.regards.framework.geojson.geometry.GeometryCollection;
import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.geojson.geometry.LineString;
import fr.cnes.regards.framework.geojson.geometry.MultiLineString;
import fr.cnes.regards.framework.geojson.geometry.MultiPoint;
import fr.cnes.regards.framework.geojson.geometry.MultiPolygon;
import fr.cnes.regards.framework.geojson.geometry.Unlocated;

/**
 * RFC 7946 -August 2016<br/>
 * GeoJson base feature representation<br/>
 *
 * This is the base class for implementing GeoJson feature. Extend it to create your own.
 *
 * @param <P> wrapper for properties
 * @param <ID> is an optional Feature identifier of type {@link String} or {@link Number}
 *
 * @author Marc Sordi
 *
 */
public abstract class AbstractFeature<P, ID extends Serializable> extends AbstractGeoJsonObject {

    /**
     * ID MUST be a {@link String} or a {@link Number}
     */
    protected ID id;

    /**
     * One of the available GeoJson geometry plus the custom unlocated one :
     * <ul>
     * <li>{@link Point}</li>
     * <li>{@link MultiPoint}</li>
     * <li>{@link LineString}</li>
     * <li>{@link MultiLineString}</li>
     * <li>{@link Polygon}</li>
     * <li>{@link MultiPolygon}</li>
     * <li>{@link GeometryCollection}</li>
     * <li>{@link Unlocated}</li>
     * </ul>
     */
    protected IGeometry geometry = IGeometry.unlocated();

    protected P properties;

    public AbstractFeature() {
        super(GeoJsonType.FEATURE);
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public IGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(IGeometry geometry) {
        this.geometry = geometry;
    }

    public P getProperties() {
        return properties;
    }

    public void setProperties(P properties) {
        this.properties = properties;
    }
}
