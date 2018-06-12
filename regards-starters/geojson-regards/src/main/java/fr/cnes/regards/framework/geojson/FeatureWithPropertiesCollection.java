/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * This class represents a default GeoJson feature collection containing default {@link Feature}
 * identifier.<br/>
 * You can define your own extending {@link AbstractFeatureCollection}.
 *
 * @author Sébastien Binda
 *
 */
public class FeatureWithPropertiesCollection extends FeatureCollection {

    protected Map<String, Object> properties;

    public FeatureWithPropertiesCollection() {
        properties = new HashMap<>();
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }
}
